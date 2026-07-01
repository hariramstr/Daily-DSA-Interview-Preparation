import java.util.*;

/*
 * Title: Maximum Audience Gain from One Schedule Insertion
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * You are given an array viewers where viewers[i] represents the expected audience size
 * of the i-th show in a streaming platform's daily lineup. The platform may insert exactly
 * one promotional show into the schedule. The inserted show has an audience value promo,
 * and it can be placed at any position: before the first show, between any two consecutive
 * shows, or after the last show.
 *
 * After insertion, the platform evaluates the best contiguous block of shows to feature
 * on its homepage. The score of a block is the sum of its audience values. Your task is
 * to return the maximum possible homepage score after inserting the promotional show
 * exactly once.
 *
 * In other words, choose one insertion position for promo, then compute the maximum
 * subarray sum of the new array, and maximize that value over all possible insertion
 * positions.
 *
 * A valid solution should be more efficient than trying every insertion explicitly.
 *
 * Constraints:
 * - 1 <= viewers.length <= 2 * 10^5
 * - -10^4 <= viewers[i] <= 10^4
 * - -10^4 <= promo <= 10^4
 * - The answer fits in a 32-bit signed integer.
 *
 * Example 1:
 * Input: viewers = [4, -2, 3, -1], promo = 5
 * Output: 10
 * Explanation:
 * Insert 5 between 4 and -2, producing [4, 5, -2, 3, -1].
 * The best contiguous block is [4, 5, -2, 3] with sum 10.
 *
 * Example 2:
 * Input: viewers = [-3, -2, -4], promo = 6
 * Output: 6
 * Explanation:
 * All original shows have negative audience values, so the best choice is to insert 6
 * anywhere and feature only that single show. The maximum possible score is 6.
 */

public class Solution {

