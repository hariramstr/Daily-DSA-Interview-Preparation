import java.util.*;

/*
 * Title: Minimum Refills to Keep a Garden Strip Green
 * Difficulty: Medium
 * Topic: Arrays
 *
 * Problem Description:
 * A city park has a long straight garden strip divided into n sections. You are given an integer
 * array waterNeed where waterNeed[i] is the amount of water required by section i. A maintenance
 * cart starts with a watering tank of capacity cap units, and it moves from left to right,
 * watering sections in order.
 *
 * Watering section i reduces the current water in the tank by waterNeed[i]. If the cart does not
 * have enough water to fully water the next section, it must return to the refill station,
 * instantly refill the tank back to cap, and then continue. Returning for a refill can happen only
 * before watering a section, never in the middle of one. Your task is to compute the minimum
 * number of refills needed to water all sections in order.
 *
 * However, there is one optimization: exactly once during the entire process, the cart may skip
 * refilling even if the tank is insufficient, but only if the deficit for that section is at most
 * bonus units. In that case, think of it as a temporary emergency reserve that can be used once
 * and only once. After using this reserve, the tank becomes 0 after watering that section, and
 * future watering still follows the normal refill rules.
 *
 * Return the minimum number of refills required.
 *
 * Constraints:
 * - 1 <= n <= 200000
 * - 1 <= waterNeed[i] <= cap <= 1000000000
 * - 0 <= bonus <= 1000000000
 * - The emergency reserve can be used at most once
 *
 * Example 1:
 * Input: waterNeed = [2, 4, 3, 5, 2], cap = 6, bonus = 2
 * Output: 2
 * Explanation:
 * Start with 6.
 * Water 2 -> 4 left.
 * Water 4 -> 0 left.
 * Section 2 needs 3, so refill (1), tank becomes 6, water 3 -> 3 left.
 * Section 3 needs 5, deficit is 2, so use the one-time reserve and finish that section with 0 left.
 * Section 4 needs 2, refill once more (2 total), then water it.
 *
 * Example 2:
 * Input: waterNeed = [5, 1, 4, 4], cap = 5, bonus = 0
 * Output: 2
 * Explanation:
 * With bonus 0, the reserve is only usable when there is no deficit, so it provides no benefit.
 * Water 5 -> 0.
 * Refill before 1 (1), then water 1 and 4 -> 0.
 * Refill before the last 4 (2), then water it.
 */

public class Solution {

