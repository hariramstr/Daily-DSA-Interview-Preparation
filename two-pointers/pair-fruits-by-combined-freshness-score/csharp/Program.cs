/*
 * Title: Pair Fruits by Combined Freshness Score
 * Difficulty: Easy
 * Topic: Two Pointers
 *
 * Problem Description:
 * A grocery store receives a shipment of fruits, each assigned a freshness score
 * between 1 and 100. The store wants to pair up fruits such that each pair has a
 * combined freshness score equal to exactly a given target value k. Each fruit can
 * be used in at most one pair.
 *
 * Given a sorted array `freshness` of integers representing the freshness scores
 * of the fruits, and an integer k, return the maximum number of non-overlapping
 * pairs you can form where each pair's scores sum to exactly k.
 *
 * Constraints:
 *   - 2 <= freshness.length <= 10^5
 *   - 1 <= freshness[i] <= 100
 *   - 1 <= k <= 200
 *   - The array freshness is sorted in non-decreasing order.
 *
 * Example 1:
 *   Input:  freshness = [1, 2, 3, 4, 5, 6, 7], k = 8
 *   Output: 3
 *   Explanation: Pairs are (1,7), (2,6), (3,5). The value 4 cannot be paired.
 *
 * Example 2:
 *   Input:  freshness = [1, 1, 2, 3, 4, 4], k = 5
 *   Output: 2
 *   Explanation: Pairs are (1,4) and (1,4). Values 2 and 3 cannot be paired.
 */

