/*
Title: Find the Earliest Duplicate Custom Alias
Difficulty: Medium
Topic: Hashing

Problem Description:
A messaging platform lets users define custom aliases for channels. Two aliases are considered equivalent if, after normalizing them, they become identical. Normalization follows these rules: convert all uppercase letters to lowercase, remove every hyphen '-' and underscore '_', and keep all other characters unchanged. Given a list of aliases in the order they were created, return the index of the first alias that is equivalent to any earlier alias after normalization. If no such alias exists, return -1.

Your task is to detect the earliest duplicate by creation time, not the earliest original alias it matches. In other words, scan the list from left to right and return the first position i such that the normalized form of aliases[i] has already appeared among aliases[0...i-1].

Implement a function that solves this efficiently for large inputs.

Constraints:
- 1 <= aliases.length <= 200000
- 1 <= aliases[i].length <= 100
- aliases[i] consists of English letters, digits, hyphens '-', underscores '_', and periods '.'
- The answer should be computed in O(total input size) expected time using hashing

Example 1:
Input: aliases = ["Team-Chat", "alerts", "team_chat", "team.chat"]
Output: 2
Explanation: "Team-Chat" normalizes to "teamchat". "team_chat" also normalizes to "teamchat", so index 2 is the first duplicate.

Example 2:
Input: aliases = ["build.v1", "build_v1", "BUILD-V2", "buildv2"]
Output: 3
Explanation: "build.v1" normalizes to "build.v1" because periods are kept. "build_v1" normalizes to "buildv1", so it is not a duplicate. "BUILD-V2" normalizes to "buildv2", and "buildv2" also normalizes to "buildv2", making index 3 the first duplicate.
*/

using System;
using System.Collections.Generic;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    - Let T be the total number of characters across all aliases.
    - Each alias is normalized exactly once.
    - Each character is processed once during normalization.
    - Each normalized alias is inserted/looked up in a HashSet in expected O(1) time.
    - Therefore, the total expected time complexity is O(T).

    Space Complexity:
    - In the worst case, all normalized aliases are distinct.
    - We store each normalized alias in a HashSet.
    - Therefore, the extra space is O(T) in the worst case, counting stored normalized strings.
    */
    public int FindEarliestDuplicateCustomAlias(string[] aliases)
    {
        // We use a HashSet<string> because we need very fast "have we seen this before?"
        // checks while scanning from left to right.
        //
        // Why HashSet?
        // - It stores unique values only.
        // - Contains(...) is expected O(1).
        // - Add(...) is expected O(1).
        //
        // This is exactly what we need for duplicate detection based on normalized aliases.
        var seenNormalizedAliases = new HashSet<string>();

        // We scan aliases in creation order from index 0 to the end.
        //
        // This left-to-right scan is important because the problem asks for:
        // "the first position i such that aliases[i] matches any earlier alias after normalization."
        //
        // So the moment we find a normalized alias that already exists in the set,
        // that current index is the correct answer and we can return immediately.
        for (int i = 0; i < aliases.Length; i++)
        {
            // Step 1: Normalize the current alias.
            //
            // This converts uppercase letters to lowercase,
            // removes '-' and '_',
            // and keeps all other characters unchanged.
            //
            // Example:
            // "Team-Chat" -> "teamchat"
            // "team_chat" -> "teamchat"
            // "team.chat" -> "team.chat"   (period stays)
            string normalized = NormalizeAlias(aliases[i]);

            // Step 2: Check whether this normalized form has already appeared earlier.
            //
            // If yes, then the current alias is the earliest duplicate by creation time,
            // because we are scanning from left to right and this is the first time
            // we encountered a repeated normalized value.
            if (seenNormalizedAliases.Contains(normalized))
            {
                return i;
            }

            // Step 3: Otherwise, record this normalized alias as seen.
            //
            // This ensures later aliases can detect a match against it.
            seenNormalizedAliases.Add(normalized);
        }

        // If we finish scanning the entire array without finding any repeated normalized alias,
        // then no duplicate exists under the normalization rules.
        return -1;
    }

    private string NormalizeAlias(string alias)
    {
        // We use StringBuilder because strings in C# are immutable.
        //
        // If we repeatedly concatenated characters into a string,
        // that would create many temporary strings and be less efficient.
        //
        // StringBuilder lets us build the normalized result efficiently.
        var builder = new StringBuilder(alias.Length);

        // Process each character one by one.
        for (int i = 0; i < alias.Length; i++)
        {
            char c = alias[i];

            // If the character is a hyphen or underscore, we remove it.
            //
            // Why?
            // The normalization rules explicitly say:
            // - remove every hyphen '-'
            // - remove every underscore '_'
            if (c == '-' || c == '_')
            {
                continue;
            }

            // If the character is an uppercase English letter, convert it to lowercase.
            //
            // Why do this manually?
            // - The problem only involves English letters.
            // - This is efficient and very explicit for learners.
            //
            // Example:
            // 'A' -> 'a'
            // 'Z' -> 'z'
            if (c >= 'A' && c <= 'Z')
            {
                c = (char)(c - 'A' + 'a');
            }

            // All other characters are kept unchanged.
            //
            // This includes:
            // - lowercase letters
            // - digits
            // - periods '.'
            //
            // Example:
            // '.' stays '.'
            // '7' stays '7'
            builder.Append(c);
        }

        // Convert the built character sequence into the final normalized string.
        return builder.ToString();
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] aliases1 = { "Team-Chat", "alerts", "team_chat", "team.chat" };
int result1 = solution.FindEarliestDuplicateCustomAlias(aliases1);
Console.WriteLine(result1); // Expected: 2

// Example 2
string[] aliases2 = { "build.v1", "build_v1", "BUILD-V2", "buildv2" };
int result2 = solution.FindEarliestDuplicateCustomAlias(aliases2);
Console.WriteLine(result2); // Expected: 3

// Additional demo: no duplicates
string[] aliases3 = { "alpha", "beta", "gamma.delta", "gamma_delta" };
int result3 = solution.FindEarliestDuplicateCustomAlias(aliases3);
Console.WriteLine(result3); // Expected: -1

// Additional demo: immediate duplicate after normalization
string[] aliases4 = { "A-B_C", "abc", "a.bc" };
int result4 = solution.FindEarliestDuplicateCustomAlias(aliases4);
Console.WriteLine(result4); // Expected: 1