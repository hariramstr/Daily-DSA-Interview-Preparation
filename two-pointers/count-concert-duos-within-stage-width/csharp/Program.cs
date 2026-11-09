/*
Title: Count Concert Duos Within Stage Width
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A concert organizer wants to place pairs of performers on the same movable stage. Each performer has a preferred standing position represented by an integer in the array positions. Two performers can share the stage if the absolute difference between their preferred positions is less than or equal to widthLimit. Your task is to count how many distinct performer pairs can be placed together.

Given an integer array positions and an integer widthLimit, return the number of index pairs (i, j) such that i < j and |positions[i] - positions[j]| <= widthLimit.

The array is not guaranteed to be sorted. A correct and efficient solution is expected for large inputs, so a brute-force O(n^2) approach may time out. Think about how sorting and a two-pointers sweep can help count many valid pairs at once.

Constraints:
- 1 <= positions.length <= 200000
- -1000000000 <= positions[i] <= 1000000000
- 0 <= widthLimit <= 1000000000
- The answer may be larger than 32-bit integer range, so use a 64-bit integer type where necessary.

Example 1:
Input: positions = [1, 3, 4, 9], widthLimit = 3
Output: 3
Explanation: Valid pairs are (1,3), (1,4), and (3,4). Any pair involving 9 exceeds the allowed stage width.

Example 2:
Input: positions = [8, 2, 2, 5, 11], widthLimit = 0
Output: 1
Explanation: Only the two performers with preferred position 2 can share the stage, because widthLimit is 0 and equal positions are required.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - The two-pointer sweep takes O(n), because each pointer only moves forward
    - Total: O(n log n)

    Space Complexity:
    - If we sort the input array in place, the extra space used by the algorithm itself is O(1)
      not counting the internal space used by the sorting implementation
    */
    public long CountConcertDuosWithinStageWidth(int[] positions, int widthLimit)
    {
        // Step 1:
        // Sort the positions array.
        //
        // Why this is necessary:
        // The condition we care about is:
        //     |positions[i] - positions[j]| <= widthLimit
        //
        // If the array is sorted, then for any pair where left < right:
        //     positions[right] >= positions[left]
        //
        // So the absolute value is no longer needed, because:
        //     |positions[right] - positions[left]| = positions[right] - positions[left]
        //
        // This makes the problem much easier to handle with two pointers.
        Array.Sort(positions);

        // This variable stores the final number of valid pairs.
        //
        // We use long instead of int because the number of pairs can be very large.
        // For example, if n = 200000, the number of possible pairs can be about 20 billion,
        // which does not fit in a 32-bit int.
        long totalPairs = 0;

        // We will use a sliding window / two-pointer technique.
        //
        // left  = start of the current valid window
        // right = end of the current valid window
        //
        // The idea:
        // For each right pointer, we want to find the smallest left pointer such that:
        //     positions[right] - positions[left] <= widthLimit
        //
        // Once we have that, every index from left to right - 1 forms a valid pair with right.
        int left = 0;

        // Step 2:
        // Move the right pointer from left to right across the sorted array.
        //
        // At each position "right", we count how many earlier elements can pair with it.
        for (int right = 0; right < positions.Length; right++)
        {
            // Step 3:
            // While the current window is invalid, move left forward.
            //
            // The window is invalid when the difference between the largest value
            // in the window (positions[right]) and the smallest value in the window
            // (positions[left]) is greater than widthLimit.
            //
            // Because the array is sorted, if positions[right] - positions[left] is too large,
            // then positions[left] cannot pair with positions[right], and neither can any even
            // smaller left that might have existed before. So we safely move left forward.
            while (positions[right] - positions[left] > widthLimit)
            {
                left++;
            }

            // Step 4:
            // Count how many valid pairs end at index "right".
            //
            // After the while-loop finishes, the window [left ... right] satisfies:
            //     positions[right] - positions[left] <= widthLimit
            //
            // Since the array is sorted, that means every element between left and right
            // also differs from positions[right] by at most widthLimit.
            //
            // Therefore, the valid partners for index right are:
            //     left, left+1, left+2, ..., right-1
            //
            // The number of such indices is:
            //     right - left
            //
            // We add that count to the answer.
            totalPairs += right - left;
        }

        // Step 5:
        // Return the total number of valid pairs found.
        return totalPairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// positions = [1, 3, 4, 9], widthLimit = 3
// Sorted: [1, 3, 4, 9]
//
// Valid pairs:
// (1,3) difference 2
// (1,4) difference 3
// (3,4) difference 1
// Pairs involving 9 are too far away
//
// Expected output: 3
int[] positions1 = { 1, 3, 4, 9 };
int widthLimit1 = 3;
long result1 = solution.CountConcertDuosWithinStageWidth(positions1, widthLimit1);
Console.WriteLine(result1);

// Example 2:
// positions = [8, 2, 2, 5, 11], widthLimit = 0
// Sorted: [2, 2, 5, 8, 11]
//
// widthLimit = 0 means only equal values can pair
// The only duplicate value is 2, appearing twice
// So there is exactly 1 valid pair
//
// Expected output: 1
int[] positions2 = { 8, 2, 2, 5, 11 };
int widthLimit2 = 0;
long result2 = solution.CountConcertDuosWithinStageWidth(positions2, widthLimit2);
Console.WriteLine(result2);

// Additional demo:
// positions = [-5, -2, 0, 1, 3], widthLimit = 3
//
// Sorted already: [-5, -2, 0, 1, 3]
//
// Valid pairs:
// (-5,-2) diff 3
// (-2,0) diff 2
// (-2,1) diff 3
// (0,1) diff 1
// (0,3) diff 3
// (1,3) diff 2
//
// Total = 6
int[] positions3 = { -5, -2, 0, 1, 3 };
int widthLimit3 = 3;
long result3 = solution.CountConcertDuosWithinStageWidth(positions3, widthLimit3);
Console.WriteLine(result3);