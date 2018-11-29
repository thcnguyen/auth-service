package net.etalia.crepuscolo.auth;

public interface AuthService {

    /**
     * "Hides" a password hashing it with SHA1 with a salt.
     * <p>
     * The input <code>fromWeb</code> should be a base64 representation of
     * the password, preferably an MD5 of the password to avoid eavesdroppers.
     * </p><p>
     * The output will be a base64 encoded SHA1 of the given input base64 + a random salt,
     * followed by a column and the base64 of the salt itself.
     * </p><p>
     * The returned string can be stored and used later, together with the same
     * <code>fromWeb</code> input stream, to check password validity in the
     * {@link #verifyPassword(String, String)} method.
     * </p><p>
     * If the given stirng is already in the right format, nothing is done and
     * the same string is simply returned, so that this method can be called
     * multiple times without accumulating outputs.
     * </p>
     *
     * @param fromWeb A base64, preferably MD5, representation of the password.
     * @return A salt protected hash that can be used in {@link #verifyPassword(String, String)} to
     * check password validity.
     */
    String hidePassword(String fromWeb);

    /**
     * Verifies if the given <code>fromWeb</code> base64 string is the same as the given
     * <code>fromDb</code> hidden password.
     * <p>
     * The input <code>fromWeb</code> should be a base64 representation of
     * the password, preferably an MD5 of the password to avoid eavesdroppers.
     * </p><p>
     * The input <code>fromDb</code> must be a string produced by {@link #hidePassword(String)},
     * previously stored in the DB.
     * <p>
     *
     * @param fromDb  The hidden password produced by {@link #hidePassword(String)} and stored on the DB
     * @param fromWeb The base64 representation of a password, preferably MD5 of it.
     * @return true if the password is verified, false otherwise
     */
    boolean verifyPassword(String fromDb, String fromWeb);

    String generateRandomToken();

    long getMaxTokenTime();

}
