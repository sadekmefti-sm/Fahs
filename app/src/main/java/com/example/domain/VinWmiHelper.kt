package com.example.domain

import com.example.core.i18n.AppLanguage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Decodes the first 3 characters (WMI — World Manufacturer Identifier) of a 17-character
 * vehicle identification number (VIN) read by the OBD scan into the vehicle brand sold in Algeria,
 * and builds clean date-and-time report titles when the user skips optional car selection.
 */
object VinWmiHelper {

    private val wmiToBrandMap: Map<String, String> = mapOf(
        // Renault & Dacia (France, Romania, Algeria Oued Tlélat, Turkey, Morocco)
        "VF1" to "Renault",
        "VN1" to "Renault",
        "VNE" to "Renault",
        "KNM" to "Renault",
        "UU1" to "Dacia",
        // Peugeot & Citroën (PSA)
        "VF3" to "Peugeot",
        "VF7" to "Citroën",
        "VR7" to "Citroën",
        "VR3" to "Peugeot",
        // Volkswagen, Seat, Skoda, Audi (VAG)
        "WVW" to "Volkswagen",
        "WVG" to "Volkswagen",
        "WV1" to "Volkswagen",
        "WV2" to "Volkswagen",
        "3VW" to "Volkswagen",
        "VSS" to "Seat",
        "TMB" to "Skoda",
        "WAU" to "Audi",
        // Hyundai & Kia
        "KMH" to "Hyundai",
        "KMF" to "Hyundai",
        "MAL" to "Hyundai",
        "NLH" to "Hyundai",
        "TMA" to "Hyundai",
        "KNA" to "Kia",
        "KNC" to "Kia",
        "KND" to "Kia",
        "KNE" to "Kia",
        "U5Y" to "Kia",
        // Toyota & Japanese brands
        "JTD" to "Toyota",
        "JT1" to "Toyota",
        "JTE" to "Toyota",
        "SB1" to "Toyota",
        "NMT" to "Toyota",
        "VNK" to "Toyota",
        "MR0" to "Toyota",
        "AHT" to "Toyota",
        "JN1" to "Nissan",
        "SJN" to "Nissan",
        "VSK" to "Nissan",
        "MNT" to "Nissan",
        "MDH" to "Nissan",
        "JS1" to "Suzuki",
        "JS2" to "Suzuki",
        "JS3" to "Suzuki",
        "MA3" to "Suzuki",
        "TSM" to "Suzuki",
        "JMB" to "Mitsubishi",
        "MMB" to "Mitsubishi",
        "MMT" to "Mitsubishi",
        // Fiat, Opel, Ford, Chevrolet
        "ZFA" to "Fiat",
        "ZFC" to "Fiat",
        "NM4" to "Fiat",
        "W0L" to "Opel",
        "W0V" to "Opel",
        "WF0" to "Ford",
        "KL1" to "Chevrolet",
        "KLG" to "Chevrolet",
        "LSG" to "Chevrolet",
        "1G1" to "Chevrolet",
        // Chinese & Newer Algerian Market Imports (Chery, Geely, MG, Haval/Great Wall, DFSK, JAC, Mahindra)
        "LVV" to "Chery",
        "L6T" to "Geely",
        "LB3" to "Geely",
        "LSJ" to "MG",
        "LZW" to "MG",
        "LGW" to "Great Wall / Haval",
        "LVG" to "DFSK",
        "LJ1" to "JAC",
        "MA1" to "Mahindra",
        // Mercedes-Benz & BMW
        "WDB" to "Mercedes-Benz",
        "WDD" to "Mercedes-Benz",
        "W1K" to "Mercedes-Benz",
        "WBA" to "BMW",
        "WBS" to "BMW"
    )

    /**
     * Returns the detected car brand from the first 3 characters of a VIN, or null if unknown.
     */
    fun detectBrandFromVin(vin: String?): String? {
        val cleaned = vin?.trim()?.uppercase(Locale.US)?.filter { it.isLetterOrDigit() }.orEmpty()
        if (cleaned.length < 3) return null
        val wmi = cleaned.substring(0, 3)
        return wmiToBrandMap[wmi]
    }

    /**
     * Formats a clean date-and-time label for an inspection when no car model was manually selected.
     */
    fun formatTimestampLabel(timestampMs: Long, language: AppLanguage): String {
        val locale = when (language) {
            AppLanguage.AR -> Locale.forLanguageTag("ar-DZ")
            AppLanguage.FR -> Locale.FRENCH
            AppLanguage.EN -> Locale.ENGLISH
        }
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", locale)
        return sdf.format(Date(timestampMs))
    }

    /**
     * Returns true when an inspection was started without choosing a specific car model.
     */
    fun isUnselectedCar(brand: String, model: String): Boolean {
        val b = brand.trim()
        val m = model.trim()
        return (b.isBlank() || b.equals("Inspection", ignoreCase = true) || b.equals("Scan", ignoreCase = true) || b.equals("Voiture", ignoreCase = true) || b.equals("سيارة", ignoreCase = true)) &&
            (m.isBlank() || m.contains(":"))
    }
}
