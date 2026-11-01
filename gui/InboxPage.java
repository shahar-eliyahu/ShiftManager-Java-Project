package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import SingleObjectClasses.Employee;

/**
 * Notifications received by the logged-in employee. Supervisors and
 * administrators are observers of the ShiftManager (Observer pattern).
 */
final class InboxPage extends JPanel {
	private static final long serialVersionUID = 1L;

	private final AppContext context;
	private final JLabel subtitle = Theme.label("", Theme.BODY, Theme.MUTED);
	private final JPanel list = new JPanel();

	InboxPage(AppContext context) {
		this.context = context;
		setOpaque(false);
		setLayout(new BorderLayout(0, 16));
		setBorder(BorderFactory.createEmptyBorder(26, 28, 28, 28));

		JPanel titles = new JPanel();
		titles.setOpaque(false);
		titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
		titles.add(Theme.label("Inbox", Theme.TITLE, Theme.INK));
		titles.add(Box.createVerticalStrut(4));
		titles.add(subtitle);
		add(titles, BorderLayout.NORTH);

		list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
		list.setBackground(Theme.SURFACE);
		JScrollPane scroll = new JScrollPane(list);
		scroll.setBorder(new Theme.RoundBorder(Theme.BORDER, 14));
		scroll.getViewport().setBackground(Theme.SURFACE);
		Theme.styleScroll(scroll);
		add(scroll, BorderLayout.CENTER);

		context.onChange(this::refresh);
		refresh();
	}

	void refresh() {
		Employee user = context.user();
		if (user == null) {
			return;
		}
		List<String> messages = user.getNotifications();
		subtitle.setText(context.isWorker()
				? "Workers do not receive notifications. Supervisors and administrators are observers of the shift manager."
				: messages.size() + " notifications from the shift manager, newest first");
		list.removeAll();
		for (int i = messages.size() - 1; i >= 0; i--) {
			list.add(row(messages.get(i), i == messages.size() - 1));
		}
		if (messages.isEmpty()) {
			JLabel empty = Theme.label("No notifications yet.", Theme.BODY, Theme.MUTED);
			empty.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
			list.add(empty);
		}
		list.add(Box.createVerticalGlue());
		list.revalidate();
		list.repaint();
	}

	private JComponent row(String message, boolean newest) {
		String time = message.length() > 16 ? message.substring(0, 16) : "";
		String text = message.length() > 18 ? message.substring(18) : message;
		Color color = colorFor(text);

		JPanel row = new JPanel(new BorderLayout(14, 0)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(newest ? Theme.mix(Theme.PRIMARY_SOFT, Color.WHITE, 0.4) : Theme.SURFACE);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Theme.GRID);
				g.fillRect(0, getHeight() - 1, getWidth(), 1);
			}
		};
		row.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
		row.setAlignmentX(LEFT_ALIGNMENT);
		JComponent icon = new JComponent() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				Theme.antialias(g2);
				g2.setColor(Theme.mix(color, Color.WHITE, 0.85));
				g2.fillOval(0, 0, 32, 32);
				g2.setColor(color);
				g2.fillOval(11, 11, 10, 10);
				g2.dispose();
			}
		};
		icon.setPreferredSize(new Dimension(32, 32));
		row.add(icon, BorderLayout.WEST);
		row.add(Theme.label(text, Theme.BODY_BOLD, Theme.INK), BorderLayout.CENTER);
		row.add(Theme.label(time, Theme.SMALL, Theme.FAINT), BorderLayout.EAST);
		return row;
	}

	private static Color colorFor(String text) {
		String t = text.toLowerCase();
		if (t.contains("removed")) {
			return Theme.DANGER;
		}
		if (t.contains("checked in") || t.contains("checked out")) {
			return Theme.SUCCESS;
		}
		if (t.contains("undid") || t.contains("updated")) {
			return Theme.WARNING;
		}
		return Theme.PRIMARY;
	}
}
