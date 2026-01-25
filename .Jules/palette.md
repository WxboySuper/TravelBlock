## 2024-05-23 - Interactive Feedback Consistency
**Learning:** Inconsistent feedback mechanisms (e.g., some buttons having haptics/opacity changes while others don't) make the app feel unpolished and can confuse users about what is interactive.
**Action:** Always ensure interactive elements (buttons, pressables) provide immediate visual and haptic feedback. Use `TouchableOpacity` or proper `Pressable` styles with `expo-haptics` to match the native feel of the platform.
