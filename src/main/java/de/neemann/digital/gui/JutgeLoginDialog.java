/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The Jutge Login of Digital
 * <p>
 * Created by Miquel Torner on 20.11.2024.
 */
public class JutgeLoginDialog extends JDialog {
    private String email;
    private String password;
    private String problem;
    private String topModule;
    private String anotations;

    /**
     * Returns the login window.
     *
     * @param parent the parent
     */
    public JutgeLoginDialog(JFrame parent) {
        super(parent, "Login", true); // Modal dialog with parent window
        setSize(450, 350); // Increase the dialog size for better visibility
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Create components
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 25)); // Set a preferred width for the email field

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 25)); // Set a preferred width for the password field

        JLabel problemLabel = new JLabel("Problem:");
        JTextField problemField = new JTextField();
        problemField.setPreferredSize(new Dimension(200, 25)); // Set a preferred width for the email field

        JLabel topModuleLabel = new JLabel("Top module name:");
        JTextField topModuleField = new JTextField();
        topModuleField.setPreferredSize(new Dimension(200, 25)); // Set a preferred width for the email field

        JLabel anotationsLabel = new JLabel("Anotations:");
        JTextField anotationsField = new JTextField();
        anotationsField.setPreferredSize(new Dimension(200, 25)); // Set a preferred width for the email field

        JCheckBox rememberCredentialsCheckBox = new JCheckBox("Remember Credentials (It will be sored in plain text)");

        JButton sendButton = new JButton("Send");

        // Load any existing credentials
        loadJutgeCredentials(topModuleField, problemField, emailField, passwordField, rememberCredentialsCheckBox);

        // Set up layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); // Padding

        // Add components to the layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(topModuleLabel, gbc);
        gbc.gridx = 1;
        add(topModuleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(problemLabel, gbc);
        gbc.gridx = 1;
        add(problemField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(anotationsLabel, gbc);
        gbc.gridx = 1;
        add(anotationsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(emailLabel, gbc);
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2; // Span checkbox across two columns
        add(rememberCredentialsCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2; // Span button across two columns
        add(sendButton, gbc);

        // Add button functionality
        sendButton.addActionListener(e -> {
            email = emailField.getText();
            password = new String(passwordField.getPassword());
            problem = problemField.getText();
            topModule = topModuleField.getText();
            anotations = anotationsField.getText();

            if (email.isEmpty() || password.isEmpty() || problem.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Some of the fields are empty.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                saveJutgeCredentials(problem, topModule, email, password, rememberCredentialsCheckBox.isSelected());
                dispose();
            }
        });
    }

    /**
     * Returns the user credentials dialog
     *
     * @return user credentials dailog
     */
    public JutgeCredentials showDialog() {
        setVisible(true); // Open dialog and block until it's closed
        if (email != null && password != null && problem != null) {
            return new JutgeCredentials(email, password, problem, topModule, anotations);
        }
        return null; // Return null if dialog was closed without valid inputs
    }

    /**
     * User Credentials Class
     *
     */
    public static class JutgeCredentials {
        private final String email;
        private final String password;
        private final String problem;
        private final String topModule;
        private final String anotations;

        /**
         * Returns the user credentials
         *
         * @param email      the email of the user
         * @param password   the password of the user
         * @param problem    the problem to be evaluated
         * @param topModule  the top module name
         * @param anotations anotations
         */
        public JutgeCredentials(String email, String password, String problem, String topModule, String anotations) {
            this.email = email;
            this.password = password;
            this.problem = problem;
            this.topModule = topModule;
            this.anotations = anotations;
        }

        /**
         * Get email method
         *
         * @return the email
         */
        public String getEmail() {
            return email;
        }

        /**
         * Get password method
         *
         * @return the password
         */
        public String getPassword() {
            return password;
        }

        /**
         * Get password method
         *
         * @return the password
         */
        public String getProblem() {
            return problem;
        }

        /**
         * Get anotations
         *
         * @return the anotations
         */
        public String getAnotations() {
            return anotations;
        }

        /**
         * Get the top module
         *
         * @return the top module
         */
        public String getTopModule() {
            return topModule;
        }

        /**
         * Get user credentials
         *
         * @return the user credentials
         */
        public JutgeCredentials getCredentials() {
            return new JutgeCredentials(email, password, problem, topModule, anotations);
        }
    }

    private void saveJutgeCredentials(String problem, String topModule, String email, String password,
            boolean rememberCredentials) {
        Properties properties = new Properties();

        // Always save the problem field
        properties.setProperty("problem", problem);
        properties.setProperty("topModule", topModule);

        // Conditionally save email and password
        if (rememberCredentials) {
            properties.setProperty("email", email);
            properties.setProperty("password", password);
        }

        try (FileOutputStream fos = new FileOutputStream("credentials.properties")) {
            properties.store(fos, "Jutge Credentials");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadJutgeCredentials(JTextField topModuleField, JTextField problemField, JTextField emailField,
            JPasswordField passwordField, JCheckBox rememberCredentialsCheckBox) {

        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream("credentials.properties")) {
            properties.load(fis);

            // Always load the problem field
            problemField.setText(properties.getProperty("problem", ""));
            topModuleField.setText(properties.getProperty("topModule", ""));

            // Conditionally load email and password if they exist
            String email = properties.getProperty("email", "");
            String password = properties.getProperty("password", "");

            emailField.setText(email);
            passwordField.setText(password);

            // Check the checkbox if email and password are not empty
            if (!email.isEmpty() && !password.isEmpty()) {
                rememberCredentialsCheckBox.setSelected(true);
            }

        } catch (IOException e) {
            System.err.println("No credentials file found. Skipping loading.");
        }
    }
}
