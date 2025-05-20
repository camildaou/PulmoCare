import axios from 'axios';
import { Patient } from './types';

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const adminApi = {
  signup: async (adminData: any) => {
    const response = await api.post('/admin/signup', adminData);
    return response.data;
  },
  signin: async (credentials: { email: string; password: string }) => {
    const response = await api.post('/admin/signin', credentials);
    return response.data;
  },
  getProfile: async (id: string) => {
    const response = await api.get(`/admin/profile/${id}`);
    return response.data;
  },
  updateProfile: async (id: string, profileData: any) => {
    const response = await api.put(`/admin/profile/${id}`, profileData);
    return response.data;
  },
  getPatientCount: async () => {
    const response = await api.get('/patient/count');
    return response.data;
  },
  getAllPatients: async () => {
    const response = await api.get('/patient');
    return response.data;
  },
  updatePatient: async (id: string, patientDetails: Patient) => {
    const response = await api.put(`/patient/${id}`, patientDetails);
    return response.data;
  },
  getPatientById: async (id: string) => {
    const response = await api.get(`/patient/${id}`);
    return response.data;
  },
  postPatient: async (patientDetails: Patient) => {
    const response = await api.post('/patient', patientDetails);
    return response.data;
  },
  getAppointmentsByDoctorId: async (doctorId: string) => {
    const response = await api.get(`/appointments/doctor/${doctorId}`);
    return response.data;
  },
};

export const doctorApi = {
  signup: async (doctorData: any) => {
    const response = await api.post('/doctors/signup', doctorData);
    return response.data;
  },
  signin: async (credentials: { email: string; password: string }) => {
    const response = await api.post('/doctors/signin', credentials);
    return response.data;
  },
  getProfile: async (id: string) => {
    const response = await api.get(`/doctors/${id}`);
    return response.data;
  },
  updateProfile: async (id: string, profileData: any) => {
    const response = await api.put(`/doctors/${id}`, profileData);
    return response.data;
  },
  getAllDoctors: async () => {
    const response = await api.get('/doctors');
    return response.data;
  },
  getDoctorCount: async () => {
    const response = await api.get('/doctors/count');
    return response.data;
  },
  getAllPatients: async () => {
    const response = await api.get('/patient');
    return response.data;
  },
};

export const scheduleApi = {
  saveAvailability: async (doctorId: string, availabilityDetails: any) => {
    const response = await api.put(`/doctors/${doctorId}/availability`, availabilityDetails);
    return response.data;
  },
  addTimeSlot: async (doctorId: string, timeSlotDetails: any) => {
    const response = await api.post(`/doctors/${doctorId}/availability/timeslot`, timeSlotDetails);
    return response.data;
  },
  getDoctorAvailability: async (doctorId: string) => {
    const response = await api.get(`/doctors/${doctorId}/availability`);
    return response.data;
  },
};

export default api;
