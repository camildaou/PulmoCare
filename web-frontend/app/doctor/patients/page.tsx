"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import Link from "next/link"
import { toast } from "sonner"

// Mock data for patients
const initialPatients = [
  {
    id: "1",
    name: "Alice Johnson",
    age: 42,
    gender: "Female",
    phone: "555-123-4567",
    email: "alice@example.com",
    address: "123 Main St, Anytown, USA",
    condition: "Asthma",
    lastVisit: "Today",
  },
  {
    id: "2",
    name: "Bob Smith",
    age: 35,
    gender: "Male",
    phone: "555-234-5678",
    email: "bob@example.com",
    address: "456 Oak Ave, Somewhere, USA",
    condition: "COPD",
    lastVisit: "Yesterday",
  },
  {
    id: "3",
    name: "Carol Williams",
    age: 28,
    gender: "Female",
    phone: "555-345-6789",
    email: "carol@example.com",
    address: "789 Pine Rd, Nowhere, USA",
    condition: "Bronchitis",
    lastVisit: "Mar 25, 2025",
  },
  {
    id: "4",
    name: "David Brown",
    age: 50,
    gender: "Male",
    phone: "555-456-7890",
    email: "david@example.com",
    address: "101 Elm St, Elsewhere, USA",
    condition: "Emphysema",
    lastVisit: "Mar 20, 2025",
  },
  {
    id: "5",
    name: "Eve Davis",
    age: 33,
    gender: "Female",
    phone: "555-567-8901",
    email: "eve@example.com",
    address: "202 Maple Dr, Anywhere, USA",
    condition: "Asthma",
    lastVisit: "Mar 15, 2025",
  },
  {
    id: "6",
    name: "Frank Miller",
    age: 45,
    gender: "Male",
    phone: "555-678-9012",
    email: "frank@example.com",
    address: "303 Cedar Ln, Someplace, USA",
    condition: "Chronic Bronchitis",
    lastVisit: "Mar 10, 2025",
  },
  {
    id: "7",
    name: "Grace Lee",
    age: 38,
    gender: "Female",
    phone: "555-789-0123",
    email: "grace@example.com",
    address: "404 Birch Blvd, Othertown, USA",
    condition: "Asthma",
    lastVisit: "Mar 5, 2025",
  },
  {
    id: "8",
    name: "Henry Wilson",
    age: 55,
    gender: "Male",
    phone: "555-890-1234",
    email: "henry@example.com",
    address: "505 Walnut Way, Thisplace, USA",
    condition: "COPD",
    lastVisit: "Mar 1, 2025",
  },
]

