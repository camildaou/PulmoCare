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
import { adminApi } from "@/lib/api"
import { Appointment } from "@/lib/types"

export default function DoctorAppointmentsPage() {
  // State for appointments
  const [upcomingAppointments, setUpcomingAppointments] = useState<Appointment[]>([])
  const [pastAppointments, setPastAppointments] = useState<Appointment[]>([])

  // State for pending reports
  const [pendingReports, setPendingReports] = useState(3)

  // State for create appointment form
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [newAppointment, setNewAppointment] = useState({
    patient: { id: "", firstName: "", lastName: "", name: "" },
    date: "", // Changed to string for manual input
    time: "",
    reason: "",
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

  // State for patients
  const [patients, setPatients] = useState<
    { id: string; name: string; firstName: string; lastName: string }[]
  >([])

  // State for available time slots
  const [availableTimes, setAvailableTimes] = useState([])

  // Predefined list of reasons
  const reasons = [
    "Routine Check-up",
    "Follow-up Appointment",
    "Respiratory Issues",
    "Asthma Management",
    "COPD Management",
    "Sleep Apnea",
    "Lung Cancer Screening",
    "Pneumonia",
    "Tuberculosis",
    "Shortness of Breath",
    "Chronic Cough",
  ]

  // Filter appointments based on search query
  const filteredUpcoming = upcomingAppointments.filter(
    (appointment) =>
      `${appointment.patient?.firstName} ${appointment.patient?.lastName}`
        .toLowerCase()
        .includes(searchQuery.toLowerCase()) ||
      (appointment.reason && appointment.reason.toLowerCase().includes(searchQuery.toLowerCase())),
  )

  const filteredPast = pastAppointments.filter(
    (appointment) =>
      `${appointment.patient?.firstName} ${appointment.patient?.lastName}`
        .toLowerCase()
        .includes(searchQuery.toLowerCase()) ||
      (appointment.reason && appointment.reason.toLowerCase().includes(searchQuery.toLowerCase())),
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

  // Fetch appointments for the current doctor
  useEffect(() => {
    async function fetchAppointments() {
      try {
        const doctorId = localStorage.getItem("pulmocare_doctor_id") // Fetch doctor ID from localStorage
        if (!doctorId) {
          console.error("Doctor ID is not available in localStorage.")
          return
        }

        const appointments: Appointment[] = await adminApi.getAppointmentsByDoctorId(doctorId)

        const now = new Date()
        const past = appointments.filter((appointment) => new Date(appointment.date) < now)
        const upcoming = appointments.filter((appointment) => new Date(appointment.date) >= now)

        setPastAppointments(past)
        setUpcomingAppointments(upcoming)
      } catch (error) {
        console.error("Error fetching appointments:", error)
      }
    }

    fetchAppointments()
  }, [])

  // Fetch patients from the database
  useEffect(() => {
    async function fetchPatients() {
      try {
        const response = await adminApi.getAllPatients() // Replace with actual API call
        setPatients(response)
      } catch (error) {
        console.error("Error fetching patients:", error)
      }
    }

    fetchPatients()
  }, [])

  // Fetch available time slots for the selected date
  useEffect(() => {
    async function fetchAvailableTimes() {
      if (!newAppointment.date) return

      try {
        const response = await adminApi.getAvailableTimes(newAppointment.date) // Replace with actual API call
        setAvailableTimes(response)
      } catch (error) {
        console.error("Error fetching available times:", error)
      }
    }

    fetchAvailableTimes()
  }, [newAppointment.date])

  // Validate the appointment form
  const validateAppointmentForm = () => {
    const errors = {
      patient: !newAppointment.patient.firstName || !newAppointment.patient.lastName,
      date: !newAppointment.date, // Validate manual date input
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

    const newAppointmentObj: Appointment = {
      id: `${Date.now()}`, // Generate a unique ID
      date: newAppointment.date, // Use manual date input
      hour: newAppointment.time,
      time: newAppointment.time, // Added the `time` property
      reason: newAppointment.reason,
      patient: { ...newAppointment.patient },
      doctor: { id: "currentDoctorId", name: "" }, // Placeholder for doctor details
    }

    // Add to appointments list
    const updatedAppointments = [...upcomingAppointments, newAppointmentObj]
    setUpcomingAppointments(updatedAppointments)

    // Reset form
    setNewAppointment({
      patient: { id: "", firstName: "", lastName: "", name: "" },
      date: "", // Reset manual date input
      time: "",
      reason: "",
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
                value={newAppointment.patient.id}
                onValueChange={(value) => {
                  const selectedPatient = patients.find((p) => p.id === value)
                  if (selectedPatient) {
                    setNewAppointment({
                      ...newAppointment,
                      patient: { id: selectedPatient.id, name: selectedPatient.name, firstName: selectedPatient.firstName, lastName: selectedPatient.lastName },
                    })
                    setFormErrors({ ...formErrors, patient: false })
                  }
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
                  {availableTimes.map((time) => (
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
                  {reasons.map((reason) => (
                    <SelectItem key={reason} value={reason}>
                      {reason}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {formErrors.reason && <p className="text-red-500 text-sm">Reason is required</p>}
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
                      <div className="font-semibold">Patient: {appointment.patient?.firstName} {appointment.patient?.lastName}</div>
                      <div className="text-sm">Date: {appointment.date}</div>
                      <div className="text-sm">Time: {appointment.hour}</div>
                      <div className="text-sm">Reason: {appointment.reason}</div>
                    </div>

                    <div className="flex gap-2">
                      <Link href={`/doctor/patients/${appointment.patient?.id}`}>
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
                      <div className="font-semibold">Patient: {appointment.patient?.firstName} {appointment.patient?.lastName}</div>
                      <div className="text-sm">Date: {appointment.date}</div>
                      <div className="text-sm">Time: {appointment.time}</div>
                      <div className="text-sm">Reason: {appointment.reason}</div>
                    </div>

                    <div className="flex gap-2">
                      <Link href={`/doctor/patients/${appointment.patient.id}`}>
                        <Button variant="outline" size="sm">
                          View Patient
                        </Button>
                      </Link>

                      <Link href={`/doctor/appointments/${appointment.id}`}>
                        <Button variant="outline" size="sm">
                          View Details
                        </Button>
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
