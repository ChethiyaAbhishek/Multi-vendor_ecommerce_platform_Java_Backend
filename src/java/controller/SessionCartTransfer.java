/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "SessionCartTransfer", urlPatterns = {"/SessionCartTransfer"})
public class SessionCartTransfer extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
                        
        if (request.getSession() != null && request.getSession().getAttribute("user") != null) {
                        
            User user = (User) request.getSession().getAttribute("user");
            
            if (request.getSession().getAttribute("cart") != null) {
                ArrayList<Cart> cart = (ArrayList<Cart>) request.getSession().getAttribute("cart");
                
                Session ses = HibernateUtil.getSessionFactory().openSession();
                
                Criteria criteria = ses.createCriteria(Cart.class);
                criteria.add(Restrictions.eq("user", user));
                                
                for (Cart cartItem : cart) {
                                        
                    criteria.add(Restrictions.eq("product", cartItem.getProduct()));
                    criteria.add(Restrictions.eq("batchDetails", cartItem.getBatchDetails()));
                    criteria.add(Restrictions.eq("color", cartItem.getColor()));
                    
                    if (criteria.list().isEmpty()) {
                        cartItem.setUser(user);
                        ses.save(cartItem);
                    } else {
                        Cart existingItem = (Cart) criteria.list().get(0);
                        existingItem.setQty(existingItem.getQty() + cartItem.getQty());
                        ses.update(existingItem);
                    }
                    
                    ses.beginTransaction().commit();
                }
                
                request.getSession().removeAttribute("cart");                
                ses.close();
            }            
        }
                
    }
    
}
