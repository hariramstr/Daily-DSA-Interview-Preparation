/*
Title: Minimum Risk to Merge Security Zones

Problem Description:
A company is consolidating a row of security zones in a data center. The zones are numbered from left to right, and zone i has a risk value risk[i]. To simplify monitoring, the company wants to repeatedly merge adjacent groups of zones until only one group remains.

If you merge two already-formed adjacent groups, the cost of that merge is equal to the sum of all risk values in the final combined group. However, not every merge order is allowed: a merge is valid only if at least one of the two groups being merged has total risk less than or equal to T. This rule models the requirement that at least one side of a merge must still be small enough to audit safely.

Return the minimum total cost to merge all zones into one group. If it is impossible to merge all zones while respecting the rule, return -1.

You may assume every zone starts as its own group, and each merge combines exactly two adjacent groups. The total cost is the sum of the costs of all performed merges.

Constraints:
- 1 <= n == risk.length <= 300
- 1 <= risk[i] <= 10^6
- 1 <= T <= 10^12

Example 1:
Input: risk = [4, 2, 7, 3], T = 6
Output: 32

Example 2:
Input: risk = [8, 9, 5], T = 6
Output: -1
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^3)
    Space Complexity: O(n^2)

    Explanation of complexity:
    - We use a classic interval dynamic programming table dp[left, right].
    - There are O(n^2) intervals.
    - For each interval, we try every possible split point, which is O(n).
    - Therefore total time is O(n^3), which is acceptable for n <= 300.
    - The DP table itself stores one value per interval, so space is O(n^2).
    */
    public long MinimumRiskToMerge(int[] risk, long t)
    {
        int n = risk.Length;

        // If there is only one zone, it is already a single group.
        // No merge is needed, so the total cost is 0.
        if (n == 1)
        {
            return 0;
        }

        // Prefix sums let us compute the total risk of any subarray in O(1).
        // prefix[i] = sum of risk[0..i-1]
        // Then sum of interval [l..r] is prefix[r+1] - prefix[l].
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + risk[i];
        }

        // We will use a very large number to represent "impossible".
        // We avoid long.MaxValue directly so that adding values to it does not overflow.
        const long INF = long.MaxValue / 4;

        // dp[l, r] = minimum cost to fully merge the subarray risk[l..r] into one group,
        // while respecting the threshold rule on every merge performed inside that interval.
        //
        // If dp[l, r] stays INF, that means this interval cannot be fully merged legally.
        long[,] dp = new long[n, n];

        // Initialize all entries to INF first.
        for (int l = 0; l < n; l++)
        {
            for (int r = 0; r < n; r++)
            {
                dp[l, r] = INF;
            }
        }

        // Base case:
        // A single zone is already one group, so cost = 0.
        // No merge operation is needed.
        for (int i = 0; i < n; i++)
        {
            dp[i, i] = 0;
        }

        // We now process intervals by increasing length.
        // This is the standard order for interval DP because when computing dp[l, r],
        // we need smaller intervals dp[l, k] and dp[k+1, r] to already be known.
        for (int length = 2; length <= n; length++)
        {
            for (int left = 0; left + length - 1 < n; left++)
            {
                int right = left + length - 1;

                // Compute the total sum of the current interval [left..right].
                // This is the cost paid for the final merge that combines the two subgroups
                // into one group covering the whole interval.
                long totalSum = prefix[right + 1] - prefix[left];

                // Try every possible final split point.
                //
                // If the final merge splits [left..right] into:
                //   [left..mid] and [mid+1..right]
                //
                // then:
                // 1. The left interval must be fully mergeable.
                // 2. The right interval must be fully mergeable.
                // 3. The final merge between those two resulting groups must be valid.
                //
                // The validity rule says:
                // at least one side's total group sum must be <= t.
                for (int mid = left; mid < right; mid++)
                {
                    // If either side cannot be formed legally, skip this split.
                    if (dp[left, mid] == INF || dp[mid + 1, right] == INF)
                    {
                        continue;
                    }

                    // Sum of the left final group [left..mid]
                    long leftSum = prefix[mid + 1] - prefix[left];

                    // Sum of the right final group [mid+1..right]
                    long rightSum = prefix[right + 1] - prefix[mid + 1];

                    // Check whether the final merge is allowed.
                    // It is allowed if at least one side is small enough.
                    if (leftSum <= t || rightSum <= t)
                    {
                        // Total cost for this split:
                        // - cost to merge left side into one group
                        // - cost to merge right side into one group
                        // - cost of the final merge, which equals totalSum
                        long candidate = dp[left, mid] + dp[mid + 1, right] + totalSum;

                        // Keep the minimum over all valid split points.
                        if (candidate < dp[left, right])
                        {
                            dp[left, right] = candidate;
                        }
                    }
                }
            }
        }

        // If the whole array cannot be merged legally, return -1.
        // Otherwise return the minimum total cost.
        return dp[0, n - 1] == INF ? -1 : dp[0, n - 1];
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] risk1 = { 4, 2, 7, 3 };
long t1 = 6;
long result1 = solution.MinimumRiskToMerge(risk1, t1);
Console.WriteLine(result1); // Expected: 32

// Example 2
int[] risk2 = { 8, 9, 5 };
long t2 = 6;
long result2 = solution.MinimumRiskToMerge(risk2, t2);
Console.WriteLine(result2); // Expected: -1

// Additional quick checks

// Single element: already merged
int[] risk3 = { 10 };
long t3 = 5;
Console.WriteLine(solution.MinimumRiskToMerge(risk3, t3)); // Expected: 0

// Simple valid chain
int[] risk4 = { 1, 2, 3 };
long t4 = 3;
Console.WriteLine(solution.MinimumRiskToMerge(risk4, t4)); // One optimal answer: 9