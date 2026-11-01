package gui;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import Enums.Rank;
import Manegers.EmployeeDataBase;
import Manegers.ShiftManager;
import SingleObjectClasses.Employee;
import SingleObjectClasses.Shift;

/** Shared state for the GUI: the two managers and the logged-in employee. */
final class AppContext {
	static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm");
	static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

	final ShiftManager shiftManager;
	final EmployeeDataBase employeeDataBase;
	private final List<Runnable> listeners = new ArrayList<>();

	AppContext() {
		// Same wiring as the console application
		shiftManager = new ShiftManager(null);
		employeeDataBase = new EmployeeDataBase(shiftManager);
		shiftManager.setEmployeeDataBase(employeeDataBase);
	}

	Employee user() {
		return employeeDataBase.getLoggedInEmployee();
	}

	boolean isWorker() {
		return user() == null || user().getRank() == Rank.WORKER;
	}

	boolean isAdmin() {
		return user() != null && user().getRank() == Rank.ADMINISTRATOR;
	}

	void onChange(Runnable listener) {
		listeners.add(listener);
	}

	void clearListeners() {
		listeners.clear();
	}

	/** Called after every change so all screens show the managers' current data. */
	void changed() {
		for (Runnable listener : new ArrayList<>(listeners)) {
			listener.run();
		}
	}

	Color colorOf(Employee employee) {
		if (employee == null) {
			return Theme.FAINT;
		}
		int index = employeeDataBase.getAllEmployees().indexOf(employee);
		if (index < 0) {
			index = Math.abs(employee.getId().hashCode());
		}
		return Theme.EMPLOYEE_COLORS.get(index % Theme.EMPLOYEE_COLORS.size());
	}

	static String name(Employee employee) {
		return employee == null ? "Unassigned" : employee.getFirstName() + " " + employee.getLastName();
	}

	static String format(LocalDateTime time) {
		return time == null ? "" : time.format(DATE_TIME);
	}

	/** Length of a shift; an ongoing shift is measured up to now. */
	static Duration length(Shift shift) {
		if (shift.getStartDateTime() == null) {
			return Duration.ZERO;
		}
		LocalDateTime end = shift.getEndDateTime() != null ? shift.getEndDateTime() : LocalDateTime.now();
		return Duration.between(shift.getStartDateTime(), end);
	}

	static String hours(Duration duration) {
		long minutes = Math.max(0, duration.toMinutes());
		return String.format("%dh %02dm", minutes / 60, minutes % 60);
	}
}
