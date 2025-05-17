"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import Link from "next/link"
import { CalendarIcon, Users, BarChart3, UserIcon } from "lucide-react"
import { useState, useEffect } from "react"

export default function AdminDashboard() {
  const [adminName, setAdminName] = useState("Administrator")

  useEffect(() => {
    // Get user info from localStorage in client component
    const userInfo = localStorage.getItem("pulmocare_user")
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (user.name) {
        setAdminName(user.name)
      }
    }
  }, [])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Welcome, {adminName}</h1>
        <p className="text-muted-foreground">Manage your clinic operations from here.</p>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Total Patients</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">248</div>
            <p className="text-xs text-muted-foreground">+12% from last month</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Active Doctors</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">8</div>
            <p className="text-xs text-muted-foreground">+1 new this month</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Today's Appointments</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">24</div>
            <p className="text-xs text-muted-foreground">3 pending confirmation</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Pending Reports</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">12</div>
            <p className="text-xs text-muted-foreground">5 urgent</p>
          </CardContent>
        </Card>
      </div>

<div className="grid gap-6 md:grid-cols-4">
  {/* Appointments */}
  <Link href="/admin/appointments">
    <Card className="h-full transition-transform duration-300 hover:scale-105 cursor-pointer">
      <CardHeader className="min-h-[100px] flex flex-col justify-center items-center text-center">
        <CardTitle>Manage Appointments</CardTitle>
        <CardDescription>Schedule and manage patient appointments</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col items-center justify-center gap-4 pt-4">
        <div className="rounded-full bg-primary/10 p-6">
          <CalendarIcon className="h-12 w-12 text-primary" />
        </div>
      </CardContent>
    </Card>
  </Link>

  {/* Doctors */}
  <Link href="/admin/doctors">
    <Card className="h-full transition-transform duration-300 hover:scale-105 cursor-pointer">
      <CardHeader className="min-h-[100px] flex flex-col justify-center items-center text-center">
        <CardTitle>Manage Doctors</CardTitle>
        <CardDescription>View doctor schedules and information</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col items-center justify-center gap-4 pt-4">
        <div className="rounded-full bg-primary/10 p-6">
          <UserIcon className="h-12 w-12 text-primary" />
        </div>
      </CardContent>
    </Card>
  </Link>

  {/* Patients */}
  <Link href="/admin/patients">
    <Card className="h-full transition-transform duration-300 hover:scale-105 cursor-pointer">
      <CardHeader className="min-h-[100px] flex flex-col justify-center items-center text-center">
        <CardTitle>Manage Patients</CardTitle>
        <CardDescription>View and update patient information</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col items-center justify-center gap-4 pt-4">
        <div className="rounded-full bg-secondary/10 p-6">
          <Users className="h-12 w-12 text-secondary" />
        </div>
      </CardContent>
    </Card>
  </Link>

  {/* Analysis */}
  <Link href="/admin/analysis">
    <Card className="h-full transition-transform duration-300 hover:scale-105 cursor-pointer">
      <CardHeader className="min-h-[100px] flex flex-col justify-center items-center text-center">
        <CardTitle>Data Analysis</CardTitle>
        <CardDescription>Access prediction and detection tools</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col items-center justify-center gap-4 pt-4">
        <div className="rounded-full bg-primary/10 p-6">
          <BarChart3 className="h-12 w-12 text-primary" />
        </div>
      </CardContent>
    </Card>
  </Link>
</div>

    </div>
  )
}
