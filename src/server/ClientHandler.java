package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import model.Attempt;
import model.CourseClass;
import model.Instructor;
import model.MultipleChoiceQuestion;
import model.Question;
import model.ShortAnswerQuestion;
import model.Student;
import model.TrueFalseQuestion;
import model.User;

/**
 * This class stores one connected client on the server side.
 */
public class ClientHandler extends Thread {

    private Socket socket;

    private InputStream inputStream;

    private OutputStream outputStream;

    private Scanner scanner;

    private PrintWriter writer;

    private QuizServer quizServer;

    private User currentUser;

    private String currentClassCode;

    private boolean running;

    public ClientHandler() {

        this(null, null);

    }

    public ClientHandler(Socket socket, QuizServer quizServer) {

        this.socket = socket;

        this.quizServer = quizServer;

        this.currentClassCode = "";

        this.running = true;

    }

    @Override
    public void run() {

        String commandLine;

        try {

            inputStream = socket.getInputStream();

            outputStream = socket.getOutputStream();

            scanner = new Scanner(inputStream);

            writer = new PrintWriter(outputStream, true);

            while (running && scanner.hasNextLine()) {

                commandLine = scanner.nextLine();

                handleCommand(commandLine);

            }

        } catch (IOException e) {

            System.out.println("Client error: " + e.getMessage());

        } finally {

            closeHandler();

        }

    }

    private void handleCommand(String commandLine) {

        String[] parts;
        String command;

        if (commandLine == null) {
            return;
        }

        parts = commandLine.split("\\|", -1);

        if (parts.length == 0) {
            sendMessage("ERROR|Invalid command");
            return;
        }

        command = parts[0];

        if ("LOGIN".equalsIgnoreCase(command)) {
            handleLogin(parts);
            return;
        }

        if ("CREATE_CLASS".equalsIgnoreCase(command)) {
            handleCreateClass(parts);
            return;
        }

        if ("JOIN_CLASS".equalsIgnoreCase(command)) {
            handleJoinClass(parts);
            return;
        }

        if ("CREATE_QUESTION".equalsIgnoreCase(command)) {
            handleCreateQuestion(parts);
            return;
        }

        if ("SUBMIT_ANSWER".equalsIgnoreCase(command)) {
            handleSubmitAnswer(parts);
            return;
        }

        if ("GET_RESULTS".equalsIgnoreCase(command)) {
            handleGetResults(parts);
            return;
        }

        if ("BYE".equalsIgnoreCase(command)) {
            running = false;
            return;
        }

        sendMessage("ERROR|Unknown command");

    }

    private void handleLogin(String[] parts) {

        User user;

        if (parts.length < 3) {
            sendMessage("ERROR|LOGIN command needs email and password");
            return;
        }

        user = quizServer.getAuthController().login(parts[1], parts[2]);

        if (user == null) {
            sendMessage("LOGIN_FAILED");
            return;
        }

        currentUser = user;

        sendMessage("LOGIN_SUCCESS|" + cleanText(user.getRole()) + "|" + cleanText(user.getName()));

    }

    private void handleCreateClass(String[] parts) {

        CourseClass courseClass;

        if (!(currentUser instanceof Instructor)) {
            sendMessage("ERROR|Only instructors can create classes");
            return;
        }

        if (parts.length < 2) {
            sendMessage("ERROR|CREATE_CLASS command needs a class name");
            return;
        }

        courseClass = quizServer.getClassController().createClass(parts[1], (Instructor) currentUser);

        if (courseClass == null) {
            sendMessage("ERROR|Class could not be created");
            return;
        }

        sendMessage("CLASS_CREATED|" + cleanText(courseClass.getClassName()) + "|" + cleanText(courseClass.getClassCode()));

    }