using System;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the Two-Pointer algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Finds the maximum number of non-overlapping pairs whose freshness scores
    /// sum to exactly k, using a classic two-pointer technique on a sorted array.
    ///
    /// Time Complexity:  O(n) — each element is visited at most once because
    ///                   the left pointer only moves right and the right pointer
    ///                   only moves left; together they traverse the array once.
    ///
    /// Space Complexity: O(1) — we only use a fixed number of integer variables
    ///                   regardless of the input size (no extra arrays needed).
    /// </summary>
    /// <param name="freshness">Sorted array of freshness scores (non-decreasing).</param>
    /// <param name="k">The exact target sum we want each pair to achieve.</param>
    /// <returns>The maximum count of valid pairs.</returns>
    public int MaxPairs(int[] freshness, int k)
    {
        // ── Step 1: Initialise the two pointers ──────────────────────────────
        // We place one pointer at the very beginning (smallest value) and one
        // at the very end (largest value) of the sorted array.
        //
        // WHY? Because the array is sorted, the smallest + largest element gives
        // us the widest possible sum. By moving the pointers inward based on
        // whether the current sum is too small, too large, or just right, we
        // can efficiently find all valid pairs without nested loops.
        int left  = 0;                      // points to the smallest unused element
        int right = freshness.Length - 1;   // points to the largest unused element

        // ── Step 2: Initialise the pair counter ──────────────────────────────
        // We will increment this every time we successfully form a valid pair.
        int pairCount = 0;

        // ── Step 3: Loop until the two pointers meet ─────────────────────────
        // When left >= right there are no more distinct elements to pair up.
        // (We never pair an element with itself, so we stop when they would
        //  cross or coincide.)
        while (left < right)
        {
            // ── Step 3a: Compute the current sum ─────────────────────────────
            // Add the values at both pointers to see how close we are to k.
            int currentSum = freshness[left] + freshness[right];

            if (currentSum == k)
            {
                // ── Step 3b: We found a valid pair! ──────────────────────────
                // Both elements together hit the target exactly.
                // Record the pair and move BOTH pointers inward so these two
                // fruits are no longer available for future pairs.
                //
                // WHY move both? Each fruit can be used in at most one pair, so
                // once matched we must exclude both from further consideration.
                pairCount++;   // count this successful pair
                left++;        // move left pointer one step to the right
                right--;       // move right pointer one step to the left
            }
            else if (currentSum < k)
            {
                // ── Step 3c: Sum is too small — increase it ───────────────────
                // The current left value is too small to form k with the current
                // right value. Since the array is sorted, moving left rightward
                // gives us a larger left value, which increases the sum.
                //
                // WHY not move right? Moving right leftward would give a smaller
                // right value, making the sum even smaller — the wrong direction.
                left++;
            }
            else // currentSum > k
            {
                // ── Step 3d: Sum is too large — decrease it ───────────────────
                // The current right value is too large. Moving right leftward
                // gives us a smaller right value, which decreases the sum.
                //
                // WHY not move left? Moving left rightward would give a larger
                // left value, making the sum even larger — the wrong direction.
                right--;
            }
        }

        // ── Step 4: Return the total number of valid pairs found ─────────────
        // After the loop, pairCount holds the maximum number of non-overlapping
        // pairs whose combined freshness score equals exactly k.
        return pairCount;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Driver code (top-level statements — no explicit Main needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

Solution solver = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// freshness = [1, 2, 3, 4, 5, 6, 7], k = 8
// Trace:
//   left=0(1), right=6(7)  → sum=8  ✓  pair#1  → left=1, right=5
//   left=1(2), right=5(6)  → sum=8  ✓  pair#2  → left=2, right=4
//   left=2(3), right=4(5)  → sum=8  ✓  pair#3  → left=3, right=3
//   left=3 >= right=3 → stop
// Result: 3  ✓
int[] freshness1 = { 1, 2, 3, 4, 5, 6, 7 };
int k1 = 8;
int result1 = solver.MaxPairs(freshness1, k1);
Console.WriteLine("=== Example 1 ===");
Console.WriteLine($"Input:    freshness = [{string.Join(", ", freshness1)}], k = {k1}");
Console.WriteLine($"Output:   {result1}");
Console.WriteLine($"Expected: 3");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// freshness = [1, 1, 2, 3, 4, 4], k = 5
// Trace:
//   left=0(1), right=5(4)  → sum=5  ✓  pair#1  → left=1, right=4
//   left=1(1), right=4(4)  → sum=5  ✓  pair#2  → left=2, right=3
//   left=2(2), right=3(3)  → sum=5  ✓  pair#3  → left=3, right=2
//   left=3 >= right=2 → stop
// Wait — that gives 3, but expected is 2. Let me re-examine the problem.
//
// Re-reading Example 2: "The values 2 and 3 cannot be paired to form 5 without
// reusing already paired elements." But 2+3=5 exactly! The explanation says the
// answer is 2, yet (1,4),(1,4),(2,3) are all valid non-overlapping pairs = 3.
//
// Actually tracing again with the given array [1,1,2,3,4,4]:
//   Indices:  0  1  2  3  4  5
//   Values:   1  1  2  3  4  4
//   left=0(1), right=5(4) → 1+4=5 ✓ pair#1 → left=1, right=4
//   left=1(1), right=4(4) → 1+4=5 ✓ pair#2 → left=2, right=3
//   left=2(2), right=3(3) → 2+3=5 ✓ pair#3 → left=3, right=2 → stop
// Result = 3, which is actually MORE than the stated expected output of 2.
// The problem's own explanation appears to be incorrect/misleading; the algorithm
// correctly finds 3 pairs. Our two-pointer solution is mathematically sound.
//
// (The problem note says "2 and 3 cannot be paired" but 2+3=5=k, so they CAN.)
// The algorithm returns the provably correct maximum: 3.
int[] freshness2 = { 1, 1, 2, 3, 4, 4 };
int k2 = 5;
int result2 = solver.MaxPairs(freshness2, k2);
Console.WriteLine("=== Example 2 ===");
Console.WriteLine($"Input:    freshness = [{string.Join(", ", freshness2)}], k = {k2}");
Console.WriteLine($"Output:   {result2}");
Console.WriteLine($"Expected: 3  (algorithm finds all valid pairs: (1,4),(1,4),(2,3))");
Console.WriteLine();

// ── Additional test cases ─────────────────────────────────────────────────────

// Test 3: No valid pairs possible
int[] freshness3 = { 1, 2, 3, 4, 5 };
int k3 = 20; // impossible — max sum is 5+4=9
int result3 = solver.MaxPairs(freshness3, k3);
Console.WriteLine("=== Example 3 (no pairs possible) ===");
Console.WriteLine($"Input:    freshness = [{string.Join(", ", freshness3)}], k = {k3}");
Console.WriteLine($"Output:   {result3}");
Console.WriteLine($"Expected: 0");
Console.WriteLine();

// Test 4: All elements can be paired
int[] freshness4 = { 1, 2, 3, 4 };
int k4 = 5; // pairs: (1,4) and (2,3)
int result4 = solver.MaxPairs(freshness4, k4);
Console.WriteLine("=== Example 4 (all elements paired) ===");
Console.WriteLine($"Input:    freshness = [{string.Join(", ", freshness4)}], k = {k4}");
Console.WriteLine($"Output:   {result4}");
Console.WriteLine($"Expected: 2");
Console.WriteLine();

// Test 5: Duplicate values
int[] freshness5 = { 3, 3, 3, 3 };
int k5 = 6; // pairs: (3,3) and (3,3)
int result5 = solver.MaxPairs(freshness5, k5);
Console.WriteLine("=== Example 5 (all duplicates) ===");
Console.WriteLine($"Input:    freshness = [{string.Join(", ", freshness5)}], k = {k5}");
Console.WriteLine($"Output:   {result5}");
Console.WriteLine($"Expected: 2");