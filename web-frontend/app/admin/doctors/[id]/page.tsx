"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useEffect, useState } from "react"
import { doctorApi } from "@/lib/api"
import { Doctor } from "@/lib/types"

export default function DoctorInfoPage({ params }: { params: { id: string } }) {
  const [doctor, setDoctor] = useState<Doctor | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const fetchDoctor = async () => {
      try {
        const response = await doctorApi.getProfile(params.id)
        setDoctor(response)
      } catch (error) {
        console.error("Error fetching doctor info:", error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchDoctor()
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

      <div className="grid gap-6 md:grid-cols-[300px_1fr]">
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
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
