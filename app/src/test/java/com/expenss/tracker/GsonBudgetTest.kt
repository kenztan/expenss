package com.expenss.tracker

import com.expenss.tracker.data.network.Budget
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import org.junit.Assert.assertEquals
import org.junit.Test

class GsonBudgetTest {

    private val doubleDeserializer = JsonDeserializer<Double> { json, _, _ ->
        runCatching {
            val p = json.asJsonPrimitive
            if (p.isNumber) p.asDouble else p.asString.toDouble()
        }.getOrDefault(0.0)
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(Double::class.javaObjectType, doubleDeserializer)
        .registerTypeAdapter(Double::class.java, doubleDeserializer)
        .create()

    @Test
    fun parsesDecimalString() {
        // Exact shape the NestJS backend returns: select { amount: true } → string
        val json = """{"amount":"1234.00"}"""
        val b = gson.fromJson(json, Budget::class.java)
        println("PARSED amount = ${b.amount}")
        assertEquals(1234.0, b.amount, 0.0001)
    }

    @Test
    fun parsesNumber() {
        val json = """{"amount":1234.5}"""
        val b = gson.fromJson(json, Budget::class.java)
        println("PARSED number amount = ${b.amount}")
        assertEquals(1234.5, b.amount, 0.0001)
    }
}
