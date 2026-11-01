package Manegers;

import java.util.ArrayList;
import java.util.Collections;

import SingleObjectClasses.*;
import Enums.*;
import Factories.*;
import Interfaces.*;
import java.util.List;

public class EmployeeDataBase implements IEmployeeManagement {
	private ArrayList<Employee> workerList = new ArrayList<>();
	private DataBase db = DataBase.getInstance();
	private Employee loggedInEmployee;
	private ShiftManager shiftManager;

	public EmployeeDataBase(ShiftManager shiftManager) {
		this.shiftManager = shiftManager;

		// Initialize with predefined employees
		Employee admin = EmployeeFactory.createEmployee("123456789", "Avi", "Shak", new Date(27, 3, 1977), "CEO",
				Rank.ADMINISTRATOR);
		Employee supervisor = EmployeeFactory.createEmployee("987654321", "John", "Carter", new Date(15, 5, 1985),
				"Supervisor", Rank.SUPERVISER);
		Employee worker1 = EmployeeFactory.createEmployee("111111111", "Alice", "Smith", new Date(12, 12, 1990),
				"Cashier", Rank.WORKER);
		Employee worker2 = EmployeeFactory.createEmployee("222222222", "Bob", "Johnson", new Date(20, 6, 1992),
				"Barista", Rank.WORKER);
		Employee worker3 = EmployeeFactory.createEmployee("333333333", "Charlie", "Brown", new Date(5, 8, 1994),
				"Cook", Rank.WORKER);
		Employee worker4 = EmployeeFactory.createEmployee("444444444", "David", "Williams", new Date(25, 9, 1988),
				"Cashier", Rank.WORKER);
		Employee worker5 = EmployeeFactory.createEmployee("555555555", "Eve", "Davis", new Date(30, 4, 1991), "Barista",
				Rank.WORKER);

		for (Employee employee : new Employee[] { admin, supervisor, worker1, worker2, worker3, worker4, worker5 }) {
			workerList.add(employee);
			db.addUser(employee.getPassword(), employee.getUser());
		}

		shiftManager.registerObserver(admin);
		shiftManager.registerObserver(supervisor);
	}

	// ------------------------------------------------------------------
	// Core operations (no console input), used by the console menus and the GUI
	// ------------------------------------------------------------------

	public Employee authenticate(String username, String password) {
		if (username == null || password == null) {
			return null;
		}
		if (db.isUser(password.trim(), username.trim())) {
			loggedInEmployee = findEmployeeByUsername(username.trim());
			return loggedInEmployee;
		}
		return null;
	}

	public void logOut() {
		loggedInEmployee = null;
	}

	public Employee addEmployee(String id, String firstName, String lastName, Date birthDay, String role, Rank rank) {
		if (!IFunctionsLab.isValidId(id)) {
			throw new IllegalArgumentException("ID must be exactly 9 digits.");
		}
		if (!IFunctionsLab.isValidAlphabetString(firstName) || !IFunctionsLab.isValidAlphabetString(lastName)) {
			throw new IllegalArgumentException("First and last name must use English letters only.");
		}
		if (role == null || role.trim().isEmpty()) {
			throw new IllegalArgumentException("Role is required.");
		}
		if (rank == null) {
			throw new IllegalArgumentException("Rank is required.");
		}
		if (findEmployeeById(id) != null) {
			throw new IllegalArgumentException("Employee with ID " + id + " already exists.");
		}
		if (findEmployeeByUsername(firstName) != null) {
			throw new IllegalArgumentException(
					"An employee named " + firstName + " already exists (the first name is the username).");
		}

		Employee newEmployee = EmployeeFactory.createEmployee(id, firstName, lastName, birthDay, role.trim(), rank);
		workerList.add(newEmployee);
		db.addUser(newEmployee.getPassword(), newEmployee.getUser());

		// Register as an observer if the rank is SUPERVISER or ADMINISTRATOR
		if (receivesNotifications(rank)) {
			shiftManager.registerObserver(newEmployee);
		}
		shiftManager.notifyAdminsAndSupervisors(
				"New employee " + newEmployee.getFirstName() + " " + newEmployee.getLastName() + " has been added.");
		return newEmployee;
	}

