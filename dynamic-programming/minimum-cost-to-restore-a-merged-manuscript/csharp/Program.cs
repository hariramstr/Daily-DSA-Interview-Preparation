/*
Minimum Cost to Restore a Merged Manuscript

Problem Description:
A digital archive stores an original manuscript as a string target. During a faulty backup
process, the manuscript was split into reusable text fragments. You are given an array
fragments, where fragments[i] is a non-empty string and cost[i] is the cost to place that
fragment into the restoration plan. You may use any fragment any number of times.

Your task is to reconstruct target exactly from left to right by concatenating chosen
fragments. Every chosen fragment must match the next characters of target at the position
where it is placed. The total restoration cost is the sum of the costs of all fragments used.
Return the minimum possible total cost to build the entire target, or -1 if it is impossible.

Two fragments may have identical text but different costs, and they should be treated as
separate options. Because fragments may be reused unlimited times, a locally cheap choice is
not always globally optimal. This makes the problem a dynamic programming problem over
prefixes of the target string.

Constraints:
- 1 <= target.length <= 5000
- 1 <= fragments.length <= 1000
- 1 <= fragments[i].length <= 50
- target and all fragments[i] consist only of lowercase English letters
- 1 <= cost[i] <= 10^6
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the grouped dictionary: O(m), where m = number of fragments
    - Dynamic programming transitions:
      For each position in target (n positions), we only try fragments that start with the
      same first character as target at that position.
      For each candidate fragment, matching costs up to O(L), where L <= 50.
      In the worst case this is O(n * m * L), but in practice grouping by first character
      reduces unnecessary checks significantly.
    - With given constraints, this is efficient and safe.

    Space Complexity:
    - O(n) for the DP array
    - O(m) for storing grouped fragments
    */
    public long MinimumCost(string target, string[] fragments, int[] cost)
    {
        // We will use dynamic programming over prefixes of the target string.
        //
        // Meaning of dp[i]:
        // dp[i] = minimum cost needed to build the first i characters of target,
        //         that is target[0..i-1].
        //
        // Example:
        // If target = "apple", then:
        // dp[0] means cost to build ""
        // dp[1] means cost to build "a"
        // dp[2] means cost to build "ap"
        // ...
        // dp[5] means cost to build "apple"
        //
        // Our final answer will be dp[target.Length].
        int n = target.Length;

        // We need a very large value to represent "currently impossible".
        // We use long because costs can add up:
        // target length up to 5000, each placement cost up to 1e6,
        // so total can be as large as 5e9, which does not fit in int safely.
        long inf = long.MaxValue / 4;

        long[] dp = new long[n + 1];
        Array.Fill(dp, inf);

        // Base case:
        // It costs 0 to build an empty prefix.
        dp[0] = 0;

        // Optimization idea:
        // At position i, only fragments whose first character equals target[i]
        // can possibly match.
        //
        // So we group fragments by their first character.
        // This avoids checking all fragments at every position.
        //
        // We keep every fragment separately, even if text is identical,
        // because the problem explicitly says identical text with different costs
        // are separate options.
        var groups = new Dictionary<char, List<(string text, int price)>>();

        for (int i = 0; i < fragments.Length; i++)
        {
            char first = fragments[i][0];

            if (!groups.ContainsKey(first))
            {
                groups[first] = new List<(string text, int price)>();
            }

            groups[first].Add((fragments[i], cost[i]));
        }

        // Now we process the target from left to right.
        // If dp[i] is reachable, we try placing every valid fragment starting at i.
        for (int i = 0; i < n; i++)
        {
            // If this prefix cannot be formed, there is no point continuing from here.
            if (dp[i] == inf)
            {
                continue;
            }

            char neededFirstChar = target[i];

            // If no fragment starts with the needed character,
            // then no transition is possible from this position.
            if (!groups.TryGetValue(neededFirstChar, out var candidates))
            {
                continue;
            }

            // Try every candidate fragment that starts with target[i].
            foreach (var candidate in candidates)
            {
                string fragment = candidate.text;
                int fragmentCost = candidate.price;
                int len = fragment.Length;

                // If the fragment would go past the end of target,
                // it cannot be placed here.
                if (i + len > n)
                {
                    continue;
                }

                // Check whether fragment exactly matches target at position i.
                //
                // This step is necessary because even if the first character matches,
                // the rest of the fragment may not.
                bool matches = true;

                for (int j = 0; j < len; j++)
                {
                    if (target[i + j] != fragment[j])
                    {
                        matches = false;
                        break;
                    }
                }

                // Only if the fragment matches can we transition to the next state.
                if (!matches)
                {
                    continue;
                }

                // Transition:
                // If we can build the first i characters with cost dp[i],
                // and this fragment matches target starting at i,
                // then we can build the first i + len characters with:
                // dp[i] + fragmentCost
                //
                // We take the minimum because there may be many ways to reach
                // the same prefix length.
                long newCost = dp[i] + fragmentCost;

                if (newCost < dp[i + len])
                {
                    dp[i + len] = newCost;
                }
            }
        }

        // If the full target is still marked impossible, return -1.
        // Otherwise return the minimum cost found.
        return dp[n] == inf ? -1 : dp[n];
    }
}

// Demo code

var solution = new Solution();

// Example 1
string target1 = "abracadabra";
string[] fragments1 = { "ab", "ra", "cad", "a", "bra" };
int[] cost1 = { 4, 2, 5, 1, 3 };

long result1 = solution.MinimumCost(target1, fragments1, cost1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
string target2 = "applepenapple";
string[] fragments2 = { "apple", "pen", "app", "lepen" };
int[] cost2 = { 5, 2, 3, 10 };

long result2 = solution.MinimumCost(target2, fragments2, cost2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo: impossible case
string target3 = "hello";
string[] fragments3 = { "he", "ll", "world" };
int[] cost3 = { 2, 2, 10 };

long result3 = solution.MinimumCost(target3, fragments3, cost3);
Console.WriteLine($"Impossible Case Result: {result3}");