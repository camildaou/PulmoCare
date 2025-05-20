"use client"

import type React from "react"

import { Sidebar } from "@/components/doctor/sidebar"
import { useEffect } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"

export default function DoctorLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter()

  useEffect(() => {
    // Check if user is logged in and is a doctor
    const userInfo = localStorage.getItem("pulmocare_user")
    if (!userInfo) {
      router.push("/")
      return
    }

    const user = JSON.parse(userInfo)
    if (user.type !== "doctor") {
      router.push("/")
    }
  }, [router])

  return (
    <div className="flex min-h-screen">
      <Sidebar />
      <div className="flex-1 p-8 pt-6 overflow-auto">

        <main>{children}</main>
      </div>
    </div>
  )
}
