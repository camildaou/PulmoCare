package com.pulmocare.controller;

import com.pulmocare.model.Appointment;
import com.pulmocare.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
      /**
     * Create a new appointment
     */
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
    try {
        // Log the incoming appointment data for debugging
        System.out.println("Creating appointment with: " + 
                          "Date=" + appointment.getDate() + 
                          ", StartTime=" + appointment.getHour() + 
                          ", EndTime=" + appointment.getEndTimeStr() +
                          ", DoctorId=" + (appointment.getDoctor() != null ? appointment.getDoctor().getId() : "null"));
        
        Appointment newAppointment = appointmentService.createAppointment(appointment);
        return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
    } catch (Exception e) {
        // Log the detailed error information
        System.err.println("Error creating appointment: " + e.getMessage());
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
    }
}
    
    /**
     * Get all appointments
     */
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        try {
            List<Appointment> appointments = appointmentService.getAllAppointments();
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Get appointment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable String id) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            return new ResponseEntity<>(appointment, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return only the status for not found cases
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Get appointments by patient ID
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatientId(@PathVariable String patientId) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByPatientId(patientId);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Get appointments by doctor ID
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorId(@PathVariable String doctorId) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Get upcoming appointments by patient ID
     */
    @GetMapping("/patient/{patientId}/upcoming")
    public ResponseEntity<List<Appointment>> getUpcomingAppointmentsByPatientId(@PathVariable String patientId) {
        try {
            List<Appointment> appointments = appointmentService.getUpcomingAppointmentsByPatientId(patientId);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Get past appointments by patient ID
     */
    @GetMapping("/patient/{patientId}/past")
    public ResponseEntity<List<Appointment>> getPastAppointmentsByPatientId(@PathVariable String patientId) {
        try {
            List<Appointment> appointments = appointmentService.getPastAppointmentsByPatientId(patientId);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Get upcoming appointments by doctor ID
     */
    @GetMapping("/doctor/{doctorId}/upcoming")
    public ResponseEntity<List<Appointment>> getUpcomingAppointmentsByDoctorId(@PathVariable String doctorId) {
        try {
            List<Appointment> appointments = appointmentService.getUpcomingAppointmentsByDoctorId(doctorId);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Get past appointments by doctor ID
     */
    @GetMapping("/doctor/{doctorId}/past")
    public ResponseEntity<List<Appointment>> getPastAppointmentsByDoctorId(@PathVariable String doctorId) {
        try {
            List<Appointment> appointments = appointmentService.getPastAppointmentsByDoctorId(doctorId);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Get appointments by date
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByDate(date);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Get appointments by doctor and date
     */
    @GetMapping("/doctor/{doctorId}/date/{date}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctorAndDate(
            @PathVariable String doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<Appointment> appointments = appointmentService.getAppointmentsByDoctorAndDate(doctorId, date);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Update appointment
     */
    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable String id, @RequestBody Appointment appointment) {
        try {
            Appointment updatedAppointment = appointmentService.updateAppointment(id, appointment);
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return only the status for not found cases
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Mark appointment as past
     */
    @PatchMapping("/{id}/mark-past")
    public ResponseEntity<Appointment> markAppointmentAsPast(@PathVariable String id) {
        try {
            Appointment updatedAppointment = appointmentService.markAppointmentAsPast(id);
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return only the status for not found cases
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }
    
    /**
     * Delete appointment
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteAppointment(@PathVariable String id) {
        try {
            appointmentService.deleteAppointment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return only the status for error cases
        }
    }

    /**
     * Update appointment statuses based on current date/time
     * Useful for manually updating appointment statuses
     */
    @PostMapping("/update-statuses")
    public ResponseEntity<String> updateAppointmentStatuses() {
        try {
            appointmentService.updateAppointmentStatuses();
            return new ResponseEntity<>("Appointment statuses updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating appointment statuses: " + e.getMessage(), 
                                       HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a new appointment ensuring the date and time are in the doctor's available time slots
     */
    @PostMapping("/web-create-appt")
    public ResponseEntity<String> createAppointmentWithValidation(@RequestBody Appointment appointment) {
        try {
            // Log the incoming request data
            System.out.println("Received Appointment Request: " + appointment);

            // Log the validation step
            System.out.println("Validating time slot for Doctor ID: " + appointment.getDoctor().getId() + ", Date: " + appointment.getDate() + ", Time: " + appointment.getHour());

            boolean isAvailable = appointmentService.isTimeSlotAvailable(
                appointment.getDoctor().getId(), 
                appointment.getDate(), 
                appointment.getHour().toString() // Convert LocalTime to String
            );            // Log the result of the validation
            System.out.println("Time slot availability: " + isAvailable);

            if (!isAvailable) {
                System.out.println("Time slot not available for Doctor ID: " + appointment.getDoctor().getId());
                // Return 200 OK with a specific message that the frontend can interpret
                return new ResponseEntity<>("TIME_SLOT_UNAVAILABLE", HttpStatus.OK);
            }

            // Log the default value assignment step
            System.out.println("Setting default values for optional fields.");            // Set additional fields to null if not provided
            appointment.setLocation(null);
            appointment.setDiagnosis(null);
            appointment.setPersonalNotes(null);
            appointment.setPlan(null);
            appointment.setPrescription(null);
            appointment.setReportPending(false);
            appointment.setUpcoming(true);
            appointment.setVaccine(false);

            System.out.println("Creating appointment: " + appointment);
            appointmentService.createAppointment(appointment);
            System.out.println("Appointment created successfully.");

            return new ResponseEntity<>("Appointment created successfully.", HttpStatus.CREATED);
        } catch (Exception e) {
            // Log detailed error information
            System.err.println("Error creating appointment: " + e.getMessage());
            e.printStackTrace();

            // Include detailed error message in the response
            return new ResponseEntity<>("An error occurred while creating the appointment: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get the currently ongoing appointment for a doctor
     */
    @GetMapping("/doctor/{doctorId}/ongoing")
    public ResponseEntity<Appointment> getCurrentOngoingAppointmentForDoctor(@PathVariable String doctorId) {
        try {
            Appointment ongoingAppointment = appointmentService.getCurrentOngoingAppointmentForDoctor(doctorId);
            if (ongoingAppointment == null) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // No ongoing appointment
            }
            return new ResponseEntity<>(ongoingAppointment, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting ongoing appointment: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get today's appointments for a doctor
     */
    @GetMapping("/doctor/{doctorId}/today")
    public ResponseEntity<List<Appointment>> getTodaysAppointmentsForDoctor(@PathVariable String doctorId) {
        try {
            List<Appointment> todaysAppointments = appointmentService.getTodaysAppointmentsForDoctor(doctorId);
            return new ResponseEntity<>(todaysAppointments, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting today's appointments: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
