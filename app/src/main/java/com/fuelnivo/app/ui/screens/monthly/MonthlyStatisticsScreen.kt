package com.fuelnivo.app.ui.screens.monthly

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fuelnivo.app.data.RefillRecord
import com.fuelnivo.app.data.Vehicle
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.ui.components.EmptyState
import com.fuelnivo.app.ui.components.SectionLabel
import com.fuelnivo.app.ui.components.StatRow
import com.fuelnivo.app.util.DateUtils
import com.fuelnivo.app.util.FuelCalculations
import com.fuelnivo.app.util.Formatting
import com.fuelnivo.app.util.Statistics
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyStatisticsScreen(
    state: AppUiState,
    navController: NavController
) {
    val vehicle = state.activeVehicle
    var month by remember { mutableStateOf(YearMonth.from(DateUtils.today())) }
    val currentMonth = YearMonth.from(DateUtils.today())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (vehicle == null) {
            EmptyState(
                icon = Icons.Filled.CalendarMonth,
                title = "No vehicle selected",
                subtitle = "Add a vehicle to view monthly statistics.",
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }

        val refills = state.refillsForVehicle(vehicle.id)
        val summary = Statistics.monthlySummary(vehicle, refills, month)
        val currency = state.settings.currencyCode

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { month = month.minusMonths(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(
                    text = DateUtils.monthLabel(month),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { month = month.plusMonths(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }
            if (month != currentMonth) {
                TextButton(onClick = { month = currentMonth }) { Text("Return to current month") }
            }
            Spacer(Modifier.height(12.dp))

            if (!summary.hasData) {
                EmptyState(
                    icon = Icons.Filled.CalendarMonth,
                    title = "No refill data for this month.",
                    subtitle = "Use the arrows to browse other months.",
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                )
                return@Column
            }

            SectionLabel("Fuel columns")
            Spacer(Modifier.height(8.dp))
            FuelColumns(vehicle, monthRefills(refills, month))

            Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(12.dp))
            SectionLabel("Summary")
            StatRow("Refills", summary.refillCount.toString())
            StatRow("Total fuel recorded", "${Formatting.fuelPlain(summary.totalFuel)} ${vehicle.fuelUnit.suffix}")
            StatRow("Total recorded cost", summary.totalCost?.let { Formatting.cost(it, currency) } ?: "—")
            StatRow("Total recorded distance",
                summary.totalDistance?.let { Formatting.distance(it, vehicle.distanceUnit) } ?: "—")
            StatRow("Average refill amount",
                summary.averageRefillAmount?.let { "${Formatting.fuelPlain(it)} ${vehicle.fuelUnit.suffix}" } ?: "—")
            StatRow("Average refill cost",
                summary.averageRefillCost?.let { Formatting.cost(it, currency) } ?: "—")
            StatRow("Valid average consumption",
                summary.averageConsumption?.let { Formatting.consumption(it, vehicle.fuelUnit, vehicle.distanceUnit) }
                    ?: "More full-tank records are needed.")
            StatRow("Highest refill amount",
                summary.highestRefill?.let { "${Formatting.fuelPlain(it)} ${vehicle.fuelUnit.suffix}" } ?: "—")
            StatRow("Longest interval between refills",
                summary.longestIntervalDays?.let { "$it days" } ?: "—")
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun monthRefills(refills: List<RefillRecord>, month: YearMonth): List<RefillRecord> =
    FuelCalculations.sortedAscending(refills).filter { DateUtils.yearMonthOf(it.date) == month }

@Composable
private fun FuelColumns(vehicle: Vehicle, refills: List<RefillRecord>) {
    if (refills.isEmpty()) return
    val maxFuel = refills.maxOf { it.fuelAmount }.coerceAtLeast(0.0001)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        refills.forEach { refill ->
            val fraction = (refill.fuelAmount / maxFuel).toFloat().coerceIn(0.05f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = Formatting.fuelPlain(refill.fuelAmount),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height((140 * fraction).dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = DateUtils.shortDisplay(refill.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
