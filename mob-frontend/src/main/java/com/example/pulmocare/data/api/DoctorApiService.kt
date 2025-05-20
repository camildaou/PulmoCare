package com.example.pulmocare.data.api

import com.example.pulmocare.data.model.Doctor
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for doctor-related operations
 */
interface DoctorApiService {
    /**
     * Get all doctors
     */
    @GET("api/doctors")
    suspend fun getAllDoctors(): Response<List<Doctor>>
    
    /**
     * Get doctor by ID
     */
    @GET("api/doctors/{id}")
    suspend fun getDoctorById(@Path("id") id: String): Response<Doctor>
    
    /**
     * Get total number of doctors
     */
    @GET("api/doctors/count")
    suspend fun getDoctorCount(): Response<Long>
    
    /**
     * Update doctor information
     */
    @PUT("api/doctors/{id}")
    suspend fun updateDoctor(
        @Path("id") id: String, 
        @Body doctor: Doctor
    ): Response<Doctor>
    
    /**
     * Get doctor availability
     */
    @GET("api/doctors/{id}/availability")
    suspend fun getDoctorAvailability(@Path("id") id: String): Response<Map<String, Any>>
    
    /**
     * Update doctor availability
     */
    @PUT("api/doctors/{id}/availability")
    suspend fun updateDoctorAvailability(
        @Path("id") id: String,
        @Body availabilityDetails: Map<String, Any>
    ): Response<Doctor>
    
    /**
     * Add time slot to specific day
     */
    @POST("api/doctors/{id}/availability/timeslot")
    suspend fun addTimeSlot(
        @Path("id") id: String,
        @Body timeSlotDetails: Map<String, String>
    ): Response<Doctor>
    
    /**
     * Set standard weekly schedule with 30-minute slots
     */
    @POST("api/doctors/{id}/availability/standard-schedule")
    suspend fun setStandardSchedule(
        @Path("id") id: String,
        @Body scheduleDetails: Map<String, Any>
    ): Response<Doctor>
    
    /**
     * Remove a time slot from a doctor's schedule
     */
    @DELETE("api/doctors/{id}/availability/timeslot")
    suspend fun removeTimeSlot(
        @Path("id") id: String,
        @Query("day") day: String,
        @Query("startTime") startTime: String
    ): Response<Doctor>
}
