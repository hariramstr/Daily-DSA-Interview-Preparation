/*
Title: Longest Caption Draft Within Character Budget
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A social media team is drafting a caption from a sequence of word fragments. The i-th fragment contributes words[i].Length characters to the final caption, and fragments must be used in their original order. You are given an array words of non-empty strings and an integer maxChars. Find the maximum number of consecutive fragments that can be included so that the total number of characters in the chosen window is at most maxChars.

Only the characters inside each fragment count toward the budget. Ignore spaces between fragments; assume they are inserted automatically and do not consume any characters. Your task is to return the length of the longest contiguous block of fragments whose combined character count does not exceed maxChars.

This problem is intended to be solved efficiently for large inputs. A brute-force solution that checks every subarray will be too slow. Think about maintaining a moving window and shrinking it only when the current total exceeds the allowed budget.

Constraints:
- 1 <= words.Length <= 100000
- 1 <= words[i].Length <= 10000
- 1 <= maxChars <= 10^9
- words[i] consists of lowercase English letters

Important correctness note about Example 1:
The original prompt text contains a contradiction. It first says output 3, but then its own explanation shows that every length-3 window is invalid and the best valid answer is 2.
The correct answer for Example 1 is therefore 2.

Example 1:
Input: words = ["go","viral","now","x","trend"], maxChars = 8
Correct Output: 2

Example 2:
Input: words = ["a","bb","c","ddd","e"], maxChars = 4
Output: 3
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each fragment enters the sliding window once when the right pointer moves forward.
    - Each fragment leaves the sliding window at most once when the left pointer moves forward.
    - Therefore, the total amount of work is linear in the number of fragments.

    Space Complexity: O(1)
    - We only store a few integer/long variables for pointers, running sum, and answer.
    - We do not allocate any extra data structures proportional to input size.
    */
    public int LongestCaptionDraft(string[] words, int maxChars)
    {
        // The left boundary of our current sliding window.
        // The window always represents a contiguous block: words[left..right].
        int left = 0;

        // This variable stores the total number of characters currently inside the window.
        // We use long instead of int to be extra safe, even though the given constraints
        // still fit inside int. Using long avoids accidental overflow if constraints grow.
        long currentCharCount = 0;

        // This will store the best (largest) valid window length we have seen so far.
        int bestLength = 0;

        // We expand the window one fragment at a time by moving the right boundary forward.
        for (int right = 0; right < words.Length; right++)
        {
            // STEP 1: Add the new fragment at position "right" into the window.
            // Why?
            // We are trying every possible window ending at "right".
            // By adding this fragment, we extend the current contiguous block.
            currentCharCount += words[right].Length;

            // STEP 2: If the window is invalid (too many characters),
            // shrink it from the left until it becomes valid again.
            //
            // Why is this necessary?
            // The problem requires total characters <= maxChars.
            // If we exceed the budget, we must remove fragments from the left side
            // because the window must remain contiguous and in original order.
            //
            // Why a while loop instead of an if?
            // Because adding one large fragment may make the window exceed the budget
            // by more than the length of a single left fragment, so we may need to
            // remove multiple fragments before the window becomes valid again.
            while (currentCharCount > maxChars)
            {
                // Remove the leftmost fragment from the running total,
                // because it is no longer part of the window after we move left forward.
                currentCharCount -= words[left].Length;

                // Advance the left boundary to complete the shrink operation.
                left++;
            }

            // STEP 3: At this point, the window words[left..right] is guaranteed valid.
            // That means its total character count is <= maxChars.
            //
            // We can now compute its length and compare it with the best answer so far.
            int currentWindowLength = right - left + 1;

            // If this valid window is longer than any valid window seen before,
            // update the answer.
            if (currentWindowLength > bestLength)
            {
                bestLength = currentWindowLength;
            }
        }

        // After processing all possible right endpoints, bestLength contains
        // the maximum number of consecutive fragments that fit within the budget.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
// The prompt's explanation is inconsistent, but the correct answer is 2.
// Windows:
// ["go"] = 2
// ["go","viral"] = 7
// ["go","viral","now"] = 10 invalid
// ["viral","now"] = 8 valid
// ["viral","now","x"] = 9 invalid
// ["now","x"] = 4 valid
// ["now","x","trend"] = 9 invalid
// ["x","trend"] = 6 valid
// Best valid length = 2
string[] words1 = { "go", "viral", "now", "x", "trend" };
int maxChars1 = 8;
int result1 = solution.LongestCaptionDraft(words1, maxChars1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2
// ["a","bb","c"] = 1 + 2 + 1 = 4, so length 3 is valid.
// Any length-4 window exceeds 4.
// Best valid length = 3
string[] words2 = { "a", "bb", "c", "ddd", "e" };
int maxChars2 = 4;
int result2 = solution.LongestCaptionDraft(words2, maxChars2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick demo
string[] words3 = { "hello", "a", "bc", "d", "efg" };
int maxChars3 = 6;
// Valid best window could be ["a","bc","d"] = 1 + 2 + 1 = 4, length 3
int result3 = solution.LongestCaptionDraft(words3, maxChars3);
Console.WriteLine($"Additional Demo Result: {result3}");