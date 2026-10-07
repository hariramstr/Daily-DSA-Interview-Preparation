/*
Title: Maximum Font Scale for Digital Signage
Difficulty: Medium
Topic: Binary Search

Problem Description:
A mall uses a digital signage system to display a single message on one line. For each candidate font scale s, the width of a character c is given by widths[c][s], where widths are nondecreasing as the scale increases. The display panel has a fixed pixel width W, and the entire message must fit on the screen at the same scale.

You are given:
- a string message consisting of lowercase English letters,
- an integer W,
- an array scales of available font scales sorted in strictly increasing order,
- a 2D integer array charWidths where charWidths[i][j] is the width of character ('a' + i) at scales[j].

Return the largest scale value from scales such that the total rendered width of message is at most W. If the message does not fit even at the smallest scale, return -1.

This problem is designed so that checking whether a scale works is straightforward, but doing it for every scale may be too slow when many scales are available. Use the monotonic nature of font sizes: if a message fits at some scale, it will also fit at every smaller scale.

Constraints:
- 1 <= message.length <= 10^5
- 1 <= W <= 10^15
- 1 <= scales.length <= 10^5
- scales is strictly increasing
- charWidths.length == 26
- charWidths[i].length == scales.length
- 1 <= charWidths[i][j] <= 10^9
- For every character i, charWidths[i][j] <= charWidths[i][j + 1]
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Counting character frequencies in the message: O(message.Length)
    - Each feasibility check scans 26 letters: O(26), which is O(1)
    - Binary search over scales.Length candidates: O(log scales.Length)
    - Total: O(message.Length + 26 * log scales.Length) = O(message.Length + log n)

    Space Complexity:
    - Frequency array for 26 lowercase letters: O(26), which is O(1)
    */
    public int MaximumFontScale(string message, long W, int[] scales, int[][] charWidths)
    {
        // Step 1:
        // We count how many times each lowercase letter appears in the message.
        //
        // Why do this?
        // If we directly compute the total width for every check by scanning the whole message,
        // each check would cost O(message.Length). Since binary search performs multiple checks,
        // that could become unnecessarily expensive.
        //
        // Instead, because there are only 26 lowercase English letters, we can compress the message
        // into a frequency table:
        // - freq[0] = number of 'a'
        // - freq[1] = number of 'b'
        // ...
        // - freq[25] = number of 'z'
        //
        // Then, for any scale index j, the total width becomes:
        // sum over all letters i of (freq[i] * charWidths[i][j])
        //
        // This means each "does this scale fit?" check only needs to inspect 26 entries.
        long[] freq = new long[26];
        foreach (char ch in message)
        {
            freq[ch - 'a']++;
        }

        // Step 2:
        // Before doing binary search, it is often helpful to understand the monotonic property:
        //
        // - If the message fits at some scale index j,
        //   then it must also fit at every smaller scale index.
        // - Why? Because widths are nondecreasing as scale increases.
        //   So smaller scales never make any character wider.
        //
        // This creates a classic binary-search-on-answer pattern:
        // valid valid valid valid invalid invalid invalid
        //
        // We want the LAST valid scale.
        int left = 0;
        int right = scales.Length - 1;
        int bestIndex = -1;

        // Step 3:
        // Standard binary search loop.
        //
        // Our goal:
        // - If mid works, record it and try to go right to find a larger valid scale.
        // - If mid does not work, go left to search smaller scales.
        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            // Check whether the message fits at the scale represented by index mid.
            if (FitsAtScaleIndex(freq, W, mid, charWidths))
            {
                // This scale is valid, so it is a candidate answer.
                bestIndex = mid;

                // Since we want the largest valid scale, continue searching to the right.
                left = mid + 1;
            }
            else
            {
                // This scale is too large, so any larger scale will also be too large.
                // Therefore, we must search on the left side.
                right = mid - 1;
            }
        }

        // Step 4:
        // If bestIndex stayed -1, even the smallest scale does not fit.
        // Otherwise, return the actual scale value from the scales array.
        return bestIndex == -1 ? -1 : scales[bestIndex];
    }

    private bool FitsAtScaleIndex(long[] freq, long W, int scaleIndex, int[][] charWidths)
    {
        // This helper method determines whether the entire message fits within width W
        // when rendered at the given scale index.
        //
        // Because we already counted letter frequencies, we do not need to scan the message again.
        // We only need to compute:
        //
        // totalWidth = sum(freq[i] * charWidths[i][scaleIndex]) for i in [0..25]
        //
        // Important detail:
        // - We use long for totalWidth because:
        //   freq can be up to 1e5
        //   char width can be up to 1e9
        //   product can be up to 1e14
        //   sum across letters can still be large
        long totalWidth = 0;

        for (int i = 0; i < 26; i++)
        {
            // If a letter does not appear in the message, it contributes nothing.
            // Skipping zero-frequency letters is not required for correctness,
            // but it avoids unnecessary multiplication.
            if (freq[i] == 0)
            {
                continue;
            }

            // Add the contribution of this letter:
            // number of occurrences * width of this letter at the chosen scale
            totalWidth += freq[i] * charWidths[i][scaleIndex];

            // Early stopping optimization:
            // As soon as totalWidth exceeds W, we already know this scale does not fit.
            // There is no need to continue summing the remaining letters.
            if (totalWidth > W)
            {
                return false;
            }
        }

        // If we finished the loop without exceeding W, the message fits.
        return totalWidth <= W;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// message = "cab"
// W = 10
// scales = [1, 2, 3]
//
// Relevant widths:
// 'a' -> [1, 2, 4]
// 'b' -> [2, 3, 5]
// 'c' -> [3, 4, 6]
//
// Totals:
// scale 1 => 3 + 1 + 2 = 6   fits
// scale 2 => 4 + 2 + 3 = 9   fits
// scale 3 => 6 + 4 + 5 = 15  does not fit
// Answer should be 2
string message1 = "cab";
long W1 = 10;
int[] scales1 = { 1, 2, 3 };
int[][] charWidths1 = new int[26][];
for (int i = 0; i < 26; i++)
{
    charWidths1[i] = new int[] { 1, 2, 3 };
}
charWidths1['a' - 'a'] = new int[] { 1, 2, 4 };
charWidths1['b' - 'a'] = new int[] { 2, 3, 5 };
charWidths1['c' - 'a'] = new int[] { 3, 4, 6 };

int result1 = solution.MaximumFontScale(message1, W1, scales1, charWidths1);
Console.WriteLine(result1);

// Example 2:
// message = "zzzz"
// W = 7
// scales = [2, 4]
//
// 'z' -> [2, 3]
// Totals:
// scale 2 => 4 * 2 = 8, already too large
// Therefore answer should be -1
string message2 = "zzzz";
long W2 = 7;
int[] scales2 = { 2, 4 };
int[][] charWidths2 = new int[26][];
for (int i = 0; i < 26; i++)
{
    charWidths2[i] = new int[] { 1, 1 };
}
charWidths2['z' - 'a'] = new int[] { 2, 3 };

int result2 = solution.MaximumFontScale(message2, W2, scales2, charWidths2);
Console.WriteLine(result2);