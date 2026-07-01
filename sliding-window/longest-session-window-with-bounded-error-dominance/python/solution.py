"""
Title: Longest Session Window With Bounded Error Dominance

Problem Description:
You are given an array `events` representing the event code recorded for each second
of an application session. Each `events[i]` is a positive integer. A contiguous window
of the session is called stable if no single event code is too dominant inside that
window. More formally, for a window `events[l..r]`, let `len = r - l + 1`, and let
`maxFreq` be the highest frequency of any event code inside the window. The window is
considered stable if `maxFreq * 2 <= len + k`, where `k` is a given non-negative
integer tolerance.

Your task is to return the length of the longest stable contiguous window.

This models a monitoring system where a session is suspicious if one error or action
code overwhelms the rest of the activity. The tolerance `k` allows a limited amount
of dominance before the window is rejected.

A correct solution is expected to handle large inputs efficiently. Naive recomputation
of frequencies for every subarray will time out.

Constraints:
- 1 <= events.length <= 2 * 10^5
- 1 <= events[i] <= 10^9
- 0 <= k <= events.length

Example 1:
Input: events = [4, 1, 4, 2, 4, 3, 2], k = 1
Output: 5

Example 2:
Input: events = [7, 7, 7, 2, 3, 7, 4, 5], k = 0
Output: 5
"""

from collections import defaultdict
from typing import DefaultDict, Dict, List


class FenwickTree:
    def __init__(self, size: int) -> None:
        """
        Fenwick Tree / Binary Indexed Tree supporting:
        - point updates
        - prefix sums
        - finding the largest index with positive count via suffix queries

        Args:
            size: Maximum index managed by the tree.

        Returns:
            None

        Time complexity:
            - Initialization: O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [0] * (size + 1)

    def add(self, index: int, delta: int) -> None:
        """
        Add `delta` to position `index`.

        Args:
            index: 1-based index to update.
            delta: Value to add.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        while index <= self.size:
            self.tree[index] += delta
            index += index & -index

    def prefix_sum(self, index: int) -> int:
        """
        Compute sum of values in range [1, index].

        Args:
            index: Right endpoint of prefix.

        Returns:
            Prefix sum.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        result: int = 0
        while index > 0:
            result += self.tree[index]
            index -= index & -index
        return result

    def total_sum(self) -> int:
        """
        Return the total sum stored in the tree.

        Args:
            None

        Returns:
            Total sum of all positions.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        return self.prefix_sum(self.size)

    def find_kth(self, k: int) -> int:
        """
        Find the smallest index `idx` such that prefix_sum(idx) >= k.

        This is a standard Fenwick Tree binary lifting operation.

        Args:
            k: 1-based order statistic.

        Returns:
            The smallest index whose prefix sum reaches at least k.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        idx: int = 0
        bit_mask: int = 1 << (self.size.bit_length() - 1)

        while bit_mask > 0:
            next_idx: int = idx + bit_mask
            if next_idx <= self.size and self.tree[next_idx] < k:
                k -= self.tree[next_idx]
                idx = next_idx
            bit_mask >>= 1

        return idx + 1

    def max_positive_index(self) -> int:
        """
        Return the largest index whose stored count is positive.

        We store, for each frequency f, how many distinct event codes currently have
        exactly frequency f inside the sliding window. Therefore, the largest index
        with positive count is exactly the current maximum frequency in the window.

        Args:
            None

        Returns:
            Largest index with positive count, or 0 if the structure is empty.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        total: int = self.total_sum()
        if total == 0:
            return 0
        return self.find_kth(total)


class Solution:
    def longest_stable_window(self, events: List[int], k: int) -> int:
        """
        Return the length of the longest contiguous stable window.

        A window is stable if:
            2 * maxFreq <= window_length + k

        We use a sliding window with exact frequency maintenance:
        - `freq[value]` stores how many times a value appears in the current window.
        - We also maintain `count_of_frequency[f]` indirectly using a Fenwick Tree:
          how many distinct values currently appear exactly `f` times.
        - The current maximum frequency is the largest `f` with positive count.

        This allows us to:
        - expand the right side of the window,
        - shrink the left side while the condition is violated,
        - always know the exact current maximum frequency efficiently.

        Args:
            events: List of positive integer event codes.
            k: Non-negative tolerance.

        Returns:
            Length of the longest stable contiguous window.

        Time complexity:
            O(n log n), where n = len(events)

        Space complexity:
            O(n)
        """
        n: int = len(events)

        # This dictionary stores the exact frequency of each event code
        # inside the current sliding window [left, right].
        freq: DefaultDict[int, int] = defaultdict(int)

        # The Fenwick Tree stores how many distinct event codes currently have
        # each possible frequency from 1 to n.
        #
        # Example:
        # If inside the current window:
        #   code 4 appears 3 times
        #   code 2 appears 2 times
        #   code 9 appears 2 times
        # then:
        #   count_of_frequency[3] = 1
        #   count_of_frequency[2] = 2
        #
        # The largest frequency with a positive count is the current maxFreq.
        bit: FenwickTree = FenwickTree(n)

        left: int = 0
        best: int = 0

        # We expand the window one element at a time using `right`.
        for right, value in enumerate(events):
            # -----------------------------
            # Step 1: Add events[right] into the window
            # -----------------------------
            #
            # Before increasing this value's frequency, if it already had some old
            # frequency `old_f > 0`, then one distinct code is leaving bucket old_f.
            old_f: int = freq[value]
            if old_f > 0:
                bit.add(old_f, -1)

            # Increase the actual frequency in the window.
            new_f: int = old_f + 1
            freq[value] = new_f

            # Now this code enters bucket new_f.
            bit.add(new_f, 1)

            # -----------------------------
            # Step 2: Shrink from the left while the window is invalid
            # -----------------------------
            #
            # The window [left, right] is stable if:
            #   2 * maxFreq <= window_length + k
            #
            # We maintain the exact maxFreq using the Fenwick Tree.
            while True:
                window_length: int = right - left + 1
                max_freq: int = bit.max_positive_index()

                # If the current window satisfies the rule, we stop shrinking.
                if 2 * max_freq <= window_length + k:
                    break

                # Otherwise, the window is too dominated by one event code,
                # so we must remove events[left] and move left forward.
                left_value: int = events[left]
                left_old_f: int = freq[left_value]

                # This code leaves its old frequency bucket.
                bit.add(left_old_f, -1)

                left_new_f: int = left_old_f - 1
                if left_new_f == 0:
                    # Frequency becomes zero, so remove the key entirely
                    # to keep the dictionary clean and easy to reason about.
                    del freq[left_value]
                else:
                    # Otherwise, update the dictionary and place the code into
                    # its new lower frequency bucket.
                    freq[left_value] = left_new_f
                    bit.add(left_new_f, 1)

                left += 1

            # -----------------------------
            # Step 3: Record the best valid window length seen so far
            # -----------------------------
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    events1: List[int] = [4, 1, 4, 2, 4, 3, 2]
    k1: int = 1
    result1: int = solution.longest_stable_window(events1, k1)
    print(result1)  # Expected: 5

    events2: List[int] = [7, 7, 7, 2, 3, 7, 4, 5]
    k2: int = 0
    result2: int = solution.longest_stable_window(events2, k2)
    print(result2)  # Expected: 5