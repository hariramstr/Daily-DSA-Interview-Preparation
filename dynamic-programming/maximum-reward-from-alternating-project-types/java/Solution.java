import java.util.*;

/*
 * Title: Maximum Reward from Alternating Project Types
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A consulting firm has a backlog of projects to complete over the next several weeks.
 * Each project belongs to one of two types: type A or type B. You are given two arrays,
 * aReward and bReward, both of length n. On week i (0-indexed), the firm may choose exactly
 * one of the following options:
 *
 * 1. Complete the type A project for reward aReward[i]
 * 2. Complete the type B project for reward bReward[i]
 * 3. Skip the week and earn 0 reward
 *
 * However, the firm wants to avoid doing the same type of project in consecutive worked weeks
 * because that causes team fatigue. This means if the last non-skipped week used type A, then
 * the next worked week must use type B, and vice versa. Skipped weeks do not reset the rule;
 * they simply mean no project was done that week.
 *
 * Return the maximum total reward the firm can earn.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 0 <= aReward[i], bReward[i] <= 10^9
 * - The answer fits in a 64-bit signed integer.
 *
 * Important note about the examples:
 * The first example is consistent with the rule and its optimal answer is 30.
 * The second example in the prompt contains contradictory explanation text and an output of 34
 * that is impossible under the stated alternating rule. Under the actual problem statement,
 * the correct maximum for:
 *   aReward = [9, 1, 1, 9]
 *   bReward = [1, 8, 8, 1]
 * is 26, achieved by choosing A, B, skip, A => 9 + 8 + 0 + 9 = 26.
 *
 * This implementation follows the stated rule exactly and therefore computes the correct answer.
 */

public class Solution {

