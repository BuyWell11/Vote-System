package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread{
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String currentUser = null;

    public ClientHandler(Socket socket){
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while (!isLogin()){
                inputLine = in.readLine();
                String[] command = inputLine.split(" ");
                if(command[0].equals("login") && command[1].contains("-u=")){
                    String username = command[1].substring(command[1].indexOf("-u=")+3);
                    login(username);
                }
                else {
                    out.println("For login use \"login -u=username\"");
                }
            }

            while ((inputLine = in.readLine()) != null){
                String[] command = inputLine.split(" ");
                if (command[0].equals("create") && command[1].equals("topic") && command[2].contains("-n=")) {
                    String topicName = command[2].substring(command[2].indexOf("-n=")+3);
                    handleCreateTopic(topicName);
                } else if (command[0].equals("view")) {
                    if(command.length == 1){
                        handleView();
                    }
                    else if(command.length == 2 && command[1].contains("-t=")){
                        String topicName = command[1].substring(command[1].indexOf("-t=")+3);
                        handleViewWithTopic(topicName);
                    }
                    else if(command.length == 3 && command[1].contains("-t=") && command[2].contains("-v=")){
                        String topicName = command[1].substring(command[1].indexOf("-t=")+3);
                        String voteName = command[2].substring(command[2].indexOf("-v=")+3);
                        handleViewWithTopicAndVote(topicName,voteName);
                    }
                } else if (command[0].equals("create") && command[1].equals("vote") && command[2].contains("-t=")) {
                    String topicName = command[2].substring(command[2].indexOf("-t=")+3);
                    handleCreateVote(topicName);
                } else if (command[0].equals("vote") && command[1].contains("-t=") && command[2].contains("-v=")) {
                    String topicName = command[1].substring(command[1].indexOf("-t=")+3);
                    String voteName = command[2].substring(command[2].indexOf("-v=")+3);
                    handleVote(topicName, voteName);
                } else if (command[0].equals("delete") && command[1].contains("-t=") && command[2].contains("-v=")) {
                    String topicName = command[1].substring(command[1].indexOf("-t=")+3);
                    String voteName = command[2].substring(command[2].indexOf("-v=")+3);
                    handleDelete(topicName, voteName);
                } else if (command[0].equals("exit")) {
                    handleExit();
                    break;
                }
                else {
                    out.println("This command doesn't exist");
                }
            }
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isLogin(){
        return currentUser != null;
    }

    private void login(String username){
        if (Server.addUser(username)){
            this.currentUser = username;
            out.println("You login as " + username);
        }
        else{
            out.println("This username already taken");
        }
    }

    private void handleCreateTopic(String topicName){
        if(Server.addTopic(topicName)){
            out.println(topicName + " is created");
            System.out.println(currentUser + " create topic " + topicName);
        }
    }

    private void handleView(){
        String str = Server.getTopicNames();
        out.println(str);
    }

    private void handleViewWithTopic(String topicName){
        out.println(Server.getVotes(topicName));
    }

    private void handleViewWithTopicAndVote(String topicName, String voteName){
        out.println(Server.getVoteAnswers(topicName, voteName));
    }

    private void handleCreateVote(String topicName){
        if(!Server.isTopicExist(topicName)){
            out.println(topicName + " doesn't exist");
            return;
        }
        try {
            out.println("Enter vote name");
            String voteName = in.readLine();
            Topic topic = Server.getTopic(topicName);
            if(topic.isVoteExists(voteName)){
                out.println(voteName + " already taken");
                return;
            }
            out.println("Enter description");
            String description = in.readLine();
            out.println("Enter the number of responses in the vote");
            int countOfVars = Integer.parseInt(in.readLine());
            List<String> answer = new ArrayList<>();
            for (int i = 0; i< countOfVars; i++){
                out.println("Enter answer");
                answer.add(in.readLine());
            }
            topic.createVote(voteName, description, this.currentUser, answer);
            out.println("Vote " + voteName+ " is created");
            System.out.println(currentUser + " created vote " + voteName + " in " + topicName);
        } catch (IOException e) {
            out.println("Error while creating vote");
        }

    }

    private void handleVote(String topicName, String voteName){
        if(!Server.isTopicExist(topicName)){
            out.println("Topic " + topicName + " doesn't exist");
            return;
        }
        Topic topic = Server.getTopic(topicName);
        if(!topic.isVoteExists(voteName)){
            out.println("Vote " + voteName + " doesn't exist");
            return;
        }
        Topic.Vote vote = topic.getVote(voteName);
        out.println(vote.getDescription() + " Answer choice: " + vote.getAnswers() + "Enter one of the possible answers ");
        try {
            String answer = in.readLine();
            while (!vote.isWordInAnswers(answer)){
                out.println("There is no such answer option");
                answer = in.readLine();
            }
            if(vote.addVote(currentUser,answer)){
                out.println("Your answer is counted");
                System.out.println(currentUser + " voted in " + voteName);
            }
            else {
                out.println("Have you already voted");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(String topicName, String voteName){
        if(!Server.isTopicExist(topicName)){
            out.println(topicName + " doesn't exist");
            return;
        }
        Topic topic = Server.getTopic(topicName);
        if(!topic.isVoteExists(voteName)){
            out.println(voteName + " doesn't exist");
            return;
        }
        if(topic.deleteVote(voteName, this.currentUser)){
            out.println(voteName + " deleted");
            System.out.println(currentUser + " delete vote " + voteName);
        }
        else {
            out.println("You are not owner of " + voteName);
        }
    }
    private void handleExit(){
        out.println("exit");
        Server.deleteUser(currentUser);
        System.out.println(currentUser + " exit");
    }
}
