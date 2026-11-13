/*
Title: Minimum Screen Width for Wrapped Chat Messages
Difficulty: Medium
Topic: Binary Search

Problem Description:
You are building a chat application that displays messages in a fixed-width text area.
Each message is a string containing lowercase letters and single spaces between words,
with no leading or trailing spaces.

A message is shown using word wrapping:
- Each line can contain one or more whole words separated by a single space
- A word cannot be split across lines
- The width of a line is the total number of characters in that line, including spaces

Given an array messages and an integer maxLines, find the minimum screen width W such that
every message can be displayed using at most maxLines lines. All messages must fit under
the same width W.

If a message contains a word of length L, then any valid width must satisfy W >= L.
Return the smallest such width.

Constraints:
- 1 <= messages.length <= 100000
- 1 <= messages[i].length <= 100000
- The total length of all messages combined does not exceed 200000
- 1 <= maxLines <= 100000
- messages[i] contains only lowercase English letters and single spaces
- Each word length is at least 1

Example 1:
Input: messages = ["chat app design", "binary search works"], maxLines = 2
Output: 12

Explanation:
With width 12:
- "chat app design" -> "chat app" / "design"
- "binary search works" -> "binary" / "search works"

Width 11 fails because "search works" has length 12, so the second message needs 3 lines.

Example 2:
Input: messages = ["a bb ccc", "dddd e"], maxLines = 3
Output: 4

Explanation:
With width 4:
- "a bb ccc" -> "a bb" / "ccc"
- "dddd e" -> "dddd" / "e"

Width 3 fails because the word "dddd" does not fit.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // Time Complexity:
    // Let T be the total number of characters across all messages.
    // Preprocessing all messages into word lengths takes O(T).
    // Each feasibility check scans all word lengths once, so it is O(T).
    // We binary search the answer over the width range, which takes O(log M),
    // where M is the maximum possible width bound (at most the longest message length).
    // Total: O(T log M)
    //
    // Space Complexity:
    // We store the word lengths for all messages, which takes O(T) in the worst case
    // because the total number of words is at most proportional to the total characters.
    // Total: O(T)
    public int MinimumWidth(string[] messages, int maxLines)
    {
        // We will preprocess each message into a list of word lengths.
        // Why do this?
        // Because during binary search we will repeatedly ask:
        // "If the width were W, how many lines would this message need?"
        // That question depends only on word lengths and spaces between words,
        // so storing lengths avoids reparsing strings every time.
        var allWordLengths = new List<int[]>(messages.Length);

        // This variable tracks the minimum possible valid width.
        // A width smaller than the longest single word can never work,
        // because words are not allowed to be split across lines.
        int lowerBound = 0;

        // This variable tracks a safe upper bound for binary search.
        // A message always fits in one line if the width is at least the full message length.
        // Therefore, the maximum message length is always a valid upper bound.
        int upperBound = 0;

        foreach (var message in messages)
        {
            // Update the upper bound using the full message length.
            // This guarantees that binary search has a valid "high" endpoint.
            if (message.Length > upperBound)
            {
                upperBound = message.Length;
            }

            var lengths = ParseWordLengths(message);
            allWordLengths.Add(lengths);

            // Update the lower bound using the longest word in this message.
            // Any valid width must be at least this large.
            foreach (var len in lengths)
            {
                if (len > lowerBound)
                {
                    lowerBound = len;
                }
            }
        }

        // Standard binary search on the answer:
        // We search for the smallest width W such that every message fits
        // within at most maxLines lines.
        int left = lowerBound;
        int right = upperBound;

        while (left < right)
        {
            // Midpoint candidate width.
            // We use this overflow-safe formula even though values are small enough,
            // because it is the standard robust pattern.
            int mid = left + (right - left) / 2;

            // Check whether this width is sufficient for all messages.
            if (CanFitAllMessages(allWordLengths, maxLines, mid))
            {
                // If mid works, then the answer could be mid or something smaller.
                // So we keep the left half including mid.
                right = mid;
            }
            else
            {
                // If mid does not work, then every width <= mid also fails,
                // so we must search strictly larger widths.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right and points to the smallest valid width.
        return left;
    }

    private int[] ParseWordLengths(string message)
    {
        // We parse the message manually instead of using Split(' ').
        // Why?
        // - It avoids creating many temporary strings.
        // - It is more memory-efficient.
        // - It is a good habit for large-input problems.
        var lengths = new List<int>();
        int currentWordLength = 0;

        foreach (char c in message)
        {
            if (c == ' ')
            {
                // We reached the end of a word.
                // Store its length and reset for the next word.
                lengths.Add(currentWordLength);
                currentWordLength = 0;
            }
            else
            {
                // We are still inside the current word.
                currentWordLength++;
            }
        }

        // Add the final word after the loop ends.
        lengths.Add(currentWordLength);

        return lengths.ToArray();
    }

    private bool CanFitAllMessages(List<int[]> allWordLengths, int maxLines, int width)
    {
        // We must verify that EVERY message can be wrapped using at most maxLines lines.
        // If even one message needs more than maxLines lines, this width is invalid.
        foreach (var wordLengths in allWordLengths)
        {
            if (!CanFitSingleMessage(wordLengths, maxLines, width))
            {
                return false;
            }
        }

        return true;
    }

    private bool CanFitSingleMessage(int[] wordLengths, int maxLines, int width)
    {
        // We greedily pack as many words as possible into the current line.
        // This greedy strategy is correct for word wrapping when the goal is to minimize
        // the number of lines:
        // - If a word fits on the current line, placing it there can never increase
        //   the number of lines compared to moving it to the next line.
        // - Therefore, the greedy packing gives the minimum number of lines for a fixed width.
        int usedLines = 1;

        // This stores the current line's occupied width.
        // It includes both letters and the single spaces between words already placed.
        int currentLineWidth = 0;

        foreach (int wordLength in wordLengths)
        {
            // Safety check:
            // If a single word is longer than the width, then this width is impossible.
            // In practice, binary search starts from the longest word length,
            // so this should not happen often, but keeping the check makes the method robust.
            if (wordLength > width)
            {
                return false;
            }

            if (currentLineWidth == 0)
            {
                // The current line is empty, so this word becomes the first word on the line.
                // No leading space is needed.
                currentLineWidth = wordLength;
            }
            else
            {
                // If we add another word to a non-empty line, we must also add one space.
                int neededWidth = currentLineWidth + 1 + wordLength;

                if (neededWidth <= width)
                {
                    // The word fits on the current line.
                    // We extend the line width accordingly.
                    currentLineWidth = neededWidth;
                }
                else
                {
                    // The word does not fit on the current line.
                    // Therefore, we must start a new line with this word.
                    usedLines++;

                    // If we already exceeded the allowed number of lines,
                    // we can stop early because this width is invalid.
                    if (usedLines > maxLines)
                    {
                        return false;
                    }

                    // The new line starts with this word alone.
                    currentLineWidth = wordLength;
                }
            }
        }

        // If we finish processing all words without exceeding maxLines,
        // then this message fits within the allowed number of lines.
        return true;
    }
}

// Demo code
var solution = new Solution();

string[] messages1 = { "chat app design", "binary search works" };
int maxLines1 = 2;
int result1 = solution.MinimumWidth(messages1, maxLines1);
Console.WriteLine(result1); // Expected: 12

string[] messages2 = { "a bb ccc", "dddd e" };
int maxLines2 = 3;
int result2 = solution.MinimumWidth(messages2, maxLines2);
Console.WriteLine(result2); // Expected: 4