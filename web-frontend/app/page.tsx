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
import { adminApi, doctorApi } from "@/lib/api"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

export default function SignInPage() {
  const router = useRouter()
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [userType, setUserType] = useState("admin") // default to admin
  const [error, setError] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  const handleSignIn = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setIsLoading(true)

    // Basic validation
    if (!email || !password) {
      setError("Please enter both email and password")
      setIsLoading(false)
      return
    }

    try {
      let response;
      if (userType === "admin") {
        response = await adminApi.signin({ email, password })
      } else {
        response = await doctorApi.signin({ email, password })
      }
      
      // Store user info in localStorage
      localStorage.setItem(
        "pulmocare_user",
        JSON.stringify({
          id: response.id,
          email: response.email,
          name: `${response.firstName} ${response.lastName}`,
          type: userType,
        }),
      )

      // Redirect based on user type
      router.push(userType === "admin" ? "/admin" : "/doctor")
    } catch (error: any) {
      setError(error.response?.data?.message || "Invalid email or password")
    } finally {
      setIsLoading(false)
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
                  <Label htmlFor="userType">Sign in as</Label>
                  <Select value={userType} onValueChange={setUserType}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="admin">Administrator</SelectItem>
                      <SelectItem value="doctor">Doctor</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

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

                <Button className="w-full" type="submit" disabled={isLoading}>
                  {isLoading ? "Signing in..." : "Sign In"}
                </Button>
              </form>

              <div className="mt-4 text-center text-sm">
                <p>Don't have an account?</p>
                <div className="flex justify-center gap-2 mt-2">
                  <Link href="/signup/admin">
                    <Button variant="outline" size="sm">Sign Up as Admin</Button>
                  </Link>
                  <Link href="/signup/doctor">
                    <Button variant="outline" size="sm">Sign Up as Doctor</Button>
                  </Link>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
