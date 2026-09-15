package com.ampara.tourism.controller;

import com.ampara.tourism.entity.TouristPlace;
import com.ampara.tourism.repository.TouristPlaceRepository;
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

class TouristPlaceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TouristPlaceRepository placeRepository;

    @InjectMocks
    private TouristPlaceController touristPlaceController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(touristPlaceController).build();
    }

    private TouristPlace samplePlace() {
        TouristPlace p = new TouristPlace();
        p.setId(1L);
        p.setName("Arugam Bay Main Point");
        p.setTamilName("ஆரூகம் பே மெயின் பாயின்ட்");
        p.setCategory("Surfing");
        p.setTown("Arugam Bay");
        p.setLatitude(6.8390);
        p.setLongitude(81.8378);
        return p;
    }

    @Test
    void listReturnsAllPlacesWhenNoFilterGiven() throws Exception {
        when(placeRepository.findAll()).thenReturn(List.of(samplePlace()));

        mockMvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arugam Bay Main Point"))
                .andExpect(jsonPath("$[0].category").value("Surfing"));
    }

    @Test
    void byTownFiltersUsingRepository() throws Exception {
        when(placeRepository.findByTownIgnoreCase("Arugam Bay")).thenReturn(List.of(samplePlace()));

        mockMvc.perform(get("/api/places/town/Arugam Bay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].town").value("Arugam Bay"));
    }

    @Test
    void getReturns404WhenPlaceMissing() throws Exception {
        when(placeRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/places/99"))
                .andExpect(status().isNotFound());
    }
}