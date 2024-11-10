import java.io.*;
import java.net.Socket;

import java.io.*;
import java.net.Socket;

import java.io.*;
import java.net.Socket;

public class Client {
    // Fields to store connection information and streams
    private Socket socket;
    private final String serverAddress;
    private final int serverPort;
    private BufferedReader userInputReader; // Reader for user input from console
    private BufferedWriter socketOutputWriter; // Writer for sending messages to server
    private BufferedReader socketInputReader; // Reader for receiving messages from server

    // Constructor to initialize client with server address and port
    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    // Method to set up input and output streams for communication with the server
    private void setupSocketStreams() throws IOException {
        // Initialize user input reader from console
        userInputReader = new BufferedReader(new InputStreamReader(System.in));
        // Initialize output stream writer to send messages to the server
        socketOutputWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        // Initialize input stream reader to receive messages from the server
        socketInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Method to start the client connection and chat
    private void start() {
        // Inform the user about connection attempt
        System.out.println("Attempting to establish a connection, please wait...");

        try {
            // Create a socket connection to the server
            socket = new Socket(serverAddress, serverPort);
            // Log successful connection
            System.out.println("Connected to server: " + socket.getRemoteSocketAddress());
            // Set up the input and output streams
            setupSocketStreams();
        } catch (IOException e) {
            // Handle connection errors and exit
            System.err.println("Error connecting to server: " + e.getMessage());
            return;
        }

        // Start a separate thread to handle incoming messages from the server
        Thread messageReceiver = new Thread(new Chat(socketInputReader));
        messageReceiver.start();

        // Handle user input and send messages to the server
        try {
            String userInput;
            while (true) {
                // Read user input from console
                userInput = userInputReader.readLine();
                // Check for quit command
                if (userInput == null || userInput.trim().equalsIgnoreCase("/quit")) {
                    // Inform the user and exit
                    System.out.println("Exiting the chat...");
                    break;
                }
                // Send the user input to the server
                socketOutputWriter.write(userInput);
                socketOutputWriter.newLine();
                socketOutputWriter.flush();
            }
        } catch (IOException e) {
            // Handle errors during communication
            System.err.println("Error during chat communication: " + e.getMessage());
        } finally {
            // Close all resources to ensure proper cleanup
            closeResources();
        }
    }

    // Method to close all open resources (socket and streams)
    private void closeResources() {
        try {
            if (socket != null && !socket.isClosed()) {
                // Close the socket connection
                socket.close();
            }
            if (userInputReader != null) {
                // Close the user input reader
                userInputReader.close();
            }
            if (socketOutputWriter != null) {
                // Close the output stream writer
                socketOutputWriter.close();
            }
            if (socketInputReader != null) {
                // Close the input stream reader
                socketInputReader.close();
            }
        } catch (IOException e) {
            // Handle errors during resource closing
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    // Main method to run the client
    public static void main(String[] args) {
        try {
            // Create a new client instance with server address and port
            Client chatClient = new Client("localhost", 8888);
            // Start the client connection and chat
            chatClient.start();
        } catch (Exception e) {
            // Handle any unexpected exceptions
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}

// Class to handle incoming messages from the server in a separate thread
class Chat implements Runnable {
    private final BufferedReader serverInputReader;

    // Constructor to initialize with the input stream reader for the server
    public Chat(BufferedReader serverInputReader) {
        this.serverInputReader = serverInputReader;
    }

    // Method to continuously read messages from the server and display them on the console
    @Override
    public void run() {
        String serverMessage;
        try {
            while ((serverMessage = serverInputReader.readLine()) != null) {
                // Print the message received from the server
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            // Handle errors during message reading
            System.err.println("Error reading messages from the server: " + e.getMessage());
        }
    }
}