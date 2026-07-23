/*
Title: First Repeated Hashtag in a Campaign Feed

Problem Description:
A marketing platform stores the hashtags used in a campaign feed as an array of strings,
in the exact order they were posted. Your task is to find the first hashtag that appears
more than once while scanning the feed from left to right.

Return the first repeated hashtag based on the moment its second occurrence is seen.
If no hashtag is repeated, return an empty string.

For example, if the feed is ["#launch", "#sale", "#launch", "#summer"], the answer is
"#launch" because it is the first hashtag whose second appearance occurs during the scan.
If multiple hashtags appear multiple times, you should not return the one with the smallest
total count or lexicographically smallest value; return the one that becomes repeated earliest.

This problem is intended to be solved efficiently using a hash-based data structure to track
which hashtags have already been seen.

Constraints:
- 1 <= hashtags.length <= 100000
- 1 <= hashtags[i].length <= 50
- hashtags[i] consists of letters, digits, underscores, and the '#' character
- Comparison is case-sensitive

Example 1:
Input: hashtags = ["#launch", "#sale", "#launch", "#summer"]
Output: "#launch"
Explanation: "#launch" is seen at index 0 and repeats at index 2, which is the earliest second occurrence.

Example 2:
Input: hashtags = ["#red", "#blue", "#green", "#blue", "#red"]
Output: "#blue"
Explanation: Although both "#blue" and "#red" repeat, "#blue" becomes repeated first when scanning from left to right.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the array exactly once.
    - Each HashSet operation (Contains/Add) is O(1) on average.

    Space Complexity: O(n)
    - In the worst case, if no hashtag repeats early, we may store all hashtags in the HashSet.
    */
    public string FirstRepeatedHashtag(string[] hashtags)
    {
        // We use a HashSet<string> because it is a hash-based data structure that lets us:
        // 1. Quickly check whether a hashtag has already been seen before.
        // 2. Quickly add a new hashtag after we process it.
        //
        // Why HashSet is a good choice:
        // - We do NOT need to count how many times each hashtag appears.
        // - We only need to know whether we have seen a hashtag before.
        // - HashSet is perfect for "seen before?" questions.
        var seen = new HashSet<string>();

        // We now scan the feed from left to right exactly in posting order.
        // This is extremely important because the problem asks for the first hashtag
        // whose SECOND occurrence is encountered earliest during the scan.
        //
        // That means:
        // - The moment we find a hashtag that is already in 'seen',
        //   we should immediately return it.
        // - We do not continue searching, because any later repeat would happen later
        //   in the scan and therefore cannot be the correct answer.
        foreach (var hashtag in hashtags)
        {
            // Step 1: Check whether the current hashtag has already been seen.
            //
            // What this does:
            // - Looks inside the HashSet to see if this exact string is already present.
            //
            // Why this is necessary:
            // - If it is already present, then this current position is the SECOND
            //   (or later) occurrence of that hashtag.
            // - Since we are scanning from left to right, this is the earliest moment
            //   we have found any repeated hashtag so far.
            if (seen.Contains(hashtag))
            {
                // We return immediately because this is the first hashtag that becomes repeated
                // during the left-to-right scan.
                return hashtag;
            }

            // Step 2: If the hashtag was not seen before, record it now.
            //
            // What this does:
            // - Stores the current hashtag in the HashSet.
            //
            // Why this is necessary:
            // - So that if the same hashtag appears again later, we can detect that repeat.
            seen.Add(hashtag);
        }

        // If we finish the entire scan without finding any repeated hashtag,
        // then no hashtag appeared more than once.
        //
        // The problem asks us to return an empty string in that case.
        return string.Empty;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// Feed: ["#launch", "#sale", "#launch", "#summer"]
// Scan trace:
// - "#launch" -> not seen, add it
// - "#sale"   -> not seen, add it
// - "#launch" -> already seen, return "#launch"
string[] hashtags1 = ["#launch", "#sale", "#launch", "#summer"];
string result1 = solution.FirstRepeatedHashtag(hashtags1);
Console.WriteLine(result1); // Expected: #launch

// Example 2:
// Feed: ["#red", "#blue", "#green", "#blue", "#red"]
// Scan trace:
// - "#red"   -> not seen, add it
// - "#blue"  -> not seen, add it
// - "#green" -> not seen, add it
// - "#blue"  -> already seen, return "#blue"
// Even though "#red" also repeats later, "#blue" becomes repeated first.
string[] hashtags2 = ["#red", "#blue", "#green", "#blue", "#red"];
string result2 = solution.FirstRepeatedHashtag(hashtags2);
Console.WriteLine(result2); // Expected: #blue

// Additional demo: no repeated hashtag
string[] hashtags3 = ["#one", "#two", "#three"];
string result3 = solution.FirstRepeatedHashtag(hashtags3);
Console.WriteLine(result3); // Expected: empty line

// Additional demo: immediate repeat
string[] hashtags4 = ["#hot", "#hot", "#new"];
string result4 = solution.FirstRepeatedHashtag(hashtags4);
Console.WriteLine(result4); // Expected: #hot