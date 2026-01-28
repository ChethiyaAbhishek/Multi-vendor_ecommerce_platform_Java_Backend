/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Admin;
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
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "UserLogin", urlPatterns = {"/UserLogin"})
public class UserLogin extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        JsonObject loginCredentials = gson.fromJson(request.getReader(), JsonObject.class);
        String email = loginCredentials.get("email").getAsString();
        String password = loginCredentials.get("password").getAsString();

        if (email.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the email address.");
        } else if (!Util.checkEmail(email)) {
            responseObject.addProperty("msg", "Please enter a valid email address.");
        } else if (password.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the password.");
        } else if (!Util.checkPassword(password)) {
            responseObject.addProperty("msg", "Please enter a valid password.");
        } else {

            Session s = HibernateUtil.getSessionFactory().openSession();

            Criteria user = s.createCriteria(User.class);
            user.add(Restrictions.eq("email", email));
            user.add(Restrictions.eq("password", password));

            HttpSession ses = request.getSession();

            if (user.list().isEmpty()) {

                Criteria c1 = s.createCriteria(Admin.class);
                c1.add(Restrictions.eq("email", email));
                c1.add(Restrictions.eq("password", password));

                if (!c1.list().isEmpty()) {

                    Admin admin = (Admin) c1.uniqueResult();

                    ses.setAttribute("admin", admin);
                    response.addCookie(Util.createSessionCookie(ses.getId()));
                    responseObject.addProperty("status", Boolean.TRUE);
                    responseObject.addProperty("msg", "admin");
                } else {
                    responseObject.addProperty("msg", "Please use valid credentials and try again.");
                    s.clear();
                }
            } else {

                User u = (User) user.list().get(0);

                ses.setAttribute("user", u);
                if (u.getUserStatus().getUsid() == 2) {
                    ses.setAttribute("seller", u);
                }
                response.addCookie(Util.createSessionCookie(ses.getId()));

                if (u.getVerified().equals("Yes")) {
                    responseObject.addProperty("msg", "1");
                } else {
                    responseObject.addProperty("msg", "2");
                }

                responseObject.addProperty("status", Boolean.TRUE);

            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
