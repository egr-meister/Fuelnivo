package com.fuelnivo.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fuelnivo.app.ui.theme.WarmYellow
import com.fuelnivo.app.util.ReminderState

/** In-app reminder panel. No push notifications or background work involved. */
@Composable
fun ReminderPanel(
    state: ReminderState,
    onAddRefill: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, body) = when (state) {
        is ReminderState.None -> return
        is ReminderState.FirstRecordPrompt ->
            "Start your fuel log?" to "You have not recorded any refills for this vehicle yet."
        is ReminderState.Due ->
            "Time to update your fuel log?" to
                "It has been ${state.daysSince} days since your last recorded refill."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
            .background(WarmYellow.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAddRefill) { Text("Add Refill") }
            TextButton(onClick = onDismiss) { Text("Not Now") }
        }
    }
}