	// Ranks the logged-in employee may give when updating the target employee
	public List<Rank> allowedRanksFor(Employee target) {
		List<Rank> ranks = new ArrayList<>();
		if (target.getRank() == Rank.ADMINISTRATOR) {
			ranks.add(Rank.ADMINISTRATOR); // an administrator's rank cannot be changed
		} else if (loggedInEmployee != null && loggedInEmployee.getRank() == Rank.ADMINISTRATOR) {
			Collections.addAll(ranks, Rank.values());
		} else {
			ranks.add(Rank.WORKER);
			ranks.add(Rank.SUPERVISER);
		}
		return ranks;
	}

	public boolean canEdit(Employee target) {
		if (loggedInEmployee == null || loggedInEmployee.getRank() == Rank.WORKER) {
			return false;
		}
		// only administrators may edit administrators
		return target.getRank() != Rank.ADMINISTRATOR || loggedInEmployee.getRank() == Rank.ADMINISTRATOR;
	}

	public void updateEmployee(Employee employee, String firstName, String lastName, Date birthDay, String role,
			Rank newRank) {
		if (employee == null || !workerList.contains(employee)) {
			throw new IllegalArgumentException("Employee not found.");
		}
		if (!canEdit(employee)) {
			throw new IllegalStateException("Only an administrator can update an administrator.");
		}
		if (!IFunctionsLab.isValidAlphabetString(firstName) || !IFunctionsLab.isValidAlphabetString(lastName)) {
			throw new IllegalArgumentException("First and last name must use English letters only.");
		}
		if (role == null || role.trim().isEmpty()) {
			throw new IllegalArgumentException("Role is required.");
		}
		if (!allowedRanksFor(employee).contains(newRank)) {
			throw new IllegalArgumentException("You cannot give " + employee.getFirstName() + " the rank " + newRank
					+ ".");
		}
		Employee sameName = findEmployeeByUsername(firstName);
		if (sameName != null && sameName != employee) {
			throw new IllegalArgumentException(
					"An employee named " + firstName + " already exists (the first name is the username).");
		}

		// Update the same object, so the employee's shifts stay linked to them
		db.removeUser(employee.getPassword(), employee.getUser());
		employee.setFirstName(firstName);
		employee.setLastName(lastName);
		employee.setBDay(birthDay);
		employee.setRole(role.trim());
		employee.setRank(newRank);
		db.addUser(employee.getPassword(), employee.getUser());

		if (receivesNotifications(newRank)) {
			shiftManager.registerObserver(employee);
		} else {
			shiftManager.removeObserver(employee);
		}
		shiftManager.notifyAdminsAndSupervisors(
				"Employee " + employee.getFirstName() + " " + employee.getLastName() + " has been updated.");
	}

	public void deleteEmployee(Employee employee) {
		if (employee == null || !workerList.contains(employee)) {
			throw new IllegalArgumentException("Employee not found.");
		}
		if (employee.getRank() == Rank.ADMINISTRATOR && countAdmins() == 1) {
			throw new IllegalStateException("Cannot remove the last administrator.");
		}
		if (employee.equals(loggedInEmployee)) {
			throw new IllegalStateException("You cannot remove yourself from the system.");
		}
		workerList.remove(employee);
		db.removeUser(employee.getPassword(), employee.getUser());
		shiftManager.removeObserver(employee);
		shiftManager.notifyAdminsAndSupervisors(
				"Employee " + employee.getFirstName() + " " + employee.getLastName() + " has been removed.");
	}

	private boolean receivesNotifications(Rank rank) {
		return rank == Rank.SUPERVISER || rank == Rank.ADMINISTRATOR;
	}

	// ------------------------------------------------------------------
	// Console operations
	// ------------------------------------------------------------------

