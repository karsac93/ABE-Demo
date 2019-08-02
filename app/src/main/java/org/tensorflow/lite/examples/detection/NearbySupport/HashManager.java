package org.tensorflow.lite.examples.detection.NearbySupport;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;

public class HashManager {
    private static Random randomGenerator;
    private static HashManager obj;

    public static HashManager getInstance(){
        if(obj == null){
            obj = new HashManager();
        }
        return obj;
    }

    private HashManager(){
        randomGenerator = new Random(new Date().getTime());
    }

    private final static int bitLenght = 256;
    public String shaHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashInBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            //System.out.println("Hash length:" + hashInBytes.length);
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Generates a random string from which key chains are generated
     * @return
     */
    public String generateKey() {
        String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        int RANDOM_STRING_LENGTH = bitLenght / 8;
        StringBuffer randStr = new StringBuffer();
        for (int i = 0; i < RANDOM_STRING_LENGTH; i++) {
            int randomInt = 0;
            randomInt = randomGenerator.nextInt(CHAR_LIST.length());
            char ch = CHAR_LIST.charAt(randomInt);
            randStr.append(ch);
        }

        return randStr.toString();
    }
}
