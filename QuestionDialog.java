import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JTextArea;


/**
 * This class is a small dialog for showing one question.
 */
public class QuestionDialog extends JDialog implements ActionListener {

    private Question question;

    private ClientConnection clientConnection;

    private ResourceBundle messages;

    private String answerText;

    private JTextField answerField;

    private JComboBox<String> answerBox;

    private JButton submitButton;

    private JTextArea promptArea;

    private JLabel statusLabel;

    public QuestionDialog() {

        this(null, null, null, null);

    }

    public QuestionDialog(JFrame owner, Question question, ClientConnection clientConnection, ResourceBundle messages) {

        super(owner, true);

        this.question = question;

        this.clientConnection = clientConnection;

        this.messages = loadMessages(Locale.ENGLISH);

        if (messages != null) {
            this.messages = messages;
        }

        this.answerText = "";

        this.answerField = new JTextField(20);

        this.answerBox = new JComboBox<String>();

        this.submitButton = new JButton("Submit");

        this.promptArea = new JTextArea();

        this.statusLabel = new JLabel(" ");

        buildWindow();

        registerListeners();

    }

    private void buildWindow() {

        JPanel centerPanel;
        JPanel buttonPanel;

        setTitle(getText("question.title", "Question"));

        setSize(450, 250);

        setModal(true);

        setLayout(new BorderLayout());

        promptArea.setEditable(false);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);

        if (question != null) {
            promptArea.setText(question.getPrompt());
        }

        centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        if (usesChoiceBox()) {
            loadChoices();
            centerPanel.add(new JLabel(getText("label.select.answer", "Select Answer:")));
            centerPanel.add(answerBox);
        } else {
            centerPanel.add(new JLabel(getText("label.your.answer", "Your Answer:")));
            centerPanel.add(answerField);
        }

        buttonPanel = new JPanel(new FlowLayout());
        submitButton.setText(getText("button.submit", "Submit"));
        buttonPanel.add(submitButton);

        add(promptArea, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        southPanel.add(buttonPanel);
        southPanel.add(statusLabel);

        add(southPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(getOwner());

    }

    private void registerListeners() {

        submitButton.addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == submitButton) {
            submitAnswer();
        }

    }

    private void submitAnswer() {

        if (question == null || clientConnection == null) {
            statusLabel.setText(getText("status.question.not.ready", "Question is not ready."));
            return;
        }

        if (usesChoiceBox()) {
            if (answerBox.getSelectedItem() == null) {
                statusLabel.setText(getText("status.choose.answer", "Choose an answer."));
                return;
            }

            answerText = String.valueOf(answerBox.getSelectedItem());
        } else {
            answerText = answerField.getText().trim();

            if (answerText.length() == 0) {
                statusLabel.setText(getText("status.enter.answer", "Enter an answer."));
                return;
            }
        }

        clientConnection.sendSubmitAnswer(question.getQuestionId(), answerText);

        dispose();

    }

    private boolean usesChoiceBox() {

        if (question instanceof MultipleChoiceQuestion) {
            return true;
        }

        if (question instanceof TrueFalseQuestion) {
            return true;
        }

        return false;

    }

    private void loadChoices() {

        int i;
        ArrayList<String> choices;

        answerBox.removeAllItems();

        if (question instanceof MultipleChoiceQuestion) {
            choices = ((MultipleChoiceQuestion) question).getChoices();

            for (i = 0; i < choices.size(); i++) {
                answerBox.addItem(choices.get(i));
            }
        }

        if (question instanceof TrueFalseQuestion) {
            answerBox.addItem("True");
            answerBox.addItem("False");
        }

    }

    public Question getQuestion() { return question; }

    public void setQuestion(Question question) { this.question = question; }

    public ClientConnection getClientConnection() { return clientConnection; }

    public void setClientConnection(ClientConnection clientConnection) { this.clientConnection = clientConnection; }

    public ResourceBundle getMessages() { return messages; }

    public void setMessages(ResourceBundle messages) { this.messages = messages; }

    public String getAnswerText() { return answerText; }

    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public JTextField getAnswerField() { return answerField; }

    public void setAnswerField(JTextField answerField) { this.answerField = answerField; }

    public JComboBox<String> getAnswerBox() { return answerBox; }

    public void setAnswerBox(JComboBox<String> answerBox) { this.answerBox = answerBox; }

    public JButton getSubmitButton() { return submitButton; }

    public void setSubmitButton(JButton submitButton) { this.submitButton = submitButton; }

    public JTextArea getPromptArea() { return promptArea; }

    public void setPromptArea(JTextArea promptArea) { this.promptArea = promptArea; }

    public JLabel getStatusLabel() { return statusLabel; }

    public void setStatusLabel(JLabel statusLabel) { this.statusLabel = statusLabel; }

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

            return ResourceBundle.getBundle("messages", locale);

        } catch (Exception e) {

            return null;

        }

    }

}
