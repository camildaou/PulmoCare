"use client";

import React, { useEffect, useState } from "react";
import { TimeSlotPicker } from "@/components/ui/time-slot-picker";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { scheduleApi, appointmentsApi } from "@/lib/api";
import dynamic from "next/dynamic";
import { Select } from "@/components/ui/select"; // Import a Select component for day selection
import { useToast } from "@/hooks/use-toast"; // Import the toast hook

const workingDays = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday"]; // Full day names for display

const dayNameMap: Record<string, string> = {
  Monday: "mon",
  Tuesday: "tue",
  Wednesday: "wed",
  Thursday: "thu",
  Friday: "fri",
}; // Map full day names to short names

const allTimeSlots = Array.from({ length: 16 }, (_, i) => {
  const hour = Math.floor(i / 2) + 9;
  const minute = (i % 2) * 30;
  return `${hour.toString().padStart(2, "0")}:${minute.toString().padStart(2, "0")}`;
});

const ScheduleManagement = dynamic(
  () =>
    Promise.resolve(() => {
      const [schedule, setSchedule] = useState<Record<string, { startTime: string; endTime: string }[]>>({}); // Updated state type to handle time slots with startTime and endTime
      const [weeklySchedule, setWeeklySchedule] = useState<{
        date: string;
        appointments: { time: string; patientName: string }[];
        availableSlots: string[];
      }[]>([]);
      const [isLoading, setIsLoading] = useState(true); // Add loading state
      const [doctorAppointments, setDoctorAppointments] = useState<any[]>([]); // Store all doctor appointments
      const { toast } = useToast();

      // Helper function to check if new time slots conflict with existing appointments
      const checkAppointmentConflicts = (day: string, slots: Array<{startTime: string; endTime: string}>) => {
        // Find the day's appointments
        const dayAppointments = doctorAppointments.filter(appt => {
          // Convert appointment date to day name (Monday, Tuesday, etc.)
          const apptDate = new Date(appt.date);
          const dayIndex = apptDate.getDay(); // 0 = Sunday, 1 = Monday, etc.
          const adjustedDayIndex = dayIndex === 0 ? 6 : dayIndex - 1;
          const dayName = workingDays[adjustedDayIndex];
          return dayName === day;
        });

        // If no appointments for this day, there's no conflict
        if (dayAppointments.length === 0) return { hasConflict: false };

        // Check each new time slot against existing appointments
        for (const slot of slots) {
          for (const appt of dayAppointments) {
            // Format appointment time if needed
            let apptTime = appt.hour;
            if (apptTime && apptTime.includes(':') && apptTime.split(':').length === 3) {
              // Convert HH:MM:SS to HH:MM
              apptTime = apptTime.split(':').slice(0, 2).join(':');
            }

            if (slot.startTime === apptTime) {
              return { 
                hasConflict: true, 
                conflictTime: apptTime,
                patientName: `${appt.patient?.firstName || ''} ${appt.patient?.lastName || 'Patient'}` 
              };
            }
          }
        }

        return { hasConflict: false };
      };

      const handleScheduleChange = (newSchedule: {
        availableDates: Date | undefined;
        timeSlots: Record<string, string[]>;
      }) => {
        const formattedSchedule = Object.fromEntries(
          Object.entries(newSchedule.timeSlots).map(([day, slots]) => [
            day,
            slots.map((startTime) => {
              const [hours, minutes] = startTime.split(":").map(Number);
              const endTime = new Date(0, 0, 0, hours, minutes + 30)
                .toTimeString()
                .split(" ")[0]
                .slice(0, 5);
              return { startTime, endTime };
            }),
          ])
        );

        setSchedule(formattedSchedule);
      };

      const handleTimeSlotChange = (day: string, slots: string[]) => {
        const formattedSlots = slots.map((startTime) => {
          const [hours, minutes] = startTime.split(":").map(Number);
          const endTime = new Date(0, 0, 0, hours, minutes + 30)
            .toTimeString()
            .split(" ")[0]
            .slice(0, 5);
          return { startTime, endTime };
        });

        // Check for appointment conflicts before updating the schedule
        const conflict = checkAppointmentConflicts(day, formattedSlots);
        if (conflict.hasConflict) {
          alert(`Cannot add this time slot: There's an existing appointment with ${conflict.patientName} at ${conflict.conflictTime}`);
          return;
        }

        setSchedule((prev) => {
          const existingSlots = prev[day]?.map((slot) => slot.startTime) || [];
          const uniqueSlots = formattedSlots.filter(
            (slot) => !existingSlots.includes(slot.startTime)
          );

          return {
            ...prev,
            [day]: [...(prev[day] || []), ...uniqueSlots], // Add only unique slots
          };
        });
      };

      const handleDaySelection = (day: string) => {
        setSchedule((prev) => {
          if (prev[day]) {
            const updatedSchedule = { ...prev };
            delete updatedSchedule[day]; // Remove day if already selected
            return updatedSchedule;
          } else {
            return { ...prev, [day]: [] }; // Add day with empty time slots if not selected
          }
        });
      };

      useEffect(() => {
        try {
          // Store doctor ID in localStorage
          const userInfo = localStorage.getItem("pulmocare_user");
          if (userInfo) {
            const user = JSON.parse(userInfo);
            if (user.id) {
              localStorage.setItem("pulmocare_doctor_id", user.id);
            }
          }
        } catch (error) {
          console.error("Error storing doctor ID in localStorage:", error);
        }
      }, []);      // The updateWeeklySchedule function is no longer needed since we have fetchAndFormatSchedule
      // This is just a wrapper for backward compatibility if needed somewhere else
      const updateWeeklySchedule = async () => {
        return fetchAndFormatSchedule();
      };const saveSchedule = async () => {
        const doctorId = localStorage.getItem("pulmocare_doctor_id");
        if (!doctorId) {
          console.error("Doctor ID is not available in localStorage.");
          return;
        }

        const newAvailability = Object.fromEntries(
          Object.entries(schedule).map(([day, slots]) => [
            dayNameMap[day],
            slots.map((slot) => ({ startTime: slot.startTime, endTime: slot.endTime })),
          ])
        );

        console.log("Payload being sent to the backend:", JSON.stringify(newAvailability, null, 2));

        try {
          // Send new availability to the backend
          const response = await scheduleApi.appendAvailability(doctorId, newAvailability);
          console.log("Schedule appended successfully:", response);
          
          // Use the refactored function to fetch both availability and appointments
          const result = await fetchAndFormatSchedule(false);
          
          if (result?.success) {
            // Clear the schedule selection after successful save
            setSchedule({});
            
            // Show success message
            toast({
              title: "Schedule Updated",
              description: "Your schedule has been updated successfully.",
              variant: "default",
            });
          }
        } catch (error) {
          console.error("Error appending schedule:", error);
          toast({
            title: "Error",
            description: "There was an error updating your schedule. Please try again.",
            variant: "destructive",
          });
        }
      };      const handleDeleteTimeSlot = async (day: string, startTime: string) => {
        const doctorId = localStorage.getItem("pulmocare_doctor_id");
        if (!doctorId) {
          console.error("Doctor ID is not available in localStorage.");
          return;
        }

        try {
          const shortDay = dayNameMap[day];
          const response = await scheduleApi.removeTimeSlot(doctorId, shortDay, startTime);
          console.log("Time slot removed successfully:", response);
          
          // Use the refactored function to fetch and format all the schedule data
          const result = await fetchAndFormatSchedule(false);
          
          if (result?.success) {
            // Show success message
            toast({
              title: "Time Slot Removed",
              description: "The time slot has been removed successfully.",
              variant: "default",
            });
          }
        } catch (error) {
          console.error("Error removing time slot:", error);
          toast({
            title: "Error",
            description: "There was an error removing the time slot. Please try again.",
            variant: "destructive",
          });
        }
      };// Refactored function to fetch and format both availability and appointments
      const fetchAndFormatSchedule = async (setLoading = true) => {
        const doctorId = localStorage.getItem("pulmocare_doctor_id");
        if (!doctorId) {
          console.error("Doctor ID is not available in localStorage.");
          return;
        }

        try {
          // Fetch both availability and appointments data
          const [availability, appointments] = await Promise.all([
            scheduleApi.getDoctorAvailability(doctorId),
            appointmentsApi.getUpcomingAppointmentsByDoctor(doctorId)
          ]);
          
          console.log("Doctor availability response:", availability);
          console.log("Doctor appointments response:", appointments);
          
          // Store all appointments for conflict checking later
          setDoctorAppointments(appointments || []);
          
          // Create a map of appointments by day for display
          const appointmentsByDay: Record<string, { time: string; patientName: string }[]> = {};
          appointments.forEach((appointment: any) => {
            // Convert appointment date to day name (Monday, Tuesday, etc.)
            const appointmentDate = new Date(appointment.date);
            const dayIndex = appointmentDate.getDay(); // 0 = Sunday, 1 = Monday, etc.
            const adjustedDayIndex = dayIndex === 0 ? 6 : dayIndex - 1;
            const dayName = workingDays[adjustedDayIndex];
            
            if (!dayName) return; // Skip if not a working day
            
            // Format the appointment time
            let appointmentTime = appointment.hour;
            if (appointmentTime && appointmentTime.includes(':') && appointmentTime.split(':').length === 3) {
              // Convert HH:MM:SS to HH:MM
              appointmentTime = appointmentTime.split(':').slice(0, 2).join(':');
            }
            
            // Add to appointments by day
            if (!appointmentsByDay[dayName]) {
              appointmentsByDay[dayName] = [];
            }
            
            appointmentsByDay[dayName].push({
              time: appointmentTime,
              patientName: `${appointment.patient?.firstName || ''} ${appointment.patient?.lastName || 'Patient'}`
            });
          });
          
          // Format schedule with both available slots and appointments
          const formattedSchedule = Object.keys(availability.availableTimeSlots).map((day) => ({
            date: Object.keys(dayNameMap).find((key) => dayNameMap[key] === day) || day, 
            availableSlots: availability.availableTimeSlots[day]
              .map((slot: { startTime: string; endTime: string }) => `${slot.startTime} - ${slot.endTime}`)
              .sort((a: string, b: string) => {
                const [aStart] = a.split(" - ");
                const [bStart] = b.split(" - ");
                return aStart.localeCompare(bStart);
              }),
            appointments: appointmentsByDay[Object.keys(dayNameMap).find((key) => dayNameMap[key] === day) || day] || [],
          }))
          .sort((a, b) => workingDays.indexOf(a.date) - workingDays.indexOf(b.date)); 
          
          setWeeklySchedule(formattedSchedule);
          return { success: true };
        } catch (error) {
          console.error("Error fetching and formatting schedule:", error);
          return { success: false, error };
        } finally {
          if (setLoading) {
            setIsLoading(false);
          }
        }
      };

      useEffect(() => {
        // Call the refactored function
        const fetchWeeklySchedule = async () => {
          await fetchAndFormatSchedule();
        };

        fetchWeeklySchedule();
      }, []);      if (isLoading) {
        return <div>Loading...</div>; // Render a loading spinner or placeholder during data fetch
      }

      return (
        <div className="p-6 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-2xl font-bold">
                Manage Your Schedule
              </CardTitle>
              <p className="text-sm text-muted-foreground">
                Note: You cannot add availability for time slots that already have booked appointments.
              </p>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {workingDays.map((day) => (
                  <div key={day} className="mb-4">
                    <label className="flex items-center space-x-2">
                      <input
                        type="checkbox"
                        checked={!!schedule[day]}
                        onChange={() => handleDaySelection(day)}
                      />
                      <span>{day}</span>
                    </label>
                    {schedule[day] && (
                      <TimeSlotPicker
                        selectedSlots={schedule[day]?.map((slot) => slot.startTime) || []} // Map to startTime for compatibility
                        onChange={(slots) => handleTimeSlotChange(day, slots)}
                      />
                    )}
                  </div>
                ))}
                <button
                  onClick={saveSchedule}
                  className="w-full px-4 py-2 bg-primary text-primary-foreground rounded hover:bg-primary-dark"
                >
                  Save Schedule
                </button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-2xl font-bold">Weekly Schedule</CardTitle>
            </CardHeader>            <CardContent>
              <div className="space-y-6">
                {weeklySchedule.map((day) => (
                  <div key={day.date} className="mb-6">
                    <h3 className="font-semibold text-lg mb-2">{day.date}</h3>
                    
                    {/* Available Time Slots */}
                    <div className="mb-2">
                      <h4 className="text-sm font-medium mb-1">Available Time Slots:</h4>
                      <div className="flex flex-wrap gap-2">
                        {day.availableSlots.length > 0 ? (
                          day.availableSlots.map((slot, index) => (
                            <div
                              key={index}
                              className="text-sm text-center py-1 px-2 rounded bg-green-100 border border-green-300 cursor-pointer hover:bg-red-200"
                              onClick={() => {
                                if (confirm(`Are you sure you want to delete the time slot ${slot}?`)) {
                                  const [startTime] = slot.split(" - ");
                                  handleDeleteTimeSlot(day.date, startTime);
                                }
                              }}
                            >
                              {slot}
                            </div>
                          ))
                        ) : (
                          <div className="text-sm text-gray-500">No available slots</div>
                        )}
                      </div>
                    </div>
                    
                    {/* Booked Appointments */}
                    <div>
                      <h4 className="text-sm font-medium mb-1">Booked Appointments:</h4>
                      <div className="flex flex-wrap gap-2">
                        {day.appointments.length > 0 ? (
                          day.appointments.map((appointment, index) => (
                            <div
                              key={index}
                              className="text-sm text-center py-1 px-2 rounded bg-red-50 border border-red-200"
                              title={`Appointment with ${appointment.patientName}`}
                            >
                              {appointment.time} - {appointment.patientName}
                            </div>
                          ))
                        ) : (
                          <div className="text-sm text-gray-500">No booked appointments</div>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
              
              {/* Legend */}
              <div className="mt-6 pt-4 border-t border-gray-200">
                <h4 className="text-sm font-medium mb-2">Legend:</h4>
                <div className="flex flex-wrap gap-4 text-xs">
                  <div className="flex items-center">
                    <div className="w-3 h-3 bg-green-100 border border-green-300 mr-1"></div>
                    <span>Available Slot (Click to delete)</span>
                  </div>
                  <div className="flex items-center">
                    <div className="w-3 h-3 bg-red-50 border border-red-200 mr-1"></div>
                    <span>Booked Appointment</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      );
    }),
  { ssr: false }
);

export default ScheduleManagement;
