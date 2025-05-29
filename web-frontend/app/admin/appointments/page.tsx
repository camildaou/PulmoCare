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
import { Calendar } from "@/components/ui/calendar"

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

// SSR-safe default date for new appointments
const getTodayString = () => {
  const now = new Date();
  return now.toISOString().split('T')[0];
};

export default function AdminAppointmentsPage() {
  const router = useRouter()

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
  const [currentEditId, setCurrentEditId] = useState<string | null>(null)
  
  // State for available time slots
  const [availableTimeSlots, setAvailableTimeSlots] = useState<string[]>(timeSlots)
  
  // State for form validation
  const [formErrors, setFormErrors] = useState({
    doctor: false,
    patient: false,
    date: false,
    time: false,
    reason: false,
  })

  // State for patients and doctors
  const [patients, setPatients] = useState<Patient[]>([])
  const [doctors, setDoctors] = useState<Doctor[]>([])

  // State for the admin user
  const [admin, setAdmin] = useState<any>(null)

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

      // Get the doctor ID and patient ID
      const doctorId = newAppointment.doctor?.id || '';
      const patientId = newAppointment.patient?.id || '';
      
      // Prepare the payload for the API
      const payload = {
        doctor: { id: doctorId },
        patient: { id: patientId },
        date: formattedDate,
        hour: newAppointment.time, // hour is used in backend
        reason: newAppointment.reason,
        status: newAppointment.status,
        upcoming: true,
      };

      // Call the API to create the appointment in the backend
      await import('@/lib/api').then(async ({ createAppointment }) => {
        await createAppointment(
          doctorId,
          patientId,
          formattedDate,
          newAppointment.time,
          newAppointment.reason
        );
      });

      // Optionally, refresh appointments list
      fetchAllAppointments();

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
    } catch (error) {
      console.error("Error creating appointment:", error);
      toast.error("Failed to create appointment. Please try again.");
    }
  }

  // Function to handle editing an appointment (now updates the database too)
  const handleEditAppointment = async () => {
    if (!editingAppointment) return;

    // Validate form
    if (!validateAppointmentForm(editingAppointment)) {
      toast.error("Please fill in all required fields");
      return;
    }

    // Prepare the payload for the API
    const payload = {
      ...editingAppointment,
      doctor: editingAppointment.doctor,
      patient: editingAppointment.patient,
      date: editingAppointment.date,
      hour: editingAppointment.time, // hour is used in backend
      time: editingAppointment.time, // keep for local state
      reason: editingAppointment.reason,
      status: editingAppointment.status,
    };

    try {
      // Update in the backend using the correct API
      await doctorApi.updateAppointment(editingAppointment.id, payload);
      toast.success("Appointment updated successfully");
      // Refresh appointments list from backend
      fetchAllAppointments();
    } catch (error) {
      toast.error("Failed to update appointment in database");
      console.error("Error updating appointment:", error);
    }

    // Reset editing state
    setEditingAppointment(null);
    setEditDialogOpen(false);
    setCurrentEditId(null);
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
        
        // Let the user know which time slot was restored
        if (appointmentToDelete.date && appointmentToDelete.hour) {
          // Format the date to be more readable
          const date = new Date(appointmentToDelete.date);
          const dayName = date.toLocaleDateString('en-US', { weekday: 'long' }).substring(0, 3).toLowerCase();
          
          toast.success(`Appointment deleted and time slot ${appointmentToDelete.hour} on ${dayName} restored to availability`, {
            duration: 4000
          });
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
  }

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

  // Helper to get available slots for a doctor on a date from backend
  const fetchDoctorAvailableSlots = async (doctorId: string, date: string) => {
    if (!doctorId || !date) return [];
    try {
      const availability = await scheduleApi.getDoctorAvailability(doctorId);
      // Find the day name (e.g., 'mon', 'tue', ...)
      const jsDate = new Date(date);
      const dayIndex = jsDate.getDay(); // 0=Sun, 1=Mon, ...
      const dayNameMap = ['sun','mon','tue','wed','thu','fri','sat'];
      const dayKey = dayNameMap[dayIndex];
      const slots = availability?.availableTimeSlots?.[dayKey] || [];
      // Return only the start times as strings (e.g., '09:00', '09:30', ...)
      return slots.map((slot: { startTime: string }) => slot.startTime);
    } catch (e) {
      return [];
    }
  };

  // Update available time slots when doctor or date changes in create form
  useEffect(() => {
    const updateSlots = async () => {
      if (newAppointment.doctor?.id && newAppointment.date) {
        const slots = await fetchDoctorAvailableSlots(newAppointment.doctor.id, newAppointment.date);
        setAvailableTimeSlots(slots);
      } else {
        setAvailableTimeSlots([]);
      }
    };
    updateSlots();
  }, [newAppointment.doctor?.id, newAppointment.date])

  // Update available time slots when doctor or date changes in edit form
  useEffect(() => {
    const updateSlots = async () => {
      if (editingAppointment?.doctor?.id && editingAppointment.date) {
        const slots = await fetchDoctorAvailableSlots(editingAppointment.doctor.id, editingAppointment.date);
        setAvailableTimeSlots(slots);
      } else {
        setAvailableTimeSlots([]);
      }
    };
    updateSlots();
  }, [editingAppointment?.doctor?.id, editingAppointment?.date])

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
  }

  // Fetch all appointments for all doctors
  const fetchAllAppointments = async () => {
    try {
      // Get all doctors first
      const allDoctors = await doctorApi.getAllDoctors();
      
      // Create arrays to store appointments
      let upcoming: Appointment[] = [];
      let past: Appointment[] = [];
      
      // Get today's date for comparison
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      // For each doctor, fetch their appointments
      for (const doctor of allDoctors) {
        const doctorAppointments = await doctorApi.getAppointmentsByDoctorId(doctor.id);
        
        // Split appointments into upcoming and past
        if (Array.isArray(doctorAppointments)) {
          doctorAppointments.forEach((appt: Appointment) => {
            const appointmentDate = new Date(appt.date);
            if (appointmentDate >= today) {
              upcoming.push(appt);
            } else {
              past.push(appt);
            }
          });
        }
      }
      
      // Update state with all appointments
      setUpcomingAppointments(upcoming);
      setPastAppointments(past);
      
      console.log(`Loaded ${upcoming.length} upcoming and ${past.length} past appointments for all doctors`);
    } catch (error) {
      console.error("Error fetching all appointments:", error);
      toast.error("Failed to fetch appointments. Please try again later.");
    }
  };

  // Fetch all doctors
  useEffect(() => {
    const fetchDoctors = async () => {
      try {
        const doctorsData = await doctorApi.getAllDoctors();
        // Make sure each doctor has a name property
        const doctorsWithNames = doctorsData.map((doctor: any) => ({
          ...doctor,
          name: doctor.name || `${doctor.firstName} ${doctor.lastName}`
        }));
        setDoctors(doctorsWithNames);
        console.log(`Loaded ${doctorsWithNames.length} doctors for appointment creation`);
      } catch (error) {
        console.error("Error fetching doctors:", error);
        toast.error("Failed to load doctor list. Please try again.");
      }
    };

    fetchDoctors();
  }, []);

  // Fetch all patients
  useEffect(() => {
    const fetchPatients = async () => {
      try {
        const patientsData = await adminApi.getAllPatients();
        // Make sure each patient has a name property
        const patientsWithNames = patientsData.map((patient: any) => ({
          ...patient,
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
  }, []);

  // Fetch admin profile and all appointments on mount
  useEffect(() => {
    const fetchAdminData = async () => {
      try {
        // Get user info from localStorage
        const userInfo = localStorage.getItem("pulmocare_user");
        
        if (!userInfo) {
          console.error("User info not found in localStorage");
          toast.error("You are not logged in. Redirecting to login page.");
          router.push('/');
          return;
        }
        
        // Parse the JSON user data
        const user = JSON.parse(userInfo);
        
        if (!user.id) {
          console.error("User ID not found in localStorage data");
          toast.error("Invalid user data. Please log in again.");
          router.push('/');
          return;
        }
        
        // Use adminApi to fetch the admin's profile
        const adminData = await adminApi.getProfile(user.id);
        setAdmin(adminData);
        
        // Fetch all appointments
        await fetchAllAppointments();
      } catch (error) {
        console.error("Error fetching admin data:", error);
        toast.error("Failed to load admin data. Please log in again.");
        router.push('/');
      }
    };

    fetchAdminData();
  }, [router]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Appointments</h1>
          <p className="text-muted-foreground">Manage clinic appointments for all doctors</p>
        </div>

        {/* Create Appointment Dialog */}
        <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button
              onClick={() => {
                setNewAppointment({
                  doctor: null,
                  patient: null,
                  date: getTodayString(), // Simple date string format
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
          </DialogTrigger>
          <DialogContent className="sm:max-w-[500px]">
            <DialogHeader className="pb-0 py-0 space-y-0 mb-1">
              <DialogTitle className="text-base leading-none">Create New Appointment</DialogTitle>
            </DialogHeader>
            <div className="grid gap-2 pt-0">
              <div className="space-y-1">
                <Label htmlFor="doctor" className="flex items-center">
                  Doctor <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={newAppointment.doctor?.id}
                  onValueChange={(value) => {
                    const selectedDoctor = doctors.find((doc) => doc.id === value) || null
                    setNewAppointment({ ...newAppointment, doctor: selectedDoctor, time: "" })
                    setFormErrors({ ...formErrors, doctor: false })
                  }}
                >
                  <SelectTrigger className={formErrors.doctor ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select doctor" />
                  </SelectTrigger>
                  <SelectContent className="max-h-[200px] overflow-y-auto" position="item-aligned">
                    {doctors.length > 0 ? (
                      doctors.map((doctor) => (
                        <SelectItem key={doctor.id} value={doctor.id}>
                          Dr. {doctor.firstName} {doctor.lastName}
                        </SelectItem>
                      ))
                    ) : (
                      <SelectItem value="loading" disabled>Loading doctors...</SelectItem>
                    )}
                  </SelectContent>
                </Select>
                {formErrors.doctor && <p className="text-red-500 text-sm">Doctor is required</p>}
                {doctors.length === 0 && <p className="text-sm text-amber-500">Loading doctor list...</p>}
              </div>
              
              <div className="space-y-1">
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
                  <SelectContent className="max-h-[200px] overflow-y-auto" position="item-aligned">
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
              </div>

              <div className="space-y-1">
                <Label htmlFor="date" className="flex items-center">
                  Date <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="date"
                  type="date"
                  className={formErrors.date ? "border-red-500" : ""}
                  value={typeof newAppointment.date === 'string' && newAppointment.date ? newAppointment.date : ''}
                  onChange={(e) => {
                    const selectedDate = e.target.value;
                    setNewAppointment({ ...newAppointment, date: selectedDate, time: "" });
                    setFormErrors({ ...formErrors, date: false });
                  }}
                  min={getTodayString()} // Disable past dates
                />
                {formErrors.date && <p className="text-red-500 text-sm">Date is required</p>}
              </div>
              
              <div className="space-y-1">
                <Label htmlFor="time" className="flex items-center">
                  Time <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={newAppointment.time}
                  onValueChange={(value) => {
                    setNewAppointment({ ...newAppointment, time: value });
                    setFormErrors({ ...formErrors, time: false });
                  }}
                  disabled={!newAppointment.doctor || !newAppointment.date || availableTimeSlots.length === 0}
                >
                  <SelectTrigger className={formErrors.time ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select time" />
                  </SelectTrigger>
                  <SelectContent className="max-h-[200px] overflow-y-auto" position="item-aligned">
                    {availableTimeSlots.length > 0 &&
                      availableTimeSlots.map((time: string) => (
                        <SelectItem key={time} value={time}>
                          {time}
                        </SelectItem>
                      ))}
                  </SelectContent>
                </Select>
                {formErrors.time && <p className="text-red-500 text-sm">Time is required</p>}
                {newAppointment.doctor && newAppointment.date && availableTimeSlots.length === 0 && (
                  <p className="text-sm text-red-500">No available time slots for this doctor on the selected date.</p>
                )}
              </div>
              
              <div className="space-y-1">
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
              </div>
            </div>
            <DialogFooter className="mt-2">
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
                    <div className="font-semibold">
                      {appointment.patient.firstName} {appointment.patient.lastName}
                    </div>
                    <div className="text-sm text-muted-foreground">
                      <span className="font-medium">Reason:</span> {appointment.reason}
                    </div>
                    <div className="text-sm">
                      <span className="font-medium">Doctor:</span> Dr. {(() => {
                        // Try to use firstName/lastName if present, else fallback to name
                        const doc = appointment.doctor as any;
                        if (doc.firstName && doc.lastName) return `${doc.firstName} ${doc.lastName}`;
                        if (doc.name) return doc.name;
                        return '';
                      })()}
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
                  </div>
                  
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        setEditDialogOpen(true)
                        initializeEditingAppointment(appointment)
                      }}
                    >
                      Edit
                    </Button>

                    <Link href={`/admin/patients/${appointment.patient.id}`} className="w-full h-full">
                      <Button variant="outline" size="sm" className="flex items-center justify-center">
                        View Patient
                      </Button>
                    </Link>
                    
                    <Button 
                      variant="destructive" 
                      size="sm"
                      onClick={() => {
                        if (window.confirm("Are you sure you want to delete this appointment?")) {
                          handleDeleteAppointment(appointment.id);
                        }
                      }}
                    >
                      Delete
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
          {upcomingAppointments.length === 0 && (
            <div className="text-center p-8 border rounded-lg bg-gray-50">
              <p className="text-muted-foreground">No upcoming appointments found.</p>
            </div>
          )}
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
                    <div className="text-sm">
                      <span className="font-medium">Doctor:</span> Dr. {(() => {
                        // Try to use firstName/lastName if present, else fallback to name
                        const doc = appointment.doctor as any;
                        if (doc.firstName && doc.lastName) return `${doc.firstName} ${doc.lastName}`;
                        if (doc.name) return doc.name;
                        return '';
                      })()}
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
                  </div>
                  
                  <div className="flex gap-2">
                    <Link href={`/admin/patients/${appointment.patient.id}`} className="w-full h-full">
                      <Button variant="outline" size="sm" className="flex items-center justify-center">
                        View Patient
                      </Button>
                    </Link>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
          {pastAppointments.length === 0 && (
            <div className="text-center p-8 border rounded-lg bg-gray-50">
              <p className="text-muted-foreground">No past appointments found.</p>
            </div>
          )}
        </TabsContent>
      </Tabs>

      {/* Edit Appointment Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Edit Appointment</DialogTitle>
          </DialogHeader>
          {editingAppointment && (
            <div className="grid gap-2 pt-0">
              <div className="space-y-1">
                <Label htmlFor="doctor" className="flex items-center">
                  Doctor <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={editingAppointment.doctor?.id}
                  onValueChange={(value) => {
                    const selectedDoctor = doctors.find((doc) => doc.id === value)
                    if (selectedDoctor) {
                      setEditingAppointment({ ...editingAppointment, doctor: selectedDoctor, time: "" })
                      setFormErrors({ ...formErrors, doctor: false })
                    }
                  }}
                >
                  <SelectTrigger className={formErrors.doctor ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select doctor" />
                  </SelectTrigger>
                  <SelectContent className="max-h-[200px] overflow-y-auto" position="item-aligned">
                    {doctors.length > 0 ? (
                      doctors.map((doctor) => (
                        <SelectItem key={doctor.id} value={doctor.id}>
                          Dr. {doctor.firstName} {doctor.lastName}
                        </SelectItem>
                      ))
                    ) : (
                      <SelectItem value="loading" disabled>Loading doctors...</SelectItem>
                    )}
                  </SelectContent>
                </Select>
                {formErrors.doctor && <p className="text-red-500 text-sm">Doctor is required</p>}
              </div>
              <div className="space-y-1">
                <Label htmlFor="patient" className="flex items-center">
                  Patient <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={editingAppointment.patient?.id}
                  onValueChange={(value) => {
                    const selectedPatient = patients.find((pat) => pat.id === value)
                    if (selectedPatient) {
                      setEditingAppointment({ ...editingAppointment, patient: selectedPatient })
                      setFormErrors({ ...formErrors, patient: false })
                    }
                  }}
                >
                  <SelectTrigger className={formErrors.patient ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select patient" />
                  </SelectTrigger>
                  <SelectContent className="max-h-[200px] overflow-y-auto" position="item-aligned">
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
              </div>
              <div className="space-y-1">
                <Label htmlFor="date" className="flex items-center">
                  Date <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="date"
                  type="date"
                  className={formErrors.date ? "border-red-500" : ""}
                  value={typeof editingAppointment.date === 'string' && editingAppointment.date ? editingAppointment.date : ''}
                  onChange={(e) => {
                    const selectedDate = e.target.value;
                    setEditingAppointment({ ...editingAppointment, date: selectedDate, time: "" });
                    setFormErrors({ ...formErrors, date: false });
                  }}
                  min={getTodayString()}
                />
                {formErrors.date && <p className="text-red-500 text-sm">Date is required</p>}
              </div>
              <div className="space-y-1">
                <Label htmlFor="time" className="flex items-center">
                  Time <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={editingAppointment.time}
                  onValueChange={(value) => {
                    setEditingAppointment({ ...editingAppointment, time: value });
                    setFormErrors({ ...formErrors, time: false });
                  }}
                  disabled={!editingAppointment.doctor || !editingAppointment.date || availableTimeSlots.length === 0}
                >
                  <SelectTrigger className={formErrors.time ? "border-red-500" : ""}>
                    <SelectValue placeholder="Select time" />
                  </SelectTrigger>
                  <SelectContent className="max-h-[200px] overflow-y-auto" position="item-aligned">
                    {availableTimeSlots.length > 0 &&
                      availableTimeSlots.map((time: string) => (
                        <SelectItem key={time} value={time}>
                          {time}
                        </SelectItem>
                      ))}
                  </SelectContent>
                </Select>
                {formErrors.time && <p className="text-red-500 text-sm">Time is required</p>}
                {editingAppointment.doctor && editingAppointment.date && availableTimeSlots.length === 0 && (
                  <p className="text-sm text-red-500">No available time slots for this doctor on the selected date.</p>
                )}
              </div>
              <div className="space-y-1">
                <Label htmlFor="reason" className="flex items-center">
                  Reason <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={editingAppointment.reason}
                  onValueChange={(value) => {
                    setEditingAppointment({ ...editingAppointment, reason: value });
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
              </div>
            </div>
          )}
          <DialogFooter className="mt-2">
            <DialogClose asChild>
              <Button variant="outline">Cancel</Button>
            </DialogClose>
            <Button onClick={handleEditAppointment}>Save Changes</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
