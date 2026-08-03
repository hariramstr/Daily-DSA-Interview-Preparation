/*
Title: Count Repeated Tag Signatures Across Articles
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given a list of articles, where each article is represented by a list of string tags.
Two articles are said to have the same tag signature if they contain exactly the same set of
distinct tags, regardless of order and regardless of duplicate occurrences inside the same article.

For example:
["ai", "cloud", "ai"] and ["cloud", "ai"]
have the same signature because both reduce to the set {"ai", "cloud"}.

Your task is to return the number of unordered pairs of articles that share the same tag signature.

In other words:
1. Normalize each article by removing duplicate tags within that article
2. Ignore order
3. Count how many pairs of articles become identical after normalization

This problem is intended to test careful use of hashing for canonical representations.
A brute-force comparison of every pair of articles would be too slow for large inputs.
Instead, we build a canonical signature for each article and use a hash map to count
how many times each signature appears.

Constraints:
- 1 <= articles.length <= 100000
- 1 <= articles[i].length <= 20
- 1 <= tags[i][j].length <= 20
- Each tag consists of lowercase English letters only
- The answer may be large, so use 64-bit integer arithmetic

Example 1:
Input: articles = [["ai","cloud","ai"],["cloud","ai"],["ml"],["ml","ml"],["cloud"]]
Output: 2

Explanation:
- Article 1 reduces to {"ai","cloud"}
- Article 2 reduces to {"ai","cloud"}
- Article 3 reduces to {"ml"}
- Article 4 reduces to {"ml"}
- Article 5 reduces to {"cloud"}

Matching signatures:
- {"ai","cloud"} appears twice -> 1 pair
- {"ml"} appears twice -> 1 pair
Total = 2

Example 2:
Input: articles = [["news","sports"],["sports","news","sports"],["finance"],["news"],["finance","finance"],["sports","news"]]
Output: 4

Explanation:
- {"news","sports"} appears 3 times -> 3 choose 2 = 3 pairs
- {"finance"} appears 2 times -> 2 choose 2 = 1 pair
Total = 4
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    Let n be the number of articles.
    Let k be the maximum number of tags in one article (at most 20).
    Let s be the average cost of handling tag strings.

    For each article:
    - We remove duplicates using a HashSet: O(k)
    - We sort the distinct tags to ignore order: O(k log k)
    - We build a canonical string signature from the sorted tags

    Overall:
    O(n * (k log k + signature_build_cost))

    Since k <= 20, this is efficient even for large n.

    Space Complexity:
    - Hash map storing counts of signatures: O(number of distinct signatures)
    - Temporary HashSet/list per article: O(k)

    So overall auxiliary space is:
    O(number of distinct signatures + k)
    */
    public long CountRepeatedTagSignatures(IList<IList<string>> articles)
    {
        // This dictionary maps:
        //   canonical signature string -> how many articles have produced this signature so far
        //
        // Why do we need this?
        // Because when we process a new article, if we already saw the same signature X times,
        // then this new article forms exactly X new unordered pairs with those previous articles.
        //
        // Example:
        // If signature "{ai,cloud}" has already appeared 2 times,
        // and we see it again now, then the new article pairs with both previous ones:
        // new pairs added = 2
        var signatureCounts = new Dictionary<string, long>();

        // We store the answer in a 64-bit integer because the number of pairs can be large.
        // For example, if many articles share the same signature, the pair count can exceed int range.
        long totalPairs = 0;

        // Process each article one by one.
        foreach (var article in articles)
        {
            // STEP 1: Remove duplicate tags inside the current article.
            //
            // Why is this necessary?
            // The problem says the signature depends on the SET of distinct tags,
            // not on repeated occurrences.
            //
            // Example:
            // ["ai", "cloud", "ai"] should become {"ai", "cloud"}
            //
            // HashSet is the natural data structure for deduplication because:
            // - it stores unique elements only
            // - insertion/check is efficient on average
            var uniqueTags = new HashSet<string>(article);

            // STEP 2: Convert the set to a list so we can sort it.
            //
            // Why sort?
            // Because order should not matter.
            //
            // Example:
            // {"ai", "cloud"} and {"cloud", "ai"} must produce the same signature.
            // Sorting ensures both become the same ordered sequence:
            // ["ai", "cloud"]
            var sortedTags = uniqueTags.ToList();
            sortedTags.Sort(StringComparer.Ordinal);

            // STEP 3: Build a canonical representation (signature) from the sorted distinct tags.
            //
            // We need a stable, repeatable key for the dictionary.
            // A simple and reliable approach is to join the sorted tags with a separator.
            //
            // Important detail:
            // We choose a separator that cannot appear in tags.
            // Tags contain only lowercase English letters, so "|" is safe.
            //
            // Example:
            // ["ai", "cloud"] -> "ai|cloud"
            // ["cloud", "ai", "ai"] -> after dedupe + sort -> "ai|cloud"
            //
            // Therefore equal signatures map to exactly the same string key.
            string signature = string.Join("|", sortedTags);

            // STEP 4: Count pairs incrementally.
            //
            // Suppose this signature has already appeared 'countSoFar' times.
            // Then the current article forms one pair with each of those previous articles.
            //
            // So:
            //   new pairs contributed by this article = countSoFar
            //
            // After adding those pairs, we increment the stored count for this signature.
            if (signatureCounts.TryGetValue(signature, out long countSoFar))
            {
                // Add all new pairs formed with previously seen articles of the same signature.
                totalPairs += countSoFar;

                // Update the frequency because we have now seen one more article with this signature.
                signatureCounts[signature] = countSoFar + 1;
            }
            else
            {
                // First time seeing this signature.
                // It cannot form any pair yet because no previous article matches it.
                signatureCounts[signature] = 1;
            }
        }

        // After processing all articles, totalPairs contains the number of unordered matching pairs.
        return totalPairs;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// [["ai","cloud","ai"],["cloud","ai"],["ml"],["ml","ml"],["cloud"]]
// Expected output: 2
IList<IList<string>> articles1 = new List<IList<string>>
{
    new List<string> { "ai", "cloud", "ai" },
    new List<string> { "cloud", "ai" },
    new List<string> { "ml" },
    new List<string> { "ml", "ml" },
    new List<string> { "cloud" }
};

long result1 = solution.CountRepeatedTagSignatures(articles1);
Console.WriteLine(result1);

// Example 2:
// [["news","sports"],["sports","news","sports"],["finance"],["news"],["finance","finance"],["sports","news"]]
// Expected output: 4
IList<IList<string>> articles2 = new List<IList<string>>
{
    new List<string> { "news", "sports" },
    new List<string> { "sports", "news", "sports" },
    new List<string> { "finance" },
    new List<string> { "news" },
    new List<string> { "finance", "finance" },
    new List<string> { "sports", "news" }
};

long result2 = solution.CountRepeatedTagSignatures(articles2);
Console.WriteLine(result2);