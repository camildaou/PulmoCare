package com.example.pulmocare.model;

public class Schedule {
    
    private String date;
    private String hour;
    
    // Constructors
    public Schedule() {
    }
    
    public Schedule(String date, String hour) {
        this.date = date;
        this.hour = hour;
    }
    
    // Getters and setters
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getHour() {
        return hour;
    }
    
    public void setHour(String hour) {
        this.hour = hour;
    }
}
