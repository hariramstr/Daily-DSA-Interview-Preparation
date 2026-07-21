/*
Title: Count Users With Matching First and Last Action Sets
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given an array logs where each element is a record [userId, action]. The records are listed in chronological order. For each user, consider the set of distinct actions performed in that user's first contiguous session and the set of distinct actions performed in that user's last contiguous session.

A contiguous session for a user is a maximal consecutive block of records in the global log belonging to that same user. In other words, if the same user appears in several adjacent records, those records form one session, and the session ends when a different user appears.

Your task is to count how many users have exactly the same set of distinct actions in their first session and in their last session. If a user appears in only one session, that user should also be counted, because the first and last session are the same session.

Return the number of such users.

Important details:
- Action order inside a session does not matter.
- Repeated occurrences of the same action within a session count only once.
- User IDs and actions are case-sensitive strings.

Constraints:
- 1 <= logs.length <= 2 * 10^5
- logs[i].length == 2
- 1 <= userId.length, action.length <= 20
- logs[i][0] and logs[i][1] consist of English letters, digits, or underscores
- The number of distinct users is at most 2 * 10^5

Examples:
1)
Input:
[["u1","open"],["u1","click"],["u2","open"],["u1","click"],["u1","open"],["u2","open"]]
Output: 2

2)
Input:
[["a","x"],["a","x"],["b","y"],["a","z"],["a","z"],["c","m"],["c","n"]]

Careful note:
The statement text says "Output: 1", but its own explanation counts both b and c, which means the correct total is 2.
So the correct answer for Example 2 is 2.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    private sealed class UserSessionInfo
    {
        public HashSet<string>? FirstSessionActions;
        public HashSet<string>? LastSessionActions;
    }

    /*
    Time Complexity:
    O(n + totalDistinctActionsAcrossSessionsCompared))
    In practice, this is linear in the number of log records plus the cost of comparing
    each user's first and last action sets once. Since each action occurrence is processed
    once while building sessions, this is efficient for the given constraints.

    Space Complexity:
    O(u + s)
    where:
    - u = number of distinct users
    - s = total number of distinct actions stored for first/last sessions
    We only keep the first and last session action sets for each user, not every session.
    */
    public int CountUsersWithMatchingFirstAndLastActionSets(string[][] logs)
    {
        // This dictionary stores, for each user, the information we care about:
        // 1) the set of distinct actions in that user's first session
        // 2) the set of distinct actions in that user's most recently completed session
        //
        // Why a dictionary?
        // Because we need fast access by userId, and Dictionary gives average O(1) lookup/update.
        var userInfo = new Dictionary<string, UserSessionInfo>();

        // These variables track the "current session" while we scan the global log from left to right.
        //
        // A session is a maximal consecutive block of records for the same user.
        // So as long as the next record has the same userId, we are still inside the same session.
        // When the userId changes, the previous session ends and a new session begins.
        string? currentSessionUser = null;
        HashSet<string>? currentSessionActions = null;

        // We scan the logs in chronological order exactly once.
        for (int i = 0; i < logs.Length; i++)
        {
            string userId = logs[i][0];
            string action = logs[i][1];

            // Step 1:
            // If this is the very first record, we must start the first session.
            if (currentSessionUser == null)
            {
                currentSessionUser = userId;
                currentSessionActions = new HashSet<string>();
                currentSessionActions.Add(action);
                continue;
            }

            // Step 2:
            // If the current record belongs to the same user as the current session,
            // then we are still inside the same contiguous session.
            if (userId == currentSessionUser)
            {
                // We add the action to the set.
                // Because this is a HashSet, duplicates are automatically ignored.
                // That matches the problem requirement: repeated actions inside a session count only once.
                currentSessionActions!.Add(action);
            }
            else
            {
                // Step 3:
                // The user changed, which means the previous session has ended.
                // We must "commit" that finished session into our per-user storage.
                CommitSession(userInfo, currentSessionUser, currentSessionActions!);

                // Step 4:
                // Start a brand-new session for the new user.
                currentSessionUser = userId;
                currentSessionActions = new HashSet<string>();
                currentSessionActions.Add(action);
            }
        }

        // Step 5:
        // After the loop ends, there is still one active session that has not yet been committed.
        // We must not forget it, otherwise the last session in the entire log would be lost.
        if (currentSessionUser != null && currentSessionActions != null)
        {
            CommitSession(userInfo, currentSessionUser, currentSessionActions);
        }

        // Step 6:
        // Count how many users have equal first-session and last-session action sets.
        //
        // Important:
        // If a user appeared in only one session, then first and last refer to the same session.
        // In our storage, that naturally means the first set and the last set are equal,
        // because the same session was both the first and the latest committed session.
        int answer = 0;

        foreach (var entry in userInfo)
        {
            var info = entry.Value;

            // These should always exist for any user that appeared at least once.
            // We still write the code clearly and defensively.
            if (info.FirstSessionActions != null &&
                info.LastSessionActions != null &&
                info.FirstSessionActions.SetEquals(info.LastSessionActions))
            {
                answer++;
            }
        }

        return answer;
    }

    private static void CommitSession(
        Dictionary<string, UserSessionInfo> userInfo,
        string userId,
        HashSet<string> sessionActions)
    {
        // This method stores one completed session for a user.
        //
        // Why isolate this logic in a helper?
        // Because every time a session ends, we need to perform the same update:
        // - if it is the user's first session, store it as FirstSessionActions
        // - always update LastSessionActions to this session
        //
        // This keeps the main loop easier to read.

        if (!userInfo.TryGetValue(userId, out var info))
        {
            // If this is the first time we have ever seen this user,
            // create a new record for them.
            info = new UserSessionInfo();
            userInfo[userId] = info;
        }

        // If the user does not yet have a first session stored,
        // then this completed session is their first session.
        //
        // We store a COPY of the set, not the original reference.
        // Why copy?
        // Because the current session set object may later be reused or modified elsewhere.
        // Storing a copy guarantees the saved first-session data remains correct forever.
        if (info.FirstSessionActions == null)
        {
            info.FirstSessionActions = new HashSet<string>(sessionActions);
        }

        // Whether this is the first, second, or tenth session,
        // this completed session is now the most recent completed session.
        // So it becomes the user's "last session" seen so far.
        //
        // Again, we store a copy for safety and correctness.
        info.LastSessionActions = new HashSet<string>(sessionActions);
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[][] logs1 =
[
    ["u1", "open"],
    ["u1", "click"],
    ["u2", "open"],
    ["u1", "click"],
    ["u1", "open"],
    ["u2", "open"]
];

int result1 = solution.CountUsersWithMatchingFirstAndLastActionSets(logs1);
Console.WriteLine(result1); // Expected: 2

// Example 2
// The problem statement's listed output says 1, but its own explanation implies 2.
// Let's verify:
// a -> first {x}, last {z} => not equal
// b -> only one session {y} => equal
// c -> only one session {m,n} => equal
// Total = 2
string[][] logs2 =
[
    ["a", "x"],
    ["a", "x"],
    ["b", "y"],
    ["a", "z"],
    ["a", "z"],
    ["c", "m"],
    ["c", "n"]
];

int result2 = solution.CountUsersWithMatchingFirstAndLastActionSets(logs2);
Console.WriteLine(result2); // Correct expected: 2

// Additional small sanity check
string[][] logs3 =
[
    ["u", "a"],
    ["u", "a"],
    ["u", "b"]
];
// Only one session for u, actions {a,b}, so answer should be 1.
int result3 = solution.CountUsersWithMatchingFirstAndLastActionSets(logs3);
Console.WriteLine(result3); // Expected: 1