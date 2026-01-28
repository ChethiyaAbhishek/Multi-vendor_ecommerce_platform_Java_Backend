/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.AvailabilityReminder;
import hibernate.HibernateUtil;
import hibernate.Product;
import hibernate.Size;
import hibernate.SizeHasCategory;
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
@WebServlet(name = "SetAvailabilityReminder", urlPatterns = {"/SetAvailabilityReminder"})
public class SetAvailabilityReminder extends HttpServlet {

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

        if (session != null && session.getAttribute("user") != null) {

            User user = (User) session.getAttribute("user");

            int product_id = Integer.parseInt(request.getParameter("product_id"));
            String size = request.getParameter("size");

            if (request.getParameter("product_id").isEmpty() || size.isEmpty()) {
                responseObject.addProperty("msg", "2");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Product product = (Product) ses.load(Product.class, product_id);

                if (product != null) {

                    Criteria crt1 = ses.createCriteria(Size.class);
                    crt1.add(Restrictions.eq("size", size));

                    if (!crt1.list().isEmpty()) {
                        Size sizeObject = (Size) crt1.list().get(0);

                        Criteria sizeCriteria = ses.createCriteria(SizeHasCategory.class);
                        sizeCriteria.add(Restrictions.eq("size", sizeObject));
                        sizeCriteria.add(Restrictions.eq("category", product.getSubCategory().getCategory()));

                        if (!sizeCriteria.list().isEmpty()) {

                            Criteria crt2 = ses.createCriteria(ActiveStatus.class);
                            crt2.add(Restrictions.ilike("astatus", "Active"));
                            ActiveStatus activeStatus = (ActiveStatus) crt2.list().get(0);

                            Criteria crt3 = ses.createCriteria(AvailabilityReminder.class);
                            crt3.add(Restrictions.eq("user", user));
                            crt3.add(Restrictions.eq("product", product));
                            crt3.add(Restrictions.eq("size", sizeObject));

                            if (crt3.list().isEmpty()) {
                                AvailabilityReminder remainder = new AvailabilityReminder();
                                remainder.setProduct(product);
                                remainder.setUser(user);
                                remainder.setActiveStatus(activeStatus);
                                remainder.setSize(sizeObject);
                                ses.save(remainder);
                                
                                ses.beginTransaction().commit();
                            } else {
                                responseObject.addProperty("msg", "3");
                            }

                            responseObject.addProperty("status", Boolean.TRUE);

                        } else {
                            responseObject.addProperty("msg", "2");
                        }

                    } else {
                        responseObject.addProperty("msg", "2");
                    }

                } else {
                    responseObject.addProperty("msg", "2");
                }

                ses.close();
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
