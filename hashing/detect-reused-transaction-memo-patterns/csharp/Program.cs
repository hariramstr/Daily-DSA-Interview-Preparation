/*
Title: Detect Reused Transaction Memo Patterns
Difficulty: Medium
Topic: Hashing

Problem Description:
A payments platform stores the free-text memo attached to each transaction as an array of lowercase words.
Two memos are considered to have the same pattern if the sequence of word repetitions is identical,
even if the actual words are different.

Example:
["rent", "paid", "rent", "late"] and ["coffee", "today", "coffee", "again"]
share the same pattern because positions 0 and 2 match each other in both memos,
while positions 1 and 3 are distinct from the others.

Given a list of memos, return all indices of memos that belong to a pattern group of size at least 2.
The returned indices must be sorted in increasing order.
Two memos can only be grouped together if they have the same length and the same repetition structure.

Constraints:
- 1 <= memos.length <= 100000
- 1 <= memos[i].length <= 100
- 1 <= memos[i][j].length <= 20
- memos[i][j] contains only lowercase English letters
- The sum of all words across all memos does not exceed 300000
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    - Let W be the total number of words across all memos.
    - We process each word once while building a canonical signature for each memo.
    - Therefore the total time is O(W), ignoring the small cost of string building proportional to memo length,
      which is still covered by the total number of words processed.
    - We also sort the final answer indices. In the worst case, if K indices are returned, that costs O(K log K).
    - Overall: O(W + K log K)

    Space Complexity:
    - We store hash maps for grouping signatures and temporary maps while normalizing each memo.
    - The total extra space is O(W) in the worst case due to stored signatures and grouped indices.
    */
    public IList<int> FindMemosInRepeatedPatternGroups(IList<IList<string>> memos)
    {
        // This dictionary groups memo indices by their normalized pattern signature.
        //
        // Key:
        //   A canonical string such as "0,1,0,2"
        //   representing the repetition structure of a memo.
        //
        // Value:
        //   A list of all memo indices that share that exact pattern.
        //
        // Why use a dictionary?
        //   Because we want to quickly find all memos with the same pattern.
        //   Hash maps give average O(1) insertion and lookup time.
        var groups = new Dictionary<string, List<int>>();

        // We iterate through every memo exactly once.
        // For each memo, we convert it into a canonical pattern signature.
        for (int memoIndex = 0; memoIndex < memos.Count; memoIndex++)
        {
            var memo = memos[memoIndex];

            // Build a normalized signature for the current memo.
            //
            // Example:
            //   ["rent", "paid", "rent", "late"]
            //
            // We assign the first unique word the id 0,
            // the next new word the id 1,
            // if a word repeats we reuse its previous id,
            // and so on.
            //
            // So:
            //   "rent" -> 0
            //   "paid" -> 1
            //   "rent" -> 0
            //   "late" -> 2
            //
            // Signature becomes: "0,1,0,2"
            string signature = BuildSignature(memo);

            // If this signature has not been seen before,
            // create a new list to hold indices for this pattern group.
            if (!groups.ContainsKey(signature))
            {
                groups[signature] = new List<int>();
            }

            // Add the current memo index to its pattern group.
            groups[signature].Add(memoIndex);
        }

        // This list will store all indices that belong to groups of size at least 2.
        var result = new List<int>();

        // Now examine every pattern group we built.
        foreach (var entry in groups)
        {
            var indices = entry.Value;

            // The problem asks us to return indices only from groups
            // that contain at least two memos.
            //
            // If a group has only one memo, that memo does not share its pattern
            // with any other memo, so it should not be included.
            if (indices.Count >= 2)
            {
                result.AddRange(indices);
            }
        }

        // The problem requires the final indices to be sorted in increasing order.
        //
        // Even though indices were added in original traversal order within each group,
        // groups themselves are stored in a dictionary, and dictionary iteration order
        // should not be relied upon for correctness.
        //
        // Therefore we explicitly sort the final result.
        result.Sort();

        return result;
    }

    private string BuildSignature(IList<string> memo)
    {
        // This dictionary maps each distinct word in the current memo
        // to the first pattern id assigned to it.
        //
        // Example for ["coffee", "today", "coffee", "again"]:
        //   "coffee" -> 0
        //   "today"  -> 1
        //   "again"  -> 2
        //
        // Why is this local to one memo?
        //   Because pattern ids are only meaningful inside a single memo.
        //   We are not comparing actual words across different memos.
        //   We only care about the repetition structure.
        var wordToId = new Dictionary<string, int>();

        // nextId tracks the next unused pattern number.
        // The first new word gets 0, the next gets 1, and so on.
        int nextId = 0;

        // StringBuilder is used to efficiently construct the signature string.
        //
        // Why not repeatedly concatenate strings?
        //   Because strings are immutable in C#, so repeated concatenation can be slower.
        //   StringBuilder is the standard efficient tool for this.
        var signatureBuilder = new StringBuilder();

        // Process each word in order, because pattern depends on position.
        for (int i = 0; i < memo.Count; i++)
        {
            string word = memo[i];

            // If this word has never appeared before in this memo,
            // assign it a new pattern id.
            if (!wordToId.ContainsKey(word))
            {
                wordToId[word] = nextId;
                nextId++;
            }

            // Append a separator before every element except the first.
            //
            // This avoids ambiguity.
            // For example, without separators:
            //   [0,11] and [0,1,1] could both look like "011"
            //
            // With commas:
            //   "0,11" and "0,1,1" are clearly different.
            if (i > 0)
            {
                signatureBuilder.Append(',');
            }

            // Append the canonical id for the current word.
            signatureBuilder.Append(wordToId[word]);
        }

        // Convert the built signature into a string key for hashing/grouping.
        return signatureBuilder.ToString();
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// ["rent","paid","rent","late"]   -> [0,1,0,2]
// ["coffee","today","coffee","again"] -> [0,1,0,2]
// ["taxi","home","taxi","home"]   -> [0,1,0,1]
// ["x","y","x","z"]               -> [0,1,0,2]
// Expected output: [0,1,3]
IList<IList<string>> memos1 = new List<IList<string>>
{
    new List<string> { "rent", "paid", "rent", "late" },
    new List<string> { "coffee", "today", "coffee", "again" },
    new List<string> { "taxi", "home", "taxi", "home" },
    new List<string> { "x", "y", "x", "z" }
};

var result1 = solution.FindMemosInRepeatedPatternGroups(memos1);
Console.WriteLine("Example 1 Output: [" + string.Join(",", result1) + "]");

// Example 2:
// ["a","b"]     -> [0,1]
// ["c","c"]     -> [0,0]
// ["dog","cat"] -> [0,1]
// ["hi"]        -> [0]
// ["m","n","m"] -> [0,1,0]
// Expected output: [0,2]
IList<IList<string>> memos2 = new List<IList<string>>
{
    new List<string> { "a", "b" },
    new List<string> { "c", "c" },
    new List<string> { "dog", "cat" },
    new List<string> { "hi" },
    new List<string> { "m", "n", "m" }
};

var result2 = solution.FindMemosInRepeatedPatternGroups(memos2);
Console.WriteLine("Example 2 Output: [" + string.Join(",", result2) + "]");