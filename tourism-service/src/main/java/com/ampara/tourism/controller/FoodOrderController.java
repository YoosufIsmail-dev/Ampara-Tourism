package com.ampara.tourism.controller;

import com.ampara.tourism.entity.FoodItem;
import com.ampara.tourism.entity.FoodOrder;
import com.ampara.tourism.repository.FoodItemRepository;
import com.ampara.tourism.repository.FoodOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food-orders")
public class FoodOrderController {
    private final FoodOrderRepository orderRepository;
    private final FoodItemRepository foodRepository;

    public FoodOrderController(FoodOrderRepository orderRepository, FoodItemRepository foodRepository) {
        this.orderRepository = orderRepository;
        this.foodRepository = foodRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody FoodOrder request) {
        if (request.getFoodItem() == null || request.getFoodItem().getId() == null ||
                request.getCustomerName() == null || request.getCustomerName().isBlank() ||
                request.getPhone() == null || request.getPhone().isBlank() ||
                request.getQuantity() == null || request.getQuantity() < 1) {
            return ResponseEntity.badRequest().body("foodItem.id, customerName, phone and quantity are required");
        }
        FoodItem item = foodRepository.findById(request.getFoodItem().getId()).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();
        request.setFoodItem(item);
        request.setTotalPrice((item.getPrice() == null ? 0 : item.getPrice()) * request.getQuantity());
        request.setStatus("CONFIRMED");
        return ResponseEntity.ok(orderRepository.save(request));
    }
}
