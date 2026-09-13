package com.ampara.tourism.dto;

import jakarta.validation.constraints.NotBlank;

public class TransportRouteRequest {

    @NotBlank
    private String type;

    @NotBlank
    private String routeName;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    private String departureTimes;
    private String durationMinutesApprox;
    private String frequency;
    private String fare;
    private String operatorName;
    private String notes;

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
