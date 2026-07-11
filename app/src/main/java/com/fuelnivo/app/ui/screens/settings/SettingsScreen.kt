package com.fuelnivo.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fuelnivo.app.ui.AppUiState
import com.fuelnivo.app.ui.MainViewModel
import com.fuelnivo.app.ui.components.ConfirmDialog
import com.fuelnivo.app.ui.components.DropdownSelector
import com.fuelnivo.app.ui.components.LabeledTextField
import com.fuelnivo.app.ui.components.SectionLabel
import com.fuelnivo.app.ui.components.ToggleRow
import com.fuelnivo.app.ui.theme.ErrorColor
import com.fuelnivo.app.util.DisclaimerText

private val REMINDER_PRESETS = listOf(7, 14, 21)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: AppUiState,
    viewModel: MainViewModel,
    navController: NavController
) {
    val settings = state.settings
    var currency by remember(settings.currencyCode) { mutableStateOf(settings.currencyCode) }
    var customDays by remember(settings.reminderSettings.intervalDays) {
        mutableStateOf(settings.reminderSettings.intervalDays.toString())
    }

    var confirmResetVehicle by remember { mutableStateOf(false) }
    var confirmDeleteAll by remember { mutableStateOf(false) }
    var confirmResetAll by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionLabel("Currency")
            Spacer(Modifier.height(8.dp))
            LabeledTextField(
                value = currency,
                onValueChange = { currency = it.uppercase().take(6) },
                label = "Currency code",
                supportingText = "Used as a label only. Examples: USD, EUR, GBP, UAH, CAD, AUD."
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = { viewModel.setCurrencyCode(currency) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save currency") }

            Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(16.dp))
            SectionLabel("Reminder")
            Spacer(Modifier.height(8.dp))
            ToggleRow(
                title = "Enable in-app reminder",
                subtitle = "Fuelnivo reminders appear inside the app. The app does not send push " +
                    "notifications or run in the background.",
                checked = settings.reminderSettings.enabled,
                onCheckedChange = { viewModel.setReminderEnabled(it) }
            )
            Spacer(Modifier.height(8.dp))
            DropdownSelector(
                label = "Reminder interval (days)",
                options = REMINDER_PRESETS + listOf(-1),
                selected = if (REMINDER_PRESETS.contains(settings.reminderSettings.intervalDays))
                    settings.reminderSettings.intervalDays else -1,
                optionLabel = { if (it == -1) "Custom" else "$it days" },
                onSelected = { if (it != -1) viewModel.setReminderInterval(it) }
            )
            Spacer(Modifier.height(8.dp))
            LabeledTextField(
                value = customDays,
                onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) customDays = it },
                label = "Custom interval (1-90 days)",
                keyboardType = KeyboardType.Number
            )
            OutlinedButton(
                onClick = { customDays.toIntOrNull()?.let { viewModel.setReminderInterval(it) } },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save custom interval") }

            Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(16.dp))
            SectionLabel("Default vehicle")
            Spacer(Modifier.height(8.dp))
            if (state.vehicles.isEmpty()) {
                Text(
                    "No vehicles yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val active = state.activeVehicle
                DropdownSelector(
                    label = "Active vehicle",
                    options = state.vehicles,
                    selected = active ?: state.vehicles.first(),
                    optionLabel = { it.name },
                    onSelected = { viewModel.setActiveVehicle(it.id) }
                )
            }

            Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(16.dp))
            SectionLabel("Onboarding")
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.showOnboardingAgain() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Show onboarding again") }

            Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(16.dp))
            SectionLabel("Data export")
            Spacer(Modifier.height(4.dp))
            Text(
                text = DisclaimerText.EXPORT_NOTE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(16.dp))
            SectionLabel("Data management")
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { confirmResetVehicle = true },
                enabled = state.activeVehicle != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete refill records for active vehicle") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { confirmDeleteAll = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete all vehicles and refill records") }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { confirmResetAll = true },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset all local data") }

            Spacer(Modifier.height(16.dp)); Divider(); Spacer(Modifier.height(16.dp))
            SectionLabel("About Fuelnivo")
            Spacer(Modifier.height(6.dp))
            Text("Fuelnivo 1.0.0", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Record refills, review fuel usage, and keep a clear local history for your vehicle.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            SectionLabel("Manual tracking")
            Spacer(Modifier.height(4.dp))
            Text(
                DisclaimerText.MANUAL_TRACKING,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            SectionLabel("Privacy")
            Spacer(Modifier.height(4.dp))
            Text(
                DisclaimerText.PRIVACY,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmResetVehicle) {
        ConfirmDialog(
            title = "Delete records for active vehicle?",
            message = "All refill records for ${state.activeVehicle?.name ?: "this vehicle"} " +
                "will be permanently removed. The vehicle is kept.",
            confirmLabel = "Delete Records",
            destructive = true,
            onConfirm = {
                confirmResetVehicle = false
                state.activeVehicle?.let { viewModel.deleteRefillsForVehicle(it.id) }
            },
            onDismiss = { confirmResetVehicle = false }
        )
    }
    if (confirmDeleteAll) {
        ConfirmDialog(
            title = "Delete all vehicles and records?",
            message = "This will permanently remove every vehicle and all refill records " +
                "stored by Fuelnivo on this device.",
            confirmLabel = "Delete Everything",
            destructive = true,
            onConfirm = {
                confirmDeleteAll = false
                viewModel.deleteAllVehiclesAndRefills()
            },
            onDismiss = { confirmDeleteAll = false }
        )
    }
    if (confirmResetAll) {
        ConfirmDialog(
            title = "Reset all local data?",
            message = "This will permanently remove all vehicles, refill records, notes, and " +
                "settings stored by Fuelnivo on this device.",
            confirmLabel = "Reset Everything",
            destructive = true,
            onConfirm = {
                confirmResetAll = false
                viewModel.resetAllLocalData()
            },
            onDismiss = { confirmResetAll = false }
        )
    }
}
