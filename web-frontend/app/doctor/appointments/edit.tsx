import { useState } from "react";
import { useRouter } from "next/router";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

export default function EditAppointmentPage() {
  const router = useRouter();
  const { id } = router.query;

  const [formData, setFormData] = useState({
    date: "",
    time: "",
    diagnosis: "",
    prescriptions: "",
    plan: "",
    confidentialNotes: "",
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSave = () => {
    // Save logic here
    console.log("Saved data:", formData);
    router.push("/doctor/appointments");
  };

  return (
    <div className="space-y-6 p-6">
      <h1 className="text-3xl font-bold">Edit Appointment</h1>
      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="date">Date</Label>
          <Input
            id="date"
            name="date"
            type="date"
            value={formData.date}
            onChange={handleInputChange}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="time">Time</Label>
          <Input
            id="time"
            name="time"
            type="time"
            value={formData.time}
            onChange={handleInputChange}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="diagnosis">Diagnosis</Label>
          <Textarea
            id="diagnosis"
            name="diagnosis"
            value={formData.diagnosis}
            onChange={handleInputChange}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="prescriptions">Prescriptions</Label>
          <Textarea
            id="prescriptions"
            name="prescriptions"
            value={formData.prescriptions}
            onChange={handleInputChange}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="plan">Plan</Label>
          <Textarea
            id="plan"
            name="plan"
            value={formData.plan}
            onChange={handleInputChange}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="confidentialNotes">Confidential Notes</Label>
          <Textarea
            id="confidentialNotes"
            name="confidentialNotes"
            value={formData.confidentialNotes}
            onChange={handleInputChange}
          />
        </div>
        <div className="flex justify-end space-x-4">
          <Button variant="outline" onClick={() => router.push("/doctor/appointments")}>Cancel</Button>
          <Button onClick={handleSave}>Save</Button>
        </div>
      </div>
    </div>
  );
}
