/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.servlet.http.Cookie;

/**
 *
 * @author hp
 */
public class Util {

    public static boolean checkEmail(String email) {
        return email.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    }

    public static boolean checkPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$");
    }

    public static String generateCode() {
        int r = (int) (Math.random() * 1000000);
        return String.format("%06d", r);
    }

    public static Cookie createSessionCookie(String sesId) {
        Cookie cookie = new Cookie("JSESSIONID", sesId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(false);
        return cookie;
    }

    public static boolean checkInteger(String value) {
        return value.matches("^\\d+$");
    }

    public static boolean checkDouble(String value) {
        return value.matches("^\\d+(\\.\\d{2})?$");
    }

    public static boolean checkColorCode(String value) {
        return value.matches("^#([A-Fa-f0-9]{6})$");
    }

    public static boolean checkBoolean(String value) {
        if (value == null) {
            return false;
        }
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    public static boolean check6DigitCode(String code) {
        return code.matches("^\\d{6}$");
    }

    public static String generateInvoiceId() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        int random = new Random().nextInt(900) + 100; // 100-999

        return "INV-" + timestamp + "-" + random;
    }

}
