import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String userName;
    private final ArrayList<ClientHandler> clients;

    public ClientHandler(Socket socket, ArrayList<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your name:");
            userName = in.readLine();

            broadcast(userName + " joined the chat");

            String message;
            while ((message = in.readLine()) != null) {
                broadcast(userName + ": " + message);
            }

        } catch (IOException e) {
            System.out.println(userName + " disconnected");
        } finally {
            clients.remove(this);
            broadcast(userName + " left the chat");
        }
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.out.println(message);
        }
    }
}