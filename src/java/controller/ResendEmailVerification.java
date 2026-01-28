/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Mail;

/**
 *
 * @author hp
 */
@WebServlet(name = "ResendEmailVerification", urlPatterns = {"/ResendEmailVerification"})
public class ResendEmailVerification extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            if (user.getVerified().equals("Yes")) {
                responseObject.addProperty("status", true);
                responseObject.addProperty("msg", "1");
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Mail.sendMail(user.getEmail(), "Account Verification", user.getVcode());
                    }
                }).start();
                responseObject.addProperty("status", true);
                responseObject.addProperty("msg", "2");
            }
        } else {
            responseObject.addProperty("msg", "Please login.");
        }
        
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(responseObject));

    }

}
