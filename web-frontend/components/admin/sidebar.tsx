"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { Stethoscope, CalendarIcon, UserIcon, LogOutIcon, Users, BarChart3, UserCog } from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

export function Sidebar() {
  const pathname = usePathname()
  const router = useRouter()

  const routes = [
    {
      href: "/admin",
      icon: Stethoscope,
      title: "Dashboard",
    },
    {
      href: "/admin/appointments",
      icon: CalendarIcon,
      title: "Appointments",
    },
    {
      href: "/admin/doctors",
      icon: UserIcon,
      title: "Doctors",
    },
    {
      href: "/admin/patients",
      icon: Users,
      title: "Patients",
    },
    {
      href: "/admin/analysis",
      icon: BarChart3,
      title: "Data Analysis",
    },
    {
      href: "/admin/profile",
      icon: UserCog,
      title: "Profile",
    },
  ]

  const handleLogout = () => {
    // In a real app, you would clear authentication tokens/cookies here
    router.push("/")
  }
  return (
    <div className="fixed h-screen w-[240px] flex flex-col border-r bg-secondary text-secondary-foreground">
      <div className="flex h-14 items-center border-b px-4">
        <Link href="/admin" className="flex items-center gap-2 font-semibold">
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
                "flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium hover:bg-secondary-foreground hover:text-secondary transition-all",
                pathname === route.href ? "bg-secondary-foreground text-secondary" : "transparent",
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
          className="w-full justify-start gap-2 bg-secondary-foreground text-secondary"
          onClick={handleLogout}
        >
          <LogOutIcon className="h-4 w-4" />
          <span>Log Out</span>
        </Button>
      </div>
    </div>
  )
}
