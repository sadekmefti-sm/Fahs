package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.R
import com.example.core.i18n.AppLanguage
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
) {
    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.AR -> "$wilayaCode - $nameAr"
        AppLanguage.FR -> "$wilayaCode - $nameFr"
        AppLanguage.EN -> "$wilayaCode - $nameEn"
    }
}

/**
 * Public, read-only shop configuration loaded from Firestore `/shop_settings/config`
 * with an offline default copy bundled in `assets/data/shop_settings.json`.
 * No phone numbers, emails, or prices are hardcoded in Kotlin code.
 */
data class ShopSettings(
    val shopEnabled: Boolean = true,
    val whatsappNumber: String = "",
    val contactEmail: String = "",
    val donglePriceDzd: Int = 0,
    val inStock: Boolean = false,
    val deliveryFees: List<WilayaDeliveryFee> = emptyList()
) {
    fun isShopConfiguredAndEnabled(): Boolean =
        shopEnabled && whatsappNumber.isNotBlank() && donglePriceDzd > 0

    fun formattedDonglePrice(): String {
        if (donglePriceDzd <= 0) return "—"
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(donglePriceDzd)
        return "$formatted DZD"
    }

    fun formatDzd(amountDzd: Int): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(amountDzd.coerceAtLeast(0))
        return "$formatted DZD"
    }

    fun calculateOrderTotalDzd(
        wilaya: WilayaDeliveryFee?,
        isHomeDelivery: Boolean,
        quantity: Int
    ): Int {
        val safeQty = quantity.coerceIn(1, 20)
        val subtotal = donglePriceDzd.coerceAtLeast(0) * safeQty
        val delivery = if (wilaya != null) {
            if (isHomeDelivery) wilaya.homeFeeDzd else wilaya.stopDeskFeeDzd
        } else {
            0
        }
        return subtotal + delivery
    }

    fun buildOrderMessage(
        language: AppLanguage,
        wilaya: WilayaDeliveryFee,
        isHomeDelivery: Boolean,
        quantity: Int,
        customerName: String = "",
        customerPhone: String = "",
        addressNote: String = ""
    ): String {
        val safeQty = quantity.coerceIn(1, 20)
        val unitPrice = donglePriceDzd.coerceAtLeast(0)
        val subtotal = unitPrice * safeQty
        val deliveryFee = if (isHomeDelivery) wilaya.homeFeeDzd else wilaya.stopDeskFeeDzd
        val total = subtotal + deliveryFee
        val cleanName = customerName.trim()
        val cleanPhone = customerPhone.trim()
        val cleanNote = addressNote.trim()

        return when (language) {
            AppLanguage.AR -> buildString {
                appendLine("السلام عليكم، أود طلب فيشة الفحص ELM327 Bluetooth V1.5 من تطبيق فحص (Fahs):")
                appendLine("• المنتج: ELM327 Bluetooth Classic V1.5 (PIC18F25K80)")
                appendLine("• الكمية: $safeQty (${formatDzd(unitPrice)} للوحدة)")
                appendLine("• الولاية: ${wilaya.wilayaCode} - ${wilaya.nameAr} (${wilaya.nameFr})")
                val methodAr = if (isHomeDelivery) "توصيل إلى المنزل (À domicile)" else "مكتب التوصيل (Stop Desk)"
                appendLine("• طريقة التوصيل: $methodAr (${formatDzd(deliveryFee)})")
                appendLine("• المبلغ الإجمالي عند الاستلام: ${formatDzd(total)}")
                if (cleanName.isNotEmpty()) appendLine("• الاسم: $cleanName")
                if (cleanPhone.isNotEmpty()) appendLine("• رقم الهاتف: $cleanPhone")
                if (cleanNote.isNotEmpty()) appendLine("• البلدية / العنوان: $cleanNote")
            }.trim()

            AppLanguage.FR -> buildString {
                appendLine("Bonjour, je souhaite commander un boîtier ELM327 Bluetooth V1.5 depuis l'application Fahs :")
                appendLine("• Produit : ELM327 Bluetooth Classic V1.5 (PIC18F25K80)")
                appendLine("• Quantité : $safeQty (${formatDzd(unitPrice)} / unité)")
                appendLine("• Wilaya : ${wilaya.wilayaCode} - ${wilaya.nameFr}")
                val methodFr = if (isHomeDelivery) "Livraison à domicile" else "Bureau Stop Desk"
                appendLine("• Mode de livraison : $methodFr (${formatDzd(deliveryFee)})")
                appendLine("• Total à payer à la livraison : ${formatDzd(total)}")
                if (cleanName.isNotEmpty()) appendLine("• Nom : $cleanName")
                if (cleanPhone.isNotEmpty()) appendLine("• Téléphone : $cleanPhone")
                if (cleanNote.isNotEmpty()) appendLine("• Commune / Adresse : $cleanNote")
            }.trim()

            AppLanguage.EN -> buildString {
                appendLine("Hello, I would like to order an ELM327 Bluetooth V1.5 dongle from the Fahs app:")
                appendLine("• Product: ELM327 Bluetooth Classic V1.5 (PIC18F25K80)")
                appendLine("• Quantity: $safeQty (${formatDzd(unitPrice)} each)")
                appendLine("• Wilaya: ${wilaya.wilayaCode} - ${wilaya.nameEn}")
                val methodEn = if (isHomeDelivery) "Home Delivery" else "Stop Desk Office"
                appendLine("• Delivery Method: $methodEn (${formatDzd(deliveryFee)})")
                appendLine("• Total Cash on Delivery: ${formatDzd(total)}")
                if (cleanName.isNotEmpty()) appendLine("• Name: $cleanName")
                if (cleanPhone.isNotEmpty()) appendLine("• Phone: $cleanPhone")
                if (cleanNote.isNotEmpty()) appendLine("• Commune / Address: $cleanNote")
            }.trim()
        }
    }

    fun buildWhatsAppUri(orderMessage: String): String {
        val digitsOnly = whatsappNumber.filter { it.isDigit() }
        val encodedText = Uri.encode(orderMessage)
        return "https://wa.me/$digitsOnly?text=$encodedText"
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
                if (parsed.deliveryFees.isNotEmpty() && parsed.whatsappNumber.isNotBlank()) {
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

    internal fun parseShopSettingsJson(jsonString: String): ShopSettings {
        val root = JSONObject(jsonString)
        val shopEnabled = if (root.has("shopEnabled")) root.optBoolean("shopEnabled", true) else true
        val whatsappNumber = root.optString("whatsappNumber", "").trim()
        val contactEmail = root.optString("contactEmail", "").trim()
        val donglePriceDzd = root.optInt("donglePriceDzd", 0).coerceAtLeast(0)
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
                        homeFeeDzd = item.optInt("homeFeeDzd", 0).coerceAtLeast(0),
                        stopDeskFeeDzd = item.optInt("stopDeskFeeDzd", 0).coerceAtLeast(0)
                    )
                )
            }
        }

        return ShopSettings(
            shopEnabled = shopEnabled,
            whatsappNumber = whatsappNumber,
            contactEmail = contactEmail,
            donglePriceDzd = donglePriceDzd,
            inStock = inStock,
            deliveryFees = feesList
        )
    }

    internal fun encodeShopSettingsJson(settings: ShopSettings): String {
        val root = JSONObject()
        root.put("shopEnabled", settings.shopEnabled)
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
            val shopEnabled = snapshot.getBoolean("shopEnabled") ?: bundled.shopEnabled
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
                        homeFeeDzd = (map["homeFeeDzd"] as? Number)?.toInt() ?: bundledMatch?.homeFeeDzd ?: 0,
                        stopDeskFeeDzd = (map["stopDeskFeeDzd"] as? Number)?.toInt() ?: bundledMatch?.stopDeskFeeDzd ?: 0
                    )
                }
            } else {
                bundled.deliveryFees
            }

            val updated = ShopSettings(
                shopEnabled = shopEnabled,
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
