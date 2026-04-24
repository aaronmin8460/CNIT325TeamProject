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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;

import model.Instructor;

/**
 * This class is the main window for an instructor user.
 */
public class InstructorFrame extends JFrame implements ActionListener, ServerMessageHandler {

    private Instructor instructor;

    private ClientConnection clientConnection;

    private ResourceBundle messages;

    private JTextField classNameField;

    private JButton createClassButton;

    private JLabel classCodeLabel;

    private JComboBox<String> questionTypeBox;

    private JTextField promptField;

    private JTextField choiceAField;

    private JTextField choiceBField;

    private JTextField choiceCField;

    private JTextField choiceDField;

    private JTextField correctAnswerField;

    private JButton createQuestionButton;

    private JButton resultsButton;

    private JTextArea resultsTextArea;

    private String currentClassCode;

    public InstructorFrame() {

        this(null, new ClientConnection(), null);

    }

    public InstructorFrame(Instructor instructor, ClientConnection clientConnection, ResourceBundle messages) {

        this.instructor = instructor;

        this.clientConnection = clientConnection;

        this.messages = loadMessages(Locale.ENGLISH);

        if (messages != null) {
            this.messages = messages;
        }

        this.classNameField = new JTextField(15);

        this.createClassButton = new JButton("Create Class");

        this.classCodeLabel = new JLabel("No class yet");

        this.questionTypeBox = new JComboBox<String>();
        this.questionTypeBox.addItem("MULTIPLE_CHOICE");
        this.questionTypeBox.addItem("TRUE_FALSE");
        this.questionTypeBox.addItem("SHORT_ANSWER");

        this.promptField = new JTextField(20);

        this.choiceAField = new JTextField(20);

        this.choiceBField = new JTextField(20);

        this.choiceCField = new JTextField(20);

        this.choiceDField = new JTextField(20);

        this.correctAnswerField = new JTextField(20);

        this.createQuestionButton = new JButton("Create Question");

        this.resultsButton = new JButton("Results");

        this.resultsTextArea = new JTextArea();

        this.currentClassCode = "";

        buildWindow();

        registerListeners();

    }

    private void buildWindow() {

        JPanel classPanel;
        JPanel questionPanel;
        JPanel resultsPanel;
        JPanel buttonPanel;
        String titleName;

        titleName = getText("instructor.title", "Instructor");

        if (instructor != null) {
            if (instructor.getName() != null && instructor.getName().length() > 0) {
                titleName = getText("instructor.title", "Instructor") + " - " + instructor.getName();
            }
        }

        setTitle(titleName);

        setSize(700, 500);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        classPanel = new JPanel(new FlowLayout());
        classPanel.add(new JLabel(getText("label.class.name", "Class Name:")));
        classPanel.add(classNameField);
        classPanel.add(createClassButton);
        classPanel.add(new JLabel(getText("label.class.code", "Class Code:")));
        classPanel.add(classCodeLabel);

        questionPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        questionPanel.add(new JLabel(getText("label.question.type", "Question Type:")));
        questionPanel.add(questionTypeBox);
        questionPanel.add(new JLabel(getText("label.prompt", "Prompt:")));
        questionPanel.add(promptField);
        questionPanel.add(new JLabel(getText("label.choice.a", "Choice A:")));
        questionPanel.add(choiceAField);
        questionPanel.add(new JLabel(getText("label.choice.b", "Choice B:")));
        questionPanel.add(choiceBField);
        questionPanel.add(new JLabel(getText("label.choice.c", "Choice C:")));
        questionPanel.add(choiceCField);
        questionPanel.add(new JLabel(getText("label.choice.d", "Choice D:")));
        questionPanel.add(choiceDField);
        questionPanel.add(new JLabel(getText("label.correct.answer", "Correct Answer:")));
        questionPanel.add(correctAnswerField);

        resultsTextArea.setEditable(false);
        resultsTextArea.setLineWrap(true);
        resultsTextArea.setWrapStyleWord(true);

        buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createQuestionButton);
        buttonPanel.add(resultsButton);

        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.add(buttonPanel, BorderLayout.NORTH);
        resultsPanel.add(resultsTextArea, BorderLayout.CENTER);

        add(classPanel, BorderLayout.NORTH);
        add(questionPanel, BorderLayout.CENTER);
        add(resultsPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);

