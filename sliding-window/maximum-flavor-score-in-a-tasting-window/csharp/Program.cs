/*
 * Maximum Flavor Score in a Tasting Window
 * =========================================
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A food critic is tasting dishes served in a sequence. Each dish has a flavor score,
 * and the critic can only evaluate a contiguous segment of dishes at a time. However,
 * due to palate fatigue, the critic's tasting window has a constraint: the difference
 * between the maximum and minimum flavor scores within the window must not exceed a
 * given threshold `t`.
 *
 * Given an integer array `flavors` where `flavors[i]` represents the flavor score of
 * the i-th dish, and an integer `t`, return the maximum number of dishes the critic
 * can taste in a single contiguous window such that the difference between the maximum
 * and minimum flavor scores in that window is at most `t`.
 *
 * Constraints:
 *   1 <= flavors.length <= 10^5
 *   0 <= flavors[i] <= 10^4
 *   0 <= t <= 10^4
 *
 * Example 1:
 *   Input:  flavors = [4, 8, 5, 1, 7, 9, 6], t = 4
 *   Output: 4
 *   Explanation: The window [5, 7, 9, 6] (indices 2..5 => values 5,1,7,9 — wait,
 *   let's recheck: index 2=5, 3=1, 4=7, 5=9 => diff=8 > 4.
 *   Actually indices 3..6 => 1,7,9,6 => max=9,min=1 => diff=8 > 4.
 *   Indices 2..5 => 5,1,7,9 => diff=8 > 4.
 *   The problem states [5,7,9,6] with max=9,min=5,diff=4 => indices 2,4,5,6 => NOT contiguous.
 *   Contiguous check: index 3=1,4=7,5=9,6=6 => max=9,min=1 => 8>4.
 *   index 2=5,3=1 => already diff=4 ok, add 4=7 => max=7,min=1 => 6>4. Shrink.
 *   Best valid window of length 4: indices 3..6 => 1,7,9,6 => 9-1=8 nope.
 *   Let me re-examine: flavors=[4,8,5,1,7,9,6]
 *     [4,8,5] => max=8,min=4,diff=4 <=4, length=3
 *     [8,5,1] => max=8,min=1,diff=7 >4
 *     [5,1,7] => max=7,min=1,diff=6 >4
 *     [1,7,9,6] => max=9,min=1,diff=8 >4
 *     [7,9,6] => max=9,min=6,diff=3 <=4, length=3
 *     [5,7,9,6] => indices 2,4,5,6 not contiguous
 *   Hmm, the problem says answer=4. Let me check all windows of length 4:
 *     [4,8,5,1] => 8-1=7 >4
 *     [8,5,1,7] => 8-1=7 >4
 *     [5,1,7,9] => 9-1=8 >4
 *     [1,7,9,6] => 9-1=8 >4
 *   No length-4 window works... but the problem says 4. The problem explanation itself
 *   seems contradictory. We'll trust the algorithm and produce the correct answer.
 *   Our algorithm will output 3 for Example 1 based on the actual data.
 *   (The problem statement's own explanation says "The longest valid window is length 4"
 *    but then traces show length 3. We implement correctly and trust the math.)
 *
 * Example 2:
 *   Input:  flavors = [10, 10, 10, 10], t = 0
 *   Output: 4
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Finds the maximum length of a contiguous subarray (window) such that
    /// the difference between the maximum and minimum values in that window
    /// is at most `t`.
    ///
    /// Time Complexity:  O(n)  — each element is added and removed from the
    ///                           deques at most once, so the total work is
    ///                           proportional to the number of elements.
    /// Space Complexity: O(n)  — in the worst case both deques hold all n
    ///                           indices (e.g., a strictly increasing array
    ///                           for the max-deque).
    /// </summary>
    public int MaxTastingWindow(int[] flavors, int t)
    {
        // ── Step 1: Handle the trivial edge case ──────────────────────────
        // If the array is empty there are no dishes to taste.
        if (flavors == null || flavors.Length == 0)
            return 0;

        int n = flavors.Length;

        // ── Step 2: Set up two monotonic deques ───────────────────────────
        //
        // WHY DEQUES?
        // A naïve approach would be: for every possible window [left, right],
        // compute max and min in O(n) time → overall O(n²). Too slow for n=10⁵.
        //
        // Instead we maintain two deques (double-ended queues) that always
        // store *indices* into the `flavors` array:
        //
        //   maxDeque  – indices in DECREASING order of their flavor values.
        //               The front (First) always holds the index of the
        //               CURRENT MAXIMUM in the window.
        //
        //   minDeque  – indices in INCREASING order of their flavor values.
        //               The front (First) always holds the index of the
        //               CURRENT MINIMUM in the window.
        //
        // Both deques are maintained so that:
        //   • All stored indices are within the current window [left, right].
        //   • The ordering invariant is preserved when we add a new element.
        //
        // This lets us query max and min in O(1) at any time.

        var maxDeque = new LinkedList<int>(); // front = index of max value
        var minDeque = new LinkedList<int>(); // front = index of min value

        // ── Step 3: Sliding-window pointers ───────────────────────────────
        // `left`  – the left boundary of the current window (inclusive).
        // `right` – the right boundary we are expanding (inclusive).
        // `best`  – the longest valid window length found so far.
        int left = 0;
        int best = 0;

        // ── Step 4: Expand the window one element at a time ───────────────
        for (int right = 0; right < n; right++)
        {
            // ── 4a: Maintain the MAX deque ─────────────────────────────
            // We want the front to always be the index of the largest value
            // in the current window.
            //
            // Before adding `right`, remove from the BACK of maxDeque any
            // indices whose flavor values are LESS THAN OR EQUAL TO the new
            // value. Those indices can never be the maximum while `right` is
            // in the window (because flavors[right] is at least as large and
            // `right` is further to the right, so it will stay in the window
            // longer).
            while (maxDeque.Count > 0 && flavors[maxDeque.Last.Value] <= flavors[right])
                maxDeque.RemoveLast();

            // Add the new index to the back of maxDeque.
            maxDeque.AddLast(right);

            // ── 4b: Maintain the MIN deque ─────────────────────────────
            // Mirror logic: remove from the BACK any indices whose flavor
            // values are GREATER THAN OR EQUAL TO the new value.
            while (minDeque.Count > 0 && flavors[minDeque.Last.Value] >= flavors[right])
                minDeque.RemoveLast();

            // Add the new index to the back of minDeque.
            minDeque.AddLast(right);

            // ── 4c: Shrink the window from the left if constraint violated ─
            // Now check whether the current window [left, right] is valid.
            // The current max = flavors[maxDeque.First.Value]
            // The current min = flavors[minDeque.First.Value]
            //
            // If their difference exceeds `t`, we must move `left` rightward
            // until the constraint is satisfied again.
            while (flavors[maxDeque.First.Value] - flavors[minDeque.First.Value] > t)
            {
                // Advance the left pointer.
                left++;

                // Evict stale indices from the fronts of both deques.
                // An index is "stale" if it has fallen outside the window,
                // i.e., its index < left.
                if (maxDeque.First.Value < left)
                    maxDeque.RemoveFirst();

                if (minDeque.First.Value < left)
                    minDeque.RemoveFirst();
            }

            // ── 4d: Update the best answer ─────────────────────────────
            // At this point [left, right] is the largest valid window ending
            // at `right`. Its length is (right - left + 1).
            int windowLength = right - left + 1;
            if (windowLength > best)
                best = windowLength;
        }

        // ── Step 5: Return the answer ─────────────────────────────────────
        return best;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solver = new Solution();

Console.WriteLine("=== Maximum Flavor Score in a Tasting Window ===");
Console.WriteLine();

// ── Example 1 ────────────────────────────────────────────────────────────────
// flavors = [4, 8, 5, 1, 7, 9, 6], t = 4
// Manual trace of all windows of length 3 that are valid:
//   [4, 8, 5] → max=8, min=4, diff=4 ✓  length 3
//   [7, 9, 6] → max=9, min=6, diff=3 ✓  length 3
// No contiguous window of length 4 satisfies diff ≤ 4 (verified above).
// Expected output: 3
int[] flavors1 = { 4, 8, 5, 1, 7, 9, 6 };
int t1 = 4;
int result1 = solver.MaxTastingWindow(flavors1, t1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  flavors = [{string.Join(", ", flavors1)}]");
Console.WriteLine($"  t       = {t1}");
Console.WriteLine($"  Output  = {result1}");   // Expected: 3
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// flavors = [10, 10, 10, 10], t = 0
// All values identical → diff always 0 ≤ 0.  Entire array is valid.
// Expected output: 4
int[] flavors2 = { 10, 10, 10, 10 };
int t2 = 0;
int result2 = solver.MaxTastingWindow(flavors2, t2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  flavors = [{string.Join(", ", flavors2)}]");
Console.WriteLine($"  t       = {t2}");
Console.WriteLine($"  Output  = {result2}");   // Expected: 4
Console.WriteLine();

// ── Extra Test 1: Single element ─────────────────────────────────────────────
int[] flavors3 = { 7 };
int t3 = 0;
int result3 = solver.MaxTastingWindow(flavors3, t3);
Console.WriteLine($"Extra Test 1 (single element):");
Console.WriteLine($"  flavors = [{string.Join(", ", flavors3)}]");
Console.WriteLine($"  t       = {t3}");
Console.WriteLine($"  Output  = {result3}");   // Expected: 1
Console.WriteLine();

// ── Extra Test 2: Strictly increasing, t = 2 ─────────────────────────────────
// flavors = [1, 2, 3, 4, 5], t = 2
// Best windows: [1,2,3], [2,3,4], [3,4,5] all have diff=2 ≤ 2, length=3.
int[] flavors4 = { 1, 2, 3, 4, 5 };
int t4 = 2;
int result4 = solver.MaxTastingWindow(flavors4, t4);
Console.WriteLine($"Extra Test 2 (strictly increasing, t=2):");
Console.WriteLine($"  flavors = [{string.Join(", ", flavors4)}]");
Console.WriteLine($"  t       = {t4}");
Console.WriteLine($"  Output  = {result4}");   // Expected: 3
Console.WriteLine();

// ── Extra Test 3: Large t (entire array always valid) ────────────────────────
int[] flavors5 = { 3, 1, 4, 1, 5, 9, 2, 6 };
int t5 = 10000;
int result5 = solver.MaxTastingWindow(flavors5, t5);
Console.WriteLine($"Extra Test 3 (t very large):");
Console.WriteLine($"  flavors = [{string.Join(", ", flavors5)}]");
Console.WriteLine($"  t       = {t5}");
Console.WriteLine($"  Output  = {result5}");   // Expected: 8 (entire array)
Console.WriteLine();

// ── Extra Test 4: t = 0 with mixed values ────────────────────────────────────
// Only runs of identical values count.
// flavors = [1, 1, 2, 2, 2, 1], t = 0
// Best run: [2,2,2] length 3.
int[] flavors6 = { 1, 1, 2, 2, 2, 1 };
int t6 = 0;
int result6 = solver.MaxTastingWindow(flavors6, t6);
Console.WriteLine($"Extra Test 4 (t=0, mixed values):");
Console.WriteLine($"  flavors = [{string.Join(", ", flavors6)}]");
Console.WriteLine($"  t       = {t6}");
Console.WriteLine($"  Output  = {result6}");   // Expected: 3