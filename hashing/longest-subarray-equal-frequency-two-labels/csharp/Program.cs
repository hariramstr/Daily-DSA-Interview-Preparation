/*
 * ============================================================
 * Title: Find Longest Subarray with Equal Frequency of Two Labels
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given an array of strings `labels` where each element is either 'A' or 'B'.
 * Your task is to find the length of the longest contiguous subarray in which the
 * number of 'A's equals the number of 'B's.
 *
 * Approach:
 * This is a classic balance-tracking problem. We assign:
 *   +1 for each 'A'
 *   -1 for each 'B'
 *
 * Then we compute a running prefix sum as we scan the array.
 * If the prefix sum at index i equals the prefix sum at index j (where i < j),
 * it means the subarray from index (i+1) to j has equal numbers of 'A's and 'B's.
 *
 * We use a hash map to record the FIRST time we see each prefix sum value.
 * Whenever we see the same prefix sum again, we compute the distance and track the max.
 *
 * Key Insight:
 *   - Initialize the map with {0: -1} to handle the case where a valid subarray
 *     starts from index 0 (prefix sum returns to 0 from the start).
 *
 * Constraints:
 *   - 1 <= labels.length <= 10^5
 *   - labels[i] is either 'A' or 'B'
 * ============================================================
 */

using System;
using System.Collections.Generic;

// ============================================================
// Solution Class
// ============================================================
public class Solution
{
    /// <summary>
    /// Finds the length of the longest contiguous subarray with equal 'A's and 'B's.
    ///
    /// Time Complexity:  O(n) — We scan the array exactly once; hash map operations are O(1) average.
    /// Space Complexity: O(n) — In the worst case, the hash map stores one entry per unique prefix sum,
    ///                          which can be at most n+1 distinct values.
    /// </summary>
    /// <param name="labels">Array of strings, each either "A" or "B"</param>
    /// <returns>Length of the longest balanced subarray</returns>
    public int FindLongestBalancedSubarray(string[] labels)
    {
        // -------------------------------------------------------
        // Step 1: Initialize the prefix-sum hash map.
        //
        // The dictionary maps: prefixSum -> firstIndexWhereThisSumOccurred
        //
        // WHY start with {0: -1}?
        // Before we process any element, the running sum is 0 at a "virtual" index -1.
        // This handles the edge case where the entire prefix from index 0 to some index j
        // is balanced (sum returns to 0). Without this sentinel, we'd miss those cases.
        // -------------------------------------------------------
        var firstOccurrence = new Dictionary<int, int>();
        firstOccurrence[0] = -1; // Sentinel: prefix sum of 0 exists before the array starts

        // -------------------------------------------------------
        // Step 2: Initialize tracking variables.
        //
        // `balance` is our running prefix sum:
        //   +1 for 'A', -1 for 'B'
        //
        // `maxLength` stores the best (longest) balanced subarray length found so far.
        // -------------------------------------------------------
        int balance = 0;
        int maxLength = 0;

        // -------------------------------------------------------
        // Step 3: Iterate through each label in the array.
        //
        // For each element, we:
        //   a) Update the running balance
        //   b) Check if this balance value was seen before
        //   c) If yes, compute the subarray length and update maxLength
        //   d) If no, record this balance value with the current index
        // -------------------------------------------------------
        for (int i = 0; i < labels.Length; i++)
        {
            // -------------------------------------------------------
            // Step 3a: Update the running balance.
            //
            // We treat 'A' as +1 and 'B' as -1.
            // This transforms the problem: finding equal counts of A and B
            // becomes finding a subarray with sum = 0.
            // -------------------------------------------------------
            if (labels[i] == "A")
            {
                balance += 1;  // 'A' contributes +1 to the balance
            }
            else
            {
                balance -= 1;  // 'B' contributes -1 to the balance
            }

            // -------------------------------------------------------
            // Step 3b: Check if this balance value has been seen before.
            //
            // If `balance` was previously seen at index `prevIndex`, then the
            // subarray from (prevIndex + 1) to i has a net balance of 0,
            // meaning equal numbers of 'A's and 'B's.
            //
            // Example:
            //   Index:   -1  0   1   2   3   4   5   6
            //   Label:       A   B   B   A   A   B   A
            //   Balance:  0  1   0   -1  0   1   0   1
            //
            //   At i=1, balance=0, previously seen at index -1 → length = 1-(-1) = 2
            //   At i=3, balance=0, previously seen at index -1 → length = 3-(-1) = 4
            //   At i=5, balance=0, previously seen at index -1 → length = 5-(-1) = 6  ← max!
            // -------------------------------------------------------
            if (firstOccurrence.TryGetValue(balance, out int prevIndex))
            {
                // The subarray from (prevIndex+1) to i is balanced.
                // Its length is: i - prevIndex
                int currentLength = i - prevIndex;

                // Update maxLength if this subarray is longer than what we've seen.
                if (currentLength > maxLength)
                {
                    maxLength = currentLength;
                }

                // IMPORTANT: We do NOT update the map here.
                // We always want the FIRST (earliest) occurrence of each balance value
                // to maximize the subarray length. Overwriting would shrink our window.
            }
            else
            {
                // -------------------------------------------------------
                // Step 3d: First time seeing this balance value.
                //
                // Record the current index as the first occurrence of this balance.
                // Future iterations that reach the same balance can compute
                // the distance back to this point.
                // -------------------------------------------------------
                firstOccurrence[balance] = i;
            }
        }

        // -------------------------------------------------------
        // Step 4: Return the maximum balanced subarray length found.
        //
        // If no balanced subarray exists (e.g., all 'A's or all 'B's),
        // maxLength remains 0, which is the correct answer.
        // -------------------------------------------------------
        return maxLength;
    }