        createClassButton.setText(getText("button.create.class", "Create Class"));
        createQuestionButton.setText(getText("button.create.question", "Create Question"));
        resultsButton.setText(getText("button.results", "Results"));

    }

    private void registerListeners() {

        createClassButton.addActionListener(this);
        createQuestionButton.addActionListener(this);
        resultsButton.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                clientConnection.disconnect();

            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == createClassButton) {
            createClass();
            return;
        }

        if (e.getSource() == createQuestionButton) {
            createQuestion();
            return;
        }

        if (e.getSource() == resultsButton) {
            loadResults();
        }

    }

    private void createClass() {

        String className;

        className = classNameField.getText().trim();

        if (className.length() == 0) {
            appendResult(getText("status.enter.class.name", "Enter a class name."));
            return;
        }

        clientConnection.sendCreateClass(className);
        appendResult(getText("status.creating.class", "Creating class..."));

    }

    private void createQuestion() {

        String questionType;
        String prompt;
        String choiceA;
        String choiceB;
        String choiceC;
        String choiceD;
        String correctAnswer;

        if (currentClassCode.length() == 0) {
            appendResult(getText("status.create.class.first", "Create a class first."));
            return;
        }

        questionType = String.valueOf(questionTypeBox.getSelectedItem());
        prompt = promptField.getText().trim();

        if (prompt.length() == 0) {
            appendResult(getText("status.enter.prompt", "Enter a question prompt."));
            return;
        }

        choiceA = choiceAField.getText().trim();
        choiceB = choiceBField.getText().trim();
        choiceC = choiceCField.getText().trim();
        choiceD = choiceDField.getText().trim();
        correctAnswer = correctAnswerField.getText().trim();

        if ("TRUE_FALSE".equals(questionType)) {
            choiceA = "True";
            choiceB = "False";
            choiceC = "";
            choiceD = "";
        }

        if ("SHORT_ANSWER".equals(questionType)) {
            choiceA = "";
            choiceB = "";
            choiceC = "";
            choiceD = "";
        }

        clientConnection.sendCreateQuestion(
            currentClassCode,
            questionType,
            prompt,
            choiceA,
            choiceB,
            choiceC,
            choiceD,
            correctAnswer
        );

        appendResult(getText("status.question.sent", "Question sent to server."));

    }

    private void loadResults() {

        if (currentClassCode.length() == 0) {
            appendResult(getText("status.create.class.first", "Create a class first."));
            return;
        }

        clientConnection.sendGetResults(currentClassCode);
        appendResult(getText("status.loading.results", "Loading results..."));

    }

    @Override
    public void handleServerMessage(String message) {

        String[] parts;

        if (message == null) {
            return;
        }

        parts = message.split("\\|", -1);

        if (parts.length == 0) {
            return;
        }

        if ("CLASS_CREATED".equals(parts[0])) {
            if (parts.length >= 3) {
                currentClassCode = parts[2];
                classCodeLabel.setText(parts[2]);
                appendResult(getText("status.class.created", "Class created:")
                    + " " + parts[1] + " (" + parts[2] + ")");
            }
            return;
        }

        if ("RESULTS_DATA".equals(parts[0])) {
            if (parts.length >= 2) {
                appendResult(formatResultsText(parts[1]));
            }
            return;
        }

        if ("ERROR".equals(parts[0])) {
            if (parts.length >= 2) {
                appendResult(getText("status.error.prefix", "Error:") + " " + parts[1]);
            } else {
                appendResult(getText("status.server.error", "Error from server."));
            }
        }

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

    private void appendResult(String text) {

        resultsTextArea.append(text + "\n");

    }

    private String formatResultsText(String text) {

        String formattedText;

        if (text == null) {
            return "";
        }

        formattedText = text;
        formattedText = formattedText.replace(". Question: ", ".\nQuestion: ");
        formattedText = formattedText.replace(" Student: ", "\nStudent: ");
        formattedText = formattedText.replace(" Answer: ", "\nAnswer: ");
        formattedText = formattedText.replace(" Correct: true.", "\nCorrect: true.\n");
        formattedText = formattedText.replace(" Correct: false.", "\nCorrect: false.\n");

        return formattedText;

    }

    public Instructor getInstructor() { return instructor; }

    public void setInstructor(Instructor instructor) { this.instructor = instructor; }

    public ClientConnection getClientConnection() { return clientConnection; }

    public void setClientConnection(ClientConnection clientConnection) { this.clientConnection = clientConnection; }

    public ResourceBundle getMessages() { return messages; }

    public void setMessages(ResourceBundle messages) { this.messages = messages; }

    public JTextField getClassNameField() { return classNameField; }

    public void setClassNameField(JTextField classNameField) { this.classNameField = classNameField; }

    public JButton getCreateClassButton() { return createClassButton; }

    public void setCreateClassButton(JButton createClassButton) { this.createClassButton = createClassButton; }

    public JLabel getClassCodeLabel() { return classCodeLabel; }

    public void setClassCodeLabel(JLabel classCodeLabel) { this.classCodeLabel = classCodeLabel; }

    public JComboBox<String> getQuestionTypeBox() { return questionTypeBox; }

    public void setQuestionTypeBox(JComboBox<String> questionTypeBox) { this.questionTypeBox = questionTypeBox; }

    public JTextField getPromptField() { return promptField; }

    public void setPromptField(JTextField promptField) { this.promptField = promptField; }

    public JTextField getChoiceAField() { return choiceAField; }

    public void setChoiceAField(JTextField choiceAField) { this.choiceAField = choiceAField; }

    public JTextField getChoiceBField() { return choiceBField; }

    public void setChoiceBField(JTextField choiceBField) { this.choiceBField = choiceBField; }

    public JTextField getChoiceCField() { return choiceCField; }

    public void setChoiceCField(JTextField choiceCField) { this.choiceCField = choiceCField; }

    public JTextField getChoiceDField() { return choiceDField; }

    public void setChoiceDField(JTextField choiceDField) { this.choiceDField = choiceDField; }

    public JTextField getCorrectAnswerField() { return correctAnswerField; }

    public void setCorrectAnswerField(JTextField correctAnswerField) { this.correctAnswerField = correctAnswerField; }

    public JButton getCreateQuestionButton() { return createQuestionButton; }

    public void setCreateQuestionButton(JButton createQuestionButton) { this.createQuestionButton = createQuestionButton; }

    public JButton getResultsButton() { return resultsButton; }

    public void setResultsButton(JButton resultsButton) { this.resultsButton = resultsButton; }

    public JTextArea getResultsTextArea() { return resultsTextArea; }

    public void setResultsTextArea(JTextArea resultsTextArea) { this.resultsTextArea = resultsTextArea; }

    public String getCurrentClassCode() { return currentClassCode; }

    public void setCurrentClassCode(String currentClassCode) { this.currentClassCode = currentClassCode; }

}
