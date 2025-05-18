"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import Link from "next/link"
import { Stethoscope, UserCog } from "lucide-react"
import { useRouter } from "next/navigation"

export default function SignUpPage() {
  const router = useRouter()

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
            <p className="text-sm text-muted-foreground">Choose your role to sign up</p>
          </div>
          <Card>
            <CardContent className="pt-6">
              <div className="space-y-4">
                <h2 className="text-xl font-semibold text-center">I am</h2>
                <div className="flex flex-col md:flex-row justify-center items-center gap-4 max-w-sm mx-auto">
                  <Button
                    className="h-24 w-full md:w-40 flex flex-col gap-2 items-center justify-center"
                    variant="outline"
                    asChild
                  >
                    <Link href="/signup/doctor" className="w-full">
                      <div className="flex flex-col items-center justify-center">
                        <Stethoscope className="h-8 w-8 text-primary" />
                        <span>Doctor</span>
                      </div>
                    </Link>
                  </Button>
                  <Button
                    className="h-24 w-full md:w-40 flex flex-col gap-2 items-center justify-center"
                    variant="outline"
                    asChild
                  >
                    <Link href="/signup/admin" className="w-full">
                      <div className="flex flex-col items-center justify-center">
                        <UserCog className="h-8 w-8 text-red-500" />
                        <span>Administrator</span>
                      </div>
                    </Link>
                  </Button>
                </div>
                <div className="text-center text-sm">
                  <Link href="/" className="text-muted-foreground hover:underline">
                    Back to Sign In
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
