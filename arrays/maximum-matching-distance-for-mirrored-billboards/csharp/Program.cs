/*
Title: Maximum Matching Distance for Mirrored Billboards

Problem Description:
A city installs two parallel rows of digital billboards along the same highway. The first row is represented by array top and the second row by array bottom, where top[i] and bottom[i] are the ad category IDs shown at position i in each row. You want to measure how well the two rows can visually reinforce each other.

Define the matching distance of a category x as the largest absolute difference |i - j| such that top[i] = x and bottom[j] = x. In other words, you may pair one occurrence of x from the top row with one occurrence of x from the bottom row, and the score for that category is how far apart those positions are. If a category appears in only one row, it contributes nothing. Your task is to return the maximum matching distance over all categories.

Return 0 if no category appears in both rows.

Constraints:
- 1 <= top.length, bottom.length <= 200000
- top.length == bottom.length
- 1 <= top[i], bottom[i] <= 1000000000
- The answer fits in a 32-bit signed integer
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private sealed class RangeInfo
    {
        public int TopMin = int.MaxValue;
        public int TopMax = int.MinValue;
        public int BottomMin = int.MaxValue;
        public int BottomMax = int.MinValue;

        public bool SeenInTop;
        public bool SeenInBottom;
    }

    /*
    Time Complexity: O(n)
    Space Complexity: O(k)

    Where:
    - n is the length of the arrays
    - k is the number of distinct category IDs

    Why this works:
    For a fixed category x, suppose:
    - top occurrences range from TopMin to TopMax
    - bottom occurrences range from BottomMin to BottomMax

    We want the largest value of |i - j| where:
    - i is any top index containing x
    - j is any bottom index containing x

    The maximum absolute difference between two sets of indices is always achieved by
    pairing one extreme from one set with one extreme from the other set. Therefore,
    for each category x, it is enough to check:
    - |TopMin - BottomMax|
    - |TopMax - BottomMin|

    Then we take the maximum over all categories that appear in both arrays.
    */
    public int MaximumMatchingDistance(int[] top, int[] bottom)
    {
        // This dictionary stores one record per category ID.
        // Key   -> category ID
        // Value -> the extreme positions where that category appears in top and bottom
        //
        // We use a dictionary because category IDs can be as large as 1,000,000,000,
        // so we cannot use a simple array indexed by category value.
        var map = new Dictionary<int, RangeInfo>();

        // We scan both arrays once from left to right.
        // At each index i:
        // - we update the information for top[i]
        // - we update the information for bottom[i]
        //
        // This single pass is enough because we only care about the smallest and largest
        // positions where each category appears in each row.
        for (int i = 0; i < top.Length; i++)
        {
            int topValue = top[i];

            // If this category has not been seen before, create a new record for it.
            if (!map.TryGetValue(topValue, out var topInfo))
            {
                topInfo = new RangeInfo();
                map[topValue] = topInfo;
            }

            // Mark that this category appears in the top row.
            topInfo.SeenInTop = true;

            // Update the smallest top index for this category.
            // This is needed because one candidate for the maximum distance is:
            // smallest top index paired with largest bottom index.
            if (i < topInfo.TopMin)
            {
                topInfo.TopMin = i;
            }

            // Update the largest top index for this category.
            // This is needed because another candidate for the maximum distance is:
            // largest top index paired with smallest bottom index.
            if (i > topInfo.TopMax)
            {
                topInfo.TopMax = i;
            }

            int bottomValue = bottom[i];

            // Same idea for the bottom row.
            if (!map.TryGetValue(bottomValue, out var bottomInfo))
            {
                bottomInfo = new RangeInfo();
                map[bottomValue] = bottomInfo;
            }

            // Mark that this category appears in the bottom row.
            bottomInfo.SeenInBottom = true;

            // Update the smallest bottom index for this category.
            if (i < bottomInfo.BottomMin)
            {
                bottomInfo.BottomMin = i;
            }

            // Update the largest bottom index for this category.
            if (i > bottomInfo.BottomMax)
            {
                bottomInfo.BottomMax = i;
            }
        }

        // This variable will store the best answer found across all categories.
        int answer = 0;

        // Now evaluate each category independently.
        foreach (var entry in map)
        {
            RangeInfo info = entry.Value;

            // A category contributes only if it appears in BOTH rows.
            // If it appears only in top or only in bottom, there is no valid pair.
            if (!info.SeenInTop || !info.SeenInBottom)
            {
                continue;
            }

            // For this category, the maximum absolute difference must come from extremes.
            //
            // Candidate 1:
            // Pair the earliest top occurrence with the latest bottom occurrence.
            int candidate1 = Math.Abs(info.TopMin - info.BottomMax);

            // Candidate 2:
            // Pair the latest top occurrence with the earliest bottom occurrence.
            int candidate2 = Math.Abs(info.TopMax - info.BottomMin);

            // The best distance for this category is the larger of the two candidates.
            int bestForThisCategory = Math.Max(candidate1, candidate2);

            // Update the global answer if this category gives a better result.
            if (bestForThisCategory > answer)
            {
                answer = bestForThisCategory;
            }
        }

        // If no category appeared in both rows, answer remains 0, which is correct.
        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] top1 = { 4, 7, 2, 7, 9 };
int[] bottom1 = { 8, 7, 4, 2, 7 };
int result1 = solution.MaximumMatchingDistance(top1, bottom1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] top2 = { 5, 1, 5, 3, 1, 6 };
int[] bottom2 = { 1, 5, 2, 5, 7, 1 };
int result2 = solution.MaximumMatchingDistance(top2, bottom2);
Console.WriteLine(result2); // Expected: 4

// Additional demo: no shared category
int[] top3 = { 10, 20, 30 };
int[] bottom3 = { 40, 50, 60 };
int result3 = solution.MaximumMatchingDistance(top3, bottom3);
Console.WriteLine(result3); // Expected: 0