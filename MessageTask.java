import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

// Main class representing the message task
public class MessageTask {
    private static final int MAX_MESSAGES_IN_QUEUE = 1;
    private static final int MAX_MESSAGES_TO_SEND_RECEIVE = 10;

    public static void main(String[] args) {
        // Create communication channels (BlockingQueues) between players
        BlockingQueue<String> firstToSecond = new ArrayBlockingQueue<>(MAX_MESSAGES_IN_QUEUE);
        BlockingQueue<String> secondToFirst = new ArrayBlockingQueue<>(MAX_MESSAGES_IN_QUEUE);

        // Both players are now instances of the Player class
        Player firstPlayer = new InitiatorPlayer(firstToSecond, secondToFirst);
        Player secondPlayer = new Player(secondToFirst, firstToSecond);

        // Starting threads for both players
        new Thread(secondPlayer).start();
        new Thread(firstPlayer).start();
    }
}

// Base class representing a generic player with message communication
// capabilities
class Player implements Runnable {
    protected final BlockingQueue<String> sent;
    protected final BlockingQueue<String> received;

    private BigInteger numberOfMessagesSent = new BigInteger("0");
    private BigInteger numberOfMessagesReceived = new BigInteger("0");
    private volatile boolean shouldTerminate = false;

    // Constructor
    public Player(BlockingQueue<String> sent, BlockingQueue<String> received) {
        this.sent = sent;
        this.received = received;
    }

    // Run method for the player thread
    @Override
    public void run() {
        while (!shouldTerminate) {
            String receivedMessage = receive();
            reply(receivedMessage);
        }
    }

    // Method to receive a message
    protected String receive() {
        String receivedMessage;
        try {
            receivedMessage = received.take();
            numberOfMessagesReceived = numberOfMessagesReceived.add(BigInteger.ONE);
        } catch (InterruptedException interrupted) {
            String error = String.format(
                    "Player [%s] failed to receive message on iteration [%d].",
                    this, numberOfMessagesSent);
            throw new IllegalStateException(error, interrupted);
        }
        return receivedMessage;
    }

    // Method to reply to a received message
    protected void reply(String receivedMessage) {
        String reply = receivedMessage + " " + numberOfMessagesSent;
        try {
            sent.put(reply);
            System.out.printf("Player [%s] sent message [%s].%n", this, reply);
            numberOfMessagesSent = numberOfMessagesSent.add(BigInteger.ONE);

            Thread.sleep(1000);

            // Graceful exit condition
            if (numberOfMessagesSent.equals(BigInteger.TEN) && numberOfMessagesReceived.equals(BigInteger.TEN)) {
                System.out.println("Player finished gracefully.");
                shouldTerminate = true; // Set the flag to indicate termination
            }
        } catch (InterruptedException interrupted) {
            String error = String.format(
                    "Player [%s] failed to send message [%s] on iteration [%d].",
                    this, reply, numberOfMessagesSent);
            throw new IllegalStateException(error);
        }
    }
}

// Class representing the Initiator Player
class InitiatorPlayer extends Player {
    private static final String INIT_MESSAGE = "initiator player";
    private static final int MAX_MESSAGES_TO_SEND_RECEIVE = 10;
    private int messagesSentAndReceived = 0;

    // Constructor
    public InitiatorPlayer(BlockingQueue<String> sent, BlockingQueue<String> received) {
        super(sent, received);
    }

    // Run method for the Initiator Player thread
    @Override
    public void run() {
        sendInitMessage();
        while (messagesSentAndReceived < MAX_MESSAGES_TO_SEND_RECEIVE) {
            String receivedMessage = receive();
            reply(receivedMessage);
            messagesSentAndReceived++;
        }
        System.out.println("Initiator Player finished gracefully.");
    }

    // Method to send the initiation message
    private void sendInitMessage() {
        try {
            sent.put(INIT_MESSAGE);
            System.out.printf("Player [%s] sent message [%s].%n", this, INIT_MESSAGE);
        } catch (InterruptedException interrupted) {
            String error = String.format(
                    "Player [%s] failed to send message [%s].",
                    this, INIT_MESSAGE);
            throw new IllegalStateException(error, interrupted);
        }
    }
}