/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Product;
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
import org.hibernate.Session;

/**
 *
 * @author hp
 */
@WebServlet(name = "ProductUpdate", urlPatterns = {"/ProductUpdate"})
public class ProductUpdate extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Gson gson = new Gson();

        HttpSession session = request.getSession();

        if (session != null && session.getAttribute("seller") != null) {

            User user = (User) session.getAttribute("seller");

            JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);

            String pid = requestObject.get("pid").getAsString();
            String sdesc = requestObject.get("sdesc").getAsString();
            String ldesc = requestObject.get("ldesc").getAsString();

            if (pid.isEmpty() || !Util.checkInteger(pid) || Integer.parseInt(pid) < 1) {
                responseObject.addProperty("msg", "Something went wrong. Please try again later");
            } else if (sdesc.isEmpty()) {
                responseObject.addProperty("msg", "Short description is required.");
            } else if (ldesc.isEmpty()) {
                responseObject.addProperty("msg", "Long description is required.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Product product = (Product) ses.get(Product.class, Integer.parseInt(pid));

                if (product != null) {

                    product.setSdescription(sdesc);
                    product.setLdescription(ldesc);

                    ses.update(product);
                    ses.beginTransaction().commit();
                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "Product not found.");
                }

                ses.clear();
                ses.close();
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
