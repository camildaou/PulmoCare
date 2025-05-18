import axios from 'axios';

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
};

export default api;
