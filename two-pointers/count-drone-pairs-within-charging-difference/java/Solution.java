import java.util.*;

/*
 * Title: Count Drone Pairs Within Charging Difference
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A robotics team maintains a sorted list of battery charge levels for idle delivery drones.
 * To form a temporary relay pair, two drones can be matched only if the absolute difference
 * between their charge levels is at most maxDiff.
 *
 * Given an integer array charge sorted in non-decreasing order, return the number of index
 * pairs (i, j) such that 0 <= i < j < charge.length and charge[j] - charge[i] <= maxDiff.
 *
 * Because the number of valid pairs can be large, the algorithm should be efficient enough
 * for large inputs. A brute-force O(n^2) solution will be too slow under the full constraints.
 *
 * The task is to count all valid pairs, not to list them.
 *
 * Constraints:
 * - 1 <= charge.length <= 2 * 10^5
 * - 0 <= charge[i] <= 10^9
 * - charge is sorted in non-decreasing order
 * - 0 <= maxDiff <= 10^9
 * - The answer may exceed 32-bit integer range, so use a 64-bit integer type if needed
 *
 * Example 1:
 * Input: charge = [10, 12, 13, 17], maxDiff = 3
 * Output: 3
 * Explanation: Valid pairs are (10,12), (10,13), and (12,13). Pairs involving 17 differ by more than 3.
 *
 * Example 2:
 * Input: charge = [4, 4, 5, 8, 9], maxDiff = 1
 * Output: 4
 * Explanation: Valid pairs are the two drones with charge 4, both pairs (4,5), and (8,9).
 */

public class Solution {

    /**
     * Counts how many index pairs (i, j) satisfy:
     * - 0 <= i < j < charge.length
     * - charge[j] - charge[i] <= maxDiff
     *
     * This method uses the classic two-pointers / sliding window technique.
     * Since the array is already sorted in non-decreasing order, for each left index,
     * we can move the right pointer only forward and count how many valid partners exist.
     *
     * @param charge the sorted array of drone charge levels
     * @param maxDiff the maximum allowed difference between two paired charge levels
     * @return the total number of valid pairs as a long
     * Time complexity: O(n), because each pointer moves at most n times
     * Space complexity: O(1), ignoring the input array
     */
    public long countPairs(int[] charge, int maxDiff) {
        int n = charge.length;

        // We store the answer in a long because the number of pairs can be very large.
        // For example, if all values are valid with each other, the number of pairs is:
        // n * (n - 1) / 2
        // With n = 200,000, this is far larger than what an int can hold.
        long totalPairs = 0L;

        // "right" will represent the first index that is NOT valid for the current "left".
        // In other words, valid partner indices for "left" will be:
        // left + 1, left + 2, ..., right - 1
        //
        // Important optimization:
        // We never move "right" backward. Because the array is sorted, once a value becomes
        // too large for a certain left index, future left indices will only allow right to
        // stay where it is or move further forward.
        int right = 0;

        // We try every index as the left endpoint of a pair.
        for (int left = 0; left < n; left++) {

            // Ensure right is always at least left + 1.
            // This matters especially at the beginning and also keeps the window valid.
            if (right < left + 1) {
                right = left + 1;
            }

            // Expand the right pointer as far as possible while the pair remains valid.
            //
            // Since the array is sorted:
            // - charge[right] >= charge[left]
            // - so absolute difference simplifies to charge[right] - charge[left]
            //
            // We keep moving right while:
            // charge[right] - charge[left] <= maxDiff
            //
            // After this loop ends:
            // - right is either n
            // - or the first index where the difference is too large
            while (right < n && (long) charge[right] - charge[left] <= maxDiff) {
                right++;
            }

            // Now all indices from left + 1 up to right - 1 are valid partners with "left".
            //
            // Count of such indices:
            // (right - 1) - (left + 1) + 1
            // = right - left - 1
            //
            // Example:
            // if left = 0 and right = 3, then valid partners are 1 and 2
            // count = 3 - 0 - 1 = 2
            totalPairs += (long) (right - left - 1);
        }

        return totalPairs;
    }

    /**
     * A helper method that prints the input and result in a beginner-friendly format.
     *
     * @param charge the sorted array of drone charge levels
     * @param maxDiff the maximum allowed difference
     * @return the computed number of valid pairs
     * Time complexity: O(n), because it calls the linear-time counting method
     * Space complexity: O(1), ignoring output formatting and the input array
     */
    public long demonstrate(int[] charge, int maxDiff) {
        long result = countPairs(charge, maxDiff);
        System.out.println("charge = " + Arrays.toString(charge));
        System.out.println("maxDiff = " + maxDiff);
        System.out.println("Valid pair count = " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations from the problem statement.
     *
     * Verified examples:
     *
     * Example 1:
     * charge = [10, 12, 13, 17], maxDiff = 3
     * Valid pairs:
     * - (10,12) difference 2
     * - (10,13) difference 3
     * - (12,13) difference 1
     * Pairs with 17 are invalid because:
     * - 17 - 10 = 7
     * - 17 - 12 = 5
     * - 17 - 13 = 4
     * So answer = 3
     *
     * Example 2:
     * charge = [4, 4, 5, 8, 9], maxDiff = 1
     * Valid pairs:
     * - (4 at index 0, 4 at index 1)
     * - (4 at index 0, 5)
     * - (4 at index 1, 5)
     * - (8,9)
     * So answer = 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(1), ignoring input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] charge1 = {10, 12, 13, 17};
        int maxDiff1 = 3;
        long result1 = solution.demonstrate(charge1, maxDiff1);
        System.out.println("Expected: 3");
        System.out.println("Matches expected: " + (result1 == 3));
        System.out.println();

        int[] charge2 = {4, 4, 5, 8, 9};
        int maxDiff2 = 1;
        long result2 = solution.demonstrate(charge2, maxDiff2);
        System.out.println("Expected: 4");
        System.out.println("Matches expected: " + (result2 == 4));
        System.out.println();

        // Additional quick sanity check:
        // All equal values with maxDiff = 0
        // Every pair is valid.
        int[] charge3 = {7, 7, 7, 7};
        int maxDiff3 = 0;
        long result3 = solution.demonstrate(charge3, maxDiff3);
        System.out.println("Expected: 6");
        System.out.println("Matches expected: " + (result3 == 6));
    }
}