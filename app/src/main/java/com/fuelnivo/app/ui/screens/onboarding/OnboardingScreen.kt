package com.fuelnivo.app.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fuelnivo.app.ui.components.FuelTankView
import com.fuelnivo.app.util.DisclaimerText

@Composable
fun OnboardingScreen(onFinish: (setUpVehicle: Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FuelTankView(
            fillFraction = 0.62f,
            percentLabel = "62%",
            overCapacity = false,
            isEmpty = false,
            contentDescription = "Fuelnivo segmented fuel tank illustration",
            width = 160.dp,
            height = 190.dp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Fuelnivo",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Build a clear fuel history.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        OnboardingPoint(
            title = "Manual refill tracking",
            body = "Add your vehicle, record refills, and review your manually entered data. " +
                "Odometer values, fuel quantities, and prices are entered by you."
        )
        OnboardingPoint(
            title = "Full-tank consumption",
            body = "Consumption is calculated from valid full-tank records. Partial refills " +
                "between full tanks are included, and missed refills are excluded."
        )
        OnboardingPoint(
            title = "Local-only data",
            body = "Your data stays on this device. There is no account, no internet use, " +
                "and no cloud sync."
        )
        OnboardingPoint(
            title = "In-app reminders",
            body = "Fuelnivo reminders appear inside the app. It does not send push " +
                "notifications or run in the background."
        )
        OnboardingPoint(
            title = "No vehicle connection",
            body = "Fuelnivo does not connect to your vehicle or OBD system. It is a manual " +
                "fuel journal."
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = DisclaimerText.MANUAL_TRACKING,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onFinish(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Set Up Vehicle")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onFinish(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Explore First")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OnboardingPoint(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
