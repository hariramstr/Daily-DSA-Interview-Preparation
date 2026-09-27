import java.util.*;

/*
Problem Title: Maximum Recharge Gain from One Idle Block

Problem Description:
A mobile device records its battery change each minute during a long session. You are given an integer array changes, where changes[i] is the net battery change during minute i. A positive value means the battery increased during that minute, and a negative value means the battery drained.

The device firmware allows exactly one optimization: you may choose one contiguous block of minutes and mark it as an idle block. During the idle block, every battery change in that block is inverted in sign. In other words, each x in the chosen block becomes -x. You must choose exactly one non-empty contiguous block.

Return the maximum possible total battery change after applying this optimization once.

This is not the same as simply taking the maximum subarray sum. Flipping a block changes the final total by subtracting twice the sum of that block, so the best block may contain both positive and negative values. Your task is to determine which single block to flip to maximize the final total battery change.

Constraints:
- 1 <= changes.length <= 200000
- -100000 <= changes[i] <= 100000
- The answer fits in a 64-bit signed integer

Example 1:
Input: changes = [4, -7, 3, -2]
Output: 12
Explanation: The original total is -2. Flipping the block [-7, 3, -2] gives [4, 7, -3, 2], whose total is 10. Flipping only [-7] gives [4, 7, 3, -2], whose total is 12, which is optimal.

Example 2:
Input: changes = [5, 2, 4]
Output: 3
Explanation: The original total is 11. Since you must flip exactly one non-empty block, some gain is lost. Flipping [4] gives [5, 2, -4], total 3. Flipping [5] gives 1, and flipping the whole array gives -11. The best possible result is 3.
*/

public class Solution {

    /**
     * Computes the maximum possible total battery change after flipping exactly one
     * non-empty contiguous block.
     *
     * Core idea:
     * If the original total sum is S, and we flip a block whose sum is B,
     * then every value x in that block becomes -x, so the block contribution changes
     * from B to -B. Therefore the total changes by:
     *
     * newTotal = S - 2 * B
     *
     * To maximize newTotal, we must minimize B.
     * So the problem becomes:
     * 1) Compute the total sum S
     * 2) Find the minimum-sum non-empty contiguous subarray B
     * 3) Return S - 2 * B
     *
     * We can find the minimum-sum subarray in linear time using a Kadane-style scan.
     *
     * @param changes the array of per-minute battery changes
     * @return the maximum possible total battery change after exactly one flip
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maximumRechargeGain(int[] changes) {
        // Step 1:
        // Compute the original total sum of the array.
        // We use long because:
        // - n can be as large as 200,000
        // - each value can be up to 100,000 in magnitude
        // So the total can exceed int range.
        long totalSum = 0L;
        for (int value : changes) {
            totalSum += value;
        }

        // Step 2:
        // Find the minimum-sum non-empty contiguous subarray.
        //
        // This is the "minimum subarray" version of Kadane's algorithm.
        //
        // Let currentMinEndingHere represent:
        // the minimum sum of a non-empty subarray that MUST end at the current index.
        //
        // Transition:
        // For each new value x, the best minimum-sum subarray ending here is either:
        // - start fresh at x
        // - extend the previous minimum-sum subarray by adding x
        //
        // So:
        // currentMinEndingHere = min(x, currentMinEndingHere + x)
        //
        // We also track the best (smallest) value seen globally in minSubarraySum.
        long currentMinEndingHere = changes[0];
        long minSubarraySum = changes[0];

        // Start from index 1 because index 0 already initialized the DP state.
        for (int i = 1; i < changes.length; i++) {
            long x = changes[i];

            // Either begin a new subarray at this element,
            // or extend the previous one.
            currentMinEndingHere = Math.min(x, currentMinEndingHere + x);

            // Update the global minimum subarray sum found so far.
            minSubarraySum = Math.min(minSubarraySum, currentMinEndingHere);
        }

        // Step 3:
        // If we flip the minimum-sum block with sum = minSubarraySum,
        // the final total becomes:
        //
        // totalSum - 2 * minSubarraySum
        //
        // Why this works:
        // Original block contributes: minSubarraySum
        // Flipped block contributes: -minSubarraySum
        // Net improvement: (-minSubarraySum) - (minSubarraySum) = -2 * minSubarraySum
        //
        // If minSubarraySum is negative, this increases the total.
        // If all numbers are positive, minSubarraySum will be the smallest positive element,
        // and because we MUST flip exactly one non-empty block, we lose as little as possible.
        return totalSum - 2L * minSubarraySum;
    }

    /**
     * A helper method that prints a full demonstration for one test case:
     * the input array and the computed answer.
     *
     * @param changes the array of battery changes to test
     * @return the computed maximum total after one required flip
     * Time complexity: O(n)
     * Space complexity: O(1), excluding output formatting
     */
    public long demonstrateCase(int[] changes) {
        long result = maximumRechargeGain(changes);
        System.out.println("changes = " + Arrays.toString(changes));
        System.out.println("maximum total after one flip = " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the algorithm on the sample inputs and a few
     * additional cases.
     *
     * Verified against the problem statement examples:
     * Example 1:
     * changes = [4, -7, 3, -2]
     * total = -2
     * minimum subarray sum = -7
     * answer = -2 - 2 * (-7) = 12
     *
     * Example 2:
     * changes = [5, 2, 4]
     * total = 11
     * minimum subarray sum = 2? No, careful:
     * subarrays include [5]=5, [2]=2, [4]=4, [5,2]=7, [2,4]=6, [5,2,4]=11
     * minimum is 2
     * answer = 11 - 2 * 2 = 7
     *
     * However, the problem statement says output 3 by flipping [4].
     * That statement is inconsistent with the actual optimization rule.
     * Since we must maximize the final total after flipping exactly one block,
     * flipping [2] gives [5, -2, 4] with total 7, which is better than 3.
     *
     * Therefore, the mathematically correct answer for Example 2 under the stated rules is 7.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(k * n) across demonstrated test cases
     * Space complexity: O(1), excluding output formatting
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the prompt.
        // Expected by correct derivation: 12
        solution.demonstrateCase(new int[]{4, -7, 3, -2});

        // Sample 2 from the prompt.
        // Under the stated rules, the correct maximum is 7 by flipping [2].
        // The prompt's stated output 3 is inconsistent with its own definition.
        solution.demonstrateCase(new int[]{5, 2, 4});

        // Additional checks for beginner-friendly understanding.

        // Single negative element: flipping it makes it positive.
        solution.demonstrateCase(new int[]{-8}); // expected 8

        // Single positive element: must flip it, so result becomes negative.
        solution.demonstrateCase(new int[]{6}); // expected -6

        // Mixed values where best block contains more than one element.
        solution.demonstrateCase(new int[]{3, -4, -2, 5}); // total 2, min block -6, answer 14

        // All negative values: flipping the whole array is often best.
        solution.demonstrateCase(new int[]{-1, -2, -3}); // total -6, min block -6, answer 6
    }
}