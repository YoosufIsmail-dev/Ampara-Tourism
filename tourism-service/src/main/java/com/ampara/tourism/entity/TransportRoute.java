package com.ampara.tourism.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transport_route")
public class TransportRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** BUS or TRAIN */
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String routeName;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    /** e.g. "05:30, 07:00, 09:30, ..." kept as free text for simplicity */
    private String departureTimes;

    private String durationMinutesApprox;

    private String frequency;

    private String fare;

    private String operatorName;

    private String notes;

    public TransportRoute() {
    }

    public TransportRoute(String type, String routeName, String origin, String destination,
                           String departureTimes, String durationMinutesApprox, String frequency,
                           String fare, String operatorName, String notes) {
        this.type = type;
        this.routeName = routeName;
        this.origin = origin;
        this.destination = destination;
        this.departureTimes = departureTimes;
        this.durationMinutesApprox = durationMinutesApprox;
        this.frequency = frequency;
        this.fare = fare;
        this.operatorName = operatorName;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getDepartureTimes() { return departureTimes; }
    public void setDepartureTimes(String departureTimes) { this.departureTimes = departureTimes; }
    public String getDurationMinutesApprox() { return durationMinutesApprox; }
    public void setDurationMinutesApprox(String durationMinutesApprox) { this.durationMinutesApprox = durationMinutesApprox; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getFare() { return fare; }
    public void setFare(String fare) { this.fare = fare; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
