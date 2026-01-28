/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.Address;
import hibernate.City;
import hibernate.District;
import hibernate.HibernateUtil;
import hibernate.User;
import hibernate.UserStatus;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Mail;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "UserRegistration", urlPatterns = {"/UserRegistration"})
public class UserRegistration extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        Gson gson = new Gson();
        JsonObject userDetails = gson.fromJson(request.getReader(), JsonObject.class);

        String fname = userDetails.get("fname").getAsString();
        String lname = userDetails.get("lname").getAsString();
        String email = userDetails.get("email").getAsString();
        String password = userDetails.get("password").getAsString();
        String mobile = userDetails.get("mobile").getAsString();
        String line1 = userDetails.get("line1").getAsString();
        String line2 = userDetails.get("line2").getAsString();
        String city = userDetails.get("city").getAsString();
        int district = userDetails.get("district").getAsInt();
        String province = userDetails.get("province").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (fname.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the first name.");
        } else if (lname.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the last name.");
        } else if (email.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the email address.");
        } else if (!Util.checkEmail(email)) {
            responseObject.addProperty("msg", "Please enter a valid email address.");
        } else if (password.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the password.");
        } else if (!Util.checkPassword(password)) {
            responseObject.addProperty("msg", "Please enter a password which contains at least a uppercase, lowercase, number, special character and minimum of eight characters.");
        } else if (mobile.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the mobile number.");
        } else if (mobile.length() != 10) {
            responseObject.addProperty("msg", "Please enter a valid mobile number.");
        } else if (line1.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the address line 01.");
        } else if (line2.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the address line 02.");
        } else if (city.isEmpty()) {
            responseObject.addProperty("msg", "Please enter the city.");
        } else if (district == 0) {
            responseObject.addProperty("msg", "Please select the district.");
        } else if (province.isEmpty()) {
            responseObject.addProperty("msg", "Please select the province.");
        } else {

            Session s = hibernate.HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = s.createCriteria(User.class);
            Criterion crt1 = Restrictions.eq("email", email);
            criteria.add(crt1);

            if (!criteria.list().isEmpty()) {
                responseObject.addProperty("msg", "Already a user with the same email address exists.");
            } else {

                ActiveStatus activeStatus = new ActiveStatus();
                activeStatus.setAid(1);
                activeStatus.setAstatus("Active");

                UserStatus userStatus = new UserStatus();
                userStatus.setUsid(1);
                userStatus.setUstatus("Client");

                String verificationCode = Util.generateCode();

                User user = new User();
                user.setFname(fname);
                user.setLname(lname);
                user.setMobile(mobile);
                user.setEmail(email);
                user.setPassword(password);
                user.setVerified("No");
                user.setVcode(verificationCode);
                user.setActiveStatus(activeStatus);
                user.setUserStatus(userStatus);
                s.save(user);

                Criteria c = s.createCriteria(City.class);
                Criterion cres1 = Restrictions.ilike("cname", city);
                c.add(cres1);

                City cityObject;

                if (c.list().isEmpty()) {

                    District dis = (District) s.load(District.class, district);

                    City addCity = new City();
                    addCity.setCname(city);
                    addCity.setDistrict(dis);
                    s.save(addCity);

                    cityObject = addCity;
                } else {
                    cityObject = (City) c.list().get(0);
                }

                Address address = new Address();
                address.setLine1(line1);
                address.setLine2(line2);
                address.setCity(cityObject);
                address.setUser(user);
                s.save(address);

                s.beginTransaction().commit();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Mail.sendMail(email, "Account Verification", "<h1>" + verificationCode + "</h1>");
                    }
                }).start();

                HttpSession webSession = request.getSession();
                webSession.setAttribute("user", user);
                Cookie cookie = new Cookie("JSESSIONID", webSession.getId());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setSecure(false);
                response.addCookie(cookie);

                responseObject.addProperty("status", true);
                responseObject.addProperty("msg", "Registered successfully. Please verify your email.");
            }

            s.close();

        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        System.out.println("1");

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("user") != null) {

            Session ses = HibernateUtil.getSessionFactory().openSession();

            UserStatus userStatus = new UserStatus();
            userStatus.setUsid(2);
            userStatus.setUstatus("Seller");

            User u = (User) session.getAttribute("user");

            Criteria criteria = ses.createCriteria(User.class);
            criteria.add(Restrictions.eq("userStatus", userStatus));
            criteria.add(Restrictions.eq("email", u.getEmail()));

            if (criteria.list().isEmpty()) {

                u.setSeller_joined_date(new Date());
                u.setUserStatus(userStatus);
                ses.update(u);
                ses.beginTransaction().commit();

                session.setAttribute("seller", u);
                session.setAttribute("user", u);
                Util.createSessionCookie(session.getId());

                responseObject.addProperty("status", Boolean.TRUE);

            } else {
                responseObject.addProperty("msg", "The user has already registered as a seller.");
            }

        } else {
            responseObject.addProperty("msg", "Please login as a user.");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
