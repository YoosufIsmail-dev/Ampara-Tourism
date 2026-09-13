package com.ampara.tourism.seed;

import com.ampara.tourism.entity.TouristPlace;
import com.ampara.tourism.repository.TouristPlaceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * Seeds the tourist_place table from src/main/resources/seed/places.json on first startup.
 *
 * NOTE: this starter dataset covers ~130 locations across the Ampara district towns you asked
 * for (Ampara Town, Akkaraipattu, Kalmunai, Arugam Bay, Pottuvil, Lahugala, etc). A handful of
 * well-known landmarks (Arugam Bay surf points, Rajagala, Magul Maha Viharaya, Senanayake
 * Samudraya...) are real and named specifically. The rest are realistic *placeholder* entries
 * (e.g. "{Town} Jumma Mosque", "{Town} Lagoon View Point") with town-level GPS coordinates
 * (jittered slightly so map markers don't overlap) - they are meant as an editable starting
 * point, not verified facts. Edit/replace them via the Admin API or directly in places.json
 * before treating this as production data - opening hours, entry fees, phone numbers and
 * ratings are placeholders and should be filled in with real details.
 */
@Component
@Order(2)
public class PlaceSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PlaceSeeder.class);

    private final TouristPlaceRepository placeRepository;
    private final ObjectMapper objectMapper;

    public PlaceSeeder(TouristPlaceRepository placeRepository, ObjectMapper objectMapper) {
        this.placeRepository = placeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (placeRepository.count() > 0) {
            return;
        }
        try (InputStream in = new ClassPathResource("seed/places.json").getInputStream()) {
            List<PlaceSeedDto> seedPlaces = objectMapper.readValue(in, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, PlaceSeedDto.class));

            List<TouristPlace> entities = seedPlaces.stream().map(this::toEntity).toList();
            placeRepository.saveAll(entities);
            log.info("Seeded {} tourist places from places.json (starter/editable dataset, see PlaceSeeder javadoc)", entities.size());
        }
    }

    private TouristPlace toEntity(PlaceSeedDto d) {
        TouristPlace p = new TouristPlace();
        p.setName(d.name);
        p.setTamilName(d.tamilName);
        p.setDescriptionEn(d.descriptionEn);
        p.setDescriptionTa(d.descriptionTa);
        p.setCategory(d.category);
        p.setLatitude(d.latitude);
        p.setLongitude(d.longitude);
        p.setTown(d.town);
        p.setDistrict(d.district);
        p.setOpeningHours(d.openingHours);
        p.setEntryFee(d.entryFee);
        p.setContactNumber(d.contactNumber);
        p.setWebsite(d.website);
        p.setImageUrl(d.imageUrl);
        p.setRating(d.rating);
        p.setActivities(d.activities == null ? List.of() : d.activities);
        p.setParking(d.parking != null && d.parking);
        p.setWheelchairAccess(d.wheelchairAccess != null && d.wheelchairAccess);
        return p;
    }

    /** Mirrors the shape of resources/seed/places.json. */
    public static class PlaceSeedDto {
        public String name;
        public String tamilName;
        public String descriptionEn;
        public String descriptionTa;
        public String category;
        public Double latitude;
        public Double longitude;
        public String town;
        public String district;
        public String openingHours;
        public String entryFee;
        public String contactNumber;
        public String website;
        public String imageUrl;
        public Double rating;
        public List<String> activities;
        public Boolean parking;
        public Boolean wheelchairAccess;
    }
}
