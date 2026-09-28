/*
Title: Shortest Playlist Segment With Mood Coverage and Replay Caps
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A music streaming team is analyzing a generated playlist. Each song belongs to exactly one mood category,
represented by an integer in the array moods, where moods[i] is the mood of the i-th song.

You are also given a dictionary required where required[x] is the minimum number of songs of mood x
that must appear in a segment, and an integer cap.

Find the length of the shortest contiguous segment of the playlist such that:
1. For every mood x in required, the segment contains at least required[x] songs of mood x.
2. No mood appears more than cap times inside the segment, including moods that are not listed in required.

Return the minimum possible length of such a segment, or -1 if no valid segment exists.

Why this is tricky:
- We need LOWER bounds for some moods: "at least required[x]"
- We also need an UPPER bound for every mood: "at most cap"
- When we expand the window to satisfy missing required moods, we may accidentally violate the cap
- When we shrink the window to fix the cap or minimize the answer, we may lose required coverage

Key idea:
Use a sliding window with two pointers:
- Expand the right pointer to include more songs
- If any mood count becomes greater than cap, move the left pointer until the cap is restored
- Track how many required moods are currently satisfied
- Whenever all required moods are satisfied and the cap condition already holds, try shrinking
  from the left to get the shortest valid window ending at the current right

This works because:
- Each element enters the window once
- Each element leaves the window once
- So the total work is linear, aside from dictionary operations

Example 1:
moods = [4,1,2,1,3,2,1,4], required = {1:2, 2:1, 3:1}, cap = 3
A shortest valid segment is [1,2,1,3,2], length = 5

Example 2:
moods = [5,5,1,2,5,3,1,2], required = {1:1, 2:1, 3:1}, cap = 2
A shortest valid segment is [2,5,3,1], length = 4
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is moods.Length

    Why O(n)?
    - The right pointer moves from left to right exactly once
    - The left pointer also moves from left to right at most once per element
    - Dictionary operations are average O(1)

    Space Complexity:
    O(k), where k is the number of distinct moods that appear in the current counting structures

    More precisely:
    - We store counts for moods seen in the window
    - We store the required dictionary
    - So memory depends on the number of distinct mood values, not on the numeric range of mood IDs
    */
    public int ShortestSegmentWithCoverageAndCap(int[] moods, Dictionary<int, int> required, int cap)
    {
        // Defensive check:
        // If the playlist is empty, no valid segment can exist.
        if (moods == null || moods.Length == 0)
        {
            return -1;
        }

        // Another useful early check:
        // If any required minimum is larger than cap, then it is impossible.
        // Reason:
        // - The segment must contain at least required[x] copies of mood x
        // - But no mood may appear more than cap times
        // So if required[x] > cap for any x, there is no solution.
        foreach (var pair in required)
        {
            if (pair.Value > cap)
            {
                return -1;
            }
        }

        // windowCounts[mood] = how many times this mood currently appears in the sliding window
        // We use Dictionary<int, int> because mood values can be as large as 1e9,
        // so array indexing by mood value is not possible.
        var windowCounts = new Dictionary<int, int>();

        // totalRequiredKinds = how many distinct moods have minimum requirements
        int totalRequiredKinds = required.Count;

        // satisfiedKinds = how many required moods currently meet their minimum count inside the window
        //
        // Example:
        // required = {1:2, 2:1, 3:1}
        // If current window counts are {1:2, 2:1, 3:0}, then satisfiedKinds = 2
        int satisfiedKinds = 0;

        // left pointer of the sliding window
        int left = 0;

        // answer initialized to "infinity"
        int best = int.MaxValue;

        // We expand the window by moving "right" from left to right.
        for (int right = 0; right < moods.Length; right++)
        {
            int addedMood = moods[right];

            // STEP 1: Add the new rightmost song into the window count.
            //
            // Why?
            // Because the sliding window now includes moods[right].
            if (!windowCounts.ContainsKey(addedMood))
            {
                windowCounts[addedMood] = 0;
            }
            windowCounts[addedMood]++;

            // STEP 2: If this mood is one of the required moods,
            // check whether adding it just made that requirement become satisfied.
            //
            // Important subtlety:
            // We only increase satisfiedKinds when the count becomes EXACTLY equal to required[addedMood].
            // Why exactly?
            // - Suppose required[1] = 2
            // - If count goes from 1 -> 2, requirement becomes newly satisfied, so satisfiedKinds++
            // - If count goes from 2 -> 3, it was already satisfied before, so we must NOT increment again
            if (required.TryGetValue(addedMood, out int neededForAddedMood))
            {
                if (windowCounts[addedMood] == neededForAddedMood)
                {
                    satisfiedKinds++;
                }
            }

            // STEP 3: Enforce the global cap condition.
            //
            // The problem says NO mood may appear more than cap times in the segment.
            // Since we only added one mood at the right side, the only possible cap violation
            // introduced by this step is for "addedMood".
            //
            // Why only addedMood?
            // - All other mood counts stayed unchanged
            // - If they were <= cap before, they still are
            //
            // So while addedMood exceeds cap, we must move left forward and remove songs
            // until the count of addedMood is back within the allowed limit.
            while (windowCounts[addedMood] > cap)
            {
                int removedMood = moods[left];

                // Before decreasing the count, if removedMood is required and is currently
                // exactly at its required threshold, then removing one will make it fall below
                // the threshold. That means one previously satisfied required mood becomes unsatisfied.
                if (required.TryGetValue(removedMood, out int neededForRemovedMood))
                {
                    if (windowCounts[removedMood] == neededForRemovedMood)
                    {
                        satisfiedKinds--;
                    }
                }

                // Actually remove the leftmost song from the window.
                windowCounts[removedMood]--;

                // Optional cleanup:
                // If a count becomes zero, we can remove the key from the dictionary.
                // This is not required for correctness, but it keeps the dictionary smaller.
                if (windowCounts[removedMood] == 0)
                {
                    windowCounts.Remove(removedMood);
                }

                left++;
            }

            // At this point, the cap condition is guaranteed to hold for the entire window.
            //
            // Why?
            // - Before adding addedMood, the window already respected the cap
            // - After adding, only addedMood could violate it
            // - The while loop fixed that violation
            //
            // So now the window satisfies the upper-bound rule for every mood.

            // STEP 4: If all required moods are satisfied, then the current window is valid.
            // We should try to shrink it from the left to make it as short as possible.
            //
            // Condition for validity:
            // satisfiedKinds == totalRequiredKinds
            //
            // Since the cap condition already holds, this means the whole window is valid.
            while (satisfiedKinds == totalRequiredKinds)
            {
                // Update the best answer using the current valid window [left..right].
                int currentLength = right - left + 1;
                if (currentLength < best)
                {
                    best = currentLength;
                }

                // Now try removing moods[left] to see whether we can keep validity
                // while making the window shorter.
                int removedMood = moods[left];

                // If removedMood is required and its count is exactly at the threshold,
                // then removing it will break the requirement for that mood.
                // So we must decrease satisfiedKinds before the count is decremented.
                if (required.TryGetValue(removedMood, out int neededForRemovedMood))
                {
                    if (windowCounts[removedMood] == neededForRemovedMood)
                    {
                        satisfiedKinds--;
                    }
                }

                // Remove the leftmost mood from the window.
                windowCounts[removedMood]--;
                if (windowCounts[removedMood] == 0)
                {
                    windowCounts.Remove(removedMood);
                }

                left++;

                // After this removal:
                // - The cap condition still holds automatically, because removing elements
                //   can never create a "count > cap" violation
                // - But the required coverage may no longer hold, which is why the loop
                //   stops when satisfiedKinds drops below totalRequiredKinds
            }
        }

        // If best was never updated, no valid segment exists.
        return best == int.MaxValue ? -1 : best;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] moods1 = { 4, 1, 2, 1, 3, 2, 1, 4 };
