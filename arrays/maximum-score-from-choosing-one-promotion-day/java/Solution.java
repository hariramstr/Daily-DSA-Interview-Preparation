import java.util.*;

/*
 * Title: Maximum Score from Choosing One Promotion Day
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array sales where sales[i] represents the net revenue earned on day i.
 * A company wants to run exactly one special promotion on a single day p. After choosing p,
 * the total promotion score is defined as the sum of two parts:
 *   1) the best non-empty contiguous revenue streak ending at day p
 *   2) the best non-empty contiguous revenue streak starting at day p
 *
 * Since day p belongs to both streaks, its value is counted only once in the final score.
 *
 * Formally, choose an index p and compute:
 *   - leftScore = maximum sum of a non-empty subarray that ends at p
 *   - rightScore = maximum sum of a non-empty subarray that starts at p
 *   - promotionScore = leftScore + rightScore - sales[p]
 *
 * Return the maximum possible promotionScore over all valid choices of p.
 *
 * This models selecting a single promotion day that can benefit from momentum built before it
 * and customer interest continuing after it. The chosen day must be included in both segments,
 * but the segments may each have length 1.
 *
 * Constraints:
 *   - 1 <= sales.length <= 200000
 *   - -1000000000 <= sales[i] <= 1000000000
 *   - The answer fits in a signed 64-bit integer.
 *
 * Example 1:
 * Input: sales = [4, -2, 3, -1, 5]
 * Output: 9
 * Explanation:
 * Choose p = 2 (value 3).
 * Best subarray ending at index 2 is [4, -2, 3] with sum 5.
 * Best subarray starting at index 2 is [3, -1, 5] with sum 7.
 * Promotion score = 5 + 7 - 3 = 9.
 * No other promotion day gives a higher score.
 *
 * Example 2:
 * Input: sales = [-5, -2, -7]
 * Output: -2
 * Explanation:
 * When all values are negative, the best choice is to select the least negative day alone.
 * Choose p = 1.
 * leftScore = -2, rightScore = -2, so promotionScore = -2.
 */

public class Solution {

    /**
     * Computes the maximum possible promotion score.
     *
     * The key idea is to precompute:
     * 1) leftBest[i]  = maximum sum of a non-empty subarray that ends exactly at index i
     * 2) rightBest[i] = maximum sum of a non-empty subarray that starts exactly at index i
     *
     * Then for every possible promotion day i:
     * promotionScore(i) = leftBest[i] + rightBest[i] - sales[i]
     *
     * We subtract sales[i] once because the chosen day belongs to both streaks and would otherwise
     * be counted twice.
     *
     * This is a direct application of Kadane-style dynamic programming from both directions.
     *
     * @param sales the array where sales[i] is the net revenue on day i
     * @return the maximum promotion score over all valid promotion days
     * Time complexity: O(n), where n is sales.length
     * Space complexity: O(n), for the two helper arrays
     */
    public long maximumPromotionScore(int[] sales) {
        int n = sales.length;

        // leftBest[i] will store:
        // "What is the maximum sum of a non-empty contiguous subarray that MUST end at i?"
        long[] leftBest = computeBestEndingAtEachIndex(sales);

        // rightBest[i] will store:
        // "What is the maximum sum of a non-empty contiguous subarray that MUST start at i?"
        long[] rightBest = computeBestStartingAtEachIndex(sales);

        // We now test every index as the promotion day.
        long answer = Long.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            // leftBest[i] includes sales[i]
            // rightBest[i] includes sales[i]
            // so sales[i] is counted twice; subtract it once.
            long promotionScore = leftBest[i] + rightBest[i] - sales[i];

            if (promotionScore > answer) {
                answer = promotionScore;
            }
        }

        return answer;
    }

    /**
     * Computes, for every index i, the maximum sum of a non-empty contiguous subarray
     * that ends exactly at i.
     *
     * Recurrence:
     * leftBest[0] = sales[0]
     * leftBest[i] = max(
     *     sales[i],              // start a new subarray at i
     *     leftBest[i - 1] + sales[i] // extend the best subarray ending at i - 1
     * )
     *
     * Why this works:
     * If a best subarray must end at i, then it has only two possibilities:
     * - it consists of just sales[i]
     * - or it extends some best subarray ending at i - 1
     *
     * @param sales the input revenue array
     * @return an array leftBest where leftBest[i] is the best non-empty subarray sum ending at i
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] computeBestEndingAtEachIndex(int[] sales) {
        int n = sales.length;
        long[] leftBest = new long[n];

        // Base case:
        // The only non-empty subarray ending at index 0 is [sales[0]] itself.
        leftBest[0] = sales[0];

        // Build the DP array from left to right.
        for (int i = 1; i < n; i++) {
            long startNew = sales[i];
            long extendPrevious = leftBest[i - 1] + sales[i];

            // Choose the better of:
            // 1) starting fresh at i
            // 2) extending the previous best ending at i - 1
            leftBest[i] = Math.max(startNew, extendPrevious);
        }

        return leftBest;
    }

    /**
     * Computes, for every index i, the maximum sum of a non-empty contiguous subarray
     * that starts exactly at i.
     *
     * Recurrence:
     * rightBest[n - 1] = sales[n - 1]
     * rightBest[i] = max(
     *     sales[i],               // start and end at i
     *     sales[i] + rightBest[i + 1] // extend to the right
     * )
     *
     * Why this works:
     * If a best subarray must start at i, then it has only two possibilities:
     * - it consists of just sales[i]
     * - or it includes sales[i] and continues with the best subarray starting at i + 1
     *
     * @param sales the input revenue array
     * @return an array rightBest where rightBest[i] is the best non-empty subarray sum starting at i
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] computeBestStartingAtEachIndex(int[] sales) {
        int n = sales.length;
        long[] rightBest = new long[n];

        // Base case:
        // The only non-empty subarray starting at the last index is [sales[n - 1]] itself.
        rightBest[n - 1] = sales[n - 1];

        // Build the DP array from right to left.
        for (int i = n - 2; i >= 0; i--) {
            long startOnlyHere = sales[i];
            long extendRight = sales[i] + rightBest[i + 1];

            // Choose the better of:
            // 1) taking only sales[i]
            // 2) extending into the best subarray starting at i + 1
            rightBest[i] = Math.max(startOnlyHere, extendRight);
        }

        return rightBest;
    }

    /**
     * Utility method to print an int array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n), due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * It also implicitly verifies correctness for the provided examples:
     * Example 1:
     *   sales = [4, -2, 3, -1, 5]
     *   expected = 9
     *
     * Example 2:
     *   sales = [-5, -2, -7]
     *   expected = -2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(n) per demonstration case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] sales1 = {4, -2, 3, -1, 5};
        long result1 = solution.maximumPromotionScore(sales1);
        System.out.println("Input: sales = " + solution.arrayToString(sales1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 9");
        System.out.println();

        int[] sales2 = {-5, -2, -7};
        long result2 = solution.maximumPromotionScore(sales2);
        System.out.println("Input: sales = " + solution.arrayToString(sales2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: -2");
        System.out.println();

        // Additional quick sanity checks for beginners:
        int[] sales3 = {7};
        long result3 = solution.maximumPromotionScore(sales3);
        System.out.println("Input: sales = " + solution.arrayToString(sales3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 7");
        System.out.println();

        int[] sales4 = {1, 2, 3};
        long result4 = solution.maximumPromotionScore(sales4);
        System.out.println("Input: sales = " + solution.arrayToString(sales4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 6");
    }
}