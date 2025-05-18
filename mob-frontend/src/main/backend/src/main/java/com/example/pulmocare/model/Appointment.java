package com.example.pulmocare.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "appointments")
public class Appointment {
    
    @Id
    private String id;
    
    private Schedule schedule;
    private String doctor;
    private String assessment;
    private String diagnosis;
    private String personalNotes;
    private String plan;
    private String location;
    private String reason;
    private Boolean upcoming;
    private Boolean vaccine;
    
    // Constructors
    public Appointment() {
    }
    
    public Appointment(Schedule schedule, String doctor, String assessment, 
                      String diagnosis, String personalNotes, String plan, 
                      String location, String reason, Boolean upcoming, Boolean vaccine) {
        this.schedule = schedule;
        this.doctor = doctor;
        this.assessment = assessment;
        this.diagnosis = diagnosis;
        this.personalNotes = personalNotes;
        this.plan = plan;
        this.location = location;
        this.reason = reason;
        this.upcoming = upcoming;
        this.vaccine = vaccine;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Schedule getSchedule() {
        return schedule;
    }
    
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
    
    public String getDoctor() {
        return doctor;
    }
    
    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }
    
    public String getAssessment() {
        return assessment;
    }
    
    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }
    
    public String getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    public String getPersonalNotes() {
        return personalNotes;
    }
    
    public void setPersonalNotes(String personalNotes) {
        this.personalNotes = personalNotes;
    }
    
    public String getPlan() {
        return plan;
    }
    
    public void setPlan(String plan) {
        this.plan = plan;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public Boolean getUpcoming() {
        return upcoming;
    }
    
    public void setUpcoming(Boolean upcoming) {
        this.upcoming = upcoming;
    }
    
    public Boolean getVaccine() {
        return vaccine;
    }
    
    public void setVaccine(Boolean vaccine) {
        this.vaccine = vaccine;
    }
}
