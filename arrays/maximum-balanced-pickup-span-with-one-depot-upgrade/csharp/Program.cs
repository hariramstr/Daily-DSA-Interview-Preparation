/*
Title: Maximum Balanced Pickup Span with One Depot Upgrade
Difficulty: Hard
Topic: Arrays

Problem Description:
A logistics company records the pickup capacity of depots along a highway in an integer array capacities,
where capacities[i] is the number of packages depot i can process in one hour.

A contiguous group of depots is called balanced if the difference between the maximum and minimum capacity
inside that group is at most limit.

The company is allowed to perform at most one upgrade operation on a single depot inside the chosen group.
In one upgrade, you may increase that depot's capacity by any value from 0 up to upgrade.
The upgraded value is used only for checking whether the chosen group is balanced.
You may also choose not to use the upgrade.

Return the maximum possible length of a contiguous balanced group after applying at most one such upgrade.

Important notes:
- You may upgrade only one element in the chosen subarray.
- Capacities can only be increased, never decreased.
- The final subarray must satisfy max(subarray) - min(subarray) <= limit after the optional upgrade.
- Because only increases are allowed, the upgraded depot may help by raising a too-small value,
  but it cannot reduce a too-large value.

Constraints:
- 1 <= capacities.length <= 2 * 10^5
- 0 <= capacities[i] <= 10^9
- 0 <= limit <= 10^9
- 0 <= upgrade <= 10^9

Important correction about Example 2:
The statement text contains a contradiction. For capacities = [8, 2, 2, 2, 8], limit = 3, upgrade = 6,
a length-4 window is NOT valid because one upgrade can only change one low value, while multiple low values
remain and still keep the spread too large. The true optimal answer is 3 by taking [2, 2, 2].

Key observation used by the algorithm:
For a window to become valid after at most one increase:
1) The maximum value in the window cannot change, because we are only allowed to increase values.
2) Therefore every element except possibly one must already be at least (max - limit).
3) If there is exactly one "too small" element x < max - limit, then it is fixable iff
   x + upgrade >= max - limit.
4) If there are two or more too-small elements, one upgrade is not enough.

So for any fixed window, validity depends on:
- current maximum M
- threshold T = M - limit
- how many elements are < T
- if there is one such element, whether it can be raised to T using the allowed upgrade

We maintain a sliding window and support these checks efficiently with:
- a monotonic deque for the window maximum
- coordinate compression + Fenwick trees to count values and sum values by value rank

The Fenwick trees let us answer for the current threshold T:
- count of elements < T
- among those, the minimum value (equivalently the largest value below T if count is 1)
  via order statistics on the compressed coordinates

This yields an O(n log n) solution.
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    /*
    Time Complexity:
    O(n log n)
    - Each array element enters and leaves the sliding window once.
    - Each window adjustment performs O(log n) Fenwick operations.
    - Coordinate compression also costs O(n log n).

    Space Complexity:
    O(n)
    - Compressed values
    - Fenwick trees
    - Monotonic deque
    */
    public int MaximumBalancedPickupSpan(int[] capacities, int limit, int upgrade)
    {
        int n = capacities.Length;
        if (n == 0) return 0;

        // ------------------------------------------------------------
        // STEP 1: Coordinate compression
        // ------------------------------------------------------------
        // Why do we need this?
        // The capacities can be as large as 1e9, which is too large to use directly
        // as Fenwick tree indices.
        //
        // Coordinate compression maps every distinct capacity value to a small index
        // in the range [1..distinctCount].
        //
        // We only need ordering between values, not their original large magnitudes,
        // so compression is perfect here.
        // ------------------------------------------------------------
        int[] sortedDistinct = capacities.Distinct().OrderBy(x => x).ToArray();
        var indexByValue = new Dictionary<int, int>(sortedDistinct.Length);
        for (int i = 0; i < sortedDistinct.Length; i++)
        {
            indexByValue[sortedDistinct[i]] = i + 1; // Fenwick tree uses 1-based indexing
        }

        // ------------------------------------------------------------
        // STEP 2: Data structures for the sliding window
        // ------------------------------------------------------------
        // maxDeque:
        //   A monotonic decreasing deque of indices.
        //   The front always stores the index of the maximum value in the current window.
        //
        // countFenwick:
        //   Stores how many times each compressed value appears in the current window.
        //
        // Why only counts, not sums?
        //   To validate a window, we need:
        //   - how many values are below threshold T
        //   - if there is exactly one such value, what that value is
        //
        // Count Fenwick can answer:
        //   - count of values < T
        //   - find the k-th smallest value in the current window by count
        //
        // If there is exactly one value below T, then that unique value is simply
        // the 1st smallest among the values < T, and we can recover it by order statistic.
        // ------------------------------------------------------------
        var countFenwick = new Fenwick(sortedDistinct.Length);
        var maxDeque = new LinkedList<int>();

        int left = 0;
        int best = 0;

        // ------------------------------------------------------------
        // STEP 3: Expand the window with the right pointer
        // ------------------------------------------------------------
        // Standard sliding window pattern:
        // - Add capacities[right]
        // - While the window is invalid, move left forward
        // - Track the maximum valid window length
        // ------------------------------------------------------------
        for (int right = 0; right < n; right++)
        {
            int value = capacities[right];
            int compressedIndex = indexByValue[value];

            // Add current value into Fenwick count structure.
            countFenwick.Add(compressedIndex, 1);

            // Maintain decreasing deque for maximum.
            // Remove smaller values from the back because they can never become
            // the maximum while the current larger value remains in the window.
            while (maxDeque.Count > 0 && capacities[maxDeque.Last!.Value] <= value)
            {
                maxDeque.RemoveLast();
            }
            maxDeque.AddLast(right);

            // --------------------------------------------------------
            // Shrink from the left until the current window becomes valid.
            // --------------------------------------------------------
            while (!IsWindowValid(capacities, limit, upgrade, left, right, maxDeque, countFenwick, sortedDistinct))
            {
                int removeValue = capacities[left];
                int removeCompressedIndex = indexByValue[removeValue];

                // Remove outgoing value from Fenwick.
                countFenwick.Add(removeCompressedIndex, -1);

                // If the outgoing index is currently the maximum at the deque front,
                // remove it from the deque as well.
                if (maxDeque.Count > 0 && maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                left++;
            }

            // At this point [left..right] is valid.
            best = Math.Max(best, right - left + 1);
        }

        return best;
    }

    private bool IsWindowValid(
        int[] capacities,
        int limit,
        int upgrade,
        int left,
        int right,
        LinkedList<int> maxDeque,
        Fenwick countFenwick,
        int[] sortedDistinct)
    {
        // ------------------------------------------------------------
        // This helper checks whether the current window [left..right]
        // can be made balanced using at most one increase.
        //
        // Let M = maximum in the window.
        // Since we can only increase values, M cannot decrease.
        // Therefore every final value must lie in [M - limit, M].
        //
        // So the only possible problem is values that are too small:
        // values < T where T = M - limit.
        //
        // Cases:
        // 1) No values < T:
        //    Already valid without any upgrade.
        //
        // 2) Exactly one value x < T:
        //    Valid iff x + upgrade >= T.
        //
        // 3) Two or more values < T:
        //    Invalid, because one upgrade can only fix one depot.
        // ------------------------------------------------------------

        int currentMax = capacities[maxDeque.First!.Value];
        long threshold = (long)currentMax - limit;

        // If threshold is <= smallest possible capacity, then no value can be below it
        // unless capacities are negative, which they are not. But we still handle it
        // generically using Fenwick queries.
        //
        // We need the number of values strictly less than threshold.
        // Since our compressed coordinates are based on actual array values,
        // we first find how many distinct values are < threshold using binary search.
        int distinctLessThanThreshold = LowerBound(sortedDistinct, threshold);

        // Fenwick indices are 1-based, and LowerBound returns a count in [0..m].
        // So querying prefix(distinctLessThanThreshold) gives count of window values < threshold.
        int tooSmallCount = countFenwick.Query(distinctLessThanThreshold);

        if (tooSmallCount == 0)
        {
            // Every value is already at least currentMax - limit.
            // Therefore max - min <= limit already holds.
            return true;
        }

        if (tooSmallCount >= 2)
        {
            // More than one value is too small.
            // One upgrade cannot fix multiple low depots.
            return false;
        }

        // Exactly one value is below threshold.
        // We must identify that value and check whether upgrade can raise it enough.
        //
        // Since there is exactly one value among all values < threshold,
        // it is the 1st smallest value in that subset.
        // In Fenwick terms, it is the value at global order "tooSmallCount" = 1
        // within the prefix of values < threshold.
        //
        // More generally, the index of the last element in that prefix is:
        // prefix count = 1
        // so FindByPrefix(1) returns the compressed index of that unique low value.
        int lowValueCompressedIndex = countFenwick.FindByPrefix(1);
        int lowValue = sortedDistinct[lowValueCompressedIndex - 1];

        return (long)lowValue + upgrade >= threshold;
    }

    private int LowerBound(int[] arr, long target)
    {
        int lo = 0;
        int hi = arr.Length;
        while (lo < hi)
        {
            int mid = lo + ((hi - lo) >> 1);
            if ((long)arr[mid] < target)
            {
                lo = mid + 1;
            }
            else
            {
                hi = mid;
            }
        }
        return lo;
    }

    private class Fenwick
    {
        private readonly int[] tree;
        private readonly int size;

        public Fenwick(int n)
        {
            size = n;
            tree = new int[n + 1];
        }

        public void Add(int index, int delta)
        {
            while (index <= size)
            {
                tree[index] += delta;
                index += index & -index;
            }
        }

        public int Query(int index)
        {
            int sum = 0;
            while (index > 0)
            {
                sum += tree[index];
                index -= index & -index;
            }
            return sum;
        }

        public int QueryRange(int left, int right)
        {
            if (right < left) return 0;
            return Query(right) - Query(left - 1);
        }

        // Finds the smallest index idx such that prefix sum >= target.
        // Assumes 1 <= target <= total count in the Fenwick tree.
        public int FindByPrefix(int target)
        {
            int idx = 0;
            int bitMask = HighestPowerOfTwoAtMost(size);

            while (bitMask != 0)
            {
                int next = idx + bitMask;
                if (next <= size && tree[next] < target)
                {
                    idx = next;
                    target -= tree[next];
                }
                bitMask >>= 1;
            }

            return idx + 1;
        }

        private int HighestPowerOfTwoAtMost(int x)
        {
            int p = 1;
            while ((p << 1) <= x) p <<= 1;
            return p;
        }
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] capacities1 = { 5, 3, 6, 4, 7 };
int limit1 = 2;
int upgrade1 = 2;
int result1 = solution.MaximumBalancedPickupSpan(capacities1, limit1, upgrade1);
Console.WriteLine(result1); // Expected: 4

// Example 2 (corrected expected answer is 3)
int[] capacities2 = { 8, 2, 2, 2, 8 };
int limit2 = 3;
int upgrade2 = 6;
int result2 = solution.MaximumBalancedPickupSpan(capacities2, limit2, upgrade2);
Console.WriteLine(result2); // Expected: 3

// Additional quick checks
int[] capacities3 = { 1 };
Console.WriteLine(solution.MaximumBalancedPickupSpan(capacities3, 0, 0)); // Expected: 1

int[] capacities4 = { 1, 10 };
Console.WriteLine(solution.MaximumBalancedPickupSpan(capacities4, 3, 6)); // Expected: 1

int[] capacities5 = { 4, 1, 4 };
Console.WriteLine(solution.MaximumBalancedPickupSpan(capacities5, 0, 3)); // Expected: 3