/*
 * Title: Zigzag Array Reconstruction
 * Difficulty: Easy
 * Topic: Arrays
 *
 * Problem Description:
 * Given an integer array `nums`, rearrange its elements so that the resulting array
 * follows a zigzag pattern. A zigzag pattern means every element at an even index
 * is less than or equal to its neighbors, and every element at an odd index is
 * greater than or equal to its neighbors. Formally:
 *
 *   nums[0] <= nums[1] >= nums[2] <= nums[3] >= nums[4] ...
 *
 * Return the rearranged array. If multiple valid answers exist, return any of them.
 *
 * Note: You may not sort the array before rearranging; instead, perform the
 * rearrangement in a single pass by swapping adjacent elements where necessary.
 *
 * Constraints:
 *   - 1 <= nums.length <= 10^4
 *   - 0 <= nums[i] <= 10^5
 *   - It is guaranteed that a valid zigzag arrangement always exists for the given input.
 *
 * Example 1:
 *   Input:  nums = [4, 3, 7, 8, 6, 2, 1]
 *   Output: [3, 7, 4, 8, 2, 6, 1]
 *   Explanation: 3 <= 7 >= 4 <= 8 >= 2 <= 6 >= 1
 *
 * Example 2:
 *   Input:  nums = [1, 2, 3]
 *   Output: [1, 3, 2]
 *   Explanation: 1 <= 3 >= 2
 */

