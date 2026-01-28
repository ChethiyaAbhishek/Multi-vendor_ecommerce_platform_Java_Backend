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
@WebServlet(name = "ForgotPasswordGetCode", urlPatterns = {"/ForgotPasswordGetCode"})
public class ForgotPasswordGetCode extends HttpServlet {

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

        if (session != null && session.getAttribute("fpcode") == null) {

            JsonObject u = gson.fromJson(request.getReader(), JsonObject.class);
            String email = u.get("email").getAsString();

            if (email.isEmpty()) {
                responseObject.addProperty("msg", "Please enter the email address.");
            } else if (!Util.checkEmail(email)) {
                responseObject.addProperty("msg", "Please enter a valid email address.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = ses.createCriteria(User.class);
                criteria.add(Restrictions.eq("email", email));

                if (criteria.list().isEmpty()) {
                    responseObject.addProperty("status", true);
                    responseObject.addProperty("msg", "1");
                } else {

                    String generatedCode = Util.generateCode();

                    User user = (User) criteria.list().get(0);
                    user.setVcode(generatedCode);
                    ses.update(user);
                    ses.beginTransaction().commit();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Mail.sendMail(email, "Forgot Password - Email Verification", "<h1>Verification Code: " + generatedCode + "</h1>");
                        }
                    }).start();

                    session.setAttribute("fpcode", generatedCode);
                    session.setAttribute("fpemail", email);
                    session.setMaxInactiveInterval(1800);
                    response.addCookie(Util.createSessionCookie(session.getId()));

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("msg", "2");

                }

                ses.close();

            }

        } else {
            responseObject.addProperty("status", Boolean.TRUE);
            responseObject.addProperty("msg", ((String) session.getAttribute("fpemail")));
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
