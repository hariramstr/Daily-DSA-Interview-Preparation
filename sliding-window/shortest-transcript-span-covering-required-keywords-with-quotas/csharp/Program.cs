/*
Title: Shortest Transcript Span Covering Required Keywords with Quotas
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a transcript represented by an array of lowercase words `words`, where `words[i]` is the `i`-th token spoken in order. You are also given a list of required keywords with minimum occurrence quotas, represented by two arrays: `required` and `need`, where `required[j]` must appear at least `need[j]` times inside the chosen contiguous span.

Your task is to return the length of the shortest contiguous subarray of `words` that satisfies all keyword quotas. If no such span exists, return `-1`.

Unlike the classic minimum-cover problems where each target appears once, the same keyword may need to appear multiple times, and the transcript can be very large. An efficient sliding window solution is expected. The challenge is to maintain counts correctly while shrinking the window as aggressively as possible without violating any quota.

Formally, find the minimum value of `r - l + 1` such that for every index `j`, the subarray `words[l...r]` contains at least `need[j]` occurrences of `required[j]`.

Constraints:
- `1 <= words.length <= 200000`
- `1 <= required.length == need.length <= 100000`
- `1 <= sum(need) <= 200000`
- `1 <= words[i].length, required[j].length <= 20`
- All strings contain only lowercase English letters.
- All values in `required` are distinct.

Example 1:
Input: words = ["api","error","db","api","timeout","error","api"], required = ["api","error"], need = [2,1]
Output: 4
Explanation: The shortest valid span is ["api","error","db","api"] with length 4. It contains "api" twice and "error" once.

Example 2:
Input: words = ["login","cache","login","queue","cache","queue"], required = ["login","queue","cache"], need = [2,1,2]
Output: 5
Explanation: The span ["login","cache","login","queue","cache"] has two "login", one "queue", and two "cache". No shorter valid span exists.

Return only the minimum length, not the span itself.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + m), where:
      n = words.Length
      m = required.Length
    Explanation:
    - We build the requirement dictionary in O(m).
    - We move the right pointer across the words array once.
    - We move the left pointer across the words array at most once.
    - Therefore, the sliding window work is linear.

    Space Complexity:
    - O(m)
    Explanation:
    - We store required counts and current window counts only for required keywords.
    */
    public int MinSpanLength(string[] words, string[] required, int[] need)
    {
        // This dictionary stores the minimum quota for each required keyword.
        // Example:
        // required = ["api", "error"], need = [2, 1]
        // then target["api"] = 2, target["error"] = 1
        //
        // Why a dictionary?
        // Because we need very fast lookup:
        // - Is this word one of the required keywords?
        // - If yes, how many times do we need it?
        //
        // Dictionary lookup is O(1) average time, which is ideal for large input sizes.
        var target = new Dictionary<string, int>(required.Length);
        for (int i = 0; i < required.Length; i++)
        {
            target[required[i]] = need[i];
        }

        // This dictionary stores how many times each required keyword currently appears
        // inside the current sliding window [left..right].
        //
        // We only track required words, because non-required words do not affect validity.
        var windowCount = new Dictionary<string, int>(required.Length);

        // This value tells us how many distinct required keywords currently satisfy their quota.
        //
        // Example:
        // If required = ["api", "error"] with need = [2, 1]
        // and current window has:
        //   api = 2  -> satisfied
        //   error = 0 -> not satisfied
        // then satisfiedKinds = 1
        //
        // Once satisfiedKinds == required.Length, the current window is valid.
        int satisfiedKinds = 0;

        // Total number of distinct required keywords that must be satisfied.
        int totalKinds = required.Length;

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        // We start with int.MaxValue to mean "no valid window found yet".
        int best = int.MaxValue;

        // Expand the window by moving the right pointer from left to right.
        for (int right = 0; right < words.Length; right++)
        {
            string currentWord = words[right];

            // Step 1: Include words[right] into the current window.
            //
            // We only care if this word is one of the required keywords.
            // If it is not required, it does not help satisfy quotas,
            // but it can still remain inside the window.
            if (target.TryGetValue(currentWord, out int requiredCount))
            {
                // Increase the count of this required word in the current window.
                int newCount = windowCount.TryGetValue(currentWord, out int existingCount)
                    ? existingCount + 1
                    : 1;

                windowCount[currentWord] = newCount;

                // Important:
                // We only increase satisfiedKinds at the exact moment this word's count
                // reaches its required quota.
                //
                // Example:
                // need["api"] = 2
                // window count goes:
                //   0 -> 1 : still not satisfied
                //   1 -> 2 : now satisfied, so satisfiedKinds++
                //   2 -> 3 : still satisfied, but do NOT increment again
                if (newCount == requiredCount)
                {
                    satisfiedKinds++;
                }
            }

            // Step 2: If the current window satisfies all quotas,
            // try to shrink it from the left as much as possible.
            //
            // Why do we shrink?
            // Because once a window is valid, any larger version of it is not better
            // than a smaller valid version. So we aggressively remove unnecessary words
            // from the left to find the shortest valid window ending at 'right'.
            while (satisfiedKinds == totalKinds)
            {
                // The current window [left..right] is valid.
                // Record its length as a candidate answer.
                int currentLength = right - left + 1;
                if (currentLength < best)
                {
                    best = currentLength;
                }

                // Now try removing words[left] from the window.
                string leftWord = words[left];

                // If leftWord is required, removing it may break validity.
                if (target.TryGetValue(leftWord, out int leftRequiredCount))
                {
                    int updatedCount = windowCount[leftWord] - 1;
                    windowCount[leftWord] = updatedCount;

                    // If after removing this word, its count drops BELOW the required quota,
                    // then the window is no longer valid.
                    //
                    // Example:
                    // need["error"] = 1
                    // count goes from 1 -> 0
                    // This means we just lost satisfaction for "error".
                    if (updatedCount < leftRequiredCount)
                    {
                        satisfiedKinds--;
                    }
                }

                // Move left boundary forward to continue shrinking.
                left++;
            }
        }

        // If best was never updated, no valid span exists.
        return best == int.MaxValue ? -1 : best;
    }
}

// Demo code for Example 1
var solution = new Solution();

string[] words1 = { "api", "error", "db", "api", "timeout", "error", "api" };
string[] required1 = { "api", "error" };
int[] need1 = { 2, 1 };
int result1 = solution.MinSpanLength(words1, required1, need1);
Console.WriteLine(result1); // Expected: 4

// Demo code for Example 2
string[] words2 = { "login", "cache", "login", "queue", "cache", "queue" };
string[] required2 = { "login", "queue", "cache" };
int[] need2 = { 2, 1, 2 };
int result2 = solution.MinSpanLength(words2, required2, need2);
Console.WriteLine(result2); // Expected: 5