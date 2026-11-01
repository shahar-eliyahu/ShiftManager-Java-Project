package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;

import Enums.Rank;
import SingleObjectClasses.Date;
import SingleObjectClasses.Employee;

/** Add an employee (administrators) or update one (with the rank rules of EmployeeDataBase). */
final class EmployeeDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final AppContext context;
	private final Employee employee; // null when adding
	private final JTextField id = field();
	private final JTextField firstName = field();
	private final JTextField lastName = field();
	private final JTextField role = field();
	private final JSpinner birthDate = new JSpinner(new SpinnerDateModel(new java.util.Date(), null, null,
			Calendar.DAY_OF_MONTH));
	private final JComboBox<Rank> rank = new JComboBox<>();
	private final JLabel error = Theme.label(" ", Theme.SMALL_BOLD, Theme.DANGER);
	private boolean saved;

	static boolean add(Component parent, AppContext context) {
		EmployeeDialog dialog = new EmployeeDialog(parent, context, null);
		dialog.setVisible(true);
		return dialog.saved;
	}

	static boolean edit(Component parent, AppContext context, Employee employee) {
		EmployeeDialog dialog = new EmployeeDialog(parent, context, employee);
		dialog.setVisible(true);
		return dialog.saved;
	}

	static EmployeeDialog build(Component parent, AppContext context, Employee employee) {
		return new EmployeeDialog(parent, context, employee);
	}

	private EmployeeDialog(Component parent, AppContext context, Employee employee) {
		super(SwingUtilities.getWindowAncestor(parent), employee == null ? "Add employee" : "Edit employee",
				ModalityType.APPLICATION_MODAL);
		this.context = context;
		this.employee = employee;

		JPanel content = new JPanel(new BorderLayout(0, 18));
		content.setBackground(Theme.SURFACE);
		content.setBorder(BorderFactory.createEmptyBorder(26, 28, 22, 28));
		JPanel head = new JPanel(new BorderLayout(0, 4));
		head.setOpaque(false);
		head.add(Theme.label(employee == null ? "Add employee" : "Edit " + AppContext.name(employee), Theme.TITLE,
				Theme.INK), BorderLayout.NORTH);
		head.add(Theme.label("The first name is the username and the ID is the password.", Theme.BODY, Theme.MUTED),
				BorderLayout.SOUTH);
		content.add(head, BorderLayout.NORTH);

		birthDate.setEditor(new JSpinner.DateEditor(birthDate, "dd/MM/yyyy"));
		Theme.styleSpinner(birthDate);
		birthDate.setPreferredSize(new Dimension(160, 38));
		Theme.styleCombo(rank);
		rank.setPreferredSize(new Dimension(200, 38));
		rank.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
					boolean focus) {
				return super.getListCellRendererComponent(list, value == null ? "" : Theme.rankName((Rank) value),
						index, selected, focus);
			}
		});

		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(6, 0, 6, 14);
		int row = 0;
		if (employee == null) {
			addRow(form, c, row++, "ID (9 digits)", id);
		} else {
			addRow(form, c, row++, "ID", Theme.label(employee.getId(), Theme.BODY_BOLD, Theme.INK));
		}
		addRow(form, c, row++, "First name", firstName);
		addRow(form, c, row++, "Last name", lastName);
		addRow(form, c, row++, "Birth date", wrap(birthDate));
		addRow(form, c, row++, "Role", role);
		addRow(form, c, row++, "Rank", wrap(rank));
		content.add(form, BorderLayout.CENTER);

		List<Rank> ranks = employee == null ? java.util.Arrays.asList(Rank.values())
				: context.employeeDataBase.allowedRanksFor(employee);
		for (Rank r : ranks) {
			rank.addItem(r);
		}
		if (employee != null) {
			firstName.setText(employee.getFirstName());
			lastName.setText(employee.getLastName());
			role.setText(employee.getRole());
			rank.setSelectedItem(employee.getRank());
			rank.setEnabled(ranks.size() > 1);
			Date b = employee.getBDay();
			birthDate.setValue(java.util.Date.from(LocalDate.of(b.getYear(), b.getMonth(), b.getDay())
					.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		} else {
			birthDate.setValue(java.util.Date.from(
					LocalDate.of(1995, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
		}

		JPanel bottom = new JPanel(new BorderLayout(0, 12));
		bottom.setOpaque(false);
		bottom.add(error, BorderLayout.NORTH);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		buttons.setOpaque(false);
		JButton cancel = Theme.button("Cancel", Theme.ButtonStyle.SECONDARY);
		JButton save = Theme.button(employee == null ? "Add employee" : "Save changes", Theme.ButtonStyle.PRIMARY);
		cancel.addActionListener(e -> dispose());
		save.addActionListener(e -> save());
		buttons.add(cancel);
		buttons.add(save);
		bottom.add(buttons, BorderLayout.SOUTH);
		content.add(bottom, BorderLayout.SOUTH);

		setContentPane(content);
		setResizable(false);
		pack();
		setSize(new Dimension(Math.max(getWidth(), 520), getHeight()));
		setLocationRelativeTo(parent);
	}

	private static JTextField field() {
		JTextField field = new JTextField();
		field.setFont(Theme.BODY);
		field.setForeground(Theme.INK);
		field.setBorder(Theme.roundBorder(Theme.BORDER, 10, 8));
		field.setPreferredSize(new Dimension(300, 38));
		return field;
	}

	private static JComponent wrap(JComponent component) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.setOpaque(false);
		panel.add(component);
		return panel;
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

	private void save() {
		LocalDate b = ((java.util.Date) birthDate.getValue()).toInstant().atZone(ZoneId.systemDefault())
				.toLocalDate();
		if (b.isAfter(LocalDate.now()) || b.getYear() < 1950) {
			error.setText("Birth date must be between 1950 and today.");
			return;
		}
		Date birthDay = new Date(b.getDayOfMonth(), b.getMonthValue(), b.getYear());
		try {
			if (employee == null) {
				context.employeeDataBase.addEmployee(id.getText().trim(), firstName.getText().trim(),
						lastName.getText().trim(), birthDay, role.getText(), (Rank) rank.getSelectedItem());
			} else {
				context.employeeDataBase.updateEmployee(employee, firstName.getText().trim(),
						lastName.getText().trim(), birthDay, role.getText(), (Rank) rank.getSelectedItem());
			}
			saved = true;
			dispose();
		} catch (IllegalArgumentException | IllegalStateException ex) {
			error.setText(ex.getMessage());
		}
	}
}
