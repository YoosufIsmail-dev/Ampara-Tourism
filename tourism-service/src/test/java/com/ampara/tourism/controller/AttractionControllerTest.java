package com.ampara.tourism.controller;

import com.ampara.tourism.entity.Attraction;
import com.ampara.tourism.repository.AttractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttractionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttractionRepository attractionRepository;

    @InjectMocks
    private AttractionController attractionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(attractionController).build();
    }

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