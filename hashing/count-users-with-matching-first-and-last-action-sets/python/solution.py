from typing import Dict, List, Set, Tuple


"""
Title: Count Users With Matching First and Last Action Sets

Problem Description:
You are given an array logs where each element is a record [userId, action].
The records are listed in chronological order. For each user, consider the set
of distinct actions performed in that user's first contiguous session and the
set of distinct actions performed in that user's last contiguous session.

A contiguous session for a user is a maximal consecutive block of records in the
global log belonging to that same user. In other words, if the same user appears
in several adjacent records, those records form one session, and the session ends
when a different user appears.

Your task is to count how many users have exactly the same set of distinct actions
in their first session and in their last session. If a user appears in only one
session, that user should also be counted, because the first and last session are
the same session.

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
"""


class Solution:
    def count_users_with_matching_first_and_last_action_sets(
        self, logs: List[List[str]]
    ) -> int:
        """
        Count users whose first session action set equals their last session action set.

        The algorithm scans the log once, builds each maximal contiguous session,
        and stores only the first session set and the most recent (current last)
        session set for every user.

        Args:
            logs: Chronological list of [userId, action] records.

        Returns:
            Number of users for whom the distinct-action set in the first session
            is exactly the same as the distinct-action set in the last session.

        Time complexity:
            O(n), where n is the number of log records.
            Each record is processed once, and each session action is inserted
            into a set once for that session.

        Space complexity:
            O(u + s), where u is the number of distinct users and s is the total
            number of distinct actions stored across first/last session sets.
        """
        # This dictionary stores, for each user:
        #   (first_session_action_set, last_session_action_set)
        #
        # Why store both?
        # - We need the first session set for final comparison.
        # - We also need the latest session set seen so far, because after the full scan
        #   that latest session is exactly the user's last session.
        #
        # Example structure:
        # {
        #   "u1": ({"open", "click"}, {"click", "open"}),
        #   "u2": ({"open"}, {"open"})
        # }
        user_sessions: Dict[str, Tuple[Set[str], Set[str]]] = {}

        # We process the log by sessions, not by individual records only.
        # A session is a maximal consecutive block of the same user.
        #
        # We use index-based scanning so we can consume one whole session at a time.
        n: int = len(logs)
        i: int = 0

        while i < n:
            # The current record starts a new session.
            current_user: str = logs[i][0]

            # We collect all distinct actions for this session in a set.
            # A set is the correct data structure because:
            # - order does not matter
            # - duplicates should count only once
            session_actions: Set[str] = set()

            # Consume the entire contiguous block for current_user.
            # This loop ends exactly when:
            # - we reach the end of logs, or
            # - the next record belongs to a different user
            while i < n and logs[i][0] == current_user:
                session_actions.add(logs[i][1])
                i += 1

            # Now we have completed one full session for current_user.
            #
            # There are two cases:
            # 1) This is the first session ever seen for this user.
            #    Then both first and last session are this same session for now.
            # 2) We have seen this user before.
            #    Then the first session stays unchanged, and the last session
            #    becomes this newly completed session.
            if current_user not in user_sessions:
                # Important detail:
                # We store copies/independent sets conceptually by assigning the
                # current session set as both first and last. Since we never mutate
                # this set again after this point, this is safe.
                user_sessions[current_user] = (session_actions, session_actions)
            else:
                first_session_actions, _ = user_sessions[current_user]
                user_sessions[current_user] = (
                    first_session_actions,
                    session_actions,
                )

        # After processing all sessions, compare first and last session sets
        # for every user and count matches.
        matching_users: int = 0

        for first_session_actions, last_session_actions in user_sessions.values():
            if first_session_actions == last_session_actions:
                matching_users += 1

        return matching_users

    def countMatchingUsers(self, logs: List[List[str]]) -> int:
        """
        Wrapper method using a shorter interview-style name.

        Args:
            logs: Chronological list of [userId, action] records.

        Returns:
            Number of users whose first and last session action sets match.

        Time complexity:
            O(n), where n is the number of log records.

        Space complexity:
            O(u + s), where u is the number of distinct users and s is the
            total stored distinct actions across tracked session sets.
        """
        return self.count_users_with_matching_first_and_last_action_sets(logs)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt
    logs1: List[List[str]] = [
        ["u1", "open"],
        ["u1", "click"],
        ["u2", "open"],
        ["u1", "click"],
        ["u1", "open"],
        ["u2", "open"],
    ]
    result1: int = solution.countMatchingUsers(logs1)
    print("Example 1 Output:", result1)  # Expected: 2

    # Example 2 from the prompt
    # The written explanation says the total is 2, even though the displayed
    # output line says 1. The explanation is internally consistent:
    # - a does not count
    # - b counts
    # - c counts
    # Therefore the correct result is 2.
    logs2: List[List[str]] = [
        ["a", "x"],
        ["a", "x"],
        ["b", "y"],
        ["a", "z"],
        ["a", "z"],
        ["c", "m"],
        ["c", "n"],
    ]
    result2: int = solution.countMatchingUsers(logs2)
    print("Example 2 Output:", result2)  # Correct based on explanation: 2

    # Additional quick sanity check:
    # One user, one session, repeated actions -> should count as 1
    logs3: List[List[str]] = [
        ["user", "login"],
        ["user", "login"],
        ["user", "logout"],
    ]
    result3: int = solution.countMatchingUsers(logs3)
    print("Additional Test Output:", result3)  # Expected: 1