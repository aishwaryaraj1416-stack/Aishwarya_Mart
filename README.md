# Aishwarya Mart

A Java Servlet-based multi-seller e-commerce marketplace developed as part of the Anna University R2025 Semester 3 project.

## Features

- User registration and login
- Buyer and Seller roles
- Admin management
- Seller product management
- Product search and category filtering
- Shopping cart
- Mock checkout and order placement
- Buyer order history
- Seller incoming orders
- Order status management
- Product reviews and star ratings
- Wishlist
- Health endpoint

## Technology Stack

- Java 17
- Java Servlets
- Apache Tomcat 9
- Maven
- H2 Database
- HikariCP
- BCrypt
- Gson
- HTML, CSS and JavaScript
- JUnit 5 and Mockito
- GitHub Actions

## Project Structure

src/main/java/com/aishwarya/aishwarya_mart/
- controller
- dao
- dto
- filter
- listener
- model
- service
- util

src/main/resources/
- db.properties
- schema.sql
- seed.sql
- db/migrations/

src/main/webapp/
- login.html
- register.html
- index.html
- seller.html
- cart.html
- checkout.html
- orders.html
- admin.html
- account.html
- wishlist.html

## Running Locally

### Requirements

- JDK 17
- Maven 3.9+
- Apache Tomcat 9

### Build

mvn clean verify

The generated WAR file will be available at:

target/aishwarya_mart.war

Deploy the WAR to Apache Tomcat and start the server.

## Test Accounts

Development accounts are provided for testing different roles:

- Admin: admin@aishwaryamart.com
- Seller: seller@aishwaryamart.com
- Buyer: buyer@aishwaryamart.com

## API Endpoints

- /auth
- /products
- /cart
- /orders
- /reviews
- /wishlist
- /admin
- /api/v1/health

## Security

- BCrypt password hashing
- Session-based authentication
- Role-based authorization
- Authentication filter
- Parameterized SQL queries
- Database credentials excluded from version control
- Input validation for important request fields

## CI

GitHub Actions runs:

mvn -B clean verify

on pushes and pull requests to the main branch.

## Project Status

Core marketplace requirements F1-F8 have been implemented and manually tested.

Deployment is planned as a later project stage.
