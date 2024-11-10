import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    public static final String RESET = "\033[0m";  // Reset color
    public static final String RED = "\033[31m";    // Red color
    public static final String GREEN = "\033[32m";  // Green color
    public static final String YELLOW = "\033[33m"; // Yellow color
    public static final String BLUE = "\033[34m";   // Blue color
    public static final String MAGENTA = "\033[35m"; // Magenta color
    public static final String CYAN = "\033[36m";    // Cyan color
    public static final String WHITE = "\033[37m";   // White color

    private final int PORT;
    private final List<ServerWorker> workers = new CopyOnWriteArrayList<>();

    // Constructor to initialize the server with a specified port
    public Server(int port) {
        this.PORT = port;
    }

    // Method to start the server and accept incoming connections
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Binding to port: " + PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                System.out.println("Waiting for a client connection...");
                Socket clientSocket = serverSocket.accept(); // Accept incoming client connections
                System.out.println("New client connected.");
                ServerWorker worker = new ServerWorker(clientSocket);
                workers.add(worker); // Add the new worker to the list
                new Thread(worker).start(); // Start the worker in a new thread
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Method to broadcast a message to all connected clients except the sender
    public void broadcast(String message, ServerWorker sender) {
        for (ServerWorker worker : workers) {
            if (worker.getName() != null && worker != sender) {
                worker.send(sender.getName() + ": " + sender.colorString + message + RESET);
            }
        }
    }

    // Method to send a private message (whisper) to a specific client
    public void whisper(String message, ServerWorker sender, String recipient) {
        for (ServerWorker worker : workers) {
            if (worker.getName() != null && worker.getName().equals(recipient)) {
                worker.send(sender.getName() + " [whisper]: " + sender.colorString + message + RESET);
                return;
            }
        }
        sender.send("User '" + recipient + "' not found.");
    }

    // Method to remove a worker from the list when they disconnect
    public void removeWorker(ServerWorker worker) {
        workers.remove(worker);
    }

    // Inner class to handle each client's connection
    private class ServerWorker implements Runnable {
        private final Socket socket;
        private BufferedReader inputReader;
        private BufferedWriter outputWriter;
        private String name;
        private String color = "default";
        private String colorString = RESET;// Default color for user messages

        // Constructor to initialize the worker with a client socket
        public ServerWorker(Socket socket) {
            this.socket = socket;
        }

        // Getter for the worker's name
        public String getName() {
            return name;
        }

        // Method to send a message to this client
        public void send(String message) {
            try {
                outputWriter.write(message);
                outputWriter.newLine();
                outputWriter.flush();
            } catch (IOException e) {
                System.err.println("Failed to send message to " + name + ": " + e.getMessage());
            }
        }

        // Method to handle the /name command
        private void handleNameCommand(String[] parts) {
            if (parts.length < 2 || parts[1].trim().isEmpty()) {
                send("Invalid name. Use: /name <new_name>");
                return;
            }
            String newName = parts[1].trim();
            for (ServerWorker worker : workers) {
                if (worker.getName() != null && worker.getName().equals(newName)) {
                    send("Name already taken. Choose a different name.");
                    return;
                }
            }
            this.name = newName;
            send("Name changed to: " + name);
        }

        // Method to handle the /whisper command
        private void handleWhisperCommand(String[] parts) {
            if (parts.length < 3) {
                send("Usage: /whisper <username> <message>");
                return;
            }
            String recipient = parts[1].trim();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                stringBuilder.append(parts[i]).append(" ");
            }
            String message = stringBuilder.toString().trim();
            whisper(message, this, recipient);
        }

        // Method to handle the /color command
        private void handleColorCommand(String[] parts) {
            if (parts.length < 2 || parts[1].trim().isEmpty()) {
                send("Invalid color. Use: /color <color_name>");
                return;
            }
            this.color = parts[1].trim();
            switch (color.toLowerCase()) {
                case "red":
                    colorString = RED;
                    break;
                case "green":
                    colorString = GREEN;
                    break;
                case "yellow":
                    colorString = YELLOW;
                    break;
                case "blue":
                    colorString = BLUE;
                    break;
                case "magenta":
                    colorString = MAGENTA;
                    break;
                case "cyan":
                    colorString = CYAN;
                    break;
                case "white":
                    colorString = WHITE;
                    break;
                default:
                    colorString = RESET;
                    color = "Default";
                    break;
            }
            send("Color changed to: " + color);
        }

        // Method to process incoming commands
        private void handleCommand(String command) {
            String[] parts = command.split(" ", 3); // Split into at most 3 parts for cleaner parsing
            if (command.startsWith("/name")) {
                handleNameCommand(parts);
            } else if (command.startsWith("/whisper")) {
                handleWhisperCommand(parts);
            } else if (command.startsWith("/color")) {
                handleColorCommand(parts);
            } else {
                send("Unknown command: " + command);
            }
        }

        // The main run method to handle client communication
        @Override
        public void run() {
            try {
                inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                send("Welcome to the chat server! Use /name <your_name> to set your name.");
                send("You can also set your message color using /color <color_name>.");

                String message;
                while ((message = inputReader.readLine()) != null) {
                    if (message.startsWith("/")) {
                        handleCommand(message);
                    } else if (this.name != null) {
                        broadcast(message, this);
                    } else {
                        send("Set your name first using [/name <your_name>].");
                    }
                }
            } catch (IOException e) {
                System.err.println("Client disconnected: " + e.getMessage());
            } finally {
                try {
                    if (inputReader != null) inputReader.close();
                    if (outputWriter != null) outputWriter.close();
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
                removeWorker(this);
                if (this.name != null) {
                    broadcast(this.name + " has left the chat.", null);
                }
            }
        }
    }

    // Main method to start the server
    public static void main(String[] args) {
        Server chatServer = new Server(8888);
        chatServer.start();
    }
}
