/*
Title: Longest Subtitle Segment Within Reading Burst Limit
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array words, where words[i] is the number of words shown in the i-th subtitle frame of a video.
A viewer can comfortably follow a contiguous segment only if:
1. The total number of words in that segment does not exceed maxWords
2. The segment contains at most k dense frames

A frame is considered dense if words[i] >= denseThreshold.

Return the length of the longest contiguous segment of subtitle frames that satisfies both conditions.

This models a real video playback system where subtitle readability depends on both the overall reading load
and the number of especially text-heavy frames. The answer should be the maximum number of consecutive frames
the viewer can watch without exceeding the reading burst limit.

Constraints:
- 1 <= words.length <= 200000
- 0 <= words[i] <= 100000
- 0 <= maxWords <= 10^14
- 0 <= k <= words.length
- 1 <= denseThreshold <= 100000

Example 1:
Input: words = [4, 9, 3, 8, 2, 7], maxWords = 20, k = 2, denseThreshold = 7
Output: 4
Explanation:
- [9, 3, 8, 2] has total 22, so it is invalid.
- [3, 8, 2, 7] has total 20 and two dense frames (8 and 7), so length 4 is valid.

Example 2:
Input: words = [6, 1, 7, 2, 9, 1, 1], maxWords = 10, k = 1, denseThreshold = 6
Output: 3
Explanation:
- [1, 7, 2] has total 10 and one dense frame, so it is valid.
- Any longer valid segment would violate either the total words limit or the dense frame limit.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Why O(n)?
    - Each element is added to the sliding window exactly once when the right pointer moves forward.
    - Each element is removed from the sliding window at most once when the left pointer moves forward.
    - Therefore, both pointers together move at most 2 * n times, which is linear.

    Why O(1) extra space?
    - We only store a few variables: left pointer, running sum, dense frame count, and best answer.
    - We do not use any extra arrays, hash maps, or other data structures proportional to input size.
    */
    public int LongestReadableSegment(int[] words, long maxWords, int k, int denseThreshold)
    {
        // This pointer marks the left boundary of our current window.
        // The window always represents a contiguous segment: words[left..right].
        int left = 0;

        // This stores the total number of words currently inside the window.
        // We use long because:
        // - words[i] can be as large as 100000
        // - length can be up to 200000
        // - total can therefore exceed int range in some scenarios
        long currentSum = 0;

        // This stores how many frames in the current window are "dense",
        // meaning words[i] >= denseThreshold.
        int denseCount = 0;

        // This stores the best (maximum) valid window length found so far.
        int bestLength = 0;

        // We expand the window by moving the right pointer from left to right across the array.
        for (int right = 0; right < words.Length; right++)
        {
            // STEP 1: Include the new frame at index "right" into the window.
            // We add its word count to the running total because the window now contains this frame.
            currentSum += words[right];

            // If this new frame is dense, we must also update the dense frame count.
            // This is necessary because one of our constraints is based on how many dense frames
            // are inside the current segment.
            if (words[right] >= denseThreshold)
            {
                denseCount++;
            }

            // STEP 2: If the window violates either rule, shrink it from the left.
            //
            // The window is invalid if:
            // - currentSum > maxWords  OR
            // - denseCount > k
            //
            // We use a while loop, not an if statement, because removing just one element
            // may still leave the window invalid. We must keep shrinking until it becomes valid.
            while (currentSum > maxWords || denseCount > k)
            {
                // Before moving left forward, remove the contribution of words[left]
                // from the current window.
                currentSum -= words[left];

                // If the frame leaving the window was dense, we must decrement denseCount.
                // This keeps our dense frame count accurate for the new smaller window.
                if (words[left] >= denseThreshold)
                {
                    denseCount--;
                }

                // Finally, move the left boundary one step to the right.
                // This effectively removes the old leftmost frame from the window.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed to be valid.
            // That means:
            // - total words <= maxWords
            // - dense frames <= k
            //
            // So we can safely compute its length and compare it with the best answer.
            int currentLength = right - left + 1;

            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // After processing all possible right endpoints, bestLength contains
        // the maximum valid contiguous segment length.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] words1 = { 4, 9, 3, 8, 2, 7 };
long maxWords1 = 20;
int k1 = 2;
int denseThreshold1 = 7;

int result1 = solution.LongestReadableSegment(words1, maxWords1, k1, denseThreshold1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] words2 = { 6, 1, 7, 2, 9, 1, 1 };
long maxWords2 = 10;
int k2 = 1;
int denseThreshold2 = 6;

int result2 = solution.LongestReadableSegment(words2, maxWords2, k2, denseThreshold2);
Console.WriteLine(result2); // Expected: 3

// Additional quick sanity checks

int[] words3 = { 1, 2, 3, 4 };
Console.WriteLine(solution.LongestReadableSegment(words3, 100, 4, 10)); // Expected: 4

int[] words4 = { 10, 10, 10 };
Console.WriteLine(solution.LongestReadableSegment(words4, 5, 3, 10)); // Expected: 0

int[] words5 = { 7, 7, 7, 1, 1 };
Console.WriteLine(solution.LongestReadableSegment(words5, 30, 2, 7)); // Expected: 4