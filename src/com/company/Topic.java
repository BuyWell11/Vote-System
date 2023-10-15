package com.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Topic implements Serializable {
    private final String name;
    private final Map<String, Vote> votes = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Topic(String name) {
        this.name = name;
    }

    public static class Vote implements Serializable{
        private final String description;
        private final String owner;
        private final List<String> userWhoVote = new ArrayList<>();
        private final Map<String, Integer> countOfAnswers = new HashMap<>();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        private Vote(String description, String owner, List<String> answers) {
            this.description = description;
            this.owner = owner;
            for (String answer : answers) {
                countOfAnswers.put(answer, 0);
            }
        }

        public boolean addVote(String username, String answer) {
            if (userWhoVote.contains(username)) {
                return false;
            }
            lock.writeLock().lock();
            try {
                countOfAnswers.put(answer, countOfAnswers.get(answer) + 1);
                userWhoVote.add(username);
                return true;
            } finally {
                lock.writeLock().unlock();
            }
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder(description + ": ");
            for (Map.Entry<String, Integer> entry : countOfAnswers.entrySet()) {
                str.append(entry.getKey()).append(": ").append(entry.getValue()).append(" ");
            }
            return str.toString();
        }

        public boolean isWordInAnswers(String word){
            return countOfAnswers.containsKey(word);
        }

        public String getDescription(){
            return this.description;
        }

        public String getAnswers(){
            StringBuilder str = new StringBuilder();
            for(String s: countOfAnswers.keySet()){
                str.append(s).append("; ");
            }
            return str.toString();
        }
    }

    public void createVote(String voteName, String description, String owner, List<String> answers) {
        lock.writeLock().lock();
        try {
            votes.put(voteName, new Vote(description, owner, answers));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isVoteExists(String voteName) {
        lock.readLock().lock();
        try {
            return votes.containsKey(voteName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean deleteVote(String voteName, String username) {
        Vote vote = votes.get(voteName);
        if (!vote.owner.equals(username)) {
            return false;
        }
        lock.writeLock().lock();
        try {
            votes.remove(voteName);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private int getCountOfVote() {
        int countOfVote = 0;
        for (Map.Entry<String, Vote> answer : votes.entrySet()) {
            countOfVote += answer.getValue().countOfAnswers.values().stream().reduce(0, Integer::sum);
        }
        return countOfVote;
    }

    @Override
    public String toString() {
        return name + " (votes in topic = " + getCountOfVote() + ")";
    }

    public String getAllVotes() {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, Vote> answer : votes.entrySet()) {
            str.append(answer.getKey()).append("; ");
        }
        return str.toString();
    }

    public String getVoteAnswers(String voteName){
        Vote vote = votes.get(voteName);
        return vote.toString();
    }

    public Vote getVote(String voteName){
        return votes.get(voteName);
    }
}
