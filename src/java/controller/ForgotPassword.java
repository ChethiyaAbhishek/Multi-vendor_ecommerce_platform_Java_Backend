/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
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
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "ForgotPassword", urlPatterns = {"/ForgotPassword"})
public class ForgotPassword extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        HttpSession session = request.getSession();

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        if (session != null && session.getAttribute("fpcode") != null) {

            JsonObject u = gson.fromJson(request.getReader(), JsonObject.class);
            String email = u.get("email").getAsString();
            String vcode = u.get("vcode").getAsString();
            String npassword = u.get("npassword").getAsString();
            String cpassword = u.get("cpassword").getAsString();

            if (email.isEmpty()) {
                responseObject.addProperty("msg", "Please enter the email address.");
            } else if (!Util.checkEmail(email)) {
                responseObject.addProperty("msg", "Please enter a valid email address.");
            } else if (vcode.isEmpty()) {
                responseObject.addProperty("msg", "Please enter the verification code.");
            } else if (npassword.isEmpty()) {
                responseObject.addProperty("msg", "Please enter the new password.");
            } else if (!Util.checkPassword(npassword)) {
                responseObject.addProperty("msg", "Please enter a password which contains at least a uppercase, lowercase, number, special character and minimum of eight characters.");
            } else if (cpassword.isEmpty()) {
                responseObject.addProperty("msg", "Please enter the confirm password.");
            } else if (!npassword.equals(cpassword)) {
                responseObject.addProperty("msg", "The two passwords does not match. Please re-enter the confirm password.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = ses.createCriteria(User.class);
                criteria.add(Restrictions.eq("email", email));
                criteria.add(Restrictions.eq("vcode", vcode));

                if (criteria.list().isEmpty()) {
                    responseObject.addProperty("msg", "The verification code does not match. Please try again.");
                } else {

                    User user = (User) criteria.list().get(0);
                    user.setPassword(cpassword);
                    ses.update(user);
                    ses.beginTransaction().commit();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Mail.sendMail(email, "Password Changed", "<h2>Your password has been changed.</h2><br/> <h5>If you did not initiate this request, please change your password or contact Customer Service</h5>");
                        }
                    }).start();

                    session.setAttribute("user", user);
                    response.addCookie(Util.createSessionCookie(session.getId()));

                    responseObject.addProperty("status", true);
                }

                ses.close();

            }

        } else {
            responseObject.addProperty("msg", "Please enter you email and verification code to proceed.");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
