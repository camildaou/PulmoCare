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
}
