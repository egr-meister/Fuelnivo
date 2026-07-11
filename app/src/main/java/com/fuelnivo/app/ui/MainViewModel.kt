package com.fuelnivo.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fuelnivo.app.data.AppData
import com.fuelnivo.app.data.AppSettings
import com.fuelnivo.app.data.FuelnivoRepository
import com.fuelnivo.app.data.RefillRecord
import com.fuelnivo.app.data.ReminderSettings
import com.fuelnivo.app.data.Vehicle
import com.fuelnivo.app.util.DateUtils
import com.fuelnivo.app.util.Reminder
import com.fuelnivo.app.util.ReminderState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Immutable UI state shared across screens. */
data class AppUiState(
    val loaded: Boolean = false,
    val data: AppData = AppData(),
    val reminderDismissed: Boolean = false
) {
    val vehicles: List<Vehicle> get() = data.vehicles
    val refills: List<RefillRecord> get() = data.refills
    val settings: AppSettings get() = data.settings

    val activeVehicle: Vehicle?
        get() {
            val id = data.settings.activeVehicleId
            return data.vehicles.firstOrNull { it.id == id } ?: data.vehicles.firstOrNull()
        }

    fun refillsForVehicle(vehicleId: String?): List<RefillRecord> =
        if (vehicleId == null) emptyList() else data.refills.filter { it.vehicleId == vehicleId }

    fun vehicleById(id: String?): Vehicle? =
        if (id == null) null else data.vehicles.firstOrNull { it.id == id }

    fun refillById(id: String?): RefillRecord? =
        if (id == null) null else data.refills.firstOrNull { it.id == id }

    val reminderState: ReminderState
        get() {
            val vehicle = activeVehicle ?: return ReminderState.None
            return Reminder.evaluate(
                enabled = settings.reminderSettings.enabled,
                intervalDays = settings.reminderSettings.intervalDays,
                hasActiveVehicle = true,
                vehicleRefills = refillsForVehicle(vehicle.id),
                today = DateUtils.today(),
                dismissedThisSession = reminderDismissed
            )
        }
}

/**
 * Single shared ViewModel. Owns the repository, exposes a combined [uiState],
 * and provides guarded mutation helpers. No DI framework is used.
 */
class MainViewModel(private val repository: FuelnivoRepository) : ViewModel() {

    private val reminderDismissed = MutableStateFlow(false)

    val uiState: StateFlow<AppUiState> =
        combine(repository.appData, reminderDismissed) { data, dismissed ->
            AppUiState(loaded = true, data = data, reminderDismissed = dismissed)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState()
        )

    // ---- Reminder session control -----------------------------------------

    fun dismissReminder() {
        reminderDismissed.value = true
    }

    private fun resetReminderDismissal() {
        reminderDismissed.value = false
    }

    // ---- Settings ----------------------------------------------------------

    fun completeOnboarding() = launchIo { repository.setOnboardingCompleted(true) }

    fun showOnboardingAgain() = launchIo { repository.setOnboardingCompleted(false) }

    fun setActiveVehicle(id: String?) = launchIo {
        repository.setActiveVehicle(id)
        resetReminderDismissal()
    }

    fun setCurrencyCode(code: String) = launchIo {
        val safe = code.trim().uppercase().take(6).ifBlank { "USD" }
        repository.updateSettings { it.copy(currencyCode = safe) }
    }

    fun setReminderEnabled(enabled: Boolean) = launchIo {
        repository.updateSettings {
            it.copy(reminderSettings = it.reminderSettings.copy(enabled = enabled))
        }
    }

    fun setReminderInterval(days: Int) = launchIo {
        val safe = days.coerceIn(1, 90)
        repository.updateSettings {
            it.copy(reminderSettings = it.reminderSettings.copy(intervalDays = safe))
        }
    }

    fun saveReminderSettings(settings: ReminderSettings) = launchIo {
        repository.updateSettings { it.copy(reminderSettings = settings) }
    }

    // ---- Vehicles ----------------------------------------------------------

    fun addVehicle(vehicle: Vehicle) = launchIo {
        repository.addVehicle(vehicle)
        resetReminderDismissal()
    }

    fun updateVehicle(vehicle: Vehicle) = launchIo { repository.updateVehicle(vehicle) }

    fun replaceVehicleAndRefills(vehicle: Vehicle, refills: List<RefillRecord>) = launchIo {
        repository.replaceVehicleAndRefills(vehicle, refills)
    }

    fun deleteVehicle(vehicleId: String) = launchIo {
        repository.deleteVehicle(vehicleId)
        resetReminderDismissal()
    }

    // ---- Refills -----------------------------------------------------------

    fun addRefill(refill: RefillRecord) = launchIo {
        repository.addRefill(refill)
        resetReminderDismissal()
    }

    fun updateRefill(refill: RefillRecord) = launchIo {
        repository.updateRefill(refill)
        resetReminderDismissal()
    }

    fun deleteRefill(refillId: String) = launchIo { repository.deleteRefill(refillId) }

    fun deleteRefillsForVehicle(vehicleId: String) = launchIo {
        repository.deleteRefillsForVehicle(vehicleId)
    }

    // ---- Destructive resets ------------------------------------------------

    fun deleteAllVehiclesAndRefills() = launchIo {
        repository.deleteAllVehiclesAndRefills()
        resetReminderDismissal()
    }

    fun resetAllLocalData() = launchIo {
        repository.resetAllLocalData()
        resetReminderDismissal()
    }

    private fun launchIo(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    /** Simple factory that injects the repository without a DI framework. */
    class Factory(private val repository: FuelnivoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
