package com.travelblock.app.ui.components

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.travelblock.app.domain.engine.FlightRouteInterpolation
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.FlightMapModel
import com.travelblock.app.domain.model.GeoPoint
import kotlin.math.roundToInt

@Composable
fun FlightProgressMapView(
    flightMap: FlightMapModel,
    originCode: String,
    destinationCode: String,
    progressLabel: String,
    modifier: Modifier = Modifier,
) {
    val html = buildFlightProgressMapHtml(
        flightMap = flightMap,
        originCode = originCode,
        destinationCode = destinationCode,
        progressLabel = progressLabel,
    )
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        Log.d(
                            "TravelBlockMap",
                            "webview:${consoleMessage.message()} @${consoleMessage.lineNumber()}",
                        )
                        return true
                    }
                }
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                loadDataWithBaseURL("https://travelblock.local/", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://travelblock.local/", html, "text/html", "UTF-8", null)
        },
    )
}

fun buildFlightMapModel(
    origin: Airport,
    destination: Airport,
    progress: Double,
): FlightMapModel {
    val route = FlightRouteInterpolation.buildRoute(
        origin = GeoPoint(origin.latitude, origin.longitude),
        destination = GeoPoint(destination.latitude, destination.longitude),
        sampleCount = 32,
    )
    val clampedProgress = progress.coerceIn(0.0, 1.0)
    val aircraftPosition = FlightRouteInterpolation.interpolate(
        origin = route.first(),
        destination = route.last(),
        progress = clampedProgress,
    )
    return FlightMapModel(
        route = route,
        aircraftPosition = aircraftPosition,
        progress = clampedProgress,
    )
}

