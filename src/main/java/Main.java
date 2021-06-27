import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Main {
    public static void main(String [] args) {
        try (Socket socket = new Socket("time.nist.gov", 13)) {
           socket.setSoTimeout(15000);

           InputStream in = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
