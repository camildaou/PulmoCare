"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import Link from "next/link"
import { useState, useEffect } from "react"
import { useParams } from "next/navigation"

export default function AppointmentDetailsPage() {
  const params = useParams()

  const appointmentId = params.id
  const [diagnosis, setDiagnosis] = useState("")
  const [prescription, setPrescription] = useState("")
  const [notes, setNotes] = useState("")
  const [assessment, setAssessment] = useState("")
  const [plan, setPlan] = useState("")
  const [confidentialNotes, setConfidentialNotes] = useState("")
  const [saved, setSaved] = useState(false)
  const [isPendingReport, setIsPendingReport] = useState(false)

  useEffect(() => {
    // Check if this appointment has a pending report
    const pastAppointments = [
      {
        id: "6",
        date: "Mar 25, 2025",
        hasPendingReport: true,
      },
      {
        id: "7",
        date: "Mar 24, 2025",
        hasPendingReport: false,
      },
      {
        id: "8",
        date: "Mar 22, 2025",
        hasPendingReport: true,
      },
    ]

    const appointment = pastAppointments.find((app) => app.id === appointmentId)
    if (appointment && appointment.hasPendingReport) {
      setIsPendingReport(true)
    }
  }, [appointmentId])

  const handleSave = () => {
    // In a real app, you would save this data to your backend
    console.log({
      appointmentId,
      diagnosis,
      prescription,
      notes,
      assessment,
      plan,
      confidentialNotes,
    })

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

      // Mark this report as no longer pending
      setIsPendingReport(false)
    }

    setSaved(true)
    setTimeout(() => setSaved(false), 3000)
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
          <Link href={`/doctor/patients/${appointmentId}`}>
            <Button variant="outline">View Patient Info</Button>
          </Link>
        </div>
      </div>

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
                  <p>Alice Johnson</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Date & Time</h3>
                  <p>March 28, 2025 • 09:00 AM</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Reason</h3>
                  <p>Follow-up</p>
                </div>
                <div>
                  <h3 className="text-sm font-medium text-muted-foreground">Status</h3>
                  <p className="text-green-500">Confirmed</p>
                </div>
              </div>

              <div>
                <h3 className="text-sm font-medium text-muted-foreground">Previous Diagnosis</h3>
                <p className="text-sm mt-1">Mild asthma exacerbation due to seasonal allergies (Feb 10, 2025)</p>
              </div>

              <div>
                <h3 className="text-sm font-medium text-muted-foreground">Previous Prescription</h3>
                <ul className="text-sm mt-1 list-disc pl-5 space-y-1">
                  <li>Albuterol inhaler, 2 puffs every 4-6 hours as needed</li>
                  <li>Fluticasone nasal spray, 1 spray in each nostril daily</li>
                </ul>
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
                <div className="text-lg font-medium">120/80 mmHg</div>
              </div>
              <div className="border rounded-lg p-3">
                <div className="text-xs text-muted-foreground">Heart Rate</div>
                <div className="text-lg font-medium">72 bpm</div>
              </div>
              <div className="border rounded-lg p-3">
                <div className="text-xs text-muted-foreground">Respiratory Rate</div>
                <div className="text-lg font-medium">16 rpm</div>
              </div>
              <div className="border rounded-lg p-3">
                <div className="text-xs text-muted-foreground">Temperature</div>
                <div className="text-lg font-medium">98.6 °F</div>
              </div>
              <div className="border rounded-lg p-3">
                <div className="text-xs text-muted-foreground">Oxygen Saturation</div>
                <div className="text-lg font-medium">98%</div>
              </div>
              <div className="border rounded-lg p-3">
                <div className="text-xs text-muted-foreground">Peak Flow</div>
                <div className="text-lg font-medium">450 L/min</div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Medical Documentation</CardTitle>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="diagnosis">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="diagnosis">Diagnosis & Prescription</TabsTrigger>
              <TabsTrigger value="assessment">Assessment & Plan</TabsTrigger>
              <TabsTrigger value="confidential">Confidential Notes</TabsTrigger>
            </TabsList>

            <TabsContent value="diagnosis" className="space-y-4 pt-4">
              <div className="space-y-4">
                {saved && (
                  <div className="bg-green-50 text-green-600 p-3 rounded-md text-sm">
                    Diagnosis and prescription saved successfully!
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

                <div className="space-y-2">
                  <Label htmlFor="notes">Additional Notes</Label>
                  <Textarea
                    id="notes"
                    placeholder="Enter additional notes"
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    rows={3}
                  />
                </div>
              </div>
            </TabsContent>

            <TabsContent value="assessment" className="space-y-4 pt-4">
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="assessment">Assessment</Label>
                  <Textarea
                    id="assessment"
                    placeholder="Enter your assessment of the patient's condition"
                    value={assessment}
                    onChange={(e) => setAssessment(e.target.value)}
                    rows={4}
                  />
                </div>

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
