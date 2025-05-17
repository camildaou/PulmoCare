"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import Link from "next/link"
import { useState, useEffect } from "react"
import { Calendar } from "@/components/ui/calendar"
import { format, isBefore } from "date-fns"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog"
import { toast } from "sonner"

// Mock data for doctors
const doctors = [
  {
    id: "1",
    name: "Dr. John Doe",
    specialty: "Pulmonologist",
    schedule: { start: "09:00", end: "17:00", days: [1, 2, 3, 4, 5] },
  },
  {
    id: "2",
    name: "Dr. Sarah Wilson",
    specialty: "Respiratory Specialist",
    schedule: { start: "08:00", end: "16:00", days: [1, 3, 5] },
  },
  {
    id: "3",
    name: "Dr. Michael Chen",
    specialty: "Pulmonologist",
    schedule: { start: "10:00", end: "18:00", days: [2, 4, 5] },
  },
]

// Mock data for patients
const patients = [
  { id: "1", name: "Alice Johnson", age: 42, condition: "Asthma" },
  { id: "2", name: "Bob Smith", age: 35, condition: "COPD" },
  { id: "3", name: "Carol Williams", age: 28, condition: "Bronchitis" },
  { id: "4", name: "David Brown", age: 50, condition: "Emphysema" },
  { id: "5", name: "Eve Davis", age: 33, condition: "Asthma" },
]

// Appointment reasons
const appointmentReasons = [
  "Initial Consultation",
  "Follow-up",
  "Test Results",
  "Respiratory Assessment",
  "Treatment Review",
  "Emergency",
]

// Time slots
const timeSlots = [
  "08:00 AM",
  "08:30 AM",
  "09:00 AM",
  "09:30 AM",
  "10:00 AM",
  "10:30 AM",
  "11:00 AM",
  "11:30 AM",
  "12:00 PM",
  "12:30 PM",
  "01:00 PM",
  "01:30 PM",
  "02:00 PM",
  "02:30 PM",
  "03:00 PM",
  "03:30 PM",
  "04:00 PM",
  "04:30 PM",
  "05:00 PM",
  "05:30 PM",
  "06:00 PM",
]

