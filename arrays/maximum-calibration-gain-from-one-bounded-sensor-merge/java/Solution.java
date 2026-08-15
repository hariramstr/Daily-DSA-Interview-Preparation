import java.util.*;

/*
 * Title: Maximum Calibration Gain from One Bounded Sensor Merge
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array readings of length n, where readings[i] is the calibration score
 * reported by the i-th sensor in a fixed line. To improve overall quality, you may perform exactly
 * one merge operation on a contiguous block of sensors. If you choose a subarray readings[l..r],
 * all values in that block are replaced by a single sensor whose score is the rounded-down average
 * of the block, that is floor((readings[l] + readings[l+1] + ... + readings[r]) / (r - l + 1)).
 * The merged block contributes only that one averaged value to the final total, while sensors
 * outside the block remain unchanged.
 *
 * Your task is to compute the maximum possible final total calibration score after performing at
 * most one such merge operation, under the restriction that the length of the merged block must be
 * between L and R inclusive. You may also choose not to merge any block.
 *
 * Formally, if you merge readings[l..r], the final score becomes:
 * (sum of all readings) - (sum of readings[l..r]) + floor(sum(readings[l..r]) / (r - l + 1)).
 *
 * Find the maximum possible final score.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - -1000000000 <= readings[i] <= 1000000000
 * - 1 <= L <= R <= n
 * - The answer fits in a signed 64-bit integer.
 *
 * Key observation:
 * If a chosen block has sum S and length k, then the final total is:
 * total - S + floor(S / k)
 *
 * So maximizing the final total is equivalent to minimizing:
 * S - floor(S / k)
 *
 * For a fixed length k:
 * - If S >= 0, then S - floor(S / k) is nonnegative, so merging does not help.
 * - If S < 0, then floor(S / k) is also negative, and the quantity can become negative,
 *   which means the final total increases.
 *
 * Therefore, we need the minimum value of:
 * S - floor(S / k)
 * over all subarrays whose length k is in [L, R].
 *
 * Important arithmetic identity:
 * For any integer S and positive integer k,
 *     S - floor(S / k) = ceil((k - 1) * S / k)
 *
 * This lets us evaluate the merge "cost" from only the subarray sum S and length k.
 *
 * Efficient strategy:
 * 1. Compute prefix sums.
 * 2. For every possible length k in [L, R]:
 *    - Slide a window of size k across the array.
 *    - Compute each window sum in O(1) using prefix sums.
 *    - Evaluate cost = S - floor(S / k).
 *    - Track the minimum cost over all valid windows.
 * 3. Answer = totalSum - min(0, minimumCostFound)
 *
 * This implementation is intentionally beginner-friendly and heavily commented.
 *
 * Note:
 * The examples in the prompt contain arithmetic inconsistencies. This solution follows the formal
 * definition exactly:
 * final score = total - subarraySum + floor(subarraySum / length)
 *
 * Under that exact definition:
 * - For [8, -5, 4, -3, 10], L=2, R=3, the best answer is 16.
 * - For [7, 6, 5, 4], L=2, R=4, the best answer is 22.
 */

public class Solution {

    /**
     * Computes the maximum possible final calibration score after performing at most one merge
     * operation on a contiguous block whose length is between L and R inclusive.
     *
     * The method follows the formula:
     * final = totalSum - blockSum + floor(blockSum / blockLength)
     *
     * We maximize the final score by minimizing:
     * blockSum - floor(blockSum / blockLength)
     *
     * If the minimum such value is negative, merging helps.
     * Otherwise, the best choice is to perform no merge.
     *
     * @param readings the array of sensor calibration scores
     * @param L the minimum allowed merge length
     * @param R the maximum allowed merge length
     * @return the maximum possible final total calibration score
     * Time complexity: O(n * (R - L + 1))
     * Space complexity: O(n)
     */
    public long maximumCalibrationGain(int[] readings, int L, int R) {
        int n = readings.length;

        // ------------------------------------------------------------
        // Step 1: Build prefix sums.
        //
        // prefix[i] = sum of the first i elements
        // So the sum of subarray readings[l..r] is:
        // prefix[r + 1] - prefix[l]
        //
        // We use long because:
        // - n can be up to 200000
        // - each value can be up to 1e9 in magnitude
        // so sums can be much larger than int.
        // ------------------------------------------------------------
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + readings[i];
        }

