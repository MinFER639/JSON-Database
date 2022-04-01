package server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

enum Server {

    INSTANCE;

    public static final Path DATA_DIR_PATH = Paths.get(
            "src" + File.separator +
                    "main" + File.separator +
                    "java" + File.separator +
                    "server" + File.separator +
                    "data").toAbsolutePath();

    private static final String ADDRESS = "localhost";
    private static final int PORT = 9000;
    private static final int BACKLOG = 50;
    final static int threads = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(threads);


    private static Main server;
    private static ServerSocket serverSocket;
    private static final String address = "127.0.0.1";

    Server() {}

    public static void stopServer() {
        //Stop the executor service.
        executor.shutdown();
        try {
            //Stop accepting requests.
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error in server shutdown");
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void runServer() {
        System.out.println("Server started!");

        try {
            serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName(address));
            Database.INSTANCE.init();
            while (true) {
                try {
                    Socket s = serverSocket.accept();
                    executor.submit(new Session(s));
                } catch (Exception e) {
                    //System.out.println("Error accepting connection");
                    //e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error starting Server on " + PORT);
            e.printStackTrace();
        }
    }


    public void start() {
        System.out.println("Server started!");
        try (ServerSocket socket = new ServerSocket(PORT,
                BACKLOG, InetAddress.getByName(ADDRESS))) {

            Database.INSTANCE.init();

            while (!executor.isShutdown()) {
                Session session = new Session(socket.accept());
                executor.submit(session);
            }
            executor.shutdown();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
