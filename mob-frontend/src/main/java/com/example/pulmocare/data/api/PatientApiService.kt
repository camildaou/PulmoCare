package com.example.pulmocare.data.api

import com.example.pulmocare.data.model.LoginCredentials
import com.example.pulmocare.data.model.Patient
import com.example.pulmocare.data.model.PatientSignup
import retrofit2.Response
import retrofit2.http.*

interface PatientApiService {
    @GET("api/patient")
    suspend fun getAllPatients(): Response<List<Patient>>
    
    @GET("api/patient/{id}")
    suspend fun getPatientById(@Path("id") id: String): Response<Patient>
    
    @POST("api/patient/signup")
    suspend fun signUp(@Body patient: PatientSignup): Response<Patient>
    
    @POST("api/patient")
    suspend fun addPatient(@Body patient: Patient): Response<Patient>
    
    @POST("api/patient/signin")
    suspend fun signIn(@Body credentials: LoginCredentials): Response<Patient>
    
    @GET("api/patient/count")
    suspend fun getPatientCount(): Response<Long>
    
    @PUT("api/patient/{id}")
    suspend fun updatePatient(@Path("id") id: String, @Body patient: Patient): Response<Patient>
}
