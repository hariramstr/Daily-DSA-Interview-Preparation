import java.util.*;

/*
Problem Title: Maximum Tips from Non-Consecutive Deliveries

Problem Description:
A courier works through a straight list of delivery requests for the day. The i-th request offers a tip given by tips[i]. However, accepting two consecutive requests is not allowed because each accepted delivery requires a cooldown period before the next nearby pickup. You may choose any set of requests as long as no two chosen requests are adjacent in the list.

Your task is to return the maximum total tip the courier can earn.

This is a classic decision process where, at each request, you either skip it or accept it and then skip the previous adjacent choice. An efficient solution should use dynamic programming to build the best answer for prefixes of the list.

Constraints:
- 1 <= tips.length <= 100000
- 0 <= tips[i] <= 10000
- The answer fits in a 32-bit signed integer

Example 1:
Input: tips = [5, 1, 2, 10, 6]
Output: 15
Explanation: The best choice is to accept requests with tips 5 and 10, for a total of 15. Choosing 5, 2, and 6 gives 13, which is smaller.

Example 2:
Input: tips = [4, 7, 3, 9]
Output: 16
Explanation: Accept the 2nd and 4th requests for tips 7 + 9 = 16. You cannot take 7 and 3 together because they are consecutive.

Return only the maximum total tip. If all tips are 0, the answer is 0.
*/

public class Solution {

    /**
     * Computes the maximum total tip that can be earned by selecting
     * non-consecutive delivery requests.
     *
     * This method uses dynamic programming with constant extra space.
     * At each position, we decide between:
     * 1) Skipping the current request, keeping the previous best total
     * 2) Taking the current request, which means we must add it to the best total
     *    from two positions back
     *
     * @param tips the array where tips[i] is the tip offered by the i-th delivery request
     * @return the maximum total tip obtainable without choosing adjacent requests
     * Time complexity: O(n), where n is the number of requests
     * Space complexity: O(1), excluding the input array
     */
    public int maxTotalTip(int[] tips) {
        // Defensive handling:
        // Although the constraints guarantee at least one element,
        // this check makes the method safer and beginner-friendly.
        if (tips == null || tips.length == 0) {
            return 0;
        }

        // "prevTwo" will represent the best answer for the subarray ending at index i - 2.
        // In DP terms, this is dp[i - 2].
        int prevTwo = 0;

        // "prevOne" will represent the best answer for the subarray ending at index i - 1.
        // In DP terms, this is dp[i - 1].
        int prevOne = 0;

        // We process each delivery request from left to right.
        for (int i = 0; i < tips.length; i++) {
            // Option 1: skip the current request.
            // If we skip it, the best total remains whatever we had up to the previous index.
            int skipCurrent = prevOne;

            // Option 2: take the current request.
            // If we take it, we cannot take the previous request,
            // so we add the current tip to the best total from two indices back.
            int takeCurrent = prevTwo + tips[i];

            // The best answer for the current position is the better of:
            // - skipping the current request
            // - taking the current request
            int currentBest = Math.max(skipCurrent, takeCurrent);

            // Move the DP window forward:
            // The old prevOne becomes prevTwo for the next iteration,
            // and currentBest becomes prevOne.
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all requests, prevOne holds the best possible total.
        return prevOne;
    }

    /**
     * Computes the maximum total tip using a full DP array.
     *
     * This version is more explicit and can be easier for beginners to understand,
     * because it stores the best answer for every prefix of the array.
     *
     * Let dp[i] mean:
     * the maximum total tip obtainable from the first i delivery requests.
     *
     * Transition:
     * dp[i] = max(
     *     dp[i - 1],                  // skip the i-th request
     *     dp[i - 2] + tips[i - 1]     // take the i-th request
     * )
     *
     * @param tips the array where tips[i] is the tip offered by the i-th delivery request
     * @return the maximum total tip obtainable without choosing adjacent requests
     * Time complexity: O(n), where n is the number of requests
     * Space complexity: O(n), for the DP array
     */
    public int maxTotalTipWithDpArray(int[] tips) {
        if (tips == null || tips.length == 0) {
            return 0;
        }

        int n = tips.length;

        // dp[i] = best answer using the first i elements
        // So dp[0] = 0 means using zero requests gives zero tip.
        int[] dp = new int[n + 1];

        // Base case:
        // Using only the first request, the best we can do is take it (or 0 if it is 0).
        dp[1] = tips[0];

        // Fill the DP table from left to right.
        for (int i = 2; i <= n; i++) {
            // If we skip the current request, answer stays dp[i - 1].
            int skipCurrent = dp[i - 1];

            // If we take the current request, we add its tip to dp[i - 2].
            int takeCurrent = dp[i - 2] + tips[i - 1];

            // Choose the better option.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        return dp[n];
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected values so the output can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) overall for the demonstrated test cases
     * Space complexity: O(1) extra for the main demonstration, excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] tips1 = {5, 1, 2, 10, 6};
        int result1 = solution.maxTotalTip(tips1);
        System.out.println("Input: " + Arrays.toString(tips1));
        System.out.println("Maximum total tip: " + result1);
        System.out.println("Expected: 15");
        System.out.println();

        int[] tips2 = {4, 7, 3, 9};
        int result2 = solution.maxTotalTip(tips2);
        System.out.println("Input: " + Arrays.toString(tips2));
        System.out.println("Maximum total tip: " + result2);
        System.out.println("Expected: 16");
        System.out.println();

        int[] tips3 = {0, 0, 0, 0};
        int result3 = solution.maxTotalTip(tips3);
        System.out.println("Input: " + Arrays.toString(tips3));
        System.out.println("Maximum total tip: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        int[] tips4 = {8};
        int result4 = solution.maxTotalTip(tips4);
        System.out.println("Input: " + Arrays.toString(tips4));
        System.out.println("Maximum total tip: " + result4);
        System.out.println("Expected: 8");
        System.out.println();

        int[] tips5 = {2, 1, 4, 9, 3, 1};
        int result5 = solution.maxTotalTip(tips5);
        System.out.println("Input: " + Arrays.toString(tips5));
        System.out.println("Maximum total tip: " + result5);
        System.out.println("Expected: 12");
    }
}