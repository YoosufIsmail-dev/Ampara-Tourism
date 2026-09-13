package com.ampara.tourism.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_order")
public class FoodOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_item_id")
    private FoodItem foodItem;

    @Column(nullable = false) private String customerName;
    @Column(nullable = false) private String phone;
    private String deliveryAddress;
    private Integer quantity;
    private Double totalPrice;
    private String status = "PENDING";
    private LocalDateTime createdAt = LocalDateTime.now();

    public FoodOrder() {}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public FoodItem getFoodItem(){return foodItem;} public void setFoodItem(FoodItem v){foodItem=v;}
    public String getCustomerName(){return customerName;} public void setCustomerName(String v){customerName=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getDeliveryAddress(){return deliveryAddress;} public void setDeliveryAddress(String v){deliveryAddress=v;}
    public Integer getQuantity(){return quantity;} public void setQuantity(Integer v){quantity=v;}
    public Double getTotalPrice(){return totalPrice;} public void setTotalPrice(Double v){totalPrice=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
