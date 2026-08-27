import java.util.*;

/*
 * Title: Maximum Comfort from Skipping Adjacent Hotel Nights
 * Difficulty: Easy
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are planning a road trip with a list of hotel options, one for each night of the trip.
 * The i-th hotel gives you a comfort score represented by comfort[i]. Because moving luggage
 * and checking in on back-to-back nights is too exhausting, you are not allowed to stay in
 * hotels on two adjacent nights. You may choose any set of nights to book, as long as no two
 * chosen nights are consecutive.
 *
 * Return the maximum total comfort score you can get.
 *
 * This is a classic decision-style dynamic programming problem: for each night, you can either
 * skip that hotel and keep the best score from previous nights, or book it and add its comfort
 * score to the best result that ends at least one night earlier.
 *
 * Constraints:
 * - 1 <= comfort.length <= 100
 * - 0 <= comfort[i] <= 1000
 *
 * Example 1:
 * Input: comfort = [6, 7, 1, 30, 8, 2, 4]
 * Output: 41
 * Explanation: One optimal choice is nights with comfort 7, 30, and 4. Their total is 41.
 * You cannot take 6 and 7 together because those nights are adjacent.
 *
 * Example 2:
 * Input: comfort = [5, 1, 1, 5]
 * Output: 10
 * Explanation: Choose the first and last nights. The total comfort is 5 + 5 = 10.
 */

public class Solution {

    /**
     * Computes the maximum total comfort score using dynamic programming with O(1) extra space.
     *
     * The idea:
     * - At each night, we have two choices:
     *   1. Skip the current hotel, so the best total remains the same as the previous night.
     *   2. Take the current hotel, so we add its comfort to the best total from two nights ago.
     * - We keep only the last two DP states instead of storing the whole DP array.
     *
     * @param comfort an array where comfort[i] is the comfort score of staying at the hotel on night i
     * @return the maximum total comfort score possible without choosing adjacent nights
     * Time complexity: O(n), where n is the number of nights
     * Space complexity: O(1), because only a constant amount of extra memory is used
     */
    public int maxComfort(int[] comfort) {
        // Defensive handling for null or empty input.
        // The problem guarantees at least one element, but this makes the method safer and beginner-friendly.
        if (comfort == null || comfort.length == 0) {
            return 0;
        }

        // prevTwo represents the best answer considering nights up to index i - 2.
        // Initially, before processing any night, that value is 0.
        int prevTwo = 0;

        // prevOne represents the best answer considering nights up to index i - 1.
        // Initially, before processing any night, that value is also 0.
        int prevOne = 0;

        // Process each hotel/night one by one from left to right.
        for (int i = 0; i < comfort.length; i++) {
            // Option 1: skip the current night.
            // If we skip it, the best total is simply the best total we already had up to the previous night.
            int skipCurrent = prevOne;

            // Option 2: take the current night.
            // If we take it, we are not allowed to take the previous night,
            // so we add the current comfort score to the best total from two nights ago.
            int takeCurrent = prevTwo + comfort[i];

            // The best answer for the current position is the better of:
            // - skipping the current hotel
            // - taking the current hotel
            int currentBest = Math.max(skipCurrent, takeCurrent);

            // Move the window forward:
            // - what used to be prevOne now becomes prevTwo for the next iteration
            // - currentBest becomes prevOne for the next iteration
            prevTwo = prevOne;
            prevOne = currentBest;
        }

        // After processing all nights, prevOne stores the best possible total comfort.
        return prevOne;
    }

    /**
     * Computes the maximum total comfort score using a full DP array.
     *
     * This version is useful for learning because it makes the state transition explicit:
     * dp[i] = maximum comfort obtainable from the first i + 1 nights.
     *
     * Transition:
     * - Skip current night: dp[i - 1]
     * - Take current night: comfort[i] + dp[i - 2]
     * - Therefore: dp[i] = max(dp[i - 1], comfort[i] + dp[i - 2])
     *
     * @param comfort an array where comfort[i] is the comfort score of staying at the hotel on night i
     * @return the maximum total comfort score possible without choosing adjacent nights
     * Time complexity: O(n), where n is the number of nights
     * Space complexity: O(n), because a DP array is used
     */
    public int maxComfortWithDpArray(int[] comfort) {
        if (comfort == null || comfort.length == 0) {
            return 0;
        }

        int n = comfort.length;

        // If there is only one night, the best we can do is either take it or skip it.
        // Since comfort values are non-negative, taking it is optimal.
        if (n == 1) {
            return comfort[0];
        }

        // dp[i] will store the maximum comfort we can get considering nights 0 through i.
        int[] dp = new int[n];

        // Base case for the first night:
        // The best we can do is take the first hotel.
        dp[0] = comfort[0];

        // Base case for the second night:
        // We cannot take both night 0 and night 1, so we choose the larger comfort value.
        dp[1] = Math.max(comfort[0], comfort[1]);

        // Fill the DP table from left to right.
        for (int i = 2; i < n; i++) {
            // If we skip night i, the best total remains dp[i - 1].
            int skipCurrent = dp[i - 1];

            // If we take night i, then we must add comfort[i] to dp[i - 2].
            int takeCurrent = dp[i - 2] + comfort[i];

            // Choose the better of the two options.
            dp[i] = Math.max(skipCurrent, takeCurrent);
        }

        // The last cell contains the answer for the entire array.
        return dp[n - 1];
    }

    /**
     * Demonstrates the solution on the sample inputs and prints the results.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     * Time complexity: O(n) per demonstration call
     * Space complexity: O(1) for the optimized method call itself
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] comfort1 = {6, 7, 1, 30, 8, 2, 4};
        int[] comfort2 = {5, 1, 1, 5};

        int result1 = solution.maxComfort(comfort1);
        int result2 = solution.maxComfort(comfort2);

        System.out.println("Example 1 Input: " + Arrays.toString(comfort1));
        System.out.println("Example 1 Output: " + result1);
        System.out.println("Expected: 41");
        System.out.println();

        System.out.println("Example 2 Input: " + Arrays.toString(comfort2));
        System.out.println("Example 2 Output: " + result2);
        System.out.println("Expected: 10");
        System.out.println();

        // Additional demonstration using the DP-array version for learning/verification.
        System.out.println("Using DP array method for Example 1: " + solution.maxComfortWithDpArray(comfort1));
        System.out.println("Using DP array method for Example 2: " + solution.maxComfortWithDpArray(comfort2));
    }
}