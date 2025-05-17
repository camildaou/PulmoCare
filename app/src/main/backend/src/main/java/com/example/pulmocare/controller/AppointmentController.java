package com.example.pulmocare.controller;

import com.example.pulmocare.model.Appointment;
import com.example.pulmocare.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;
    
    // Get all appointments
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }
    
    // Get appointment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable String id) {
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isPresent()) {
            return new ResponseEntity<>(appointment.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // Create a new appointment
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        Appointment newAppointment = appointmentRepository.save(appointment);
        return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
    }
    
    // Update an appointment
    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable String id, @RequestBody Appointment appointment) {
        if (!appointmentRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        appointment.setId(id);
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
    }
    
    // Delete an appointment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String id) {
        if (!appointmentRepository.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        appointmentRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    // Get appointments by doctor
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctor(@PathVariable String doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctor(doctorId);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }
    
    // Get upcoming appointments
    @GetMapping("/upcoming")
    public ResponseEntity<List<Appointment>> getUpcomingAppointments() {
        List<Appointment> appointments = appointmentRepository.findByUpcoming(true);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }
    
    // Get vaccine appointments
    @GetMapping("/vaccine")
    public ResponseEntity<List<Appointment>> getVaccineAppointments() {
        List<Appointment> appointments = appointmentRepository.findByVaccine(true);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }
}
