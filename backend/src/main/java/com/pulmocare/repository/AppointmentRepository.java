package com.pulmocare.repository;

import com.pulmocare.model.Appointment;
import com.pulmocare.model.Patient;
import com.pulmocare.model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    
    // Find appointments by patient
    List<Appointment> findByPatientId(String patientId);
    
    // Find appointments by doctor
    List<Appointment> findByDoctorId(String doctorId);
    
    // Find upcoming appointments by patient
    List<Appointment> findByPatientIdAndUpcomingTrue(String patientId);
    
    // Find past appointments by patient
    List<Appointment> findByPatientIdAndUpcomingFalse(String patientId);
    
    // Find upcoming appointments by doctor
    List<Appointment> findByDoctorIdAndUpcomingTrue(String doctorId);
    
    // Find past appointments by doctor
    List<Appointment> findByDoctorIdAndUpcomingFalse(String doctorId);
    
    // Find appointments by date
    List<Appointment> findByDate(LocalDate date);
    
    // Find appointments by doctor and date
    List<Appointment> findByDoctorIdAndDate(String doctorId, LocalDate date);
    
    // Find vaccine appointments by patient
    List<Appointment> findByPatientIdAndIsVaccineTrue(String patientId);
}
