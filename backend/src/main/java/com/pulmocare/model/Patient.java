package com.pulmocare.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.List;
import java.util.Map;

@Document(collection = "patients")
public class Patient {
    @Id
    private String id;
    
    // Basic Information
    private String photo;
    private String insuranceProvider;
    private String firstName;
    private String lastName;
    private String gender;    private int age;
      @Indexed(unique = true)
    private String email;
    
    private String password;
    private String dateOfBirth;
    
    private String bloodType;
    private double height;
    private double weight;
    private String location;
    private String maritalStatus;
    private String occupation;
    private boolean hasPets;
    private boolean isSmoking;
    
    // Medical History

    private String symptomsAssessment;
    private String report;
    
    // Medical Tests
    private List<Map<String, Object>> bloodTests;
    private List<Map<String, Object>> xRays;
    private List<Map<String, Object>> otherImaging;
    
    // Vaccination and Vitals
    private List<Map<String, Object>> vaccinationHistory;
    private Map<String, Object> vitals; // blood pressure, heart rate, respiration, temperature, oxygen saturation, peak flow
    
    // Medical Conditions
    private List<String> allergies;
    private List<String> chronicConditions;
    private List<Map<String, Object>> surgeriesHistory;
    
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public boolean isHasPets() {
        return hasPets;
    }

    public void setHasPets(boolean hasPets) {
        this.hasPets = hasPets;
    }

    public boolean isSmoking() {
        return isSmoking;
    }

    public void setSmoking(boolean isSmoking) {
        this.isSmoking = isSmoking;
    }

    public String getSymptomsAssessment() {
        return symptomsAssessment;
    }

    public void setSymptomsAssessment(String symptomsAssessment) {
        this.symptomsAssessment = symptomsAssessment;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public List<Map<String, Object>> getBloodTests() {
        return bloodTests;
    }

    public void setBloodTests(List<Map<String, Object>> bloodTests) {
        this.bloodTests = bloodTests;
    }

    public List<Map<String, Object>> getxRays() {
        return xRays;
    }

    public void setxRays(List<Map<String, Object>> xRays) {
        this.xRays = xRays;
    }

    public List<Map<String, Object>> getOtherImaging() {
        return otherImaging;
    }

    public void setOtherImaging(List<Map<String, Object>> otherImaging) {
        this.otherImaging = otherImaging;
    }

    public List<Map<String, Object>> getVaccinationHistory() {
        return vaccinationHistory;
    }

    public void setVaccinationHistory(List<Map<String, Object>> vaccinationHistory) {
        this.vaccinationHistory = vaccinationHistory;
    }

    public Map<String, Object> getVitals() {
        return vitals;
    }

    public void setVitals(Map<String, Object> vitals) {
        this.vitals = vitals;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public List<String> getChronicConditions() {
        return chronicConditions;
    }

    public void setChronicConditions(List<String> chronicConditions) {
        this.chronicConditions = chronicConditions;
    }

    public List<Map<String, Object>> getSurgeriesHistory() {
        return surgeriesHistory;
    }

    public void setSurgeriesHistory(List<Map<String, Object>> surgeriesHistory) {
        this.surgeriesHistory = surgeriesHistory;
    }
}
