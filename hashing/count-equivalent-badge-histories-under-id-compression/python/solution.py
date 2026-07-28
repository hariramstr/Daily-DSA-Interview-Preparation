"""
Title: Count Equivalent Badge Histories Under ID Compression
Difficulty: Hard
Topic: Hashing

Problem Description:
A company stores each employee's badge scan history as an integer array. Two histories
are considered equivalent if they have the same repetition structure after compressing
badge IDs by first appearance order.

Example:
- [42, 99, 42, 17] compresses to [0, 1, 0, 2]
- [7, 3, 7, 8] compresses to [0, 1, 0, 2]
So these two histories are equivalent.

Another example:
- [5, 5, 8] compresses to [0, 0, 1]
- [5, 8, 5] compresses to [0, 1, 0]
These are not equivalent.

You are given n badge histories, where the i-th history is an array of integers and
histories may have different lengths. Return the number of unordered pairs of histories
that are equivalent under this compression rule.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= total number of scanned IDs across all histories <= 2 * 10^5
- -10^9 <= badgeID <= 10^9
- 1 <= length of each history
- Return the answer as a 64-bit integer
"""

from typing import Dict, List, Tuple


class Solution:
    def compress_history(self, history: List[int]) -> Tuple[int, ...]:
        """
        Convert one badge history into its canonical compressed pattern.

        The compression rule is:
        - The first distinct value seen gets ID 0
        - The next new distinct value gets ID 1
        - And so on
        - Repeated values reuse the same assigned compressed ID

        Example:
        [42, 99, 42, 17] -> (0, 1, 0, 2)

        Args:
            history: A single badge history as a list of integers.

        Returns:
            A tuple representing the compressed pattern.

        Time complexity:
            O(m), where m is the length of the history.

        Space complexity:
            O(k), where k is the number of distinct values in the history.
        """
        # This dictionary remembers the compressed ID assigned to each original badge ID
        # the first time we encounter it in this history.
        #
        # Example while processing [42, 99, 42, 17]:
        # after seeing 42 -> {42: 0}
        # after seeing 99 -> {42: 0, 99: 1}
        # seeing 42 again reuses 0
        # after seeing 17 -> {42: 0, 99: 1, 17: 2}
        first_seen_to_id: Dict[int, int] = {}

        # This list will store the compressed pattern step by step.
        compressed: List[int] = []

        # This variable tracks the next new compressed ID to assign.
        next_id = 0

        # Process the history from left to right because the compression depends
        # specifically on first appearance order.
        for badge_id in history:
            # If this badge ID has never appeared before in this history,
            # assign it the next available compressed ID.
            if badge_id not in first_seen_to_id:
                first_seen_to_id[badge_id] = next_id
                next_id += 1

            # Append the already-known compressed ID for this badge ID.
            compressed.append(first_seen_to_id[badge_id])

        # We return a tuple instead of a list because tuples are hashable and can be used
        # as dictionary keys. This is important for counting how many histories share
        # the exact same compressed pattern.
        return tuple(compressed)

    def count_equivalent_histories(self, histories: List[List[int]]) -> int:
        """
        Count unordered pairs of badge histories that are equivalent under compression.

        The main idea:
        1. Convert each history into a canonical compressed pattern.
        2. Count how many times each pattern appears.
        3. For each pattern appearing c times, it contributes c * (c - 1) // 2 pairs.

        Args:
            histories: A list of badge histories.

        Returns:
            The number of unordered equivalent pairs.

        Time complexity:
            O(T), where T is the total number of badge IDs across all histories.

        Space complexity:
            O(T), in the worst case for storing patterns and temporary maps.
        """
        # This dictionary maps:
        # compressed pattern -> how many histories have exactly this pattern
        #
        # Example:
        # (0, 1, 0, 2) -> 3
        # (0, 0, 1)    -> 2
        pattern_count: Dict[Tuple[int, ...], int] = {}

        # We will compute the answer incrementally.
        #
        # Why incremental counting is nice:
        # Suppose we process histories one by one.
        # If the current pattern has already appeared x times,
        # then the current history forms exactly x new pairs with those previous histories.
        #
        # This avoids a second pass with combination formulas, although both are valid.
        answer = 0

        # Process every history independently.
        for history in histories:
            # Convert the current history into its canonical representation.
            pattern = self.compress_history(history)

            # If this pattern has appeared before k times, then this new history forms
            # k new unordered pairs with those previous k histories.
            previous_count = pattern_count.get(pattern, 0)
            answer += previous_count

            # Record that we have now seen one more history with this pattern.
            pattern_count[pattern] = previous_count + 1

        # Python integers automatically support large values, so this safely handles
        # 64-bit answers and beyond.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the statement
    histories1 = [
        [42, 99, 42, 17],
        [7, 3, 7, 8],
        [5, 5, 8],
        [8, 8, 1],
        [10, 11, 10, 12],
    ]
    result1 = solution.count_equivalent_histories(histories1)
    print("Example 1 result:", result1)  # Expected: 4

    # About Example 2:
    # The statement itself notes an inconsistency and clarifies that for the exact list
    # shown, the correct answer is 0.
    histories2 = [
        [1, 2, 1, 2],
        [4, 4, 5, 5],
        [9],
        [3, 1, 3],
        [8, 6, 8, 7],
    ]
    result2 = solution.count_equivalent_histories(histories2)
    print("Example 2 result:", result2)  # Correct for this exact list: 0

    # A corrected variant of Example 2 that really has one matching pair:
    histories2_corrected = [
        [1, 2, 1, 2],
        [6, 7, 6, 7],
        [9],
        [3, 1, 3],
        [8, 6, 8, 7],
    ]
    result2_corrected = solution.count_equivalent_histories(histories2_corrected)
    print("Corrected Example 2 result:", result2_corrected)  # Expected: 1