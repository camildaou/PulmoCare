"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar } from "@/components/ui/calendar"
import { format, isBefore } from "date-fns"
import Link from "next/link"
import { useState, useEffect } from "react"
import { toast } from "sonner"

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
]

export default function DoctorAppointmentsPage() {
  // State for appointments
  const [upcomingAppointments, setUpcomingAppointments] = useState([
    {
      id: 1,
      date: "Today",
      time: "09:00 AM",
      patient: "Alice Johnson",
      patientId: "1",
      reason: "Follow-up",
      status: "Confirmed",
    },
    {
      id: 2,
      date: "Today",
      time: "10:30 AM",
      patient: "Bob Smith",
      patientId: "2",
      reason: "Initial Consultation",
      status: "Confirmed",
    },
    {
      id: 3,
      date: "Tomorrow",
      time: "01:00 PM",
      patient: "Carol Williams",
      patientId: "3",
      reason: "Test Results",
      status: "Confirmed",
    },
    {
      id: 4,
      date: "Mar 30, 2025",
      time: "02:30 PM",
      patient: "David Brown",
      patientId: "4",
      reason: "Follow-up",
      status: "Pending",
    },
  ])

  const [pastAppointments, setPastAppointments] = useState([
    {
      id: 6,
      date: "Mar 25, 2025",
      time: "11:00 AM",
      patient: "Frank Miller",
      patientId: "6",
      reason: "Follow-up",
      status: "Completed",
    },
    {
      id: 7,
      date: "Mar 24, 2025",
      time: "09:30 AM",
      patient: "Grace Lee",
      patientId: "7",
      reason: "Asthma Review",
      status: "Completed",
    },
    {
      id: 8,
      date: "Mar 22, 2025",
      time: "02:00 PM",
      patient: "Alice Johnson",
      patientId: "1",
      reason: "Follow-up",
      status: "Completed",
    },
  ])

  // State for pending reports
  const [pendingReports, setPendingReports] = useState(3)

  // State for create appointment form
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [newAppointment, setNewAppointment] = useState({
    patientId: "",
    date: new Date(),
    time: "",
    reason: "",
    status: "Pending",
  })

  // State for add patient form
  const [addPatientDialogOpen, setAddPatientDialogOpen] = useState(false)
  const [newPatient, setNewPatient] = useState({
    name: "",
    age: "",
    gender: "",
    phone: "",
    email: "",
    address: "",
    condition: "",
  })

  // State for form validation
  const [formErrors, setFormErrors] = useState({
    patient: false,
    date: false,
    time: false,
    reason: false,
  })

  // State for patient form validation
  const [patientFormErrors, setPatientFormErrors] = useState({
    name: false,
    age: false,
    gender: false,
    phone: false,
    email: false,
    condition: false,
  })

  // State for search
  const [searchQuery, setSearchQuery] = useState("")

  // Filter appointments based on search query
  const filteredUpcoming = upcomingAppointments.filter(
    (appointment) =>
      appointment.patient.toLowerCase().includes(searchQuery.toLowerCase()) ||
      appointment.reason.toLowerCase().includes(searchQuery.toLowerCase()),
  )

  const filteredPast = pastAppointments.filter(
    (appointment) =>
      appointment.patient.toLowerCase().includes(searchQuery.toLowerCase()) ||
      appointment.reason.toLowerCase().includes(searchQuery.toLowerCase()),
  )

  // Load pending reports count from localStorage
  useEffect(() => {
    const pendingReportsData = localStorage.getItem("pulmocare_pending_reports")
    if (pendingReportsData) {
      setPendingReports(Number.parseInt(pendingReportsData))
    } else {
      localStorage.setItem("pulmocare_pending_reports", "3")
    }
  }, [])

  // Validate the appointment form
  const validateAppointmentForm = () => {
    const errors = {
      patient: !newAppointment.patientId,
      date: !newAppointment.date,
      time: !newAppointment.time,
      reason: !newAppointment.reason,
    }

    setFormErrors(errors)
    return !Object.values(errors).some((error) => error)
  }

  // Validate the patient form
  const validatePatientForm = () => {
    const errors = {
      name: !newPatient.name,
      age: !newPatient.age || isNaN(Number(newPatient.age)),
      gender: !newPatient.gender,
      phone: !newPatient.phone,
      email: !newPatient.email,
      condition: !newPatient.condition,
    }

    setPatientFormErrors(errors)
    return !Object.values(errors).some((error) => error)
  }

  // Handle creating a new appointment
  const handleCreateAppointment = () => {
    // Validate form
    if (!validateAppointmentForm()) {
      toast.error("Please fill in all required fields")
      return
    }

    // Create new appointment
    const patient = patients.find((p) => p.id === newAppointment.patientId)

    const newAppointmentObj = {
      id: upcomingAppointments.length + pastAppointments.length + 1,
      date: format(newAppointment.date, "MMM dd, yyyy"),
      time: newAppointment.time,
      patient: patient.name,
      patientId: patient.id,
      reason: newAppointment.reason,
      status: newAppointment.status,
    }

    // Add to appointments list
    const updatedAppointments = [...upcomingAppointments, newAppointmentObj]
    setUpcomingAppointments(updatedAppointments)

    // Reset form
    setNewAppointment({
      patientId: "",
      date: new Date(),
      time: "",
      reason: "",
      status: "Pending",
    })

    setCreateDialogOpen(false)
    toast.success("Appointment created successfully")
  }

  // Handle adding a new patient
  const handleAddPatient = () => {
    if (!validatePatientForm()) {
      toast.error("Please fill in all required fields correctly")
      return
    }

    // In a real app, you would save this to your backend
    // For now, we'll just show a success message
    toast.success("Patient added successfully")
    setAddPatientDialogOpen(false)

    // Reset form
    setNewPatient({
      name: "",
      age: "",
      gender: "",
      phone: "",
      email: "",
      address: "",
      condition: "",
    })
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Appointments</h1>
          <p className="text-muted-foreground">Manage your appointments and patient visits.</p>
        </div>
        <div className="flex gap-2">
          <Button
            onClick={() => {
              setCreateDialogOpen(true)
              setFormErrors({
                patient: false,
                date: false,
                time: false,
                reason: false,
              })
            }}
          >
            Create Appointment
          </Button>
        </div>
      </div>

      {/* Add Patient Dialog */}
      <Dialog open={addPatientDialogOpen} onOpenChange={setAddPatientDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Add New Patient</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4 max-h-[70vh] overflow-y-auto pr-2">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="name" className="flex items-center">
                  Full Name <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="name"
                  placeholder="Enter patient name"
                  value={newPatient.name}
                  onChange={(e) => {
                    setNewPatient({ ...newPatient, name: e.target.value })
                    setPatientFormErrors({ ...patientFormErrors, name: false })
                  }}
                  className={patientFormErrors.name ? "border-red-500" : ""}
                />
                {patientFormErrors.name && <p className="text-red-500 text-sm">Name is required</p>}
              </div>
              <div className="space-y-2">
                <Label htmlFor="age" className="flex items-center">
                  Age <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="age"
                  type="number"
                  placeholder="Enter age"
                  value={newPatient.age}
                  onChange={(e) => {
                    setNewPatient({ ...newPatient, age: e.target.value })
                    setPatientFormErrors({ ...patientFormErrors, age: false })
                  }}
                  className={patientFormErrors.age ? "border-red-500" : ""}
                />
                {patientFormErrors.age && <p className="text-red-500 text-sm">Valid age is required</p>}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="gender" className="flex items-center">
                Gender <span className="text-red-500 ml-1">*</span>
              </Label>
              <Select
                value={newPatient.gender}
                onValueChange={(value) => {
                  setNewPatient({ ...newPatient, gender: value })
                  setPatientFormErrors({ ...patientFormErrors, gender: false })
                }}
              >
                <SelectTrigger className={patientFormErrors.gender ? "border-red-500" : ""}>
                  <SelectValue placeholder="Select gender" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Male">Male</SelectItem>
                  <SelectItem value="Female">Female</SelectItem>
                  <SelectItem value="Other">Other</SelectItem>
                </SelectContent>
              </Select>
              {patientFormErrors.gender && <p className="text-red-500 text-sm">Gender is required</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="phone" className="flex items-center">
                  Phone Number <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="phone"
                  placeholder="Enter phone number"
                  value={newPatient.phone}
                  onChange={(e) => {
                    setNewPatient({ ...newPatient, phone: e.target.value })
                    setPatientFormErrors({ ...patientFormErrors, phone: false })
                  }}
                  className={patientFormErrors.phone ? "border-red-500" : ""}
                />
                {patientFormErrors.phone && <p className="text-red-500 text-sm">Phone number is required</p>}
              </div>
              <div className="space-y-2">
                <Label htmlFor="email" className="flex items-center">
                  Email <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="Enter email address"
                  value={newPatient.email}
                  onChange={(e) => {
                    setNewPatient({ ...newPatient, email: e.target.value })
                    setPatientFormErrors({ ...patientFormErrors, email: false })
                  }}
                  className={patientFormErrors.email ? "border-red-500" : ""}
                />
                {patientFormErrors.email && <p className="text-red-500 text-sm">Email is required</p>}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="address">Address</Label>
              <Input
                id="address"
                placeholder="Enter address (optional)"
                value={newPatient.address}
                onChange={(e) => setNewPatient({ ...newPatient, address: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="condition" className="flex items-center">
                Medical Condition <span className="text-red-500 ml-1">*</span>
              </Label>
              <Input
                id="condition"
                placeholder="Enter primary medical condition"
                value={newPatient.condition}
                onChange={(e) => {
                  setNewPatient({ ...newPatient, condition: e.target.value })
                  setPatientFormErrors({ ...patientFormErrors, condition: false })
                }}
                className={patientFormErrors.condition ? "border-red-500" : ""}
              />
              {patientFormErrors.condition && <p className="text-red-500 text-sm">Medical condition is required</p>}
            </div>
          </div>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">Cancel</Button>
            </DialogClose>
            <Button onClick={handleAddPatient}>Add Patient</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Create Appointment Dialog */}
      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Create New Appointment</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4 max-h-[70vh] overflow-y-auto pr-2">
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

            <div className="space-y-2">
              <Label className="flex items-center">
                Date <span className="text-red-500 ml-1">*</span>
              </Label>
              <div className={`border rounded-md p-2 ${formErrors.date ? "border-red-500" : ""}`}>
                <Calendar
                  mode="single"
                  selected={newAppointment.date}
                  onSelect={(date) => {
                    if (date) {
                      setNewAppointment({ ...newAppointment, date, time: "" })
                      setFormErrors({ ...formErrors, date: false })
                    }
                  }}
                  disabled={(date) => {
                    // Disable past dates
                    const today = new Date()
                    today.setHours(0, 0, 0, 0)
                    return isBefore(date, today)
                  }}
                  className="mx-auto"
                />
              </div>
              {formErrors.date && <p className="text-red-500 text-sm">Date is required</p>}
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
              >
                <SelectTrigger className={formErrors.time ? "border-red-500" : ""}>
                  <SelectValue placeholder="Select time" />
                </SelectTrigger>
                <SelectContent>
                  {timeSlots.map((time) => (
                    <SelectItem key={time} value={time}>
                      {time}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {formErrors.time && <p className="text-red-500 text-sm">Time is required</p>}
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
          </div>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">Cancel</Button>
            </DialogClose>
            <Button onClick={handleCreateAppointment}>Create Appointment</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <div className="flex items-center space-x-2">
        <Input
          type="search"
          placeholder="Search appointments..."
          className="max-w-sm"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      {pendingReports > 0 && (
        <Card className="bg-amber-50 border-amber-200">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-medium text-amber-800">Pending Reports</h3>
                <p className="text-sm text-amber-700">
                  You have {pendingReports} appointment report{pendingReports !== 1 ? "s" : ""} pending completion.
                </p>
              </div>
              <Link href="/doctor/appointments/6">
                <Button variant="outline" className="border-amber-500 text-amber-700 hover:bg-amber-100">
                  Complete Reports
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      )}

      <Tabs defaultValue="upcoming">
        <TabsList>
          <TabsTrigger value="upcoming">Upcoming</TabsTrigger>
          <TabsTrigger value="past">Past</TabsTrigger>
        </TabsList>

        <TabsContent value="upcoming" className="space-y-4 pt-4">
          {filteredUpcoming.length > 0 ? (
            filteredUpcoming.map((appointment) => (
              <Card key={appointment.id}>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <div className="grid gap-1">
                      <div className="font-semibold">{appointment.patient}</div>
                      <div className="text-sm text-muted-foreground">{appointment.reason}</div>
                      <div className="flex items-center gap-2 text-sm">
                        <span>{appointment.date}</span>
                        <span>•</span>
                        <span>{appointment.time}</span>
                        <span>•</span>
                        <span className={appointment.status === "Confirmed" ? "text-green-500" : "text-amber-500"}>
                          {appointment.status}
                        </span>
                      </div>
                    </div>

                    <div className="flex gap-2">
                      <Link href={`/doctor/patients/${appointment.patientId}`}>
                        <Button variant="outline" size="sm">
                          View Patient
                        </Button>
                      </Link>

                      <Link href={`/doctor/appointments/${appointment.id}`}>
                        <Button size="sm">Start Appointment</Button>
                      </Link>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          ) : (
            <div className="text-center py-10">
              <p className="text-muted-foreground">No upcoming appointments found.</p>
            </div>
          )}
        </TabsContent>

        <TabsContent value="past" className="space-y-4 pt-4">
          {filteredPast.length > 0 ? (
            filteredPast.map((appointment) => (
              <Card key={appointment.id}>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <div className="grid gap-1">
                      <div className="font-semibold">{appointment.patient}</div>
                      <div className="text-sm text-muted-foreground">{appointment.reason}</div>
                      <div className="flex items-center gap-2 text-sm">
                        <span>{appointment.date}</span>
                        <span>•</span>
                        <span>{appointment.time}</span>
                        <span>•</span>
                        <span className="text-green-500">{appointment.status}</span>
                      </div>
                    </div>

                    <div className="flex gap-2">
<Link href={`/doctor/patients/${appointment.patientId}`}>
  <Button variant="outline" size="sm">View Patient</Button>
</Link>

<Link href={`/doctor/appointments/${appointment.id}`}>
  <Button variant="outline" size="sm">View Details</Button>
</Link>

                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          ) : (
            <div className="text-center py-10">
              <p className="text-muted-foreground">No past appointments found.</p>
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}