	@Override
	public boolean systemEntry() {
		System.out.println("Enter username: ");
		String username = IFunctionsLab.stringInput();
		String password = IFunctionsLab.inputPass();
		return authenticate(username, password) != null;
	}

	public Employee getLoggedInEmployee() {
		return loggedInEmployee;
	}

	private Employee findEmployeeByUsername(String username) {
		for (Employee employee : workerList) {
			if (employee.getUser().equals(username)) {
				return employee;
			}
		}
		return null;
	}

	@Override
	public Employee findEmployee() {
		System.out.println("Enter employee ID: ");
		String id = IFunctionsLab.inputId();
		Employee employee = findEmployeeById(id);
		System.out.println(employee != null ? employee : "Employee not found.");
		return employee;
	}

	public Employee findEmployeeById(String employeeId) {
		for (Employee employee : workerList) {
			if (employee.getId().equals(employeeId)) {
				return employee;
			}
		}
		return null;
	}

	public List<Employee> getAllEmployees() {
		return Collections.unmodifiableList(workerList);
	}

	@Override
	public void addNewEmployee() {
		String id = IFunctionsLab.inputId();
		if (findEmployeeById(id) != null) {
			System.out.println("Employee with ID " + id + " already exists. The employee was not added.");
			return;
		}
		String firstName = IFunctionsLab.firstNameInput();
		if (findEmployeeByUsername(firstName) != null) {
			System.out.println("Employee with this first name already exists. The employee was not added. "
					+ "(The first name is the username)");
			return;
		}
		String lastName = IFunctionsLab.lastNameInput();
		System.out.println("Enter date of birth:");
		Date date = IFunctionsLab.birthDateInput();
		System.out.println("Enter role:");
		String role = IFunctionsLab.stringInput();

		System.out.println("Enter new rank(indx):\n 1)WORKER\n 2)SUPERVISER\n 3)ADMINISTRATOR");
		Rank rank = Rank.get(IFunctionsLab.IntegerChoiceRange(1, 3) - 1);

		try {
			Employee newEmployee = addEmployee(id, firstName, lastName, date, role, rank);
			if (newEmployee.getRank() == Rank.WORKER) {
				System.out.println("Employee " + newEmployee.getFirstName() + " " + newEmployee.getLastName()
						+ " is a WORKER and will not receive notifications.");
			}
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void updateEmployee() {
		Employee employee = findEmployee();
		if (employee == null) {
			return;
		}
		// Check permissions before asking for any details
		if (!canEdit(employee)) {
			System.out.println("Only an administrator can update an administrator.");
			return;
		}

		String firstName = IFunctionsLab.firstNameInput();
		String lastName = IFunctionsLab.lastNameInput();
		System.out.println("Enter date of birth:");
		Date date = IFunctionsLab.birthDateInput();
		System.out.println("Enter new role:");
		String role = IFunctionsLab.stringInput();

		List<Rank> allowed = allowedRanksFor(employee);
		Rank newRank = allowed.get(0);
		if (allowed.size() == 1) {
			System.out.println("Rank stays " + newRank + ".");
		} else {
			System.out.println("Enter new rank(indx):");
			for (int i = 0; i < allowed.size(); i++) {
				System.out.println(" " + (i + 1) + ")" + allowed.get(i));
			}
			newRank = allowed.get(IFunctionsLab.IntegerChoiceRange(1, allowed.size()) - 1);
		}

		try {
			updateEmployee(employee, firstName, lastName, date, role, newRank);
			System.out.println("Employee updated.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void removeEmployee() {
		Employee employee = findEmployee();
		if (employee != null) {
			try {
				deleteEmployee(employee);
				System.out.println("Employee removed successfully.");
			} catch (IllegalStateException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@Override
	public void printAllEmployee() {
		for (Employee employee : workerList) {
			System.out.println(employee);
		}
	}

	private int countAdmins() {
		int count = 0;
		for (Employee employee : workerList) {
			if (employee.getRank() == Rank.ADMINISTRATOR) {
				count++;
			}
		}
		return count;
	}
}
