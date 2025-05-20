"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import Link from "next/link"
import { Search } from "lucide-react"
import { useEffect, useState } from "react"
import { doctorApi } from "@/lib/api"
import { Doctor } from "@/lib/types"

export default function DoctorsPage() {
  const [doctors, setDoctors] = useState<Doctor[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")

  useEffect(() => {
    const fetchDoctors = async () => {
      try {
        const response = await doctorApi.getAllDoctors()
        setDoctors(response)
      } catch (error) {
        console.error("Error fetching doctors:", error)
      } finally {
        setIsLoading(false)
      }
    }

    fetchDoctors()
  }, [])

  const filteredDoctors = doctors.filter((doctor) => {
    const query = searchQuery.toLowerCase()
    return (
      doctor.firstName.toLowerCase().includes(query) ||
      doctor.lastName.toLowerCase().includes(query) ||
      (doctor.location && doctor.location.toLowerCase().includes(query))
    )
  })

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value)
  }

  if (isLoading) {
    return <p>Loading doctors...</p>
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Doctors</h1>
        <p className="text-muted-foreground">Manage doctor schedules and information.</p>
      </div>

      <div className="flex items-center space-x-2">
        <div className="relative flex-1">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search doctors..."
            className="w-full pl-8"
            value={searchQuery}
            onChange={handleSearchChange}
          />
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {filteredDoctors.map((doctor) => (
          <Card key={doctor.id}>
            <CardContent className="p-4">
              <div className="space-y-2">
                <div className="font-semibold">
                  {doctor.firstName} {doctor.lastName}
                </div>
                <div className="text-sm text-muted-foreground">
                  {doctor.description || "Specialty not provided"}
                </div>
               
               {/* <div className="flex justify-between text-sm">
                  <span>{doctor.patients || 0} patients</span>
                  <span>{doctor.appointments || 0} appointments today</span>
                </div> */}
                
                <div className="flex justify-between pt-2">
                  <Link href={`/admin/doctors/${doctor.id}`}>
                    <Button variant="outline" size="sm">
                      See More
                    </Button>
                  </Link>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
}
