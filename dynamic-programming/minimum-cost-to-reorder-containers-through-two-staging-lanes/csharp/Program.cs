/*
Title: Minimum Cost to Reorder Containers Through Two Staging Lanes

Problem Description:
A port receives containers in a fixed arrival order. Each container has a positive integer weight.
Before loading them onto a ship, the port may route every arriving container into exactly one of
two staging lanes, A or B. Containers assigned to the same lane must remain in their original
relative order. After all containers are assigned, the ship is loaded by repeatedly taking the
front container from either lane until both lanes are empty.

The final loading order must be nondecreasing by weight. If a container of weight w is placed
immediately after a container of weight p in the final sequence, the loading cost increases by
|w - p|. The first loaded container adds no cost.

Your task is to compute the minimum possible total loading cost, or return -1 if no valid
nondecreasing loading order can be formed using exactly these two staging lanes.

In other words, you must partition the original sequence into two subsequences, preserving order
within each subsequence, so that they can be merged into one nondecreasing sequence. Among all
such feasible partitions and merges, minimize the sum of absolute differences between consecutive
loaded weights.

Constraints:
- 1 <= n <= 3000
- 1 <= weights[i] <= 10^9
- An O(n^2) dynamic programming solution is expected.

Important observation:
Because the final loading order must be nondecreasing and it contains exactly the same multiset of
weights as the input, the final loaded sequence is forced to be the globally sorted order of the
input values (with duplicates grouped naturally). Therefore:

1. Feasibility:
   We need to know whether the original sequence can be partitioned into two subsequences such that
   each subsequence is nondecreasing. This is exactly the same as asking whether the sequence can be
   merged from two nondecreasing lanes.

2. Cost:
   Since every valid final loading order is simply the sorted array, the minimum possible loading
   cost is fixed whenever a feasible partition exists:
       sum(sorted[i] - sorted[i-1]) for i = 1..n-1
   because the sorted order is nondecreasing, so absolute value is unnecessary there.

So the real algorithmic task is feasibility of partitioning into two nondecreasing subsequences.

Dynamic Programming idea:
Process containers from left to right. At any moment, each processed container has been assigned to
lane A or lane B. Since each lane must be nondecreasing, the only information we need to continue
is the last weight placed into each lane.

A classic O(n^2) DP state:
- Let dp[j] mean:
  after processing some prefix ending at index i, container i is the last one placed into one lane,
  and container j is the last one placed into the other lane (j may be 0 meaning that lane is empty).
  If this state is reachable, dp[j] is true.

We use 1-based indexing for convenience and a sentinel index 0 with value -infinity.

Transition when processing next container x = a[i+1]:
- Put x into the same lane that currently ends with a[i]:
  allowed if x >= a[i], then the other lane's last index j stays unchanged.
- Put x into the other lane:
  allowed if x >= a[j], then the roles swap, so the new state becomes "last indices are (j becomes i)".

This checks whether a 2-lane nondecreasing partition exists.

If feasible, compute the cost from the sorted array.
*/

