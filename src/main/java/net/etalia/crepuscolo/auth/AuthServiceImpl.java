package net.etalia.crepuscolo.auth;

import net.etalia.crepuscolo.codec.Base64Codec;
import net.etalia.crepuscolo.codec.Digester;
import org.springframework.beans.factory.annotation.Configurable;

import java.security.SecureRandom;

@Configurable
public class AuthServiceImpl implements AuthService {

    private SecureRandom secureRandom = new SecureRandom();

    /**
     * Max time (in millis) for normal tokens
     */
    private long maxTokenTime = 24l * 60l * 60000l;

    public void setMaxTokenTime(long maxTokenTime) {
        this.maxTokenTime = maxTokenTime;
    }

    private String hidePasswordFormatV1(String fromWeb, byte[] saltBytes) {
        // Parse the fromWeb as a base64
        Base64Codec b64 = new Base64Codec();
        byte[] decoded = b64.decodeNoGzip(fromWeb);

        // Put them together
        byte[] payload = new byte[decoded.length + saltBytes.length];
        System.arraycopy(decoded, 0, payload, 0, decoded.length);
        System.arraycopy(saltBytes, 0, payload, decoded.length, saltBytes.length);

        // Run a few rounds of SHA
        Digester digester = new Digester();
        for (int i = 0; i < 100; i++) {
            payload = digester.sha1(payload).unwrap();
        }

        // Encode it
        return "1/" + b64.encodeUrlSafeNoPad(payload) + ":" + b64.encodeUrlSafeNoPad(saltBytes);
    }

    @Override
    public String hidePassword(String fromWeb) {
        // Check if it already has the right format
        if (fromWeb.charAt(1) == '/' && fromWeb.indexOf(':') != -1)
            return fromWeb;
        // Generate a random salt
        byte[] saltBytes = new byte[20];
        secureRandom.nextBytes(saltBytes);
        return hidePasswordFormatV1(fromWeb, saltBytes);
    }

    @Override
    public boolean verifyPassword(String fromDb, String fromWeb) {
        if (fromDb.charAt(1) != '/' || fromDb.indexOf(':') == -1)
            throw new IllegalArgumentException("Given hidden password from DB is not in valid format");

        if (fromDb.startsWith("1/")) {
            // Version 1
            // Decode the salt
            String salt = fromDb.substring(fromDb.indexOf(':') + 1);
            byte[] saltBytes = new Base64Codec().decodeNoGzip(salt);
            return hidePasswordFormatV1(fromWeb, saltBytes).equals(fromDb);
        } else {
            throw new IllegalArgumentException("Version " + fromDb.charAt(0) + " of hidden password is unsupported");
        }
    }

    @Override
    public String generateRandomToken() {
        SecureRandom sr = new SecureRandom();
        sr.setSeed(sr.generateSeed(16));
        byte[] result = new byte[100];
        sr.nextBytes(result);
        return org.apache.commons.codec.binary.Hex.encodeHexString(result);
    }

    protected boolean checkTimeValidityCondition(long timeStamp) {
        return System.currentTimeMillis() <= timeStamp + this.maxTokenTime;
    }

    public long getMaxTokenTime() {
        return maxTokenTime;
    }
}
