package com.pulmocare.controller;

import com.pulmocare.model.Patient;
import com.pulmocare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;
    
    /**
     * Get all patients
     * 
     * @return List of all patients
     */
    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Get a patient by ID
     * 
     * @param id The ID of the patient to get
     * @return The patient
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPatientById(@PathVariable String id) {
        try {
            Patient patient = patientService.getPatient(id);
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Add a new patient (via signup)
     * 
     * @param patient The patient to create
     * @return The created patient
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Patient patient) {
        try {
            Patient createdPatient = patientService.createPatient(patient);
            return ResponseEntity.ok(createdPatient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Add a new patient (via admin or doctor)
     * 
     * @param patient The patient to create
     * @return The created patient
     */
    @PostMapping
    public ResponseEntity<?> addPatient(@RequestBody Patient patient) {
        try {
            Patient createdPatient = patientService.createPatient(patient);
            return ResponseEntity.ok(createdPatient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Patient sign in
     * 
     * @param credentials The email and password for authentication
     * @return The patient if authentication succeeds
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            if (email == null || password == null) {
                return ResponseEntity.badRequest().body("Email and password are required");
            }
            
            Patient patient = patientService.signIn(email, password);
            return ResponseEntity.ok(patient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Get the total number of patients
     * 
     * @return The count of patients
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getPatientCount() {
        long count = patientService.getPatientCount();
        return ResponseEntity.ok(count);
    }
    
    /**
     * Update a patient by ID
     * 
     * @param id The ID of the patient to update
     * @param patientDetails The updated patient details
     * @return The updated patient
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable String id, @RequestBody Patient patientDetails) {
        try {
            Patient updatedPatient = patientService.updatePatient(id, patientDetails);
            return ResponseEntity.ok(updatedPatient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
