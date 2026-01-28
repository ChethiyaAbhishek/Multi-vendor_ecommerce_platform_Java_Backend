/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Category;
import hibernate.HibernateUtil;
import hibernate.Size;
import hibernate.SizeHasCategory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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
@WebServlet(name = "Sizes", urlPatterns = {"/Sizes"})
public class Sizes extends HttpServlet {

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

        String catId = request.getParameter("catId");

        if (catId.isEmpty()) {
            responseObject.addProperty("msg", "1");
        } else if (!Util.checkInteger(catId) && Integer.parseInt(catId) < 1) {
            responseObject.addProperty("msg", "1");
        } else {

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Category category = (Category) ses.load(Category.class, Integer.parseInt(catId));

            if (category != null) {

                Criteria c1 = ses.createCriteria(SizeHasCategory.class);
                c1.add(Restrictions.eq("category", category));

                if (!c1.list().isEmpty()) {
                    responseObject.add("sizes", gson.toJsonTree(c1.list()));
                    responseObject.addProperty("status", Boolean.TRUE);
                } else {
                    responseObject.addProperty("msg", "1");
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
