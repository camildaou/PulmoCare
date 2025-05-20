"use client";

import React, { useEffect, useState } from "react";
import { TimeSlotPicker } from "@/components/ui/time-slot-picker";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { scheduleApi } from "@/lib/api";
import dynamic from "next/dynamic";

const ScheduleManagement = dynamic(
  () =>
    Promise.resolve(() => {
      const [selectedDate, setSelectedDate] = useState<string | undefined>(
        undefined
      );
      const [timeSlots, setTimeSlots] = useState<string[]>([]);
      const [schedule, setSchedule] = useState<{
        availableDates: Date | undefined;
        timeSlots: Record<string, string[]>;
      }>({
        availableDates: undefined,
        timeSlots: {},
      });
      const [weeklySchedule, setWeeklySchedule] = useState<{
        date: string;
        appointments: { time: string; patientName: string }[];
        availableSlots: string[];
      }[]>([]);

      const handleScheduleChange = (newSchedule: {
        availableDates: Date | undefined;
        timeSlots: Record<string, string[]>;
      }) => {
        setSchedule(newSchedule);
      };

      const handleTimeSlotChange = (slots: string[]) => {
        if (selectedDate) {
          setTimeSlots(slots);
          setSchedule({
            ...schedule,
            timeSlots: { ...schedule.timeSlots, [selectedDate]: slots },
          });
        } else {
          alert("No date selected. Please select a date.");
        }
      };

      const handleDateChange = (date: Date) => {
        const dateString = date.toISOString().split("T")[0];
        setSelectedDate(dateString);
        setTimeSlots(schedule.timeSlots[dateString] || []); // Ensure time slots are updated for the selected date
        if (!schedule.timeSlots[dateString]) {
          setSchedule({
            ...schedule,
            timeSlots: { ...schedule.timeSlots, [dateString]: [] },
          });
        }
      };

      const saveSchedule = async () => {
        if (!selectedDate) {
          console.error("No date selected to save schedule.");
          return;
        }

        const availabilityDetails = {
          availableDays: Object.keys(schedule.timeSlots),
          availableTimeSlots: Object.values(schedule.timeSlots).flat(), // Flatten the time slots into a single list
        };

        try {
          const response = await scheduleApi.saveAvailability(
            "doctorId",
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
                <div className="mb-4">
                  <input
                    type="date"
                    className="w-full px-4 py-2 border rounded"
                    onChange={(e) => {
                      const selectedDate = e.target.value;
                      setSelectedDate(selectedDate);
                      setTimeSlots(schedule.timeSlots[selectedDate] || []); // Update time slots for the selected date
                      if (!schedule.timeSlots[selectedDate]) {
                        setSchedule({
                          ...schedule,
                          timeSlots: { ...schedule.timeSlots, [selectedDate]: [] },
                        });
                      }
                    }}
                  />
                </div>
                <div className="mb-4">
                  <h2 className="text-lg font-semibold">Available Time Slots</h2>
                  <TimeSlotPicker
                    selectedSlots={timeSlots} // Pass only time slots
                    onChange={(slots) => {
                      setTimeSlots(slots);
                      if (selectedDate) {
                        setSchedule({
                          ...schedule,
                          timeSlots: { ...schedule.timeSlots, [selectedDate]: slots },
                        });
                      }
                    }}
                  />
                </div>
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
