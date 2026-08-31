package com.valorant.devopshub.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Endpoint tests for the JSON content API that the static frontend depends
 * on. These guard against accidentally breaking the data contract the
 * JavaScript pages rely on.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContentApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void agentsEndpointReturnsAtLeastTenAgents() throws Exception {
        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].abilities").isArray());
    }

    @Test
    void mapsEndpointReturnsMaps() throws Exception {
        mockMvc.perform(get("/api/maps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].sites").isArray());
    }

    @Test
    void weaponsEndpointReturnsWeapons() throws Exception {
        mockMvc.perform(get("/api/weapons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].magazineSize").exists());
    }

    @Test
    void patchNotesEndpointReturnsEntries() throws Exception {
        mockMvc.perform(get("/api/patch-notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].version").exists());
    }

    @Test
    void infoEndpointReturnsRuntimeMetadata() throws Exception {
        mockMvc.perform(get("/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationVersion").exists())
                .andExpect(jsonPath("$.javaVersion").exists())
                .andExpect(jsonPath("$.environment").exists())
                .andExpect(jsonPath("$.hostname").exists());
    }
}
