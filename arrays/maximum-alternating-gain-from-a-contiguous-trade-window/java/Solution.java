import java.util.*;

/*
 * Title: Maximum Alternating Gain from a Contiguous Trade Window
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an integer array profits where profits[i] represents the net profit or loss
 * of the i-th trade executed during a day. A risk analyst wants to choose exactly one contiguous
 * window of trades and evaluate it with an alternating sign rule: the first chosen trade contributes
 * normally, the second is subtracted, the third is added, the fourth is subtracted, and so on.
 *
 * In other words, for a chosen subarray profits[l..r], its score is:
 * profits[l] - profits[l+1] + profits[l+2] - profits[l+3] + ...
 *
 * Return the maximum possible alternating score over all non-empty contiguous subarrays.
 *
 * The chosen window must remain contiguous, but you may start and end anywhere in the array.
 * Values may be positive, zero, or negative. Because subtraction can turn a negative value into a gain,
 * the best answer is not necessarily the same as the maximum subarray sum.
 *
 * Constraints:
 * - 1 <= profits.length <= 200000
 * - -1000000000 <= profits[i] <= 1000000000
 * - The answer fits in a signed 64-bit integer.
 *
 * Example 1:
 * Input: profits = [4, 2, 5, 3]
 * Output: 7
 * Explanation: Choosing the subarray [4, 2, 5] gives 4 - 2 + 5 = 7.
 *
 * Example 2:
 * Input: profits = [5, -1, -3, 4]
 * Correct Output: 6
 * Explanation:
 * - [5, -1] gives 5 - (-1) = 6
 * - [-1, -3, 4] gives -1 - (-3) + 4 = 6
 * So the maximum alternating score is 6.
 */

public class Solution {

    /**
     * Computes the maximum alternating score over all non-empty contiguous subarrays.
     *
     * Core idea:
     * We process the array from left to right and maintain two dynamic programming states:
     *
     * 1. plusEnd:
     *    The maximum alternating score of a subarray that ends at the current index,
     *    where the current element is taken with a '+' sign.
     *
     * 2. minusEnd:
     *    The maximum alternating score of a subarray that ends at the current index,
     *    where the current element is taken with a '-' sign.
     *
     * Why these states work:
     * - Any valid subarray must start with '+'.
     * - Therefore:
     *   - A subarray ending with '+' can either:
     *       a) start fresh at the current element, score = profits[i]
     *       b) extend a previous subarray that ended with '-', then add current value
     *          score = previousMinusEnd + profits[i]
     *
     *   - A subarray ending with '-' cannot start fresh, because the first sign of any chosen
     *     subarray must be '+'.
     *     So it can only be formed by extending a previous subarray that ended with '+':
     *       score = previousPlusEnd - profits[i]
     *
     * We compute these transitions for every index and keep a global maximum.
     *
     * @param profits the array of trade profits/losses
     * @return the maximum alternating score among all non-empty contiguous subarrays
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maxAlternatingGain(int[] profits) {
        // We use a very small sentinel value to represent an impossible state.
        // Specifically, "minusEnd" is impossible at the first element because a subarray
        // cannot begin with a '-' sign.
        final long NEG_INF = Long.MIN_VALUE / 4;

        // plusEnd:
        // Best alternating score for a subarray ending at current index,
        // with current element contributing as '+'.
        long plusEnd = NEG_INF;

        // minusEnd:
        // Best alternating score for a subarray ending at current index,
        // with current element contributing as '-'.
        long minusEnd = NEG_INF;

        // This stores the best answer seen anywhere in the array.
        long answer = NEG_INF;

        // Process each trade one by one.
        for (int value : profits) {
            long x = value;

            // Save previous states before updating.
            long previousPlusEnd = plusEnd;
            long previousMinusEnd = minusEnd;

            // Step 1: Compute newPlusEnd.
            //
            // A subarray ending here with '+' can be formed in two ways:
            //
            // Option A: Start a brand new subarray at this index.
            //           Since the first sign is always '+', score = x.
            //
            // Option B: Extend a previous subarray that ended with '-',
            //           so the sign alternates back to '+'.
            //           score = previousMinusEnd + x
            //
            // We take the better of the valid options.
            long newPlusEnd = x;
            if (previousMinusEnd != NEG_INF) {
                newPlusEnd = Math.max(newPlusEnd, previousMinusEnd + x);
            }

            // Step 2: Compute newMinusEnd.
            //
            // A subarray ending here with '-' cannot start fresh,
            // because the first element of any chosen subarray must be '+'.
            //
            // Therefore, the only valid way is to extend a previous subarray
            // that ended with '+'.
            //
            // score = previousPlusEnd - x
            long newMinusEnd = NEG_INF;
            if (previousPlusEnd != NEG_INF) {
                newMinusEnd = previousPlusEnd - x;
            }

            // Step 3: Store updated states.
            plusEnd = newPlusEnd;
            minusEnd = newMinusEnd;

            // Step 4: Update the global answer.
            //
            // Any valid subarray ending here may end with either '+' or '-',
            // so both states are candidates.
            answer = Math.max(answer, plusEnd);
            answer = Math.max(answer, minusEnd);
        }

        return answer;
    }

    /**
     * Convenience wrapper that accepts a List of integers and converts it to an array.
     * This is not required for the core algorithm, but it can be helpful for demonstrations.
     *
     * @param profitsList the trade profits/losses as a list
     * @return the maximum alternating score among all non-empty contiguous subarrays
     * Time complexity: O(n)
     * Space complexity: O(n) due to array conversion
     */
    public long maxAlternatingGain(List<Integer> profitsList) {
        int[] profits = new int[profitsList.size()];
        for (int i = 0; i < profitsList.size(); i++) {
            profits[i] = profitsList.get(i);
        }
        return maxAlternatingGain(profits);
    }

    /**
     * Helper method to print an input array and its computed answer.
     *
     * @param profits the input array
     * @return nothing
     * Time complexity: O(n)
     * Space complexity: O(1) auxiliary
     */
    public static void runExample(int[] profits) {
        Solution solution = new Solution();
        long result = solution.maxAlternatingGain(profits);
        System.out.println("profits = " + Arrays.toString(profits));
        System.out.println("maximum alternating score = " + result);
        System.out.println();
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra cases.
     *
     * Verified examples:
     * - [4, 2, 5, 3] -> 7
     * - [5, -1, -3, 4] -> 6
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total number of demonstrated elements)
     * Space complexity: O(1) auxiliary, excluding input storage
     */
    public static void main(String[] args) {
        // Sample 1 from the statement.
        // Best subarray: [4, 2, 5] => 4 - 2 + 5 = 7
        runExample(new int[]{4, 2, 5, 3});

        // Sample 2 from the statement.
        // Correct maximum is 6.
        // Examples:
        // [5, -1] => 5 - (-1) = 6
        // [-1, -3, 4] => -1 - (-3) + 4 = 6
        runExample(new int[]{5, -1, -3, 4});

        // Extra checks.
        runExample(new int[]{7});                 // Single element => 7
        runExample(new int[]{-5});                // Single negative => -5
        runExample(new int[]{1, 2, 3, 4});        // Best may be [3,4] => 3 - 4 = -1, but [3] or [4] better; answer should be 4
        runExample(new int[]{3, -2, 5, -1, 6});  // Mixed values
    }
}