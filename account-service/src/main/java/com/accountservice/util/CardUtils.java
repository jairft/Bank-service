package com.accountservice.util;

import java.time.YearMonth;
import java.util.concurrent.ThreadLocalRandom;

public class CardUtils {

    public static String generateCardNumber(String brand) {
        String prefix = switch (brand.toUpperCase()) {
            case "VISA" -> "4";
            case "MASTERCARD" -> "5";
            case "ELO" -> "636";
            case "AMEX" -> "34";
            default -> "9";
        };
        StringBuilder number = new StringBuilder(prefix);
        while (number.length() < 15) {
            number.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        int check = luhnCheckDigit(number.toString());
        return number + String.valueOf(check);
    }

    private static int luhnCheckDigit(String num) {
        int sum = 0, alt = 0;
        for (int i = num.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(num.substring(i, i + 1));
            if (alt % 2 == 0) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alt++;
        }
        return (10 - (sum % 10)) % 10;
    }

    public static String maskCardNumber(String number) {
        return number.substring(0, 4) + " **** **** " + number.substring(number.length() - 4);
    }

    public static String generateExpiry() {
        YearMonth future = YearMonth.now().plusYears(4);
        return String.format("%02d/%02d", future.getMonthValue(), future.getYear() % 100);
    }

    public static String generateCvv() {
        return String.format("%03d", ThreadLocalRandom.current().nextInt(0, 1000));
    }
}