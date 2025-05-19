"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import Link from "next/link"
import { Search, Plus } from "lucide-react"
import { useState, useEffect } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { toast } from "sonner"
import { adminApi } from "@/lib/api"
import { Patient } from "@/lib/types"

export default function PatientsPage() {
  const [patients, setPatients] = useState<Patient[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [addPatientOpen, setAddPatientOpen] = useState(false)
  const [newPatient, setNewPatient] = useState({
    firstName: "",
    lastName: "",
    location: "",
    age: "",
    gender: "",
    email: "",
    bloodType: "",
    height: "",
    weight: "",
    maritalStatus: "",
    occupation: "",
    hasPets: false,
    isSmoking: false,
    password: "",
  })
  const [formErrors, setFormErrors] = useState({
    firstName: false,
    lastName: false,
    location: false,
    age: false,
    gender: false,
    email: false,
    bloodType: false,
    height: false,
    weight: false,
    maritalStatus: false,
    occupation: false,
    hasPets: false,
    isSmoking: false,
    password: false,
  })

  // Add state for editing a patient
  const [editPatient, setEditPatient] = useState<Patient | null>(null)
  const [editFormErrors, setEditFormErrors] = useState({
    firstName: false,
    lastName: false,
    age: false,
    location: false,
    condition: false,
  })

  useEffect(() => {
    const fetchPatients = async () => {
      try {
        const response = await adminApi.getAllPatients()
        setPatients(response)
      } catch (error) {
        console.error("Error fetching patients:", error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchPatients()
  }, [])

  // Filter patients based on search query
  const filteredPatients = patients.filter((patient) => {
    const query = searchQuery.toLowerCase()
    return (
      patient.firstName.toLowerCase().includes(query) ||
      patient.lastName.toLowerCase().includes(query) ||
      (patient.location && patient.location.toLowerCase().includes(query))
    )
  })

  // Validate the new patient form
  const validatePatientForm = () => {
    const errors = {
      firstName: !newPatient.firstName,
      lastName: !newPatient.lastName,
      location: !newPatient.location,
      age: !newPatient.age || isNaN(Number(newPatient.age)),
      gender: !newPatient.gender,
      email: !newPatient.email,
      bloodType: !newPatient.bloodType,
      height: !newPatient.height || isNaN(Number(newPatient.height)),
      weight: !newPatient.weight || isNaN(Number(newPatient.weight)),
      maritalStatus: !newPatient.maritalStatus,
      occupation: !newPatient.occupation,
      hasPets: typeof newPatient.hasPets !== "boolean",
      isSmoking: typeof newPatient.isSmoking !== "boolean",
      password: !validatePassword(newPatient.password), // Validate password
    }

    setFormErrors(errors)
    return !Object.values(errors).some((error) => error)
  }

  // Password validation function
  const validatePassword = (password: string) => {
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
    return passwordRegex.test(password)
  }


  // Function to handle editing a patient
  const handleEditPatient = async () => {
    if (!editPatient) return

    // Validate the form
    const errors = {
      firstName: !editPatient.firstName,
      lastName: !editPatient.lastName,
      age: !editPatient.age || isNaN(Number(editPatient.age)),
      location: !editPatient.location,
      condition: !editPatient.condition,
    }

    setEditFormErrors(errors)
    if (Object.values(errors).some((error) => error)) {
      toast.error("Please fill in all required fields correctly")
      return
    }

    try {
      // Add logging to debug the payload being sent
      console.log("Updating patient with payload:", editPatient) // Log the payload
      const updatedPatient = await adminApi.updatePatient(editPatient.id, editPatient)
      setPatients((prev) => prev.map((p) => (p.id === updatedPatient.id ? updatedPatient : p)))
      setEditPatient(null)
      toast.success("Patient updated successfully")
    } catch (error) {
      console.error("Error updating patient:", error)
      toast.error("Failed to update patient")
    }
  }

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value)
  }

  if (isLoading) {
    return <p>Loading patients...</p>
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Patients</h1>
        <p className="text-muted-foreground">Manage patient information and records.</p>
      </div>

      <div className="flex items-center space-x-2">
        <div className="relative flex-1">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search patients..."
            className="w-full pl-8"
            value={searchQuery}
            onChange={handleSearchChange}
          />
        </div>
      </div>

      {/* Enhance patient card layout */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {filteredPatients.map((patient) => (
          <Card key={patient.id} className="shadow-md hover:shadow-lg transition-shadow">
            <CardContent className="p-6">
              <div className="space-y-4">
                <div className="font-semibold text-lg">
                  {patient.firstName} {patient.lastName}
                </div>
                <div className="text-sm text-muted-foreground">
                  Location: {patient.location || "Not provided"}
                </div>
                <div className="text-sm">Age: {patient.age || "Unknown"}</div>
                <div className="text-sm">Gender: {patient.gender || "Unknown"}</div>
                <div className="text-sm">Email: {patient.email || "Not provided"}</div>
                <div className="flex justify-between pt-4">
                  <Link href={`/admin/patients/${patient.id}`}>
                    <Button variant="outline" size="sm">
                      View Details
                    </Button>
                  </Link>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}

        {filteredPatients.length === 0 && (
          <div className="col-span-full text-center py-10">
            <p className="text-muted-foreground">No patients found matching your search.</p>
          </div>
        )}
      </div>

      {/* Edit Patient Dialog */}
      {editPatient && (
        <Dialog open={!!editPatient} onOpenChange={() => setEditPatient(null)}>
          <DialogContent className="sm:max-w-[800px]">
            <DialogHeader>
              <DialogTitle>Edit Patient Information</DialogTitle>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="photo">Photo URL</Label>
                <Input
                  id="photo"
                  value={editPatient.photo || ""}
                  onChange={(e) => setEditPatient({ ...editPatient, photo: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="insuranceProvider">Insurance Provider</Label>
                <Input
                  id="insuranceProvider"
                  value={editPatient.insuranceProvider || ""}
                  onChange={(e) => setEditPatient({ ...editPatient, insuranceProvider: e.target.value })}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="firstName">First Name</Label>
                  <Input
                    id="firstName"
                    value={editPatient.firstName}
                    onChange={(e) => setEditPatient({ ...editPatient, firstName: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="lastName">Last Name</Label>
                  <Input
                    id="lastName"
                    value={editPatient.lastName}
                    onChange={(e) => setEditPatient({ ...editPatient, lastName: e.target.value })}
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="gender">Gender</Label>
                  <Input
                    id="gender"
                    value={editPatient.gender}
                    onChange={(e) => setEditPatient({ ...editPatient, gender: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="age">Age</Label>
                  <Input
                    id="age"
                    type="number"
                    value={editPatient.age}
                    onChange={(e) => setEditPatient({ ...editPatient, age: Number(e.target.value) })}
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="bloodType">Blood Type</Label>
                  <Input
                    id="bloodType"
                    value={editPatient.bloodType || ""}
                    onChange={(e) => setEditPatient({ ...editPatient, bloodType: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="height">Height (cm)</Label>
                  <Input
                    id="height"
                    type="number"
                    value={editPatient.height || 0}
                    onChange={(e) => setEditPatient({ ...editPatient, height: Number(e.target.value) })}
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="weight">Weight (kg)</Label>
                  <Input
                    id="weight"
                    type="number"
                    value={editPatient.weight || 0}
                    onChange={(e) => setEditPatient({ ...editPatient, weight: Number(e.target.value) })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="location">Location</Label>
                  <Input
                    id="location"
                    value={editPatient.location || ""}
                    onChange={(e) => setEditPatient({ ...editPatient, location: e.target.value })}
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="maritalStatus">Marital Status</Label>
                  <Input
                    id="maritalStatus"
                    value={editPatient.maritalStatus || ""}
                    onChange={(e) => setEditPatient({ ...editPatient, maritalStatus: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="occupation">Occupation</Label>
                  <Input
                    id="occupation"
                    value={editPatient.occupation || ""}
                    onChange={(e) => setEditPatient({ ...editPatient, occupation: e.target.value })}
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="hasPets">Has Pets</Label>
                  <Input
                    id="hasPets"
                    type="checkbox"
                    checked={editPatient.hasPets || false}
                    onChange={(e) => setEditPatient({ ...editPatient, hasPets: e.target.checked })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="isSmoking">Smoking</Label>
                  <Input
                    id="isSmoking"
                    type="checkbox"
                    checked={editPatient.isSmoking || false}
                    onChange={(e) => setEditPatient({ ...editPatient, isSmoking: e.target.checked })}
                  />
                </div>
              </div>

              {/* Add the email field back to the edit form as non-editable information */}
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  value={editPatient.email || ""}
                  readOnly
                  className="bg-gray-100 cursor-not-allowed"
                />
              </div>

              {/* Add similar inputs for other attributes like previousDiagnosis, vitals, etc. */}
            </div>
            <DialogFooter>
              <DialogClose asChild>
                <Button variant="outline">Cancel</Button>
              </DialogClose>
              <Button onClick={handleEditPatient}>Save Changes</Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </div>
  )
}
