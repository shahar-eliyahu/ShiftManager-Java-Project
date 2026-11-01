package WorkPlaceManeger;

import Manegers.*;
import SingleObjectClasses.*;
import Enums.*;
import Interfaces.*;
import SingleObjectClasses.Shift;

public class ShiftManagementSystem {
	private static EmployeeDataBase employeeDataBase;
	private static ShiftManager shiftManager;
	private static Employee loggedInEmployee = null;

	public static void main(String[] args) {
		// Initialize ShiftManager first without EmployeeDataBase reference
		shiftManager = new ShiftManager(null);

		// Initialize EmployeeDataBase with the ShiftManager reference
		employeeDataBase = new EmployeeDataBase(shiftManager);

		// Now set the EmployeeDataBase in the ShiftManager after it's fully constructed
		shiftManager.setEmployeeDataBase(employeeDataBase);

		boolean running = true;
		while (running) {
			System.out.println("Welcome to the Shift Management System");
			System.out.println("1. Log In");
			System.out.println("0. Shut Down System");
			System.out.print("Enter your choice: ");
			int initialChoice = IFunctionsLab.IntegerChoiceRange(0, 1);

			if (initialChoice == 0) {
				System.out.println("System shutting down. Goodbye!");
				break;
			}

			if (employeeDataBase.systemEntry()) {
				// After successful login, set the logged-in employee
				loggedInEmployee = employeeDataBase.getLoggedInEmployee();
				if (loggedInEmployee != null) {
					running = displayMainMenu();
				} else {
					System.out.println("Login failed. Please try again.");
				}
			} else {
				System.out.println("Invalid login. Try again.");
			}
		}
	}

	private static boolean displayMainMenu() {
		boolean loggedIn = true;

		while (loggedIn) {
			System.out.println("Main Menu:");
			System.out.println("1. Check In");
			System.out.println("2. Check Out");
			System.out.println("3. Undo Last Check-In/Out");
			System.out.println("4. Print Shifts");
			if (loggedInEmployee != null && loggedInEmployee.getRank() != Rank.WORKER) {
				System.out.println("5. Manage Shifts");
				System.out.println("6. Manage Employees");
			}
			System.out.println("0. Log Out");
			System.out.println("-1. Shut Down System");
			System.out.print("Enter your choice: ");

			int choice = IFunctionsLab.IntegerChoiceRange(-1,
					(loggedInEmployee != null && loggedInEmployee.getRank() == Rank.WORKER) ? 4 : 6);

			switch (choice) {
			case 1:
				try {
					shiftManager.checkIn(loggedInEmployee);
				} catch (IllegalStateException e) {
					System.out.println(e.getMessage());
				}
				break;
			case 2:
				try {
					shiftManager.checkOut(loggedInEmployee);
				} catch (IllegalStateException e) {
					System.out.println(e.getMessage());
				}
				break;
			case 3:
				try {
					System.out.println(shiftManager.undoLastCheckInOut(loggedInEmployee));
				} catch (IllegalStateException e) {
					System.out.println(e.getMessage());
				}
				break;
			case 4:
				if (shiftManager.hasShifts()) {
					printShiftsMenu();
				} else {
					System.out.println("No shifts available in the system.");
				}
				break;
			case 5:
				if (loggedInEmployee != null && loggedInEmployee.getRank() != Rank.WORKER) {
					manageShiftsMenu();
				} else {
					System.out.println("Invalid choice. Please try again.");
				}
				break;
			case 6:
				if (loggedInEmployee != null && loggedInEmployee.getRank() != Rank.WORKER) {
					manageEmployeesMenu();
				} else {
					System.out.println("Invalid choice. Please try again.");
				}
				break;
			case 0:
				loggedIn = false;
				employeeDataBase.logOut();
				System.out.println("Logged out successfully.");
				break;
			case -1:
				System.out.println("Confirm shutdown? (yes/no)");
				String confirmation = IFunctionsLab.stringInput();
				if ("yes".equalsIgnoreCase(confirmation)) {
					return false; // Ends the system loop
				}
				break;
			default:
				System.out.println("Invalid choice. Please try again.");
			}
		}
		return true; // Continue system running after logout
	}

