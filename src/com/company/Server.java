package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server extends Thread {
    private static final int PORT = 3000;
    private static List<String> users = new ArrayList<>();
    private static Map<String,Topic> topics = new HashMap<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    Scanner input = new Scanner(System.in);

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT);) {
            System.out.println("Server is running");
            new Thread(() -> {
                try {
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println(clientSocket + " connected");
                        new ClientHandler(clientSocket).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            String serverInput;
            while ((serverInput = input.nextLine()) != null) {
                String[] command = serverInput.split(" ");
                if (command.length == 2 && command[0].equals("load")) {
                    String filePath = command[1];
                    lock.writeLock().lock();
                    try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
                        topics = (Map<String, Topic>) in.readObject();
                        System.out.println("Date restored");
                    } catch (IOException | ClassNotFoundException e){
                        e.printStackTrace();
                    } finally {
                        lock.writeLock().unlock();
                    }
                } else if (command.length == 2 && command[0].equals("save")) {
                    String filePath = command[1];
                    lock.readLock().lock();
                    createFile(filePath);
                    try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
                        out.writeObject(topics);
                        System.out.println("Date saved");
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    finally {
                        lock.readLock().unlock();
                    }
                } else if (serverInput.equals("exit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile(String filePath){
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static public boolean addUser(String username) {
        if (inUserList(username)) {
            return false;
        }
        lock.writeLock().lock();
        try {
            users.add(username);
            System.out.println(username + " is login");
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    static private boolean inUserList(String username) {
        lock.readLock().lock();
        try {
            for (String name : users) {
                if (name.equals(username)) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    static public boolean addTopic(String topicName){
        if(isTopicExist(topicName)){
            return false;
        }
        lock.writeLock().lock();
        try {
            topics.put(topicName, new Topic(topicName));
            return true;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    static public boolean isTopicExist(String topicName){
        lock.readLock().lock();
        try {
            return topics.containsKey(topicName);
        }
        finally {
            lock.readLock().unlock();
        }

    }

    static public String getTopicNames(){
        lock.readLock().lock();
        try {
            if(topics.isEmpty()){
                return "No topics";
            }
            StringBuilder str = new StringBuilder();
            for (Topic topic : topics.values()){
                str.append(topic).append("; ");
            }
            return str.toString();
        }
        finally {
            lock.readLock().unlock();
        }
    }

    static public String getVotes(String topicName){
        if(!isTopicExist(topicName)){
            return topicName + " doesn't exist";
        }
        lock.readLock().lock();
        try {
            return topics.get(topicName).getAllVotes();
        }
        finally {
            lock.readLock().unlock();
        }
    }

    static public String getVoteAnswers(String topicName, String voteName){
        if(!isTopicExist(topicName)){
            return topicName + " doesn't exist";
        }
        else if (!topics.get(topicName).isVoteExists(voteName)){
            return voteName + " doesn't exist in " + topicName;
        }
        lock.readLock().lock();
        try {
            return topics.get(topicName).getVoteAnswers(voteName);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public static Topic getTopic(String topicName){
        lock.readLock().lock();
        try {
            return topics.get(topicName);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public static void deleteUser(String username){
        lock.writeLock().lock();
        try {
            users.remove(username);
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}
