package pl.edu.mimuw.tuto.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtil {
  public static String toNameCase(String string) {
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }

  public static String getHash(String stringToHash) throws NoSuchAlgorithmException {
    MessageDigest digester = MessageDigest.getInstance("SHA-256");
    digester.update(stringToHash.getBytes());
    byte[] hash = digester.digest();
    StringBuilder stringBuilder = new StringBuilder();

    for (byte bytes : hash) {
      stringBuilder.append(String.format("%02x", bytes & 0xff));
    }
    return stringBuilder.toString();
  }
}
