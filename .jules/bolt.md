## 2024-05-24 - Search Optimization
**Learning:** Returning large arrays (28k+) from search operations blocks the React Native JS thread during serialization/mapping, even if the UI uses `FlatList` virtualization.
**Action:** Always implement a hard limit on search results (e.g. 100-500) at the service level to prevent UI freezes on broad queries.
