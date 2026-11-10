package ui;

import model.User;
import storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.GridLayout;

/**
 * Small helper methods used by all screens, so the same code is not repeated.
 */
public class UIUtil {

    // =========================
    // MESSAGES
    // =========================

    // Shows an error message (used when a file cannot be saved)
    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Shows a warning when the user's input or action is not allowed
    public static void validation(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    // Shows a success message
    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Asks a Yes/No question and returns true if the user clicked Yes
    public static boolean confirm(Component parent, String message) {
        int answer = JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.YES_OPTION;
    }

    // =========================
    // PANELS
    // =========================

    // Creates a two-column form panel: label, field, label, field ...
    public static JPanel form(String title) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return panel;
    }

    // Puts a border with a title around a component
    public static void titled(JComponent c, String title) {
        c.setBorder(BorderFactory.createTitledBorder(title));
    }

    // Wraps a component in a scroll pane so it can scroll when it is too big
    public static JScrollPane scroll(Component c) {
        return new JScrollPane(c);
    }

    // =========================
    // TABLES
    // =========================

    // Creates a table model whose cells cannot be edited by typing
    public static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // Creates a table where only one row can be selected at a time
    public static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        // Columns keep their own width; a scroll bar appears if the table is wider than the window
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        fitColumns(table);
        return table;
    }

    // Makes every column wide enough for its longest text (about 7 pixels per character).
    // Call this after the rows of a table have been loaded.
    public static void fitColumns(JTable table) {
        for (int col = 0; col < table.getColumnCount(); col++) {
            int longest = table.getColumnName(col).length();
            for (int row = 0; row < table.getRowCount(); row++) {
                Object value = table.getValueAt(row, col);
                if (value != null && value.toString().length() > longest) {
                    longest = value.toString().length();
                }
            }
            int width = longest * 7 + 16;
            if (width > 500) {
                width = 500;
            }
            table.getColumnModel().getColumn(col).setPreferredWidth(width);
        }
    }

    // Returns the value of a column in the selected row, or null if no row is selected
    public static String selectedValue(JTable table, int column) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        Object value = table.getValueAt(row, column);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    // =========================
    // DATA HELPERS
    // =========================

    // Returns the name of a user, e.g. "Sadiksha Timsina"
    public static String nameOf(String userId) {
        User user = DataStore.users().findById(userId);
        if (user == null) {
            return userId + " (removed)";
        }
        return user.getName();
    }

    // Returns the ID and name of a user, e.g. "C001 - Binaya Shrestha"
    public static String idAndName(String userId) {
        return DataStore.users().idAndName(userId);
    }

    // Formats an amount of money, e.g. "RM 80.00"
    public static String money(double amount) {
        return String.format("RM %.2f", amount);
    }
}
