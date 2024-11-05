import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private final String SERVER_ADDRESS;
    private final int SERVER_PORT;
    private BufferedReader inputBufferedReader;
    private BufferedWriter outputBufferedWriter;

    private BufferedReader inputBufferedReaderServer;

    public Client(String serverAddress, int serverPort) {

        SERVER_ADDRESS = serverAddress;
        SERVER_PORT = serverPort;

    }

    private void setupSocketStreams() throws IOException {

        inputBufferedReader = new BufferedReader(new InputStreamReader(System.in));
        outputBufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        inputBufferedReaderServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    private void start() throws IOException {

        System.out.println("Trying to establishing the connection, please wait...");

        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to: " + socket.getLocalSocketAddress());
            setupSocketStreams();
        } catch (IOException e) {
            System.out.println("Host unknown: " + e.getMessage());
            System.exit(1);
        }

        Thread group = new Thread(new Chat(inputBufferedReaderServer));
        group.start();

        String line = "";
        while (!line.equals("/quit")) {
            try {
                line = inputBufferedReader.readLine();
                outputBufferedWriter.write(line);
                outputBufferedWriter.newLine();
                outputBufferedWriter.flush();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                break;
            }

        }
        try {

            if (socket != null) {
                System.out.println("Closing the socket");
                socket.close();
            }

        } catch (IOException ex) {

            System.out.println("Error closing connection: " + ex.getMessage());

        }

    }

    public static void main(String[] args) {

        try {
            Client chatClient = new Client("localhost", 8888);
            chatClient.start();

        } catch (NumberFormatException ex) {

            System.out.println(ex.getMessage());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
