package org.fg.readnest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.stream.Collectors;

public class Crypto {

    public static String secretKey = "9k7m82gt1ho043lp";
    private String dbURL;
    private String dbUser;
    private String dbPass;

    /** Liest config.enc und entschlüsselt Login für DB-Anwendung **/
    public Crypto() {
        /** Lokale DB: Umgebungsvariablen haben Vorrang vor config.enc (siehe localdb/setup.sh) **/
        if (System.getenv("READNEST_DB_URL") != null) {
            dbURL = System.getenv("READNEST_DB_URL");
            dbUser = System.getenv("READNEST_DB_USER");
            dbPass = System.getenv("READNEST_DB_PASS");
            return;
        }
        try (InputStream input = getClass().getResourceAsStream("/org/fg/readnest/config.enc")) {
            if (input == null) {
                System.out.println("Datei nicht gefunden");
                return;
            }
            String content = new BufferedReader(new InputStreamReader(input))
                    .lines()
                    .collect(Collectors.joining());
            String[] s = content.trim().split(";");
            if (s.length >= 3) {
                dbUser = decrypt(s[1].trim());
                dbPass = decrypt(s[2].trim());
                dbURL = decrypt(s[0].trim());
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /** Entschlüsselt AES/ECB mit PKCS5-Padding **/
    public static String decrypt(String encrypted) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] b = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(b);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public String getDbURL() {
        return dbURL;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }
}
