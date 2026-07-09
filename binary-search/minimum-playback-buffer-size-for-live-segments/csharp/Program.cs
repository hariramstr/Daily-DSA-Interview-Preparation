/*
Title: Minimum Playback Buffer Size for Live Segments
Difficulty: Medium
Topic: Binary Search

Problem Description:
A video platform is preparing a live event made of n consecutive segments. Segment i has size
segments[i] megabytes. The player downloads data into a temporary buffer before each segment begins,
and the same fixed buffer size must be used for the entire event.

If a segment is larger than the buffer, it cannot be played.

The platform is allowed to split the event into at most k playback sessions. Each session must
contain a contiguous group of segments, and the total size of all segments assigned to a single
session cannot exceed the buffer size.

Your task is to compute the minimum buffer size required so that all segments can be played in order
using at most k sessions.

In other words, partition the array into at most k contiguous parts while minimizing the maximum
part sum.

Return that minimum possible buffer size.

Constraints:
- 1 <= n <= 100000
- 1 <= segments[i] <= 1000000000
- 1 <= k <= n
- The answer fits in a 64-bit signed integer.

Example 1:
Input: segments = [8, 3, 5, 7, 2], k = 3
Output: 9

Explanation:
A valid partition with maximum session sum 9 is:
[8], [3, 5], [7, 2]
The session sums are 8, 8, and 9, so buffer size 9 works.
No smaller value can satisfy the requirement with at most 3 sessions.

Example 2:
Input: segments = [4, 4, 4, 4], k = 2
Output: 8

Explanation:
We can split as [4,4] and [4,4].
The largest session sum is 8, and no smaller buffer can fit all segments into only 2 contiguous sessions.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n * log(S)), where:
      n = number of segments
      S = range of possible answers = (sum of segments - max segment + 1)
    Why:
    - We binary search the answer.
    - For each candidate buffer size, we scan the array once to check feasibility.

    Space Complexity:
    - O(1) extra space
    Why:
    - We only use a few variables and do not allocate extra arrays proportional to input size.
    */
    public long MinimumBufferSize(int[] segments, int k)
    {
        // Step 1:
        // Establish the binary search boundaries.
        //
        // Why do we need boundaries?
        // Binary search works only when we know the search interval [left, right].
        // Here, we are searching for the minimum feasible buffer size.
        //
        // The smallest possible answer cannot be less than the largest single segment,
        // because every segment must fit into the buffer by itself.
        //
        // The largest possible answer is the sum of all segments,
        // because one session containing the entire event would need that much buffer.
        long left = 0;
        long right = 0;

        foreach (int size in segments)
        {
            // left must be at least the maximum segment size.
            if (size > left)
            {
                left = size;
            }

            // right is the total sum of all segments.
            right += size;
        }

        // Step 2:
        // Perform binary search on the answer.
        //
        // Key idea:
        // If a buffer size X is feasible, then any larger buffer size is also feasible.
        // This creates a monotonic "true/false" pattern:
        // small values -> not feasible
        // large values -> feasible
        //
        // That monotonic property is exactly what binary search needs.
        while (left < right)
        {
            // Compute the middle candidate carefully using long arithmetic.
            // This avoids overflow that could happen with (left + right) / 2.
            long mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this candidate buffer size is enough.
            //
            // If feasible:
            //   mid might be the answer, but maybe we can do even smaller.
            //   So we keep searching on the left half, including mid.
            //
            // If not feasible:
            //   mid is too small, so we must search strictly larger values.
            if (CanSplitIntoAtMostKSessions(segments, k, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // When left == right, binary search has converged to the minimum feasible value.
        return left;
    }

    private bool CanSplitIntoAtMostKSessions(int[] segments, int k, long bufferSize)
    {
        // This helper method answers:
        // "If the buffer size is exactly bufferSize, can we play all segments
        //  using at most k contiguous sessions?"
        //
        // We use a greedy scan from left to right.
        //
        // Greedy rule:
        // Put as many consecutive segments as possible into the current session
        // without exceeding bufferSize.
        //
        // Why is this greedy strategy correct?
        // Because for a fixed maximum allowed session sum, delaying a split as long as possible
        // minimizes the number of sessions used.
        // If even this best greedy packing needs more than k sessions, then bufferSize is not feasible.

        // We start with one session because if there is at least one segment,
        // we need at least one session to hold it.
        int sessionsUsed = 1;

        // currentSessionSum stores the total size of the segments currently placed
        // into the ongoing session.
        long currentSessionSum = 0;

        foreach (int size in segments)
        {
            // Safety check:
            // If any single segment is larger than bufferSize, then this candidate is impossible.
            // In practice, our binary search lower bound already prevents this,
            // but keeping this check makes the helper method robust and self-contained.
            if (size > bufferSize)
            {
                return false;
            }

            // Try to add the current segment to the current session.
            if (currentSessionSum + size <= bufferSize)
            {
                // It fits, so we extend the current session.
                currentSessionSum += size;
            }
            else
            {
                // It does not fit.
                //
                // Therefore, we must start a new session beginning with this segment.
                sessionsUsed++;
                currentSessionSum = size;

                // Early exit optimization:
                // If we already need more than k sessions, there is no reason to continue scanning.
                if (sessionsUsed > k)
                {
                    return false;
                }
            }
        }

        // If we finished scanning and used at most k sessions, the candidate buffer size works.
        return true;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1:
// segments = [8, 3, 5, 7, 2], k = 3
// Expected answer: 9
int[] segments1 = { 8, 3, 5, 7, 2 };
int k1 = 3;
long result1 = solution.MinimumBufferSize(segments1, k1);
Console.WriteLine($"Example 1 Result: {result1}");

// Example 2:
// segments = [4, 4, 4, 4], k = 2
// Expected answer: 8
int[] segments2 = { 4, 4, 4, 4 };
int k2 = 2;
long result2 = solution.MinimumBufferSize(segments2, k2);
Console.WriteLine($"Example 2 Result: {result2}");

// Additional demo:
// If k equals n, each segment can be its own session,
// so the answer should be the maximum segment size.
int[] segments3 = { 2, 10, 3, 6 };
int k3 = 4;
long result3 = solution.MinimumBufferSize(segments3, k3);
Console.WriteLine($"Additional Example Result: {result3}");