/*
Title: Maximum Visitors Covered by One Billboard Move

Problem Description:
A city avenue is represented by an integer array visitors, where visitors[i] is the number of pedestrians expected to pass block i during the day. The city has exactly one advertising billboard that currently occupies a contiguous segment of length k blocks. A billboard covers every block in its segment, and the total exposure is the sum of visitors on those covered blocks.

Before the campaign starts, you may relocate the billboard at most once. Relocating means choosing any other contiguous segment of length k. However, moving the billboard has a setup cost: the new segment must overlap the original segment in fewer than k blocks, and every block that is newly covered instead of previously covered counts as a moved block. You are given an integer m, and the relocation is allowed only if the number of moved blocks is at most m. If you do not relocate, the moved block count is 0.

Given visitors, the starting left index start of the current billboard, the billboard length k, and the relocation limit m, return the maximum total exposure achievable.

Two segments of length k with left indices a and b overlap in max(0, k - |a - b|) blocks, so the number of moved blocks is k - overlap.

Constraints:
- 1 <= visitors.length <= 100000
- 1 <= visitors[i] <= 10000
- 1 <= k <= visitors.length
- 0 <= start <= visitors.length - k
- 0 <= m <= k

Key observation:
For two windows of equal length k with left indices start and dest:
- overlap = max(0, k - |dest - start|)
- movedBlocks = k - overlap

Because overlap = max(0, k - distance), movedBlocks becomes:
- movedBlocks = min(k, |dest - start|)

Since m <= k, the relocation is allowed exactly when:
- |dest - start| <= m

So the problem reduces to:
1. Compute the sum of every length-k window.
2. Among all valid destination windows whose left index is in [start - m, start + m]
   and also within array bounds, return the maximum window sum.

This gives an efficient O(n) solution.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We compute the first window sum once.
    - Then we slide the window across the array exactly once.
    - Finally, we scan only the valid destination window indices, which is at most O(n).

    Space Complexity: O(n)
    - We store the sum of every length-k window in an array.
    - There are n - k + 1 such windows.
    */
    public long MaxExposure(int[] visitors, int start, int k, int m)
    {
        // Step 1:
        // Determine how many length-k windows exist in the array.
        //
        // If the array length is n, then the possible left indices for a window of length k are:
        // 0, 1, 2, ..., n - k
        //
        // So the total number of windows is:
        // n - k + 1
        //
        // We will compute the sum for each of these windows so that later we can quickly
        // check which valid relocation gives the best total exposure.
        int n = visitors.Length;
        int windowCount = n - k + 1;

        // Step 2:
        // Create an array to store the sum of each length-k window.
        //
        // windowSums[i] will mean:
        // "the total visitors covered by a billboard placed on blocks [i .. i + k - 1]"
        //
        // This is useful because once all window sums are known, the problem becomes:
        // "Among the allowed destination windows, which one has the largest sum?"
        long[] windowSums = new long[windowCount];

        // Step 3:
        // Compute the sum of the very first window: blocks [0 .. k - 1].
        //
        // We do this directly with a loop.
        // After that, we will use a sliding window technique to compute all later sums efficiently.
        //
        // Why sliding window?
        // Because recomputing each window from scratch would cost O(k) per window,
        // leading to O(n * k) in the worst case, which is too slow for n up to 100000.
        //
        // Sliding window lets us update the sum in O(1) time per shift:
        // newSum = oldSum - elementLeaving + elementEntering
        long currentWindowSum = 0;
        for (int i = 0; i < k; i++)
        {
            currentWindowSum += visitors[i];
        }

        // Store the sum of the first window.
        windowSums[0] = currentWindowSum;

        // Step 4:
        // Slide the window from left to right to compute all remaining window sums.
        //
        // Suppose the current window starts at index i - 1.
        // Then the next window starts at index i.
        //
        // To move from window [i - 1 .. i + k - 2] to [i .. i + k - 1]:
        // - Remove visitors[i - 1] because it leaves the window
        // - Add visitors[i + k - 1] because it enters the window
        //
        // This gives each new window sum in O(1) time.
        for (int i = 1; i < windowCount; i++)
        {
            currentWindowSum -= visitors[i - 1];
            currentWindowSum += visitors[i + k - 1];
            windowSums[i] = currentWindowSum;
        }

        // Step 5:
        // Convert the movement rule into a simple range of valid destination left indices.
        //
        // Original billboard left index = start
        // Destination billboard left index = dest
        //
        // For equal-length windows:
        // overlap = max(0, k - |dest - start|)
        // movedBlocks = k - overlap
        //
        // This simplifies to:
        // movedBlocks = min(k, |dest - start|)
        //
        // Since m <= k, the move is allowed exactly when:
        // |dest - start| <= m
        //
        // Therefore, the destination left index must lie in:
        // [start - m, start + m]
        //
        // But we also must stay within valid window indices:
        // [0, windowCount - 1]
        //
        // So we clamp the range to array bounds.
        int leftBound = Math.Max(0, start - m);
        int rightBound = Math.Min(windowCount - 1, start + m);

        // Step 6:
        // Scan all valid destination windows and find the maximum exposure.
        //
        // This includes the original position automatically because start is inside
        // [start - m, start + m], and when dest == start, movedBlocks == 0.
        //
        // So "do not relocate" is naturally handled by the same logic.
        long best = 0;
        for (int dest = leftBound; dest <= rightBound; dest++)
        {
            if (windowSums[dest] > best)
            {
                best = windowSums[dest];
            }
        }

        // Step 7:
        // Return the best exposure found among all allowed placements.
        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] visitors1 = { 5, 1, 3, 8, 2, 6, 4 };
int start1 = 1;
int k1 = 3;
int m1 = 2;
long result1 = solution.MaxExposure(visitors1, start1, k1, m1);
Console.WriteLine(result1); // Expected: 16

// Example 2
int[] visitors2 = { 4, 7, 2, 9, 1, 5 };
int start2 = 2;
int k2 = 2;
int m2 = 0;
long result2 = solution.MaxExposure(visitors2, start2, k2, m2);
Console.WriteLine(result2); // Expected: 11

// Additional quick sanity check
int[] visitors3 = { 10, 20, 30, 40, 50 };
int start3 = 1;
int k3 = 2;
int m3 = 1;
// Valid destination starts: [0..2]
// Window sums: [30, 50, 70, 90]
// Best among starts 0,1,2 is 70
long result3 = solution.MaxExposure(visitors3, start3, k3, m3);
Console.WriteLine(result3); // Expected: 70