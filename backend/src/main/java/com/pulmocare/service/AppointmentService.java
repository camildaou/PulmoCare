package com.pulmocare.service;

import com.pulmocare.model.Appointment;
import com.pulmocare.model.Patient;
import com.pulmocare.model.Doctor;
import com.pulmocare.repository.AppointmentRepository;
import com.pulmocare.repository.PatientRepository;
import com.pulmocare.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    /**
     * Create a new appointment
     */
    public Appointment createAppointment(Appointment appointment) {
        // Set the appointment as upcoming by default
        appointment.setUpcoming(true);
        
        // Check if the referenced patient and doctor exist
        validatePatientAndDoctor(appointment);
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Get all appointments
     */
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
    
    /**
     * Get appointment by ID
     */
    public Appointment getAppointmentById(String id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
    }
    
    /**
     * Get appointments by patient ID
     */
    public List<Appointment> getAppointmentsByPatientId(String patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }
    
    /**
     * Get appointments by doctor ID
     */
    public List<Appointment> getAppointmentsByDoctorId(String doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }
    
    /**
     * Get upcoming appointments by patient ID
     */
    public List<Appointment> getUpcomingAppointmentsByPatientId(String patientId) {
        return appointmentRepository.findByPatientIdAndUpcomingTrue(patientId);
    }
    
    /**
     * Get past appointments by patient ID
     */
    public List<Appointment> getPastAppointmentsByPatientId(String patientId) {
        return appointmentRepository.findByPatientIdAndUpcomingFalse(patientId);
    }
    
    /**
     * Get upcoming appointments by doctor ID
     */
    public List<Appointment> getUpcomingAppointmentsByDoctorId(String doctorId) {
        return appointmentRepository.findByDoctorIdAndUpcomingTrue(doctorId);
    }
    
    /**
     * Get past appointments by doctor ID
     */
    public List<Appointment> getPastAppointmentsByDoctorId(String doctorId) {
        return appointmentRepository.findByDoctorIdAndUpcomingFalse(doctorId);
    }
    
    /**
     * Get appointments by date
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByDate(date);
    }
    
    /**
     * Get appointments by doctor and date
     */
    public List<Appointment> getAppointmentsByDoctorAndDate(String doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndDate(doctorId, date);
    }
    
    /**
     * Update appointment details
     */
    public Appointment updateAppointment(String id, Appointment appointmentDetails) {
        Appointment appointment = getAppointmentById(id);
        
        // Update fields from the details object
        if (appointmentDetails.getDate() != null) {
            appointment.setDate(appointmentDetails.getDate());
        }
        
        if (appointmentDetails.getHour() != null) {
            appointment.setHour(appointmentDetails.getHour());
        }
        
        if (appointmentDetails.getAssessmentInfo() != null) {
            appointment.setAssessmentInfo(appointmentDetails.getAssessmentInfo());
        }
        
        appointment.setReportPending(appointmentDetails.isReportPending());
        
        if (appointmentDetails.getDiagnosis() != null) {
            appointment.setDiagnosis(appointmentDetails.getDiagnosis());
        }
        
        if (appointmentDetails.getPersonalNotes() != null) {
            appointment.setPersonalNotes(appointmentDetails.getPersonalNotes());
        }
        
        if (appointmentDetails.getPlan() != null) {
            appointment.setPlan(appointmentDetails.getPlan());
        }
        
        if (appointmentDetails.getLocation() != null) {
            appointment.setLocation(appointmentDetails.getLocation());
        }
        
        if (appointmentDetails.getReason() != null) {
            appointment.setReason(appointmentDetails.getReason());
        }
        
        appointment.setUpcoming(appointmentDetails.isUpcoming());
        appointment.setVaccine(appointmentDetails.isVaccine());
        
        // Save and return the updated appointment
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Mark an appointment as past (completed)
     */
    public Appointment markAppointmentAsPast(String id) {
        Appointment appointment = getAppointmentById(id);
        appointment.setUpcoming(false);
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Cancel (delete) an appointment
     */
    public void deleteAppointment(String id) {
        appointmentRepository.deleteById(id);
    }
    
    /**
     * Validate if the patient and doctor in the appointment exist
     */
    private void validatePatientAndDoctor(Appointment appointment) {
        if (appointment.getPatient() != null && appointment.getPatient().getId() != null) {
            Patient patient = patientRepository.findById(appointment.getPatient().getId())
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + appointment.getPatient().getId()));
            appointment.setPatient(patient);
        }
        
        if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
            Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + appointment.getDoctor().getId()));
            appointment.setDoctor(doctor);
        }
    }
}
