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

export interface Patient {
  id: string;
  firstName: string;
  lastName: string;
  name: string; // Full name for display purposes
  location?: string;
  age?: number;
  condition?: string;
  lastVisit?: string;
  photo?: string;
  insuranceProvider?: string;
  gender?: string;
  email?: string;
  bloodType?: string;
  height?: number;
  weight?: number;
  maritalStatus?: string;
  occupation?: string;
  hasPets?: boolean;
  isSmoking?: boolean;
  previousDiagnosis?: string[];
  previousPlans?: string[];
  previousPrescriptions?: string[];
  previousResources?: string[];
  symptomsAssessment?: string;
  report?: string;
  bloodTests?: Record<string, any>[];
  xRays?: Record<string, any>[];
  otherImaging?: Record<string, any>[];
  vaccinationHistory?: Record<string, any>[];
  vitals?: Record<string, any>;
  allergies?: string[];
  chronicConditions?: string[];
  surgeriesHistory?: Record<string, any>[];
  password?: string;
}

