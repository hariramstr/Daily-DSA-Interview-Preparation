/*
Minimum Cost to Reconfigure a Data Center Rack Row

Problem Description:
A data center has a row of n server racks, numbered from 0 to n - 1. Each rack must end up assigned to exactly one power profile:
profile A, profile B, or profile C. Reconfiguring rack i to profile p has a given cost cost[i][p]. However, safety rules also
impose structure on the final row.

The row must be partitioned into exactly k contiguous zones. Inside each zone, all racks must use the same power profile.
Adjacent zones must use different profiles. In addition, the length of every zone must be between minLen and maxLen inclusive.

Your task is to compute the minimum total reconfiguration cost to assign profiles to all racks while satisfying all rules.
If no valid partition exists, return -1.

Formally, choose exactly k segments that cover the entire array without overlap, where each segment is contiguous, each segment
length is in [minLen, maxLen], every segment is assigned one of the 3 profiles, all racks in a segment share that profile,
and neighboring segments have different profiles. The cost of a segment from l to r using profile p is the sum of cost[i][p]
for l <= i <= r.

Return the minimum possible total cost.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(k * n * 3)
    More precisely, for each of the k segment counts, we scan all n ending positions and all 3 colors,
    while maintaining sliding-window minima for transitions from the previous segment count.

    Space Complexity:
    O(n * 3)
    We only keep DP for the previous segment count and the current segment count, plus prefix sums.
    */
    public long MinimumCost(int n, int[][] cost, int k, int minLen, int maxLen)
    {
        const long INF = long.MaxValue / 4;

        // ------------------------------------------------------------
        // Quick feasibility checks.
        // ------------------------------------------------------------
        // If each of the k segments must have length at least minLen,
        // then the total covered length is at least k * minLen.
        // If that already exceeds n, no solution exists.
        //
        // Similarly, if each segment can have length at most maxLen,
        // then the total covered length is at most k * maxLen.
        // If that is still less than n, we also cannot cover all racks.
        // ------------------------------------------------------------
        if ((long)k * minLen > n || (long)k * maxLen < n)
            return -1;

        // ------------------------------------------------------------
        // Prefix sums for each of the 3 profiles.
        //
        // prefix[p, i] = total cost of assigning racks [0 .. i-1] to profile p.
        //
        // This lets us compute the cost of any segment [l .. r] with profile p in O(1):
        // segmentCost = prefix[p, r+1] - prefix[p, l]
        //
        // Since our DP will repeatedly ask for segment costs, this preprocessing
        // is essential for efficiency.
        // ------------------------------------------------------------
        long[,] prefix = new long[3, n + 1];
        for (int i = 0; i < n; i++)
        {
            for (int p = 0; p < 3; p++)
            {
                prefix[p, i + 1] = prefix[p, i] + cost[i][p];
            }
        }

        // ------------------------------------------------------------
        // DP definition:
        //
        // prev[i, p] = minimum cost to cover exactly the first i racks
        //              using exactly (segmentsUsed - 1) segments,
        //              where the last used segment has profile p.
        //
        // curr[i, p] = same idea, but using exactly segmentsUsed segments.
        //
        // Important note:
        // i is a prefix length, so i ranges from 0 to n.
        // "cover first i racks" means racks [0 .. i-1].
        //
        // For transitions:
        // Suppose the last segment has profile p and ends at position i-1.
        // Let that last segment start at position s.
        // Then its length is i - s, which must be in [minLen, maxLen].
        //
        // The cost is:
        //   best previous cost covering first s racks with previous profile q != p
        //   + cost of assigning racks [s .. i-1] to profile p
        //
        // So:
        //   curr[i, p] = min over valid s:
        //       (min over q != p of prev[s, q]) + (prefix[p, i] - prefix[p, s])
        //
        // Rearranged:
        //   curr[i, p] = prefix[p, i] + min over valid s:
        //       (min over q != p of prev[s, q] - prefix[p, s])
        //
        // This rearrangement is the key optimization:
        // for each p, as i increases, the valid s values form a sliding window.
        // We maintain the minimum of:
        //       min(prev[s, other1], prev[s, other2]) - prefix[p, s]
        // over that window using a monotonic deque.
        // ------------------------------------------------------------
        long[,] prev = new long[n + 1, 3];
        long[,] curr = new long[n + 1, 3];

        // Initialize all states to INF.
        for (int i = 0; i <= n; i++)
        {
            for (int p = 0; p < 3; p++)
            {
                prev[i, p] = INF;
                curr[i, p] = INF;
            }
        }

        // ------------------------------------------------------------
        // Base case:
        // With 0 segments, we can cover exactly 0 racks with cost 0.
        //
        // There is no "last profile" in a real sense, but to make the first
        // segment transition easy, we set prev[0, p] = 0 for all p.
        //
        // Why is this safe?
        // Because when building the first segment of profile p, we will look at
        // min(prev[0, q]) for q != p, which is still 0. That correctly means
        // "no previous segment exists, so there is no adjacency conflict."
        // ------------------------------------------------------------
        prev[0, 0] = 0;
        prev[0, 1] = 0;
        prev[0, 2] = 0;

        // ------------------------------------------------------------
        // We will process segment counts from 1 to k.
        // For each segment count, we compute curr from prev.
        // ------------------------------------------------------------
        for (int segmentsUsed = 1; segmentsUsed <= k; segmentsUsed++)
        {
            // Reset current layer to INF before filling it.
            for (int i = 0; i <= n; i++)
            {
                for (int p = 0; p < 3; p++)
                {
                    curr[i, p] = INF;
                }
            }

            // --------------------------------------------------------
            // For each possible profile p of the LAST segment,
            // we compute curr[i, p] for all i.
            //
            // We use a monotonic deque over candidate start positions s.
            //
            // For fixed p, define:
            //   candidateValue(s, p) = min(prev[s, q] for q != p) - prefix[p, s]
            //
            // Then:
            //   curr[i, p] = prefix[p, i] + min candidateValue(s, p)
            // over s in [i - maxLen, i - minLen].
            //
            // As i increases by 1, this valid range slides by 1.
            // So we can maintain the minimum candidateValue in that range
            // with a deque in amortized O(1) per position.
            // --------------------------------------------------------
            for (int p = 0; p < 3; p++)
            {
                int other1 = (p + 1) % 3;
                int other2 = (p + 2) % 3;

                // Simple array-based deque for speed.
                int[] dequeIndex = new int[n + 1];
                long[] dequeValue = new long[n + 1];
                int head = 0;
                int tail = 0; // deque is [head, tail)

                // ----------------------------------------------------
                // Scan ending prefix length i from 0 to n.
                // We only meaningfully compute states for i >= 1,
                // but scanning from 0 keeps the window logic uniform.
                // ----------------------------------------------------
                for (int i = 0; i <= n; i++)
                {
                    // ------------------------------------------------
                    // Step 1: Add the new start position s = i - minLen
                    // because from now on, a segment ending at i or later
                    // may start there and still have length at least minLen.
                    //
                    // This candidate becomes eligible exactly when i reaches s + minLen.
                    // ------------------------------------------------
                    int addS = i - minLen;
                    if (addS >= 0)
                    {
                        long bestPrevDifferent = Math.Min(prev[addS, other1], prev[addS, other2]);

                        if (bestPrevDifferent < INF)
                        {
                            long value = bestPrevDifferent - prefix[p, addS];

                            // ----------------------------------------
                            // Maintain deque in increasing order of value.
                            // The front always stores the minimum value.
                            //
                            // If the new value is <= values at the back,
                            // those back entries can never be optimal again,
                            // so we remove them.
                            // ----------------------------------------
                            while (head < tail && dequeValue[tail - 1] >= value)
                            {
                                tail--;
                            }

                            dequeIndex[tail] = addS;
                            dequeValue[tail] = value;
                            tail++;
                        }
                    }

                    // ------------------------------------------------
                    // Step 2: Remove start positions that are too old.
                    // For a segment ending at i, valid starts satisfy:
                    //   i - maxLen <= s <= i - minLen
                    //
                    // So any s < i - maxLen is no longer allowed.
                    // ------------------------------------------------
                    int minAllowedS = i - maxLen;
                    while (head < tail && dequeIndex[head] < minAllowedS)
                    {
                        head++;
                    }

                    // ------------------------------------------------
                    // Step 3: If the deque is non-empty, its front gives
                    // the minimum candidate value over all valid starts.
                    //
                    // Then:
                    //   curr[i, p] = prefix[p, i] + dequeMin
                    //
                    // This means:
                    //   best previous valid partition ending at start s
                    //   + cost of painting [s .. i-1] with profile p
                    // ------------------------------------------------
                    if (head < tail)
                    {
                        curr[i, p] = prefix[p, i] + dequeValue[head];
                    }
                }
            }

            // Move current layer into previous layer for the next iteration.
            var temp = prev;
            prev = curr;
            curr = temp;
        }

        // ------------------------------------------------------------
        // After exactly k segments, we must have covered exactly n racks.
        // The last segment can end with any of the 3 profiles.
        // So the answer is the minimum among prev[n, 0], prev[n, 1], prev[n, 2].
        // ------------------------------------------------------------
        long answer = Math.Min(prev[n, 0], Math.Min(prev[n, 1], prev[n, 2]));
        return answer >= INF ? -1 : answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------
var solution = new Solution();

// Example 1
int n1 = 6;
int[][] cost1 =
{
    new[] { 1, 5, 3 },
    new[] { 2, 4, 6 },
    new[] { 7, 1, 2 },
    new[] { 3, 8, 4 },
    new[] { 6, 2, 5 },
    new[] { 4, 3, 1 }
};
int k1 = 3;
int minLen1 = 2;
int maxLen1 = 2;
Console.WriteLine(solution.MinimumCost(n1, cost1, k1, minLen1, maxLen1));

// Example 2
int n2 = 5;
int[][] cost2 =
{
    new[] { 3, 1, 9 },
    new[] { 2, 5, 4 },
    new[] { 8, 2, 3 },
    new[] { 6, 1, 7 },
    new[] { 4, 3, 2 }
};
int k2 = 2;
int minLen2 = 3;
int maxLen2 = 3;
Console.WriteLine(solution.MinimumCost(n2, cost2, k2, minLen2, maxLen2));