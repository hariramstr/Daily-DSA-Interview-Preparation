"""
Title: Maximum Reliable Prefix After One Sensor Removal

Problem Description:
You are given an array readings of length n, where readings[i] is the signal quality
reported by the i-th sensor in a fixed processing order.

A prefix of the array is considered reliable if the greatest common divisor (GCD) of
every pair of adjacent values in that prefix is greater than 1.

Formally, a prefix of length L is reliable if for every index j with
0 <= j < L - 1, gcd(readings[j], readings[j + 1]) > 1.

You may permanently remove at most one element from the array before evaluating the
prefix. After removing zero or one element, the remaining elements keep their original
relative order, and elements that were separated by the removed element become adjacent.

Return the maximum possible length of a reliable prefix of the resulting array.

Important note:
The prefix is taken from the resulting array after deletion, so removing an early element
changes which values appear in the prefix and which pairs become adjacent.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= readings[i] <= 10^9
- Expected complexity: O(n) or O(n log V), where V is the maximum reading value
"""

from math import gcd
from typing import List


class Solution:
    def max_reliable_prefix(self, readings: List[int]) -> int:
        """
        Compute the maximum possible reliable prefix length after removing at most one element.

        The key idea is to convert the original condition on adjacent array values into a
        condition on a boolean array:
            good[i] = (gcd(readings[i], readings[i + 1]) > 1)

        Without deletion, the longest reliable prefix is simply:
            1 + number of consecutive True values from the start of good

        With one deletion:
        - If we delete an element at index k, then:
          * all adjacency checks before k - 1 stay the same
          * the pair involving k disappears
          * if k has both a left and right neighbor, a new "bridge" pair appears:
                gcd(readings[k - 1], readings[k + 1]) > 1
          * after that, the remaining original adjacency checks continue, shifted by one

        We precompute:
        - good: adjacency validity in the original array
        - next_bad[i]: the first index >= i where good[index] is False, or len(good) if none

        Then for each possible deletion index k, we can determine in O(1) how far the
        reliable prefix extends in the resulting array.

        Args:
            readings: List of sensor readings.

        Returns:
            Maximum reliable prefix length after removing at most one element.

        Time complexity:
            O(n log V), because each gcd call costs O(log V), and we perform O(n) gcd calls.

        Space complexity:
            O(n), for the adjacency and helper arrays.
        """
        n: int = len(readings)

        # If there is only one reading, the prefix of length 1 is always reliable.
        # There are no adjacent pairs to check.
        if n == 1:
            return 1

        # ---------------------------------------------------------------------
        # Step 1: Build the adjacency-validity array.
        #
        # good[i] corresponds to the pair:
        #   (readings[i], readings[i + 1])
        #
        # good[i] is True if gcd(readings[i], readings[i + 1]) > 1.
        #
        # Example:
        # readings = [6, 10, 15, 7, 14]
        # good     = [T,  T,  F, T]
        # because gcds are [2, 5, 1, 7]
        # ---------------------------------------------------------------------
        m: int = n - 1
        good: List[bool] = [gcd(readings[i], readings[i + 1]) > 1 for i in range(m)]

        # ---------------------------------------------------------------------
        # Step 2: Precompute next_bad.
        #
        # next_bad[i] = the smallest index j >= i such that good[j] is False.
        # If no such j exists, next_bad[i] = m.
        #
        # This lets us answer:
        # "Starting from adjacency index s, how many consecutive valid edges remain?"
        # in O(1), because the first failure is next_bad[s].
        #
        # We also define next_bad[m] = m for convenience.
        # ---------------------------------------------------------------------
        next_bad: List[int] = [m] * (m + 1)
        next_bad[m] = m

        for i in range(m - 1, -1, -1):
            if not good[i]:
                next_bad[i] = i
            else:
                next_bad[i] = next_bad[i + 1]

        # ---------------------------------------------------------------------
        # Step 3: Baseline answer with no deletion.
        #
        # If the first bad adjacency is at index b, then the reliable prefix length is b + 1.
        # Why +1?
        # Because a prefix of length L has exactly L - 1 adjacency checks.
        #
        # If all adjacencies are good, next_bad[0] = m, and the prefix length is n.
        # ---------------------------------------------------------------------
        first_bad_without_deletion: int = next_bad[0]
        answer: int = first_bad_without_deletion + 1

        # ---------------------------------------------------------------------
        # Step 4: Try deleting each index k.
        #
        # We compute the reliable prefix length in the resulting array after deleting readings[k].
        #
        # There are three structural cases:
        #
        # A) k == 0:
        #    The resulting array starts at original index 1.
        #    Its adjacency checks correspond exactly to original good[1], good[2], ...
        #
        # B) k == n - 1:
        #    We delete the last element. This cannot improve any earlier failure.
        #    The resulting prefix length is at most n - 1, and its adjacency checks are
        #    original good[0], good[1], ..., good[n - 3].
        #
        # C) 0 < k < n - 1:
        #    The new adjacency sequence in the resulting array is:
        #      good[0], good[1], ..., good[k - 2],
        #      bridge = gcd(readings[k - 1], readings[k + 1]) > 1,
        #      good[k + 1], good[k + 2], ...
        #
        # We only care about the longest reliable prefix, so we stop at the first failure.
        # ---------------------------------------------------------------------
        for k in range(n):
            # -------------------------------------------------------------
            # Case A: delete the first element.
            #
            # Resulting array:
            #   readings[1], readings[2], ..., readings[n - 1]
            #
            # Its first adjacency corresponds to original good[1].
            #
            # If the first bad adjacency from index 1 onward is at original index b,
            # then in the new array that is still the first failure, and the number of
            # valid edges before it is (b - 1). Therefore prefix length is:
            #   (b - 1) + 1 = b
            #
            # More simply:
            #   first failure among good[1:] is next_bad[1]
            #   resulting prefix length = next_bad[1]
            #
            # If all are good, next_bad[1] = m, and the resulting array length is n - 1.
            # Since m = n - 1, this matches.
            # -------------------------------------------------------------
            if k == 0:
                candidate: int = next_bad[1]
                answer = max(answer, candidate)
                continue

            # -------------------------------------------------------------
            # Case B: delete the last element.
            #
            # Resulting array:
            #   readings[0], readings[1], ..., readings[n - 2]
            #
            # Its adjacency checks are exactly good[0] through good[n - 3].
            # So the reliable prefix is the same as the original one, but cannot exceed n - 1.
            #
            # Since the original first failure already determines the prefix, we can write:
            #   candidate = min(answer_without_deletion, n - 1)
            #
            # But to keep the logic explicit:
            #   first bad is next_bad[0]
            #   prefix length is min(next_bad[0] + 1, n - 1)
            # -------------------------------------------------------------
            if k == n - 1:
                candidate = min(next_bad[0] + 1, n - 1)
                answer = max(answer, candidate)
                continue

            # -------------------------------------------------------------
            # Case C: delete an interior element at index k.
            #
            # Before the bridge:
            #   The adjacencies good[0] ... good[k - 2] remain unchanged.
            #
            # Therefore, if there is already a bad adjacency before k - 1,
            # deletion at k cannot fix it, because the prefix fails before we even reach
            # the deleted position.
            #
            # Specifically:
            #   if next_bad[0] <= k - 2
            # then the reliable prefix length is simply next_bad[0] + 1.
            #
            # Otherwise, all adjacencies before the bridge are good, and we must test
            # the new bridge pair:
            #   gcd(readings[k - 1], readings[k + 1]) > 1
            #
            # If the bridge is bad, then the prefix ends right before that failed bridge.
            # The number of good edges before the bridge is k - 1, so prefix length is k.
            #
            # If the bridge is good, then we continue checking original good[k + 1], good[k + 2], ...
            # The first failure there is next_bad[k + 1].
            #
            # If next_bad[k + 1] = b, then the number of valid edges in the new array is:
            #   - (k - 1) edges before the bridge
            #   - 1 bridge edge
            #   - (b - (k + 1)) edges after the bridge before the first failure
            # Total = b - 1 valid edges
            # So prefix length = (b - 1) + 1 = b
            #
            # If there is no failure after that, next_bad[k + 1] = m = n - 1,
            # and the resulting array length is n - 1, which again matches.
            # -------------------------------------------------------------
            first_bad_before_bridge: int = next_bad[0]
            if first_bad_before_bridge <= k - 2:
                candidate = first_bad_before_bridge + 1
                answer = max(answer, candidate)
                continue

            bridge_good: bool = gcd(readings[k - 1], readings[k + 1]) > 1
            if not bridge_good:
                candidate = k
                answer = max(answer, candidate)
                continue

            candidate = next_bad[k + 1]
            answer = max(answer, candidate)

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the statement (corrected):
    # readings = [6, 10, 15, 7, 14]
    # Original gcds: 2, 5, 1, 7
    # Longest reliable prefix without deletion is length 3.
    # No single deletion improves it beyond 3.
    readings_1: List[int] = [6, 10, 15, 7, 14]
    result_1: int = solution.max_reliable_prefix(readings_1)
    print(f"Input: {readings_1}")
    print(f"Maximum reliable prefix length: {result_1}")
    print("Expected: 3")
    print()

    # Example 2 from the statement (corrected):
    # readings = [8, 12, 18, 25, 35, 49]
    # Original gcds: 4, 6, 1, 5, 7
    # Longest reliable prefix without deletion is length 3.
    # No single deletion improves it beyond 3.
    readings_2: List[int] = [8, 12, 18, 25, 35, 49]
    result_2: int = solution.max_reliable_prefix(readings_2)
    print(f"Input: {readings_2}")
    print(f"Maximum reliable prefix length: {result_2}")
    print("Expected: 3")
    print()

    # Additional sanity checks:
    readings_3: List[int] = [2]
    result_3: int = solution.max_reliable_prefix(readings_3)
    print(f"Input: {readings_3}")
    print(f"Maximum reliable prefix length: {result_3}")
    print("Expected: 1")
    print()

    readings_4: List[int] = [6, 10, 15, 21]
    result_4: int = solution.max_reliable_prefix(readings_4)
    print(f"Input: {readings_4}")
    print(f"Maximum reliable prefix length: {result_4}")
    print("Expected: 4")
    print()

    readings_5: List[int] = [2, 3, 6]
    result_5: int = solution.max_reliable_prefix(readings_5)
    print(f"Input: {readings_5}")
    print(f"Maximum reliable prefix length: {result_5}")
    print("Expected: 2")
    print()