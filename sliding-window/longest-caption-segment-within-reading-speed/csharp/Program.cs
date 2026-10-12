/*
Title: Longest Caption Segment Within Reading Speed
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A video platform stores the number of words shown in each consecutive subtitle frame in an integer array `words`,
where `words[i]` is the word count displayed during the `i`-th frame interval.

A viewer has a reading capacity limit `maxWords`, meaning they can comfortably follow any contiguous segment
of subtitle frames only if the total number of words shown in that segment does not exceed `maxWords`.

Your task is to return the length of the longest contiguous segment of frames the viewer can watch comfortably.
In other words, find the maximum size of a subarray whose sum is less than or equal to `maxWords`.

This problem should be solved efficiently for large inputs. A brute-force solution that checks every possible
segment will be too slow. Since all values in `words` are non-negative, a sliding window approach is expected.

Constraints:
- 1 <= words.length <= 100000
- 0 <= words[i] <= 10000
- 0 <= maxWords <= 10^9

Example 1:
Input: words = [3, 1, 2, 1, 1, 4], maxWords = 5
Output: 4
Explanation: The longest valid segment is [1, 2, 1, 1], whose total is 5, so the answer is 4.

Example 2:
Input: words = [5, 2, 2, 6, 1], maxWords = 7
Output: 2
Explanation: Valid longest segments include [5, 2] and [6, 1], each with total 7.
No contiguous segment of length 3 or more stays within the reading limit.

Goal:
Return only the maximum number of consecutive frame intervals that can be included in such a segment.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each element is added to the window once by the right pointer.
    - Each element is removed from the window at most once by the left pointer.
    - Therefore, the total amount of work is linear in the size of the array.

    Space Complexity: O(1)
    - We only use a few extra variables: left pointer, running sum, and best length.
    - No extra data structures proportional to input size are needed.
    */
    public int LongestCaptionSegment(int[] words, int maxWords)
    {
        // This pointer marks the beginning of our current sliding window.
        // The window will always represent a contiguous segment: words[left..right].
        int left = 0;

        // This stores the sum of all values currently inside the window.
        // We use long instead of int to be extra safe when summing many values,
        // even though the given constraints would still fit in int.
        long currentSum = 0;

        // This keeps track of the maximum valid window length we have seen so far.
        int maxLength = 0;

        // We expand the window one step at a time by moving the right pointer.
        // For every right index, we include words[right] into the current segment.
        for (int right = 0; right < words.Length; right++)
        {
            // Step 1: Expand the window to include the new rightmost element.
            // Why this is necessary:
            // We want to consider every possible contiguous segment ending at 'right'.
            // By adding words[right], we grow the current segment by one frame interval.
            currentSum += words[right];

            // Step 2: If the window has become invalid (sum > maxWords),
            // we must shrink it from the left until it becomes valid again.
            //
            // Why shrinking from the left works:
            // All values are non-negative. That means adding more elements can only
            // increase or keep the sum the same, never decrease it.
            // So once the sum is too large, the only way to fix it is to remove
            // elements from the beginning of the window.
            while (currentSum > maxWords && left <= right)
            {
                // Remove the leftmost element from the running sum because
                // that element is no longer part of the window after we move left forward.
                currentSum -= words[left];

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3: At this point, the window words[left..right] is guaranteed valid:
            // currentSum <= maxWords.
            //
            // So we compute its length and compare it with the best answer found so far.
            int currentLength = right - left + 1;

            // If this valid window is longer than any previous valid window,
            // update our answer.
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After checking all possible right endpoints, maxLength contains
        // the length of the longest valid contiguous segment.
        return maxLength;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] words1 = { 3, 1, 2, 1, 1, 4 };
int maxWords1 = 5;
int result1 = solution.LongestCaptionSegment(words1, maxWords1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] words2 = { 5, 2, 2, 6, 1 };
int maxWords2 = 7;
int result2 = solution.LongestCaptionSegment(words2, maxWords2);
Console.WriteLine(result2); // Expected: 2

// Additional quick checks
int[] words3 = { 0, 0, 0, 0 };
int maxWords3 = 0;
int result3 = solution.LongestCaptionSegment(words3, maxWords3);
Console.WriteLine(result3); // Expected: 4

int[] words4 = { 10, 1, 1 };
int maxWords4 = 2;
int result4 = solution.LongestCaptionSegment(words4, maxWords4);
Console.WriteLine(result4); // Expected: 2