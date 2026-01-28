/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.ActiveStatus;
import hibernate.Address;
import hibernate.BatchDetails;
import hibernate.Cart;
import hibernate.DeliveryCost;
import hibernate.DeliveryStatus;
import hibernate.HibernateUtil;
import hibernate.Invoice;
import hibernate.InvoiceItems;
import hibernate.Product;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.PayHere;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author hp
 */
@WebServlet(name = "CartCheckout", urlPatterns = {"/CartCheckout"})
public class CartCheckout extends HttpServlet {

    private static final int ACTIVE_STATUS = 1;
    private static final int INACTIVE_STATUS = 2;
    private static final int PENDING_STATUS = 1;

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

        if (session != null && session.getAttribute("user") != null) {

            User user = (User) session.getAttribute("user");

            JsonObject requestObject = gson.fromJson(request.getReader(), JsonObject.class);
            String selectedAddress = requestObject.get("selectedAddress").getAsString();
            String couponDiscount = requestObject.get("couponDiscount").getAsString();
//            String couponCode = requestObject.get("couponCode").getAsString();

            if (selectedAddress.isEmpty()) {
                responseObject.addProperty("msg", "Please selecct an address.");
            } else if (!Util.checkInteger(selectedAddress)) {
                responseObject.addProperty("msg", "Invalid address.");
            } else {

                Session ses = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = ses.beginTransaction();

                Criteria c1 = ses.createCriteria(Address.class);
                c1.add(Restrictions.eq("user", user));
                c1.add(Restrictions.eq("aid", Integer.parseInt(selectedAddress)));

                if (!c1.list().isEmpty()) {
                    try {

                        Address address = (Address) c1.uniqueResult();
                        ActiveStatus activeStatus = (ActiveStatus) ses.get(ActiveStatus.class, CartCheckout.ACTIVE_STATUS);

                        Criteria c2 = ses.createCriteria(DeliveryCost.class);
                        c2.add(Restrictions.eq("district", address.getCity().getDistrict()));
                        c2.add(Restrictions.eq("activeStatus", activeStatus));
                        DeliveryCost deliveryCost = (DeliveryCost) c2.uniqueResult();

                        DeliveryStatus deliveryStatus = (DeliveryStatus) ses.get(DeliveryStatus.class, CartCheckout.PENDING_STATUS);

                        String invoiceNo = Util.generateInvoiceId();

                        Invoice invoice = new Invoice();
                        invoice.setInvoice_no(invoiceNo);
                        invoice.setDate_time(new Date());
                        invoice.setUser(user);
                        invoice.setDeliveryCost(deliveryCost);
                        invoice.setDeliveryStatus(deliveryStatus);
                        ses.save(invoice);

                        Criteria c3 = ses.createCriteria(Cart.class);
                        c3.add(Restrictions.eq("user", user));
                        List<Cart> cartItemList = c3.list();

                        double amount = 0;
                        String items = "";

                        amount += deliveryCost.getPrice();

                        for (Cart cart : cartItemList) {
                            amount += cart.getQty() * cart.getBatchDetails().getPrice();
                            items += cart.getProduct().getTitle() + " x " + cart.getQty() + ", ";

                            BatchDetails batchDetails = cart.getBatchDetails();

                            InvoiceItems invoiceItems = new InvoiceItems();
                            invoiceItems.setQty(cart.getQty());
                            invoiceItems.setProduct(cart.getProduct());
                            invoiceItems.setColor(cart.getColor());
                            invoiceItems.setBatchDetails(batchDetails);
                            invoiceItems.setInvoice(invoice);
                            ses.save(invoiceItems);

                            batchDetails.setQty(batchDetails.getQty() - cart.getQty());
                            ses.update(batchDetails);

                            ses.delete(cart);
                        }

                        transaction.commit();

                        //PayHere process
                        String merchantID = "1224658";
                        String merchantSecret = "NDE2MDIwMTY2MzA4MjkxMTMxMTE4NjAwOTk0NjM1NDY5NTk2MjY=";
                        String orderID = invoiceNo;
                        String currency = "LKR";
                        String formattedAmount = new DecimalFormat("0.00").format(amount);
                        String merchantSecretMD5 = PayHere.generateMD5(merchantSecret);
                        String fullString = merchantID + orderID + formattedAmount + currency + merchantSecretMD5;

                        String hash = PayHere.generateMD5(fullString);

                        System.out.println("---- HASH DEBUG ----");
                        System.out.println("merchant_id: '" + merchantID + "'");
                        System.out.println("order_id: '" + orderID + "'");
                        System.out.println("amount: '" + formattedAmount + "'");
                        System.out.println("currency: '" + currency + "'");
                        System.out.println("md5(secret): '" + merchantSecretMD5 + "'");
                        System.out.println("full string: '" + fullString + "'");
                        System.out.println("MD5(full string): '" + PayHere.generateMD5(fullString) + "'");
                        System.out.println("--------------------");

                        JsonObject payHereJson = new JsonObject();
                        payHereJson.addProperty("sandbox", true);
                        payHereJson.addProperty("merchant_id", merchantID);

                        payHereJson.addProperty("return_url", "https://55954bec92ee.ngrok-free.app/Smarttrade/VerifyPayments");
                        payHereJson.addProperty("cancel_url", "https://55954bec92ee.ngrok-free.app/Smarttrade/VerifyPayments");
                        payHereJson.addProperty("notify_url", "https://55954bec92ee.ngrok-free.app/Smarttrade/VerifyPayments");

                        payHereJson.addProperty("order_id", orderID);
                        payHereJson.addProperty("items", items);
                        payHereJson.addProperty("amount", formattedAmount);
                        payHereJson.addProperty("currency", currency);
                        payHereJson.addProperty("hash", hash);

                        payHereJson.addProperty("first_name", user.getFname());
                        payHereJson.addProperty("last_name", user.getLname());
                        payHereJson.addProperty("email", user.getEmail());

                        payHereJson.addProperty("phone", user.getMobile());
                        payHereJson.addProperty("address", address.getLine1() + ", " + address.getLine2());
                        payHereJson.addProperty("city", address.getCity().getCname());
                        payHereJson.addProperty("country", "Sri Lanka");

                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", "Checkout completed");
                        responseObject.add("payhereJson", new Gson().toJsonTree(payHereJson));

                    } catch (Exception e) {
                        transaction.rollback();
                    }

                } else {
                    responseObject.addProperty("msg", "Selected shipping address not found.");
                }
            }

        } else {
            responseObject.addProperty("msg", "1");
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
