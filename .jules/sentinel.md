## 2024-05-22 - Input Validation Gaps in Client-Side Search
**Vulnerability:** Unbounded string processing in client-side search (`searchAirports`).
**Learning:** Client-side search functions processing user input synchronously can be vulnerable to DoS/ReDoS if input length is not validated, even without a backend.
**Prevention:** Always enforce reasonable length limits on all user inputs, even for local operations.
