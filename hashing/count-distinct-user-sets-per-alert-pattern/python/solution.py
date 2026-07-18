"""
Title: Count Distinct User Sets per Alert Pattern

Problem Description:
A monitoring system records security alerts as pairs (userId, alertCode). Multiple
records may exist for the same pair because the same user can trigger the same alert
many times. For each alertCode, define its user set as the set of distinct users who
triggered that alert at least once. Two alert codes are considered equivalent if their
user sets are exactly the same.

Your task is to return the number of unordered pairs of distinct alert codes that are
equivalent.

For example, if alert A1 was triggered by users {2, 5, 9} and alert B7 was also
triggered by users {2, 5, 9}, then (A1, B7) contributes 1 to the answer. Duplicate
records must not change a user set. Alert codes triggered by no users do not appear
in the input and therefore are not considered.

Design an algorithm that works efficiently for large inputs. A naive comparison of
every pair of alert codes and every user in their sets will be too slow. We instead
group records by alert code, remove duplicate users per alert, build a canonical
representation of each alert's user set, and count how many alerts share the same
representation.

Constraints:
- 1 <= records.length <= 2 * 10^5
- Each record is a pair [userId, alertCode]
- 1 <= userId <= 10^9
- 1 <= alertCode <= 10^9
- The same (userId, alertCode) pair may appear many times
- The answer can be as large as n * (n - 1) / 2, so use 64-bit arithmetic
"""

from typing import Dict, List, Set, Tuple


class Solution:
    def count_equivalent_alert_pairs(self, records: List[List[int]]) -> int:
        """
        Count unordered pairs of distinct alert codes that have exactly the same set
        of distinct users.

        The method works in three main stages:
        1. Build a mapping from each alert code to the set of users who triggered it.
           Using a set automatically removes duplicate (userId, alertCode) records.
        2. Convert each alert's user set into a canonical, hashable representation.
           We use a sorted tuple of user IDs so that alerts with the same users in
           different encounter orders still map to the same key.
        3. Count how many alert codes share each canonical representation. If a
           representation appears k times, then it contributes k * (k - 1) // 2
           unordered equivalent pairs.

        Args:
            records: A list of [userId, alertCode] pairs.

        Returns:
            The number of unordered pairs of distinct alert codes with identical user sets.

        Time complexity:
            O(m + sum(s_i log s_i)), where:
            - m is the number of input records
            - s_i is the number of distinct users for alert i
            The sorting cost comes from converting each user set into a sorted tuple.

        Space complexity:
            O(u), where u is the total number of distinct (alertCode, userId)
            relationships stored across all alert sets.
        """
        # ---------------------------------------------------------------------
        # Step 1: Build the user set for every alert code.
        #
        # We need, for each alertCode, the set of DISTINCT users who triggered it.
        # A Python set is the perfect data structure here because:
        # - inserting the same user multiple times has no effect
        # - average-case insertion is O(1)
        #
        # Example:
        # records = [[1,10],[2,10],[2,10],[1,20],[2,20],[3,30],[4,30]]
        #
        # After this loop:
        # alert_to_users = {
        #     10: {1, 2},
        #     20: {1, 2},
        #     30: {3, 4}
        # }
        # ---------------------------------------------------------------------
        alert_to_users: Dict[int, Set[int]] = {}

        for user_id, alert_code in records:
            # If this alert code has not been seen before, create an empty set for it.
            if alert_code not in alert_to_users:
                alert_to_users[alert_code] = set()

            # Add the user to the alert's set.
            # If the exact same (user_id, alert_code) appeared before, the set
            # simply ignores the duplicate automatically.
            alert_to_users[alert_code].add(user_id)

        # ---------------------------------------------------------------------
        # Step 2: Convert each alert's user set into a canonical representation.
        #
        # Why do we need this?
        # Sets are not hashable, so they cannot be used directly as dictionary keys.
        # Also, two sets with the same values may have different internal orders.
        #
        # To solve this, we:
        # - sort the users in each set
        # - convert the sorted result into a tuple
        #
        # This gives us a stable, hashable representation:
        # {2, 1} -> (1, 2)
        # {1, 2} -> (1, 2)
        #
        # Therefore, alerts with identical user sets will produce the exact same key.
        # ---------------------------------------------------------------------
        pattern_count: Dict[Tuple[int, ...], int] = {}

        for users in alert_to_users.values():
            # Sort to ensure a consistent order, then convert to tuple so it can
            # be used as a dictionary key.
            canonical_pattern: Tuple[int, ...] = tuple(sorted(users))

            # Count how many alert codes share this exact user-set pattern.
            if canonical_pattern not in pattern_count:
                pattern_count[canonical_pattern] = 0
            pattern_count[canonical_pattern] += 1

        # ---------------------------------------------------------------------
        # Step 3: For each shared pattern, count how many unordered alert pairs it creates.
        #
        # If a particular user-set pattern appears k times, that means there are
        # k different alert codes with the same user set.
        #
        # The number of unordered pairs among k items is:
        #   C(k, 2) = k * (k - 1) // 2
        #
        # We sum this over all patterns.
        #
        # Example 1:
        # pattern_count might be:
        #   (1, 2) -> 2
        #   (3, 4) -> 1
        #
        # Contribution:
        #   2 * 1 // 2 = 1
        #   1 * 0 // 2 = 0
        # Total = 1
        #
        # Example 2:
        #   (5, 7)   -> 2  contributes 1
        #   (8,)     -> 1  contributes 0
        #   (9, 10)  -> 2  contributes 1
        # Total = 2
        # ---------------------------------------------------------------------
        result: int = 0

        for count in pattern_count.values():
            result += count * (count - 1) // 2

        return result


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement.
    records1: List[List[int]] = [
        [1, 10],
        [2, 10],
        [2, 10],
        [1, 20],
        [2, 20],
        [3, 30],
        [4, 30],
    ]
    result1: int = solution.count_equivalent_alert_pairs(records1)
    print("Example 1 Output:", result1)  # Expected: 1

    # Example 2 from the problem statement.
    records2: List[List[int]] = [
        [5, 100],
        [7, 100],
        [5, 200],
        [7, 200],
        [8, 300],
        [8, 300],
        [9, 400],
        [10, 400],
        [9, 500],
        [10, 500],
    ]
    result2: int = solution.count_equivalent_alert_pairs(records2)
    print("Example 2 Output:", result2)  # Expected: 2

    # Additional small sanity check:
    # Alerts:
    # 1 -> {42}
    # 2 -> {42}
    # 3 -> {99}
    # Equivalent pairs: only (1, 2), so answer = 1
    records3: List[List[int]] = [
        [42, 1],
        [42, 1],
        [42, 2],
        [99, 3],
    ]
    result3: int = solution.count_equivalent_alert_pairs(records3)
    print("Additional Check Output:", result3)  # Expected: 1