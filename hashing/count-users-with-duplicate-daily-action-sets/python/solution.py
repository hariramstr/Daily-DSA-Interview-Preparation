"""
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
    ["u1", "view"],
    ["u2", "click"],
    ["u1", "click"],
    ["u2", "view"],
    ["u3", "view"],
    ["u3", "view"]
]
Output: 2

Explanation:
- u1 has set {"view", "click"}
- u2 has set {"click", "view"}
- u3 has set {"view"}

Only u1 and u2 share an identical daily action set, so the answer is 2.

Example 2:
Input:
events = [
    ["a", "login"],
    ["a", "upload"],
    ["b", "upload"],
    ["b", "login"],
    ["c", "login"],
    ["d", "pay"],
    ["d", "refund"],
    ["e", "refund"],
    ["e", "pay"]
]
Output: 4

Explanation:
- a and b share {"login", "upload"}
- d and e share {"pay", "refund"}
- c has {"login"} alone

Therefore, 4 users belong to matching groups.
"""

from collections import defaultdict
from typing import DefaultDict, Dict, FrozenSet, List, Set, Tuple


class Solution:
    def build_user_action_sets(self, events: List[List[str]]) -> Dict[str, Set[str]]:
        """
        Build a mapping from each user to the set of distinct actions they performed.

        Args:
            events: A list of [userId, action] pairs.

        Returns:
            A dictionary where:
            - key   = userId
            - value = set of distinct actions performed by that user

        Time complexity:
            O(n), where n is the number of events.
            Each event is processed once, and set insertion is O(1) average.

        Space complexity:
            O(u + a), where:
            - u is the number of distinct users
            - a is the total number of distinct (user, action) pairs stored
        """
        # We use defaultdict(set) so that each new user automatically starts with
        # an empty set. This avoids writing extra "if user not in map" checks.
        user_to_actions: DefaultDict[str, Set[str]] = defaultdict(set)

        # Process every event exactly once.
        for user_id, action in events:
            # Because the value is a set, repeated actions for the same user
            # are automatically ignored after the first insertion.
            #
            # Example:
            # If we see ["u3", "view"] twice, the set remains {"view"}.
            user_to_actions[user_id].add(action)

        return dict(user_to_actions)

    def countUsersWithDuplicateDailyActionSets(self, events: List[List[str]]) -> int:
        """
        Count how many users belong to a group where at least one other user has
        the exact same set of distinct daily actions.

        Args:
            events: A list of [userId, action] pairs.

        Returns:
            The number of distinct users whose daily action set matches at least
            one other user's daily action set.

        Time complexity:
            O(n + m * k log k) in the sorting-based interpretation, but this
            implementation avoids sorting by using frozenset:
            O(n + total_distinct_actions_across_users) on average.

            More specifically:
            - O(n) to build per-user action sets
            - O(m) to convert each user's set into a frozenset key, where the
              total size of all sets contributes to the overall work

            Here:
            - n = number of events
            - m = number of distinct users

        Space complexity:
            O(u + a), where:
            - u is the number of distinct users
            - a is the total number of distinct (user, action) pairs
        """
        # ---------------------------------------------------------------------
        # STEP 1: Build each user's distinct action set.
        #
        # Why?
        # The problem explicitly says repeated actions by the same user should
        # count only once. So before comparing users, we must reduce each user's
        # event list into a set of unique actions.
        #
        # Example 1:
        # u1 -> {"view", "click"}
        # u2 -> {"click", "view"}
        # u3 -> {"view"}
        #
        # Notice that sets ignore order and duplicates, which is exactly what we need.
        # ---------------------------------------------------------------------
        user_to_actions: Dict[str, Set[str]] = self.build_user_action_sets(events)

        # ---------------------------------------------------------------------
        # STEP 2: Group users by a canonical representation of their action set.
        #
        # We need a hashable representation so it can be used as a dictionary key.
        # A normal Python set is mutable and therefore not hashable.
        #
        # So we convert each set into a frozenset:
        # - frozenset({"click", "view"}) is hashable
        # - order does not matter
        # - duplicates were already removed earlier
        #
        # This is a perfect fit for "same set of actions" matching.
        # ---------------------------------------------------------------------
        action_set_count: DefaultDict[FrozenSet[str], int] = defaultdict(int)

        for actions in user_to_actions.values():
            # Convert the mutable set into an immutable frozenset so it can be
            # used as a dictionary key.
            canonical_key: FrozenSet[str] = frozenset(actions)

            # Count how many users have this exact action set.
            action_set_count[canonical_key] += 1

        # ---------------------------------------------------------------------
        # STEP 3: Sum the sizes of all matching groups.
        #
        # If a canonical action set appears:
        # - once  -> that user is alone, do not count
        # - twice -> both users count
        # - three times -> all three users count
        #
        # So for every group with frequency >= 2, add the full frequency.
        # ---------------------------------------------------------------------
        matching_user_count: int = 0

        for frequency in action_set_count.values():
            if frequency >= 2:
                matching_user_count += frequency

        return matching_user_count


def run_example(solution: Solution, events: List[List[str]], expected: int) -> None:
    """
    Run one example test case and print the result.

    Args:
        solution: An instance of Solution.
        events: Input event list.
        expected: Expected answer for comparison.

    Returns:
        None

    Time complexity:
        Same as the main solution method for the given input size.

    Space complexity:
        Same as the main solution method for the given input size.
    """
    result: int = solution.countUsersWithDuplicateDailyActionSets(events)
    print("Events:", events)
    print("Output:", result)
    print("Expected:", expected)
    print("Pass:", result == expected)
    print("-" * 60)


if __name__ == "__main__":
    solver = Solution()

    # Example 1 from the problem statement.
    #
    # Trace:
    # u1 -> {"view", "click"}
    # u2 -> {"click", "view"}   same as u1
    # u3 -> {"view"}
    #
    # Matching group: {u1, u2}
    # Count = 2
    events1: List[List[str]] = [
        ["u1", "view"],
        ["u2", "click"],
        ["u1", "click"],
        ["u2", "view"],
        ["u3", "view"],
        ["u3", "view"],
    ]
    run_example(solver, events1, 2)

    # Example 2 from the problem statement.
    #
    # Trace:
    # a -> {"login", "upload"}
    # b -> {"upload", "login"}  same as a
    # c -> {"login"}
    # d -> {"pay", "refund"}
    # e -> {"refund", "pay"}    same as d
    #
    # Matching groups:
    # {a, b} and {d, e}
    # Count = 4
    events2: List[List[str]] = [
        ["a", "login"],
        ["a", "upload"],
        ["b", "upload"],
        ["b", "login"],
        ["c", "login"],
        ["d", "pay"],
        ["d", "refund"],
        ["e", "refund"],
        ["e", "pay"],
    ]
    run_example(solver, events2, 4)

    # Additional small sanity check:
    #
    # x -> {"view"}
    # y -> {"click"}
    # z -> {"share"}
    #
    # No duplicates, so answer = 0
    events3: List[List[str]] = [
        ["x", "view"],
        ["y", "click"],
        ["z", "share"],
    ]
    run_example(solver, events3, 0)