"use client"

import type React from "react"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import Image from "next/image"
import { useState, useRef, useEffect } from "react"

export default function DoctorProfilePage() {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [profileImage, setProfileImage] = useState<string | null>(null)
  const [imageError, setImageError] = useState(false)
  const [doctorName, setDoctorName] = useState("John Doe")
  const [formData, setFormData] = useState({
    firstName: "John",
    lastName: "Doe",
    gender: "Male",
    age: "45",
    email: "john.doe@example.com",
    countryCode: "+1",
    phone: "5551234567",
    license: "ML12345678",
    about:
      "Pulmonology specialist with over 15 years of experience in treating respiratory conditions. Special interest in asthma management and COPD.",
    location: "123 Medical Center, New York, NY",
    hospital: "Memorial Hospital, Building A, Floor 3",
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [saved, setSaved] = useState(false)

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
            setFormData((prev) => ({
              ...prev,
              firstName: nameParts[0],
              lastName: nameParts.slice(1).join(" "),
            }))
          }
        }

        // Load email from user data
        if (user.email) {
          setFormData((prev) => ({
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
          setFormData((prev) => ({
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

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
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
                const newProfile = { ...formData, profileImage: imageData }
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
    const requiredFields = ["firstName", "lastName", "email", "phone", "about", "location", "hospital"]
    requiredFields.forEach((field) => {
      if (!formData[field as keyof typeof formData]) {
        newErrors[field] = "This field is required"
      }
    })

    // Validate email
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email address"
    }

    // Validate phone number based on country code
    const phoneNumber = formData.phone.replace(/\D/g, "") // Remove non-digits
    const countryCode = formData.countryCode || "+1"
    const rule = phoneValidationRules[countryCode as keyof typeof phoneValidationRules]
    if (rule && phoneNumber.length !== rule.length) {
      newErrors.phone = `Please enter a valid ${rule.length}-digit phone number for ${countryCode}`
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSave = () => {
    if (validateForm()) {
      try {
        // Update the doctor name in localStorage
        const userInfo = localStorage.getItem("pulmocare_user")
        if (userInfo) {
          const user = JSON.parse(userInfo)
          user.name = `${formData.firstName} ${formData.lastName}`
          user.email = formData.email
          localStorage.setItem("pulmocare_user", JSON.stringify(user))
          setDoctorName(user.name)
        }

        // Save the full profile data
        const profileData = {
          ...formData,
          // Only include profileImage if it exists and hasn't errored
          ...(profileImage && !imageError ? { profileImage } : {}),
        }
        localStorage.setItem("pulmocare_doctor_profile", JSON.stringify(profileData))

        setSaved(true)
        setTimeout(() => setSaved(false), 3000)
      } catch (error) {
        console.error("Error saving profile:", error)
        alert("Failed to save profile. Please try again.")
      }
    }
  }

  const handleImageError = () => {
    setImageError(true)
    clearProfileImage()
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
            <div className="relative w-32 h-32 rounded-full overflow-hidden border-4 border-primary">
              {profileImage && !imageError ? (
                // Use regular img tag for data URLs to avoid Next.js Image optimization issues
                <img
                  src={profileImage || "/placeholder.svg"}
                  alt="Doctor profile"
                  className="w-full h-full object-cover"
                  onError={handleImageError}
                />
              ) : (
                // Use Next.js Image only for the placeholder
                <Image
                  src="/placeholder.svg?height=128&width=128"
                  alt="Doctor profile"
                  width={128}
                  height={128}
                  className="object-cover"
                />
              )}
            </div>
            <div className="text-center">
              <h2 className="text-xl font-bold">Dr. {doctorName}</h2>
              <p className="text-sm text-muted-foreground">Pulmonologist</p>
              <p className="text-xs text-muted-foreground mt-1">{formData.hospital}</p>
            </div>
            <div className="w-full space-y-2">
              <Button variant="outline" className="w-full" onClick={() => fileInputRef.current?.click()}>
                Change Photo
              </Button>
              <input type="file" ref={fileInputRef} className="hidden" accept="image/*" onChange={handleImageChange} />
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
            {saved && (
              <div className="bg-green-50 text-green-600 p-3 rounded-md text-sm">
                Profile information saved successfully!
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName">First Name</Label>
                <Input id="firstName" name="firstName" value={formData.firstName} onChange={handleChange} />
                {errors.firstName && <p className="text-red-500 text-sm mt-1">{errors.firstName}</p>}
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input id="lastName" name="lastName" value={formData.lastName} onChange={handleChange} />
                {errors.lastName && <p className="text-red-500 text-sm mt-1">{errors.lastName}</p>}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="gender">Gender</Label>
                <Input id="gender" name="gender" value={formData.gender} readOnly />
              </div>
              <div className="space-y-2">
                <Label htmlFor="age">Age</Label>
                <Input id="age" name="age" value={formData.age} readOnly />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                className="w-full"
              />
              {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
            </div>

            <div className="grid grid-cols-4 gap-4">
              <div className="space-y-2 col-span-1">
                <Label htmlFor="countryCode">Country Code</Label>
                <Select
                  onValueChange={(value) => setFormData((prev) => ({ ...prev, countryCode: value }))}
                  value={formData.countryCode}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="+1" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="+1">+1 (US/CA)</SelectItem>
                    <SelectItem value="+44">+44 (UK)</SelectItem>
                    <SelectItem value="+33">+33 (FR)</SelectItem>
                    <SelectItem value="+49">+49 (DE)</SelectItem>
                    <SelectItem value="+61">+61 (AU)</SelectItem>
                    <SelectItem value="+91">+91 (IN)</SelectItem>
                    <SelectItem value="+966">+966 (SA)</SelectItem>
                    <SelectItem value="+971">+971 (UAE)</SelectItem>
                    <SelectItem value="+962">+962 (JO)</SelectItem>
                    <SelectItem value="+961">+961 (LB)</SelectItem>
                    <SelectItem value="+20">+20 (EG)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2 col-span-3">
                <Label htmlFor="phone">Phone Number</Label>
                <Input id="phone" name="phone" value={formData.phone} onChange={handleChange} />
                {errors.phone && <p className="text-red-500 text-sm mt-1">{errors.phone}</p>}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="license">Medical License</Label>
              <Input id="license" name="license" value={formData.license} readOnly />
            </div>

            <div className="space-y-2">
              <Label htmlFor="about">About the Doctor</Label>
              <Textarea id="about" name="about" value={formData.about} onChange={handleChange} rows={4} />
              {errors.about && <p className="text-red-500 text-sm mt-1">{errors.about}</p>}
            </div>

            <div className="space-y-2">
              <Label htmlFor="location">Location</Label>
              <Input id="location" name="location" value={formData.location} onChange={handleChange} />
              {errors.location && <p className="text-red-500 text-sm mt-1">{errors.location}</p>}
            </div>

            <div className="space-y-2">
              <Label htmlFor="hospital">Hospital/Clinic</Label>
              <Input id="hospital" name="hospital" value={formData.hospital} onChange={handleChange} />
              {errors.hospital && <p className="text-red-500 text-sm mt-1">{errors.hospital}</p>}
            </div>

            <Button className="w-full" onClick={handleSave}>
              Save Changes
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
