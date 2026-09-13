package com.ampara.tourism.seed;

import com.ampara.tourism.entity.Attraction;
import com.ampara.tourism.entity.Hotel;
import com.ampara.tourism.entity.HotelRoom;
import com.ampara.tourism.entity.FoodItem;
import com.ampara.tourism.repository.HotelRoomRepository;
import com.ampara.tourism.repository.FoodItemRepository;
import com.ampara.tourism.entity.Role;
import com.ampara.tourism.entity.User;
import com.ampara.tourism.repository.AttractionRepository;
import com.ampara.tourism.repository.HotelRepository;
import com.ampara.tourism.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AttractionRepository attractionRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final HotelRoomRepository hotelRoomRepository;
    private final FoodItemRepository foodItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataSeeder(AttractionRepository attractionRepository, HotelRepository hotelRepository,
                       UserRepository userRepository, PasswordEncoder passwordEncoder,
                       HotelRoomRepository hotelRoomRepository, FoodItemRepository foodItemRepository) {
        this.attractionRepository = attractionRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.hotelRoomRepository = hotelRoomRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedAttractions();
        seedHotels();
        seedRoomsAndFood();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        userRepository.save(new User("Admin", adminEmail, passwordEncoder.encode(adminPassword), Role.ADMIN));
        log.info("Seeded admin account -> email: {} (password set via ADMIN_PASSWORD env var, default is dev-only)", adminEmail);
    }

    private void seedHotels() {
        if (hotelRepository.count() > 0) return;
        List<Hotel> hotels = List.of(
            hotel("Arugam Bay Surf Resort","Pottuvil","Arugam Bay Main Road",6500.0,4.3,6.8404,81.8368,
                    "Beachfront rooms a short walk from the main surf point."),
            hotel("Gal Oya Lodge","Inginiyagala","Near Gal Oya National Park",9000.0,4.6,7.2167,81.3667,
                    "Eco-lodge near Senanayake Samudraya; safari arrangements available."),
            hotel("Ampara Town Inn","Ampara","Ampara Town",3500.0,3.8,7.2975,81.6747,
                    "Simple central budget accommodation."),
            hotel("Akkaraipattu City Hotel","Akkaraipattu","Akkaraipattu Town",4000.0,4.0,7.2165,81.8541,
                    "Town-centre accommodation for short stays."),
            hotel("Kalmunai Beach View","Kalmunai","Kalmunai Coast",5500.0,4.1,7.4160,81.8280,
                    "Coastal stay close to local shops and dining."),
            hotel("Oluvil Coastal Resort","Oluvil","Oluvil Coast",6000.0,4.2,7.2137,81.8526,
                    "Quiet coastal accommodation."),
            hotel("Nintavur Lagoon Stay","Nintavur","Nintavur Lagoon Road",4500.0,3.9,7.2160,81.8510,
                    "Budget stay near lagoon and town facilities."),
            hotel("Karaitivu Lagoon Hotel","Karaitivu","Karaitivu Town",4200.0,3.9,7.2670,81.8600,
                    "Simple rooms near lagoon-side attractions."),
            hotel("Sainthamaruthu Seaside Inn","Sainthamaruthu","Sainthamaruthu Coast",4800.0,4.0,7.3990,81.8350,
                    "Seaside rooms with easy access to local food.")
        );
        hotelRepository.saveAll(hotels);
    }

    private Hotel hotel(String name,String town,String address,double price,double rating,double lat,double lng,String desc) {
        Hotel h = new Hotel(name,"Ampara",address,price,rating,null,desc);
        h.setTown(town); h.setLatitude(lat); h.setLongitude(lng);
        return h;
    }

    private void seedRoomsAndFood() {
        if (hotelRoomRepository.count() == 0) {
            for (Hotel h : hotelRepository.findAll()) {
                hotelRoomRepository.save(new HotelRoom(h,"Standard Room",h.getPricePerNight(),2,true,
                        "Wi-Fi, AC, private bathroom",null));
                hotelRoomRepository.save(new HotelRoom(h,"Family Room",h.getPricePerNight()+25,4,true,
                        "Wi-Fi, AC, private bathroom, breakfast option",null));
            }
        }
        if (foodItemRepository.count() == 0) {
            foodItemRepository.saveAll(List.of(
                food("Seafood Rice & Curry","Pottuvil","Sri Lankan","Arugam Bay Local Kitchen",1800.0,"Local rice and curry with fresh seafood.",true),
                food("Kottu Roti","Pottuvil","Street Food","Pottuvil Food Corner",1000.0,"Popular Sri Lankan chopped roti dish.",false),
                food("Fresh King Coconut","Pottuvil","Drink","Arugam Bay Beach Vendors",300.0,"Fresh king coconut drink.",true),
                food("Seafood Rice & Curry","Akkaraipattu","Sri Lankan","Akkaraipattu Family Restaurant",1700.0,"Sri Lankan rice and curry selection.",false),
                food("Kottu Roti","Kalmunai","Street Food","Kalmunai City Restaurant",1000.0,"Freshly prepared kottu roti.",false),
                food("String Hoppers & Curry","Ampara","Breakfast","Ampara Town Kitchen",900.0,"Traditional breakfast with curry.",true),
                food("Parotta & Curry","Oluvil","Sri Lankan","Oluvil Coastal Restaurant",1000.0,"Popular local meal.",false),
                food("Fish Curry Rice","Nintavur","Sri Lankan","Nintavur Lagoon Restaurant",1500.0,"Local fish curry with rice.",false),
                food("Vegetable Rice & Curry","Karaitivu","Vegetarian","Karaitivu Family Kitchen",1000.0,"Vegetarian local meal.",true),
                food("Hoppers","Sainthamaruthu","Breakfast","Sainthamaruthu Food House",900.0,"Traditional hoppers and accompaniments.",true)
            ));
        }
        // Existing databases may already contain food rows. Give every food outlet a
        // usable coordinate by inheriting the town's seeded accommodation coordinate
        // when the outlet has not supplied its own GPS position.
        for (FoodItem item : foodItemRepository.findAll()) {
            if (item.getLatitude() == null || item.getLongitude() == null) {
                hotelRepository.findByTownIgnoreCase(item.getTown()).stream().findFirst().ifPresent(h -> {
                    item.setLatitude(h.getLatitude());
                    item.setLongitude(h.getLongitude());
                    foodItemRepository.save(item);
                });
            }
        }
    }

    private FoodItem food(String name,String town,String category,String restaurant,double price,String desc,boolean veg) {
        return new FoodItem(name,town,category,restaurant,price,desc,null,veg,null);
    }

    private void seedAttractions() {
        if (attractionRepository.count() > 0) {
            return;
        }

        attractionRepository.saveAll(List.of(
                new Attraction(
                        "Gal Oya National Park", "Boat safari on Senanayake Samudraya reservoir, elephant herds year-round.",
                        "Ampara", "Wildlife", 7.2167, 81.3667, "May-September",
                        2, List.of("Boat safari", "Elephant watching", "Bird watching", "Vedda village walk")
                ),
                new Attraction(
                        "Kumana National Park", "Renowned for migratory birds, plus leopards, elephants and crocodiles.",
                        "Ampara", "Wildlife", 6.5131, 81.6878, "May-September",
                        1, List.of("Bird watching", "Jeep safari", "Wildlife photography")
                ),
                new Attraction(
                        "Lahugala Kitulana National Park", "One of Sri Lanka's smallest national parks, an important elephant habitat.",
                        "Ampara", "Wildlife", 6.8830, 81.6670, "May-September",
                        1, List.of("Elephant watching", "Nature walk")
                ),
                new Attraction(
                        "Buddhangala Raja Maha Viharaya", "Ancient forest hermitage near Ampara town with scenic rock viewpoints.",
                        "Ampara", "Heritage", 7.2800, 81.6800, "Year-round",
                        1, List.of("Temple visit", "Rock climb viewpoint", "Meditation")
                ),
                new Attraction(
                        "Rajagala Archaeological Site", "Ancient forest monastery ruins second in scale only to Mihintale.",
                        "Ampara", "Heritage", 7.4780, 81.6329, "May-September",
                        1, List.of("Hiking", "Archaeology tour", "Photography")
                ),
                new Attraction(
                        "Arugam Bay", "World-famous surf beach on the east coast with a lively restaurant scene.",
                        "Ampara", "Beach", 6.8404, 81.8368, "April-October",
                        2, List.of("Surfing", "Beach relaxing", "Seafood dining")
                ),
                new Attraction(
                        "Senanayake Samudraya", "Sri Lanka's largest reservoir, popular for sunset views next to Ampara town.",
                        "Ampara", "Nature", 7.3000, 81.6500, "Year-round",
                        1, List.of("Sunset viewing", "Boating")
                ),
                new Attraction(
                        "Magul Maha Viharaya", "Archaeologically protected 2nd-century BC Buddhist temple near Lahugala, known for its moonstone.",
                        "Ampara", "Heritage", 6.8781, 81.7200, "Year-round",
                        1, List.of("Temple visit", "Archaeology")
                )
        ));
    }
}
