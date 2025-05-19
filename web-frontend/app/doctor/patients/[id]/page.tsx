"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import Link from "next/link"
import { useEffect, useState } from "react"
import { toast } from "sonner"
import { useParams } from "next/navigation"
import { adminApi } from '@/lib/api';
import { Patient } from "@/lib/types";

export default function PatientDetailsPage() {
  const { id } = useParams();
  const [patient, setPatient] = useState<Patient | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [editedPatient, setEditedPatient] = useState({
    id: id,
    firstName: '',
    lastName: '',
    name: '',
    age: '',
    gender: '',
    email: '',
    phone: '',
    height: '',
    weight: '',
    location: '',
    maritalStatus: '',
    occupation: '',
    pets: '',
    smoking: '',
    address: '',
    insuranceProvider: '',
    bloodType: '',
  })
  const [formErrors, setFormErrors] = useState({
    name: false,
    age: false,
    gender: false,
    email: false,
    phone: false,
  })
  const [editVitalsDialogOpen, setEditVitalsDialogOpen] = useState(false)
  const [editedVitals, setEditedVitals] = useState({
    heartRate: '',
    bloodPressure: '',
    temperature: '',
    respiratoryRate: '',
  })
  const [editAllergiesDialogOpen, setEditAllergiesDialogOpen] = useState(false);
  const [editSurgeriesDialogOpen, setEditSurgeriesDialogOpen] = useState(false);
  const [editChronicDialogOpen, setEditChronicDialogOpen] = useState(false);
  const [editedAllergies, setEditedAllergies] = useState(patient?.allergies || []);
  const [editedSurgeries, setEditedSurgeries] = useState<{ name: string }[]>([]);
  const [editedChronicConditions, setEditedChronicConditions] = useState(patient?.chronicConditions || []);

  useEffect(() => {
    const fetchPatient = async () => {
      try {
        if (typeof id === "string") {
          const response = await adminApi.getPatientById(id);
          setPatient(response);
          setEditedPatient((prev) => ({
            ...prev,
            id: response.id,
            firstName: response.firstName || '',
            lastName: response.lastName || '',
            name: response.name || '',
            age: response.age?.toString() || '',
            gender: response.gender || '',
            email: response.email || '',
            phone: response.phone || '',
            height: response.height?.toString() || '',
            weight: response.weight?.toString() || '',
            location: response.location || '',
            maritalStatus: response.maritalStatus || '',
            occupation: response.occupation || '',
            pets: response.hasPets ? 'Yes' : 'No',
            smoking: response.isSmoking ? 'Yes' : 'No',
            address: response.address || '',
            insuranceProvider: response.insuranceProvider || '',
            bloodType: response.bloodType || '',
          }));
        }
      } catch (error) {
        console.error("Error fetching patient details:", error);
      }
    };

    fetchPatient();
  }, [id]);

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
  const handleSavePatient = async () => {
    try {
      if (typeof id === 'string') {
        // Validate and sanitize data
        const updatedPatientData: Patient = {
          id: id,
          firstName: editedPatient.firstName.trim(),
          lastName: editedPatient.lastName.trim(),
          name: editedPatient.name.trim(),
          location: editedPatient.location?.trim() || '',
          age: parseInt(editedPatient.age, 10) || 0, // Default to 0 if invalid
          condition: patient?.condition || '',
          lastVisit: patient?.lastVisit || '',
          photo: patient?.photo || '',
          insuranceProvider: editedPatient.insuranceProvider?.trim() || '',
          gender: editedPatient.gender?.trim() || '',
          email: editedPatient.email?.trim() || '',
          bloodType: editedPatient.bloodType?.trim() || '',
          height: parseFloat(editedPatient.height) || 0, // Default to 0 if invalid
          weight: parseFloat(editedPatient.weight) || 0, // Default to 0 if invalid
          maritalStatus: editedPatient.maritalStatus?.trim() || '',
          occupation: editedPatient.occupation?.trim() || '',
          hasPets: editedPatient.pets === 'Yes',
          isSmoking: editedPatient.smoking === 'Yes',
          previousDiagnosis: patient?.previousDiagnosis || [],
          previousPlans: patient?.previousPlans || [],
          previousPrescriptions: patient?.previousPrescriptions || [],
          previousResources: patient?.previousResources || [],
          symptomsAssessment: patient?.symptomsAssessment || '',
          report: patient?.report || '',
          bloodTests: patient?.bloodTests || [],
          xRays: patient?.xRays || [],
          otherImaging: patient?.otherImaging || [],
          vaccinationHistory: patient?.vaccinationHistory || [],
          vitals: patient?.vitals || {},
          allergies: patient?.allergies?.map((item) => item.trim()) || [],
          chronicConditions: patient?.chronicConditions?.map((item) => item.trim()) || [],
          surgeriesHistory: patient?.surgeriesHistory || [],
        };

        console.log('Payload being sent to server:', updatedPatientData);

        // Call the PUT endpoint to update the patient info in the database
        await adminApi.updatePatient(id, updatedPatientData);

        toast.success('Patient information updated successfully!');
        setEditDialogOpen(false);

        // Optionally, update local state or cache if needed
        setPatient(updatedPatientData);
      } else {
        console.error('Invalid patient ID');
        toast.error('Failed to update patient information.');
      }
    } catch (error) {
      console.error('Error updating patient:', error);
      toast.error('Failed to update patient information.');
    }
  }

  // Handle saving edited vitals
  const handleSaveVitals = async () => {
    try {
      if (typeof id === 'string') {
        // Prepare updated vitals data
        const updatedVitalsData = {
          heartRate: parseFloat(editedVitals.heartRate),
          bloodPressure: editedVitals.bloodPressure,
          temperature: parseFloat(editedVitals.temperature),
          respiratoryRate: parseFloat(editedVitals.respiratoryRate),
        };

        console.log('Vitals payload being sent to server:', updatedVitalsData);

        // Call the PUT endpoint to update the patient vitals in the database
        await adminApi.updatePatient(id, {
          ...(patient as Patient),
          vitals: updatedVitalsData,
        });

        toast.success('Patient vitals updated successfully!');
        setEditVitalsDialogOpen(false);

        // Optionally, update local state or cache if needed
        setPatient((prev) => prev ? {
          ...prev,
          vitals: {
            ...prev.vitals,
            ...updatedVitalsData,
          },
        } : null);
      } else {
        console.error('Invalid patient ID');
        toast.error('Failed to update patient vitals.');
      }
    } catch (error) {
      console.error('Error updating patient vitals:', error);
      toast.error('Failed to update patient vitals.');
    }
  }

  // Handle saving edited allergies
  const handleSaveAllergies = async () => {
    try {
      if (typeof id === 'string') {
        // Ensure allergies are formatted as a string array
        const formattedAllergies = editedAllergies.map((item) => item.trim()).filter((item) => item);

        // Call the PUT endpoint to update the patient allergies in the database
        await adminApi.updatePatient(id, {
          ...(patient as Patient),
          allergies: formattedAllergies,
        });

        toast.success('Allergies updated successfully!');
        setEditAllergiesDialogOpen(false);

        // Optionally, update local state or cache if needed
        setPatient((prev) => prev ? { ...prev, allergies: formattedAllergies } : prev);
      } else {
        console.error('Invalid patient ID');
        toast.error('Failed to update allergies.');
      }
    } catch (error) {
      console.error('Error updating allergies:', error);
      toast.error('Failed to update allergies.');
    }
  };

  // Handle saving edited chronic conditions
  const handleSaveChronicConditions = async () => {
    try {
      if (typeof id === 'string') {
        // Ensure chronic conditions are formatted as a string array
        const formattedChronicConditions = editedChronicConditions.map((item) => item.trim()).filter((item) => item);

        // Call the PUT endpoint to update the patient chronic conditions in the database
        await adminApi.updatePatient(id, {
          ...(patient as Patient),
          chronicConditions: formattedChronicConditions,
        });

        toast.success('Chronic conditions updated successfully!');
        setEditChronicDialogOpen(false);

        // Optionally, update local state or cache if needed
        setPatient((prev) => prev ? { ...prev, chronicConditions: formattedChronicConditions } : prev);
      } else {
        console.error('Invalid patient ID');
        toast.error('Failed to update chronic conditions.');
      }
    } catch (error) {
      console.error('Error updating chronic conditions:', error);
      toast.error('Failed to update chronic conditions.');
    }
  };

  // Handle saving edited surgeries
  const handleSaveSurgeries = async () => {
    try {
      if (typeof id === 'string') {
        // Ensure surgeries history is formatted as an array of objects with a `name` property
        const formattedSurgeries = editedSurgeries.map((item) => ({
          name: item.name.trim(),
        })).filter((item) => item.name);

        // Call the PUT endpoint to update the patient surgeries history in the database
        await adminApi.updatePatient(id, {
          ...(patient as Patient),
          surgeriesHistory: formattedSurgeries,
        });

        toast.success('Surgeries history updated successfully!');
        setEditSurgeriesDialogOpen(false);

        // Optionally, update local state or cache if needed
        setPatient((prev) => prev ? { ...prev, surgeriesHistory: formattedSurgeries } : prev);
      } else {
        console.error('Invalid patient ID');
        toast.error('Failed to update surgeries history.');
      }
    } catch (error) {
      console.error('Error updating surgeries history:', error);
      toast.error('Failed to update surgeries history.');
    }
  };

  const handleInputChange = (setter: React.Dispatch<React.SetStateAction<string[]>>) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const input = e.target.value;
    const items = input.split(',').map((item) => item.trim()).filter((item) => item);
    setter(items);
  };

  const handleStringArrayInputChange = (setter: React.Dispatch<React.SetStateAction<string[]>>) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const input = e.target.value;
    const items = input.split(',').map((item) => item.trim()).filter((item) => item);
    setter(items);
  };

  const handleObjectArrayInputChange = (setter: React.Dispatch<React.SetStateAction<Record<string, any>[]>>) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const input = e.target.value;
    const items = input.split(',').map((item) => ({ name: item.trim() })).filter((item) => item.name);
    setter(items);
  };

  function handleSurgeriesInputChange(value: string) {
    const surgeriesArray = value.split(',').map((item) => ({ name: item.trim() })).filter((item) => item.name);
    setEditedSurgeries(surgeriesArray);
  }

  // Update the display logic for surgeries to show only the value without the `name:` prefix
  const displayedSurgeries = patient?.surgeriesHistory?.map((surgery) => surgery.name || surgery) || [];

  if (!patient) {
    return <p>Loading patient details...</p>;
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
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-[1fr_3fr]">
        {/* Left Section */}
        <Card>
          <CardContent className="p-6 flex flex-col items-center gap-4">
            <div className="relative w-32 h-32 rounded-full overflow-hidden border-4 border-muted">
              <div
                className="w-full h-full bg-center bg-cover"
                style={{ backgroundImage: `url('${patient.photo || '/placeholder.svg?height=128&width=128'}')` }}
                aria-label="Patient profile"
              />
            </div>
            <div className="text-center">
              <h2 className="text-xl font-bold">{patient.name || `${patient.firstName} ${patient.lastName}`}</h2>
              <p className="text-sm text-muted-foreground">{patient.email}</p>
            </div>
            <div className="w-full space-y-4 pt-4">
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="text-muted-foreground">Gender:</div>
                <div>{patient.gender}</div>
                <div className="text-muted-foreground">Age:</div>
                <div>{patient.age}</div>
                <div className="text-muted-foreground">Location:</div>
                <div>{patient.location}</div>
                <div className="text-muted-foreground">Insurance Provider:</div>
                <div>{patient.insuranceProvider || "Not provided"}</div>
                <div className="text-muted-foreground">Blood Type:</div>
                <div>{patient.bloodType || "Not provided"}</div>
                <div className="text-muted-foreground">Height:</div>
                <div>{patient.height}</div>
                <div className="text-muted-foreground">Weight:</div>
                <div>{patient.weight}</div>
                <div className="text-muted-foreground">Marital Status:</div>
                <div>{patient.maritalStatus}</div>
                <div className="text-muted-foreground">Occupation:</div>
                <div>{patient.occupation}</div>
                <div className="text-muted-foreground">Pets:</div>
                <div>{patient.hasPets ? "Yes" : "No"}</div>
                <div className="text-muted-foreground">Smoking:</div>
                <div>{patient.isSmoking ? "Yes" : "No"}</div>
              </div>
            </div>
            <div className="pt-4 text-center">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setEditDialogOpen(true)}
              >
                Edit Info
              </Button>
            </div>

            {/* Edit Patient Dialog */}
            <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
                          <DialogContent className="sm:max-w-[800px] max-w-full">
                            <DialogHeader>
                              <DialogTitle>Edit Patient Information</DialogTitle>
                            </DialogHeader>
                            <div className="grid gap-4 py-4">
                              <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                  <Label htmlFor="firstName">First Name</Label>
                                  <Input
                                    id="firstName"
                                    value={editedPatient.firstName}
                                    onChange={(e) => setEditedPatient({ ...editedPatient, firstName: e.target.value })}
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label htmlFor="lastName">Last Name</Label>
                                  <Input
                                    id="lastName"
                                    value={editedPatient.lastName}
                                    onChange={(e) => setEditedPatient({ ...editedPatient, lastName: e.target.value })}
                                  />
                                </div>
                              </div>
                              <div className="space-y-2">
                                <Label htmlFor="gender">Gender</Label>
                                <Input
                                  id="gender"
                                  value={editedPatient.gender}
                                  readOnly
                                  className="bg-gray-100 cursor-not-allowed"
                                />
                              </div>
                              <div className="space-y-2">
                                <Label htmlFor="age">Age</Label>
                                <Input
                                  id="age"
                                  type="number"
                                  value={editedPatient.age}
                                  onChange={(e) => setEditedPatient({ ...editedPatient, age: e.target.value })}
                                />
                              </div>
                              <div className="space-y-2">  
                                <Label htmlFor="location">Location</Label>  
                                <Input  
                                  id="location"  
                                  value={editedPatient.location}  
                                  onChange={(e) => setEditedPatient({ ...editedPatient, location: e.target.value })}  
                                />  
                              </div>
                              <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                  <Label htmlFor="insuranceProvider">Insurance Provider</Label>
                                  <Input
                                    id="insuranceProvider"
                                    value={editedPatient.insuranceProvider}
                                    onChange={(e) => setEditedPatient({ ...editedPatient, insuranceProvider: e.target.value })}
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label htmlFor="bloodType">Blood Type</Label>
                                  <Select
                                    value={editedPatient.bloodType}
                                    onValueChange={(value) => setEditedPatient({ ...editedPatient, bloodType: value })}
                                  >
                                    <SelectTrigger>
                                      <SelectValue placeholder="Select" />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="A+">A+</SelectItem>
                                      <SelectItem value="A-">A-</SelectItem>
                                      <SelectItem value="B+">B+</SelectItem>
                                      <SelectItem value="B-">B-</SelectItem>
                                      <SelectItem value="AB+">AB+</SelectItem>
                                      <SelectItem value="AB-">AB-</SelectItem>
                                      <SelectItem value="O+">O+</SelectItem>
                                      <SelectItem value="O-">O-</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                              </div>
                              <div className="space-y-2">
                                <Label htmlFor="email">Email</Label>
                                <Input
                                  id="email"
                                  value={editedPatient.email}
                                  readOnly
                                  className="bg-gray-100 cursor-not-allowed"
                                />
                              </div>
                              <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                  <Label htmlFor="maritalStatus">Marital Status</Label>
                                  <Input
                                    id="maritalStatus"
                                    value={editedPatient.maritalStatus}
                                    onChange={(e) => setEditedPatient({ ...editedPatient, maritalStatus: e.target.value })}
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label htmlFor="occupation">Occupation</Label>
                                  <Input
                                    id="occupation"
                                    value={editedPatient.occupation}
                                    onChange={(e) => setEditedPatient({ ...editedPatient, occupation: e.target.value })}
                                  />
                                </div>
                              </div>
                              <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                  <Label htmlFor="height">Height</Label>
                                  <Input
                                    id="height"
                                    type="number"
                                    value={editedPatient.height}
                                    onChange={(e) => setEditedPatient({ ...editedPatient, height: e.target.value })}
                                  />
                                </div>
                                <div className="space-y-2">
                                  <Label htmlFor="weight">Weight</Label>
                                  <Input
                                    id="weight"
                                    type="number"
                                    value={editedPatient.weight}
                                    onChange={(e) => setEditedPatient({ ...editedPatient, weight: e.target.value })}
                                  />
                                </div>
                              </div>
                              <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                  <Label htmlFor="pets">Pets</Label>
                                  <Select
                                    value={editedPatient.pets}
                                    onValueChange={(value) => setEditedPatient({ ...editedPatient, pets: value })}
                                  >
                                    <SelectTrigger>
                                      <SelectValue placeholder="Select" />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="Yes">Yes</SelectItem>
                                      <SelectItem value="No">No</SelectItem>
                                    </SelectContent>
                                  </Select>
                                </div>
                                <div className="space-y-2">
                                  <Label htmlFor="smoking">Smoking</Label>
                                  <Select
                                    value={editedPatient.smoking}
                                    onValueChange={(value) => setEditedPatient({ ...editedPatient, smoking: value })}
                                  >
                                    <SelectTrigger>
                                      <SelectValue placeholder="Select" />
                                    </SelectTrigger>
                                    <SelectContent>
                                      <SelectItem value="Yes">Yes</SelectItem>
                                      <SelectItem value="No">No</SelectItem>
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
          </CardContent>
        </Card>

        {/* Right Section */}
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
                  {/* ...prescriptions content... */}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="symptoms" className="space-y-4 pt-4">
              <Card>
                <CardHeader>
                  <CardTitle>Symptoms Assessment</CardTitle>
                </CardHeader>
                <CardContent>
                  {/* ...symptoms content... */}
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
                  {/* ...reports content... */}
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
                  {/* ...blood tests content... */}
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
                  {/* ...x-rays content... */}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="vitals" className="space-y-4 pt-4">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle>Vitals & Conditions</CardTitle>
                </CardHeader>
                <CardContent>
                  {/* Vitals Section */}
                  <div className="space-y-8 pb-8">
                    <h3 className="text-lg font-semibold border-b pb-2">Vitals</h3>
                    {patient.vitals ? (
                      <div className="grid grid-cols-2 gap-y-4 gap-x-6">
                        <div className="flex items-center text-muted-foreground font-medium">
                          <span className="mr-2">❤️</span> Heart Rate:
                        </div>
                        <div>{patient.vitals.heartRate || "Not provided"}</div>
                        <div className="flex items-center text-muted-foreground font-medium">
                          <span className="mr-2">🩸</span> Blood Pressure:
                        </div>
                        <div>{patient.vitals.bloodPressure || "Not provided"}</div>
                        <div className="flex items-center text-muted-foreground font-medium">
                          <span className="mr-2">🌡️</span> Temperature:
                        </div>
                        <div>{patient.vitals.temperature || "Not provided"}</div>
                        <div className="flex items-center text-muted-foreground font-medium">
                          <span className="mr-2">💨</span> Respiratory Rate:
                        </div>
                        <div>{patient.vitals.respiratoryRate || "Not provided"}</div>
                      </div>
                    ) : (
                      <p className="text-muted-foreground italic">No vitals data available.</p>
                    )}
                    <div className="flex justify-end pt-4">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setEditVitalsDialogOpen(true)}
                      >
                        Edit Vitals
                      </Button>
                    </div>

                    {/* Edit Vitals Dialog */}
                    <Dialog open={editVitalsDialogOpen} onOpenChange={setEditVitalsDialogOpen}>
                      <DialogContent className="sm:max-w-[800px] max-w-full">
                        <DialogHeader>
                          <DialogTitle>Edit Vitals</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                          <div className="space-y-2">
                            <Label htmlFor="heartRate">Heart Rate</Label>
                            <Input
                              id="heartRate"
                              type="number"
                              value={editedVitals.heartRate}
                              onChange={(e) => setEditedVitals({ ...editedVitals, heartRate: e.target.value })}
                            />
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="bloodPressure">Blood Pressure</Label>
                            <Input
                              id="bloodPressure"
                              value={editedVitals.bloodPressure}
                              onChange={(e) => setEditedVitals({ ...editedVitals, bloodPressure: e.target.value })}
                            />
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="temperature">Temperature</Label>
                            <Input
                              id="temperature"
                              type="number"
                              value={editedVitals.temperature}
                              onChange={(e) => setEditedVitals({ ...editedVitals, temperature: e.target.value })}
                            />
                          </div>
                          <div className="space-y-2">
                            <Label htmlFor="respiratoryRate">Respiratory Rate</Label>
                            <Input
                              id="respiratoryRate"
                              type="number"
                              value={editedVitals.respiratoryRate}
                              onChange={(e) => setEditedVitals({ ...editedVitals, respiratoryRate: e.target.value })}
                            />
                          </div>
                        </div>
                        <DialogFooter>
                          <DialogClose asChild>
                            <Button variant="outline">Cancel</Button>
                          </DialogClose>
                          <Button onClick={handleSaveVitals}>Save Changes</Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>
                  </div>

                  {/* Allergies Section */}
                  <div className="space-y-8 pb-8">
                    <h3 className="text-lg font-semibold border-b pb-2">Allergies</h3>
                    {patient.allergies && patient.allergies.length > 0 ? (
                      <ul className="list-disc pl-6 space-y-1">
                        {patient.allergies.map((allergy, index) => (
                          <li key={index} className="text-muted-foreground">{allergy}</li>
                        ))}
                      </ul>
                    ) : (
                      <p className="text-muted-foreground italic">No allergies reported.</p>
                    )}
                    <div className="flex justify-end">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setEditAllergiesDialogOpen(true)}
                      >
                        Edit Allergies
                      </Button>
                    </div>

                    {/* Edit Allergies Dialog */}
                    <Dialog open={editAllergiesDialogOpen} onOpenChange={setEditAllergiesDialogOpen}>
                      <DialogContent className="sm:max-w-[800px] max-w-full">
                        <DialogHeader>
                          <DialogTitle>Edit Allergies</DialogTitle>
                        </DialogHeader>
                        <div className="py-4">
                          <Label htmlFor="allergies">Allergies</Label>
                          <Input
                            id="allergies"
                            value={editedAllergies.join(', ')}
                            onChange={handleStringArrayInputChange(setEditedAllergies)}
                            placeholder="Enter allergies separated by commas"
                          />
                        </div>
                        <DialogFooter>
                          <DialogClose asChild>
                            <Button variant="outline">Cancel</Button>
                          </DialogClose>
                          <Button onClick={handleSaveAllergies}>Save Changes</Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>
                  </div>

                  {/* Chronic Conditions Section */}
                  <div className="space-y-8 pb-8">
                    <h3 className="text-lg font-semibold border-b pb-2">Chronic Conditions</h3>
                    {patient.chronicConditions && patient.chronicConditions.length > 0 ? (
                      <ul className="list-disc pl-6 space-y-1">
                        {patient.chronicConditions.map((condition, index) => (
                          <li key={index} className="text-muted-foreground">{condition}</li>
                        ))}
                      </ul>
                    ) : (
                      <p className="text-muted-foreground italic">No chronic conditions reported.</p>
                    )}
                    <div className="flex justify-end">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setEditChronicDialogOpen(true)}
                      >
                        Edit Chronic Conditions
                      </Button>
                    </div>

                    {/* Edit Chronic Conditions Dialog */}
                    <Dialog open={editChronicDialogOpen} onOpenChange={setEditChronicDialogOpen}>
                      <DialogContent className="sm:max-w-[800px] max-w-full">
                        <DialogHeader>
                          <DialogTitle>Edit Chronic Conditions</DialogTitle>
                        </DialogHeader>
                        <div className="py-4">
                          <Label htmlFor="chronicConditions">Chronic Conditions</Label>
                          <Input
                            id="chronicConditions"
                            value={editedChronicConditions.join(', ')}
                            onChange={handleStringArrayInputChange(setEditedChronicConditions)}
                            placeholder="Enter chronic conditions separated by commas"
                          />
                        </div>
                        <DialogFooter>
                          <DialogClose asChild>
                            <Button variant="outline">Cancel</Button>
                          </DialogClose>
                          <Button onClick={handleSaveChronicConditions}>Save Changes</Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>
                  </div>

                  {/* Surgeries History Section */}
                  <div className="space-y-8 pb-8">
                    <h3 className="text-lg font-semibold border-b pb-2">Surgeries History</h3>
                    {patient.surgeriesHistory && patient.surgeriesHistory.length > 0 ? (
                      <ul className="list-disc pl-6 space-y-1">
                        {patient.surgeriesHistory.map((surgery, index) => (
                          <li key={index} className="text-muted-foreground">{surgery.name || surgery}</li>
                        ))}
                      </ul>
                    ) : (
                      <p className="text-muted-foreground italic">No surgeries history available.</p>
                    )}
                    <div className="flex justify-end">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setEditSurgeriesDialogOpen(true)}
                      >
                        Edit Surgeries History
                      </Button>
                    </div>

                    {/* Edit Surgeries History Dialog */}
                    <Dialog open={editSurgeriesDialogOpen} onOpenChange={setEditSurgeriesDialogOpen}>
                      <DialogContent className="sm:max-w-[800px] max-w-full">
                        <DialogHeader>
                          <DialogTitle>Edit Surgeries History</DialogTitle>
                        </DialogHeader>
                        <div className="py-4">
                          <Label htmlFor="surgeriesHistory">Surgeries History</Label>
                          <Input
                            id="surgeriesHistory"
                            value={editedSurgeries.map((item) => item.name || item).join(', ')}
                            onChange={(e) => handleSurgeriesInputChange(e.target.value)}
                            placeholder="Enter surgeries history separated by commas"
                          />
                        </div>
                        <DialogFooter>
                          <DialogClose asChild>
                            <Button variant="outline">Cancel</Button>
                          </DialogClose>
                          <Button onClick={handleSaveSurgeries}>Save Changes</Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>
                  </div>

                  {/* Vaccination History Section */}
                  <div className="space-y-8 pb-8">
                    <h3 className="text-lg font-semibold border-b pb-2">Vaccination History</h3>
                    {Array.isArray(patient.vaccinationHistory) && patient.vaccinationHistory.length > 0 ? (
                      <ul className="list-disc pl-6 space-y-1">
                        {patient.vaccinationHistory.map((vaccine, index) => (
                          <li key={index} className="text-muted-foreground">{typeof vaccine === 'string' ? vaccine : JSON.stringify(vaccine)}</li>
                        ))}
                      </ul>
                    ) : (
                      <p className="text-muted-foreground italic">No vaccination history available.</p>
                    )}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
}
