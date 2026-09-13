package com.ampara.tourism.seed;

import com.ampara.tourism.entity.EmergencyContact;
import com.ampara.tourism.entity.Event;
import com.ampara.tourism.entity.NearbyFacility;
import com.ampara.tourism.entity.TransportRoute;
import com.ampara.tourism.repository.EmergencyContactRepository;
import com.ampara.tourism.repository.EventRepository;
import com.ampara.tourism.repository.NearbyFacilityRepository;
import com.ampara.tourism.repository.TransportRouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

/**
 * Seeds starter data for the newer feature set (emergency contacts, bus/train routes,
 * nearby hospitals/ATMs, and the event calendar) on first startup.
 *
 * Nationwide emergency numbers and general bus/train info are real and verified as of
 * this writing. District-level facility entries (specific ATM branches, clinic addresses)
 * are realistic *placeholders* meant to be replaced with verified local details via the
 * Admin API before production use - same caveat as PlaceSeeder's town-level entries.
 */
@Component
@Order(3)
public class FeatureSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FeatureSeeder.class);

    private final EmergencyContactRepository emergencyContactRepository;
    private final TransportRouteRepository transportRouteRepository;
    private final NearbyFacilityRepository nearbyFacilityRepository;
    private final EventRepository eventRepository;

    public FeatureSeeder(EmergencyContactRepository emergencyContactRepository,
                          TransportRouteRepository transportRouteRepository,
                          NearbyFacilityRepository nearbyFacilityRepository,
                          EventRepository eventRepository) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.transportRouteRepository = transportRouteRepository;
        this.nearbyFacilityRepository = nearbyFacilityRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        seedEmergencyContacts();
        seedTransport();
        seedNearbyFacilities();
        seedEvents();
    }

    private void seedEmergencyContacts() {
        if (emergencyContactRepository.count() > 0) return;

        emergencyContactRepository.saveAll(List.of(
                new EmergencyContact("Police Emergency", "POLICE", "119", null, null, null,
                        "Nationwide police emergency hotline.", true),
                new EmergencyContact("Tourist Police", "TOURIST_POLICE", "1912", null, null, null,
                        "Dedicated hotline for tourists - English-speaking officers.", true),
                new EmergencyContact("Suwa Seriya Ambulance", "AMBULANCE", "1990", null, null, null,
                        "Free nationwide emergency ambulance service.", true),
                new EmergencyContact("Fire & Rescue", "FIRE", "110", null, null, null,
                        "Nationwide fire and rescue hotline.", true),
                new EmergencyContact("Disaster Management Centre", "DISASTER", "117", null, null, null,
                        "Nationwide hotline for floods, landslides and other disasters.", true),
                new EmergencyContact("Ampara Base Hospital", "HOSPITAL", "063-2222261", "Ampara", "Ampara Town",
                        "Hospital Road, Ampara", "Main government hospital for the district.", false),
                new EmergencyContact("Akkaraipattu Base Hospital", "HOSPITAL", "067-2276261", "Ampara", "Akkaraipattu",
                        null, null, false),
                new EmergencyContact("Kalmunai Base Hospital (North)", "HOSPITAL", "067-2223261", "Ampara", "Kalmunai",
                        null, null, false)
        ));
        log.info("Seeded emergency contacts");
    }

    private void seedTransport() {
        if (transportRouteRepository.count() > 0) return;

        transportRouteRepository.saveAll(List.of(
                new TransportRoute("BUS", "Colombo - Ampara (Highway)", "Colombo", "Ampara",
                        "06:00, 09:00, 13:00, 21:30 (night)", "~360", "3-4 times daily", "Rs. 700-950",
                        "SLTB / Private", "Book Highway/AC buses ahead during festival season."),
                new TransportRoute("BUS", "Ampara - Arugam Bay / Pottuvil", "Ampara", "Pottuvil",
                        "Every 30-45 min, 05:30-18:00", "~90", "Frequent", "Rs. 150-200",
                        "SLTB", "Also stops near Lahugala National Park."),
                new TransportRoute("BUS", "Ampara - Kalmunai", "Ampara", "Kalmunai",
                        "Every 20-30 min, 05:00-19:00", "~60", "Frequent", "Rs. 100-140",
                        "SLTB / Private", null),
                new TransportRoute("TRAIN", "Colombo Fort - Batticaloa (nearest rail line)", "Colombo Fort", "Batticaloa",
                        "05:45, 20:15 (night mail)", "~420", "2 times daily", "Rs. 400-1200 (class-dependent)",
                        "Sri Lanka Railways", "No direct rail into Ampara district - onward bus/tuk-tuk needed from Batticaloa (~1.5-2 hrs)."),
                new TransportRoute("BUS", "Batticaloa - Ampara", "Batticaloa", "Ampara",
                        "Every 45-60 min, 05:30-19:00", "~90", "Frequent", "Rs. 150-220",
                        "SLTB / Private", "Best connection from the nearest railway station.")
        ));
        log.info("Seeded transport routes");
    }

    private void seedNearbyFacilities() {
        if (nearbyFacilityRepository.count() > 0) return;

        List<NearbyFacility> facilities = List.of(
                facility("Ampara Base Hospital", "HOSPITAL", 7.2975, 81.6747, "Ampara", "Ampara",
                        "Hospital Road, Ampara", "063-2222261", "Government", false),
                facility("Arugam Bay Medical Centre", "CLINIC", 6.8395, 81.8360, "Pottuvil", "Ampara",
                        "Main Street, Arugam Bay", null, "Private clinic", false),
                facility("Bank of Ceylon ATM - Ampara Town", "ATM", 7.2965, 81.6735, "Ampara", "Ampara",
                        "Main Street, Ampara", null, "Bank of Ceylon", true),
                facility("Commercial Bank ATM - Ampara Town", "ATM", 7.2970, 81.6740, "Ampara", "Ampara",
                        "D.S. Senanayake Street, Ampara", null, "Commercial Bank", true),
                facility("People's Bank - Pottuvil Branch", "BANK", 6.8630, 81.8320, "Pottuvil", "Ampara",
                        "Main Street, Pottuvil", "063-2248222", "People's Bank", false),
                facility("Sampath Bank ATM - Kalmunai", "ATM", 7.4160, 81.8280, "Kalmunai", "Ampara",
                        "Main Street, Kalmunai", null, "Sampath Bank", true)
        );
        nearbyFacilityRepository.saveAll(facilities);
        log.info("Seeded nearby facilities");
    }

    private NearbyFacility facility(String name, String type, double lat, double lng, String town, String district,
                                     String address, String phone, String operatorOrBank, boolean open24) {
        NearbyFacility f = new NearbyFacility();
        f.setName(name);
        f.setType(type);
        f.setLatitude(lat);
        f.setLongitude(lng);
        f.setTown(town);
        f.setDistrict(district);
        f.setAddress(address);
        f.setPhoneNumber(phone);
        f.setOperatorOrBank(operatorOrBank);
        f.setOpen24Hours(open24);
        return f;
    }

    private void seedEvents() {
        if (eventRepository.count() > 0) return;

        int year = LocalDateTime.now().getYear();
        if (LocalDateTime.now().getMonthValue() >= 11) year += 1; // roll forward once the year's mostly over

        eventRepository.saveAll(List.of(
                event("Thai Pongal", "தை பொங்கல்", "තෛ පොංගල්",
                        "Tamil harvest festival celebrated across Ampara's Tamil communities with kolam art, pongal cooking and family gatherings.",
                        "FESTIVAL", LocalDateTime.of(year, Month.JANUARY, 14, 6, 0), null, "Ampara district", "Ampara", null, true),
                event("Sinhala & Tamil New Year", "தமிழ்ப் புத்தாண்டு", "අලුත් අවුරුද්ද",
                        "Traditional new year celebrated island-wide with games, sweets and family visits.",
                        "CULTURAL", LocalDateTime.of(year, Month.APRIL, 13, 0, 0),
                        LocalDateTime.of(year, Month.APRIL, 14, 23, 59), "Island-wide", "Ampara", null, true),
                event("Vel Festival", "வேல் விழா", "වෙල් උත්සවය",
                        "Hindu chariot festival honouring Lord Murugan, observed at kovils across the district.",
                        "RELIGIOUS", LocalDateTime.of(year, Month.JULY, 20, 17, 0), null, "Kalmunai / Ampara Town kovils", "Ampara", null, true),
                event("Kataragama Esala Festival Season", null, null,
                        "Peak pilgrimage and festival season at the nearby Kataragama shrine complex, drawing visitors from across Ampara district.",
                        "RELIGIOUS", LocalDateTime.of(year, Month.AUGUST, 1, 0, 0),
                        LocalDateTime.of(year, Month.AUGUST, 15, 23, 59), "Kataragama", "Kataragama", null, true),
                event("Arugam Bay Surf Season Opening", null, null,
                        "Start of the main surf season on the east coast, with informal contests and beach events.",
                        "SPORTS", LocalDateTime.of(year, Month.APRIL, 1, 8, 0), null, "Arugam Bay", "Pottuvil", null, true)
        ));
        log.info("Seeded events");
    }

    private Event event(String title, String titleTa, String titleSi, String description, String category,
                         LocalDateTime start, LocalDateTime end, String location, String town, Long placeId, boolean recurring) {
        Event e = new Event();
        e.setTitle(title);
        e.setTitleTa(titleTa);
        e.setTitleSi(titleSi);
        e.setDescription(description);
        e.setCategory(category);
        e.setStartDateTime(start);
        e.setEndDateTime(end);
        e.setLocation(location);
        e.setTown(town);
        e.setPlaceId(placeId);
        e.setRecurringYearly(recurring);
        return e;
    }
}
