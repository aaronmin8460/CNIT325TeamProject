import javax.swing.*;

/**
 * This class starts the client side of the quiz project.
 */
public class ClientMain {

    private String host;

    private int port;

    public ClientMain() {

        this("127.0.0.1", 8189);

    }

    public ClientMain(String host, int port) {

        this.host = host;

        this.port = port;

    }

    public void start() {

        System.out.println("Connecting to server " + host + ":" + port);

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

        String host;
        int port;
        ClientMain clientMain;

        host = "127.0.0.1";
        port = 8189;

        if (args != null && args.length > 0) {
            host = args[0];
        }

        if (args != null && args.length > 1) {
            port = parsePort(args[1], port);
        }

        clientMain = new ClientMain(host, port);

        clientMain.start();

    }

    private static int parsePort(String portText, int defaultPort) {

        try {
            return Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port: " + portText);
            System.out.println("Using default port: " + defaultPort);
            return defaultPort;
        }

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
