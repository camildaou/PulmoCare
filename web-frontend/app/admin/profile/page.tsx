"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useState, useEffect } from "react"
import { adminApi } from "@/lib/api"

interface ProfileData {
  firstName: string
  lastName: string
  gender: string
  age: string
  email: string
  phone: string
  employeeId: string
  location: string
  password?: string // Optional password field for updates
}

export default function AdminProfilePage() {  const [profileData, setProfileData] = useState<ProfileData>({
    firstName: "",
    lastName: "",
    gender: "",
    age: "",
    email: "",
    phone: "",
    employeeId: "",
    location: "",
  })

  const [isSaving, setIsSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState("")
  const [isLoading, setIsLoading] = useState(true)

  // Load profile data from logged in user
  useEffect(() => {
    const loadProfile = async () => {
      try {
        const userData = localStorage.getItem("pulmocare_user")
        if (!userData) {
          throw new Error("User not found")
        }

        const { id } = JSON.parse(userData)
        const response = await adminApi.getProfile(id) // We'll add this endpoint to our api.ts
        
        setProfileData({
          firstName: response.firstName,
          lastName: response.lastName,
          gender: response.gender,
          age: response.age.toString(),
          email: response.email,
          phone: response.phoneNumber,
          employeeId: response.employeeId,
          location: response.location,
        })
      } catch (e) {
        console.error("Error loading profile data:", e)
      } finally {
        setIsLoading(false)
      }
    }

    loadProfile()
  }, [])
  const handleInputChange = (field: keyof ProfileData, value: string) => {
    setProfileData((prev) => ({
      ...prev,
      [field]: value,
    }))
  }
  const handleSaveChanges = async () => {
    setIsSaving(true)
    setSaveMessage("")

    try {
      // Get the admin ID from localStorage
      const userData = localStorage.getItem("pulmocare_user")
      if (!userData) {
        throw new Error("User data not found")
      }
      const { id } = JSON.parse(userData)

      // Call the update profile endpoint
      await adminApi.updateProfile(id, {
        firstName: profileData.firstName,
        lastName: profileData.lastName,
        gender: profileData.gender,
        age: parseInt(profileData.age),
        phoneNumber: profileData.phone,
        location: profileData.location,
      })

      setSaveMessage("Profile updated successfully!")

      // Update localStorage with new data
      const updatedUserData = {
        ...JSON.parse(userData),
        name: `${profileData.firstName} ${profileData.lastName}`,
      }
      localStorage.setItem("pulmocare_user", JSON.stringify(updatedUserData))

      // Clear success message after 3 seconds
      setTimeout(() => {
        setSaveMessage("")
      }, 3000)
    } catch (error: any) {
      setSaveMessage(error.response?.data || "Failed to update profile")
    } finally {
      setIsSaving(false)
    }
  }
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p>Loading profile...</p>
      </div>
    )
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

            <div className="space-y-2">              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={profileData.email}
                readOnly
                className="bg-gray-50"
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
            </div>            <div className="space-y-2">
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
