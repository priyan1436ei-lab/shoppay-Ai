package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.BusinessAnalyticsSummary
import com.example.data.model.DashboardMetrics
import com.example.data.model.IncomeForecast
import com.example.data.model.ShopProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeminiAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun askAssistant(
        userPrompt: String,
        profile: ShopProfile,
        metrics: DashboardMetrics,
        analytics: BusinessAnalyticsSummary,
        forecast: IncomeForecast
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        val curr = profile.currency

        // Construct factual ground truth
        val businessContext = """
            SHOP CONTEXT:
            Shop Name: ${profile.shopName}
            Owner: ${profile.ownerName}
            Category: ${profile.category}
            Currency: $curr
            Today's Income: $curr${String.format(Locale.US, "%,.2f", metrics.todayIncome)}
            Today's Transactions: ${metrics.todayTransactionCount}
            Today vs Yesterday: ${String.format(Locale.US, "%.1f", metrics.todayGrowthPercent)}%
            Monthly Revenue: $curr${String.format(Locale.US, "%,.2f", metrics.monthlyRevenue)}
            Monthly Target: $curr${String.format(Locale.US, "%,.2f", metrics.monthlyTarget)} (${metrics.targetProgressPercent}% achieved, $curr${String.format(Locale.US, "%,.2f", metrics.targetRemaining)} remaining)
            Average Transaction Value: $curr${String.format(Locale.US, "%,.2f", analytics.averageTransactionValue)}
            Highest Revenue Day: ${analytics.highestRevenueDay} ($curr${String.format(Locale.US, "%,.2f", analytics.highestRevenueAmount)})
            Lowest Revenue Day: ${analytics.lowestRevenueDay} ($curr${String.format(Locale.US, "%,.2f", analytics.lowestRevenueAmount)})
            Weekly Average Revenue: $curr${String.format(Locale.US, "%,.2f", analytics.weeklyAverage)}
            Daily Average Revenue: $curr${String.format(Locale.US, "%,.2f", analytics.dailyAverage)}
            Weekend Average Revenue: $curr${String.format(Locale.US, "%,.2f", analytics.weekendAverage)}
            Weekday Average Revenue: $curr${String.format(Locale.US, "%,.2f", analytics.weekdayAverage)}
            Dominant Payment Method: ${analytics.dominantPaymentMethod} (${analytics.dominantPaymentPercent}%)
            UPI Revenue: $curr${String.format(Locale.US, "%,.2f", metrics.upiTotal)} (${metrics.upiPercent}%)
            Card Revenue: $curr${String.format(Locale.US, "%,.2f", metrics.cardTotal)} (${metrics.cardPercent}%)
            Bank Transfer Revenue: $curr${String.format(Locale.US, "%,.2f", metrics.bankTotal)} (${metrics.bankPercent}%)
            Total All-Time Recorded Revenue: $curr${String.format(Locale.US, "%,.2f", analytics.totalRevenue)} across ${analytics.totalTransactions} transactions
            Expected Tomorrow: $curr${String.format(Locale.US, "%,.0f", forecast.expectedTomorrowMin)} - $curr${String.format(Locale.US, "%,.0f", forecast.expectedTomorrowMax)}
            Expected Next 7 Days: $curr${String.format(Locale.US, "%,.0f", forecast.expectedNext7DaysMin)} - $curr${String.format(Locale.US, "%,.0f", forecast.expectedNext7DaysMax)}
            Unusual Transactions flagged: ${metrics.unusualTransactionsCount}
        """.trimIndent()

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                
                val promptPayload = JSONObject().apply {
                    val contentsArray = JSONArray()
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray()
                        partsArray.put(JSONObject().put("text", """
                            You are ShopPay AI, a friendly, concise, and highly knowledgeable FinTech AI financial assistant for small shop owners.
                            Use ONLY the real financial context provided below to answer the user's question accurately.
                            Keep answers practical, encouraging, simple, without complex accounting jargon.
                            Always format monetary figures clearly with the shop currency ($curr).
                            
                            $businessContext
                            
                            User Question: $userPrompt
                        """.trimIndent()))
                        put("parts", partsArray)
                    }
                    contentsArray.put(contentObj)
                    put("contents", contentsArray)
                }

                val body = promptPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val json = JSONObject(responseStr)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text")
                            if (text.isNotBlank()) return@withContext text.trim()
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback to deterministic analytics
            }
        }

        // Deterministic Financial Intelligence Engine (Zero Hallucination)
        val p = userPrompt.lowercase()
        when {
            p.contains("earn") && (p.contains("week") || p.contains("this week") || p.contains("weekly")) -> {
                "You received approximately $curr${String.format(Locale.US, "%,.2f", analytics.weeklyAverage)} this week across your digital payment channels. Your daily average is $curr${String.format(Locale.US, "%,.2f", analytics.dailyAverage)}."
            }
            p.contains("best day") || p.contains("highest") || p.contains("top day") -> {
                "Your highest recorded revenue day was ${analytics.highestRevenueDay} with $curr${String.format(Locale.US, "%,.2f", analytics.highestRevenueAmount)} in total collections. Weekends are generally your peak period."
            }
            p.contains("payment method") || p.contains("upi") || p.contains("card") || p.contains("most") -> {
                "${analytics.dominantPaymentMethod} is your most popular payment method, accounting for ${analytics.dominantPaymentPercent}% of total revenue ($curr${String.format(Locale.US, "%,.2f", metrics.upiTotal)} via UPI, $curr${String.format(Locale.US, "%,.2f", metrics.cardTotal)} via Cards)."
            }
            p.contains("compare") || p.contains("month") || p.contains("target") -> {
                "Your current monthly revenue is $curr${String.format(Locale.US, "%,.2f", metrics.monthlyRevenue)}, achieving ${metrics.targetProgressPercent}% of your $curr${String.format(Locale.US, "%,.2f", metrics.monthlyTarget)} monthly goal. You have $curr${String.format(Locale.US, "%,.2f", metrics.targetRemaining)} remaining to hit target."
            }
            p.contains("forecast") || p.contains("expected") || p.contains("future") || p.contains("next week") -> {
                "Based on your recent 14-day transaction trend, your expected revenue for tomorrow is $curr${String.format(Locale.US, "%,.0f", forecast.expectedTomorrowMin)} – $curr${String.format(Locale.US, "%,.0f", forecast.expectedTomorrowMax)}, and next 7 days estimate is $curr${String.format(Locale.US, "%,.0f", forecast.expectedNext7DaysMin)} – $curr${String.format(Locale.US, "%,.0f", forecast.expectedNext7DaysMax)}."
            }
            p.contains("unusual") || p.contains("anomaly") || p.contains("review") -> {
                if (metrics.unusualTransactionsCount > 0) {
                    "You have ${metrics.unusualTransactionsCount} transaction(s) flagged as 'Needs Review' due to unusually high single-order amounts. Check the 'Needs Review' filter in Transactions."
                } else {
                    "No suspicious or unusual transactions are currently flagged in your store data. Everything looks normal."
                }
            }
            p.contains("weekend") || p.contains("weekday") -> {
                val diff = if (analytics.weekdayAverage > 0) {
                    (((analytics.weekendAverage - analytics.weekdayAverage) / analytics.weekdayAverage) * 100).toInt()
                } else 0
                "Your weekend average is $curr${String.format(Locale.US, "%,.2f", analytics.weekendAverage)} compared to $curr${String.format(Locale.US, "%,.2f", analytics.weekdayAverage)} on weekdays (a $diff% difference). Consider boosting fast-moving inventory before Friday."
            }
            else -> {
                "Today you have collected $curr${String.format(Locale.US, "%,.2f", metrics.todayIncome)} across ${metrics.todayTransactionCount} transactions (${String.format(Locale.US, "%+.1f", metrics.todayGrowthPercent)}% vs yesterday). Your average transaction size is $curr${String.format(Locale.US, "%,.2f", analytics.averageTransactionValue)}."
            }
        }
    }
}
