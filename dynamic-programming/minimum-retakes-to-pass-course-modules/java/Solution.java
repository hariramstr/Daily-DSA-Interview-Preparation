import java.util.*;

/*
 * Title: Minimum Retakes to Pass Course Modules
 * Difficulty: Medium
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given an array modules of length n, where modules[i] is the score you would earn
 * on the i-th course module if you attempt it. You must process the modules from left to right.
 * For each module, you have two choices: keep the score as-is, or spend one retake to improve
 * that module's score by exactly d points. Each module can be retaken at most once.
 *
 * Your goal is to make the final sequence of scores non-decreasing, meaning the score of every
 * module must be at least the score of the previous module after all retake decisions are applied.
 * Return the minimum number of retakes needed to achieve this. If it is impossible, return -1.
 *
 * This models a realistic training platform where scores arrive in a fixed order, and a retake
 * can only boost a module by a fixed amount. You are not allowed to reorder modules, skip modules,
 * or retake a module multiple times.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 0 <= modules[i] <= 1000000000
 * - 0 <= d <= 1000000000
 *
 * Example 1:
 * Input: modules = [4, 2, 5, 5], d = 3
 * Output: 1
 * Explanation: Retake the second module only. The final scores become [4, 5, 5, 5],
 * which is non-decreasing.
 *
 * Example 2:
 * Input: modules = [7, 3, 2], d = 4
 * Output: -1
 * Explanation: The possible values are [7 or 11], [3 or 7], [2 or 6]. No combination
 * produces a non-decreasing sequence from left to right.
 */

public class Solution {

    /**
     * Computes the minimum number of retakes needed so that the final sequence of module scores
     * becomes non-decreasing.
     *
     * Core dynamic programming idea:
     * For each module i, there are only two possible final values:
     * 1) modules[i]       -> if we do not retake it
     * 2) modules[i] + d   -> if we retake it once
     *
     * Therefore, at every position we only need to track two DP states:
     * - dpKeep:   minimum retakes needed up to current index if current module is kept as-is
     * - dpRetake: minimum retakes needed up to current index if current module is retaken
     *
     * Transition rule:
     * A current state is valid only if its chosen final value is at least the previous chosen final value.
     * So we try all 2 previous states -> 2 current states transitions, which is constant work per index.
     *
     * @param modules the original scores for each module, processed from left to right
     * @param d the exact score increase obtained by retaking a module once
     * @return the minimum number of retakes required to make the final sequence non-decreasing;
     *         returns -1 if no valid sequence exists
     *
     * Time complexity: O(n), because each module performs only a constant number of transitions.
     * Space complexity: O(1), because only the previous two DP states are stored.
     */
    public int minimumRetakes(int[] modules, int d) {
        int n = modules.length;

        // A large sentinel value representing "impossible".
        // We use n + 1 because the answer can never exceed n retakes.
        final int INF = n + 1;

        // Base case for the first module:
        // If we keep it, we use 0 retakes.
        int prevKeepCost = 0;
        long prevKeepValue = modules[0];

        // If we retake it, we use 1 retake.
        int prevRetakeCost = 1;
        long prevRetakeValue = (long) modules[0] + d;

        // Process modules from left to right starting from the second module.
        for (int i = 1; i < n; i++) {
            long keepValue = modules[i];
            long retakeValue = (long) modules[i] + d;

            // Initialize current DP states as impossible.
            int currKeepCost = INF;
            int currRetakeCost = INF;

            /*
             * We now compute the best way to end at index i with:
             * 1) current module kept as-is
             * 2) current module retaken
             *
             * For each target state, we check whether it can follow:
             * - previous kept state
             * - previous retaken state
             *
             * The non-decreasing condition is:
             * currentFinalValue >= previousFinalValue
             */

            // ----- Try to end current module in "keep" state -----

            // Transition from previous "keep" state to current "keep" state.
            if (keepValue >= prevKeepValue) {
                currKeepCost = Math.min(currKeepCost, prevKeepCost);
            }

            // Transition from previous "retake" state to current "keep" state.
            if (keepValue >= prevRetakeValue) {
                currKeepCost = Math.min(currKeepCost, prevRetakeCost);
            }

            // ----- Try to end current module in "retake" state -----

            // Transition from previous "keep" state to current "retake" state.
            if (retakeValue >= prevKeepValue) {
                currRetakeCost = Math.min(currRetakeCost, prevKeepCost + 1);
            }

            // Transition from previous "retake" state to current "retake" state.
            if (retakeValue >= prevRetakeValue) {
                currRetakeCost = Math.min(currRetakeCost, prevRetakeCost + 1);
            }

            // Move current states into previous states for the next iteration.
            prevKeepCost = currKeepCost;
            prevKeepValue = keepValue;

            prevRetakeCost = currRetakeCost;
            prevRetakeValue = retakeValue;
        }

        int answer = Math.min(prevKeepCost, prevRetakeCost);
        return answer >= INF ? -1 : answer;
    }

    /**
     * Convenience helper that prints a test case and its computed answer.
     *
     * @param modules the module scores array
     * @param d the fixed increase from one retake
     * @return the computed minimum number of retakes for the provided input
     *
     * Time complexity: O(n), delegated to minimumRetakes.
     * Space complexity: O(1), delegated to minimumRetakes.
     */
    public int runAndPrint(int[] modules, int d) {
        int result = minimumRetakes(modules, d);
        System.out.println("modules = " + Arrays.toString(modules) + ", d = " + d + " -> " + result);
        return result;
    }

    /**
     * Demonstrates the solution on the sample inputs and a few additional cases.
     *
     * Expected sample outputs:
     * - [4, 2, 5, 5], d = 3 -> 1
     * - [7, 3, 2], d = 4 -> -1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total number of elements across demonstrated test cases).
     * Space complexity: O(1) extra space excluding input storage.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the problem statement.
        // Keep 4, retake 2 -> 5, keep 5, keep 5 => [4, 5, 5, 5], so answer is 1.
        solution.runAndPrint(new int[]{4, 2, 5, 5}, 3);

        // Sample 2 from the problem statement.
        // No valid non-decreasing sequence can be formed, so answer is -1.
        solution.runAndPrint(new int[]{7, 3, 2}, 4);

        // Additional beginner-friendly checks.
        solution.runAndPrint(new int[]{1, 2, 3}, 5);   // Already non-decreasing -> 0
        solution.runAndPrint(new int[]{3, 1}, 2);      // Retake second -> [3, 3] -> 1
        solution.runAndPrint(new int[]{5, 4}, 0);      // Cannot improve anything -> -1
        solution.runAndPrint(new int[]{2}, 10);        // Single element is always valid -> 0
    }
}