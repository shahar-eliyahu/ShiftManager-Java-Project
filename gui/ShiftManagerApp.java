package gui;

import java.awt.AlphaComposite;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import Manegers.ShiftManager;
import SingleObjectClasses.Shift;

/**
 * Swing front end for the Shift Management System. It uses the same
 * ShiftManager and EmployeeDataBase as the console application.
 *
 * Run: java gui.ShiftManagerApp
 * Screenshots: java gui.ShiftManagerApp --screenshots folder
 */
public final class ShiftManagerApp {
	private static final String LOGIN = "login";
	private static final String MAIN = "main";

	private final AppContext context = new AppContext();
	private final JFrame frame = new JFrame("WorkManager | Shift Management System");
	private final CardLayout cards = new CardLayout();
	private final JPanel root = new JPanel(cards);
	private final LoginPanel login;
	private MainPanel main;

	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception ignored) {
			// default look and feel
		}
		UIManager.put("ToolTip.font", Theme.SMALL);
		UIManager.put("ToolTip.background", Theme.SURFACE);
		UIManager.put("ToolTip.foreground", Theme.INK);
		UIManager.put("ToolTip.border", Theme.roundBorder(Theme.BORDER, 8, 6));
		UIManager.put("ComboBox.selectionBackground", Theme.PRIMARY_SOFT);
		UIManager.put("ComboBox.selectionForeground", Theme.INK);
		UIManager.put("ComboBox.background", Theme.SURFACE);
		UIManager.put("ScrollBar.width", 10);
		ToolTipManager.sharedInstance().setInitialDelay(250);

		if (args.length >= 2 && "--screenshots".equals(args[0])) {
			File folder = new File(args[1]);
			folder.mkdirs();
			SwingUtilities.invokeAndWait(() -> {
				try {
					new ShiftManagerApp().screenshots(folder);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			System.exit(0);
		}
		SwingUtilities.invokeLater(() -> new ShiftManagerApp().frame.setVisible(true));
	}

	private ShiftManagerApp() {
		DemoData.seed(context);
		login = new LoginPanel(context, employee -> openMain());
		root.add(login, LOGIN);
		frame.setContentPane(root);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension(1180, 760));
		frame.setSize(1380, 880);
		frame.setLocationRelativeTo(null);
	}

	private void openMain() {
		context.clearListeners();
		if (main != null) {
			root.remove(main);
		}
		main = new MainPanel(context, this::logOut);
		root.add(main, MAIN);
		cards.show(root, MAIN);
		root.revalidate();
	}

	private void logOut() {
		context.employeeDataBase.logOut();
		context.clearListeners();
		login.reset();
		cards.show(root, LOGIN);
	}

	// ------------------------------------------------------------------ screenshots

	private void screenshots(File folder) throws IOException {
		frame.setVisible(true);
		save(root, folder, "01-login");

		signIn("John", "987654321");
		main.showPage(MainPanel.CLOCK);
		save(root, folder, "02-time-clock");
		logOut();

		signIn("Avi", "123456789");
		main.showPage(MainPanel.SCHEDULE);
		save(root, folder, "03-schedule");
		main.showPage(MainPanel.SHIFTS);
		((ShiftsPage) main.page(MainPanel.SHIFTS)).select(1);
		save(root, folder, "04-shifts");

		ShiftManager shifts = context.shiftManager;
		Shift open = null;
		for (Shift shift : shifts.getShifts()) {
			if (shift.getEmployee() == null && shift.getStartDateTime() != null) {
				open = shift;
			}
		}
		if (open != null) {
			JDialog dialog = ShiftDialog.build(root, context, open);
			saveDialog(root, dialog, folder, "05-assign-shift");
		}
		main.showPage(MainPanel.EMPLOYEES);
		((EmployeesPage) main.page(MainPanel.EMPLOYEES)).select(4);
		save(root, folder, "06-employees");
		JDialog employeeDialog = EmployeeDialog.build(root, context,
				context.employeeDataBase.findEmployeeById("333333333"));
		saveDialog(root, employeeDialog, folder, "07-edit-employee");

		// A few real actions, so the inbox shows different kinds of notifications
		SingleObjectClasses.Employee alice = context.employeeDataBase.findEmployeeById("111111111");
		SingleObjectClasses.Employee eve = context.employeeDataBase.findEmployeeById("555555555");
		if (open != null) {
			shifts.assignEmployee(open, eve);
		}
		shifts.checkIn(alice);
		shifts.checkOut(alice);
		shifts.undoLastCheckInOut(alice);
		context.employeeDataBase.updateEmployee(context.employeeDataBase.findEmployeeById("333333333"), "Charlie",
				"Brown", new SingleObjectClasses.Date(5, 8, 1994), "Head Cook", Enums.Rank.WORKER);
		context.changed();
		main.showPage(MainPanel.INBOX);
		save(root, folder, "08-inbox");
		logOut();

		signIn("Alice", "111111111");
		main.showPage(MainPanel.SCHEDULE);
		save(root, folder, "09-worker-schedule");
		main.showPage(MainPanel.CLOCK);
		save(root, folder, "10-worker-time-clock");
		frame.dispose();
	}

	private void signIn(String user, String password) {
		login.fill(user, password);
		login.signIn();
	}

	private static BufferedImage render(Component component) {
		layoutAll(component);
		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		component.paint(g2);
		g2.dispose();
		return image;
	}

	private static void layoutAll(Component component) {
		component.doLayout();
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				layoutAll(child);
			}
		}
	}

	private static void save(Component component, File folder, String name) throws IOException {
		component.validate();
		ImageIO.write(render(component), "png", new File(folder, name + ".png"));
		System.out.println("Saved " + name + ".png");
	}

	/** Dialog drawn over the dimmed screen it was opened from. */
	private static void saveDialog(Component behind, JDialog dialog, File folder, String name) throws IOException {
		dialog.pack();
		dialog.setSize(new Dimension(Math.max(dialog.getWidth(), 520), dialog.getHeight()));
		Container content = dialog.getContentPane();
		content.setSize(dialog.getContentPane().getPreferredSize().width < 520 ? 520
				: dialog.getContentPane().getPreferredSize().width, content.getPreferredSize().height);
		content.validate();
		BufferedImage back = render(behind);
		BufferedImage front = render(content);
		Graphics2D g2 = back.createGraphics();
		Theme.antialias(g2);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
		g2.setColor(new Color(0x1E2230));
		g2.fillRect(0, 0, back.getWidth(), back.getHeight());
		g2.setComposite(AlphaComposite.SrcOver);
		int x = (back.getWidth() - front.getWidth()) / 2;
		int y = (back.getHeight() - front.getHeight()) / 2;
		for (int i = 12; i > 0; i--) {
			g2.setColor(new Color(0, 0, 0, 6));
			g2.fillRoundRect(x - i, y - i + 6, front.getWidth() + 2 * i, front.getHeight() + 2 * i, 24 + i, 24 + i);
		}
		g2.setClip(new java.awt.geom.RoundRectangle2D.Double(x, y, front.getWidth(), front.getHeight(), 18, 18));
		g2.drawImage(front, x, y, null);
		g2.dispose();
		ImageIO.write(back, "png", new File(folder, name + ".png"));
		dialog.dispose();
		System.out.println("Saved " + name + ".png");
	}
}
