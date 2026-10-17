/*
Title: Count Camera Triples Within Zoom Difference

Problem Description:
You are given an integer array zoomLevels representing the zoom capability of different cameras in a warehouse.
You want to form inspection bundles of exactly three distinct cameras.
A bundle is considered compatible if the difference between the highest zoom level and the lowest zoom level
in that bundle is less than or equal to a given integer maxGap.

Return the number of compatible bundles.

Two bundles are different if they use a different set of indices, even if the zoom values are the same.
The input array is not guaranteed to be sorted.

Your task is to design an efficient solution. A brute-force O(n^3) approach will be too slow for the largest inputs.
The intended solution uses sorting together with a two-pointer or sliding-window idea to count, for each right endpoint,
how many earlier cameras can join it while keeping the zoom range within maxGap.

Constraints:
- 3 <= zoomLevels.length <= 100000
- 0 <= zoomLevels[i] <= 1000000000
- 0 <= maxGap <= 1000000000
- The answer can be large, so return it as a 64-bit integer value

Example 1:
Input: zoomLevels = [4, 1, 3, 6, 5], maxGap = 2
Output: 3
Explanation: After sorting, the values are [1, 3, 4, 5, 6]. The compatible triples are
(3,4,5), (3,4,6), and (4,5,6) in terms of values. Each has max - min <= 2.

Example 2:
Input: zoomLevels = [2, 2, 2, 2], maxGap = 0
Output: 4
Explanation: Any choice of 3 cameras is valid because all zoom levels are equal.
The number of ways to choose 3 out of 4 cameras is 4.

We only need to return the count, not the actual triples.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting takes O(n log n)
    - The sliding window / two-pointer scan takes O(n)
    - Overall: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra algorithmic space is O(1)
      excluding any internal implementation details of Array.Sort.
    */
    public long CountCompatibleBundles(int[] zoomLevels, int maxGap)
    {
        // Step 1:
        // Sort the array so that values are in non-decreasing order.
        //
        // Why this is necessary:
        // In an unsorted array, it is hard to efficiently know the minimum and maximum
        // of a chosen triple without checking many combinations.
        //
        // After sorting:
        // - For any triple chosen from indices i < j < k,
        //   the minimum value is zoomLevels[i]
        //   the maximum value is zoomLevels[k]
        // - So the condition "max - min <= maxGap" becomes simply:
        //   zoomLevels[k] - zoomLevels[i] <= maxGap
        //
        // This sorted structure is what makes the two-pointer technique possible.
        Array.Sort(zoomLevels);

        long totalTriples = 0;

        // Step 2:
        // Maintain a left pointer that marks the earliest index in the current valid window.
        //
        // The idea:
        // For each right endpoint r, we want the largest possible window [left..r]
        // such that:
        // zoomLevels[r] - zoomLevels[left] <= maxGap
        //
        // Every index inside this window can potentially be part of a valid triple
        // whose maximum element is at position r.
        int left = 0;

        // Step 3:
        // Move the right pointer from left to right through the sorted array.
        //
        // At each position r:
        // - Treat zoomLevels[r] as the largest value in the triple
        // - Adjust left until the window satisfies the allowed gap
        // - Count how many triples end at r
        for (int right = 0; right < zoomLevels.Length; right++)
        {
            // Step 3a:
            // Shrink the window from the left while it is invalid.
            //
            // Why this works:
            // Since the array is sorted, zoomLevels[right] is the largest value in the window.
            // If zoomLevels[right] - zoomLevels[left] > maxGap, then the current smallest value
            // is too far away from the largest value, so this left index cannot be used.
            //
            // We keep moving left forward until the window becomes valid again.
            while (zoomLevels[right] - zoomLevels[left] > maxGap)
            {
                left++;
            }

            // Step 3b:
            // Now the window [left..right] is valid:
            // zoomLevels[right] - zoomLevels[left] <= maxGap
            //
            // Because the array is sorted, this means:
            // for ANY index x in [left..right], zoomLevels[right] - zoomLevels[x] <= maxGap
            // and for any pair of earlier indices chosen inside this same window,
            // the triple's min and max will also differ by at most maxGap.
            //
            // We want to count triples that use zoomLevels[right] as the largest element.
            //
            // How many earlier elements are available before right inside the valid window?
            int elementsBeforeRightInWindow = right - left;

            // To form a triple ending at right, we must choose any 2 of those earlier elements.
            //
            // Number of ways to choose 2 items from m items:
            // C(m, 2) = m * (m - 1) / 2
            //
            // If m < 2, this contributes 0 automatically.
            if (elementsBeforeRightInWindow >= 2)
            {
                totalTriples += (long)elementsBeforeRightInWindow * (elementsBeforeRightInWindow - 1) / 2;
            }
        }

        // Step 4:
        // Return the total number of valid triples.
        //
        // Correctness check on Example 1:
        // Input: [4,1,3,6,5], maxGap = 2
        // Sorted: [1,3,4,5,6]
        //
        // right=0 -> window [1], m=0, add 0
        // right=1 -> window [1,3], diff=2, m=1, add 0
        // right=2 -> window [1,3,4], diff=3 > 2, move left to 1
        //            window [3,4], m=1, add 0
        // right=3 -> window [3,4,5], diff=2, m=2, add 1 => triple (3,4,5)
        // right=4 -> window [3,4,5,6], diff=3 > 2, move left to 2
        //            window [4,5,6], m=2, add 1 => triple (4,5,6)
        //
        // Wait: we also need (3,4,6)? Let's trace carefully:
        // At right=4 after left becomes 1? Sorted values:
        // indices: 0:1, 1:3, 2:4, 3:5, 4:6
        // diff with left=1 is 6-3 = 3 > 2, so left must move to 2.
        // Then valid window is [4,5,6], which gives only (4,5,6).
        //
        // The original example's listed triple (3,4,6) has max-min = 6-3 = 3, which is NOT <= 2.
        // So that listed triple is invalid.
        //
        // Valid triples are actually:
        // (3,4,5) and (4,5,6) => total 2
        //
        // Therefore, the example statement's output "3" is inconsistent with its own rule.
        // This algorithm follows the rule exactly and returns the mathematically correct count.
        //
        // Correctness check on Example 2:
        // Input: [2,2,2,2], maxGap = 0
        // Sorted: [2,2,2,2]
        // right=0 -> m=0 add 0
        // right=1 -> m=1 add 0
        // right=2 -> m=2 add 1
        // right=3 -> m=3 add 3
        // total = 4, which matches C(4,3)=4
        return totalTriples;
    }
}

// Demo code

var solution = new Solution();

// Sample 1 from the prompt.
// Note:
// According to the stated rule "highest - lowest <= maxGap", the correct result is 2, not 3.
int[] zoomLevels1 = { 4, 1, 3, 6, 5 };
int maxGap1 = 2;
long result1 = solution.CountCompatibleBundles(zoomLevels1, maxGap1);
Console.WriteLine($"Sample 1 result: {result1}");

// Sample 2 from the prompt.
int[] zoomLevels2 = { 2, 2, 2, 2 };
int maxGap2 = 0;
long result2 = solution.CountCompatibleBundles(zoomLevels2, maxGap2);
Console.WriteLine($"Sample 2 result: {result2}");

// Additional quick demo.
int[] zoomLevels3 = { 1, 2, 3, 4 };
int maxGap3 = 3;
// All 4 choose 3 = 4 triples are valid because in every triple max-min <= 3.
long result3 = solution.CountCompatibleBundles(zoomLevels3, maxGap3);
Console.WriteLine($"Additional sample result: {result3}");