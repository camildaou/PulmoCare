"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import Link from "next/link"
import { Search, Plus } from "lucide-react"
import { useState } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { toast } from "sonner"

export default function PatientsPage() {
  const [patients, setPatients] = useState([
    {
      id: 1,
      name: "Alice Johnson",
      age: 42,
      condition: "Asthma",
      lastVisit: "Mar 25, 2025",
    },
    {
      id: 2,
      name: "Bob Smith",
      age: 56,
      condition: "COPD",
      lastVisit: "Mar 26, 2025",
    },
    {
      id: 3,
      name: "Carol Williams",
      age: 38,
      condition: "Bronchitis",
      lastVisit: "Mar 27, 2025",
    },
    {
      id: 4,
      name: "David Brown",
      age: 62,
      condition: "Pulmonary Fibrosis",
      lastVisit: "Mar 20, 2025",
    },
    {
      id: 5,
      name: "Eve Davis",
      age: 45,
      condition: "Sleep Apnea",
      lastVisit: "Mar 22, 2025",
    },
    {
      id: 6,
      name: "Frank Miller",
      age: 51,
      condition: "Chronic Bronchitis",
      lastVisit: "Mar 25, 2025",
    },
    {
      id: 7,
      name: "Grace Lee",
      age: 34,
      condition: "Asthma",
      lastVisit: "Mar 24, 2025",
    },
    {
      id: 8,
      name: "Henry Wilson",
      age: 67,
      condition: "COPD",
      lastVisit: "Mar 22, 2025",
    },
  ])

  const [searchQuery, setSearchQuery] = useState("")
  const [addPatientOpen, setAddPatientOpen] = useState(false)
  const [newPatient, setNewPatient] = useState({
    name: "",
    age: "",
    gender: "",
    phone: "",
    email: "",
    address: "",
    condition: "",
  })
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

  // Validate the new patient form
  const validatePatientForm = () => {
    const errors = {
      name: !newPatient.name,
      age: !newPatient.age || isNaN(Number(newPatient.age)),
      gender: !newPatient.gender,
      phone: !newPatient.phone,
      email: !newPatient.email,
      condition: !newPatient.condition,
    }

    setFormErrors(errors)
    return !Object.values(errors).some((error) => error)
  }

  // Handle adding a new patient
  const handleAddPatient = () => {
    if (!validatePatientForm()) {
      toast.error("Please fill in all required fields correctly")
      return
    }

    const newPatientObj = {
      id: patients.length + 1,
      name: newPatient.name,
      age: Number(newPatient.age),
      condition: newPatient.condition,
      lastVisit: "New Patient",
      gender: newPatient.gender,
      phone: newPatient.phone,
      email: newPatient.email,
      address: newPatient.address || "Not provided",
    }

    // Add the new patient to the list
    setPatients([...patients, newPatientObj])

    // Reset the form
    setNewPatient({
      name: "",
      age: "",
      gender: "",
      phone: "",
      email: "",
      address: "",
      condition: "",
    })

    // Close the dialog
    setAddPatientOpen(false)

    // Show success message
    toast.success("Patient added successfully")
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
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <Button
          onClick={() => {
            setAddPatientOpen(true)
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
          <Plus className="h-4 w-4 mr-2" />
          Add Patient
        </Button>
      </div>

      {/* Add Patient Dialog */}
      <Dialog open={addPatientOpen} onOpenChange={setAddPatientOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>Add New Patient</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4">
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

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {filteredPatients.map((patient) => (
          <Card key={patient.id}>
            <CardContent className="p-4">
              <div className="space-y-2">
                <div className="font-semibold">{patient.name}</div>
                <div className="text-sm text-muted-foreground">Age: {patient.age}</div>
                <div className="text-sm">Condition: {patient.condition}</div>
                <div className="text-sm">Last Visit: {patient.lastVisit}</div>
                <Link href={`/admin/patients/${patient.id}`} className="w-full">
                  <Button variant="outline" size="sm" className="w-full">
                    View Details
                  </Button>
                </Link>
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
    </div>
  )
}
