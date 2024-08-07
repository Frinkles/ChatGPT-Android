package com.ai.alchemist;

public class Message {
    public static String SENT_BY_ME = "me";
    public static String SENT_BY_BOT = "bot";

    String content;
    String sentBy;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public Message(String content, String sentBy) {
        this.content = content;
        this.sentBy = sentBy;
    }
}
