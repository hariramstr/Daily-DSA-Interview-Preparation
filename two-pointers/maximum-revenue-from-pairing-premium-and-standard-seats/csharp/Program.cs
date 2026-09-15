/*
Title: Maximum Revenue from Pairing Premium and Standard Seats

Problem Description:
You are managing ticket upgrades for a concert venue. There are two sorted integer arrays: premium and standard.
premium[i] is the minimum acceptable payment expected by the i-th premium seat holder if they give up their seat,
and standard[j] is the amount the j-th standard customer is willing to pay for an upgrade.

A valid upgrade pair matches one premium seat with one standard customer such that:
    standard[j] >= premium[i]

Each seat holder and each customer can be used at most once.

For every valid pair (i, j), the venue earns revenue:
    standard[j] - premium[i]

However, the venue is only allowed to create exactly k upgrade pairs.

Return the maximum total revenue possible, or -1 if it is impossible to form exactly k valid pairs.

Important note:
This is not a simple "match everything greedily" problem. A locally attractive choice can block better future
choices because each premium seat and each standard customer can only be used once.

Constraints:
- 1 <= premium.length, standard.length <= 2 * 10^5
- 0 <= premium[i], standard[j] <= 10^9
- premium is sorted in non-decreasing order
- standard is sorted in non-decreasing order
- 1 <= k <= min(premium.length, standard.length)
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity:
        O((n + m) * log(min(n, m))) in practice due to binary search over the number of "cross" pairs,
        plus O(n + m) preprocessing for prefix sums and O(log n) / O(log m) lower_bound style searches.
        More precisely, each candidate check is O(log n + log m), and we binary search over a monotone range.

    Space Complexity:
        O(n + m) for prefix sums.

    High-level idea:
    ----------------
    We want exactly k valid one-to-one pairs maximizing:

        sum(standard chosen) - sum(premium chosen)

    So the problem becomes:
    - choose k premium seats
    - choose k standard customers
    - arrange them so every chosen standard can afford its matched premium
    - maximize chosen standard sum minus chosen premium sum

    Key structural insight:
    -----------------------
    For any fixed set of k chosen premium seats and k chosen standard customers, a feasible matching exists
    if and only if after sorting both chosen sets increasingly, for every position t:

        chosenStandard[t] >= chosenPremium[t]

    Because arrays are already sorted globally, an optimal solution can be described by splitting the k pairs into:
    1) x "cross" pairs:
       - take x smallest premium seats from the whole array
       - pair them with x largest standard customers from the whole array
       These are always the most profitable type of pair because they use very small premium values and very large
       standard values.

    2) k - x "middle" pairs:
       - take a contiguous block of k - x premium seats from the right side of the remaining premium array
       - take a contiguous block of k - x standard customers from the left side of the remaining standard array
       These must still satisfy feasibility position-by-position.

    Why this structure works:
    -------------------------
    - To maximize revenue, we want premium values as small as possible and standard values as large as possible.
    - If we decide to use x very large standard customers, the best partners for them are x very small premium seats.
    - The remaining k - x pairs must be feasible among the middle portions.
    - For the middle feasible part, to maximize revenue we want:
        * the smallest possible premium block that can still be matched
        * the largest possible standard block that can still be matched
      Because arrays are sorted, these become contiguous blocks determined by feasibility.

    We can binary search the largest x for which the remaining middle part is feasible.
    Then compute the total revenue from:
        - x smallest premiums
        - x largest standards
        - best feasible middle blocks of size k - x

    This gives the optimal answer.

    The implementation below carefully computes:
    - prefix sums for fast range sums
    - a feasibility test for the middle part
    - the resulting total revenue for a chosen x
    */
    public long MaximumRevenue(int[] premium, int[] standard, int k)
    {
        int n = premium.Length;
        int m = standard.Length;

        // ------------------------------------------------------------
        // Step 1: Quick impossibility check.
        // ------------------------------------------------------------
        // Even before maximizing revenue, we must know whether it is possible
        // to form exactly k valid pairs at all.
        //
        // Since arrays are sorted, the maximum number of valid pairs can be found
        // greedily by trying to match the smallest premium seat with the smallest
        // standard customer who can afford it.
        //
        // This is the classic maximum bipartite matching count for sorted arrays.
        // If that maximum count is less than k, then forming exactly k pairs is impossible.
        // ------------------------------------------------------------
        if (MaxPairCount(premium, standard) < k)
            return -1;

        // ------------------------------------------------------------
        // Step 2: Build prefix sums.
        // ------------------------------------------------------------
        // Prefix sums let us compute sums of:
        // - first x elements
        // - last x elements
        // - any contiguous block
        //
        // in O(1) time after O(n) preprocessing.
        // We use long because sums can be large:
        // up to 2e5 * 1e9 = 2e14.
        // ------------------------------------------------------------
        long[] prePremium = BuildPrefixSum(premium);
        long[] preStandard = BuildPrefixSum(standard);

        // ------------------------------------------------------------
        // Step 3: Binary search the largest number x of "cross" pairs.
        // ------------------------------------------------------------
        // x means:
        // - use x smallest premium seats
        // - use x largest standard customers
        //
        // Then we still need to form rem = k - x pairs from the middle portions:
        // premium indices [x .. n-1]
        // standard indices [0 .. m-x-1]
        //
        // For the middle part, we want to know if there exists a feasible matching
        // of size rem. This feasibility is monotone in x:
        //
        // If some x works, then any smaller x also works.
        // Why?
        // - using fewer cross pairs leaves more flexibility in the middle.
        //
        // Therefore we can binary search the maximum feasible x.
        // ------------------------------------------------------------
        int low = 0;
        int high = k;
        int bestX = 0;

        while (low <= high)
        {
            int mid = low + (high - low) / 2;

            if (CanUseCrossPairs(premium, standard, k, mid))
            {
                bestX = mid;
                low = mid + 1;
            }
            else
            {
                high = mid - 1;
            }
        }

        // ------------------------------------------------------------
        // Step 4: Compute the optimal revenue using bestX.
        // ------------------------------------------------------------
        // Revenue = sum(chosen standards) - sum(chosen premiums)
        //
        // Cross part:
        //   premiums: first bestX elements
        //   standards: last bestX elements
        //
        // Middle part:
        //   rem = k - bestX
        //   We must choose:
        //     - a premium block of length rem from premium[bestX .. n-1]
        //     - a standard block of length rem from standard[0 .. m-bestX-1]
        //   maximizing revenue while maintaining sorted feasibility.
        //
        // For the middle part:
        //   To maximize revenue, we want the largest possible standard block of length rem,
        //   i.e. as far right as possible inside standard[0 .. m-bestX-1],
        //   and the smallest possible premium block of length rem,
        //   i.e. as far left as possible inside premium[bestX .. n-1],
        //   subject to feasibility.
        //
        // The feasibility condition for blocks:
        //   premium[startP + t] <= standard[startS + t] for all t in [0, rem-1]
        //
        // With the maximal feasible cross count bestX, the optimal middle blocks are:
        //   premium block starts at bestX
        //   standard block ends at m - bestX - 1
        //
        // In other words:
        //   middle premiums = premium[bestX .. bestX + rem - 1]
        //   middle standards = standard[(m - bestX - rem) .. (m - bestX - 1)]
        //
        // This arrangement is feasible exactly because bestX was chosen as the largest feasible
        // number of cross pairs.
        // ------------------------------------------------------------
        int remPairs = k - bestX;

        long premiumCrossSum = SumRange(prePremium, 0, bestX - 1);
        long standardCrossSum = SumRange(preStandard, m - bestX, m - 1);

        long premiumMiddleSum = 0;
        long standardMiddleSum = 0;

        if (remPairs > 0)
        {
            premiumMiddleSum = SumRange(prePremium, bestX, bestX + remPairs - 1);
            standardMiddleSum = SumRange(preStandard, m - bestX - remPairs, m - bestX - 1);
        }

        long answer = (standardCrossSum + standardMiddleSum) - (premiumCrossSum + premiumMiddleSum);
        return answer;
    }

    // ------------------------------------------------------------
    // Counts the maximum number of valid pairs possible.
    //
    // Greedy proof idea:
    // Always match the current smallest premium seat with the smallest
    // standard customer who can afford it. This never hurts future options.
    // ------------------------------------------------------------
    private int MaxPairCount(int[] premium, int[] standard)
    {
        int i = 0;
        int j = 0;
        int count = 0;

        while (i < premium.Length && j < standard.Length)
        {
            if (standard[j] >= premium[i])
            {
                count++;
                i++;
                j++;
            }
            else
            {
                j++;
            }
        }

        return count;
    }

    // ------------------------------------------------------------
    // Checks whether it is feasible to use exactly x cross pairs.
    //
    // Cross pairs consume:
    // - x smallest premium seats
    // - x largest standard customers
    //
    // Then we need rem = k - x more pairs from:
    // - premium[x .. n-1]
    // - standard[0 .. m-x-1]
    //
    // To maximize future revenue, the middle part should use:
    // - the smallest rem premiums available there
    // - the largest rem standards available there
    //
    // Feasibility of those rem pairs is equivalent to:
    //   premium[x + t] <= standard[(m - x - rem) + t] for all t
    //
    // Because both arrays are sorted, if this strongest revenue-oriented choice is feasible,
    // then x cross pairs can be part of an optimal solution.
    // ------------------------------------------------------------
    private bool CanUseCrossPairs(int[] premium, int[] standard, int k, int x)
    {
        int n = premium.Length;
        int m = standard.Length;
        int rem = k - x;

        if (rem == 0)
            return true;

        int startP = x;
        int startS = m - x - rem;

        if (startP + rem > n || startS < 0)
            return false;

        for (int t = 0; t < rem; t++)
        {
            if (premium[startP + t] > standard[startS + t])
                return false;
        }

        return true;
    }

    // ------------------------------------------------------------
    // Builds prefix sum array where:
    // pre[0] = 0
    // pre[i+1] = arr[0] + ... + arr[i]
    //
    // Then sum of arr[l..r] is:
    // pre[r+1] - pre[l]
    // ------------------------------------------------------------
    private long[] BuildPrefixSum(int[] arr)
    {
        long[] pre = new long[arr.Length + 1];
        for (int i = 0; i < arr.Length; i++)
            pre[i + 1] = pre[i] + arr[i];
        return pre;
    }

    // ------------------------------------------------------------
    // Returns sum of arr[l..r], inclusive.
    // If l > r, returns 0.
    // ------------------------------------------------------------
    private long SumRange(long[] pre, int l, int r)
    {
        if (l > r) return 0;
        return pre[r + 1] - pre[l];
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

// Example 1
int[] premium1 = { 2, 4, 7 };
int[] standard1 = { 5, 8, 10 };
int k1 = 2;

var solution = new Solution();
long result1 = solution.MaximumRevenue(premium1, standard1, k1);
Console.WriteLine(result1); // Expected: 9

// Example 2
int[] premium2 = { 3, 6, 9 };
int[] standard2 = { 4, 5, 7 };
int k2 = 2;

long result2 = solution.MaximumRevenue(premium2, standard2, k2);
Console.WriteLine(result2); // Expected: -1

// Additional sanity checks

int[] premium3 = { 1, 2, 3 };
int[] standard3 = { 3, 4, 5 };
int k3 = 3;
Console.WriteLine(solution.MaximumRevenue(premium3, standard3, k3)); // 6

int[] premium4 = { 5 };
int[] standard4 = { 5 };
int k4 = 1;
Console.WriteLine(solution.MaximumRevenue(premium4, standard4, k4)); // 0