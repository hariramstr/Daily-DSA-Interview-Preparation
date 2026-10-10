import java.util.*;

/*
 * Title: Count Repair Teams Within Arrival Spread
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * A facilities company is scheduling emergency repair teams for a large campus.
 * You are given an integer array arrivalTimes where arrivalTimes[i] is the arrival time,
 * in minutes, of the i-th team. You are also given an integer maxSpread.
 * Two teams are considered compatible if the absolute difference between their arrival times
 * is less than or equal to maxSpread.
 *
 * Your task is to return the total number of distinct pairs of teams (i, j) such that
 * i < j and the two teams are compatible.
 *
 * Because the input may be unsorted and can be large, an efficient solution is required.
 * A brute-force O(n^2) approach will be too slow for the largest cases.
 * The intended solution uses sorting together with a two-pointer scan to count,
 * for each right endpoint, how many earlier teams can pair with it while staying
 * within the allowed spread.
 *
 * Constraints:
 * - 1 <= arrivalTimes.length <= 200000
 * - 0 <= arrivalTimes[i] <= 1000000000
 * - 0 <= maxSpread <= 1000000000
 * - The answer may exceed 32-bit integer range, so use a 64-bit integer type where needed.
 *
 * Example 1:
 * Input: arrivalTimes = [12, 5, 9, 14], maxSpread = 4
 * Output: 4
 * Explanation: After sorting, arrival times are [5, 9, 12, 14].
 * Compatible pairs are (5,9), (9,12), (9,14), and (12,14).
 *
 * Example 2:
 * Input: arrivalTimes = [3, 3, 3, 10], maxSpread = 0
 * Output: 3
 * Explanation: Only teams with exactly the same arrival time can pair.
 * The three teams arriving at time 3 form 3 distinct pairs.
 */

public class Solution {

    /**
     * Counts the number of distinct compatible pairs of teams.
     *
     * The method first sorts the arrival times. After sorting, for every position "right",
     * all valid partners for arrivalTimes[right] must lie in a contiguous range ending at right - 1.
     * We maintain a pointer "left" such that the window [left, right] is the smallest window
     * whose maximum minus minimum is at most maxSpread.
     *
     * For each right:
     * - Move left forward while the spread in the current window is too large.
     * - Once valid, every index from left to right - 1 can pair with right.
     * - That contributes (right - left) pairs.
     *
     * @param arrivalTimes the array of team arrival times in minutes
     * @param maxSpread the maximum allowed absolute difference between two compatible arrival times
     * @return the total number of distinct compatible pairs as a long
     * Time complexity: O(n log n) due to sorting, plus O(n) for the two-pointer scan
     * Space complexity: O(1) extra space beyond the sorting implementation details used by Java
     */
    public long countCompatiblePairs(int[] arrivalTimes, int maxSpread) {
        // Defensive handling for completeness.
        // With fewer than 2 teams, no pair can exist.
        if (arrivalTimes == null || arrivalTimes.length < 2) {
            return 0L;
        }

        // Sort the array so that:
        // 1. The absolute difference condition becomes easier to reason about.
        // 2. For any fixed "right", all valid "left-side" partners form one continuous block.
        Arrays.sort(arrivalTimes);

        // This will store the total number of valid pairs.
        // We use long because the number of pairs can be as large as n * (n - 1) / 2,
        // which exceeds int for large n.
        long totalPairs = 0L;

        // "left" marks the beginning of the current valid window.
        // The window will always satisfy:
        // arrivalTimes[right] - arrivalTimes[left] <= maxSpread
        // after we finish adjusting it for each right.
        int left = 0;

        // Expand the window by moving "right" from left to right across the sorted array.
        for (int right = 0; right < arrivalTimes.length; right++) {

            // While the current window violates the allowed spread,
            // move "left" forward to shrink the window.
            //
            // Because the array is sorted:
            // - arrivalTimes[right] is the largest value in the window
            // - arrivalTimes[left] is the smallest value in the window
            //
            // So checking only:
            // arrivalTimes[right] - arrivalTimes[left] > maxSpread
            // is enough to know the window is invalid.
            while ((long) arrivalTimes[right] - arrivalTimes[left] > maxSpread) {
                left++;
            }

            // At this point, the window [left, right] is valid.
            //
            // Every index i in [left, right - 1] can pair with "right",
            // because:
            // arrivalTimes[right] - arrivalTimes[i] <= arrivalTimes[right] - arrivalTimes[left] <= maxSpread
            //
            // Number of such indices = right - left
            totalPairs += (right - left);
        }

        return totalPairs;
    }

