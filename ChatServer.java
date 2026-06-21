import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

    static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler =
                        new ClientHandler(socket, clients);
                clients.add(clientHandler);
                clientHandler.start();
            }

        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}