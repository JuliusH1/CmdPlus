package me.JuliusH_1;

public class Transaction {
    private final String playerName;
    private final double amount;
    private final long timestamp;

    public Transaction(String playerName, double amount, long timestamp) {
        this.playerName = playerName;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}