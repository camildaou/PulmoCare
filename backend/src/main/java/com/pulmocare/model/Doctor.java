package com.pulmocare.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

@Document(collection = "doctors")
public class Doctor {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private Integer age;
    private String description;
    private String location;
    private String countryCode;
    private String phone;
    
    @Indexed(unique = true)
    private String email;
    private String password;
    private String medicalLicense;
    
    // Availability fields
    private List<String> availableDays; // ["mon", "tue", "wed", "thu", "fri"]
    private Map<String, List<TimeSlot>> availableTimeSlots; // Map of day to list of time slots
    private List<String> unavailableDates; // Dates when the doctor is unavailable
      // TimeSlot class to represent a time range
    public static class TimeSlot {
        private String startTime; // "09:00"
        private String endTime;   // "09:30"
        
        public TimeSlot() {}
        
        public TimeSlot(String startTime, String endTime) {
            validateTimeFormat(startTime);
            validateTimeFormat(endTime);
            validateTimeSlotDuration(startTime, endTime);
            
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        // Validate time format (HH:MM)
        private void validateTimeFormat(String time) {
            if (!time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                throw new IllegalArgumentException("Time must be in format HH:MM");
            }
        }
        
        // Validate time slot duration (must be 30 minutes)
        private void validateTimeSlotDuration(String startTime, String endTime) {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");
            
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            
            // Convert to minutes for easier calculation
            int startTotalMinutes = startHour * 60 + startMinute;
            int endTotalMinutes = endHour * 60 + endMinute;
            int duration = endTotalMinutes - startTotalMinutes;
            
            if (duration != 30) {
                throw new IllegalArgumentException("Time slot duration must be exactly 30 minutes");
            }
        }
        
        public String getStartTime() {
            return startTime;
        }
        
        public void setStartTime(String startTime) {
            validateTimeFormat(startTime);
            if (this.endTime != null) {
                validateTimeSlotDuration(startTime, this.endTime);
            }
            this.startTime = startTime;
        }
        
        public String getEndTime() {
            return endTime;
        }
        
        public void setEndTime(String endTime) {
            validateTimeFormat(endTime);
            if (this.startTime != null) {
                validateTimeSlotDuration(this.startTime, endTime);
            }
            this.endTime = endTime;
        }
        
        @Override
        public String toString() {
            return startTime + " - " + endTime;
        }
    }

    // Default constructor
    public Doctor() {
        this.availableDays = new ArrayList<>();
        this.availableTimeSlots = new HashMap<>();
        this.unavailableDates = new ArrayList<>();
    }

    // Constructor with fields
    public Doctor(String firstName, String lastName, String gender, Integer age,
                 String description, String location, String countryCode,
                 String phone, String email, String password, String medicalLicense,
                 List<String> availableDays, Map<String, List<TimeSlot>> availableTimeSlots,
                 List<String> unavailableDates) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.age = age;
        this.description = description;
        this.location = location;
        this.countryCode = countryCode;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.medicalLicense = medicalLicense;
        this.availableDays = availableDays;
        this.availableTimeSlots = availableTimeSlots;
        this.unavailableDates = unavailableDates;
    }
    
    // Constructor without availability fields
    public Doctor(String firstName, String lastName, String gender, Integer age,
                 String description, String location, String countryCode,
                 String phone, String email, String password, String medicalLicense) {
        this(firstName, lastName, gender, age, description, location, countryCode,
            phone, email, password, medicalLicense, new ArrayList<>(), new HashMap<>(), new ArrayList<>());
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMedicalLicense() {
        return medicalLicense;
    }

    public void setMedicalLicense(String medicalLicense) {
        this.medicalLicense = medicalLicense;
    }

    public List<String> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(List<String> availableDays) {
        this.availableDays = availableDays;
    }

    public Map<String, List<TimeSlot>> getAvailableTimeSlots() {
        return availableTimeSlots;
    }

    public void setAvailableTimeSlots(Map<String, List<TimeSlot>> availableTimeSlots) {
        this.availableTimeSlots = availableTimeSlots;
    }

    public List<String> getUnavailableDates() {
        return unavailableDates;
    }

    public void setUnavailableDates(List<String> unavailableDates) {
        this.unavailableDates = unavailableDates;
    }
}