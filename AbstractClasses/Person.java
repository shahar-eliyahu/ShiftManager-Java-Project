package AbstractClasses;

import SingleObjectClasses.Date;

public abstract class Person {
	private String id;
	private String firstName;
	private String lastName;
	private Date BDay;

	public Person(String id, String FName, String LName, Date date) {
		this.id = id;
		this.firstName = FName;
		this.lastName = LName;
		this.BDay = date;
	}

	@Override
	public String toString() {
		return "";
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getId() {
		return id;
	}

	public Date getBDay() {
		return BDay;
	}

	public void setBDay(Date bDay) {
		this.BDay = bDay;
	}

}
