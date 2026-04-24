package client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;

import model.MultipleChoiceQuestion;
import model.Question;
import model.ShortAnswerQuestion;
import model.Student;
import model.TrueFalseQuestion;

/**
 * This class is the main window for a student user.
 */
public class StudentFrame extends JFrame implements ActionListener, ServerMessageHandler {

    private Student student;

    private ClientConnection clientConnection;

    private ResourceBundle messages;

    private JTextField classCodeField;

    private JButton joinClassButton;

    private JTextArea statusTextArea;

    private QuestionDialog questionDialog;

    public StudentFrame() {

        this(null, new ClientConnection(), null);

    }

    public StudentFrame(Student student, ClientConnection clientConnection, ResourceBundle messages) {

        this.student = student;

        this.clientConnection = clientConnection;

        this.messages = loadMessages(Locale.ENGLISH);

        if (messages != null) {
            this.messages = messages;
        }

        this.classCodeField = new JTextField(15);

        this.joinClassButton = new JButton("Join Class");

        this.statusTextArea = new JTextArea();

        this.questionDialog = null;

        buildWindow();

        registerListeners();

    }

    private void buildWindow() {

        JPanel topPanel;
        String titleName;

        titleName = getText("student.title", "Student");

        if (student != null) {
            if (student.getName() != null && student.getName().length() > 0) {
                titleName = getText("student.title", "Student") + " - " + student.getName();
            }
        }

        setTitle(titleName);

        setSize(600, 400);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel(getText("label.class.code", "Class Code:")));
        topPanel.add(classCodeField);
        topPanel.add(joinClassButton);

        statusTextArea.setEditable(false);
        statusTextArea.setLineWrap(true);
        statusTextArea.setWrapStyleWord(true);

        add(topPanel, BorderLayout.NORTH);
        add(statusTextArea, BorderLayout.CENTER);

        setLocationRelativeTo(null);

        joinClassButton.setText(getText("button.join.class", "Join Class"));

    }

    private void registerListeners() {

        joinClassButton.addActionListener(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                clientConnection.disconnect();

            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == joinClassButton) {
            joinClass();
        }

    }

    private void joinClass() {

        String classCode;

        classCode = classCodeField.getText().trim();

        if (classCode.length() == 0) {
            appendStatus(getText("status.enter.class.code", "Enter a class code."));
            return;
        }

        clientConnection.sendJoinClass(classCode);
        appendStatus(getText("status.joining.class", "Joining class..."));

    }

    @Override
    public void handleServerMessage(String message) {

        String[] parts;
        Question question;

        if (message == null) {
            return;
        }

        parts = message.split("\\|", -1);

        if (parts.length == 0) {
            return;
        }

        if ("JOIN_SUCCESS".equals(parts[0])) {
            if (parts.length >= 2) {
                if (student != null) {
                    student.setClassCode(parts[1]);
                }
                appendStatus(getText("status.joined.class", "Joined class:") + " " + parts[1]);
            }
            return;
        }

        if ("JOIN_FAILED".equals(parts[0])) {
            appendStatus(getText("status.join.failed", "Join failed."));
            return;
        }

        if ("QUESTION_PUSH".equals(parts[0])) {
            question = buildQuestion(parts);

            if (question != null) {
                appendStatus(getText("status.question.received", "Question received."));
                questionDialog = new QuestionDialog(this, question, clientConnection, messages);
                questionDialog.setVisible(true);
            } else {
                appendStatus(getText("status.question.open.failed", "Could not open question."));
            }
            return;
        }

        if ("ANSWER_RESULT".equals(parts[0])) {
            if (parts.length >= 4) {
                appendStatus(
                    getText("status.question.result", "Question")
                    + " " + parts[1]
                    + " " + getText("status.question.result.middle", "result:")
                    + " " + parts[2]
                    + ". "
                    + getText("status.correct.answer", "Correct answer:")
                    + " " + parts[3]
                );
            }
            return;
        }

        if ("ERROR".equals(parts[0])) {
            if (parts.length >= 2) {
                appendStatus(getText("status.error.prefix", "Error:") + " " + parts[1]);
            } else {
                appendStatus(getText("status.server.error", "Error from server."));
            }
            return;
        }

        if ("RESULTS_DATA".equals(parts[0])) {
            if (parts.length >= 2) {
                appendStatus(parts[1]);
            }
        }

    }

    private Question buildQuestion(String[] parts) {

        int questionId;
        String classCode;
        String type;
        String prompt;
        MultipleChoiceQuestion multipleChoiceQuestion;
        TrueFalseQuestion trueFalseQuestion;
        ShortAnswerQuestion shortAnswerQuestion;
        ArrayList<String> choices;

        if (parts.length < 9) {
            return null;
        }

        questionId = parseNumber(parts[1]);
        classCode = parts[2];
        type = parts[3];
        prompt = parts[4];

        if ("MULTIPLE_CHOICE".equals(type)) {
            multipleChoiceQuestion = new MultipleChoiceQuestion();
            multipleChoiceQuestion.setQuestionId(questionId);
            multipleChoiceQuestion.setClassCode(classCode);
            multipleChoiceQuestion.setPrompt(prompt);
            choices = new ArrayList<String>();

            if (parts[5].length() > 0) {
                choices.add(parts[5]);
            }

            if (parts[6].length() > 0) {
                choices.add(parts[6]);
            }

            if (parts[7].length() > 0) {
                choices.add(parts[7]);
            }

            if (parts[8].length() > 0) {
                choices.add(parts[8]);
            }

            multipleChoiceQuestion.setChoices(choices);
            return multipleChoiceQuestion;
        }

        if ("TRUE_FALSE".equals(type)) {
            trueFalseQuestion = new TrueFalseQuestion();
            trueFalseQuestion.setQuestionId(questionId);
            trueFalseQuestion.setClassCode(classCode);
            trueFalseQuestion.setPrompt(prompt);
            return trueFalseQuestion;
        }

        shortAnswerQuestion = new ShortAnswerQuestion();
        shortAnswerQuestion.setQuestionId(questionId);
        shortAnswerQuestion.setClassCode(classCode);
        shortAnswerQuestion.setPrompt(prompt);

        return shortAnswerQuestion;

    }

    private int parseNumber(String text) {

        try {

            return Integer.parseInt(text);

        } catch (NumberFormatException e) {

            return 0;

        }

    }

    private void appendStatus(String text) {

        statusTextArea.append(text + "\n");

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

    public Student getStudent() { return student; }

    public void setStudent(Student student) { this.student = student; }

    public ClientConnection getClientConnection() { return clientConnection; }

    public void setClientConnection(ClientConnection clientConnection) { this.clientConnection = clientConnection; }

    public ResourceBundle getMessages() { return messages; }

    public void setMessages(ResourceBundle messages) { this.messages = messages; }

    public JTextField getClassCodeField() { return classCodeField; }

    public void setClassCodeField(JTextField classCodeField) { this.classCodeField = classCodeField; }

    public JButton getJoinClassButton() { return joinClassButton; }

    public void setJoinClassButton(JButton joinClassButton) { this.joinClassButton = joinClassButton; }

    public JTextArea getStatusTextArea() { return statusTextArea; }

    public void setStatusTextArea(JTextArea statusTextArea) { this.statusTextArea = statusTextArea; }

    public QuestionDialog getQuestionDialog() { return questionDialog; }

    public void setQuestionDialog(QuestionDialog questionDialog) { this.questionDialog = questionDialog; }

}
