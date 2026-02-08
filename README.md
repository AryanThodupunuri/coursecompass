java -Dserver.port=$PORT -jar backend/target/backend-0.1.0-SNAPSHOT.jar
Update the extension API base URL in `extension/content.js` to:

- `https://<your-render-host>/api/v1/analyze`

## Load the Chrome Extension

1. Open Chrome → `chrome://extensions`
2. Enable **Developer mode**
3. Click **Load unpacked**
4. Select the `extension/` folder
5. Visit `https://louslist.org/` and you should see **Analyze 🔍** buttons injected.

## Notes

- RateMyProfessor GraphQL and Reddit parsing in `ScraperService` are intentionally lightweight “starter” implementations.
- Next step for Phase 3 is wiring Postgres config (`application.properties`) + a `JpaRepository` for `CourseCache` to actually cache results.
