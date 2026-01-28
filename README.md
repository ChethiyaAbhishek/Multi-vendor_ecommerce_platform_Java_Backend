# Men's Fashion Hub - Multi-Seller E-Commerce Platform

A fully functional multi-vendor online marketplace specialized in men's clothing and shoes. Vendors can manage their own products, customers can browse and purchase items with advanced search and cart features, and administrators oversee the entire platform.

## Features

### Vendor Features
- Register and manage products (add/update items, batches with colors/sizes, brands, and subcategories pending admin approval)
- Handle orders up to "pending" and "processing" states
- View customer reviews

### Admin Features
- Activate/deactivate sellers and products
- Approve subcategories
- Manage full order lifecycle (including "shipped" and "delivered")
- View seller statistics and oversee all orders

### Customer Features
- Home page with trending items (by sales), new arrivals, top-rated products, and category browsing
- Basic and advanced search (by category, subcategory, size, color, price) with sorting options
- Wishlist (login required) and cart (session-based without login, persisted with login)
- Product pages with images, descriptions, stock status, reviews, and related items
- Checkout with address selection, coupon application, district-based delivery fees, and PayHere payment integration
- Order tracking, invoice viewing/printing
- Email notifications for out-of-stock items

### General Features
- User registration/login (customer or vendor) with email verification and password recovery
- Profile management (name, mobile, addresses - max 2 shipping addresses)
- Coupon system (5-20% discounts for orders above certain thresholds, emailed to customers)
- Pagination and image uploading

## Technologies Used
- **Backend**: Java EE 7 (Servlets, JSP)
- **ORM**: Hibernate
- **Application Server**: GlassFish
- **Database**: MySQL
- **Payment Gateway**: PayHere (Sri Lanka)
- **Frontend**: HTML, CSS, JavaScript (served via JSP/Servlets)

## Setup Instructions

1. **Prerequisites**
   - Java JDK 8 or higher
   - GlassFish Server (tested on GlassFish 4/5)
   - MySQL Server
   - PayHere merchant account (for live payments; use sandbox for testing)

2. **Database Setup**
   - Create a MySQL database (e.g., `mens_fashion_hub`)
   - Run the schema scripts generated via Hibernate (or import the provided dump if available)
   - Update Hibernate configuration (`hibernate.cfg.xml`) with your database credentials

3. **Deployment**
   - Build the project as a WAR file
   - Deploy the WAR to GlassFish via admin console or `asadmin` command
   - Start GlassFish server

4. **Configuration**
   - Configure PayHere credentials in the payment integration module
   - Set up email service for verification and notifications

5. **Run**
   - Access the application at `http://localhost:8080/[your-war-name]/`

Note: The application is designed for local/development deployment. For production, deploy to a cloud server and configure proper domain/SSL.

## Limitations
- Currently runs on localhost/GlassFish (no cloud deployment included)
- Payment limited to PayHere gateway
- Maximum 2 shipping addresses per customer
- Web-only (no mobile app)
- Basic coupon and email features

## Future Enhancements
- Social media login integration
- Mobile app development
- Advanced analytics and sales reports
- Additional payment gateways and international shipping
- AI-based product recommendations
- Multi-language and currency support
