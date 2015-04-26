package edu.cmu.cs.cs214.rec15.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import edu.cmu.cs.cs214.rec15.server.Message;
import edu.cmu.cs.cs214.rec15.util.Log;

/**
 * Implementation of a chat client that connects to server using Sockets
 * 
 * @author tsun
 *
 */
public class ChatClientImpl extends Thread implements ChatClient {
    private static final String TAG = "CLIENT";
    private Socket socket = null;
    private String username = null;
    private ObjectOutputStream out = null;


    @Override
    public boolean sendMessage(String message) {
        try {
            Message msg = new Message(message, username);
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public void connectToServer(String host, int port) {
        // Terminate any existing connections
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore the error
                Log.e(TAG, "Unable to terminate connection with previous host");
            }
        }
        // Connect to the new server
        try {
            Log.i(TAG, String.format("Connected to server %s:%d.", host, port));
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Log.e(TAG, String.format("Could not connect to %s:%d", host, port));
        }
        this.start();
    }


    @Override
    public void run() {
        try {
            while (true) {
                ObjectInputStream in = new ObjectInputStream(
                        socket.getInputStream());
                Message msg = (Message) in.readObject();
                System.out.println(msg);
                System.out.println();
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.toString());
        }
    }


    public static void main(String[] args) {
        String username = "Dummy"; // Default username
        String defaultHost = "localhost";
        int defaultPort = 15214;
        
        // Convert all args into a username
        if(args.length > 0){
            StringBuilder nameBuilder = new StringBuilder();
            for (int x = 0; x < args.length; x++) {
                if (x > 0) {
                    nameBuilder.append(" ");
                }
                nameBuilder.append(args[x]);
            }
            username = nameBuilder.toString();
        }
        
        // Creates client and connects to server
        ChatClient client = new ChatClientImpl();
        client.setUsername(username);
        client.connectToServer(defaultHost, defaultPort);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String msg = null;
        try {
            while (true) {
                msg = br.readLine();
                if (msg.equals("/quit")) {
                    break;
                }
                client.sendMessage(msg);
                System.out.println();
            }
        } catch (IOException e) {
            System.out.println("Error reading from system in");
            System.exit(1);
        }
        try {
            br.close();
        } catch (IOException e) {
            // Ignore
        }
        System.exit(1);
    }
}
