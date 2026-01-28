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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "EmailVerification", urlPatterns = {"/EmailVerification"})
public class EmailVerification extends HttpServlet {

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

        JsonObject data = gson.fromJson(request.getReader(), JsonObject.class);

        String vcode = data.get("vcode").getAsString();

        if (vcode.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the verification code.");
        } else {

            HttpSession ses = request.getSession();
            User user = (User) ses.getAttribute("user");

            if (user.getVerified().equals("Yes")) {
                responseObject.addProperty("status", Boolean.TRUE);
                responseObject.addProperty("msg", "1");
            } else {

                Session s = HibernateUtil.getSessionFactory().openSession();

                Criteria c = s.createCriteria(User.class);
                c.add(Restrictions.eq("email", user.getEmail()));
                c.add(Restrictions.eq("vcode", vcode));

                if (c.list().isEmpty()) {
                    responseObject.addProperty("msg", "Invalid verification code. please try again.");
                } else {

                    User u = (User) c.list().get(0);
                    u.setVerified("Yes");
                    s.update(u);
                    s.beginTransaction().commit();
                    s.close();

                    ses.setAttribute("user", u);
                    
                    responseObject.addProperty("status", Boolean.TRUE);
                    responseObject.addProperty("msg", "2");

                }

            }

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
