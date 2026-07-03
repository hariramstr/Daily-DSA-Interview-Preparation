import java.util.*;

/*
Problem Title: Maximum Donation Sum from Skipping Adjacent Booths

Problem Description:
You are organizing a charity fair with a row of donation booths. Each booth has a non-negative integer amount
representing how much money can be collected from that booth. However, due to staffing limits, you cannot
operate two adjacent booths on the same day. If you open booth i, then booths i - 1 and i + 1 must remain closed.

Your task is to return the maximum total donation amount that can be collected from the row of booths while
following this rule.

This is a classic one-dimensional decision problem: for each booth, you can either skip it or open it, but
opening it prevents using the previous booth. An efficient solution should use dynamic programming to build
the best answer from smaller prefixes of the array.

Constraints:
- 1 <= booths.length <= 100
- 0 <= booths[i] <= 1000

Input format:
- An integer array booths where booths[i] is the donation amount available at booth i.

Output format:
- Return a single integer: the maximum donation sum obtainable without choosing adjacent booths.

Example 1:
Input: booths = [5, 1, 2, 10, 6]
Output: 15
Explanation: The best valid choice is 5 + 10 = 15. Choosing 5 + 2 + 6 gives 13, which is smaller.

Example 2:
Input: booths = [4, 7, 3, 9]
Output: 16
Explanation: The best choice is booths with amounts 7 and 9. They are not adjacent, so the total is 16.
*/

public class Solution {

    /**
     * Computes the maximum donation sum that can be collected without selecting
     * two adjacent booths.
     *
     * This method uses dynamic programming:
     * - For each booth, we decide whether to:
     *   1) skip it, keeping the best answer from the previous booth
     *   2) take it, adding its value to the best answer from two booths back
     * - The best answer at each position is the maximum of those two choices
     *
     * @param booths the array where booths[i] is the donation amount at booth i
     * @return the maximum total donation sum obtainable without choosing adjacent booths
     * Time complexity: O(n), where n is the number of booths
     * Space complexity: O(n), due to the dynamic programming array
     */
    public int maxDonation(int[] booths) {
        // Defensive handling for null or empty input.
        // The problem guarantees at least one element, but this makes the method safer.
        if (booths == null || booths.length == 0) {
            return 0;
        }

        // If there is only one booth, the answer is simply its donation amount,
        // because there are no adjacent booths to worry about.
        if (booths.length == 1) {
            return booths[0];
        }

        // dp[i] will store the maximum donation sum we can collect
        // considering booths from index 0 through index i.
        int[] dp = new int[booths.length];

        // Base case 1:
        // If we only consider booth 0, the best we can do is take booth 0.
        dp[0] = booths[0];

        // Base case 2:
        // If we consider booths 0 and 1, we cannot take both because they are adjacent.
        // So the best answer is the larger of the two values.
        dp[1] = Math.max(booths[0], booths[1]);

        // Build the solution from left to right.
        for (int i = 2; i < booths.length; i++) {
            // Option 1: skip the current booth.
            // Then our best total remains whatever was best up to booth i - 1.
            int skipCurrent = dp[i - 1];

            // Option 2: take the current booth.
            // If we take booth i, we cannot take booth i - 1,
            // so we add booths[i] to the best answer up to booth i - 2.
            int takeCurrent = dp[i - 2] + booths[i];

            // Choose the better of the two options.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        // The last entry contains the best answer for the entire array.
        return dp[booths.length - 1];
    }

    /**
     * Computes the maximum donation sum that can be collected without selecting
     * two adjacent booths, using an optimized constant-space dynamic programming approach.
     *
     * This version keeps only the last two DP states instead of a full array:
     * - prevTwo = best answer up to i - 2
     * - prevOne = best answer up to i - 1
     *
     * @param booths the array where booths[i] is the donation amount at booth i
     * @return the maximum total donation sum obtainable without choosing adjacent booths
     * Time complexity: O(n), where n is the number of booths
     * Space complexity: O(1), excluding input storage
     */
    public int maxDonationOptimized(int[] booths) {
        // Defensive handling for null or empty input.
        if (booths == null || booths.length == 0) {
            return 0;
        }

        // If there is only one booth, return its value directly.
        if (booths.length == 1) {
            return booths[0];
        }

        // prevTwo represents dp[i - 2]
        int prevTwo = booths[0];

        // prevOne represents dp[i - 1]
        int prevOne = Math.max(booths[0], booths[1]);

        // Process remaining booths one by one.
        for (int i = 2; i < booths.length; i++) {
            // If we skip the current booth, total remains prevOne.
            int skipCurrent = prevOne;

            // If we take the current booth, total becomes prevTwo + booths[i].
            int takeCurrent = prevTwo + booths[i];

            // Current best is the better of skipping or taking.
            int current = Math.max(skipCurrent, takeCurrent);

            // Shift the window forward for the next iteration.
            prevTwo = prevOne;
            prevOne = current;
        }

        // prevOne now holds the best answer for the full array.
        return prevOne;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstration calls, excluding the algorithm calls
     * Space complexity: O(1), excluding the arrays created for demonstration
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] booths1 = {5, 1, 2, 10, 6};
        int[] booths2 = {4, 7, 3, 9};
        int[] booths3 = {8};
        int[] booths4 = {2, 1, 1, 2};
        int[] booths5 = {0, 0, 0, 0};

        System.out.println("Example 1:");
        System.out.println("Input: " + Arrays.toString(booths1));
        System.out.println("Output: " + solution.maxDonation(booths1));
        System.out.println("Expected: 15");
        System.out.println();

        System.out.println("Example 2:");
        System.out.println("Input: " + Arrays.toString(booths2));
        System.out.println("Output: " + solution.maxDonation(booths2));
        System.out.println("Expected: 16");
        System.out.println();

        System.out.println("Additional Test 1:");
        System.out.println("Input: " + Arrays.toString(booths3));
        System.out.println("Output: " + solution.maxDonation(booths3));
        System.out.println("Expected: 8");
        System.out.println();

        System.out.println("Additional Test 2:");
        System.out.println("Input: " + Arrays.toString(booths4));
        System.out.println("Output: " + solution.maxDonation(booths4));
        System.out.println("Expected: 4");
        System.out.println();

        System.out.println("Additional Test 3:");
        System.out.println("Input: " + Arrays.toString(booths5));
        System.out.println("Output: " + solution.maxDonation(booths5));
        System.out.println("Expected: 0");
        System.out.println();

        System.out.println("Optimized version check for Example 1:");
        System.out.println("Input: " + Arrays.toString(booths1));
        System.out.println("Output: " + solution.maxDonationOptimized(booths1));
        System.out.println("Expected: 15");
    }
}