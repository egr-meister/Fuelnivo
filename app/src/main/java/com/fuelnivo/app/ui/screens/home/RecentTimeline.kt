package com.fuelnivo.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fuelnivo.app.data.Vehicle
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.util.DateUtils
import com.fuelnivo.app.util.FuelCalculations
import com.fuelnivo.app.util.Formatting

/** A compact vertical timeline of the most recent refills. */
@Composable
fun RecentTimeline(
    state: AppUiState,
    vehicle: Vehicle,
    maxItems: Int = 4,
    onOpen: (String) -> Unit
) {
    val refills = FuelCalculations.sortedDescending(state.refillsForVehicle(vehicle.id))
        .take(maxItems)
    Column {
        refills.forEachIndexed { index, refill ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(refill.id) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    if (index != refills.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = DateUtils.longDisplay(refill.date),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${Formatting.fuelPlain(refill.fuelAmount)} ${vehicle.fuelUnit.suffix} · ${Formatting.odometer(refill.odometer, vehicle.distanceUnit)}" +
                            if (refill.fullTank) " · Full tank" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (refill.totalCost != null) {
                    Text(
                        text = Formatting.cost(refill.totalCost, state.settings.currencyCode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
