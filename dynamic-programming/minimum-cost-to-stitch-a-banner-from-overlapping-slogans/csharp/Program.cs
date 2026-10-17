/*
Title: Minimum Cost to Stitch a Banner from Overlapping Slogans
Difficulty: Hard
Topic: Dynamic Programming

Problem Description:
You are given a target banner string `target` that a marketing system must assemble exactly from left to right.
You also receive `n` slogan fragments. The `i`-th fragment is a string `fragments[i]` with an associated usage cost `cost[i]`.
You may use any fragment any number of times.

When placing a fragment, it must match a contiguous substring of `target` starting at some position `p`.
However, unlike simple concatenation, the new fragment is allowed to overlap the already-built banner by up to `k` characters.

More formally, if you have already built the prefix `target[0...p-1]`, then you may place a fragment of length `L`
so that its first `t` characters overlap the suffix `target[p-t...p-1]` for some
`0 <= t <= min(k, L-1, p)`, and its full content still matches `target[p-t...p-t+L-1]`.
After placing it, the built prefix extends to `max(p, p-t+L)`. Each placement adds `cost[i]`.

Return the minimum total cost to build the entire `target`, or `-1` if it is impossible.

This is a weighted string-construction problem where the best solution may require intentionally using overlaps
to reduce total cost. Greedy choices are not sufficient because a cheaper fragment now may block a more efficient overlap later.

Constraints:
- 1 <= target.length <= 5000
- 1 <= n <= 2000
- 1 <= fragments[i].length <= 50
- 0 <= k <= 50
- 1 <= cost[i] <= 10^9
- target and all fragments[i] consist of lowercase English letters.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    Let m = target.Length, n = fragments.Length, and maxLen = maximum fragment length (<= 50).

    1) Preprocessing all fragment matches against every target start position:
       O(n * m * maxLen)

    2) Dynamic programming transitions:
       For each built prefix length p (0..m), we only need to consider overlap sizes t in [0..k],
       which means possible fragment start positions s = p - t are in a window of size at most k + 1.
       For each such start position, we iterate over the fragments that match there.
       In the worst case this is O(m * (k + 1) * n), but because maxLen is small and matching fragments
       are filtered by actual string equality, this is efficient enough for the given constraints.

    With the problem limits (m <= 5000, n <= 2000, maxLen <= 50, k <= 50), this approach is practical.

    Space Complexity:
    O(m * n) in the worst case for storing which fragments match at which target positions,
    plus O(m) for the DP array.
    */
    public long MinimumCost(string target, string[] fragments, int[] cost, int k)
    {
        int m = target.Length;
        int n = fragments.Length;

        // We use a very large value to represent "unreachable".
        // long is necessary because costs can be up to 1e9 and we may use many fragments.
        long inf = long.MaxValue / 4;

        // --------------------------------------------------------------------
        // STEP 1: Precompute which fragments can be placed starting at each index.
        // --------------------------------------------------------------------
        //
        // Why do this?
        // During DP, we will repeatedly ask:
        // "If I want to place a fragment whose actual start in target is s,
        //  which fragments match target[s..] exactly?"
        //
        // Instead of re-checking string equality every time inside the DP,
        // we compute all valid placements once up front.
        //
        // matchesAt[s] will store a list of fragment indices i such that:
        // fragments[i] == target.Substring(s, fragments[i].Length)
        //
        // Then the DP can simply iterate those valid fragment choices.
        var matchesAt = new List<int>[m];
        for (int i = 0; i < m; i++)
        {
            matchesAt[i] = new List<int>();
        }

        for (int i = 0; i < n; i++)
        {
            string frag = fragments[i];
            int len = frag.Length;

            // A fragment can only start at positions where it fully fits inside target.
            for (int start = 0; start + len <= m; start++)
            {
                bool ok = true;

                // Compare fragment character-by-character with target at this start.
                // max fragment length is only 50, so this direct comparison is simple and fast.
                for (int j = 0; j < len; j++)
                {
                    if (target[start + j] != frag[j])
                    {
                        ok = false;
                        break;
                    }
                }

                if (ok)
                {
                    matchesAt[start].Add(i);
                }
            }
        }

        // --------------------------------------------------------------------
        // STEP 2: Dynamic Programming over built prefix length.
        // --------------------------------------------------------------------
        //
        // dp[p] = minimum cost to have built exactly the prefix target[0..p-1].
        //
        // Important interpretation:
        // If dp[p] is known, then positions [0..p-1] are already constructed.
        // We may place a new fragment with overlap t, where:
        //   0 <= t <= min(k, L-1, p)
        //
        // If the current built prefix length is p, and overlap is t,
        // then the fragment's actual start in target is:
        //   s = p - t
        //
        // The fragment must match target[s..s+L-1].
        // After placing it, the new built prefix becomes:
        //   newP = max(p, s + L)
        //
        // Since s = p - t and t <= L - 1, we always have s + L > p,
        // so in practice newP = s + L.
        //
        // Therefore each valid placement creates a transition:
        //   dp[newP] = min(dp[newP], dp[p] + fragmentCost)
        //
        // This is a shortest-path style DP on prefix lengths.
        var dp = new long[m + 1];
        for (int i = 0; i <= m; i++)
        {
            dp[i] = inf;
        }

        // Base case:
        // Building an empty prefix costs 0.
        dp[0] = 0;

        for (int p = 0; p <= m; p++)
        {
            // If this prefix length is unreachable, there is nothing to extend from here.
            if (dp[p] == inf)
            {
                continue;
            }

            // If we already built the full target, no need to extend further.
            if (p == m)
            {
                continue;
            }

            // ------------------------------------------------------------
            // For the current built prefix length p, try every allowed overlap.
            // ------------------------------------------------------------
            //
            // overlap t means the fragment starts at s = p - t.
            //
            // Constraints on t:
            // - t cannot exceed k
            // - t cannot exceed p (cannot overlap more characters than already built)
            //
            // We do not yet check t <= L - 1 here because L depends on the fragment.
            // That condition will be checked per fragment.
            int maxOverlap = Math.Min(k, p);

            for (int t = 0; t <= maxOverlap; t++)
            {
                int s = p - t;

                // s must be a valid target start position for some fragment.
                // If s == m, no fragment can start there because every fragment has length >= 1.
                if (s >= m)
                {
                    continue;
                }

                // Every fragment listed in matchesAt[s] exactly matches target starting at s.
                // Now we only need to verify that this fragment is allowed to be placed
                // from state p with overlap t.
                foreach (int idx in matchesAt[s])
                {
                    int len = fragments[idx].Length;

                    // A fragment is allowed to overlap by at most len - 1 characters.
                    // Why?
                    // The statement requires:
                    //   t <= min(k, L - 1, p)
                    //
                    // If t == len, the entire fragment would be inside the already-built area
                    // and would not extend construction, which is not allowed by the rule.
                    if (t > len - 1)
                    {
                        continue;
                    }

                    int newP = s + len;

                    // Because the fragment fully matches target at start s,
                    // and because t is a valid overlap amount,
                    // placing it extends the built prefix to newP.
                    long candidate = dp[p] + cost[idx];
                    if (candidate < dp[newP])
                    {
                        dp[newP] = candidate;
                    }
                }
            }
        }

        return dp[m] == inf ? -1 : dp[m];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
string target1 = "ababa";
string[] fragments1 = { "aba", "baba", "ab" };
int[] cost1 = { 4, 5, 2 };
int k1 = 2;
Console.WriteLine(solution.MinimumCost(target1, fragments1, cost1, k1)); // Expected: 6

// Example 2
string target2 = "aaaaab";
string[] fragments2 = { "aaa", "aaab", "b" };
int[] cost2 = { 3, 4, 10 };
int k2 = 2;
Console.WriteLine(solution.MinimumCost(target2, fragments2, cost2, k2)); // Expected: 7

// Additional small sanity checks
Console.WriteLine(solution.MinimumCost("abc", new[] { "a", "bc" }, new[] { 2, 3 }, 0)); // Expected: 5
Console.WriteLine(solution.MinimumCost("abc", new[] { "ab" }, new[] { 2 }, 1)); // Expected: -1