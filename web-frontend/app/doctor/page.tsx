"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { useState, useEffect } from "react"
import Link from "next/link"
import { appointmentsApi } from "@/lib/api"
import { scheduleApi } from "@/lib/api"
import { Skeleton } from "@/components/ui/skeleton"
import { Calendar } from "@/components/ui/calendar"

export default function DoctorDashboard() {
  const [doctorName, setDoctorName] = useState("Doctor")
  const [doctorId, setDoctorId] = useState<string | null>(null)
  const [pendingReports, setPendingReports] = useState(3)
  const [ongoingAppointment, setOngoingAppointment] = useState<any | null>(null)
  const [todaysAppointments, setTodaysAppointments] = useState<any[]>([])
  const [upcomingAppointments, setUpcomingAppointments] = useState<any[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [weeklySchedule, setWeeklySchedule] = useState<any[]>([])
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(undefined)
  const [showSchedule, setShowSchedule] = useState(false)
  
  // Days of the week starting from Monday (1) to Sunday (7)
  const daysOfWeek = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
  // Available time slots (30-minute intervals from 8:00 to 17:30)
  const timeSlots = Array.from({ length: 20 }, (_, i) => {
    const hour = Math.floor(i / 2) + 8
    const minute = (i % 2) * 30
    return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
  })
  const [isClient, setIsClient] = useState(false);

  // Use this effect to handle client-side initialization
  useEffect(() => {
    setIsClient(true);
    
    // Get user info from localStorage in client component
    const userInfo = localStorage.getItem("pulmocare_user")
    if (userInfo) {
      const user = JSON.parse(userInfo)
      if (user.name) {
        setDoctorName(`Dr. ${user.name}`)
      }
      if (user.id) {
        setDoctorId(user.id)
      }
    }

    // Get pending reports count from localStorage
    const pendingReportsData = localStorage.getItem("pulmocare_pending_reports")
    if (pendingReportsData) {
      setPendingReports(Number.parseInt(pendingReportsData))
    } else {      // Initialize with default value if not set
      localStorage.setItem("pulmocare_pending_reports", "3")
    }
  }, []);
    useEffect(() => {
    // Only run this effect if we have a doctorId and are on the client
    if (doctorId && isClient) {
      setIsLoading(true)
      
      // Fetch ongoing appointment
      const fetchOngoingAppointment = async () => {
        const appointment = await appointmentsApi.getOngoingAppointment(doctorId)
        setOngoingAppointment(appointment)
      }
      
      // Fetch today's appointments
      const fetchTodaysAppointments = async () => {
        const appointments = await appointmentsApi.getTodaysAppointments(doctorId)
        setTodaysAppointments(appointments || [])
      }
      
      // Fetch upcoming appointments
      const fetchUpcomingAppointments = async () => {
        const appointments = await appointmentsApi.getUpcomingAppointmentsByDoctor(doctorId)
        setUpcomingAppointments(appointments || [])
      }
        // Fetch doctor's availability 
      const fetchDoctorData = async () => {
        try {
          // Fetch all data in parallel
          const [ongoingAppointmentData, todaysAppointmentsData, upcomingAppointmentsData, availabilityData] = 
            await Promise.all([
              appointmentsApi.getOngoingAppointment(doctorId),
              appointmentsApi.getTodaysAppointments(doctorId),
              appointmentsApi.getUpcomingAppointmentsByDoctor(doctorId),
              scheduleApi.getDoctorAvailability(doctorId)
            ]);
          
          // Set the state with fetched data
          setOngoingAppointment(ongoingAppointmentData);
          setTodaysAppointments(todaysAppointmentsData || []);
          setUpcomingAppointments(upcomingAppointmentsData || []);
          
          // Now process the weekly schedule with all the appointment data
          console.log("Today's appointments:", todaysAppointmentsData);
          console.log("Upcoming appointments:", upcomingAppointmentsData);
          
          const schedule = processWeeklySchedule(availabilityData, todaysAppointmentsData || [], upcomingAppointmentsData || []);
          setWeeklySchedule(schedule);
        } catch (error) {
          console.error('Error fetching data:', error);
        } finally {
          setIsLoading(false);
        }
      };
        // Fetch all data
      fetchDoctorData();
    }
  }, [doctorId, isClient])
  
  // Format time for display (e.g., "09:30" -> "9:30 AM")
  const formatTimeForDisplay = (time: string) => {
    if (!time) return ''
    
    const [hours, minutes] = time.split(':')
    const hour = parseInt(hours, 10)
    const ampm = hour >= 12 ? 'PM' : 'AM'
    const displayHour = hour % 12 || 12
    
    return `${displayHour}:${minutes} ${ampm}`
  }
  
  // Format elapsed time for the ongoing appointment
  const getElapsedTime = (startTime: string) => {
    if (!startTime) return ''
    
    const [hours, minutes] = startTime.split(':')
    const appointmentTime = new Date()
    appointmentTime.setHours(parseInt(hours, 10), parseInt(minutes, 10), 0, 0)
    
    const now = new Date()
    const elapsedMs = now.getTime() - appointmentTime.getTime()
    const elapsedMinutes = Math.floor(elapsedMs / (1000 * 60))
    
    return `${elapsedMinutes} minutes ago`
  }  // Process the availability data from the API to create a weekly schedule view
  const processWeeklySchedule = (availability: any, todaysAppointmentsData: any[] = [], upcomingAppointmentsData: any[] = []) => {
    if (!availability || !availability.availableTimeSlots) {
      console.error("No availability data or time slots found");
      return []
    }
    
    console.log("Availability data:", JSON.stringify(availability));
    console.log("Available days from API:", availability.availableDays);
    console.log("Available time slots structure:", Object.keys(availability.availableTimeSlots));
    console.log("Today's appointments:", todaysAppointmentsData);
    console.log("Upcoming appointments:", upcomingAppointmentsData);
      // Create a map to store appointments by day and time
    const appointmentMap: Record<string, Record<string, any>> = {}
    
    // Pre-populate the appointmentMap with today's appointments and upcoming appointments
    const allAppointments = [...todaysAppointmentsData, ...upcomingAppointmentsData]
    console.log("Processing appointments for schedule, total appointments:", allAppointments.length);
    
    // Initialize empty records for each day of the week
    daysOfWeek.forEach(day => {
      appointmentMap[day] = {};
    });
    
    allAppointments.forEach(appointment => {
      // Debug each appointment to understand its structure and date format
      console.log("Appointment details:", {
        id: appointment.id,
        date: appointment.date, 
        hour: appointment.hour,
        formatCheck: typeof appointment.hour === 'string' ? 
          `Format valid: ${/^([01]?[0-9]|2[0-3]):[0-5][0-9](:[0-5][0-9])?$/.test(appointment.hour)}` : 
          'Not a string',
        upcoming: appointment.upcoming,
        patientName: appointment.patient?.firstName
      });

      // Format hour to HH:MM format if it comes as HH:MM:SS
      let formattedHour = appointment.hour;
      if (appointment.hour && appointment.hour.includes(':') && appointment.hour.split(':').length === 3) {
        // If format is HH:MM:SS, convert to HH:MM
        formattedHour = appointment.hour.split(':').slice(0, 2).join(':');
        console.log(`Converting time format from ${appointment.hour} to ${formattedHour}`);
      }

      // The "upcoming" field might not be reliable - let's determine if it's upcoming based on the date
      const appointmentDate = new Date(appointment.date);
      const currentDate = new Date();
      currentDate.setHours(0, 0, 0, 0); // Set to start of day for date comparison

      // Get the day name (Monday, Tuesday, etc.) based on the date
      // Find the day of the week based on the date
      const dayIndex = appointmentDate.getDay(); // 0 for Sunday, 1 for Monday, etc.
      // Adjust to match our daysOfWeek array (which starts with Monday)
      const adjustedDayIndex = dayIndex === 0 ? 6 : dayIndex - 1;
      const dayName = daysOfWeek[adjustedDayIndex];
      
      console.log("Appointment date:", appointment.date, "Day of week:", dayName, "Day index:", dayIndex, "Adjusted index:", adjustedDayIndex);
      
      // Only process if it's today or in the future
      if (appointmentDate >= currentDate) {
        if (!appointmentMap[dayName]) {
          appointmentMap[dayName] = {};
        }
        
        // Use the formatted hour (without seconds)
        appointmentMap[dayName][formattedHour] = {
          ...appointment,
          hour: formattedHour // Store the formatted hour for consistency
        };
        console.log("✓ Added appointment to map:", dayName, formattedHour);
      } else {
        console.log("✗ Skipping past appointment:", appointment.date);
      }
    })
      // Debug the appointment map
    console.log("Final appointment map:", JSON.stringify(appointmentMap));
    
      
    // Create a schedule for each day of the week
    const schedule = daysOfWeek.map(day => {
      // Get lowercase short day name for API data (e.g., "Monday" -> "mon")
      // Note: the backend may use lowercase 3-letter abbreviations
      const shortDay = day.toLowerCase().substring(0, 3);
      
      // Also try with just the first letter lowercase in case backend uses a different format
      const alternativeShortDay = day.substring(0, 3).toLowerCase();
      
      // Get available slots from API response for this day, trying both formats
      const availableSlots = 
        availability.availableTimeSlots[shortDay] || 
        availability.availableTimeSlots[alternativeShortDay] || 
        [];
      
      console.log(`Processing day: ${day}, Available slots:`, availableSlots);
      
      return {
        day,
        slots: timeSlots.map(time => {
          // Check if there's an appointment at this time slot
          if (appointmentMap[day] && appointmentMap[day][time]) {
            const appointment = appointmentMap[day][time];
            const appointmentDate = new Date(appointment.date);
            const currentDate = new Date();
            const isUpcoming = appointmentDate > currentDate;

            console.log(`Appointment at ${time} on ${day}:`, {
              date: appointment.date,
              hour: appointment.hour,
              isUpcoming,
              appointmentDate,
              currentDate
            });

            return {
              time,
              status: isUpcoming ? 'upcoming' : 'booked',
              appointment
            }
          }
          
          // Try alternative time formats (handle potential inconsistencies)
          // For example, if backend sends "9:30" but we're looking for "09:30"
          const hourMinute = time.split(':');
          const alternativeFormat = `${parseInt(hourMinute[0])}:${hourMinute[1]}`; // e.g., "09:30" -> "9:30"
          
          if (appointmentMap[day] && appointmentMap[day][alternativeFormat]) {
            const appointment = appointmentMap[day][alternativeFormat];
            const appointmentDate = new Date(appointment.date);
            const currentDate = new Date();
            const isUpcoming = appointmentDate > currentDate;

            console.log(`Appointment at ${alternativeFormat} on ${day}:`, {
              date: appointment.date,
              hour: appointment.hour,
              isUpcoming,
              appointmentDate,
              currentDate
            });

            return {
              time,
              status: isUpcoming ? 'upcoming' : 'booked',
              appointment
            }
          }
          
          // Check if this is an available time slot by comparing the exact startTime
          const isAvailable = availableSlots.some((slot: any) => {
            return slot.startTime === time || slot.startTime === alternativeFormat;
          });
          
          return {
            time,
            status: isAvailable ? 'available' : 'unavailable'
          }
        })
      }
    })
    
    // Display some summary statistics for debugging
    const bookedSlots = schedule.reduce((count, day) => 
      count + day.slots.filter(slot => slot.status === 'booked').length, 0);
    
    console.log(`Weekly schedule created: ${bookedSlots} booked slots, ${schedule.length} days`);
    
    return schedule
  }

  // Handle date selection in the calendar
  const handleDateSelect = (date: Date | undefined) => {
    setSelectedDate(date)
    setShowSchedule(true)
  }
  // If not yet on client, render minimal placeholder to avoid hydration mismatch
  if (!isClient) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Loading doctor dashboard...</h1>
          <p className="text-muted-foreground">Please wait while we load your schedule.</p>
        </div>
        <div className="space-y-4">
          <Skeleton className="h-32 w-full" />
          <Skeleton className="h-64 w-full" />
        </div>
      </div>
    );
  }
  
  // Full rendering once on client
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Welcome, {doctorName}</h1>
        <p className="text-muted-foreground">Here's your schedule for today and upcoming days.</p>
      </div>      <Card className="border-2 border-secondary">
        <CardHeader>
          <CardTitle>Ongoing Appointment</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-4 w-[250px]" />
              <Skeleton className="h-4 w-[200px]" />
            </div>
          ) : ongoingAppointment ? (
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-medium">{`${ongoingAppointment.patient?.firstName || ''} ${ongoingAppointment.patient?.lastName || ''}`}</h3>
                <p className="text-sm text-muted-foreground">{ongoingAppointment.reason}</p>
                <p className="text-sm">Started at: {formatTimeForDisplay(ongoingAppointment.hour)} ({getElapsedTime(ongoingAppointment.hour)})</p>
              </div>
              <Link href={`/doctor/appointments/${ongoingAppointment.id}`}>
                <Button className="w-full">
                  View Details
                </Button>
              </Link>
            </div>
          ) : (
            <div className="text-center py-4">
              <p className="text-muted-foreground">No ongoing appointments at this time.</p>
            </div>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-6 md:grid-cols-2">        <Card className="col-span-1">
          <CardHeader>
            <CardTitle>Today's Schedule</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-16 w-full" />
                <Skeleton className="h-16 w-full" />
                <Skeleton className="h-16 w-full" />
              </div>
            ) : (
              <div className="space-y-4">
                {todaysAppointments.length === 0 ? (
                  <p className="text-sm text-muted-foreground">No appointments scheduled for today.</p>
                ) : (
                  todaysAppointments.map((appointment, i) => (
                    <div key={i} className="flex items-center justify-between rounded-lg border p-3">
                      <div className="space-y-1">
                        <p className="font-medium">{`${appointment.patient?.firstName || ''} ${appointment.patient?.lastName || ''}`}</p>
                        <p className="text-sm text-muted-foreground">{appointment.reason}</p>
                      </div>
                      <div className="flex items-center gap-4">
                        <div className="text-sm font-medium text-primary">{formatTimeForDisplay(appointment.hour)}</div>
                        <Link href={`/doctor/appointments/${appointment.id}`}>
                          <Button size="sm" variant="outline">
                            View
                          </Button>
                        </Link>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}
          </CardContent>
        </Card><Card className="col-span-1">
          <CardHeader>
            <CardTitle>Weekly Schedule</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-full" />
              </div>
            ) : (
              <div className="overflow-x-auto">                <table className="min-w-full border-collapse">
                  <thead>
                    <tr>
                      <th className="p-2 border text-sm">Time</th>
                      {weeklySchedule.slice(0, 5).map((day) => ( // Show only Monday-Friday
                        <th key={day.day} className="p-2 border text-sm bg-gray-100">{day.day}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {timeSlots.map((time, index) => (
                      <tr key={time}>
                        <td className="p-2 border text-sm font-medium bg-gray-50">{formatTimeForDisplay(time)}</td>
                        {weeklySchedule.slice(0, 5).map((day) => { // Show only Monday-Friday
                          const slot = day.slots[index];
                          let bgColor = "bg-white";
                          let content = "";
                          let hoverClass = "";
                          
                          if (slot.status === 'booked' || slot.status === 'upcoming') {
                            // Use the same light red styling for both booked and upcoming appointments
                            bgColor = "bg-red-50 border border-red-200"; 
                            content = `${slot.appointment?.patient?.firstName || ''} ${slot.appointment?.patient?.lastName || 'Patient'}`;
                            hoverClass = "cursor-pointer hover:bg-red-100";
                          } else if (slot.status === 'available') {
                            bgColor = "bg-green-100 border border-green-300"; // Enhanced styling for available slots
                            content = "Available";
                          }
                          
                          return (
                            <td 
                              key={`${day.day}-${time}`} 
                              className={`p-2 border text-xs ${bgColor} ${hoverClass}`}
                              onClick={() => {
                                if ((slot.status === 'booked' || slot.status === 'upcoming') && slot.appointment?.id) {
                                  window.location.href = `/doctor/appointments/${slot.appointment.id}`;
                                }
                              }}
                              title={slot.status === 'booked' || slot.status === 'upcoming' ? 
                                `${content} - ${time} - Click to view details` : 
                                `${day.day} at ${time}: ${slot.status}`
                              }
                            >
                              {content}
                            </td>
                          );
                        })}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}            <div className="mt-4 flex flex-wrap gap-3 text-xs">
              <div className="flex items-center">
                <div className="w-3 h-3 bg-green-100 border border-green-300 mr-1"></div>
                <span>Available Slot</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-red-50 border border-red-200 mr-1"></div>
                <span>Appointment (Click to view)</span>
              </div>
              <div className="flex items-center">
                <div className="w-3 h-3 bg-white border mr-1"></div>
                <span>Unavailable</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
