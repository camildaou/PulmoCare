"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useEffect, useState } from "react"
import { doctorApi, scheduleApi } from "@/lib/api"
import { Doctor } from "@/lib/types"
import { useParams } from "next/navigation"

const daysOfWeek = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

export default function DoctorInfoPage() {
  const params = useParams()

  const [doctor, setDoctor] = useState<Doctor | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [schedule, setSchedule] = useState<Record<string, { startTime: string; endTime: string }[]>>({})

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

    fetchDoctor()
    fetchSchedule()
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
            <div className="relative w-32 h-32 rounded-full overflow-hidden border-4 border-primary">
              <div
                className="w-full h-full bg-center bg-cover"
                style={{ backgroundImage: `url('/placeholder.svg?height=128&width=128')` }}
                aria-label="Doctor profile"
              />
            </div>
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
                <div className="space-y-4">
                  {daysOfWeek.map((day, index) => {
                    const dayKey = day.toLowerCase().slice(0, 3) // Map 'Monday' to 'mon', 'Tuesday' to 'tue', etc.
                    const slots = schedule[dayKey] || []
                    if (slots.length === 0) return null // Skip days with no available slots

                    return (
                      <div key={index} className="flex items-center space-x-4">
                        <h3 className="font-semibold w-24">{day}</h3>
                        <div className="flex-1 flex space-x-2 overflow-x-auto">
                          {slots
                            .sort((a, b) => a.startTime.localeCompare(b.startTime))
                            .map((slot, index) => (
                              <div
                                key={index}
                                className="bg-green-200 text-sm text-center rounded px-2 py-1"
                              >
                                {slot.startTime} - {slot.endTime}
                              </div>
                            ))}
                        </div>
                      </div>
                    )
                  })}
                </div>
              </CardContent>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
