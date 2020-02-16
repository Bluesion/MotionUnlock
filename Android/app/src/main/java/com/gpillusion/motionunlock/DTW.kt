package com.gpillusion.motionunlock

import kotlin.math.min

/**
 * A class that implements the awesome Dynamic Time Warping algorithm.
 * Absolutely all credit for this implementation goes to the developers of the Gesture and Activity Recognition Toolkit, (GART).
 * @http://trac.research.cc.gatech.edu/GART/browser/GART/weka/edu/gatech/gart/ml/weka/DTW.java?rev=9
 */
open class DTW
/** Default constructor for a class which implements dynamic time warping.  */
{
    /** Defines the result for a Dynamic Time Warping operation.  */
    class Result// Initialize Member Variables.
    /** Constructor.  */(
        /* Getters. */
        /* Member Variables. */
        val warpingPath: Array<IntArray>,
        val distance: Double
    )

    fun compute(
        pSample: FloatArray,
        pTemplate: FloatArray
    ): Result { // Declare Iteration Constants.
        val lN = pSample.size
        val lM = pTemplate.size
        // Ensure the samples are valid.
        if (lN == 0 || lM == 0) { // Assert a bad result.
            return Result(
                arrayOf(),
                Double.NaN
            )
        }
        // Define the Scalar Qualifier.
        var lK = 1
        // Allocate the Warping Path. (Math.max(N, M) <= K < (N + M).
        val lWarpingPath =
            Array(lN + lM) { IntArray(2) }
        // Declare the Local Distances.
        val lL =
            Array(lN) { DoubleArray(lM) }
        // Declare the Global Distances.
        val lG =
            Array(lN) { DoubleArray(lM) }
        // Declare the MinimalBuffer.
        val lMinimalBuffer = DoubleArray(3)
        // Declare iteration variables.
        var j: Int
        // Iterate the Sample.
        var i = 0
        while (i < lN) {
            // Fetch the Sample.
            val lSample = pSample[i]
            // Iterate the Template.
            j = 0
            while (j < lM) {
                // Calculate the Distance between the Sample and the Template for this Index.
                lL[i][j] = getDistanceBetween(
                    lSample.toDouble(),
                    pTemplate[j].toDouble()
                )
                j++
            }
            i++
        }
        // Initialize the Global.
        lG[0][0] = lL[0][0]
        i = 1
        while (i < lN) {
            lG[i][0] = lL[i][0] + lG[i - 1][0]
            i++
        }
        j = 1
        while (j < lM) {
            lG[0][j] = lL[0][j] + lG[0][j - 1]
            j++
        }
        i = 1
        while (i < lN) {
            j = 1
            while (j < lM) {
                // Accumulate the path.
                lG[i][j] = min(
                    min(
                        lG[i - 1][j],
                        lG[i - 1][j - 1]
                    ), lG[i][j - 1]
                ) + lL[i][j]
                j++
            }
            i++
        }
        // Update iteration varaibles.
        lWarpingPath[lK - 1][0] = lN - 1
        i = lWarpingPath[lK - 1][0]
        lWarpingPath[lK - 1][1] = lM - 1
        j = lWarpingPath[lK - 1][1]
        // Whilst there are samples to process...
        while (i + j != 0) { // Handle the offset.
            when {
                i == 0 -> { // Decrement the iteration variable.
                    j -= 1
                }
                j == 0 -> { // Decrement the iteration variable.
                    i -= 1
                }
                else -> { // Update the contents of the MinimaBuffer.
                    lMinimalBuffer[0] = lG[i - 1][j]
                    lMinimalBuffer[1] = lG[i][j - 1]
                    lMinimalBuffer[2] = lG[i - 1][j - 1]
                    // Calculate the Index of the Minimum.
                    val lMinimumIndex = getMinimumIndex(lMinimalBuffer)
                    // Declare booleans.
                    val lMinIs0 = lMinimumIndex == 0
                    val lMinIs1 = lMinimumIndex == 1
                    val lMinIs2 = lMinimumIndex == 2
                    // Update the iteration components.
                    i -= if (lMinIs0 || lMinIs2) 1 else 0
                    j -= if (lMinIs1 || lMinIs2) 1 else 0
                }
            }
            // Increment the qualifier.
            lK++
            // Update the Warping Path.
            lWarpingPath[lK - 1][0] = i
            lWarpingPath[lK - 1][1] = j
        }
        // Return the Result. (Calculate the Warping Path and the Distance.)
        return Result(
            this.reverse(lWarpingPath, lK),
            lG[lN - 1][lM - 1] / lK
        )
    }

    /** Changes the order of the warping path, in increasing order.  */
    private fun reverse(
        pPath: Array<IntArray>,
        pK: Int
    ): Array<IntArray> { // Allocate the Path.
        val lPath =
            Array(pK) { IntArray(2) }
        // Iterate.
        for (i in 0 until pK) { // Iterate.
            for (j in 0..1) { // Update the Path.
                lPath[i][j] = pPath[pK - i - 1][j]
            }
        }
        // Return the Allocated Path.
        return lPath
    }

    /** Computes a distance between two points.  */
    private fun getDistanceBetween(
        p1: Double,
        p2: Double
    ): Double { // Calculate the square error.
        return (p1 - p2) * (p1 - p2)
    }

    /** Finds the index of the minimum element from the given array.  */
    private fun getMinimumIndex(pArray: DoubleArray): Int { // Declare iteration variables.
        var lIndex = 0
        var lValue = pArray[0]
        // Iterate the Array.
        for (i in 1 until pArray.size) { // .Is the current value smaller?
            val lIsSmaller = pArray[i] < lValue
            // Update the search metrics.
            lValue = if (lIsSmaller) pArray[i] else lValue
            lIndex = if (lIsSmaller) i else lIndex
        }
        // Return the Index.
        return lIndex
    }
}