    /**
     * Computes the minimum number of refills needed to water all sections in order,
     * when the cart may use a one-time emergency reserve on at most one section.
     *
     * Core idea:
     * We use dynamic programming with only two states while scanning left to right:
     *
     * 1) "reserve not used yet"
     * 2) "reserve already used"
     *
     * For each state, we keep:
     * - the minimum number of refills needed so far
     * - among all ways achieving that minimum, the maximum remaining water in the tank
     *
     * Why keep the maximum remaining water for the same refill count?
     * Because having more water left can only help in future sections and never hurts.
     * So for each refill count, only the best remaining-water value matters.
     *
     * Transition for each section need:
     * - If current water is enough, simply water it.
     * - Otherwise, we may:
     *   a) refill before this section, then water it
     *   b) if reserve not used yet and deficit <= bonus, use reserve here, and remaining water becomes 0
     *
     * We carefully compare candidate states:
     * - fewer refills is always better
     * - if refills tie, larger remaining water is better
     *
     * This yields an O(n) time solution and O(1) extra space.
     *
     * @param waterNeed array where waterNeed[i] is the water required by section i
     * @param cap tank capacity after each refill
     * @param bonus maximum allowed deficit for the one-time emergency reserve
     * @return the minimum number of refills required to water all sections
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public int minimumRefills(int[] waterNeed, int cap, int bonus) {
        // We use long for safety in arithmetic and sentinel handling,
        // even though int would also be enough for the given constraints.
        final long INF = Long.MAX_VALUE / 4;

        // State A: reserve has NOT been used yet.
        // aRefills = minimum refills to reach current position under this state.
        // aWater   = maximum remaining water among all ways with aRefills refills.
        long aRefills = 0;
        long aWater = cap;

        // State B: reserve HAS already been used.
        // Initially impossible, because before processing any section we have not used it.
        long bRefills = INF;
        long bWater = -1;

        for (int need : waterNeed) {
            // Next states after processing this section.
            long nextARefills = INF;
            long nextAWater = -1;

            long nextBRefills = INF;
            long nextBWater = -1;

            // ------------------------------------------------------------
            // Transition from State A (reserve not used yet)
            // ------------------------------------------------------------
            if (aRefills < INF) {
                // Option 1: Water this section without refill, if enough water exists.
                if (aWater >= need) {
                    long candidateRefills = aRefills;
                    long candidateWater = aWater - need;

                    // Still have not used reserve.
                    if (isBetter(candidateRefills, candidateWater, nextARefills, nextAWater)) {
                        nextARefills = candidateRefills;
                        nextAWater = candidateWater;
                    }
                } else {
                    // Not enough water currently.
                    // Option 2: Refill before this section, then water it.
                    long candidateRefills = aRefills + 1;
                    long candidateWater = (long) cap - need;

                    if (isBetter(candidateRefills, candidateWater, nextARefills, nextAWater)) {
                        nextARefills = candidateRefills;
                        nextAWater = candidateWater;
                    }

                    // Option 3: Use the one-time reserve here, if deficit <= bonus.
                    // Deficit = need - currentWater.
                    long deficit = need - aWater;
                    if (deficit <= bonus) {
                        // After using reserve, the problem states remaining water becomes 0.
                        candidateRefills = aRefills;
                        candidateWater = 0;

                        // Now reserve becomes used, so transition into State B.
                        if (isBetter(candidateRefills, candidateWater, nextBRefills, nextBWater)) {
                            nextBRefills = candidateRefills;
                            nextBWater = candidateWater;
                        }
                    }
                }
            }

            // ------------------------------------------------------------
            // Transition from State B (reserve already used)
            // ------------------------------------------------------------
            if (bRefills < INF) {
                // Since reserve is already used, only normal watering/refill rules remain.
                if (bWater >= need) {
                    long candidateRefills = bRefills;
                    long candidateWater = bWater - need;

                    if (isBetter(candidateRefills, candidateWater, nextBRefills, nextBWater)) {
                        nextBRefills = candidateRefills;
                        nextBWater = candidateWater;
                    }
                } else {
                    long candidateRefills = bRefills + 1;
                    long candidateWater = (long) cap - need;

                    if (isBetter(candidateRefills, candidateWater, nextBRefills, nextBWater)) {
                        nextBRefills = candidateRefills;
                        nextBWater = candidateWater;
                    }
                }
            }

            // Move to the next section.
            aRefills = nextARefills;
            aWater = nextAWater;
            bRefills = nextBRefills;
            bWater = nextBWater;
        }

        // Final answer is the better of:
        // - finishing without ever using reserve
        // - finishing after using reserve at some point
        return (int) Math.min(aRefills, bRefills);
    }

    /**
     * Compares two DP states and decides whether the candidate is better.
     *
     * Ordering rule:
     * 1) Fewer refills is better.
     * 2) If refill counts are equal, more remaining water is better.
     *
     * This rule is exactly what we need because:
     * - the objective is to minimize refills
     * - for the same number of refills, having more water left can only improve future options
     *
     * @param candidateRefills refill count of the candidate state
     * @param candidateWater remaining water of the candidate state
     * @param currentRefills refill count of the currently stored best state
     * @param currentWater remaining water of the currently stored best state
     * @return true if the candidate state is better than the current stored state
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public boolean isBetter(long candidateRefills, long candidateWater, long currentRefills, long currentWater) {
        if (candidateRefills != currentRefills) {
            return candidateRefills < currentRefills;
        }
        return candidateWater > currentWater;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected answers so the output can be visually verified.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(n) across the demonstrated examples
     * Space complexity: O(1) extra space excluding input arrays
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] waterNeed1 = {2, 4, 3, 5, 2};
        int cap1 = 6;
        int bonus1 = 2;
        int result1 = solution.minimumRefills(waterNeed1, cap1, bonus1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 2");

        int[] waterNeed2 = {5, 1, 4, 4};
        int cap2 = 5;
        int bonus2 = 0;
        int result2 = solution.minimumRefills(waterNeed2, cap2, bonus2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 2");

        // Additional quick sanity checks.
        int[] waterNeed3 = {3};
        int cap3 = 5;
        int bonus3 = 10;
        int result3 = solution.minimumRefills(waterNeed3, cap3, bonus3);
        System.out.println("Single section result: " + result3);
        System.out.println("Expected: 0");

        int[] waterNeed4 = {4, 4, 4};
        int cap4 = 4;
        int bonus4 = 0;
        int result4 = solution.minimumRefills(waterNeed4, cap4, bonus4);
        System.out.println("Exact capacity sections result: " + result4);
        System.out.println("Expected: 2");
    }
}