package com.example

import com.example.data.repository.SrsRating
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.max

class EnglishLearningUnitTest {

    @Test
    fun testSrsSm2AlgorithmProgression() {
        // Test Good rating progression
        var rep = 0
        var interval = 1
        var ef = 2.5f

        // Rep 0 -> 1 day
        interval = when (rep) {
            0 -> 1
            1 -> 3
            else -> (interval * ef).toInt()
        }
        rep++
        assertEquals(1, interval)
        assertEquals(1, rep)

        // Rep 1 -> 3 days
        interval = when (rep) {
            0 -> 1
            1 -> 3
            else -> (interval * ef).toInt()
        }
        rep++
        assertEquals(3, interval)
        assertEquals(2, rep)

        // Rep 2 -> 3 * 2.5 = 7 days
        interval = when (rep) {
            0 -> 1
            1 -> 3
            else -> (interval * ef).toInt()
        }
        rep++
        assertEquals(7, interval)
        assertEquals(3, rep)

        // Again rating resets repetition and drops ease factor
        rep = 0
        interval = 1
        ef = max(1.3f, ef - 0.2f)
        assertEquals(0, rep)
        assertEquals(1, interval)
        assertEquals(2.3f, ef, 0.01f)
    }

    @Test
    fun testLevenshteinSimilarity() {
        fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
            var cost = IntArray(lhs.length + 1) { it }
            var newCost = IntArray(lhs.length + 1) { 0 }
            for (i in 1..rhs.length) {
                newCost[0] = i
                for (j in 1..lhs.length) {
                    val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                    val costReplace = cost[j - 1] + match
                    val costInsert = cost[j] + 1
                    val costDelete = newCost[j - 1] + 1
                    newCost[j] = minOf(costInsert, costDelete, costReplace)
                }
                val swap = cost
                cost = newCost
                newCost = swap
            }
            return cost[lhs.length]
        }

        fun calculateSimilarity(s1: String, s2: String): Int {
            if (s1 == s2) return 100
            val distance = levenshteinDistance(s1, s2)
            val maxLen = max(s1.length, s2.length)
            return ((1.0 - distance.toDouble() / maxLen.toDouble()) * 100).toInt()
        }

        assertEquals(100, calculateSimilarity("sheep", "sheep"))
        assertTrue(calculateSimilarity("ship", "sheep") in 60..85)
        assertEquals(0, calculateSimilarity("", "anything"))
    }

    @Test
    fun testCefrDiagnosticLevelScoring() {
        fun assignLevel(score: Int): String {
            return when {
                score <= 2 -> "A1"
                score <= 4 -> "A2"
                score <= 6 -> "B1"
                score <= 8 -> "B2"
                score == 9 -> "C1"
                else -> "C2"
            }
        }

        assertEquals("A1", assignLevel(1))
        assertEquals("A2", assignLevel(4))
        assertEquals("B1", assignLevel(5))
        assertEquals("B2", assignLevel(8))
        assertEquals("C1", assignLevel(9))
        assertEquals("C2", assignLevel(10))
    }
}
