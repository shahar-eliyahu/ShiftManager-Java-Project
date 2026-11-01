package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import SingleObjectClasses.Employee;

/** Header with tabs, and the pages the logged-in employee is allowed to open. */
final class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	static final String CLOCK = "Time Clock";
	static final String SCHEDULE = "Schedule";
	static final String SHIFTS = "Shifts";
	static final String EMPLOYEES = "Employees";
	static final String INBOX = "Inbox";

	private final AppContext context;
	private final CardLayout cards = new CardLayout();
	private final JPanel pages = new JPanel(cards);
	private final Map<String, Tab> tabs = new LinkedHashMap<>();
	private final Map<String, JComponent> pageMap = new LinkedHashMap<>();
	private String current;

	MainPanel(AppContext context, Runnable onLogOut) {
		this.context = context;
		setLayout(new BorderLayout());
		setBackground(Theme.BACKGROUND);
		Employee user = context.user();

		JPanel header = new JPanel(new BorderLayout()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(Theme.SURFACE);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(Theme.BORDER);
				g.fillRect(0, getHeight() - 1, getWidth(), 1);
			}
		};
		header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
		header.setPreferredSize(new Dimension(0, 68));

		JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				Theme.antialias(g2);
				Theme.paintLogo(g2, 0, 17, 34, Theme.PRIMARY);
				g2.setFont(Theme.HEADING.deriveFont(17f));
				g2.setColor(Theme.INK);
				g2.drawString("WorkManager", 46, 40);
				g2.dispose();
			}
		};
		brand.setOpaque(false);
		brand.setPreferredSize(new Dimension(200, 68));
		header.add(brand, BorderLayout.WEST);

		JPanel tabRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 15));
		tabRow.setOpaque(false);
		header.add(tabRow, BorderLayout.CENTER);

		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
		right.setOpaque(false);
		right.add(new UserBadge(user));
		JButton logOut = Theme.button("Log out", Theme.ButtonStyle.SECONDARY);
		logOut.addActionListener(e -> onLogOut.run());
		right.add(logOut);
		header.add(right, BorderLayout.EAST);

		add(header, BorderLayout.NORTH);
		pages.setOpaque(false);
		add(pages, BorderLayout.CENTER);

		addPage(tabRow, CLOCK, new TimeClockPage(context));
		addPage(tabRow, SCHEDULE, new SchedulePage(context));
		if (!context.isWorker()) {
			addPage(tabRow, SHIFTS, new ShiftsPage(context));
			addPage(tabRow, EMPLOYEES, new EmployeesPage(context));
		}
		addPage(tabRow, INBOX, new InboxPage(context));
		context.onChange(this::refreshBadges);
		refreshBadges();
		showPage(CLOCK);
	}

	private void addPage(JPanel tabRow, String name, JComponent page) {
		Tab tab = new Tab(name);
		tabs.put(name, tab);
		tabRow.add(tab);
		pages.add(page, name);
		pageMap.put(name, page);
	}

	JComponent page(String name) {
		return pageMap.get(name);
	}

	void showPage(String name) {
		if (!tabs.containsKey(name)) {
			return;
		}
		current = name;
		cards.show(pages, name);
		for (Tab tab : tabs.values()) {
			tab.repaint();
		}
	}

	private void refreshBadges() {
		Tab inbox = tabs.get(INBOX);
		if (inbox != null) {
			inbox.badge = context.user() == null ? 0 : context.user().getNotifications().size();
			inbox.repaint();
		}
	}

	private final class Tab extends JComponent {
		private static final long serialVersionUID = 1L;
		private final String name;
		private int badge;
		private boolean hover;

		Tab(String name) {
			this.name = name;
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			FontMetrics fm = getFontMetrics(Theme.BODY_BOLD);
			setPreferredSize(new Dimension(fm.stringWidth(name) + (INBOX.equals(name) ? 64 : 34), 38));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					showPage(name);
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					hover = true;
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					hover = false;
					repaint();
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			Theme.antialias(g2);
			boolean active = name.equals(current);
			if (active) {
				g2.setColor(Theme.PRIMARY_SOFT);
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
			} else if (hover) {
				g2.setColor(Theme.SURFACE_ALT);
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
			}
			g2.setFont(Theme.BODY_BOLD);
			g2.setColor(active ? Theme.PRIMARY : Theme.MUTED);
			FontMetrics fm = g2.getFontMetrics();
			int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
			g2.drawString(name, 17, ty);
			if (INBOX.equals(name) && badge > 0) {
				String text = badge > 99 ? "99+" : String.valueOf(badge);
				g2.setFont(Theme.SMALL_BOLD);
				FontMetrics sm = g2.getFontMetrics();
				int bw = Math.max(20, sm.stringWidth(text) + 10);
				int bx = 17 + fm.stringWidth(name) + 7;
				g2.setColor(active ? Theme.PRIMARY : Theme.DANGER);
				g2.fillRoundRect(bx, (getHeight() - 18) / 2, bw, 18, 18, 18);
				g2.setColor(Color.WHITE);
				g2.drawString(text, bx + (bw - sm.stringWidth(text)) / 2, (getHeight() + sm.getAscent()) / 2 - 1);
			}
			g2.dispose();
		}
	}

	/** Avatar circle, name and rank of the logged-in employee. */
	private final class UserBadge extends JPanel {
		private static final long serialVersionUID = 1L;

		UserBadge(Employee user) {
			setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
			setOpaque(false);
			JComponent avatar = new JComponent() {
				private static final long serialVersionUID = 1L;

				@Override
				protected void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					Theme.antialias(g2);
					g2.setColor(context.colorOf(user));
					g2.fillOval(0, 0, 36, 36);
					g2.setColor(Color.WHITE);
					g2.setFont(Theme.BODY_BOLD);
					String initials = user.getFirstName().substring(0, 1) + user.getLastName().substring(0, 1);
					FontMetrics fm = g2.getFontMetrics();
					g2.drawString(initials, (36 - fm.stringWidth(initials)) / 2, (36 + fm.getAscent()) / 2 - 2);
					g2.dispose();
				}
			};
			avatar.setPreferredSize(new Dimension(36, 36));
			add(avatar);
			JPanel text = new JPanel();
			text.setOpaque(false);
			text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
			text.add(Theme.label(AppContext.name(user), Theme.BODY_BOLD, Theme.INK));
			text.add(Box.createVerticalStrut(1));
			String rank = Theme.rankName(user.getRank());
			String detail = rank.equalsIgnoreCase(user.getRole()) ? rank : rank + "  |  " + user.getRole();
			text.add(Theme.label(detail, Theme.SMALL, Theme.MUTED));
			add(text);
		}
	}
}
