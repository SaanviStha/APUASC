package ui;

import model.Receipt;
import util.ActivityLog;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Shows a receipt with an option to save it as a text file.
 */
public class ReceiptDialog extends JDialog {

    private final Receipt receipt;
    private final JTextArea area;

    public ReceiptDialog(Window owner, Receipt receipt) {
        super(owner, "Receipt " + receipt.getReceiptNo(), ModalityType.APPLICATION_MODAL);
        this.receipt = receipt;

        // The receipt text, shown in a fixed-width font so the columns line up
        area = new JTextArea(receipt.format());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);

        JButton saveButton = new JButton("Save as .txt");
        JButton closeButton = new JButton("Close");
        saveButton.addActionListener(e -> saveToFile());
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        add(UIUtil.scroll(area), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setSize(450, 520);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    // Writes the receipt into receipts/<receipt number>.txt
    private void saveToFile() {
        File folder = new File("receipts");
        folder.mkdirs();   // create the folder if it does not exist yet
        File file = new File(folder, receipt.getReceiptNo() + ".txt");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(area.getText());
            writer.close();
            ActivityLog.write("Receipt " + receipt.getReceiptNo() + " saved to file");
            UIUtil.info(this, "Receipt saved to " + file.getAbsolutePath());
        } catch (IOException ex) {
            UIUtil.error(this, "Could not save receipt: " + ex.getMessage());
        }
    }
}
