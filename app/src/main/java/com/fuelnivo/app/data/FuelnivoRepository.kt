package com.fuelnivo.app.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "FuelnivoRepo"

// Single DataStore instance for the whole process.
private val Context.dataStore by preferencesDataStore(name = "fuelnivo_store")

/**
 * The single local repository for Fuelnivo. All data is stored as serialized
 * JSON strings inside DataStore Preferences. Reads are defensive: corrupted or
 * missing values fall back to safe defaults instead of crashing.
 */
class FuelnivoRepository(private val context: Context) {

    private object Keys {
        val VEHICLES = stringPreferencesKey("vehicles_json")
        val REFILLS = stringPreferencesKey("refills_json")
        val SETTINGS = stringPreferencesKey("settings_json")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    /** Observable snapshot of all app data, reassembled on every change. */
    val appData: Flow<AppData> = context.dataStore.data.map { prefs -> prefs.toAppData() }

    private fun Preferences.toAppData(): AppData = AppData(
        vehicles = decodeVehicles(this[Keys.VEHICLES]),
        refills = decodeRefills(this[Keys.REFILLS]),
        settings = decodeSettings(this[Keys.SETTINGS])
    )

    // ---- Decoding helpers (never throw into the flow) ----------------------

    private fun decodeVehicles(raw: String?): List<Vehicle> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<Vehicle>>(raw)
        } catch (e: Exception) {
            Log.w(TAG, "Vehicle JSON could not be parsed; using empty list. ${e.javaClass.simpleName}")
            emptyList()
        }
    }

    private fun decodeRefills(raw: String?): List<RefillRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<RefillRecord>>(raw)
        } catch (e: Exception) {
            Log.w(TAG, "Refill JSON could not be parsed; using empty list. ${e.javaClass.simpleName}")
            emptyList()
        }
    }

    private fun decodeSettings(raw: String?): AppSettings {
        if (raw.isNullOrBlank()) return AppSettings()
        return try {
            json.decodeFromString<AppSettings>(raw)
        } catch (e: Exception) {
            Log.w(TAG, "Settings JSON could not be parsed; using defaults. ${e.javaClass.simpleName}")
            AppSettings()
        }
    }

    // ---- Snapshot readers --------------------------------------------------

    private suspend fun snapshot(): AppData = appData.first()

    // ---- Low-level writers -------------------------------------------------

    private suspend fun persistVehicles(vehicles: List<Vehicle>) {
        context.dataStore.edit { it[Keys.VEHICLES] = json.encodeToString(vehicles) }
    }

    private suspend fun persistRefills(refills: List<RefillRecord>) {
        context.dataStore.edit { it[Keys.REFILLS] = json.encodeToString(refills) }
    }

    private suspend fun persistSettings(settings: AppSettings) {
        context.dataStore.edit { it[Keys.SETTINGS] = json.encodeToString(settings) }
    }

    // ---- Public settings API ----------------------------------------------

    suspend fun saveSettings(settings: AppSettings) = persistSettings(settings)

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        persistSettings(transform(snapshot().settings))
    }

    suspend fun setActiveVehicle(vehicleId: String?) {
        updateSettings { it.copy(activeVehicleId = vehicleId) }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        updateSettings { it.copy(onboardingCompleted = completed) }
    }

    // ---- Public vehicle API ------------------------------------------------

    suspend fun addVehicle(vehicle: Vehicle) {
        val data = snapshot()
        persistVehicles(data.vehicles + vehicle)
        // First vehicle becomes active automatically.
        if (data.settings.activeVehicleId == null) {
            persistSettings(data.settings.copy(activeVehicleId = vehicle.id))
        }
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        val list = snapshot().vehicles.map { if (it.id == vehicle.id) vehicle else it }
        persistVehicles(list)
    }

    /** Replaces the stored vehicle and all its refills (used by unit conversion). */
    suspend fun replaceVehicleAndRefills(vehicle: Vehicle, updatedRefills: List<RefillRecord>) {
        val data = snapshot()
        val vehicles = data.vehicles.map { if (it.id == vehicle.id) vehicle else it }
        val others = data.refills.filter { it.vehicleId != vehicle.id }
        persistVehicles(vehicles)
        persistRefills(others + updatedRefills)
    }

    suspend fun deleteVehicle(vehicleId: String) {
        val data = snapshot()
        val vehicles = data.vehicles.filterNot { it.id == vehicleId }
        val refills = data.refills.filterNot { it.vehicleId == vehicleId }
        persistVehicles(vehicles)
        persistRefills(refills)
        // Reassign active vehicle if the deleted one was active.
        if (data.settings.activeVehicleId == vehicleId) {
            persistSettings(data.settings.copy(activeVehicleId = vehicles.firstOrNull()?.id))
        }
    }

    // ---- Public refill API -------------------------------------------------

    suspend fun addRefill(refill: RefillRecord) {
        persistRefills(snapshot().refills + refill)
    }

    suspend fun updateRefill(refill: RefillRecord) {
        val list = snapshot().refills.map { if (it.id == refill.id) refill else it }
        persistRefills(list)
    }

    suspend fun deleteRefill(refillId: String) {
        persistRefills(snapshot().refills.filterNot { it.id == refillId })
    }

    suspend fun deleteRefillsForVehicle(vehicleId: String) {
        persistRefills(snapshot().refills.filterNot { it.vehicleId == vehicleId })
    }

    // ---- Destructive resets ------------------------------------------------

    suspend fun deleteAllVehiclesAndRefills() {
        persistVehicles(emptyList())
        persistRefills(emptyList())
        updateSettings { it.copy(activeVehicleId = null) }
    }

    suspend fun resetAllLocalData() {
        context.dataStore.edit { it.clear() }
    }
}
