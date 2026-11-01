package SingleObjectClasses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import AbstractClasses.Person;
import Enums.Rank;
import Interfaces.Observer;

public class Employee extends Person implements Observer {
	private String role;
	private Rank rank;
	private String password;
	// Messages received as an observer of the shift manager
	private final List<String> notifications = new ArrayList<>();

	public Employee(String id, String FName, String LName, Date date, String role, Rank rank) {
		super(id, FName, LName, date);
		this.rank = rank;
		this.role = role;
		this.password = id;
	}

	public String getPassword() {
		return password;
	}

	// The username is always the employee's current first name
	public String getUser() {
		return getFirstName();
	}

	public List<String> getNotifications() {
		return Collections.unmodifiableList(notifications);
	}

	@Override
	public String toString() {
		return "Employee [Role=" + role + ", Rank=" + rank + ", First Name=" + getFirstName() + ", Last Name="
				+ getLastName() + ", Id=" + getId() + ", Birth Day=" + getBDay() + "]";
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Rank getRank() {
		return rank;
	}

	public void setRank(Rank rank) {
		this.rank = rank;
	}

	@Override
	public void update(String message) {
		String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		notifications.add(time + "  " + message);
		System.out.println("Notification for " + getFirstName() + " " + getLastName() + ": " + message);
	}
}
