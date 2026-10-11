# Railway deployment (H2 database)

This project is a Java 17 / Servlet 4 application packaged as a WAR and run on Tomcat 9. The Railway Docker setup builds the WAR and runs it under the `/aishwarya_mart` context.

## Deploy

1. Push/merge this deployment setup into `main`.
2. In Railway, create a project and choose **Deploy from GitHub Repo**, then select this repository and the `main` branch.
3. Railway should detect the root `Dockerfile`.
4. Add a **Volume** to the web service and mount it at `/data`. This is required so the H2 database survives container replacement/redeployment.
5. Add the optional `GEMINI_API_KEY` service variable if Boba's Gemini-powered open-ended replies are wanted. Keep the value secret; never commit it.
6. Generate a public domain in Railway's service networking settings.

## Database configuration

The startup script defaults to:

- `DB_URL=jdbc:h2:file:/data/aishwarya_mart_db;MODE=MySQL;AUTO_SERVER=TRUE`
- `DB_USERNAME=sa`
- `DB_PASSWORD=` (empty)

These match the application's H2 configuration. The database schema and seed scripts run when the web application starts. Do not point `DB_URL` at a different database unless intentionally migrating the project.

## First deployment checks

- Open `https://YOUR-DOMAIN/aishwarya_mart/`.
- Check `https://YOUR-DOMAIN/aishwarya_mart/api/v1/health`.
- Register a test buyer and seller; sign in; create a seller product; add it to the cart; place a mock order; confirm order history.
- Restart/redeploy the service and confirm those records still exist. If not, verify that the Railway volume is mounted at `/data`.
- Test Boba's product search and cart actions. Gemini-powered free-form replies require `GEMINI_API_KEY`.

Do not use real payment credentials; checkout is mock payment only.
