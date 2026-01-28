/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.Admin;
import hibernate.BatchDetails;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.Size;
import hibernate.SubCategory;
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
@WebServlet(name = "SubCategories", urlPatterns = {"/SubCategories"})
public class SubCategories extends HttpServlet {

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

        Gson gson = new Gson();

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("admin") != null) {

            Admin admin = (Admin) session.getAttribute("admin");

            String firstResult = request.getParameter("fresult");

            if (!firstResult.isEmpty() || Util.checkInteger(firstResult)) {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                ActiveStatus inactiveStatus = (ActiveStatus) ses.get(ActiveStatus.class, SubCategories.INACTIVE_STATUS);

                Criteria c1 = ses.createCriteria(SubCategory.class);
                c1.add(Restrictions.eq("activeStatus", inactiveStatus));

                if (!c1.list().isEmpty()) {

                    responseObject.addProperty("itemsCount", c1.list().size());

                    c1.setFirstResult(Integer.parseInt(firstResult));
                    c1.setMaxResults(4);

                    responseObject.add("subcategories", gson.toJsonTree(c1.list()));
                    responseObject.addProperty("status", Boolean.TRUE);
                } else {
                    responseObject.addProperty("msg", "3");
                    responseObject.addProperty("status", Boolean.TRUE);
                }
            } else {
                responseObject.addProperty("msg", "2");
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

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

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Gson gson = new Gson();

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("admin") != null) {

            Admin admin = (Admin) session.getAttribute("admin");

            JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);

            String subId = requestObject.get("subId").getAsString();

            if (!subId.isEmpty() || Util.checkInteger(subId)) {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, SubCategories.ACTIVE_STATUS);

                SubCategory subCategory = (SubCategory) ses.get(SubCategory.class, Integer.parseInt(subId));

                if (subCategory != null) {
                    if (subCategory.getActiveStatus().getAid() == SubCategories.INACTIVE_STATUS) {
                        subCategory.setActiveStatus(activeStatus);
                        ses.update(subCategory);
                        ses.beginTransaction().commit();
                        responseObject.addProperty("status", Boolean.TRUE);
                    }
                } else {
                    responseObject.addProperty("msg", "2");
                }

                ses.clear();
                ses.close();
            } else {
                responseObject.addProperty("msg", "2");
            }
        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