	private static void printShiftsMenu() {
		if (!shiftManager.hasShifts()) {
			System.out.println("No shifts available in the system.");
			return;
		}

		if (loggedInEmployee.getRank() == Rank.WORKER) {
			printWorkerShiftsMenu();
		} else {
			printSupervisorOrAdminShiftsMenu();
		}
	}

	private static void printWorkerShiftsMenu() {
		System.out.println("Print Your Shifts:");
		System.out.println("1. Print Shifts by Day");
		System.out.println("2. Print Shifts by Week");
		System.out.println("3. Print Shifts by Month");
		System.out.println("0. Back to Main Menu");
		System.out.print("Enter your choice: ");

		int choice = IFunctionsLab.IntegerChoiceRange(0, 3);

		switch (choice) {
		case 1:
			shiftManager.printShiftsForEmployeeByDay(loggedInEmployee);
			break;
		case 2:
			shiftManager.printShiftsForEmployeeByWeek(loggedInEmployee);
			break;
		case 3:
			shiftManager.printShiftsForEmployeeByMonth(loggedInEmployee);
			break;
		case 0:
			System.out.println("Returning to Main Menu...");
			return;
		default:
			System.out.println("Invalid choice. Please try again.");
		}
	}

	private static void printSupervisorOrAdminShiftsMenu() {
		System.out.println("Print Shifts:");
		System.out.println("1. Print Shifts by Day");
		System.out.println("2. Print Shifts by Week");
		System.out.println("3. Print Shifts by Month");
		System.out.println("4. Print Shifts for Specific Employee");
		System.out.println("5. Print All Shifts");
		System.out.println("0. Back to Main Menu");
		System.out.print("Enter your choice: ");

		int choice = IFunctionsLab.IntegerChoiceRange(0, 5);

		switch (choice) {
		case 1:
			shiftManager.printShiftsByDay();
			break;
		case 2:
			shiftManager.printShiftsByWeek();
			break;
		case 3:
			shiftManager.printShiftsByMonth();
			break;
		case 4:
			System.out.println("Enter Employee ID: ");
			employeeDataBase.printAllEmployee(); // Print employees for easier selection
			String employeeId = IFunctionsLab.inputId();
			Employee employee = employeeDataBase.findEmployeeById(employeeId);
			if (employee != null) {
				shiftManager.printShiftsForEmployee(employee);
			} else {
				System.out.println("Employee not found.");
			}
			break;
		case 5:
			shiftManager.printShifts();
			break;
		case 0:
			System.out.println("Returning to Main Menu...");
			return;
		default:
			System.out.println("Invalid choice. Please try again.");
		}
	}

