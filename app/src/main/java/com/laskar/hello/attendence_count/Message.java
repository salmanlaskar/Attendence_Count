package com.laskar.hello.attendence_count;

public class Message {
    String name;
    String message;

    public Message() {
    }
    /*public  Message(Message message){
        this.name=message.getName();
        this.message=message.getMessage();
    }*/
    public Message(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
