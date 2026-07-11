package com.fuelnivo.app.ui.navigation

/** Type-safe route definitions for Navigation Compose. */
object Routes {
    const val ONBOARDING = "onboarding"

    const val HOME = "home"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
    const val VEHICLES = "vehicles"
    const val SETTINGS = "settings"

    const val MONTHLY = "monthly"

    const val ADD_REFILL = "add_refill"
    const val EDIT_REFILL = "edit_refill"
    const val REFILL_DETAIL = "refill_detail"

    const val ADD_VEHICLE = "add_vehicle"
    const val EDIT_VEHICLE = "edit_vehicle"

    const val ARG_REFILL_ID = "refillId"
    const val ARG_VEHICLE_ID = "vehicleId"

    fun refillDetail(id: String) = "$REFILL_DETAIL/$id"
    fun editRefill(id: String) = "$EDIT_REFILL/$id"
    fun editVehicle(id: String) = "$EDIT_VEHICLE/$id"
}

/** Bottom navigation destinations. */
enum class BottomDestination(val route: String, val label: String) {
    Home(Routes.HOME, "Home"),
    History(Routes.HISTORY, "History"),
    Statistics(Routes.STATISTICS, "Statistics"),
    Vehicles(Routes.VEHICLES, "Vehicles"),
    Settings(Routes.SETTINGS, "Settings")
}
