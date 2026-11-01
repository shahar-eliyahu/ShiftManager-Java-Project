package SingleObjectClasses;

import java.util.Stack;

/**
 * Caretaker of the Memento pattern. Before every check-in or check-out of one
 * employee, the shift's state is saved here, so the last actions can be undone
 * in reverse order.
 */
public class CheckInOutRecord {

	private final Employee employee;
	private final Stack<Shift> changedShifts = new Stack<>();
	private final Stack<ShiftMemento> shiftHistory = new Stack<>();

	public CheckInOutRecord(Employee employee) {
		this.employee = employee;
	}

	// Save the shift's state before it changes
	public void save(Shift shift) {
		changedShifts.push(shift);
		shiftHistory.push(shift.saveToMemento());
	}

	public boolean canUndo() {
		return !shiftHistory.isEmpty();
	}

	// Restores the last saved state and returns the shift that was restored
	public Shift undo() {
		if (shiftHistory.isEmpty()) {
			return null;
		}
		Shift shift = changedShifts.pop();
		shift.restoreFromMemento(shiftHistory.pop());
		return shift;
	}

	public Employee getEmployee() {
		return employee;
	}
}