    /**
     * Brute-force verification method for small inputs.
     * This is useful for learning and testing correctness, but not for large inputs.
     *
     * It checks every pair (i, j) with i < j and counts it if the absolute difference
     * between arrival times is at most maxSpread.
     *
     * @param arrivalTimes the array of team arrival times in minutes
     * @param maxSpread the maximum allowed absolute difference between two compatible arrival times
     * @return the total number of distinct compatible pairs as a long
     * Time complexity: O(n^2)
     * Space complexity: O(1)
     */
    public long countCompatiblePairsBruteForce(int[] arrivalTimes, int maxSpread) {
        if (arrivalTimes == null || arrivalTimes.length < 2) {
            return 0L;
        }

        long count = 0L;

        for (int i = 0; i < arrivalTimes.length; i++) {
            for (int j = i + 1; j < arrivalTimes.length; j++) {
                if (Math.abs((long) arrivalTimes[i] - arrivalTimes[j]) <= maxSpread) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Creates a copy of the given array.
     * This helper is used in the demo so that methods that sort do not modify the original sample arrays.
     *
     * @param arr the input array
     * @return a copied array, or null if the input is null
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] copyArray(int[] arr) {
        if (arr == null) {
            return null;
        }
        return Arrays.copyOf(arr, arr.length);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * It also prints the expected answers so a beginner can visually confirm correctness.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n log n) per demonstrated test case
     * Space complexity: O(n) for copied arrays used in demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] arrivalTimes1 = {12, 5, 9, 14};
        int maxSpread1 = 4;
        long result1 = solution.countCompatiblePairs(solution.copyArray(arrivalTimes1), maxSpread1);

        System.out.println("Sample 1:");
        System.out.println("arrivalTimes = " + Arrays.toString(arrivalTimes1));
        System.out.println("maxSpread = " + maxSpread1);
        System.out.println("Output = " + result1);
        System.out.println("Expected = 4");
        System.out.println();

        // Sample 2
        int[] arrivalTimes2 = {3, 3, 3, 10};
        int maxSpread2 = 0;
        long result2 = solution.countCompatiblePairs(solution.copyArray(arrivalTimes2), maxSpread2);

        System.out.println("Sample 2:");
        System.out.println("arrivalTimes = " + Arrays.toString(arrivalTimes2));
        System.out.println("maxSpread = " + maxSpread2);
        System.out.println("Output = " + result2);
        System.out.println("Expected = 3");
        System.out.println();

        // Additional small verification examples
        int[] arrivalTimes3 = {1, 2, 3, 4, 5};
        int maxSpread3 = 2;
        long result3 = solution.countCompatiblePairs(solution.copyArray(arrivalTimes3), maxSpread3);
        long brute3 = solution.countCompatiblePairsBruteForce(arrivalTimes3, maxSpread3);

        System.out.println("Additional Test 1:");
        System.out.println("arrivalTimes = " + Arrays.toString(arrivalTimes3));
        System.out.println("maxSpread = " + maxSpread3);
        System.out.println("Two-pointer Output = " + result3);
        System.out.println("Brute-force Output = " + brute3);
        System.out.println();

        int[] arrivalTimes4 = {7, 7, 7, 7};
        int maxSpread4 = 0;
        long result4 = solution.countCompatiblePairs(solution.copyArray(arrivalTimes4), maxSpread4);
        long brute4 = solution.countCompatiblePairsBruteForce(arrivalTimes4, maxSpread4);

        System.out.println("Additional Test 2:");
        System.out.println("arrivalTimes = " + Arrays.toString(arrivalTimes4));
        System.out.println("maxSpread = " + maxSpread4);
        System.out.println("Two-pointer Output = " + result4);
        System.out.println("Brute-force Output = " + brute4);
    }
}