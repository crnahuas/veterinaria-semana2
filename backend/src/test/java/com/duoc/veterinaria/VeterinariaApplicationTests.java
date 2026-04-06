package com.duoc.veterinaria;

import com.duoc.veterinaria.model.Paciente;
import com.duoc.veterinaria.repository.CitaRepository;
import com.duoc.veterinaria.repository.FacturaRepository;
import com.duoc.veterinaria.repository.PacienteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VeterinariaApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void loginReturnsJwtForValidCredentials() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void privateApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void veterinarioCannotCreatePatients() throws Exception {
        String token = loginAndGetToken("veterinario", "vet123");

        mockMvc.perform(post("/api/pacientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Luna",
                                  "especie": "Perro",
                                  "raza": "Beagle",
                                  "edad": 4,
                                  "nombreDueno": "Carla Soto"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void recepcionistaCanCreatePatientsAndPersistThem() throws Exception {
        long initialCount = pacienteRepository.count();
        String token = loginAndGetToken("recepcionista", "vet123");

        MvcResult result = mockMvc.perform(post("/api/pacientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Milo",
                                  "especie": "Gato",
                                  "raza": "Mestizo",
                                  "edad": 2,
                                  "nombreDueno": "Pedro Rojas"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Milo"))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        Long pacienteId = responseJson.get("id").asLong();

        assertThat(pacienteRepository.count()).isEqualTo(initialCount + 1);

        Paciente persisted = pacienteRepository.findById(pacienteId).orElseThrow();
        assertThat(persisted.getNombre()).isEqualTo("Milo");
        assertThat(persisted.getEspecie()).isEqualTo("Gato");
        assertThat(persisted.getRaza()).isEqualTo("Mestizo");
        assertThat(persisted.getEdad()).isEqualTo(2);
        assertThat(persisted.getNombreDueno()).isEqualTo("Pedro Rojas");
    }

    @Test
    void corsPreflightAllowsConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/pacientes")
                        .header("Origin", "http://localhost:5500")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5500"))
                .andExpect(header().string("Vary", containsString("Origin")));
    }

    @Test
    void authenticatedUsersCanListVeterinarios() throws Exception {
        String token = loginAndGetToken("admin", "admin123");

        mockMvc.perform(get("/api/usuarios/veterinarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("veterinario"));
    }

    @Test
    void recepcionistaCanDeleteCitas() throws Exception {
        long initialCitas = citaRepository.count();
        long initialFacturas = facturaRepository.count();
        String token = loginAndGetToken("recepcionista", "vet123");
        Long pacienteId = createPaciente(token, "Nina");
        Long citaId = createCita(token, pacienteId, "Control anual");
        Long facturaId = createFactura(token, pacienteId, citaId);

        assertThat(citaRepository.count()).isEqualTo(initialCitas + 1);
        assertThat(facturaRepository.count()).isEqualTo(initialFacturas + 1);

        mockMvc.perform(delete("/api/citas/{id}", citaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(citaRepository.findById(citaId)).isEmpty();
        assertThat(facturaRepository.findById(facturaId)).isEmpty();
        assertThat(citaRepository.count()).isEqualTo(initialCitas);
        assertThat(facturaRepository.count()).isEqualTo(initialFacturas);
    }

    @Test
    void recepcionistaCanDeletePatientsAndCascadeAppointments() throws Exception {
        long initialPacientes = pacienteRepository.count();
        long initialCitas = citaRepository.count();
        long initialFacturas = facturaRepository.count();
        String token = loginAndGetToken("recepcionista", "vet123");
        Long pacienteId = createPaciente(token, "Toby");
        Long citaId = createCita(token, pacienteId, "Vacunación");
        Long facturaId = createFactura(token, pacienteId, citaId);

        mockMvc.perform(delete("/api/pacientes/{id}", pacienteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(pacienteRepository.findById(pacienteId)).isEmpty();
        assertThat(citaRepository.findById(citaId)).isEmpty();
        assertThat(facturaRepository.findById(facturaId)).isEmpty();
        assertThat(pacienteRepository.count()).isEqualTo(initialPacientes);
        assertThat(citaRepository.count()).isEqualTo(initialCitas);
        assertThat(facturaRepository.count()).isEqualTo(initialFacturas);
    }

    @Test
    void recepcionistaCanGenerateDetailedInvoicesForVisits() throws Exception {
        String token = loginAndGetToken("recepcionista", "vet123");
        Long pacienteId = createPaciente(token, "Lola");
        Long citaId = createCita(token, pacienteId, "Chequeo general");

        mockMvc.perform(post("/api/facturas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "citaId": %d,
                                  "servicios": [
                                    {
                                      "descripcion": "Consulta general",
                                      "cantidad": 1,
                                      "precioUnitario": 25000
                                    }
                                  ],
                                  "medicamentos": [
                                    {
                                      "descripcion": "Antibiotico",
                                      "cantidad": 2,
                                      "precioUnitario": 8500
                                    }
                                  ],
                                  "cargosAdicionales": [
                                    {
                                      "descripcion": "Insumos clinicos",
                                      "cantidad": 1,
                                      "precioUnitario": 5000
                                    }
                                  ],
                                  "observaciones": "Paciente en observacion"
                                }
                                """.formatted(pacienteId, citaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.citaId").value(citaId))
                .andExpect(jsonPath("$.servicios", hasSize(1)))
                .andExpect(jsonPath("$.medicamentos", hasSize(1)))
                .andExpect(jsonPath("$.cargosAdicionales", hasSize(1)))
                .andExpect(jsonPath("$.subtotalServicios").value(25000))
                .andExpect(jsonPath("$.subtotalMedicamentos").value(17000))
                .andExpect(jsonPath("$.subtotalAdicionales").value(5000))
                .andExpect(jsonPath("$.total").value(47000));
    }

    @Test
    void veterinarioCannotGenerateInvoices() throws Exception {
        String token = loginAndGetToken("veterinario", "vet123");
        Long pacienteId = createPaciente(loginAndGetToken("recepcionista", "vet123"), "Mora");
        Long citaId = createCita(loginAndGetToken("recepcionista", "vet123"), pacienteId, "Control");

        mockMvc.perform(post("/api/facturas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "citaId": %d,
                                  "servicios": [
                                    {
                                      "descripcion": "Consulta general",
                                      "cantidad": 1,
                                      "precioUnitario": 25000
                                    }
                                  ]
                                }
                                """.formatted(pacienteId, citaId)))
                .andExpect(status().isForbidden());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = json.get("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private Long createPaciente(String token, String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/pacientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "%s",
                                  "especie": "Perro",
                                  "raza": "Mestizo",
                                  "edad": 3,
                                  "nombreDueno": "Dueño %s"
                                }
                                """.formatted(nombre, nombre)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createCita(String token, Long pacienteId, String motivo) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/citas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "fechaHora": "2099-01-01T10:00:00",
                                  "motivo": "%s",
                                  "veterinarioAsignado": "veterinario"
                                }
                                """.formatted(pacienteId, motivo)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private Long createFactura(String token, Long pacienteId, Long citaId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/facturas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "citaId": %d,
                                  "servicios": [
                                    {
                                      "descripcion": "Consulta general",
                                      "cantidad": 1,
                                      "precioUnitario": 20000
                                    }
                                  ],
                                  "medicamentos": [
                                    {
                                      "descripcion": "Antinflamatorio",
                                      "cantidad": 1,
                                      "precioUnitario": 6000
                                    }
                                  ]
                                }
                                """.formatted(pacienteId, citaId)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
