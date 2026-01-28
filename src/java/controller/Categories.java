/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.Category;
import hibernate.HibernateUtil;
import hibernate.SubCategory;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "Categories", urlPatterns = {"/Categories"})
public class Categories extends HttpServlet {

    private static final int ACTIVE_STATUS = 1;
    private static final int INACTIVE_STATUS = 2;

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

        ActiveStatus activeStatus = new ActiveStatus();
        activeStatus.setAid(1);
        activeStatus.setAstatus("Active");

        Criteria criteria = ses.createCriteria(Category.class);
        criteria.add(Restrictions.eq("activeStatus", activeStatus));

        if (!criteria.list().isEmpty()) {

            responseObject.add("categories", gson.toJsonTree(criteria.list()));

            List<Category> list = criteria.list();

            for (Category category : list) {

                Criteria subCat = ses.createCriteria(SubCategory.class);
                subCat.add(Restrictions.eq("category", category));
                subCat.add(Restrictions.eq("activeStatus", activeStatus));

                if (!subCat.list().isEmpty()) {
                    responseObject.add("subCategories_" + category.getCat_id(), gson.toJsonTree(subCat.list()));
                }

            }

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

            String selectedCategory = requestObject.get("selectedCategory").getAsString();
            String subcatName = requestObject.get("subcatName").getAsString();

            if (subcatName.isEmpty()) {
                responseObject.addProperty("msg", "Subcategory name is required.");
            } else if (selectedCategory.isEmpty()) {
                responseObject.addProperty("msg", "Please select a category.");
            } else if (!Util.checkInteger(selectedCategory) || Integer.parseInt(selectedCategory) == 0) {
                responseObject.addProperty("msg", "Invalid category.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Category category = (Category) ses.get(Category.class, Integer.parseInt(selectedCategory));

                if (category != null) {

                    Criteria criteria = ses.createCriteria(SubCategory.class);
                    criteria.add(Restrictions.eq("sub_name", subcatName));
                    criteria.add(Restrictions.eq("category", category));

                    if (criteria.list().isEmpty()) {

                        ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, Categories.INACTIVE_STATUS);

                        SubCategory subCategory = new SubCategory();
                        subCategory.setSub_name(subcatName);
                        subCategory.setCategory(category);
                        subCategory.setActiveStatus(activeStatus);
                        ses.save(subCategory);

                        ses.beginTransaction().commit();
                        ses.close();
                        responseObject.addProperty("status", Boolean.TRUE);

                    } else {
                        responseObject.addProperty("msg", "The subcategory already exists.");
                    }

                } else {
                    responseObject.addProperty("msg", "Invalid Category.");
                }
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

}
