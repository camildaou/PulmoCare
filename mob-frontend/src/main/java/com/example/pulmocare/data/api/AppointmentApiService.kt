package com.example.pulmocare.data.api

import com.example.pulmocare.data.model.Appointment
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for appointment-related operations
 */
interface AppointmentApiService {
    /**
     * Get all appointments
     */
    @GET("api/appointments")
    suspend fun getAllAppointments(): Response<List<Appointment>>
    
    /**
     * Get appointment by ID
     */
    @GET("api/appointments/{id}")
    suspend fun getAppointmentById(@Path("id") id: String): Response<Appointment>
    
    /**
     * Create a new appointment
     */
    @POST("api/appointments")
    suspend fun createAppointment(@Body appointment: Appointment): Response<Appointment>
    
    /**
     * Update an existing appointment
     */
    @PUT("api/appointments/{id}")
    suspend fun updateAppointment(
        @Path("id") id: String, 
        @Body appointment: Appointment
    ): Response<Appointment>
    
    /**
     * Delete an appointment
     */
    @DELETE("api/appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: String): Response<Void>
    
    /**
     * Get appointments by patient ID
     */
    @GET("api/appointments/patient/{patientId}")
    suspend fun getAppointmentsByPatientId(@Path("patientId") patientId: String): Response<List<Appointment>>
    
    /**
     * Get upcoming appointments by patient ID
     */
    @GET("api/appointments/patient/{patientId}/upcoming")
    suspend fun getUpcomingAppointmentsByPatientId(@Path("patientId") patientId: String): Response<List<Appointment>>
    
    /**
     * Get past appointments by patient ID
     */
    @GET("api/appointments/patient/{patientId}/past")
    suspend fun getPastAppointmentsByPatientId(@Path("patientId") patientId: String): Response<List<Appointment>>
    
    /**
     * Get appointments by doctor ID
     */
    @GET("api/appointments/doctor/{doctorId}")
    suspend fun getAppointmentsByDoctorId(@Path("doctorId") doctorId: String): Response<List<Appointment>>
    
    /**
     * Get upcoming appointments by doctor ID
     */
    @GET("api/appointments/doctor/{doctorId}/upcoming")
    suspend fun getUpcomingAppointmentsByDoctorId(@Path("doctorId") doctorId: String): Response<List<Appointment>>
    
    /**
     * Get past appointments by doctor ID
     */
    @GET("api/appointments/doctor/{doctorId}/past")
    suspend fun getPastAppointmentsByDoctorId(@Path("doctorId") doctorId: String): Response<List<Appointment>>
    
    /**
     * Mark an appointment as past (completed)
     */
    @PATCH("api/appointments/{id}/mark-past")
    suspend fun markAppointmentAsPast(@Path("id") id: String): Response<Appointment>
}
