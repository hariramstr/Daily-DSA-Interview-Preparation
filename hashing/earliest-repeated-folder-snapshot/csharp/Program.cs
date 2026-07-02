/*
Title: Earliest Repeated Folder Snapshot
Difficulty: Medium
Topic: Hashing

Problem Description:
A cloud storage system records the state of a folder once per minute. Each snapshot is represented by an array of file IDs currently present in the folder. The order of file IDs inside a snapshot is not meaningful, but duplicate IDs will never appear within the same snapshot.

Two snapshots are considered identical if they contain exactly the same set of file IDs, regardless of order. Given a list of snapshots in chronological order, return the earliest pair of indices [i, j] such that i < j and snapshots[i] and snapshots[j] are identical. If multiple repeated states exist, choose the pair with the smallest j. If there is still a tie, choose the one with the smallest i. If no folder state repeats, return [-1, -1].

Your task is to design an efficient solution using hashing so that each snapshot can be normalized and compared quickly.

Constraints:
- 1 <= snapshots.length <= 100000
- 0 <= snapshots[i].length <= 1000
- 0 <= fileID <= 1000000000
- The sum of all snapshots[i].length over the entire input does not exceed 200000
- Each individual snapshot contains distinct file IDs

Example 1:
Input: snapshots = [[5,1,9],[3,4],[9,5,1],[4,3],[7]]
Output: [0,2]
Explanation: Snapshot 0 and snapshot 2 contain the same file IDs {1,5,9}. Although snapshot 1 and snapshot 3 also match, index 2 is the earliest repeated occurrence.

Example 2:
Input: snapshots = [[],[8],[2,6],[6,2],[]]
Output: [2,3]
Explanation:
- Snapshot 2 and snapshot 3 both represent the set {2,6}, so they repeat at j = 3.
- Snapshot 0 and snapshot 4 are also identical, but j = 4 is later than j = 3.
Therefore the earliest repeated occurrence is [2,3].

Return the answer as a length-2 array. A typical approach is to convert each snapshot into a canonical representation, such as a sorted tuple or delimiter-safe string, then use a hash map to store the first index where each normalized state appeared.
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Solution
{
    /*
        Time Complexity:
        Let n be the number of snapshots, and let K be the total number of file IDs across all snapshots.
        For each snapshot, we sort its contents to create a canonical representation.
        If a snapshot has length m, sorting costs O(m log m).
        Therefore, the total time is:
            O(sum over all snapshots of (m log m))
        With the given constraints, this is efficient enough.

        Space Complexity:
        We store:
        1. A dictionary from normalized snapshot representation to its first index
        2. Temporary arrays / strings used during normalization
        Overall extra space is:
            O(K)
        in the worst case, across stored normalized keys.
    */
    public int[] EarliestRepeatedFolderSnapshot(int[][] snapshots)
    {
        // This dictionary is the core hashing data structure for the solution.
        //
        // Key:
        //   A canonical string representation of a snapshot.
        //   Example:
        //     [5,1,9] -> after sorting -> [1,5,9] -> "1,5,9"
        //
        // Value:
        //   The earliest index where this exact normalized snapshot first appeared.
        //
        // Why a dictionary?
        //   We need very fast lookup to answer:
        //   "Have we seen this folder state before?"
        //   A hash map / dictionary gives average O(1) lookup and insertion.
        var firstSeenIndex = new Dictionary<string, int>();

        // We scan snapshots from left to right in chronological order.
        //
        // This order is extremely important because the problem asks for:
        //   1. The smallest possible j
        //   2. If tied, the smallest possible i
        //
        // By scanning j from 0 upward:
        //   - The first repeated snapshot we encounter automatically has the smallest j.
        //   - Because we store only the first occurrence of each normalized state,
        //     the matching i is automatically the smallest possible i for that j.
        for (int j = 0; j < snapshots.Length; j++)
        {
            // Step 1: Normalize the current snapshot.
            //
            // Why do we need normalization?
            //   The order inside a snapshot does not matter.
            //   So [5,1,9] and [9,5,1] should be treated as identical.
            //
            // The easiest beginner-friendly way:
            //   - Copy the array
            //   - Sort the copy
            //   - Convert it into a delimiter-safe string key
            //
            // We copy first so we do NOT modify the caller's input data.
            string key = NormalizeSnapshot(snapshots[j]);

            // Step 2: Check whether this normalized folder state has appeared before.
            //
            // If yes:
            //   We have found a repeated snapshot pair [i, j].
            //   Because we are scanning j from left to right, this is the earliest possible j.
            //   Because the dictionary stores the first index only, i is the smallest possible i.
            if (firstSeenIndex.TryGetValue(key, out int i))
            {
                return new[] { i, j };
            }

            // Step 3: If this state has not been seen before, record its first occurrence.
            //
            // Why only the first occurrence?
            //   Suppose the same state appears at indices 2, 5, 8.
            //   For any future repeat, the smallest valid i is 2, not 5 or 8.
            //   So we should preserve the earliest index and never overwrite it.
            firstSeenIndex[key] = j;
        }

        // If we finish scanning all snapshots and never find a repeat,
        // the problem asks us to return [-1, -1].
        return new[] { -1, -1 };
    }

    private string NormalizeSnapshot(int[] snapshot)
    {
        // We create a copy because sorting should not mutate the original input.
        //
        // This is a good habit in interview-style problems unless mutation is explicitly allowed.
        int[] copy = new int[snapshot.Length];
        Array.Copy(snapshot, copy, snapshot.Length);

        // Sorting transforms all equivalent sets into the same order.
        //
        // Examples:
        //   [5,1,9] -> [1,5,9]
        //   [9,5,1] -> [1,5,9]
        //
        // After sorting, equal sets become identical arrays.
        Array.Sort(copy);

        // Now we convert the sorted numbers into a string key.
        //
        // We use a delimiter (comma) so values do not run together ambiguously.
        // For example:
        //   [1,23]  -> "1,23"
        //   [12,3]  -> "12,3"
        // These are clearly different.
        //
        // Empty snapshot:
        //   [] -> ""
        //
        // That is perfectly fine as a dictionary key.
        return string.Join(",", copy);
    }
}

// -------------------------
// Demo code
// -------------------------

var solution = new Solution();

// Example 1
int[][] snapshots1 =
{
    new[] { 5, 1, 9 },
    new[] { 3, 4 },
    new[] { 9, 5, 1 },
    new[] { 4, 3 },
    new[] { 7 }
};

int[] result1 = solution.EarliestRepeatedFolderSnapshot(snapshots1);
Console.WriteLine($"Example 1 Result: [{result1[0]}, {result1[1]}]");

// Example 2
int[][] snapshots2 =
{
    Array.Empty<int>(),
    new[] { 8 },
    new[] { 2, 6 },
    new[] { 6, 2 },
    Array.Empty<int>()
};

int[] result2 = solution.EarliestRepeatedFolderSnapshot(snapshots2);
Console.WriteLine($"Example 2 Result: [{result2[0]}, {result2[1]}]");

// Additional demo: no repeated snapshot
int[][] snapshots3 =
{
    new[] { 1, 2 },
    new[] { 3 },
    Array.Empty<int>(),
    new[] { 4, 5, 6 }
};

int[] result3 = solution.EarliestRepeatedFolderSnapshot(snapshots3);
Console.WriteLine($"Example 3 Result: [{result3[0]}, {result3[1]}]");