    /**
     * Computes the maximum possible maximum-subarray sum after inserting the promotional
     * show exactly once at the best position.
     *
     * Core idea:
     * For every possible insertion gap, the best subarray that includes the inserted promo
     * can be described as:
     *
     *     bestSuffixEndingJustBeforeGap + promo + bestPrefixStartingJustAfterGap
     *
     * where:
     * - the left part is optional, so we only take it if it is positive
     * - the right part is optional, so we only take it if it is positive
     *
     * We also compare against the original maximum subarray sum, because after insertion
     * the best subarray might ignore the promo completely.
     *
     * To do this efficiently:
     * 1. Build leftEnd[i] = maximum subarray sum ending exactly at index i
     * 2. Build rightStart[i] = maximum subarray sum starting exactly at index i
     * 3. For each insertion gap:
     *      candidate = max(0, best left suffix) + promo + max(0, best right prefix)
     * 4. Take the maximum over all gaps and the original Kadane result
     *
     * @param viewers the audience values of the existing shows
     * @param promo the audience value of the promotional show to insert exactly once
     * @return the maximum possible homepage score after one insertion
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int maximumAudienceGain(int[] viewers, int promo) {
        int n = viewers.length;

        // leftEnd[i] will store the maximum subarray sum that MUST end at index i.
        // This is the standard "ending here" DP from Kadane's algorithm.
        int[] leftEnd = new int[n];

        // rightStart[i] will store the maximum subarray sum that MUST start at index i.
        // This is the symmetric version computed from right to left.
        int[] rightStart = new int[n];

        // -----------------------------
        // Step 1: Compute leftEnd[]
        // -----------------------------
        // Base case:
        // The best subarray ending at index 0 is simply viewers[0].
        leftEnd[0] = viewers[0];

        // We will also compute the original maximum subarray sum while building leftEnd[].
        int originalBest = leftEnd[0];

        for (int i = 1; i < n; i++) {
            // For a subarray that must end at i, we have exactly two choices:
            //
            // 1. Start fresh at i:
            //      viewers[i]
            //
            // 2. Extend the best subarray that ended at i - 1:
            //      leftEnd[i - 1] + viewers[i]
            //
            // We choose the better of the two.
            leftEnd[i] = Math.max(viewers[i], leftEnd[i - 1] + viewers[i]);

            // Update the best original maximum subarray sum seen so far.
            originalBest = Math.max(originalBest, leftEnd[i]);
        }

        // -----------------------------
        // Step 2: Compute rightStart[]
        // -----------------------------
        // Base case:
        // The best subarray starting at the last index is simply viewers[n - 1].
        rightStart[n - 1] = viewers[n - 1];

        for (int i = n - 2; i >= 0; i--) {
            // For a subarray that must start at i, we again have two choices:
            //
            // 1. Start and stop at i:
            //      viewers[i]
            //
            // 2. Extend into the best subarray that starts at i + 1:
            //      viewers[i] + rightStart[i + 1]
            //
            // We choose the better of the two.
            rightStart[i] = Math.max(viewers[i], viewers[i] + rightStart[i + 1]);
        }

        // -----------------------------
        // Step 3: Try every insertion gap
        // -----------------------------
        // There are n + 1 possible insertion positions:
        // gap 0     : before viewers[0]
        // gap 1     : between viewers[0] and viewers[1]
        // ...
        // gap n - 1 : between viewers[n - 2] and viewers[n - 1]
        // gap n     : after viewers[n - 1]
        //
        // For each gap, the best subarray that includes promo is:
        //
        //   max(0, best suffix ending on the left side)
        // + promo
        // + max(0, best prefix starting on the right side)
        //
        // Why max(0, ...)?
        // Because the left or right side is optional. If it hurts the sum, we skip it.
        int answer = originalBest;

        for (int gap = 0; gap <= n; gap++) {
            int bestLeftContribution = 0;
            int bestRightContribution = 0;

            // If there is at least one element on the left side of the insertion gap,
            // then the best possible contiguous contribution from the left must end
            // exactly at gap - 1.
            if (gap > 0) {
                bestLeftContribution = Math.max(0, leftEnd[gap - 1]);
            }

            // If there is at least one element on the right side of the insertion gap,
            // then the best possible contiguous contribution from the right must start
            // exactly at gap.
            if (gap < n) {
                bestRightContribution = Math.max(0, rightStart[gap]);
            }

            // Candidate score for a best subarray that includes the inserted promo.
            int candidate = bestLeftContribution + promo + bestRightContribution;

            // Update the global answer.
            answer = Math.max(answer, candidate);
        }

        return answer;
    }

    /**
     * A helper method that prints a detailed demonstration for one test case.
     *
     * @param viewers the audience values of the existing shows
     * @param promo the promotional show's audience value
     * @return the computed maximum possible homepage score
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int demonstrateCase(int[] viewers, int promo) {
        System.out.println("Viewers: " + Arrays.toString(viewers));
        System.out.println("Promo: " + promo);
        int result = maximumAudienceGain(viewers, promo);
        System.out.println("Maximum possible homepage score: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method to demonstrate the solution on the sample inputs from the problem
     * statement and print the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(n) per demonstrated test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // viewers = [4, -2, 3, -1], promo = 5
        // Expected output: 10
        int[] viewers1 = {4, -2, 3, -1};
        int promo1 = 5;
        int result1 = solution.demonstrateCase(viewers1, promo1);
        System.out.println("Expected: 10");
        System.out.println("Matches expected: " + (result1 == 10));
        System.out.println();

        // Sample 2:
        // viewers = [-3, -2, -4], promo = 6
        // Expected output: 6
        int[] viewers2 = {-3, -2, -4};
        int promo2 = 6;
        int result2 = solution.demonstrateCase(viewers2, promo2);
        System.out.println("Expected: 6");
        System.out.println("Matches expected: " + (result2 == 6));
        System.out.println();

        // A few extra beginner-friendly checks.
        int[] viewers3 = {1, 2, 3};
        int promo3 = -2;
        solution.demonstrateCase(viewers3, promo3);

        int[] viewers4 = {-5};
        int promo4 = -1;
        solution.demonstrateCase(viewers4, promo4);
    }
}