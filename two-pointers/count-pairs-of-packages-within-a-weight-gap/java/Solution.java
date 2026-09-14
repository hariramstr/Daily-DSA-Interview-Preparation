import java.util.*;

/*
 * Title: Count Pairs of Packages Within a Weight Gap
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an integer array weights where weights[i] is the weight of the ith package,
 * and two integers lowGap and highGap. A pair of packages (i, j) is considered compatible
 * if i < j and the absolute difference between their weights is between lowGap and highGap,
 * inclusive. In other words, a pair is valid if:
 *
 *     lowGap <= |weights[i] - weights[j]| <= highGap
 *
 * Return the number of compatible pairs.
 *
 * A straightforward O(n^2) solution checks every pair, but that is too slow for large inputs.
 * Design an algorithm efficient enough for arrays with up to 10^5 elements.
 * The intended solution uses sorting together with a two-pointer counting strategy.
 *
 * Note that package weights are not initially sorted, and multiple packages may have the same
 * weight. Also note that when lowGap = 0, equal-weight pairs may be valid if their difference
 * is within the allowed range.
 *
 * Constraints:
 * - 1 <= weights.length <= 100000
 * - 0 <= weights[i] <= 1000000000
 * - 0 <= lowGap <= highGap <= 1000000000
 *
 * Example 1:
 * Input: weights = [4, 1, 7, 3], lowGap = 2, highGap = 4
 * Output: 4
 * Explanation:
 * Valid pairs by weight are:
 * - (4,1) diff 3
 * - (4,7) diff 3
 * - (1,3) diff 2
 * - (7,3) diff 4
 *
 * Example 2:
 * Input: weights = [5, 5, 8, 10], lowGap = 0, highGap = 3
 * Output: 5
 * Explanation:
 * Valid pairs are:
 * - the two 5s -> diff 0
 * - first 5 with 8 -> diff 3
 * - second 5 with 8 -> diff 3
 * - 8 with 10 -> diff 2
 * Total = 4
 *
 * Important note:
 * The example text says output 5, but if we enumerate all pairs carefully:
 * (5,5)=0 valid
 * (5,8)=3 valid
 * (5,10)=5 invalid
 * (5,8)=3 valid
 * (5,10)=5 invalid
 * (8,10)=2 valid
 * Therefore the correct total is 4, not 5.
 *
 * This implementation returns the mathematically correct answer.
 */

public class Solution {

    /**
     * Counts the number of compatible pairs whose absolute weight difference is within
     * the inclusive range [lowGap, highGap].
     *
     * The key observation is:
     * after sorting, for any pair i < j, we have weights[j] >= weights[i], so:
     *
     *     |weights[j] - weights[i]| = weights[j] - weights[i]
     *
     * That removes the absolute value and allows a two-pointer counting strategy.
     *
     * We compute:
     *     count(diff <= highGap) - count(diff < lowGap)
     *
     * Since:
     *     count(lowGap <= diff <= highGap)
     *   = count(diff <= highGap) - count(diff <= lowGap - 1)
     *   = count(diff < lowGap) subtracted from count(diff <= highGap)
     *
     * @param weights the array of package weights
     * @param lowGap the minimum allowed difference, inclusive
     * @param highGap the maximum allowed difference, inclusive
     * @return the number of compatible pairs
     * Time complexity: O(n log n) because of sorting; the two-pointer scans are O(n)
     * Space complexity: O(n) due to cloning the array for sorting
     */
    public long countCompatiblePairs(int[] weights, int lowGap, int highGap) {
        int[] sorted = weights.clone();
        Arrays.sort(sorted);

        // Count all pairs with difference <= highGap.
        long atMostHigh = countPairsWithDifferenceAtMost(sorted, highGap);

        // Count all pairs with difference < lowGap.
        // This is equivalent to pairs with difference <= lowGap - 1,
        // but using a strict comparison helper avoids edge-case issues when lowGap = 0.
        long lessThanLow = countPairsWithDifferenceLessThan(sorted, lowGap);

        return atMostHigh - lessThanLow;
    }

    /**
     * Counts how many pairs (i, j), with i < j, satisfy:
     *
     *     sorted[j] - sorted[i] <= limit
     *
     * This method assumes the input array is already sorted in non-decreasing order.
     *
     * Two-pointer idea:
     * - Maintain a left pointer for the smallest valid starting index.
     * - Expand the right pointer from left to right.
     * - While the difference becomes too large, move left forward.
     * - Once the window is valid, every index from left to right - 1 forms a valid pair with right.
     *
     * Example:
     * sorted = [1, 3, 4, 7], limit = 4
     *
     * right = 0: no earlier elements, add 0
     * right = 1: [1,3], diff = 2 <= 4, add 1 pair
     * right = 2: [1,3,4], diffs with 4 are 3 and 1, add 2 pairs
     * right = 3: [1,3,4,7], diff 7-1 = 6 too large, move left to 1
     *            now 7-3 = 4 valid, add right-left = 3-1 = 2 pairs
     *
     * Total = 0 + 1 + 2 + 2 = 5
     *
     * @param sorted a sorted array of package weights
     * @param limit the maximum allowed difference, inclusive
     * @return the number of pairs with difference at most limit
     * Time complexity: O(n)
     * Space complexity: O(1) extra space beyond the input array
     */
    public long countPairsWithDifferenceAtMost(int[] sorted, int limit) {
        long count = 0L;
        int left = 0;

        // We move "right" one step at a time.
        // For each right, we adjust left until the window [left, right]
        // satisfies sorted[right] - sorted[left] <= limit.
        for (int right = 0; right < sorted.length; right++) {

            // If the current difference is too large, shrink the window from the left.
            // Because the array is sorted, increasing left can only decrease the difference.
            while (left < right && (long) sorted[right] - sorted[left] > limit) {
                left++;
            }

            // Now every index i in [left, right-1] forms a valid pair (i, right).
            // Number of such indices = right - left.
            count += right - left;
        }

        return count;
    }

