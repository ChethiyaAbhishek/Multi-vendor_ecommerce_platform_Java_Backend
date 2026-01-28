/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.ProductReview;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
@WebServlet(name = "LoadReviews", urlPatterns = {"/LoadReviews"})
public class LoadReviews extends HttpServlet {

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

        if (session != null && session.getAttribute("seller") != null) {

            User user = (User) session.getAttribute("seller");

            String fResult = request.getParameter("firstResult");

            int firstResult = 0;

            if (!fResult.isEmpty() || Util.checkInteger(fResult)) {
                firstResult = Integer.parseInt(fResult);
            }

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Criteria c1 = ses.createCriteria(ProductReview.class);

            if (!c1.list().isEmpty()) {

                List<ProductReview> plist = c1.list();
                List<ProductReview> reviewList = new ArrayList<>();
                HashMap customerNames = new HashMap<>();
                for (ProductReview review : plist) {
                    if (review.getProduct().getUser().getUid() == user.getUid()) {
                        customerNames.put(review.getRid(), review.getUser().getFname() + " " + review.getUser().getLname());
                        reviewList.add(review);
                    }
                }

                for (ProductReview productReview : reviewList) {
                    productReview.setUser(null);
                    productReview.getProduct().setUser(null);
                }

                responseObject.add("reviews", gson.toJsonTree(reviewList));
                responseObject.add("customers", gson.toJsonTree(customerNames));
                responseObject.addProperty("status", Boolean.TRUE);

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

    }

}
