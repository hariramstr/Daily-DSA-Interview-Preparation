import java.util.*;

/*
Problem Title: Maximum Satisfaction from Alternating Workshop Tracks

Problem Description:
A conference offers a sequence of workshops over N time slots. For each slot i, you may attend either
the Engineering track or the Design track. If you attend Engineering in slot i, you gain
engineering[i] satisfaction points. If you attend Design, you gain design[i] satisfaction points.

However, the organizers want attendees to avoid staying in the same track for too long. You are given
an integer K, and you may not attend more than K consecutive workshops from the same track. In other
words, any valid schedule must ensure that every maximal run of Engineering choices has length at most K,
and every maximal run of Design choices also has length at most K.

Your task is to compute the maximum total satisfaction possible across all N slots.

This is a dynamic programming problem because the best choice at each time slot depends not only on the
current slot's values, but also on how many consecutive times you have already chosen the current track.

Constraints:
- 1 <= N <= 100000
- 1 <= K <= N
- engineering.length == design.length == N
- 0 <= engineering[i], design[i] <= 10000
- It is guaranteed that at least one valid schedule exists.

Example 1:
Input:
engineering = [8, 3, 5, 7]
design = [4, 6, 2, 9]
K = 2
Output:
28

Explanation:
One optimal schedule is Engineering, Design, Engineering, Design.
Total satisfaction = 8 + 6 + 5 + 9 = 28.
Choosing Engineering for the first three slots would violate K = 2.

Example 2:
Input:
engineering = [10, 10, 1, 10, 10]
design = [1, 1, 20, 1, 1]
K = 2
Output:
60

Explanation:
An optimal schedule is Engineering, Engineering, Design, Engineering, Engineering.
This respects the limit of at most 2 consecutive workshops from the same track.
Total satisfaction = 10 + 10 + 20 + 10 + 10 = 60.

Goal:
Return the maximum total satisfaction as an integer.
*/

public class Solution {

    /**
     * Computes the maximum total satisfaction while ensuring that no track is chosen
     * more than K times consecutively.
     *
     * Core dynamic programming idea:
     * We process the workshop slots from left to right.
     *
     * Let:
     * - dpE[len] = best total satisfaction after processing the current slot,
     *              where the current slot is chosen as Engineering and the current
     *              consecutive Engineering run has exact length len.
     * - dpD[len] = best total satisfaction after processing the current slot,
     *              where the current slot is chosen as Design and the current
     *              consecutive Design run has exact length len.
     *
     * Transition rules for slot i:
     * 1) If we choose Engineering at slot i:
     *    - We may extend a previous Engineering run of length len-1 into len.
     *    - Or we may switch from any Design state into Engineering with run length 1.
     *
     * 2) If we choose Design at slot i:
     *    - We may extend a previous Design run of length len-1 into len.
     *    - Or we may switch from any Engineering state into Design with run length 1.
     *
     * To make switching efficient, we maintain:
     * - bestPrevE = maximum over all previous dpE[len]
     * - bestPrevD = maximum over all previous dpD[len]
     *
     * This reduces the transition time per slot from O(K^2) to O(K).
     *
     * Because N can be as large as 100000, this O(N * K) approach is acceptable
     * for moderate K and is the standard beginner-friendly DP formulation for this problem.
     *
     * @param engineering satisfaction gained by choosing Engineering at each slot
     * @param design satisfaction gained by choosing Design at each slot
     * @param K maximum allowed number of consecutive choices of the same track
     * @return the maximum total satisfaction over all valid schedules
     * Time complexity: O(N * K)
     * Space complexity: O(K)
     */
    public int maxSatisfaction(int[] engineering, int[] design, int K) {
        validateInput(engineering, design, K);

        int n = engineering.length;

        // We use a very negative number to represent an impossible DP state.
        // Long is used internally for safety during transitions, even though
        // the final answer is guaranteed to fit in int per the problem statement.
        final long NEG = Long.MIN_VALUE / 4;

        // dpE[len] means:
        // after processing the previous slot,
        // the best total if we ended with Engineering
        // and the current consecutive Engineering streak length is exactly len.
        long[] dpE = new long[K + 1];

        // dpD[len] means:
        // after processing the previous slot,
        // the best total if we ended with Design
        // and the current consecutive Design streak length is exactly len.
        long[] dpD = new long[K + 1];

        // Initially, before processing any slot, no state is valid.
        Arrays.fill(dpE, NEG);
        Arrays.fill(dpD, NEG);

        // Base case for slot 0:
        // We can start with either Engineering or Design, each with streak length 1.
        dpE[1] = engineering[0];
        dpD[1] = design[0];

        // Process each remaining slot one by one.
        for (int i = 1; i < n; i++) {
            // These arrays will store the DP values for the current slot i.
            long[] nextE = new long[K + 1];
            long[] nextD = new long[K + 1];
            Arrays.fill(nextE, NEG);
            Arrays.fill(nextD, NEG);

            // Compute the best previous total among all Engineering-ending states.
            // This is needed when we switch from Engineering to Design.
            long bestPrevE = NEG;
            for (int len = 1; len <= K; len++) {
                bestPrevE = Math.max(bestPrevE, dpE[len]);
            }

            // Compute the best previous total among all Design-ending states.
            // This is needed when we switch from Design to Engineering.
            long bestPrevD = NEG;
            for (int len = 1; len <= K; len++) {
                bestPrevD = Math.max(bestPrevD, dpD[len]);
            }

            // -----------------------------
            // Build states ending in Engineering at slot i
            // -----------------------------

            // Case 1: switch from Design to Engineering.
            // If the previous slot ended in Design (with any valid streak length),
            // then choosing Engineering now starts a new Engineering streak of length 1.
            if (bestPrevD != NEG) {
                nextE[1] = bestPrevD + engineering[i];
            }

            // Case 2: extend an existing Engineering streak.
            // If previous streak length was len - 1, then current becomes len.
            // This is only allowed up to K.
            for (int len = 2; len <= K; len++) {
                if (dpE[len - 1] != NEG) {
                    nextE[len] = dpE[len - 1] + engineering[i];
                }
            }

            // -----------------------------
            // Build states ending in Design at slot i
            // -----------------------------

            // Case 1: switch from Engineering to Design.
            // This starts a new Design streak of length 1.
            if (bestPrevE != NEG) {
                nextD[1] = bestPrevE + design[i];
            }

            // Case 2: extend an existing Design streak.
            for (int len = 2; len <= K; len++) {
                if (dpD[len - 1] != NEG) {
                    nextD[len] = dpD[len - 1] + design[i];
                }
            }

            // Move current results into dp arrays for the next iteration.
            dpE = nextE;
            dpD = nextD;
        }

        // The answer is the best value among all valid ending states,
        // whether we end in Engineering or Design, and for any streak length 1..K.
        long answer = 0;
        for (int len = 1; len <= K; len++) {
            answer = Math.max(answer, dpE[len]);
            answer = Math.max(answer, dpD[len]);
        }

        return (int) answer;
    }

