/*
Title: Maximum Prefix Score After One Value Relocation
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array nums of length n. Define the prefix score of the array as the number of indices i (0 <= i < n) such that the sum of the first i + 1 elements is strictly greater than 0. In other words, if prefix[i] = nums[0] + nums[1] + ... + nums[i], then index i contributes to the score if prefix[i] > 0.

You may perform at most one relocation operation. In a relocation, choose one element nums[j], remove it from its current position, and insert it at any other position in the array (including the beginning or the end). The relative order of all other elements must stay the same.

Return the maximum possible prefix score after applying at most one relocation.

This problem asks you to reason about how moving a single value changes many prefix sums at once. A brute-force solution that tries every move and recomputes all prefix sums will be too slow.

Constraints:
- 1 <= n <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- The answer fits in a 32-bit signed integer.

Example 1:
Input: nums = [4, -5, 2, 1]
Output: 4
Explanation: Move -5 from index 1 to the end, producing [4, 2, 1, -5].
The prefix sums become [4, 6, 7, 2], all of which are positive, so the score is 4.
Without any move, the prefix sums are [4, -1, 1, 2], so the score is only 3.

Example 2:
Input: nums = [-2, 3, -1, 2]
Output: 3
Explanation: Move -2 from index 0 to index 2, producing [3, -1, -2, 2].
The prefix sums become [3, 2, 0, 2]. Positive prefixes occur at indices 0, 1, and 3, so the score is 3.
No relocation can make all 4 prefix sums strictly positive.
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity: O(n log n)
    Space Complexity: O(n)

    High-level idea:
    ----------------
    Let pref[i] be the original prefix sum up to index i.

    If we remove nums[j] and insert it later (to the right), then:
    - Prefixes before j are unchanged.
    - Prefixes from j to insertion-1 increase by (-nums[j]) because nums[j] is removed from them.
    - Prefixes after insertion-1 are unchanged again, because nums[j] gets added back later.

    This means moving a NEGATIVE value to the right can only help:
    it adds a positive amount to one contiguous range of prefix sums.

    Similarly, if we remove nums[j] and insert it earlier (to the left), then:
    - Prefixes in one contiguous range decrease by nums[j].
    This is only helpful when nums[j] is POSITIVE? No, decreasing prefixes is bad.
    But if nums[j] is NEGATIVE and moved earlier, prefixes decrease even more, also bad.
    If nums[j] is POSITIVE and moved later, prefixes decrease on a range, also bad.
    Therefore, the only useful move is:
        move one NEGATIVE element to some later position.

    So the problem becomes:
    choose one negative nums[j] = x,
    let delta = -x > 0,
    and choose a suffix-ending position so that on some range [j .. k] we add delta to pref[*].
    We want to maximize how many values in that range become > 0.

    For a fixed j:
    - Every index i >= j with pref[i] in (-delta, 0] becomes newly positive.
    - Indices already > 0 stay positive.
    - We can choose k as far right as we want before reinserting, so the best choice is to extend
      the affected range as far right as possible, i.e. move nums[j] to the end.
      Why? Because adding delta to more prefixes cannot hurt; it can only keep or increase the count
      of positive prefixes. The final total prefix sum at the very end remains unchanged, so the last
      prefix is also unchanged, but every earlier extra affected prefix is potentially beneficial.
    Therefore, the optimal relocation is:
        choose one negative element and move it to the end.

    After moving nums[j] to the end:
    - Prefixes for i < j are unchanged.
    - Prefixes for j <= i <= n-2 become pref[i] - nums[j] = pref[i] + delta.
    - Prefix at n-1 stays the total sum, unchanged.

    So the gain for choosing j is:
        number of indices i in [j .. n-2] such that
            pref[i] <= 0 and pref[i] + delta > 0
        i.e.
            pref[i] in (-delta, 0]

    We need to compute this efficiently for every negative j.

    We process j from right to left and maintain a multiset of prefix sums pref[i] for i in [j .. n-2].
    For each negative nums[j], with delta = -nums[j], we need the count of values in that multiset
    that lie in [-delta + 1? careful with integers?]
    Since values are long integers and condition is:
        -delta < pref[i] <= 0
    equivalently:
        pref[i] in [(-delta + 1) .. 0] for integers,
    but easier and safer with order statistics:
        count(pref <= 0) - count(pref <= -delta)

    Because:
        pref in (-delta, 0]  <=>  pref <= 0 minus pref <= -delta

    We use coordinate compression + Fenwick tree to support:
    - add one prefix sum
    - query how many inserted prefix sums are <= some value

    Base score = count of original pref[i] > 0.
    Answer = base score + best gain.
    */
    public int MaxPrefixScoreAfterOneRelocation(int[] nums)
    {
        int n = nums.Length;

        // Step 1:
        // Build the original prefix sums.
        // We use long because:
        // - nums[i] can be as large as 1e9 in magnitude
        // - n can be 2e5
        // - cumulative sums can exceed int range
        long[] pref = new long[n];
        long running = 0;
        int baseScore = 0;

        for (int i = 0; i < n; i++)
        {
            running += nums[i];
            pref[i] = running;

            // A prefix contributes to the score if it is strictly greater than 0.
            if (pref[i] > 0)
            {
                baseScore++;
            }
        }

        // If n == 1, relocation cannot improve anything in a meaningful way.
        // The general logic below still works, but this early return keeps things simple.
        if (n == 1)
        {
            return baseScore;
        }

        // Step 2:
        // Coordinate compression for all prefix sums, plus the query boundary values we will use.
        //
        // We need to query:
        //   count of inserted prefix sums <= 0
        //   count of inserted prefix sums <= -delta
        //
        // Since delta = -nums[j] for negative nums[j], the value -delta is exactly nums[j].
        // So query boundaries are:
        //   0
        //   nums[j] for negative nums[j]
        //
        // Also we insert actual prefix sums pref[i] for i in [j .. n-2].
        var values = new List<long>(n + n + 1);

        for (int i = 0; i < n - 1; i++)
        {
            values.Add(pref[i]);
        }

        values.Add(0);

        for (int i = 0; i < n; i++)
        {
            if (nums[i] < 0)
            {
                values.Add(nums[i]);
            }
        }

        values.Sort();

        // Remove duplicates from the sorted list.
        var compressed = new List<long>(values.Count);
        foreach (long v in values)
        {
            if (compressed.Count == 0 || compressed[^1] != v)
            {
                compressed.Add(v);
            }
        }

        // Helper local function:
        // returns 1-based compressed index for an exact value known to exist in compressed.
        int GetIndex(long value)
        {
            int pos = compressed.BinarySearch(value);
            return pos + 1;
        }

        // Fenwick tree (Binary Indexed Tree) stores frequencies of prefix sums currently in the active range.
        var bit = new Fenwick(compressed.Count);

        int bestGain = 0;

        // Step 3:
        // Sweep j from right to left.
        //
        // Invariant before processing position j:
        //   the Fenwick tree contains prefix sums pref[i] for all i in [j .. n-2]
        //
        // We achieve this by:
        //   at each step, first insert pref[j] if j <= n-2,
        //   then evaluate moving nums[j] to the end.
        //
        // Why only up to n-2?
        // Because after moving nums[j] to the end, the final prefix at index n-1 is the total sum,
        // which does not change. So only prefixes up to n-2 can be affected.
        for (int j = n - 1; j >= 0; j--)
        {
            if (j <= n - 2)
            {
                bit.Add(GetIndex(pref[j]), 1);
            }

            // Only moving a negative value to the end can help.
            if (nums[j] < 0)
            {
                // Let delta = -nums[j] > 0.
                // A prefix pref[i] in the affected range becomes newly positive iff:
                //   pref[i] <= 0
                //   pref[i] + delta > 0
                // <=> pref[i] > -delta
                // <=> pref[i] > nums[j]
                //
                // Combined:
                //   nums[j] < pref[i] <= 0
                //
                // Count = count(pref <= 0) - count(pref <= nums[j])
                int countLeZero = bit.Sum(GetIndex(0));
                int countLeNumsJ = bit.Sum(GetIndex(nums[j]));
                int gain = countLeZero - countLeNumsJ;

                if (gain > bestGain)
                {
                    bestGain = gain;
                }
            }
        }

        return baseScore + bestGain;
    }

    // Standard Fenwick tree for prefix frequency sums.
    // This is a compact and efficient data structure for:
    // - point updates
    // - prefix sum queries
    private class Fenwick
    {
        private readonly int[] tree;

        public Fenwick(int size)
        {
            tree = new int[size + 2];
        }

        public void Add(int index, int delta)
        {
            while (index < tree.Length)
            {
                tree[index] += delta;
                index += index & -index;
            }
        }

        public int Sum(int index)
        {
            int result = 0;
            while (index > 0)
            {
                result += tree[index];
                index -= index & -index;
            }
            return result;
        }
    }
}

// Demo code
var solution = new Solution();

int[] nums1 = { 4, -5, 2, 1 };
int result1 = solution.MaxPrefixScoreAfterOneRelocation(nums1);
Console.WriteLine(result1); // Expected: 4

int[] nums2 = { -2, 3, -1, 2 };
int result2 = solution.MaxPrefixScoreAfterOneRelocation(nums2);
Console.WriteLine(result2); // Expected: 3

int[] nums3 = { 1, 2, 3 };
int result3 = solution.MaxPrefixScoreAfterOneRelocation(nums3);
Console.WriteLine(result3); // Expected: 3

int[] nums4 = { -5 };
int result4 = solution.MaxPrefixScoreAfterOneRelocation(nums4);
Console.WriteLine(result4); // Expected: 0

int[] nums5 = { 5, -10, 10 };
int result5 = solution.MaxPrefixScoreAfterOneRelocation(nums5);
Console.WriteLine(result5); // Expected: 3