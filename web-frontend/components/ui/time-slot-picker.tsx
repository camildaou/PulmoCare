import React, { useState } from "react";
import { Button } from "./button";

interface TimeSlotPickerProps {
  selectedSlots: string[]; // Updated to handle only time slots
  onChange: (slots: string[]) => void; // Updated to handle only time slots
}

const TimeSlotPicker: React.FC<TimeSlotPickerProps> = ({ selectedSlots, onChange }) => {
  const [timeSlots, setTimeSlots] = useState<string[]>(selectedSlots);

  const availableTimes = [
    "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
    "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
    "15:00", "15:30", "16:00", "16:30", "17:00", "17:30",
  ];

  const toggleTimeSlot = (time: string) => {
    const updatedSlots = timeSlots.includes(time)
      ? timeSlots.filter((slot) => slot !== time)
      : [...timeSlots, time];

    setTimeSlots(updatedSlots);
    onChange(updatedSlots); // Notify parent of the updated time slots
  };

  return (
    <div className="grid grid-cols-4 gap-2">
      {availableTimes.map((time) => (
        <Button
          key={time}
          variant={timeSlots.includes(time) ? "default" : "outline"}
          onClick={() => toggleTimeSlot(time)}
        >
          {time}
        </Button>
      ))}
    </div>
  );
};

export { TimeSlotPicker };
