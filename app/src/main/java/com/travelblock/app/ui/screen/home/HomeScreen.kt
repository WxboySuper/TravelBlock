package com.travelblock.app.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.domain.engine.FlightRouteInterpolation
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.ui.components.EmptyState
import com.travelblock.app.ui.components.airportLocationLabel
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.DeepNavy
import com.travelblock.app.ui.theme.MutedText
import com.travelblock.app.ui.theme.RunwayLine
import com.travelblock.app.ui.theme.SkyBlue
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.TerminalBlue
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val HomeBackground = Color(0xFFF4F9FD)
private val BoardInk = DeepNavy
private val BoardPanel = Color(0xFFFFFFFF)
private val BoardBlue = TerminalBlue
private val BoardLine = Color(0xFFD6E7F3)
private val BoardSky = Color(0xFFEAF7FF)
private val RowSelected = Color(0xFFE4F4FF)

@Composable
fun HomeScreen(
    container: TravelBlockContainer,
    activeFlight: ActiveFlight?,
    onReturnToCockpit: () -> Unit,
    onBookSelectedRoute: (DraftFlightBooking) -> Unit,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            airportRepository = container.airportRepository,
            settingsRepository = container.settingsRepository,
            pointsRepository = container.pointsRepository,
        ),
    )

    HomeScreenContent(
        uiState = viewModel.uiState,
        activeFlight = activeFlight,
        onDurationSelected = viewModel::onDurationSelected,
        onRouteSelected = viewModel::onRouteSelected,
        onReturnToCockpit = onReturnToCockpit,
        onBookSelectedRoute = {
            viewModel.createSelectedDraftBooking()?.let(onBookSelectedRoute)
        },
        onLoadMoreRoutes = viewModel::loadMoreRoutes,
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    activeFlight: ActiveFlight? = null,
    onDurationSelected: (Int) -> Unit,
    onRouteSelected: (String) -> Unit,
    onReturnToCockpit: () -> Unit = {},
    onBookSelectedRoute: () -> Unit,
    onLoadMoreRoutes: () -> Unit = {},
) {
    val visibleRoutes = uiState.visibleRoutes
    val selectedRoute = uiState.selectedRoute
    val listState = rememberLazyListState()

    LaunchedEffect(listState, uiState.hasMoreRoutes, uiState.displayedRouteCount) {
        if (!uiState.hasMoreRoutes) return@LaunchedEffect
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.totalItemsCount == 0) return@snapshotFlow false
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= layoutInfo.totalItemsCount - 2
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd) onLoadMoreRoutes()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AirportJourneyZone(
                    airport = uiState.currentAirport,
                    points = uiState.points,
                    selectedRoute = selectedRoute,
                )
            }
            if (activeFlight != null) {
                item {
                    InFlightHomePanel(activeFlight = activeFlight)
                }
            } else {
                item {
                    when {
                        uiState.isLoading -> EmptyState(
                            title = "Loading departures",
                            message = "Checking reachable airports from your current gate.",
                        )
                        uiState.errorMessage != null && uiState.routes.isEmpty() -> EmptyState(
                            title = "Departure board unavailable",
                            message = uiState.errorMessage,
                        )
                        uiState.routes.isEmpty() -> EmptyState(
                            title = "No departures found",
                            message = "No airports fit within ${uiState.selectedDurationMinutes} min. Try 25, 45, or 60 min.",
                        )
                        else -> RouteExplorerZone(
                            currentAirport = uiState.currentAirport,
                            selectedRoute = selectedRoute,
                            selectedDurationMinutes = uiState.selectedDurationMinutes,
                            availableDurationsMinutes = uiState.availableDurationsMinutes,
                            visibleCount = visibleRoutes.size,
                            totalCount = uiState.routes.size,
                            onDurationSelected = onDurationSelected,
                        )
                    }
                }
                if (visibleRoutes.isNotEmpty()) {
                    item {
                        DepartureRowsHeader(
                            visibleCount = visibleRoutes.size,
                            totalCount = uiState.routes.size,
                        )
                    }
                    itemsIndexed(visibleRoutes, key = { _, route -> route.id }) { _, route ->
                        CompactDepartureRow(
                            route = route,
                            selected = route.id == uiState.selectedRouteId,
                            onSelected = { onRouteSelected(route.id) },
                        )
                    }
                    if (uiState.hasMoreRoutes) {
                        item {
                            LoadMoreDeparturesRow(
                                remainingCount = uiState.remainingRouteCount,
                                onLoadMore = onLoadMoreRoutes,
                            )
                        }
                    }
                }
            }
        }
        TerminalActionFooter(
            selectedRoute = selectedRoute,
            activeFlight = activeFlight,
            onReturnToCockpit = onReturnToCockpit,
            onBookSelectedRoute = onBookSelectedRoute,
        )
    }
}

