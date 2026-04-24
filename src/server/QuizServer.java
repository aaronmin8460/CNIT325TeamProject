package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import model.Question;
import service.DataService;
import service.MockDataService;

/**
 * This class stores the main server objects for the quiz system.
 */
public class QuizServer {

    private int port;

    private ServerSocket serverSocket;

    private ArrayList<ClientHandler> clientHandlers;

    private HashMap<String, ArrayList<ClientHandler>> connectedStudentsByClassCode;

    private AuthController authController;

    private ClassController classController;

    private QuestionController questionController;

    private DataService dataService;

    private boolean running;

    public QuizServer() {

        this(8189, new MockDataService());

    }

    public QuizServer(int port, DataService dataService) {

        this.port = port;

        this.dataService = dataService;

        this.clientHandlers = new ArrayList<ClientHandler>();

        this.connectedStudentsByClassCode = new HashMap<String, ArrayList<ClientHandler>>();

        this.authController = new AuthController(dataService);

        this.classController = new ClassController(dataService);

        this.questionController = new QuestionController(dataService);

        this.running = false;

    }

    public void startServer() {

        Socket socket;
        ClientHandler clientHandler;

        try {

            serverSocket = new ServerSocket(port);

            running = true;

            System.out.println("Quiz server started on port " + port);

            while (running) {

                socket = serverSocket.accept();

                clientHandler = new ClientHandler(socket, this);

                addClientHandler(clientHandler);

                clientHandler.start();

            }

        } catch (IOException e) {

            if (running) {
                System.out.println("Server error: " + e.getMessage());
            }

        }

    }

    public void stopServer() {

        running = false;

        try {

            if (serverSocket != null) {
                serverSocket.close();
            }

        } catch (IOException e) {

            System.out.println("Server close error: " + e.getMessage());

        }

    }

    public synchronized void addClientHandler(ClientHandler clientHandler) {

        clientHandlers.add(clientHandler);

    }

    public synchronized void removeClientHandler(ClientHandler clientHandler) {

        clientHandlers.remove(clientHandler);

    }

    public synchronized void addStudentConnection(String classCode, ClientHandler clientHandler) {

        ArrayList<ClientHandler> classHandlers;

        if (classCode == null || classCode.length() == 0 || clientHandler == null) {
            return;
        }

        classHandlers = connectedStudentsByClassCode.get(classCode);

        if (classHandlers == null) {
            classHandlers = new ArrayList<ClientHandler>();
            connectedStudentsByClassCode.put(classCode, classHandlers);
        }

        if (!classHandlers.contains(clientHandler)) {
            classHandlers.add(clientHandler);
        }

    }

    public synchronized void removeStudentConnection(String classCode, ClientHandler clientHandler) {

        ArrayList<ClientHandler> classHandlers;

        if (classCode == null || clientHandler == null) {
            return;
        }

        classHandlers = connectedStudentsByClassCode.get(classCode);

        if (classHandlers == null) {
            return;
        }

        classHandlers.remove(clientHandler);

        if (classHandlers.size() == 0) {
            connectedStudentsByClassCode.remove(classCode);
        }

    }

    public synchronized void broadcastQuestionToClass(String classCode, Question question) {

        ArrayList<ClientHandler> classHandlers;
        ArrayList<ClientHandler> handlersToSend;
        int i;
        ClientHandler clientHandler;
        String message;

        if (classCode == null || question == null) {
            return;
        }

        classHandlers = connectedStudentsByClassCode.get(classCode);

        if (classHandlers == null) {
            return;
        }

        handlersToSend = new ArrayList<ClientHandler>();

        for (i = 0; i < classHandlers.size(); i++) {
            handlersToSend.add(classHandlers.get(i));
        }

        message = buildQuestionPushMessage(question);

        for (i = 0; i < handlersToSend.size(); i++) {
            clientHandler = handlersToSend.get(i);
            clientHandler.sendMessage(message);
        }

    }

    private String buildQuestionPushMessage(Question question) {

        StringBuilder builder;

        builder = new StringBuilder();

        builder.append("QUESTION_PUSH|");
        builder.append(question.getQuestionId());
        builder.append("|");
        builder.append(cleanText(question.getClassCode()));
        builder.append("|");
        builder.append(questionController.getQuestionType(question));
        builder.append("|");
        builder.append(cleanText(question.getPrompt()));
        builder.append("|");
        builder.append(questionController.getChoiceText(question, 0));
        builder.append("|");
        builder.append(questionController.getChoiceText(question, 1));
        builder.append("|");
        builder.append(questionController.getChoiceText(question, 2));
        builder.append("|");
        builder.append(questionController.getChoiceText(question, 3));

        return builder.toString();

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

    public int getPort() { return port; }

    public void setPort(int port) { this.port = port; }

    public ServerSocket getServerSocket() { return serverSocket; }

    public void setServerSocket(ServerSocket serverSocket) { this.serverSocket = serverSocket; }

    public ArrayList<ClientHandler> getClientHandlers() { return clientHandlers; }

    public void setClientHandlers(ArrayList<ClientHandler> clientHandlers) { this.clientHandlers = clientHandlers; }

    public HashMap<String, ArrayList<ClientHandler>> getConnectedStudentsByClassCode() { return connectedStudentsByClassCode; }

    public void setConnectedStudentsByClassCode(HashMap<String, ArrayList<ClientHandler>> connectedStudentsByClassCode) { this.connectedStudentsByClassCode = connectedStudentsByClassCode; }

    public AuthController getAuthController() { return authController; }

    public void setAuthController(AuthController authController) { this.authController = authController; }

    public ClassController getClassController() { return classController; }

    public void setClassController(ClassController classController) { this.classController = classController; }

    public QuestionController getQuestionController() { return questionController; }

    public void setQuestionController(QuestionController questionController) { this.questionController = questionController; }

    public DataService getDataService() { return dataService; }

    public void setDataService(DataService dataService) {

        this.dataService = dataService;

        authController.setDataService(dataService);

        classController.setDataService(dataService);

        questionController.setDataService(dataService);

    }

    public boolean isRunning() { return running; }

    public void setRunning(boolean running) { this.running = running; }

}
