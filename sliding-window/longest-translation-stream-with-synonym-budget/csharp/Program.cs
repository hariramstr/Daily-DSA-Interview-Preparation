/*
Title: Longest Translation Stream With Synonym Budget
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are building a real-time translation quality monitor. A transcript has already been tokenized into an array `words`, where each `words[i]` is the normalized meaning ID of the spoken word at position `i`. Because multiple surface words may map to the same meaning, repeated IDs are allowed and expected. A translation team considers a contiguous segment valid if no meaning ID appears more than `limit[id]` times inside that segment. However, the monitor is also allowed to spend from a global synonym budget `B`: for each extra occurrence of a meaning ID beyond its allowed limit inside the current segment, the segment consumes 1 unit of budget. For example, if meaning 7 appears 5 times and `limit[7] = 3`, then meaning 7 contributes 2 to the budget usage of that segment.

Your task is to return the length of the longest contiguous segment whose total budget usage is at most `B`.

Formally, for a window `[l, r]`, let `freq[x]` be the number of times meaning ID `x` appears in that window. Its violation cost is `sum(max(0, freq[x] - limit[x]))` over all meaning IDs that appear in the window. Find the maximum window length with violation cost `<= B`.

If a meaning ID appears in `words` but is not present in `limit`, its allowed limit is 0.

Constraints:
- `1 <= words.length <= 2 * 10^5`
- `0 <= words[i] <= 10^9`
- `0 <= B <= 2 * 10^5`
- `0 <= limit.size() <= 2 * 10^5`
- `0 <= limit[id] <= words.length`
- The answer must be computed in better than `O(n^2)` time.

Example 1:
Input:
words = [4, 1, 4, 2, 4, 1, 3]
limit = {4: 2, 1: 1, 2: 1, 3: 1}
B = 1
Output:
5

Example 2:
Input:
words = [8, 8, 5, 8, 5, 6, 5, 5]
limit = {8: 1, 5: 2, 6: 1}
B = 2
Output:
6
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the number of words.
    Reason:
    - The right pointer moves from left to right exactly once.
    - The left pointer also moves from left to right at most once per element.
    - Every frequency update is O(1) average time using dictionaries.

    Space Complexity:
    O(k), where k is the number of distinct meaning IDs currently tracked.
    In the worst case, this can be O(n) if all words are different.
    */
    public int LongestTranslationStreamWithSynonymBudget(int[] words, Dictionary<int, int> limit, int B)
    {
        // This dictionary stores the current frequency of each meaning ID
        // inside the active sliding window [left, right].
        //
        // Why do we need it?
        // Because the budget usage depends on how many times each ID appears
        // in the current window, so we must know the current count quickly.
        var freq = new Dictionary<int, int>();

        // 'left' is the left boundary of our sliding window.
        int left = 0;

        // 'best' stores the maximum valid window length found so far.
        int best = 0;

        // 'currentCost' stores the total violation cost of the current window.
        //
        // Definition reminder:
        // For each ID x, contribution is max(0, freq[x] - limit[x]).
        // currentCost is the sum of those contributions over all IDs.
        //
        // The key idea of this solution is:
        // Instead of recomputing the full sum every time the window changes,
        // we update the cost incrementally when one word enters or leaves.
        int currentCost = 0;

        // Expand the window by moving 'right' one step at a time.
        for (int right = 0; right < words.Length; right++)
        {
            int id = words[right];

            // Look up the allowed limit for this meaning ID.
            // If the ID does not exist in the limit dictionary,
            // the problem says its allowed limit is 0.
            int allowed = limit.TryGetValue(id, out int foundLimit) ? foundLimit : 0;

            // Get the old frequency before adding this new occurrence.
            int oldFreq = freq.TryGetValue(id, out int existingFreq) ? existingFreq : 0;

            // Add the new occurrence to the frequency map.
            int newFreq = oldFreq + 1;
            freq[id] = newFreq;

            // Now we must update the total violation cost.
            //
            // Before adding this word, this ID contributed:
            // max(0, oldFreq - allowed)
            //
            // After adding this word, this ID contributes:
            // max(0, newFreq - allowed)
            //
            // Since only one ID changed, the total cost changes only by the
            // difference between these two values.
            //
            // There is an even simpler observation:
            // Adding one occurrence increases the cost by 1 exactly when
            // the new frequency becomes greater than the allowed limit.
            //
            // Example:
            // allowed = 2
            // oldFreq = 2 -> newFreq = 3, cost increases by 1
            // oldFreq = 1 -> newFreq = 2, still within limit, no increase
            if (newFreq > allowed)
            {
                currentCost++;
            }

            // If the window is now too expensive, we must shrink it from the left
            // until the total cost becomes <= B again.
            //
            // This is the heart of the sliding window technique:
            // - right only moves forward
            // - left only moves forward
            // Therefore the total work is linear.
            while (currentCost > B)
            {
                int leftId = words[left];

                // Get the allowed limit for the ID that is leaving the window.
                int leftAllowed = limit.TryGetValue(leftId, out int leftFoundLimit) ? leftFoundLimit : 0;

                // Current frequency before removal.
                int leftOldFreq = freq[leftId];

                // Remove one occurrence from the window.
                int leftNewFreq = leftOldFreq - 1;

                // Update the dictionary.
                if (leftNewFreq == 0)
                {
                    // Optional cleanup:
                    // removing zero-count keys keeps the dictionary smaller and cleaner.
                    freq.Remove(leftId);
                }
                else
                {
                    freq[leftId] = leftNewFreq;
                }

                // Update the violation cost after removing one occurrence.
                //
                // Removing one occurrence decreases the cost by 1 exactly when
                // the old frequency was already above the allowed limit.
                //
                // Example:
                // allowed = 2
                // oldFreq = 4 -> newFreq = 3, still above limit, cost decreases by 1
                // oldFreq = 3 -> newFreq = 2, crosses back to valid, cost decreases by 1
                // oldFreq = 2 -> newFreq = 1, was not violating before, no cost change
                if (leftOldFreq > leftAllowed)
                {
                    currentCost--;
                }

                // Move the left boundary rightward because that element is no longer in the window.
                left++;
            }

            // At this point, the current window [left, right] is valid:
            // currentCost <= B
            //
            // So we can safely consider its length as a candidate answer.
            int windowLength = right - left + 1;
            if (windowLength > best)
            {
                best = windowLength;
            }
        }

        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] words1 = { 4, 1, 4, 2, 4, 1, 3 };
var limit1 = new Dictionary<int, int>
{
    [4] = 2,
    [1] = 1,
    [2] = 1,
    [3] = 1
};
int B1 = 1;
int result1 = solution.LongestTranslationStreamWithSynonymBudget(words1, limit1, B1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] words2 = { 8, 8, 5, 8, 5, 6, 5, 5 };
var limit2 = new Dictionary<int, int>
{
    [8] = 1,
    [5] = 2,
    [6] = 1
};
int B2 = 2;
int result2 = solution.LongestTranslationStreamWithSynonymBudget(words2, limit2, B2);
Console.WriteLine(result2); // Expected: 6