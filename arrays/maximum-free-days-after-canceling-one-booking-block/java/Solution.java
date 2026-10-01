import java.util.*;

/*
 * Title: Maximum Free Days After Canceling One Booking Block
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer n representing the number of days in a planning horizon,
 * labeled from 1 to n, and an array booked of length n where booked[i] is either 0 or 1.
 * A value of 1 means day i + 1 is already booked, and 0 means the day is free.
 *
 * You are allowed to cancel at most one contiguous block of booked days. In other words,
 * you may choose indices l and r such that every value in booked[l..r] is 1, and change
 * all of them to 0. You may also choose not to cancel anything.
 *
 * Return the maximum possible length of a contiguous run of free days after this operation.
 *
 * This models a scheduling system where one existing reservation block can be removed to
 * create the longest uninterrupted free interval.
 *
 * A cancellation block must be entirely inside one existing run of 1s. Canceling only part
 * of a booked run is allowed if that gives a better answer. Your goal is not to maximize
 * the number of canceled days, but to maximize the longest consecutive sequence of 0s in
 * the final array.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - booked.length == n
 * - booked[i] is either 0 or 1
 *
 * Example 1:
 * Input: booked = [0,1,1,0,0,1,0]
 * Output: 5
 * Explanation: Cancel the block [1,1] at indices 1..2. The array becomes
 * [0,0,0,0,0,1,0], so the longest free run has length 5.
 *
 * Example 2:
 * Input: booked = [1,0,1,1,1,0,1]
 * Output: 5
 * Explanation: Cancel the middle block of three booked days at indices 2..4.
 * The array becomes [1,0,0,0,0,0,1], producing a free run of length 5.
 */

public class Solution {

    /**
     * Computes the maximum possible length of a contiguous run of free days (0s)
     * after canceling at most one contiguous block of booked days (1s).
     *
     * Core idea:
     * - Any cancellation block must lie completely inside one existing run of 1s.
     * - If we cancel only part of a run of 1s, the resulting new 0s can extend at most
     *   one side unless the entire run is removed.
     * - Therefore, for a given run of 1s:
     *   1) Canceling a prefix can create: leftZeroRun + canceledLength
     *   2) Canceling a suffix can create: canceledLength + rightZeroRun
     *   3) Canceling the entire run can create: leftZeroRun + runLength + rightZeroRun
     * - Since the entire-run option is always at least as good as any partial cancellation
     *   within that same run, it is sufficient to evaluate each maximal run of 1s and
     *   merge the zero-run on its left and the zero-run on its right through that run.
     *
     * More concretely:
     * - First compute the longest existing run of 0s in case we choose not to cancel anything.
     * - Then scan every maximal run of 1s.
     * - For each such run, count:
     *   leftZeros  = number of consecutive 0s immediately before the run
     *   onesLength = length of the run of 1s
     *   rightZeros = number of consecutive 0s immediately after the run
     * - If we cancel that whole run, we get one merged free interval of:
     *   leftZeros + onesLength + rightZeros
     * - Take the maximum over all runs and also compare with the "do nothing" answer.
     *
     * @param booked the binary array where 1 means booked and 0 means free
     * @return the maximum possible length of a contiguous free interval after canceling
     *         at most one contiguous block of booked days
     *
     * Time complexity: O(n), because we scan the array a constant number of times.
     * Space complexity: O(1), excluding the input array.
     */
    public int maximumFreeDays(int[] booked) {
        int n = booked.length;

        // ------------------------------------------------------------
        // Step 1: Compute the best answer if we choose NOT to cancel anything.
        // ------------------------------------------------------------
        // This is simply the longest existing consecutive run of 0s.
        int best = longestZeroRun(booked);

        // ------------------------------------------------------------
        // Step 2: Scan the array and process every maximal run of 1s.
        // ------------------------------------------------------------
        // A maximal run of 1s means:
        // - it starts at index start
        // - it ends at index end
        // - all values in [start..end] are 1
        // - the positions just outside the run (if they exist) are not 1
        //
        // For each such run, if we cancel the ENTIRE run, then the zero-run on the left
        // and the zero-run on the right become connected through the canceled days.
        //
        // The resulting free interval length is:
        // leftZeros + onesLength + rightZeros
        //
        // We update the global best answer with this value.
        int i = 0;
        while (i < n) {
            // Skip free days until we find the start of a booked run.
            if (booked[i] == 0) {
                i++;
                continue;
            }

            // We are at the start of a run of 1s.
            int start = i;

            // Move i forward until the run of 1s ends.
            while (i < n && booked[i] == 1) {
                i++;
            }

            // Now:
            // - the run starts at 'start'
            // - the run ends at 'i - 1'
            int end = i - 1;
            int onesLength = end - start + 1;

            // --------------------------------------------------------
            // Step 2a: Count consecutive zeros immediately to the left.
            // --------------------------------------------------------
            int leftZeros = 0;
            int left = start - 1;
            while (left >= 0 && booked[left] == 0) {
                leftZeros++;
                left--;
            }

            // --------------------------------------------------------
            // Step 2b: Count consecutive zeros immediately to the right.
            // --------------------------------------------------------
            int rightZeros = 0;
            int right = end + 1;
            while (right < n && booked[right] == 0) {
                rightZeros++;
                right++;
            }

            // --------------------------------------------------------
            // Step 2c: If we cancel this whole run of 1s, the left and right
            // zero-runs merge into one larger zero-run.
            // --------------------------------------------------------
            int mergedFreeRun = leftZeros + onesLength + rightZeros;

            // Update the best answer found so far.
            best = Math.max(best, mergedFreeRun);
        }

        return best;
    }

