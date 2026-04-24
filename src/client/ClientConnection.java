package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.SwingUtilities;

/**
 * This class stores client socket connection information.
 */
public class ClientConnection {

    private String host;

    private int port;

    private Socket socket;

    private InputStream inputStream;

    private OutputStream outputStream;

    private Scanner scanner;

    private PrintWriter writer;

    private Thread listenerThread;

    private boolean listening;

    private ServerMessageHandler messageHandler;

    public ClientConnection() {

        this("localhost", 8189);

    }

    public ClientConnection(String host, int port) {

        this.host = host;

        this.port = port;

    }

    public boolean connect() {

        if (isConnected()) {
            return true;
        }

        try {

            socket = new Socket(host, port);

            inputStream = socket.getInputStream();

            outputStream = socket.getOutputStream();

            scanner = new Scanner(inputStream);

            writer = new PrintWriter(outputStream, true);

            return true;

        } catch (IOException e) {

            return false;

        }

    }

    public void startListening() {

        if (scanner == null) {
            return;
        }

        if (listening) {
            return;
        }

        listening = true;

        listenerThread = new Thread(new Runnable() {
            @Override
            public void run() {

                listenForMessages();

            }
        });

        listenerThread.start();

    }

    private void listenForMessages() {

        String message;

        while (listening) {

            try {

                if (scanner.hasNextLine()) {
                    message = scanner.nextLine();
                    deliverMessage(message);
                } else {
                    listening = false;
                }

            } catch (Exception e) {

                listening = false;

            }

        }

    }

    private void deliverMessage(final String message) {

        final ServerMessageHandler currentHandler;

        currentHandler = messageHandler;

        if (currentHandler == null) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                currentHandler.handleServerMessage(message);

            }
        });

    }

    public void sendMessage(String message) {

        if (writer != null) {
            writer.println(message);
        }

    }

    public void sendCommand(String command) {

        sendMessage(command);

    }

    public void sendLogin(String email, String password) {

        sendCommand("LOGIN|" + cleanText(email) + "|" + cleanText(password));

    }

    public void sendCreateClass(String className) {

        sendCommand("CREATE_CLASS|" + cleanText(className));

    }

    public void sendJoinClass(String classCode) {

        sendCommand("JOIN_CLASS|" + cleanText(classCode));

    }

    public void sendCreateQuestion(String classCode, String type, String prompt, String choiceA, String choiceB, String choiceC, String choiceD, String correctAnswer) {

        StringBuilder builder;

        builder = new StringBuilder();

        builder.append("CREATE_QUESTION|");
        builder.append(cleanText(classCode));
        builder.append("|");
        builder.append(cleanText(type));
        builder.append("|");
        builder.append(cleanText(prompt));
        builder.append("|");
        builder.append(cleanText(choiceA));
        builder.append("|");
        builder.append(cleanText(choiceB));
        builder.append("|");
        builder.append(cleanText(choiceC));
        builder.append("|");
        builder.append(cleanText(choiceD));
        builder.append("|");
        builder.append(cleanText(correctAnswer));

        sendCommand(builder.toString());

    }

    public void sendSubmitAnswer(int questionId, String answer) {

        sendCommand("SUBMIT_ANSWER|" + questionId + "|" + cleanText(answer));

    }

    public void sendGetResults(String classCode) {

        sendCommand("GET_RESULTS|" + cleanText(classCode));

    }

    public String readResponse() {

        if (scanner != null) {
            if (scanner.hasNextLine()) {
                return scanner.nextLine();
            }
        }

        return null;

    }

    public void disconnect() {

        listening = false;

        try {

            if (writer != null) {
                writer.println("BYE");
            }

            if (scanner != null) {
                scanner.close();
            }

            if (socket != null) {
                socket.close();
            }

            if (writer != null) {
                writer.close();
            }

        } catch (IOException e) {

            // Keep the close logic simple for now.

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

    public String getHost() { return host; }

    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }

    public void setPort(int port) { this.port = port; }

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

    public Thread getListenerThread() { return listenerThread; }

    public void setListenerThread(Thread listenerThread) { this.listenerThread = listenerThread; }

    public boolean isListening() { return listening; }

    public void setListening(boolean listening) { this.listening = listening; }

    public ServerMessageHandler getMessageHandler() { return messageHandler; }

    public void setMessageHandler(ServerMessageHandler messageHandler) { this.messageHandler = messageHandler; }

    public boolean isConnected() {

        if (socket == null) {
            return false;
        }

        if (socket.isClosed()) {
            return false;
        }

        return socket.isConnected();

    }

}
