"use client"

import React, { useState, useRef, useEffect, ChangeEvent, FormEvent } from "react"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Checkbox } from "@/components/ui/checkbox"
import Link from "next/link"
import { useRouter } from "next/navigation"
import { Stethoscope } from "lucide-react"

export default function DoctorSignUpPage() {
  const router = useRouter()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    dob: "",
    gender: "",
    countryCode: "+1",
    phone: "",
    address: "",
    license: "",
    about: "",
    email: "",
    password: "",
    confirmPassword: "",
    agreeToTerms: false,
  })

  const [profileImage, setProfileImage] = useState<string | null>(null)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [passwordConditions, setPasswordConditions] = useState({
    length: false,
    uppercase: false,
    number: false,
    special: false,
  })

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

  useEffect(() => {
    // Check password conditions on every password change
    const password = formData.password
    setPasswordConditions({
      length: password.length >= 8,
      uppercase: /[A-Z]/.test(password),
      number: /[0-9]/.test(password),
      special: /[!@#$%^&*(),.?":{}|<>]/.test(password),
    })
  }, [formData.password])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev: typeof formData) => ({ ...prev, [name]: value }))
  }

  const handleSelectChange = (name: string, value: string) => {
    setFormData((prev: typeof formData) => ({ ...prev, [name]: value }))
  }

  const handleCheckboxChange = (checked: boolean) => {
    setFormData((prev: typeof formData) => ({ ...prev, agreeToTerms: checked }))
  }

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onloadend = () => {
        setProfileImage(reader.result as string)
      }
      reader.readAsDataURL(file)
    }
  }

  const validateForm = () => {
    const newErrors: Record<string, string> = {}

    // Check required fields
    const requiredFields = [
      "firstName",
      "lastName",
      "dob",
      "gender",
      "phone",
      "address",
      "license",
      "about",
      "email",
      "password",
      "confirmPassword",
    ]
    requiredFields.forEach((field) => {
      if (!formData[field as keyof typeof formData]) {
        newErrors[field] = "This field is required"
      }
    })

    // Validate date of birth
    if (formData.dob) {
      const dobDate = new Date(formData.dob)
      const today = new Date()
      if (dobDate > today) {
        newErrors.dob = "Date of birth cannot be in the future"
      }

      // Check if doctor is at least 18 years old
      const eighteenYearsAgo = new Date()
      eighteenYearsAgo.setFullYear(today.getFullYear() - 18)
      if (dobDate > eighteenYearsAgo) {
        newErrors.dob = "Doctor must be at least 18 years old"
      }
    }

    // Validate phone number based on country code
    if (formData.phone) {
      const rule = phoneValidationRules[formData.countryCode as keyof typeof phoneValidationRules]
      if (rule && !rule.pattern.test(formData.phone)) {
        newErrors.phone = `Please enter a valid ${rule.length}-digit phone number for ${formData.countryCode}`
      }
    }

    // Validate email
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email address"
    }

    // Validate password
    if (formData.password) {
      const conditions = []
      if (!passwordConditions.length) conditions.push("at least 8 characters")
      if (!passwordConditions.uppercase) conditions.push("at least one uppercase letter")
      if (!passwordConditions.number) conditions.push("at least one number")
      if (!passwordConditions.special) conditions.push("at least one special character")

      if (conditions.length > 0) {
        newErrors.password = `Password must contain ${conditions.join(", ")}`
      }
    }

    // Validate password confirmation
    if (formData.password && formData.confirmPassword && formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = "Passwords do not match"
    }

    // Check terms agreement
    if (!formData.agreeToTerms) {
      newErrors.agreeToTerms = "You must agree to the terms and policies"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (validateForm()) {
      // In a real app, you would send this data to your backend
      console.log("Form submitted:", formData)

      // Format phone with country code for display
      const formattedPhone = `${formData.countryCode} ${formData.phone}`

      // Store user info in localStorage for demo purposes
      localStorage.setItem(
        "pulmocare_user",
        JSON.stringify({
          email: formData.email,
          name: `${formData.firstName} ${formData.lastName}`,
          type: "doctor",
        }),
      )

      // Store complete profile data
      const profileData = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        gender: formData.gender,
        age: calculateAge(formData.dob),
        email: formData.email,
        countryCode: formData.countryCode,
        phone: formData.phone,
        license: formData.license,
        about: formData.about,
        location: formData.address,
        profileImage: profileImage,
      }

      localStorage.setItem("pulmocare_doctor_profile", JSON.stringify(profileData))

      // Initialize pending reports count
      localStorage.setItem("pulmocare_pending_reports", "3")

      // Redirect to doctor portal
      router.push("/doctor")
    }
  }

  // Helper function to calculate age from date of birth
  const calculateAge = (dob: string): string => {
    const birthDate = new Date(dob)
    const today = new Date()
    let age = today.getFullYear() - birthDate.getFullYear()
    const monthDiff = today.getMonth() - birthDate.getMonth()

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--
    }

    return age.toString()
  }

  return (
    <div className="flex min-h-screen bg-background">
      <div className="hidden lg:flex lg:w-1/2 bg-primary items-center justify-center">
        <div className="p-12">
          <div className="flex flex-col items-center justify-center space-y-6">
            <div className="rounded-full bg-white p-6 flex items-center justify-center">
              <Stethoscope className="h-24 w-24 text-primary" />
            </div>
            <h1 className="text-4xl font-bold text-white text-center">PulmoCare</h1>
            <p className="text-xl text-white text-center max-w-md">Join our network of pulmonology specialists</p>
          </div>
        </div>
      </div>
      <div className="flex flex-1 items-center justify-center overflow-auto py-8">
        <div className="w-full max-w-3xl p-6">
          <div className="flex flex-col space-y-2 text-center mb-6">
            <div className="flex items-center justify-center">
              <Stethoscope className="h-8 w-8 text-primary mr-2" />
              <h1 className="text-3xl font-bold tracking-tight text-primary">PulmoCare</h1>
            </div>
            <p className="text-sm text-muted-foreground">Doctor Registration</p>
          </div>
          <Card>
            <CardContent className="pt-6">
              <form className="space-y-4" onSubmit={handleSubmit}>
                <div className="flex justify-center mb-4">
                  <div className="relative">
                    <div
                      className="w-24 h-24 rounded-full overflow-hidden border-2 border-primary bg-muted flex items-center justify-center cursor-pointer"
                      onClick={() => fileInputRef.current?.click()}
                    >
                      {profileImage ? (
                        <div className="w-full h-full flex items-center justify-center">
                          <div
                            className="w-full h-full bg-center bg-cover"
                            style={{ backgroundImage: `url(${profileImage})` }}
                          />
                        </div>
                      ) : (
                        <Stethoscope className="h-12 w-12 text-primary opacity-50" />
                      )}
                    </div>
                    <input
                      type="file"
                      ref={fileInputRef}
                      className="hidden"
                      accept="image/*"
                      onChange={handleImageChange}
                    />
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      className="absolute bottom-0 right-0"
                      onClick={() => fileInputRef.current?.click()}
                    >
                      Add
                    </Button>
                  </div>
                </div>

                <div className="grid grid-cols-4 gap-4">
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="firstName">First Name</Label>
                    <Input
                      id="firstName"
                      name="firstName"
                      placeholder="First Name"
                      value={formData.firstName}
                      onChange={handleChange}
                    />
                    {errors.firstName && <p className="text-sm text-red-500 mt-1">{errors.firstName}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="lastName">Last Name</Label>
                    <Input
                      id="lastName"
                      name="lastName"
                      placeholder="Last Name"
                      value={formData.lastName}
                      onChange={handleChange}
                    />
                    {errors.lastName && <p className="text-sm text-red-500 mt-1">{errors.lastName}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="dob">Date of Birth</Label>
                    <Input id="dob" name="dob" type="date" value={formData.dob} onChange={handleChange} />
                    {errors.dob && <p className="text-sm text-red-500 mt-1">{errors.dob}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="gender">Gender</Label>
                    <Select onValueChange={(value: string) => handleSelectChange("gender", value)} value={formData.gender}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select gender" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="male">Male</SelectItem>
                        <SelectItem value="female">Female</SelectItem>
                        <SelectItem value="other">Other</SelectItem>
                      </SelectContent>
                    </Select>
                    {errors.gender && <p className="text-sm text-red-500 mt-1">{errors.gender}</p>}
                  </div>
                  <div className="space-y-2 col-span-1">
                    <Label htmlFor="countryCode">Country Code</Label>
                    <Select onValueChange={(value: string) => handleSelectChange("countryCode", value)} value={formData.countryCode}>
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
                    {errors.countryCode && <p className="text-sm text-red-500 mt-1">{errors.countryCode}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="phone">Phone Number</Label>
                    <Input
                      id="phone"
                      name="phone"
                      placeholder="Phone Number"
                      value={formData.phone}
                      onChange={handleChange}
                    />
                    {errors.phone && <p className="text-sm text-red-500 mt-1">{errors.phone}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="address">Address</Label>
                    <Input
                      id="address"
                      name="address"
                      placeholder="Address"
                      value={formData.address}
                      onChange={handleChange}
                    />
                    {errors.address && <p className="text-sm text-red-500 mt-1">{errors.address}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="license">Medical License Number</Label>
                    <Input
                      id="license"
                      name="license"
                      placeholder="Medical License Number"
                      value={formData.license}
                      onChange={handleChange}
                    />
                    {errors.license && <p className="text-sm text-red-500 mt-1">{errors.license}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="about">About You</Label>
                    <Input
                      id="about"
                      name="about"
                      placeholder="Tell us about yourself"
                      value={formData.about}
                      onChange={handleChange}
                    />
                    {errors.about && <p className="text-sm text-red-500 mt-1">{errors.about}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      name="email"
                      placeholder="Email"
                      value={formData.email}
                      onChange={handleChange}
                    />
                    {errors.email && <p className="text-sm text-red-500 mt-1">{errors.email}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="password">Password</Label>
                    <Input
                      id="password"
                      name="password"
                      type="password"
                      placeholder="Password"
                      value={formData.password}
                      onChange={handleChange}
                    />
                    {errors.password && <p className="text-sm text-red-500 mt-1">{errors.password}</p>}
                  </div>
                  <div className="space-y-2 col-span-2">
                    <Label htmlFor="confirmPassword">Confirm Password</Label>
                    <Input
                      id="confirmPassword"
                      name="confirmPassword"
                      type="password"
                      placeholder="Confirm Password"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                    />
                    {errors.confirmPassword && <p className="text-sm text-red-500 mt-1">{errors.confirmPassword}</p>}
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="agreeToTerms"
                    checked={formData.agreeToTerms}
                    onCheckedChange={handleCheckboxChange}
                  />
                  <Label htmlFor="agreeToTerms" className="text-sm">
                    I agree to the{" "}
                    <Link href="/terms" className="text-primary underline">
                      terms and conditions
                    </Link>{" "}
                    and{" "}
                    <Link href="/privacy" className="text-primary underline">
                      privacy policy
                    </Link>
                  </Label>
                </div>
                {errors.agreeToTerms && <p className="text-sm text-red-500 mt-1">{errors.agreeToTerms}</p>}
                <Button type="submit" className="w-full">
                  Register
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
