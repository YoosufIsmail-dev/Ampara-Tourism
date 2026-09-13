package com.ampara.tourism.controller;

import com.ampara.tourism.entity.FoodItem;
import com.ampara.tourism.repository.FoodItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodItemController {
    private final FoodItemRepository repository;
    public FoodItemController(FoodItemRepository repository){this.repository=repository;}
    @GetMapping public List<FoodItem> list(@RequestParam(required=false) String town){
        return town==null ? repository.findAll() : repository.findByTownIgnoreCase(town);
    }
    @GetMapping("/{id}") public ResponseEntity<FoodItem> get(@PathVariable Long id){
        return repository.findById(id).map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());
    }
    @PostMapping public FoodItem create(@RequestBody FoodItem item){return repository.save(item);}
}