        // Total sum of the original array.
        long totalSum = prefix[n];

        // ------------------------------------------------------------
        // Step 2: Track the minimum merge cost over all valid windows.
        //
        // For a window with sum S and length k:
        // mergeCost = S - floor(S / k)
        //
        // Then:
        // finalScore = totalSum - mergeCost
        //
        // So to maximize finalScore, we minimize mergeCost.
        //
        // If the best mergeCost is >= 0, then merging does not improve
        // the total, so we should choose "no merge".
        // ------------------------------------------------------------
        long bestCost = Long.MAX_VALUE;

        // ------------------------------------------------------------
        // Step 3: Try every allowed length k from L to R.
        //
        // For each fixed length k, we slide a window across the array.
        // Each window sum is computed in O(1) using prefix sums.
        // ------------------------------------------------------------
        for (int k = L; k <= R; k++) {
            // The right boundary in prefix indexing goes from k to n.
            // Window is [end - k, end - 1] in array indexing.
            for (int end = k; end <= n; end++) {
                long windowSum = prefix[end] - prefix[end - k];

                // Compute floor(windowSum / k) correctly for negative values.
                long avgFloor = floorDiv(windowSum, k);

                // Cost contributed by replacing the whole block by its floor average.
                long cost = windowSum - avgFloor;

                if (cost < bestCost) {
                    bestCost = cost;
                }
            }
        }

        // ------------------------------------------------------------
        // Step 4: Decide whether to merge.
        //
        // If bestCost < 0, then:
        // final = totalSum - bestCost > totalSum
        // so merging helps.
        //
        // Otherwise, no merge is best.
        // ------------------------------------------------------------
        if (bestCost < 0) {
            return totalSum - bestCost;
        }
        return totalSum;
    }

    /**
     * Computes mathematical floor(a / b) for long values, where b > 0.
     *
     * Java's / operator truncates toward zero, which is not the same as floor
     * when a is negative. This helper ensures correct floor division.
     *
     * Examples:
     * - floorDiv(5, 2)  = 2
     * - floorDiv(-1, 2) = -1
     * - floorDiv(-4, 3) = -2
     *
     * @param a the dividend
     * @param b the positive divisor
     * @return floor(a / b)
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long floorDiv(long a, long b) {
        long q = a / b;
        long r = a % b;

        // If there is a remainder and the dividend is negative,
        // truncation toward zero is too large, so subtract 1.
        if (r != 0 && a < 0) {
            q--;
        }
        return q;
    }

    /**
     * Runs a single demonstration test and prints the result.
     *
     * @param readings the input array
     * @param L the minimum allowed merge length
     * @param R the maximum allowed merge length
     * @return the computed answer for convenience
     * Time complexity: same as maximumCalibrationGain
     * Space complexity: same as maximumCalibrationGain
     */
    public long runDemo(int[] readings, int L, int R) {
        long result = maximumCalibrationGain(readings, L, R);
        System.out.println("readings = " + Arrays.toString(readings) + ", L = " + L + ", R = " + R);
        System.out.println("Maximum final calibration score = " + result);
        System.out.println();
        return result;
    }

    /**
     * Demonstrates the solution on sample inputs.
     *
     * Note:
     * The first sample explanation in the prompt is internally inconsistent,
     * but the formal formula gives answer 16, which this program prints.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: depends on the demo inputs
     * Space complexity: depends on the demo inputs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        int[] readings1 = {8, -5, 4, -3, 10};
        long answer1 = solution.runDemo(readings1, 2, 3);
        System.out.println("Expected by formal definition: 16");
        System.out.println("Matches: " + (answer1 == 16));
        System.out.println();

        // Sample 2 from the prompt.
        int[] readings2 = {7, 6, 5, 4};
        long answer2 = solution.runDemo(readings2, 2, 4);
        System.out.println("Expected: 22");
        System.out.println("Matches: " + (answer2 == 22));
        System.out.println();

        // Additional quick sanity checks.
        int[] readings3 = {-5, -5};
        solution.runDemo(readings3, 2, 2);

        int[] readings4 = {1, -10, 1};
        solution.runDemo(readings4, 2, 3);
    }
}