@Composable
private fun AirportJourneyZone(
    airport: Airport?,
    points: Int,
    selectedRoute: HomeRouteCardUiModel?,
) {
    val localTime = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "TRAVELBLOCK",
                    style = MaterialTheme.typography.titleMedium,
                    color = BoardInk,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Departures · $localTime",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Surface(
                color = BoardPanel,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, BoardLine),
                shadowElevation = 2.dp,
            ) {
                Text(
                    text = "$points pts",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = BoardBlue,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        GateTile(
            airport = airport,
            destinationCode = selectedRoute?.destinationCode,
        )
    }
}

@Composable
private fun GateTile(
    airport: Airport?,
    destinationCode: String?,
) {
    val code = airport?.displayCode ?: HomeViewModel.DefaultOriginCode
    val icao = airport?.icao ?: HomeViewModel.DefaultOriginCode
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BoardPanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BoardLine),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = BoardBlue,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = code,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "GATE STATUS · $icao",
                    style = MaterialTheme.typography.labelSmall,
                    color = BoardBlue,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = airport?.name ?: "University of Oklahoma Westheimer Airport",
                    style = MaterialTheme.typography.titleSmall,
                    color = BoardInk,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = airport?.let(::airportLocationLabel) ?: "Norman, Oklahoma",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            StatusPill(destinationCode?.let { "TO $it" } ?: "READY")
        }
    }
}

@Composable
private fun RouteExplorerZone(
    currentAirport: Airport?,
    selectedRoute: HomeRouteCardUiModel?,
    selectedDurationMinutes: Int,
    availableDurationsMinutes: List<Int>,
    visibleCount: Int,
    totalCount: Int,
    onDurationSelected: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BoardPanel,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BoardLine),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "ROUTE EXPLORER",
                        style = MaterialTheme.typography.labelSmall,
                        color = BoardBlue,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "Board from ${currentAirport?.displayCode ?: HomeViewModel.DefaultOriginCode}",
                        style = MaterialTheme.typography.titleMedium,
                        color = BoardInk,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(
                    color = BoardSky,
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, BoardLine),
                ) {
                    Text(
                        text = "$visibleCount/$totalCount",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = BoardBlue,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                availableDurationsMinutes.forEach { duration ->
                    DurationToken(
                        label = "${duration}m",
                        selected = duration == selectedDurationMinutes,
                        onClick = { onDurationSelected(duration) },
                    )
                }
            }
            LargeRouteBoard(
                originCode = currentAirport?.displayCode ?: HomeViewModel.DefaultOriginCode,
                selectedRoute = selectedRoute,
                maxFlightTimeMinutes = selectedDurationMinutes,
            )
        }
    }
}