private fun buildFlightProgressMapHtml(
    flightMap: FlightMapModel,
    originCode: String,
    destinationCode: String,
    progressLabel: String,
): String {
    val routePoints = flightMap.route.joinToString(separator = ",") { "[${it.latitude}, ${it.longitude}]" }
    val traveledCount = ((flightMap.route.lastIndex) * flightMap.progress).roundToInt().coerceIn(1, flightMap.route.lastIndex)
    val traveledPoints = flightMap.route.take(traveledCount + 1).joinToString(separator = ",") { "[${it.latitude}, ${it.longitude}]" }
    val aircraft = flightMap.aircraftPosition
    val progressPct = (flightMap.progress * 100).roundToInt()
    val safeOrigin = originCode.escapeHtml()
    val safeDestination = destinationCode.escapeHtml()
    val safeProgressLabel = progressLabel.escapeHtml()

    return """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
          <style>
            html, body, #map, #mapCanvas { height: 100%; margin: 0; padding: 0; background: #081522; }
            #map { position: relative; overflow: hidden; }
            #mapCanvas { width: 100%; height: 100%; display: block; }
            .hud {
              position: absolute;
              top: 8px;
              right: 8px;
              z-index: 500;
              padding: 4px 8px;
              border-radius: 999px;
              background: rgba(255,255,255,0.14);
              border: 1px solid rgba(255,255,255,0.28);
              color: #ffffff;
              font: 700 11px system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
              letter-spacing: 0.04em;
            }
            .labels {
              position: absolute;
              left: 8px;
              right: 8px;
              bottom: 8px;
              z-index: 500;
              display: flex;
              justify-content: space-between;
              color: #f4faff;
              font: 700 11px system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
            }
            .plane {
              width: 24px;
              height: 24px;
              border-radius: 12px;
              background: #54c7ff;
              border: 2px solid #ffffff;
              display: flex;
              align-items: center;
              justify-content: center;
              color: #ffffff;
              font: 900 13px system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
              transform: rotate(-20deg);
              box-shadow: 0 4px 12px rgba(0,0,0,0.35);
            }
            .airport {
              padding: 4px 6px;
              border-radius: 8px;
              background: rgba(6,17,29,0.85);
              border: 1px solid rgba(84,199,255,0.38);
              color: #f4faff;
              font: 800 10px system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
              text-align: center;
            }
          </style>
        </head>
        <body>
          <div id="map">
            <canvas id="mapCanvas"></canvas>
          </div>
          <div class="hud">${progressPct}% • ${safeProgressLabel}</div>
          <div class="labels"><span>${safeOrigin}</span><span>${safeDestination}</span></div>
          <script>
            const fullRoute = [${routePoints}];
            const traveledRoute = [${traveledPoints}];
            const aircraft = [${aircraft.latitude}, ${aircraft.longitude}];
            const zoom = 8;
            const tileSize = 256;
            const mapEl = document.getElementById('map');
            const canvas = document.getElementById('mapCanvas');
            const ctx = canvas ? canvas.getContext('2d') : null;
            let painted = false;

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

            function drawTextFallback(message) {
              if (!ctx || !canvas) {
                if (mapEl) {
                  mapEl.style.display = 'flex';
                  mapEl.style.alignItems = 'center';
                  mapEl.style.justifyContent = 'center';
                  mapEl.style.color = '#cfe8f9';
                  mapEl.style.font = '700 13px system-ui, -apple-system, BlinkMacSystemFont, sans-serif';
                  mapEl.textContent = message;
                }
                return;
              }
              ctx.fillStyle = '#081522';
              ctx.fillRect(0, 0, canvas.width, canvas.height);
              ctx.fillStyle = '#cfe8f9';
              ctx.font = '700 13px system-ui, -apple-system, BlinkMacSystemFont, sans-serif';
              ctx.textAlign = 'center';
              ctx.fillText(message, canvas.width / 2, canvas.height / 2);
              painted = true;
            }

            async function renderMap() {
              if (!ctx || !canvas || !mapEl) {
                drawTextFallback('Canvas unavailable');
                return;
              }
              if (!Array.isArray(fullRoute) || fullRoute.length < 2) {
                drawTextFallback('Invalid route data');
                return;
              }

              const width = Math.max(1, mapEl.clientWidth);
              const height = Math.max(1, mapEl.clientHeight);
              canvas.width = width;
              canvas.height = height;

              const projected = fullRoute.map(p => project(p[0], p[1], zoom));
              if (projected.some(p => !Number.isFinite(p.x) || !Number.isFinite(p.y))) {
                drawTextFallback('Invalid map coordinates');
                return;
              }
              const xs = projected.map(p => p.x);
              const ys = projected.map(p => p.y);
              const minX = Math.min(...xs) - 80;
              const maxX = Math.max(...xs) + 80;
              const minY = Math.min(...ys) - 80;
              const maxY = Math.max(...ys) + 80;
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
                      const finish = (ok) => {
                        img.onload = null;
                        img.onerror = null;
                        resolve(ok);
                      };
                      const timeout = setTimeout(() => finish(false), 2500);
                      img.onload = () => {
                        clearTimeout(timeout);
                        try {
                          ctx.drawImage(img, dx, dy, tileSize, tileSize);
                          finish(true);
                        } catch (_) {
                          finish(false);
                        }
                      };
                      img.onerror = () => {
                        clearTimeout(timeout);
                        finish(false);
                      };
                      img.src = url;
                    })
                  );
                }
              }

              const loaded = await Promise.all(tileJobs);
              if (!loaded.some(Boolean)) {
                drawTextFallback('Map tiles failed to load');
                return;
              }

              const toCanvas = (lat, lon) => {
                const p = project(lat, lon, zoom);
                return { x: p.x - topLeftX, y: p.y - topLeftY };
              };

              ctx.strokeStyle = 'rgba(139,191,219,0.72)';
              ctx.lineWidth = 3;
              ctx.setLineDash([8, 6]);
              ctx.beginPath();
              fullRoute.forEach((point, idx) => {
                const p = toCanvas(point[0], point[1]);
                if (idx === 0) ctx.moveTo(p.x, p.y); else ctx.lineTo(p.x, p.y);
              });
              ctx.stroke();

              ctx.strokeStyle = '#54c7ff';
              ctx.lineWidth = 4;
              ctx.setLineDash([]);
              ctx.beginPath();
              traveledRoute.forEach((point, idx) => {
                const p = toCanvas(point[0], point[1]);
                if (idx === 0) ctx.moveTo(p.x, p.y); else ctx.lineTo(p.x, p.y);
              });
              ctx.stroke();

              const o = toCanvas(fullRoute[0][0], fullRoute[0][1]);
              const d = toCanvas(fullRoute[fullRoute.length - 1][0], fullRoute[fullRoute.length - 1][1]);
              const a = toCanvas(aircraft[0], aircraft[1]);

              ctx.fillStyle = '#1d7cc0';
              ctx.beginPath(); ctx.arc(o.x, o.y, 6, 0, Math.PI * 2); ctx.fill();
              ctx.fillStyle = '#ffffff';
              ctx.beginPath(); ctx.arc(d.x, d.y, 5, 0, Math.PI * 2); ctx.fill();

              ctx.fillStyle = '#54c7ff';
              ctx.beginPath(); ctx.arc(a.x, a.y, 10, 0, Math.PI * 2); ctx.fill();
              ctx.strokeStyle = '#ffffff';
              ctx.lineWidth = 2;
              ctx.beginPath();
              ctx.moveTo(a.x - 9, a.y + 3);
              ctx.lineTo(a.x + 9, a.y - 3);
              ctx.stroke();
              painted = true;
            }

            window.onerror = function(msg) {
              drawTextFallback('Map JS error');
              return true;
            };

            setTimeout(() => {
              if (!painted) {
                drawTextFallback('Map render timeout');
              }
            }, 3000);

            renderMap().catch(() => drawTextFallback('Map failed to render'));
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
