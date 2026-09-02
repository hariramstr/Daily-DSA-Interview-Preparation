/*
Title: Count User Pairs With the Same Relative Notification Delays

Problem Description:
A product team records the times, in minutes, when each user received notifications during a single day.
For each user, the absolute start time is not important; only the pattern of gaps between consecutive
notifications matters.

Two users are considered equivalent if, after sorting their notification times in increasing order,
they produce the same sequence of relative delays from the first notification.

If a user's sorted times are:
[t0, t1, t2, ...]

Then their delay signature is:
[0, t1 - t0, t2 - t0, ...]

Users with different numbers of notifications can never be equivalent.

You are given a list of users, where each user's data is an array of integers representing notification times.
Count how many unordered pairs of users have the same delay signature.

Important notes:
- If a user has only one notification, their signature is simply [0].
- Duplicate times for the same user are allowed and should be preserved after sorting.
- Return the total number of equivalent user pairs.

Constraints:
- 1 <= users.length <= 100000
- 1 <= users[i].length <= 100
- 0 <= users[i][j] <= 10^9
- The sum of all users[i].length does not exceed 2 * 10^5

Example 1:
Input: users = [[5,10,20],[100,105,115],[3,8,18],[7,7,9],[20,20,22]]
Output: 4

Explanation:
- [5,10,20] -> sorted [5,10,20] -> signature [0,5,15]
- [100,105,115] -> signature [0,5,15]
- [3,8,18] -> signature [0,5,15]
- [7,7,9] -> signature [0,0,2]
- [20,20,22] -> signature [0,0,2]
The first three users form 3 pairs, and the last two form 1 pair, for a total of 4.

Example 2:
Input: users = [[4],[9],[1,4,4],[10,13,13],[2,5,6]]
Output: 2

Explanation:
- [4] and [9] both have signature [0], contributing 1 pair.
- [1,4,4] -> signature [0,3,3]
- [10,13,13] -> signature [0,3,3], contributing 1 pair.
- [2,5,6] -> signature [0,3,4]
So the answer is 2.
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    Let n be the number of users, and let k be the average number of notifications per user.
    More precisely, if user i has length m_i, then:
    - Sorting each user's array costs O(m_i log m_i)
    - Building the signature costs O(m_i)
    Summed over all users, total time is:
    O(Σ(m_i log m_i))
    Since each m_i <= 100 and total Σ(m_i) <= 2 * 10^5, this is efficient.

    Space Complexity:
    - O(Σ(m_i)) in the worst case for storing signature strings in the hash map
    - O(m_i) temporary space when copying/sorting one user's timestamps
    */
    public long CountEquivalentUserPairs(int[][] users)
    {
        // This dictionary maps:
        //   signature string  ->  how many users seen so far have this exact signature
        //
        // Why use a dictionary?
        // Because we want to quickly group users by their canonical pattern.
        // If two users produce the same signature, they belong to the same group.
        //
        // Example:
        //   "0,5,15" -> 3
        // means we have already seen 3 users whose relative delays are [0,5,15].
        var signatureCount = new Dictionary<string, long>();

        // This will store the final number of unordered equivalent pairs.
        //
        // We use long instead of int because the number of pairs can be large.
        // In the worst case, if many users share the same signature,
        // the pair count can exceed the range of int.
        long pairs = 0;

        // Process each user independently.
        foreach (var userTimes in users)
        {
            // Step 1: Copy the current user's timestamps.
            //
            // Why copy?
            // Because sorting in place would modify the original input array.
            // In many interview and production settings, it is safer not to mutate input
            // unless the problem explicitly allows or encourages it.
            int[] sorted = new int[userTimes.Length];
            Array.Copy(userTimes, sorted, userTimes.Length);

            // Step 2: Sort the timestamps.
            //
            // Why is sorting necessary?
            // The problem defines equivalence based on the timestamps after sorting.
            // So [20,5,10] and [5,10,20] must produce the same signature.
            //
            // Duplicate values are preserved by sorting, which is exactly what we want.
            // For example:
            //   [7,7,9] sorted stays [7,7,9]
            // and its signature becomes [0,0,2].
            Array.Sort(sorted);

            // Step 3: Build a canonical signature for this user.
            //
            // The signature is:
            //   [0, sorted[1] - sorted[0], sorted[2] - sorted[0], ...]
            //
            // We convert that signature into a string such as:
            //   "0,5,15"
            //
            // Why a string?
            // Because it is a simple and reliable hash key for a dictionary.
            // Since the lengths and values must match exactly, a comma-separated string
            // uniquely represents the signature.
            //
            // Important:
            // Users with different numbers of notifications can never be equivalent.
            // This is naturally handled because their signature strings will have
            // different numbers of components.
            string signature = BuildSignature(sorted);

            // Step 4: Count pairs incrementally.
            //
            // Suppose this signature has already appeared 'f' times.
            // Then the current user forms exactly 'f' new pairs:
            // one pair with each previously seen user in that same group.
            //
            // Example:
            // If "0,5,15" has already been seen 3 times,
            // the 4th user with that signature creates 3 new pairs.
            if (signatureCount.TryGetValue(signature, out long seen))
            {
                pairs += seen;
                signatureCount[signature] = seen + 1;
            }
            else
            {
                signatureCount[signature] = 1;
            }
        }

        // After processing all users, 'pairs' contains the total number
        // of unordered equivalent user pairs.
        return pairs;
    }

    private string BuildSignature(int[] sorted)
    {
        // If there is only one notification, the signature is simply [0].
        //
        // Returning "0" is enough to represent that canonical form.
        if (sorted.Length == 1)
        {
            return "0";
        }

        // We use StringBuilder because we are constructing a string piece by piece.
        // This is more efficient than repeated string concatenation.
        var sb = new StringBuilder();

        // The first element of every signature is always 0,
        // because the first notification is compared to itself.
        int baseTime = sorted[0];
        sb.Append('0');

        // For every later timestamp, append:
        //   "," + (sorted[i] - baseTime)
        //
        // This converts absolute times into relative delays from the first notification.
        //
        // Example:
        // sorted = [100,105,115]
        // baseTime = 100
        // signature = "0,5,15"
        for (int i = 1; i < sorted.Length; i++)
        {
            sb.Append(',');
            sb.Append(sorted[i] - baseTime);
        }

        return sb.ToString();
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[][] users1 =
{
    new[] { 5, 10, 20 },
    new[] { 100, 105, 115 },
    new[] { 3, 8, 18 },
    new[] { 7, 7, 9 },
    new[] { 20, 20, 22 }
};

long result1 = solution.CountEquivalentUserPairs(users1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[][] users2 =
{
    new[] { 4 },
    new[] { 9 },
    new[] { 1, 4, 4 },
    new[] { 10, 13, 13 },
    new[] { 2, 5, 6 }
};

long result2 = solution.CountEquivalentUserPairs(users2);
Console.WriteLine(result2); // Expected: 2

// Additional quick sanity check
int[][] users3 =
{
    new[] { 20, 5, 10 },   // sorted -> [5,10,20] -> "0,5,15"
    new[] { 50, 55, 65 },  // "0,5,15"
    new[] { 1, 1, 3 },     // "0,0,2"
    new[] { 9 }            // "0"
};

long result3 = solution.CountEquivalentUserPairs(users3);
Console.WriteLine(result3); // Expected: 1