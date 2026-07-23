/*
Title: Shortest Transcript Span Covering Speaker Quotas
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a meeting transcript represented by an array speakers, where speakers[i] is the speaker ID
of the person who spoke the i-th utterance. You are also given a list of quota requirements, where each
element is a pair [speakerId, minCount] meaning that a valid excerpt must contain at least minCount
utterances from that speaker.

Your task is to find the shortest contiguous span of the transcript that satisfies all speaker quotas.
If multiple spans have the same minimum length, return the one with the smallest starting index.
If no such span exists, return [-1, -1].

This problem models real interview scenarios such as extracting the smallest meeting segment that contains
enough participation from required stakeholders. The challenge is that speaker IDs may be large, repeated
many times, and only some speakers are constrained by quotas. An efficient solution should avoid checking
every subarray.

Return the answer as a pair [start, end] using 0-based indices.

Constraints:
- 1 <= speakers.length <= 2 * 10^5
- 1 <= requirements.length <= 10^5
- 1 <= speakerId <= 10^9
- 1 <= minCount <= speakers.length
- All speakerId values in requirements are distinct

Example 1:
Input:
speakers = [4, 2, 7, 2, 4, 2, 9, 7, 4]
requirements = [[2, 2], [4, 2], [7, 1]]
Output:
[0, 4]

Example 2:
Input:
speakers = [5, 1, 5, 3, 1, 5, 2, 3]
requirements = [[1, 2], [3, 2], [2, 1]]
Output:
[3, 7]
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the requirement map takes O(r), where r = requirements.Length.
    - The sliding window scans the speakers array with two pointers.
      Each element is added to the window at most once and removed at most once.
      Therefore the total sliding work is O(n), where n = speakers.Length.
    - Overall: O(n + r)

    Space Complexity:
    - We store requirement counts and current window counts only for required speakers.
    - Overall: O(r)
    */
    public int[] ShortestTranscriptSpan(int[] speakers, int[][] requirements)
    {
        // This dictionary stores the minimum required count for each constrained speaker.
        // Key   = speaker ID
        // Value = how many times that speaker must appear in a valid window
        //
        // We use Dictionary<int, int> because:
        // 1. Speaker IDs can be as large as 1e9, so using an array indexed by speaker ID is impossible.
        // 2. We only care about speakers mentioned in requirements, not every possible speaker.
        var requiredCounts = new Dictionary<int, int>();

        // Build the requirement map from the input pairs.
        for (int i = 0; i < requirements.Length; i++)
        {
            int speakerId = requirements[i][0];
            int minCount = requirements[i][1];
            requiredCounts[speakerId] = minCount;
        }

        // This dictionary stores how many times each required speaker currently appears
        // inside the active sliding window [left, right].
        //
        // We only track required speakers because non-required speakers do not affect validity.
        var windowCounts = new Dictionary<int, int>();

        // "needSatisfied" counts how many distinct required speakers currently meet their quota.
        //
        // Example:
        // If requirements are:
        //   speaker 2 needs 2
        //   speaker 4 needs 2
        //   speaker 7 needs 1
        // then totalRequiredTypes = 3.
        //
        // When the current window has:
        //   speaker 2 count >= 2
        //   speaker 4 count >= 2
        //   speaker 7 count >= 1
        // then needSatisfied == 3, meaning the window is valid.
        int needSatisfied = 0;
        int totalRequiredTypes = requiredCounts.Count;

        // Left pointer of the sliding window.
        int left = 0;

        // Best answer found so far.
        // We initialize bestLength to a very large value so that any real valid window is better.
        int bestStart = -1;
        int bestEnd = -1;
        int bestLength = int.MaxValue;

        // Expand the window by moving "right" from left to right across the transcript.
        for (int right = 0; right < speakers.Length; right++)
        {
            int currentSpeaker = speakers[right];

            // Step 1: Add the new rightmost speaker into the window.
            //
            // This matters only if the speaker is one of the required speakers.
            // If the speaker is not required, it can still be inside the window,
            // but it does not help or hurt quota satisfaction directly.
            if (requiredCounts.TryGetValue(currentSpeaker, out int requiredForThisSpeaker))
            {
                // Get the old count in the window, defaulting to 0 if not present yet.
                windowCounts.TryGetValue(currentSpeaker, out int oldCount);

                int newCount = oldCount + 1;
                windowCounts[currentSpeaker] = newCount;

                // Very important logic:
                // We increase needSatisfied only at the exact moment this speaker's count
                // reaches its required quota for the first time.
                //
                // Example:
                // requirement for speaker 2 is 2
                // oldCount = 1, newCount = 2  => quota just became satisfied, so increment
                // oldCount = 2, newCount = 3  => already satisfied before, do NOT increment again
                if (oldCount < requiredForThisSpeaker && newCount >= requiredForThisSpeaker)
                {
                    needSatisfied++;
                }
            }

            // Step 2: If all required speaker quotas are satisfied,
            // try to shrink the window from the left to make it as short as possible.
            //
            // This is the heart of the sliding window technique:
            // - Expand right until valid
            // - Then contract left while still valid
            //
            // This guarantees we examine only O(n) total pointer moves.
            while (needSatisfied == totalRequiredTypes && left <= right)
            {
                int currentLength = right - left + 1;

                // Update the best answer if:
                // 1. This window is shorter than the best one seen so far, or
                // 2. Same length but smaller starting index
                //
                // The second rule handles the tie-breaking requirement in the problem.
                if (currentLength < bestLength || (currentLength == bestLength && left < bestStart))
                {
                    bestLength = currentLength;
                    bestStart = left;
                    bestEnd = right;
                }

                int leftSpeaker = speakers[left];

                // We are about to remove speakers[left] from the window by moving left forward.
                // Again, only required speakers matter for validity tracking.
                if (requiredCounts.TryGetValue(leftSpeaker, out int requiredForLeftSpeaker))
                {
                    int oldCount = windowCounts[leftSpeaker];
                    int newCount = oldCount - 1;
                    windowCounts[leftSpeaker] = newCount;

                    // If removing this speaker causes its count to drop BELOW the required quota,
                    // then the window is no longer valid after this removal.
                    //
                    // Example:
                    // requirement for speaker 4 is 2
                    // oldCount = 2, newCount = 1 => quota broken, decrement needSatisfied
                    if (oldCount >= requiredForLeftSpeaker && newCount < requiredForLeftSpeaker)
                    {
                        needSatisfied--;
                    }
                }

                // Actually shrink the window.
                left++;
            }
        }

        // If bestStart is still -1, we never found any valid window.
        if (bestStart == -1)
        {
            return new[] { -1, -1 };
        }

        return new[] { bestStart, bestEnd };
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] speakers1 = { 4, 2, 7, 2, 4, 2, 9, 7, 4 };
int[][] requirements1 =
{
    new[] { 2, 2 },
    new[] { 4, 2 },
    new[] { 7, 1 }
};

int[] result1 = solution.ShortestTranscriptSpan(speakers1, requirements1);
Console.WriteLine($"Example 1 Result: [{result1[0]}, {result1[1]}]");

// Example 2
int[] speakers2 = { 5, 1, 5, 3, 1, 5, 2, 3 };
int[][] requirements2 =
{
    new[] { 1, 2 },
    new[] { 3, 2 },
    new[] { 2, 1 }
};

int[] result2 = solution.ShortestTranscriptSpan(speakers2, requirements2);
Console.WriteLine($"Example 2 Result: [{result2[0]}, {result2[1]}]");

// Additional demo: impossible case
int[] speakers3 = { 1, 2, 3, 4 };
int[][] requirements3 =
{
    new[] { 2, 1 },
    new[] { 5, 1 }
};

int[] result3 = solution.ShortestTranscriptSpan(speakers3, requirements3);
Console.WriteLine($"Example 3 Result: [{result3[0]}, {result3[1]}]");