    /**
     * Counts how many pairs (i, j), with i < j, satisfy:
     *
     *     sorted[j] - sorted[i] < limit
     *
     * This method assumes the input array is already sorted in non-decreasing order.
     *
     * This is especially useful for computing:
     *     count(diff in [lowGap, highGap])
     * by subtracting:
     *     count(diff <= highGap) - count(diff < lowGap)
     *
     * Two-pointer idea:
     * - Maintain a left pointer such that the current window satisfies the strict inequality.
     * - For each right, move left forward while the difference is >= limit.
     * - Then all indices from left to right - 1 are valid partners for right.
     *
     * Important edge case:
     * If limit = 0, then no non-negative difference can be < 0.
     * Since sorted[j] - sorted[i] is always >= 0 for j > i, the answer is 0.
     *
     * @param sorted a sorted array of package weights
     * @param limit the strict upper bound on the difference
     * @return the number of pairs with difference strictly less than limit
     * Time complexity: O(n)
     * Space complexity: O(1) extra space beyond the input array
     */
    public long countPairsWithDifferenceLessThan(int[] sorted, int limit) {
        if (limit <= 0) {
            return 0L;
        }

        long count = 0L;
        int left = 0;

        for (int right = 0; right < sorted.length; right++) {

            // We need sorted[right] - sorted[left] < limit.
            // If it is >= limit, move left forward until the condition becomes true.
            while (left < right && (long) sorted[right] - sorted[left] >= limit) {
                left++;
            }

            // After adjustment, all indices from left to right - 1
            // produce a difference strictly less than limit.
            count += right - left;
        }

        return count;
    }

    /**
     * A simple brute-force checker for small inputs.
     * This is not efficient for large arrays, but it is useful for verifying correctness
     * during demonstration and testing.
     *
     * @param weights the array of package weights
     * @param lowGap the minimum allowed difference, inclusive
     * @param highGap the maximum allowed difference, inclusive
     * @return the number of compatible pairs computed by checking every pair
     * Time complexity: O(n^2)
     * Space complexity: O(1)
     */
    public long countCompatiblePairsBruteForce(int[] weights, int lowGap, int highGap) {
        long count = 0L;

        for (int i = 0; i < weights.length; i++) {
            for (int j = i + 1; j < weights.length; j++) {
                long diff = Math.abs((long) weights[i] - weights[j]);
                if (diff >= lowGap && diff <= highGap) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Demonstrates the solution on sample inputs and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstrated test case
     * Space complexity: O(n) per demonstrated test case due to sorting copy
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] weights1 = {4, 1, 7, 3};
        int lowGap1 = 2;
        int highGap1 = 4;
        long result1 = solution.countCompatiblePairs(weights1, lowGap1, highGap1);
        long brute1 = solution.countCompatiblePairsBruteForce(weights1, lowGap1, highGap1);

        System.out.println("Example 1:");
        System.out.println("weights = " + Arrays.toString(weights1));
        System.out.println("lowGap = " + lowGap1 + ", highGap = " + highGap1);
        System.out.println("Optimized result = " + result1);
        System.out.println("Brute-force result = " + brute1);
        System.out.println("Expected = 4");
        System.out.println();

        int[] weights2 = {5, 5, 8, 10};
        int lowGap2 = 0;
        int highGap2 = 3;
        long result2 = solution.countCompatiblePairs(weights2, lowGap2, highGap2);
        long brute2 = solution.countCompatiblePairsBruteForce(weights2, lowGap2, highGap2);

        System.out.println("Example 2:");
        System.out.println("weights = " + Arrays.toString(weights2));
        System.out.println("lowGap = " + lowGap2 + ", highGap = " + highGap2);
        System.out.println("Optimized result = " + result2);
        System.out.println("Brute-force result = " + brute2);
        System.out.println("Correct mathematical answer = 4");
        System.out.println("Note: The prompt's stated output 5 is inconsistent with its own pair listing.");
        System.out.println();

        int[] weights3 = {1, 1, 1};
        int lowGap3 = 0;
        int highGap3 = 0;
        long result3 = solution.countCompatiblePairs(weights3, lowGap3, highGap3);

        System.out.println("Additional test:");
        System.out.println("weights = " + Arrays.toString(weights3));
        System.out.println("lowGap = " + lowGap3 + ", highGap = " + highGap3);
        System.out.println("Optimized result = " + result3);
        System.out.println("Expected = 3");
    }
}