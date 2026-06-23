/*
Title: Minimum Upgrade Level for Reliable Service Bundles

Problem Description:
A cloud platform offers n microservices. The i-th service currently runs at reliability level levels[i].
You may apply a global upgrade policy with integer strength X. After the policy is applied, every service
with level below X is upgraded up to exactly X, while services already at or above X remain unchanged.

The platform sells bundles of consecutive services. A bundle is considered reliable if the sum of the final
reliability levels of all services in that bundle is at least target. You are given an integer k, and your
goal is to make at least k reliable bundles.

Return the minimum integer X such that after applying the upgrade policy, the number of reliable contiguous
bundles is at least k. If the condition is already satisfied without any upgrade, return 0.

Formally, define final[i] = max(levels[i], X). Count how many pairs (l, r) with 0 <= l <= r < n satisfy
sum(final[l..r]) >= target. Find the smallest X for which this count is at least k.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= levels[i] <= 10^9
- 1 <= target <= 10^18
- 1 <= k <= n * (n + 1) / 2
- X is an integer in the range [0, 10^9]
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of services.
    - We binary search the answer X over [0, 1e9], which takes O(log 1e9) ~= 31 iterations.
    - For each candidate X, we count how many subarrays have sum >= target in O(n),
      using a two-pointer / sliding window technique.
    - Total: O(n log 1e9)

    Space Complexity:
    - O(n) for storing the transformed values for the current X.
    - The counting itself uses O(1) extra beyond that array.
    */
    public int MinimumUpgradeLevel(int[] levels, long target, long k)
    {
        // ------------------------------------------------------------
        // Step 1:
        // Before doing any binary search, check whether X = 0 already works.
        //
        // Why?
        // The problem explicitly says:
        // "If the condition is already satisfied without any upgrade, return 0."
        //
        // Since final[i] = max(levels[i], 0), and levels[i] are non-negative,
        // X = 0 means the array stays unchanged.
        // ------------------------------------------------------------
        if (CountReliableBundles(levels, 0, target, k) >= k)
        {
            return 0;
        }

        // ------------------------------------------------------------
        // Step 2:
        // Binary search for the minimum X in [1, 1e9].
        //
        // Why binary search is valid:
        // If some X works, then any larger X also works.
        //
        // Reason:
        // final[i] = max(levels[i], X)
        // Increasing X can only keep each final[i] the same or increase it.
        // Therefore every subarray sum stays the same or increases.
        // So the number of subarrays with sum >= target is monotonic non-decreasing.
        //
        // This monotonic property is exactly what binary search needs.
        // ------------------------------------------------------------
        int left = 1;
        int right = 1_000_000_000;
        int answer = right;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            // --------------------------------------------------------
            // Step 3:
            // Check whether this candidate upgrade level mid is enough.
            //
            // If count >= k:
            //   mid is valid, but maybe there is a smaller valid X.
            //   So we store it and continue searching left half.
            //
            // Else:
            //   mid is too small, so we must search right half.
            // --------------------------------------------------------
            long count = CountReliableBundles(levels, mid, target, k);

            if (count >= k)
            {
                answer = mid;
                right = mid - 1;
            }
            else
            {
                left = mid + 1;
            }
        }

        return answer;
    }

    private long CountReliableBundles(int[] levels, int x, long target, long kLimit)
    {
        int n = levels.Length;

        // ------------------------------------------------------------
        // Step 1:
        // Build the transformed array:
        // transformed[i] = max(levels[i], x)
        //
        // Why do this explicitly?
        // It makes the later sliding-window logic easier to read and explain.
        // Since all values are non-negative, sliding window works correctly.
        // ------------------------------------------------------------
        long[] transformed = new long[n];
        for (int i = 0; i < n; i++)
        {
            transformed[i] = Math.Max((long)levels[i], (long)x);
        }

        // ------------------------------------------------------------
        // Step 2:
        // Count subarrays with sum >= target.
        //
        // Important observation:
        // All transformed values are non-negative.
        //
        // For arrays with non-negative numbers, we can use a sliding window:
        // - Expand the right end until the sum becomes >= target.
        // - Once sum >= target for a fixed left, then every larger right also works,
        //   because adding more non-negative values cannot decrease the sum.
        //
        // So if the smallest valid right for this left is r,
        // then the number of valid subarrays starting at left is:
        //   n - r
        //
        // Then move left forward and update the window sum.
        //
        // This gives O(n) counting instead of O(n^2).
        // ------------------------------------------------------------
        long count = 0;
        long windowSum = 0;
        int right = 0;

        for (int left = 0; left < n; left++)
        {
            // --------------------------------------------------------
            // Expand the right pointer until:
            // - either the window sum reaches target
            // - or right reaches the end of the array
            //
            // Why this is efficient:
            // right only moves forward across the entire algorithm,
            // never backward, so total work is O(n).
            // --------------------------------------------------------
            while (right < n && windowSum < target)
            {
                windowSum += transformed[right];
                right++;
            }

            // --------------------------------------------------------
            // If windowSum >= target now, then:
            // the current window is [left .. right-1]
            // and it is the smallest right endpoint found for this left.
            //
            // Therefore all subarrays:
            // [left .. right-1], [left .. right], ..., [left .. n-1]
            // are valid.
            //
            // Count added = n - (right - 1) = n - right + 1
            // --------------------------------------------------------
            if (windowSum >= target)
            {
                count += (n - right + 1);

                // ----------------------------------------------------
                // Small optimization:
                // We only need to know whether count >= k in the binary search.
                // So if we already reached or exceeded kLimit, we can stop early.
                //
                // This avoids unnecessary work on large inputs.
                // ----------------------------------------------------
                if (count >= kLimit)
                {
                    return count;
                }
            }

            // --------------------------------------------------------
            // Before moving left to left + 1, remove transformed[left]
            // from the current window sum.
            //
            // But only if left is actually inside the current window.
            //
            // There is one subtle case:
            // If right == left, it means the window is empty.
            // Then we should advance right as well to keep pointers consistent.
            //
            // In this implementation, because we always expand while
            // right < n and windowSum < target, and values are non-negative,
            // the standard removal logic below is enough:
            // - if right > left, transformed[left] is in the window
            // - otherwise the window is empty and we do nothing
            // --------------------------------------------------------
            if (right > left)
            {
                windowSum -= transformed[left];
            }
            else
            {
                right = left + 1;
            }
        }

        return count;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// levels = [1, 3, 2], target = 5, k = 4
// Expected output: 2
int[] levels1 = { 1, 3, 2 };
long target1 = 5;
long k1 = 4;
int result1 = solution.MinimumUpgradeLevel(levels1, target1, k1);
Console.WriteLine(result1);

// Example 2:
// levels = [0, 0, 4, 1], target = 4, k = 8
// Expected output: 3
int[] levels2 = { 0, 0, 4, 1 };
long target2 = 4;
long k2 = 8;
int result2 = solution.MinimumUpgradeLevel(levels2, target2, k2);
Console.WriteLine(result2);