    /// <summary>
    /// Follow-up: Generalized solution for k distinct labels.
    /// Finds the longest subarray where ALL distinct labels appear with equal frequency.
    ///
    /// Approach:
    /// For k labels, we track a "difference signature" between consecutive label counts.
    /// We normalize by subtracting the count of the first label from all others,
    /// creating a tuple of (k-1) differences. When the same difference tuple repeats,
    /// the subarray between those two positions is balanced.
    ///
    /// Time Complexity:  O(n * k) — n elements, each requiring O(k) to build the signature key
    /// Space Complexity: O(n * k) — storing signatures in the hash map
    /// </summary>
    /// <param name="labels">Array of strings with arbitrary label values</param>
    /// <returns>Length of the longest subarray where all labels appear equally often</returns>
    public int FindLongestBalancedSubarrayGeneralized(string[] labels)
    {
        // -------------------------------------------------------
        // Step 1: Identify all distinct labels in the input.
        //
        // We need to know which labels exist so we can track their counts.
        // -------------------------------------------------------
        var distinctLabels = new List<string>();
        var labelSet = new HashSet<string>();
        foreach (var label in labels)
        {
            if (labelSet.Add(label))
            {
                distinctLabels.Add(label);
            }
        }

        int k = distinctLabels.Count;

        // Edge case: only one distinct label → no balanced subarray possible
        if (k == 1)
        {
            return 0;
        }

        // -------------------------------------------------------
        // Step 2: Map each label to an index for fast lookup.
        // -------------------------------------------------------
        var labelIndex = new Dictionary<string, int>();
        for (int i = 0; i < k; i++)
        {
            labelIndex[distinctLabels[i]] = i;
        }

        // -------------------------------------------------------
        // Step 3: Track prefix counts for each label.
        //
        // counts[j] = how many times label j has appeared so far.
        // -------------------------------------------------------
        int[] counts = new int[k];

        // -------------------------------------------------------
        // Step 4: Use a hash map from "difference signature" → first index.
        //
        // The signature is: (count[1]-count[0], count[2]-count[0], ..., count[k-1]-count[0])
        // When the same signature repeats, all labels have grown by the same amount
        // in between, meaning they appear equally in that subarray.
        //
        // Initialize with the zero-difference signature at index -1 (before the array).
        // -------------------------------------------------------
        var firstOccurrenceGen = new Dictionary<string, int>();
        string initialKey = BuildKey(counts, k);
        firstOccurrenceGen[initialKey] = -1;

        int maxLen = 0;

        // -------------------------------------------------------
        // Step 5: Scan through labels, updating counts and checking signatures.
        // -------------------------------------------------------
        for (int i = 0; i < labels.Length; i++)
        {
            // Update the count for the current label
            counts[labelIndex[labels[i]]]++;

            // Build the current difference signature
            string key = BuildKey(counts, k);

            if (firstOccurrenceGen.TryGetValue(key, out int prev))
            {
                // Subarray from (prev+1) to i is balanced across all labels
                int len = i - prev;
                if (len > maxLen) maxLen = len;
            }
            else
            {
                firstOccurrenceGen[key] = i;
            }
        }

        return maxLen;
    }

