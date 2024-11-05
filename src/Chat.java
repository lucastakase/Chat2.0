import java.io.BufferedReader;
import java.io.IOException;

public class Chat implements Runnable {
    BufferedReader fromChat;

    public Chat(BufferedReader fromChat) {
        this.fromChat = fromChat;
    }

    @Override
    public void run() {
        String wholeChat = "";
        while (true) {
            try {
                wholeChat = fromChat.readLine();
                if (wholeChat == null) {
                    fromChat.close();
                    break;
                }
                if (fromChat != null) {
                    System.out.println(wholeChat);
                }

            } catch (IOException e) {
                System.exit(0);
            }
        }
    }
}
