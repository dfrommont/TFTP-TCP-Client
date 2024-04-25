package Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient {

    private static final int MAX_SIZE = 512; //Max size of data that can be communicated
    protected static Socket clientSocket = null; //Initialise client socket

    protected static PrintWriter out = null; //Initialise socket output
    protected static BufferedReader in = null; //Initialise socket input

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
            clientSocket = new Socket(); //Open the client socket
            clientSocket.connect(new InetSocketAddress(InetAddress.getByName(name), 69)); //Connect socket to the machine's address and port number 69

            clientSocket.setSoTimeout(30000); //Socket has a 30-second timeout on communication

            out = new PrintWriter(clientSocket.getOutputStream(), true); //Use a PrintWriter to access the outputStream of the socket
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //Open the socket's inputStream within a BufferedReader

            if (flag == 1) {
                System.out.println("Enter the file name including it's extension:");
                String fileName = input.nextLine(); //Retrieve file name from client
                receiveFile(clientSocket, fileName); //Call method to handle a read request with format: 'GET fileName'
            }

            if (flag == 2) {
                System.out.println("Enter the file name including it's extension (File should be in the 'files' directory):");
                String fileName = input.nextLine();//Retrieve file name from client
                sendFile(clientSocket, out, fileName, args);//Call method to handle a write request with format: 'PUT fileName'
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException) {
                clientSocket.close(); //Close socket if communication has timed out
            }
        } finally {
            if (clientSocket != null) {
                clientSocket.close(); //Close the socket when communication is over if it hasn't already been closed
            }
        }
    }

    private static void sendFile(Socket socket, PrintWriter out, String fileName, String[] args) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream("./files/" + fileName)) { //Open a FileInputStream for the desired file
            byte[] bytesBuffer = new byte[MAX_SIZE];
            int bytes;
            OutputStream outputStream = socket.getOutputStream(); //Open the socket's outputStream

            out.println("PUT " + fileName); //Write 'PUT file name' request to Server

            while ((bytes = fileInputStream.read(bytesBuffer)) != -1) {
                outputStream.write(bytesBuffer, 0, bytes); //Write the file data received from request to socket
            }

            out.println("END"); //Send end of communication tag to the server

            outputStream.flush(); //Push all the data to the socket and Server

            System.out.println("PUT " + fileName + " successful");
        } catch (Exception e) {
            if (e instanceof  FileNotFoundException) {
                System.out.println(fileName + " could not be found");
                main(args); //if desired file to send could not be found, restart the main method
            }
            e.printStackTrace();
        }
    }


    private static void receiveFile(Socket socket, String fileName) throws IOException {
        out.println("GET " + fileName); //Send 'GET file name' to Server

        try (FileOutputStream fileOutputStream = new FileOutputStream("./files/" + fileName)) { //Open a fileOutputStream for the desired file in './files/'
            byte[] bytesBuffer = new byte[MAX_SIZE];
            int bytes;
            InputStream inputStream = socket.getInputStream(); //Open the socket inputStream

            while ((bytes = inputStream.read(bytesBuffer)) != -1) {
                String receivedData = new String(bytesBuffer, 0, bytes);
                if (receivedData.trim().equals("END")) {
                    fileOutputStream.flush();
                    break; //If the data received on the inputStream is the end of communication tag, stop writing to the file
                }
                fileOutputStream.write(bytesBuffer, 0, bytes); //Write the data received from the Server for the fileOutputStream
            }

            System.out.println("GET " + fileName + " successful");
        }
    }
}