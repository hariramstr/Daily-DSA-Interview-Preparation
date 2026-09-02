"""
Title: Count User Pairs With the Same Relative Notification Delays

Problem Description:
A product team records the times, in minutes, when each user received notifications
during a single day. For each user, the absolute start time is not important; only
the pattern of gaps between consecutive notifications matters.

Two users are considered equivalent if, after sorting their notification times in
increasing order, they produce the same sequence of relative delays from the first
notification. In other words, if a user's sorted times are [t0, t1, t2, ...], their
delay signature is [0, t1 - t0, t2 - t0, ...].

Users with different numbers of notifications can never be equivalent.

You are given a list of users, where each user's data is an array of integers
representing notification times. Count how many unordered pairs of users have the
same delay signature.

If a user has only one notification, their signature is simply [0]. Duplicate times
for the same user are allowed and should be preserved after sorting.

Return the total number of equivalent user pairs.

Constraints:
- 1 <= users.length <= 100000
- 1 <= users[i].length <= 100
- 0 <= users[i][j] <= 10^9
- The sum of all users[i].length does not exceed 2 * 10^5

Example 1:
Input: users = [[5,10,20],[100,105,115],[3,8,18],[7,7,9],[20,20,22]]
Output: 4

Example 2:
Input: users = [[4],[9],[1,4,4],[10,13,13],[2,5,6]]
Output: 2
"""

from typing import Dict, List, Tuple


class Solution:
    def _build_signature(self, times: List[int]) -> Tuple[int, ...]:
        """
        Build a canonical delay signature for one user's notification times.

        The times are first sorted because the problem defines equivalence using
        sorted notification times. Then we convert the sorted list into a tuple
        of delays measured from the first notification time.

        Example:
        times = [20, 5, 10]
        sorted = [5, 10, 20]
        signature = (0, 5, 15)

        Args:
            times: A list of notification times for one user.

        Returns:
            A tuple representing the user's relative delay signature.

        Time complexity:
            O(k log k), where k is the number of notifications for this user,
            due to sorting.

        Space complexity:
            O(k), for the sorted copy and the signature tuple.
        """
        # We sort the times because the problem explicitly says equivalence is based
        # on the timestamps after sorting in increasing order.
        #
        # Important detail:
        # Duplicate times must be preserved. Sorting naturally keeps duplicates,
        # which means signatures such as [0, 0, 2] are handled correctly.
        sorted_times: List[int] = sorted(times)

        # The first sorted notification becomes our reference point.
        # Every other value in the signature is measured relative to this first time.
        first_time: int = sorted_times[0]

        # We use a tuple instead of a list because:
        # 1. Tuples are immutable.
        # 2. Tuples are hashable, so they can be used directly as dictionary keys.
        # This makes them ideal for grouping users by identical signatures.
        signature: Tuple[int, ...] = tuple(time - first_time for time in sorted_times)

        return signature

    def count_same_delay_pairs(self, users: List[List[int]]) -> int:
        """
        Count unordered pairs of users that share the same relative delay signature.

        The algorithm processes each user independently:
        1. Sort that user's notification times.
        2. Convert them into a canonical signature of delays from the first time.
        3. Use a hash map to count how many times each signature has appeared.
        4. Each time we see a signature again, it forms a new pair with every
           previous user that had the same signature.

        This incremental counting avoids a second pass for combination formulas,
        though both approaches are valid.

        Args:
            users: A list where each element is a list of notification times for one user.

        Returns:
            The total number of unordered equivalent user pairs.

        Time complexity:
            O(sum(k_i log k_i)) across all users, where k_i is the number of
            notifications for user i.

        Space complexity:
            O(u), where u is the number of distinct signatures stored in the hash map.
        """
        # This dictionary maps:
        #   signature -> how many users seen so far have this exact signature
        #
        # Example:
        #   (0, 5, 15) -> 3
        # means we have already processed 3 users whose relative delays are [0, 5, 15].
        signature_count: Dict[Tuple[int, ...], int] = {}

        # This will store the final answer.
        total_pairs: int = 0

        # We process users one by one.
        for times in users:
            # Convert the current user's timestamps into a canonical, hashable form.
            signature: Tuple[int, ...] = self._build_signature(times)

            # If we have seen this signature before, then the current user forms
            # one new pair with each previous user having the same signature.
            #
            # Example:
            # If signature_count[signature] == 3, then this new user creates:
            #   3 new pairs
            # with those 3 earlier matching users.
            previous_matches: int = signature_count.get(signature, 0)
            total_pairs += previous_matches

            # Now record that we have seen one more user with this signature.
            signature_count[signature] = previous_matches + 1

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # [5,10,20]     -> sorted [5,10,20]     -> signature (0,5,15)
    # [100,105,115] -> sorted [100,105,115] -> signature (0,5,15)
    # [3,8,18]      -> sorted [3,8,18]      -> signature (0,5,15)
    # [7,7,9]       -> sorted [7,7,9]       -> signature (0,0,2)
    # [20,20,22]    -> sorted [20,20,22]    -> signature (0,0,2)
    #
    # Pairs:
    # - Three users with (0,5,15) => 3 choose 2 = 3 pairs
    # - Two users with (0,0,2)    => 2 choose 2 = 1 pair
    # Total = 4
    users1: List[List[int]] = [
        [5, 10, 20],
        [100, 105, 115],
        [3, 8, 18],
        [7, 7, 9],
        [20, 20, 22],
    ]
    result1: int = solution.count_same_delay_pairs(users1)
    print(result1)  # Expected: 4

    # Example 2:
    # [4]        -> signature (0)
    # [9]        -> signature (0)
    # [1,4,4]    -> sorted [1,4,4]    -> signature (0,3,3)
    # [10,13,13] -> sorted [10,13,13] -> signature (0,3,3)
    # [2,5,6]    -> sorted [2,5,6]    -> signature (0,3,4)
    #
    # Pairs:
    # - Two users with (0,)      => 1 pair
    # - Two users with (0,3,3)   => 1 pair
    # Total = 2
    users2: List[List[int]] = [
        [4],
        [9],
        [1, 4, 4],
        [10, 13, 13],
        [2, 5, 6],
    ]
    result2: int = solution.count_same_delay_pairs(users2)
    print(result2)  # Expected: 2