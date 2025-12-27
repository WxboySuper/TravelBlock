// https://docs.expo.dev/guides/using-eslint/
const { defineConfig } = require('eslint/config');
const expoConfig = require('eslint-config-expo/flat');

module.exports = defineConfig([
  expoConfig,
  {
    ignores: ['dist/*'],
    // Plugins are provided by the extended Expo config; avoid redefining them here.
    settings: {
      react: {
        version: 'detect',
      },
    },
    rules: {
      // New JSX transform doesn't require React in scope
      'react/jsx-uses-react': 'off',
      'react/react-in-jsx-scope': 'off',
      // Enforce hooks rules
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
    },
  },
]);
