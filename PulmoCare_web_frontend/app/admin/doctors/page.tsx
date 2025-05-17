import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import Link from "next/link"
import { Search } from "lucide-react"

export default function DoctorsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Doctors</h1>
        <p className="text-muted-foreground">Manage doctor schedules and information.</p>
      </div>

      <div className="flex items-center space-x-2">
        <div className="relative flex-1">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input type="search" placeholder="Search doctors..." className="w-full pl-8" />
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {[
          {
            id: 1,
            name: "Dr. John Doe",
            specialty: "Pulmonology, Critical Care",
            patients: 45,
            appointments: 8,
          },
          {
            id: 2,
            name: "Dr. Sarah Wilson",
            specialty: "Pulmonology, Sleep Medicine",
            patients: 38,
            appointments: 6,
          },
          {
            id: 3,
            name: "Dr. Michael Chen",
            specialty: "Pulmonology, Interventional Pulmonology",
            patients: 42,
            appointments: 7,
          },
          {
            id: 4,
            name: "Dr. Emily Rodriguez",
            specialty: "Pulmonology, Asthma Specialist",
            patients: 36,
            appointments: 5,
          },
          {
            id: 5,
            name: "Dr. David Kim",
            specialty: "Pulmonology, Lung Cancer",
            patients: 40,
            appointments: 6,
          },
          {
            id: 6,
            name: "Dr. Lisa Patel",
            specialty: "Pulmonology, Cystic Fibrosis",
            patients: 32,
            appointments: 4,
          },
        ].map((doctor) => (
          <Card key={doctor.id}>
            <CardContent className="p-4">
              <div className="space-y-2">
                <div className="font-semibold">{doctor.name}</div>
                <div className="text-sm text-muted-foreground">{doctor.specialty}</div>
                <div className="flex justify-between text-sm">
                  <span>{doctor.patients} patients</span>
                  <span>{doctor.appointments} appointments today</span>
                </div>
                <div className="flex justify-between pt-2">
<Link href={`/admin/doctors/${doctor.id}`}>
  <Button variant="outline" size="sm">See More</Button>
</Link>
<Link href={`/admin/doctors/schedule/${doctor.id}`}>
  <Button variant="outline" size="sm">View Schedule</Button>
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
