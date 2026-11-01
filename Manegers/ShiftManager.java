package Manegers;

import SingleObjectClasses.CheckInOutRecord;
import SingleObjectClasses.Shift;
import Interfaces.IFunctionsLab;
import Interfaces.IShiftManagement;
import Interfaces.Observer;
import Interfaces.Subject;
import SingleObjectClasses.Employee;
import Enums.Rank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftManager implements IShiftManagement, Subject {
	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private List<Shift> shifts = new ArrayList<>();
	private List<Observer> observers = new ArrayList<>();
	private Map<String, CheckInOutRecord> checkInOutRecords = new HashMap<>();
	private EmployeeDataBase employeeDataBase;

	// Constructor
	public ShiftManager(EmployeeDataBase employeeDataBase) {
		this.employeeDataBase = employeeDataBase;
	}

	public boolean hasShifts() {
		return !shifts.isEmpty();
	}

	public List<Shift> getShifts() {
		return Collections.unmodifiableList(shifts);
	}

	public void setEmployeeDataBase(EmployeeDataBase employeeDataBase) {
		this.employeeDataBase = employeeDataBase;
	}

	// ------------------------------------------------------------------
	// Core operations (no console input), used by the console menus and the GUI.
	// Rule violations throw IllegalArgumentException / IllegalStateException
	// with a message for the user.
	// ------------------------------------------------------------------

	public Shift createShift() {
		Shift newShift = new Shift();
		shifts.add(newShift);
		notifyObservers("Shift ID: " + newShift.getId() + " has been added.");
		return newShift;
	}

	public Shift createShift(LocalDateTime start, LocalDateTime end, Employee employee) {
		validateTimes(start, end);
		Shift newShift = new Shift();
		newShift.setStartDateTime(start);
		newShift.setEndDateTime(end);
		newShift.setEmployee(employee);
		shifts.add(newShift);
		notifyObservers("Shift ID: " + newShift.getId() + " has been added"
				+ (employee != null ? " for " + employee.getFirstName() + " " + employee.getLastName() : "") + ".");
		return newShift;
	}

	public void assignEmployee(Shift shift, Employee employee) {
		if (shift == null || !shifts.contains(shift)) {
			throw new IllegalArgumentException("Shift not found or removed.");
		}
		if (employee == null) {
			throw new IllegalArgumentException("Employee not found.");
		}
		if (shift.getEmployee() != null) {
			throw new IllegalStateException("Shift already has an employee assigned.");
		}
		shift.setEmployee(employee);
		notifyObservers("Employee " + employee.getFirstName() + " " + employee.getLastName()
				+ " has been assigned to Shift ID: " + shift.getId());
	}

	public void updateShiftTimes(Shift shift, LocalDateTime start, LocalDateTime end) {
		if (shift == null || !shifts.contains(shift)) {
			throw new IllegalArgumentException("Shift not found.");
		}
		validateTimes(start, end);
		shift.setStartDateTime(start);
		shift.setEndDateTime(end);
		notifyObservers("Shift ID: " + shift.getId() + " has been updated.");
	}

	public void deleteShift(Shift shift) {
		if (shift == null || !shifts.remove(shift)) {
			throw new IllegalArgumentException("Shift not found.");
		}
		notifyObservers("Shift ID: " + shift.getId() + " has been removed.");
	}

	private void validateTimes(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null) {
			throw new IllegalArgumentException("Start and end times are required.");
		}
		if (!end.isAfter(start)) {
			throw new IllegalArgumentException("End time must be after start time.");
		}
	}

	public Shift findShiftById(int shiftId) {
		for (Shift shift : shifts) {
			if (shift.getId() == shiftId) {
				return shift;
			}
		}
		return null;
	}

	public List<Shift> getShiftsForEmployee(Employee employee) {
		List<Shift> result = new ArrayList<>();
		for (Shift shift : shifts) {
			if (shift.getEmployee() != null && shift.getEmployee().equals(employee)) {
				result.add(shift);
			}
		}
		return result;
	}

	public List<Shift> getShiftsBetween(LocalDate from, LocalDate to) {
		List<Shift> result = new ArrayList<>();
		for (Shift shift : shifts) {
			if (shift.getStartDateTime() != null) {
				LocalDate shiftDate = shift.getStartDateTime().toLocalDate();
				if (!shiftDate.isBefore(from) && !shiftDate.isAfter(to)) {
					result.add(shift);
				}
			}
		}
		return result;
	}

	// ------------------------------------------------------------------
	// Check-in / check-out with undo (Memento)
	// ------------------------------------------------------------------

	private CheckInOutRecord recordFor(Employee employee) {
		return checkInOutRecords.computeIfAbsent(employee.getId(), id -> new CheckInOutRecord(employee));
	}

	public Shift checkIn(Employee employee) {
		if (getOngoingShiftForEmployee(employee) != null) {
			throw new IllegalStateException(employee.getFirstName() + " is already checked in.");
		}
		Shift shift = new Shift();
		shift.setEmployee(employee);
		recordFor(employee).save(shift); // state before check-in: not started
		shift.startShift();
		shifts.add(shift); // Ensure shift is added when started
		notifyObservers("Employee " + employee.getFirstName() + " " + employee.getLastName() + " checked in.");
		return shift;
	}

	public Shift checkOut(Employee employee) {
		Shift shift = getOngoingShiftForEmployee(employee);
		if (shift == null) {
			throw new IllegalStateException(
					"No ongoing shift found for employee: " + employee.getFirstName() + " " + employee.getLastName());
		}
		recordFor(employee).save(shift); // state before check-out: ongoing
		shift.endShift();
		notifyObservers("Employee " + employee.getFirstName() + " " + employee.getLastName() + " checked out.");
		return shift;
	}

	public boolean canUndo(Employee employee) {
		CheckInOutRecord record = checkInOutRecords.get(employee.getId());
		return record != null && record.canUndo();
	}

	// Undoes the employee's last check-in or check-out by restoring the saved state
	public String undoLastCheckInOut(Employee employee) {
		CheckInOutRecord record = checkInOutRecords.get(employee.getId());
		Shift shift = record != null ? record.undo() : null;
		if (shift == null) {
			throw new IllegalStateException("Nothing to undo for " + employee.getFirstName() + ".");
		}
		String message;
		if (shift.getStartDateTime() == null) {
			shifts.remove(shift); // the check-in created this shift, so undoing it removes the shift
			message = "Employee " + employee.getFirstName() + " " + employee.getLastName() + " undid a check-in.";
		} else {
			message = "Employee " + employee.getFirstName() + " " + employee.getLastName()
					+ " undid a check-out. Shift ID: " + shift.getId() + " is ongoing again.";
		}
		notifyObservers(message);
		return message;
	}

	public Shift getOngoingShiftForEmployee(Employee employee) {
		for (Shift shift : shifts) {
			if (shift.getEmployee() != null && shift.getEmployee().equals(employee) && shift.getStartDateTime() != null
					&& shift.getEndDateTime() == null) {
				return shift;
			}
		}
		return null;
	}

	// ------------------------------------------------------------------
	// Console operations
	// ------------------------------------------------------------------

	@Override
	public void printShiftsByDay() {
		System.out.println("Enter a day of the current week:");
		System.out.println("1) Monday\n2) Tuesday\n3) Wednesday\n4) Thursday\n5) Friday\n6) Saturday\n7) Sunday");
		int choice = IFunctionsLab.IntegerChoiceRange(1, 7);
		LocalDate today = LocalDate.now();
		LocalDate searchDate = today.with(java.time.DayOfWeek.of(choice));

		boolean found = false;
		for (Shift shift : getShiftsBetween(searchDate, searchDate)) {
			System.out.println(shift);
			found = true;
		}

		if (!found) {
			System.out.println("No shifts found on " + searchDate.getDayOfWeek() + " " + searchDate);
		}
	}

	@Override
	public void printShiftsByWeek() {
		System.out.println("1) Future Shifts\n2) Past Shifts");
		int choice = IFunctionsLab.IntegerChoiceRange(1, 2);

		LocalDate today = LocalDate.now();
		LocalDate startOfWeek;
		LocalDate endOfWeek;

		if (choice == 1) {
			startOfWeek = today.plusDays(1);
			endOfWeek = today.plusDays(7);
		} else {
			startOfWeek = today.minusDays(6);
			endOfWeek = today;
		}

		boolean found = false;
		for (Shift shift : getShiftsBetween(startOfWeek, endOfWeek)) {
			System.out.println(shift);
			found = true;
		}

		if (!found) {
			System.out.println("No shifts found in the specified week.");
		}
	}

	@Override
	public void printShiftsByMonth() {
		System.out.println("Enter month (yyyy-MM): ");
		int year = IFunctionsLab.shiftYearInput();
		int month = IFunctionsLab.monthInput();
		String monthStr = String.format("%04d-%02d", year, month);
		LocalDate searchDate = LocalDate.of(year, month, 1);
		LocalDate startOfMonth = searchDate.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate endOfMonth = searchDate.with(TemporalAdjusters.lastDayOfMonth());
		boolean found = false;

		for (Shift shift : getShiftsBetween(startOfMonth, endOfMonth)) {
			System.out.println(shift);
			found = true;
		}

		if (!found) {
			System.out.println("No shifts found in " + monthStr);
		}
	}

	@Override
	public void inlayEmployee() {
		System.out.println("Enter Employee ID: ");
		listEmployees(); // Print employees for selection
		String employeeId = IFunctionsLab.inputId();
		Employee employee = employeeDataBase.findEmployeeById(employeeId);

		if (employee == null) {
			System.out.println("Employee not found.");
			return;
		}
		printShifts();

		System.out.println("Enter Shift ID: ");
		int shiftId = IFunctionsLab.IntegerChoiceRange(0, Shift.counter - 1);
		try {
			assignEmployee(findShiftById(shiftId), employee);
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println(e.getMessage());
		}
	}

	private void listEmployees() {
		// Print all employees for selection
		for (Employee employee : employeeDataBase.getAllEmployees()) {
			System.out.println(employee);
		}
	}

	@Override
	public void addShift() {
		Shift newShift = createShift();
		System.out.println("Shift added: " + newShift.getId());
	}

	@Override
	public void updateShift() {
		System.out.println("Enter shift ID to update: ");
		int shiftId = IFunctionsLab.IntegerChoiceRange(0, Shift.counter - 1);
		Shift shiftToUpdate = findShiftById(shiftId);

		if (shiftToUpdate != null) {
			System.out.println("Enter new start time (yyyy-MM-dd HH:mm:ss): ");
			String startTime = IFunctionsLab.formatTime();
			System.out.println("Enter new end time (yyyy-MM-dd HH:mm:ss): ");
			String endTime = IFunctionsLab.formatTime();

			LocalDateTime newStart = LocalDateTime.parse(startTime, DATE_TIME);
			LocalDateTime newEnd = LocalDateTime.parse(endTime, DATE_TIME);

			try {
				updateShiftTimes(shiftToUpdate, newStart, newEnd);
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage() + " Returning to main menu.");
			}
		} else {
			System.out.println("Shift not found.");
		}
	}

	@Override
	public void removeShift() {
		System.out.println("Enter shift ID to remove: ");
		int shiftId = IFunctionsLab.IntegerChoiceRange(0, Shift.counter - 1);
		Shift shiftToRemove = findShiftById(shiftId);

		if (shiftToRemove != null) {
			deleteShift(shiftToRemove);
			System.out.println("Shift removed.");
		} else {
			System.out.println("Shift not found.");
		}
	}

	public void printShifts() {
		if (shifts.isEmpty()) {
			System.out.println("No shifts available.");
			return;
		}

		for (Shift shift : shifts) {
			System.out.println(shift);
		}
	}

	@Override
	public void registerObserver(Observer o) {
		if (!observers.contains(o)) {
			observers.add(o);
		}
	}

	@Override
	public void removeObserver(Observer o) {
		observers.remove(o);
	}

	@Override
	public void notifyObservers(String message) {
		for (Observer observer : observers) {
			observer.update(message);
		}
	}

	public void notifyAdminsAndSupervisors(String message) {
		for (Observer observer : observers) {
			if (observer instanceof Employee) {
				Employee employee = (Employee) observer;
				if (employee.getRank() == Rank.ADMINISTRATOR || employee.getRank() == Rank.SUPERVISER) {
					observer.update(message);
				}
			}
		}
	}

	public void searchShiftsByDate(String date) {
		LocalDate searchDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
		boolean found = false;
		for (Shift shift : getShiftsBetween(searchDate, searchDate)) {
			System.out.println(shift);
			found = true;
		}
		if (!found) {
			System.out.println("No shifts found on " + date);
		}
	}

	public void searchShiftsById(int shiftId) {
		Shift shift = findShiftById(shiftId);
		if (shift != null) {
			System.out.println(shift);
		} else {
			System.out.println(" shift Not found or removed with ID: " + shiftId);
		}
	}

	public void searchShiftsByEmployeeId(String employeeId) {
		boolean found = false;
		for (Shift shift : shifts) {
			if (shift.getEmployee() != null && shift.getEmployee().getId().equals(employeeId)) {
				System.out.println(shift);
				found = true;
			}
		}
		if (!found) {
			System.out.println("No shifts found for Employee ID: " + employeeId);
		}
	}

	public void printShiftsForEmployee(Employee employee) {
		System.out.println("Shifts for employee: " + employee.toString());
		List<Shift> employeeShifts = getShiftsForEmployee(employee);
		for (Shift shift : employeeShifts) {
			System.out.println(shift);
		}
		if (employeeShifts.isEmpty()) {
			System.out
					.println("No shifts found for employee: " + employee.getFirstName() + " " + employee.getLastName());
		}
	}

	private void printEmployeeShiftsBetween(Employee employee, LocalDate from, LocalDate to, String period) {
		boolean found = false;
		for (Shift shift : getShiftsBetween(from, to)) {
			if (shift.getEmployee() != null && shift.getEmployee().equals(employee)) {
				System.out.println(shift);
				found = true;
			}
		}
		if (!found) {
			System.out.println("No shifts found for employee: " + employee.getFirstName() + " " + employee.getLastName()
					+ " for " + period + ".");
		}
	}

	public void printShiftsForEmployeeByDay(Employee employee) {
		LocalDate today = LocalDate.now();
		printEmployeeShiftsBetween(employee, today, today, "today");
	}

	public void printShiftsForEmployeeByWeek(Employee employee) {
		LocalDate today = LocalDate.now();
		LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
		LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
		printEmployeeShiftsBetween(employee, startOfWeek, endOfWeek, "this week");
	}

	public void printShiftsForEmployeeByMonth(Employee employee) {
		LocalDate today = LocalDate.now();
		LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
		printEmployeeShiftsBetween(employee, startOfMonth, endOfMonth, "this month");
	}
}
