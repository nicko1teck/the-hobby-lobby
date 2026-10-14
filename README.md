# The Hobby Lobby

A social web app for sharing hobbies. Users register (with email verification), build a profile with photos, post status updates, search for people, and chat.

Built with Spring Boot 2.4, Spring Security, JSP with Apache Tiles for views, MySQL, WebSocket chat, Cloudinary for image storage, and Gmail SMTP for email.

## Compatibility note

This project uses the `javax.*` servlet namespace (Spring Boot 2.x), so it runs on **Tomcat 9**. It will not deploy on Tomcat 10 or newer, which use `jakarta.*`.

## Configuration

Secrets and environment-specific values are **not** stored in the code. `application.properties` reads them from environment variables:

| Variable | Required | Purpose |
| --- | --- | --- |
| `DB_USERNAME` | Yes | MySQL user |
| `DB_PASSWORD` | Yes | MySQL password |
| `DB_URL` | No | JDBC URL. Defaults to `jdbc:mysql://localhost:3306/springboottutorial?...` |
| `CLOUDINARY_CLOUD_NAME` | Yes | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Yes | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Yes | Cloudinary API secret |
| `MAIL_USERNAME` | Yes | Gmail address used to send email |
| `MAIL_PASSWORD` | Yes | Gmail **app password** |
| `SITE_URL` | No | Public address of the app, used in verification-email links. Defaults to `http://localhost:8080/the-hobby-lobby` |

The app will not start if a required variable is missing.

## Run locally

1. Create a MySQL database and user for the app.
2. Provide the variables above, either:
   - as environment variables in your IDE's run configuration, or
   - by copying `src/main/resources/application-dev.properties.example` to `application-dev.properties` (git-ignored), filling it in, and running with `--spring.profiles.active=dev`.
3. Start the app from your IDE, or build and deploy the WAR to a local Tomcat 9.

## Build

```
mvn -DskipTests clean package
```

The WAR is created in `target/`. Skipping tests is recommended: the tests are `@SpringBootTest` tests that connect to the configured database, and one of them creates sample users. Only run them against a throwaway database.

## Deploy to Tomcat 9 (EC2)

1. Copy `setenv.sh.example` to `$CATALINA_BASE/bin/setenv.sh` on the server, fill in the real values, and run `chmod 600` on it.
2. Copy the WAR into Tomcat's `webapps/` folder and name it `the-hobby-lobby.war`. The file name sets the URL path, so the app is served at `/the-hobby-lobby/`.
3. Make sure MySQL is running and reachable before Tomcat starts. The app cannot start without its database.
4. Restart Tomcat and watch `logs/catalina.out` until deployment finishes.

## Notes

- Never commit `setenv.sh`, `application-dev.properties`, or any `.env` file. All are git-ignored.
- The Gmail account must use an app password (requires 2-step verification), not its normal password.
