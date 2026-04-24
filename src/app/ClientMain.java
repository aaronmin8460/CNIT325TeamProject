package app;

import client.ClientConnection;
import client.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * This class starts the client side of the quiz project.
 */
public class ClientMain {

    private String host;

    private int port;

    public ClientMain() {

        this("localhost", 8189);

    }

    public ClientMain(String host, int port) {

        this.host = host;

        this.port = port;

    }

    public void start() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                ClientConnection clientConnection;
                LoginFrame loginFrame;

                clientConnection = new ClientConnection(host, port);
                loginFrame = new LoginFrame(clientConnection, null);
                loginFrame.setVisible(true);

            }
        });

    }

    public static void main(String[] args) {

        ClientMain clientMain = new ClientMain();

        clientMain.start();

    }

    public String getHost() { return host; }

    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }

    public void setPort(int port) { this.port = port; }

}
