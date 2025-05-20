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
import java.time.format.DateTimeFormatter;
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
        
        // Validate that the appointment time is available for the doctor
        validateAppointmentTime(appointment);
        
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
        updateAppointmentStatuses();
        return appointmentRepository.findByPatientIdAndUpcomingTrue(patientId);
    }
    
    /**
     * Get past appointments by patient ID
     */
    public List<Appointment> getPastAppointmentsByPatientId(String patientId) {
        updateAppointmentStatuses();
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
     * Update appointment statuses based on current date and time
     * This method automatically marks appointments as past if their date/time has passed
     */
    public void updateAppointmentStatuses() {
        // Get current date and time
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        // Get all upcoming appointments
        List<Appointment> upcomingAppointments = appointmentRepository.findByUpcomingTrue();
        
        // Check each appointment to see if it's in the past
        for (Appointment appointment : upcomingAppointments) {
            if (appointment.getDate().isBefore(currentDate) || 
                (appointment.getDate().isEqual(currentDate) && 
                 appointment.getHour().isBefore(currentTime))) {
                // Mark as past
                appointment.setUpcoming(false);
                appointmentRepository.save(appointment);
                System.out.println("Marked appointment " + appointment.getId() + " as past");
            }
        }
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
        
        // Check if the date or time is being changed, and validate availability if so
        boolean timeChanged = (appointmentDetails.getDate() != null && !appointmentDetails.getDate().equals(appointment.getDate())) ||
                             (appointmentDetails.getHour() != null && !appointmentDetails.getHour().equals(appointment.getHour()));
        
        if (timeChanged && appointmentDetails.getDoctor() != null) {
            // Create a temporary appointment for validation
            Appointment tempAppointment = new Appointment();
            tempAppointment.setDoctor(appointmentDetails.getDoctor());
            tempAppointment.setDate(appointmentDetails.getDate() != null ? appointmentDetails.getDate() : appointment.getDate());
            tempAppointment.setHour(appointmentDetails.getHour() != null ? appointmentDetails.getHour() : appointment.getHour());
            
            // Validate the new time slot
            validateAppointmentTime(tempAppointment);
        }
        
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
    
    /**
     * Validate that the appointment time is available for the doctor
     */    private void validateAppointmentTime(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null || 
            appointment.getDate() == null || appointment.getHour() == null) {
            throw new RuntimeException("Doctor, date, and time are required for appointment validation");
        }
        
        Doctor doctor = appointment.getDoctor();
        LocalDate date = appointment.getDate();
        LocalTime startTime = appointment.getHour();
          // Calculate end time (30 minutes later) if not provided
        LocalTime endTime;
        String endTimeStr;
        
        System.out.println("Validating appointment time: Doctor ID=" + doctor.getId() + 
                          ", Date=" + date + ", StartTime=" + startTime +
                          ", Provided EndTimeStr=" + appointment.getEndTimeStr());
        
        if (appointment.getEndTimeStr() != null && !appointment.getEndTimeStr().isEmpty()) {
            // Use provided end time string
            endTimeStr = appointment.getEndTimeStr();
            System.out.println("Using provided end time: " + endTimeStr);
            
            // Parse to LocalTime if needed for comparisons
            String[] parts = endTimeStr.split(":");
            if (parts.length == 2) {
                try {
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    endTime = LocalTime.of(hour, minute);
                } catch (NumberFormatException e) {
                    // Fallback to calculated end time
                    endTime = startTime.plusMinutes(30);
                    endTimeStr = String.format("%02d:%02d", endTime.getHour(), endTime.getMinute());
                    System.out.println("Invalid end time format, falling back to calculated: " + endTimeStr);
                }
            } else {
                // Fallback to calculated end time
                endTime = startTime.plusMinutes(30);
                endTimeStr = String.format("%02d:%02d", endTime.getHour(), endTime.getMinute());
                System.out.println("Invalid end time format, falling back to calculated: " + endTimeStr);
            }
        } else {
            // Calculate end time (30 minutes later)
            endTime = startTime.plusMinutes(30);
            endTimeStr = String.format("%02d:%02d", endTime.getHour(), endTime.getMinute());
            System.out.println("No end time provided, calculated end time: " + endTimeStr);
        }
        
        // Format times as HH:MM for validation
        String startTimeStr = String.format("%02d:%02d", startTime.getHour(), startTime.getMinute());
        System.out.println("Using formatted start time: " + startTimeStr + " and end time: " + endTimeStr);
        
        // Check if the date is in the unavailable dates list
        if (doctor.getUnavailableDates() != null && 
            doctor.getUnavailableDates().contains(date.toString())) {
            throw new RuntimeException("Doctor is unavailable on this date");
        }
        
        // Get the day of the week (lowercase)
        String dayOfWeek = date.getDayOfWeek().toString().toLowerCase().substring(0, 3);
        
        // Check if the doctor works on this day
        if (!doctor.getAvailableDays().contains(dayOfWeek)) {
            throw new RuntimeException("Doctor doesn't work on " + date.getDayOfWeek().toString());
        }
          // Get the available time slots for this day
        List<Doctor.TimeSlot> availableSlots = doctor.getAvailableTimeSlots().get(dayOfWeek);
        if (availableSlots == null || availableSlots.isEmpty()) {
            throw new RuntimeException("Doctor has no available time slots on this day");
        }
        
        System.out.println("Available time slots for doctor " + doctor.getId() + " on " + dayOfWeek + ":");
        for (Doctor.TimeSlot slot : availableSlots) {
            System.out.println("  - " + slot.getStartTime() + " to " + slot.getEndTime());
        }        // Check if the requested time slot is in the doctor's available time slots
        boolean timeSlotAvailable = false;
        
        System.out.println("Looking for an available time slot...");
        
        // Parse start and end times for comparison
        LocalTime requestedStartTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime requestedEndTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        
        // Check if the requested time slot falls within any of the doctor's available time slots
        for (Doctor.TimeSlot slot : availableSlots) {
            LocalTime doctorStartTime = LocalTime.parse(slot.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime doctorEndTime = LocalTime.parse(slot.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
            
            System.out.println("Checking if " + startTimeStr + "-" + endTimeStr + 
                              " falls within doctor's slot " + slot.getStartTime() + "-" + slot.getEndTime());
            
            // Check if the appointment start time is equal to or after the doctor's start time
            // AND the appointment end time is equal to or before the doctor's end time
            if ((requestedStartTime.equals(doctorStartTime) || requestedStartTime.isAfter(doctorStartTime)) && 
                (requestedEndTime.equals(doctorEndTime) || requestedEndTime.isBefore(doctorEndTime))) {
                
                System.out.println("Found matching time slot: " + startTimeStr + "-" + endTimeStr + 
                                  " fits within " + slot.getStartTime() + "-" + slot.getEndTime());
                timeSlotAvailable = true;
                break;
            }
            
            // Also allow exact start time match with end time that fits within the doctor's slot
            if (requestedStartTime.equals(doctorStartTime) && 
                (requestedEndTime.equals(doctorEndTime) || requestedEndTime.isBefore(doctorEndTime))) {
                
                System.out.println("Found slot with matching start time and end time within doctor's slot");
                timeSlotAvailable = true;
                break;
            }
        }
          if (!timeSlotAvailable) {
            System.out.println("No matching time slot found!");
            throw new RuntimeException("The requested time slot is not in the doctor's available time slots");
        }
        System.out.println("Time slot is available");
        
        // Check if there are any existing appointments at this time
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndDate(doctor.getId(), date);
        for (Appointment existingAppointment : existingAppointments) {
            if (existingAppointment.getHour().equals(startTime)) {
                throw new RuntimeException("The doctor already has an appointment at this time");
            }
        }
    }
}
