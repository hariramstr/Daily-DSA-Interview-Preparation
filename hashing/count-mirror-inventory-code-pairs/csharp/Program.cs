/*
Title: Count Mirror Inventory Code Pairs
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given an array of product codes used in a warehouse system. Each code is a non-empty lowercase string.
Two codes form a mirror pair if one code can be transformed into the other by reversing the order of its
characters and then rotating the result by any number of positions, including zero.

Example:
- Reverse("abca") = "acba"
- Rotations of "acba" are: "acba", "cbaa", "baac", "aacb"

So any code equal to one of those strings forms a mirror pair with "abca".

Your task is to count how many unordered index pairs (i, j) with i < j are mirror pairs.

Important observation:
Two strings s and t form a mirror pair exactly when:
t is a rotation of reverse(s)

This can be rewritten in a very useful equivalent form:
reverse(t) is a rotation of s

And because "being a rotation of" is an equivalence relation that is preserved by reversal,
the condition becomes:
s and reverse(t) belong to the same rotation class

That means we can assign every string a canonical representative for its rotation class.
Then for each code:
- compute the canonical rotation of the code itself
- compute the canonical rotation of its reversed string

If canonical(code_i) == canonical(reverse(code_j)), then i and j form a valid mirror pair.

To count unordered pairs efficiently:
- process codes from left to right
- maintain a frequency map of canonical forms of previously seen original strings
- for the current string s, compute canonical(reverse(s))
- every previous string whose canonical(original) equals canonical(reverse(s)) forms a valid pair with s

This avoids O(n^2) comparisons.

Constraints:
- 1 <= codes.length <= 100000
- 1 <= codes[i].length <= 50
- codes[i] contains only lowercase English letters
- Only codes of the same length can form a mirror pair
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of codes
    - Let m be the maximum code length (m <= 50)
    - For each string, we compute:
      1) canonical rotation of the original string
      2) canonical rotation of the reversed string
    - We use Booth's algorithm to find the lexicographically smallest rotation in O(m)
    - Building the canonical string itself also takes O(m)
    - Therefore each code costs O(m), and total time is O(n * m)

    Space Complexity:
    - The hash map stores at most one entry per distinct canonical rotation
    - In the worst case this is O(n * m) total character storage across keys
    - Auxiliary working space per string is O(m)
    */
    public long CountMirrorPairs(string[] codes)
    {
        // This dictionary stores how many previously processed strings belong to each
        // rotation class, represented by a canonical string.
        //
        // Key:
        //   canonical rotation of the ORIGINAL previously seen string
        //
        // Value:
        //   how many times we have seen that canonical form so far
        //
        // Why this works:
        // For the current string s, we want to know how many earlier strings p satisfy:
        //   p is a mirror pair with s
        //
        // By the problem rule:
        //   s must be a rotation of reverse(p)
        //
        // Equivalent reformulation:
        //   p must belong to the same rotation class as reverse(s)
        //
        // So if we compute canonical(reverse(s)), then every earlier string whose
        // canonical(original) equals that value contributes one valid pair.
        var seenCanonicalOriginal = new Dictionary<string, long>();

        long totalPairs = 0;

        foreach (var code in codes)
        {
            // STEP 1: Compute the canonical representative of the reversed current string.
            //
            // This tells us which earlier original strings can pair with the current code.
            //
            // If an earlier string p has:
            //   canonical(p) == canonical(reverse(code))
            // then p and code form a valid mirror pair.
            string reversed = ReverseString(code);
            string canonicalOfReversed = CanonicalRotation(reversed);

            // STEP 2: Count how many earlier strings match this needed rotation class.
            //
            // Each such earlier string forms exactly one unordered pair with the current index,
            // because we process from left to right and only count pairs with previous elements.
            if (seenCanonicalOriginal.TryGetValue(canonicalOfReversed, out long count))
            {
                totalPairs += count;
            }

            // STEP 3: Insert the current string into the "seen" structure using the canonical
            // rotation of the ORIGINAL string.
            //
            // This prepares it to be matched by future strings.
            string canonicalOfOriginal = CanonicalRotation(code);

            if (seenCanonicalOriginal.ContainsKey(canonicalOfOriginal))
            {
                seenCanonicalOriginal[canonicalOfOriginal]++;
            }
            else
            {
                seenCanonicalOriginal[canonicalOfOriginal] = 1;
            }
        }

        return totalPairs;
    }

    private string ReverseString(string s)
    {
        // We convert to a character array so we can reverse efficiently in-place.
        char[] chars = s.ToCharArray();
        Array.Reverse(chars);
        return new string(chars);
    }

    private string CanonicalRotation(string s)
    {
        // The canonical representative of a rotation class is chosen as the
        // lexicographically smallest rotation.
        //
        // Example:
        // s = "cbaa"
        // rotations are:
        //   "cbaa", "baac", "aacb", "acba"
        // smallest is "aacb"
        //
        // Any two strings that are rotations of each other will produce the same
        // lexicographically smallest rotation, so this is a perfect canonical signature.
        int start = BoothSmallestRotationIndex(s);
        return RotateFromIndex(s, start);
    }

    private int BoothSmallestRotationIndex(string s)
    {
        // Booth's algorithm finds the starting index of the lexicographically smallest
        // rotation in linear time.
        //
        // Beginner-friendly intuition:
        // - Imagine all rotations of the string.
        // - We want the smallest one in dictionary order.
        // - A naive solution would compare all rotations, which is too slow in general.
        // - Booth's algorithm cleverly skips impossible candidates.
        //
        // We work on the doubled string conceptually:
        //   s + s
        // so that any rotation of length n appears as a contiguous substring.
        int n = s.Length;

        // For length 1, the only rotation starts at index 0.
        if (n == 1)
        {
            return 0;
        }

        // i and j are two candidate starting positions for the smallest rotation.
        // k is how many characters into the current comparison we have matched so far.
        int i = 0;
        int j = 1;
        int k = 0;

        while (i < n && j < n && k < n)
        {
            char a = s[(i + k) % n];
            char b = s[(j + k) % n];

            if (a == b)
            {
                // If the compared characters are equal, continue comparing the next position.
                k++;
                continue;
            }

            if (a > b)
            {
                // Rotation starting at i is worse than rotation starting at j,
                // because at the first differing character it has a larger letter.
                //
                // Therefore, any start between i and i+k cannot be the smallest either,
                // so we skip them all at once.
                i = i + k + 1;

                // If both candidates collide, move one ahead to keep them distinct.
                if (i == j)
                {
                    i++;
                }
            }
            else
            {
                // Symmetric case: rotation at j is worse, so skip j..j+k.
                j = j + k + 1;

                if (i == j)
                {
                    j++;
                }
            }

            // Reset matched prefix length because we are comparing new candidates now.
            k = 0;
        }

        // The smaller of the surviving candidates is the answer.
        return Math.Min(i, j) % n;
    }

    private string RotateFromIndex(string s, int start)
    {
        // Build the rotation:
        // s[start..end] + s[0..start-1]
        //
        // Example:
        // s = "acba", start = 3
        // result = "a" + "acb" = "aacb"
        int n = s.Length;
        char[] result = new char[n];

        for (int i = 0; i < n; i++)
        {
            result[i] = s[(start + i) % n];
        }

        return new string(result);
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] codes1 = { "abca", "cbaa", "zz", "zz", "aacb" };
long result1 = solution.CountMirrorPairs(codes1);
Console.WriteLine(result1); // Expected: 3

// Example 2
string[] codes2 = { "abc", "cab", "bca", "xy", "yx", "aa" };
long result2 = solution.CountMirrorPairs(codes2);
Console.WriteLine(result2); // Expected: 2

// Additional quick sanity checks
string[] codes3 = { "a", "a", "b" };
Console.WriteLine(solution.CountMirrorPairs(codes3)); // Expected: 1

string[] codes4 = { "ab", "ba", "ab", "ba" };
Console.WriteLine(solution.CountMirrorPairs(codes4)); // Expected: 6