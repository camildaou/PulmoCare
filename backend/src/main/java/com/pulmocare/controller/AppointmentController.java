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
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
