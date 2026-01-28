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
import java.util.List;
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
@WebServlet(name = "SaveShippingAddress", urlPatterns = {"/SaveShippingAddress"})
public class SaveShippingAddress extends HttpServlet {

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

            User user = (User) webSession.getAttribute("user");

            JsonObject addressDetails = gson.fromJson(request.getReader(), JsonObject.class);

            String line1 = addressDetails.get("line1").getAsString();
            String line2 = addressDetails.get("line2").getAsString();
            String city = addressDetails.get("city").getAsString();
            int district = addressDetails.get("district").getAsInt();

            if (line1.isEmpty()) {
                responseObject.addProperty("msg", "Address line 01 cannot be empty.");
            } else if (line2.isEmpty()) {
                responseObject.addProperty("msg", "Address line 02 cannot be empty.");
            } else if (city.isEmpty()) {
                responseObject.addProperty("msg", "City is required.");
            } else if (district == 0) {
                responseObject.addProperty("msg", "Please select the district.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria checkAddress = ses.createCriteria(Address.class);
                checkAddress.add(Restrictions.eq("user", user));

                List<Address> add = checkAddress.list();

                boolean saveAddress = false;

                if (add.size() == 2) {
                    responseObject.addProperty("msg", "You have reached the max number of shipping addresses. Try editing an existing address.");
                } else if (add.size() < 3) {
                    for (Address address : add) {
                        if (!address.getLine1().equals(line1)
                                || !address.getLine2().equals(line2)
                                || !address.getCity().getCname().equals(city)
                                || address.getCity().getDistrict().getDis_id() != district) {
                            saveAddress = true;
                        } else {
                            saveAddress = false;
                        }
                    }
                }

                if (saveAddress) {

                    District dis = (District) ses.load(District.class, district);

                    Criteria crt = ses.createCriteria(City.class);
                    crt.add(Restrictions.ilike("cname", city));
//                crt.add(Restrictions.eq("district", dis));

                    City c;

                    if (crt.list().isEmpty()) {

                        c = new City();
                        c.setCname(city);
                        c.setDistrict(dis);
                        ses.save(c);

                    } else {
                        c = (City) crt.list().get(0);
                    }

                    Address address = new Address();
                    address.setLine1(line1);
                    address.setLine2(line2);
                    address.setCity(c);
                    address.setUser(user);
                    ses.save(address);

                    ses.beginTransaction().commit();
                    responseObject.addProperty("status", true);

                } else {
                    responseObject.addProperty("msg", "The entered address already exists.");
                }

                ses.close();
            }

        }

        response.getWriter().write(gson.toJson(responseObject));
        response.setContentType("application/json");

    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        Gson gson = new Gson();

        HttpSession webSession = request.getSession(false);

        if (webSession != null && webSession.getAttribute("user") != null) {

            User user = (User) webSession.getAttribute("user");

            JsonObject addressDetails = gson.fromJson(request.getReader(), JsonObject.class);

            String line1 = addressDetails.get("line1").getAsString();
            String line2 = addressDetails.get("line2").getAsString();
            String city = addressDetails.get("city").getAsString();
            int district = addressDetails.get("district").getAsInt();

            if (line1.isEmpty()) {
                responseObject.addProperty("msg", "Address line 01 cannot be empty.");
            } else if (line2.isEmpty()) {
                responseObject.addProperty("msg", "Address line 02 cannot be empty.");
            } else if (city.isEmpty()) {
                responseObject.addProperty("msg", "City is required.");
            } else if (district == 0) {
                responseObject.addProperty("msg", "Please select the district.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();

                Criteria checkAddress = ses.createCriteria(Address.class);
                checkAddress.add(Restrictions.eq("user", user));

                Address ship1 = (Address) checkAddress.list().get(0);
                Address ship2 = (Address) checkAddress.list().get(1);

                boolean saveAddress = false;

                if (ship1.getLine1().equals(line1)
                        && ship1.getLine2().equals(line2)
                        && ship1.getCity().getCname().equals(city)
                        && ship1.getCity().getDistrict().getDis_id() == district) {
                    responseObject.addProperty("msg", "The entered address already exists.");
                } else if (ship2.getLine1().equals(line1)
                        && ship2.getLine2().equals(line2)
                        && ship2.getCity().getCname().equals(city)
                        && ship2.getCity().getDistrict().getDis_id() == district) {
                    responseObject.addProperty("status", true);
                }else{                    
                    
                    District dis = (District) ses.load(District.class, district);

                    Criteria crt = ses.createCriteria(City.class);
                    crt.add(Restrictions.ilike("cname", city));
//                crt.add(Restrictions.eq("district", dis));

                    City c;

                    if (crt.list().isEmpty()) {

                        c = new City();
                        c.setCname(city);
                        c.setDistrict(dis);
                        ses.save(c);

                    } else {
                        c = (City) crt.list().get(0);
                    }

                    ship2.setLine1(line1);
                    ship2.setLine2(line2);
                    ship2.setCity(c);
                    ship2.setUser(user);
                    ses.update(ship2);

                    ses.beginTransaction().commit();
                    responseObject.addProperty("status", true);

                    
                }

                ses.close();
            }

        }

        response.getWriter().write(gson.toJson(responseObject));
        response.setContentType("application/json");

    }

}
