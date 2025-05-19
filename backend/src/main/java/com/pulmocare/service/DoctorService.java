package com.pulmocare.service;

import com.pulmocare.model.Doctor;
import com.pulmocare.model.Appointment;
import com.pulmocare.repository.DoctorRepository;
import com.pulmocare.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

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
        
        // Update availability fields if they are set
        if (doctorDetails.getAvailableDays() != null) {
            doctor.setAvailableDays(doctorDetails.getAvailableDays());
        }
        
        if (doctorDetails.getAvailableTimeSlots() != null) {
            doctor.setAvailableTimeSlots(doctorDetails.getAvailableTimeSlots());
        }
        
        if (doctorDetails.getUnavailableDates() != null) {
            doctor.setUnavailableDates(doctorDetails.getUnavailableDates());
        }
        
        // Note: We don't update password or email here as those should be separate operations

        return doctorRepository.save(doctor);
    }
    
    /**
     * Check if a time slot is available for a specific doctor on a specific date
     * 
     * @param doctorId the ID of the doctor
     * @param date the date to check
     * @param startTime the start time of the appointment (HH:MM)
     * @param endTime the end time of the appointment (HH:MM)
     * @return true if the time slot is available, false otherwise
     */
    public boolean isTimeSlotAvailable(String doctorId, LocalDate date, String startTime, String endTime) {
        Doctor doctor = getDoctorById(doctorId);
        
        // Check if the date is in the unavailable dates list
        if (doctor.getUnavailableDates() != null && 
            doctor.getUnavailableDates().contains(date.toString())) {
            return false;
        }
        
        // Get the day of the week (lowercase)
        String dayOfWeek = date.getDayOfWeek().toString().toLowerCase().substring(0, 3);
        
        // Check if the doctor works on this day
        if (!doctor.getAvailableDays().contains(dayOfWeek)) {
            return false;
        }
        
        // Get the available time slots for this day
        List<Doctor.TimeSlot> availableSlots = doctor.getAvailableTimeSlots().get(dayOfWeek);
        if (availableSlots == null || availableSlots.isEmpty()) {
            return false;
        }
        
        // Check if the requested time slot is in the doctor's available time slots
        boolean timeSlotAvailable = false;
        for (Doctor.TimeSlot slot : availableSlots) {
            if (slot.getStartTime().equals(startTime) && slot.getEndTime().equals(endTime)) {
                timeSlotAvailable = true;
                break;
            }
        }
        if (!timeSlotAvailable) {
            return false;
        }
        
        // Check if there are any existing appointments at this time
        LocalTime localStartTime = LocalTime.parse(startTime);
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctorId, date);
        for (Appointment appointment : appointments) {
            if (appointment.getHour().equals(localStartTime)) {
                return false; // Time slot is already booked
            }
        }
        
        return true;
    }
}
