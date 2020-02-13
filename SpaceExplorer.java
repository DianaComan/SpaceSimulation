import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;

/**
 * Class for a space explorer.
 */
public class SpaceExplorer extends Thread {

    /**
     * Creates a {@code SpaceExplorer} object.
     *
     * @param hashCount
     *            number of times that a space explorer repeats the hash operation
     *            when decoding
     * @param discovered
     *            set containing the IDs of the discovered solar systems
     * @param channel
     *            communication channel between the space explorers and the
     *            headquarters
     */
    public static int hashCount = -1;

    Set<Integer> disc;
    public static CommunicationChannel channel = null;

    final static String Miner_EXIT = "EXIT";


    public SpaceExplorer(Integer hashCount, Set<Integer> discovered, CommunicationChannel channel) {
        this.channel = channel;
        this.hashCount = hashCount;
        disc = discovered;
    }

    @Override
    public void run() {

        Integer parentSolarSystem, currentSolarSystem;
        String key;
        Message keyMessage, solvedMessage;

        while (true) {

            keyMessage = channel.getMessageHeadQuarterChannel();

            if (keyMessage == null)
                continue;
            if (keyMessage.getData().equals("EXIT"))
                break;
            if (disc.contains(keyMessage.getCurrentSolarSystem()))
                continue;

            parentSolarSystem = keyMessage.getParentSolarSystem();
            currentSolarSystem = keyMessage.getCurrentSolarSystem();
            key = keyMessage.getData();

            disc.add(currentSolarSystem);
            key = encryptMultipleTimes(key, hashCount);

            solvedMessage = new Message(parentSolarSystem, currentSolarSystem, key);
            channel.putMessageSpaceExplorerChannel(solvedMessage);

        }
    }


    /**
     * Applies a hash function to a string for a given number of times (i.e.,
     * decodes a frequency).
     *
     * @param input
     *            string to he hashed multiple times
     * @param count
     *            number of times that the string is hashed
     * @return hashed string (i.e., decoded frequency)
     */
    private String encryptMultipleTimes(String input, Integer count) {
        String hashed = input;
        for (int i = 0; i < count; ++i) {
            hashed = encryptThisString(hashed);
        }

        return hashed;
    }

    /**
     * Applies a hash function to a string (to be used multiple times when decoding
     * a frequency).
     *
     * @param input
     *            string to be hashed
     * @return hashed string
     */
    private static String encryptThisString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xff & messageDigest[i]);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
