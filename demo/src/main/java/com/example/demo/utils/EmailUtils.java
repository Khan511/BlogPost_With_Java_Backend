package com.example.demo.utils;

public class EmailUtils {

    public static String getEmailMessage(String name, String host, String key) {

        return "Hello " + name
                + ",\n\nYour new account has been created. Please click on the link below to verify your account.\n\n"
                + getVerificationUrl(host, key) + "\n\nThe Support Team";
    }

    public static String getVerificationUrl(String host, String key) {
        return host + "/api/blog/verify/account?key=" + key;
    }
}
