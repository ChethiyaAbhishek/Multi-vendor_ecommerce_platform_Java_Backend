/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Brand;
import hibernate.HibernateUtil;
import hibernate.Varient;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "Varients", urlPatterns = {"/Varients"})
public class Varients extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
      
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Session ses = HibernateUtil.getSessionFactory().openSession();

        Gson gson = new Gson();

        Criteria criteria = ses.createCriteria(Varient.class);

        if (!criteria.list().isEmpty()) {

            responseObject.add("varients", gson.toJsonTree(criteria.list()));
            responseObject.addProperty("status", Boolean.TRUE);

        } else {
            responseObject.addProperty("msg", "An error occured. Please try again later.");
        }

        ses.close();

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
        
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
     
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        Gson gson = new Gson();

        JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        if (request.getSession() != null && request.getSession().getAttribute("seller") != null) {

            String varientName = requestObject.get("varient").getAsString();

            if (varientName.isEmpty()) {
                responseObject.addProperty("msg", "Varient name is required.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = ses.createCriteria(Varient.class);
                criteria.add(Restrictions.eq("vname", varientName));

                if (criteria.list().isEmpty()) {

                    Varient varient = new Varient();
                    varient.setVname(varientName);
                    ses.save(varient);

                    ses.beginTransaction().commit();
                    ses.clear();
                    ses.close();
                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "The varient already exists.");
                }

            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
        
        
    }

}
