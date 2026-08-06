/*
Title: Minimum Fatigue to Tune a Multi-String Instrument

Problem Description:
A musician is preparing an electronic instrument with n strings. For each string i, the desired final pitch is target[i].
You are given a list of m tuning operations. The j-th operation is described by four integers [l_j, r_j, delta_j, cost_j],
meaning you may apply this operation at most once, and if you do, every string in the inclusive range l_j..r_j has its
pitch increased by exactly delta_j, while you pay fatigue cost cost_j. Operations can be applied in any order, and multiple
operations may affect the same string. Initially, all string pitches are 0.

Your task is to compute the minimum total fatigue required to make every string end at exactly its target pitch.
If it is impossible, return -1.

This is not a local optimization problem: an operation that helps one string may overshoot another, so the best answer
may require carefully coordinating overlapping interval updates. The ranges are 0-indexed.

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

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    Let S be the number of reachable valid states (states that never exceed target on any string).
    For each of the m operations, we iterate over all currently reachable states once.
    Therefore the time complexity is O(m * S * n), where n <= 8 is the cost to apply/check one operation.
    In the worst theoretical case S can be up to product(target[i] + 1), but pruning keeps only valid states.

    Space Complexity:
    O(S) for the dynamic programming dictionary that stores the minimum fatigue cost for each reachable state.
    */
    public int MinimumFatigue(int[] target, int[][] operations)
    {
        int n = target.Length;

        // We encode a whole pitch vector into a single 64-bit integer.
        //
        // Why this is useful:
        // - A dictionary needs a compact key.
        // - n is very small (at most 8), and each target[i] is at most 40.
        // - So each coordinate fits comfortably in 6 bits (because 0..63 fits in 6 bits).
        //
        // This lets us store a state like [3, 0, 5, 2] as one long value.
        // Then dictionary lookups are fast and memory-efficient.
        const int BitsPerString = 6;
        const long Mask = (1L << BitsPerString) - 1;

        long Encode(int[] arr)
        {
            long key = 0;
            for (int i = 0; i < arr.Length; i++)
            {
                key |= ((long)arr[i] << (i * BitsPerString));
            }
            return key;
        }

        void Decode(long key, int[] arr)
        {
            for (int i = 0; i < arr.Length; i++)
            {
                arr[i] = (int)((key >> (i * BitsPerString)) & Mask);
            }
        }

        // The target state is the exact pitch vector we want to reach.
        long targetKey = Encode(target);

        // Dynamic programming over subsets of operations, but compressed by state:
        //
        // dp[state] = minimum total fatigue needed to reach this exact pitch vector
        // after considering some prefix of operations.
        //
        // We start at the all-zero vector with cost 0.
        var dp = new Dictionary<long, int>
        {
            [0L] = 0
        };

        // Reusable buffer to avoid allocating a new array every time we decode a state.
        int[] current = new int[n];

        // Process operations one by one.
        //
        // This is the standard "0/1 choice" pattern:
        // for each operation, from every existing state we can:
        // 1) skip it
        // 2) use it once, if doing so does not exceed target on any string
        //
        // Because each operation may be used at most once, we must build a new dictionary
        // from the old one, rather than updating in place in a way that could reuse the same
        // operation multiple times.
        foreach (var op in operations)
        {
            int l = op[0];
            int r = op[1];
            int delta = op[2];
            int cost = op[3];

            // Start next as a copy of current dp.
            //
            // Why copy?
            // - "Skip this operation" means every old state remains reachable with the same cost.
            // - Then we additionally try "take this operation" transitions.
            var next = new Dictionary<long, int>(dp);

            // Iterate through every currently reachable state.
            foreach (var entry in dp)
            {
                long stateKey = entry.Key;
                int currentCost = entry.Value;

                // Decode the compact key back into the pitch vector.
                Decode(stateKey, current);

                // Try applying the current operation.
                //
                // We must check whether any affected string would exceed its target.
                // If even one string overshoots, this transition is invalid and must be discarded.
                bool valid = true;
                for (int i = l; i <= r; i++)
                {
                    if (current[i] + delta > target[i])
                    {
                        valid = false;
                        break;
                    }
                }

                if (!valid)
                {
                    continue;
                }

                // Build the new state after applying the operation.
                //
                // Since n is tiny, the simplest and clearest approach is:
                // - copy current vector
                // - add delta on the interval
                int[] updated = new int[n];
                Array.Copy(current, updated, n);
                for (int i = l; i <= r; i++)
                {
                    updated[i] += delta;
                }

                long newKey = Encode(updated);
                int newCost = currentCost + cost;

                // Relaxation step:
                // if we found a cheaper way to reach the same state, keep the cheaper one.
                if (!next.TryGetValue(newKey, out int existingCost) || newCost < existingCost)
                {
                    next[newKey] = newCost;
                }
            }

            dp = next;
        }

        // After all operations have been considered, the answer is the minimum cost
        // stored for the exact target vector, if it exists.
        return dp.TryGetValue(targetKey, out int answer) ? answer : -1;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] target1 = { 3, 3 };
int[][] operations1 =
{
    new[] { 0, 0, 3, 4 },
    new[] { 1, 1, 3, 5 },
    new[] { 0, 1, 3, 6 }
};
int result1 = solution.MinimumFatigue(target1, operations1);
Console.WriteLine(result1); // Expected: 6

// Example 2
int[] target2 = { 2, 1, 2 };
int[][] operations2 =
{
    new[] { 0, 1, 1, 3 },
    new[] { 1, 2, 1, 4 },
    new[] { 0, 2, 2, 10 }
};
int result2 = solution.MinimumFatigue(target2, operations2);
Console.WriteLine(result2); // Expected: -1