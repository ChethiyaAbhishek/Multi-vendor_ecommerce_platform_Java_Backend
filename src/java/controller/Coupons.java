/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.Cart;
import hibernate.Coupon;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
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
@WebServlet(name = "Coupons", urlPatterns = {"/Coupons"})
public class Coupons extends HttpServlet {

    private static final int ACTIVE_STATUS_ID = 1;

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

        String couponCode = request.getParameter("code").trim();

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");

            if (couponCode.isEmpty()) {
                responseObject.addProperty("msg", "Coupon code is required.");
            } else if (!Util.check6DigitCode(couponCode)) {
                responseObject.addProperty("msg", "Invalid coupon code.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, Coupons.ACTIVE_STATUS_ID);

                Criteria c1 = ses.createCriteria(Coupon.class);
                c1.add(Restrictions.eq("code", couponCode));
                c1.add(Restrictions.eq("user", user));
                c1.add(Restrictions.eq("activeStatus", activeStatus));

                if (!c1.list().isEmpty()) {

                    Coupon coupon = (Coupon) c1.uniqueResult();

                    LocalDate activatedOn = coupon.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate expireDate = activatedOn.plusDays(coupon.getActive_duration());
                    LocalDate today = LocalDate.now();

                    if (today.isBefore(expireDate)) {

                        responseObject.addProperty("status", true);
                        responseObject.addProperty("discount", coupon.getCouponDiscounts().getDiscount());

                    } else {
                        responseObject.addProperty("msg", "The coupon code has expired.");
                    }

                } else {
                    responseObject.addProperty("msg", "No any coupon codes found for the logged user.");
                }
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
