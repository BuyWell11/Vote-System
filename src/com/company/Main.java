package com.company;

public class Main {

    public static void main(String[] args) {
        if(args.length != 1){
            System.out.println("Use client or server");
            System.exit(1);
        }

        String mode = args[0];

        if(mode.equalsIgnoreCase("server")){
            Server server = new Server();
            server.start();
            try {
                server.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if(mode.equalsIgnoreCase("client")){
            Client client = new Client();
            client.start();
            try {
                client.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Use client or server");
        }
    }
}
