export interface Doctor {
  id: string;
  name: string; // Full name for display purposes
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

export interface Appointment {
  id: string;
  date: string; // ISO date string
  hour: string; // Time in HH:mm format
  time: string; // Alias for hour
  reason?: string; // Reason for the appointment
  status?: string; // Status of the appointment (e.g., Confirmed, Pending)
  doctor: {
    id: string;
    name: string;
  };
  patient: Patient; // Updated to use the Patient interface
  
  // Medical documentation
  diagnosis?: string; // Diagnosis provided by the doctor
  prescriptions?: string; // Medications prescribed to the patient (plural form)
  prescription?: string; // Alternative field name for prescriptions (singular form)
  plan?: string; // Treatment plan
  confidentialNotes?: string; // Doctor's personal/confidential notes
  reportPending?: boolean; // Whether a report is pending for this appointment
  location?: string; // Appointment location
}

