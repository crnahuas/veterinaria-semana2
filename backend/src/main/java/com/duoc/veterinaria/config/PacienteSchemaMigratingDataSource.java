package com.duoc.veterinaria.config;

import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacienteSchemaMigratingDataSource extends DelegatingDataSource {

    private static final String TABLE_NAME = "pacientes";
    private static final String TARGET_COLUMN = "nombre_dueno";
    private static final Set<String> KNOWN_LEGACY_COLUMNS = Set.of(
            "nombre_dueño",
            "nombre_due�o",
            "nombre_dueÃ±o"
    );

    private final AtomicBoolean migrationChecked = new AtomicBoolean(false);
    private final Object migrationLock = new Object();

    public PacienteSchemaMigratingDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        migrateIfNeeded();
        return super.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        migrateIfNeeded();
        return super.getConnection(username, password);
    }

    private void migrateIfNeeded() throws SQLException {
        if (migrationChecked.get()) {
            return;
        }

        synchronized (migrationLock) {
            if (migrationChecked.get()) {
                return;
            }

            try (Connection connection = obtainTargetDataSource().getConnection()) {
                migratePacienteOwnerColumn(connection);
            }

            migrationChecked.set(true);
        }
    }

    private void migratePacienteOwnerColumn(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        String tableName = findTableName(metadata, connection.getCatalog(), connection.getSchema());

        if (tableName == null) {
            return;
        }

        List<String> columns = findColumns(metadata, connection.getCatalog(), connection.getSchema(), tableName);
        if (columns.isEmpty()) {
            return;
        }

        boolean targetExists = columns.stream().anyMatch(TARGET_COLUMN::equalsIgnoreCase);
        Optional<String> legacyColumn = columns.stream()
                .filter(column -> !TARGET_COLUMN.equalsIgnoreCase(column))
                .filter(this::isLegacyOwnerColumn)
                .findFirst();

        if (!targetExists && legacyColumn.isEmpty()) {
            return;
        }

        String tableIdentifier = identifierExpression(metadata, tableName);
        String targetIdentifier = identifierExpression(metadata, TARGET_COLUMN);

        try (Statement statement = connection.createStatement()) {
            if (!targetExists) {
                statement.execute(
                        "ALTER TABLE " + tableIdentifier +
                                " ADD COLUMN " + targetIdentifier + " VARCHAR(100)"
                );
            }

            if (legacyColumn.isPresent()) {
                String legacyIdentifier = identifierExpression(metadata, legacyColumn.get());

                statement.execute(
                        "UPDATE " + tableIdentifier +
                                " SET " + targetIdentifier + " = " + legacyIdentifier +
                                " WHERE " + targetIdentifier + " IS NULL"
                );

                statement.execute(
                        "ALTER TABLE " + tableIdentifier +
                                " DROP COLUMN " + legacyIdentifier
                );
            }

            if (!hasNullOwnerNames(statement, tableIdentifier, targetIdentifier)) {
                applyNotNullConstraint(statement, metadata, tableIdentifier, targetIdentifier);
            }
        }
    }

    private String findTableName(DatabaseMetaData metadata, String catalog, String schema) throws SQLException {
        try (ResultSet tables = metadata.getTables(catalog, schema, null, new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (TABLE_NAME.equalsIgnoreCase(tableName)) {
                    return tableName;
                }
            }
        }

        try (ResultSet tables = metadata.getTables(catalog, null, null, new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (TABLE_NAME.equalsIgnoreCase(tableName)) {
                    return tableName;
                }
            }
        }

        return null;
    }

    private List<String> findColumns(DatabaseMetaData metadata, String catalog, String schema, String tableName) throws SQLException {
        List<String> columns = readColumns(metadata, catalog, schema, tableName);
        if (!columns.isEmpty()) {
            return columns;
        }

        columns = readColumns(metadata, catalog, null, tableName);
        if (!columns.isEmpty()) {
            return columns;
        }

        columns = readColumns(metadata, null, schema, tableName);
        if (!columns.isEmpty()) {
            return columns;
        }

        return readColumns(metadata, null, null, tableName);
    }

    private List<String> readColumns(DatabaseMetaData metadata, String catalog, String schema, String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();
        try (ResultSet resultSet = metadata.getColumns(catalog, schema, tableName, null)) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("COLUMN_NAME"));
            }
        }
        return columns;
    }

    private boolean isLegacyOwnerColumn(String columnName) {
        if (KNOWN_LEGACY_COLUMNS.contains(columnName)) {
            return true;
        }

        String normalized = normalizeIdentifier(columnName);
        return "nombre_dueno".equals(normalized)
                || normalized.startsWith("nombre_due") && normalized.endsWith("o");
    }

    private String normalizeIdentifier(String identifier) {
        String normalized = Normalizer.normalize(identifier, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_");

        if (normalized.startsWith("_")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("_")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String identifierExpression(DatabaseMetaData metadata, String identifier) throws SQLException {
        if (identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            if (metadata.storesUpperCaseIdentifiers()) {
                return identifier.toUpperCase(Locale.ROOT);
            }
            if (metadata.storesLowerCaseIdentifiers()) {
                return identifier.toLowerCase(Locale.ROOT);
            }
            return identifier;
        }

        String quote = metadata.getIdentifierQuoteString();
        return quote == null || quote.isBlank() ? identifier : quote.trim() + identifier + quote.trim();
    }

    private boolean hasNullOwnerNames(Statement statement, String tableIdentifier, String targetIdentifier) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(
                "SELECT COUNT(*) FROM " + tableIdentifier + " WHERE " + targetIdentifier + " IS NULL"
        )) {
            resultSet.next();
            return resultSet.getLong(1) > 0;
        }
    }

    private void applyNotNullConstraint(
            Statement statement,
            DatabaseMetaData metadata,
            String tableIdentifier,
            String targetIdentifier
    ) throws SQLException {
        String databaseProduct = metadata.getDatabaseProductName().toLowerCase(Locale.ROOT);

        if (databaseProduct.contains("mysql")) {
            statement.execute(
                    "ALTER TABLE " + tableIdentifier +
                            " MODIFY " + targetIdentifier + " VARCHAR(100) NOT NULL"
            );
            return;
        }

        statement.execute(
                "ALTER TABLE " + tableIdentifier +
                        " ALTER COLUMN " + targetIdentifier + " SET NOT NULL"
        );
    }
}
