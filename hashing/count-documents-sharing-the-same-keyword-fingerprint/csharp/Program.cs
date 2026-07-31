/*
Title: Count Documents Sharing the Same Keyword Fingerprint

Problem Description:
You are given a collection of documents. Each document is represented by a list of keywords,
where keywords may repeat within the same document because a term can appear multiple times.

Define the fingerprint of a document as the multiset of keyword frequencies, ignoring the actual
keyword names.

Example:
["red", "red", "blue", "green", "green"]
has keyword counts:
red   -> 2
blue  -> 1
green -> 2
So its fingerprint is the sorted list of counts: [1, 2, 2]

Another document:
["cat", "cat", "dog", "fox", "fox"]
has counts:
cat -> 2
dog -> 1
fox -> 2
So its fingerprint is also [1, 2, 2]

These two documents match because the sorted frequency lists are identical.

Task:
Count how many unordered pairs of documents share the same fingerprint.

Important note:
- Keyword order inside a document does not matter.
- Actual keyword names do not matter after frequencies are computed.
- Only the sorted list of frequency counts matters.

Constraints:
- 1 <= documents.length <= 10^5
- 1 <= total number of keywords across all documents <= 3 * 10^5
- 1 <= keyword.length <= 20
- Keywords contain only lowercase English letters
- Each document contains at least 1 keyword
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    Let T be the total number of keywords across all documents.
    Let D be the number of documents.

    For each document:
    1. We count frequencies of keywords in that document.
    2. We collect the frequency values.
    3. We sort those frequency values.

    Across all documents, counting frequencies costs O(T).
    Sorting the frequency lists costs sum over documents of O(k_i log k_i),
    where k_i is the number of distinct keywords in document i.

    Since the total number of keywords is bounded and each k_i <= document length,
    this is efficient for the given constraints.

    Overall:
    O(T + sum(k_i log k_i))

    Space Complexity:
    O(T) in the worst case across temporary dictionaries and stored fingerprint keys.
    */
    public long CountMatchingPairs(IList<IList<string>> documents)
    {
        // This dictionary maps:
        // fingerprint string -> how many documents have this fingerprint
        //
        // Why do we need this?
        // Because once we convert each document into a canonical fingerprint,
        // we want to group identical fingerprints together.
        //
        // Example:
        // "[1,2,2]" -> 3
        // means 3 documents share that fingerprint.
        //
        // Then the number of unordered pairs contributed by that group is:
        // 3 choose 2 = 3
        var fingerprintCounts = new Dictionary<string, long>();

        // We process each document independently.
        foreach (var document in documents)
        {
            // Step 1: Count how many times each keyword appears in the current document.
            //
            // Example document:
            // ["red", "red", "blue", "green", "green"]
            //
            // After this loop:
            // red   -> 2
            // blue  -> 1
            // green -> 2
            //
            // We use Dictionary<string, int> because:
            // - key   = the keyword text
            // - value = how many times it appears
            var keywordFrequency = new Dictionary<string, int>();

            foreach (var keyword in document)
            {
                if (keywordFrequency.TryGetValue(keyword, out int currentCount))
                {
                    keywordFrequency[keyword] = currentCount + 1;
                }
                else
                {
                    keywordFrequency[keyword] = 1;
                }
            }

            // Step 2: Extract only the frequency values.
            //
            // This is the key idea of the problem:
            // we no longer care about the actual keyword names.
            //
            // For example:
            // red -> 2, blue -> 1, green -> 2
            // becomes:
            // [2, 1, 2]
            //
            // Another document:
            // cat -> 2, dog -> 1, fox -> 2
            // also becomes:
            // [2, 1, 2]
            //
            // Since keyword names are ignored, these documents should match.
            var counts = new List<int>(keywordFrequency.Values);

            // Step 3: Sort the frequency list.
            //
            // Why is sorting necessary?
            // Because the order in which dictionary values appear is not meaningful.
            //
            // [2,1,2] and [1,2,2] represent the same multiset of frequencies.
            // Sorting gives us a canonical form:
            // [1,2,2]
            //
            // Once every document is converted to the same canonical ordering,
            // equal fingerprints become easy to compare.
            counts.Sort();

            // Step 4: Build a hashable representation of the sorted counts.
            //
            // We need something we can use as a dictionary key.
            // A string like "1#2#2" works well:
            // - easy to build
            // - easy to compare
            // - unambiguous because we include separators
            //
            // We do NOT use keyword names here, only the sorted frequencies.
            string fingerprint = string.Join("#", counts);

            // Step 5: Record that we have seen one more document with this fingerprint.
            if (fingerprintCounts.TryGetValue(fingerprint, out long seen))
            {
                fingerprintCounts[fingerprint] = seen + 1;
            }
            else
            {
                fingerprintCounts[fingerprint] = 1;
            }
        }

        // Step 6: Count unordered pairs inside each fingerprint group.
        //
        // If a fingerprint appears c times, then the number of unordered pairs is:
        // c * (c - 1) / 2
        //
        // Example:
        // c = 3 documents with fingerprint [1,2,2]
        // pairs = 3 * 2 / 2 = 3
        //
        // Those pairs are:
        // (doc0, doc1), (doc0, doc3), (doc1, doc3)
        long totalPairs = 0;

        foreach (var entry in fingerprintCounts)
        {
            long count = entry.Value;
            totalPairs += count * (count - 1) / 2;
        }

        return totalPairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the statement
var documents1 = new List<IList<string>>
{
    new List<string> { "red", "red", "blue", "green", "green" }, // fingerprint [1,2,2]
    new List<string> { "cat", "cat", "dog", "fox", "fox" },      // fingerprint [1,2,2]
    new List<string> { "a", "b", "b", "c" },                     // fingerprint [1,1,2]
    new List<string> { "m", "m", "n", "n", "p" },                // fingerprint [1,2,2]
    new List<string> { "z" }                                     // fingerprint [1]
};

var result1 = solution.CountMatchingPairs(documents1);
Console.WriteLine(result1); // Expected: 3

// Example 2 as formally defined by the problem statement
//
// Let's compute carefully:
// ["aa","bb","aa","cc","cc","cc"] -> counts [2,1,3] -> fingerprint [1,2,3]
// ["x","y","y","z","z","z"]       -> counts [1,2,3] -> fingerprint [1,2,3]
// ["p","p","q","q","r","r"]       -> counts [2,2,2] -> fingerprint [2,2,2]
// ["k"]                           -> counts [1]     -> fingerprint [1]
// ["u","v","w"]                   -> counts [1,1,1] -> fingerprint [1,1,1]
//
// Only the first two documents match each other.
// So the mathematically correct answer under the formal definition is 1.
var documents2 = new List<IList<string>>
{
    new List<string> { "aa", "bb", "aa", "cc", "cc", "cc" },
    new List<string> { "x", "y", "y", "z", "z", "z" },
    new List<string> { "p", "p", "q", "q", "r", "r" },
    new List<string> { "k" },
    new List<string> { "u", "v", "w" }
};

var result2 = solution.CountMatchingPairs(documents2);
Console.WriteLine(result2); // Correct by formal definition: 1

// Additional custom demo:
// Add another document with fingerprint [2,2,2], then that class contributes one pair too.
var documents3 = new List<IList<string>>
{
    new List<string> { "aa", "bb", "aa", "cc", "cc", "cc" }, // [1,2,3]
    new List<string> { "x", "y", "y", "z", "z", "z" },       // [1,2,3]
    new List<string> { "p", "p", "q", "q", "r", "r" },       // [2,2,2]
    new List<string> { "i", "i", "j", "j", "k", "k" }        // [2,2,2]
};

var result3 = solution.CountMatchingPairs(documents3);
Console.WriteLine(result3); // Expected: 2