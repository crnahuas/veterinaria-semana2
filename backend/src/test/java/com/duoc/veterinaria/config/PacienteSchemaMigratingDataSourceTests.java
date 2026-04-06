package com.duoc.veterinaria.config;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PacienteSchemaMigratingDataSourceTests {

    @Test
    void migratesLegacyOwnerColumnToAsciiName() throws Exception {
        JdbcDataSource delegate = new JdbcDataSource();
        delegate.setURL("jdbc:h2:mem:paciente-migration-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
        delegate.setUser("sa");
        delegate.setPassword("");

        createLegacySchema(delegate);

        DataSource dataSource = new PacienteSchemaMigratingDataSource(delegate);
        try (Connection ignored = dataSource.getConnection()) {
            // Trigger migration before JPA would use the connection.
        }

        try (Connection connection = delegate.getConnection()) {
            assertThat(columnNames(connection))
                    .contains("nombre_dueno")
                    .doesNotContain("nombre_dueño");

            assertThat(isNotNullable(connection, "nombre_dueno")).isTrue();

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT nombre_dueno FROM pacientes WHERE nombre = ?"
            )) {
                statement.setString(1, "Luna");

                try (ResultSet resultSet = statement.executeQuery()) {
                    assertThat(resultSet.next()).isTrue();
                    assertThat(resultSet.getString(1)).isEqualTo("Carla Soto");
                }
            }
        }
    }

    private void createLegacySchema(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE pacientes (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        nombre VARCHAR(100) NOT NULL,
                        especie VARCHAR(50) NOT NULL,
                        raza VARCHAR(100),
                        edad INT,
                        "nombre_dueño" VARCHAR(100) NOT NULL
                    )
                    """);

            statement.execute("""
                    INSERT INTO pacientes (nombre, especie, raza, edad, "nombre_dueño")
                    VALUES ('Luna', 'Perro', 'Beagle', 4, 'Carla Soto')
                    """);
        }
    }

    private List<String> columnNames(Connection connection) throws SQLException {
        List<String> names = new ArrayList<>();
        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet resultSet = metadata.getColumns(connection.getCatalog(), connection.getSchema(), "pacientes", null)) {
            while (resultSet.next()) {
                names.add(resultSet.getString("COLUMN_NAME"));
            }
        }

        return names;
    }

    private boolean isNotNullable(Connection connection, String columnName) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet resultSet = metadata.getColumns(connection.getCatalog(), connection.getSchema(), "pacientes", columnName)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls;
        }
    }
}
