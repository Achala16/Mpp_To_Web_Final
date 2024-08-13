package com.example.project.service;

import org.bouncycastle.crypto.digests.SHAKEDigest;
import java.nio.charset.StandardCharsets;

public class HelperService {

    public String shortenedUid(String uid) {
        SHAKEDigest digest = new SHAKEDigest(256);
        byte[] hash = new byte[32]; // 32 bytes for the output length
        digest.update(uid.getBytes(StandardCharsets.UTF_8), 0, uid.length());
        digest.doFinal(hash, 0, 32);
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) {
        HelperService helperService = new HelperService();
        String uid = "example-uid";
        String shortenedUid = helperService.shortenedUid(uid);
        System.out.println("Shortened UID: " + shortenedUid);
    }
}
