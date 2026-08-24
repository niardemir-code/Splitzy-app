package com.apleq.app.data.util

import android.util.Log
import com.apleq.app.data.model.CurrencyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object CurrencyRateService {

    private const val TAG = "CurrencyRateService"
    private const val API_URL = "https://open.er-api.com/v6/latest/EUR"

    suspend fun fetchLatestRates(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val json = JSONObject(response.toString())
                if (json.has("rates")) {
                    val ratesObj = json.getJSONObject("rates")
                    val ratesMap = mutableMapOf<String, Double>()
                    val keys = ratesObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val rateValue = ratesObj.optDouble(key, 0.0)
                        if (rateValue > 0.0) {
                            ratesMap[key] = rateValue
                        }
                    }
                    CurrencyManager.updateRates(ratesMap)
                    Log.d(TAG, "Currency exchange rates successfully updated with ${ratesMap.size} currencies.")
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch live currency rates (using fallback rates): ${e.message}")
        }
        return@withContext false
    }
}
