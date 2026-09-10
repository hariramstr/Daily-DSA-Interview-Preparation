/*
Title: Find the Shortest Unique Username Abbreviation
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given an array of distinct lowercase usernames and an integer index p pointing to one target username.
A valid abbreviation of a username is formed by keeping a non-empty prefix of the username and replacing the
remaining suffix with *. For example, the username "marina" can produce "m*", "ma*", "mar*", "mari*",
and "marin*". The full username without * is also allowed and is considered an abbreviation of length equal
to the whole word.

Your task is to return the shortest valid abbreviation of usernames[p] that does not match the abbreviation
of any other username in the array. Two abbreviations match if their resulting strings are exactly equal.
If multiple shortest answers exist, return the lexicographically smallest one, although for this abbreviation
rule the answer is usually unique.

This problem models generating compact but unambiguous user labels in a system. An efficient solution should
avoid comparing the target against every possible prefix of every string repeatedly, and instead use hashing
or frequency counting over prefixes.

Constraints:
- 1 <= usernames.length <= 2 * 10^5
- 1 <= usernames[i].length <= 10^5
- Sum of all username lengths does not exceed 2 * 10^5
- All usernames contain only lowercase English letters
- All usernames are distinct
- 0 <= p < usernames.length

Example 1:
Input: usernames = ["marina","mark","mason","mila"], p = 0
Output: "mari*"
Explanation:
m* matches several usernames,
ma* matches marina, mark, and mason,
mar* matches marina and mark,
but mari* matches only marina.

Example 2:
Input: usernames = ["zoe","zora","zack","amy"], p = 3
Output: "a*"
Explanation:
No other username starts with 'a', so the shortest unique abbreviation is a*.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(totalLength)
    where totalLength is the sum of lengths of all usernames.

    Why?
    - We walk through every username once.
    - For each username, we generate all of its non-empty prefixes once.
    - The total number of generated prefixes across all words is exactly the total sum of lengths.

    Space Complexity:
    O(totalLength)

    Why?
    - We store a frequency count for every prefix that appears.
    - In the worst case, all prefixes are different, so the number of stored prefixes is proportional
      to the total number of characters across all usernames.
    */
    public string ShortestUniqueAbbreviation(string[] usernames, int p)
    {
        // This dictionary will count how many usernames share each non-empty prefix.
        //
        // Example:
        // If usernames are ["marina", "mark", "mason", "mila"], then:
        // "m"   -> 4
        // "ma"  -> 3
        // "mar" -> 2
        // "mari"-> 1
        //
        // Why do we need this?
        // Because an abbreviation like "mar*" is unique exactly when the prefix "mar"
        // belongs to only one username in the whole array.
        //
        // We use Dictionary<string, int> because:
        // - key   = prefix text
        // - value = number of usernames having that prefix
        var prefixCount = new Dictionary<string, int>();

        // STEP 1:
        // Build frequency counts for every non-empty prefix of every username.
        //
        // This is the core preprocessing step.
        // Instead of repeatedly comparing the target username against all other usernames,
        // we summarize the entire input into prefix frequencies.
        //
        // After this step, we can answer:
        // "Is prefix X unique?" in O(1) average dictionary lookup time.
        foreach (var username in usernames)
        {
            // We build prefixes incrementally:
            // "m", "ma", "mar", ...
            //
            // Using a char buffer avoids repeatedly slicing substrings from the original string
            // in a less controlled way. We still create strings for dictionary keys, because
            // dictionary keys must be actual strings here.
            var chars = new char[username.Length];

            for (int i = 0; i < username.Length; i++)
            {
                chars[i] = username[i];

                // Current prefix length is i + 1.
                string prefix = new string(chars, 0, i + 1);

                if (prefixCount.TryGetValue(prefix, out int count))
                {
                    prefixCount[prefix] = count + 1;
                }
                else
                {
                    prefixCount[prefix] = 1;
                }
            }
        }

        // STEP 2:
        // Examine prefixes of the target username from shortest to longest.
        //
        // Why shortest to longest?
        // Because the problem asks for the shortest valid abbreviation.
        //
        // If the first unique prefix has length k, then:
        // - prefix + "*" is the shortest unique starred abbreviation
        // - unless k == full word length, in which case the full word itself is allowed
        //   and is shorter than adding "*" to the full word.
        string target = usernames[p];
        var targetChars = new char[target.Length];

        for (int i = 0; i < target.Length; i++)
        {
            targetChars[i] = target[i];
            string prefix = new string(targetChars, 0, i + 1);

            // If this prefix appears in exactly one username, then it uniquely identifies target.
            //
            // This means:
            // - If prefix length < full length, abbreviation is prefix + "*"
            // - If prefix length == full length, the full username itself is allowed and should be returned
            //   because the problem explicitly allows the full word without '*'.
            if (prefixCount[prefix] == 1)
            {
                if (i + 1 == target.Length)
                {
                    return target;
                }

                return prefix + "*";
            }
        }

        // Because all usernames are distinct, the full username must always be unique.
        // So in valid input, the loop above must always return by the time it reaches the full word.
        //
        // This return is only a defensive fallback and should never be needed.
        return target;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// usernames = ["marina","mark","mason","mila"], p = 0
// Prefix counts relevant to "marina":
// "m"    -> 4
// "ma"   -> 3
// "mar"  -> 2
// "mari" -> 1
// Therefore answer is "mari*"
string[] usernames1 = { "marina", "mark", "mason", "mila" };
int p1 = 0;
string result1 = solution.ShortestUniqueAbbreviation(usernames1, p1);
Console.WriteLine(result1);

// Example 2:
// usernames = ["zoe","zora","zack","amy"], p = 3
// For "amy":
// "a" -> 1
// Therefore answer is "a*"
string[] usernames2 = { "zoe", "zora", "zack", "amy" };
int p2 = 3;
string result2 = solution.ShortestUniqueAbbreviation(usernames2, p2);
Console.WriteLine(result2);

// Additional quick sanity check:
// If the target itself must be fully written to be unique, the method returns the full word.
// Example: ["ab", "ac", "b"], target "ab"
// "a" is shared, but "ab" is unique, so answer is "ab"
string[] usernames3 = { "ab", "ac", "b" };
int p3 = 0;
string result3 = solution.ShortestUniqueAbbreviation(usernames3, p3);
Console.WriteLine(result3);