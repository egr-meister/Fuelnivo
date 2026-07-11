package com.fuelnivo.app.util

/** Centralized product-positioning and privacy text. */
object DisclaimerText {

    const val MANUAL_TRACKING: String =
        "Fuelnivo is a manual fuel log. Vehicle details, odometer values, fuel " +
            "quantities, prices, costs, and notes are entered by the user. The app " +
            "does not connect to a vehicle, OBD system, GPS service, fuel station, " +
            "or financial service."

    const val PRIVACY: String =
        "Fuelnivo stores vehicles, refill records, odometer values, fuel quantities, " +
            "costs, notes, preferences, and reminder settings locally on this device. " +
            "The app has no account, no cloud sync, no internet access, no ads, no " +
            "analytics, no payments, no location tracking, no vehicle connection, and " +
            "no OBD integration."

    const val TANK_NOTE: String = "Based on your latest manual refill"

    const val EXPORT_NOTE: String =
        "Data export is not available in this version. Your records remain on this " +
            "device only."
}