    private void handleJoinClass(String[] parts) {

        boolean joined;
        String classCode;

        if (!(currentUser instanceof Student)) {
            sendMessage("ERROR|Only students can join classes");
            return;
        }

        if (parts.length < 2) {
            sendMessage("ERROR|JOIN_CLASS command needs a class code");
            return;
        }

        classCode = parts[1];
        joined = quizServer.getClassController().joinClass(classCode, (Student) currentUser);

        if (!joined) {
            sendMessage("JOIN_FAILED");
            return;
        }

        if (currentClassCode != null && currentClassCode.length() > 0) {
            quizServer.removeStudentConnection(currentClassCode, this);
        }

        currentClassCode = classCode;

        quizServer.addStudentConnection(classCode, this);

        sendMessage("JOIN_SUCCESS|" + cleanText(classCode));

    }

    private void handleCreateQuestion(String[] parts) {

        String classCode;
        Question question;
        Question savedQuestion;

        if (!(currentUser instanceof Instructor)) {
            sendMessage("ERROR|Only instructors can create questions");
            return;
        }

        if (parts.length < 9) {
            sendMessage("ERROR|CREATE_QUESTION command is missing fields");
            return;
        }

        classCode = parts[1];

        if (quizServer.getClassController().findClassByCode(classCode) == null) {
            sendMessage("ERROR|Class not found");
            return;
        }

        question = buildQuestion(parts);

        savedQuestion = quizServer.getQuestionController().saveQuestion(classCode, question);

        if (savedQuestion == null) {
            sendMessage("ERROR|Question could not be saved");
            return;
        }

        quizServer.broadcastQuestionToClass(classCode, savedQuestion);

    }

    private Question buildQuestion(String[] parts) {

        String type;
        MultipleChoiceQuestion multipleChoiceQuestion;
        TrueFalseQuestion trueFalseQuestion;
        ShortAnswerQuestion shortAnswerQuestion;
        ArrayList<String> choices;

        type = parts[2].toUpperCase(Locale.ENGLISH);

        if ("TRUE_FALSE".equals(type) || "TRUEFALSE".equals(type) || "TF".equals(type)) {
            trueFalseQuestion = new TrueFalseQuestion();
            trueFalseQuestion.setPrompt(parts[3]);
            trueFalseQuestion.setPoints(1);
            trueFalseQuestion.setCorrectAnswer(Boolean.parseBoolean(parts[8]));
            return trueFalseQuestion;
        }

        if ("SHORT_ANSWER".equals(type) || "SHORTANSWER".equals(type)) {
            shortAnswerQuestion = new ShortAnswerQuestion();
            shortAnswerQuestion.setPrompt(parts[3]);
            shortAnswerQuestion.setPoints(1);
            shortAnswerQuestion.setSampleAnswer(parts[8]);
            return shortAnswerQuestion;
        }

        multipleChoiceQuestion = new MultipleChoiceQuestion();
        multipleChoiceQuestion.setPrompt(parts[3]);
        multipleChoiceQuestion.setPoints(1);

        choices = new ArrayList<String>();
        choices.add(parts[4]);
        choices.add(parts[5]);
        choices.add(parts[6]);
        choices.add(parts[7]);

        multipleChoiceQuestion.setChoices(choices);
        multipleChoiceQuestion.setCorrectAnswer(parts[8]);

        return multipleChoiceQuestion;

    }

