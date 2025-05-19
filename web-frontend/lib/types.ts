export interface Doctor {
  id: string;
  firstName: string;
  lastName: string;
  description?: string;
  patients?: number;
  appointments?: number;
  gender?: string;
  age?: number;
  email?: string;
  phone?: string;
  medicalLicense?: string;
  location?: string;
}

