"use client";

import React, { useEffect, useState } from "react";
import { TimeSlotPicker } from "@/components/ui/time-slot-picker";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { scheduleApi } from "@/lib/api";
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

        setSchedule((prev) => ({
          ...prev,
          [day]: formattedSlots, // Update time slots for the selected day
        }));
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
      }, []);

      const updateWeeklySchedule = async () => {
        const doctorId = localStorage.getItem("pulmocare_doctor_id");
        if (!doctorId) {
          console.error("Doctor ID is not available in localStorage.");
          return;
        }

        try {
          const availability = await scheduleApi.getDoctorAvailability(doctorId);
          console.log("Updated doctor availability response:", availability);
          const formattedSchedule = Object.keys(availability.availableTimeSlots)
            .map((day) => ({
              date: Object.keys(dayNameMap).find((key) => dayNameMap[key] === day) || day,
              availableSlots: availability.availableTimeSlots[day].map((slot: { startTime: string; endTime: string }) => `${slot.startTime} - ${slot.endTime}`),
              appointments: [],
            }))
            .sort((a, b) => workingDays.indexOf(a.date) - workingDays.indexOf(b.date)); // Ensure sorting by workingDays order
          setWeeklySchedule(formattedSchedule);
        } catch (error) {
          console.error("Error updating weekly schedule:", error);
        }
      };

      const saveSchedule = async () => {
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
          const response = await scheduleApi.appendAvailability(doctorId, newAvailability);
          console.log("Schedule appended successfully:", response);
          alert("Your schedule has been updated successfully.");
          await updateWeeklySchedule(); // Update the weekly schedule after saving
        } catch (error) {
          console.error("Error appending schedule:", error);
          alert("There was an error updating your schedule. Please try again.");
        }
      };

      const handleDeleteTimeSlot = async (day: string, startTime: string) => {
        const doctorId = localStorage.getItem("pulmocare_doctor_id");
        if (!doctorId) {
          console.error("Doctor ID is not available in localStorage.");
          return;
        }

        try {
          const shortDay = dayNameMap[day];
          const response = await scheduleApi.removeTimeSlot(doctorId, shortDay, startTime);
          console.log("Time slot removed successfully:", response);
          alert("Time slot removed successfully.");
          await updateWeeklySchedule(); // Refresh the weekly schedule
        } catch (error) {
          console.error("Error removing time slot:", error);
          alert("There was an error removing the time slot. Please try again.");
        }
      };

      useEffect(() => {
        const fetchWeeklySchedule = async () => {
          const doctorId = localStorage.getItem("pulmocare_doctor_id");
          if (!doctorId) {
            console.error("Doctor ID is not available in localStorage.");
            return;
          }

          try {
            const availability = await scheduleApi.getDoctorAvailability(doctorId);
            console.log("Doctor availability response:", availability); // Log the availability data structure
            const formattedSchedule = Object.keys(availability.availableTimeSlots).map((day) => ({
              date: Object.keys(dayNameMap).find((key) => dayNameMap[key] === day) || day, // Convert short day names to full names
              availableSlots: availability.availableTimeSlots[day].map((slot: { startTime: string; endTime: string }) => `${slot.startTime} - ${slot.endTime}`),
              appointments: [], // Assuming appointments are not part of the response
            }))
            .sort((a, b) => workingDays.indexOf(a.date) - workingDays.indexOf(b.date)); // Ensure sorting by workingDays order
            setWeeklySchedule(formattedSchedule);
          } catch (error) {
            console.error("Error fetching weekly schedule:", error);
          } finally {
            setIsLoading(false);
          }
        };

        fetchWeeklySchedule();
      }, []);

      if (isLoading) {
        return <div>Loading...</div>; // Render a loading spinner or placeholder during data fetch
      }

      return (
        <div className="p-6 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-2xl font-bold">
                Manage Your Schedule
              </CardTitle>
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
                  className="w-full px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                  Save Schedule
                </button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-2xl font-bold">Weekly Schedule</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-6">
                {weeklySchedule.map((day) => (
                  <div key={day.date} className="mb-6">
                    <h3 className="font-semibold text-lg mb-2">{day.date}</h3>
                    <div className="flex space-x-4">
                      {day.availableSlots.length > 0 ? (
                        day.availableSlots.map((slot, index) => (
                          <div
                            key={index}
                            className="text-sm text-center py-1 px-2 rounded bg-green-200 cursor-pointer hover:bg-red-200"
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
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      );
    }),
  { ssr: false }
);

export default ScheduleManagement;
