"""
Title: Longest Translation Stream With Synonym Budget
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are building a real-time translation quality monitor. A transcript has already been
tokenized into an array `words`, where each `words[i]` is the normalized meaning ID of
the spoken word at position `i`. Because multiple surface words may map to the same
meaning, repeated IDs are allowed and expected.

A translation team considers a contiguous segment valid if no meaning ID appears more
than `limit[id]` times inside that segment. However, the monitor is also allowed to
spend from a global synonym budget `B`: for each extra occurrence of a meaning ID beyond
its allowed limit inside the current segment, the segment consumes 1 unit of budget.

For example, if meaning 7 appears 5 times and `limit[7] = 3`, then meaning 7 contributes
2 to the budget usage of that segment.

Your task is to return the length of the longest contiguous segment whose total budget
usage is at most `B`.

Formally, for a window `[l, r]`, let `freq[x]` be the number of times meaning ID `x`
appears in that window. Its violation cost is:

    sum(max(0, freq[x] - limit[x])) for all meaning IDs x in the window

Find the maximum window length with violation cost `<= B`.

If a meaning ID appears in `words` but is not present in `limit`, its allowed limit is 0.

Constraints:
- 1 <= words.length <= 2 * 10^5
- 0 <= words[i] <= 10^9
- 0 <= B <= 2 * 10^5
- 0 <= limit.size() <= 2 * 10^5
- 0 <= limit[id] <= words.length
- The answer must be computed in better than O(n^2) time.

Example 1:
Input:
words = [4, 1, 4, 2, 4, 1, 3]
limit = {4: 2, 1: 1, 2: 1, 3: 1}
B = 1
Output:
5

Explanation:
The segment [4, 1, 4, 2, 4] has frequencies {4:3, 1:1, 2:1}. Only meaning 4 exceeds
its limit, by 1, so the total cost is 1. Its length is 5. Any longer valid segment
would require cost greater than 1.

Example 2:
Input:
words = [8, 8, 5, 8, 5, 6, 5, 5]
limit = {8: 1, 5: 2, 6: 1}
B = 2
Output:
6

Explanation:
The segment [8, 5, 8, 5, 6, 5] has frequencies {8:2, 5:3, 6:1}. Meaning 8 exceeds its
limit by 1 and meaning 5 exceeds its limit by 1, so total cost = 2. Its length is 6,
which is optimal.
"""

from typing import Dict, List


class Solution:
    def longest_translation_stream(self, words: List[int], limit: Dict[int, int], B: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose total violation cost
        is at most B.

        The violation cost of a window is:
            sum(max(0, freq[x] - limit[x])) over all IDs x in the window

        A meaning ID not present in `limit` is treated as having allowed limit 0.

        Args:
            words: List of normalized meaning IDs.
            limit: Dictionary mapping meaning ID -> allowed count inside a valid window.
            B: Global budget for total excess occurrences.

        Returns:
            The length of the longest valid contiguous segment.

        Time complexity:
            O(n), where n is len(words), because each index enters and leaves the
            sliding window at most once.

        Space complexity:
            O(k), where k is the number of distinct IDs currently tracked in the
            frequency dictionary, at most O(n) in the worst case.
        """
        # Frequency table for the current sliding window.
        # freq[x] tells us how many times meaning ID x appears in the current window.
        freq: Dict[int, int] = {}

        # `left` is the left boundary of the sliding window.
        left: int = 0

        # `current_cost` stores the total violation cost of the current window.
        #
        # Very important observation:
        # When we add one occurrence of some ID x:
        #   - If its old frequency was already >= allowed limit, then this new copy
        #     creates exactly +1 additional excess.
        #   - Otherwise, it creates no new excess.
        #
        # Similarly, when we remove one occurrence of x from the left:
        #   - If its old frequency was > allowed limit, then removing it reduces
        #     the excess by exactly 1.
        #   - Otherwise, the excess does not change.
        #
        # This lets us maintain the total cost incrementally in O(1) per move,
        # instead of recomputing the full sum every time.
        current_cost: int = 0

        # Best window length found so far.
        best: int = 0

        # Expand the window by moving `right` from left to right across the array.
        for right, word_id in enumerate(words):
            # Read the allowed limit for this meaning ID.
            # If it does not exist in the `limit` dictionary, the allowed count is 0.
            allowed: int = limit.get(word_id, 0)

            # Get the old frequency before adding the new element.
            old_count: int = freq.get(word_id, 0)

            # If old_count is already at least the allowed limit, then adding one more
            # occurrence creates one extra violation unit.
            #
            # Example:
            # allowed = 2
            # old_count = 2 -> new_count = 3
            # excess changes from max(0, 2-2)=0 to max(0, 3-2)=1, so +1
            #
            # Also works when allowed = 0:
            # old_count = 0 -> new_count = 1
            # excess changes from 0 to 1, so +1
            if old_count >= allowed:
                current_cost += 1

            # Actually add the element into the window frequency table.
            freq[word_id] = old_count + 1

            # If the window is too expensive, shrink it from the left until it becomes valid.
            while current_cost > B:
                left_word_id: int = words[left]
                left_allowed: int = limit.get(left_word_id, 0)
                left_count: int = freq[left_word_id]

                # We are about to remove one occurrence of left_word_id.
                #
                # If the current count is strictly greater than the allowed limit,
                # then this occurrence is contributing to the excess, so removing it
                # reduces total cost by 1.
                #
                # Example:
                # allowed = 2, left_count = 4
                # excess before = 2, after removal count becomes 3, excess becomes 1
                # so cost decreases by 1
                if left_count > left_allowed:
                    current_cost -= 1

                # Update the frequency after removing the leftmost element.
                new_left_count: int = left_count - 1
                if new_left_count == 0:
                    # Optional cleanup: remove keys with zero count to keep the
                    # dictionary smaller and easier to inspect.
                    del freq[left_word_id]
                else:
                    freq[left_word_id] = new_left_count

                # Move the left boundary rightward.
                left += 1

            # At this point, the current window [left, right] is valid:
            # current_cost <= B
            window_length: int = right - left + 1
            if window_length > best:
                best = window_length

        return best

    def longestTranslationStream(self, words: List[int], limit: Dict[int, int], B: int) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            words: List of normalized meaning IDs.
            limit: Dictionary mapping meaning ID -> allowed count.
            B: Maximum allowed total violation cost.

        Returns:
            The maximum valid contiguous segment length.

        Time complexity:
            O(n)

        Space complexity:
            O(k)
        """
        return self.longest_translation_stream(words, limit, B)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    words1: List[int] = [4, 1, 4, 2, 4, 1, 3]
    limit1: Dict[int, int] = {4: 2, 1: 1, 2: 1, 3: 1}
    B1: int = 1
    result1: int = solution.longest_translation_stream(words1, limit1, B1)
    print(result1)  # Expected: 5

    # Example 2
    words2: List[int] = [8, 8, 5, 8, 5, 6, 5, 5]
    limit2: Dict[int, int] = {8: 1, 5: 2, 6: 1}
    B2: int = 2
    result2: int = solution.longest_translation_stream(words2, limit2, B2)
    print(result2)  # Expected: 6