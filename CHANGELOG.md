# Changelog

All notable changes to this project are listed below.

## v0.1.0-alpha — The Hello World (Expo)

- Installed Node.js & Expo CLI.
- Expo project initialized
- Initial platform setup and verification completed (v0.1.0-alpha).
- Added GitHub templates and contribution guide:
  - .github/PULL_REQUEST_TEMPLATE.md
  - .github/ISSUE_TEMPLATE/bug_report.md
  - .github/ISSUE_TEMPLATE/feature_request.md
  - .github/CONTRIBUTING.md

## v0.2.0-alpha — Teaching the app geography

- Implemented core distance utilities (Haversine) for accurate great-circle distance calculations (miles / km).
- Added Airport data layer and `AirportService`: lazy-loading of `airports.json`, search by name/city/ICAO/IATA, distance-based filtering and caching.
- Built the Select Airport modal UI and list components with debounced search, themed styling, and accessibility improvements.
- Integrated the modal with `AirportService`: real-time search results, distance display, and selection callbacks.
- Added comprehensive unit tests for utilities and services with high coverage; CI-friendly test mocks for native modules.
- Introduced an in-repo KV-style persistence shim for development/tests (in-memory fallback) to avoid native module runtime issues in Expo Go.
- Performance and UX improvements: virtualized lists for large results, memoized calculations, optimistic save with revert and user feedback.

See `docs/v0.2.0-alpha/implementation-plan.md` for details and follow-up items.
