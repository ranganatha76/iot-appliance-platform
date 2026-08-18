package com.example.iot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplianceWorkflowIntegrationTest {
    @Autowired MockMvc mockMvc;

    /** Verifies that the browser-facing root endpoint exposes API discovery data. */
    @Test void rootReturnsApiDiscovery() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("IoT Appliance Platform"))
                .andExpect(jsonPath("$.endpoints.appliances").value("/api/appliances"));
    }

    /** Verifies the API workflow from registration through collection to report aggregation. */
    @Test void applianceCanBeRegisteredCollectedAndReported() throws Exception {
        mockMvc.perform(post("/api/appliances").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Kitchen fridge\",\"type\":\"refrigerator\",\"vendor\":\"acme\",\"collectionIntervalSeconds\":60}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber());
        mockMvc.perform(post("/api/collections/run")).andExpect(status().isOk()).andExpect(jsonPath("$.metricsStored").value(2));
        String start = Instant.now().minusSeconds(60).toString(); String end = Instant.now().plusSeconds(60).toString();
        mockMvc.perform(get("/api/reports").param("start", start).param("end", end)).andExpect(status().isOk()).andExpect(jsonPath("$.metrics.length()").value(2)).andExpect(jsonPath("$.metrics[0].samples").value(1));
    }

    /** Verifies that invalid resource IDs and time ranges return client errors. */
    @Test void invalidRequestsReturnClientErrors() throws Exception {
        mockMvc.perform(delete("/api/appliances/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Appliance not found: 999"));
        Instant instant = Instant.now();
        mockMvc.perform(get("/api/metrics").param("start", instant.toString()).param("end", instant.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("start must be before end"));
    }
}