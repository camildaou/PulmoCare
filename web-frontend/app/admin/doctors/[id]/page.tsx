"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useEffect, useState } from "react"
import { doctorApi, scheduleApi } from "@/lib/api"
import { Doctor, Appointment } from "@/lib/types"
import { useParams } from "next/navigation"

const daysOfWeek = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

const dayNameMap: Record<string, string> = {
  Monday: "mon",
  Tuesday: "tue",
  Wednesday: "wed",
  Thursday: "thu",
  Friday: "fri",
  Saturday: "sat",
  Sunday: "sun",
};

export default function DoctorInfoPage() {
  const params = useParams()

  const [doctor, setDoctor] = useState<Doctor | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [schedule, setSchedule] = useState<Record<string, { startTime: string; endTime: string }[]>>({})
  const [appointments, setAppointments] = useState<Appointment[]>([]);

  useEffect(() => {
    const fetchDoctor = async () => {
      try {
        if (typeof params.id === "string") {
          const response = await doctorApi.getProfile(params.id)
          setDoctor(response)
        } else {
          console.error("Invalid doctor ID")
        }
      } catch (error) {
        console.error("Error fetching doctor info:", error)
      } finally {
        setIsLoading(false)
      }
    }

    // Updated the fetchSchedule function to retrieve timeslots from the database based on the doctor ID
    const fetchSchedule = async () => {
      try {
        if (typeof params.id === "string") {
          const availability = await scheduleApi.getDoctorAvailability(params.id)
          console.log("Fetched schedule:", availability.availableTimeSlots) // Debugging log
          setSchedule(availability.availableTimeSlots)
        }
      } catch (error) {
        console.error("Error fetching schedule:", error)
      }
    }

    const fetchAppointments = async () => {
      try {
        if (typeof params.id === "string") {
          // Only fetch upcoming appointments for this doctor
          const appts = await doctorApi.getUpcomingAppointmentsByDoctorId(params.id);
          setAppointments(appts || []);
        }
      } catch (error) {
        setAppointments([]);
      }
    }

    fetchDoctor()
    fetchSchedule()
    fetchAppointments()
  }, [params.id])

  if (isLoading) {
    return <p>Loading doctor information...</p>
  }

  if (!doctor) {
    return <p>Doctor not found.</p>
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Doctor Information</h1>
          <p className="text-muted-foreground">View doctor details.</p>
        </div>
        <Link href="/admin/doctors">
          <Button variant="outline">Back to Doctors</Button>
        </Link>
      </div>

      <div className="grid gap-6 md:grid-cols-[2fr_5fr]">
        <Card>
          <CardContent className="p-6 flex flex-col items-center gap-4">
            <div className="text-center">
              <h2 className="text-xl font-bold">
                {doctor.firstName} {doctor.lastName}
              </h2>
              <p className="text-sm text-muted-foreground">{doctor.description || "No specialty provided"}</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Personal Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName">First Name</Label>
                <Input id="firstName" value={doctor.firstName} readOnly />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input id="lastName" value={doctor.lastName} readOnly />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="gender">Gender</Label>
                <Input id="gender" value={doctor.gender} readOnly />
              </div>
              <div className="space-y-2">
                <Label htmlFor="age">Age</Label>
                <Input id="age" value={doctor.age?.toString() || "N/A"} readOnly />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" value={doctor.email} readOnly />
            </div>

            <div className="space-y-2">
              <Label htmlFor="phone">Phone Number</Label>
              <Input id="phone" value={doctor.phone} readOnly />
            </div>

            <div className="space-y-2">
              <Label htmlFor="license">Medical License</Label>
              <Input id="license" value={doctor.medicalLicense} readOnly />
            </div>

            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <Input id="location" value={doctor.location} readOnly />
            </div>

            <div className="space-y-4">
              <CardHeader>
                <CardTitle>Weekly Schedule</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="overflow-x-auto">
                  <table className="min-w-full border-collapse">
                    <thead>
                      <tr>
                        <th className="p-2 border text-sm">Time</th>
                        {daysOfWeek.slice(0, 5).map((day) => (
                          <th key={day} className="p-2 border text-sm bg-gray-100">{day}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {Array.from({ length: 20 }, (_, i) => {
                        const hour = Math.floor(i / 2) + 8;
                        const minute = (i % 2) * 30;
                        // Use 24-hour format for matching (e.g., 08:00, 13:30)
                        const time24 = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
                        // Also support 12-hour format for display (e.g., 08:00 AM)
                        const ampm = hour < 12 ? 'AM' : 'PM';
                        const hour12 = hour % 12 === 0 ? 12 : hour % 12;
                        const time12 = `${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} ${ampm}`;
                        return (
                          <tr key={time24}>
                            <td className="p-2 border text-sm font-medium bg-gray-50">{time12}</td>
                            {daysOfWeek.slice(0, 5).map((day) => {
                              const dayKey = day.toLowerCase().slice(0, 3);
                              const slots = schedule[dayKey] || [];
                              // Find slot for this time (match by 24h format)
                              const slot = slots.find((s) => s.startTime === time24);
                              // Find if this slot is booked (match by 24h or 12h format, and by both hour/time fields)
                              const bookedAppt = appointments.find((appt) => {
                                const apptDate = new Date(appt.date);
                                const apptDay = apptDate.toLocaleDateString("en-US", { weekday: "long" });
                                // Normalize all possible time representations
                                const apptTimes = [appt.hour, appt.time]
                                  .filter(Boolean)
                                  .map((t) => t.replace(/\s*AM|\s*PM/i, ''));
                                return (
                                  apptDay === day &&
                                  (apptTimes.includes(time24) || apptTimes.includes(time12.replace(/\s*AM|\s*PM/i, '')))
                                );
                              });
                              let bgColor = "bg-white";
                              let content = "";
                              if (bookedAppt) {
                                bgColor = "bg-red-50 border border-red-200";
                                content = `${bookedAppt.patient?.firstName || ''} ${bookedAppt.patient?.lastName || 'Patient'}`;
                              } else if (slot) {
                                bgColor = "bg-green-100 border border-green-300";
                                content = "Available";
                              }
                              return (
                                <td
                                  key={`${day}-${time24}`}
                                  className={`p-2 border text-xs ${bgColor}`}
                                  title={bookedAppt ? `${content} - ${time12}` : slot ? `Available at ${time12}` : `Unavailable`}
                                >
                                  {content}
                                </td>
                              );
                            })}
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                  <div className="mt-4 flex flex-wrap gap-3 text-xs">
                    <div className="flex items-center">
                      <div className="w-3 h-3 bg-green-100 border border-green-300 mr-1"></div>
                      <span>Available Slot</span>
                    </div>
                    <div className="flex items-center">
                      <div className="w-3 h-3 bg-red-50 border border-red-200 mr-1"></div>
                      <span>Booked Appointment</span>
                    </div>
                    <div className="flex items-center">
                      <div className="w-3 h-3 bg-white border mr-1"></div>
                      <span>Unavailable</span>
                    </div>
                  </div>
                </div>
              </CardContent>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
