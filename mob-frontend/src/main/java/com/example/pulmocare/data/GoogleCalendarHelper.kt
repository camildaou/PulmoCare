package com.example.pulmocare.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.example.pulmocare.data.model.Appointment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Helper class to interact with Google Calendar
 * This implementation uses the built-in Calendar Intent to add events to Google Calendar
 * rather than using the Google Calendar API directly, which would require OAuth setup.
 */
class GoogleCalendarHelper {
    
    companion object {
        
        /**
         * Add an appointment to Google Calendar using an Intent
         * 
         * @param context The activity context
         * @param appointment The appointment to add to the calendar
         * @return An intent that can be used to start the calendar activity
         */
        fun createAddToCalendarIntent(context: Context, appointment: Appointment): Intent {            // Parse the date and time to get start time in milliseconds
            val startTime = parseDateTime(appointment.date, appointment.hour)
            
            // Set end time based on appointment data or default to 30 minutes
            val endTime = if (appointment.endTime != null) {
                parseDateTime(appointment.date, appointment.endTime)
            } else {
                // Default to 30-minute appointment if end time is not provided
                startTime + (30 * 60 * 1000)
            }
            
            // Create intent to add event to calendar
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                putExtra(CalendarContract.Events.TITLE, "PulmoCare Appointment: Dr. ${appointment.doctor?.getFullName() ?: "Unknown"}")
                putExtra(CalendarContract.Events.DESCRIPTION, buildString {
                    append("Medical appointment with Dr. ${appointment.doctor?.getFullName() ?: "Unknown"}\n")
                    append("Specialty: ${appointment.doctor?.specialization ?: "Specialist"}\n")
                    if (appointment.reason.isNotEmpty()) {
                        append("Reason: ${appointment.reason}\n")
                    }
                    append("\nPlease arrive 10 minutes before your scheduled time.")
                })
                putExtra(CalendarContract.Events.EVENT_LOCATION, appointment.location)
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                
                // Add reminders (24 hours and 1 hour before)
                putExtra(CalendarContract.Reminders.MINUTES, 60) // 1 hour reminder
                val reminderIntent = Intent(Intent.ACTION_EDIT)
                putExtra(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                putExtra(Intent.EXTRA_EMAIL, appointment.doctor?.email)
                
                // Set calendar color (optional)
                putExtra("eventColor", 0xFF4285F4.toInt()) // Google Blue
            }
            
            return intent
        }
        
        /**
         * Open Google Calendar directly to a specific date
         * 
         * @param context The activity context
         * @param date The date to open in the calendar
         * @return An intent that can be used to start the calendar activity
         */
        fun openGoogleCalendar(context: Context, date: String): Intent {
            val timeInMillis = parseDate(date)
            
            // Create URI to open Google Calendar app
            val builder = Uri.Builder()
                .scheme("content")
                .authority("com.android.calendar")
                .appendPath("time")
                .appendPath(timeInMillis.toString())
            
            return Intent(Intent.ACTION_VIEW, builder.build())
        }
          /**
         * Parse date and time strings to get milliseconds since epoch
         * 
         * @param dateStr The date string in format "yyyy-MM-dd" (ISO format)
         * @param timeStr The time string in format "HH:mm"
         * @return Time in milliseconds since epoch
         */        private fun parseDateTime(dateStr: String, timeStr: String): Long {
            // Convert dateStr from "yyyy-MM-dd" to a Date object
            // Convert timeStr from "HH:mm" to hours and minutes
            val dateTimeStr = "$dateStr $timeStr"
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            format.timeZone = TimeZone.getDefault()
            
            return try {
                val date = format.parse(dateTimeStr)
                android.util.Log.d("GoogleCalendarHelper", "Parsed date: $dateStr, time: $timeStr to ${date?.time}")
                date?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                android.util.Log.e("GoogleCalendarHelper", "Error parsing date: $dateStr, time: $timeStr", e)
                e.printStackTrace()
                System.currentTimeMillis()
            }
        }
          /**
         * Parse date string to get milliseconds since epoch
         * 
         * @param dateStr The date string in "yyyy-MM-dd" format (ISO format)
         * @return Time in milliseconds since epoch
         */
        private fun parseDate(dateStr: String): Long {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            format.timeZone = TimeZone.getDefault()
            
            return try {
                val date = format.parse(dateStr)
                date?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                e.printStackTrace()
                System.currentTimeMillis()
            }
        }
    }
}
