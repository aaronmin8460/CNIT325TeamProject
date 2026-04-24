package app;

import server.QuizServer;
import service.MockDataService;

/**
 * This class starts the server side of the quiz project.
 */
public class ServerMain {

    private int port;

    public ServerMain() {

        this(8189);

    }

    public ServerMain(int port) {

        this.port = port;

    }

    public void start() {

        QuizServer quizServer;

        quizServer = new QuizServer(port, new MockDataService());

        quizServer.startServer();

    }

    public static void main(String[] args) {

        ServerMain serverMain = new ServerMain();

        serverMain.start();

    }

    public int getPort() { return port; }

    public void setPort(int port) { this.port = port; }

}
