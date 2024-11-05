import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Server {
    private Socket clientSocket;
    private final int PORT;
    private ServerSocket serverSocket;
    private BufferedReader inputBufferedReader;
    private BufferedWriter outputBufferedWriter;
    public List<ServerWorker> list = new ArrayList<>();


    public Server(int port) {
        this.PORT = port;
    }

    public void start() {
        try {
            System.out.println("Binding to port " + PORT);
            serverSocket = new ServerSocket(PORT);

            System.out.println("Server started: " + serverSocket);

            // block waiting for a client to connect
            System.out.println("Waiting for a client connection");
            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("New client inside");
                ServerWorker worker = new ServerWorker(clientSocket);
                Thread newClient = new Thread(worker);
                list.add(worker);
                newClient.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendToAll(String str) {
        System.out.println("sending to all");
        for (ServerWorker l : list) {
            try {
                outputBufferedWriter = new BufferedWriter(new OutputStreamWriter(l.socket.getOutputStream()));
                outputBufferedWriter.write(str);
                outputBufferedWriter.newLine();
                outputBufferedWriter.flush();
            } catch (IOException e) {
                System.out.println("ERROR SENDING TO ALL");
            }
        }
    }

    public void quit(ServerWorker worker) {
        try {
            worker.socket.close();
            list.remove(worker);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void kick(String string) {
        System.out.println(1223);
        for (ServerWorker l : list) {
            System.out.println(l.name);
            if (l.name.equals(string)) {
                quit(l);
            }
        }
    }

    public static void main(String[] args) {
        Server chatServer = new Server(8888);
        chatServer.start();

    }

    private class ServerWorker implements Runnable {

        public Socket socket;

        public String str = "";
        private String name = "Anonymous: ";

        public ServerWorker(Socket socket) {
            this.socket = socket;
            this.name = name;

        }

        public void send(String str) {
            sendToAll(str);
        }

        public void changeName(String name) {
            this.name = name;

        }

        @Override
        public void run() {
            try {
                inputBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    str = inputBufferedReader.readLine();
                    if (str == null) {
                        break;
                    }
                    if (str.startsWith("/quit")) {
                        quit(this);
                        break;
                    }
                    if (str.startsWith("/name")) {
                        String[] spited = str.split(" ");
                        changeName(spited[1]);
                        send(name + ": " + "This is my new name");
                        continue;
                    }
                    if (str.startsWith("/kick")) {
                        System.out.println(41);
                        String[] spited = str.split(" ");
                        kick(spited[1]);
                    }
                    send(this.name + ": " + str);
                }
            } catch (IOException e) {
                System.out.println("bye");
            } finally {
                send(name + " is out");
                System.out.println(socket.getLocalAddress() + " is out!");
                quit(this);
            }
        }
    }
}

