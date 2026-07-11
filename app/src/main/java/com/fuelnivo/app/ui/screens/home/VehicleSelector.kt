package com.fuelnivo.app.ui.screens.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fuelnivo.app.data.Vehicle

/** Compact active-vehicle selector shown at the top of Home. */
@Composable
fun VehicleSelector(
    vehicles: List<Vehicle>,
    active: Vehicle,
    onSelect: (Vehicle) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable(enabled = vehicles.size > 1) { expanded = true }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.DirectionsCar,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = active.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (vehicles.size > 1) {
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Switch vehicle")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            vehicles.forEach { vehicle ->
                DropdownMenuItem(
                    text = { Text(vehicle.name) },
                    onClick = {
                        onSelect(vehicle)
                        expanded = false
                    }
                )
            }
        }
    }
}
