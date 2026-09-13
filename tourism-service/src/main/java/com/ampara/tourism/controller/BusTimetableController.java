package com.ampara.tourism.controller;

import com.ampara.tourism.service.BusTimetableSourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transport/bus-live")
public class BusTimetableController {
    private final BusTimetableSourceService sourceService;

    public BusTimetableController(BusTimetableSourceService sourceService) {
        this.sourceService = sourceService;
    }

    @GetMapping
    public Map<String, Object> search(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String town) {
        return sourceService.searchAllSources(origin, destination, town);
    }
}
