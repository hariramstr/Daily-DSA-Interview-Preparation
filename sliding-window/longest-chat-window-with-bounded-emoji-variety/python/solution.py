"""
Title: Longest Chat Window With Bounded Emoji Variety

Problem Description:
A messaging platform stores the sequence of emoji reactions added to a live chat as an
array of strings, where each string is a single emoji code such as ":smile:" or ":fire:".
Product analysts want to identify the longest contiguous time window in which the
conversation stayed focused enough that no more than k distinct emoji types were used.

Given an array reactions and an integer k, return the length of the longest contiguous
subarray that contains at most k distinct emoji strings.

A window is contiguous, so you may only choose reactions that appear next to each other
in the original array. If k is 0, the answer is 0 because no emoji types are allowed.
If the array is empty, return 0.

Your solution should be efficient enough for large chat logs, so an approach that checks
every possible subarray will be too slow.

Constraints:
- 0 <= reactions.length <= 200000
- 0 <= k <= reactions.length
- Each reactions[i] is a non-empty string of length 1 to 20
- reactions[i] consists of visible ASCII characters

Example 1:
Input: reactions = [":smile:",":fire:",":smile:",":heart:",":fire:",":fire:"], k = 2
Output: 3

Example 2:
Input: reactions = [":ok:",":ok:",":wave:",":wave:",":wave:",":star:"], k = 1
Output: 3
"""

from typing import Dict, List


class Solution:
    def longest_chat_window(self, reactions: List[str], k: int) -> int:
        """
        Return the length of the longest contiguous subarray containing at most
        k distinct emoji strings.

        Args:
            reactions: List of emoji reaction strings in chat order.
            k: Maximum number of distinct emoji types allowed in the window.

        Returns:
            The maximum valid window length.

        Time complexity:
            O(n), where n is the length of reactions, because each element is
            added to and removed from the sliding window at most once.

        Space complexity:
            O(k) in the typical sliding-window sense for the frequency map of
            elements currently inside the window. In the worst case, this can be
            O(n) if k is large and the current window contains many distinct values.
        """
        # If no emoji types are allowed, then no non-empty window can be valid.
        # The problem explicitly states that when k == 0, the answer is 0.
        if k == 0:
            return 0

        # If the input list is empty, there is no window to examine.
        if not reactions:
            return 0

        # This dictionary will store how many times each emoji appears inside
        # the current sliding window.
        #
        # Example:
        # If the current window is [":smile:", ":fire:", ":smile:"],
        # then counts would be:
        # {
        #     ":smile:": 2,
        #     ":fire:": 1
        # }
        counts: Dict[str, int] = {}

        # left marks the beginning of the current window.
        # We will expand the window by moving 'right' forward one step at a time.
        left: int = 0

        # best_length stores the longest valid window length found so far.
        best_length: int = 0

        # Iterate over the array with 'right' as the end of the current window.
        for right, emoji in enumerate(reactions):
            # Step 1: Expand the window to include reactions[right].
            #
            # We increase the count of this emoji in the frequency map.
            # If it was not already present, get(..., 0) starts it at 0 first.
            counts[emoji] = counts.get(emoji, 0) + 1

            # Step 2: If the window now contains too many distinct emoji types,
            # shrink it from the left until it becomes valid again.
            #
            # The number of distinct emoji types currently in the window is
            # exactly len(counts), because each key in the dictionary represents
            # one emoji type with a positive count.
            while len(counts) > k:
                # Identify the emoji that is about to leave the window.
                left_emoji: str = reactions[left]

                # Decrease its count because we are moving the left boundary
                # one step to the right, removing this emoji from the window.
                counts[left_emoji] -= 1

                # If its count becomes zero, that means this emoji type is no
                # longer present anywhere in the current window.
                #
                # We remove it from the dictionary entirely so that len(counts)
                # correctly reflects the number of distinct emoji types.
                if counts[left_emoji] == 0:
                    del counts[left_emoji]

                # Move the left boundary rightward to complete the shrink step.
                left += 1

            # Step 3: At this point, the window [left, right] is guaranteed
            # to contain at most k distinct emoji types, so it is valid.
            #
            # Compute its length and update the best answer if this window is
            # longer than any valid window seen before.
            current_length: int = right - left + 1
            if current_length > best_length:
                best_length = current_length

        # After scanning the entire array, best_length holds the answer.
        return best_length

    def length_of_longest_subarray_at_most_k_distinct(
        self, reactions: List[str], k: int
    ) -> int:
        """
        Convenience wrapper that calls the main sliding-window solution.

        Args:
            reactions: List of emoji reaction strings in chat order.
            k: Maximum number of distinct emoji types allowed.

        Returns:
            The maximum length of a contiguous subarray with at most k distinct strings.

        Time complexity:
            O(n), where n is the length of reactions.

        Space complexity:
            O(k) on average for the active window frequency map, with worst-case O(n).
        """
        return self.longest_chat_window(reactions, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    reactions1: List[str] = [
        ":smile:",
        ":fire:",
        ":smile:",
        ":heart:",
        ":fire:",
        ":fire:",
    ]
    k1: int = 2
    result1: int = solution.longest_chat_window(reactions1, k1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2 from the problem statement
    reactions2: List[str] = [
        ":ok:",
        ":ok:",
        ":wave:",
        ":wave:",
        ":wave:",
        ":star:",
    ]
    k2: int = 1
    result2: int = solution.longest_chat_window(reactions2, k2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional edge cases for completeness
    reactions3: List[str] = []
    k3: int = 2
    result3: int = solution.longest_chat_window(reactions3, k3)
    print("Empty Array Result:", result3)  # Expected: 0

    reactions4: List[str] = [":a:", ":b:", ":a:"]
    k4: int = 0
    result4: int = solution.longest_chat_window(reactions4, k4)
    print("k = 0 Result:", result4)  # Expected: 0

    reactions5: List[str] = [":x:", ":x:", ":x:"]
    k5: int = 1
    result5: int = solution.longest_chat_window(reactions5, k5)
    print("Single Distinct Emoji Result:", result5)  # Expected: 3