export default function AdminAppointmentsPage() {
  // State for appointments
  const [upcomingAppointments, setUpcomingAppointments] = useState([
    {
      id: 1,
      date: "Today",
      time: "09:00 AM",
      patient: "Alice Johnson",
      patientId: "1",
      doctor: "Dr. John Doe",
      doctorId: "1",
      reason: "Follow-up",
      status: "Confirmed",
      vaccines: "None",
    },
    {
      id: 2,
      date: "Today",
      time: "10:30 AM",
      patient: "Bob Smith",
      patientId: "2",
      doctor: "Dr. Sarah Wilson",
      doctorId: "2",
      reason: "Initial Consultation",
      status: "Confirmed",
      vaccines: "Flu shot reminder",
    },
    {
      id: 3,
      date: "Tomorrow",
      time: "01:00 PM",
      patient: "Carol Williams",
      patientId: "3",
      doctor: "Dr. John Doe",
      doctorId: "1",
      reason: "Test Results",
      status: "Confirmed",
      vaccines: "None",
    },
    {
      id: 4,
      date: "Mar 30, 2025",
      time: "02:30 PM",
      patient: "David Brown",
      patientId: "4",
      doctor: "Dr. Michael Chen",
      doctorId: "3",
      reason: "Follow-up",
      status: "Pending",
      vaccines: "None",
    },
    {
      id: 5,
      date: "Mar 31, 2025",
      time: "04:00 PM",
      patient: "Eve Davis",
      patientId: "5",
      doctor: "Dr. John Doe",
      doctorId: "1",
      reason: "Respiratory Assessment",
      status: "Confirmed",
      vaccines: "Pneumonia vaccine due",
    },
  ])

  const [pastAppointments, setPastAppointments] = useState([
    {
      id: 6,
      date: "Mar 25, 2025",
      time: "11:00 AM",
      patient: "Frank Miller",
      patientId: "6",
      doctor: "Dr. John Doe",
      doctorId: "1",
      reason: "Follow-up",
      status: "Completed",
      vaccines: "None",
    },
    {
      id: 7,
      date: "Mar 24, 2025",
      time: "09:30 AM",
      patient: "Grace Lee",
      patientId: "7",
      doctor: "Dr. Sarah Wilson",
      doctorId: "2",
      reason: "Asthma Review",
      status: "Completed",
      vaccines: "None",
    },
  ])

  // State for create appointment form
  const [newAppointment, setNewAppointment] = useState({
    doctorId: "",
    patientId: "",
    date: new Date(),
    time: "",
    reason: "",
    status: "Pending",
    vaccines: "",
  })

  // State for edit appointment form
  const [editingAppointment, setEditingAppointment] = useState(null)

  // State to track which dialog is open
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [currentEditId, setCurrentEditId] = useState(null)

  // State for available time slots
  const [availableTimeSlots, setAvailableTimeSlots] = useState([])

  // State for form validation
  const [formErrors, setFormErrors] = useState({
    doctor: false,
    patient: false,
    date: false,
    time: false,
    reason: false,
  })

  // Function to validate the appointment form
  const validateAppointmentForm = (appointment) => {
    const errors = {
      doctor: !appointment.doctorId,
      patient: !appointment.patientId,
      date: !appointment.date,
      time: !appointment.time,
      reason: !appointment.reason,
    }

    setFormErrors(errors)
    return !Object.values(errors).some((error) => error)
  }

  // Function to handle creating a new appointment
  const handleCreateAppointment = () => {
    // Validate form
    if (!validateAppointmentForm(newAppointment)) {
      toast.error("Please fill in all required fields")
      return
    }

    // Create new appointment
    const doctor = doctors.find((d) => d.id === newAppointment.doctorId)
    const patient = patients.find((p) => p.id === newAppointment.patientId)

    const newAppointmentObj = {
      id: upcomingAppointments.length + pastAppointments.length + 1,
      date: format(newAppointment.date, "MMM dd, yyyy"),
      time: newAppointment.time,
      patient: patient.name,
      patientId: patient.id,
      doctor: doctor.name,
      doctorId: doctor.id,
      reason: newAppointment.reason,
      status: newAppointment.status,
      vaccines: newAppointment.vaccines || "None",
    }

    // Add to appointments list
    const updatedAppointments = [...upcomingAppointments, newAppointmentObj]
    setUpcomingAppointments(updatedAppointments)

    // Save to localStorage for doctor portal
    localStorage.setItem(
      "pulmocare_appointments",
      JSON.stringify({
        upcoming: updatedAppointments,
        past: pastAppointments,
      }),
    )

    // Reset form
    setNewAppointment({
      doctorId: "",
      patientId: "",
      date: new Date(),
      time: "",
      reason: "",
      status: "Pending",
      vaccines: "",
    })

    setCreateDialogOpen(false)
    toast.success("Appointment created successfully")
  }

  // Function to handle editing an appointment
  const handleEditAppointment = () => {
    if (!editingAppointment) return

    // Validate form
    if (!validateAppointmentForm(editingAppointment)) {
      toast.error("Please fill in all required fields")
      return
    }

    // Format date for saving
    const formattedAppointment = { ...editingAppointment }

    if (typeof formattedAppointment.date !== "string") {
      const today = new Date()
      const tomorrow = new Date(today)
      tomorrow.setDate(today.getDate() + 1)

      // Format date as "Today", "Tomorrow", or "MMM dd, yyyy"
      if (formattedAppointment.date.toDateString() === today.toDateString()) {
        formattedAppointment.date = "Today"
      } else if (formattedAppointment.date.toDateString() === tomorrow.toDateString()) {
        formattedAppointment.date = "Tomorrow"
      } else {
        formattedAppointment.date = format(formattedAppointment.date, "MMM dd, yyyy")
      }
    }

    // Update appointment in the appropriate list
    const isUpcoming = upcomingAppointments.some((app) => app.id === formattedAppointment.id)

    let updatedUpcoming = upcomingAppointments
    let updatedPast = pastAppointments

    if (isUpcoming) {
      updatedUpcoming = upcomingAppointments.map((app) =>
        app.id === formattedAppointment.id ? formattedAppointment : app,
      )
      setUpcomingAppointments(updatedUpcoming)
    } else {
      updatedPast = pastAppointments.map((app) => (app.id === formattedAppointment.id ? formattedAppointment : app))
      setPastAppointments(updatedPast)
    }

    // Save to localStorage for doctor portal
    localStorage.setItem(
      "pulmocare_appointments",
      JSON.stringify({
        upcoming: updatedUpcoming,
        past: updatedPast,
      }),
    )

    // Reset editing state
    setEditingAppointment(null)
    setEditDialogOpen(false)
    setCurrentEditId(null)
    toast.success("Appointment updated successfully")
  }

  // Function to handle deleting an appointment
  const handleDeleteAppointment = (id) => {
    // Check if appointment is in upcoming or past list
    const isUpcoming = upcomingAppointments.some((app) => app.id === id)

    let updatedUpcoming = upcomingAppointments
    let updatedPast = pastAppointments

    if (isUpcoming) {
      updatedUpcoming = upcomingAppointments.filter((app) => app.id !== id)
      setUpcomingAppointments(updatedUpcoming)
    } else {
      updatedPast = pastAppointments.filter((app) => app.id !== id)
      setPastAppointments(updatedPast)
    }

    // Save to localStorage for doctor portal
    localStorage.setItem(
      "pulmocare_appointments",
      JSON.stringify({
        upcoming: updatedUpcoming,
        past: updatedPast,
      }),
    )

    setEditDialogOpen(false)
    setCurrentEditId(null)
    toast.success("Appointment deleted successfully")
  }

  // Function to check if a time slot is available for a doctor on a specific date
  const isTimeSlotAvailable = (doctorId, date, time, currentAppointmentId = null) => {
    // Get doctor's schedule
    const doctor = doctors.find((d) => d.id === doctorId)
    if (!doctor) return false

    // Check if doctor works on this day
    const dayOfWeek = date.getDay() // 0 = Sunday, 1 = Monday, etc.
    if (!doctor.schedule.days.includes(dayOfWeek)) return false

    // Check if time is within doctor's working hours
    const timeHour = Number.parseInt(time.split(":")[0])
    const startHour = Number.parseInt(doctor.schedule.start.split(":")[0])
    const endHour = Number.parseInt(doctor.schedule.end.split(":")[0])
    if (timeHour < startHour || timeHour >= endHour) return false

    // Check if doctor already has an appointment at this time
    const formattedDate = format(date, "MMM dd, yyyy")
    const hasConflict = upcomingAppointments.some(
      (app) =>
        app.doctorId === doctorId && app.date === formattedDate && app.time === time && app.id !== currentAppointmentId,
    )

    return !hasConflict
  }

  // Function to get available time slots for a doctor on a specific date
  const getAvailableTimeSlots = (doctorId, date, currentAppointmentId = null) => {
    if (!doctorId || !date) return []

    // Get doctor's schedule
    const doctor = doctors.find((d) => d.id === doctorId)
    if (!doctor) return []

    // Check if doctor works on this day
    const dayOfWeek = date.getDay() // 0 = Sunday, 1 = Monday, etc.
    if (!doctor.schedule.days.includes(dayOfWeek)) return []

    // Filter time slots based on doctor's schedule and existing appointments
    return timeSlots.filter((time) => {
      const timeHour = Number.parseInt(time.split(":")[0])
      const isPM = time.includes("PM")
      const hour24 = isPM && timeHour !== 12 ? timeHour + 12 : timeHour

      // Check if time is within doctor's working hours
      const startHour = Number.parseInt(doctor.schedule.start.split(":")[0])
      const endHour = Number.parseInt(doctor.schedule.end.split(":")[0])
      if (hour24 < startHour || hour24 >= endHour) return false

      // Check if doctor already has an appointment at this time
      const formattedDate = format(date, "MMM dd, yyyy")
      const hasConflict = upcomingAppointments.some(
        (app) =>
          app.doctorId === doctorId &&
          app.date === formattedDate &&
          app.time === time &&
          app.id !== currentAppointmentId,
      )

      return !hasConflict
    })
  }

  // Update available time slots when doctor or date changes in create form
  useEffect(() => {
    if (newAppointment.doctorId && newAppointment.date) {
      setAvailableTimeSlots(getAvailableTimeSlots(newAppointment.doctorId, newAppointment.date))
    }
  }, [newAppointment.doctorId, newAppointment.date])

  // Update available time slots when doctor or date changes in edit form
  useEffect(() => {
    if (editingAppointment?.doctorId && editingAppointment?.date) {
      const date =
        typeof editingAppointment.date === "string" ? new Date(editingAppointment.date) : editingAppointment.date

      setAvailableTimeSlots(getAvailableTimeSlots(editingAppointment.doctorId, date, editingAppointment.id))
    }
  }, [editingAppointment?.doctorId, editingAppointment?.date])

  // Function to initialize editing appointment
  const initializeEditingAppointment = (appointment) => {
    if (currentEditId !== appointment.id) {
      setCurrentEditId(appointment.id)
      setEditingAppointment({
        ...appointment,
        date: new Date(
          appointment.date === "Today"
            ? new Date()
            : appointment.date === "Tomorrow"
              ? new Date(new Date().setDate(new Date().getDate() + 1))
              : appointment.date,
        ),
      })
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Appointments</h1>
          <p className="text-muted-foreground">Manage clinic appointments.</p>
        </div>

        {/* Create Appointment Dialog */}
        <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button
              onClick={() => {
                setNewAppointment({
                  doctorId: "",
                  patientId: "",
                  date: new Date(),
                  time: "",
                  reason: "",
                  status: "Pending",
                  vaccines: "",
                })
                setFormErrors({
                  doctor: false,
                  patient: false,
                  date: false,
                  time: false,
                  reason: false,
                })
                setCreateDialogOpen(true)
              }}
            >
              Create Appointment
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[500px]">
            <DialogHeader>
              <DialogTitle>Create New Appointment</DialogTitle>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="doctor" className="flex items-center">
                    Doctor <span className="text-red-500 ml-1">*</span>
                  </Label>
                  <Select
                    value={newAppointment.doctorId}
                    onValueChange={(value) => {
                      setNewAppointment({ ...newAppointment, doctorId: value, time: "" })
                      setFormErrors({ ...formErrors, doctor: false })
                    }}
                  >
                    <SelectTrigger className={formErrors.doctor ? "border-red-500" : ""}>
                      <SelectValue placeholder="Select doctor" />
                    </SelectTrigger>
                    <SelectContent>
                      {doctors.map((doctor) => (
                        <SelectItem key={doctor.id} value={doctor.id}>
                          {doctor.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {formErrors.doctor && <p className="text-red-500 text-sm">Doctor is required</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="patient" className="flex items-center">
                    Patient <span className="text-red-500 ml-1">*</span>
                  </Label>
                  <Select
                    value={newAppointment.patientId}
                    onValueChange={(value) => {
                      setNewAppointment({ ...newAppointment, patientId: value })
                      setFormErrors({ ...formErrors, patient: false })
                    }}
                  >
                    <SelectTrigger className={formErrors.patient ? "border-red-500" : ""}>
                      <SelectValue placeholder="Select patient" />
                    </SelectTrigger>
                    <SelectContent>
                      {patients.map((patient) => (
                        <SelectItem key={patient.id} value={patient.id}>
                          {patient.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {formErrors.patient && <p className="text-red-500 text-sm">Patient is required</p>}
                </div>
              </div>

              <div className="space-y-2">
                <Label className="flex items-center">
                  Date <span className="text-red-500 ml-1">*</span>
                </Label>
                <div className={`border rounded-md p-2 ${formErrors.date ? "border-red-500" : ""}`}>
                  <Calendar
                    mode="single"
                    selected={newAppointment.date}
                    onSelect={(date) => {
                      setNewAppointment({ ...newAppointment, date, time: "" })
                      setFormErrors({ ...formErrors, date: false })
                    }}
                    disabled={(date) => {
                      // Disable past dates and weekends if doctor doesn't work
                      const today = new Date()
                      today.setHours(0, 0, 0, 0)

                      if (isBefore(date, today)) return true

                      if (newAppointment.doctorId) {
                        const doctor = doctors.find((d) => d.id === newAppointment.doctorId)
                        if (doctor && !doctor.schedule.days.includes(date.getDay())) {
                          return true
                        }
                      }

                      return false
                    }}
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
                {formErrors.date && <p className="text-red-500 text-sm">Date is required</p>}
                {newAppointment.doctorId && newAppointment.date && (
                  <div className="text-sm mt-1">
                    <p className="font-medium">Doctor's Schedule:</p>
                    {(() => {
                      const doctor = doctors.find((d) => d.id === newAppointment.doctorId)
                      if (doctor) {
                        const dayOfWeek = newAppointment.date.getDay()
                        if (doctor.schedule.days.includes(dayOfWeek)) {
                          return (
                            <p className="text-green-600">
                              Available from {doctor.schedule.start} to {doctor.schedule.end}
                            </p>
                          )
                        } else {
                          return <p className="text-red-500">Doctor is not available on this day</p>
                        }
                      }
                      return null
                    })()}
                  </div>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="time" className="flex items-center">
                  Time <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={newAppointment.time}
                  onValueChange={(value) => {
                    setNewAppointment({ ...newAppointment, time: value })
                    setFormErrors({ ...formErrors, time: false })
                  }}
                  disabled={!newAppointment.doctorId || !newAppointment.date}
                >
                  <SelectTrigger className={formErrors.time ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select time" />
                  </SelectTrigger>
                  <SelectContent>
                    {availableTimeSlots.map((time) => (
                      <SelectItem key={time} value={time}>
                        {time}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {formErrors.time && <p className="text-red-500 text-sm">Time is required</p>}
                {newAppointment.doctorId && newAppointment.date && availableTimeSlots.length === 0 && (
                  <p className="text-sm text-red-500">No available time slots for this doctor on the selected date.</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="reason" className="flex items-center">
                  Reason <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={newAppointment.reason}
                  onValueChange={(value) => {
                    setNewAppointment({ ...newAppointment, reason: value })
                    setFormErrors({ ...formErrors, reason: false })
                  }}
                >
                  <SelectTrigger className={formErrors.reason ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select reason" />
                  </SelectTrigger>
                  <SelectContent>
                    {appointmentReasons.map((reason) => (
                      <SelectItem key={reason} value={reason}>
                        {reason}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {formErrors.reason && <p className="text-red-500 text-sm">Reason is required</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="status">Status</Label>
                <Select
                  value={newAppointment.status}
                  onValueChange={(value) => setNewAppointment({ ...newAppointment, status: value })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Confirmed">Confirmed</SelectItem>
                    <SelectItem value="Pending">Pending</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="vaccine">Vaccine Reminder (Optional)</Label>
                <Input
                  id="vaccine"
                  placeholder="Add vaccine reminder"
                  value={newAppointment.vaccines}
                  onChange={(e) => setNewAppointment({ ...newAppointment, vaccines: e.target.value })}
                />
              </div>
            </div>
            <DialogFooter>
              <DialogClose asChild>
                <Button variant="outline">Cancel</Button>
              </DialogClose>
              <Button onClick={handleCreateAppointment}>Create Appointment</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <Tabs defaultValue="upcoming">
        <TabsList>
          <TabsTrigger value="upcoming">Upcoming</TabsTrigger>
          <TabsTrigger value="past">Past</TabsTrigger>
        </TabsList>

        <TabsContent value="upcoming" className="space-y-4 pt-4">
          {upcomingAppointments.map((appointment) => (
            <Card key={appointment.id}>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div className="grid gap-1">
                    <div className="font-semibold">{appointment.patient}</div>
                    <div className="text-sm text-muted-foreground">{appointment.reason}</div>
                    <div className="text-sm">{appointment.doctor}</div>
                    <div className="flex items-center gap-2 text-sm">
                      <span>{appointment.date}</span>
                      <span>•</span>
                      <span>{appointment.time}</span>
                      <span>•</span>
                      <span className={appointment.status === "Confirmed" ? "text-green-500" : "text-amber-500"}>
                        {appointment.status}
                      </span>
                    </div>
                    {appointment.vaccines !== "None" && (
                      <div className="text-sm text-secondary">Vaccine: {appointment.vaccines}</div>
                    )}
                  </div>

                  <div className="flex gap-2">
                    {/* Edit Dialog */}
                    <Dialog
                      open={editDialogOpen && currentEditId === appointment.id}
                      onOpenChange={(open) => {
                        setEditDialogOpen(open)
                        if (open) {
                          initializeEditingAppointment(appointment)
                          setFormErrors({
                            doctor: false,
                            patient: false,
                            date: false,
                            time: false,
                            reason: false,
                          })
                        } else {
                          setCurrentEditId(null)
                        }
                      }}
                    >
                      <DialogTrigger asChild>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setEditDialogOpen(true)
                            initializeEditingAppointment(appointment)
                            setFormErrors({
                              doctor: false,
                              patient: false,
                              date: false,
                              time: false,
                              reason: false,
                            })
                          }}
                        >
                          Edit
                        </Button>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader>
                          <DialogTitle>Edit Appointment</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                          {editingAppointment && editingAppointment.id === appointment.id && (
                            <>
                              <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                  <Label htmlFor="doctor" className="flex items-center">
                                    Doctor <span className="text-red-500 ml-1">*</span>
                                  </Label>
                                  <Select
                                    value={editingAppointment.doctorId}
                                    onValueChange={(value) => {
                                      setEditingAppointment({
                                        ...editingAppointment,
                                        doctorId: value,
                                        doctor: doctors.find((d) => d.id === value)?.name || "",
                                        time: "", // Reset time when doctor changes
                                      })
                                      setFormErrors({ ...formErrors, doctor: false })
                                    }}
                                  >
                                    <SelectTrigger className={formErrors.doctor ? "border-red-500" : ""}>
                                      <SelectValue placeholder="Select doctor" />
                                    </SelectTrigger>
                                    <SelectContent>
                                      {doctors.map((doctor) => (
                                        <SelectItem key={doctor.id} value={doctor.id}>
                                          {doctor.name}
                                        </SelectItem>
                                      ))}
                                    </SelectContent>
                                  </Select>
                                  {formErrors.doctor && <p className="text-red-500 text-sm">Doctor is required</p>}
                                </div>
                                <div className="space-y-2">
                                  <Label htmlFor="patient" className="flex items-center">
                                    Patient <span className="text-red-500 ml-1">*</span>
                                  </Label>
                                  <Select
                                    value={editingAppointment.patientId}
                                    onValueChange={(value) => {
                                      setEditingAppointment({
                                        ...editingAppointment,
                                        patientId: value,
                                        patient: patients.find((p) => p.id === value)?.name || "",
                                      })
                                      setFormErrors({ ...formErrors, patient: false })
                                    }}
                                  >
                                    <SelectTrigger className={formErrors.patient ? "border-red-500" : ""}>
                                      <SelectValue placeholder="Select patient" />
                                    </SelectTrigger>
                                    <SelectContent>
                                      {patients.map((patient) => (
                                        <SelectItem key={patient.id} value={patient.id}>
                                          {patient.name}
                                        </SelectItem>
                                      ))}
                                    </SelectContent>
                                  </Select>
                                  {formErrors.patient && <p className="text-red-500 text-sm">Patient is required</p>}
                                </div>
                              </div>

                              <div className="space-y-2">
                                <Label className="flex items-center">
                                  Date <span className="text-red-500 ml-1">*</span>
                                </Label>
                                <div className={`border rounded-md p-2 ${formErrors.date ? "border-red-500" : ""}`}>
                                  <Calendar
                                    mode="single"
                                    selected={
                                      typeof editingAppointment.date === "string"
                                        ? new Date(editingAppointment.date)
                                        : editingAppointment.date
                                    }
                                    onSelect={(date) => {
                                      setEditingAppointment({
                                        ...editingAppointment,
                                        date,
                                        time: "", // Reset time when date changes
                                      })
                                      setFormErrors({ ...formErrors, date: false })
                                    }}
                                    disabled={(date) => {
                                      // Disable past dates and weekends if doctor doesn't work
                                      const today = new Date()
                                      today.setHours(0, 0, 0, 0)

                                      if (isBefore(date, today)) return true

                                      if (editingAppointment.doctorId) {
                                        const doctor = doctors.find((d) => d.id === editingAppointment.doctorId)
                                        if (doctor && !doctor.schedule.days.includes(date.getDay())) {
                                          return true
                                        }
                                      }

                                      return false
                                    }}
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
                                {formErrors.date && <p className="text-red-500 text-sm">Date is required</p>}
                                {editingAppointment.doctorId && editingAppointment.date && (
                                  <div className="text-sm mt-1">
                                    <p className="font-medium">Doctor's Schedule:</p>
                                    {(() => {
                                      const doctor = doctors.find((d) => d.id === editingAppointment.doctorId)
                                      if (doctor) {
                                        const date =
                                          typeof editingAppointment.date === "string"
                                            ? new Date(editingAppointment.date)
                                            : editingAppointment.date
                                        const dayOfWeek = date.getDay()
                                        if (doctor.schedule.days.includes(dayOfWeek)) {
                                          return (
                                            <p className="text-green-600">
                                              Available from {doctor.schedule.start} to {doctor.schedule.end}
                                            </p>
                                          )
                                        } else {
                                          return <p className="text-red-500">Doctor is not available on this day</p>
                                        }
                                      }
                                      return null
                                    })()}
                                  </div>
                                )}
                              </div>

                              <div className="space-y-2">
                                <Label htmlFor="time" className="flex items-center">
                                  Time <span className="text-red-500 ml-1">*</span>
                                </Label>
                                <Select
                                  value={editingAppointment.time}
                                  onValueChange={(value) => {
                                    setEditingAppointment({ ...editingAppointment, time: value })
                                    setFormErrors({ ...formErrors, time: false })
                                  }}
                                >
                                  <SelectTrigger className={formErrors.time ? "border-red-500" : ""}>
                                    <SelectValue placeholder="Select time" />
                                  </SelectTrigger>
                                  <SelectContent>
                                    {availableTimeSlots.map((time) => (
                                      <SelectItem key={time} value={time}>
                                        {time}
                                      </SelectItem>
                                    ))}
                                  </SelectContent>
                                </Select>
                                {formErrors.time && <p className="text-red-500 text-sm">Time is required</p>}
                                {editingAppointment.doctorId &&
                                  editingAppointment.date &&
                                  availableTimeSlots.length === 0 && (
                                    <p className="text-sm text-red-500">
                                      No available time slots for this doctor on the selected date.
                                    </p>
                                  )}
                              </div>

                              <div className="space-y-2">
                                <Label htmlFor="reason" className="flex items-center">
                                  Reason <span className="text-red-500 ml-1">*</span>
                                </Label>
                                <Select
                                  value={editingAppointment.reason}
                                  onValueChange={(value) => {
                                    setEditingAppointment({ ...editingAppointment, reason: value })
                                    setFormErrors({ ...formErrors, reason: false })
                                  }}
                                >
                                  <SelectTrigger className={formErrors.reason ? "border-red-500" : ""}>
                                    <SelectValue placeholder="Select reason" />
                                  </SelectTrigger>
                                  <SelectContent>
                                    {appointmentReasons.map((reason) => (
                                      <SelectItem key={reason} value={reason}>
                                        {reason}
                                      </SelectItem>
                                    ))}
                                  </SelectContent>
                                </Select>
                                {formErrors.reason && <p className="text-red-500 text-sm">Reason is required</p>}
                              </div>

                              <div className="space-y-2">
                                <Label htmlFor="status">Status</Label>
                                <Select
                                  value={editingAppointment.status}
                                  onValueChange={(value) =>
                                    setEditingAppointment({ ...editingAppointment, status: value })
                                  }
                                >
                                  <SelectTrigger>
                                    <SelectValue placeholder="Select status" />
                                  </SelectTrigger>
                                  <SelectContent>
                                    <SelectItem value="Confirmed">Confirmed</SelectItem>
                                    <SelectItem value="Pending">Pending</SelectItem>
                                    <SelectItem value="Cancelled">Cancelled</SelectItem>
                                  </SelectContent>
                                </Select>
                              </div>

                              <div className="space-y-2">
                                <Label htmlFor="vaccine">Vaccine Reminder</Label>
                                <Input
                                  id="vaccine"
                                  placeholder="Add vaccine reminder"
                                  value={editingAppointment.vaccines}
                                  onChange={(e) =>
                                    setEditingAppointment({ ...editingAppointment, vaccines: e.target.value })
                                  }
                                />
                              </div>
                            </>
                          )}
                        </div>
                        <DialogFooter>
                          <AlertDialog>
                            <AlertDialogTrigger asChild>
                              <Button variant="destructive">Delete</Button>
                            </AlertDialogTrigger>
                            <AlertDialogContent>
                              <AlertDialogHeader>
                                <AlertDialogTitle>Are you sure?</AlertDialogTitle>
                                <AlertDialogDescription>
                                  This action cannot be undone. This will permanently delete the appointment.
                                </AlertDialogDescription>
                              </AlertDialogHeader>
                              <AlertDialogFooter>
                                <AlertDialogCancel>Cancel</AlertDialogCancel>
                                <AlertDialogAction
                                  onClick={() => {
                                    handleDeleteAppointment(appointment.id)
                                  }}
                                >
                                  Delete
                                </AlertDialogAction>
                              </AlertDialogFooter>
                            </AlertDialogContent>
                          </AlertDialog>

                          <DialogClose asChild>
                            <Button variant="outline">Cancel</Button>
                          </DialogClose>

                          <Button onClick={handleEditAppointment}>Save Changes</Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>

                    {/* Doctor's Schedule Link */}
                    <Link href={`/admin/doctors/schedule/${appointment.doctorId}`} className="w-full h-full">
  <Button variant="outline" size="sm" className="flex items-center justify-center">
    Doctor's Schedule
  </Button>
</Link>

{/* View Patient Link */}
<Link href={`/admin/patients/${appointment.patientId}`} className="w-full h-full">
  <Button variant="outline" size="sm" className="flex items-center justify-center">
    View Patient
  </Button>
</Link>

                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </TabsContent>

        <TabsContent value="past" className="space-y-4 pt-4">
          {pastAppointments.map((appointment) => (
            <Card key={appointment.id}>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div className="grid gap-1">
                    <div className="font-semibold">{appointment.patient}</div>
                    <div className="text-sm text-muted-foreground">{appointment.reason}</div>
                    <div className="text-sm">{appointment.doctor}</div>
                    <div className="flex items-center gap-2 text-sm">
                      <span>{appointment.date}</span>
                      <span>•</span>
                      <span>{appointment.time}</span>
                      <span>•</span>
                      <span className="text-green-500">{appointment.status}</span>
                    </div>
                    {appointment.vaccines !== "None" && (
                      <div className="text-sm text-secondary">Vaccine: {appointment.vaccines}</div>
                    )}
                  </div>

                  <div className="flex gap-2">
<Link href={`/admin/doctors/schedule/${appointment.doctorId}`}>
  <Button variant="outline" size="sm">Doctor's Schedule</Button>
</Link>

<Link href={`/admin/patients/${appointment.patientId}`}>
  <Button variant="outline" size="sm">View Patient</Button>
</Link>

                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </TabsContent>
      </Tabs>
    </div>
  )
}
