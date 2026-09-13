package com.ampara.tourism.controller;

import com.ampara.tourism.entity.Attraction;
import com.ampara.tourism.repository.AttractionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttractionController.class)
@AutoConfigureMockMvc(addFilters = false) // security is tested separately; this exercises controller logic only
class AttractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttractionRepository attractionRepository;

    @Test
    void listReturnsAllAttractionsWhenNoFilterGiven() throws Exception {
        Attraction galOya = new Attraction("Gal Oya National Park", "Boat safari", "Ampara", "Wildlife",
                7.2167, 81.3667, "May-September", 2, List.of("Boat safari"));

        when(attractionRepository.findAll()).thenReturn(List.of(galOya));

        mockMvc.perform(get("/api/attractions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gal Oya National Park"))
                .andExpect(jsonPath("$[0].district").value("Ampara"));
    }

    @Test
    void listFiltersByCategoryWhenProvided() throws Exception {
        Attraction arugamBay = new Attraction("Arugam Bay", "Surf beach", "Ampara", "Beach",
                6.8404, 81.8368, "April-October", 2, List.of("Surfing"));

        when(attractionRepository.findByCategoryIgnoreCase("Beach")).thenReturn(List.of(arugamBay));

        mockMvc.perform(get("/api/attractions").param("category", "Beach"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arugam Bay"));
    }

    @Test
    void getReturns404WhenAttractionMissing() throws Exception {
        when(attractionRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/attractions/999"))
                .andExpect(status().isNotFound());
    }
}