    /**
     * Computes the length of the longest existing contiguous run of free days (0s)
     * in the array.
     *
     * This helper is useful because the problem allows canceling "at most one" block,
     * which means we are also allowed to do nothing. Therefore, the final answer must
     * be at least the longest zero-run already present in the original array.
     *
     * @param booked the binary array where 1 means booked and 0 means free
     * @return the maximum length of any contiguous run of 0s already present
     *
     * Time complexity: O(n), because we scan the array once.
     * Space complexity: O(1), because only a few variables are used.
     */
    public int longestZeroRun(int[] booked) {
        int best = 0;
        int current = 0;

        // Scan each day one by one.
        for (int value : booked) {
            if (value == 0) {
                // If the current day is free, extend the current zero-run.
                current++;
                best = Math.max(best, current);
            } else {
                // If the current day is booked, the zero-run ends here.
                current = 0;
            }
        }

        return best;
    }

    /**
     * Utility method to convert an int array into a readable string.
     *
     * @param arr the input integer array
     * @return a string representation such as [0, 1, 1, 0]
     *
     * Time complexity: O(n), because every element is visited once.
     * Space complexity: O(n), due to the created string content.
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional edge cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] booked1 = {0, 1, 1, 0, 0, 1, 0};
        int result1 = solution.maximumFreeDays(booked1);
        System.out.println("Input:  " + solution.arrayToString(booked1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 5");
        System.out.println();

        // Sample 2
        int[] booked2 = {1, 0, 1, 1, 1, 0, 1};
        int result2 = solution.maximumFreeDays(booked2);
        System.out.println("Input:  " + solution.arrayToString(booked2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 5");
        System.out.println();

        // Additional test: all free already
        int[] booked3 = {0, 0, 0, 0};
        int result3 = solution.maximumFreeDays(booked3);
        System.out.println("Input:  " + solution.arrayToString(booked3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional test: all booked
        int[] booked4 = {1, 1, 1, 1};
        int result4 = solution.maximumFreeDays(booked4);
        System.out.println("Input:  " + solution.arrayToString(booked4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional test: single day free
        int[] booked5 = {0};
        int result5 = solution.maximumFreeDays(booked5);
        System.out.println("Input:  " + solution.arrayToString(booked5));
        System.out.println("Output: " + result5);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional test: single day booked
        int[] booked6 = {1};
        int result6 = solution.maximumFreeDays(booked6);
        System.out.println("Input:  " + solution.arrayToString(booked6));
        System.out.println("Output: " + result6);
        System.out.println("Expected: 1");
        System.out.println();
    }
}