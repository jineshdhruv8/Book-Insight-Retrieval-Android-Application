package AmazonBook;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by JINESH on 8/2/2017.
 */
public class ThreadServer {

    static final int PORT = 9000;

    public static void main(String args[]) throws IOException {
        ServerSocket serverSocket = null;
        Socket socket = null;
        InetAddress inetAddress= null;

        Server server = new Server("classifier");
        server.start();

        try {
            inetAddress = InetAddress.getLocalHost();
            String client_ip=inetAddress.getHostAddress();
            System.out.println("Server IP Address = "+client_ip + " Port = " + PORT);
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                System.out.println("Waiting for Client.....");
                socket = serverSocket.accept();
                System.out.println("Connection Made");
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            new Server("app",socket).start();
        }
    }
}
