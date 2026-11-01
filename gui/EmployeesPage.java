package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import Enums.Rank;
import SingleObjectClasses.Employee;
import SingleObjectClasses.Shift;

/** Employee list with add, edit and delete according to the logged-in employee's rank. */
final class EmployeesPage extends JPanel {
	private static final long serialVersionUID = 1L;

	private final AppContext context;
	private final JLabel count = Theme.label("", Theme.BODY, Theme.MUTED);
	private final JLabel message = Theme.label(" ", Theme.BODY_BOLD, Theme.DANGER);
	private final List<Employee> rows = new ArrayList<>();
	private final DefaultTableModel model = new DefaultTableModel(new String[] { "ID", "Name", "Role", "Rank",
			"Birth date", "Shifts this week", "Hours worked this week", "Notifications" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	private final JTable table = new JTable(model);
	private final JButton edit = Theme.button("Edit", Theme.ButtonStyle.SECONDARY);
	private final JButton delete = Theme.button("Delete", Theme.ButtonStyle.DANGER);

	EmployeesPage(AppContext context) {
		this.context = context;
		setOpaque(false);
		setLayout(new BorderLayout(0, 16));
		setBorder(BorderFactory.createEmptyBorder(26, 28, 28, 28));

		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		JPanel titles = new JPanel();
		titles.setOpaque(false);
		titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
		titles.add(Theme.label("Employees", Theme.TITLE, Theme.INK));
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
			JButton add = Theme.button("+  Add employee", Theme.ButtonStyle.PRIMARY);
			add.addActionListener(e -> {
				if (EmployeeDialog.add(this, context)) {
					context.changed();
				}
			});
			actions.add(Box.createHorizontalStrut(6));
			actions.add(add);
		}
		header.add(actions, BorderLayout.EAST);

		JPanel north = new JPanel(new BorderLayout(0, 8));
		north.setOpaque(false);
		north.add(header, BorderLayout.NORTH);
		north.add(message, BorderLayout.SOUTH);
		add(north, BorderLayout.NORTH);

		add(Theme.styleTable(table), BorderLayout.CENTER);
		table.getColumnModel().getColumn(3)
				.setCellRenderer(Theme.pillRenderer(text -> Theme.rankColors(rankOf(text))));
		table.getColumnModel().getColumn(7).setCellRenderer(Theme.pillRenderer(
				text -> text.startsWith("Yes") ? new java.awt.Color[] { Theme.SUCCESS_SOFT, Theme.SUCCESS }
						: new java.awt.Color[] { new java.awt.Color(0xF0EFEC), Theme.MUTED }));
		int[] widths = { 100, 150, 100, 120, 100, 110, 150, 110 };
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

	private static Rank rankOf(String text) {
		for (Rank rank : Rank.values()) {
			if (Theme.rankName(rank).equals(text)) {
				return rank;
			}
		}
		return Rank.WORKER;
	}

	void refresh() {
		rows.clear();
		rows.addAll(context.employeeDataBase.getAllEmployees());
		model.setRowCount(0);
		LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		List<Shift> week = context.shiftManager.getShiftsBetween(monday, monday.plusDays(6));
		for (Employee e : rows) {
			int shifts = 0;
			java.time.Duration hours = java.time.Duration.ZERO;
			for (Shift shift : week) {
				if (e.equals(shift.getEmployee())) {
					shifts++;
					if (!"Scheduled".equals(shift.getStatus())) {
						hours = hours.plus(AppContext.length(shift));
					}
				}
			}
			boolean observer = e.getRank() != Rank.WORKER;
			model.addRow(new Object[] { e.getId(), AppContext.name(e), e.getRole(), Theme.rankName(e.getRank()),
					String.format("%02d/%02d/%d", e.getBDay().getDay(), e.getBDay().getMonth(), e.getBDay().getYear()),
					shifts, AppContext.hours(hours), observer ? "Yes" : "No" });
		}
		count.setText(rows.size() + " employees   |   supervisors and administrators receive notifications");
		updateButtons();
	}

	private Employee selected() {
		int row = table.getSelectedRow();
		return row < 0 || row >= rows.size() ? null : rows.get(row);
	}

	private void updateButtons() {
		Employee e = selected();
		edit.setEnabled(e != null && context.employeeDataBase.canEdit(e));
		delete.setEnabled(e != null && !e.equals(context.user()));
	}

	private void editSelected() {
		Employee e = selected();
		if (e != null && context.employeeDataBase.canEdit(e) && EmployeeDialog.edit(this, context, e)) {
			message.setText(" ");
			context.changed();
		}
	}

	private void deleteSelected() {
		Employee e = selected();
		if (e == null) {
			return;
		}
		try {
			context.employeeDataBase.deleteEmployee(e);
			message.setText(" ");
		} catch (IllegalStateException | IllegalArgumentException ex) {
			message.setText(ex.getMessage());
		}
		context.changed();
	}

	void select(int index) {
		if (index < table.getRowCount()) {
			table.setRowSelectionInterval(index, index);
		}
	}
}
