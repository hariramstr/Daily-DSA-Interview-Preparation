/*
Title: Longest Workout Plan Within Heart Rate Drift
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array heartRate where heartRate[i] is the athlete's heart rate recorded during the i-th minute of a workout.
A workout segment is considered stable if the difference between the maximum and minimum heart rate inside that contiguous
segment is at most limit.

Your task is to return the length of the longest stable contiguous segment.

In other words, find the maximum window size such that for some indices l and r, the subarray heartRate[l...r] satisfies:
max(heartRate[l...r]) - min(heartRate[l...r]) <= limit.

This problem models fitness tracking systems that try to identify the longest period of steady exertion without large spikes
or drops in heart rate.

Constraints:
- 1 <= heartRate.length <= 100000
- 1 <= heartRate[i] <= 1000000000
- 0 <= limit <= 1000000000

This is the classic "longest continuous subarray with absolute diff less than or equal to limit" problem.
An O(n^2) solution would be too slow for up to 100000 elements, so we use a sliding window with two monotonic deques.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n)
    Each element is added to and removed from each deque at most once.

    Space Complexity:
    O(n)
    In the worst case, the deques can together store up to n indices.

    Beginner-friendly idea:
    We maintain a sliding window [left..right].
    For every new right position, we want to know the maximum and minimum values inside the current window quickly.

    To do that efficiently:
    - maxDeque stores indices in decreasing value order, so the front always points to the maximum value in the window.
    - minDeque stores indices in increasing value order, so the front always points to the minimum value in the window.

    If max - min becomes larger than limit, the window is invalid, so we move left forward until it becomes valid again.
    During that process, if the leftmost index leaves the window, we remove it from the corresponding deque front.
    */
    public int LongestStableSegment(int[] heartRate, int limit)
    {
        // This deque will store indices of elements in decreasing order of heartRate values.
        // Why indices instead of values?
        // Because when the left side of the window moves forward, we need to know whether
        // the element at the front is no longer inside the window.
        LinkedList<int> maxDeque = new LinkedList<int>();

        // This deque will store indices of elements in increasing order of heartRate values.
        // The front of this deque will always be the index of the minimum element in the current window.
        LinkedList<int> minDeque = new LinkedList<int>();

        // left is the starting index of our sliding window.
        int left = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Expand the window one element at a time by moving right from 0 to n - 1.
        for (int right = 0; right < heartRate.Length; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Insert heartRate[right] into maxDeque
            // ------------------------------------------------------------
            // We want maxDeque to remain in decreasing order of values.
            // That means:
            // - If the new value is greater than values at the back,
            //   those smaller values can never become the maximum while the new value remains in the window.
            // - So we remove them from the back.
            while (maxDeque.Count > 0 && heartRate[maxDeque.Last!.Value] < heartRate[right])
            {
                maxDeque.RemoveLast();
            }

            // Now append the current index.
            // After this, the deque is still decreasing by value.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Insert heartRate[right] into minDeque
            // ------------------------------------------------------------
            // We want minDeque to remain in increasing order of values.
            // That means:
            // - If the new value is smaller than values at the back,
            //   those larger values can never become the minimum while the new value remains in the window.
            // - So we remove them from the back.
            while (minDeque.Count > 0 && heartRate[minDeque.Last!.Value] > heartRate[right])
            {
                minDeque.RemoveLast();
            }

            // Append the current index.
            // After this, the deque is still increasing by value.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Shrink the window while it is invalid
            // ------------------------------------------------------------
            // The current maximum is at maxDeque.First
            // The current minimum is at minDeque.First
            //
            // If max - min > limit, then the current window [left..right] is not stable.
            // We must move left forward until the window becomes valid again.
            while (heartRate[maxDeque.First!.Value] - heartRate[minDeque.First!.Value] > limit)
            {
                // If the element leaving the window is exactly the current maximum,
                // remove it from the front of maxDeque.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                // If the element leaving the window is exactly the current minimum,
                // remove it from the front of minDeque.
                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Move the left boundary rightward to shrink the window.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 4: Update the best answer
            // ------------------------------------------------------------
            // At this point, the window [left..right] is guaranteed to be valid.
            // So we compute its length and compare it with the best answer found so far.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        // best now contains the length of the longest stable contiguous segment.
        return best;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt.
// Correct analysis:
// [120, 123, 121] => max 123, min 120, diff 3 => valid
// [123, 121, 126] => max 126, min 121, diff 5 => invalid
// [126, 124, 122] => max 126, min 122, diff 4 => valid
// Longest valid length is 3
int[] heartRate1 = { 120, 123, 121, 126, 124, 122 };
int limit1 = 4;
int result1 = solution.LongestStableSegment(heartRate1, limit1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2 from the prompt.
// Correct analysis:
// Entire array [98, 100, 101, 99, 102, 100, 99] has max 102 and min 98 => diff 4 => invalid
// One longest valid segment is [100, 101, 99, 102, 100] => max 102, min 99 => diff 3 => valid
// Length is 5
int[] heartRate2 = { 98, 100, 101, 99, 102, 100, 99 };
int limit2 = 3;
int result2 = solution.LongestStableSegment(heartRate2, limit2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional quick sanity checks
int[] heartRate3 = { 5 };
int limit3 = 0;
Console.WriteLine($"Single Element Result: {solution.LongestStableSegment(heartRate3, limit3)}");

int[] heartRate4 = { 1, 1, 1, 1 };
int limit4 = 0;
Console.WriteLine($"All Equal Result: {solution.LongestStableSegment(heartRate4, limit4)}");

int[] heartRate5 = { 1, 10, 1, 10, 1 };
int limit5 = 0;
Console.WriteLine($"Alternating Result: {solution.LongestStableSegment(heartRate5, limit5)}");