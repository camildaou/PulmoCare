package com.pulmocare.service;

import com.pulmocare.model.Doctor;
import com.pulmocare.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    // Get a single doctor by ID
    public Doctor getDoctorById(String id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
    }

    // Sign in doctor
    public Doctor signIn(String email, String password) {
        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found with email: " + email));
        
        if (!doctor.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        
        return doctor;
    }

    // Create new doctor (sign up)
    public Doctor createDoctor(Doctor doctor) {
        if (doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        return doctorRepository.save(doctor);
    }

    // Get all doctors
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // Get total number of doctors
    public long getDoctorCount() {
        return doctorRepository.countAllDoctors();
    }

    // Update doctor information
    public Doctor updateDoctor(String id, Doctor doctorDetails) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        doctor.setFirstName(doctorDetails.getFirstName());
        doctor.setLastName(doctorDetails.getLastName());
        doctor.setGender(doctorDetails.getGender());
        doctor.setAge(doctorDetails.getAge());
        doctor.setDescription(doctorDetails.getDescription());
        doctor.setLocation(doctorDetails.getLocation());
        doctor.setCountryCode(doctorDetails.getCountryCode());
        doctor.setPhone(doctorDetails.getPhone());
        doctor.setMedicalLicense(doctorDetails.getMedicalLicense());
        // Note: We don't update password or email here as those should be separate operations

        return doctorRepository.save(doctor);
    }
}
