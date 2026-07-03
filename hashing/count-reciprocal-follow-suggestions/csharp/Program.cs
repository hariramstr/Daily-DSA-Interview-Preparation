/*
Title: Count Reciprocal Follow Suggestions

Problem Description:
You are given a list of directed follow relationships in a social platform. Each relationship
is represented as a pair [a, b], meaning user a follows user b.

A reciprocal follow suggestion exists for a pair of distinct users (u, v) if exactly one of
the two directed relationships exists:
- u follows v
or
- v follows u
but not both.

Important rules:
1. Each unordered pair of users should be counted at most once.
2. Duplicate directed relationships should be treated as a single relationship.
3. Self-follows [x, x] should be ignored completely.

Goal:
Return the total number of unordered user pairs that have exactly one directed follow
relationship between them after deduplication.

Key implementation idea:
- First deduplicate directed edges.
- Then normalize each pair into an unordered pair (min, max).
- Track whether we have seen one direction or both directions for that unordered pair.
- Count pairs whose final state is "exactly one direction exists".
*/

using System;
using System.Collections.Generic;

public class Solution
{
    // Time Complexity:
    // O(n), where n is the number of input relationships.
    // Explanation:
    // - We process each relationship once.
    // - HashSet and Dictionary operations are O(1) on average.
    //
    // Space Complexity:
    // O(n)
    // Explanation:
    // - In the worst case, all directed edges are unique, so the HashSet stores O(n) items.
    // - The Dictionary of unordered pairs also stores O(n) items in the worst case.
    public int CountReciprocalFollowSuggestions(int[][] relationships)
    {
        // This HashSet stores directed edges after deduplication.
        //
        // Why do we need this?
        // The input may contain duplicates such as [4,5] appearing many times.
        // The problem explicitly says duplicates should count as a single follow relationship.
        //
        // We encode each directed edge (a -> b) into a single long value so it can be stored
        // efficiently in a HashSet.
        var uniqueDirectedEdges = new HashSet<long>();

        // This Dictionary tracks the state of each unordered pair.
        //
        // Key:
        //   The unordered pair (minUser, maxUser), encoded into a long.
        //
        // Value:
        //   A small bitmask describing which direction(s) exist:
        //   - 1 means minUser -> maxUser exists
        //   - 2 means maxUser -> minUser exists
        //   - 3 means both directions exist
        //
        // Why is this useful?
        // For each unordered pair, we only care whether exactly one direction exists
        // or both directions exist.
        var pairState = new Dictionary<long, int>();

        // Step 1: Process every relationship from the input.
        foreach (var relation in relationships)
        {
            int a = relation[0];
            int b = relation[1];

            // Step 1a: Ignore self-follows.
            //
            // Why?
            // A user cannot be suggested to follow themselves back.
            // So [x, x] should not affect the answer in any way.
            if (a == b)
            {
                continue;
            }

            // Step 1b: Deduplicate the directed edge.
            //
            // We encode the ordered pair (a, b) exactly as-is because direction matters here.
            // For example:
            // - (1,2) is different from (2,1)
            long directedKey = EncodePair(a, b);

            // If this exact directed edge was already seen, skip it.
            //
            // Why?
            // Duplicate input relationships should be treated as one relationship only.
            if (!uniqueDirectedEdges.Add(directedKey))
            {
                continue;
            }

            // Step 1c: Normalize the pair into an unordered pair.
            //
            // For example:
            // - (1,3) and (3,1) should map to the same unordered pair key.
            int low = Math.Min(a, b);
            int high = Math.Max(a, b);
            long unorderedKey = EncodePair(low, high);

            // Step 1d: Determine which direction this edge represents relative to (low, high).
            //
            // If a == low and b == high, then the direction is low -> high, represented by bit 1.
            // Otherwise, the direction is high -> low, represented by bit 2.
            int directionBit = (a == low && b == high) ? 1 : 2;

            // Step 1e: Update the stored state for this unordered pair.
            //
            // If the pair has not been seen before, initialize it with the current direction.
            // If it has been seen, combine the old direction(s) with the new one using bitwise OR.
            //
            // Examples:
            // - First time seeing (1,3) as 1 -> 3: state becomes 1
            // - Later seeing 3 -> 1: state becomes 1 | 2 = 3
            if (pairState.TryGetValue(unorderedKey, out int existingState))
            {
                pairState[unorderedKey] = existingState | directionBit;
            }
            else
            {
                pairState[unorderedKey] = directionBit;
            }
        }

        // Step 2: Count how many unordered pairs have exactly one direction present.
        //
        // Valid states for "exactly one direction":
        // - 1 : only low -> high exists
        // - 2 : only high -> low exists
        //
        // Invalid state:
        // - 3 : both directions exist, so no suggestion is needed
        int count = 0;

        foreach (var entry in pairState)
        {
            int state = entry.Value;

            if (state == 1 || state == 2)
            {
                count++;
            }
        }

        return count;
    }

    // Helper method to encode two int values into one long key.
    //
    // Why do this?
    // Hash-based collections like HashSet and Dictionary work very well with a single key.
    // Since user IDs can be as large as 1e9, int is enough for each ID, and long is enough
    // to combine two ints safely.
    //
    // We cast the first value to long and shift it left by 32 bits, then place the second
    // value in the lower 32 bits.
    private long EncodePair(int first, int second)
    {
        return ((long)first << 32) | (uint)second;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[][] relationships1 =
{
    new[] { 1, 2 },
    new[] { 2, 1 },
    new[] { 1, 3 },
    new[] { 4, 5 },
    new[] { 4, 5 },
    new[] { 6, 7 }
};

int result1 = solution.CountReciprocalFollowSuggestions(relationships1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
//
// The problem statement contains an ambiguity in the written output,
// but then explicitly clarifies the intended interpretation:
// count every unordered pair independently after deduplication.
//
// Under that correct interpretation:
// - (10,20) => one direction
// - (20,30) => both directions
// - (40,40) => ignored
// - (50,60) => both directions
// - (70,80) => one direction
// - (80,90) => one direction
// Total = 3
int[][] relationships2 =
{
    new[] { 10, 20 },
    new[] { 20, 30 },
    new[] { 30, 20 },
    new[] { 40, 40 },
    new[] { 50, 60 },
    new[] { 60, 50 },
    new[] { 70, 80 },
    new[] { 80, 90 }
};

int result2 = solution.CountReciprocalFollowSuggestions(relationships2);
Console.WriteLine("Example 2 Result: " + result2); // Expected under clarified definition: 3

// Additional quick sanity check
int[][] relationships3 =
{
    new[] { 1, 1 }, // ignored
    new[] { 2, 3 },
    new[] { 2, 3 }, // duplicate
    new[] { 3, 2 }  // now both directions exist
};

int result3 = solution.CountReciprocalFollowSuggestions(relationships3);
Console.WriteLine("Sanity Check Result: " + result3); // Expected: 0