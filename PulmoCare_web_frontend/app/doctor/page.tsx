"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Calendar } from "@/components/ui/calendar"
import { Button } from "@/components/ui/button"
import { useState, useEffect } from "react"
import Link from "next/link"

export default function DoctorDashboard() {
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(new Date())
  const [showSchedule, setShowSchedule] = useState(false)
  const [doctorName, setDoctorName] = useState("Doctor")
  const [pendingReports, setPendingReports] = useState(3)

  useEffect(() => {
    // Get user info from localStorage in client component
    const userInfo = localStorage.getItem("pulmocare_user")
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (user.name) {
        setDoctorName(`Dr. ${user.name}`)
      }
    }

    // Get pending reports count from localStorage
    const pendingReportsData = localStorage.getItem("pulmocare_pending_reports")
    if (pendingReportsData) {
      setPendingReports(Number.parseInt(pendingReportsData))
    } else {
      // Initialize with default value if not set
      localStorage.setItem("pulmocare_pending_reports", "3")
    }
  }, [])

  const handleDateSelect = (date: Date | undefined) => {
    setSelectedDate(date)
    setShowSchedule(true)
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Welcome, {doctorName}</h1>
        <p className="text-muted-foreground">Here's your schedule for today and upcoming days.</p>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Today's Appointments</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">5</div>
<Link href="/doctor/appointments">
  <Button variant="outline" size="sm">
    View All
  </Button>
</Link>

          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Pending Reports</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{pendingReports}</div>
<Link href="/doctor/appointments?filter=pending">
  <Button variant="outline" size="sm">
    View Reports
  </Button>
</Link>

          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Total Patients</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">128</div>
          </CardContent>
        </Card>
      </div>

      <Card className="border-2 border-secondary">
        <CardHeader>
          <CardTitle>Ongoing Appointment</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-medium">Alice Johnson</h3>
              <p className="text-sm text-muted-foreground">Follow-up Appointment</p>
              <p className="text-sm">Started at: 10:30 AM (25 minutes ago)</p>
            </div>
<Link href="/doctor/appointments/1">
  <Button className="w-full">
    View Details
  </Button>
</Link>

          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 md:grid-cols-2">
        <Card className="col-span-1">
          <CardHeader>
            <CardTitle>Today's Schedule</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {[
                { time: "09:00 AM", patient: "Alice Johnson", reason: "Follow-up", id: "1" },
                { time: "10:30 AM", patient: "Bob Smith", reason: "Initial Consultation", id: "2" },
                { time: "01:00 PM", patient: "Carol Williams", reason: "Test Results", id: "3" },
                { time: "02:30 PM", patient: "David Brown", reason: "Follow-up", id: "4" },
                { time: "04:00 PM", patient: "Eve Davis", reason: "Respiratory Assessment", id: "5" },
              ].map((appointment, i) => (
                <div key={i} className="flex items-center justify-between rounded-lg border p-3">
                  <div className="space-y-1">
                    <p className="font-medium">{appointment.patient}</p>
                    <p className="text-sm text-muted-foreground">{appointment.reason}</p>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="text-sm font-medium text-primary">{appointment.time}</div>
<Link href={`/doctor/appointments/${appointment.id}`}>
  <Button size="sm" variant="outline" className="w-full">
    View
  </Button>
</Link>

                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card className="col-span-1">
          <CardHeader>
            <CardTitle>Calendar</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col items-center">
            <div className="w-full max-w-[350px]">
              <Calendar
                mode="single"
                className="rounded-md border"
                selected={selectedDate}
                onSelect={handleDateSelect}
                classNames={{
                  months: "flex flex-col space-y-4",
                  month: "space-y-4",
                  caption: "flex justify-center pt-1 relative items-center",
                  caption_label: "text-sm font-medium",
                  nav: "space-x-1 flex items-center",
                  nav_button: "h-7 w-7 bg-transparent p-0 opacity-50 hover:opacity-100",
                  nav_button_previous: "absolute left-1",
                  nav_button_next: "absolute right-1",
                  table: "w-full border-collapse space-y-1",
                  head_row: "flex",
                  head_cell: "text-muted-foreground rounded-md w-9 font-normal text-[0.8rem]",
                  row: "flex w-full mt-2",
                  cell: "h-9 w-9 text-center text-sm p-0 relative [&:has([aria-selected].day-range-end)]:rounded-r-md [&:has([aria-selected].day-outside)]:bg-accent/50 [&:has([aria-selected])]:bg-accent first:[&:has([aria-selected])]:rounded-l-md last:[&:has([aria-selected])]:rounded-r-md focus-within:relative focus-within:z-20",
                  day: "h-9 w-9 p-0 font-normal aria-selected:opacity-100",
                  day_range_end: "day-range-end",
                  day_selected:
                    "bg-primary text-primary-foreground hover:bg-primary hover:text-primary-foreground focus:bg-primary focus:text-primary-foreground",
                  day_today: "bg-accent text-accent-foreground",
                  day_outside: "day-outside text-muted-foreground opacity-50",
                  day_disabled: "text-muted-foreground opacity-50",
                  day_range_middle: "aria-selected:bg-accent aria-selected:text-accent-foreground",
                  day_hidden: "invisible",
                }}
              />
            </div>

            {showSchedule && (
              <div className="mt-4 border rounded-md p-4 w-full">
                <h3 className="font-medium mb-2">
                  Schedule for{" "}
                  {selectedDate?.toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric" })}
                </h3>
                <div className="space-y-2">
                  {selectedDate?.getDay() === new Date().getDay() ? (
                    // Today's schedule
                    <>
                      <div className="text-sm flex justify-between">
                        <span>09:00 AM</span>
                        <span>Alice Johnson (Follow-up)</span>
                      </div>
                      <div className="text-sm flex justify-between">
                        <span>10:30 AM</span>
                        <span>Bob Smith (Initial Consultation)</span>
                      </div>
                      <div className="text-sm flex justify-between">
                        <span>01:00 PM</span>
                        <span>Carol Williams (Test Results)</span>
                      </div>
                      <div className="text-sm flex justify-between">
                        <span>02:30 PM</span>
                        <span>David Brown (Follow-up)</span>
                      </div>
                      <div className="text-sm flex justify-between">
                        <span>04:00 PM</span>
                        <span>Eve Davis (Respiratory Assessment)</span>
                      </div>
                    </>
                  ) : selectedDate?.getDay() === 0 || selectedDate?.getDay() === 6 ? (
                    // Weekend
                    <p className="text-sm text-muted-foreground">No appointments scheduled (weekend)</p>
                  ) : (
                    // Other days
                    <>
                      <div className="text-sm flex justify-between">
                        <span>10:00 AM</span>
                        <span>Frank Miller (Follow-up)</span>
                      </div>
                      <div className="text-sm flex justify-between">
                        <span>11:30 AM</span>
                        <span>Grace Lee (Asthma Review)</span>
                      </div>
                      <div className="text-sm flex justify-between">
                        <span>02:00 PM</span>
                        <span>Henry Wilson (Pulmonary Function Test)</span>
                      </div>
                    </>
                  )}
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
