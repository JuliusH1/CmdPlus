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

    @Override
    public String toString() {
        return "Transaction{" +
                "playerName='" + playerName + '\'' +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
    public double getAmount() {
        return amount;
    }

    public String getFormattedTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
}