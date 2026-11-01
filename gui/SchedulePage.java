package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import SingleObjectClasses.Employee;
import SingleObjectClasses.Shift;

/** Week calendar of shifts. Workers see their own shifts, supervisors and administrators see everyone's. */
final class SchedulePage extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final int FIRST_HOUR = 6;
	private static final int LAST_HOUR = 24;

	private final AppContext context;
	private LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	private final JLabel range = Theme.label("", Theme.BODY, Theme.MUTED);
	private final JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
	private final WeekCalendar calendar = new WeekCalendar();

	SchedulePage(AppContext context) {
		this.context = context;
		setOpaque(false);
		setLayout(new BorderLayout(0, 14));
		setBorder(BorderFactory.createEmptyBorder(26, 28, 28, 28));

		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		JPanel titles = new JPanel();
		titles.setOpaque(false);
		titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
		titles.add(Theme.label(context.isWorker() ? "My schedule" : "Team schedule", Theme.TITLE, Theme.INK));
		titles.add(Box.createVerticalStrut(4));
		titles.add(range);
		header.add(titles, BorderLayout.WEST);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
		actions.setOpaque(false);
		JButton previous = Theme.button("<  Previous", Theme.ButtonStyle.SECONDARY);
		JButton today = Theme.button("This week", Theme.ButtonStyle.SECONDARY);
		JButton next = Theme.button("Next  >", Theme.ButtonStyle.SECONDARY);
		previous.addActionListener(e -> moveWeeks(-1));
		next.addActionListener(e -> moveWeeks(1));
		today.addActionListener(e -> {
			monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			refresh();
		});
		actions.add(previous);
		actions.add(today);
		actions.add(next);
		if (context.isAdmin()) {
			JButton add = Theme.button("+  New shift", Theme.ButtonStyle.PRIMARY);
			add.addActionListener(e -> {
				if (ShiftDialog.create(this, context)) {
					context.changed();
				}
			});
			actions.add(Box.createHorizontalStrut(6));
			actions.add(add);
		}
		header.add(actions, BorderLayout.EAST);

		legend.setOpaque(false);
		JPanel north = new JPanel(new BorderLayout(0, 12));
		north.setOpaque(false);
		north.add(header, BorderLayout.NORTH);
		north.add(legend, BorderLayout.SOUTH);
		add(north, BorderLayout.NORTH);

		Theme.Card card = new Theme.Card();
		card.setLayout(new BorderLayout());
		card.setBorder(BorderFactory.createEmptyBorder(6, 6, 10, 10));
		card.add(calendar, BorderLayout.CENTER);
		add(card, BorderLayout.CENTER);

		context.onChange(this::refresh);
		new Timer(30_000, e -> calendar.repaint()).start();
		refresh();
	}

	private void moveWeeks(int weeks) {
		monday = monday.plusWeeks(weeks);
		refresh();
	}

	void refresh() {
		DateTimeFormatter day = DateTimeFormatter.ofPattern("d MMM");
		LocalDate sunday = monday.plusDays(6);
		range.setText("Week of " + monday.format(day) + " - " + sunday.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
				+ "   |   " + visibleShifts().size() + " shifts");

		legend.removeAll();
		Set<Employee> people = new LinkedHashSet<>();
		boolean open = false;
		for (Shift shift : visibleShifts()) {
			if (shift.getEmployee() != null) {
				people.add(shift.getEmployee());
			} else {
				open = true;
			}
		}
		for (Employee employee : context.employeeDataBase.getAllEmployees()) {
			if (people.contains(employee)) {
				legend.add(legendItem(AppContext.name(employee), context.colorOf(employee), false));
			}
		}
		if (open) {
			legend.add(legendItem("Open shift", Theme.FAINT, true));
		}
		legend.revalidate();
		legend.repaint();
		calendar.repaint();
	}

	private JComponent legendItem(String text, Color color, boolean dashed) {
		JLabel label = new JLabel(text) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				Theme.antialias(g2);
				int y = (getHeight() - 12) / 2;
				if (dashed) {
					g2.setColor(color);
					g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
							new float[] { 3f, 2f }, 0f));
					g2.drawRoundRect(0, y, 11, 11, 4, 4);
				} else {
					g2.setColor(color);
					g2.fillRoundRect(0, y, 12, 12, 4, 4);
				}
				g2.dispose();
				super.paintComponent(g);
			}
		};
		label.setFont(Theme.SMALL_BOLD);
		label.setForeground(Theme.MUTED);
		label.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
		return label;
	}

	private List<Shift> visibleShifts() {
		List<Shift> result = new ArrayList<>();
		for (Shift shift : context.shiftManager.getShiftsBetween(monday, monday.plusDays(6))) {
			if (!context.isWorker() || context.user().equals(shift.getEmployee())) {
				result.add(shift);
			}
		}
		return result;
	}

	/** Custom-painted week grid: one column per day, one row per hour. */
	private final class WeekCalendar extends JComponent {
		private static final long serialVersionUID = 1L;
		private static final int GUTTER = 58;
		private static final int HEADER = 54;
		private final List<Rectangle> blockBounds = new ArrayList<>();
		private final List<Shift> blockShifts = new ArrayList<>();

		WeekCalendar() {
			setPreferredSize(new Dimension(900, 560));
			setToolTipText("");
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Shift shift = shiftAt(e.getX(), e.getY());
					if (shift != null && !context.isWorker() && SwingUtilities.isLeftMouseButton(e)) {
						if (ShiftDialog.edit(SchedulePage.this, context, shift)) {
							context.changed();
						}
					}
				}
			});
		}

		private Shift shiftAt(int x, int y) {
			for (int i = blockBounds.size() - 1; i >= 0; i--) {
				if (blockBounds.get(i).contains(x, y)) {
					return blockShifts.get(i);
				}
			}
			return null;
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			Shift shift = shiftAt(e.getX(), e.getY());
			if (shift == null) {
				return null;
			}
			return "<html><b>" + AppContext.name(shift.getEmployee()) + "</b><br>Shift " + shift.getId() + ": "
					+ AppContext.format(shift.getStartDateTime()) + " - "
					+ (shift.getEndDateTime() == null ? "now" : shift.getEndDateTime().format(AppContext.TIME))
					+ "<br>" + shift.getStatus() + (context.isWorker() ? "" : "<br><i>Click to edit</i>") + "</html>";
		}

		private double hourHeight() {
			return (getHeight() - HEADER - 4) / (double) (LAST_HOUR - FIRST_HOUR);
		}

		private double yOf(LocalTime time, boolean endOfDay) {
			double hours = time.getHour() + time.getMinute() / 60.0;
			if (endOfDay && hours == 0) {
				hours = 24;
			}
			hours = Math.max(FIRST_HOUR, Math.min(LAST_HOUR, hours));
			return HEADER + (hours - FIRST_HOUR) * hourHeight();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			Theme.antialias(g2);
			int w = getWidth();
			int h = getHeight();
			double dayWidth = (w - GUTTER) / 7.0;
			LocalDate today = LocalDate.now();
			DateTimeFormatter dayName = DateTimeFormatter.ofPattern("EEE");

			// today's column
			for (int d = 0; d < 7; d++) {
				if (monday.plusDays(d).equals(today)) {
					g2.setColor(Theme.TODAY);
					g2.fillRect((int) (GUTTER + d * dayWidth), HEADER, (int) Math.ceil(dayWidth), h - HEADER);
				}
			}

			// hour lines and labels
			g2.setFont(Theme.SMALL);
			for (int hour = FIRST_HOUR; hour <= LAST_HOUR; hour++) {
				int y = (int) (HEADER + (hour - FIRST_HOUR) * hourHeight());
				g2.setColor(hour % 2 == 0 ? Theme.GRID : Theme.mix(Theme.GRID, Color.WHITE, 0.5));
				g2.drawLine(GUTTER, y, w, y);
				if (hour < LAST_HOUR && hour % 2 == 0) {
					g2.setColor(Theme.FAINT);
					g2.drawString(String.format("%02d:00", hour), 10, y + 4);
				}
			}
			// day columns and headers
			for (int d = 0; d < 7; d++) {
				LocalDate date = monday.plusDays(d);
				int x = (int) (GUTTER + d * dayWidth);
				g2.setColor(Theme.GRID);
				g2.drawLine(x, 10, x, h);
				boolean isToday = date.equals(today);
				g2.setFont(Theme.SMALL_BOLD);
				g2.setColor(isToday ? Theme.PRIMARY : Theme.FAINT);
				String name = date.format(dayName).toUpperCase();
				FontMetrics fm = g2.getFontMetrics();
				int cx = (int) (x + dayWidth / 2);
				g2.drawString(name, cx - fm.stringWidth(name) - 4, 33);
				g2.setFont(Theme.HEADING);
				fm = g2.getFontMetrics();
				String number = String.valueOf(date.getDayOfMonth());
				if (isToday) {
					g2.setColor(Theme.PRIMARY);
					g2.fillOval(cx + 1, 13, 28, 28);
					g2.setColor(Color.WHITE);
				} else {
					g2.setColor(Theme.INK);
				}
				g2.drawString(number, cx + 15 - fm.stringWidth(number) / 2, 33);
			}
			g2.setColor(Theme.BORDER);
			g2.drawLine(GUTTER, HEADER, w, HEADER);

			// shift blocks, side by side when they overlap on the same day
			blockBounds.clear();
			blockShifts.clear();
			for (int d = 0; d < 7; d++) {
				LocalDate date = monday.plusDays(d);
				List<Shift> day = new ArrayList<>();
				for (Shift shift : visibleShifts()) {
					if (shift.getStartDateTime().toLocalDate().equals(date)) {
						day.add(shift);
					}
				}
				day.sort(Comparator.comparing(Shift::getStartDateTime));
				List<LocalDateTime> laneEnds = new ArrayList<>();
				int[] lanes = new int[day.size()];
				for (int i = 0; i < day.size(); i++) {
					Shift shift = day.get(i);
					int lane = 0;
					while (lane < laneEnds.size() && laneEnds.get(lane).isAfter(shift.getStartDateTime())) {
						lane++;
					}
					LocalDateTime end = endOf(shift);
					if (lane == laneEnds.size()) {
						laneEnds.add(end);
					} else {
						laneEnds.set(lane, end);
					}
					lanes[i] = lane;
				}
				int laneCount = Math.max(1, laneEnds.size());
				double colX = GUTTER + d * dayWidth + 4;
				double laneWidth = (dayWidth - 8) / laneCount;
				for (int i = 0; i < day.size(); i++) {
					Shift shift = day.get(i);
					int x = (int) (colX + lanes[i] * laneWidth);
					int bw = (int) laneWidth - 3;
					int y1 = (int) yOf(shift.getStartDateTime().toLocalTime(), false) + 1;
					LocalDateTime end = endOf(shift);
					int y2 = end.toLocalDate().isAfter(date) ? (int) yOf(LocalTime.MIDNIGHT, true)
							: (int) yOf(end.toLocalTime(), true);
					paintBlock(g2, shift, x, y1, bw, Math.max(22, y2 - y1 - 2));
				}
			}

			// current time line
			LocalDateTime now = LocalDateTime.now();
			int todayIndex = (int) java.time.temporal.ChronoUnit.DAYS.between(monday, now.toLocalDate());
			if (todayIndex >= 0 && todayIndex < 7 && now.getHour() >= FIRST_HOUR) {
				int y = (int) yOf(now.toLocalTime(), false);
				int x = (int) (GUTTER + todayIndex * dayWidth);
				g2.setColor(Theme.DANGER);
				g2.setStroke(new BasicStroke(2f));
				g2.drawLine(x, y, (int) (x + dayWidth), y);
				g2.fillOval(x - 4, y - 4, 8, 8);
			}
			g2.dispose();
		}

		private LocalDateTime endOf(Shift shift) {
			return shift.getEndDateTime() != null ? shift.getEndDateTime() : LocalDateTime.now();
		}

		private void paintBlock(Graphics2D g2, Shift shift, int x, int y, int w, int h) {
			Employee employee = shift.getEmployee();
			Color color = context.colorOf(employee);
			boolean ongoing = shift.getEndDateTime() == null;
			Shape shape = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
			blockBounds.add(new Rectangle(x, y, w, h));
			blockShifts.add(shift);

			if (employee == null) {
				g2.setColor(Theme.SURFACE_ALT);
				g2.fill(shape);
				g2.setColor(Theme.FAINT);
				g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
						new float[] { 5f, 3f }, 0f));
				g2.draw(shape);
			} else {
				g2.setColor(Theme.mix(color, Color.WHITE, 0.86));
				g2.fill(shape);
				g2.setColor(color);
				g2.fillRoundRect(x, y, 5, h, 5, 5);
				if (ongoing) {
					g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
							new float[] { 4f, 3f }, 0f));
					g2.draw(shape);
				}
			}
			g2.setStroke(new BasicStroke(1f));

			Shape old = g2.getClip();
			g2.clipRect(x, y, w, h);
			Color ink = employee == null ? Theme.MUTED : Theme.mix(color, Color.BLACK, 0.35);
			g2.setColor(ink);
			g2.setFont(Theme.SMALL_BOLD);
			String title = employee == null ? "Open shift" : employee.getFirstName();
			g2.drawString(title, x + 11, y + 16);
			if (h > 34) {
				g2.setFont(Theme.SMALL);
				String end = ongoing ? "now" : shift.getEndDateTime().format(AppContext.TIME);
				g2.drawString(shift.getStartDateTime().format(AppContext.TIME) + " - " + end, x + 11, y + 31);
			}
			if (h > 52) {
				g2.setFont(Theme.SMALL);
				String note = ongoing ? "On shift" : employee == null ? "Needs an employee" : employee.getRole();
				g2.setColor(Theme.mix(ink, Color.WHITE, 0.25));
				g2.drawString(note, x + 11, y + 46);
			}
			g2.setClip(old);
		}
	}
}