@Composable
private fun LargeRouteBoard(
    originCode: String,
    selectedRoute: HomeRouteCardUiModel?,
    maxFlightTimeMinutes: Int,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(286.dp),
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BoardLine),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            HomeRouteMapView(
                selectedRoute = selectedRoute,
                modifier = Modifier.matchParentSize(),
            )
            BoardAirportAnchor(
                label = "ORIGIN",
                code = originCode,
                detail = "Current gate",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
            )
            BoardAirportAnchor(
                label = "DESTINATION",
                code = selectedRoute?.destinationCode ?: "---",
                detail = selectedRoute?.destinationCityRegion?.substringBefore(",") ?: "Select a row",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                alignEnd = true,
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                color = Color.White.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "MAX WINDOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${maxFlightTimeMinutes}m",
                        style = MaterialTheme.typography.titleMedium,
                        color = BoardBlue,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            selectedRoute?.let { route ->
                BoardMetricStrip(
                    route = route,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeRouteMapView(
    selectedRoute: HomeRouteCardUiModel?,
    modifier: Modifier = Modifier,
) {
    val origin = GeoPoint(
        latitude = selectedRoute?.originLatitude ?: 35.2456,
        longitude = selectedRoute?.originLongitude ?: -97.4721,
    )
    val destination = GeoPoint(
        latitude = selectedRoute?.destinationLatitude ?: origin.latitude,
        longitude = selectedRoute?.destinationLongitude ?: origin.longitude,
    )
    val route = FlightRouteInterpolation.buildRoute(
        origin = origin,
        destination = destination,
        sampleCount = 24,
    )
    val latitudes = route.map { it.latitude }
    val longitudes = route.map { it.longitude }
    val latMargin = 0.3
    val lonMargin = 0.3
    val minLat = latitudes.minOrNull()?.minus(latMargin) ?: origin.latitude - latMargin
    val maxLat = latitudes.maxOrNull()?.plus(latMargin) ?: origin.latitude + latMargin
    val minLon = longitudes.minOrNull()?.minus(lonMargin) ?: origin.longitude - lonMargin
    val maxLon = longitudes.maxOrNull()?.plus(lonMargin) ?: origin.longitude + lonMargin

    fun project(point: GeoPoint, width: Float, height: Float): Offset {
        val contentLeft = width * 0.06f
        val contentTop = height * 0.08f
        val contentWidth = width * 0.88f
        val contentHeight = height * 0.82f
        val xRatio = ((point.longitude - minLon) / (maxLon - minLon)).toFloat().coerceIn(0f, 1f)
        val yRatio = ((point.latitude - minLat) / (maxLat - minLat)).toFloat().coerceIn(0f, 1f)
        return Offset(
            x = contentLeft + xRatio * contentWidth,
            y = contentTop + (1f - yRatio) * contentHeight,
        )
    }

    Canvas(modifier = modifier) {
        drawRect(color = BoardSky.copy(alpha = 0.88f))

        val gridColor = BoardBlue.copy(alpha = 0.14f)
        repeat(4) { index ->
            val y = (size.height * (index + 1)) / 5f
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }
        repeat(5) { index ->
            val x = (size.width * (index + 1)) / 6f
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f,
            )
        }

        val projected = route.map { project(it, size.width, size.height) }
        if (projected.size >= 2) {
            val path = Path().apply {
                moveTo(projected.first().x, projected.first().y)
                projected.drop(1).forEach { point -> lineTo(point.x, point.y) }
            }
            drawPath(
                path = path,
                color = BoardBlue.copy(alpha = 0.35f),
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
                ),
            )
            drawCircle(
                color = BoardBlue,
                radius = 6.dp.toPx(),
                center = projected.first(),
            )
            drawCircle(
                color = TerminalBlue,
                radius = 6.dp.toPx(),
                center = projected.last(),
            )
            val aircraft = projected[projected.lastIndex / 2]
            drawCircle(
                color = SkyBlue,
                radius = 9.dp.toPx(),
                center = aircraft,
            )
            drawLine(
                color = Color.White,
                start = Offset(aircraft.x - 10.dp.toPx(), aircraft.y + 3.dp.toPx()),
                end = Offset(aircraft.x + 10.dp.toPx(), aircraft.y - 3.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun buildRouteMapHtml(
    originCode: String,
    selectedRoute: HomeRouteCardUiModel?,
): String {
    val originLat = selectedRoute?.originLatitude ?: 35.2456
    val originLon = selectedRoute?.originLongitude ?: -97.4721
    val destinationLat = selectedRoute?.destinationLatitude ?: originLat
    val destinationLon = selectedRoute?.destinationLongitude ?: originLon
    val destinationCode = selectedRoute?.destinationCode ?: "SELECT"
    val distanceLabel = selectedRoute?.distanceLabel ?: "-- mi"
    val timeLabel = selectedRoute?.focusTimeLabel ?: "-- min"
    val rewardLabel = selectedRoute?.estimatedPointsLabel ?: "+-- pts"
    val statusLabel = selectedRoute?.boardStatusLabel() ?: "SELECT"
    val destinationName = selectedRoute?.destinationName?.escapeHtml() ?: "Choose a departure row"
    val safeOriginCode = originCode.escapeHtml()
    val safeDestinationCode = destinationCode.escapeHtml()
    val safeDistanceLabel = distanceLabel.escapeHtml()
    val safeTimeLabel = timeLabel.escapeHtml()
    val safeRewardLabel = rewardLabel.escapeHtml()
    val safeStatusLabel = statusLabel.escapeHtml()

    return """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
          <style>
            html, body, #map, #mapCanvas { height: 100%; margin: 0; padding: 0; background: #eaf7ff; }
            #map {
              position: relative;
              overflow: hidden;
              filter: saturate(1.2) contrast(1.04);
              font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
            }
            #mapCanvas { width: 100%; height: 100%; display: block; }
            .tb-glass {
              position: absolute;
              inset: 0;
              z-index: 500;
              pointer-events: none;
              background:
                radial-gradient(circle at 15% 18%, rgba(255,255,255,0.78), rgba(255,255,255,0.0) 22%),
                radial-gradient(circle at 78% 24%, rgba(11,142,234,0.22), rgba(11,142,234,0.0) 34%),
                linear-gradient(135deg, rgba(255,255,255,0.30), rgba(255,255,255,0.02) 48%, rgba(15,34,51,0.10));
            }
            .tb-scan {
              position: absolute;
              left: -30%;
              top: 0;
              width: 32%;
              height: 100%;
              z-index: 520;
              pointer-events: none;
              background: linear-gradient(90deg, rgba(255,255,255,0), rgba(255,255,255,0.44), rgba(255,255,255,0));
              transform: skewX(-18deg);
              animation: scan 5.8s infinite ease-in-out;
            }
            @keyframes scan {
              0% { left: -38%; opacity: 0; }
              12% { opacity: 1; }
              58% { opacity: 0.8; }
              100% { left: 110%; opacity: 0; }
            }
            .tb-vignette {
              position: absolute;
              inset: 0;
              z-index: 510;
              pointer-events: none;
              box-shadow: inset 0 0 0 1px rgba(255,255,255,0.65), inset 0 -80px 120px rgba(15,34,51,0.18);
              border-radius: 14px;
            }
            .tb-hud {
              position: absolute;
              left: 10px;
              right: 10px;
              bottom: 10px;
              z-index: 530;
              display: grid;
              grid-template-columns: 1.25fr 0.75fr 0.75fr;
              gap: 7px;
              pointer-events: none;
            }
            .tb-hud-card {
              border-radius: 11px;
              background: rgba(255,255,255,0.88);
              border: 1px solid rgba(255,255,255,0.92);
              box-shadow: 0 10px 28px rgba(15,34,51,0.16);
              padding: 8px 9px;
              backdrop-filter: blur(10px);
              overflow: hidden;
            }
            .tb-hud-label {
              color: rgba(15,34,51,0.58);
              font-weight: 900;
              font-size: 9px;
              letter-spacing: 0.08em;
              line-height: 1.1;
              white-space: nowrap;
            }
            .tb-hud-value {
              color: #0f2233;
              font-weight: 950;
              font-size: 13px;
              line-height: 1.2;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
            .tb-hud-accent { color: #b97900; }
            .tb-pulse-ring {
              position: absolute;
              inset: -9px;
              border-radius: 999px;
              border: 2px solid rgba(11,142,234,0.38);
              animation: pulse 2.2s infinite ease-out;
            }
            @keyframes pulse {
              from { transform: scale(0.62); opacity: 0.95; }
              to { transform: scale(1.75); opacity: 0; }
            }
            .tb-pin {
              position: relative;
              min-width: 42px;
              padding: 4px 7px;
              border-radius: 9px;
              background: #ffffff;
              border: 2px solid #0b5f8a;
              color: #0f2233;
              font: 800 12px system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
              text-align: center;
              box-shadow: 0 6px 18px rgba(15, 34, 51, 0.18);
            }
            .tb-plane {
              width: 34px;
              height: 34px;
              border-radius: 17px;
              background: #0b5f8a;
              border: 3px solid #ffffff;
              box-shadow: 0 6px 18px rgba(15, 34, 51, 0.22);
              color: #ffffff;
              display: flex;
              align-items: center;
              justify-content: center;
              font: 900 18px system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
              transform: rotate(-18deg);
              animation: planeFloat 2.8s infinite ease-in-out;
            }
            @keyframes planeFloat {
              0%, 100% { transform: rotate(-18deg) translateY(0); }
              50% { transform: rotate(-18deg) translateY(-5px); }
            }
            .tb-metric {
              border-radius: 10px;
              background: rgba(255,255,255,0.92);
              border: 1px solid rgba(214,231,243,0.95);
              padding: 8px 10px;
              color: #0f2233;
              font: 800 12px system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
              box-shadow: 0 8px 24px rgba(15, 34, 51, 0.12);
            }
          </style>
        </head>
        <body>
          <div id="map"><canvas id="mapCanvas"></canvas></div>
          <div class="tb-glass"></div>
          <div class="tb-scan"></div>
          <div class="tb-vignette"></div>
          <div class="tb-hud">
            <div class="tb-hud-card">
              <div class="tb-hud-label">SELECTED ROUTE</div>
              <div class="tb-hud-value">$safeDestinationCode · $destinationName</div>
            </div>
            <div class="tb-hud-card">
              <div class="tb-hud-label">BLOCK</div>
              <div class="tb-hud-value">$safeTimeLabel</div>
            </div>
            <div class="tb-hud-card">
              <div class="tb-hud-label">EARN</div>
              <div class="tb-hud-value tb-hud-accent">$safeRewardLabel</div>
            </div>
          </div>
          <div style="position:absolute;right:10px;bottom:66px;z-index:540;pointer-events:none;">
            <div class="tb-metric">$safeStatusLabel · $safeDistanceLabel</div>
          </div>
          <script>
            const origin = [$originLat, $originLon];
            const destination = [$destinationLat, $destinationLon];
            const zoom = 8;
            const tileSize = 256;
            const mapEl = document.getElementById('map');
            const canvas = document.getElementById('mapCanvas');
            const ctx = canvas.getContext('2d');

            function project(lat, lon, z) {
              const scale = tileSize * Math.pow(2, z);
              const sinLat = Math.sin(lat * Math.PI / 180);
              const x = (lon + 180) / 360 * scale;
              const y = (0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI)) * scale;
              return { x, y };
            }

            function normalizeTileX(x, z) {
              const max = Math.pow(2, z);
              return ((x % max) + max) % max;
            }

            function drawFallback(message) {
              ctx.fillStyle = '#eaf7ff';
              ctx.fillRect(0, 0, canvas.width, canvas.height);
              ctx.fillStyle = '#0f2233';
              ctx.font = '700 13px system-ui, -apple-system, BlinkMacSystemFont, sans-serif';
              ctx.textAlign = 'center';
              ctx.fillText(message, canvas.width / 2, canvas.height / 2);
            }

            async function renderMap() {
              const width = Math.max(1, mapEl.clientWidth);
              const height = Math.max(1, mapEl.clientHeight);
              canvas.width = width;
              canvas.height = height;

              const route = [origin, destination];
              const projected = route.map(p => project(p[0], p[1], zoom));
              const xs = projected.map(p => p.x);
              const ys = projected.map(p => p.y);
              const minX = Math.min(...xs) - 110;
              const maxX = Math.max(...xs) + 110;
              const minY = Math.min(...ys) - 110;
              const maxY = Math.max(...ys) + 110;
              const centerX = (minX + maxX) / 2;
              const centerY = (minY + maxY) / 2;
              const topLeftX = centerX - width / 2;
              const topLeftY = centerY - height / 2;

              const xStart = Math.floor(topLeftX / tileSize) - 1;
              const yStart = Math.floor(topLeftY / tileSize) - 1;
              const xEnd = Math.floor((topLeftX + width) / tileSize) + 1;
              const yEnd = Math.floor((topLeftY + height) / tileSize) + 1;
              const maxYTile = Math.pow(2, zoom) - 1;

              const tileJobs = [];
              for (let ty = yStart; ty <= yEnd; ty++) {
                if (ty < 0 || ty > maxYTile) continue;
                for (let tx = xStart; tx <= xEnd; tx++) {
                  const tileX = normalizeTileX(tx, zoom);
                  const url = `https://tile.openstreetmap.org/${'$'}{zoom}/${'$'}{tileX}/${'$'}{ty}.png`;
                  const dx = tx * tileSize - topLeftX;
                  const dy = ty * tileSize - topLeftY;
                  tileJobs.push(
                    new Promise(resolve => {
                      const img = new Image();
                      img.onload = () => { ctx.drawImage(img, dx, dy, tileSize, tileSize); resolve(true); };
                      img.onerror = () => resolve(false);
                      img.src = url;
                    })
                  );
                }
              }

              const loaded = await Promise.all(tileJobs);
              if (!loaded.some(Boolean)) {
                drawFallback('Map tiles failed to load');
                return;
              }

              const toCanvas = (lat, lon) => {
                const p = project(lat, lon, zoom);
                return { x: p.x - topLeftX, y: p.y - topLeftY };
              };

              const o = toCanvas(origin[0], origin[1]);
              const d = toCanvas(destination[0], destination[1]);
              const plane = {
                x: o.x + (d.x - o.x) * 0.58,
                y: o.y + (d.y - o.y) * 0.58
              };

              ctx.strokeStyle = 'rgba(255,255,255,0.36)';
              ctx.lineWidth = 10;
              ctx.lineCap = 'round';
              ctx.beginPath();
              ctx.moveTo(o.x, o.y);
              ctx.lineTo(d.x, d.y);
              ctx.stroke();

              ctx.strokeStyle = '#0b8eea';
              ctx.lineWidth = 4;
              ctx.setLineDash([12, 10]);
              ctx.beginPath();
              ctx.moveTo(o.x, o.y);
              ctx.lineTo(d.x, d.y);
              ctx.stroke();
              ctx.setLineDash([]);

              function drawPin(x, y, code) {
                ctx.fillStyle = '#ffffff';
                ctx.strokeStyle = '#0b5f8a';
                ctx.lineWidth = 2;
                const w = 44;
                const h = 24;
                ctx.beginPath();
                ctx.rect(x - w / 2, y - h / 2, w, h);
                ctx.fill();
                ctx.stroke();
                ctx.fillStyle = '#0f2233';
                ctx.font = '800 12px system-ui, -apple-system, BlinkMacSystemFont, sans-serif';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText(code, x, y + 0.5);
              }

              drawPin(o.x, o.y, '$safeOriginCode');
              drawPin(d.x, d.y, '$safeDestinationCode');

              ctx.fillStyle = '#0b5f8a';
              ctx.strokeStyle = '#ffffff';
              ctx.lineWidth = 3;
              ctx.beginPath();
              ctx.arc(plane.x, plane.y, 17, 0, Math.PI * 2);
              ctx.fill();
              ctx.stroke();
              ctx.fillStyle = '#ffffff';
              ctx.font = '900 18px system-ui, -apple-system, BlinkMacSystemFont, sans-serif';
              ctx.textAlign = 'center';
              ctx.textBaseline = 'middle';
              ctx.fillText('✈', plane.x, plane.y + 1);
            }

            renderMap().catch(() => drawFallback('Map failed to render'));
          </script>
        </body>
        </html>
    """.trimIndent()
}

private fun String.escapeHtml(): String = buildString(length) {
    this@escapeHtml.forEach { char ->
        append(
            when (char) {
                '&' -> "&amp;"
                '<' -> "&lt;"
                '>' -> "&gt;"
                '"' -> "&quot;"
                '\'' -> "&#39;"
                else -> char
            },
        )
    }
}

@Composable
private fun BoardAirportAnchor(
    label: String,
    code: String,
    detail: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MutedText,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = code,
            style = MaterialTheme.typography.headlineMedium,
            color = BoardInk,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.labelSmall,
            color = BoardBlue,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BoardMetricStrip(
    route: HomeRouteCardUiModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.90f),
        shape = RoundedCornerShape(9.dp),
        border = BorderStroke(1.dp, Color.White),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BoardMetric("TIME", route.focusTimeLabel)
            BoardMetric("DIST", route.distanceLabel)
            BoardMetric("EARN", route.estimatedPointsLabel, accent = true)
        }
    }
}

@Composable
private fun BoardMetric(
    label: String,
    value: String,
    accent: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MutedText,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = if (accent) CabinAmber else BoardInk,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun DepartureRowsHeader(
    visibleCount: Int,
    totalCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "DEPARTURE ROWS",
            style = MaterialTheme.typography.labelLarge,
            color = BoardInk,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "$visibleCount of $totalCount",
            style = MaterialTheme.typography.labelMedium,
            color = MutedText,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CompactDepartureRow(
    route: HomeRouteCardUiModel,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onSelected),
        color = if (selected) RowSelected else BoardPanel,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) BoardBlue.copy(alpha = 0.42f) else BoardLine),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = route.destinationCode,
                modifier = Modifier.weight(0.94f),
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) BoardBlue else BoardInk,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
            DepartureCell(route.focusTimeLabel, Modifier.weight(0.78f))
            DepartureCell(route.distanceLabel, Modifier.weight(0.78f))
            DepartureCell(route.estimatedPointsLabel, Modifier.weight(0.82f), accent = true)
            StatusPill(
                text = route.boardStatusLabel(),
                selected = selected,
                modifier = Modifier.weight(1.0f),
            )
        }
    }
}

