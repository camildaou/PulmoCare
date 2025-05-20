"use client"

import type React from "react"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import Image from "next/image"
import { useState, useRef, useEffect } from "react"
import { doctorApi } from "@/lib/api"

interface ProfileData {
  firstName: string
  lastName: string
  gender: string
  age: string
  email: string
  phone: string
  license: string
  about: string
  location: string
  countryCode?: string // Added countryCode to the interface
}

export default function DoctorProfilePage() {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [profileImage, setProfileImage] = useState<string | null>(null)
  const [imageError, setImageError] = useState(false)
  const [doctorName, setDoctorName] = useState("John Doe")
  const [profileData, setProfileData] = useState<ProfileData>(() => ({
    firstName: "",
    lastName: "",
    gender: "",
    age: "",
    email: "",
    phone: "",
    license: "",
    about: "",
    location: "",
    countryCode: "+1", // Default country code
  }))
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [isSaving, setIsSaving] = useState(false)
  const [saveMessage, setSaveMessage] = useState("")
  const [isLoading, setIsLoading] = useState(true)

  // Phone validation rules by country code
  const phoneValidationRules = {
    "+1": { length: 10, pattern: /^\d{10}$/, example: "1234567890" },
    "+44": { length: 10, pattern: /^\d{10}$/, example: "7123456789" },
    "+33": { length: 9, pattern: /^\d{9}$/, example: "612345678" },
    "+49": { length: 11, pattern: /^\d{11}$/, example: "15123456789" },
    "+61": { length: 9, pattern: /^\d{9}$/, example: "412345678" },
    "+91": { length: 10, pattern: /^\d{10}$/, example: "9123456789" },
    "+966": { length: 9, pattern: /^\d{9}$/, example: "512345678" },
    "+971": { length: 9, pattern: /^\d{9}$/, example: "501234567" },
    "+962": { length: 9, pattern: /^\d{9}$/, example: "791234567" },
    "+961": { length: 8, pattern: /^\d{8}$/, example: "71123456" },
    "+20": { length: 10, pattern: /^\d{10}$/, example: "1012345678" },
  }

  // Clear profile image from localStorage
  const clearProfileImage = () => {
    try {
      const profileData = localStorage.getItem("pulmocare_doctor_profile")
      if (profileData) {
        const profile = JSON.parse(profileData)
        delete profile.profileImage
        localStorage.setItem("pulmocare_doctor_profile", JSON.stringify(profile))
      }
    } catch (error) {
      console.error("Error clearing profile image:", error)
    }
  }

  useEffect(() => {
    try {
      // Get user info from localStorage in client component
      const userInfo = localStorage.getItem("pulmocare_user")
      if (userInfo) {
        const user = JSON.parse(userInfo)
        if (user.name) {
          setDoctorName(user.name)
          const nameParts = user.name.split(" ")
          if (nameParts.length >= 2) {
            setProfileData((prev) => ({
              ...prev,
              firstName: nameParts[0],
              lastName: nameParts.slice(1).join(" "),
            }))
          }
        }

        // Load email from user data
        if (user.email) {
          setProfileData((prev) => ({
            ...prev,
            email: user.email,
          }))
        }
      }

      // Load profile data if available
      const profileData = localStorage.getItem("pulmocare_doctor_profile")
      if (profileData) {
        try {
          const profile = JSON.parse(profileData)
          setProfileData((prev) => ({
            ...prev,
            ...profile,
          }))

          // If there's a profile image, load it
          if (profile.profileImage) {
            setProfileImage(profile.profileImage)
          }
        } catch (error) {
          console.error("Error parsing profile data:", error)
          // Clear corrupted data
          localStorage.removeItem("pulmocare_doctor_profile")
        }
      }
    } catch (error) {
      console.error("Error loading profile data:", error)
    }
  }, [])

  useEffect(() => {
    const fetchDoctorProfile = async () => {
      try {
        const userInfo = localStorage.getItem("pulmocare_user")
        if (!userInfo) {
          throw new Error("User information not found. Please log in again.")
        }

        const user = JSON.parse(userInfo)
        const doctorId = user.id

        // Fetch the doctor's profile from the backend
        const doctorProfile = await doctorApi.getProfile(doctorId)

        // Update the form data with the fetched profile
        setProfileData((prev) => ({
          ...prev,
          ...doctorProfile,
        }))

        // If there's a profile image, load it
        if (doctorProfile.profileImage) {
          setProfileImage(doctorProfile.profileImage)
        }
      } catch (error) {
        console.error("Error fetching doctor profile:", error)
        alert("An error occurred while fetching the profile. Please try again.")
      } finally {
        setIsLoading(false)
      }
    }

    fetchDoctorProfile()
  }, [])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setProfileData((prev) => ({ ...prev, [name]: value }))
  }

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      if (file.size > 1024 * 1024 * 2) {
        // 2MB limit
        alert("Image is too large. Please select an image under 2MB.")
        return
      }

      try {
        const reader = new FileReader()
        reader.onloadend = () => {
          try {
            const imageData = reader.result as string
            setProfileImage(imageData)
            setImageError(false)

            // Save the image to localStorage
            try {
              const profileData = localStorage.getItem("pulmocare_doctor_profile")
              if (profileData) {
                const profile = JSON.parse(profileData)
                profile.profileImage = imageData
                localStorage.setItem("pulmocare_doctor_profile", JSON.stringify(profile))
              } else {
                // Create new profile data if it doesn't exist
                const newProfile = { profileImage: imageData };
                localStorage.setItem("pulmocare_doctor_profile", JSON.stringify(newProfile))
              }
            } catch (error) {
              console.error("Error saving to localStorage:", error)
              // If localStorage fails, still show the image in the current session
            }
          } catch (error) {
            console.error("Error saving profile image:", error)
            alert("Failed to save profile image. Please try again.")
          }
        }

        reader.onerror = () => {
          console.error("Error reading file")
          alert("Failed to read image file. Please try again.")
        }

        reader.readAsDataURL(file)
      } catch (error) {
        console.error("Error processing image:", error)
        alert("Failed to process image. Please try again.")
      }
    }
  }

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    // Check required fields
    const requiredFields = ["firstName", "lastName", "email", "phone", "about", "location"]
    requiredFields.forEach((field) => {
      if (!profileData[field as keyof typeof profileData]) {
        newErrors[field] = "This field is required"
      }
    })

    // Validate email
    if (profileData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(profileData.email)) {
      newErrors.email = "Please enter a valid email address"
    }

    // Validate phone number based on country code
    const phoneNumber = profileData.phone.replace(/\D/g, "") // Remove non-digits
    const countryCode = profileData.countryCode || "+1"
    const rule = phoneValidationRules[countryCode as keyof typeof phoneValidationRules]
    if (rule && phoneNumber.length !== rule.length) {
      newErrors.phone = `Please enter a valid ${rule.length}-digit phone number for ${countryCode}`
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  // Ensure the specified fields are editable and bound to the profileData state
  const handleSaveChanges = async () => {
    if (validateForm()) {
      setIsSaving(true)
      setSaveMessage("")

      try {
        const userInfo = localStorage.getItem("pulmocare_user")
        if (!userInfo) {
          throw new Error("User information not found. Please log in again.")
        }

        const user = JSON.parse(userInfo)
        const doctorId = user.id

        // Map 'about' to 'description' in the payload
        const updatedProfileData = {
          ...profileData,
          description: profileData.about, // Map 'about' to 'description'
        };

        await doctorApi.updateProfile(doctorId, updatedProfileData)

        setSaveMessage("Profile updated successfully!")

        // Update localStorage user data
        const updatedUserData = {
          ...JSON.parse(userInfo),
          name: `${profileData.firstName} ${profileData.lastName}`,
        }
        localStorage.setItem("pulmocare_user", JSON.stringify(updatedUserData))

        setTimeout(() => setSaveMessage(""), 3000)
      } catch (error: any) {
        console.error("Error updating profile:", error)
        setSaveMessage(error.response?.data || "Failed to update profile")
      } finally {
        setIsSaving(false)
      }
    }
  }

  const handleImageError = () => {
    setImageError(true)
    clearProfileImage()
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <p>Loading profile...</p>
      </div>
    )
  }

  // Ensure all fields are displayed, but only specified ones are editable
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
                aria-label="Doctor profile"
              />
            </div>
            <div className="text-center">
              <h2 className="text-xl font-bold">
                {profileData.firstName} {profileData.lastName}
              </h2>
              <p className="text-sm text-muted-foreground">Pulmonologist</p>
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
                  name="firstName"
                  value={profileData.firstName}
                  onChange={handleChange}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input
                  id="lastName"
                  name="lastName"
                  value={profileData.lastName}
                  onChange={handleChange}
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
              <Input id="email" type="email" value={profileData.email} readOnly className="bg-gray-50" />
            </div>

            <div className="space-y-2">
              <Label htmlFor="phone">Phone Number</Label>
              <Input
                id="phone"
                name="phone"
                value={profileData.phone}
                onChange={handleChange}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="license">Medical License</Label>
              <Input id="license" value={profileData.license} readOnly className="bg-gray-50" />
            </div>

            <div className="space-y-2">
              <Label htmlFor="about">About</Label>
              <Textarea
                id="about"
                name="about"
                value={profileData.about}
                onChange={handleChange}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <Input
                id="location"
                name="location"
                value={profileData.location}
                onChange={handleChange}
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
