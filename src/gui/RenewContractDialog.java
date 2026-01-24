package gui;

import dao.ContractDAO;
import model.Contract;
import model.Employee;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

public class RenewContractDialog extends JDialog {
    private boolean isSaved = false;
    private Contract currentContract;
    private ContractDAO contractDAO;

    private JComboBox<String> cmbMonth;
    private JComboBox<Integer> cmbYear;

    public RenewContractDialog(Window owner, Employee emp, Contract contract) {
        super(owner, "Renew Contract: " + emp.getFullName(), ModalityType.APPLICATION_MODAL);
        this.currentContract = contract;
        this.contractDAO = new ContractDAO();

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(new JLabel("Current End Date:"));
        mainPanel.add(new JLabel(contract.getEndDate().toString()));

        mainPanel.add(new JLabel("New End Date (Month):"));
        String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December" };
        cmbMonth = new JComboBox<>(months);
        // Default to current contract month
        cmbMonth.setSelectedIndex(contract.getEndDate().getMonthValue() - 1);
        mainPanel.add(cmbMonth);

        mainPanel.add(new JLabel("New End Date (Year):"));
        int startYear = contract.getEndDate().getYear();
        Integer[] years = new Integer[11];
        for (int i = 0; i <= 10; i++) {
            years[i] = startYear + i;
        }
        cmbYear = new JComboBox<>(years);
        cmbYear.setSelectedItem(startYear);
        mainPanel.add(cmbYear);

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton btnRenew = new JButton("Renew");
        JButton btnCancel = new JButton("Cancel");

        buttonPanel.add(btnRenew);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnRenew.addActionListener(e -> onRenew());

        pack();
        setLocationRelativeTo(owner);
    }

    private void onRenew() {
        int year = (Integer) cmbYear.getSelectedItem();
        int month = cmbMonth.getSelectedIndex() + 1;

        // Last day of selected month
        LocalDate newEndDate = LocalDate.of(year, month, 1)
                .withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth());

        if (newEndDate.isBefore(currentContract.getEndDate())) {
            JOptionPane.showMessageDialog(this,
                    "The new end date must be after the current end date (" + currentContract.getEndDate() + ")",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            contractDAO.updateContractEndDate(currentContract.getId(), newEndDate);
            isSaved = true;
            JOptionPane.showMessageDialog(this, "Contract renewed successfully until " + newEndDate, "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to update contract: " + e.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public boolean isSaved() {
        return isSaved;
    }
}
