/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.PayHere;

/**
 *
 * @author hp
 */
@WebServlet(name = "PaymentVerification", urlPatterns = {"/PaymentVerification"})
public class PaymentVerification extends HttpServlet {

    private static final int PAYHERE_SUCCESS = 2;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        System.out.println("veri awa");

        String merchantId = request.getParameter("merchant_id");
        String orderId = request.getParameter("order_id");
        String payhereAmount = request.getParameter("payhere_amount");
        String payhereCurrency = request.getParameter("payhere_currency");
        String statusCode = request.getParameter("status_code");
        String md5sig = request.getParameter("md5sig");

        String merchantSecret = "";
        String merchantSecretMD5 = PayHere.generateMD5(merchantSecret);
        String hash = PayHere.generateMD5(merchantId + orderId + payhereAmount + payhereCurrency + statusCode + merchantSecretMD5);

        if (md5sig.equals(hash) && Integer.parseInt(statusCode) == PaymentVerification.PAYHERE_SUCCESS) {
            System.out.println("Payment Completed. Order Id:" + orderId);
            String order_id = orderId.substring(1);
            System.out.println(order_id); // 1
        }

    }

}
