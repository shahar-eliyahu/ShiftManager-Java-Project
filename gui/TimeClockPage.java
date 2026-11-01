package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import SingleObjectClasses.Employee;
import SingleObjectClasses.Shift;

/** Check in, check out and undo for the logged-in employee, plus their week. */
final class TimeClockPage extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");

	private final AppContext context;
	private final JLabel greeting = Theme.label("", Theme.TITLE, Theme.INK);
	private final JLabel date = Theme.label("", Theme.BODY, Theme.MUTED);
	private final JLabel clock = Theme.label("", Theme.CLOCK, Theme.INK);
	private final JLabel status = Theme.label("", Theme.HEADING, Theme.INK);
	private final JLabel since = Theme.label(" ", Theme.BODY, Theme.MUTED);
	private final JLabel message = Theme.label(" ", Theme.BODY_BOLD, Theme.SUCCESS);
	private final StatusDot dot = new StatusDot();
	private final JButton checkIn = Theme.button("Check in", Theme.ButtonStyle.SUCCESS);
	private final JButton checkOut = Theme.button("Check out", Theme.ButtonStyle.PRIMARY);
	private final JButton undo = Theme.button("Undo last action", Theme.ButtonStyle.SECONDARY);
	private final JLabel weekHours = Theme.label("", Theme.DISPLAY, Theme.INK);
	private final JLabel weekShifts = Theme.label("", Theme.BODY, Theme.MUTED);
	private final JLabel nextShift = Theme.label("", Theme.BODY_BOLD, Theme.INK);
	private final JPanel onShiftList = new JPanel();
	private final DefaultTableModel model = new DefaultTableModel(
			new String[] { "Day", "Start", "End", "Length", "Status" }, 0) {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	TimeClockPage(AppContext context) {
		this.context = context;
		setOpaque(false);
		setLayout(new BorderLayout(0, 20));
		setBorder(BorderFactory.createEmptyBorder(26, 28, 28, 28));

		JPanel top = new JPanel();
		top.setOpaque(false);
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.add(greeting);
		top.add(Box.createVerticalStrut(4));
		top.add(date);
		add(top, BorderLayout.NORTH);

		JPanel grid = new JPanel(new GridBagLayout());
		grid.setOpaque(false);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 0, 18, 18);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.62;
		grid.add(clockCard(), c);
		c.gridx = 1;
		c.weightx = 0.38;
		c.insets = new Insets(0, 0, 18, 0);
		grid.add(weekCard(), c);
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1;
		c.insets = new Insets(0, 0, 0, 18);
		grid.add(shiftsCard(), c);
		c.gridx = 1;
		c.insets = new Insets(0, 0, 0, 0);
		grid.add(onShiftCard(), c);
		add(grid, BorderLayout.CENTER);

		checkIn.addActionListener(e -> act(() -> {
			Shift shift = context.shiftManager.checkIn(context.user());
			return "Checked in at " + shift.getStartDateTime().format(AppContext.TIME) + ".";
		}));
		checkOut.addActionListener(e -> act(() -> {
			Shift shift = context.shiftManager.checkOut(context.user());
			return "Checked out. Shift length " + AppContext.hours(AppContext.length(shift)) + ".";
		}));
		undo.addActionListener(e -> act(() -> context.shiftManager.undoLastCheckInOut(context.user())
				.replace("Employee " + AppContext.name(context.user()), "You")));

		context.onChange(this::refresh);
		new Timer(1000, e -> tick()).start();
		refresh();
	}

	private interface Action {
		String run();
	}

	private void act(Action action) {
		try {
			message.setForeground(Theme.SUCCESS);
			message.setText(action.run());
		} catch (IllegalStateException | IllegalArgumentException ex) {
			message.setForeground(Theme.DANGER);
			message.setText(ex.getMessage());
		}
		context.changed();
	}

	private JComponent clockCard() {
		Theme.Card card = new Theme.Card();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
		card.add(left(Theme.label("TIME CLOCK", Theme.SMALL_BOLD, Theme.FAINT)));
		card.add(Box.createVerticalStrut(6));
		card.add(left(clock));
		card.add(Box.createVerticalStrut(8));
		JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		statusRow.setOpaque(false);
		statusRow.add(dot);
		statusRow.add(Box.createHorizontalStrut(10));
		statusRow.add(status);
		card.add(left(statusRow));
		card.add(Box.createVerticalStrut(4));
		card.add(left(since));
		card.add(Box.createVerticalStrut(20));
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		buttons.setOpaque(false);
		buttons.add(checkIn);
		buttons.add(Box.createHorizontalStrut(10));
		buttons.add(checkOut);
		buttons.add(Box.createHorizontalStrut(10));
		buttons.add(undo);
		card.add(left(buttons));
		card.add(Box.createVerticalStrut(14));
		card.add(left(message));
		return card;
	}

	private JComponent weekCard() {
		Theme.Card card = new Theme.Card(Theme.PRIMARY_SOFT);
		card.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.add(left(Theme.label("THIS WEEK", Theme.SMALL_BOLD, Theme.PRIMARY)));
		card.add(Box.createVerticalStrut(8));
		card.add(left(weekHours));
		card.add(left(weekShifts));
		card.add(Box.createVerticalStrut(22));
		card.add(left(Theme.label("NEXT SHIFT", Theme.SMALL_BOLD, Theme.PRIMARY)));
		card.add(Box.createVerticalStrut(6));
		card.add(left(nextShift));
		card.add(Box.createVerticalGlue());
		return card;
	}

	private JComponent shiftsCard() {
		Theme.Card card = new Theme.Card();
		card.setLayout(new BorderLayout(0, 12));
		card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
		card.add(Theme.label("My shifts this week", Theme.HEADING, Theme.INK), BorderLayout.NORTH);
		JTable table = new JTable(model);
		JComponent scroll = Theme.styleTable(table);
		table.getColumnModel().getColumn(4).setCellRenderer(Theme.pillRenderer(Theme::statusColors));
		card.add(scroll, BorderLayout.CENTER);
		card.setPreferredSize(new Dimension(500, 300));
		return card;
	}

	private JComponent onShiftCard() {
		Theme.Card card = new Theme.Card();
		card.setLayout(new BorderLayout(0, 12));
		card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
		card.add(Theme.label(context.isWorker() ? "Working now" : "Team on shift now", Theme.HEADING, Theme.INK),
				BorderLayout.NORTH);
		onShiftList.setOpaque(false);
		onShiftList.setLayout(new BoxLayout(onShiftList, BoxLayout.Y_AXIS));
		card.add(onShiftList, BorderLayout.CENTER);
		card.setPreferredSize(new Dimension(320, 300));
		return card;
	}

	private static JComponent left(JComponent component) {
		component.setAlignmentX(LEFT_ALIGNMENT);
		return component;
	}

	private void tick() {
		clock.setText(LocalDateTime.now().format(CLOCK));
		Shift ongoing = context.user() == null ? null : context.shiftManager.getOngoingShiftForEmployee(context.user());
		if (ongoing != null) {
			since.setText("Since " + ongoing.getStartDateTime().format(AppContext.TIME) + "   |   "
					+ AppContext.hours(AppContext.length(ongoing)) + " so far");
		}
	}

	void refresh() {
		Employee user = context.user();
		if (user == null) {
			return;
		}
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		String part = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
		greeting.setText(part + ", " + user.getFirstName());
		date.setText(now.format(LONG_DATE));

		Shift ongoing = context.shiftManager.getOngoingShiftForEmployee(user);
		if (ongoing != null) {
			status.setText("On shift");
			dot.color = Theme.SUCCESS;
		} else {
			status.setText("Not checked in");
			dot.color = Theme.FAINT;
			since.setText("Check in when you start working.");
		}
		dot.repaint();
		checkIn.setEnabled(ongoing == null);
		checkOut.setEnabled(ongoing != null);
		undo.setEnabled(context.shiftManager.canUndo(user));
		tick();

		LocalDate today = LocalDate.now();
		LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		List<Shift> mine = new ArrayList<>();
		for (Shift shift : context.shiftManager.getShiftsBetween(monday, monday.plusDays(6))) {
			if (user.equals(shift.getEmployee())) {
				mine.add(shift);
			}
		}
		mine.sort(Comparator.comparing(Shift::getStartDateTime));
		model.setRowCount(0);
		Duration worked = Duration.ZERO;
		for (Shift shift : mine) {
			String status = shift.getStatus();
			if (!"Scheduled".equals(status)) {
				worked = worked.plus(AppContext.length(shift));
			}
			model.addRow(new Object[] { shift.getStartDateTime().format(DateTimeFormatter.ofPattern("EEE d MMM")),
					shift.getStartDateTime().format(AppContext.TIME),
					shift.getEndDateTime() == null ? "Now" : shift.getEndDateTime().format(AppContext.TIME),
					AppContext.hours(AppContext.length(shift)), status });
		}
		weekHours.setText(AppContext.hours(worked));
		weekShifts.setText("worked in " + mine.size() + (mine.size() == 1 ? " shift" : " shifts") + " this week");

		Shift next = null;
		for (Shift shift : context.shiftManager.getShiftsForEmployee(user)) {
			if ("Scheduled".equals(shift.getStatus())
					&& (next == null || shift.getStartDateTime().isBefore(next.getStartDateTime()))) {
				next = shift;
			}
		}
		nextShift.setText(next == null ? "No upcoming shifts"
				: AppContext.format(next.getStartDateTime()) + " - " + next.getEndDateTime().format(AppContext.TIME));

		onShiftList.removeAll();
		int count = 0;
		for (Shift shift : context.shiftManager.getShifts()) {
			String s = shift.getStatus();
			boolean working = "Ongoing".equals(s) || "In progress".equals(s);
			if (working && shift.getEmployee() != null && (!context.isWorker() || user.equals(shift.getEmployee()))) {
				onShiftList.add(personRow(shift));
				onShiftList.add(Box.createVerticalStrut(10));
				count++;
			}
		}
		if (count == 0) {
			onShiftList.add(left(Theme.label("Nobody is on shift right now.", Theme.BODY, Theme.MUTED)));
		}
		onShiftList.revalidate();
		onShiftList.repaint();
	}

	private JComponent personRow(Shift shift) {
		Employee employee = shift.getEmployee();
		JPanel row = new JPanel(new BorderLayout(10, 0));
		row.setOpaque(false);
		row.setAlignmentX(LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		JComponent avatar = new JComponent() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				Theme.antialias(g2);
				g2.setColor(context.colorOf(employee));
				g2.fillOval(0, 2, 32, 32);
				g2.setColor(Color.WHITE);
				g2.setFont(Theme.SMALL_BOLD);
				String initials = employee.getFirstName().substring(0, 1) + employee.getLastName().substring(0, 1);
				g2.drawString(initials, 16 - g2.getFontMetrics().stringWidth(initials) / 2, 22);
				g2.dispose();
			}
		};
		avatar.setPreferredSize(new Dimension(32, 36));
		row.add(avatar, BorderLayout.WEST);
		JPanel text = new JPanel();
		text.setOpaque(false);
		text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
		text.add(Theme.label(AppContext.name(employee), Theme.BODY_BOLD, Theme.INK));
		String detail = employee.getRole() + "  |  since " + shift.getStartDateTime().format(AppContext.TIME);
		text.add(Theme.label(detail, Theme.SMALL, Theme.MUTED));
		row.add(text, BorderLayout.CENTER);
		Color[] colors = Theme.statusColors(shift.getStatus());
		JPanel pillWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
		pillWrap.setOpaque(false);
		pillWrap.add(Theme.pill(shift.getStatus(), colors[0], colors[1]));
		row.add(pillWrap, BorderLayout.EAST);
		return row;
	}

	private static final class StatusDot extends JComponent {
		private static final long serialVersionUID = 1L;
		Color color = Theme.FAINT;

		StatusDot() {
			setPreferredSize(new Dimension(14, 20));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			Theme.antialias(g2);
			g2.setColor(Theme.mix(color, Color.WHITE, 0.7));
			g2.fillOval(0, 3, 14, 14);
			g2.setColor(color);
			g2.fillOval(3, 6, 8, 8);
			g2.dispose();
		}
	}
}
