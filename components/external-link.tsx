import { Href, Link } from 'expo-router';
import { openBrowserAsync, WebBrowserPresentationStyle } from 'expo-web-browser';
import { type ComponentProps } from 'react';

type Props = Omit<ComponentProps<typeof Link>, 'href'> & { href: Href & string };

/**
 * Render a Link that opens an external URL using the platform-appropriate browser.
 *
 * @param href - The external URL to open when the link is activated.
 * @returns A Link element configured to open `href` in a new tab on web or in an in-app browser on native platforms.
 */
export function ExternalLink({ href, ...rest }: Props) {
  const handlePress = async (event: any) => {
    if (process.env.EXPO_OS !== 'web') {
      // Prevent the default behavior of linking to the default browser on native.
      event.preventDefault();
      // Open the link in an in-app browser.
      await openBrowserAsync(href, {
        presentationStyle: WebBrowserPresentationStyle.AUTOMATIC,
      });
    }

    // Call any provided onPress handler passed in via `rest`.
    (rest as any).onPress?.(event);
  };

  return <Link target="_blank" {...rest} href={href} onPress={handlePress} />;
}