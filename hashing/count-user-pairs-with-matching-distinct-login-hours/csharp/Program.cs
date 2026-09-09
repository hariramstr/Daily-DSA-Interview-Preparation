/*
Title: Count User Pairs With Matching Distinct Login Hours

Problem Description:
You are given a list of login records from an application. Each record is a pair [userId, hour],
where userId is a string and hour is an integer from 0 to 23 representing the hour of day when
that user logged in. A user may appear many times, and the same user may log in multiple times
during the same hour.

Two users are considered hour-equivalent if the set of distinct hours in which they logged in is
exactly the same. For example, if one user logged in at hours [1, 3, 3, 8] and another logged in
at hours [8, 1, 3], they are hour-equivalent because both users have the distinct hour set
{1, 3, 8}.

Return the number of unordered pairs of different users that are hour-equivalent.

A very efficient approach is to build a compact signature for each user's distinct login hours,
then count how many users share the same signature.

Constraints:
- 1 <= records.length <= 200000
- records[i].length == 2
- 1 <= userId.length <= 20
- userId consists of lowercase English letters, digits, or underscores
- 0 <= hour <= 23
- There may be up to 100000 distinct users

Examples:
1)
Input:
[["alice",1],["alice",3],["alice",3],["bob",3],["bob",1],["cara",2],["cara",5]]
Output: 1

Explanation:
- alice distinct hours = {1,3}
- bob distinct hours   = {1,3}
- cara distinct hours  = {2,5}
Only (alice, bob) is a matching pair.

2)
Input:
[["u1",0],["u1",23],["u2",23],["u2",0],["u3",0],["u3",0],["u4",5]]
Output: 1

Explanation:
- u1 distinct hours = {0,23}
- u2 distinct hours = {0,23}
- u3 distinct hours = {0}
- u4 distinct hours = {5}
Only (u1, u2) is a matching pair.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n + u), where:
      n = number of records
      u = number of distinct users
    Explanation:
    - We scan every record once to build each user's hour signature.
    - Then we scan each user once to count how many users share the same signature.

    Space Complexity:
    - O(u), where u = number of distinct users
    Explanation:
    - We store one compact bitmask per user.
    - We also store counts of how many users share each bitmask.
    */
    public long CountHourEquivalentPairs(IList<IList<object>> records)
    {
        // This dictionary maps:
        //   userId -> bitmask of distinct login hours
        //
        // Why a bitmask?
        // There are only 24 possible hours: 0 through 23.
        // So we can represent a user's distinct hour set inside a single integer.
        //
        // Example:
        // If a user logged in during hours 1 and 3,
        // then bits 1 and 3 are turned on.
        //
        // This is compact, fast, and automatically ignores duplicates:
        // setting the same bit multiple times changes nothing.
        var userToMask = new Dictionary<string, int>();

        // Step 1:
        // Build the distinct-hour signature for every user.
        //
        // We process each login record one by one.
        // Each record is [userId, hour].
        foreach (var record in records)
        {
            // Extract the user id and hour from the current record.
            // The input format for this demo method uses IList<object>,
            // so we cast each field to its expected type.
            string userId = (string)record[0];
            int hour = Convert.ToInt32(record[1]);

            // Create a bit that represents this hour.
            //
            // Example:
            // hour = 3  =>  1 << 3  => binary 00001000
            int hourBit = 1 << hour;

            // If this user has not been seen before, initialize their mask to 0.
            if (!userToMask.ContainsKey(userId))
            {
                userToMask[userId] = 0;
            }

            // Turn on the bit for this hour in the user's mask.
            //
            // Why OR (|)?
            // Because OR keeps any previously seen hours and adds the new one.
            //
            // Example:
            // current mask for {1}      = 00000010
            // hourBit for hour 3        = 00001000
            // result after OR           = 00001010  => {1,3}
            //
            // If the same hour appears again, OR-ing the same bit again does nothing,
            // which is exactly what we want because we care about DISTINCT hours only.
            userToMask[userId] |= hourBit;
        }

        // This dictionary maps:
        //   hour-signature bitmask -> number of users with that exact signature
        //
        // Once we know how many users share the same signature,
        // we can compute how many unordered pairs they form.
        var maskCount = new Dictionary<int, long>();

        // Step 2:
        // Count how many users have each exact distinct-hour signature.
        foreach (var entry in userToMask)
        {
            int mask = entry.Value;

            if (!maskCount.ContainsKey(mask))
            {
                maskCount[mask] = 0;
            }

            maskCount[mask]++;
        }

        // Step 3:
        // For each signature group of size k, the number of unordered pairs is:
        //   k * (k - 1) / 2
        //
        // Why?
        // Because from k users, we choose any 2 different users.
        long totalPairs = 0;

        foreach (var entry in maskCount)
        {
            long count = entry.Value;
            totalPairs += count * (count - 1) / 2;
        }

        // The final answer is the sum of pairs across all signature groups.
        return totalPairs;
    }
}

// -------------------------
// Demo / sample test code
// -------------------------

var solution = new Solution();

// Example 1:
// alice -> {1,3}
// bob   -> {1,3}
// cara  -> {2,5}
// Matching unordered pairs: (alice, bob) => 1
var records1 = new List<IList<object>>
{
    new List<object> { "alice", 1 },
    new List<object> { "alice", 3 },
    new List<object> { "alice", 3 },
    new List<object> { "bob", 3 },
    new List<object> { "bob", 1 },
    new List<object> { "cara", 2 },
    new List<object> { "cara", 5 }
};

long result1 = solution.CountHourEquivalentPairs(records1);
Console.WriteLine(result1); // Expected: 1

// Example 2:
// u1 -> {0,23}
// u2 -> {0,23}
// u3 -> {0}
// u4 -> {5}
// Matching unordered pairs: (u1, u2) => 1
var records2 = new List<IList<object>>
{
    new List<object> { "u1", 0 },
    new List<object> { "u1", 23 },
    new List<object> { "u2", 23 },
    new List<object> { "u2", 0 },
    new List<object> { "u3", 0 },
    new List<object> { "u3", 0 },
    new List<object> { "u4", 5 }
};

long result2 = solution.CountHourEquivalentPairs(records2);
Console.WriteLine(result2); // Expected: 1

// Additional demo:
// a -> {2,4}
// b -> {2,4}
// c -> {2,4}
// d -> {1}
// Three users share the same signature, so number of unordered pairs is:
// C(3,2) = 3
var records3 = new List<IList<object>>
{
    new List<object> { "a", 2 },
    new List<object> { "a", 4 },
    new List<object> { "b", 4 },
    new List<object> { "b", 2 },
    new List<object> { "c", 2 },
    new List<object> { "c", 2 },
    new List<object> { "c", 4 },
    new List<object> { "d", 1 }
};

long result3 = solution.CountHourEquivalentPairs(records3);
Console.WriteLine(result3); // Expected: 3