var required1 = new Dictionary<int, int>
{
    [1] = 2,
    [2] = 1,
    [3] = 1
};
int cap1 = 3;

int result1 = solution.ShortestSegmentWithCoverageAndCap(moods1, required1, cap1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] moods2 = { 5, 5, 1, 2, 5, 3, 1, 2 };
var required2 = new Dictionary<int, int>
{
    [1] = 1,
    [2] = 1,
    [3] = 1
};
int cap2 = 2;

int result2 = solution.ShortestSegmentWithCoverageAndCap(moods2, required2, cap2);
Console.WriteLine(result2); // Expected: 4

// Additional quick checks

// No solution because required mood 3 never appears
int[] moods3 = { 1, 2, 1, 2 };
var required3 = new Dictionary<int, int>
{
    [1] = 1,
    [3] = 1
};
int cap3 = 2;

int result3 = solution.ShortestSegmentWithCoverageAndCap(moods3, required3, cap3);
Console.WriteLine(result3); // Expected: -1

// Exact cap usage is allowed
int[] moods4 = { 7, 7, 8, 9 };
var required4 = new Dictionary<int, int>
{
    [7] = 2,
    [8] = 1
};
int cap4 = 2;

int result4 = solution.ShortestSegmentWithCoverageAndCap(moods4, required4, cap4);
Console.WriteLine(result4); // Expected: 3