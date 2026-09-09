"""
Title: Count User Pairs With Matching Distinct Login Hours

Problem Description:
You are given a list of login records from an application. Each record is a pair
[userId, hour], where userId is a string and hour is an integer from 0 to 23
representing the hour of day when that user logged in. A user may appear many
times, and the same user may log in multiple times during the same hour.

Two users are considered hour-equivalent if the set of distinct hours in which
they logged in is exactly the same. For example, if one user logged in at hours
[1, 3, 3, 8] and another logged in at hours [8, 1, 3], they are hour-equivalent
because both users have the distinct hour set {1, 3, 8}.

Return the number of unordered pairs of different users that are hour-equivalent.

The solution should be efficient for large inputs. A compact and fast approach is
to build a bitmask signature for each user's distinct login hours, then count how
many users share the same signature.

Constraints:
- 1 <= records.length <= 200000
- records[i].length == 2
- 1 <= userId.length <= 20
- userId consists of lowercase English letters, digits, or underscores
- 0 <= hour <= 23
- There may be up to 100000 distinct users
"""

from typing import Dict, List


class Solution:
    def count_hour_equivalent_pairs(self, records: List[List[object]]) -> int:
        """
        Count unordered pairs of different users whose distinct login-hour sets match.

        The method uses a 24-bit integer bitmask as a compact signature:
        - Bit h is 1 if the user logged in during hour h at least once.
        - Duplicate logins in the same hour do not matter, because setting the same
          bit multiple times keeps the bitmask unchanged.

        After building one bitmask per user, we count how many users share each
        bitmask. If a signature appears k times, then it contributes
        k * (k - 1) // 2 unordered matching pairs.

        Args:
            records: A list of [userId, hour] login records.

        Returns:
            The number of unordered pairs of different users that are hour-equivalent.

        Time complexity:
            O(n + u), where n is the number of records and u is the number of distinct users.

        Space complexity:
            O(u), where u is the number of distinct users.
        """
        # This dictionary maps each user ID to a 24-bit integer bitmask.
        #
        # Why a bitmask?
        # - There are only 24 possible hours: 0 through 23.
        # - A single integer can store whether each hour has appeared.
        # - This is much more compact and faster than storing a set for each user.
        #
        # Example:
        # If a user logged in at hours 1 and 3, then their mask is:
        #   (1 << 1) | (1 << 3)
        # which means bits 1 and 3 are turned on.
        user_to_mask: Dict[str, int] = {}

        # Step 1: Build the distinct-hour signature for every user.
        #
        # We process each record one by one.
        # For a record [user_id, hour]:
        # - Compute the bit corresponding to that hour: 1 << hour
        # - OR it into the user's current mask
        #
        # Important detail:
        # Using OR automatically handles duplicates correctly.
        # If the same user logs in multiple times during the same hour,
        # the same bit is set repeatedly, but the mask does not change.
        for record in records:
            user_id = str(record[0])
            hour = int(record[1])

            # Create a bit with only the given hour turned on.
            hour_bit = 1 << hour

            # Fetch the user's current mask, defaulting to 0 if this is the first
            # time we have seen the user.
            current_mask = user_to_mask.get(user_id, 0)

            # Turn on the bit for this hour.
            updated_mask = current_mask | hour_bit

            # Store the updated signature back into the dictionary.
            user_to_mask[user_id] = updated_mask

        # Step 2: Count how many users share each exact signature.
        #
        # This dictionary maps:
        #   bitmask signature -> number of users with that signature
        #
        # If two users have the same bitmask, then they logged in during exactly
        # the same set of distinct hours.
        signature_count: Dict[int, int] = {}

        for mask in user_to_mask.values():
            signature_count[mask] = signature_count.get(mask, 0) + 1

        # Step 3: For each signature group of size k, add the number of unordered pairs.
        #
        # Number of unordered pairs from k users is:
        #   C(k, 2) = k * (k - 1) // 2
        #
        # Example:
        # - If a signature appears 2 times, it contributes 1 pair.
        # - If a signature appears 3 times, it contributes 3 pairs.
        # - If a signature appears 1 time, it contributes 0 pairs.
        total_pairs = 0

        for count in signature_count.values():
            total_pairs += count * (count - 1) // 2

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # alice -> hours {1, 3}
    # bob   -> hours {1, 3}
    # cara  -> hours {2, 5}
    # Matching unordered pairs: (alice, bob)
    records1: List[List[object]] = [
        ["alice", 1],
        ["alice", 3],
        ["alice", 3],
        ["bob", 3],
        ["bob", 1],
        ["cara", 2],
        ["cara", 5],
    ]
    result1 = solution.count_hour_equivalent_pairs(records1)
    print("Example 1 Output:", result1)  # Expected: 1

    # Example 2:
    # u1 -> hours {0, 23}
    # u2 -> hours {0, 23}
    # u3 -> hours {0}
    # u4 -> hours {5}
    # Matching unordered pairs: (u1, u2)
    records2: List[List[object]] = [
        ["u1", 0],
        ["u1", 23],
        ["u2", 23],
        ["u2", 0],
        ["u3", 0],
        ["u3", 0],
        ["u4", 5],
    ]
    result2 = solution.count_hour_equivalent_pairs(records2)
    print("Example 2 Output:", result2)  # Expected: 1

    # Additional quick check:
    # a -> {1}
    # b -> {1}
    # c -> {1}
    # d -> {2}
    # Signature {1} appears 3 times => 3 pairs: (a,b), (a,c), (b,c)
    records3: List[List[object]] = [
        ["a", 1],
        ["b", 1],
        ["c", 1],
        ["d", 2],
    ]
    result3 = solution.count_hour_equivalent_pairs(records3)
    print("Additional Check Output:", result3)  # Expected: 3