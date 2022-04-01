package server;

import com.google.gson.Gson;
import server.cli.CommandExecutor;
import server.cli.commands.DeleteCommand;
import server.cli.commands.GetCommand;
import server.cli.commands.SetCommand;
import server.exceptions.BadRequestException;
import server.exceptions.NoSuchKeyException;
import server.requests.Request;
import server.requests.Response;
import server.utils.GsonUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {

    private final Socket socket;
    private final CommandExecutor executor = new CommandExecutor();
    private Gson gson = new Gson();

    public Session(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                socket;
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {

            Request request = new Gson().fromJson(input.readUTF(), Request.class);
            Response response = new Response();

            try {
                switch (request.getType()) {
                    case "get":
                        response.setResponse(Response.STATUS_OK);
                        GetCommand getCmd = new GetCommand(request.getKey());
                        executor.executeCommand(getCmd);
                        response.setValue(getCmd.getResult());
                        break;
                    case "set":
                        SetCommand setCmd = new SetCommand(
                                request.getKey(),
                                request.getValue());
                        executor.executeCommand(setCmd);
                        response.setResponse(Response.STATUS_OK);
                        break;
                    case "delete":
                        DeleteCommand deleteCmd = new DeleteCommand(
                                request.getKey());
                        executor.executeCommand(deleteCmd);
                        response.setResponse(Response.STATUS_OK);
                        break;
                    case "exit":
                        response.setResponse(Response.STATUS_OK);

                        break;
                    default:
                        throw new BadRequestException();
                }
            } catch (BadRequestException | NoSuchKeyException e) {
                response.setResponse(Response.STATUS_ERROR);
                response.setReason(e.getMessage());
            } catch (Exception e) {
                response.setResponse(Response.STATUS_ERROR);
                e.printStackTrace();
            } finally {
                //GsonUtils.prettyPrint(response);
                output.writeUTF(gson.toJson(response));
                if(request.getType().equals("exit")){
                    Server.stopServer();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
