## 2024-05-22 - Input Validation Gaps in Client-Side Search
**Vulnerability:** Unbounded string processing in client-side search (`searchAirports`).
**Learning:** Client-side search functions processing user input synchronously can be vulnerable to DoS/ReDoS if input length is not validated, even without a backend.
**Prevention:** Always enforce reasonable length limits on all user inputs, even for local operations.

## 2024-05-23 - Insecure Deserialization of Local Data
**Vulnerability:** Trusted `JSON.parse` output from local storage without validation (`getHomeAirport`).
**Learning:** Even local storage can be corrupted or tampered with. Blindly casting parsed JSON to TypeScript types bypasses safety and can lead to runtime errors or undefined behavior.
**Prevention:** Implement runtime type guards (schema validation) for all data deserialized from storage or external sources before using it.
