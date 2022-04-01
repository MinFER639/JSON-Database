package client;

import com.google.gson.Gson;
import server.cli.CommandLineArgs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;


public class Client {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 9000;
    private static Gson gson = new Gson();

    public static void start(CommandLineArgs cla) {
        System.out.println("Client Started");

        try (
                Socket socket = new Socket(InetAddress.getByName(ADDRESS), PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {

            String request = cla.filename != null
                    ? new String(Files.readAllBytes(Path.of("C:\\Users\\48602\\IdeaProjects\\JSON Database\\JSON Database\\task\\src\\client\\" + cla.filename)))
                    : gson.toJson(cla);

            if (request.isBlank() || "{}".equals(request)) {
                System.out.println("No request given !");
                return;
            }

            Gson gson = new Gson();
            output.writeUTF(request);
            System.out.println("Sent: " + request);
            System.out.println("Received: " + input.readUTF());
        } catch (NoSuchFileException e) {
            System.out.println("Cannot read file: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String makeJSON(CommandLineArgs cla) {
        Map<String, String> reqToSent = new LinkedHashMap<>();
        Gson gson = new Gson();
        String type = "type";
        String key = "key";
        String value = "value";

        String reqType = cla.type;;
        String reqArg;
        String reqRecord;
        if (reqType.equals("set")) {
            reqRecord = cla.value;
            reqArg = cla.key;
            reqToSent.put(type, reqType);
            reqToSent.put(key, reqArg);
            reqToSent.put(value, reqRecord);
        } else if (reqType.equals("exit")) {
            reqToSent.put(type, reqType);
        } else {
            reqArg = cla.key;
            reqToSent.put(type, reqType);
            reqToSent.put(key, reqArg);
        }
         return gson.toJson(reqToSent);
    }
}
