import java.util.*;

/*
 * Title: Locate Customer Tier by Spend Threshold
 * Difficulty: Easy
 * Topic: Binary Search
 *
 * Problem Description:
 * A company stores its loyalty program tiers as a sorted array of minimum spend thresholds.
 * Each value represents the smallest total yearly spend needed to qualify for that tier.
 * For example, if the thresholds are [0, 100, 300, 700], then a customer who spent 450
 * qualifies for the tier starting at 300, while a customer who spent 90 qualifies for the
 * tier starting at 0.
 *
 * Given a sorted integer array thresholds in strictly increasing order and an integer spend,
 * return the index of the highest threshold that is less than or equal to spend. If spend is
 * smaller than the first threshold, return -1.
 *
 * Your solution should run in O(log n) time, so a binary search approach is expected.
 *
 * Constraints:
 * - 1 <= thresholds.length <= 100000
 * - 0 <= thresholds[i] <= 1000000000
 * - thresholds is sorted in strictly increasing order
 * - 0 <= spend <= 1000000000
 *
 * Example 1:
 * Input: thresholds = [0, 100, 300, 700], spend = 450
 * Output: 2
 * Explanation: thresholds[2] = 300 is the largest threshold not exceeding 450.
 * The next threshold, 700, is too high.
 *
 * Example 2:
 * Input: thresholds = [50, 120, 200], spend = 20
 * Output: -1
 * Explanation: The customer has not reached the minimum spend for the first tier,
 * so no valid threshold index exists.
 *
 * This problem tests whether you can adapt binary search to find the rightmost valid
 * position instead of checking for an exact match. Be careful with boundary cases such
 * as a spend value below all thresholds, exactly equal to a threshold, or larger than
 * every threshold in the array.
 */

public class Solution {

    /**
     * Finds the index of the highest threshold that is less than or equal to the given spend.
     *
     * This method uses binary search, but instead of looking for an exact match only,
     * it keeps track of the best valid index seen so far. Whenever thresholds[mid] is
     * less than or equal to spend, that index is a candidate answer, and we continue
     * searching to the right to see if there is an even larger valid threshold.
     *
     * @param thresholds a sorted array of strictly increasing minimum spend thresholds
     * @param spend the customer's total yearly spend
     * @return the index of the rightmost threshold value that is less than or equal to spend;
     *         returns -1 if spend is smaller than the first threshold
     *
     * Time complexity: O(log n), where n is thresholds.length
     * Space complexity: O(1)
     */
    public int locateCustomerTier(int[] thresholds, int spend) {
        // "left" marks the beginning of the current search range.
        int left = 0;

        // "right" marks the end of the current search range.
        int right = thresholds.length - 1;

        // "answer" stores the best valid index found so far.
        // We start with -1 because if no threshold is <= spend,
        // then the correct result must be -1.
        int answer = -1;

        // Continue searching while the search range is valid.
        // As long as left <= right, there is still at least one element to inspect.
        while (left <= right) {
            // Compute the middle index safely.
            // This avoids potential integer overflow that could happen with (left + right) / 2.
            int mid = left + (right - left) / 2;

            // Case 1:
            // If thresholds[mid] is <= spend, then mid is a valid candidate.
            // But it may not be the RIGHTMOST valid one.
            // So:
            //   1. Save mid as the current best answer.
            //   2. Move left to mid + 1 to search the right half for a larger valid index.
            if (thresholds[mid] <= spend) {
                answer = mid;
                left = mid + 1;
            } else {
                // Case 2:
                // thresholds[mid] > spend, so this threshold is too large.
                // Since the array is sorted in increasing order, everything to the right
                // of mid is also too large.
                // Therefore, we must search only the left half.
                right = mid - 1;
            }
        }

        // After the loop ends, "answer" holds the index of the largest threshold
        // that does not exceed spend, or -1 if no such threshold exists.
        return answer;
    }

    /**
     * Helper method to print a test case and its result in a beginner-friendly format.
     *
     * @param thresholds a sorted array of strictly increasing minimum spend thresholds
     * @param spend the customer's total yearly spend
     * @return the computed tier index for the provided input
     *
     * Time complexity: O(log n) for the search itself, plus O(n) for array-to-string printing
     * Space complexity: O(1) auxiliary space, ignoring output formatting internals
     */
    public int demonstrateCase(int[] thresholds, int spend) {
        int result = locateCustomerTier(thresholds, spend);
        System.out.println("thresholds = " + Arrays.toString(thresholds) + ", spend = " + spend);
        System.out.println("Output: " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations of the binary search solution.
     *
     * This main method verifies the examples from the problem statement and also
     * shows a few additional boundary cases:
     * - spend below all thresholds
     * - spend exactly equal to a threshold
     * - spend larger than every threshold
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(log n) per demonstrated test case, excluding printing cost
     * Space complexity: O(1) auxiliary space
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // thresholds = [0, 100, 300, 700], spend = 450
        // The largest threshold <= 450 is 300, which is at index 2.
        solution.demonstrateCase(new int[]{0, 100, 300, 700}, 450);

        // Example 2 from the problem statement:
        // thresholds = [50, 120, 200], spend = 20
        // 20 is smaller than the first threshold 50, so the answer is -1.
        solution.demonstrateCase(new int[]{50, 120, 200}, 20);

        // Additional boundary case:
        // spend exactly equals a threshold.
        // thresholds[2] = 300, so the answer should be 2.
        solution.demonstrateCase(new int[]{0, 100, 300, 700}, 300);

        // Additional boundary case:
        // spend is larger than every threshold.
        // The rightmost valid threshold is 700 at index 3.
        solution.demonstrateCase(new int[]{0, 100, 300, 700}, 1000);

        // Additional boundary case:
        // single-element array where spend qualifies.
        solution.demonstrateCase(new int[]{50}, 50);

        // Additional boundary case:
        // single-element array where spend does not qualify.
        solution.demonstrateCase(new int[]{50}, 10);
    }
}