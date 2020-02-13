import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {

    /**
     * Creates a {@code CommunicationChannel} object.
     */

    private BlockingQueue<Message> spaceChannel;
    private BlockingQueue<Message> HQChannel;
    private static AtomicInteger mess;
    private Integer pSolarSystem;
    private static ReentrantLock permision = new ReentrantLock();

    public CommunicationChannel() {
        spaceChannel = new LinkedBlockingQueue<Message>();
        HQChannel = new LinkedBlockingQueue<Message>();
        mess = new AtomicInteger(0);
    }

    /**
     * Puts a message on the space explorer channel (i.e., where space explorers write to and
     * headquarters read from).
     *
     * @param message
     *            message to be put on the channel
     */
    public void putMessageSpaceExplorerChannel(Message message) {
        spaceChannel.add(message);
    }

    /**
     * Gets a message from the space explorer channel (i.e., where space explorers write to and
     * headquarters read from).
     *
     * @return message from the space explorer channel
     */
    public Message getMessageSpaceExplorerChannel() {
            return spaceChannel.poll();
    }

    /**
     * Puts a message on the headquarters channel (i.e., where headquarters write to and
     * space explorers read from).
     *
     * @param message
     *            message to be put on the channel
     */
    public void putMessageHeadQuarterChannel(Message message) {
        permision.lock();
        if (mess.get() == 0) {
            mess.set(2);
            if (message.getData().equals("EXIT"))
                HQChannel.add(message);
            if (!message.getData().equals("END") && !message.getData().equals("EXIT")) {
                pSolarSystem = message.getCurrentSolarSystem();
                mess.set(1);
            }

        } else {
            if (mess.get() == 1) {
                int currentSolarSystem = message.getCurrentSolarSystem();
                String key = message.getData();
                Message newMessage = new Message(pSolarSystem, currentSolarSystem, key);
                HQChannel.add(newMessage);
                mess.set(2);
            }
        }

        if (mess.get() == 2) {
            mess.set(0);
            int lock_no = permision.getHoldCount();
            for (int i = 0; i < lock_no; i++)
                permision.unlock();
        }
    }

    /**
     * Gets a message from the headquarters channel (i.e., where headquarters write to and
     * space explorer read from).
     *
     * @return message from the header quarter channel
     */
    public Message getMessageHeadQuarterChannel() {
        Message messageFromHQ = null;
        do {
            messageFromHQ = HQChannel.poll();
        } while(messageFromHQ == null);

        return messageFromHQ;
    }
}

