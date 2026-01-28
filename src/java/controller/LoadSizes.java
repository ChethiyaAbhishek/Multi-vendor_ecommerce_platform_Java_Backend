/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Category;
import hibernate.HibernateUtil;
import hibernate.SizeHasCategory;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "LoadSizes", urlPatterns = {"/LoadSizes"})
public class LoadSizes extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Gson gson = new Gson();

        String categoryId = request.getParameter("catId");

        if (categoryId.isEmpty()) {
            responseObject.addProperty("msg", "1");
        } else if (!Util.checkInteger(categoryId)) {
            responseObject.addProperty("msg", "1");
        } else {

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Category category = (Category) ses.get(Category.class, Integer.parseInt(categoryId));

            if (category != null) {

                Criteria criteria = ses.createCriteria(SizeHasCategory.class);
                criteria.add(Restrictions.eq("category", category));

                if (!criteria.list().isEmpty()) {
                    responseObject.addProperty("status", Boolean.TRUE);
                    responseObject.add("size", gson.toJsonTree(criteria.list()));
                } else {
                    responseObject.addProperty("msg", "2");
                }

            } else {
                responseObject.addProperty("msg", "1");
            }

            ses.close();

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

}
