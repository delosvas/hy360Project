package gui;

import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import dao.SettingsDAO;

/*
 * Settings Panel
 * 
 * Is the user interface for viewing and modifying all payroll settings 
 * of the system.
 * 
 * It is responsible :
 * 1. retrieve all settings from the database
 * 2. display the settings in a layout using GridBagLayout
 * 3. provide save and reset buttons for either updating either reverting settings
 */
public class SettingsPanel extends JPanel {

	private SettingsDAO dao;
	private Map<String, JFormattedTextField> fields = new HashMap<>();
    private Map<String, Double> initialValues = new HashMap<>();

    /*
     * Constructor of the class that initializes the panel and the buttons' actions
     */
	public SettingsPanel() {
		dao= new SettingsDAO();
		setLayout(new BorderLayout());
		
		// Main panel for settings fields tab
		JPanel layoutPanel=new JPanel(new GridBagLayout());
		layoutPanel.setBackground(new Color(245, 245, 245));
		add(layoutPanel, BorderLayout.CENTER);
		
		loadSettings(layoutPanel); // Loading settings from the database
		
		JPanel buttonPanel = new JPanel(); // Button panel on the bottom
		
		JButton btnSave = new JButton("Save"); // Button for save
		btnSave.setFont(new Font("Arial" ,Font.PLAIN, 12));
		btnSave.setBackground(new Color(60, 120, 200));
		btnSave.setForeground(new Color(60, 120, 200));
		btnSave.setPreferredSize(new Dimension(90, 30));
		
		JButton btnReset = new JButton("Reset"); // Button for reset
		btnReset.setFont(new Font("Arial" ,Font.PLAIN, 12));
		btnReset.setBackground(new Color(200, 60, 60));
		btnReset.setForeground(new Color(200, 60, 60));
		btnReset.setPreferredSize(new Dimension(90, 30));
		
		// Adding the buttons to the panel
		buttonPanel.add(btnSave);
        buttonPanel.add(btnReset);
        add(buttonPanel, BorderLayout.SOUTH);
		
        // Button actions
		btnSave.addActionListener(e -> saveSettings());
		btnReset.addActionListener(e -> resetValues());
		
	}
	
	/*
	 * The function loads all the system settings from the database 
	 * and creates a label and an input field for every setting
	 * 
	 * @param layoutPanel
	 */
	public void loadSettings(JPanel layoutPanel) {
		try {
			
			// Clearing any previous components and old left values
			layoutPanel.removeAll();
			fields.clear();
			initialValues.clear();
			
			Map <String, Double> settings=dao.getAllSettings();
			GridBagConstraints gbc= new GridBagConstraints();
			
			gbc.insets=new Insets(6,6,6,6);
			gbc.fill=GridBagConstraints.HORIZONTAL;
			gbc.gridy=0;
			
			// Number format used for decimal values
			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMinimumFractionDigits(2);
			format.setMaximumFractionDigits(2);
		
			// UI row creation for every setting
			for(Map.Entry<String, Double> entry : settings.entrySet()) {
				String key=entry.getKey();
				Double value=entry.getValue();
				
				// Label for every setting
				JLabel label = new JLabel(key + ":");
	            label.setFont(new Font("Arial", Font.PLAIN, 12));
	            
	            // Input field 
				JFormattedTextField field =new JFormattedTextField(format);
				field.setValue(value);
				field.setToolTipText("New value must be greater than the current one");
				
				// Storing for validation and saving 
				fields.put(key, field);
				initialValues.put(key, value);

				gbc.gridx=0; // Label
				gbc.weightx=0.3;
				layoutPanel.add(label, gbc);
				
				// Input field
				gbc.gridx=1;
				gbc.weightx=0.7;
				layoutPanel.add(field, gbc);
				
				gbc.gridy++;
				
			}
			
			gbc.weighty=1.0;
			
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load settings: " + e.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
	}
	
	/*
	 * Function that validates that the settings are rightly changed
	 * and saves the updated value of them 
	 */
	public void saveSettings() {
		try {
			for (String key: fields.keySet()) { 
				double newValue = ((Number) fields.get(key).getValue()).doubleValue();
				double currentValue = initialValues.get(key);
				if(currentValue>newValue) { // Checking to prevent decreasing values
					JOptionPane.showMessageDialog(this, "Setting's value " + key +" cannot be decreased", "Warning", 
							JOptionPane.INFORMATION_MESSAGE); 
					return;
				}
				dao.updateSetting(key, newValue); // Updating database
			}
			JOptionPane.showMessageDialog(this, "Settings updated successfully.", "Success", 
					JOptionPane.INFORMATION_MESSAGE); 
			resetInitialValues(); // Setting back to initial values
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error saving settings", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		} 
	}

	/*
	 * Updates the initial value for each setting
	 * in case of cancel or wrong value settings
	 */
	public void resetInitialValues() {
		for (String key: fields.keySet()) { 
			initialValues.put(key, ((Number) fields.get(key).getValue()).doubleValue());
    	}
	}
	
	/*
	 * Restores all fields to their last saved values used for when
	 * the user wants to dispose of any unsaved changes
	 */
    public void resetValues() {
    	for (String key: fields.keySet()) { 
    		fields.get(key).setValue(initialValues.get(key));
    	}
    }
}

