"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import Link from "next/link"
import { useEffect, useState } from "react"
import { toast } from "sonner"
import { useParams } from "next/navigation"
import { adminApi } from '@/lib/api';
import { Patient } from '@/lib/types';

const PatientInfoPage = () => {
  const params = useParams();
  const patientId = params.id;

  const [patient, setPatient] = useState<Patient | null>(null);

  useEffect(() => {
    const fetchPatient = async () => {
      const id = Array.isArray(patientId) ? patientId[0] : patientId;
      if (!id) {
        console.error("Patient ID is undefined.");
        return;
      }

      try {
        const response = await adminApi.getPatientById(id);
        setPatient(response);
      } catch (error) {
        console.error("Error fetching patient details:", error);
      }
    };

    fetchPatient();
  }, [patientId]);

  if (!patient) {
    return <p>Loading patient details...</p>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Patient Information</h1>
          <p className="text-muted-foreground">View and manage patient details.</p>
        </div>
        <div className="flex gap-2">
          <Link href="/doctor/appointments">
            <Button variant="outline">Back to Appointments</Button>
          </Link>
        </div>
      </div>

      <Card>
        <CardContent>
          <h2 className="text-xl font-bold">{patient.name}</h2>
          <p className="text-sm text-muted-foreground">Patient ID: {patientId}</p>
          <div className="grid grid-cols-2 gap-2 text-sm mt-4">
            <div className="text-muted-foreground">Gender:</div>
            <div>{patient.gender}</div>
            <div className="text-muted-foreground">Age:</div>
            <div>{patient.age}</div>
            <div className="text-muted-foreground">Blood Type:</div>
            <div>{patient.bloodType}</div>
            <div className="text-muted-foreground">Email:</div>
            <div className="truncate">{patient.email}</div>
            <div className="text-muted-foreground">Height:</div>
            <div>{patient.height}</div>
            <div className="text-muted-foreground">Weight:</div>
            <div>{patient.weight}</div>
            <div className="text-muted-foreground">Marital Status:</div>
            <div>{patient.maritalStatus}</div>
            <div className="text-muted-foreground">Occupation:</div>
            <div>{patient.occupation}</div>
            <div className="text-muted-foreground">Pets:</div>
            <div>{patient.hasPets ? 'Yes' : 'No'}</div>
            <div className="text-muted-foreground">Smoking:</div>
            <div>{patient.isSmoking ? 'Yes' : 'No'}</div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default PatientInfoPage;