@Composable
private fun DepartureCell(
    text: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = if (accent) CabinAmber else BoardInk,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun DurationToken(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) BoardBlue else BoardSky,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, if (selected) BoardBlue else BoardLine),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else BoardBlue,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TerminalActionFooter(
    selectedRoute: HomeRouteCardUiModel?,
    activeFlight: ActiveFlight?,
    onReturnToCockpit: () -> Unit,
    onBookSelectedRoute: () -> Unit,
) {
    val enabled = activeFlight != null || selectedRoute != null
    val title = when {
        activeFlight != null -> "Return to Cockpit"
        selectedRoute != null -> "Continue to Check-in"
        else -> "Select a Departure"
    }
    val subtitle = when {
        activeFlight != null -> "${activeFlight.origin.displayCode} to ${activeFlight.destination.displayCode} · in flight"
        selectedRoute != null -> "${selectedRoute.destinationCode} · ${selectedRoute.focusTimeLabel} · ${selectedRoute.distanceLabel} · ${selectedRoute.estimatedPointsLabel}"
        else -> "Pick a departure row to stage your route"
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, BoardLine),
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(62.dp)
                .clickable(enabled = enabled) {
                    if (activeFlight != null) onReturnToCockpit() else onBookSelectedRoute()
                },
            color = if (enabled) BoardInk else Color(0xFFB8C4CC),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (enabled) CabinAmber.copy(alpha = 0.50f) else Color.Transparent),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = if (enabled) CabinAmber else Color.White.copy(alpha = 0.35f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "TB",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (enabled) BoardInk else Color.White,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.72f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "GATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) CabinAmber else Color.White.copy(alpha = 0.62f),
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
private fun InFlightHomePanel(activeFlight: ActiveFlight) {
    Surface(
        color = BoardPanel,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BoardLine),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "IN FLIGHT",
                style = MaterialTheme.typography.labelLarge,
                color = BoardBlue,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Flight ${activeFlight.flightNumber} is airborne from ${activeFlight.origin.displayCode} to ${activeFlight.destination.displayCode}. Booking is closed until you land or divert.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedText,
            )
        }
    }
}

@Composable
private fun LoadMoreDeparturesRow(
    remainingCount: Int,
    onLoadMore: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clickable(onClick = onLoadMore),
        color = BoardSky,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BoardLine),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "Load $remainingCount more holding departures",
                style = MaterialTheme.typography.labelLarge,
                color = BoardBlue,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = if (selected) BoardBlue else SoftSky,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, if (selected) BoardBlue else BoardLine),
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color.White else BoardBlue,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun HomeRouteCardUiModel.boardStatusLabel(): String {
    if (availabilityLabel.equals("Best match", ignoreCase = true)) return "Best"
    val miles = distanceMiles.roundToInt()
    return when {
        miles < 150 -> "Short"
        miles < 500 -> "Regional"
        miles < 1_200 -> "Major"
        else -> "Long"
    }
}
