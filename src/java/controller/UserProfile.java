/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.City;
import hibernate.District;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
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
@MultipartConfig
@WebServlet(name = "UserProfile", urlPatterns = {"/UserProfile"})
public class UserProfile extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        HttpSession session = request.getSession(false);
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Gson gson = new Gson();

        if (session != null && session.getAttribute("user") != null) {

            User u = (User) session.getAttribute("user");

            Session ses = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = ses.createCriteria(Address.class);
            criteria.add(Restrictions.eq("user", u));

            if (!criteria.list().isEmpty()) {

                responseObject.addProperty("status", Boolean.TRUE);

                List<Address> address = criteria.list();
                responseObject.add("address", gson.toJsonTree(address));

                Criteria districts = ses.createCriteria(District.class);
                responseObject.add("districts", gson.toJsonTree(districts.list()));
            }
        }

        response.getWriter().write(gson.toJson(responseObject));
        response.setContentType("application/json");
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

        HttpSession webSession = request.getSession(false);

        if (webSession != null && webSession.getAttribute("user") != null) {
            
            String fname = request.getParameter("fname");
            String lname = request.getParameter("lname");
            String mobile = request.getParameter("mobile");
            String email = request.getParameter("email");
            String line1 = request.getParameter("line1");
            String line2 = request.getParameter("line2");
            String city = request.getParameter("city");
            int district = Integer.parseInt(request.getParameter("district"));
            String profileImg = request.getParameter("profileImg");

            if (fname.isEmpty()) {
                responseObject.addProperty("msg", "First name cannot be empty.");
            } else if (lname.isEmpty()) {
                responseObject.addProperty("msg", "Last name cannot be empty.");
            } else if (mobile.isEmpty()) {
                responseObject.addProperty("msg", "Mobile number is required.");
            } else if (mobile.length() != 10) {
                responseObject.addProperty("msg", "Please enter a valid mobile number.");
            } else if (email.isEmpty()) {
                responseObject.addProperty("msg", "Email address is required.");
            } else if (!Util.checkEmail(email)) {
                responseObject.addProperty("msg", "Please enter a valid email address.");
            } else if (line1.isEmpty()) {
                responseObject.addProperty("msg", "Address line 01 cannot be empty.");
            } else if (line2.isEmpty()) {
                responseObject.addProperty("msg", "Address line 02 cannot be empty.");
            } else if (city.isEmpty()) {
                responseObject.addProperty("msg", "City is required.");
            } else if (district == 0) {
                responseObject.addProperty("msg", "Please select the district.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = ses.createCriteria(User.class);
                criteria.add(Restrictions.eq("email", email));

                if (!criteria.list().isEmpty()) {

                    User user = (User) criteria.list().get(0);

                    if (!user.getFname().equals(fname)
                            || !user.getLname().equals(lname)
                            || !user.getMobile().equals(mobile)) {
                        user.setFname(fname);
                        user.setLname(lname);
                        user.setMobile(mobile);
                        ses.update(user);
                    }

                    Criteria addr = ses.createCriteria(Address.class);
                    addr.add(Restrictions.eq("user", user));
//                    addr.add(Restrictions.eq("city", user.get));
//                    List<Address> add = addr.list();
                    Address a = (Address) addr.list().get(0);
                    
                    

//                    for (Address a : add) {
                        if (!a.getLine1().equals(line1)
                                || !a.getLine2().equals(line2)
                                || !a.getCity().getCname().equals(city)) {

                            City c = a.getCity();
                            
                            if (!a.getCity().getCname().equals(city)) {
                                
                                Criteria crt = ses.createCriteria(City.class);
                                crt.add(Restrictions.ilike("cname", city));

                                if (crt.list().isEmpty()) {

                                    Criteria dis = ses.createCriteria(District.class);
                                    dis.add(Restrictions.eq("dis_id", district));

                                    District disObject = (District) dis.list().get(0);

                                    c = new City();
                                    c.setCname(city);
                                    c.setDistrict(disObject);
                                    ses.save(c);

                                } else {
                                    c = (City) crt.list().get(0);
                                }
                            }

                            a.setLine1(line1);
                            a.setLine2(line2);
                            a.setCity(c);
                            a.setUser(user);
                            ses.update(a);
                        }
//                    }
                    
                    ses.beginTransaction().commit();
                    responseObject.addProperty("status", Boolean.TRUE);

                } else {
                    responseObject.addProperty("msg", "An error occured. Please try again later.");
                }

                ses.close();

            }

        }

        response.getWriter().write(gson.toJson(responseObject));
        response.setContentType("application/json");
    }

}
