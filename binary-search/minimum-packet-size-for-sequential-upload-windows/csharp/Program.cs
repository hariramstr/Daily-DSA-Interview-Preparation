/*
Title: Minimum Packet Size for Sequential Upload Windows

Problem Description:
A media company needs to upload a sequence of video clips in the given order. The i-th clip has size clips[i] megabytes. The uploader sends data in fixed-size packets, and every packet can carry at most P megabytes. A single clip may be split across multiple packets, but packets cannot mix data from different upload windows.

You are also given an integer w, the maximum number of upload windows available. In one upload window, the company uploads a contiguous group of clips in order, and the total size of all clips assigned to that window must be fully transmitted using packets of size P. If the total size of a window is S, then that window consumes ceil(S / P) packets. However, each upload window is allowed to use at most one packet. That means the total size of clips placed in any single window must be at most P.

Your task is to find the minimum integer packet size P such that all clips can be partitioned into at most w contiguous upload windows, with each window having total size at most P.

In other words, choose the smallest possible P so the array can be split into at most w contiguous parts, and the sum of each part does not exceed P.

Return that minimum packet size.

Constraints:
- 1 <= clips.length <= 100000
- 1 <= clips[i] <= 1000000000
- 1 <= w <= clips.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: clips = [8, 3, 5, 7, 2], w = 3
Output: 10
Explanation: One optimal partition is [8], [3,5], [7,2]. The window sums are 8, 8, and 9, so packet size 10 is sufficient. Any packet size smaller than 10 fails because with P = 9, the clips would require more than 3 contiguous windows.

Example 2:
Input: clips = [4, 4, 4, 4], w = 2
Output: 8
Explanation: Split the clips as [4,4] and [4,4]. Each window has total size 8, so P = 8 works. P = 7 is impossible because no window may exceed 7, forcing at least 4 windows.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n log S), where:
      n = number of clips
      S = sum of all clip sizes
    Explanation:
    - We binary search the answer P between:
      lower bound = max(clips)
      upper bound = sum(clips)
    - For each candidate P, we scan the array once to check whether all clips
      can be split into at most w contiguous windows.
    - That feasibility check is O(n).
    - Binary search performs O(log S) checks.

    Space Complexity:
    - O(1) extra space
    Explanation:
    - We only use a few variables.
    - No extra arrays or advanced data structures are needed.
    */
    public long MinimumPacketSize(int[] clips, int w)
    {
        // Step 1:
        // Establish the binary search range for the answer.
        //
        // Why do we need a range?
        // Because we are searching for the smallest packet size P that works.
        // Binary search only works when we know:
        // 1) a lower bound that definitely cannot be smaller than the answer
        // 2) an upper bound that definitely can contain the answer
        //
        // Lower bound:
        // The packet size must be at least the size of the largest single clip.
        // Even though the story mentions clips may be split across packets in general,
        // the actual simplified condition of the problem is:
        // each upload window must have total size <= P.
        // Since every clip must belong to some window, and a window containing that clip
        // must have sum at least clip size, P cannot be smaller than the largest clip.
        //
        // Upper bound:
        // If we choose P = sum of all clips, then we can place everything into one window.
        // Since w >= 1, that is always valid.
        long left = 0;
        long right = 0;

        foreach (int clip in clips)
        {
            if (clip > left)
            {
                left = clip;
            }

            right += clip;
        }

        // Step 2:
        // Perform binary search on packet size P.
        //
        // Key monotonic property:
        // - If a packet size P works, then any larger packet size also works.
        // - If a packet size P does NOT work, then any smaller packet size also does NOT work.
        //
        // This "false false false ... true true true" pattern is exactly what binary search needs.
        while (left < right)
        {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            long mid = left + (right - left) / 2;

            // Step 3:
            // Check whether this candidate packet size 'mid' is sufficient.
            //
            // If it is sufficient, we try to find an even smaller valid answer,
            // so we move the right boundary down to mid.
            //
            // If it is not sufficient, then we must increase packet size,
            // so we move the left boundary up to mid + 1.
            if (CanSplitIntoAtMostWWindows(clips, w, mid))
            {
                right = mid;
            }
            else
            {
                left = mid + 1;
            }
        }

        // When binary search ends, left == right, and that value is the minimum valid packet size.
        return left;
    }

    private bool CanSplitIntoAtMostWWindows(int[] clips, int w, long maxWindowSum)
    {
        // This helper answers:
        // "If each window is allowed to have total size at most maxWindowSum,
        // can we partition the clips into at most w contiguous windows?"
        //
        // We use a greedy scan.
        //
        // Why greedy works:
        // We always keep adding clips to the current window as long as doing so
        // does not exceed maxWindowSum.
        //
        // The moment adding the next clip would exceed the limit, we MUST start
        // a new window there.
        //
        // This is optimal for minimizing the number of windows because:
        // - making a window smaller than necessary never helps reduce the number of windows
        // - packing each window as much as possible gives the fewest windows for this limit
        //
        // Data structure choice:
        // We do not need any extra data structure.
        // A simple running sum and a window counter are enough.

        // Start with one window, because if there is at least one clip,
        // we need at least one contiguous part.
        int windowsUsed = 1;

        // This stores the current total size of the active window.
        long currentWindowSum = 0;

        foreach (int clip in clips)
        {
            // Safety check:
            // If a single clip is larger than maxWindowSum, then it is impossible
            // to place that clip into any valid window.
            //
            // In our binary search, maxWindowSum is always >= max(clips),
            // so this condition should normally not trigger.
            // Still, keeping it here makes the helper robust and self-contained.
            if (clip > maxWindowSum)
            {
                return false;
            }

            // Try to add this clip to the current window.
            if (currentWindowSum + clip <= maxWindowSum)
            {
                // It fits, so we extend the current window.
                currentWindowSum += clip;
            }
            else
            {
                // It does not fit.
                // Therefore, we must start a new window beginning with this clip.
                windowsUsed++;
                currentWindowSum = clip;

                // Early exit optimization:
                // If we already used more than w windows, then this packet size fails.
                if (windowsUsed > w)
                {
                    return false;
                }
            }
        }

        // If we finished scanning and used at most w windows, then this packet size works.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] clips1 = { 8, 3, 5, 7, 2 };
int w1 = 3;
long result1 = solution.MinimumPacketSize(clips1, w1);
Console.WriteLine(result1); // Expected: 10

// Example 2
int[] clips2 = { 4, 4, 4, 4 };
int w2 = 2;
long result2 = solution.MinimumPacketSize(clips2, w2);
Console.WriteLine(result2); // Expected: 8

// Additional quick checks
int[] clips3 = { 1, 2, 3, 4, 5 };
int w3 = 2;
long result3 = solution.MinimumPacketSize(clips3, w3);
Console.WriteLine(result3); // Expected: 9 -> [1,2,3] and [4,5]

int[] clips4 = { 10 };
int w4 = 1;
long result4 = solution.MinimumPacketSize(clips4, w4);
Console.WriteLine(result4); // Expected: 10