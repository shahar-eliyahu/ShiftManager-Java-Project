package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import SingleObjectClasses.Employee;

/** Sign-in screen. Authentication goes through EmployeeDataBase and the DataBase singleton. */
final class LoginPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final AppContext context;
	private final Consumer<Employee> onLogin;
	private final JTextField username = field(new JTextField());
	private final JPasswordField password = (JPasswordField) field(new JPasswordField());
	private final JLabel error = Theme.label(" ", Theme.SMALL_BOLD, Theme.DANGER);

	LoginPanel(AppContext context, Consumer<Employee> onLogin) {
		this.context = context;
		this.onLogin = onLogin;
		setLayout(new GridBagLayout());
		setBackground(Theme.BACKGROUND);

		JPanel shell = new JPanel(new BorderLayout());
		shell.setOpaque(false);
		shell.add(new BrandPanel(), BorderLayout.WEST);
		shell.add(formCard(), BorderLayout.CENTER);
		add(shell);
	}

	private static JTextField field(JTextField field) {
		field.setFont(Theme.BODY);
		field.setForeground(Theme.INK);
		field.setBackground(Theme.SURFACE);
		field.setCaretColor(Theme.PRIMARY);
		field.setBorder(Theme.roundBorder(Theme.BORDER, 12, 9));
		field.setPreferredSize(new Dimension(320, 42));
		field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
		field.setAlignmentX(LEFT_ALIGNMENT);
		return field;
	}

	private JComponent formCard() {
		Theme.Card card = new Theme.Card();
		card.setBorder(BorderFactory.createEmptyBorder(48, 48, 40, 48));
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setPreferredSize(new Dimension(440, 560));

		card.add(left(Theme.label("Sign in", Theme.TITLE, Theme.INK)));
		card.add(Box.createVerticalStrut(6));
		card.add(left(Theme.label("Your username is your first name and your", Theme.BODY, Theme.MUTED)));
		card.add(left(Theme.label("password is your 9-digit ID.", Theme.BODY, Theme.MUTED)));
		card.add(Box.createVerticalStrut(26));
		card.add(left(Theme.label("Username", Theme.SMALL_BOLD, Theme.MUTED)));
		card.add(Box.createVerticalStrut(6));
		card.add(username);
		card.add(Box.createVerticalStrut(16));
		card.add(left(Theme.label("Password", Theme.SMALL_BOLD, Theme.MUTED)));
		card.add(Box.createVerticalStrut(6));
		card.add(password);
		card.add(Box.createVerticalStrut(10));
		card.add(left(error));
		card.add(Box.createVerticalStrut(10));

		JButton signIn = Theme.button("Sign in", Theme.ButtonStyle.PRIMARY);
		signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
		signIn.setAlignmentX(LEFT_ALIGNMENT);
		signIn.addActionListener(e -> signIn());
		password.addActionListener(e -> signIn());
		username.addActionListener(e -> password.requestFocusInWindow());
		card.add(signIn);

		card.add(Box.createVerticalStrut(30));
		card.add(left(Theme.label("DEMO ACCOUNTS", Theme.SMALL_BOLD, Theme.FAINT)));
		card.add(Box.createVerticalStrut(8));
		JPanel demo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		demo.setOpaque(false);
		demo.setAlignmentX(LEFT_ALIGNMENT);
		demo.add(demoButton("Avi", "123456789", "Administrator"));
		demo.add(Box.createHorizontalStrut(8));
		demo.add(demoButton("John", "987654321", "Supervisor"));
		demo.add(Box.createHorizontalStrut(8));
		demo.add(demoButton("Alice", "111111111", "Worker"));
		card.add(demo);
		card.add(Box.createVerticalGlue());
		return card;
	}

	private JButton demoButton(String user, String pass, String role) {
		JButton button = Theme.button(role, Theme.ButtonStyle.SECONDARY);
		button.setFont(Theme.SMALL_BOLD);
		button.setPreferredSize(new Dimension(button.getPreferredSize().width - 6, 32));
		button.setToolTipText(user + " / " + pass);
		button.addActionListener(e -> fill(user, pass));
		return button;
	}

	private static JComponent left(JComponent component) {
		component.setAlignmentX(LEFT_ALIGNMENT);
		return component;
	}

	void fill(String user, String pass) {
		username.setText(user);
		password.setText(pass);
		error.setText(" ");
	}

	void reset() {
		fill("", "");
	}

	void signIn() {
		Employee employee = context.employeeDataBase.authenticate(username.getText(),
				new String(password.getPassword()));
		if (employee == null) {
			error.setText("Wrong username or password.");
			return;
		}
		error.setText(" ");
		onLogin.accept(employee);
	}

	/** Indigo side panel with the logo and a small drawn week preview. */
	private static final class BrandPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		BrandPanel() {
			setPreferredSize(new Dimension(420, 560));
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			Theme.antialias(g2);
			int w = getWidth();
			int h = getHeight();
			g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY, w, h, Theme.PRIMARY_DARK));
			g2.fillRoundRect(0, 0, w + 30, h - 1, 18, 18);

			Theme.paintLogo(g2, 44, 48, 46, new Color(0x6366F1));
			g2.setColor(Color.WHITE);
			g2.setFont(Theme.TITLE);
			g2.drawString("WorkManager", 104, 80);
			g2.setFont(Theme.SMALL);
			g2.setColor(new Color(0xC7D2FE));
			g2.drawString("Shift Management System", 105, 98);

			g2.setFont(Theme.DISPLAY.deriveFont(28f));
			g2.setColor(Color.WHITE);
			g2.drawString("Shifts, check-ins", 44, 190);
			g2.drawString("and your team,", 44, 226);
			g2.drawString("in one place.", 44, 262);

			// mini week preview
			String[] days = { "M", "T", "W", "T", "F", "S", "S" };
			int[][] blocks = { { 0, 2 }, { 1, 3 }, { 0, 1 }, { 2, 3 }, { 0, 2 }, { 1, 2 }, { 3, 3 } };
			int x0 = 44;
			int y0 = 320;
			int col = 46;
			g2.setColor(new Color(255, 255, 255, 28));
			g2.fillRoundRect(x0 - 14, y0 - 16, col * 7 + 20, 176, 16, 16);
			g2.setFont(Theme.SMALL_BOLD);
			Color[] colors = { new Color(0xA5B4FC), new Color(0x5EEAD4), new Color(0xFDBA74), new Color(0xF9A8D4) };
			for (int d = 0; d < 7; d++) {
				g2.setColor(new Color(0xC7D2FE));
				g2.drawString(days[d], x0 + d * col + 14, y0 + 6);
				for (int b = blocks[d][0]; b <= blocks[d][1]; b++) {
					g2.setColor(colors[(d + b) % colors.length]);
					g2.fillRoundRect(x0 + d * col, y0 + 18 + b * 34, col - 8, 28, 8, 8);
				}
			}
			g2.setStroke(new BasicStroke(2f));
			g2.setColor(new Color(0xFDE68A));
			int nowX = x0 + 3 * col - 4;
			g2.drawLine(nowX, y0 + 14, nowX, y0 + 150);
			g2.fillOval(nowX - 4, y0 + 10, 8, 8);

			g2.setFont(Theme.SMALL);
			g2.setColor(new Color(0xC7D2FE));
			g2.drawString("Java OOP project  |  Swing interface", 44, h - 36);
			g2.dispose();
		}
	}
}
