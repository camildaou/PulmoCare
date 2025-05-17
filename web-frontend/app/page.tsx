"use client"

import type React from "react"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useState } from "react"
import { useRouter } from "next/navigation"
import { Stethoscope } from "lucide-react"

export default function SignInPage() {
  const router = useRouter()
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")

  const handleSignIn = (e: React.FormEvent) => {
    e.preventDefault()

    // Basic validation
    if (!email || !password) {
      setError("Please enter both email and password")
      return
    }

    // In a real app, you would validate against a database
    // For demo purposes, we'll use some hardcoded values
    const registeredUsers = [
      { email: "doctor@example.com", password: "Doctor123!", type: "doctor", name: "John Doe" },
      { email: "admin@example.com", password: "Admin123!", type: "admin", name: "Jane Smith" },
    ]

    const user = registeredUsers.find((user) => user.email === email)

    if (!user) {
      setError("User not found. Please check your email or sign up.")
      return
    }

    if (user.password !== password) {
      setError("Incorrect password. Please try again.")
      return
    }

    // Store user info in localStorage for demo purposes
    // In a real app, you would use a more secure method like JWT tokens
    localStorage.setItem(
      "pulmocare_user",
      JSON.stringify({
        email: user.email,
        name: user.name,
        type: user.type,
      }),
    )

    // Redirect based on user type
    if (user.type === "doctor") {
      router.push("/doctor")
    } else if (user.type === "admin") {
      router.push("/admin")
    }
  }

  return (
    <div className="flex min-h-screen bg-background">
      <div className="hidden lg:flex lg:w-1/2 bg-primary items-center justify-center">
        <div className="p-12">
          <div className="flex flex-col items-center justify-center space-y-6">
            <div className="rounded-full bg-white p-6">
              <Stethoscope className="h-24 w-24 text-primary" />
            </div>
            <h1 className="text-4xl font-bold text-white text-center">PulmoCare</h1>
            <p className="text-xl text-white text-center max-w-md">
              A comprehensive management system for pulmonologist clinics
            </p>
          </div>
        </div>
      </div>
      <div className="flex flex-1 items-center justify-center">
        <div className="w-full max-w-md p-6">
          <div className="flex flex-col space-y-2 text-center mb-8">
            <div className="flex items-center justify-center">
              <Stethoscope className="h-8 w-8 text-primary mr-2" />
              <h1 className="text-3xl font-bold tracking-tight text-primary">PulmoCare</h1>
            </div>
            <p className="text-sm text-muted-foreground">Pulmonologist Clinic Management System</p>
          </div>
          <Card>
            <CardContent className="pt-6">
              <form onSubmit={handleSignIn} className="space-y-4">
                {error && <div className="p-3 text-sm bg-red-50 text-red-600 rounded-md">{error}</div>}

                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    placeholder="Enter your email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="password">Password</Label>
                  <Input
                    id="password"
                    placeholder="Enter your password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                </div>
                <Button className="w-full bg-primary" type="submit">
                  Sign In
                </Button>
                </form>
                <Button className="w-full bg-primary mt-2" type="submit">
                  <Link href="/signup">Sign Up</Link>
                </Button>
                <div className="text-center text-sm mt-2">
                  <Link href="/terms" className="text-primary hover:underline">
                    Terms and Policies
                  </Link>
                </div>
              
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
