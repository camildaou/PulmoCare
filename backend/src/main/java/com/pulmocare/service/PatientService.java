package com.pulmocare.service;

import com.pulmocare.model.Patient;
import com.pulmocare.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    /**
     * Create a new patient
     * 
     * @param patient The patient to create
     * @return The created patient
     */
    public Patient createPatient(Patient patient) {
        // Check if patient with email already exists
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        return patientRepository.save(patient);
    }

    /**
     * Update an existing patient
     * 
     * @param id The ID of the patient to update
     * @param patientDetails The updated patient details
     * @return The updated patient
     */
    public Patient updatePatient(String id, Patient patientDetails) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
                
        // Update patient information
        patient.setPhoto(patientDetails.getPhoto());
        patient.setInsuranceProvider(patientDetails.getInsuranceProvider());
        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setGender(patientDetails.getGender());
        patient.setAge(patientDetails.getAge());
        patient.setBloodType(patientDetails.getBloodType());
        patient.setHeight(patientDetails.getHeight());
        patient.setWeight(patientDetails.getWeight());
        patient.setLocation(patientDetails.getLocation());
        patient.setMaritalStatus(patientDetails.getMaritalStatus());
        patient.setOccupation(patientDetails.getOccupation());
        patient.setHasPets(patientDetails.isHasPets());
        patient.setSmoking(patientDetails.isSmoking());
        
        // Update medical history
        patient.setSymptomsAssessment(patientDetails.getSymptomsAssessment());
        patient.setReport(patientDetails.getReport());
        
        // Update medical tests
        patient.setBloodTests(patientDetails.getBloodTests());
        patient.setxRays(patientDetails.getxRays());
        patient.setOtherImaging(patientDetails.getOtherImaging());
        
        // Update vaccinations and vitals
        patient.setVaccinationHistory(patientDetails.getVaccinationHistory());
        patient.setVitals(patientDetails.getVitals());
        
        // Update medical conditions
        patient.setAllergies(patientDetails.getAllergies());
        patient.setChronicConditions(patientDetails.getChronicConditions());
        patient.setSurgeriesHistory(patientDetails.getSurgeriesHistory());
        
        return patientRepository.save(patient);
    }

    /**
     * Get a patient by ID
     * 
     * @param id The ID of the patient to get
     * @return The patient
     */
    public Patient getPatient(String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    /**
     * Get a patient by email
     * 
     * @param email The email of the patient to get
     * @return The patient
     */
    public Patient getPatientByEmail(String email) {
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    /**
     * Delete a patient by ID
     * 
     * @param id The ID of the patient to delete
     */
    public void deletePatient(String id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        patientRepository.delete(patient);
    }

    /**
     * Get all patients
     * 
     * @return A list of all patients
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Get the total number of patients
     * 
     * @return The count of patients
     */
    public long getPatientCount() {
        return patientRepository.count();
    }

    /**
     * Patient sign in
     * 
     * @param email The email address
     * @param password The password
     * @return The patient if authentication succeeds
     * @throws RuntimeException if authentication fails
     */
    public Patient signIn(String email, String password) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
                
        // Note: In a real application, you should use a password encoder
        // to securely verify the password. This is a simplified implementation.
        if (!password.equals(patient.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        return patient;
    }
}
