package com.sharxpenses.domain

import com.sharxpenses.data.remote.CustomShare
import com.sharxpenses.data.remote.PercentShare
import com.sharxpenses.data.remote.SplitMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor
import kotlin.math.roundToInt

@Singleton
class Splitter @Inject constructor() {

    /**
     * Retorna mapa userId -> valor em centavos (Long)
     */
    fun split(
        totalAmount: Double,
        participants: List<String>,
        mode: SplitMode,
        percents: List<PercentShare>? = null,
        custom: List<CustomShare>? = null
    ): Map<String, Long> {
        val total = (totalAmount * 100.0).roundToInt()
        require(total >= 0) { "Valor total inválido" }
        require(participants.isNotEmpty()) { "Participantes vazios" }

        return when (mode) {
            SplitMode.EQUAL -> splitEqual(total, participants)
            SplitMode.PERCENT -> splitPercent(total, participants, percents ?: emptyList())
            SplitMode.CUSTOM -> splitCustom(total, participants, custom ?: emptyList())
        }
    }

    private fun splitEqual(totalCents: Int, participants: List<String>): Map<String, Long> {
        val n = participants.size
        val base = floor(totalCents.toDouble() / n).toLong()
        var remainder = (totalCents - base * n).toInt()
        val out = LinkedHashMap<String, Long>(n)
        for (i in participants.indices) {
            var share = base
            if (remainder > 0) {
                share += 1
                remainder -= 1
            }
            out[participants[i]] = share
        }
        return out
    }

    private fun splitPercent(totalCents: Int, participants: List<String>, percents: List<PercentShare>): Map<String, Long> {
        require(percents.isNotEmpty()) { "Percents vazios" }
        val map = percents.associateBy { it.userId }
        val sum = percents.sumOf { it.percent }
        require(kotlin.math.abs(sum - 100.0) < 0.0001) { "Percentuais precisam somar 100%" }

        val provisional = mutableMapOf<String, Long>()
        var assigned = 0L
        for (p in participants) {
            val pc = map[p]?.percent ?: 0.0
            val cents = floor(totalCents * (pc / 100.0)).toLong()
            provisional[p] = cents
            assigned += cents
        }
        var remainder = totalCents - assigned
        val sorted = participants.sortedByDescending { map[it]?.percent ?: 0.0 }
        for (u in sorted) {
            if (remainder <= 0) break
            provisional[u] = (provisional[u] ?: 0) + 1
            remainder -= 1
        }
        return provisional
    }

    private fun splitCustom(totalCents: Int, participants: List<String>, custom: List<CustomShare>): Map<String, Long> {
        require(custom.isNotEmpty()) { "CustomShares vazios" }
        val map = custom.associate { it.userId to (it.amount * 100.0).toLong() }.toMutableMap()
        val sum = map.values.sum()
        require(sum == totalCents.toLong()) { "Soma de shares custom não bate com total" }

        // garante todos os participantes presentes (mesmo 0)
        for (p in participants) {
            map.putIfAbsent(p, 0)
        }
        return map
    }
}