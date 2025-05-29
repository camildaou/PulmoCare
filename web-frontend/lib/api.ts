import axios from 'axios';
import { Patient, Appointment } from './types';

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
  getAppointmentsByDoctorId: async (doctorId: string) => {
    const response = await api.get(`/appointments/doctor/${doctorId}`);
    return response.data;
  },
  getUpcomingAppointmentsByDoctorId: async (doctorId: string) => {
    // This endpoint should return only upcoming appointments
    const response = await api.get(`/appointments/doctor/${doctorId}/upcoming`);
    return response.data;
  },
  getPastAppointmentsByPatientId: async (patientId: string) => {
    const response = await api.get(`/appointments/patient/${patientId}/past`);
    return response.data;
  },
  updateAppointment: async (appointmentId: string, appointmentData: any) => {
    const response = await api.put(`/appointments/${appointmentId}`, appointmentData);
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
  appendAvailability: async (doctorId: string, newAvailability: any) => {
    const response = await api.post(`/doctors/${doctorId}/availability/append`, newAvailability);
    return response.data;
  },
  removeTimeSlot: async (doctorId: string, day: string, startTime: string) => {
    const response = await api.delete(`/doctors/${doctorId}/availability/remove-timeslot`, {
      params: { day, startTime },
    });
    return response.data;
  },
};

export const createAppointment = async (doctorId: string, patientId: string, date: string, time: string, reason: string) => {
  try {
    console.log('Creating appointment with:', { doctorId, patientId, date, time, reason });
    
    // Ensure date is in ISO format (YYYY-MM-DD) which Java's LocalDate can parse
    let formattedDate = date;
    if (date && !date.match(/^\d{4}-\d{2}-\d{2}$/)) {
      // Convert to ISO format if it's not already
      formattedDate = new Date(date).toISOString().split('T')[0];
    }
    
    // Convert 12-hour time format (e.g., "08:00 AM") to 24-hour format (e.g., "08:00")
    // Java's LocalTime expects HH:MM format (or HH:MM:SS)
    let formattedTime = time;
    if (time.includes('AM') || time.includes('PM')) {
      const [timePart, ampm] = time.split(' ');
      const [hours, minutes] = timePart.split(':');
      let hour = parseInt(hours, 10);
      
      if (ampm === 'PM' && hour < 12) {
        hour += 12;
      } else if (ampm === 'AM' && hour === 12) {
        hour = 0;
      }
      
      formattedTime = `${hour.toString().padStart(2, '0')}:${minutes}`;
    }
    
    console.log('Formatted date and time:', { formattedDate, formattedTime });

    // Constructing the request exactly how the Java backend expects it
    const requestData = {
      doctor: { id: doctorId },
      patient: { id: patientId },
      date: formattedDate,
      hour: formattedTime,
      reason: reason,
      upcoming: true, // Setting default values expected by the backend
      isVaccine: false,
      reportPending: false
    };
    
    console.log('Sending request:', requestData);
      // Make request with the correct Content-Type header
    const response = await api.post('/appointments/web-create-appt', requestData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    console.log('Response received:', response.data);
    
    // Check if we got the time slot unavailable message from the backend
    if (response.data === "TIME_SLOT_UNAVAILABLE") {
      // Instead of returning an object, directly show alert
      alert("The selected time slot is not available. Please choose another time.");
      
      // Return a special object indicating we've already shown the alert
      return {
        success: false,
        error: true,
        message: "The selected time slot is not available.",
        timeSlotError: true,
        alertShown: true
      };
    }
    
    return response.data;
  } catch (error: any) {
    console.error('Error creating appointment:', error);
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      console.error('Error response data:', error.response.data);
      console.error('Error response status:', error.response.status);
      console.error('Error response headers:', error.response.headers);
    } else if (error.request) {
      // The request was made but no response was received
      console.error('Error request:', error.request);
    } else {
      // Something happened in setting up the request that triggered an Error
      console.error('Error message:', error.message);
    }
    throw error;
  }
};

export const appointmentsApi = {
  getOngoingAppointment: async (doctorId: string) => {
    try {
      const response = await api.get(`/appointments/doctor/${doctorId}/ongoing`);
      return response.data;
    } catch (error) {
      console.error('Error fetching ongoing appointment:', error);
      return null;
    }
  },
  
  getTodaysAppointments: async (doctorId: string) => {
    try {
      const response = await api.get(`/appointments/doctor/${doctorId}/today`);
      return response.data;
    } catch (error) {
      console.error('Error fetching today\'s appointments:', error);
      return [];
    }
  },
  
  getUpcomingAppointmentsByDoctor: async (doctorId: string) => {
    try {
      const response = await api.get(`/appointments/doctor/${doctorId}/upcoming`);
      return response.data;
    } catch (error) {
      console.error('Error fetching upcoming appointments:', error);
      return [];
    }
  },
  
  getAppointmentById: async (appointmentId: string) => {
    try {
      const response = await api.get(`/appointments/${appointmentId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching appointment details:', error);
      return null;
    }
  },
  
  deleteAppointment: async (appointmentId: string) => {
    try {
      const response = await api.delete(`/appointments/${appointmentId}`);
      return response.status === 204; // Returns true if successfully deleted
    } catch (error) {
      console.error('Error deleting appointment:', error);
      throw error;
    }
  },
  
  /**
   * Fetches past appointments for a given patient ID.
   * @param patientId - The ID of the patient.
   * @returns A promise resolving to an array of appointments.
   */
  fetchPastAppointments: async (patientId: string): Promise<Appointment[]> => {
    const response = await api.get(`/appointments/past/${patientId}`);
    return response.data;
  },
};

export default api;
