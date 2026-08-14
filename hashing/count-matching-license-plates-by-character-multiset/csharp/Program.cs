/*
Title: Count Matching License Plates by Character Multiset
Difficulty: Medium
Topic: Hashing

Problem Description:
A parking analytics system stores vehicle license plates as uppercase alphanumeric strings.
Two plates are considered matching if they contain exactly the same characters with the same
frequencies, regardless of order.

Examples:
- "A1B1" and "1AB1" match
- "AB12" and "AB21" match
- "AAB1" and "AB11" do not match

Given an array plates, return the number of unordered pairs of indices (i, j) such that
i < j and plates[i] matches plates[j] by character multiset.

Efficient idea:
Convert each plate into a canonical signature that records the frequency of every possible
character ('0'..'9' and 'A'..'Z'). Then use a hash map to count how many previous plates
already had the same signature.

Constraints:
- 1 <= plates.length <= 100000
- 1 <= plates[i].length <= 20
- plates[i] consists only of characters 'A' to 'Z' and digits '0' to '9'
- The answer may be large, so use a 64-bit integer type where needed
*/

using System;
using System.Collections.Generic;
using System.Text;

public class Solution
{
    // Time Complexity:
    // Let n be the number of plates, and let L be the maximum plate length.
    // For each plate, we count characters in O(L), then build a fixed-size signature
    // over 36 possible characters in O(36), which is effectively constant.
    // Total: O(n * (L + 36)) = O(n * L)
    //
    // Space Complexity:
    // The dictionary stores one entry per distinct signature.
    // In the worst case, all plates are different, so O(n) extra space.
    public long CountMatchingPairs(string[] plates)
    {
        // This dictionary maps:
        //   signature of a plate -> how many times we have seen this signature so far
        //
        // Why do we need this?
        // If the current plate has a signature that has already appeared k times,
        // then the current plate forms exactly k new matching pairs:
        // one pair with each previous plate that had the same signature.
        var signatureCounts = new Dictionary<string, long>();

        // We use long because the number of pairs can be large.
        // For example, if many plates are identical by multiset, the number of pairs
        // can exceed the range of int.
        long totalPairs = 0;

        // Process each plate one by one.
        foreach (var plate in plates)
        {
            // Step 1: Build a frequency array for all allowed characters.
            //
            // There are exactly 36 possible characters:
            // - 10 digits: '0' to '9'
            // - 26 uppercase letters: 'A' to 'Z'
            //
            // We store counts in a fixed-size array:
            // indices 0..9   -> digits '0'..'9'
            // indices 10..35 -> letters 'A'..'Z'
            //
            // Why is this useful?
            // Because two plates match if and only if all 36 character counts are identical.
            int[] freq = new int[36];

            // Step 2: Count each character in the current plate.
            //
            // We examine every character and map it to the correct slot in the frequency array.
            foreach (char ch in plate)
            {
                if (ch >= '0' && ch <= '9')
                {
                    // Digits go into positions 0..9.
                    freq[ch - '0']++;
                }
                else
                {
                    // Letters 'A'..'Z' go into positions 10..35.
                    freq[10 + (ch - 'A')]++;
                }
            }

            // Step 3: Convert the frequency array into a canonical string signature.
            //
            // Why do we need a signature string?
            // Arrays cannot be used directly as dictionary keys in a value-based way,
            // because array equality in C# is reference-based by default.
            //
            // So we serialize the 36 counts into a string such that:
            // - plates with the same character multiset produce exactly the same string
            // - plates with different multisets produce different strings
            //
            // Example:
            // If a plate has:
            //   '1' count = 2
            //   'A' count = 1
            //   'B' count = 1
            // and all others 0,
            // then its signature will reflect those exact counts in fixed positions.
            string signature = BuildSignature(freq);

            // Step 4: If we have seen this signature before, then each previous occurrence
            // forms a new matching pair with the current plate.
            //
            // Example:
            // Suppose this signature has appeared 3 times already.
            // Then the current plate forms 3 new pairs.
            if (signatureCounts.TryGetValue(signature, out long seenCount))
            {
                totalPairs += seenCount;
                signatureCounts[signature] = seenCount + 1;
            }
            else
            {
                // First time seeing this signature.
                signatureCounts[signature] = 1;
            }
        }

        // After processing all plates, totalPairs contains the number of unordered pairs.
        return totalPairs;
    }

    private string BuildSignature(int[] freq)
    {
        // We build a compact but unambiguous representation.
        //
        // Important detail:
        // We must separate numbers with a delimiter, otherwise counts like [1, 11]
        // and [11, 1] could become ambiguous if simply concatenated.
        //
        // Using '#' between counts guarantees uniqueness of the representation.
        var sb = new StringBuilder();

        for (int i = 0; i < freq.Length; i++)
        {
            sb.Append(freq[i]);
            sb.Append('#');
        }

        return sb.ToString();
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// Correct analysis:
// "A1B1" and "1AB1" match
// "AB12" and "B2A1" match
// "XYZ" and "ZYX" match
// Total = 3
string[] plates1 = { "A1B1", "1AB1", "AB12", "B2A1", "XYZ", "ZYX" };
long result1 = solution.CountMatchingPairs(plates1);
Console.WriteLine(result1); // Expected: 3

// Example 2 from the prompt.
// First group: "AA11", "1A1A", "A11A" => 3 choose 2 = 3
// Second group: "BB2", "2BB", "B2B"   => 3 choose 2 = 3
// "C3" matches none
// Total = 6
string[] plates2 = { "AA11", "1A1A", "A11A", "BB2", "2BB", "B2B", "C3" };
long result2 = solution.CountMatchingPairs(plates2);
Console.WriteLine(result2); // Expected: 6

// Additional quick sanity check:
// "AB12" and "AB21" match because order does not matter.
string[] plates3 = { "AB12", "AB21" };
long result3 = solution.CountMatchingPairs(plates3);
Console.WriteLine(result3); // Expected: 1