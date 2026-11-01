package gui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import Manegers.EmployeeDataBase;
import Manegers.ShiftManager;
import SingleObjectClasses.Employee;
import SingleObjectClasses.Shift;

/**
 * Sample rota for the dashboard: this week and the start of next week, created
 * through the ShiftManager (createShift and checkIn).
 */
final class DemoData {
	private DemoData() {
	}

	private static final LocalTime[][] TEMPLATES = { { LocalTime.of(7, 0), LocalTime.of(15, 0) },
			{ LocalTime.of(10, 0), LocalTime.of(18, 0) }, { LocalTime.of(15, 0), LocalTime.of(23, 0) } };

	// Employee IDs per weekday (Monday first) for the morning, middle and evening shifts
	private static final String[][] THIS_WEEK = {
			{ "111111111", "222222222", "111111111", "333333333", "111111111", "555555555", "222222222" },
			{ "987654321", "333333333", "987654321", "222222222", "987654321", "333333333", "987654321" },
			{ "444444444", "555555555", "444444444", "555555555", "333333333", "444444444", "555555555" } };
	private static final String[][] NEXT_WEEK = { { "111111111", "222222222", "555555555" },
			{ "987654321", null, "987654321" }, { "444444444", "333333333", "444444444" } };

	static void seed(AppContext context) {
		ShiftManager shifts = context.shiftManager;
		EmployeeDataBase employees = context.employeeDataBase;
		LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDateTime now = LocalDateTime.now();

		for (int day = 0; day < 7; day++) {
			for (int t = 0; t < TEMPLATES.length; t++) {
				add(shifts, employees.findEmployeeById(THIS_WEEK[t][day]), monday.plusDays(day), t, now);
			}
		}
		for (int day = 0; day < 3; day++) {
			for (int t = 0; t < TEMPLATES.length; t++) {
				String id = NEXT_WEEK[t][day];
				add(shifts, id == null ? null : employees.findEmployeeById(id), monday.plusWeeks(1).plusDays(day), t,
						now);
			}
		}
	}

	private static void add(ShiftManager shifts, Employee employee, LocalDate date, int template, LocalDateTime now) {
		LocalDateTime start = date.atTime(TEMPLATES[template][0]);
		LocalDateTime end = date.atTime(TEMPLATES[template][1]);
		if (employee != null && !start.isAfter(now) && end.isAfter(now)) {
			// Happening right now: the employee checked in at the start of the shift
			Shift shift = shifts.checkIn(employee);
			shift.setStartDateTime(start);
		} else {
			shifts.createShift(start, end, employee);
		}
	}
}
