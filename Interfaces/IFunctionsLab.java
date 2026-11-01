package Interfaces;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Scanner;

import SingleObjectClasses.Date;

public interface IFunctionsLab {
	public static Scanner scan = new Scanner(System.in);

	public static int IntegerChoiceRange(int min, int max) {
		int choice = 0;
		do {
			choice = checkInputStringToInt();
			System.out.println((choice < min || choice > max) ? "Invalid Option, Try Again" : "Good.");
		} while (choice < min || choice > max);
		return choice;
	}

	public static String inputId() {
		System.out.println("Enter ID [9 digits]:");
		return nineDigitInput("Invalid ID, try again");
	}

	public static String inputPass() {
		System.out.println("Enter password [9 digits]:");
		return nineDigitInput("Invalid password, try again");
	}

	// Reads the value as text so IDs that start with 0 keep their leading zeros
	private static String nineDigitInput(String errorMessage) {
		String value;
		do {
			value = scan.nextLine().trim();
			System.out.println(isValidId(value) ? "Good." : errorMessage);
		} while (!isValidId(value));
		return value;
	}

	public static boolean isValidId(String value) {
		return value != null && value.matches("^\\d{9}$");
	}

	public static int dayInput(int year, int month) {
		int lastDay = YearMonth.of(year, month).lengthOfMonth();
		System.out.println("Enter Day [1 - " + lastDay + "]:");
		return IntegerChoiceRange(1, lastDay);
	}

	public static int monthInput() {
		System.out.println("Enter Month [1 - 12]:");
		int month = IntegerChoiceRange(1, 12);
		return month;
	}

	// Birth years
	public static int yearInput() {
		int currentYear = LocalDate.now().getYear();
		System.out.println("Enter Year [1950 - " + currentYear + "]:");
		return IntegerChoiceRange(1950, currentYear);
	}

	// Shift years: last year up to two years ahead
	public static int shiftYearInput() {
		int currentYear = LocalDate.now().getYear();
		System.out.println("Enter Year [" + (currentYear - 1) + " - " + (currentYear + 2) + "]:");
		return IntegerChoiceRange(currentYear - 1, currentYear + 2);
	}

	public static int hourInput() {
		System.out.println("Enter hour [0 - 23]:");
		int hour = IntegerChoiceRange(0, 23);
		return hour;
	}

	public static int minuteInput() {
		System.out.println("Enter minute [0 - 59]:");
		int minute = IntegerChoiceRange(0, 59);
		return minute;
	}

	public static int secondsInput() {
		System.out.println("Enter seconds [0 - 59]:");
		int seconds = IntegerChoiceRange(0, 59);
		return seconds;
	}

	public static Date birthDateInput() {
		int year = yearInput();
		int month = monthInput();
		int day = dayInput(year, month);
		return new Date(day, month, year);
	}

	public static String[] DateToStringInput() {
		String[] str;
		str = scan.nextLine().split("/");
		return str;
	}

	public static String formatDate() {
		int year = shiftYearInput();
		int month = monthInput();
		int day = dayInput(year, month);
		return String.format("%04d-%02d-%02d", year, month, day); // Ensure leading zeros
	}

	public static String formatTime() {
		int year = shiftYearInput();
		int month = monthInput();
		int day = dayInput(year, month);
		int hour = hourInput();
		int minute = minuteInput();
		int seconds = secondsInput();
		return String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, seconds); // Ensure
																										// leading zeros
	}

	public static int checkInputStringToInt() {
		boolean success = false;
		int num = 0;
		while (!success) {
			try {
				String choiceText = scan.nextLine();
				num = Integer.parseInt(choiceText.trim());
				success = true;
			} catch (NumberFormatException inputTextException) {
				System.out.println("You should type a number!");
			}
		}
		return num;
	}

	public static String lastNameInput() {
		System.out.println("Please enter last name:");
		return stringInput();
	}

	public static String firstNameInput() {
		System.out.println("Please enter first name:");
		return stringInput();
	}

	public static String stringInput() {
		String str;
		do {
			str = scan.nextLine().trim();
			if (!isValidAlphabetString(str)) {
				System.out.println("Use English letters only, try again:");
			}
		} while (!isValidAlphabetString(str));
		return str;
	}

	public static boolean isValidAlphabetString(String input) {
		// Check if the input contains only alphabets
		return input != null && input.matches("^[a-zA-Z]+$");
	}
}
