/*
Title: Minimum Swaps to Group Fragile Packages

Problem Description:
A warehouse stores packages in a single row represented by an integer array `packages`,
where `packages[i] = 1` means the package at position `i` is fragile and `packages[i] = 0`
means it is not fragile.

For safety inspection, all fragile packages should be placed next to each other in one
contiguous block. In one operation, you may swap the contents of any two positions in the array.

Return the minimum number of swaps needed to group all fragile packages together.

You are not required to preserve the relative order of packages. If there are zero or one
fragile packages, the answer is `0` because they are already trivially grouped.

Key observation:
- If there are `k` fragile packages in total, then after grouping them together, they must
  occupy some contiguous window of length `k`.
- Inside that chosen window, every `0` is a "wrong" value that must be swapped out.
- Equivalently, we want the window of length `k` that already contains the maximum number of `1`s.
- If the best window contains `maxOnesInWindow` fragile packages, then the number of swaps needed is:
      k - maxOnesInWindow
  because those are exactly the non-fragile packages inside the chosen window.

Examples:
1) packages = [1,0,1,0,1]
   Total fragile packages = 3
   Windows of length 3:
   - [1,0,1] => 2 ones
   - [0,1,0] => 1 one
   - [1,0,1] => 2 ones
   Best window has 2 ones, so answer = 3 - 2 = 1

2) packages = [0,0,1,0,1,1,0]
   Total fragile packages = 3
   Windows of length 3:
   - [0,0,1] => 1 one
   - [0,1,0] => 1 one
   - [1,0,1] => 2 ones
   - [0,1,1] => 2 ones
   - [1,1,0] => 2 ones
   Best window has 2 ones, so answer = 3 - 2 = 1

Constraints:
- 1 <= packages.length <= 100000
- packages[i] is either 0 or 1
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We make one pass to count how many fragile packages (1s) exist.
    - We make another pass with a sliding window across the array.
    - Each element is added to and removed from the window at most once.

    Space Complexity: O(1)
    - We only use a few integer variables.
    - No extra arrays or collections are needed.
    */
    public int MinSwaps(int[] packages)
    {
        // Step 1: Count the total number of fragile packages.
        //
        // Why this is necessary:
        // If there are k fragile packages in the entire array, then in the final grouped arrangement,
        // those k fragile packages must occupy one contiguous block of exactly length k.
        //
        // This count determines the size of the sliding window we need to inspect.
        int totalFragile = 0;
        foreach (int value in packages)
        {
            if (value == 1)
            {
                totalFragile++;
            }
        }

        // Step 2: Handle easy edge cases.
        //
        // Why this is necessary:
        // - If there are 0 fragile packages, there is nothing to group.
        // - If there is only 1 fragile package, it is already trivially grouped by itself.
        //
        // In both cases, no swaps are needed.
        if (totalFragile <= 1)
        {
            return 0;
        }

        // Step 3: Build the first window of size totalFragile.
        //
        // Data structure choice:
        // We do NOT need an actual queue or subarray.
        // We only need the count of how many 1s are currently inside the window.
        //
        // Why this is enough:
        // The number of swaps needed for a window is:
        //   windowSize - numberOfOnesInWindow
        // Since windowSize is fixed at totalFragile, maximizing ones in the window
        // automatically minimizes zeros in the window, and therefore minimizes swaps.
        int currentOnesInWindow = 0;
        for (int i = 0; i < totalFragile; i++)
        {
            if (packages[i] == 1)
            {
                currentOnesInWindow++;
            }
        }

        // Step 4: Record the best window seen so far.
        //
        // Why this is necessary:
        // We want the maximum number of fragile packages in any window of size totalFragile.
        // Starting with the first window gives us an initial best value.
        int maxOnesInAnyWindow = currentOnesInWindow;

        // Step 5: Slide the window one position at a time across the array.
        //
        // Window movement idea:
        // If the current window is [left ... right], then the next window is [left+1 ... right+1].
        //
        // Instead of recounting the entire window every time (which would be slow),
        // we update the count efficiently:
        // - Remove the element that leaves the window
        // - Add the element that enters the window
        //
        // This is the classic "sliding window" optimization.
        for (int right = totalFragile; right < packages.Length; right++)
        {
            // The leftmost index of the previous window is:
            int left = right - totalFragile;

            // Step 5a: Remove the contribution of the element leaving the window.
            //
            // Why this is necessary:
            // That element is no longer part of the new window, so if it was a 1,
            // it should no longer be counted.
            if (packages[left] == 1)
            {
                currentOnesInWindow--;
            }

            // Step 5b: Add the contribution of the new element entering the window.
            //
            // Why this is necessary:
            // This new element is now part of the current window, so if it is a 1,
            // it should be included in the count.
            if (packages[right] == 1)
            {
                currentOnesInWindow++;
            }

            // Step 5c: Update the best answer seen so far.
            //
            // Why this is necessary:
            // We are searching for the window with the maximum number of 1s.
            // That window will require the fewest swaps.
            if (currentOnesInWindow > maxOnesInAnyWindow)
            {
                maxOnesInAnyWindow = currentOnesInWindow;
            }
        }

        // Step 6: Compute the minimum swaps from the best window.
        //
        // Why this formula works:
        // - The chosen window has length totalFragile.
        // - If it already contains maxOnesInAnyWindow fragile packages,
        //   then the remaining positions in that window must be non-fragile packages (0s).
        // - Each such 0 must be swapped with a 1 from outside the window.
        //
        // Therefore:
        //   minimum swaps = totalFragile - maxOnesInAnyWindow
        return totalFragile - maxOnesInAnyWindow;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] packages1 = { 1, 0, 1, 0, 1 };
int result1 = solution.MinSwaps(packages1);
Console.WriteLine("Example 1:");
Console.WriteLine("Input: [1,0,1,0,1]");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Example 2
int[] packages2 = { 0, 0, 1, 0, 1, 1, 0 };
int result2 = solution.MinSwaps(packages2);
Console.WriteLine("Example 2:");
Console.WriteLine("Input: [0,0,1,0,1,1,0]");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 1");
Console.WriteLine();

// Additional edge case: no fragile packages
int[] packages3 = { 0, 0, 0, 0 };
int result3 = solution.MinSwaps(packages3);
Console.WriteLine("Edge Case 1:");
Console.WriteLine("Input: [0,0,0,0]");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

// Additional edge case: one fragile package
int[] packages4 = { 0, 1, 0, 0 };
int result4 = solution.MinSwaps(packages4);
Console.WriteLine("Edge Case 2:");
Console.WriteLine("Input: [0,1,0,0]");
Console.WriteLine($"Output: {result4}");
Console.WriteLine("Expected: 0");
Console.WriteLine();

// Additional test: already grouped
int[] packages5 = { 0, 1, 1, 1, 0 };
int result5 = solution.MinSwaps(packages5);
Console.WriteLine("Additional Test:");
Console.WriteLine("Input: [0,1,1,1,0]");
Console.WriteLine($"Output: {result5}");
Console.WriteLine("Expected: 0");