    /**
     * Validates the input according to the problem constraints.
     *
     * @param engineering satisfaction values for Engineering track
     * @param design satisfaction values for Design track
     * @param K maximum allowed consecutive workshops in the same track
     * @return nothing
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public void validateInput(int[] engineering, int[] design, int K) {
        if (engineering == null || design == null) {
            throw new IllegalArgumentException("Input arrays must not be null.");
        }
        if (engineering.length != design.length) {
            throw new IllegalArgumentException("Engineering and design arrays must have the same length.");
        }
        if (engineering.length == 0) {
            throw new IllegalArgumentException("Input arrays must not be empty.");
        }
        if (K <= 0 || K > engineering.length) {
            throw new IllegalArgumentException("K must satisfy 1 <= K <= N.");
        }
    }

    /**
     * Runs the sample test cases from the problem statement and prints the results.
     *
     * Verified manually:
     * Example 1:
     * engineering = [8, 3, 5, 7]
     * design      = [4, 6, 2, 9]
     * K = 2
     * Best schedule: E, D, E, D => 8 + 6 + 5 + 9 = 28
     *
     * Example 2:
     * engineering = [10, 10, 1, 10, 10]
     * design      = [1, 1, 20, 1, 1]
     * K = 2
     * Best schedule: E, E, D, E, E => 10 + 10 + 20 + 10 + 10 = 60
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(N * K) across the demonstrated examples
     * Space complexity: O(K)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] engineering1 = {8, 3, 5, 7};
        int[] design1 = {4, 6, 2, 9};
        int k1 = 2;
        int result1 = solution.maxSatisfaction(engineering1, design1, k1);
        System.out.println("Example 1 Output: " + result1);

        int[] engineering2 = {10, 10, 1, 10, 10};
        int[] design2 = {1, 1, 20, 1, 1};
        int k2 = 2;
        int result2 = solution.maxSatisfaction(engineering2, design2, k2);
        System.out.println("Example 2 Output: " + result2);

        int[] engineering3 = {5};
        int[] design3 = {9};
        int k3 = 1;
        int result3 = solution.maxSatisfaction(engineering3, design3, k3);
        System.out.println("Single Slot Example Output: " + result3);
    }
}