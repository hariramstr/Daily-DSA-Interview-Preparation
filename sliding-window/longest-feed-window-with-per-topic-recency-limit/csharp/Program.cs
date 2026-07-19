/*
Title: Longest Feed Window With Per-Topic Recency Limit
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are building a ranking service for a social media feed. Each post belongs to a topic represented by an integer in the array topics, where topics[i] is the topic of the i-th post in chronological order. A feed window is any contiguous subarray of posts.

To keep the feed diverse, the platform enforces a recency rule: for every topic, the distance between any two consecutive appearances of that same topic inside the chosen window must be at most limit. In other words, if a topic appears at positions p1 < p2 < ... < pk within the window, then for every j, pj+1 - pj <= limit must hold. A topic that appears only once in the window always satisfies the rule.

Return the length of the longest contiguous feed window that satisfies this condition.

Note that this is not a global condition on the whole array. A pair of equal topics only matters if both occurrences are inside the same chosen window. Also, topics may be as large as 10^9, so solutions that depend on the numeric range of values are not acceptable.

Constraints:
- 1 <= topics.length <= 2 * 10^5
- 1 <= topics[i] <= 10^9
- 1 <= limit <= topics.length

Example 1:
Input: topics = [4, 1, 4, 2, 4, 3, 2], limit = 2
Output: 4
Explanation: The longest valid window is [4, 2, 4, 3] from indices 2 to 5. Inside this window, topic 4 appears at positions 2 and 4, whose distance is 2, which is allowed. Any longer window includes topic 2 at positions 3 and 6 with distance 3, violating the rule.

Example 2:
Input: topics = [7, 5, 7, 8, 5, 9, 7, 5], limit = 3
Output: 5
Explanation:
The statement text contains a correction note: the full array is NOT valid because topic 7 appears at indices 0, 2, 6,
and the consecutive gap 6 - 2 = 4 exceeds the limit 3. Therefore the longest valid windows have length 5,
such as [7, 5, 7, 8, 5] or [8, 5, 9, 7, 5].
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(n)

    Why O(n)?
    - We scan the array once from left to right with the right pointer.
    - The left boundary only ever moves forward, never backward.
    - Each index is processed a constant number of times.
    - Dictionary operations are average O(1).

    Core idea:
    A window [left..right] is valid if, for every topic, every pair of consecutive occurrences
    INSIDE the window has distance <= limit.

    A very important observation:
    - A window becomes invalid only when it contains some "bad gap":
      two consecutive occurrences of the same topic whose distance is > limit.
    - For a topic value x, if its consecutive occurrences in the full array are at positions ... prev, curr ...
      and curr - prev > limit, then any window that contains BOTH prev and curr is invalid.
    - Therefore, when we process curr, the left boundary must move to at least prev + 1,
      so that prev is excluded and that bad pair is no longer fully inside the window.

    This gives a clean sliding-window rule:
    - Track the most recent index where each topic appeared.
    - When we see the same topic again at index right:
        if right - lastIndex[topic] > limit,
        then the current window cannot include both positions,
        so set left = max(left, lastIndex[topic] + 1).
    - Update lastIndex[topic] = right.
    - The current window [left..right] is then valid.
    */
    public int LongestFeedWindow(int[] topics, int limit)
    {
        // This dictionary stores, for each topic value, the most recent index where we saw it.
        //
        // Why a dictionary?
        // - Topic values can be as large as 10^9.
        // - We cannot use an array indexed by topic value.
        // - A dictionary lets us store only the topics that actually appear.
        var lastIndex = new Dictionary<int, int>();

        // "left" is the start of our current sliding window.
        // We will expand the window by moving "right" from left to right.
        int left = 0;

        // This will store the best (maximum) valid window length found so far.
        int best = 0;

        // Move the right boundary one step at a time.
        for (int right = 0; right < topics.Length; right++)
        {
            // Current topic at the right boundary.
            int topic = topics[right];

            // Check whether this topic has appeared before.
            if (lastIndex.TryGetValue(topic, out int prev))
            {
                // We found the previous occurrence of the same topic.
                // Now we examine the distance between consecutive appearances:
                // prev and right are consecutive occurrences of this topic
                // with respect to everything processed so far, because "prev"
                // is the latest previous occurrence.
                int gap = right - prev;

                // If this gap is larger than the allowed limit, then any window
                // containing BOTH prev and right is invalid.
                //
                // Why?
                // Because inside that window, these two positions are consecutive
                // appearances of this topic (there is no same-topic occurrence between them),
                // and their distance exceeds the limit.
                //
                // Therefore, to restore validity, we must exclude prev from the window.
                // The smallest left boundary that excludes prev is prev + 1.
                if (gap > limit)
                {
                    // We take max because left is only allowed to move forward.
                    // There may have been earlier violations that already pushed left further.
                    left = Math.Max(left, prev + 1);
                }
            }

            // Update the most recent index for this topic to the current position.
            // This is necessary because future occurrences of the same topic must compare
            // against the latest previous occurrence, not an older one.
            lastIndex[topic] = right;

            // After possibly moving left, the window [left..right] is valid.
            // Compute its length and update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// ----------------------
// Demo / sample execution
// ----------------------

var solution = new Solution();

// Example 1
int[] topics1 = { 4, 1, 4, 2, 4, 3, 2 };
int limit1 = 2;
int result1 = solution.LongestFeedWindow(topics1, limit1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] topics2 = { 7, 5, 7, 8, 5, 9, 7, 5 };
int limit2 = 3;
int result2 = solution.LongestFeedWindow(topics2, limit2);
Console.WriteLine(result2); // Expected: 5

// Additional quick sanity checks

// Single element: always valid
int[] topics3 = { 42 };
int limit3 = 1;
Console.WriteLine(solution.LongestFeedWindow(topics3, limit3)); // Expected: 1

// All same topic, every consecutive gap is 1, so whole array is valid if limit >= 1
int[] topics4 = { 9, 9, 9, 9 };
int limit4 = 1;
Console.WriteLine(solution.LongestFeedWindow(topics4, limit4)); // Expected: 4

// A case where a large gap forces the left boundary to jump
int[] topics5 = { 1, 2, 3, 1 };
int limit5 = 2;
Console.WriteLine(solution.LongestFeedWindow(topics5, limit5)); // Expected: 3