    private void handleSubmitAnswer(String[] parts) {

        int questionId;
        Question question;
        String answer;
        boolean correct;
        Attempt attempt;
        String correctAnswer;

        if (!(currentUser instanceof Student)) {
            sendMessage("ERROR|Only students can submit answers");
            return;
        }

        if (parts.length < 3) {
            sendMessage("ERROR|SUBMIT_ANSWER command needs a question id and answer");
            return;
        }

        questionId = parseNumber(parts[1]);

        if (questionId <= 0) {
            sendMessage("ERROR|Invalid question id");
            return;
        }

        question = quizServer.getQuestionController().findQuestionById(questionId);

        if (question == null) {
            sendMessage("ERROR|Question not found");
            return;
        }

        answer = parts[2];
        correct = quizServer.getQuestionController().isCorrectAnswer(question, answer);
        correctAnswer = quizServer.getQuestionController().getCorrectAnswer(question);

        attempt = new Attempt();
        attempt.setStudent((Student) currentUser);
        attempt.setQuestion(question);
        attempt.setSubmittedAnswer(answer);
        attempt.setCorrect(correct);

        if (correct) {
            attempt.setPointsEarned(question.getPoints());
        } else {
            attempt.setPointsEarned(0);
        }

        quizServer.getDataService().saveAttempt(attempt);

        sendMessage("ANSWER_RESULT|" + questionId + "|" + correct + "|" + cleanText(correctAnswer));

    }

    private void handleGetResults(String[] parts) {

        String classCode;
        CourseClass courseClass;
        ArrayList<Attempt> classAttempts;
        int correctCount;
        int i;
        Attempt attempt;
        String resultsText;

        if (parts.length < 2) {
            sendMessage("ERROR|GET_RESULTS command needs a class code");
            return;
        }

        classCode = parts[1];
        courseClass = quizServer.getClassController().findClassByCode(classCode);

        if (courseClass == null) {
            sendMessage("ERROR|Class not found");
            return;
        }

        classAttempts = quizServer.getDataService().getAttemptsForClass(classCode);
        correctCount = 0;

        for (i = 0; i < classAttempts.size(); i++) {
            attempt = classAttempts.get(i);

            if (attempt.isCorrect()) {
                correctCount++;
            }
        }

        resultsText = "Class " + courseClass.getClassName()
            + " has " + classAttempts.size()
            + " attempts. Correct: " + correctCount
            + ". Incorrect: " + (classAttempts.size() - correctCount) + ".";

        sendMessage("RESULTS_DATA|" + cleanText(resultsText));

    }

    public void sendMessage(String message) {

        if (writer != null) {
            writer.println(message);
        }

    }

    public void closeHandler() {

        running = false;

        if (currentClassCode != null && currentClassCode.length() > 0) {
            quizServer.removeStudentConnection(currentClassCode, this);
        }

        quizServer.removeClientHandler(this);

        try {

            if (writer != null) {
                writer.close();
            }

            if (scanner != null) {
                scanner.close();
            }

            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {

            System.out.println("Close error: " + e.getMessage());

        }

    }

    private int parseNumber(String text) {

        try {

            return Integer.parseInt(text);

        } catch (NumberFormatException e) {

            return -1;

        }

    }

    private String cleanText(String text) {

        String safeText;

        if (text == null) {
            return "";
        }

        safeText = text;
        safeText = safeText.replace("|", "/");
        safeText = safeText.replace("\n", " ");
        safeText = safeText.replace("\r", " ");

        return safeText;

    }

    public Socket getSocket() { return socket; }

    public void setSocket(Socket socket) { this.socket = socket; }

    public InputStream getInputStream() { return inputStream; }

    public void setInputStream(InputStream inputStream) { this.inputStream = inputStream; }

    public OutputStream getOutputStream() { return outputStream; }

    public void setOutputStream(OutputStream outputStream) { this.outputStream = outputStream; }

    public Scanner getScanner() { return scanner; }

    public void setScanner(Scanner scanner) { this.scanner = scanner; }

    public PrintWriter getWriter() { return writer; }

    public void setWriter(PrintWriter writer) { this.writer = writer; }

    public QuizServer getQuizServer() { return quizServer; }

    public void setQuizServer(QuizServer quizServer) { this.quizServer = quizServer; }

    public User getCurrentUser() { return currentUser; }

    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    public String getCurrentClassCode() { return currentClassCode; }

    public void setCurrentClassCode(String currentClassCode) { this.currentClassCode = currentClassCode; }

    public boolean isRunning() { return running; }

    public void setRunning(boolean running) { this.running = running; }

}
