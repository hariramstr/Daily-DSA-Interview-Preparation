/*
Title: Count Pairs of Sessions With the Same Unique Error Codes
Difficulty: Medium
Topic: Hashing

Problem Description:
A monitoring system records application sessions, where each session contains a list of error codes that occurred during that session. The same error code may appear multiple times inside one session if the issue was triggered repeatedly. Two sessions are considered equivalent if the set of distinct error codes seen in the two sessions is exactly the same, regardless of the order of codes and regardless of how many times each code repeats.

Given an array sessions, where sessions[i] is a non-empty array of integers representing the error codes seen in the i-th session, return the number of pairs of indices (i, j) such that i < j and sessions[i] and sessions[j] are equivalent.

For example, the sessions [4, 7, 4, 9] and [9, 7, 4] are equivalent because both contain the unique code set {4, 7, 9}. However, [4, 7] and [4, 7, 8] are not equivalent.

Constraints:
- 1 <= sessions.length <= 100000
- 1 <= sessions[i].length <= 100
- 0 <= sessions[i][j] <= 1000000000
- The total number of error codes across all sessions does not exceed 300000

Example 1:
Input: sessions = [[4,7,4,9],[9,4,7],[1,2,2],[2,1],[5]]
Output: 2
Explanation: The first two sessions share the unique set {4,7,9}. The third and fourth sessions share the unique set {1,2}. So there are 2 equivalent pairs.

Example 2:
Input: sessions = [[8,8,8],[8],[1,3,1,3],[3,1],[2,2,4],[4,2,4],[2,4,5]]
Output: 4
Explanation: Sessions 0 and 1 are equivalent with unique set {8}. Sessions 2 and 3 are equivalent with unique set {1,3}. Sessions 4 and 5 are equivalent with unique set {2,4}. No other pair has the same distinct-code set.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    Let T be the total number of error codes across all sessions.
    For each session, we:
    1. Remove duplicates using a HashSet
    2. Sort the distinct values to create a canonical representation

    If a session has k values and d distinct values, the work is:
    - O(k) to build the HashSet
    - O(d log d) to sort the distinct values

    Across all sessions, this is efficient because:
    - Total input size is at most 300000
    - Each individual session length is at most 100

    So the practical total complexity is:
    O(sum over all sessions of (k + d log d))

    Space Complexity:
    O(U)
    where U is the total number of distinct canonical keys stored in the dictionary,
    plus temporary space for each session's HashSet and sorted distinct list.
    */
    public long CountEquivalentSessionPairs(int[][] sessions)
    {
        // This dictionary maps:
        //   canonical representation of a session's distinct error-code set
        // to:
        //   how many previous sessions have already produced exactly that same representation.
        //
        // Why do we need this?
        // Because when we process the current session, every previous session with the same
        // canonical form creates one valid pair with the current session.
        //
        // Example:
        // If we have already seen the key "1,2" two times, and the current session also
        // becomes "1,2", then it forms 2 new pairs with those earlier sessions.
        var frequencyByCanonicalSet = new Dictionary<string, long>();

        // We store the answer in a long because the number of pairs can be large.
        //
        // In the worst case, if all sessions are equivalent and there are 100000 sessions,
        // the number of pairs is:
        // 100000 * 99999 / 2 = 4,999,950,000
        //
        // That does not fit in a 32-bit int, so long is required.
        long pairCount = 0;

        // Process each session one by one.
        foreach (var session in sessions)
        {
            // STEP 1: Remove duplicates inside the current session.
            //
            // Why is this necessary?
            // The problem says two sessions are equivalent if their SET of distinct error codes
            // is the same. Repeated occurrences do not matter.
            //
            // Example:
            // [4,7,4,9] should be treated the same as [9,7,4]
            // because both reduce to the distinct set {4,7,9}.
            //
            // A HashSet is the natural data structure for this because:
            // - it automatically removes duplicates
            // - insertion is efficient on average
            var distinctCodes = new HashSet<int>();

            foreach (var code in session)
            {
                distinctCodes.Add(code);
            }

            // STEP 2: Convert the set into a sorted list.
            //
            // Why sort?
            // A set has no guaranteed order, but we need a canonical representation:
            // the same distinct set must always produce the exact same key string.
            //
            // Without sorting:
            // {4,7,9} might appear as "4,7,9" in one session and "9,4,7" in another,
            // which would incorrectly look different.
            //
            // With sorting:
            // both become [4,7,9], so they produce the same key.
            var sortedDistinctCodes = distinctCodes.ToList();
            sortedDistinctCodes.Sort();

            // STEP 3: Build a canonical string key from the sorted distinct values.
            //
            // Why use a string key?
            // Because Dictionary needs a stable comparable key.
            // A comma-joined sorted list is simple, readable, and correct.
            //
            // Example:
            // [1,2]   -> "1,2"
            // [4,7,9] -> "4,7,9"
            //
            // Since the values are sorted and duplicates were removed,
            // two equivalent sessions will always produce the same key.
            string canonicalKey = string.Join(",", sortedDistinctCodes);

            // STEP 4: Count how many previous sessions had the same canonical key.
            //
            // If this key has already been seen X times, then the current session forms
            // exactly X new valid pairs:
            // - one pair with each previous matching session
            //
            // This works because we process sessions from left to right,
            // so all counted matches automatically satisfy i < j.
            if (frequencyByCanonicalSet.TryGetValue(canonicalKey, out long previousCount))
            {
                pairCount += previousCount;
                frequencyByCanonicalSet[canonicalKey] = previousCount + 1;
            }
            else
            {
                // If this is the first time we see this canonical set,
                // it does not form any pair yet, but we record it for future sessions.
                frequencyByCanonicalSet[canonicalKey] = 1;
            }
        }

        // After processing all sessions, pairCount contains the total number of equivalent pairs.
        return pairCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// sessions = [[4,7,4,9],[9,4,7],[1,2,2],[2,1],[5]]
// Distinct canonical forms:
// [4,7,4,9] -> "4,7,9"
// [9,4,7]   -> "4,7,9"   => 1 pair
// [1,2,2]   -> "1,2"
// [2,1]     -> "1,2"     => 1 pair
// [5]       -> "5"
// Total = 2
int[][] sessions1 =
[
    [4, 7, 4, 9],
    [9, 4, 7],
    [1, 2, 2],
    [2, 1],
    [5]
];

long result1 = solution.CountEquivalentSessionPairs(sessions1);
Console.WriteLine(result1); // Expected: 2

// Example 2:
// sessions = [[8,8,8],[8],[1,3,1,3],[3,1],[2,2,4],[4,2,4],[2,4,5]]
// Distinct canonical forms:
// [8,8,8]   -> "8"
// [8]       -> "8"       => 1 pair
// [1,3,1,3] -> "1,3"
// [3,1]     -> "1,3"     => 1 pair
// [2,2,4]   -> "2,4"
// [4,2,4]   -> "2,4"     => 1 pair
// [2,4,5]   -> "2,4,5"
// Total = 3
//
// Note:
// The problem statement says Output: 4, but tracing the sessions carefully shows only 3 pairs.
// The algorithm correctly returns 3 for this input.
int[][] sessions2 =
[
    [8, 8, 8],
    [8],
    [1, 3, 1, 3],
    [3, 1],
    [2, 2, 4],
    [4, 2, 4],
    [2, 4, 5]
];

long result2 = solution.CountEquivalentSessionPairs(sessions2);
Console.WriteLine(result2); // Correct traced result: 3

// Additional quick sanity check:
// All three sessions reduce to the same distinct set {2,4}
// Number of pairs should be 3 choose 2 = 3
int[][] sessions3 =
[
    [2, 2, 4],
    [4, 2],
    [2, 4, 4, 2]
];

long result3 = solution.CountEquivalentSessionPairs(sessions3);
Console.WriteLine(result3); // Expected: 3