using System;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the zigzag rearrangement algorithm
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Rearranges the array in-place so it satisfies the zigzag pattern:
    ///   nums[0] <= nums[1] >= nums[2] <= nums[3] >= nums[4] ...
    ///
    /// Time Complexity:  O(n)  — we make exactly one pass through the array.
    /// Space Complexity: O(1)  — we only use a single temporary variable for swapping;
    ///                           no extra data structures are needed.
    /// </summary>
    public int[] ZigzagArray(int[] nums)
    {
        // ── Step 1: Handle the trivial edge case ──────────────────────────────
        // If the array has 0 or 1 element, it is already in zigzag form
        // (there are no adjacent pairs to compare), so we return immediately.
        if (nums.Length <= 1)
            return nums;

        // ── Step 2: Understand the pattern we need to enforce ─────────────────
        //
        // Index:   0   1   2   3   4   5  ...
        // Rule:   <=  >=  <=  >=  <=  >= ...
        //
        // In other words:
        //   • At an EVEN index i, nums[i] should be <= nums[i+1]
        //     (the element at an even position must be a "valley").
        //   • At an ODD  index i, nums[i] should be >= nums[i+1]
        //     (the element at an odd  position must be a "peak").
        //
        // We can capture this with a single boolean flag:
        //   shouldBeLess = true  when we are at an even index (want nums[i] <= nums[i+1])
        //   shouldBeLess = false when we are at an odd  index (want nums[i] >= nums[i+1])

        // ── Step 3: Single-pass swap loop ─────────────────────────────────────
        // We iterate over every adjacent pair (i, i+1).
        // At each step we check whether the current pair violates the required
        // relationship.  If it does, we swap the two elements to fix it.
        //
        // KEY INSIGHT: swapping nums[i] and nums[i+1] only affects the relationship
        // between those two elements.  It does NOT break the relationship between
        // nums[i-1] and nums[i] that we already fixed in the previous iteration,
        // because after a swap the element that moves to position i is the one that
        // was already compared (and correctly placed relative to i-1) in the
        // previous step — or, more precisely, the swap only makes the value at
        // position i smaller (for a valley) or larger (for a peak), which can only
        // help the already-satisfied constraint with the left neighbor.

        for (int i = 0; i < nums.Length - 1; i++)
        {
            // Determine what relationship we need between nums[i] and nums[i+1].
            // When i is even  → we need nums[i] <= nums[i+1]  (shouldBeLess = true)
            // When i is odd   → we need nums[i] >= nums[i+1]  (shouldBeLess = false)
            bool shouldBeLess = (i % 2 == 0);

            // ── Step 3a: Check if the current pair violates the required rule ──
            if (shouldBeLess)
            {
                // We are at an even index: we need nums[i] <= nums[i+1].
                // If nums[i] > nums[i+1], the pair is in the wrong order → swap.
                if (nums[i] > nums[i + 1])
                {
                    // Swap nums[i] and nums[i+1] using a temporary variable.
                    // After the swap, nums[i] < nums[i+1], satisfying the valley rule.
                    int temp = nums[i];
                    nums[i] = nums[i + 1];
                    nums[i + 1] = temp;
                }
                // If nums[i] <= nums[i+1] already, no action is needed.
            }
            else
            {
                // We are at an odd index: we need nums[i] >= nums[i+1].
                // If nums[i] < nums[i+1], the pair is in the wrong order → swap.
                if (nums[i] < nums[i + 1])
                {
                    // Swap nums[i] and nums[i+1].
                    // After the swap, nums[i] > nums[i+1], satisfying the peak rule.
                    int temp = nums[i];
                    nums[i] = nums[i + 1];
                    nums[i + 1] = temp;
                }
                // If nums[i] >= nums[i+1] already, no action is needed.
            }
        }

        // ── Step 4: Return the modified array ─────────────────────────────────
        // The array has been rearranged in-place and now satisfies the zigzag
        // pattern.  We return the same array reference.
        return nums;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helper: verify that an array truly satisfies the zigzag property
// ─────────────────────────────────────────────────────────────────────────────
static bool IsZigzag(int[] arr)
{
    for (int i = 0; i < arr.Length - 1; i++)
    {
        if (i % 2 == 0)
        {
            // Even index: must be <= next element
            if (arr[i] > arr[i + 1]) return false;
        }
        else
        {
            // Odd index: must be >= next element
            if (arr[i] < arr[i + 1]) return false;
        }
    }
    return true;
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────
var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
int[] example1 = { 4, 3, 7, 8, 6, 2, 1 };
Console.WriteLine("=== Example 1 ===");
Console.WriteLine("Input:    " + string.Join(", ", example1));

int[] result1 = solution.ZigzagArray(example1);
Console.WriteLine("Output:   " + string.Join(", ", result1));
Console.WriteLine("Valid zigzag? " + IsZigzag(result1));
// Expected: some valid zigzag, e.g. [3, 7, 4, 8, 2, 6, 1]
// Let's trace manually:
//   i=0 (even): 4 vs 3 → 4>3 → swap → [3,4,7,8,6,2,1]
//   i=1 (odd):  4 vs 7 → 4<7 → swap → [3,7,4,8,6,2,1]
//   i=2 (even): 4 vs 8 → 4<=8 → no swap → [3,7,4,8,6,2,1]
//   i=3 (odd):  8 vs 6 → 8>=6 → no swap → [3,7,4,8,6,2,1]
//   i=4 (even): 6 vs 2 → 6>2  → swap → [3,7,4,8,2,6,1]
//   i=5 (odd):  6 vs 1 → 6>=1 → no swap → [3,7,4,8,2,6,1]
// Result: [3,7,4,8,2,6,1]  ✓  3<=7>=4<=8>=2<=6>=1

Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
int[] example2 = { 1, 2, 3 };
Console.WriteLine("=== Example 2 ===");
Console.WriteLine("Input:    " + string.Join(", ", example2));

int[] result2 = solution.ZigzagArray(example2);
Console.WriteLine("Output:   " + string.Join(", ", result2));
Console.WriteLine("Valid zigzag? " + IsZigzag(result2));
// Expected: [1, 3, 2]
// Trace:
//   i=0 (even): 1 vs 2 → 1<=2 → no swap → [1,2,3]
//   i=1 (odd):  2 vs 3 → 2<3  → swap → [1,3,2]
// Result: [1,3,2]  ✓  1<=3>=2

Console.WriteLine();

// ── Additional edge cases ─────────────────────────────────────────────────────

// Single element
int[] single = { 42 };
Console.WriteLine("=== Single Element ===");
Console.WriteLine("Input:    " + string.Join(", ", single));
int[] resultSingle = solution.ZigzagArray(single);
Console.WriteLine("Output:   " + string.Join(", ", resultSingle));
Console.WriteLine("Valid zigzag? " + IsZigzag(resultSingle));

Console.WriteLine();

// All same elements
int[] allSame = { 5, 5, 5, 5, 5 };
Console.WriteLine("=== All Same Elements ===");
Console.WriteLine("Input:    " + string.Join(", ", allSame));
int[] resultSame = solution.ZigzagArray(allSame);
Console.WriteLine("Output:   " + string.Join(", ", resultSame));
Console.WriteLine("Valid zigzag? " + IsZigzag(resultSame));
// All equal → every <= and >= is satisfied trivially.

Console.WriteLine();

// Already zigzag
int[] alreadyZigzag = { 1, 5, 2, 6, 3 };
Console.WriteLine("=== Already Zigzag ===");
Console.WriteLine("Input:    " + string.Join(", ", alreadyZigzag));
int[] resultZZ = solution.ZigzagArray(alreadyZigzag);
Console.WriteLine("Output:   " + string.Join(", ", resultZZ));
Console.WriteLine("Valid zigzag? " + IsZigzag(resultZZ));

Console.WriteLine();

// Descending order
int[] descending = { 5, 4, 3, 2, 1 };
Console.WriteLine("=== Descending Order ===");
Console.WriteLine("Input:    " + string.Join(", ", descending));
int[] resultDesc = solution.ZigzagArray(descending);
Console.WriteLine("Output:   " + string.Join(", ", resultDesc));
Console.WriteLine("Valid zigzag? " + IsZigzag(resultDesc));
// Trace:
//   i=0 (even): 5 vs 4 → 5>4 → swap → [4,5,3,2,1]
//   i=1 (odd):  5 vs 3 → 5>=3 → no swap → [4,5,3,2,1]
//   i=2 (even): 3 vs 2 → 3>2 → swap → [4,5,2,3,1]
//   i=3 (odd):  3 vs 1 → 3>=1 → no swap → [4,5,2,3,1]
// Result: [4,5,2,3,1]  ✓  4<=5>=2<=3>=1