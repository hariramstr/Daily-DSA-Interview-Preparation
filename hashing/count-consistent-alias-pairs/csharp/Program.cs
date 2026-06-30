/*
Title: Count Consistent Alias Pairs
Difficulty: Medium
Topic: Hashing

Problem Description:
A messaging platform stores user aliases as lowercase strings. Two aliases are considered consistent
if they use exactly the same set of distinct characters, regardless of order or how many times each
character appears.

For example:
- "abbca" and "cba" are consistent because both contain the character set {a, b, c}
- "aabc" and "abdd" are not consistent

Given an array aliases, return the number of index pairs (i, j) such that:
0 <= i < j < aliases.length
and aliases[i] is consistent with aliases[j].

The key idea is to convert each alias into a canonical representation of its distinct characters.
Because aliases contain only lowercase English letters, we can represent the set of characters
using a 26-bit integer bitmask:
- bit 0 represents 'a'
- bit 1 represents 'b'
- ...
- bit 25 represents 'z'

Then:
- aliases with the same bitmask have exactly the same set of distinct letters
- we count how many times each bitmask has already appeared
- for each new alias, the number of new valid pairs equals the number of previous aliases
  with the same bitmask
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n * m)
    where:
    - n = number of aliases
    - m = average length of an alias
    We scan each character of each string once to build its bitmask.

    Space Complexity:
    O(k)
    where:
    - k = number of distinct bitmasks seen
    In the worst case, this is at most min(n, 2^26), but practically at most n.
    */
    public long CountConsistentPairs(string[] aliases)
    {
        // This dictionary stores:
        // key   -> the canonical bitmask representing the set of distinct letters in an alias
        // value -> how many previous aliases produced exactly this same bitmask
        //
        // Why a dictionary?
        // We need very fast lookup and update for each alias.
        // Dictionary gives average O(1) time for both operations.
        var frequencyByMask = new Dictionary<int, long>();

        // We use long for the answer because the number of pairs can be large.
        // Example:
        // if all 100,000 aliases are consistent with each other,
        // the number of pairs is 100000 * 99999 / 2 = 4,999,950,000,
        // which does NOT fit in int.
        long pairCount = 0;

        // Process aliases one by one.
        // For each alias:
        // 1. Build its bitmask
        // 2. See how many previous aliases had the same bitmask
        // 3. Add that count to the answer
        // 4. Record this alias in the dictionary
        foreach (var alias in aliases)
        {
            // Start with an empty set of characters.
            // In bitmask form, 0 means no letters are present.
            int mask = 0;

            // Read every character in the current alias.
            foreach (char ch in alias)
            {
                // Convert the character into a bit position:
                // 'a' -> 0
                // 'b' -> 1
                // ...
                // 'z' -> 25
                int bitIndex = ch - 'a';

                // Turn on the corresponding bit.
                //
                // Example:
                // if ch == 'c', bitIndex == 2
                // (1 << 2) == 000...0100
                //
                // Using OR means:
                // - if the bit was off, it becomes on
                // - if the bit was already on, it stays on
                //
                // This is exactly what we want because we only care about DISTINCT characters.
                // Repeated letters should not change the final representation.
                mask |= 1 << bitIndex;
            }

            // At this point, "mask" is the canonical representation of the alias.
            //
            // Examples:
            // "abbca" -> bits for a, b, c are on
            // "cba"   -> bits for a, b, c are on
            // Therefore both produce the same mask.
            //
            // If we have seen this mask before, then every previous alias with this mask
            // forms a valid pair with the current alias.
            if (frequencyByMask.TryGetValue(mask, out long previousCount))
            {
                // Add all new pairs formed with earlier matching aliases.
                pairCount += previousCount;

                // Now include the current alias in the frequency count.
                frequencyByMask[mask] = previousCount + 1;
            }
            else
            {
                // First time we see this mask.
                // No pair is formed yet, because there are no earlier aliases with this mask.
                frequencyByMask[mask] = 1;
            }
        }

        // After processing all aliases, pairCount contains the total number of valid pairs.
        return pairCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] aliases1 = { "abbca", "cba", "aaaa", "a", "bac", "xy", "yx" };
long result1 = solution.CountConsistentPairs(aliases1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 5

// Quick verification for Example 1:
// "abbca", "cba", "bac" -> same set {a,b,c} -> 3 pairs
// "aaaa", "a"           -> same set {a}     -> 1 pair
// "xy", "yx"            -> same set {x,y}   -> 1 pair
// Total = 5

// Example 2
string[] aliases2 = { "abc", "de", "eed", "fff", "fed", "cab", "xyz" };
long result2 = solution.CountConsistentPairs(aliases2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 2

// Quick verification for Example 2:
// "abc" and "cab" -> same set {a,b,c}
// "de" and "eed"  -> same set {d,e}
// "fff" -> {f}, "fed" -> {d,e,f}, "xyz" -> {x,y,z}
// Total = 2

// Additional small sanity check
string[] aliases3 = { "a", "aa", "aaa", "b", "ab", "ba" };
long result3 = solution.CountConsistentPairs(aliases3);
Console.WriteLine("Additional Test Result: " + result3); // Expected: 4

// Explanation for additional test:
// "a", "aa", "aaa" -> all map to {a} -> C(3,2) = 3 pairs
// "ab", "ba"       -> both map to {a,b} -> 1 pair
// Total = 4