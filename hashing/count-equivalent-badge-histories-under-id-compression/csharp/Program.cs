/*
Title: Count Equivalent Badge Histories Under ID Compression
Difficulty: Hard
Topic: Hashing

Problem Description:
A company stores each employee's badge scan history as an integer array. Two histories are considered equivalent if they have the same repetition structure after compressing badge IDs by first appearance order. For example, the histories [42, 99, 42, 17] and [7, 3, 7, 8] are equivalent because both compress to [0, 1, 0, 2]. Likewise, [5, 5, 8] compresses to [0, 0, 1], which is different from [5, 8, 5] compressing to [0, 1, 0].

You are given n badge histories, where the i-th history is an array of integers and histories may have different lengths. Return the number of unordered pairs of histories that are equivalent under this compression rule.

Because badge IDs may be very large or negative, solutions that rely on value ranges are not acceptable. An efficient solution should build a canonical representation or hash for each history and count how many times the same pattern appears.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= total number of scanned IDs across all histories <= 2 * 10^5
- -10^9 <= badgeID <= 10^9
- 1 <= length of each history
- Return the answer as a 64-bit integer

Example 1:
Input: histories = [[42,99,42,17],[7,3,7,8],[5,5,8],[8,8,1],[10,11,10,12]]
Output: 4
Explanation: The first, second, and fifth histories all compress to [0,1,0,2], contributing 3 pairs. The third and fourth histories both compress to [0,0,1], contributing 1 pair. Total = 4.

Example 2:
Input: histories = [[1,2,1,2],[4,4,5,5],[9],[3,1,3],[8,6,8,7]]
The statement's explanation is inconsistent. For this exact input, the correct output is 0 because:
- [1,2,1,2] -> [0,1,0,1]
- [4,4,5,5] -> [0,0,1,1]
- [9] -> [0]
- [3,1,3] -> [0,1,0]
- [8,6,8,7] -> [0,1,0,2]
No two compressed patterns are equal.

Task:
Implement a function that computes the number of unordered equivalent pairs in near-linear time.
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    /*
    Time Complexity:
    Let T be the total number of integers across all histories.
    - We process each badge ID exactly once while building its compressed pattern.
    - We also build one canonical string per history whose total size is proportional to that history's length.
    Overall: O(T)

    Space Complexity:
    - A temporary dictionary is used for each history to map original badge IDs to compressed IDs.
    - A global dictionary counts how many times each canonical pattern appears.
    Overall: O(T) in the worst case across stored pattern keys.
    */
    public long CountEquivalentHistories(IList<IList<int>> histories)
    {
        // This dictionary stores:
        // key   = canonical representation of a compressed history pattern
        // value = how many previous histories had exactly this same pattern
        //
        // Why do we need this?
        // Because if the current history has a pattern we've seen k times before,
        // then it forms exactly k new unordered equivalent pairs with those histories.
        var patternCount = new Dictionary<string, long>();

        // This will store the final answer.
        // We use long because the number of pairs can be large:
        // up to n * (n - 1) / 2, which does not safely fit in int for large n.
        long answer = 0;

        // Process each history independently.
        foreach (var history in histories)
        {
            // For the current history, we need to compress badge IDs by order of first appearance.
            //
            // Example:
            // history = [42, 99, 42, 17]
            // first time 42 appears -> assign 0
            // first time 99 appears -> assign 1
            // 42 already seen      -> use 0
            // first time 17 appears -> assign 2
            // compressed pattern = [0, 1, 0, 2]
            //
            // This dictionary maps:
            // original badge ID -> compressed ID assigned when first seen in THIS history
            //
            // Important:
            // This dictionary must be recreated for each history,
            // because compression depends only on the order inside that one history.
            var firstSeenOrder = new Dictionary<int, int>();

            // nextCompressedId tells us what compressed number to assign
            // to the next new badge ID we encounter.
            int nextCompressedId = 0;

            // We need a canonical representation so that two equivalent histories
            // produce exactly the same key.
            //
            // A simple and safe approach is to build a comma-separated string
            // of the compressed IDs.
            //
            // Example:
            // [0,1,0,2] becomes "0,1,0,2"
            //
            // Why include separators?
            // Without separators, patterns like [0,11] and [0,1,1] could both look like "011".
            // Separators remove ambiguity.
            var parts = new string[history.Count];

            // Walk through the current history from left to right.
            for (int i = 0; i < history.Count; i++)
            {
                int badgeId = history[i];

                // Check whether this badge ID has already appeared earlier in this history.
                if (!firstSeenOrder.TryGetValue(badgeId, out int compressedId))
                {
                    // This is the first time we see this badge ID in the current history.
                    // Assign the next available compressed ID.
                    compressedId = nextCompressedId;

                    // Record the mapping so future occurrences reuse the same compressed ID.
                    firstSeenOrder[badgeId] = compressedId;

                    // Advance to the next unused compressed ID.
                    nextCompressedId++;
                }

                // Store the compressed ID as text for canonical key construction.
                parts[i] = compressedId.ToString();
            }

            // Join all compressed IDs into one canonical key.
            string key = string.Join(",", parts);

            // If this exact pattern has been seen before, then each previous occurrence
            // forms one new unordered pair with the current history.
            if (patternCount.TryGetValue(key, out long seen))
            {
                answer += seen;
                patternCount[key] = seen + 1;
            }
            else
            {
                // First time this pattern appears.
                patternCount[key] = 1;
            }
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement.
// Patterns:
// [42,99,42,17] -> [0,1,0,2]
// [7,3,7,8]     -> [0,1,0,2]
// [5,5,8]       -> [0,0,1]
// [8,8,1]       -> [0,0,1]
// [10,11,10,12] -> [0,1,0,2]
//
// Group sizes:
// [0,1,0,2] appears 3 times -> 3 * 2 / 2 = 3 pairs
// [0,0,1]   appears 2 times -> 2 * 1 / 2 = 1 pair
// Total = 4
var histories1 = new List<IList<int>>
{
    new List<int> { 42, 99, 42, 17 },
    new List<int> { 7, 3, 7, 8 },
    new List<int> { 5, 5, 8 },
    new List<int> { 8, 8, 1 },
    new List<int> { 10, 11, 10, 12 }
};

long result1 = solution.CountEquivalentHistories(histories1);
Console.WriteLine(result1); // Expected: 4

// Example 2 as written in the statement is inconsistent.
// For the exact list below, the correct answer is 0.
//
// Patterns:
// [1,2,1,2] -> [0,1,0,1]
// [4,4,5,5] -> [0,0,1,1]
// [9]       -> [0]
// [3,1,3]   -> [0,1,0]
// [8,6,8,7] -> [0,1,0,2]
//
// No duplicates, so answer = 0.
var histories2 = new List<IList<int>>
{
    new List<int> { 1, 2, 1, 2 },
    new List<int> { 4, 4, 5, 5 },
    new List<int> { 9 },
    new List<int> { 3, 1, 3 },
    new List<int> { 8, 6, 8, 7 }
};

long result2 = solution.CountEquivalentHistories(histories2);
Console.WriteLine(result2); // Correct for this exact input: 0

// Optional corrected variant of Example 2 to produce 1 pair.
// Replace [4,4,5,5] with [6,7,6,7] so both it and [1,2,1,2] compress to [0,1,0,1].
var histories2Corrected = new List<IList<int>>
{
    new List<int> { 1, 2, 1, 2 },
    new List<int> { 6, 7, 6, 7 },
    new List<int> { 9 },
    new List<int> { 3, 1, 3 },
    new List<int> { 8, 6, 8, 7 }
};

long result2Corrected = solution.CountEquivalentHistories(histories2Corrected);
Console.WriteLine(result2Corrected); // Expected: 1