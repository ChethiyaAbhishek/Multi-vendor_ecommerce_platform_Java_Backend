/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.Brand;
import hibernate.Category;
import hibernate.HibernateUtil;
import hibernate.SubCategory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "Brands", urlPatterns = {"/Brands"})
public class Brands extends HttpServlet {

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

        ses.clear();
        Criteria criteria = ses.createCriteria(Brand.class);

        if (!criteria.list().isEmpty()) {

            responseObject.add("brands", gson.toJsonTree(criteria.list()));
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

            String brandName = requestObject.get("brandName").getAsString();

            if (brandName.isEmpty()) {
                responseObject.addProperty("msg", "Brand name is required.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = ses.createCriteria(Brand.class);
                criteria.add(Restrictions.eq("bname", brandName));

                if (criteria.list().isEmpty()) {

                    Brand brand = new Brand();
                    brand.setBname(brandName);
                    ses.save(brand);

                    ses.beginTransaction().commit();
                    ses.clear();
                    ses.close();
                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "The brand already exists.");
                }

            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
