"use client"

import type React from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { BarChart3, Calendar, Home, Users, User, LogOutIcon } from "lucide-react"

interface SidebarProps extends React.HTMLAttributes<HTMLDivElement> {}

export function Sidebar({ className }: SidebarProps) {
  const pathname = usePathname()

  return (
    <div className={cn("pb-12 bg-blue-600 text-white", className)}> {/* Sidebar background blue */}
      <div className="space-y-4 py-4">
        <div className="px-3 py-2">
          <h2 className="mb-2 px-4 text-lg font-semibold tracking-tight">Dashboard</h2>
          <div className="space-y-1">
            {/* Home Link */}
            <Link
              href="/doctor"
              className={cn(
                "flex items-center rounded-md px-3 py-2 text-sm font-medium hover:bg-blue-700 hover:text-white", // Blue hover effect
                pathname === "/doctor" ? "bg-blue-700" : "transparent", // Highlight active link
              )}
            >
              <Home className="mr-2 h-4 w-4" />
              <span>Home</span>
            </Link>
            {/* Appointments Link */}
            <Link
              href="/doctor/appointments"
              className={cn(
                "flex items-center rounded-md px-3 py-2 text-sm font-medium hover:bg-blue-700 hover:text-white",
                pathname === "/doctor/appointments" || pathname.startsWith("/doctor/appointments/") ? "bg-blue-700" : "transparent",
              )}
            >
              <Calendar className="mr-2 h-4 w-4" />
              <span>Appointments</span>
            </Link>
            {/* Patients Link */}
            <Link
              href="/doctor/patients"
              className={cn(
                "flex items-center rounded-md px-3 py-2 text-sm font-medium hover:bg-blue-700 hover:text-white",
                pathname === "/doctor/patients" || pathname.startsWith("/doctor/patients/") ? "bg-blue-700" : "transparent",
              )}
            >
              <Users className="mr-2 h-4 w-4" />
              <span>Patients</span>
            </Link>
            {/* Analysis Link */}
            <Link
              href="/doctor/analysis"
              className={cn(
                "flex items-center rounded-md px-3 py-2 text-sm font-medium hover:bg-blue-700 hover:text-white",
                pathname === "/doctor/analysis" ? "bg-blue-700" : "transparent",
              )}
            >
              <BarChart3 className="mr-2 h-4 w-4" />
              <span>Analysis</span>
            </Link>
            {/* Profile Link */}
            <Link
              href="/doctor/profile"
              className={cn(
                "flex items-center rounded-md px-3 py-2 text-sm font-medium hover:bg-blue-700 hover:text-white",
                pathname === "/doctor/profile" ? "bg-blue-700" : "transparent",
              )}
            >
              <User className="mr-2 h-4 w-4" />
              <span>Profile</span>
            </Link>
      
          </div>
        </div>
        <div className={cn("flex flex-col h-full pb-12", className)}>
  {/* Top section */}
  

  {/* Log Out button at bottom */}
  <div className="mt-auto px-3 py-2">
    <Link
      href="/"
      className={cn(
        "flex items-center rounded-md px-3 py-2 text-sm font-medium hover:bg-blue-700 hover:text-white"
      )}
    >
      <LogOutIcon className="mr-2 h-4 w-4" />
      <span>Log Out</span>
    </Link>
  </div>
</div>

</div>

</div>
  )
}
