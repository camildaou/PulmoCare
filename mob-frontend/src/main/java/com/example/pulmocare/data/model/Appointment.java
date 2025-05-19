package com.example.pulmocare.data.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Data class representing an appointment
 */
public class Appointment {
    private String id;
    
    // Schedule information
    private String date; // In ISO format: YYYY-MM-DD
    private String hour; // In format: HH:MM
    
    // References to patient and doctor
    private Patient patient;
    private Doctor doctor;
    
    // Assessment information
    private String assessmentInfo;
    
    @SerializedName("reportPending")
    private boolean isReportPending;
    
    // Medical details
    private String diagnosis;
    private String personalNotes; // doctor's notes
    private String plan;
    
    // Appointment details
    private String location;
    private String reason;
    
    @SerializedName("upcoming")
    private boolean isUpcoming; // true if upcoming, false if past
    
    @SerializedName("vaccine")
    private boolean isVaccine; // true if this is a vaccine appointment
    
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public String getAssessmentInfo() {
        return assessmentInfo;
    }

    public void setAssessmentInfo(String assessmentInfo) {
        this.assessmentInfo = assessmentInfo;
    }

    public boolean isReportPending() {
        return isReportPending;
    }

    public void setReportPending(boolean reportPending) {
        isReportPending = reportPending;
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

    public boolean isUpcoming() {
        return isUpcoming;
    }

    public void setUpcoming(boolean upcoming) {
        isUpcoming = upcoming;
    }

    public boolean isVaccine() {
        return isVaccine;
    }

    public void setVaccine(boolean vaccine) {
        isVaccine = vaccine;
    }
}
