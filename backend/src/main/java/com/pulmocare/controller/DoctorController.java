package com.pulmocare.controller;

import com.pulmocare.model.Doctor;
import com.pulmocare.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Get doctor by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctor(@PathVariable String id) {
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            return ResponseEntity.ok(doctor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Sign in
    @PostMapping("/signin")
    public ResponseEntity<Doctor> signIn(@RequestBody Map<String, String> credentials) {
        return ResponseEntity.ok(
            doctorService.signIn(credentials.get("email"), credentials.get("password"))
        );
    }

    // Sign up
    @PostMapping("/signup")
    public ResponseEntity<?> createDoctor(@RequestBody Doctor doctor) {
        try {
            Doctor createdDoctor = doctorService.createDoctor(doctor);
            return ResponseEntity.ok(createdDoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all doctors
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    // Get total number of doctors
    @GetMapping("/count")
    public ResponseEntity<Long> getDoctorCount() {
        return ResponseEntity.ok(doctorService.getDoctorCount());
    }

    // Update doctor
    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable String id, @RequestBody Doctor doctorDetails) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, doctorDetails));
    }
}
