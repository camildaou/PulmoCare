"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import Link from "next/link"
import { useState } from "react"
import { toast } from "sonner"

export default function PatientDetailsPage({ params }: { params: { id: string } }) {
  // In a real app, you would fetch patient data based on the ID
  const patientId = params.id

  const [patient, setPatient] = useState({
    id: patientId,
    name: "Alice Johnson",
    age: "42",
    gender: "Female",
    email: "alice.johnson@example.com",
    phone: "555-123-4567",
    height: "5'6\" (168 cm)",
    weight: "145 lbs (66 kg)",
    location: "New York, NY",
    maritalStatus: "Married",
    occupation: "Teacher",
    pets: "1 dog",
    smoking: "Non-smoker",
    address: "123 Main St, New York, NY",
  })

  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [editedPatient, setEditedPatient] = useState({ ...patient })
  const [formErrors, setFormErrors] = useState({
    name: false,
    age: false,
    gender: false,
    email: false,
    phone: false,
  })

  // Validate the patient form
  const validatePatientForm = () => {
    const errors = {
      name: !editedPatient.name,
      age: !editedPatient.age || isNaN(Number(editedPatient.age)),
      gender: !editedPatient.gender,
      email: !editedPatient.email,
      phone: !editedPatient.phone,
    }

    setFormErrors(errors)
    return !Object.values(errors).some((error) => error)
  }

  // Handle saving edited patient
  const handleSavePatient = () => {
    if (!validatePatientForm()) {
      toast.error("Please fill in all required fields correctly")
      return
    }

    // Update patient data
    setPatient(editedPatient)
    setEditDialogOpen(false)
    toast.success("Patient information updated successfully")
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Patient Details</h1>
          <p className="text-muted-foreground">View and manage patient information.</p>
        </div>
        <div className="flex gap-2">
          <Link href="/admin/patients">
            <Button variant="outline">Back to Patients</Button>
          </Link>
          <Button
            onClick={() => {
              setEditedPatient({ ...patient })
              setFormErrors({
                name: false,
                age: false,
                gender: false,
                email: false,
                phone: false,
              })
              setEditDialogOpen(true)
            }}
          >
            Edit Patient
          </Button>
        </div>
      </div>

      {/* Edit Patient Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Edit Patient Information</DialogTitle>
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
                  value={editedPatient.name}
                  onChange={(e) => {
                    setEditedPatient({ ...editedPatient, name: e.target.value })
                    setFormErrors({ ...formErrors, name: false })
                  }}
                  className={formErrors.name ? "border-red-500" : ""}
                />
                {formErrors.name && <p className="text-red-500 text-sm">Name is required</p>}
              </div>
              <div className="space-y-2">
                <Label htmlFor="age" className="flex items-center">
                  Age <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="age"
                  type="number"
                  placeholder="Enter age"
                  value={editedPatient.age}
                  onChange={(e) => {
                    setEditedPatient({ ...editedPatient, age: e.target.value })
                    setFormErrors({ ...formErrors, age: false })
                  }}
                  className={formErrors.age ? "border-red-500" : ""}
                />
                {formErrors.age && <p className="text-red-500 text-sm">Valid age is required</p>}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="gender" className="flex items-center">
                Gender <span className="text-red-500 ml-1">*</span>
              </Label>
              <Select
                value={editedPatient.gender}
                onValueChange={(value) => {
                  setEditedPatient({ ...editedPatient, gender: value })
                  setFormErrors({ ...formErrors, gender: false })
                }}
              >
                <SelectTrigger className={formErrors.gender ? "border-red-500" : ""}>
                  <SelectValue placeholder="Select gender" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Male">Male</SelectItem>
                  <SelectItem value="Female">Female</SelectItem>
                  <SelectItem value="Other">Other</SelectItem>
                </SelectContent>
              </Select>
              {formErrors.gender && <p className="text-red-500 text-sm">Gender is required</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="phone" className="flex items-center">
                  Phone Number <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="phone"
                  placeholder="Enter phone number"
                  value={editedPatient.phone}
                  onChange={(e) => {
                    setEditedPatient({ ...editedPatient, phone: e.target.value })
                    setFormErrors({ ...formErrors, phone: false })
                  }}
                  className={formErrors.phone ? "border-red-500" : ""}
                />
                {formErrors.phone && <p className="text-red-500 text-sm">Phone number is required</p>}
              </div>
              <div className="space-y-2">
                <Label htmlFor="email" className="flex items-center">
                  Email <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="Enter email address"
                  value={editedPatient.email}
                  onChange={(e) => {
                    setEditedPatient({ ...editedPatient, email: e.target.value })
                    setFormErrors({ ...formErrors, email: false })
                  }}
                  className={formErrors.email ? "border-red-500" : ""}
                />
                {formErrors.email && <p className="text-red-500 text-sm">Email is required</p>}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="address">Address</Label>
              <Input
                id="address"
                placeholder="Enter address"
                value={editedPatient.address}
                onChange={(e) => setEditedPatient({ ...editedPatient, address: e.target.value })}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="height">Height</Label>
                <Input
                  id="height"
                  placeholder="Enter height"
                  value={editedPatient.height}
                  onChange={(e) => setEditedPatient({ ...editedPatient, height: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="weight">Weight</Label>
                <Input
                  id="weight"
                  placeholder="Enter weight"
                  value={editedPatient.weight}
                  onChange={(e) => setEditedPatient({ ...editedPatient, weight: e.target.value })}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <Input
                id="location"
                placeholder="Enter location"
                value={editedPatient.location}
                onChange={(e) => setEditedPatient({ ...editedPatient, location: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="maritalStatus">Marital Status</Label>
              <Select
                value={editedPatient.maritalStatus}
                onValueChange={(value) => setEditedPatient({ ...editedPatient, maritalStatus: value })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select marital status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Single">Single</SelectItem>
                  <SelectItem value="Married">Married</SelectItem>
                  <SelectItem value="Divorced">Divorced</SelectItem>
                  <SelectItem value="Widowed">Widowed</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="occupation">Occupation</Label>
              <Input
                id="occupation"
                placeholder="Enter occupation"
                value={editedPatient.occupation}
                onChange={(e) => setEditedPatient({ ...editedPatient, occupation: e.target.value })}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="pets">Pets at Home</Label>
                <Input
                  id="pets"
                  placeholder="Enter pets information"
                  value={editedPatient.pets}
                  onChange={(e) => setEditedPatient({ ...editedPatient, pets: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="smoking">Smoking Status</Label>
                <Select
                  value={editedPatient.smoking}
                  onValueChange={(value) => setEditedPatient({ ...editedPatient, smoking: value })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select smoking status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Non-smoker">Non-smoker</SelectItem>
                    <SelectItem value="Former smoker">Former smoker</SelectItem>
                    <SelectItem value="Current smoker">Current smoker</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">Cancel</Button>
            </DialogClose>
            <Button onClick={handleSavePatient}>Save Changes</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <div className="grid gap-6 md:grid-cols-[300px_1fr]">
        <Card>
          <CardContent className="p-6 flex flex-col items-center gap-4">
            <div className="relative w-32 h-32 rounded-full overflow-hidden border-4 border-muted">
              <div
                className="w-full h-full bg-center bg-cover"
                style={{ backgroundImage: `url('/placeholder.svg?height=128&width=128')` }}
                aria-label="Patient profile"
              />
            </div>
            <div className="text-center">
              <h2 className="text-xl font-bold">{patient.name}</h2>
              <p className="text-sm text-muted-foreground">Patient ID: {patientId}</p>
            </div>
            <div className="w-full space-y-4 pt-4">
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="text-muted-foreground">Gender:</div>
                <div>{patient.gender}</div>
                <div className="text-muted-foreground">Age:</div>
                <div>{patient.age}</div>
                <div className="text-muted-foreground">Email:</div>
                <div className="truncate">{patient.email}</div>
                <div className="text-muted-foreground">Height:</div>
                <div>{patient.height}</div>
                <div className="text-muted-foreground">Weight:</div>
                <div>{patient.weight}</div>
                <div className="text-muted-foreground">Location:</div>
                <div>{patient.location}</div>
                <div className="text-muted-foreground">Marital Status:</div>
                <div>{patient.maritalStatus}</div>
                <div className="text-muted-foreground">Occupation:</div>
                <div>{patient.occupation}</div>
                <div className="text-muted-foreground">Pets:</div>
                <div>{patient.pets}</div>
                <div className="text-muted-foreground">Smoking:</div>
                <div>{patient.smoking}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-6">
          <Tabs defaultValue="prescriptions">
            <TabsList className="grid grid-cols-6 w-full">
              <TabsTrigger value="prescriptions">Prescriptions</TabsTrigger>
              <TabsTrigger value="symptoms">Symptoms</TabsTrigger>
              <TabsTrigger value="reports">Reports</TabsTrigger>
              <TabsTrigger value="bloodtests">Blood Tests</TabsTrigger>
              <TabsTrigger value="xrays">X-Rays</TabsTrigger>
              <TabsTrigger value="vitals">Vitals</TabsTrigger>
            </TabsList>

            <TabsContent value="prescriptions" className="space-y-4 pt-4">
              <Card>
                <CardHeader>
                  <CardTitle>Prescriptions & Diagnosis</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="border rounded-lg p-4">
                      <div className="flex justify-between items-start">
                        <div>
                          <h3 className="font-medium">Follow-up Appointment</h3>
                          <p className="text-sm text-muted-foreground">Mar 25, 2025</p>
                        </div>
                        <div className="text-sm font-medium text-primary">Dr. John Doe</div>
                      </div>
                      <div className="mt-2 pt-2 border-t">
                        <h4 className="text-sm font-medium">Diagnosis:</h4>
                        <p className="text-sm">Mild asthma exacerbation due to seasonal allergies</p>
                        <h4 className="text-sm font-medium mt-2">Prescription:</h4>
                        <p className="text-sm">Albuterol inhaler, 2 puffs every 4-6 hours as needed</p>
                        <p className="text-sm">Fluticasone nasal spray, 1 spray in each nostril daily</p>
                      </div>
                    </div>

                    <div className="border rounded-lg p-4">
                      <div className="flex justify-between items-start">
                        <div>
                          <h3 className="font-medium">Initial Consultation</h3>
                          <p className="text-sm text-muted-foreground">Feb 10, 2025</p>
                        </div>
                        <div className="text-sm font-medium text-primary">Dr. John Doe</div>
                      </div>
                      <div className="mt-2 pt-2 border-t">
                        <h4 className="text-sm font-medium">Diagnosis:</h4>
                        <p className="text-sm">Suspected seasonal asthma, requires pulmonary function testing</p>
                        <h4 className="text-sm font-medium mt-2">Prescription:</h4>
                        <p className="text-sm">Referred for pulmonary function test</p>
                        <p className="text-sm">Cetirizine 10mg daily for allergies</p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="symptoms" className="space-y-4 pt-4">
              <Card>
                <CardHeader>
                  <CardTitle>Symptoms Assessment</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="border rounded-lg p-4">
                      <div className="flex justify-between items-start">
                        <div>
                          <h3 className="font-medium">Chatbot Assessment</h3>
                          <p className="text-sm text-muted-foreground">Mar 24, 2025</p>
                        </div>
                      </div>
                      <div className="mt-2 pt-2 border-t">
                        <h4 className="text-sm font-medium">Reported Symptoms:</h4>
                        <ul className="list-disc pl-6 space-y-1 text-sm">
                          <li>Shortness of breath during physical activity</li>
                          <li>Occasional wheezing</li>
                          <li>Coughing at night</li>
                          <li>Chest tightness</li>
                        </ul>
                        <h4 className="text-sm font-medium mt-2">AI Assessment:</h4>
                        <p className="text-sm">
                          Symptoms consistent with mild asthma exacerbation. Recommended follow-up with physician.
                        </p>
                      </div>
                    </div>

                    <div className="border rounded-lg p-4">
                      <div className="flex justify-between items-start">
                        <div>
                          <h3 className="font-medium">Chatbot Assessment</h3>
                          <p className="text-sm text-muted-foreground">Feb 5, 2025</p>
                        </div>
                      </div>
                      <div className="mt-2 pt-2 border-t">
                        <h4 className="text-sm font-medium">Reported Symptoms:</h4>
                        <ul className="list-disc pl-6 space-y-1 text-sm">
                          <li>Persistent dry cough</li>
                          <li>Mild chest discomfort</li>
                          <li>Fatigue</li>
                          <li>Nasal congestion</li>
                        </ul>
                        <h4 className="text-sm font-medium mt-2">AI Assessment:</h4>
                        <p className="text-sm">
                          Symptoms may indicate upper respiratory infection or allergic response. Recommended
                          consultation with physician.
                        </p>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="reports" className="space-y-4 pt-4">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle>Medical Reports</CardTitle>
                  <Button size="sm">Add Report</Button>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {[
                      { name: "Pulmonary Function Test Report", date: "Mar 15, 2025" },
                      { name: "Allergy Test Results", date: "Feb 20, 2025" },
                      { name: "Annual Physical Examination", date: "Jan 05, 2025" },
                    ].map((report, i) => (
                      <div key={i} className="flex items-center justify-between p-3 border rounded-lg">
                        <div>
                          <div className="font-medium">{report.name}</div>
                          <div className="text-sm text-muted-foreground">{report.date}</div>
                        </div>
                        <div className="flex gap-2">
                          <Button variant="outline" size="sm">
                            View PDF
                          </Button>
                          <Button variant="outline" size="sm">
                            Delete
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="bloodtests" className="space-y-4 pt-4">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle>Blood Tests</CardTitle>
                  <Button size="sm">Add Blood Test</Button>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {[
                      { name: "Complete Blood Count", date: "Mar 10, 2025" },
                      { name: "Metabolic Panel", date: "Mar 10, 2025" },
                      { name: "Lipid Profile", date: "Jan 05, 2025" },
                    ].map((test, i) => (
                      <div key={i} className="flex items-center justify-between p-3 border rounded-lg">
                        <div>
                          <div className="font-medium">{test.name}</div>
                          <div className="text-sm text-muted-foreground">{test.date}</div>
                        </div>
                        <div className="flex gap-2">
                          <Button variant="outline" size="sm">
                            View PDF
                          </Button>
                          <Button variant="outline" size="sm">
                            Delete
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="xrays" className="space-y-4 pt-4">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle>X-Rays & Imaging</CardTitle>
                  <Button size="sm">Add X-Ray</Button>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2">
                    {[
                      { name: "Chest X-Ray", date: "Mar 12, 2025" },
                      { name: "CT Scan - Lungs", date: "Feb 15, 2025" },
                    ].map((xray, i) => (
                      <div key={i} className="flex items-center justify-between p-3 border rounded-lg">
                        <div>
                          <div className="font-medium">{xray.name}</div>
                          <div className="text-sm text-muted-foreground">{xray.date}</div>
                        </div>
                        <div className="flex gap-2">
                          <Button variant="outline" size="sm">
                            View Image
                          </Button>
                          <Button variant="outline" size="sm">
                            Delete
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="vitals" className="space-y-4 pt-4">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle>Vitals & Vaccination History</CardTitle>
                  <Button size="sm">Add Record</Button>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div>
                      <h3 className="text-sm font-medium mb-2">Latest Vitals (Mar 25, 2025)</h3>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
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
                    </div>

                    <div className="pt-2">
                      <h3 className="text-sm font-medium mb-2">Vaccination History</h3>
                      <div className="space-y-2">
                        {[
                          { name: "Influenza Vaccine", date: "Oct 15, 2024", due: "Oct 2025" },
                          { name: "Pneumococcal Vaccine", date: "Mar 10, 2023", due: "Mar 2028" },
                          { name: "COVID-19 Booster", date: "Jan 05, 2025", due: "Jan 2026" },
                        ].map((vaccine, i) => (
                          <div key={i} className="flex items-center justify-between p-3 border rounded-lg">
                            <div>
                              <div className="font-medium">{vaccine.name}</div>
                              <div className="text-sm text-muted-foreground">
                                Administered: {vaccine.date} • Due: {vaccine.due}
                              </div>
                            </div>
                            <Button variant="outline" size="sm">
                              View Details
                            </Button>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  )
}
