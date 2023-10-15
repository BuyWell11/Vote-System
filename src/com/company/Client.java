package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread{
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 3000;
    private static PrintWriter out;
    private static BufferedReader in;

    @Override
    public void run() {
        try (Socket socket = new Socket(SERVER_IP, PORT)){
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            String userInput;

            System.out.println("Use login -u=username for login in system");
            while ((userInput = consoleInput.readLine()) != null){
                out.println(userInput);
                String serverResponse = in.readLine();
                System.out.println("Server: " + serverResponse);
                if(serverResponse.equals("exit")){
                    break;
                }
            }
            out.close();
            in.close();
            consoleInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
