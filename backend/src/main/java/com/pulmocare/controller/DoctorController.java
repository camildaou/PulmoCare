package com.pulmocare.controller;

import com.pulmocare.model.Doctor;
import com.pulmocare.model.Appointment;
import com.pulmocare.service.DoctorService;
import com.pulmocare.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private AppointmentService appointmentService;

    // Get doctor by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctor(@PathVariable String id) {
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            return ResponseEntity.ok(doctor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Sign in
    @PostMapping("/signin")
    public ResponseEntity<Doctor> signIn(@RequestBody Map<String, String> credentials) {
        return ResponseEntity.ok(
            doctorService.signIn(credentials.get("email"), credentials.get("password"))
        );
    }

    // Sign up
    @PostMapping("/signup")
    public ResponseEntity<?> createDoctor(@RequestBody Doctor doctor) {
        try {
            Doctor createdDoctor = doctorService.createDoctor(doctor);
            return ResponseEntity.ok(createdDoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all doctors
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    // Get total number of doctors
    @GetMapping("/count")
    public ResponseEntity<Long> getDoctorCount() {
        return ResponseEntity.ok(doctorService.getDoctorCount());
    }

    // Update doctor
    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable String id, @RequestBody Doctor doctorDetails) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, doctorDetails));
    }
    
    // Get doctor availability
    @GetMapping("/{id}/availability")
    public ResponseEntity<?> getDoctorAvailability(@PathVariable String id) {
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            
            Map<String, Object> availabilityData = Map.of(
                "availableDays", doctor.getAvailableDays(),
                "availableTimeSlots", doctor.getAvailableTimeSlots(),
                "unavailableDates", doctor.getUnavailableDates()
            );
            
            return ResponseEntity.ok(availabilityData);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Update doctor availability
    @PutMapping("/{id}/availability")
    public ResponseEntity<?> updateDoctorAvailability(
            @PathVariable String id,
            @RequestBody Map<String, Object> availabilityDetails) {
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            
            // Update available days if provided
            if (availabilityDetails.containsKey("availableDays")) {
                @SuppressWarnings("unchecked")
                List<String> availableDays = (List<String>) availabilityDetails.get("availableDays");
                doctor.setAvailableDays(availableDays);
            }
              // Update available time slots if provided
            if (availabilityDetails.containsKey("availableTimeSlots")) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, List<Map<String, String>>> rawTimeSlots = 
                        (Map<String, List<Map<String, String>>>) availabilityDetails.get("availableTimeSlots");
                    
                    // Convert raw map to proper TimeSlot objects with validation
                    Map<String, List<Doctor.TimeSlot>> processedTimeSlots = new java.util.HashMap<>();
                    
                    for (Map.Entry<String, List<Map<String, String>>> entry : rawTimeSlots.entrySet()) {
                        String day = entry.getKey();
                        List<Map<String, String>> slots = entry.getValue();
                        List<Doctor.TimeSlot> timeSlots = new java.util.ArrayList<>();
                        
                        for (Map<String, String> slot : slots) {
                            String startTime = slot.get("startTime");
                            String endTime = slot.get("endTime");
                            
                            // This will validate 30-minute intervals
                            Doctor.TimeSlot timeSlot = new Doctor.TimeSlot(startTime, endTime);
                            timeSlots.add(timeSlot);
                        }
                        
                        processedTimeSlots.put(day, timeSlots);
                    }
                    
                    doctor.setAvailableTimeSlots(processedTimeSlots);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Invalid time slot: " + e.getMessage());
                }
            }
            
            // Update unavailable dates if provided
            if (availabilityDetails.containsKey("unavailableDates")) {
                @SuppressWarnings("unchecked")
                List<String> unavailableDates = (List<String>) availabilityDetails.get("unavailableDates");
                doctor.setUnavailableDates(unavailableDates);
            }
            
            // Save the updated doctor
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctor);
            return ResponseEntity.ok(updatedDoctor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Add time slot to specific day
    @PostMapping("/{id}/availability/timeslot")
    public ResponseEntity<?> addTimeSlot(
            @PathVariable String id,
            @RequestBody Map<String, Object> timeSlotDetails) {
        try {
            // Validate input
            if (!timeSlotDetails.containsKey("day") || 
                !timeSlotDetails.containsKey("startTime") || 
                !timeSlotDetails.containsKey("endTime")) {
                return ResponseEntity.badRequest().body("Day, startTime, and endTime are required");
            }
            
            String day = (String) timeSlotDetails.get("day");
            String startTime = (String) timeSlotDetails.get("startTime");
            String endTime = (String) timeSlotDetails.get("endTime");
            
            // Validate that the time slot is exactly 30 minutes
            try {
                Doctor doctor = doctorService.getDoctorById(id);
                
                // Create new TimeSlot (this will validate the 30-minute duration)
                Doctor.TimeSlot newTimeSlot = new Doctor.TimeSlot(startTime, endTime);
                
                // Get or initialize the time slots for this day
                Map<String, List<Doctor.TimeSlot>> availableTimeSlots = doctor.getAvailableTimeSlots();
                if (!availableTimeSlots.containsKey(day)) {
                    availableTimeSlots.put(day, new java.util.ArrayList<>());
                }
                
                // Add the new time slot
                availableTimeSlots.get(day).add(newTimeSlot);
                
                // Ensure the day is in the availableDays list
                List<String> availableDays = doctor.getAvailableDays();
                if (!availableDays.contains(day)) {
                    availableDays.add(day);
                }
            
                // Save the updated doctor
                Doctor updatedDoctor = doctorService.updateDoctor(id, doctor);
                return ResponseEntity.ok(updatedDoctor);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid time slot: " + e.getMessage());
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Check if a specific time slot is available for a doctor on a date
     */
    @GetMapping("/{id}/availability/check")
    public ResponseEntity<?> checkTimeSlotAvailability(
            @PathVariable String id,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            boolean isAvailable = doctorService.isTimeSlotAvailable(id, localDate, startTime, endTime);
            
            Map<String, Object> response = Map.of(
                "available", isAvailable,
                "doctorId", id,
                "date", date,
                "startTime", startTime,
                "endTime", endTime
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Set standard weekly schedule with 30-minute slots
     */
    @PostMapping("/{id}/availability/standard-schedule")
    public ResponseEntity<?> setStandardSchedule(
            @PathVariable String id,
            @RequestBody Map<String, Object> scheduleDetails) {
        try {
            if (!scheduleDetails.containsKey("workDays") || !scheduleDetails.containsKey("workHours")) {
                return ResponseEntity.badRequest().body("workDays and workHours are required");
            }
            
            @SuppressWarnings("unchecked")
            List<String> workDays = (List<String>) scheduleDetails.get("workDays");
            
            @SuppressWarnings("unchecked")
            Map<String, String> workHours = (Map<String, String>) scheduleDetails.get("workHours");
            
            if (!workHours.containsKey("start") || !workHours.containsKey("end")) {
                return ResponseEntity.badRequest().body("workHours must contain start and end times");
            }
            
            String startWork = workHours.get("start");
            String endWork = workHours.get("end");
            
            // Validate time format
            if (!startWork.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$") ||
                !endWork.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                return ResponseEntity.badRequest().body("Time must be in format HH:MM");
            }
            
            Doctor doctor = doctorService.getDoctorById(id);
            
            // Set the available days
            doctor.setAvailableDays(workDays);
            
            // Generate 30-minute time slots for each work day
            Map<String, List<Doctor.TimeSlot>> availableTimeSlots = new java.util.HashMap<>();
            
            // Parse start and end times
            String[] startParts = startWork.split(":");
            String[] endParts = endWork.split(":");
            
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            
            // Create LocalTime objects for start and end
            LocalTime startTime = LocalTime.of(startHour, startMinute);
            LocalTime endTime = LocalTime.of(endHour, endMinute);
            
            // Generate 30-minute slots
            for (String day : workDays) {
                List<Doctor.TimeSlot> daySlots = new java.util.ArrayList<>();
                
                LocalTime currentTime = startTime;
                while (currentTime.plusMinutes(30).compareTo(endTime) <= 0) {
                    String currentTimeStr = String.format("%02d:%02d", currentTime.getHour(), currentTime.getMinute());
                    String nextTimeStr = String.format("%02d:%02d", currentTime.plusMinutes(30).getHour(), 
                                                      currentTime.plusMinutes(30).getMinute());
                    
                    Doctor.TimeSlot slot = new Doctor.TimeSlot(currentTimeStr, nextTimeStr);
                    daySlots.add(slot);
                    
                    currentTime = currentTime.plusMinutes(30);
                }
                
                availableTimeSlots.put(day, daySlots);
            }
            
            doctor.setAvailableTimeSlots(availableTimeSlots);
            
            // Save the updated doctor
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctor);
            return ResponseEntity.ok(updatedDoctor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
      /**
     * Get available time slots for a specific date
     */
    @GetMapping("/{id}/availability/slots")
    public ResponseEntity<?> getAvailableTimeSlots(
            @PathVariable String id,
            @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            
            // Get the doctor
            Doctor doctor = doctorService.getDoctorById(id);
            
            // Check if date is in unavailable dates
            if (doctor.getUnavailableDates() != null && 
                doctor.getUnavailableDates().contains(date)) {
                return ResponseEntity.ok(Map.of("availableSlots", new java.util.ArrayList<>()));
            }
            
            // Get day of week
            String dayOfWeek = localDate.getDayOfWeek().toString().toLowerCase().substring(0, 3);
            
            // Check if the doctor works on this day
            if (!doctor.getAvailableDays().contains(dayOfWeek)) {
                return ResponseEntity.ok(Map.of("availableSlots", new java.util.ArrayList<>()));
            }
            
            // Get working hours for this day
            List<Doctor.TimeSlot> workDaySlots = doctor.getAvailableTimeSlots().get(dayOfWeek);
            if (workDaySlots == null || workDaySlots.isEmpty()) {
                return ResponseEntity.ok(Map.of("availableSlots", new java.util.ArrayList<>()));
            }
            
            // Determine working hours boundary
            LocalTime startWorkingHour = null;
            LocalTime endWorkingHour = null;
            
            for (Doctor.TimeSlot slot : workDaySlots) {
                String[] startParts = slot.getStartTime().split(":");
                LocalTime slotStartTime = LocalTime.of(
                    Integer.parseInt(startParts[0]), 
                    Integer.parseInt(startParts[1])
                );
                
                String[] endParts = slot.getEndTime().split(":");
                LocalTime slotEndTime = LocalTime.of(
                    Integer.parseInt(endParts[0]), 
                    Integer.parseInt(endParts[1])
                );
                
                if (startWorkingHour == null || slotStartTime.isBefore(startWorkingHour)) {
                    startWorkingHour = slotStartTime;
                }
                
                if (endWorkingHour == null || slotEndTime.isAfter(endWorkingHour)) {
                    endWorkingHour = slotEndTime;
                }
            }
            
            // Generate all possible 30-minute slots for this day within working hours
            List<Doctor.TimeSlot> allSlots = new java.util.ArrayList<>();
            LocalTime currentTime = startWorkingHour;

            if (currentTime != null && endWorkingHour != null) {
                while (currentTime.plusMinutes(30).compareTo(endWorkingHour) <= 0) {
                    String currentTimeStr = String.format("%02d:%02d", currentTime.getHour(), currentTime.getMinute());
                    String nextTimeStr = String.format("%02d:%02d", 
                                                  currentTime.plusMinutes(30).getHour(), 
                                                  currentTime.plusMinutes(30).getMinute());
                    
                    try {
                        Doctor.TimeSlot slot = new Doctor.TimeSlot(currentTimeStr, nextTimeStr);
                        allSlots.add(slot);
                    } catch (IllegalArgumentException e) {
                        // Skip invalid slots
                    }
                    
                    currentTime = currentTime.plusMinutes(30);
                }
            }
            
            // Get existing appointments for this date
            List<Appointment> existingAppointments = appointmentService.getAppointmentsByDoctorAndDate(id, localDate);
            
            // Create a set of booked start times
            java.util.Set<String> bookedTimes = new java.util.HashSet<>();
            for (Appointment appointment : existingAppointments) {
                LocalTime time = appointment.getHour();
                bookedTimes.add(String.format("%02d:%02d", time.getHour(), time.getMinute()));
            }
            
            // Filter out booked slots
            List<Map<String, String>> availableSlots = new java.util.ArrayList<>();
            for (Doctor.TimeSlot slot : allSlots) {
                if (!bookedTimes.contains(slot.getStartTime())) {
                    availableSlots.add(Map.of(
                        "startTime", slot.getStartTime(),
                        "endTime", slot.getEndTime()
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "doctorId", id, 
                "date", date,
                "dayOfWeek", localDate.getDayOfWeek().toString(),
                "availableSlots", availableSlots
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    /**
     * Remove a time slot from a doctor's schedule
     */
    @DeleteMapping("/{id}/availability/timeslot")
    public ResponseEntity<?> removeTimeSlot(
            @PathVariable String id,
            @RequestParam String day,
            @RequestParam String startTime) {
        try {
            Doctor doctor = doctorService.getDoctorById(id);
            
            // Get the available time slots for this day
            Map<String, List<Doctor.TimeSlot>> availableTimeSlots = doctor.getAvailableTimeSlots();
            if (!availableTimeSlots.containsKey(day) || availableTimeSlots.get(day).isEmpty()) {
                return ResponseEntity.badRequest().body("No time slots found for day: " + day);
            }
            
            List<Doctor.TimeSlot> daySlots = availableTimeSlots.get(day);
            boolean removed = false;
            
            // Find and remove the slot with matching start time
            for (int i = 0; i < daySlots.size(); i++) {
                if (daySlots.get(i).getStartTime().equals(startTime)) {
                    daySlots.remove(i);
                    removed = true;
                    break;
                }
            }
            
            if (!removed) {
                return ResponseEntity.badRequest().body("Time slot not found: " + startTime);
            }
            
            // If this day has no more slots, consider removing the day from available days
            if (daySlots.isEmpty()) {
                availableTimeSlots.remove(day);
                List<String> availableDays = doctor.getAvailableDays();
                availableDays.remove(day);
            }
            
            // Save the updated doctor
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctor);
            return ResponseEntity.ok(updatedDoctor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
