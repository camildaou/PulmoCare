package com.pulmocare.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "appointments")
public class Appointment {
    @Id
    private String id;
      // Schedule information
    private LocalDate date;
    private LocalTime hour;    
    private String endTimeStr; // For handling the end time string from the frontend
    
    // References to patient and doctor
    @DBRef
    private Patient patient;
    
    @DBRef
    private Doctor doctor;
    
    // Assessment information
    private String assessmentInfo;
    private boolean reportPending;
    
    // Medical details
    private String diagnosis;
    private String personalNotes; // doctor's notes
    private String plan;
    
    // Appointment details
    private String location;
    private String reason;
    private boolean upcoming; // true if upcoming, false if past
    private boolean isVaccine; // true if this is a vaccine appointment
      // Constructors
    public Appointment() {
    }

    public Appointment(LocalDate date, LocalTime hour, Patient patient, Doctor doctor, 
                    String reason, String location, boolean isVaccine) {
        this.date = date;
        this.hour = hour;
        this.patient = patient;
        this.doctor = doctor;
        this.reason = reason;
        this.location = location;
        this.isVaccine = isVaccine;
        this.upcoming = true; // New appointments are always upcoming
        this.reportPending = false; // No report pending by default
    }
    
    public Appointment(LocalDate date, LocalTime hour, String endTimeStr, Patient patient, Doctor doctor, 
                    String reason, String location, boolean isVaccine) {
        this.date = date;
        this.hour = hour;
        this.endTimeStr = endTimeStr;
        this.patient = patient;
        this.doctor = doctor;
        this.reason = reason;
        this.location = location;
        this.isVaccine = isVaccine;
        this.upcoming = true; // New appointments are always upcoming
        this.reportPending = false; // No report pending by default
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHour() {
        return hour;
    }

    public void setHour(LocalTime hour) {
        this.hour = hour;
    }

    public String getEndTimeStr() {
        return endTimeStr;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
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
        return reportPending;
    }

    public void setReportPending(boolean reportPending) {
        this.reportPending = reportPending;
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
        return upcoming;
    }

    public void setUpcoming(boolean upcoming) {
        this.upcoming = upcoming;
    }

    public boolean isVaccine() {
        return isVaccine;
    }

    public void setVaccine(boolean vaccine) {
        isVaccine = vaccine;
    }

}