	private static void manageShiftsMenu() {

		System.out.println("Manage Shifts:");
		System.out.println("1. Inlay Employee to Shift");
		System.out.println("2. Search Shifts");
		if (loggedInEmployee != null && loggedInEmployee.getRank() == Rank.ADMINISTRATOR) {
			System.out.println("3. Add Shift");
			System.out.println("4. Delete Shift");
		}
		System.out.println("5. Update Shift");
		System.out.println("6. Print Shifts");
		System.out.println("0. Back to Main Menu");
		System.out.print("Enter your choice: ");

		int choice = IFunctionsLab.IntegerChoiceRange(0, 6);

		switch (choice) {
		case 1:
			if (!shiftManager.hasShifts()) {
				System.out.println("No shifts available in the system.");
				return;
			}
			shiftManager.inlayEmployee();
			break;
		case 2:
			if (!shiftManager.hasShifts()) {
				System.out.println("No shifts available in the system.");
				return;
			}
			searchShiftsMenu();
			break;
		case 3:
			if (loggedInEmployee != null && loggedInEmployee.getRank() == Rank.ADMINISTRATOR) {
				shiftManager.addShift();
			} else {
				System.out.println("Invalid choice. Please try again.");
			}
			break;
		case 4:
			if (!shiftManager.hasShifts()) {
				System.out.println("No shifts available in the system.");
				return;
			}
			if (loggedInEmployee != null && loggedInEmployee.getRank() == Rank.ADMINISTRATOR) {
				shiftManager.removeShift();
			} else {
				System.out.println("Invalid choice. Please try again.");
			}
			break;
		case 5:
			if (!shiftManager.hasShifts()) {
				System.out.println("No shifts available in the system.");
				return;
			}
			shiftManager.updateShift();
			break;
		case 6:
			if (!shiftManager.hasShifts()) {
				System.out.println("No shifts available in the system.");
				return;
			}
			shiftManager.printShifts();
			break;
		case 0:
			System.out.println("Returning to Main Menu...");
			return;
		default:
			System.out.println("Invalid choice. Please try again.");
		}
	}

	private static void searchShiftsMenu() {
		if (!shiftManager.hasShifts()) {
			System.out.println("No shifts available in the system.");
			return;
		}

		System.out.println("Search Shifts by:");
		System.out.println("1. Date");
		System.out.println("2. Shift ID");
		System.out.println("3. Employee ID");
		System.out.println("0. Back to Manage Shifts");
		System.out.print("Enter your choice: ");

		int choice = IFunctionsLab.IntegerChoiceRange(0, 3);

		switch (choice) {
		case 1:
			System.out.println("Enter date (yyyy-MM-dd): ");
			String date = IFunctionsLab.formatDate();
			shiftManager.searchShiftsByDate(date);
			break;
		case 2:
			shiftManager.printShifts();
			System.out.println("Enter Shift ID: ");
			int shiftId = IFunctionsLab.IntegerChoiceRange(0, Shift.counter - 1);
			shiftManager.searchShiftsById(shiftId);
			break;
		case 3:
			System.out.println("Enter Employee ID: ");
			employeeDataBase.printAllEmployee(); // Print employees for easier selection
			String employeeId = IFunctionsLab.inputId();
			shiftManager.searchShiftsByEmployeeId(employeeId);
			break;
		case 0:
			System.out.println("Returning to Manage Shifts...");
			return;
		default:
			System.out.println("Invalid choice. Please try again.");
		}
	}

	private static void manageEmployeesMenu() {
		System.out.println("Manage Employees:");
		System.out.println("1. Search Employee");
		if (loggedInEmployee != null && loggedInEmployee.getRank() == Rank.ADMINISTRATOR) {
			System.out.println("2. Add Employee");
			System.out.println("3. Delete Employee");
		}
		System.out.println("4. Update Employee");
		System.out.println("5. Print All Employees");
		System.out.println("0. Back to Main Menu");
		System.out.print("Enter your choice: ");

		int choice = IFunctionsLab.IntegerChoiceRange(0, 5);

		switch (choice) {
		case 1:
			employeeDataBase.findEmployee();
			break;
		case 2:
			if (loggedInEmployee != null && loggedInEmployee.getRank() == Rank.ADMINISTRATOR) {
				employeeDataBase.addNewEmployee();
			} else {
				System.out.println("Invalid choice. Please try again.");
			}
			break;
		case 3:
			if (loggedInEmployee != null && loggedInEmployee.getRank() == Rank.ADMINISTRATOR) {
				employeeDataBase.removeEmployee();
			} else {
				System.out.println("Invalid choice. Please try again.");
			}
			break;
		case 4:
			employeeDataBase.updateEmployee();
			break;
		case 5:
			employeeDataBase.printAllEmployee();
			break;
		case 0:
			System.out.println("Returning to Main Menu...");
			return;
		default:
			System.out.println("Invalid choice. Please try again.");
		}
	}
}
