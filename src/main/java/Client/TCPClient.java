package Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient {

    private static final int MAX_PACKET_SIZE = 10000; // Maximum size of UDP packet
    protected static Socket clientSocket = null;

    protected static PrintWriter out = null;
    protected static BufferedReader in = null;
    protected static BufferedReader stdin = null;

    public static void main(String[] args) throws IOException {

        Scanner input = new Scanner(System.in);
        System.out.println("TCP Socket Client");
        System.out.println("Please enter IP or name of Server:");
        String name = input.nextLine();
        System.out.println("1. Read file from " + name);
        System.out.println("2. Write file to " + name);
        System.out.println("Pick an option:");
        int flag = Integer.parseInt(input.nextLine());

        try {

            // Server address and port
            InetAddress inetAddress = InetAddress.getByName(name);
            SocketAddress clientAddress = new InetSocketAddress(inetAddress, 1235);

            // Create a UDP socket
            clientSocket = new Socket();
            clientSocket.connect(clientAddress);

            //clientSocket.setSoTimeout(5000);

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            stdin = new BufferedReader(new InputStreamReader(System.in));

            if (flag == 1) {
                // Request to retrieve a file by name
                System.out.println("Enter the file name including it's extension:");
                String file_name = input.nextLine();
                receiveFile(clientSocket, in, file_name);
            }

            if (flag == 2) {
                // Request to send a file to the server
                System.out.println("Enter the file name including it's extension (File should be in the 'files' directory):");
                String file_name = input.nextLine();
                sendFile(clientSocket, out, file_name);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException) {
                clientSocket.close();
            }
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }

    private static void sendFile(Socket socket, PrintWriter out, String file_name) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream("./files/" + file_name)) {
            byte[] buffer = new byte[MAX_PACKET_SIZE];
            int bytesRead;
            OutputStream outputStream = socket.getOutputStream();

            // Send file name
            out.println(file_name);

            // Send file content
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            out.println("__END_OF_TRANSMISSION__");

            outputStream.flush();

            System.out.println("File sent successfully.");
        }
    }

    private static void receiveFile(Socket socket, BufferedReader in, String file_name) throws IOException {
        out.println("GET " + file_name);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file_name)) {
            byte[] buffer = new byte[MAX_PACKET_SIZE];
            int bytesRead;
            InputStream inputStream = socket.getInputStream();

            // Receive file content
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.flush();
            System.out.println("File received successfully.");
        }
    }
}