    /// <summary>
    /// Helper: Builds a string key representing the difference signature.
    /// The key encodes (counts[1]-counts[0], counts[2]-counts[0], ...).
    /// </summary>
    private string BuildKey(int[] counts, int k)
    {
        // Use counts[0] as the baseline; compute differences for all other labels
        var parts = new System.Text.StringBuilder();
        for (int j = 1; j < k; j++)
        {
            parts.Append(counts[j] - counts[0]);
            parts.Append(',');
        }
        return parts.ToString();
    }
}

// ============================================================
// Demo / Test Code (Top-Level Statements)
// ============================================================

var solution = new Solution();

Console.WriteLine("=== Find Longest Subarray with Equal Frequency of Two Labels ===");
Console.WriteLine();

// -------------------------------------------------------
// Example 1:
// Input:  ["A", "B", "B", "A", "A", "B", "A"]
// Expected Output: 6
//
// Trace:
//   i=-1 (sentinel): balance=0, map={0:-1}
//   i=0, label=A: balance=1, not in map → map={0:-1, 1:0}
//   i=1, label=B: balance=0, seen at -1 → length=1-(-1)=2, maxLength=2
//   i=2, label=B: balance=-1, not in map → map={..., -1:2}
//   i=3, label=A: balance=0, seen at -1 → length=3-(-1)=4, maxLength=4
//   i=4, label=A: balance=1, seen at 0 → length=4-0=4, maxLength=4
//   i=5, label=B: balance=0, seen at -1 → length=5-(-1)=6, maxLength=6  ← winner!
//   i=6, label=A: balance=1, seen at 0 → length=6-0=6, maxLength=6
//
// Result: 6 ✓
// -------------------------------------------------------
string[] labels1 = ["A", "B", "B", "A", "A", "B", "A"];
int result1 = solution.FindLongestBalancedSubarray(labels1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  Input:    [{string.Join(", ", labels1)}]");
Console.WriteLine($"  Output:   {result1}");
Console.WriteLine($"  Expected: 6");
Console.WriteLine($"  Pass:     {result1 == 6}");
Console.WriteLine();

// -------------------------------------------------------
// Example 2:
// Input:  ["A", "A", "A", "B"]
// Expected Output: 2
//
// Trace:
//   i=-1 (sentinel): balance=0, map={0:-1}
//   i=0, label=A: balance=1, not in map → map={0:-1, 1:0}
//   i=1, label=A: balance=2, not in map → map={..., 2:1}
//   i=2, label=A: balance=3, not in map → map={..., 3:2}
//   i=3, label=B: balance=2, seen at 1 → length=3-1=2, maxLength=2
//
// Result: 2 ✓
// -------------------------------------------------------
string[] labels2 = ["A", "A", "A", "B"];
int result2 = solution.FindLongestBalancedSubarray(labels2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  Input:    [{string.Join(", ", labels2)}]");
Console.WriteLine($"  Output:   {result2}");
Console.WriteLine($"  Expected: 2");
Console.WriteLine($"  Pass:     {result2 == 2}");
Console.WriteLine();

// -------------------------------------------------------
// Additional Edge Case 1: All same label
// Input:  ["A", "A", "A"]
// Expected Output: 0 (no balanced subarray)
// -------------------------------------------------------
string[] labels3 = ["A", "A", "A"];
int result3 = solution.FindLongestBalancedSubarray(labels3);
Console.WriteLine($"Edge Case 1 (All same label):");
Console.WriteLine($"  Input:    [{string.Join(", ", labels3)}]");
Console.WriteLine($"  Output:   {result3}");
Console.WriteLine($"  Expected: 0");
Console.WriteLine($"  Pass:     {result3 == 0}");
Console.WriteLine();

// -------------------------------------------------------
// Additional Edge Case 2: Perfectly balanced
// Input:  ["A", "B"]
// Expected Output: 2
// -------------------------------------------------------
string[] labels4 = ["A", "B"];
int result4 = solution.FindLongestBalancedSubarray(labels4);
Console.WriteLine($"Edge Case 2 (Perfectly balanced pair):");
Console.WriteLine($"  Input:    [{string.Join(", ", labels4)}]");
Console.WriteLine($"  Output:   {result4}");
Console.WriteLine($"  Expected: 2");
Console.WriteLine($"  Pass:     {result4 == 2}");
Console.WriteLine();

// -------------------------------------------------------
// Additional Edge Case 3: Single element
// Input:  ["B"]
// Expected Output: 0
// -------------------------------------------------------
string[] labels5 = ["B"];
int result5 = solution.FindLongestBalancedSubarray(labels5);
Console.WriteLine($"Edge Case 3 (Single element):");
Console.WriteLine