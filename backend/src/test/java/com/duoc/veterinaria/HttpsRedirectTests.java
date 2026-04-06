package com.duoc.veterinaria;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "server.ssl.enabled=true",
        "app.http.port=8080",
        "server.port=8384"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HttpsRedirectTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void httpRequestRedirectsToConfiguredHttpsPort() throws Exception {
        mockMvc.perform(get("/login").with(request -> {
                    request.setSecure(false);
                    request.setScheme("http");
                    request.setServerName("localhost");
                    request.setServerPort(8080);
                    return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "https://localhost:8384/login"));
    }
}
