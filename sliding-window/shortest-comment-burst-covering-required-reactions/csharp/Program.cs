/*
Title: Shortest Comment Burst Covering Required Reactions

Problem Description:
You are given a chronological stream of reactions on a live video. Each reaction is represented by a string in the array reactions, where reactions[i] is the reaction type posted at second i. You are also given a list required where each element is a pair [type, count], meaning the chosen contiguous time window must contain at least count occurrences of that reaction type. A reaction type may appear many times in the stream, and reaction types not mentioned in required may also appear inside the window.

Return the length of the shortest contiguous subarray of reactions that satisfies all required reaction counts. If no such window exists, return -1. If multiple valid windows have the same minimum length, any one of them would be acceptable conceptually, but your function should return only the minimum length.

This is a hard sliding window problem because the target is not just to include a set of labels, but to satisfy per-label multiplicity requirements while processing a long stream efficiently. A brute-force check over all subarrays will time out.

Constraints:
- 1 <= reactions.length <= 200000
- 1 <= required.length <= 100000
- 1 <= reactions[i].length <= 20
- 1 <= required[j][1] <= 10^9
- Sum of distinct reaction types in required is at most 100000
- Reaction strings consist of lowercase English letters, digits, and underscore
- If a reaction type appears multiple times in required, their counts should be added together before evaluating the answer
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + m), where:
      n = reactions.Length
      m = required.Length
    Explanation:
    - We first merge duplicate requirements in O(m).
    - Then we scan the reactions array with a sliding window.
    - Each reaction enters the window once and leaves the window once, so the two pointers
      together move at most 2 * n steps.
    - Dictionary operations are average O(1).

    Space Complexity:
    - O(k), where k is the number of distinct reaction types mentioned in required.
    Explanation:
    - We store required counts and current window counts only for relevant reaction types.
    */
    public int ShortestBurst(string[] reactions, string[][] required)
    {
        // Step 1:
        // Build a dictionary that stores the TOTAL required count for each reaction type.
        //
        // Why this is necessary:
        // The problem states that if a reaction type appears multiple times in "required",
        // their counts should be added together before evaluating the answer.
        //
        // Example:
        // required = [["like", 1], ["like", 2]]
        // Then the real requirement is:
        // like -> 3
        //
        // We use long for counts to be extra safe because required counts can be large.
        var need = new Dictionary<string, long>(StringComparer.Ordinal);

        foreach (var pair in required)
        {
            string type = pair[0];
            long count = long.Parse(pair[1]);

            if (need.ContainsKey(type))
            {
                need[type] += count;
            }
            else
            {
                need[type] = count;
            }
        }

        // If there are no requirements, the shortest valid window would conceptually be 0.
        // The given constraints imply required.length >= 1, but this guard makes the method robust.
        if (need.Count == 0)
        {
            return 0;
        }

        // Step 2:
        // Prepare another dictionary to track how many times each required reaction type
        // currently appears inside our sliding window.
        //
        // Important design choice:
        // We only care about reaction types that are in "need".
        // Reactions not mentioned in "need" do not affect validity, so we do not need to
        // store counts for them.
        var have = new Dictionary<string, long>(StringComparer.Ordinal);

        // Step 3:
        // "formed" counts how many distinct required reaction types are currently satisfied.
        //
        // A type is satisfied when:
        // have[type] >= need[type]
        //
        // "requiredTypes" is the total number of distinct reaction types that must be satisfied.
        int formed = 0;
        int requiredTypes = need.Count;

        // Step 4:
        // Standard sliding window pointers.
        // left = start of current window
        // right = end of current window
        int left = 0;

        // Step 5:
        // Track the best (smallest) window length found so far.
        // We initialize with int.MaxValue to mean "no valid window found yet".
        int bestLength = int.MaxValue;

        // Step 6:
        // Expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < reactions.Length; right++)
        {
            string currentReaction = reactions[right];

            // Step 6a:
            // If the current reaction is one of the required types,
            // update its count in the current window.
            //
            // Why this is necessary:
            // Only required types can help us satisfy the target conditions.
            if (need.ContainsKey(currentReaction))
            {
                if (!have.ContainsKey(currentReaction))
                {
                    have[currentReaction] = 0;
                }

                have[currentReaction]++;

                // Step 6b:
                // Check whether this increment caused the current reaction type
                // to become satisfied for the first time.
                //
                // This happens exactly when:
                // have[currentReaction] == need[currentReaction]
                //
                // Why equality and not >= ?
                // Because we only want to increase "formed" once per type,
                // at the moment it first reaches the required threshold.
                if (have[currentReaction] == need[currentReaction])
                {
                    formed++;
                }
            }

            // Step 7:
            // If all distinct required types are satisfied, then the current window is valid.
            //
            // Now we try to shrink it from the left as much as possible while keeping it valid.
            //
            // Why this is necessary:
            // The sliding window technique works by:
            // 1) expanding until valid
            // 2) shrinking to find the smallest valid window ending at "right"
            while (formed == requiredTypes)
            {
                // Step 7a:
                // Update the best answer using the current valid window [left..right].
                int currentLength = right - left + 1;
                if (currentLength < bestLength)
                {
                    bestLength = currentLength;
                }

                // Step 7b:
                // We are about to move "left" forward, so first inspect the reaction
                // that is leaving the window.
                string leftReaction = reactions[left];

                // Step 7c:
                // If the leaving reaction is a required type, decrement its count in "have".
                if (need.ContainsKey(leftReaction))
                {
                    have[leftReaction]--;

                    // Step 7d:
                    // If after decrementing, the count falls BELOW the required amount,
                    // then this type is no longer satisfied.
                    //
                    // That means the window becomes invalid after this removal,
                    // so we must decrease "formed".
                    if (have[leftReaction] < need[leftReaction])
                    {
                        formed--;
                    }
                }

                // Step 7e:
                // Finally move the left boundary forward to continue shrinking.
                left++;
            }
        }

        // Step 8:
        // If bestLength was never updated, then no valid window exists.
        // Return -1 in that case.
        return bestLength == int.MaxValue ? -1 : bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] reactions1 = { "like", "wow", "sad", "like", "heart", "wow", "like", "heart" };
string[][] required1 =
{
    new[] { "like", "2" },
    new[] { "heart", "1" },
    new[] { "wow", "1" }
};

int result1 = solution.ShortestBurst(reactions1, required1);
Console.WriteLine(result1); // Expected: 4

// Example 2
string[] reactions2 = { "fire", "clap", "fire", "laugh", "clap", "fire" };
string[][] required2 =
{
    new[] { "fire", "2" },
    new[] { "clap", "2" },
    new[] { "boo", "1" }
};

int result2 = solution.ShortestBurst(reactions2, required2);
Console.WriteLine(result2); // Expected: -1

// Additional demo: duplicate required entries should be merged.
// Need like total 3 and wow total 1.
// Shortest valid window in this stream is ["like","wow","like","like"] => length 4
string[] reactions3 = { "like", "wow", "like", "like", "heart" };
string[][] required3 =
{
    new[] { "like", "1" },
    new[] { "wow", "1" },
    new[] { "like", "2" }
};

int result3 = solution.ShortestBurst(reactions3, required3);
Console.WriteLine(result3); // Expected: 4