/*
Title: Longest Toll Segment Within Daily Pass Limit
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A driver is traveling along a highway divided into consecutive road segments. For each segment i, you are given tolls[i], the toll cost required to drive through that segment. The driver also owns a special daily pass that can fully waive the toll on at most k segments within one continuous part of the trip.

Your task is to find the maximum number of consecutive segments the driver can travel such that, after choosing at most k segments in that interval to waive with the pass, the total amount actually paid is no more than budget.

Formally, for any contiguous subarray tolls[l..r], the driver may select up to k elements in that subarray and treat their cost as 0. The remaining segment costs must sum to at most budget. Return the largest possible length of such a subarray.

This models a realistic route-planning system where a subscription can cover only a limited number of toll booths in a chosen stretch, and the driver wants the longest uninterrupted drive without exceeding a spending cap.

Constraints:
- 1 <= tolls.length <= 200000
- 0 <= tolls[i] <= 1000000000
- 0 <= budget <= 100000000000000
- 0 <= k <= tolls.length
- The answer must be computed efficiently for large inputs.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n log n)
    Explanation:
    - We process each toll when it enters the sliding window and when it leaves the window.
    - Each add/remove operation updates balanced tree structures in O(log n).
    - The left and right pointers each move at most n times.
    - Therefore total complexity is O(n log n).

    Space Complexity:
    O(n)
    Explanation:
    - In the worst case, the sliding window may contain all elements.
    - Our balanced tree structures store the elements currently in the window.
    */

    public int LongestTollSegmentWithinBudget(int[] tolls, long budget, int k)
    {
        // We use a classic sliding window:
        // - "right" expands the window by adding a new segment.
        // - If the window becomes invalid, "left" shrinks it until it becomes valid again.
        //
        // The challenge is checking whether a window is valid:
        // For the current window, we may waive up to k segments.
        // To minimize the amount paid, we should waive the k most expensive tolls in the window.
        //
        // So for any window:
        // paid amount = total sum of window - sum of largest k values in the window
        //
        // Therefore, we need a data structure that supports:
        // 1. Add a value
        // 2. Remove a value
        // 3. Quickly know the sum of the largest k values
        //
        // We maintain two multisets:
        // - "large": contains the currently waived values, i.e. the largest up to k values
        // - "small": contains all remaining values
        //
        // We also maintain:
        // - totalSum: sum of all values in the current window
        // - largeSum: sum of values inside "large"
        //
        // Then:
        // paid amount = totalSum - largeSum
        //
        // Important detail:
        // "large" should always contain the largest possible values from the window,
        // and its size should be min(k, window size).

        var small = new MultiSet();
        var large = new MultiSet();

        long totalSum = 0;
        long largeSum = 0;

        int left = 0;
        int best = 0;

        void Rebalance()
        {
            // This helper function restores the required invariant:
            //
            // Invariant A:
            // large.Count == min(k, currentWindowSize)
            //
            // Invariant B:
            // Every value in "large" is >= every value in "small"
            //
            // Why do we need these?
            // Because "large" is supposed to represent the waived segments,
            // and to minimize paid cost we must waive the largest tolls.

            // Step 1:
            // If "large" has too many elements, move the smallest waived value back to "small".
            //
            // Why the smallest from "large"?
            // Because if we must reduce the number of waived segments,
            // the least valuable waiver should be removed first.
            while (large.Count > k)
            {
                int value = large.MinKey;
                large.Remove(value);
                largeSum -= value;
                small.Add(value);
            }

            // Step 2:
            // If "large" has too few elements and "small" has available elements,
            // move the largest value from "small" into "large".
            //
            // Why the largest from "small"?
            // Because waived segments should be the most expensive ones.
            while (large.Count < k && small.Count > 0)
            {
                int value = small.MaxKey;
                small.Remove(value);
                large.Add(value);
                largeSum += value;
            }

            // Step 3:
            // Even if sizes are correct, the partition may still be wrong.
            // Example:
            // small contains 100, large contains 50
            // This violates the rule that "large" must contain the largest values.
            //
            // So while the largest value in "small" is bigger than the smallest value in "large",
            // swap them.
            while (small.Count > 0 && large.Count > 0 && small.MaxKey > large.MinKey)
            {
                int fromSmall = small.MaxKey;
                int fromLarge = large.MinKey;

                small.Remove(fromSmall);
                large.Remove(fromLarge);

                small.Add(fromLarge);
                large.Add(fromSmall);

                largeSum += (long)fromSmall - fromLarge;
            }
        }

        void AddValue(int value)
        {
            // We are extending the window to the right, so a new toll enters the window.
            // First, update the total sum of the window.
            totalSum += value;

            // We tentatively place the new value into "large" if:
            // - we still have room to waive more segments, or
            // - it is large enough to deserve being among the waived values.
            //
            // A simple and safe strategy:
            // - If "large" currently has fewer than k elements, put it there.
            // - Otherwise compare against the smallest waived value.
            //   If the new value is bigger, it should become waived.
            //   Otherwise it belongs to "small".
            if (k > 0 && large.Count < k)
            {
                large.Add(value);
                largeSum += value;
            }
            else if (k > 0 && large.Count > 0 && value > large.MinKey)
            {
                large.Add(value);
                largeSum += value;
            }
            else
            {
                small.Add(value);
            }

            // After insertion, the partition may violate size/order rules.
            // Rebalance fixes that.
            Rebalance();
        }

        void RemoveValue(int value)
        {
            // We are shrinking the window from the left, so one toll leaves the window.
            // First, update the total sum.
            totalSum -= value;

            // We must remove the value from whichever multiset currently contains it.
            //
            // Because duplicates are possible, we use multiset counts.
            if (large.Contains(value))
            {
                large.Remove(value);
                largeSum -= value;
            }
            else
            {
                small.Remove(value);
            }

            // After removal, the partition may again violate size/order rules.
            Rebalance();
        }

        for (int right = 0; right < tolls.Length; right++)
        {
            // Expand the window by including tolls[right].
            AddValue(tolls[right]);

            // Compute the amount actually paid in the current window:
            // total sum minus the sum of waived tolls.
            //
            // If this exceeds the budget, the window is invalid and must be shrunk.
            while (left <= right && totalSum - largeSum > budget)
            {
                RemoveValue(tolls[left]);
                left++;
            }

            // At this point the window [left..right] is valid.
            // Update the best answer with its length.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }

    private class MultiSet
    {
        // We need:
        // - duplicate support
        // - ability to get minimum key
        // - ability to get maximum key
        // - O(log n) add/remove/contains
        //
        // SortedDictionary gives ordered keys in O(log n) updates.
        // We store counts to simulate a multiset.

        private readonly SortedDictionary<int, int> _map = new();
        public int Count { get; private set; }

        public void Add(int value)
        {
            if (_map.TryGetValue(value, out int count))
            {
                _map[value] = count + 1;
            }
            else
            {
                _map[value] = 1;
            }

            Count++;
        }

        public void Remove(int value)
        {
            int count = _map[value];
            if (count == 1)
            {
                _map.Remove(value);
            }
            else
            {
                _map[value] = count - 1;
            }

            Count--;
        }

        public bool Contains(int value) => _map.ContainsKey(value);

        public int MinKey
        {
            get
            {
                foreach (var pair in _map)
                {
                    return pair.Key;
                }

                throw new InvalidOperationException("Multiset is empty.");
            }
        }

        public int MaxKey
        {
            get
            {
                foreach (var pair in _map.Reverse())
                {
                    return pair.Key;
                }

                throw new InvalidOperationException("Multiset is empty.");
            }
        }
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] tolls1 = { 7, 2, 5, 10, 3 };
long budget1 = 10;
int k1 = 1;
int result1 = solution.LongestTollSegmentWithinBudget(tolls1, budget1, k1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] tolls2 = { 4, 8, 1, 9, 2, 6 };
long budget2 = 11;
int k2 = 2;
int result2 = solution.LongestTollSegmentWithinBudget(tolls2, budget2, k2);
Console.WriteLine(result2); // Expected: 5

// Additional quick checks
int[] tolls3 = { 5, 5, 5 };
long budget3 = 0;
int k3 = 3;
int result3 = solution.LongestTollSegmentWithinBudget(tolls3, budget3, k3);
Console.WriteLine(result3); // Expected: 3

int[] tolls4 = { 1, 2, 3, 4 };
long budget4 = 3;
int k4 = 0;
int result4 = solution.LongestTollSegmentWithinBudget(tolls4, budget4, k4);
Console.WriteLine(result4); // Expected: 2