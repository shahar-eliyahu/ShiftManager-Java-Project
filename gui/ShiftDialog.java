package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;

import SingleObjectClasses.Employee;
import SingleObjectClasses.Shift;

/** Create a shift (administrators) or edit its times and assign an employee (supervisors and administrators). */
final class ShiftDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final AppContext context;
	private final Shift shift; // null when creating
	private final JSpinner date = spinner("EEE dd/MM/yyyy", Calendar.DAY_OF_MONTH);
	private final JSpinner start = spinner("HH:mm", Calendar.MINUTE);
	private final JSpinner end = spinner("HH:mm", Calendar.MINUTE);
	private final JComboBox<Employee> employee = new JComboBox<>();
	private final JLabel hint = Theme.label(" ", Theme.SMALL, Theme.MUTED);
	private final JLabel error = Theme.label(" ", Theme.SMALL_BOLD, Theme.DANGER);
	private boolean saved;

	static boolean create(Component parent, AppContext context) {
		ShiftDialog dialog = new ShiftDialog(parent, context, null);
		dialog.setVisible(true);
		return dialog.saved;
	}

	static boolean edit(Component parent, AppContext context, Shift shift) {
		ShiftDialog dialog = new ShiftDialog(parent, context, shift);
		dialog.setVisible(true);
		return dialog.saved;
	}

	/** Builds the dialog without showing it (used by the screenshot mode). */
	static ShiftDialog build(Component parent, AppContext context, Shift shift) {
		return new ShiftDialog(parent, context, shift);
	}

	private ShiftDialog(Component parent, AppContext context, Shift shift) {
		super(SwingUtilities.getWindowAncestor(parent), shift == null ? "New shift" : "Shift " + shift.getId(),
				ModalityType.APPLICATION_MODAL);
		this.context = context;
		this.shift = shift;

		JPanel content = new JPanel(new BorderLayout(0, 18));
		content.setBackground(Theme.SURFACE);
		content.setBorder(BorderFactory.createEmptyBorder(26, 28, 22, 28));

		JPanel head = new JPanel(new BorderLayout(0, 4));
		head.setOpaque(false);
		head.add(Theme.label(shift == null ? "New shift" : "Shift " + shift.getId(), Theme.TITLE, Theme.INK),
				BorderLayout.NORTH);
		String sub = shift == null ? "Plan a shift and optionally assign an employee."
				: "Status: " + shift.getStatus() + ".  An assigned employee cannot be replaced.";
		head.add(Theme.label(sub, Theme.BODY, Theme.MUTED), BorderLayout.SOUTH);
		content.add(head, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(6, 0, 6, 14);
		addRow(form, c, 0, "Date", date);
		JPanel times = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		times.setOpaque(false);
		times.add(start);
		times.add(Box.createHorizontalStrut(10));
		times.add(Theme.label("to", Theme.BODY, Theme.MUTED));
		times.add(Box.createHorizontalStrut(10));
		times.add(end);
		addRow(form, c, 1, "Time", times);
		c.gridx = 1;
		c.gridy = 2;
		c.insets = new Insets(0, 0, 6, 0);
		form.add(hint, c);
		c.insets = new Insets(6, 0, 6, 14);
		addRow(form, c, 3, "Employee", employee);
		content.add(form, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout(0, 12));
		bottom.setOpaque(false);
		bottom.add(error, BorderLayout.NORTH);
		JPanel buttons = new JPanel(new BorderLayout());
		buttons.setOpaque(false);
		JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightButtons.setOpaque(false);
		JButton cancel = Theme.button("Cancel", Theme.ButtonStyle.SECONDARY);
		JButton save = Theme.button(shift == null ? "Create shift" : "Save changes", Theme.ButtonStyle.PRIMARY);
		cancel.addActionListener(e -> dispose());
		save.addActionListener(e -> save());
		rightButtons.add(cancel);
		rightButtons.add(save);
		buttons.add(rightButtons, BorderLayout.EAST);
		if (shift != null && context.isAdmin()) {
			JButton delete = Theme.button("Delete shift", Theme.ButtonStyle.DANGER);
			delete.addActionListener(e -> {
				context.shiftManager.deleteShift(shift);
				saved = true;
				dispose();
			});
			JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			leftButtons.setOpaque(false);
			leftButtons.add(delete);
			buttons.add(leftButtons, BorderLayout.WEST);
		}
		bottom.add(buttons, BorderLayout.SOUTH);
		content.add(bottom, BorderLayout.SOUTH);

		fillEmployees();
		fillTimes();
		if (ongoing()) {
			date.setEnabled(false);
			start.setEnabled(false);
			end.setEnabled(false);
		}
		start.addChangeListener(e -> updateHint());
		end.addChangeListener(e -> updateHint());
		updateHint();

		setContentPane(content);
		setResizable(false);
		pack();
		setSize(new Dimension(Math.max(getWidth(), 520), getHeight()));
		setLocationRelativeTo(parent);
	}

	private static JSpinner spinner(String pattern, int field) {
		JSpinner spinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, field));
		spinner.setEditor(new JSpinner.DateEditor(spinner, pattern));
		Theme.styleSpinner(spinner);
		spinner.setPreferredSize(new Dimension(pattern.length() > 6 ? 200 : 104, 38));
		return spinner;
	}

	private static void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
		c.gridx = 0;
		c.gridy = row;
		c.weightx = 0;
		form.add(Theme.label(label, Theme.BODY_BOLD, Theme.MUTED), c);
		c.gridx = 1;
		c.weightx = 1;
		form.add(field, c);
	}

	private void fillEmployees() {
		Theme.styleCombo(employee);
		employee.setPreferredSize(new Dimension(320, 38));
		employee.addItem(null);
		for (Employee e : context.employeeDataBase.getAllEmployees()) {
			employee.addItem(e);
		}
		employee.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
					boolean focus) {
				Employee e = (Employee) value;
				String text = e == null ? "Unassigned (open shift)"
						: AppContext.name(e) + "   |   " + e.getRole();
				return super.getListCellRendererComponent(list, text, index, selected, focus);
			}
		});
		if (shift != null && shift.getEmployee() != null) {
			employee.setSelectedItem(shift.getEmployee());
			employee.setEnabled(false);
		}
	}

	private void fillTimes() {
		LocalDateTime from;
		LocalDateTime to;
		if (shift != null && shift.getStartDateTime() != null) {
			from = shift.getStartDateTime();
			to = shift.getEndDateTime() != null ? shift.getEndDateTime() : LocalDateTime.now();
		} else {
			from = LocalDate.now().plusDays(1).atTime(9, 0);
			to = from.withHour(17);
		}
		date.setValue(toDate(from));
		start.setValue(toDate(from));
		end.setValue(toDate(to));
	}

	private static Date toDate(LocalDateTime time) {
		return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
	}

	private static LocalDateTime toLocal(Object value) {
		return LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault());
	}

	private LocalDateTime[] chosenTimes() {
		LocalDate day = toLocal(date.getValue()).toLocalDate();
		LocalTime from = toLocal(start.getValue()).toLocalTime().withSecond(0).withNano(0);
		LocalTime to = toLocal(end.getValue()).toLocalTime().withSecond(0).withNano(0);
		LocalDateTime startTime = day.atTime(from);
		LocalDateTime endTime = day.atTime(to);
		if (!endTime.isAfter(startTime)) {
			endTime = endTime.plusDays(1); // overnight shift
		}
		return new LocalDateTime[] { startTime, endTime };
	}

	private boolean ongoing() {
		return shift != null && shift.getStartDateTime() != null && shift.getEndDateTime() == null;
	}

	private void updateHint() {
		if (ongoing()) {
			hint.setText("Ongoing: the employee ends it by checking out.");
			return;
		}
		LocalDateTime[] t = chosenTimes();
		String length = AppContext.hours(java.time.Duration.between(t[0], t[1]));
		boolean overnight = t[1].toLocalDate().isAfter(t[0].toLocalDate());
		hint.setText("Length " + length + (overnight ? ", ends the next day" : ""));
	}

	private void save() {
		LocalDateTime[] t = chosenTimes();
		Employee chosen = (Employee) employee.getSelectedItem();
		try {
			if (shift == null) {
				context.shiftManager.createShift(t[0], t[1], chosen);
			} else {
				if (!ongoing() && (!t[0].equals(shift.getStartDateTime()) || !t[1].equals(shift.getEndDateTime()))) {
					context.shiftManager.updateShiftTimes(shift, t[0], t[1]);
				}
				if (shift.getEmployee() == null && chosen != null) {
					context.shiftManager.assignEmployee(shift, chosen);
				}
			}
			saved = true;
			dispose();
		} catch (IllegalArgumentException | IllegalStateException ex) {
			error.setText(ex.getMessage());
		}
	}
}
