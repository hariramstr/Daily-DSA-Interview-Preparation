import java.util.*;

/*
 * Title: Longest Checkout Span With Gift Card Balance Floor
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an integer array transactions where transactions[i] represents the net effect
 * of the i-th checkout event on a customer's gift card balance. A positive value means money
 * was added to the card, and a negative value means money was spent.
 *
 * The customer starts with an initial gift card balance startBalance. You want to find the
 * longest contiguous span of checkout events that could be processed in order such that,
 * at every point inside that span, the running balance never drops below 0.
 *
 * Formally, for a subarray transactions[l...r], define the running balance inside the span as
 * startBalance plus the prefix sum of that subarray up to each position. The span is valid if
 * for every index k between l and r, the balance after processing transactions[l...k] is at least 0.
 *
 * Return the length of the longest valid contiguous span.
 *
 * This is a sliding window problem: as you expand the right end of the window, the window may
 * become invalid because some prefix inside the current window causes the balance to go negative.
 * You must then shrink the left end until the window becomes valid again.
 *
 * Constraints:
 * - 1 <= transactions.length <= 200000
 * - -100000 <= transactions[i] <= 100000
 * - 0 <= startBalance <= 1000000000
 *
 * Example 1:
 * Input: transactions = [4, -3, -2, 5, -1], startBalance = 2
 * Output: 5
 * Explanation: Starting from 2, the running balances are 6, 3, 1, 6, 5.
 * They never go below 0, so the entire array is valid.
 *
 * Example 2:
 * Input: transactions = [-4, 3, -2, -1, 2], startBalance = 2
 * Output: 4
 * Explanation: The full array is invalid because the first event would make the balance -2.
 * The longest valid span is [3, -2, -1, 2]. Starting from 2, the running balances in that span
 * are 5, 3, 2, 4, so the answer is 4.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous span such that, starting from startBalance,
     * every running balance inside that span stays at least 0.
     *
     * Core idea:
     * 1. Build a global prefix sum array:
     *      prefix[i] = sum of transactions[0..i-1]
     * 2. For a window [l..r], the running sums inside the window are:
     *      transactions[l]
     *      transactions[l] + transactions[l+1]
     *      ...
     *      transactions[l] + ... + transactions[r]
     *
     *    In terms of the global prefix array, these are:
     *      prefix[l+1] - prefix[l],
     *      prefix[l+2] - prefix[l],
     *      ...
     *      prefix[r+1] - prefix[l]
     *
     *    Therefore the minimum running sum inside the window is:
     *      min(prefix[l+1..r+1]) - prefix[l]
     *
     *    The window is valid iff:
     *      startBalance + min(prefix[l+1..r+1]) - prefix[l] >= 0
     *
     * 3. While moving the right pointer, we need the minimum value of prefix in the range
     *    [l+1..r+1]. We maintain that minimum with a monotonic deque of prefix indices.
     *
     * 4. If the window becomes invalid, we move l forward until it becomes valid again.
     *
     * This is a true sliding window with efficient validity checks.
     *
     * @param transactions the net balance changes for each checkout event
     * @param startBalance the initial gift card balance before processing any event in a chosen span
     * @return the maximum length of a contiguous valid span
     * Time complexity: O(n), because each index enters and leaves the deque at most once.
     * Space complexity: O(n), for the prefix array and deque.
     */
    public int longestValidSpan(int[] transactions, long startBalance) {
        int n = transactions.length;

        // We use long for prefix sums because:
        // - n can be up to 200000
        // - each value can be up to 100000 in magnitude
        // So cumulative sums can exceed int range.
        long[] prefix = buildPrefixSums(transactions);

        // Deque will store indices into the prefix array.
        // Important invariant:
        // - indices are increasing from front to back
        // - prefix values at those indices are nondecreasing from front to back
        //
        // This means the front always holds the index of the minimum prefix value
        // in the current relevant range.
        Deque<Integer> minDeque = new ArrayDeque<>();

        int left = 0;
        int best = 0;

        // We expand the window by choosing each possible right endpoint.
        for (int right = 0; right < n; right++) {

            // For window [left..right], the relevant prefix range for minimum checking is [left+1..right+1].
            // As right increases by 1, we need to add prefix index (right + 1) into our structure.
            int newPrefixIndex = right + 1;

            // Maintain monotonic increasing prefix values in the deque.
            // If the new prefix value is smaller than or equal to values at the back,
            // those back indices can never become the minimum for any future window that also includes
            // this new index, so we remove them.
            while (!minDeque.isEmpty() && prefix[minDeque.peekLast()] >= prefix[newPrefixIndex]) {
                minDeque.pollLast();
            }
            minDeque.offerLast(newPrefixIndex);

            // Now shrink from the left while the current window is invalid.
            //
            // Validity condition:
            // startBalance + min(prefix[left+1..right+1]) - prefix[left] >= 0
            //
            // Since the deque front stores the minimum prefix index in the current range,
            // we can test validity in O(1).
            while (left <= right) {

                // Before checking validity, ensure the deque front is still inside [left+1..right+1].
                // If an index is <= left, it is no longer part of the required range.
                while (!minDeque.isEmpty() && minDeque.peekFirst() <= left) {
                    minDeque.pollFirst();
                }

                long minPrefixInWindow = prefix[minDeque.peekFirst()];
                long minimumRunningBalance = startBalance + (minPrefixInWindow - prefix[left]);

                // If the minimum running balance is nonnegative, the whole window is valid.
                if (minimumRunningBalance >= 0) {
                    break;
                }

                // Otherwise, the window is invalid, so we must move left forward.
                left++;
            }

            // After shrinking, [left..right] is valid (or left > right, which cannot happen here
            // because a single element window may still be invalid, but the loop structure ensures
            // left can become right+1 only after invalidating the current window. In that case,
            // the computed length below becomes 0, which is fine).
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Builds a standard prefix sum array where:
     * prefix[0] = 0
     * prefix[i] = sum of transactions[0..i-1] for i >= 1
     *
     * Example:
     * transactions = [4, -3, -2]
     * prefix = [0, 4, 1, -1]
     *
     * @param transactions the input transaction array
     * @return a long[] prefix sum array of length transactions.length + 1
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long[] buildPrefixSums(int[] transactions) {
        long[] prefix = new long[transactions.length + 1];
        for (int i = 0; i < transactions.length; i++) {
            prefix[i + 1] = prefix[i] + transactions[i];
        }
        return prefix;
    }

    /**
     * Convenience overload that accepts startBalance as int.
     *
     * @param transactions the net balance changes for each checkout event
     * @param startBalance the initial gift card balance
     * @return the maximum valid span length
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int longestValidSpan(int[] transactions, int startBalance) {
        return longestValidSpan(transactions, (long) startBalance);
    }

    /**
     * Demonstrates the algorithm on the sample inputs from the problem statement
     * and prints the results.
     *
     * Expected outputs:
     * Example 1 -> 5
     * Example 2 -> 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) across the demonstrated examples
     * Space complexity: O(n) for each example run
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] transactions1 = {4, -3, -2, 5, -1};
        int startBalance1 = 2;
        int result1 = solution.longestValidSpan(transactions1, startBalance1);
        System.out.println("Example 1 result: " + result1);

        int[] transactions2 = {-4, 3, -2, -1, 2};
        int startBalance2 = 2;
        int result2 = solution.longestValidSpan(transactions2, startBalance2);
        System.out.println("Example 2 result: " + result2);

        // Additional quick checks for beginners:
        int[] transactions3 = {-5};
        int startBalance3 = 4;
        int result3 = solution.longestValidSpan(transactions3, startBalance3);
        System.out.println("Single invalid event result: " + result3);

        int[] transactions4 = {-5};
        int startBalance4 = 5;
        int result4 = solution.longestValidSpan(transactions4, startBalance4);
        System.out.println("Single exactly-valid event result: " + result4);
    }
}