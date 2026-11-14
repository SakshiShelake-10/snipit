# SnipIt

URL shortener backend with analytics. Built with Spring Boot, PostgreSQL and JWT.

## Features
- Register / login with JWT authentication
- Short links with custom alias, expiry date and click limit
- Enable/disable, edit and delete links (each user sees only their own)
- Password-protected links (BCrypt)
- Click analytics: clicks per day, browser, device, referrer
- QR code for every short link

## Run locally
1. Install Java 21 and PostgreSQL, create a database named `snipit`.
2. Set environment variables:
    - `DB_PASSWORD` (your PostgreSQL password)
    - `JWT_SECRET` (random string, at least 48 characters)
3. Run `SnipitApplication`. The API starts on http://localhost:8080.

## Status
Backend complete. React frontend and deployment in progress.