package com.fuelnivo.app.ui.screens.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fuelnivo.app.data.Vehicle
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.ui.components.EmptyState
import com.fuelnivo.app.ui.components.SectionLabel
import com.fuelnivo.app.ui.components.StatRow
import com.fuelnivo.app.ui.navigation.Routes
import com.fuelnivo.app.util.DateUtils
import com.fuelnivo.app.util.Formatting
import com.fuelnivo.app.util.Statistics
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    state: AppUiState,
    navController: NavController
) {
    val vehicle = state.activeVehicle

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistics") }) }
    ) { padding ->
        if (vehicle == null) {
            EmptyState(
                icon = Icons.Filled.BarChart,
                title = "No vehicle selected",
                subtitle = "Add a vehicle to view its statistics.",
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }

        val refills = state.refillsForVehicle(vehicle.id)
        if (refills.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.BarChart,
                title = "Not enough data yet",
                subtitle = "Record refills to see fuel, distance, consumption, and cost statistics.",
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }

        val stats = Statistics.lifetimeStats(vehicle, refills)
        val currency = state.settings.currencyCode
        val consUnit = Formatting.consumptionUnitLabel(vehicle.fuelUnit, vehicle.distanceUnit)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(vehicle.name, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            SectionLabel("Fuel")
            StatRow("Total refills", stats.totalRefills.toString())
            StatRow("Total recorded fuel", "${Formatting.fuelPlain(stats.totalFuel)} ${vehicle.fuelUnit.suffix}")
            StatRow("Average refill amount",
                stats.averageRefillAmount?.let { "${Formatting.fuelPlain(it)} ${vehicle.fuelUnit.suffix}" } ?: "—")

            Spacer(Modifier.height(8.dp)); Divider(); Spacer(Modifier.height(8.dp))
            SectionLabel("Distance")
            StatRow("Current odometer",
                stats.currentOdometer?.let { Formatting.odometer(it, vehicle.distanceUnit) } ?: "—")
            StatRow("First recorded odometer",
                stats.firstOdometer?.let { Formatting.odometer(it, vehicle.distanceUnit) } ?: "—")
            StatRow("Total recorded distance",
                stats.totalDistance?.let { Formatting.distance(it, vehicle.distanceUnit) } ?: "—")

            Spacer(Modifier.height(8.dp)); Divider(); Spacer(Modifier.height(8.dp))
            SectionLabel("Consumption ($consUnit)")
            StatRow("Latest valid consumption",
                stats.latestConsumption?.let { Formatting.consumption(it, vehicle.fuelUnit, vehicle.distanceUnit) }
                    ?: "More full-tank records are needed.")
            StatRow("Lifetime average consumption",
                stats.lifetimeConsumption?.let { Formatting.consumption(it, vehicle.fuelUnit, vehicle.distanceUnit) }
                    ?: "More full-tank records are needed.")

            Spacer(Modifier.height(8.dp)); Divider(); Spacer(Modifier.height(8.dp))
            SectionLabel("Cost")
            StatRow("Lifetime recorded cost",
                stats.totalCost?.let { Formatting.cost(it, currency) } ?: "Not recorded")
            StatRow("Average refill cost",
                stats.averageRefillCost?.let { Formatting.cost(it, currency) } ?: "Not recorded")
            StatRow(Formatting.costPerDistanceLabel(vehicle.distanceUnit),
                stats.costPerDistanceUnit?.let { Formatting.cost(it, currency) } ?: "—")

            Spacer(Modifier.height(8.dp)); Divider(); Spacer(Modifier.height(8.dp))
            SectionLabel("Record quality")
            StatRow("Full-tank records", stats.fullTankCount.toString())
            StatRow("Valid consumption intervals", stats.validIntervalCount.toString())
            StatRow("Intervals excluded from calculations", stats.excludedIntervalCount.toString())

            Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(16.dp))
            MonthComparison(vehicle, refills, currency)

            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = { navController.navigate(Routes.MONTHLY) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Monthly Statistics")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MonthComparison(
    vehicle: Vehicle,
    refills: List<com.fuelnivo.app.data.RefillRecord>,
    currency: String
) {
    val current = YearMonth.from(DateUtils.today())
    val previous = current.minusMonths(1)
    val cur = Statistics.monthlySummary(vehicle, refills, current)
    val prev = Statistics.monthlySummary(vehicle, refills, previous)

    SectionLabel("Month comparison")
    Spacer(Modifier.height(4.dp))
    Text(
        text = "${DateUtils.monthLabel(current)} vs ${DateUtils.monthLabel(previous)}",
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(6.dp))
    if (!prev.hasData) {
        Text(
            text = "No previous month data.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
    }
    StatRow(
        "Fuel",
        "${Formatting.fuelPlain(cur.totalFuel)} ${vehicle.fuelUnit.suffix}  " +
            comparisonNote(cur.totalFuel, if (prev.hasData) prev.totalFuel else null)
    )
    StatRow(
        "Cost",
        (cur.totalCost?.let { Formatting.cost(it, currency) } ?: "—") + "  " +
            comparisonNote(cur.totalCost, prev.totalCost)
    )
    StatRow(
        "Distance",
        (cur.totalDistance?.let { Formatting.distance(it, vehicle.distanceUnit) } ?: "—") + "  " +
            comparisonNote(cur.totalDistance, prev.totalDistance)
    )
}

/** Neutral change wording; never labeled good or bad. */
private fun comparisonNote(current: Double?, previous: Double?): String {
    if (current == null || previous == null) return ""
    val diff = current - previous
    val epsilon = 0.0001
    return when {
        diff > epsilon -> "(Higher than previous month)"
        diff < -epsilon -> "(Lower than previous month)"
        else -> "(No change)"
    }
}
