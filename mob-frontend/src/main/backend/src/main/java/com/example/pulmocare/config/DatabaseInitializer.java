package com.example.pulmocare.config;

import com.example.pulmocare.model.Doctor;
import com.example.pulmocare.model.User;
import com.example.pulmocare.repository.DoctorRepository;
import com.example.pulmocare.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only initialize if the database is empty
        if (doctorRepository.count() == 0 && userRepository.count() == 0) {
            initializeSampleDoctors();
            initializeSampleUsers();
        }
    }

    private void initializeSampleDoctors() {
        List<Doctor> doctors = Arrays.asList(
            createDoctor(
                "Dr. Jane Smith",
                "Pulmonologist",
                "https://randomuser.me/api/portraits/women/44.jpg",
                4.9f, 124,
                "Memorial Hospital, New York",
                "+1-555-123-4567",
                "jane.smith@example.com",
                "Dr. Jane Smith is a board-certified pulmonologist with over 15 years of experience in treating respiratory conditions. She specializes in asthma, COPD, and sleep apnea.",
                Arrays.asList("Monday", "Tuesday", "Thursday", "Friday")
            ),
            createDoctor(
                "Dr. Michael Chen",
                "Respiratory Therapist",
                "https://randomuser.me/api/portraits/men/32.jpg",
                4.7f, 98,
                "City Medical Center, Boston",
                "+1-555-987-6543",
                "michael.chen@example.com",
                "Dr. Michael Chen is a respiratory therapist who specializes in pulmonary rehabilitation and breathing exercises for patients with chronic lung diseases.",
                Arrays.asList("Monday", "Wednesday", "Friday")
            ),
            createDoctor(
                "Dr. Sarah Johnson",
                "Pulmonologist",
                "https://randomuser.me/api/portraits/women/67.jpg",
                4.8f, 156,
                "Riverside Hospital, Chicago",
                "+1-555-456-7890",
                "sarah.johnson@example.com",
                "Dr. Sarah Johnson is a pulmonologist with expertise in interventional pulmonology and advanced diagnostic techniques for lung cancer and other respiratory conditions.",
                Arrays.asList("Tuesday", "Wednesday", "Thursday", "Saturday")
            ),
            createDoctor(
                "Dr. Robert Williams",
                "Allergist",
                "https://randomuser.me/api/portraits/men/52.jpg",
                4.5f, 87,
                "Allergy & Asthma Center, San Francisco",
                "+1-555-234-5678",
                "robert.williams@example.com",
                "Dr. Robert Williams is an allergist who specializes in treating respiratory allergies, asthma, and other immune system disorders affecting the respiratory system.",
                Arrays.asList("Monday", "Thursday", "Friday", "Saturday")
            )
        );

        doctorRepository.saveAll(doctors);
    }
    
    private Doctor createDoctor(String name, String specialty, String image, Float rating, 
                              Integer reviews, String location, String phone, String email, 
                              String bio, List<String> availability) {
        Doctor doctor = new Doctor();
        doctor.setName(name);
        doctor.setSpecialty(specialty);
        doctor.setImage(image);
        doctor.setRating(rating);
        doctor.setReviews(reviews);
        doctor.setLocation(location);
        doctor.setPhone(phone);
        doctor.setEmail(email);
        doctor.setBio(bio);
        doctor.setAvailability(availability);
        return doctor;
    }
    
    private void initializeSampleUsers() {
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setPhone("+1-555-111-2222");
        admin.setAddress("123 Main St, Anytown, USA");
        admin.setDateOfBirth(LocalDate.of(1985, 5, 15));
        admin.setInsurance("National Health Insurance");
        admin.setProfileImage("https://randomuser.me/api/portraits/lego/1.jpg");
        
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setPhone("+1-555-333-4444");
        user.setAddress("456 Elm St, Somewhere, USA");
        user.setDateOfBirth(LocalDate.of(1990, 8, 20));
        user.setInsurance("Medical Plus");
        user.setProfileImage("https://randomuser.me/api/portraits/lego/2.jpg");
        
        userRepository.saveAll(Arrays.asList(admin, user));
    }
}