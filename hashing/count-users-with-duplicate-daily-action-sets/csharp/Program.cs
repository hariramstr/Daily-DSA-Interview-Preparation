/*
Title: Count Users With Duplicate Daily Action Sets

Problem Description:
A product analytics team stores user activity for a single day as a list of events.
Each event is represented by a pair [userId, action], where userId is a string and
action is a lowercase string such as "click", "view", or "share".

A user's daily action set is the set of distinct actions they performed that day.
Repeated occurrences of the same action by the same user should count only once.

Your task is to return the number of users whose daily action set is identical to at
least one other user's daily action set. Two users are considered matching if the set
of distinct actions they performed is exactly the same, regardless of the order of
events or how many times each action appears.

For example, if user A performed ["view", "click", "click"] and user B performed
["click", "view"], then they have the same daily action set: {"click", "view"}.
If user C performed only ["view"], then C does not match A or B.

Return the count of distinct users that belong to any matching group. A group may
contain more than two users, and every user in that group should be counted.

Constraints:
- 1 <= events.length <= 200000
- Each event has exactly 2 values: [userId, action]
- 1 <= userId.length, action.length <= 20
- userId and action contain only lowercase English letters and digits
- There are at most 100000 distinct users

Example 1:
Input:
events = [
  ["u1","view"],
  ["u2","click"],
  ["u1","click"],
  ["u2","view"],
  ["u3","view"],
  ["u3","view"]
]
Output: 2

Explanation:
u1 has set {view, click}
u2 has set {click, view}
u3 has set {view}
Only u1 and u2 share an identical daily action set, so the answer is 2.

Example 2:
Input:
events = [
  ["a","login"],
  ["a","upload"],
  ["b","upload"],
  ["b","login"],
  ["c","login"],
  ["d","pay"],
  ["d","refund"],
  ["e","refund"],
  ["e","pay"]
]
Output: 4

Explanation:
Users a and b share the set {login, upload}
Users d and e share the set {pay, refund}
User c has {login} alone
Therefore, 4 users belong to matching groups.

Expected idea:
Use hashing to normalize each user's distinct-action set into a canonical representation,
then count how many users share the same representation.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    Let E = number of events
    Let U = number of distinct users
    Let A = total number of distinct (user, action) pairs across all users

    1. Building per-user HashSet collections takes O(E) average time.
       - Each event is processed once.
       - Duplicate actions for the same user are naturally ignored by HashSet.

    2. For each user, we sort that user's distinct actions to build a canonical key.
       If a user has k distinct actions, sorting costs O(k log k).
       Across all users, total cost is:
       O(sum over users of k log k)

    So the full complexity is:
    O(E + sum(k log k))

    In practice, this is efficient for the given constraints.

    Space Complexity:
    O(A + U)
    - We store each user's distinct actions.
    - We also store canonical representations and their frequencies.
    */
    public int CountUsersWithDuplicateDailyActionSets(IList<IList<string>> events)
    {
        // Step 1:
        // Build a mapping from userId -> set of distinct actions performed by that user.
        //
        // Why this is necessary:
        // The problem explicitly says repeated occurrences of the same action by the same user
        // should count only once. That means we do NOT care about frequencies; we only care
        // about the set of unique actions.
        //
        // Data structure choice:
        // - Dictionary<string, HashSet<string>>
        //   Key   = userId
        //   Value = the set of unique actions for that user
        //
        // Why HashSet?
        // - Fast average O(1) insertion
        // - Automatically removes duplicates
        var userToActions = new Dictionary<string, HashSet<string>>();

        // Process every event exactly once.
        foreach (var ev in events)
        {
            // Each event has exactly two values:
            // ev[0] = userId
            // ev[1] = action
            string userId = ev[0];
            string action = ev[1];

            // If this is the first time we see this user, create an empty action set.
            if (!userToActions.ContainsKey(userId))
            {
                userToActions[userId] = new HashSet<string>();
            }

            // Add the action to the user's set.
            // If the action was already present, HashSet ignores the duplicate.
            userToActions[userId].Add(action);
        }

        // Step 2:
        // Convert each user's action set into a canonical representation.
        //
        // Why this is necessary:
        // Sets do not have order, but strings do.
        // For example:
        //   {"view", "click"} and {"click", "view"}
        // are the same set, but if we simply iterate over them, the order may differ.
        //
        // To compare sets reliably, we:
        // 1. Take all distinct actions for a user
        // 2. Sort them
        // 3. Join them into one string key
        //
        // Example:
        //   {"view", "click"} -> sort -> ["click", "view"] -> "click|view"
        //
        // Then any user with the same distinct action set will produce the same key.
        //
        // Data structure choice:
        // - Dictionary<string, int>
        //   Key   = canonical representation of a user's action set
        //   Value = how many users have that exact set
        var signatureCount = new Dictionary<string, int>();

        foreach (var entry in userToActions)
        {
            HashSet<string> actionSet = entry.Value;

            // Convert the set to a list so we can sort it.
            List<string> sortedActions = actionSet.ToList();

            // Sorting is the key normalization step.
            // Without sorting, equal sets could produce different string orders.
            sortedActions.Sort(StringComparer.Ordinal);

            // Join with a separator that cannot create ambiguity between adjacent strings.
            // Since actions contain only lowercase letters and digits, "|" is safe here.
            string signature = string.Join("|", sortedActions);

            // Count how many users share this exact normalized action set.
            if (!signatureCount.ContainsKey(signature))
            {
                signatureCount[signature] = 0;
            }

            signatureCount[signature]++;
        }

        // Step 3:
        // Count how many users belong to groups whose signature appears at least twice.
        //
        // Why this works:
        // If a signature count is:
        // - 1 -> that user's action set is unique, so they should NOT be counted
        // - 2 or more -> all users with that signature belong to a matching group,
        //                so all of them should be counted
        int result = 0;

        foreach (var entry in signatureCount)
        {
            int usersWithThisSignature = entry.Value;

            if (usersWithThisSignature >= 2)
            {
                result += usersWithThisSignature;
            }
        }

        return result;
    }
}

// Demo code

var solution = new Solution();

// Example 1
var events1 = new List<IList<string>>
{
    new List<string> { "u1", "view" },
    new List<string> { "u2", "click" },
    new List<string> { "u1", "click" },
    new List<string> { "u2", "view" },
    new List<string> { "u3", "view" },
    new List<string> { "u3", "view" }
};

int result1 = solution.CountUsersWithDuplicateDailyActionSets(events1);
Console.WriteLine(result1); // Expected: 2

// Example 2
var events2 = new List<IList<string>>
{
    new List<string> { "a", "login" },
    new List<string> { "a", "upload" },
    new List<string> { "b", "upload" },
    new List<string> { "b", "login" },
    new List<string> { "c", "login" },
    new List<string> { "d", "pay" },
    new List<string> { "d", "refund" },
    new List<string> { "e", "refund" },
    new List<string> { "e", "pay" }
};

int result2 = solution.CountUsersWithDuplicateDailyActionSets(events2);
Console.WriteLine(result2); // Expected: 4

// Additional quick sanity check:
// x -> {a}
// y -> {a}
// z -> {b}
// answer should be 2
var events3 = new List<IList<string>>
{
    new List<string> { "x", "a" },
    new List<string> { "y", "a" },
    new List<string> { "z", "b" }
};

int result3 = solution.CountUsersWithDuplicateDailyActionSets(events3);
Console.WriteLine(result3); // Expected: 2