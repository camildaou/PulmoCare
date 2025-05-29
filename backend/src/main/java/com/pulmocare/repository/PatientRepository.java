package com.pulmocare.repository;

import com.pulmocare.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PatientRepository extends MongoRepository<Patient, String> {
    boolean existsByEmail(String email);
    Optional<Patient> findByEmail(String email);
}