export default function DoctorPatientsPage() {
  const [patients, setPatients] = useState(initialPatients)
  const [searchQuery, setSearchQuery] = useState("")
  const [addPatientDialogOpen, setAddPatientDialogOpen] = useState(false)
  const [editPatientDialogOpen, setEditPatientDialogOpen] = useState(false)
  const [selectedPatient, setSelectedPatient] = useState(null)
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
    name: false,
    age: false,
    gender: false,
    phone: false,
    email: false,
    condition: false,
  })

  // Filter patients based on search query
  const filteredPatients = patients.filter(
    (patient) =>
      patient.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      patient.condition.toLowerCase().includes(searchQuery.toLowerCase()),
  )

  // Validate the form
  const validateForm = (patient) => {
    const errors = {
      name: !patient.name,
      age: !patient.age || isNaN(Number(patient.age)),
      gender: !patient.gender,
      phone: !patient.phone,
      email: !patient.email,
      condition: !patient.condition,
    }

    setFormErrors(errors)
    return !Object.values(errors).some((error) => error)
  }

  // Handle adding a new patient
  const handleAddPatient = () => {
    if (!validateForm(newPatient)) {
      toast.error("Please fill in all required fields correctly")
      return
    }

    const newPatientObj = {
      id: (patients.length + 1).toString(),
      ...newPatient,
      age: Number(newPatient.age),
      lastVisit: "Never",
    }

    setPatients([...patients, newPatientObj])
    setAddPatientDialogOpen(false)
    toast.success("Patient added successfully")

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

  // Handle editing a patient
  const handleEditPatient = () => {
    if (!validateForm(selectedPatient)) {
      toast.error("Please fill in all required fields correctly")
      return
    }

    const updatedPatients = patients.map((patient) =>
      patient.id === selectedPatient.id ? { ...selectedPatient, age: Number(selectedPatient.age) } : patient,
    )

    setPatients(updatedPatients)
    setEditPatientDialogOpen(false)
    toast.success("Patient updated successfully")
  }

  // Open edit dialog with patient data
  const openEditDialog = (patient) => {
    setSelectedPatient({ ...patient })
    setEditPatientDialogOpen(true)
    setFormErrors({
      name: false,
      age: false,
      gender: false,
      phone: false,
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
            setAddPatientDialogOpen(true)
            setFormErrors({
              name: false,
              age: false,
              gender: false,
              phone: false,
              email: false,
              condition: false,
            })
          }}
        >
          Add Patient
        </Button>
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
                  value={newPatient.age}
                  onChange={(e) => {
                    setNewPatient({ ...newPatient, age: e.target.value })
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
                value={newPatient.gender}
                onValueChange={(value) => {
                  setNewPatient({ ...newPatient, gender: value })
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
                  value={newPatient.phone}
                  onChange={(e) => {
                    setNewPatient({ ...newPatient, phone: e.target.value })
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
                  value={newPatient.email}
                  onChange={(e) => {
                    setNewPatient({ ...newPatient, email: e.target.value })
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
                  setFormErrors({ ...formErrors, condition: false })
                }}
                className={formErrors.condition ? "border-red-500" : ""}
              />
              {formErrors.condition && <p className="text-red-500 text-sm">Medical condition is required</p>}
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
                  <Label htmlFor="edit-name" className="flex items-center">
                    Full Name <span className="text-red-500 ml-1">*</span>
                  </Label>
                  <Input
                    id="edit-name"
                    placeholder="Enter patient name"
                    value={selectedPatient.name}
                    onChange={(e) => {
                      setSelectedPatient({ ...selectedPatient, name: e.target.value })
                      setFormErrors({ ...formErrors, name: false })
                    }}
                    className={formErrors.name ? "border-red-500" : ""}
                  />
                  {formErrors.name && <p className="text-red-500 text-sm">Name is required</p>}
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
                      setSelectedPatient({ ...selectedPatient, age: e.target.value })
                      setFormErrors({ ...formErrors, age: false })
                    }}
                    className={formErrors.age ? "border-red-500" : ""}
                  />
                  {formErrors.age && <p className="text-red-500 text-sm">Valid age is required</p>}
                </div>
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
                  <Label htmlFor="edit-phone" className="flex items-center">
                    Phone Number <span className="text-red-500 ml-1">*</span>
                  </Label>
                  <Input
                    id="edit-phone"
                    placeholder="Enter phone number"
                    value={selectedPatient.phone}
                    onChange={(e) => {
                      setSelectedPatient({ ...selectedPatient, phone: e.target.value })
                      setFormErrors({ ...formErrors, phone: false })
                    }}
                    className={formErrors.phone ? "border-red-500" : ""}
                  />
                  {formErrors.phone && <p className="text-red-500 text-sm">Phone number is required</p>}
                </div>
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
                <Label htmlFor="edit-address">Address</Label>
                <Input
                  id="edit-address"
                  placeholder="Enter address (optional)"
                  value={selectedPatient.address}
                  onChange={(e) => setSelectedPatient({ ...selectedPatient, address: e.target.value })}
                />
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
                    <h3 className="font-semibold text-lg">{patient.name}</h3>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-sm">
                    <div>
                      <span className="font-medium">Age:</span> {patient.age}
                    </div>
                    <div>
                      <span className="font-medium">Gender:</span> {patient.gender}
                    </div>
                    <div className="col-span-2">
                      <span className="font-medium">Condition:</span> {patient.condition}
                    </div>
                    <div className="col-span-2">
                      <span className="font-medium">Last Visit:</span> {patient.lastVisit}
                    </div>
                  </div>
                </div>
              </CardContent>
              <CardFooter className="bg-muted/50 p-2 flex justify-between">
                <Button variant="outline" size="sm" onClick={() => openEditDialog(patient)}>
                  Edit
                </Button>
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
