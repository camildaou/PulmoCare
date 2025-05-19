"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import Link from "next/link"
import { toast } from "sonner"
import { Search, Plus } from "lucide-react"
import { doctorApi } from "@/lib/api"
import { Patient } from "@/lib/types"

export default function DoctorPatientsPage() {
  const [patients, setPatients] = useState<Patient[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null)
  const [editPatientDialogOpen, setEditPatientDialogOpen] = useState(false)
  const [addPatientDialogOpen, setAddPatientDialogOpen] = useState(false)

  // State for form validation
  const [formErrors, setFormErrors] = useState({
    firstName: false,
    lastName: false,
    age: false,
    gender: false,
    email: false,
    condition: false,
  })

  useEffect(() => {
    const fetchPatients = async () => {
      try {
        const response = await doctorApi.getAllPatients();
        setPatients(response);
      } catch (error) {
        console.error("Error fetching patients:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchPatients();
  }, [])

  // Filter patients based on search query
  const filteredPatients = patients.filter((patient) => {
    const query = searchQuery.toLowerCase();
    const fullName = `${patient.firstName} ${patient.lastName}`.toLowerCase();

    return (
      fullName.includes(query) ||
      (patient.name && patient.name.toLowerCase().includes(query)) ||
      (patient.condition && patient.condition.toLowerCase().includes(query)) ||
      (patient.location && patient.location.toLowerCase().includes(query))
    );
  })

  // Validate the form
  const validateForm = (patient: Patient) => {
    const errors = {
      firstName: !patient.firstName,
      lastName: !patient.lastName,
      age: !patient.age || isNaN(Number(patient.age)),
      gender: !patient.gender,
      email: !patient.email,
      condition: !patient.condition,
    }

    setFormErrors(errors)
    return !Object.values(errors).some((error) => error)
  }

  // Handle adding a new patient
  const handleAddPatient = async () => {
    toast.error("Adding new patients is not implemented yet.")
  }

  // Handle editing a patient
  const handleEditPatient = async () => {
    if (!selectedPatient) return
    
    if (!validateForm(selectedPatient)) {
      toast.error("Please fill in all required fields correctly")
      return
    }

    try {
      // In a real implementation, this would call an API
      // const updatedPatient = await doctorApi.updatePatient(selectedPatient.id, selectedPatient)
      
      // Update the local state
      const updatedPatients = patients.map((patient) =>
        patient.id === selectedPatient.id ? { ...selectedPatient } : patient
      )

      setPatients(updatedPatients)
      setEditPatientDialogOpen(false)
      setSelectedPatient(null)
      toast.success("Patient updated successfully")
    } catch (error) {
      console.error("Error updating patient:", error)
      toast.error("Failed to update patient")
    }
  }

  // Open add patient dialog
  const openAddDialog = () => {
    setAddPatientDialogOpen(true)
    setFormErrors({
      firstName: false,
      lastName: false,
      age: false,
      gender: false,
      email: false,
      condition: false,
    })
  }

  // Open edit dialog with patient data
  const openEditDialog = (patient: Patient) => {
    setSelectedPatient({ ...patient })
    setEditPatientDialogOpen(true)
    setFormErrors({
      firstName: false,
      lastName: false,
      age: false,
      gender: false,
      email: false,
      condition: false,
    })
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Patients</h1>
          <p className="text-muted-foreground">Manage your patients and their medical records.</p>
        </div>
        <Button
          onClick={() => {
            toast.error("Adding new patients is not implemented yet.")
          }}
        >
          Add Patient
        </Button>
      </div>

      {/* Edit Patient Dialog */}
      <Dialog open={editPatientDialogOpen} onOpenChange={setEditPatientDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Edit Patient</DialogTitle>
          </DialogHeader>
          {selectedPatient && (
            <div className="grid gap-4 py-4 max-h-[70vh] overflow-y-auto pr-2">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="edit-first-name" className="flex items-center">
                    First Name <span className="text-red-500 ml-1">*</span>
                  </Label>
                  <Input
                    id="edit-first-name"
                    placeholder="Enter patient's first name"
                    value={selectedPatient.firstName}
                    onChange={(e) => {
                      setSelectedPatient({ ...selectedPatient, firstName: e.target.value })
                      setFormErrors({ ...formErrors, firstName: false })
                    }}
                    className={formErrors.firstName ? "border-red-500" : ""}
                  />
                  {formErrors.firstName && <p className="text-red-500 text-sm">First name is required</p>}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="edit-last-name" className="flex items-center">
                    Last Name <span className="text-red-500 ml-1">*</span>
                  </Label>
                  <Input
                    id="edit-last-name"
                    placeholder="Enter patient's last name"
                    value={selectedPatient.lastName}
                    onChange={(e) => {
                      setSelectedPatient({ ...selectedPatient, lastName: e.target.value })
                      setFormErrors({ ...formErrors, lastName: false })
                    }}
                    className={formErrors.lastName ? "border-red-500" : ""}
                  />
                  {formErrors.lastName && <p className="text-red-500 text-sm">Last name is required</p>}
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="edit-age" className="flex items-center">
                  Age <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="edit-age"
                  type="number"
                  placeholder="Enter age"
                  value={selectedPatient.age}
                  onChange={(e) => {
                    setSelectedPatient({ ...selectedPatient, age: parseInt(e.target.value, 10) });
                    setFormErrors({ ...formErrors, age: false });
                  }}
                  className={formErrors.age ? "border-red-500" : ""}
                />
                {formErrors.age && <p className="text-red-500 text-sm">Valid age is required</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="edit-gender" className="flex items-center">
                  Gender <span className="text-red-500 ml-1">*</span>
                </Label>
                <Select
                  value={selectedPatient.gender}
                  onValueChange={(value) => {
                    setSelectedPatient({ ...selectedPatient, gender: value })
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
                  <Label htmlFor="edit-email" className="flex items-center">
                    Email <span className="text-red-500 ml-1">*</span>
                  </Label>
                  <Input
                    id="edit-email"
                    type="email"
                    placeholder="Enter email address"
                    value={selectedPatient.email}
                    onChange={(e) => {
                      setSelectedPatient({ ...selectedPatient, email: e.target.value })
                      setFormErrors({ ...formErrors, email: false })
                    }}
                    className={formErrors.email ? "border-red-500" : ""}
                  />
                  {formErrors.email && <p className="text-red-500 text-sm">Email is required</p>}
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="edit-condition" className="flex items-center">
                  Medical Condition <span className="text-red-500 ml-1">*</span>
                </Label>
                <Input
                  id="edit-condition"
                  placeholder="Enter primary medical condition"
                  value={selectedPatient.condition}
                  onChange={(e) => {
                    setSelectedPatient({ ...selectedPatient, condition: e.target.value })
                    setFormErrors({ ...formErrors, condition: false })
                  }}
                  className={formErrors.condition ? "border-red-500" : ""}
                />
                {formErrors.condition && <p className="text-red-500 text-sm">Medical condition is required</p>}
              </div>
            </div>
          )}
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">Cancel</Button>
            </DialogClose>
            <Button onClick={handleEditPatient}>Save Changes</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <div className="flex items-center space-x-2">
        <Input
          type="search"
          placeholder="Search patients by name or condition..."
          className="max-w-sm"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredPatients.length > 0 ? (
          filteredPatients.map((patient) => (
            <Card key={patient.id} className="overflow-hidden">
              <CardContent className="p-4">
                <div className="space-y-2">
                  <div className="flex justify-between items-start">
                    <h3 className="font-semibold text-lg">{patient.name || `${patient.firstName} ${patient.lastName}`}</h3>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-sm">
                    
                    <div>
                      <span className="font-medium">Age:</span> {patient.age}
                    </div>
                    <div>
                      <span className="font-medium">Gender:</span> {patient.gender}
                    </div>
                    <div>
                      <span className="font-medium">Location:</span> {patient.location}
                    </div>
                    <div className="col-span-2">
                      <span className="font-medium">Email:</span> {patient.email}
                    </div>
                  </div>
                </div>
              </CardContent>
              <CardFooter className="bg-muted/50 p-2 flex justify-between">
                <Link href={`/doctor/patients/${patient.id}`}>
                  <Button variant="default" size="sm">View Details</Button>
                </Link>
              </CardFooter>
            </Card>
          ))
        ) : (
          <div className="col-span-3 text-center py-10">
            <p className="text-muted-foreground">
              No patients found. Try a different search term or add a new patient.
            </p>
          </div>
        )}
      </div>
    </div>
  )
}
