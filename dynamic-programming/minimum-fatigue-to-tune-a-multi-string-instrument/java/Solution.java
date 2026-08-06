import java.util.*;

/*
Problem Title: Minimum Fatigue to Tune a Multi-String Instrument

Problem Description:
A musician is preparing an electronic instrument with n strings. For each string i, the desired final pitch is target[i].
You are given a list of m tuning operations. The j-th operation is described by four integers [l_j, r_j, delta_j, cost_j],
meaning you may apply this operation at most once, and if you do, every string in the inclusive range l_j..r_j has its
pitch increased by exactly delta_j, while you pay fatigue cost cost_j. Operations can be applied in any order, and multiple
operations may affect the same string. Initially, all string pitches are 0.

Your task is to compute the minimum total fatigue required to make every string end at exactly its target pitch. If it is
impossible, return -1.

This is not a local optimization problem: an operation that helps one string may overshoot another, so the best answer may
require carefully coordinating overlapping interval updates. The ranges are 0-indexed.

Constraints:
- 1 <= n <= 8
- 1 <= m <= 60
- 0 <= target[i] <= 40
- 0 <= l_j <= r_j < n
- 1 <= delta_j <= 20
- 1 <= cost_j <= 10^4
- Each operation may be used at most once.

Example 1:
Input: target = [3, 3], operations = [[0,0,3,4],[1,1,3,5],[0,1,3,6]]
Output: 6

Example 2:
Input: target = [2, 1, 2], operations = [[0,1,1,3],[1,2,1,4],[0,2,2,10]]
Output: -1
*/

public class Solution {

    /**
     * Computes the minimum total fatigue needed to reach the exact target pitch vector.
     *
     * Core idea:
     * We perform dynamic programming over compact encoded states.
     * A state represents the current pitch of every string.
     *
     * Since:
     * - n <= 8
     * - target[i] <= 40
     * the total number of reachable valid states is manageable when we prune any state
     * that exceeds target on any coordinate.
     *
     * We process operations one by one, exactly like a 0/1 knapsack:
     * for each existing state, we may:
     * 1) skip the current operation
     * 2) apply it once, if doing so does not exceed target on any affected string
     *
     * We store the minimum cost for each reachable encoded state.
     *
     * @param target the desired final pitch for each string
     * @param operations each operation is [l, r, delta, cost]
     * @return the minimum total fatigue, or -1 if it is impossible
     * Time complexity: O(m * S * n), where S is the number of reachable valid states
     * Space complexity: O(S)
     */
    public int minimumFatigue(int[] target, int[][] operations) {
        int n = target.length;

        // For compact state encoding, we use mixed radix.
        // If target[i] = T, then string i can only legally be in range [0..T].
        // So the radix/base for dimension i is (T + 1).
        //
        // Example:
        // target = [3, 3]
        // base = [1, 4]
        // state [a, b] is encoded as a*1 + b*4
        //
        // This gives a unique integer for every valid pitch vector.
        int[] base = buildBase(target);

        // DP map:
        // key   = encoded state
        // value = minimum cost to reach that state after considering some prefix of operations
        //
        // Start from all-zero pitches with cost 0.
        Map<Integer, Integer> dp = new HashMap<>();
        dp.put(0, 0);

        // Process each operation once.
        for (int[] op : operations) {
            int l = op[0];
            int r = op[1];
            int delta = op[2];
            int cost = op[3];

            // next starts as a copy of dp, representing the "skip this operation" choice.
            Map<Integer, Integer> next = new HashMap<>(dp);

            // For every currently reachable state, try applying this operation once.
            for (Map.Entry<Integer, Integer> entry : dp.entrySet()) {
                int state = entry.getKey();
                int currentCost = entry.getValue();

                // Decode the current state into the actual pitch vector.
                int[] pitches = decodeState(state, target, base);

                // Try to apply the operation.
                // Because the operation increases every string in [l..r] by delta,
                // we must ensure no affected string exceeds its target.
                boolean valid = true;
                for (int i = l; i <= r; i++) {
                    if (pitches[i] + delta > target[i]) {
                        valid = false;
                        break;
                    }
                }

                // If applying this operation would overshoot any string,
                // this transition is illegal and must be discarded.
                if (!valid) {
                    continue;
                }

                // Build the new state after applying the operation.
                for (int i = l; i <= r; i++) {
                    pitches[i] += delta;
                }

                int newState = encodeState(pitches, base);
                int newCost = currentCost + cost;

                // Keep only the cheapest way to reach newState.
                int old = next.getOrDefault(newState, Integer.MAX_VALUE);
                if (newCost < old) {
                    next.put(newState, newCost);
                }
            }

            // Move to the next layer of DP.
            dp = next;
        }

        // The target state is simply the encoding of the target vector itself.
        int targetState = encodeState(target, base);

        return dp.getOrDefault(targetState, -1);
    }

    /**
     * Builds the mixed-radix base array used for state encoding.
     *
     * If target = [t0, t1, t2], then:
     * base[0] = 1
     * base[1] = (t0 + 1)
     * base[2] = (t0 + 1) * (t1 + 1)
     *
     * Then a vector v is encoded as:
     * v[0] * base[0] + v[1] * base[1] + ...
     *
     * @param target the target pitch array
     * @return the base multipliers for each dimension
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] buildBase(int[] target) {
        int n = target.length;
        int[] base = new int[n];
        base[0] = 1;
        for (int i = 1; i < n; i++) {
            base[i] = base[i - 1] * (target[i - 1] + 1);
        }
        return base;
    }

    /**
     * Encodes a pitch vector into a single integer using mixed radix.
     *
     * @param pitches the current pitch vector
     * @param base the precomputed base multipliers
     * @return the encoded integer state
     * Time complexity: O(n)
     * Space complexity: O(1) excluding output
     */
    public int encodeState(int[] pitches, int[] base) {
        int state = 0;
        for (int i = 0; i < pitches.length; i++) {
            state += pitches[i] * base[i];
        }
        return state;
    }

    /**
     * Decodes an encoded state back into the pitch vector.
     *
     * Because each dimension i ranges from 0 to target[i], we decode using:
     * pitches[i] = (state / base[i]) % (target[i] + 1)
     *
     * @param state the encoded state
     * @param target the target pitch array, used to know each radix size
     * @param base the precomputed base multipliers
     * @return the decoded pitch vector
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int[] decodeState(int state, int[] target, int[] base) {
        int n = target.length;
        int[] pitches = new int[n];
        for (int i = 0; i < n; i++) {
            pitches[i] = (state / base[i]) % (target[i] + 1);
        }
        return pitches;
    }

    /**
     * Demonstrates the solution on the sample test cases from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 6
     * Example 2 -> -1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called algorithm
     * Space complexity: O(1) excluding called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] target1 = {3, 3};
        int[][] operations1 = {
                {0, 0, 3, 4},
                {1, 1, 3, 5},
                {0, 1, 3, 6}
        };
        int result1 = solution.minimumFatigue(target1, operations1);
        System.out.println(result1); // Expected: 6

        int[] target2 = {2, 1, 2};
        int[][] operations2 = {
                {0, 1, 1, 3},
                {1, 2, 1, 4},
                {0, 2, 2, 10}
        };
        int result2 = solution.minimumFatigue(target2, operations2);
        System.out.println(result2); // Expected: -1
    }
}