package common;

import org.jasypt.util.text.StrongTextEncryptor;

public class EncryptionHelper {

    public static String encrypt(String text, String password) throws Exception
    {
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(password);
        return textEncryptor.encrypt(text);
    }

    public static String decrypt(String text, String password) {
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(password);
        return textEncryptor.decrypt(text);
    }


}
