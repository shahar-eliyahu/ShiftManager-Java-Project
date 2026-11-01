package SingleObjectClasses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Shift {

	private int ShiftID;
	public static int counter = 0;
	private LocalDateTime startDateTime;
	private LocalDateTime endDateTime;
	private Employee employee;

	// Constructor
	public Shift() {
		this.ShiftID = counter++;
	}

	// Start the shift
	public void startShift() {
		this.startDateTime = LocalDateTime.now();
		System.out.println("Shift started at: " + formatDateTime(startDateTime));
	}

	// End the shift
	public void endShift() {
		this.endDateTime = LocalDateTime.now();
		System.out.println("Shift ended at: " + formatDateTime(endDateTime));
	}

	// Format LocalDateTime to a readable string, handling null values
	private String formatDateTime(LocalDateTime dateTime) {
		if (dateTime == null) {
			return "N/A"; // Safe handling for null values
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return dateTime.format(formatter);
	}

	// Getters
	public int getId() {
		return ShiftID;
	}

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	// Setters for startDateTime and endDateTime
	public void setStartDateTime(LocalDateTime startDateTime) {
		this.startDateTime = startDateTime;
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		this.endDateTime = endDateTime;
	}

	// Get the duration of the shift
	public String getShiftDuration() {
		if (startDateTime != null && endDateTime != null) {
			long seconds = java.time.Duration.between(startDateTime, endDateTime).getSeconds();
			long hours = seconds / 3600;
			long minutes = (seconds % 3600) / 60;
			seconds = seconds % 60;
			return String.format("%02d:%02d:%02d", hours, minutes, seconds);
		} else {
			return "N/A"; // Duration is not available if shift has not ended
		}
	}

	// Not scheduled (no times yet), Scheduled (starts later), In progress,
	// Ongoing (checked in, not checked out) or Completed
	public String getStatus() {
		LocalDateTime now = LocalDateTime.now();
		if (startDateTime == null) {
			return "Not scheduled";
		}
		if (endDateTime == null) {
			return "Ongoing";
		}
		if (startDateTime.isAfter(now)) {
			return "Scheduled";
		}
		if (endDateTime.isAfter(now)) {
			return "In progress";
		}
		return "Completed";
	}

	public ShiftMemento saveToMemento() {
		return new ShiftMemento(startDateTime, endDateTime);
	}

	public void restoreFromMemento(ShiftMemento memento) {
		this.startDateTime = memento.getStartDateTime();
		this.endDateTime = memento.getEndDateTime();
	}

	@Override
	public String toString() {
		return "Shift [ShiftID=" + ShiftID + ", Start=" + formatDateTime(startDateTime) + ", End="
				+ formatDateTime(endDateTime) + ", Duration=" + getShiftDuration() + ", Employee="
				+ (employee != null ? employee.getFirstName() + " " + employee.getLastName() : "None") + ", Status="
				+ getStatus() + "]";
	}
}
