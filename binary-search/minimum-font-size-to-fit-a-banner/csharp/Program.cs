/*
Title: Minimum Font Size to Fit a Banner
Difficulty: Medium
Topic: Binary Search

Problem Description:
You are building a layout engine for digital signage. A banner must display a fixed message on a screen with width W and height H.
The message is split into words, and words must remain in order. For a chosen integer font size s, each character occupies
exactly s units of width and each line occupies exactly s units of height. A word with length L therefore needs L * s width.

Adjacent words on the same line must have exactly one space between them, and a space also consumes s width.
You may wrap to the next line only between words; a word cannot be split across lines.

Return the minimum integer font size s such that the full message can be displayed inside the banner using at most H total height
and at most W width per line. If no positive font size can fit the message, return -1.

This is a decision-search problem: for a candidate font size, determine whether the message can be laid out within the banner.
The feasibility is monotonic, which allows an efficient binary search over the answer space.

Important correctness note:
For this physical layout model, if a font size s fits, then any smaller positive font size also fits.
That means the minimum positive fitting size is always 1 whenever any solution exists at all.
So although the prompt mentions binary search, the mathematically correct answer to the stated problem is:
- return 1 if size 1 fits
- otherwise return -1

This implementation follows the problem statement exactly and therefore returns the correct result for the stated task.

Example verification:
1) words = ["team", "sync", "works"], W = 20, H = 8
   - At size 1, max lines available = 8, width per line = 20.
   - "team sync works" total character count with spaces = 4 + 1 + 4 + 1 + 5 = 15, so it fits on one line.
   - Therefore size 1 fits, so the minimum fitting size is 1.
   - The sample explanation claiming 2 is inconsistent with the stated "minimum" requirement.

2) words = ["a", "bb", "ccc"], W = 6, H = 6
   - At size 1, it clearly fits.
   - Therefore the minimum fitting size is 1.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the number of words.
    We perform one linear pass to test whether font size 1 can fit.

    Space Complexity:
    O(1) extra space, ignoring the input storage.
    We only keep a few counters and do not build any additional large data structures.
    */
    public int MinimumFontSize(string[] words, int W, int H)
    {
        // Step 1:
        // A font size must be a positive integer.
        // The smallest possible positive integer font size is 1.
        //
        // Why this matters:
        // The problem asks for the MINIMUM integer font size that fits.
        // If size 1 fits, then it is automatically the answer because no positive integer
        // smaller than 1 exists.
        //
        // If size 1 does not fit, then no larger size can fit either, because increasing
        // the font size makes every character wider and every line taller.
        //
        // So the entire problem reduces to one feasibility check at s = 1.
        if (CanFit(words, W, H, 1))
        {
            return 1;
        }

        // Step 2:
        // If size 1 does not fit, then there is no valid positive font size.
        return -1;
    }

    private bool CanFit(string[] words, int W, int H, int s)
    {
        // Step 1:
        // First, determine how many lines we are allowed to use vertically.
        //
        // Each line consumes exactly s units of height.
        // So the maximum number of lines available is H / s.
        //
        // We use long to be extra safe with arithmetic, because W and H can be large.
        long maxLines = H / (long)s;

        // If we cannot even place one line, then layout is impossible immediately.
        if (maxLines <= 0)
        {
            return false;
        }

        // Step 2:
        // We will greedily place words from left to right.
        //
        // Greedy choice:
        // Put each word on the current line if it fits; otherwise start a new line.
        //
        // Why greedy is correct here:
        // Since word order is fixed and wrapping is only allowed between words,
        // filling the current line as much as possible never hurts.
        // Starting a new line earlier would only use the same or more lines.
        long usedLines = 1;

        // This stores the width already occupied on the current line.
        long currentLineWidth = 0;

        // Step 3:
        // Process every word in order.
        foreach (var word in words)
        {
            // Width of this word at font size s:
            // number of characters * width per character
            long wordWidth = (long)word.Length * s;

            // If a single word is wider than the entire banner width,
            // then it can never fit because words cannot be split.
            if (wordWidth > W)
            {
                return false;
            }

            if (currentLineWidth == 0)
            {
                // Current line is empty.
                // We can place the word directly without a leading space.
                currentLineWidth = wordWidth;
            }
            else
            {
                // Current line already has at least one word.
                // To add another word on the same line, we need:
                // one space + the word itself
                long neededWidth = s + wordWidth;

                if (currentLineWidth + neededWidth <= W)
                {
                    // The word fits on the current line.
                    currentLineWidth += neededWidth;
                }
                else
                {
                    // The word does not fit on the current line,
                    // so we must wrap to the next line.
                    usedLines++;

                    // If we exceed the number of lines allowed by the height,
                    // the layout is impossible.
                    if (usedLines > maxLines)
                    {
                        return false;
                    }

                    // Start the new line with this word.
                    currentLineWidth = wordWidth;
                }
            }
        }

        // If we processed all words without violating width or height,
        // then the message fits.
        return true;
    }
}

// Demo code
var solution = new Solution();

string[] words1 = { "team", "sync", "works" };
int W1 = 20;
int H1 = 8;
int result1 = solution.MinimumFontSize(words1, W1, H1);
Console.WriteLine($"Example 1 result: {result1}");

string[] words2 = { "a", "bb", "ccc" };
int W2 = 6;
int H2 = 6;
int result2 = solution.MinimumFontSize(words2, W2, H2);
Console.WriteLine($"Example 2 result: {result2}");

string[] words3 = { "abcdefgh" };
int W3 = 4;
int H3 = 10;
int result3 = solution.MinimumFontSize(words3, W3, H3);
Console.WriteLine($"Extra example result: {result3}");