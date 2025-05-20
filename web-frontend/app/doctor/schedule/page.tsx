"use client";

import React, { useEffect, useState } from "react";
import { TimeSlotPicker } from "@/components/ui/time-slot-picker";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { scheduleApi } from "@/lib/api";
import dynamic from "next/dynamic";
import { Select } from "@/components/ui/select"; // Import a Select component for day selection

const workingDays = ["mon", "tue", "wed", "thu", "fri"]; // Updated working days to lowercase format

const ScheduleManagement = dynamic(
  () =>
    Promise.resolve(() => {
      const [schedule, setSchedule] = useState<Record<string, { startTime: string; endTime: string }[]>>({}); // Updated state type to handle time slots with startTime and endTime
      const [weeklySchedule, setWeeklySchedule] = useState<{
        date: string;
        appointments: { time: string; patientName: string }[];
        availableSlots: string[];
      }[]>([]);

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

      const saveSchedule = async () => {
        const doctorId = localStorage.getItem("pulmocare_doctor_id"); // Retrieve doctor ID from localStorage
        if (!doctorId) {
          console.error("Doctor ID is not available in localStorage.");
          return;
        }

        const selectedDays = Object.keys(schedule);
        if (selectedDays.length === 0) {
          console.error("No days selected to save schedule.");
          return;
        }

        const availabilityDetails = {
          availableDays: selectedDays,
          availableTimeSlots: schedule, // Send selected days and their time slots to the backend
        };

        console.log("Payload being sent to the backend:", availabilityDetails); // Log the payload for debugging

        try {
          const response = await scheduleApi.saveAvailability(
            doctorId,
            availabilityDetails
          );
          console.log("Schedule saved successfully:", response);
        } catch (error) {
          console.error("Error saving schedule:", error);
        }
      };

      useEffect(() => {
        // TODO: Fetch weekly schedule from the backend
        // Example structure for weeklySchedule:
        // [
        //   {
        //     date: "2025-05-20",
        //     appointments: [
        //       { time: "09:00", patientName: "John Doe" },
        //       { time: "10:30", patientName: "Jane Smith" },
        //     ],
        //     availableSlots: ["09:30", "11:00", "11:30"],
        //   },
        // ]
      }, []);

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
              <CardTitle className="text-2xl font-bold">
                Weekly Schedule
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {weeklySchedule.map((day) => (
                  <div key={day.date} className="border-b pb-4 mb-4">
                    <h3 className="text-lg font-semibold">
                      {new Date(day.date).toDateString()}
                    </h3>
                    <div className="grid grid-cols-2 gap-4">
                      {day.appointments.map((appointment) => (
                        <div
                          key={appointment.time}
                          className="bg-red-500 text-white p-2 rounded"
                        >
                          {appointment.time} - {appointment.patientName}
                        </div>
                      ))}
                      {day.availableSlots.map((slot) => (
                        <div
                          key={slot}
                          className="bg-green-500 text-white p-2 rounded"
                        >
                          {slot}
                        </div>
                      ))}
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
