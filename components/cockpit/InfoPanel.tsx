import { AppIcon } from '@/components/ui/AppIcon';
import { BorderRadius, Colors, Spacing, Typography } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import type { FlightBooking } from '@/types/flight';
import { ScrollView, StyleSheet, Text, View, type TextStyle } from 'react-native';

export interface InfoPanelProps {
  booking: FlightBooking;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    gap: Spacing.md,
    padding: Spacing.md,
    paddingBottom: Spacing.xl,
  },
  heroCard: {
    borderRadius: BorderRadius.xl,
    borderWidth: 1,
    padding: Spacing.lg,
  },
  eyebrow: {
    fontSize: Typography.fontSize.xs,
    fontWeight: Typography.fontWeight.semibold as TextStyle['fontWeight'],
    letterSpacing: 1.1,
    marginBottom: Spacing.md,
    textTransform: 'uppercase',
  },
  routeRow: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  routeSide: {
    flex: 1,
  },
  routeSideRight: {
    alignItems: 'flex-end',
    flex: 1,
  },
  routeCode: {
    fontSize: 34,
    fontWeight: Typography.fontWeight.bold as TextStyle['fontWeight'],
    letterSpacing: -1,
  },
  routeCity: {
    fontSize: Typography.fontSize.lg,
    marginTop: 4,
  },
  routeAirport: {
    fontSize: Typography.fontSize.xs,
    marginTop: 4,
  },
  routeBadge: {
    alignItems: 'center',
    borderRadius: BorderRadius.full,
    height: 52,
    justifyContent: 'center',
    width: 52,
  },
  routeMetaRow: {
    flexDirection: 'row',
    gap: Spacing.sm,
    marginTop: Spacing.lg,
  },
  metaPill: {
    borderRadius: BorderRadius.full,
    borderWidth: 1,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  metaPillText: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.medium as TextStyle['fontWeight'],
  },
  sectionCard: {
    borderRadius: BorderRadius.xl,
    borderWidth: 1,
    overflow: 'hidden',
  },
  sectionHeader: {
    alignItems: 'center',
    flexDirection: 'row',
    gap: Spacing.sm,
    paddingHorizontal: Spacing.md,
    paddingTop: Spacing.md,
  },
  sectionTitle: {
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold as TextStyle['fontWeight'],
    letterSpacing: 0.6,
    textTransform: 'uppercase',
  },
  sectionBody: {
    padding: Spacing.md,
    paddingTop: Spacing.sm,
  },
  infoRow: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between',
    minHeight: 42,
  },
  infoRowDivider: {
    borderBottomWidth: 1,
  },
  infoLabel: {
    fontSize: Typography.fontSize.sm,
  },
  infoValue: {
    flexShrink: 1,
    fontSize: Typography.fontSize.sm,
    fontWeight: Typography.fontWeight.semibold as TextStyle['fontWeight'],
    marginLeft: Spacing.md,
    textAlign: 'right',
  },
});

function formatDateTime(isoString: string): string {
  const date = new Date(isoString);
  return date.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  });
}

function InfoRow({
  isLast = false,
  label,
  mutedColor,
  value,
  valueColor,
}: {
  isLast?: boolean;
  label: string;
  mutedColor: string;
  value: string;
  valueColor: string;
}) {
  return (
    <View style={[styles.infoRow, !isLast ? [styles.infoRowDivider, { borderBottomColor: 'rgba(148, 163, 184, 0.12)' }] : null]}>
      <Text style={[styles.infoLabel, { color: mutedColor }]}>{label}</Text>
      <Text style={[styles.infoValue, { color: valueColor }]}>{value}</Text>
    </View>
  );
}

function InfoSection({
  children,
  colors,
  icon,
  title,
}: {
  children: React.ReactNode;
  colors: typeof Colors.light;
  icon: React.ComponentProps<typeof AppIcon>['name'];
  title: string;
}) {
  return (
    <View style={[styles.sectionCard, { backgroundColor: colors.cockpitSurface, borderColor: colors.cockpitBorder }]}>
      <View style={styles.sectionHeader}>
        <AppIcon color={colors.cockpitAccent} name={icon} size={18} />
        <Text style={[styles.sectionTitle, { color: '#FFFFFF' }]}>{title}</Text>
      </View>
      <View style={styles.sectionBody}>{children}</View>
    </View>
  );
}

