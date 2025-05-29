package com.pulmocare.service;

import com.pulmocare.model.Admin;
import com.pulmocare.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    public Admin createAdmin(Admin admin) {
        // Check if admin with email already exists
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Check if admin with employeeId already exists
        if (adminRepository.existsByEmployeeId(admin.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists");
        }
        
        return adminRepository.save(admin);
    }

    public Admin updateAdmin(String id, Admin adminDetails) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));        // Update only the allowed fields
        admin.setFirstName(adminDetails.getFirstName());
        admin.setLastName(adminDetails.getLastName());
        admin.setGender(adminDetails.getGender());
        admin.setAge(adminDetails.getAge());        admin.setPhoneNumber(adminDetails.getPhoneNumber());
        admin.setLocation(adminDetails.getLocation());
        
        return adminRepository.save(admin);
    }

    public Admin signIn(String email, String password) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        if (!admin.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        
        return admin;
    }

    public Admin getAdmin(String id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }
}
