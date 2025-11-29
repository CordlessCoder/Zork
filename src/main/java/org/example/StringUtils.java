package org.example;

public class StringUtils {
    public static String centerString(String str, int width) {
        if (str.length() >= width) {
            return str;
        }
        int totalPadding = width - str.length();
        int paddingStart = totalPadding / 2;
        int paddingEnd = totalPadding - paddingStart;
        return " ".repeat(paddingStart) + str + " ".repeat(paddingEnd);
    }
}
