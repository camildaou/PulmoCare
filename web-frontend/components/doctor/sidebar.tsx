"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { Stethoscope, CalendarIcon, Users, BarChart3, User, LogOutIcon } from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

export function Sidebar() {
  const pathname = usePathname()
  const router = useRouter()

  const routes = [
    {
      href: "/doctor",
      icon: Stethoscope,
      title: "Dashboard",
    },
    {
      href: "/doctor/schedule",
      icon: CalendarIcon,
      title: "My Schedule",
    },
    {
      href: "/doctor/appointments",
      icon: CalendarIcon,
      title: "Appointments",
    },
    {
      href: "/doctor/patients",
      icon: Users,
      title: "Patients",
    },
    {
      href: "/doctor/analysis",
      icon: BarChart3,
      title: "Analysis",
    },
    {
      href: "/doctor/profile",
      icon: User,
      title: "Profile",
    },

  ]

  const handleLogout = () => {
    router.push("/")
  }

  return (
    <div className="flex h-screen w-[240px] flex-col border-r bg-blue-800 text-white">
      <div className="flex h-14 items-center border-b px-4">
        <Link href="/doctor" className="flex items-center gap-2 font-semibold">
          <Stethoscope className="h-6 w-6" />
          <span>PulmoCare</span>
        </Link>
      </div>
      <div className="flex-1 overflow-auto py-2">
        <nav className="grid gap-1 px-2">
          {routes.map((route) => (
            <Link
              key={route.href}
              href={route.href}
              className={cn(
                "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium hover:bg-white hover:text-blue-800 transition-all",
                pathname === route.href ? "bg-white text-blue-800" : "transparent",
              )}
            >
              <route.icon className="h-5 w-5" />
              {route.title}
            </Link>
          ))}
        </nav>
      </div>
      <div className="mt-auto p-4">
        <Button
          variant="outline"
          className="w-full justify-start gap-2 bg-white text-blue-800 hover:bg-blue-700 hover:text-white"
          onClick={handleLogout}
        >
          <LogOutIcon className="h-4 w-4" />
          <span>Log Out</span>
        </Button>
      </div>
    </div>
  )
}
