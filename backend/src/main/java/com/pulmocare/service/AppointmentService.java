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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        
        // Save the appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Remove the booked time slot from the doctor's availability
        removeBookedTimeSlot(appointment);
        
        return savedAppointment;
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
        
        appointment.setReportPending(appointmentDetails.isReportPending());
          if (appointmentDetails.getDiagnosis() != null) {
            appointment.setDiagnosis(appointmentDetails.getDiagnosis());
        }
        
        if (appointmentDetails.getPrescription() != null) {
            appointment.setPrescription(appointmentDetails.getPrescription());
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
     * Cancel (delete) an appointment and restore the time slot to the doctor's availability
     */
public void deleteAppointment(String id) {
    // Get the appointment before deleting it
    Appointment appointmentToDelete = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
    
    // Restore the time slot to the doctor's availability before deleting
    restoreTimeSlotToDoctor(appointmentToDelete);
    
    // Delete the appointment
    appointmentRepository.deleteById(id);
}

private void restoreTimeSlotToDoctor(Appointment appointment) {
    try {
        // Get the doctor ID from the appointment
        String doctorId = appointment.getDoctor().getId();
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        
        // Get the day of the week from the appointment date
        LocalDate appointmentDate = LocalDate.parse(appointment.getDate());
        String dayName = appointmentDate.getDayOfWeek().toString().substring(0, 3).toLowerCase();
          // Get the start and end times
        String appointmentHour = appointment.getHour();
        String startTimeStr;
        
        // Check if appointmentHour is a LocalTime or a String
        if (appointmentHour instanceof String) {
            // Format might be "08:00 AM" or "08:00"
            startTimeStr = convertToStandardTimeFormat(appointmentHour);
        } else {
            // Handle case where appointmentHour is a LocalTime
            startTimeStr = appointmentHour;
        }
        
        // Calculate end time (assuming 30-minute appointments)
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = startTime.plusMinutes(30);
        String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
          // Get the doctor's availability for that day
        Map<String, List<Doctor.TimeSlot>> availability = doctor.getAvailableTimeSlots();
        if (availability == null) {
            availability = new HashMap<>();
            doctor.setAvailableTimeSlots(availability);
        }
        
        // Get or create time slots for the day
        List<Doctor.TimeSlot> timeSlots = availability.get(dayName);
        if (timeSlots == null) {
            timeSlots = new ArrayList<>();
            availability.put(dayName, timeSlots);
        }
        
        // Check if this time slot already exists
        boolean timeSlotExists = false;
        for (Doctor.TimeSlot slot : timeSlots) {
            if (slot.getStartTime().equals(startTimeStr)) {
                timeSlotExists = true;
                break;
            }
        }
        
        // Add the time slot back if it doesn't exist
        if (!timeSlotExists) {
            Doctor.TimeSlot newSlot = new Doctor.TimeSlot(startTimeStr, endTimeStr);
            timeSlots.add(newSlot);
            
            // Sort the time slots by start time for consistency
            timeSlots.sort((slot1, slot2) -> {
                LocalTime time1 = LocalTime.parse(slot1.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime time2 = LocalTime.parse(slot2.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                return time1.compareTo(time2);
            });
            
            // Save the updated doctor availability
            doctorRepository.save(doctor);
            System.out.println("Restored time slot " + startTimeStr + " to doctor's availability on " + dayName);
        }
    } catch (Exception e) {
        System.err.println("Error restoring time slot: " + e.getMessage());
        e.printStackTrace();
    }
}
/**
 * Converts a time string to standard "HH:mm" 24-hour format.
 */
private String convertToStandardTimeFormat(String timeStr) {
    try {
        // Try parsing as "HH:mm"
        LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    } catch (Exception e) {
        // Try parsing as "hh:mm a" (e.g., "08:00 AM")
        try {
            LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("hh:mm a"));
            return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ex) {
            throw new RuntimeException("Invalid time format: " + timeStr);
        }
    }
}
    /**
     * Validate if the patient and doctor in the appointment exist
     */
    private void validatePatientAndDoctor(Appointment appointment) {
```
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
    
    /**
     * Check if a time slot is available for a doctor on a specific date
     */
    public boolean isTimeSlotAvailable(String doctorId, LocalDate date, String time) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));

        String dayOfWeek = date.getDayOfWeek().toString().toLowerCase().substring(0, 3);
        List<Doctor.TimeSlot> availableSlots = doctor.getAvailableTimeSlots().get(dayOfWeek);

        if (availableSlots == null || availableSlots.isEmpty()) {
            return false;
        }

        return availableSlots.stream().anyMatch(slot ->
            slot.getStartTime().equals(time)
        );
    }
    
    /**
     * Remove the booked time slot from the doctor's availability
     */
    private void removeBookedTimeSlot(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        LocalDate date = appointment.getDate();
        String dayOfWeek = date.getDayOfWeek().toString().toLowerCase().substring(0, 3);

        List<Doctor.TimeSlot> availableSlots = doctor.getAvailableTimeSlots().get(dayOfWeek);
        if (availableSlots != null) {
            availableSlots.removeIf(slot -> slot.getStartTime().equals(appointment.getHour().toString()));

            // Update the doctor's availability in the repository
            doctorRepository.save(doctor);
        }
    }
    
    /**
     * Get the currently ongoing appointment for a doctor
     * An ongoing appointment is one that's happening today and its time slot includes the current time
     */
    public Appointment getCurrentOngoingAppointmentForDoctor(String doctorId) {
        // Get current date and time
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        
        // Find all appointments for today for this doctor
        List<Appointment> todaysAppointments = appointmentRepository.findByDoctorIdAndDate(doctorId, currentDate);
        
        // Find the appointment that is currently ongoing
        // An appointment is ongoing if the current time is between the start time and end time (assuming appointments are 30 minutes)
        for (Appointment appointment : todaysAppointments) {
            LocalTime appointmentStartTime = appointment.getHour();
            // Calculate end time (30 minutes after start)
            LocalTime appointmentEndTime = appointmentStartTime.plusMinutes(30);
            
            // Check if current time is within the appointment time slot
            if (currentTime.isAfter(appointmentStartTime) && currentTime.isBefore(appointmentEndTime)) {
                return appointment;
            }
        }
        
        // No ongoing appointment was found
        return null;
    }

    /**
     * Get appointments for today for a doctor, ordered by time
     */
    public List<Appointment> getTodaysAppointmentsForDoctor(String doctorId) {
        // Get current date
        LocalDate currentDate = LocalDate.now();
        
        // Find all appointments for today for this doctor
        List<Appointment> todaysAppointments = appointmentRepository.findByDoctorIdAndDate(doctorId, currentDate);
        
        // Sort by time
        todaysAppointments.sort((a1, a2) -> a1.getHour().compareTo(a2.getHour()));
        
        return todaysAppointments;
    }
}
