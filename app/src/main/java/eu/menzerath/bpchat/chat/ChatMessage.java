package eu.menzerath.bpchat.chat;

import java.util.Date;

/**
 * Eine normale Chat-Nachricht mit alle notwendigen Daten
 * Klasse implementiert außerdem eine Methode, sodass die Nachrichten sortiert werden können
 */
public class ChatMessage implements Comparable<ChatMessage> {
    private final int id;
    private final String from;
    private final Date time;
    private final String message;
    private final boolean ownMessage;

    public ChatMessage(int id, String from, Date time, String message, boolean ownMessage) {
        this.id = id;
        this.from = from;
        this.time = time;
        this.message = message;
        this.ownMessage = ownMessage;
    }

    @Override
    public int compareTo(ChatMessage chatMessage) {
        // Methode, damit die Nachrichten (nach ihrer ID) sortiert werden können ("implements Comparable...")
        return Integer.compare(this.id, chatMessage.id); // Methode erst ab API 19 verfügbar?!
    }

    public int getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public Date getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public boolean isOwnMessage() {
        return ownMessage;
    }
}