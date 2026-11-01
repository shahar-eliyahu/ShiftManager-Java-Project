package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import SingleObjectClasses.Employee;
import SingleObjectClasses.Shift;

/** All shifts with filters, for supervisors and administrators. */
final class ShiftsPage extends JPanel {
	private static final long serialVersionUID = 1L;

	private final AppContext context;
	private final JComboBox<String> period = new JComboBox<>(
			new String[] { "All shifts", "Today", "This week", "This month", "Not scheduled" });
	private final JComboBox<Employee> person = new JComboBox<>();
	private final JLabel count = Theme.label("", Theme.BODY, Theme.MUTED);
	private final List<Shift> rows = new ArrayList<>();
	private final DefaultTableModel model = new DefaultTableModel(
			new String[] { "ID", "Employee", "Role", "Start", "End", "Length", "Status" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	private final JTable table = new JTable(model);
	private final JButton edit = Theme.button("Edit / assign", Theme.ButtonStyle.SECONDARY);
	private final JButton delete = Theme.button("Delete", Theme.ButtonStyle.DANGER);

	ShiftsPage(AppContext context) {
		this.context = context;
		setOpaque(false);
		setLayout(new BorderLayout(0, 16));
		setBorder(BorderFactory.createEmptyBorder(26, 28, 28, 28));

		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		JPanel titles = new JPanel();
		titles.setOpaque(false);
		titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
		titles.add(Theme.label("Shifts", Theme.TITLE, Theme.INK));
		titles.add(Box.createVerticalStrut(4));
		titles.add(count);
		header.add(titles, BorderLayout.WEST);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
		actions.setOpaque(false);
		edit.addActionListener(e -> editSelected());
		actions.add(edit);
		if (context.isAdmin()) {
			delete.addActionListener(e -> deleteSelected());
			actions.add(delete);
			JButton add = Theme.button("+  New shift", Theme.ButtonStyle.PRIMARY);
			add.addActionListener(e -> {
				if (ShiftDialog.create(this, context)) {
					context.changed();
				}
			});
			actions.add(Box.createHorizontalStrut(6));
			actions.add(add);
			JButton empty = Theme.button("+  Empty shift", Theme.ButtonStyle.SECONDARY);
			empty.setToolTipText("Adds a shift without times, like the console's Add Shift");
			empty.addActionListener(e -> {
				context.shiftManager.createShift();
				context.changed();
			});
			actions.add(empty);
		}
		header.add(actions, BorderLayout.EAST);

		JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		filters.setOpaque(false);
		filters.add(Theme.label("Show", Theme.BODY_BOLD, Theme.MUTED));
		filters.add(Box.createHorizontalStrut(10));
		style(period, 170);
		filters.add(period);
		filters.add(Box.createHorizontalStrut(18));
		filters.add(Theme.label("Employee", Theme.BODY_BOLD, Theme.MUTED));
		filters.add(Box.createHorizontalStrut(10));
		style(person, 220);
		person.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
					boolean focus) {
				String text = value == null ? "Everyone" : AppContext.name((Employee) value);
				return super.getListCellRendererComponent(list, text, index, selected, focus);
			}
		});
		filters.add(person);
		period.addActionListener(e -> refresh());
		person.addActionListener(e -> refresh());

		JPanel north = new JPanel(new BorderLayout(0, 14));
		north.setOpaque(false);
		north.add(header, BorderLayout.NORTH);
		north.add(filters, BorderLayout.SOUTH);
		add(north, BorderLayout.NORTH);

		add(Theme.styleTable(table), BorderLayout.CENTER);
		table.getColumnModel().getColumn(6).setCellRenderer(Theme.pillRenderer(Theme::statusColors));
		int[] widths = { 50, 170, 110, 160, 160, 90, 130 };
		for (int i = 0; i < widths.length; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
		}
		table.getSelectionModel().addListSelectionListener(e -> updateButtons());
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					editSelected();
				}
			}
		});

		context.onChange(this::refresh);
		refresh();
	}

	private static void style(JComboBox<?> combo, int width) {
		Theme.styleCombo(combo);
		combo.setPreferredSize(new Dimension(width, 38));
	}

	void refresh() {
		Employee selectedPerson = (Employee) person.getSelectedItem();
		if (person.getItemCount() != context.employeeDataBase.getAllEmployees().size() + 1) {
			person.removeAllItems();
			person.addItem(null);
			for (Employee e : context.employeeDataBase.getAllEmployees()) {
				person.addItem(e);
			}
			person.setSelectedItem(selectedPerson);
			return; // the combo's action listener refreshes again
		}

		LocalDate today = LocalDate.now();
		String filter = (String) period.getSelectedItem();
		rows.clear();
		for (Shift shift : context.shiftManager.getShifts()) {
			if (selectedPerson != null && !selectedPerson.equals(shift.getEmployee())) {
				continue;
			}
			LocalDate day = shift.getStartDateTime() == null ? null : shift.getStartDateTime().toLocalDate();
			boolean keep;
			switch (filter) {
			case "Today":
				keep = today.equals(day);
				break;
			case "This week":
				LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
				keep = day != null && !day.isBefore(monday) && !day.isAfter(monday.plusDays(6));
				break;
			case "This month":
				keep = day != null && day.getMonth() == today.getMonth() && day.getYear() == today.getYear();
				break;
			case "Not scheduled":
				keep = day == null;
				break;
			default:
				keep = true;
			}
			if (keep) {
				rows.add(shift);
			}
		}
		// newest first; shifts without times at the top
		rows.sort(Comparator.comparing(Shift::getStartDateTime, Comparator.nullsFirst(Comparator.reverseOrder()))
				.thenComparing(Shift::getId));

		model.setRowCount(0);
		for (Shift shift : rows) {
			Employee e = shift.getEmployee();
			LocalDateTime end = shift.getEndDateTime();
			model.addRow(new Object[] { shift.getId(), AppContext.name(e), e == null ? "" : e.getRole(),
					shift.getStartDateTime() == null ? "Not set" : AppContext.format(shift.getStartDateTime()),
					shift.getStartDateTime() == null ? "Not set" : end == null ? "Now" : AppContext.format(end),
					shift.getStartDateTime() == null ? "" : AppContext.hours(AppContext.length(shift)),
					shift.getStatus() });
		}
		count.setText(rows.size() + " of " + context.shiftManager.getShifts().size()
				+ " shifts, newest first   |   double-click a row to edit");
		updateButtons();
	}

	private Shift selected() {
		int row = table.getSelectedRow();
		return row < 0 || row >= rows.size() ? null : rows.get(table.convertRowIndexToModel(row));
	}

	private void updateButtons() {
		edit.setEnabled(selected() != null);
		delete.setEnabled(selected() != null);
	}

	private void editSelected() {
		Shift shift = selected();
		if (shift != null && ShiftDialog.edit(this, context, shift)) {
			context.changed();
		}
	}

	private void deleteSelected() {
		Shift shift = selected();
		if (shift != null) {
			context.shiftManager.deleteShift(shift);
			context.changed();
		}
	}

	void select(int index) {
		if (index < table.getRowCount()) {
			table.setRowSelectionInterval(index, index);
		}
	}
}
