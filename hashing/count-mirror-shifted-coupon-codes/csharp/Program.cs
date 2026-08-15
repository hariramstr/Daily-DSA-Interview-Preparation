/*
Title: Count Mirror-Shifted Coupon Codes
Difficulty: Medium
Topic: Hashing

Problem Description:
An e-commerce platform stores promotional coupon codes as strings of lowercase English letters.
Two coupon codes are considered mirror-shifted if one can be transformed into the other by
applying the same cyclic alphabet shift to every character.

Examples:
- "abc" shifted by 2 becomes "cde"
- "xyz" shifted by 3 becomes "abc" because the alphabet wraps around

Therefore, "abc", "bcd", and "xyz" all belong to the same mirror-shifted group.
However, strings of different lengths can never belong to the same group.

Given an array codes where each element is a non-empty string, count how many unordered pairs
of indices (i, j) with i < j belong to the same mirror-shifted group.

A standard approach is:
- Convert each string into a canonical signature
- The signature is based on the differences between consecutive characters modulo 26
- Strings with the same signature belong to the same mirror-shifted group
- Use a hash map to count how many times each signature appears
- For each new string with an already-seen signature, add the current count to the answer

Important note:
- Any two single-character strings are always mirror-shifted, because one letter can always be
  shifted to any other letter.

Example 1:
Input: ["abc", "bcd", "ace", "xyz", "az", "ba", "a", "z"]
Output: 5

Grouping:
- ["abc", "bcd", "xyz"] -> 3 pairs
- ["az", "ba"] -> 1 pair
- ["a", "z"] -> 1 pair
Total = 5

Example 2:
Input: ["aa", "bb", "ab", "za", "yx"]
Output: 4

Grouping:
- ["aa", "bb"] -> 1 pair
- ["ab", "za", "yx"] -> 3 pairs
Total = 4
*/

using System;
using System.Collections.Generic;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    O(T), where T is the sum of lengths of all strings.
    Reason:
    - We process each string once.
    - For each string of length L, we compute its signature in O(L).
    - The total of all L values is bounded by the total input size.

    Space Complexity:
    O(T) in the worst case.
    Reason:
    - We store one signature per distinct group in the hash map.
    - The total size of all generated signatures is proportional to the total input size.
    */
    public long CountMirrorShiftedPairs(string[] codes)
    {
        // This dictionary maps:
        //   signature -> how many strings with this signature we have seen so far
        //
        // Why a dictionary?
        // Because we need very fast lookup and update by signature.
        // In C#, Dictionary provides average O(1) insert/find time.
        var signatureCount = new Dictionary<string, long>();

        // We use long for the answer because the number of pairs can be large.
        // For example, if all 100000 strings belong to the same group,
        // the number of pairs is 100000 * 99999 / 2, which does not fit in int.
        long totalPairs = 0;

        // Process each code one by one.
        foreach (var code in codes)
        {
            // Convert the current string into its canonical signature.
            //
            // Why do we need a canonical signature?
            // Because directly comparing every string with every other string would be too slow:
            // O(n^2) comparisons is not acceptable for large input sizes.
            //
            // Instead, we transform each string into a representation such that:
            // - mirror-shifted strings produce the same signature
            // - non-equivalent strings produce different signatures
            //
            // Then the problem becomes:
            // "How many pairs of equal signatures are there?"
            string signature = BuildSignature(code);

            // If we have already seen this signature k times,
            // then the current string forms exactly k new valid pairs:
            // one with each previously seen string in the same group.
            if (signatureCount.TryGetValue(signature, out long seenSoFar))
            {
                totalPairs += seenSoFar;
                signatureCount[signature] = seenSoFar + 1;
            }
            else
            {
                // First time seeing this signature.
                signatureCount[signature] = 1;
            }
        }

        return totalPairs;
    }

    private string BuildSignature(string s)
    {
        // Special case: single-character strings.
        //
        // Why is this special?
        // Because any one-letter string can be shifted to any other one-letter string.
        // Example:
        // - "a" can shift to "z"
        // - "m" can shift to "b"
        //
        // So all length-1 strings must share the same signature.
        if (s.Length == 1)
        {
            return "#";
        }

        // We build a signature from the differences between consecutive characters.
        //
        // Example:
        // "abc" -> differences:
        //   b - a = 1
        //   c - b = 1
        // signature: "1,1"
        //
        // "bcd" -> differences:
        //   c - b = 1
        //   d - c = 1
        // signature: "1,1"
        //
        // "xyz" -> differences:
        //   y - x = 1
        //   z - y = 1
        // signature: "1,1"
        //
        // So all of them match, as desired.
        //
        // Why consecutive differences work:
        // If every character is shifted by the same amount, the relative gaps between
        // neighboring characters stay unchanged modulo 26.
        //
        // We also need modulo 26 because the alphabet wraps around.
        // Example:
        // "az"
        //   z - a = 25
        // signature: "25"
        //
        // "ba"
        //   a - b = -1
        // modulo 26 => 25
        // signature: "25"
        //
        // Therefore "az" and "ba" are grouped together correctly.

        var sb = new StringBuilder();

        // Include the length in the signature to make the grouping explicit.
        // Different lengths can never be in the same group.
        // While the number of differences already implies length,
        // including length makes the signature clearer and safer.
        sb.Append(s.Length);
        sb.Append('|');

        for (int i = 1; i < s.Length; i++)
        {
            // Compute the raw difference between current and previous character.
            int diff = s[i] - s[i - 1];

            // Convert it into the range [0, 25] using modulo arithmetic.
            //
            // In C#, negative % positive can still be negative,
            // so we use (diff + 26) % 26 to guarantee a non-negative result.
            diff = (diff + 26) % 26;

            // Append a separator before each number so signatures are unambiguous.
            //
            // For example, without separators:
            // [1, 11] and [11, 1] could both look confusing as "111"
            //
            // With separators:
            // "|1|11" vs "|11|1"
            sb.Append(diff);
            sb.Append('|');
        }

        return sb.ToString();
    }
}

// -------------------------
// Demo / sample test code
// -------------------------

var solution = new Solution();

// Example 1
string[] codes1 = { "abc", "bcd", "ace", "xyz", "az", "ba", "a", "z" };
long result1 = solution.CountMirrorShiftedPairs(codes1);
Console.WriteLine(result1); // Expected: 5

// Example 2
string[] codes2 = { "aa", "bb", "ab", "za", "yx" };
long result2 = solution.CountMirrorShiftedPairs(codes2);
Console.WriteLine(result2); // Expected: 4

// Additional quick sanity checks

// All single-character strings belong to one group.
// 3 strings => 3 pairs: (0,1), (0,2), (1,2)
string[] codes3 = { "a", "m", "z" };
long result3 = solution.CountMirrorShiftedPairs(codes3);
Console.WriteLine(result3); // Expected: 3

// No matching groups
string[] codes4 = { "ab", "ac", "aaa", "b" };
long result4 = solution.CountMirrorShiftedPairs(codes4);
Console.WriteLine(result4); // Expected: 0

// Mixed wrap-around cases
string[] codes5 = { "az", "ba", "cb", "yx" };
// "az" -> 25
// "ba" -> 25
// "cb" -> 25
// "yx" -> 25
// 4 strings in one group => 6 pairs
long result5 = solution.CountMirrorShiftedPairs(codes5);
Console.WriteLine(result5); // Expected: 6