import java.util.*;

/*
 * Title: Longest Even-Parity Access Window
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * A security system records a sequence of access events. Each event is labeled with an integer
 * from 0 to 19 representing one of 20 badge categories. You are given an array events where
 * events[i] is the category of the i-th event.
 *
 * A contiguous window of events is called balanced if, for every badge category, the number of
 * times it appears inside that window is even.
 *
 * Your task is to return the length of the longest balanced contiguous window.
 *
 * Because there are only 20 possible categories, an efficient solution should take advantage of
 * bit manipulation. One useful observation is that while scanning the array from left to right,
 * you can represent the parity (even or odd count) of each category seen so far as a bitmask of
 * length 20. If the same parity mask appears at two different positions, then the subarray between
 * those positions has even frequency for every category.
 *
 * Return 0 if no non-empty balanced window exists.
 *
 * Constraints:
 * - 1 <= events.length <= 200000
 * - 0 <= events[i] < 20
 * - Expected time complexity: O(n)
 * - Expected extra space: O(min(n, 2^20))
 *
 * Example 1:
 * Input: events = [3, 5, 3, 5, 7, 7]
 * Output: 6
 * Explanation: In the full array, categories 3, 5, and 7 each appear exactly twice,
 * so the entire window is balanced.
 *
 * Example 2:
 * Input: events = [1, 2, 1, 4, 2, 4, 4]
 * Output: 6
 * Explanation: The subarray [1, 2, 1, 4, 2, 4] is balanced because categories 1, 2,
 * and 4 each appear twice. The full array is not balanced because category 4 appears
 * three times.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray in which every category
     * appears an even number of times.
     *
     * Core idea:
     * - Maintain a 20-bit parity mask while scanning from left to right.
     * - Bit k is 0 if category k has appeared an even number of times so far.
     * - Bit k is 1 if category k has appeared an odd number of times so far.
     * - If the same mask appears at two different prefix positions, then the subarray
     *   between those positions has even count for every category.
     *
     * @param events the array of badge category events, where each value is in the range [0, 19]
     * @return the maximum length of a balanced contiguous window; returns 0 if none exists
     * Time complexity: O(n)
     * Space complexity: O(min(n, 2^20))
     */
    public int longestBalancedWindow(int[] events) {
        // There are exactly 2^20 possible parity masks because each of the 20 categories
        // can be either even-parity (0) or odd-parity (1).
        final int TOTAL_MASKS = 1 << 20;

        // firstSeen[mask] will store the earliest prefix index where this mask appeared.
        //
        // Important indexing convention:
        // - We think in terms of prefix lengths.
        // - Prefix length 0 means "before reading any element".
        // - After processing events[i], the current prefix length is i + 1.
        //
        // If the same mask appears first at prefix p and later at prefix q,
        // then the subarray from index p to q - 1 is balanced and has length q - p.
        int[] firstSeen = new int[TOTAL_MASKS];

        // Fill with a sentinel meaning "this mask has not been seen yet".
        Arrays.fill(firstSeen, -2);

        // Before processing any events, parity mask is 0 because all counts are zero (even).
        // This mask is seen at prefix length 0.
        firstSeen[0] = 0;

        // Current parity mask while scanning the array.
        int mask = 0;

        // Best answer found so far.
        int best = 0;

        // Process each event one by one.
        for (int i = 0; i < events.length; i++) {
            int category = events[i];

            // Toggle the bit corresponding to this category.
            //
            // Why toggle?
            // - If this category has appeared an even number of times so far, one more
            //   occurrence makes it odd.
            // - If it has appeared an odd number of times so far, one more occurrence
            //   makes it even.
            //
            // XOR with (1 << category) flips exactly that one bit.
            mask ^= (1 << category);

            // Current prefix length after including events[i].
            int currentPrefixLength = i + 1;

            // If we have seen this exact mask before, then the subarray between the first
            // occurrence and now is balanced.
            //
            // Reason:
            // Let prefixParity(a) be the parity mask after reading a elements.
            // If prefixParity(p) == prefixParity(q), then for every category, the parity
            // change from p to q is zero, meaning each category appears an even number of
            // times in that subarray.
            if (firstSeen[mask] != -2) {
                int candidateLength = currentPrefixLength - firstSeen[mask];
                if (candidateLength > best) {
                    best = candidateLength;
                }
            } else {
                // Store only the earliest occurrence of this mask.
                //
                // This is crucial for maximizing subarray length:
                // if the same mask appears again later, pairing the current position with
                // the earliest occurrence gives the longest possible balanced window ending here.
                firstSeen[mask] = currentPrefixLength;
            }
        }

        return best;
    }

    /**
     * Helper method to print an integer array in a readable format.
     *
     * @param arr the array to print
     * @return a string representation such as [1, 2, 3]
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement
     * and prints the results.
     *
     * Also includes expected outputs so it is easy to verify correctness manually.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demo arrays)
     * Space complexity: O(1) excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] events1 = {3, 5, 3, 5, 7, 7};
        int result1 = solution.longestBalancedWindow(events1);
        System.out.println("Sample 1:");
        System.out.println("events = " + solution.arrayToString(events1));
        System.out.println("Longest balanced window length = " + result1);
        System.out.println("Expected = 6");
        System.out.println();

        // Sample 2
        int[] events2 = {1, 2, 1, 4, 2, 4, 4};
        int result2 = solution.longestBalancedWindow(events2);
        System.out.println("Sample 2:");
        System.out.println("events = " + solution.arrayToString(events2));
        System.out.println("Longest balanced window length = " + result2);
        System.out.println("Expected = 6");
        System.out.println();

        // Additional quick checks for beginners:
        // 1) No non-empty balanced window
        int[] events3 = {0};
        int result3 = solution.longestBalancedWindow(events3);
        System.out.println("Additional Check 1:");
        System.out.println("events = " + solution.arrayToString(events3));
        System.out.println("Longest balanced window length = " + result3);
        System.out.println("Expected = 0");
        System.out.println();

        // 2) Entire array balanced
        int[] events4 = {2, 2, 3, 3, 4, 4};
        int result4 = solution.longestBalancedWindow(events4);
        System.out.println("Additional Check 2:");
        System.out.println("events = " + solution.arrayToString(events4));
        System.out.println("Longest balanced window length = " + result4);
        System.out.println("Expected = 6");
    }
}