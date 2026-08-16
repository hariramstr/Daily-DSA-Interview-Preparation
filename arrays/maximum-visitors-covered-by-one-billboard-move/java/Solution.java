import java.util.*;

/*
Problem Title: Maximum Visitors Covered by One Billboard Move

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

Example 1:
Input: visitors = [5,1,3,8,2,6,4], start = 1, k = 3, m = 2
Output: 16
Explanation: The original billboard covers blocks [1..3] with exposure 1 + 3 + 8 = 12.
With at most 2 moved blocks, the new segment can shift by at most 2 positions because:
moved blocks = k - overlap
             = k - max(0, k - |newStart - start|)
             = min(k, |newStart - start|)
For equal-length segments, this simplifies to the shift distance as long as the shift is at most k.
So any destination with |newStart - start| <= 2 is allowed.
Choosing segment [3..5] gives exposure 8 + 2 + 6 = 16, which is optimal.

Example 2:
Input: visitors = [4,7,2,9,1,5], start = 2, k = 2, m = 0
Output: 11
Explanation: Since no moved blocks are allowed, the billboard must stay in its original position.
It covers blocks [2..3], giving exposure 2 + 9 = 11.
*/

public class Solution {

    /**
     * Computes the maximum total exposure obtainable by keeping the billboard in place
     * or relocating it once, subject to the moved-block limit.
     *
     * Core idea:
     * 1. Precompute the sum of every contiguous window of length k.
     * 2. A move from original left index "start" to new left index "i" is allowed if
     *    the number of moved blocks is at most m.
     * 3. For two equal-length segments of length k, the overlap is:
     *       overlap = max(0, k - |i - start|)
     *    Therefore moved blocks are:
     *       moved = k - overlap
     *             = k - max(0, k - |i - start|)
     *             = min(k, |i - start|)
     *    So the move is allowed exactly when min(k, |i - start|) <= m.
     *    Since m <= k, this becomes simply:
     *       |i - start| <= m
     * 4. Among all valid destination windows, return the maximum window sum.
     *
     * @param visitors the expected pedestrian counts for each block
     * @param start the current left index of the billboard
     * @param k the fixed billboard length
     * @param m the maximum number of moved blocks allowed
     * @return the maximum exposure achievable under the relocation rule
     *
     * Time complexity: O(n), where n = visitors.length
     * Space complexity: O(n) for storing all window sums
     */
    public long maximumExposure(int[] visitors, int start, int k, int m) {
        int n = visitors.length;

        // There are exactly (n - k + 1) possible windows of length k.
        int windowCount = n - k + 1;

        // Precompute the sum of every length-k window.
        long[] windowSums = computeWindowSums(visitors, k);

        // The billboard can move only to windows whose left index differs from "start"
        // by at most m. Because m <= k, this exactly matches the moved-block constraint.
        int leftBound = Math.max(0, start - m);
        int rightBound = Math.min(windowCount - 1, start + m);

        // Initialize answer with the original position's exposure.
        long best = windowSums[start];

        // Check every valid destination window in the allowed range.
        for (int newStart = leftBound; newStart <= rightBound; newStart++) {
            best = Math.max(best, windowSums[newStart]);
        }

        return best;
    }

    /**
     * Computes the sum of every contiguous subarray (window) of fixed length k.
     *
     * This method uses the classic sliding window technique:
     * - First compute the sum of the first k elements.
     * - Then slide the window one step at a time:
     *   remove the element leaving the window and add the new entering element.
     *
     * Example:
     * visitors = [5,1,3,8,2,6,4], k = 3
     * windows:
     * [5,1,3] -> 9
     * [1,3,8] -> 12
     * [3,8,2] -> 13
     * [8,2,6] -> 16
     * [2,6,4] -> 12
     *
     * @param visitors the array of pedestrian counts
     * @param k the required window length
     * @return an array where result[i] is the sum of visitors[i..i+k-1]
     *
     * Time complexity: O(n), where n = visitors.length
     * Space complexity: O(n) for the returned window sums array
     */
    public long[] computeWindowSums(int[] visitors, int k) {
        int n = visitors.length;
        int windowCount = n - k + 1;
        long[] sums = new long[windowCount];

        long currentSum = 0L;

        // Step 1: build the first window sum using the first k elements.
        for (int i = 0; i < k; i++) {
            currentSum += visitors[i];
        }
        sums[0] = currentSum;

        // Step 2: slide the window from left to right.
        // For each new position:
        // - subtract the element that leaves the window
        // - add the element that enters the window
        for (int left = 1; left < windowCount; left++) {
            currentSum -= visitors[left - 1];
            currentSum += visitors[left + k - 1];
            sums[left] = currentSum;
        }

        return sums;
    }

    /**
     * Computes the number of overlapping blocks between two length-k segments
     * with left indices a and b.
     *
     * @param a left index of the first segment
     * @param b left index of the second segment
     * @param k common segment length
     * @return the number of overlapping blocks
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int overlapLength(int a, int b, int k) {
        return Math.max(0, k - Math.abs(a - b));
    }

    /**
     * Computes how many blocks are newly covered when moving a length-k billboard
     * from left index a to left index b.
     *
     * Since both segments have the same length:
     * moved blocks = k - overlap
     *
     * @param a original left index
     * @param b new left index
     * @param k billboard length
     * @return the number of moved blocks
     *
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int movedBlocks(int a, int b, int k) {
        return k - overlapLength(a, b, k);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints a few intermediate values so a beginner can verify the logic:
     * - all window sums
     * - original exposure
     * - final maximum exposure
     *
     * @param args command-line arguments (unused)
     * @return nothing
     *
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(n) per demonstration case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        int[] visitors1 = {5, 1, 3, 8, 2, 6, 4};
        int start1 = 1;
        int k1 = 3;
        int m1 = 2;

        long[] sums1 = solution.computeWindowSums(visitors1, k1);
        long answer1 = solution.maximumExposure(visitors1, start1, k1, m1);

        System.out.println("Sample 1 window sums: " + Arrays.toString(sums1));
        System.out.println("Sample 1 original exposure: " + sums1[start1]);
        System.out.println("Sample 1 maximum exposure: " + answer1);
        System.out.println("Expected: 16");
        System.out.println();

        // Sample 2
        int[] visitors2 = {4, 7, 2, 9, 1, 5};
        int start2 = 2;
        int k2 = 2;
        int m2 = 0;

        long[] sums2 = solution.computeWindowSums(visitors2, k2);
        long answer2 = solution.maximumExposure(visitors2, start2, k2, m2);

        System.out.println("Sample 2 window sums: " + Arrays.toString(sums2));
        System.out.println("Sample 2 original exposure: " + sums2[start2]);
        System.out.println("Sample 2 maximum exposure: " + answer2);
        System.out.println("Expected: 11");
        System.out.println();

        // Extra quick sanity check:
        // If k equals the entire array length, there is only one possible position.
        int[] visitors3 = {3, 6, 1, 2};
        int start3 = 0;
        int k3 = 4;
        int m3 = 4;

        long answer3 = solution.maximumExposure(visitors3, start3, k3, m3);
        System.out.println("Sanity check maximum exposure: " + answer3);
        System.out.println("Expected: 12");
    }
}