"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useState, useEffect } from "react"

export default function AdminProfilePage() {
  const [profileData, setProfileData] = useState({
    firstName: "Jane",
    lastName: "Smith",
    gender: "Female",
    age: "35",
    email: "jane.smith@example.com",
    phone: "+1 (555) 987-6543",
    employeeId: "ADM12345",
    location: "123 Medical Center, New York, NY",
  })

  const [isSaving, setIsSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState("")

  // Load profile data from localStorage if available
  useEffect(() => {
    const savedProfile = localStorage.getItem("adminProfile")
    if (savedProfile) {
      try {
        setProfileData(JSON.parse(savedProfile))
      } catch (e) {
        console.error("Error loading profile data:", e)
      }
    }
  }, [])

  const handleInputChange = (field, value) => {
    setProfileData((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleSaveChanges = () => {
    setIsSaving(true)

    // Simulate API call
    setTimeout(() => {
      // Save to localStorage
      localStorage.setItem("adminProfile", JSON.stringify(profileData))

      setIsSaving(false)
      setSaveMessage("Profile updated successfully!")

      // Clear message after 3 seconds
      setTimeout(() => {
        setSaveMessage("")
      }, 3000)
    }, 800)
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Profile</h1>
        <p className="text-muted-foreground">Manage your personal information.</p>
      </div>

      <div className="grid gap-6 md:grid-cols-[300px_1fr]">
        <Card>
          <CardContent className="p-6 flex flex-col items-center gap-4">
            <div className="relative w-32 h-32 rounded-full overflow-hidden border-4 border-secondary">
              <div
                className="w-full h-full bg-center bg-cover"
                style={{ backgroundImage: `url('/placeholder.svg?height=128&width=128')` }}
                aria-label="Admin profile"
              />
            </div>
            <div className="text-center">
              <h2 className="text-xl font-bold">
                {profileData.firstName} {profileData.lastName}
              </h2>
              <p className="text-sm text-muted-foreground">Administrator</p>
            </div>
            <div className="w-full space-y-2">
              <Button variant="outline" className="w-full">
                Change Photo
              </Button>
              <Button variant="outline" className="w-full">
                Change Password
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Personal Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName">First Name</Label>
                <Input
                  id="firstName"
                  value={profileData.firstName}
                  onChange={(e) => handleInputChange("firstName", e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input
                  id="lastName"
                  value={profileData.lastName}
                  onChange={(e) => handleInputChange("lastName", e.target.value)}
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="gender">Gender</Label>
                <Input id="gender" value={profileData.gender} readOnly />
              </div>
              <div className="space-y-2">
                <Label htmlFor="age">Age</Label>
                <Input id="age" value={profileData.age} readOnly />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={profileData.email}
                onChange={(e) => handleInputChange("email", e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="phone">Phone Number</Label>
              <Input
                id="phone"
                value={profileData.phone}
                onChange={(e) => handleInputChange("phone", e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="employeeId">Employee ID</Label>
              <Input id="employeeId" value={profileData.employeeId} readOnly />
            </div>

            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <Input
                id="location"
                value={profileData.location}
                onChange={(e) => handleInputChange("location", e.target.value)}
              />
            </div>

            {saveMessage && <div className="p-2 bg-green-100 text-green-800 rounded-md text-center">{saveMessage}</div>}

            <Button className="w-full" onClick={handleSaveChanges} disabled={isSaving}>
              {isSaving ? "Saving..." : "Save Changes"}
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
