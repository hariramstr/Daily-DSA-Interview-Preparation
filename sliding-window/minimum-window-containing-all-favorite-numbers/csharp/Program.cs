/*
 * ============================================================
 * Title: Minimum Window Containing All Favorite Numbers
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an integer array `nums` and a list of distinct integers `favorites`.
 * Your task is to find the shortest contiguous subarray of `nums` that contains
 * all the integers in `favorites` (each favorite number must appear at least once).
 *
 * Return the length of the minimum such subarray.
 * If no such subarray exists, return -1.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^5
 * - 1 <= nums[i] <= 10^6
 * - 1 <= favorites.length <= 100
 * - 1 <= favorites[i] <= 10^6
 * - All values in favorites are distinct.
 * - It is guaranteed that favorites.length <= nums.length.
 * ============================================================
 */

using System;
using System.Collections.Generic;

// ============================================================
// SOLUTION CLASS
// ============================================================
public class Solution
{
    /// <summary>
    /// Finds the length of the minimum window subarray containing all favorites.
    ///
    /// APPROACH: Sliding Window (Two Pointers)
    ///
    /// The idea is to maintain a window [left, right] that expands to the right
    /// until it contains all favorites, then shrinks from the left to minimize
    /// the window size, then expands again, and so on.
    ///
    /// WHY SLIDING WINDOW?
    /// - We need a contiguous subarray → sliding window is perfect for this.
    /// - As we move right, we add elements; as we move left, we remove elements.
    /// - This avoids re-checking all subarrays (brute force O(n^2) or worse).
    ///
    /// Time Complexity:  O(n) — each element is added and removed at most once.
    /// Space Complexity: O(f) — where f = favorites.length, for the frequency maps.
    /// </summary>
    public int MinWindow(int[] nums, int[] favorites)
    {
        // -------------------------------------------------------
        // STEP 1: Build a "need" map — how many of each favorite
        //         we still need to include in our current window.
        //
        // WHY a HashSet first? We only care about favorites, not
        // every number in nums. Using a dictionary keyed by the
        // favorite values lets us do O(1) lookups.
        // -------------------------------------------------------
        var need = new Dictionary<int, int>();
        foreach (int fav in favorites)
        {
            // Each favorite must appear at least once, so count = 1.
            need[fav] = 1;
        }

        // -------------------------------------------------------
        // STEP 2: Check feasibility — if any favorite never appears
        //         in nums at all, we can immediately return -1.
        //
        // WHY do this early? It saves us from running the full
        // sliding window when the answer is impossible.
        // -------------------------------------------------------
        var numsSet = new HashSet<int>(nums);
        foreach (int fav in favorites)
        {
            if (!numsSet.Contains(fav))
            {
                // This favorite is completely absent from nums.
                return -1;
            }
        }

        // -------------------------------------------------------
        // STEP 3: Set up the sliding window variables.
        //
        // `window`   : tracks how many of each relevant number
        //              (only favorites) are currently in our window.
        //
        // `have`     : how many distinct favorites are currently
        //              satisfied (count in window >= 1).
        //
        // `required` : total number of distinct favorites we need
        //              to satisfy (= favorites.length).
        //
        // `minLen`   : the best (smallest) window length found so far.
        //
        // `left`     : the left pointer of our sliding window.
        // -------------------------------------------------------
        var window = new Dictionary<int, int>(); // counts of favorites inside current window
        int have = 0;                             // how many favorites are currently satisfied
        int required = favorites.Length;          // total favorites we must satisfy
        int minLen = int.MaxValue;                // best answer so far (start with "infinity")
        int left = 0;                             // left boundary of the window

        // We also need a fast lookup: is a number a favorite?
        // Using a HashSet for O(1) membership check.
        var favSet = new HashSet<int>(favorites);

        // -------------------------------------------------------
        // STEP 4: Expand the window by moving `right` pointer.
        //
        // For each position `right`, we try to include nums[right]
        // in our window and update our state accordingly.
        // -------------------------------------------------------
        for (int right = 0; right < nums.Length; right++)
        {
            int currentNum = nums[right];

            // -------------------------------------------------------
            // STEP 4a: Only track numbers that are favorites.
            //
            // WHY? Non-favorite numbers don't affect whether our
            // window is "valid" (contains all favorites). Ignoring
            // them keeps our `window` dictionary small and clean.
            // -------------------------------------------------------
            if (favSet.Contains(currentNum))
            {
                // Add this favorite to our window count.
                if (!window.ContainsKey(currentNum))
                    window[currentNum] = 0;

                window[currentNum]++;

                // -------------------------------------------------------
                // STEP 4b: Check if this favorite is now "satisfied".
                //
                // A favorite is satisfied when its count in the window
                // reaches exactly 1 (we need at least 1 of each).
                // We only increment `have` the first time it's satisfied
                // (i.e., when count goes from 0 to 1).
                // -------------------------------------------------------
                if (window[currentNum] == 1)
                {
                    // This favorite just became satisfied for the first time
                    // in the current window expansion.
                    have++;
                }
            }

            // -------------------------------------------------------
            // STEP 5: Shrink the window from the left as long as it
            //         remains valid (contains all favorites).
            //
            // WHY shrink? Once we have a valid window, we want the
            // SMALLEST valid window. We shrink by moving `left` right,
            // removing the leftmost element, until the window becomes
            // invalid again.
            // -------------------------------------------------------
            while (have == required)
            {
                // -------------------------------------------------------
                // STEP 5a: We have a valid window! Record its length.
                //
                // Current window spans indices [left, right], inclusive.
                // Length = right - left + 1.
                // -------------------------------------------------------
                int currentLen = right - left + 1;
                if (currentLen < minLen)
                {
                    minLen = currentLen; // Update best answer
                }

                // -------------------------------------------------------
                // STEP 5b: Try to shrink the window by removing nums[left].
                //
                // If nums[left] is a favorite, removing it might break
                // the validity of our window (if its count drops to 0).
                // -------------------------------------------------------
                int leftNum = nums[left];
                left++; // Move left pointer forward (shrink window)

                if (favSet.Contains(leftNum))
                {
                    // Decrease the count of this favorite in our window.
                    window[leftNum]--;

                    // -------------------------------------------------------
                    // STEP 5c: Check if removing this favorite broke validity.
                    //
                    // If count drops to 0, this favorite is no longer
                    // satisfied in the window → decrement `have`.
                    // The while loop condition (have == required) will then
                    // be false, so we stop shrinking and expand again.
                    // -------------------------------------------------------
                    if (window[leftNum] == 0)
                    {
                        have--; // This favorite is no longer covered
                    }
                }
            }
            // After the while loop, the window is no longer valid (or was never valid).
            // We continue expanding by incrementing `right` in the for loop.
        }

        // -------------------------------------------------------
        // STEP 6: Return the result.
        //
        // If minLen was never updated (stayed at int.MaxValue),
        // no valid window was found → return -1.
        // Otherwise, return the minimum window length found.
        // -------------------------------------------------------
        return minLen == int.MaxValue ? -1 : minLen;
    }
}

