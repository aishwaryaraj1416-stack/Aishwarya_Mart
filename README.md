# Aishwarya Mart

A Java Servlet-based multi-seller e-commerce marketplace developed as part of the Anna University R2025 Semester 3 project.

## Live Deployment

- **Application:** https://aishwarya-mart-production.up.railway.app/aishwarya_mart/
- **Hosting:** Railway
- **Runtime:** Apache Tomcat 9 on Java 17 (Docker)
- **Database:** H2 file database stored on a Railway persistent volume

The live application has been deployed and manually checked. The database URL is configured on Railway through the `DB_URL` environment variable. The persistent volume is mounted at `/data` so the H2 database can survive container redeployments.

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
- Docker
- Railway

## Project Structure

`src/main/java/com/aishwarya/aishwarya_mart/`
- `controller/`
- `dao/`
- `dto/`
- `filter/`
- `listener/`
- `model/`
- `service/`
- `util/`

`src/main/resources/`
- `db.properties`
- `schema.sql`
- `seed.sql`
- `db/migrations/`

`src/main/webapp/`
- `login.html`
- `register.html`
- `index.html`
- `seller.html`
- `cart.html`
- `checkout.html`
- `orders.html`
- `admin.html`
- `account.html`
- `wishlist.html`

## Running Locally

### Requirements

- JDK 17
- Maven 3.9+
- Apache Tomcat 9

### Build and test

From the repository root, run:

```bash
mvn clean verify
```

The generated WAR file will be available at:

```text
target/aishwarya_mart.war
```

Deploy the WAR to Apache Tomcat 9 and start the server. The default development database configuration is in `src/main/resources/db.properties`; it uses a local H2 file database under the project's `data` directory.

### Docker

The root-level `Dockerfile` builds the WAR with Maven and runs it on Tomcat 9 with Java 17. To build and run locally:

```bash
docker build -t aishwarya-mart .
docker run --rm -p 8080:8080 aishwarya-mart
```

Open `http://localhost:8080/aishwarya_mart/`.

For a persistent H2 database in a container, mount a persistent directory and configure `DB_URL` to point to an H2 file inside that directory. Do not use a temporary container filesystem for production database storage.

## Test Accounts

Development accounts are provided for testing different roles (where seeded in the local database):

- Admin: `admin@aishwaryamart.com`
- Seller: `seller@aishwaryamart.com`
- Buyer: `buyer@aishwaryamart.com`

Use the corresponding development passwords configured for your local seed data. Do not publish real or production passwords in this README.

## API Endpoints

- `/auth`
- `/products`
- `/cart`
- `/orders`
- `/reviews`
- `/wishlist`
- `/admin`
- `/api/v1/health`

The health endpoint can be checked at:

`https://aishwarya-mart-production.up.railway.app/aishwarya_mart/api/v1/health`

## Security

- BCrypt password hashing
- Session-based authentication
- Role-based authorization
- Authentication filter
- Parameterized SQL queries
- Database credentials excluded from version control
- Input validation for important request fields

Never commit real credentials, production passwords, database files, or secret environment-variable values.

## CI

The project is intended to be validated with:

```bash
mvn -B clean verify
```

Run this command before pushing changes and confirm the build and tests pass.

## Project Status

The application is deployed on Railway and the main buyer, seller, and admin flows have been manually checked. Checkout is a mock/demo payment flow; it does not process real payments.
