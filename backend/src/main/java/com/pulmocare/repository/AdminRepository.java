package com.pulmocare.repository;

import com.pulmocare.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdminRepository extends MongoRepository<Admin, String> {
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    Optional<Admin> findByEmail(String email);
}
