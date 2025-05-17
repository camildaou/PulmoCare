"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Calendar } from "@/components/ui/calendar"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useState, useEffect } from "react"

// Mock data for doctors
const doctors = [
  {
    id: "1",
    name: "Dr. John Doe",
    specialty: "Pulmonologist",
    schedule: { start: "09:00", end: "17:00", days: [1, 2, 3, 4, 5] },
    location: "Memorial Hospital, Building A, Floor 3",
  },
  {
    id: "2",
    name: "Dr. Sarah Wilson",
    specialty: "Respiratory Specialist",
    schedule: { start: "08:00", end: "16:00", days: [1, 3, 5] },
    location: "City Medical Center, Pulmonology Wing",
  },
  {
    id: "3",
    name: "Dr. Michael Chen",
    specialty: "Pulmonologist",
    schedule: { start: "10:00", end: "18:00", days: [2, 4, 5] },
    location: "Riverside Clinic, Suite 205",
  },
]

export default function DoctorSchedulePage({ params }: { params: { id: string } }) {
  // Get doctor data based on the ID
  const [doctor, setDoctor] = useState(doctors.find((d) => d.id === params.id) || doctors[0])
  const [selectedDate, setSelectedDate] = useState<Date>(new Date())
  const [showSchedule, setShowSchedule] = useState(true)

  // Get appointments for this doctor
  const [appointments, setAppointments] = useState([
    {
      id: 1,
      time: "09:00 AM",
      patient: "Alice Johnson",
      reason: "Follow-up",
      status: "Confirmed",
      vaccines: "None",
    },
    {
      id: 3,
      time: "11:30 AM",
      patient: "Carol Williams",
      reason: "Test Results",
      status: "Confirmed",
      vaccines: "None",
    },
    {
      id: 5,
      time: "02:00 PM",
      patient: "Eve Davis",
      reason: "Respiratory Assessment",
      status: "Confirmed",
      vaccines: "Pneumonia vaccine due",
    },
  ])

  useEffect(() => {
    // In a real app, you would fetch doctor data and appointments based on the ID
    const foundDoctor = doctors.find((d) => d.id === params.id)
    if (foundDoctor) {
      setDoctor(foundDoctor)
    }
  }, [params.id])

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{doctor.name}'s Schedule</h1>
          <p className="text-muted-foreground">View and manage appointments.</p>
          <p className="text-sm text-muted-foreground mt-1">Location: {doctor.location}</p>
        </div>
        <div className="flex gap-2">
          <Link href="/admin/doctors">
            <Button variant="outline">Back to Doctors</Button>
          </Link>

          <Link href={`/admin/doctors/${params.id}`}>
            <Button>View Doctor Info</Button>
          </Link>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-[1fr_300px]">
        <Card>
          <CardHeader>
            <CardTitle>Today's Appointments</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {appointments.map((appointment) => (
                <div key={appointment.id} className="flex items-center justify-between rounded-lg border p-3">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium">{appointment.patient}</span>
                      <span
                        className={
                          appointment.status === "Confirmed" ? "text-xs text-green-500" : "text-xs text-amber-500"
                        }
                      >
                        {appointment.status}
                      </span>
                    </div>
                    <p className="text-sm text-muted-foreground">{appointment.reason}</p>
                    {appointment.vaccines !== "None" && (
                      <p className="text-xs text-secondary">Vaccine: {appointment.vaccines}</p>
                    )}
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="text-sm font-medium">{appointment.time}</div>
                    <Dialog>
                      <DialogTrigger asChild>
                        <Button variant="outline" size="sm">
                          Check Appt
                        </Button>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader>
                          <DialogTitle>Appointment Details</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                          <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-2">
                              <Label htmlFor="date">Date</Label>
                              <Input id="date" type="date" defaultValue="2025-03-28" readOnly />
                            </div>
                            <div className="space-y-2">
                              <Label htmlFor="time">Time</Label>
                              <Input
                                id="time"
                                type="time"
                                defaultValue={appointment.time.replace(" AM", "").replace(" PM", "")}
                                readOnly
                              />
                            </div>
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="patient">Patient</Label>
                            <Input id="patient" defaultValue={appointment.patient} readOnly />
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="reason">Reason</Label>
                            <Input id="reason" defaultValue={appointment.reason} readOnly />
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="vaccine">Vaccine</Label>
                            <Input id="vaccine" defaultValue={appointment.vaccines} readOnly />
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="status">Status</Label>
                            <select className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50">
                              <option value="confirmed" selected={appointment.status === "Confirmed"}>
                                Confirmed
                              </option>
                              <option value="completed">Completed</option>
                              <option value="cancelled">Cancelled</option>
                              <option value="no-show">No Show</option>
                            </select>
                          </div>
                        </div>
                        <div className="flex justify-end">
                          <Button>Update Status</Button>
                        </div>
                      </DialogContent>
                    </Dialog>
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
                onSelect={(date) => date && setSelectedDate(date)}
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

      <Card>
        <CardHeader>
          <CardTitle>Weekly Schedule</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-7 gap-2">
            {["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map((day) => (
              <div key={day} className="text-center font-medium">
                {day}
              </div>
            ))}
            {["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map((day, i) => {
              const dayIndex = i + 1 === 7 ? 0 : i + 1 // Convert to 0-6 where 0 is Sunday
              const isWorkDay = doctor.schedule.days.includes(dayIndex)

              return (
                <div key={i} className="min-h-[100px] rounded-md border p-2">
                  {!isWorkDay ? (
                    <div className="flex h-full items-center justify-center text-sm text-muted-foreground">Off</div>
                  ) : (
                    <div className="space-y-1">
                      <div className="text-xs text-muted-foreground">
                        {doctor.schedule.start} - {doctor.schedule.end}
                      </div>
                      <div className="text-xs font-medium">
                        {day === "Mon"
                          ? "5 appointments"
                          : day === "Tue"
                            ? "4 appointments"
                            : day === "Wed"
                              ? "6 appointments"
                              : day === "Thu"
                                ? "3 appointments"
                                : "4 appointments"}
                      </div>
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
