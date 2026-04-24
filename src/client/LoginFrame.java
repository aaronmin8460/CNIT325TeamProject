package client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import model.Instructor;
import model.Student;

/**
 * This class is the login window for the client.
 */
public class LoginFrame extends JFrame implements ActionListener, ServerMessageHandler {

    private ClientConnection clientConnection;

    private ResourceBundle messages;

    private JTextField emailField;

    private JPasswordField passwordField;

    private JComboBox<String> languageBox;

    private JButton loginButton;

    private JLabel emailLabel;

    private JLabel passwordLabel;

    private JLabel languageLabel;

    private JLabel statusLabel;

    public LoginFrame() {

        this(new ClientConnection(), null);

    }

    public LoginFrame(ClientConnection clientConnection, ResourceBundle messages) {

        this.clientConnection = clientConnection;

        this.messages = loadMessages(Locale.ENGLISH);

        if (messages != null) {
            this.messages = messages;
        }

        this.emailField = new JTextField(20);

        this.passwordField = new JPasswordField(20);

        this.languageBox = new JComboBox<String>();
        this.languageBox.addItem("English");
        this.languageBox.addItem("Espanol");

        this.loginButton = new JButton("Login");

        this.emailLabel = new JLabel("Email:");

        this.passwordLabel = new JLabel("Password:");

        this.languageLabel = new JLabel("Language:");

        this.statusLabel = new JLabel(" ");

        buildWindow();

        applyTexts();

        registerListeners();

    }

    private void buildWindow() {

        JPanel formPanel;
        JPanel buttonPanel;
        JPanel bottomPanel;

        setTitle(getText("login.title", "Login"));

        setSize(400, 200);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(languageLabel);
        formPanel.add(languageBox);

        buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);

        bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        bottomPanel.add(buttonPanel);
        bottomPanel.add(statusLabel);

        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);

    }

    private void registerListeners() {

        loginButton.addActionListener(this);
        languageBox.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                clientConnection.disconnect();

            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == loginButton) {
            attemptLogin();
            return;
        }

        if (e.getSource() == languageBox) {
            updateLanguage();
        }

    }

    private void attemptLogin() {

        String email;
        String password;
        boolean connected;

        email = emailField.getText().trim();
        password = new String(passwordField.getPassword());

        if (email.length() == 0 || password.length() == 0) {
            statusLabel.setText(getText("status.enter.email.password", "Enter email and password."));
            return;
        }

        clientConnection.setMessageHandler(this);

        if (!clientConnection.isConnected()) {
            connected = clientConnection.connect();

            if (!connected) {
                statusLabel.setText(getText("status.connect.failed", "Could not connect to server."));
                return;
            }

            clientConnection.startListening();
        }

        statusLabel.setText(getText("status.logging.in", "Logging in..."));

        clientConnection.sendLogin(email, password);

    }

    private void updateLanguage() {

        Locale locale;

        if (languageBox.getSelectedIndex() == 1) {
            locale = new Locale("es");
        } else {
            locale = Locale.ENGLISH;
        }

        messages = loadMessages(locale);
        applyTexts();

    }

    private void applyTexts() {

        setTitle(getText("login.title", "Login"));
        emailLabel.setText(getText("label.email", "Email:"));
        passwordLabel.setText(getText("label.password", "Password:"));
        languageLabel.setText(getText("label.language", "Language:"));
        loginButton.setText(getText("button.login", "Login"));

    }

    @Override
    public void handleServerMessage(String message) {

        String[] parts;
        String role;
        String name;

        if (message == null) {
            return;
        }

        parts = message.split("\\|", -1);

        if (parts.length == 0) {
            return;
        }

        if ("LOGIN_SUCCESS".equals(parts[0])) {
            if (parts.length >= 3) {
                role = parts[1];
                name = parts[2];

                if ("instructor".equalsIgnoreCase(role)) {
                    openInstructorFrame(name);
                    return;
                }

                if ("student".equalsIgnoreCase(role)) {
                    openStudentFrame(name);
                    return;
                }
            }
        }

        if ("LOGIN_FAILED".equals(parts[0])) {
            statusLabel.setText(getText("status.login.failed", "Login failed."));
            return;
        }

        if ("ERROR".equals(parts[0])) {
            if (parts.length >= 2) {
                statusLabel.setText(parts[1]);
            } else {
                statusLabel.setText("Server error.");
            }
        }

    }

    private void openInstructorFrame(String name) {

        Instructor instructor;
        InstructorFrame instructorFrame;

        instructor = new Instructor();
        instructor.setEmail(emailField.getText().trim());
        instructor.setName(name);
        instructor.setRole("instructor");

        instructorFrame = new InstructorFrame(instructor, clientConnection, messages);
        clientConnection.setMessageHandler(instructorFrame);
        instructorFrame.setVisible(true);

        dispose();

    }

    private void openStudentFrame(String name) {

        Student student;
        StudentFrame studentFrame;

        student = new Student();
        student.setEmail(emailField.getText().trim());
        student.setName(name);
        student.setRole("student");

        studentFrame = new StudentFrame(student, clientConnection, messages);
        clientConnection.setMessageHandler(studentFrame);
        studentFrame.setVisible(true);

        dispose();

    }

    private String getText(String key, String defaultText) {

        if (messages == null) {
            return defaultText;
        }

        if (messages.containsKey(key)) {
            return messages.getString(key);
        }

        return defaultText;

    }

    private ResourceBundle loadMessages(Locale locale) {

        try {

            return ResourceBundle.getBundle("i18n.messages", locale);

        } catch (Exception e) {

            return null;

        }

    }

    public ClientConnection getClientConnection() { return clientConnection; }

    public void setClientConnection(ClientConnection clientConnection) { this.clientConnection = clientConnection; }

    public ResourceBundle getMessages() { return messages; }

    public void setMessages(ResourceBundle messages) { this.messages = messages; }

    public JTextField getEmailField() { return emailField; }

    public void setEmailField(JTextField emailField) { this.emailField = emailField; }

    public JPasswordField getPasswordField() { return passwordField; }

    public void setPasswordField(JPasswordField passwordField) { this.passwordField = passwordField; }

    public JComboBox<String> getLanguageBox() { return languageBox; }

    public void setLanguageBox(JComboBox<String> languageBox) { this.languageBox = languageBox; }

    public JButton getLoginButton() { return loginButton; }

    public void setLoginButton(JButton loginButton) { this.loginButton = loginButton; }

    public JLabel getEmailLabel() { return emailLabel; }

    public void setEmailLabel(JLabel emailLabel) { this.emailLabel = emailLabel; }

    public JLabel getPasswordLabel() { return passwordLabel; }

    public void setPasswordLabel(JLabel passwordLabel) { this.passwordLabel = passwordLabel; }

    public JLabel getLanguageLabel() { return languageLabel; }

    public void setLanguageLabel(JLabel languageLabel) { this.languageLabel = languageLabel; }

    public JLabel getStatusLabel() { return statusLabel; }

    public void setStatusLabel(JLabel statusLabel) { this.statusLabel = statusLabel; }

}
