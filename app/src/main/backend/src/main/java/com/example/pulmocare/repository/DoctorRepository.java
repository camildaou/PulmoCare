package com.example.pulmocare.repository;

import com.example.pulmocare.model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends MongoRepository<Doctor, String> {
    List<Doctor> findBySpecialty(String specialty);
    Optional<Doctor> findByEmail(String email);
    List<Doctor> findByLocationContaining(String location);
    List<Doctor> findByCountryCode(String countryCode);
}