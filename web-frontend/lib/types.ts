export interface DoctorSignupData {
  firstName: string;
  lastName: string;
  gender: string;
  age: number;
  description: string;
  location: string;
  countryCode: string;
  phone: string;
  email: string;
  password: string;
  medicalLicense: string;
}

export interface DoctorSigninCredentials {
  email: string;
  password: string;
}

export interface Doctor {
  id: string;
  firstName: string;
  lastName: string;
  gender: string;
  age: number;
  description: string;
  location: string;
  countryCode: string;
  phone: string;
  email: string;
  medicalLicense: string;
}

export interface DoctorUpdateData extends Omit<DoctorSignupData, 'password' | 'email'> {}
