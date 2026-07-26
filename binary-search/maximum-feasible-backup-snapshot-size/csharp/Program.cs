/*
Title: Maximum Feasible Backup Snapshot Size

Problem Description:
A company stores daily database backups in a fixed order. The i-th backup has size backups[i] gigabytes.
To reduce restore complexity, the operations team wants to group the backups into exactly k contiguous
restore bundles. Every backup must belong to exactly one bundle, and bundles must preserve the original order.

For a chosen snapshot size limit S, a bundle is considered valid only if the total size of backups inside
that bundle is at least S. Since large bundles are harder to manage, the team wants to know the largest
snapshot size limit S such that it is still possible to partition the array into exactly k contiguous valid bundles.

Return the maximum possible value of S.

In other words, split the array into exactly k non-empty contiguous parts, maximize the minimum part sum,
and return that optimal minimum sum.

Key idea:
- If some value S is feasible, then every smaller value is also feasible.
- That monotonic property makes binary search a perfect fit.

Examples:
1) backups = [7,2,5,10,8], k = 2
   Answer = 14
   Partition: [7,2,5] and [10,8] => sums 14 and 18 => minimum is 14

2) backups = [4,4,4,4,4,4,4], k = 3
   Answer = 8
   Partition: [4,4], [4,4], [4,4,4] => sums 8, 8, 12 => minimum is 8
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Let n be backups.Length
    - Each feasibility check scans the array once: O(n)
    - Binary search runs over the answer range [1, totalSum / k], which takes O(log(totalSum))
    - Total: O(n * log(totalSum))

    Space Complexity:
    - O(1) extra space, ignoring the input array

    Beginner-friendly summary:
    - We binary search the answer S.
    - For each candidate S, we greedily count how many contiguous bundles can be formed
      such that each bundle has sum at least S.
    - If we can form at least k such bundles, then S is feasible.
    - Otherwise, S is too large.
    */
    public long MaximizeMinimumBundleSum(int[] backups, int k)
    {
        // Step 1:
        // Compute the total sum of all backups.
        //
        // Why do we need this?
        // - The minimum bundle sum can never be larger than totalSum / k.
        //   Reason: if every one of the k bundles had sum > totalSum / k, then the total
        //   would exceed totalSum, which is impossible.
        //
        // We use long because:
        // - backups[i] can be as large as 1,000,000,000
        // - n can be as large as 200,000
        // - The total can be much larger than what int can safely hold.
        long totalSum = 0;
        foreach (int size in backups)
        {
            totalSum += size;
        }

        // Step 2:
        // Establish the binary search boundaries.
        //
        // low:
        // - The smallest meaningful candidate is 1 because all values are positive.
        //
        // high:
        // - As explained above, the answer cannot exceed totalSum / k.
        //
        // We search for the largest feasible S.
        long low = 1;
        long high = totalSum / k;

        // This variable will store the best feasible answer found so far.
        long best = 0;

        // Step 3:
        // Standard binary search on the answer space.
        //
        // Invariant:
        // - If mid is feasible, then every value <= mid is also feasible.
        // - If mid is not feasible, then every value > mid is also not feasible.
        while (low <= high)
        {
            // Compute the middle carefully.
            // This form avoids overflow in general binary search patterns.
            long mid = low + (high - low) / 2;

            // Check whether it is possible to split the array into exactly k contiguous bundles
            // such that every bundle has sum at least mid.
            //
            // Important subtle point:
            // We actually check whether we can form AT LEAST k bundles with sum >= mid.
            //
            // Why is "at least k" enough when the problem asks for exactly k?
            // Because all numbers are positive.
            // If we can form more than k valid bundles, we can merge adjacent valid bundles together.
            // Merging preserves contiguity and only increases the sum, so the merged bundle is still valid.
            // Therefore, "at least k" implies "exactly k" is achievable.
            if (CanMakeAtLeastKBundles(backups, k, mid))
            {
                // mid is feasible, so it is a candidate answer.
                best = mid;

                // Since we want the maximum feasible value, try larger values.
                low = mid + 1;
            }
            else
            {
                // mid is too large, so we must search smaller values.
                high = mid - 1;
            }
        }

        // After binary search finishes, best holds the largest feasible minimum bundle sum.
        return best;
    }

    private bool CanMakeAtLeastKBundles(int[] backups, int k, long target)
    {
        // This method performs a greedy feasibility check.
        //
        // Goal:
        // - Count how many contiguous bundles we can create where each bundle sum is at least "target".
        //
        // Greedy rule:
        // - Keep adding backups to the current bundle.
        // - As soon as the current bundle reaches or exceeds target, immediately "close" that bundle
        //   and start a new one.
        //
        // Why is this greedy strategy correct?
        // - We want to maximize the number of valid bundles.
        // - Closing a bundle as early as possible leaves as many remaining elements as possible
        //   for future bundles.
        // - Since all numbers are positive, delaying the cut can never help us create MORE bundles.
        //
        // Therefore, this greedy process gives the maximum number of bundles achievable for this target.
        int bundlesFormed = 0;
        long currentSum = 0;

        foreach (int size in backups)
        {
            // Add the current backup to the running sum of the current bundle.
            currentSum += size;

            // If the current bundle has reached the required minimum sum,
            // we finalize this bundle immediately.
            if (currentSum >= target)
            {
                bundlesFormed++;

                // Reset for the next bundle.
                currentSum = 0;

                // Small optimization:
                // If we already formed at least k bundles, we can stop early.
                if (bundlesFormed >= k)
                {
                    return true;
                }
            }
        }

        // If we finished scanning the array and formed fewer than k bundles,
        // then target is not feasible.
        return false;
    }
}

// Demo code
var solution = new Solution();

// Example 1:
// backups = [7,2,5,10,8], k = 2
// Expected output: 14
int[] backups1 = { 7, 2, 5, 10, 8 };
int k1 = 2;
long result1 = solution.MaximizeMinimumBundleSum(backups1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// backups = [4,4,4,4,4,4,4], k = 3
// Expected output: 8
int[] backups2 = { 4, 4, 4, 4, 4, 4, 4 };
int k2 = 3;
long result2 = solution.MaximizeMinimumBundleSum(backups2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick sanity checks:

// If k equals the number of backups, each bundle must contain exactly one element.
// Therefore the answer is the minimum element.
int[] backups3 = { 9, 1, 7, 3 };
int k3 = 4;
long result3 = solution.MaximizeMinimumBundleSum(backups3, k3);
Console.WriteLine($"Sanity Check 1 Result: {result3}");

// If k = 1, the whole array is one bundle, so the answer is the total sum.
int[] backups4 = { 5, 6, 7 };
int k4 = 1;
long result4 = solution.MaximizeMinimumBundleSum(backups4, k4);
Console.WriteLine($"Sanity Check 2 Result: {result4}");