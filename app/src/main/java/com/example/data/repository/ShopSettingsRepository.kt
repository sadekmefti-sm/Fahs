package com.example.data.repository

import android.content.Context
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

data class WilayaDeliveryFee(
    val wilayaCode: String,
    val nameFr: String,
    val nameAr: String,
    val nameEn: String,
    val zone: String,
    val homeFeeDzd: Int,
    val stopDeskFeeDzd: Int
)

data class ShopSettings(
    val whatsappNumber: String = "+971524830059",
    val contactEmail: String = "sadekmefti@gmail.com",
    val donglePriceDzd: Int = 2500,
    val inStock: Boolean = true,
    val deliveryFees: List<WilayaDeliveryFee> = emptyList()
) {
    fun formattedDonglePrice(): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(donglePriceDzd)
        return "$formatted DZD"
    }
}

class ShopSettingsRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("fahs_shop_settings_cache", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadCachedOrBundled())
    val settings: StateFlow<ShopSettings> = _settings.asStateFlow()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            if (FirebaseApp.getApps(context).isEmpty()) {
                null
            } else {
                val dbId = try {
                    context.getString(R.string.firestore_database_id)
                } catch (_: Exception) {
                    "(default)"
                }
                if (dbId.isNotBlank() && dbId != "(default)") {
                    FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
                } else {
                    FirebaseFirestore.getInstance()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Loads the latest cached shop settings if available, or falls back to the bundled
     * `assets/data/shop_settings.json` copy for offline and first-launch use.
     */
    fun loadCachedOrBundled(): ShopSettings {
        val cachedJson = prefs.getString("cached_shop_settings_json", null)
        if (!cachedJson.isNullOrBlank()) {
            try {
                val parsed = parseShopSettingsJson(cachedJson)
                if (parsed.deliveryFees.isNotEmpty()) {
                    return parsed
                }
            } catch (_: Exception) {
                // Fall back to bundled JSON
            }
        }
        return loadBundledDefault()
    }

    fun loadBundledDefault(): ShopSettings {
        return try {
            val raw = context.assets.open("data/shop_settings.json").bufferedReader().use { it.readText() }
            parseShopSettingsJson(raw)
        } catch (_: Exception) {
            ShopSettings()
        }
    }

    private fun parseShopSettingsJson(jsonString: String): ShopSettings {
        val root = JSONObject(jsonString)
        val whatsappNumber = root.optString("whatsappNumber", "+971524830059").ifBlank { "+971524830059" }
        val contactEmail = root.optString("contactEmail", "sadekmefti@gmail.com").ifBlank { "sadekmefti@gmail.com" }
        val donglePriceDzd = root.optInt("donglePriceDzd", 2500).let { if (it > 0) it else 2500 }
        val inStock = if (root.has("inStock")) root.optBoolean("inStock", true) else true

        val feesList = mutableListOf<WilayaDeliveryFee>()
        val feesArray = root.optJSONArray("deliveryFees")
        if (feesArray != null) {
            for (i in 0 until feesArray.length()) {
                val item = feesArray.optJSONObject(i) ?: continue
                val code = item.optString("wilayaCode", "").trim()
                if (code.isEmpty()) continue
                feesList.add(
                    WilayaDeliveryFee(
                        wilayaCode = code,
                        nameFr = item.optString("nameFr", code),
                        nameAr = item.optString("nameAr", code),
                        nameEn = item.optString("nameEn", code),
                        zone = item.optString("zone", "NORTH"),
                        homeFeeDzd = item.optInt("homeFeeDzd", 800),
                        stopDeskFeeDzd = item.optInt("stopDeskFeeDzd", 450)
                    )
                )
            }
        }

        return ShopSettings(
            whatsappNumber = whatsappNumber,
            contactEmail = contactEmail,
            donglePriceDzd = donglePriceDzd,
            inStock = inStock,
            deliveryFees = feesList
        )
    }

    private fun encodeShopSettingsJson(settings: ShopSettings): String {
        val root = JSONObject()
        root.put("whatsappNumber", settings.whatsappNumber)
        root.put("contactEmail", settings.contactEmail)
        root.put("donglePriceDzd", settings.donglePriceDzd)
        root.put("inStock", settings.inStock)

        val feesArray = JSONArray()
        for (fee in settings.deliveryFees) {
            val item = JSONObject()
            item.put("wilayaCode", fee.wilayaCode)
            item.put("nameFr", fee.nameFr)
            item.put("nameAr", fee.nameAr)
            item.put("nameEn", fee.nameEn)
            item.put("zone", fee.zone)
            item.put("homeFeeDzd", fee.homeFeeDzd)
            item.put("stopDeskFeeDzd", fee.stopDeskFeeDzd)
            feesArray.put(item)
        }
        root.put("deliveryFees", feesArray)
        return root.toString()
    }

    /**
     * Refreshes the local copy from Firestore `/shop_settings/config` whenever online.
     * Read-only from the app: if the remote document exists, merges any updated fields
     * with the bundled defaults and saves to SharedPreferences.
     */
    suspend fun refreshFromFirestore(): ShopSettings = withContext(Dispatchers.IO) {
        val current = loadCachedOrBundled()
        _settings.value = current
        val db = firestore ?: return@withContext current
        try {
            val snapshot = db.collection("shop_settings").document("config").get().await()
            if (!snapshot.exists()) {
                return@withContext current
            }
            val bundled = loadBundledDefault()
            val whatsappNumber = (snapshot.getString("whatsappNumber"))?.trim()?.takeIf { it.isNotEmpty() }
                ?: bundled.whatsappNumber
            val contactEmail = (snapshot.getString("contactEmail"))?.trim()?.takeIf { it.isNotEmpty() }
                ?: bundled.contactEmail
            val donglePriceDzd = (snapshot.getLong("donglePriceDzd")?.toInt())
                ?.takeIf { it > 0 }
                ?: bundled.donglePriceDzd
            val inStock = snapshot.getBoolean("inStock") ?: bundled.inStock

            @Suppress("UNCHECKED_CAST")
            val rawFees = snapshot.get("deliveryFees") as? List<Map<String, Any?>>
            val parsedFees = if (!rawFees.isNullOrEmpty()) {
                rawFees.mapNotNull { map ->
                    val code = (map["wilayaCode"] as? String)?.trim() ?: return@mapNotNull null
                    val bundledMatch = bundled.deliveryFees.find { it.wilayaCode == code }
                    WilayaDeliveryFee(
                        wilayaCode = code,
                        nameFr = (map["nameFr"] as? String) ?: bundledMatch?.nameFr ?: code,
                        nameAr = (map["nameAr"] as? String) ?: bundledMatch?.nameAr ?: code,
                        nameEn = (map["nameEn"] as? String) ?: bundledMatch?.nameEn ?: code,
                        zone = (map["zone"] as? String) ?: bundledMatch?.zone ?: "NORTH",
                        homeFeeDzd = (map["homeFeeDzd"] as? Number)?.toInt() ?: bundledMatch?.homeFeeDzd ?: 800,
                        stopDeskFeeDzd = (map["stopDeskFeeDzd"] as? Number)?.toInt() ?: bundledMatch?.stopDeskFeeDzd ?: 450
                    )
                }
            } else {
                bundled.deliveryFees
            }

            val updated = ShopSettings(
                whatsappNumber = whatsappNumber,
                contactEmail = contactEmail,
                donglePriceDzd = donglePriceDzd,
                inStock = inStock,
                deliveryFees = if (parsedFees.isNotEmpty()) parsedFees else bundled.deliveryFees
            )
            prefs.edit().putString("cached_shop_settings_json", encodeShopSettingsJson(updated)).apply()
            _settings.value = updated
            updated
        } catch (_: Exception) {
            current
        }
    }
}
