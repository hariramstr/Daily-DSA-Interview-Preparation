import java.util.*;

/*
Problem Title: Maximum Net Gain from One Interior Refund Block

Problem Description:
You are given an integer array transactions where transactions[i] represents the net profit or loss
from the i-th transaction of a day. Positive values are profits and negative values are losses.
You are also given an integer baseGain, representing a fixed gain that is always counted.

You may choose exactly one contiguous interior block of transactions to mark as refunded.
Refunding a block means its total contribution is subtracted from the day's result instead of added.
In other words, if the chosen block has sum S, the final result becomes:

baseGain + totalSum(transactions) - 2 * S

However, the refunded block must be an interior block:
- it cannot start at index 0
- it cannot end at index n - 1
- it must contain at least one element

Return the maximum possible final result after choosing one valid interior block.

This models a system where only a middle segment of transactions can be reversed after audit,
while the first and last transactions are locked and cannot be included.

Constraints:
- 3 <= transactions.length <= 200000
- -10^9 <= transactions[i] <= 10^9
- -10^9 <= baseGain <= 10^9
- The answer fits in a signed 64-bit integer.

Examples:
1) transactions = [4, -7, 3, -2, 5], baseGain = 10
   totalSum = 3
   We want to maximize:
   baseGain + totalSum - 2 * refundedSum
   Since baseGain and totalSum are fixed, we must minimize refundedSum over all valid interior subarrays.
   The minimum interior subarray sum is -7, achieved by [-7].
   Final result = 10 + 3 - 2 * (-7) = 27

2) transactions = [8, 2, 6], baseGain = -5
   The only valid interior block is [2]
   totalSum = 16
   Final result = -5 + 16 - 2 * 2 = 7

Efficient Insight:
The expression
    baseGain + totalSum - 2 * S
is maximized when S is as small as possible.
So the task becomes:
    Find the minimum-sum contiguous subarray entirely inside indices [1, n - 2].

This can be done in O(n) time using a Kadane-style minimum subarray algorithm.
*/

public class Solution {

    /**
     * Computes the maximum possible final result after refunding exactly one valid interior block.
     *
     * Core idea:
     * The final result is:
     *     baseGain + totalSum(transactions) - 2 * refundedBlockSum
     *
     * Since baseGain and totalSum(transactions) are fixed for the input,
     * maximizing the final result is exactly the same as minimizing refundedBlockSum.
     *
     * Because the refunded block must be interior, we are only allowed to choose
     * a contiguous subarray fully contained in indices [1, n - 2].
     *
     * Therefore, the problem reduces to:
     *     Find the minimum contiguous subarray sum in the subarray transactions[1..n-2].
     *
     * We solve that in linear time using a minimum-subarray version of Kadane's algorithm.
     *
     * @param transactions the array of transaction gains/losses; first and last elements cannot be refunded
     * @param baseGain the fixed gain always included in the final result
     * @return the maximum possible final result after refunding exactly one valid interior block
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maximumNetGain(int[] transactions, int baseGain) {
        long totalSum = totalSum(transactions);
        long minimumInteriorBlockSum = minimumInteriorSubarraySum(transactions);
        return (long) baseGain + totalSum - 2L * minimumInteriorBlockSum;
    }

    /**
     * Computes the total sum of all transactions.
     *
     * @param transactions the input transaction array
     * @return the sum of all elements as a long
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long totalSum(int[] transactions) {
        long sum = 0L;
        for (int value : transactions) {
            sum += value;
        }
        return sum;
    }

    /**
     * Finds the minimum sum of any contiguous subarray that lies completely inside the interior:
     * indices [1, n - 2].
     *
     * This is a "minimum subarray sum" version of Kadane's algorithm.
     *
     * Step-by-step idea:
     * 1. We are not allowed to use index 0 or index n - 1.
     * 2. So we only process indices 1 through n - 2.
     * 3. Let currentMinEndingHere represent the minimum sum of a valid subarray
     *    that must end at the current index.
     * 4. At each new interior element x, we have two choices:
     *      - start a new subarray at x
     *      - extend the previous minimum-ending subarray by adding x
     *    Therefore:
     *      currentMinEndingHere = min(x, previousCurrentMinEndingHere + x)
     * 5. Track the best (smallest) value seen overall.
     *
     * Because n >= 3, there is always at least one interior element.
     *
     * @param transactions the input transaction array
     * @return the minimum contiguous subarray sum among all valid interior blocks
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long minimumInteriorSubarraySum(int[] transactions) {
        int n = transactions.length;

        // The first valid interior index is 1.
        // We initialize both variables with that value so the algorithm starts
        // from a real valid subarray of length 1.
        long currentMinEndingHere = transactions[1];
        long bestMinSoFar = transactions[1];

        // Process the rest of the interior indices: 2 through n - 2.
        for (int i = 2; i <= n - 2; i++) {
            long value = transactions[i];

            // Very important Kadane-style transition:
            //
            // Option A: start a brand new subarray at index i
            //           sum = value
            //
            // Option B: extend the previous minimum-sum subarray that ended at i - 1
            //           sum = currentMinEndingHere + value
            //
            // Since we want the minimum possible sum, we choose the smaller one.
            currentMinEndingHere = Math.min(value, currentMinEndingHere + value);

            // Update the global best minimum if the current ending subarray is even smaller.
            bestMinSoFar = Math.min(bestMinSoFar, currentMinEndingHere);
        }

        return bestMinSoFar;
    }

    /**
     * Runs a demonstration of the solution on sample inputs and a few extra checks.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the fixed demo cases shown here
     * Space complexity: O(1)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] transactions1 = {4, -7, 3, -2, 5};
        int baseGain1 = 10;
        long result1 = solution.maximumNetGain(transactions1, baseGain1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 27");

        int[] transactions2 = {8, 2, 6};
        int baseGain2 = -5;
        long result2 = solution.maximumNetGain(transactions2, baseGain2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 7");

        int[] extra1 = {5, -1, -2, 10};
        int baseExtra1 = 0;
        long resultExtra1 = solution.maximumNetGain(extra1, baseExtra1);
        System.out.println("Extra test 1 result: " + resultExtra1);

        int[] extra2 = {-3, 4, -5, 2, -1, 7};
        int baseExtra2 = 6;
        long resultExtra2 = solution.maximumNetGain(extra2, baseExtra2);
        System.out.println("Extra test 2 result: " + resultExtra2);
    }
}