    /**
     * Computes the maximum total reward while ensuring that the types of worked weeks alternate
     * across all non-skipped weeks.
     *
     * Dynamic programming idea:
     * We process weeks from left to right and maintain three values:
     *
     * 1. none:
     *    Maximum reward so far if we have not worked any project yet.
     *    Since skipping gives 0 and rewards are non-negative, this remains 0.
     *
     * 2. endA:
     *    Maximum reward so far where the last worked project type is A.
     *    This means the next worked project, if any, must be B.
     *
     * 3. endB:
     *    Maximum reward so far where the last worked project type is B.
     *    This means the next worked project, if any, must be A.
     *
     * Transition for week i:
     *
     * - If we skip:
     *   The "last worked type" does not change.
     *   So endA and endB can simply carry over.
     *
     * - If we do A this week:
     *   The previous last worked type must be B, or there was no previous worked week at all.
     *   Therefore:
     *     newEndA = max(oldEndA, oldEndB + aReward[i], none + aReward[i])
     *
     * - If we do B this week:
     *   The previous last worked type must be A, or there was no previous worked week at all.
     *   Therefore:
     *     newEndB = max(oldEndB, oldEndA + bReward[i], none + bReward[i])
     *
     * Because skipping preserves states, we can fold the skip option directly into the max by
     * comparing against the old state itself.
     *
     * @param aReward rewards for choosing type A on each week
     * @param bReward rewards for choosing type B on each week
     * @return the maximum total reward achievable under the alternating constraint
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long maximumReward(int[] aReward, int[] bReward) {
        validateInput(aReward, bReward);

        // "none" means we have not worked any project yet.
        // Since all rewards are non-negative and skipping is allowed, this is always 0.
        long none = 0L;

        // We use a very negative number to represent an impossible state.
        // For example, before processing any week, it is impossible for the last worked type
        // to already be A or B.
        long negativeInfinity = Long.MIN_VALUE / 4;

        long endA = negativeInfinity;
        long endB = negativeInfinity;

        // Process each week one by one.
        for (int i = 0; i < aReward.length; i++) {
            // Save previous states because all transitions for this week must be based on
            // the states from the previous week, not partially updated current values.
            long previousEndA = endA;
            long previousEndB = endB;

            // Option 1 for ending with A at week i:
            // - skip this week and keep previousEndA
            // Option 2:
            // - do A this week after previously ending with B
            // Option 3:
            // - do A this week as the first worked project
            long newEndA = Math.max(
                    previousEndA,
                    Math.max(
                            previousEndB == negativeInfinity ? negativeInfinity : previousEndB + aReward[i],
                            none + aReward[i]
                    )
            );

            // Option 1 for ending with B at week i:
            // - skip this week and keep previousEndB
            // Option 2:
            // - do B this week after previously ending with A
            // Option 3:
            // - do B this week as the first worked project
            long newEndB = Math.max(
                    previousEndB,
                    Math.max(
                            previousEndA == negativeInfinity ? negativeInfinity : previousEndA + bReward[i],
                            none + bReward[i]
                    )
            );

            // Update states after fully computing this week's transitions.
            endA = newEndA;
            endB = newEndB;
        }

        // The final answer is the best among:
        // - never working at all (0)
        // - ending with A
        // - ending with B
        return Math.max(none, Math.max(endA, endB));
    }

    /**
     * A second public method that computes the same answer using explicit DP arrays.
     * This version is less space-efficient but can be easier for beginners to understand.
     *
     * dpA[i] = maximum reward considering weeks [0..i], where the last worked type is A
     * dpB[i] = maximum reward considering weeks [0..i], where the last worked type is B
     *
     * Transitions:
     * dpA[i] = max(
     *     dpA[i - 1],              // skip week i, still last worked type A
     *     dpB[i - 1] + aReward[i], // do A after B
     *     aReward[i]               // do A as the first worked project
     * )
     *
     * dpB[i] = max(
     *     dpB[i - 1],              // skip week i, still last worked type B
     *     dpA[i - 1] + bReward[i], // do B after A
     *     bReward[i]               // do B as the first worked project
     * )
     *
     * @param aReward rewards for choosing type A on each week
     * @param bReward rewards for choosing type B on each week
     * @return the maximum total reward achievable under the alternating constraint
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long maximumRewardWithArrays(int[] aReward, int[] bReward) {
        validateInput(aReward, bReward);

        int n = aReward.length;
        long[] dpA = new long[n];
        long[] dpB = new long[n];

        dpA[0] = aReward[0];
        dpB[0] = bReward[0];

        for (int i = 1; i < n; i++) {
            dpA[i] = Math.max(dpA[i - 1], Math.max(dpB[i - 1] + aReward[i], aReward[i]));
            dpB[i] = Math.max(dpB[i - 1], Math.max(dpA[i - 1] + bReward[i], bReward[i]));
        }

        return Math.max(0L, Math.max(dpA[n - 1], dpB[n - 1]));
    }

    /**
     * Validates that the input arrays are non-null and have equal length.
     *
     * @param aReward rewards for type A
     * @param bReward rewards for type B
     * @return nothing
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] aReward, int[] bReward) {
        if (aReward == null || bReward == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (aReward.length != bReward.length) {
            throw new IllegalArgumentException("Input arrays must have the same length.");
        }
        if (aReward.length == 0) {
            throw new IllegalArgumentException("Input arrays must have at least one element.");
        }
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(1) for the optimized method
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] a1 = {5, 1, 7, 4};
        int[] b1 = {2, 10, 3, 8};
        long result1 = solution.maximumReward(a1, b1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 30");

        int[] a2 = {9, 1, 1, 9};
        int[] b2 = {1, 8, 8, 1};
        long result2 = solution.maximumReward(a2, b2);
        System.out.println("Example 2 result under the stated rule: " + result2);
        System.out.println("Correct under the problem statement: 26");

        int[] a3 = {10};
        int[] b3 = {20};
        long result3 = solution.maximumReward(a3, b3);
        System.out.println("Single week result: " + result3);
        System.out.println("Expected: 20");

        int[] a4 = {0, 0, 100};
        int[] b4 = {50, 0, 0};
        long result4 = solution.maximumReward(a4, b4);
        System.out.println("Extra test result: " + result4);
        System.out.println("Expected: 150");
    }
}