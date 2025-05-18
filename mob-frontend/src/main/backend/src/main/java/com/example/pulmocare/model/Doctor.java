package com.example.pulmocare.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "doctors")
public class Doctor {
    
    @Id
    private String id;
    
    private String firstName;
    private String lastName;
    private String gender;
    private Integer age;
    private String description;
    private Integer numberOfPatients;
    private List<Schedule> schedule;
    private String image;
    private String location;
    private String countryCode;
    private String phone;
    private String email;
    private String password;
    private String medicalLicense;
    private String specialty;
    private Float rating;
    private Integer reviews;
    private List<String> availability;    // Constructors
    public Doctor() {
    }
    
    public Doctor(String firstName, String lastName, String gender, Integer age, 
                 String description, Integer numberOfPatients, List<Schedule> schedule, 
                 String image, String location, String countryCode, String phone, 
                 String email, String password, String medicalLicense, String specialty,
                 Float rating, Integer reviews, List<String> availability) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.age = age;
        this.description = description;
        this.numberOfPatients = numberOfPatients;
        this.schedule = schedule;
        this.image = image;
        this.location = location;
        this.countryCode = countryCode;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.medicalLicense = medicalLicense;
        this.specialty = specialty;
        this.rating = rating;
        this.reviews = reviews;
        this.availability = availability;
    }
    
    // Getters and setters
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
    
    public Integer getNumberOfPatients() {
        return numberOfPatients;
    }
    
    public void setNumberOfPatients(Integer numberOfPatients) {
        this.numberOfPatients = numberOfPatients;
    }
    
    public List<Schedule> getSchedule() {
        return schedule;
    }
    
    public void setSchedule(List<Schedule> schedule) {
        this.schedule = schedule;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
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
    
    public String getSpecialty() {
        return specialty;
    }
    
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
    
    public Float getRating() {
        return rating;
    }
    
    public void setRating(Float rating) {
        this.rating = rating;
    }
    
    public Integer getReviews() {
        return reviews;
    }
    
    public void setReviews(Integer reviews) {
        this.reviews = reviews;
    }
    
    public List<String> getAvailability() {
        return availability;
    }
    
    public void setAvailability(List<String> availability) {
        this.availability = availability;
    }
}