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
import { useEffect, useState } from "react"
import { format, isBefore, formatISO, parseISO } from "date-fns"
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
import { useRouter } from "next/navigation"
import axios from "axios"
import { Doctor, Patient, Appointment } from "@/lib/types"
import { adminApi, doctorApi, scheduleApi, appointmentsApi } from "@/lib/api"

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
  const router = useRouter()

  // State for logged-in doctor
  const [doctor, setDoctor] = useState<Doctor | null>(null)

  // State for appointments
  const [upcomingAppointments, setUpcomingAppointments] = useState<Appointment[]>([])
  const [pastAppointments, setPastAppointments] = useState<Appointment[]>([])

  // State for create appointment form
  const [newAppointment, setNewAppointment] = useState({
    doctor: null as Doctor | null,
    patient: null as Patient | null,
    date: "", // Use string for date
    time: "",
    reason: "",
    status: "Pending",
  })

  // State for edit appointment form
  const [editingAppointment, setEditingAppointment] = useState<Appointment | null>(null)

  // State to track which dialog is open
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [currentEditId, setCurrentEditId] = useState(null)  // State for available time slots
  const [availableTimeSlots, setAvailableTimeSlots] = useState<string[]>(timeSlots)
  
  // State for form validation
  const [formErrors, setFormErrors] = useState({
    doctor: false,
    patient: false,
    date: false,
    time: false,
    reason: false,
  })

  // State for patients
  const [patients, setPatients] = useState<Patient[]>([])

  // State for doctors
  const [doctors, setDoctors] = useState<Doctor[]>([])

  // Function to validate the appointment form
  const validateAppointmentForm = (appointment: { [key: string]: any }) => {
    const errors = {
      doctor: !appointment.doctor,
      patient: !appointment.patient,
      date: !appointment.date,
      time: !appointment.time,
      reason: !appointment.reason,
    }

    setFormErrors(errors)
    return !Object.values(errors).some((error) => error)
  }
  // Function to handle creating a new appointment
  const handleCreateAppointment = async () => {
    // Validate form
    if (!validateAppointmentForm(newAppointment)) {
      toast.error("Please fill in all required fields")
      return
    }

    try {
      // Format the date to ISO string if it's not already
      const formattedDate = typeof newAppointment.date === 'string' 
        ? newAppointment.date 
        : new Date(newAppointment.date).toISOString().split('T')[0];

      // Get the doctor ID (using the logged-in doctor)
      const doctorId = doctor?.id || '';
      
      // Get the patient ID
      const patientId = newAppointment.patient?.id || '';
        // Call the API to create the appointment
      await import('@/lib/api').then(async ({ createAppointment }) => {
        const response = await createAppointment(
          doctorId,
          patientId,
          formattedDate,
          newAppointment.time,
          newAppointment.reason
        );
          console.log('Appointment created:', response);
            // Check if we got an error response about time slot not being available
        if (response && response.error && response.timeSlotError) {
          // The alert was already shown in the api.ts file
          // We just need to return to prevent further processing
          return;
        }
        
        // Create local appointment object for UI update
        const newAppointmentObj: Appointment = {
          id: response.id || `${upcomingAppointments.length + pastAppointments.length + 1}`,
          date: formattedDate,
          hour: newAppointment.time,
          time: newAppointment.time,
          reason: newAppointment.reason,
          status: newAppointment.status,
          doctor: doctor!,
          patient: newAppointment.patient!,
        }

        // Add to appointments list
        const updatedAppointments = [...upcomingAppointments, newAppointmentObj];
        setUpcomingAppointments(updatedAppointments);
        
        // Reset form
        setNewAppointment({
          doctor: null,
          patient: null,
          date: "",
          time: "",
          reason: "",
          status: "Pending",
        });

        setCreateDialogOpen(false);
        toast.success("Appointment created successfully");
        
        // Refresh appointments list
        if (doctor?.id) {
          const freshAppointments = await doctorApi.getAppointmentsByDoctorId(doctor.id);
          
          // Split appointments into upcoming and past based on current date
          const today = new Date();
          today.setHours(0, 0, 0, 0);
          
          const upcoming = freshAppointments.filter((appt: Appointment) => {
            const appointmentDate = new Date(appt.date);
            return appointmentDate >= today;
          });
          
          const past = freshAppointments.filter((appt: Appointment) => {
            const appointmentDate = new Date(appt.date);
            return appointmentDate < today;
          });
          
          setUpcomingAppointments(upcoming);
          setPastAppointments(past);
        }
      });
    } catch (error) {
      console.error("Error creating appointment:", error);
      toast.error("Failed to create appointment. Please try again.");
    }
  }

  // Function to handle editing an appointment
  const handleEditAppointment = () => {
    if (!editingAppointment) return

    // Validate form
    if (!validateAppointmentForm(editingAppointment)) {
      toast.error("Please fill in all required fields")
      return
    }

    // Ensure `formattedAppointment` includes all required properties
    const formattedAppointment: Appointment = editingAppointment
      ? { ...editingAppointment, hour: editingAppointment.time, patient: editingAppointment.patient }
      : {
          id: "",
          doctor: { id: "", name: "" },
          date: new Date().toISOString(), // Convert to ISO string
          hour: "",
          time: "",
          patient: { id: "", firstName: "", lastName: "", name: "" },
        }

    // Convert `Date` to string for `date` property
    const handleDateConversion = (date: Date | null): string => {
      return date ? date.toISOString() : ""
    }

    // Ensure `formattedAppointment.date` is treated as a string
    if (formattedAppointment.date) {
      const today = new Date()
      const tomorrow = new Date(today)
      tomorrow.setDate(today.getDate() + 1)

      const appointmentDate = new Date(formattedAppointment.date)

      if (appointmentDate.toDateString() === today.toDateString()) {
        formattedAppointment.date = "Today"
      } else if (appointmentDate.toDateString() === tomorrow.toDateString()) {
        formattedAppointment.date = "Tomorrow"
      } else {
        formattedAppointment.date = format(appointmentDate, "MMM dd, yyyy")
      }
    }

    // Update appointment in the appropriate list
    const isUpcoming = upcomingAppointments.some((app) => app.id.toString() === formattedAppointment.id)

    let updatedUpcoming = upcomingAppointments
    let updatedPast = pastAppointments

    if (isUpcoming) {
      updatedUpcoming = upcomingAppointments.map((app) =>
        app.id.toString() === formattedAppointment.id ? formattedAppointment : app,
      )
      setUpcomingAppointments(updatedUpcoming)
    } else {
      updatedPast = pastAppointments.map((app) => (app.id.toString() === formattedAppointment.id ? formattedAppointment : app))
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
const handleDeleteAppointment = async (id: string) => {
  try {
    // First, find the appointment to be deleted so we know what we're deleting
    const appointmentToDelete = [...upcomingAppointments, ...pastAppointments].find(
      appointment => appointment.id === id
    );
    
    if (!appointmentToDelete) {
      console.error("Appointment not found:", id);
      toast.error("Could not find the appointment to delete");
      return;
    }
    
    console.log("Deleting appointment:", appointmentToDelete);
    
    // Call the API to delete the appointment
    const success = await appointmentsApi.deleteAppointment(id);
    
    if (success) {
      // Remove the deleted appointment from the state
      setUpcomingAppointments(prev => 
        prev.filter(appointment => appointment.id !== id)
      );
      setPastAppointments(prev => 
        prev.filter(appointment => appointment.id !== id)
      );
      
      // If we're in the doctor view, also refresh their availability
      const userInfo = localStorage.getItem("pulmocare_user");
      if (userInfo) {
        const user = JSON.parse(userInfo);
        if (user.type === "doctor") {
          // Fetch updated doctor availability and store it to display
          // It's important to tell the user that the time slot has been restored
          const doctorId = user.id;
          const updatedAvailability = await scheduleApi.getDoctorAvailability(doctorId);
          console.log("Updated doctor availability:", updatedAvailability);
          
          // Let the user know which time slot was restored
          if (appointmentToDelete.date && appointmentToDelete.hour) {
            // Format the date to be more readable
            const date = new Date(appointmentToDelete.date);
            const dayName = date.toLocaleDateString('en-US', { weekday: 'long' }).substring(0, 3).toLowerCase();
            
            toast.success(`Appointment deleted and time slot ${appointmentToDelete.hour} on ${dayName} restored to availability`, {
              duration: 4000
            });
          } else {
            toast.success("Appointment deleted successfully and availability updated");
          }
        } else {
          toast.success("Appointment deleted successfully");
        }
      } else {
        toast.success("Appointment deleted successfully");
      }
    } else {
      toast.error("Failed to delete appointment");
    }
  } catch (error) {
    console.error("Error deleting appointment:", error);
    toast.error("Failed to delete appointment. Please try again.");
  }
};  

  // Function to check if a time slot is available for a doctor on a specific date
  const isTimeSlotAvailable = (
    doctorId: string,
    date: Date,
    time: string,
    currentAppointmentId: string | null = null,
  ) => {
    // Get doctor by ID
    const doctor = doctors.find((d) => d.id === doctorId)
    if (!doctor) return false

    // Check if doctor already has an appointment at this time
    const formattedDate = format(date, "MMM dd, yyyy")
    const hasConflict = upcomingAppointments.some(
      (app) =>
        app.doctor.id === doctorId && app.date === formattedDate && app.time === time && app.id.toString() !== currentAppointmentId,
    )

    return !hasConflict
  }
  // Function to get available time slots for a doctor on a specific date
  const getAvailableTimeSlots = (
    doctorId: string,
    date: Date,
    currentAppointmentId: string | null = null,
  ) => {
    if (!doctorId || !date) return timeSlots

    // Filter time slots based on existing appointments
    return timeSlots.filter((time) => {
      const formattedDate = format(date, "MMM dd, yyyy")
      const hasConflict = upcomingAppointments.some(
        (app) =>
          app.doctor.id === doctorId &&
          app.date === formattedDate &&
          app.time === time &&
          app.id.toString() !== currentAppointmentId,
      )

      return !hasConflict
    })
  }
  // Update available time slots when doctor or date changes in create form
  useEffect(() => {
    if (newAppointment.doctor?.id && newAppointment.date) {
      try {
        const date = parseISO(newAppointment.date);
        const availableSlots = getAvailableTimeSlots(
          newAppointment.doctor.id,
          date
        );
        setAvailableTimeSlots(availableSlots);
        console.log("Available time slots updated:", availableSlots);
      } catch (error) {
        console.error("Error updating available time slots:", error);
        // Fallback to all time slots if there's an error
        setAvailableTimeSlots(timeSlots);
      }
    } else {
      // Default to all time slots when doctor or date is not selected
      setAvailableTimeSlots(timeSlots);
    }
  }, [newAppointment.doctor?.id, newAppointment.date])

  // Update available time slots when doctor or date changes in edit form
  useEffect(() => {
    if (editingAppointment) {
      const date = parseISO(editingAppointment.date) // Convert LocalDate to Date
      const availableSlots = getAvailableTimeSlots(
        editingAppointment.doctor.id,
        date,
        editingAppointment.id.toString()
      )
      setAvailableTimeSlots(availableSlots as never[])
    }
  }, [editingAppointment?.doctor.id, editingAppointment?.date])

  // Function to initialize editing appointment
  const initializeEditingAppointment = (appointment: { [key: string]: any }) => {
    if (currentEditId !== appointment.id) {
      setCurrentEditId(appointment.id)
      setEditingAppointment({
        id: appointment.id,
        date: appointment.date, // Keep as LocalDate
        hour: appointment.hour,
        time: appointment.time,
        reason: appointment.reason,
        status: appointment.status,
        doctor: appointment.doctor,
        patient: appointment.patient,
      })
    }
  }  // Fetch logged-in doctor's data
  useEffect(() => {
    const fetchDoctorData = async () => {
      try {
        // Get user info from localStorage using the correct key
        const userInfo = localStorage.getItem("pulmocare_user");
        
        if (!userInfo) {
          console.error("User info not found in localStorage");
          toast.error("You are not logged in. Redirecting to login page.");
          router.push('/doctor/login'); // Redirect to login page
          return;
        }
        
        // Parse the JSON user data
        const user = JSON.parse(userInfo);
        
        if (!user.id) {
          console.error("User ID not found in localStorage data");
          toast.error("Invalid user data. Please log in again.");
          router.push('/doctor/login');
          return;
        }
        
        // Use doctorApi to fetch the doctor's profile
        const doctorData = await doctorApi.getProfile(user.id);
        
        // Set doctor state with dynamically computed name
        setDoctor({
          ...doctorData,
          name: `${doctorData.firstName} ${doctorData.lastName}` // Compute name property dynamically
        });
      } catch (error) {
        console.error("Error fetching doctor data:", error);
        toast.error("Failed to load doctor data. Please log in again.");
        router.push('/doctor/login'); // Redirect to login page on error
      }
    };

    fetchDoctorData();
  }, [router])
  // Fetch appointments for the logged-in doctor
  useEffect(() => {
    const fetchAppointments = async () => {
      if (doctor?.id) {
        try {
          // Use doctorApi to fetch appointments for the logged-in doctor
          const appointments = await doctorApi.getAppointmentsByDoctorId(doctor.id);
          
          // Make sure we have valid appointments data
          if (!Array.isArray(appointments)) {
            console.error("Invalid appointments data:", appointments);
            toast.error("Received invalid appointments data from the server.");
            return;
          }
          
          // Split appointments into upcoming and past based on current date
          const today = new Date();
          today.setHours(0, 0, 0, 0);
          
          const upcoming = appointments.filter((appt: Appointment) => {
            const appointmentDate = new Date(appt.date);
            return appointmentDate >= today;
          });
          
          const past = appointments.filter((appt: Appointment) => {
            const appointmentDate = new Date(appt.date);
            return appointmentDate < today;
          });
          
          setUpcomingAppointments(upcoming);
          setPastAppointments(past);
          
          console.log(`Loaded ${upcoming.length} upcoming and ${past.length} past appointments for doctor ID: ${doctor.id}`);
        } catch (error) {
          console.error("Error fetching appointments:", error);
          toast.error("Failed to fetch appointments. Please try again later.");
        }
      }
    };

    fetchAppointments();
  }, [doctor?.id]);
  // Fetch all patients from the database
  useEffect(() => {
    const fetchPatients = async () => {
      try {
        // Use doctorApi to fetch all patients
        const response = await doctorApi.getAllPatients();
        
        // Make sure each patient has a name property for display in dropdown
        const patientsWithNames = response.map((patient: Patient) => ({
          ...patient,
          // Use name if it exists, otherwise create from firstName and lastName
          name: patient.name || `${patient.firstName} ${patient.lastName}`
        }));
        
        setPatients(patientsWithNames);
        console.log(`Loaded ${patientsWithNames.length} patients for appointment creation`);
      } catch (error) {
        console.error("Error fetching patients:", error);
        toast.error("Failed to load patient list. Please try again.");
      }
    };

    fetchPatients();
  }, [])
  return (
    <div className="space-y-6">
      
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Appointments</h1>
          <p className="text-muted-foreground">
            Manage clinic appointments for Dr. {doctor ? `${doctor.firstName} ${doctor.lastName}` : ''}
          </p>
        </div>

        {/* Create Appointment Dialog */}        <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>          <DialogTrigger asChild>
            <Button
              onClick={() => {
                setNewAppointment({
                  doctor: doctor, // Pre-select the logged-in doctor
                  patient: null,
                  date: new Date().toISOString().split('T')[0], // Simple date string format
                  time: "",
                  reason: "",
                  status: "Pending", // Default status (hidden from form)
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
          </DialogTrigger><DialogContent className="sm:max-w-[500px]">
            <DialogHeader>
              <DialogTitle>Create New Appointment</DialogTitle>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="doctor" className="flex items-center">
                  Doctor
                </Label>
                <div className="p-2 border rounded-md bg-gray-50">
                  {doctor ? `Dr. ${doctor.firstName} ${doctor.lastName}` : 'Loading doctor information...'}
                </div>
              </div>              <div className="space-y-2">
                <Label htmlFor="patient" className="flex items-center">
                  Patient <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={newAppointment.patient?.id}
                  onValueChange={(value) => {
                    const selectedPatient = patients.find((pat) => pat.id === value) || null
                    setNewAppointment({ ...newAppointment, patient: selectedPatient })
                    setFormErrors({ ...formErrors, patient: false })
                  }}
                >
                  <SelectTrigger className={formErrors.patient ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select patient" />
                  </SelectTrigger>
                  <SelectContent className="max-h-[200px] overflow-y-auto">
                    {patients.length > 0 ? (
                      patients.map((patient) => (
                        <SelectItem key={patient.id} value={patient.id}>
                          {patient.name || `${patient.firstName} ${patient.lastName}`}
                        </SelectItem>
                      ))
                    ) : (
                      <SelectItem value="loading" disabled>Loading patients...</SelectItem>
                    )}
                  </SelectContent>
                </Select>
                {formErrors.patient && <p className="text-red-500 text-sm">Patient is required</p>}
                {patients.length === 0 && <p className="text-sm text-amber-500">Loading patient list...</p>}
              </div><div className="space-y-2">
                <Label htmlFor="date" className="flex items-center">
                  Date <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="date"
                  type="date"
                  className={formErrors.date ? "border-red-500" : ""}
                  value={newAppointment.date ? new Date(newAppointment.date).toISOString().split('T')[0] : ""}
                  onChange={(e) => {
                    const selectedDate = e.target.value;
                    setNewAppointment({ ...newAppointment, date: selectedDate, time: "" });
                    setFormErrors({ ...formErrors, date: false });
                  }}
                  min={new Date().toISOString().split('T')[0]} // Disable past dates
                />
                {formErrors.date && <p className="text-red-500 text-sm">Date is required</p>}
              </div>              <div className="space-y-2">
                <Label htmlFor="time" className="flex items-center">
                  Time <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={newAppointment.time}
                  onValueChange={(value) => {
                    console.log("Time selected:", value);
                    setNewAppointment({ ...newAppointment, time: value });
                    setFormErrors({ ...formErrors, time: false });
                  }}
                  disabled={!newAppointment.doctor || !newAppointment.date}
                >
                  <SelectTrigger className={formErrors.time ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select time" />
                  </SelectTrigger>
                  <SelectContent className="max-h-[200px] overflow-y-auto" position="item-aligned">
                    {availableTimeSlots && availableTimeSlots.length > 0 ? (
                      availableTimeSlots.map((time: string) => (
                        <SelectItem key={time} value={time}>
                          {time}
                        </SelectItem>
                      ))
                    ) : (
                      timeSlots.map((time: string) => (
                        <SelectItem key={time} value={time}>
                          {time}
                        </SelectItem>
                      ))
                    )}
                  </SelectContent>
                </Select>
                {formErrors.time && <p className="text-red-500 text-sm">Time is required</p>}
                {newAppointment.doctor && newAppointment.date && availableTimeSlots.length === 0 && (
                  <p className="text-sm text-red-500">No available time slots for this doctor on the selected date.</p>
                )}
              </div>              <div className="space-y-2">
                <Label htmlFor="reason" className="flex items-center">
                  Reason <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={newAppointment.reason}
                  onValueChange={(value) => {
                    console.log("Reason selected:", value);
                    setNewAppointment({ ...newAppointment, reason: value });
                    setFormErrors({ ...formErrors, reason: false });
                  }}
                >
                  <SelectTrigger className={formErrors.reason ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select reason" />
                  </SelectTrigger>
                  <SelectContent className="max-h-[200px] overflow-y-auto" position="item-aligned">
                    {appointmentReasons.map((reason) => (
                      <SelectItem key={reason} value={reason}>
                        {reason}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {formErrors.reason && <p className="text-red-500 text-sm">Reason is required</p>}
              </div>{/* Status field removed as requested */}
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
        </TabsList>        <TabsContent value="upcoming" className="space-y-4 pt-4">
          {upcomingAppointments.map((appointment) => (
            <Card key={appointment.id}>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div className="grid gap-1">
                    <div className="font-semibold">
                      {appointment.patient.firstName} {appointment.patient.lastName}
                    </div>
                    <div className="text-sm text-muted-foreground">
                      <span className="font-medium">Reason:</span> {appointment.reason}
                    </div>
                    <div className="flex items-center gap-2 text-sm">
                      <span>
                        <span className="font-medium">Date:</span> {appointment.date}
                      </span>
                      <span>•</span>
                      <span>
                        <span className="font-medium">Time:</span> {appointment.hour}
                      </span>
                    </div>
                  </div>                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => router.push(`/doctor/appointments/${appointment.id}`)}
                    >
                      Edit
                    </Button>

                    <Link href={`/doctor/patients/${appointment.patient.id}`} className="w-full h-full">
                      <Button variant="outline" size="sm" className="flex items-center justify-center">
                        View Patient
                      </Button>
                    </Link>
                    
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button variant="destructive" size="sm">
                          Delete
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent className="max-w-[350px]">
                        <AlertDialogHeader>
                          <AlertDialogTitle>Delete Appointment</AlertDialogTitle>
                          <AlertDialogDescription className="text-sm">
                            Are you sure you want to delete this appointment?
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter className="space-x-2 pt-2">
                          <AlertDialogCancel className="h-8">Cancel</AlertDialogCancel>
                          <AlertDialogAction className="h-8 px-3" onClick={() => handleDeleteAppointment(appointment.id)}>
                            Delete
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
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
                    <div className="font-semibold">
                      {appointment.patient.firstName} {appointment.patient.lastName}
                    </div>
                    <div className="text-sm text-muted-foreground">
                      <span className="font-medium">Reason:</span> {appointment.reason}
                    </div>
                    <div className="flex items-center gap-2 text-sm">
                      <span>
                        <span className="font-medium">Date:</span> {appointment.date}
                      </span>
                      <span>•</span>
                      <span>
                        <span className="font-medium">Time:</span> {appointment.hour}
                      </span>
                    </div>
                  </div>                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => router.push(`/doctor/appointments/${appointment.id}`)}
                    >
                      Edit
                    </Button>

                    <Link href={`/doctor/patients/${appointment.patient.id}`}>
                      <Button variant="outline" size="sm">
                        View Patient
                      </Button>
                    </Link>
                    
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button variant="destructive" size="sm">
                          Delete
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent className="max-w-[350px]">
                        <AlertDialogHeader>
                          <AlertDialogTitle>Delete Appointment</AlertDialogTitle>
                          <AlertDialogDescription className="text-sm">
                            Are you sure you want to delete this appointment?
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter className="space-x-2 pt-2">
                          <AlertDialogCancel className="h-8">Cancel</AlertDialogCancel>
                          <AlertDialogAction className="h-8 px-3" onClick={() => handleDeleteAppointment(appointment.id)}>
                            Delete
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
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
