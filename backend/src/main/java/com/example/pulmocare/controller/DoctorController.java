package com.example.pulmocare.controller;

import com.example.pulmocare.model.Doctor;
import com.example.pulmocare.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable String id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        return doctor.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialty(@PathVariable String specialty) {
        List<Doctor> doctors = doctorRepository.findBySpecialty(specialty);
        return ResponseEntity.ok(doctors);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<Doctor> getDoctorByEmail(@PathVariable String email) {
        Optional<Doctor> doctor = doctorRepository.findByEmail(email);
        return doctor.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Doctor>> getDoctorsByLocation(@PathVariable String location) {
        List<Doctor> doctors = doctorRepository.findByLocationContaining(location);
        return ResponseEntity.ok(doctors);
    }
    
    @GetMapping("/country-code/{countryCode}")
    public ResponseEntity<List<Doctor>> getDoctorsByCountryCode(@PathVariable String countryCode) {
        List<Doctor> doctors = doctorRepository.findByCountryCode(countryCode);
        return ResponseEntity.ok(doctors);
    }
    
    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        Doctor savedDoctor = doctorRepository.save(doctor);
        return ResponseEntity.ok(savedDoctor);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable String id, @RequestBody Doctor doctorDetails) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            
            // Update fields
            doctor.setFirstName(doctorDetails.getFirstName());
            doctor.setLastName(doctorDetails.getLastName());
            doctor.setGender(doctorDetails.getGender());
            doctor.setAge(doctorDetails.getAge());
            doctor.setDescription(doctorDetails.getDescription());
            doctor.setNumberOfPatients(doctorDetails.getNumberOfPatients());
            doctor.setSchedule(doctorDetails.getSchedule());
            doctor.setImage(doctorDetails.getImage());
            doctor.setLocation(doctorDetails.getLocation());
            doctor.setCountryCode(doctorDetails.getCountryCode());
            doctor.setPhone(doctorDetails.getPhone());
            doctor.setEmail(doctorDetails.getEmail());
            doctor.setPassword(doctorDetails.getPassword());
            doctor.setMedicalLicense(doctorDetails.getMedicalLicense());
            doctor.setSpecialty(doctorDetails.getSpecialty());
            doctor.setRating(doctorDetails.getRating());
            doctor.setReviews(doctorDetails.getReviews());
            doctor.setAvailability(doctorDetails.getAvailability());
            
            Doctor updatedDoctor = doctorRepository.save(doctor);
            return ResponseEntity.ok(updatedDoctor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable String id) {
        if (doctorRepository.existsById(id)) {
            doctorRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}