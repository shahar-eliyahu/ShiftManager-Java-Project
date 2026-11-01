package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import Enums.Rank;

/** Colours, fonts and small reusable widgets for the light "planner" look. */
final class Theme {
	private Theme() {
	}

	// Paper-like light palette with an indigo accent
	static final Color BACKGROUND = new Color(0xF5F3EF);
	static final Color SURFACE = Color.WHITE;
	static final Color SURFACE_ALT = new Color(0xFAF9F6);
	static final Color BORDER = new Color(0xE4E0D8);
	static final Color GRID = new Color(0xEDEAE4);
	static final Color INK = new Color(0x1E2230);
	static final Color MUTED = new Color(0x6B6F7B);
	static final Color FAINT = new Color(0x9A9DA6);
	static final Color PRIMARY = new Color(0x4338CA);
	static final Color PRIMARY_DARK = new Color(0x312E81);
	static final Color PRIMARY_SOFT = new Color(0xEEF0FF);
	static final Color SUCCESS = new Color(0x15803D);
	static final Color SUCCESS_SOFT = new Color(0xE7F6EC);
	static final Color DANGER = new Color(0xB42318);
	static final Color DANGER_SOFT = new Color(0xFDECEA);
	static final Color WARNING = new Color(0xB45309);
	static final Color WARNING_SOFT = new Color(0xFEF3E2);
	static final Color TODAY = new Color(0xFFF8E6);

	// One colour per employee on the calendar
	static final List<Color> EMPLOYEE_COLORS = Arrays.asList(new Color(0x4338CA), new Color(0x0F766E),
			new Color(0xC2410C), new Color(0xBE185D), new Color(0x7C3AED), new Color(0x0369A1), new Color(0x4D7C0F),
			new Color(0xA16207), new Color(0x9F1239), new Color(0x475569));

	static final String FAMILY = pickFamily();
	static final Font DISPLAY = new Font(FAMILY, Font.BOLD, 30);
	static final Font TITLE = new Font(FAMILY, Font.BOLD, 22);
	static final Font HEADING = new Font(FAMILY, Font.BOLD, 15);
	static final Font BODY = new Font(FAMILY, Font.PLAIN, 13);
	static final Font BODY_BOLD = new Font(FAMILY, Font.BOLD, 13);
	static final Font SMALL = new Font(FAMILY, Font.PLAIN, 11);
	static final Font SMALL_BOLD = new Font(FAMILY, Font.BOLD, 11);
	static final Font CLOCK = new Font(FAMILY, Font.BOLD, 54);

	private static String pickFamily() {
		List<String> available = Arrays
				.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		for (String family : new String[] { "Segoe UI", "Inter", "Helvetica Neue", "DejaVu Sans" }) {
			if (available.contains(family)) {
				return family;
			}
		}
		return Font.SANS_SERIF;
	}

