package app;

import server.QuizServer;
import service.DataService;
import service.MockDataService;
import service.SupabaseService;

/**
 * This class starts the server side of the quiz project.
 */
public class ServerMain {

    private int port;

    private DataService dataService;

    public ServerMain() {

        this(8189, new MockDataService());

    }

    public ServerMain(int port, DataService dataService) {

        this.port = port;

        this.dataService = dataService;

    }

    public void start() {

        QuizServer quizServer;

        quizServer = new QuizServer(port, dataService);

        quizServer.startServer();

    }

    public static void main(String[] args) {

        String mode;
        DataService dataService;
        ServerMain serverMain;

        mode = "mock";

        if (args != null && args.length > 0) {
            mode = args[0];
        }

        dataService = createDataService(mode);

        if (dataService == null) {
            System.out.println("Server was not started.");
            return;
        }

        serverMain = new ServerMain(8189, dataService);

        serverMain.start();

    }

    private static DataService createDataService(String mode) {

        SupabaseService supabaseService;

        if ("supabase".equalsIgnoreCase(mode)) {
            supabaseService = new SupabaseService();

            if (!supabaseService.isConfigured()) {
                System.out.println("SUPABASE_URL or SUPABASE_SERVICE_KEY is missing.");
                System.out.println("Set both environment variables before using supabase mode.");
                return null;
            }

            System.out.println("Starting server with SupabaseService.");
            return supabaseService;
        }

        if (!"mock".equalsIgnoreCase(mode)) {
            System.out.println("Unknown mode: " + mode);
            System.out.println("Use either: mock or supabase");
        }

        System.out.println("Starting server with MockDataService.");
        return new MockDataService();

    }

    public int getPort() { return port; }

    public void setPort(int port) { this.port = port; }

    public DataService getDataService() { return dataService; }

    public void setDataService(DataService dataService) { this.dataService = dataService; }

}
