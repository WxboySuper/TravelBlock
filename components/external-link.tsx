import { Href, Link } from 'expo-router';
import { openBrowserAsync, WebBrowserPresentationStyle } from 'expo-web-browser';
import type { ComponentProps, MouseEvent } from 'react';
import type { GestureResponderEvent } from 'react-native';
import { useCallback } from 'react';

type Props = Omit<ComponentProps<typeof Link>, 'href'> & { href: Href & string };

/**
 * Render a Link that opens an external URL using the platform-appropriate browser.
 *
 * @param href - The external URL to open when the link is activated.
 * @returns A Link element configured to open `href` in a new tab on web or in an in-app browser on native platforms.
 */
export function ExternalLink({ href, onPress, ...rest }: Props) {
  type LinkOnPress = ComponentProps<typeof Link>['onPress'];

  const handlePress = useCallback(
    async (event: MouseEvent<HTMLAnchorElement> | GestureResponderEvent) => {
      if (process.env.EXPO_OS !== 'web') {
        // Prevent the default behavior of linking to the default browser on native/web.
        if (
          'preventDefault' in (event as MouseEvent<HTMLAnchorElement>) &&
          typeof (event as MouseEvent<HTMLAnchorElement>).preventDefault === 'function'
        ) {
          (event as MouseEvent<HTMLAnchorElement>).preventDefault();
        }

        // Open the link in an in-app browser.
        await openBrowserAsync(href, {
          presentationStyle: WebBrowserPresentationStyle.AUTOMATIC,
        });
      }

      // Call any provided onPress handler passed in via the component props.
      const linkOnPress = onPress as LinkOnPress | undefined;
      linkOnPress?.(event as Parameters<NonNullable<LinkOnPress>>[0]);
    },
    [href, onPress],
  );

  return <Link target="_blank" {...rest} href={href} onPress={handlePress} />;
}