	static void antialias(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	static Color mix(Color a, Color b, double t) {
		return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
				(int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
				(int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
	}

	static JLabel label(String text, Font font, Color color) {
		JLabel label = new JLabel(text);
		label.setFont(font);
		label.setForeground(color);
		return label;
	}

	/** Rounded border with an optional fill painted by the panel. */
	static Border roundBorder(Color color, int radius, int padding) {
		return BorderFactory.createCompoundBorder(new RoundBorder(color, radius),
				BorderFactory.createEmptyBorder(padding, padding, padding, padding));
	}

	static final class RoundBorder extends AbstractBorder {
		private static final long serialVersionUID = 1L;
		private final Color color;
		private final int radius;

		RoundBorder(Color color, int radius) {
			this.color = color;
			this.radius = radius;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2 = (Graphics2D) g.create();
			antialias(g2);
			g2.setColor(color);
			g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
			g2.dispose();
		}

		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(2, 2, 2, 2);
		}
	}

	/** White card with rounded corners. */
	static class Card extends JPanel {
		private static final long serialVersionUID = 1L;
		private final Color fill;

		Card() {
			this(SURFACE);
		}

		Card(Color fill) {
			this.fill = fill;
			setOpaque(false);
			setBorder(roundBorder(BORDER, 18, 18));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			antialias(g2);
			g2.setColor(fill);
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
			g2.dispose();
			super.paintComponent(g);
		}
	}

	enum ButtonStyle {
		PRIMARY, SECONDARY, SUCCESS, DANGER, GHOST
	}

	static JButton button(String text, ButtonStyle style) {
		JButton button = new JButton(text) {
			private static final long serialVersionUID = 1L;
			private boolean hover;
			{
				addMouseListener(new MouseAdapter() {
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
				antialias(g2);
				Color fill;
				Color text;
				Color line = null;
				switch (style) {
				case PRIMARY:
					fill = PRIMARY;
					text = Color.WHITE;
					break;
				case SUCCESS:
					fill = SUCCESS;
					text = Color.WHITE;
					break;
				case DANGER:
					fill = DANGER_SOFT;
					text = DANGER;
					line = new Color(0xF5C2BC);
					break;
				case GHOST:
					fill = new Color(0, 0, 0, 0);
					text = PRIMARY;
					break;
				default:
					fill = SURFACE;
					text = INK;
					line = BORDER;
				}
				if (!isEnabled()) {
					fill = style == ButtonStyle.GHOST ? fill : new Color(0xEFEDE8);
					text = FAINT;
					line = null;
				} else if (hover) {
					fill = style == ButtonStyle.GHOST ? PRIMARY_SOFT : mix(fill, Color.BLACK, 0.07);
				}
				g2.setColor(fill);
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
				if (line != null) {
					g2.setColor(line);
					g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
				}
				g2.setFont(getFont());
				g2.setColor(text);
				FontMetrics fm = g2.getFontMetrics();
				int tx = (getWidth() - fm.stringWidth(getText())) / 2;
				int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
				g2.drawString(getText(), tx, ty);
				g2.dispose();
			}
		};
		button.setFont(BODY_BOLD);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setOpaque(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		int width = button.getFontMetrics(BODY_BOLD).stringWidth(text) + 34;
		button.setPreferredSize(new Dimension(Math.max(width, 84), 38));
		return button;
	}

	/** Small rounded label, used for ranks and statuses. */
	static JComponent pill(String text, Color fill, Color ink) {
		JLabel pill = new JLabel(text, SwingConstants.CENTER) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				antialias(g2);
				g2.setColor(fill);
				g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
				g2.dispose();
				super.paintComponent(g);
			}
		};
		pill.setFont(SMALL_BOLD);
		pill.setForeground(ink);
		pill.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
		return pill;
	}

	static String rankName(Rank rank) {
		switch (rank) {
		case ADMINISTRATOR:
			return "Administrator";
		case SUPERVISER:
			return "Supervisor";
		default:
			return "Worker";
		}
	}

	static Color[] rankColors(Rank rank) {
		switch (rank) {
		case ADMINISTRATOR:
			return new Color[] { PRIMARY_SOFT, PRIMARY };
		case SUPERVISER:
			return new Color[] { WARNING_SOFT, WARNING };
		default:
			return new Color[] { new Color(0xEEF2F6), new Color(0x475569) };
		}
	}

	static Color[] statusColors(String status) {
		switch (status) {
		case "Ongoing":
		case "In progress":
			return new Color[] { SUCCESS_SOFT, SUCCESS };
		case "Scheduled":
			return new Color[] { PRIMARY_SOFT, PRIMARY };
		case "Not scheduled":
			return new Color[] { WARNING_SOFT, WARNING };
		default:
			return new Color[] { new Color(0xF0EFEC), MUTED };
		}
	}

	/** Applies the light table look used on every list screen. */
	static JScrollPane styleTable(JTable table) {
		table.setFont(BODY);
		table.setForeground(INK);
		table.setRowHeight(40);
		table.setShowVerticalLines(false);
		table.setShowHorizontalLines(true);
		table.setGridColor(GRID);
		table.setIntercellSpacing(new Dimension(0, 1));
		table.setSelectionBackground(PRIMARY_SOFT);
		table.setSelectionForeground(INK);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);
		table.setBackground(SURFACE);
		table.setDefaultRenderer(Object.class, paddedRenderer());
		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		header.setDefaultRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus,
					int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, false, false, row, column);
				label.setFont(SMALL_BOLD);
				label.setForeground(MUTED);
				label.setBackground(SURFACE_ALT);
				label.setText(value == null ? "" : value.toString().toUpperCase());
				label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
						BorderFactory.createEmptyBorder(0, 14, 0, 14)));
				return label;
			}
		});
		header.setPreferredSize(new Dimension(0, 36));
		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(new RoundBorder(BORDER, 14));
		scroll.getViewport().setBackground(SURFACE);
		styleScroll(scroll);
		return scroll;
	}

	/** Thin grey scroll bars instead of the default ones. */
	static void styleScroll(JScrollPane scroll) {
		for (JScrollBar bar : new JScrollBar[] { scroll.getVerticalScrollBar(), scroll.getHorizontalScrollBar() }) {
			bar.setUI(new BasicScrollBarUI() {
				@Override
				protected void configureScrollBarColors() {
					thumbColor = new Color(0xD5D1C8);
					trackColor = SURFACE;
				}

				@Override
				protected JButton createDecreaseButton(int orientation) {
					return zeroButton();
				}

				@Override
				protected JButton createIncreaseButton(int orientation) {
					return zeroButton();
				}

				@Override
				protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle r) {
					Graphics2D g2 = (Graphics2D) g.create();
					antialias(g2);
					g2.setColor(thumbColor);
					g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
					g2.dispose();
				}

				@Override
				protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle r) {
					g.setColor(SURFACE);
					g.fillRect(r.x, r.y, r.width, r.height);
				}
			});
			bar.setPreferredSize(new Dimension(10, 10));
			bar.setUnitIncrement(16);
		}
	}

	private static JButton zeroButton() {
		JButton button = new JButton();
		button.setPreferredSize(new Dimension(0, 0));
		button.setMinimumSize(new Dimension(0, 0));
		button.setMaximumSize(new Dimension(0, 0));
		return button;
	}

	/** Small flat arrow button used by combo boxes and spinners. */
	private static JButton arrowButton(boolean up) {
		JButton button = new JButton() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				antialias(g2);
				g2.setColor(getModel().isRollover() ? SURFACE_ALT : SURFACE);
				g2.fillRect(0, 0, getWidth(), getHeight());
				g2.setColor(MUTED);
				g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				int cx = getWidth() / 2;
				int cy = getHeight() / 2;
				if (up) {
					g2.drawLine(cx - 4, cy + 2, cx, cy - 2);
					g2.drawLine(cx, cy - 2, cx + 4, cy + 2);
				} else {
					g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
					g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
				}
				g2.dispose();
			}
		};
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setRolloverEnabled(true);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.setPreferredSize(new Dimension(28, 16));
		return button;
	}

	static void styleCombo(JComboBox<?> combo) {
		combo.setUI(new BasicComboBoxUI() {
			@Override
			protected JButton createArrowButton() {
				return arrowButton(false);
			}

			@Override
			public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
				g.setColor(combo.isEnabled() ? SURFACE : SURFACE_ALT);
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		});
		combo.setFont(BODY);
		combo.setForeground(INK);
		combo.setBackground(SURFACE);
		combo.setBorder(roundBorder(BORDER, 10, 4));
	}

	static void styleSpinner(JSpinner spinner) {
		spinner.setUI(new BasicSpinnerUI() {
			@Override
			protected Component createNextButton() {
				JButton button = arrowButton(true);
				installNextButtonListeners(button);
				return button;
			}

			@Override
			protected Component createPreviousButton() {
				JButton button = arrowButton(false);
				installPreviousButtonListeners(button);
				return button;
			}
		});
		spinner.setFont(BODY);
		spinner.setBorder(roundBorder(BORDER, 10, 3));
		spinner.setBackground(SURFACE);
		JComponent editor = spinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			javax.swing.JFormattedTextField text = ((JSpinner.DefaultEditor) editor).getTextField();
			text.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));
			text.setHorizontalAlignment(javax.swing.JTextField.LEFT);
			text.setFont(BODY);
			text.setForeground(INK);
			text.setBackground(SURFACE);
		}
	}

	static DefaultTableCellRenderer paddedRenderer() {
		return new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus,
					int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, selected, false, row, column);
				label.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
				if (!selected) {
					label.setBackground(SURFACE);
				}
				return label;
			}
		};
	}

	/** Renders a cell as a coloured pill (status or rank text). */
	static DefaultTableCellRenderer pillRenderer(java.util.function.Function<String, Color[]> colors) {
		return new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			private String text = "";
			private boolean selected;

			@Override
			public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean focus,
					int row, int column) {
				text = value == null ? "" : value.toString();
				selected = isSelected;
				return this;
			}

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				antialias(g2);
				g2.setColor(selected ? PRIMARY_SOFT : SURFACE);
				g2.fillRect(0, 0, getWidth(), getHeight());
				Color[] c = colors.apply(text);
				g2.setFont(SMALL_BOLD);
				FontMetrics fm = g2.getFontMetrics();
				int w = fm.stringWidth(text) + 20;
				int h = 22;
				int y = (getHeight() - h) / 2;
				g2.setColor(c[0]);
				g2.fillRoundRect(14, y, w, h, h, h);
				g2.setColor(c[1]);
				g2.drawString(text, 24, y + (h + fm.getAscent() - fm.getDescent()) / 2);
				g2.dispose();
			}
		};
	}

	/** Square logo: a calendar page with a check mark. */
	static void paintLogo(Graphics2D g2, int x, int y, int size, Color base) {
		g2.setPaint(new GradientPaint(x, y, base, x + size, y + size, mix(base, Color.BLACK, 0.25)));
		g2.fillRoundRect(x, y, size, size, size / 3, size / 3);
		g2.setColor(Color.WHITE);
		int pad = size / 4;
		g2.fillRoundRect(x + pad, y + pad + 2, size - 2 * pad, size - 2 * pad - 2, 5, 5);
		g2.setColor(base);
		g2.fillRect(x + pad, y + pad + 2, size - 2 * pad, Math.max(3, size / 9));
		g2.setStroke(new BasicStroke(Math.max(2f, size / 14f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		int cx = x + size / 2;
		int cy = y + size / 2 + size / 10;
		g2.drawLine(cx - size / 8, cy, cx - size / 30, cy + size / 10);
		g2.drawLine(cx - size / 30, cy + size / 10, cx + size / 7, cy - size / 10);
	}
}
