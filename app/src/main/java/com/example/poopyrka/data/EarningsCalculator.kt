package com.example.poopyrka.data

import kotlin.math.max
import kotlin.math.min

object EarningsCalculator {
    private const val THRESHOLD_LINES = 1000
    private const val COEFF_BASE = 1.0
    private const val COEFF_REDUCED = 0.8

    /**
     * Вычисляет сумму заработка на основе количества строк.
     * До 1000 строк — коэффициент 1.0
     * От 1000 до 1400 (и выше, согласно текущим вводным) — коэффициент 0.8
     */
    fun calculate(totalLines: Int): Double {
        val basePart = min(totalLines, THRESHOLD_LINES) * COEFF_BASE
        val reducedPart = max(0, totalLines - THRESHOLD_LINES) * COEFF_REDUCED
        return basePart + reducedPart
    }

    fun getCoeffLabel(totalLines: Int): String {
        return if (totalLines > THRESHOLD_LINES) "Коэф 0.8" else "Коэф 1.0"
    }
}
