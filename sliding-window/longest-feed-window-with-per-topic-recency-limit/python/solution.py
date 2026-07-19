"""
Title: Longest Feed Window With Per-Topic Recency Limit

Problem Description:
You are building a ranking service for a social media feed. Each post belongs to a topic
represented by an integer in the array topics, where topics[i] is the topic of the i-th
post in chronological order. A feed window is any contiguous subarray of posts.

To keep the feed diverse, the platform enforces a recency rule: for every topic, the
distance between any two consecutive appearances of that same topic inside the chosen
window must be at most limit. In other words, if a topic appears at positions
p1 < p2 < ... < pk within the window, then for every j, pj+1 - pj <= limit must hold.
A topic that appears only once in the window always satisfies the rule.

Return the length of the longest contiguous feed window that satisfies this condition.

Note that this is not a global condition on the whole array. A pair of equal topics only
matters if both occurrences are inside the same chosen window. Also, topics may be as
large as 10^9, so solutions that depend on the numeric range of values are not acceptable.

Constraints:
- 1 <= topics.length <= 2 * 10^5
- 1 <= topics[i] <= 10^9
- 1 <= limit <= topics.length
"""

from collections import defaultdict, deque
from typing import Deque, DefaultDict, List


class Solution:
    def longest_feed_window(self, topics: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous window such that, for every topic,
        the distance between every pair of consecutive appearances inside the window
        is at most `limit`.

        Args:
            topics: List of topic IDs in chronological order.
            limit: Maximum allowed distance between consecutive equal topics inside
                the chosen window.

        Returns:
            The maximum valid window length.

        Time complexity:
            O(n), where n is len(topics). Each index is added once and removed once
            from its topic deque, and the left pointer only moves forward.

        Space complexity:
            O(n) in the worst case for storing indices across deques.
        """
        # For each topic value, we store the indices of that topic that are currently
        # inside the active sliding window [left, right].
        #
        # Why a deque?
        # - We append new indices at the right as we expand the window.
        # - We remove old indices from the left when the window's left boundary moves.
        # - Both operations are O(1).
        positions: DefaultDict[int, Deque[int]] = defaultdict(deque)

        # `left` is the left boundary of our sliding window.
        left: int = 0

        # `best` stores the maximum valid window length found so far.
        best: int = 0

        # We expand the window one element at a time using `right`.
        for right, topic in enumerate(topics):
            # Add the current index to the deque for this topic, because this topic
            # is now included in the window [left, right].
            positions[topic].append(right)

            # The only way the window can become invalid after adding topics[right]
            # is through this exact topic.
            #
            # Reason:
            # Before adding `right`, the previous window [left, right - 1] was valid.
            # Adding one new element cannot create a new violation for any *other*
            # topic, because their sets of occurrences inside the window did not gain
            # a new consecutive pair. Only the current topic got a new occurrence.
            #
            # So we only need to inspect the last two occurrences of `topic` that are
            # currently inside the window.
            #
            # If the distance between those two consecutive occurrences is greater than
            # `limit`, then the window is invalid and we must move `left` forward until
            # that older occurrence is excluded from the window.
            while len(positions[topic]) >= 2 and positions[topic][-1] - positions[topic][-2] > limit:
                # The older of the two problematic consecutive occurrences.
                bad_prev_index: int = positions[topic][-2]

                # To remove that occurrence from the window, `left` must move past it.
                # Every index from the current `left` up to and including bad_prev_index
                # leaves the window.
                new_left: int = bad_prev_index + 1

                # As we move `left`, we must also remove those outgoing indices from
                # their respective topic deques, because those indices are no longer
                # inside the current window.
                while left < new_left:
                    outgoing_topic: int = topics[left]

                    # Since indices are stored in increasing order, and `left` is moving
                    # from left to right, the outgoing index must be at the front of the
                    # deque for its topic.
                    positions[outgoing_topic].popleft()

                    left += 1

                # After this adjustment, the older problematic occurrence is gone, so
                # the violating consecutive pair for `topic` no longer both lie inside
                # the window.
                #
                # It is possible in theory that if there were many occurrences, we may
                # need to check again, so we keep the `while` loop.

            # At this point, the window [left, right] is valid.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    topics1 = [4, 1, 4, 2, 4, 3, 2]
    limit1 = 2
    result1 = solution.longest_feed_window(topics1, limit1)
    print(f"topics = {topics1}, limit = {limit1}")
    print(f"Longest valid feed window length: {result1}")
    print("Expected: 4")
    print()

    # Example 2
    # The problem statement's explanation corrects itself:
    # the full array is invalid because topic 7 appears at indices 0, 2, 6,
    # and the consecutive gap 6 - 2 = 4 exceeds limit = 3.
    # The longest valid window length is 5.
    topics2 = [7, 5, 7, 8, 5, 9, 7, 5]
    limit2 = 3
    result2 = solution.longest_feed_window(topics2, limit2)
    print(f"topics = {topics2}, limit = {limit2}")
    print(f"Longest valid feed window length: {result2}")
    print("Expected: 5")
    print()

    # Additional quick checks
    topics3 = [1]
    limit3 = 1
    result3 = solution.longest_feed_window(topics3, limit3)
    print(f"topics = {topics3}, limit = {limit3}")
    print(f"Longest valid feed window length: {result3}")
    print("Expected: 1")
    print()

    topics4 = [1, 2, 1, 2, 1, 2]
    limit4 = 2
    result4 = solution.longest_feed_window(topics4, limit4)
    print(f"topics = {topics4}, limit = {limit4}")
    print(f"Longest valid feed window length: {result4}")
    print("Expected: 6")