using System;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility DP: O(n^2)
    - Sorting for cost: O(n log n)
    Overall: O(n^2), which matches the expected complexity.

    Space Complexity:
    - O(n) for the rolling DP arrays
    - O(n) for the sorted copy
    Overall: O(n)
    */
    public long MinimumCostToReorderContainersThroughTwoStagingLanes(int[] weights)
    {
        int n = weights.Length;

        // ------------------------------------------------------------
        // Step 1: Handle the smallest trivial case.
        // ------------------------------------------------------------
        // If there is only one container, it can always be loaded first,
        // and the first loaded container contributes zero cost.
        if (n == 1)
        {
            return 0;
        }

        // ------------------------------------------------------------
        // Step 2: Build a 1-based array with a sentinel at index 0.
        // ------------------------------------------------------------
        // Why do this?
        // - It makes the DP transitions much cleaner.
        // - Index 0 represents an empty lane.
        // - We assign it a value smaller than every possible weight so
        //   that any first real container can be placed into that lane.
        //
        // Since weights are positive integers, using long.MinValue as the
        // sentinel is perfectly safe.
        long[] a = new long[n + 1];
        a[0] = long.MinValue;
        for (int i = 1; i <= n; i++)
        {
            a[i] = weights[i - 1];
        }

        // ------------------------------------------------------------
        // Step 3: DP for feasibility of partitioning into two
        //         nondecreasing subsequences.
        // ------------------------------------------------------------
        //
        // State meaning:
        // After processing containers 1..i, we maintain states where:
        // - container i is the last item placed into one lane
        // - container j is the last item placed into the other lane
        // - reachable[j] == true means such an assignment exists
        //
        // We only need a rolling array because transitions from i to i+1
        // depend only on the previous layer.
        //
        // Initial state:
        // After processing the first container (index 1), we can place it
        // into one lane, while the other lane is still empty.
        // So reachable[0] = true.
        bool[] reachable = new bool[n + 1];
        reachable[0] = true;

        // Process next containers one by one.
        for (int i = 1; i < n; i++)
        {
            bool[] next = new bool[n + 1];
            long currentLast = a[i];
            long nextValue = a[i + 1];

            // Try every reachable state for the processed prefix 1..i.
            for (int j = 0; j < i; j++)
            {
                if (!reachable[j])
                {
                    continue;
                }

                // ----------------------------------------------------
                // Option 1: Put a[i+1] into the same lane that currently
                //           ends with a[i].
                // ----------------------------------------------------
                // This is allowed only if that lane remains nondecreasing,
                // meaning nextValue >= currentLast.
                //
                // If we do this, the "other lane" still ends at j, so the
                // state remains represented by next[j].
                if (nextValue >= currentLast)
                {
                    next[j] = true;
                }

                // ----------------------------------------------------
                // Option 2: Put a[i+1] into the other lane, the one that
                //           currently ends with a[j].
                // ----------------------------------------------------
                // This is allowed only if nextValue >= a[j].
                //
                // After placing a[i+1] there, the roles of the two lanes
                // swap in our compressed representation:
                // - one lane now ends at i   (the lane that used to end at i)
                // - the other lane now ends at i+1, which becomes the
                //   distinguished "current" lane for the next iteration
                //
                // Therefore the new stored index becomes i.
                if (nextValue >= a[j])
                {
                    next[i] = true;
                }
            }

            reachable = next;
        }

        // ------------------------------------------------------------
        // Step 4: Check whether any feasible final state exists.
        // ------------------------------------------------------------
        // If no state is reachable after processing all containers,
        // then it is impossible to split the sequence into two
        // nondecreasing lanes, so the answer is -1.
        bool feasible = false;
        for (int j = 0; j < n; j++)
        {
            if (reachable[j])
            {
                feasible = true;
                break;
            }
        }

        if (!feasible)
        {
            return -1;
        }

        // ------------------------------------------------------------
        // Step 5: Compute the minimum cost.
        // ------------------------------------------------------------
        // Once feasibility is established, the final loaded sequence must
        // be the globally sorted order of all weights, because:
        // - it must contain exactly all containers
        // - it must be nondecreasing
        //
        // Therefore the cost is simply the sum of differences between
        // consecutive elements in the sorted array.
        //
        // We use long because:
        // - weights can be up to 1e9
        // - n can be up to 3000
        // - the total sum can exceed int range
        int[] sorted = weights.ToArray();
        Array.Sort(sorted);

        long cost = 0;
        for (int i = 1; i < sorted.Length; i++)
        {
            cost += (long)sorted[i] - sorted[i - 1];
        }

        return cost;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] weights1 = { 4, 1, 3, 2 };
long result1 = solution.MinimumCostToReorderContainersThroughTwoStagingLanes(weights1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] weights2 = { 3, 1, 2, 1 };
long result2 = solution.MinimumCostToReorderContainersThroughTwoStagingLanes(weights2);
Console.WriteLine(result2); // Expected: -1

// Additional quick checks
int[] weights3 = { 1 };
Console.WriteLine(solution.MinimumCostToReorderContainersThroughTwoStagingLanes(weights3)); // Expected: 0

int[] weights4 = { 2, 1 };
Console.WriteLine(solution.MinimumCostToReorderContainersThroughTwoStagingLanes(weights4)); // Expected: 1

int[] weights5 = { 1, 2, 3, 4 };
Console.WriteLine(solution.MinimumCostToReorderContainersThroughTwoStagingLanes(weights5)); // Expected: 3