package com.example.pulmocare.repository;

import com.example.pulmocare.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    // Find appointments by doctor ID
    List<Appointment> findByDoctor(String doctorId);
    
    // Find upcoming appointments
    List<Appointment> findByUpcoming(Boolean upcoming);
    
    // Find vaccine appointments
    List<Appointment> findByVaccine(Boolean vaccine);
}
