/*
Title: Count Mirror Username Pairs
Difficulty: Medium
Topic: Hashing

Problem Description:
A social platform stores a list of usernames in the order they were created. Two usernames form a mirror pair if one of them is exactly the reverse of the other, and the two usernames appear at different indices.

For example:
- "stressed" and "desserts" form a mirror pair
- A single username does not pair with itself unless the same string appears again at another index

Task:
Given an array usernames of length n, return the total number of distinct index pairs (i, j) with i < j such that usernames[j] is the reverse of usernames[i].

Important details:
- Pairs are counted by indices, not by unique values
- Duplicate usernames can create multiple valid pairs
- Palindromes such as "level" can pair with other equal copies of the same palindrome

Examples:
1) usernames = ["abc", "cba", "xy", "yx", "abc"]
   Output: 2
   Explanation:
   - (0,1): "abc" <-> "cba"
   - (2,3): "xy" <-> "yx"
   The final "abc" does not have a later "cba", so it adds nothing.

2) usernames = ["aa", "aa", "aa", "ab", "ba"]
   Output: 4
   Explanation:
   - "aa" is a palindrome, so among three copies we get C(3,2) = 3 pairs
   - "ab" and "ba" add 1 more pair
   Total = 4

Constraints:
- 1 <= n <= 200000
- 1 <= usernames[i].length <= 30
- usernames[i] consists only of lowercase English letters
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n * L), where:
      n = number of usernames
      L = maximum username length
    Why:
    - We scan the array once
    - For each username, we build its reversed string in O(L)
    - Dictionary operations are O(1) average time

    Space Complexity:
    - O(n * L) in the worst case
    Why:
    - The dictionary may store up to n distinct usernames
    */
    public long CountMirrorPairs(string[] usernames)
    {
        // This dictionary stores how many times each username has appeared so far
        // while scanning from left to right.
        //
        // Key   = a username string that we have already seen
        // Value = how many times it has appeared at earlier indices
        //
        // Why this helps:
        // Suppose we are currently at usernames[j].
        // We want to count all earlier indices i < j such that:
        // usernames[i] reversed == usernames[j]
        //
        // That is equivalent to asking:
        // "How many earlier usernames are equal to reverse(usernames[j])?"
        //
        // If we can quickly look up how many times reverse(usernames[j]) has already
        // appeared, then we can add that count directly to the answer.
        var seenCounts = new Dictionary<string, int>();

        // We use long because the number of valid pairs can be large.
        // Example:
        // If all 200,000 usernames are the same palindrome, the number of pairs is:
        // 200000 * 199999 / 2 = 19,999,900,000
        // which does not fit in int.
        long totalPairs = 0;

        // Process usernames in creation order.
        // This guarantees that when we are at index j, the dictionary only contains
        // usernames from indices 0..j-1, so every counted pair automatically satisfies i < j.
        foreach (var username in usernames)
        {
            // Step 1: Compute the reversed version of the current username.
            //
            // Why:
            // A mirror pair exists when one string is exactly the reverse of the other.
            // So for the current username, we need to know what earlier string would match it.
            //
            // Example:
            // current = "cba"
            // reverse(current) = "abc"
            // Then every earlier "abc" forms a valid pair with this "cba".
            string reversed = ReverseString(username);

            // Step 2: Count how many earlier usernames equal this reversed string.
            //
            // If the dictionary contains reversed, then each previous occurrence creates
            // one valid pair with the current index.
            //
            // Example:
            // usernames = ["abc", "cba", "abc", "cba"]
            // At the last "cba", reversed = "abc"
            // If "abc" has appeared 2 times before, then this "cba" forms 2 new pairs.
            if (seenCounts.TryGetValue(reversed, out int matchingEarlierCount))
            {
                totalPairs += matchingEarlierCount;
            }

            // Step 3: Record the current username as seen.
            //
            // Why after counting, not before?
            // Because pairs must use different indices with i < j.
            // If we inserted first and then counted, a palindrome like "aa" could incorrectly
            // pair with itself at the same index, which is not allowed.
            //
            // By counting first and inserting second, we ensure only earlier indices contribute.
            if (seenCounts.ContainsKey(username))
            {
                seenCounts[username]++;
            }
            else
            {
                seenCounts[username] = 1;
            }
        }

        // After scanning all usernames, totalPairs contains the number of valid index pairs.
        return totalPairs;
    }

    private string ReverseString(string s)
    {
        // Convert the string to a character array so we can reverse it efficiently.
        char[] chars = s.ToCharArray();

        // Use two pointers:
        // - left starts at the beginning
        // - right starts at the end
        //
        // We swap characters until the pointers meet.
        int left = 0;
        int right = chars.Length - 1;

        while (left < right)
        {
            (chars[left], chars[right]) = (chars[right], chars[left]);
            left++;
            right--;
        }

        return new string(chars);
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the problem statement
string[] usernames1 = ["abc", "cba", "xy", "yx", "abc"];
long result1 = solution.CountMirrorPairs(usernames1);
Console.WriteLine(result1); // Expected: 2

// Example 2 from the problem statement
string[] usernames2 = ["aa", "aa", "aa", "ab", "ba"];
long result2 = solution.CountMirrorPairs(usernames2);
Console.WriteLine(result2); // Expected: 4

// Additional demo: duplicates creating multiple cross-pairs
// Two "abc" and three "cba" => 2 * 3 = 6
string[] usernames3 = ["abc", "cba", "abc", "cba", "cba"];
long result3 = solution.CountMirrorPairs(usernames3);
Console.WriteLine(result3); // Expected: 6

// Additional demo: palindromes
// Four copies of "level" => C(4,2) = 6
string[] usernames4 = ["level", "level", "level", "level"];
long result4 = solution.CountMirrorPairs(usernames4);
Console.WriteLine(result4); // Expected: 6