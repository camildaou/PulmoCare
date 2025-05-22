"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import Link from "next/link"
import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import { doctorApi } from "@/lib/api"
import { Appointment } from "@/lib/types"
import { toast } from "sonner"

export default function AppointmentDetailsPage() {
  const params = useParams()
  const router = useRouter()

  const appointmentId = params.id as string
  const [appointment, setAppointment] = useState<Appointment | null>(null)
  const [loading, setLoading] = useState(true)

  // Medical documentation states
  const [diagnosis, setDiagnosis] = useState("")
  const [prescription, setPrescription] = useState("")
  const [plan, setPlan] = useState("")
  const [confidentialNotes, setConfidentialNotes] = useState("")
  const [saved, setSaved] = useState(false)
  const [isPendingReport, setIsPendingReport] = useState(false)

  // Fetch appointment data
  useEffect(() => {
    const fetchAppointmentData = async () => {
      try {
        setLoading(true)
        // Get user info from localStorage
        const userInfo = localStorage.getItem("pulmocare_user")
        if (!userInfo) {
          toast.error("You are not logged in. Redirecting to login page.")
          router.push('/doctor/login')
          return
        }

        const user = JSON.parse(userInfo)
        
        // Fetch the doctor's appointments
        const appointments = await doctorApi.getAppointmentsByDoctorId(user.id)
        
        // Find the specific appointment by ID
        const foundAppointment = appointments.find((app: Appointment) => app.id.toString() === appointmentId)
        
        if (foundAppointment) {
          setAppointment(foundAppointment)
            // Initialize form fields with existing data if available
          setDiagnosis(foundAppointment.diagnosis || "")
          setPrescription(foundAppointment.prescriptions || "")
          setPlan(foundAppointment.plan || "")
          // Handle confidential notes - check both personalNotes and confidentialNotes to ensure compatibility
          setConfidentialNotes(foundAppointment.confidentialNotes || foundAppointment.personalNotes || "")
          
          // Check if this is a pending report (past appointment with no diagnosis)
          const appointmentDate = new Date(foundAppointment.date)
          const today = new Date()
          if (appointmentDate < today && !foundAppointment.diagnosis) {
            setIsPendingReport(true)
          }
        } else {
          toast.error("Appointment not found")
          router.push('/doctor/appointments')
        }
      } catch (error) {
        console.error("Error fetching appointment:", error)
        toast.error("Failed to load appointment data")
      } finally {
        setLoading(false)
      }
    }

    fetchAppointmentData()
  }, [appointmentId, router])
  const handleSave = async () => {
    if (!appointment) return
    
    try {      // Prepare updated appointment data
      const updatedAppointment = {
        ...appointment,
        diagnosis,
        prescriptions: prescription, // maps to 'prescription' in backend
        plan,
        confidentialNotes, // will be mapped to 'personalNotes' in the backend
        personalNotes: confidentialNotes, // For backward compatibility
      }
        // Save to backend API
      try {
        await doctorApi.updateAppointment(appointmentId, updatedAppointment);
      } catch (error) {
        console.error("Error saving to backend:", error);
        toast.error("Failed to save to server, saving locally only");
      }
      
      console.log("Saving appointment data:", updatedAppointment)
      
      // Also update in localStorage as a fallback
      const appointmentsString = localStorage.getItem("pulmocare_appointments")
      if (appointmentsString) {
        const appointmentsData = JSON.parse(appointmentsString)
        
        // Update in the appropriate list (upcoming or past)
        if (appointmentsData.upcoming) {
          appointmentsData.upcoming = appointmentsData.upcoming.map((app: Appointment) => 
            app.id.toString() === appointmentId ? updatedAppointment : app
          )
        }
        
        if (appointmentsData.past) {
          appointmentsData.past = appointmentsData.past.map((app: Appointment) => 
            app.id.toString() === appointmentId ? updatedAppointment : app
          )
        }
        
        // Save back to localStorage
        localStorage.setItem("pulmocare_appointments", JSON.stringify(appointmentsData))
      }

      // If this was a pending report, update the count
      if (isPendingReport) {
        // Get current pending reports count
        const pendingReportsData = localStorage.getItem("pulmocare_pending_reports")
        let pendingCount = pendingReportsData ? Number.parseInt(pendingReportsData) : 3

        // Decrease the count and save back to localStorage
        if (pendingCount > 0) {
          pendingCount--
          localStorage.setItem("pulmocare_pending_reports", pendingCount.toString())
        }

        setIsPendingReport(false)
      }

      toast.success("Appointment details saved successfully")
      setSaved(true)
      setTimeout(() => setSaved(false), 3000)
    } catch (error) {
      console.error("Error saving appointment:", error)
      toast.error("Failed to save appointment details")
    }
  }

  if (loading) {
    return <div className="flex items-center justify-center h-[60vh]">
      <p className="text-lg">Loading appointment details...</p>
    </div>
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Appointment Details</h1>
          <p className="text-muted-foreground">Manage appointment information and add diagnosis.</p>
        </div>
        <div className="flex gap-2">
          <Link href="/doctor/appointments">
            <Button variant="outline">Back to Appointments</Button>
          </Link>
          {appointment && (
            <Link href={`/doctor/patients/${appointment.patient.id}`}>
              <Button variant="outline">View Patient Info</Button>
            </Link>
          )}
        </div>
      </div>

      {appointment && (
        <div className="grid gap-6 md:grid-cols-[1fr_1fr]">
          <Card>
            <CardHeader>
              <CardTitle>Appointment Information</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <h3 className="text-sm font-medium text-muted-foreground">Patient</h3>
                    <p>{appointment.patient.firstName} {appointment.patient.lastName}</p>
                  </div>
                  <div>
                    <h3 className="text-sm font-medium text-muted-foreground">Date & Time</h3>
                    <p>{appointment.date} • {appointment.hour}</p>
                  </div>
                  <div>
                    <h3 className="text-sm font-medium text-muted-foreground">Reason</h3>
                    <p>{appointment.reason}</p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Patient Vitals</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div className="border rounded-lg p-3">
                  <div className="text-xs text-muted-foreground">Blood Pressure</div>
                  <div className="text-lg font-medium">
                    {appointment.patient.vitals?.bloodPressure || "Not recorded"} 
                    {appointment.patient.vitals?.bloodPressure ? " mmHg" : ""}
                  </div>
                </div>
                <div className="border rounded-lg p-3">
                  <div className="text-xs text-muted-foreground">Heart Rate</div>
                  <div className="text-lg font-medium">
                    {appointment.patient.vitals?.heartRate || "Not recorded"}
                    {appointment.patient.vitals?.heartRate ? " bpm" : ""}
                  </div>
                </div>
                <div className="border rounded-lg p-3">
                  <div className="text-xs text-muted-foreground">Respiratory Rate</div>
                  <div className="text-lg font-medium">
                    {appointment.patient.vitals?.respiratoryRate || "Not recorded"}
                    {appointment.patient.vitals?.respiratoryRate ? " rpm" : ""}
                  </div>
                </div>
                <div className="border rounded-lg p-3">
                  <div className="text-xs text-muted-foreground">Temperature</div>
                  <div className="text-lg font-medium">
                    {appointment.patient.vitals?.temperature || "Not recorded"}
                    {appointment.patient.vitals?.temperature ? " °C" : ""}
                  </div>
                </div>
                <div className="border rounded-lg p-3">
                  <div className="text-xs text-muted-foreground">Oxygen Saturation</div>
                  <div className="text-lg font-medium">
                    {appointment.patient.vitals?.oxygenSaturation || "Not recorded"}
                    {appointment.patient.vitals?.oxygenSaturation ? "%" : ""}
                  </div>
                </div>
                <div className="border rounded-lg p-3">
                  <div className="text-xs text-muted-foreground">Peak Flow</div>
                  <div className="text-lg font-medium">
                    {appointment.patient.vitals?.peakFlow || "Not recorded"}
                    {appointment.patient.vitals?.peakFlow ? " L/min" : ""}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Medical Documentation</CardTitle>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="diagnosis">            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="diagnosis">Diagnosis & Prescription</TabsTrigger>
              <TabsTrigger value="plan">Treatment Plan</TabsTrigger>
              <TabsTrigger value="confidential">Confidential Notes</TabsTrigger>
            </TabsList>

            <TabsContent value="diagnosis" className="space-y-4 pt-4">
              <div className="space-y-4">
                {saved && (
                  <div className="bg-green-50 text-green-600 p-3 rounded-md text-sm">
                    Saved successfully!
                  </div>
                )}

                {saved && isPendingReport && (
                  <div className="bg-green-50 text-green-600 p-3 rounded-md text-sm mb-4">
                    Pending report completed! The dashboard will be updated.
                  </div>
                )}

                <div className="space-y-2">
                  <Label htmlFor="diagnosis">Diagnosis</Label>
                  <Textarea
                    id="diagnosis"
                    placeholder="Enter diagnosis"
                    value={diagnosis}
                    onChange={(e) => setDiagnosis(e.target.value)}
                    rows={3}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="prescription">Prescription</Label>
                  <Textarea
                    id="prescription"
                    placeholder="Enter prescription details"
                    value={prescription}
                    onChange={(e) => setPrescription(e.target.value)}
                    rows={3}
                  />
                </div>
              </div>
            </TabsContent>            <TabsContent value="plan" className="space-y-4 pt-4">
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="plan">Treatment Plan</Label>
                  <Textarea
                    id="plan"
                    placeholder="Enter treatment plan and follow-up recommendations"
                    value={plan}
                    onChange={(e) => setPlan(e.target.value)}
                    rows={4}
                  />
                </div>
              </div>
            </TabsContent>

            <TabsContent value="confidential" className="space-y-4 pt-4">
              <div className="space-y-4">
                <div className="bg-amber-50 text-amber-600 p-3 rounded-md text-sm">
                  These notes are confidential and will not be shared with the patient.
                </div>

                <div className="space-y-2">
                  <Label htmlFor="confidentialNotes">Confidential Notes</Label>
                  <Textarea
                    id="confidentialNotes"
                    placeholder="Enter confidential notes (not visible to patient)"
                    value={confidentialNotes}
                    onChange={(e) => setConfidentialNotes(e.target.value)}
                    rows={6}
                  />
                </div>
              </div>
            </TabsContent>
          </Tabs>

          <div className="mt-6">
            <Button className="w-full" onClick={handleSave}>
              Save to Patient Record
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