function RouteHero({ booking, colors }: { booking: FlightBooking; colors: typeof Colors.light }) {
  return (
    <View
      style={[
        styles.heroCard,
        {
          backgroundColor: colors.cockpitSurface,
          borderColor: colors.cockpitBorder,
        },
      ]}
    >
      <Text style={[styles.eyebrow, { color: colors.cockpitTextSecondary }]}>Flight Route</Text>

      <View style={styles.routeRow}>
        <View style={styles.routeSide}>
          <Text style={[styles.routeCode, { color: '#FFFFFF' }]}>{booking.origin.iata}</Text>
          <Text style={[styles.routeCity, { color: '#FFFFFF' }]}>{booking.origin.city}</Text>
          <Text style={[styles.routeAirport, { color: colors.cockpitTextSecondary }]}>{booking.origin.name}</Text>
        </View>

        <View style={[styles.routeBadge, { backgroundColor: colors.cockpitAccentSoft }]}>
          <AppIcon color={colors.cockpitAccent} name="aircraft" size={22} style={{ transform: [{ rotate: '90deg' }] }} />
        </View>

        <View style={styles.routeSideRight}>
          <Text style={[styles.routeCode, { color: '#FFFFFF' }]}>{booking.destination.iata}</Text>
          <Text style={[styles.routeCity, { color: '#FFFFFF' }]}>{booking.destination.city}</Text>
          <Text style={[styles.routeAirport, { color: colors.cockpitTextSecondary }]}>{booking.destination.name}</Text>
        </View>
      </View>

      <View style={styles.routeMetaRow}>
        <View style={[styles.metaPill, { backgroundColor: colors.cockpitGlass, borderColor: colors.cockpitBorder }]}>
          <Text style={[styles.metaPillText, { color: '#FFFFFF' }]}>{booking.flightNumber}</Text>
        </View>
        <View style={[styles.metaPill, { backgroundColor: colors.cockpitGlass, borderColor: colors.cockpitBorder }]}>
          <Text style={[styles.metaPillText, { color: '#FFFFFF' }]}>{booking.aircraft.name}</Text>
        </View>
      </View>
    </View>
  );
}

export function InfoPanel({ booking }: InfoPanelProps) {
  const colorScheme = useColorScheme();
  const colors = Colors[colorScheme ?? 'light'];

  return (
    <ScrollView
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
      style={styles.container}
    >
      <RouteHero booking={booking} colors={colors} />

      <InfoSection colors={colors} icon="info" title="Flight Details">
          <InfoRow label="Flight Number" mutedColor={colors.cockpitTextSecondary} value={booking.flightNumber} valueColor="#FFFFFF" />
          <InfoRow label="Aircraft" mutedColor={colors.cockpitTextSecondary} value={booking.aircraft.name} valueColor="#FFFFFF" />
          <InfoRow label="Distance" mutedColor={colors.cockpitTextSecondary} value={`${Math.round(booking.distanceKm * 0.621371)} mi`} valueColor="#FFFFFF" />
          <InfoRow label="Gate" mutedColor={colors.cockpitTextSecondary} value={booking.gate} valueColor="#FFFFFF" />
          <InfoRow isLast label="Terminal" mutedColor={colors.cockpitTextSecondary} value={booking.terminal} valueColor="#FFFFFF" />
      </InfoSection>

      <InfoSection colors={colors} icon="time" title="Schedule">
          <InfoRow label="Boarding" mutedColor={colors.cockpitTextSecondary} value={formatDateTime(booking.boardingTime)} valueColor="#FFFFFF" />
          <InfoRow label="Departure" mutedColor={colors.cockpitTextSecondary} value={formatDateTime(booking.departureTime)} valueColor="#FFFFFF" />
          <InfoRow isLast label="Arrival" mutedColor={colors.cockpitTextSecondary} value={formatDateTime(booking.arrivalTime)} valueColor="#FFFFFF" />
      </InfoSection>

      <InfoSection colors={colors} icon="check" title="Booking">
          <InfoRow label="Confirmation" mutedColor={colors.cockpitTextSecondary} value={booking.bookingReference} valueColor="#FFFFFF" />
          <InfoRow isLast label="Booked" mutedColor={colors.cockpitTextSecondary} value={new Date(booking.bookedAt).toLocaleDateString('en-US')} valueColor="#FFFFFF" />
      </InfoSection>
    </ScrollView>
  );
}