// ============================================================
// DEMO / TEST CODE (Top-Level Statements)
// ============================================================

var solution = new Solution();

Console.WriteLine("=== Minimum Window Containing All Favorite Numbers ===");
Console.WriteLine();

// -------------------------------------------------------
// Example 1:
// nums     = [4, 1, 3, 2, 1, 5, 3, 2]
// favorites = [1, 3, 2]
//
// Trace through the sliding window:
// right=0: num=4 (not fav), have=0
// right=1: num=1 (fav), window={1:1}, have=1
// right=2: num=3 (fav), window={1:1,3:1}, have=2
// right=3: num=2 (fav), window={1:1,3:1,2:1}, have=3 == required=3
//   → valid window [1..3], len=3, minLen=3
//   → shrink: remove nums[1]=1, window={1:0,3:1,2:1}, have=2, left=2
//   → have != required, stop shrinking
// right=4: num=1 (fav), window={1:1,3:1,2:1}, have=3 == required=3
//   → valid window [2..4], len=3, minLen=3
//   → shrink: remove nums[2]=3, window={1:1,3:0,2:1}, have=2, left=3
//   → have != required, stop shrinking
// right=5: num=5 (not fav), have=2
// right=6: num=3 (fav), window={1:1,3:1,2:1}, have=3 == required=3
//   → valid window [3..6], len=4, minLen stays 3
//   → shrink: remove nums[3]=2, window={1:1,3:1,2:0}, have=2, left=4
//   → have != required, stop shrinking
// right=7: num=2 (fav), window={1:1,3:1,2:1}, have=3 == required=3
//   → valid window [4..7], len=4, minLen stays 3
//   → shrink: remove nums[4]=1, window={1:0,3:1,2:1}, have=2, left=5
//   → have != required, stop shrinking
// Final minLen = 3 ✓
// -------------------------------------------------------
int[] nums1 = { 4, 1, 3, 2, 1, 5, 3, 2 };
int[] favorites1 = { 1, 3, 2 };
int result1 = solution.MinWindow(nums1, favorites1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  nums      = [{string.Join(", ", nums1)}]");
Console.WriteLine($"  favorites = [{string.Join(", ", favorites1)}]");
Console.WriteLine($"  Output    = {result1}");
Console.WriteLine($"  Expected  = 3");
Console.WriteLine($"  Correct?  = {result1 == 3}");
Console.WriteLine();

// -------------------------------------------------------
// Example 2:
// nums     = [7, 2, 5, 1, 8]
// favorites = [3, 6]
//
// Neither 3 nor 6 appears in nums → feasibility check fails → return -1 ✓
// -------------------------------------------------------
int[] nums2 = { 7, 2, 5, 1, 8 };
int[] favorites2 = { 3, 6 };
int result2 = solution.MinWindow(nums2, favorites2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  nums      = [{string.Join(", ", nums2)}]");
Console.WriteLine($"  favorites = [{string.Join(", ", favorites2)}]");
Console.WriteLine($"  Output    = {result2}");
Console.WriteLine($"  Expected  = -1");
Console.WriteLine($"  Correct?  = {result2 == -1}");
Console.WriteLine();

// -------------------------------------------------------
// Additional Test 3: Single favorite
// nums     = [5, 3, 1, 3, 5]
// favorites = [3]
// The minimum window containing 3 is just [3] → length 1
// -------------------------------------------------------
int[] nums3 = { 5, 3, 1, 3, 5 };
int[] favorites3 = { 3 };
int result3 = solution.MinWindow(nums3, favorites3);
Console.WriteLine("Example 3 (single favorite):");
Console.WriteLine($"  nums      = [{string.Join(", ", nums3)}]");
Console.WriteLine($"  favorites = [{string.Join(", ", favorites3)}]");
Console.WriteLine($"  Output    = {result3}");
Console.WriteLine($"  Expected  = 1");
Console.WriteLine($"  Correct?  = {result3 == 1}");
Console.WriteLine();

// -------------------------------------------------------
// Additional Test 4: Favorites span entire array
// nums     = [1, 2, 3]
// favorites = [1, 2, 3]
// Only one valid window: the entire array → length 3
// -------------------------------------------------------
int[] nums4 = { 1, 2, 3 };
int[] favorites4 = { 1, 2, 3 };
int result4 = solution.MinWindow(nums4, favorites4);
Console.WriteLine("Example 4 (entire array is minimum window):");
Console.WriteLine($"  nums      = [{string.Join(", ", nums4)}]");
Console.WriteLine($"  favorites = [{string.Join(", ", favorites4)}]");
Console.WriteLine($"  Output    = {result4}");
Console.WriteLine($"  Expected  = 3");
Console.WriteLine($"  Correct?  = {result4 == 3}");
Console.WriteLine();

// -------------------------------------------------------
// Additional Test 5: Duplicate favorites in nums
// nums     = [2, 1, 2, 1, 2]
// favorites = [1, 2]
// Minimum window: any adjacent [2,1] or [1,2] → length 2
// -------------------------------------------------------
int[] nums5 = { 2, 1, 2, 1, 2 };
int[] favorites5 = { 1, 2 };
int result5 = solution.MinWindow(nums5, favorites5);
Console.WriteLine("Example 5 (duplicates in nums):");
Console.WriteLine($"  nums      = [{string.Join(", ", nums5)}]");
Console.WriteLine($"  favorites = [{string.Join(", ", favorites5)}]");
Console.WriteLine($"  Output    = {result5}");
Console.WriteLine($"  Expected  = 2");
Console.WriteLine($"  Correct?  = {result5 == 2}");
Console.WriteLine();

Console.WriteLine("=== All tests complete ===");