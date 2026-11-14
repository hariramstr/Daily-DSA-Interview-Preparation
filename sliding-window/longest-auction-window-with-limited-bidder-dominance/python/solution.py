"""
Title: Longest Auction Window With Limited Bidder Dominance
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given an array bids where bids[i] is the bidder ID that placed the i-th bid
in a live auction feed, and an integer limit. A contiguous window of bids is called
fair if no single bidder appears more than limit times more than all other bidders
combined inside that window.

Formally, for a window of length L, if the maximum frequency of any bidder in the
window is maxFreq, then the window is fair when:

    maxFreq <= (L - maxFreq) + limit

Your task is to return the length of the longest fair contiguous window.

This models a fraud-detection rule for auction systems: a bidder may be active, but
cannot dominate the stream too heavily relative to everyone else unless the configured
tolerance limit allows it.

A valid solution is expected to run efficiently on large inputs. Since bidder IDs may
be large integers, your algorithm should not assume a small fixed value range.

Constraints:
- 1 <= bids.length <= 200000
- 1 <= bids[i] <= 10^9
- 0 <= limit <= bids.length
- The answer must be computed for a contiguous subarray

Examples:
1) bids = [4, 7, 4, 4, 8, 7, 4, 9], limit = 1
   Output: 5

2) bids = [5, 5, 5, 2, 3, 5, 2, 2, 2], limit = 0
   Output: 6
"""

from collections import defaultdict
from typing import DefaultDict, Dict, List


class MaxFrequencyTracker:
    """
    Helper data structure that supports:
    - increasing/decreasing the count of a bidder inside the current window
    - querying the current maximum bidder frequency in O(1)

    We maintain:
    1. bidder_count[bidder] = current frequency of that bidder in the window
    2. freq_count[f] = how many bidders currently appear exactly f times
    3. current_max_freq = largest frequency that currently exists in the window

    This avoids expensive rescans of all bidder counts whenever the window changes.

    Time complexity:
        Each add/remove is amortized O(1)
    Space complexity:
        O(number of distinct bidders in the current window)
    """

    def __init__(self) -> None:
        """
        Initialize empty tracking structures.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.bidder_count: DefaultDict[int, int] = defaultdict(int)
        self.freq_count: DefaultDict[int, int] = defaultdict(int)
        self.current_max_freq: int = 0

    def add(self, bidder: int) -> None:
        """
        Add one occurrence of a bidder to the current window.

        Args:
            bidder: The bidder ID being added.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1) additional
        """
        old_freq: int = self.bidder_count[bidder]
        new_freq: int = old_freq + 1

        # If the bidder already had a positive frequency, one bidder is leaving
        # the "old_freq" bucket.
        if old_freq > 0:
            self.freq_count[old_freq] -= 1

        # Update the bidder's actual count.
        self.bidder_count[bidder] = new_freq

        # One bidder now belongs to the "new_freq" bucket.
        self.freq_count[new_freq] += 1

        # If this bidder reached a new highest frequency, update the tracker.
        if new_freq > self.current_max_freq:
            self.current_max_freq = new_freq

    def remove(self, bidder: int) -> None:
        """
        Remove one occurrence of a bidder from the current window.

        Args:
            bidder: The bidder ID being removed.

        Returns:
            None

        Time complexity:
            O(1) amortized

        Space complexity:
            O(1) additional
        """
        old_freq: int = self.bidder_count[bidder]
        new_freq: int = old_freq - 1

        # The bidder leaves the old frequency bucket.
        self.freq_count[old_freq] -= 1

        # If the new frequency is zero, remove the bidder entirely from the map.
        # Otherwise, place the bidder into the new frequency bucket.
        if new_freq == 0:
            del self.bidder_count[bidder]
        else:
            self.bidder_count[bidder] = new_freq
            self.freq_count[new_freq] += 1

        # If the highest frequency bucket became empty, move current_max_freq down
        # until it points to a frequency that still exists.
        while self.current_max_freq > 0 and self.freq_count[self.current_max_freq] == 0:
            self.current_max_freq -= 1

    def max_frequency(self) -> int:
        """
        Return the current maximum bidder frequency in the window.

        Args:
            None

        Returns:
            The largest frequency among all bidders currently in the window.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        return self.current_max_freq


class Solution:
    def longest_fair_window(self, bids: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous fair window.

        A window is fair if:
            maxFreq <= (window_length - maxFreq) + limit

        Rearranging:
            2 * maxFreq <= window_length + limit

        We use a classic sliding window:
        - Expand the right end one bid at a time.
        - Track frequencies and the current maximum frequency.
        - If the window becomes unfair, shrink from the left until it becomes fair again.
        - Record the maximum valid window length seen.

        Why sliding window works:
        - The condition depends only on the current contiguous window.
        - When adding a new element makes the window invalid, removing elements from the
          left is the natural way to restore validity while preserving contiguity.
        - Every index enters and leaves the window at most once, giving linear behavior.

        Args:
            bids: List of bidder IDs.
            limit: Allowed dominance tolerance.

        Returns:
            The maximum length of a fair contiguous subarray.

        Time complexity:
            O(n), where n is len(bids), because each element is added once and removed once.

        Space complexity:
            O(k), where k is the number of distinct bidder IDs in the current window
            (and at most the number of distinct IDs in the whole array).
        """
        # This tracker gives us exact current frequencies and exact current maximum
        # frequency in the active window.
        tracker: MaxFrequencyTracker = MaxFrequencyTracker()

        # "left" is the start index of the current sliding window.
        left: int = 0

        # "best" stores the longest fair window length found so far.
        best: int = 0

        # Move "right" from left to right across the array, expanding the window.
        for right, bidder in enumerate(bids):
            # Step 1: include the new rightmost bidder in the window.
            tracker.add(bidder)

            # Step 2: while the window is unfair, shrink it from the left.
            #
            # Current window length is:
            #   window_len = right - left + 1
            #
            # Fairness condition:
            #   2 * maxFreq <= window_len + limit
            #
            # If this is violated, some bidder dominates too much, so we must remove
            # bids from the left until the condition becomes true again.
            while 2 * tracker.max_frequency() > (right - left + 1) + limit:
                tracker.remove(bids[left])
                left += 1

            # Step 3: now the window [left, right] is guaranteed fair.
            # Update the best answer.
            current_len: int = right - left + 1
            if current_len > best:
                best = current_len

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    bids1: List[int] = [4, 7, 4, 4, 8, 7, 4, 9]
    limit1: int = 1
    result1: int = solution.longest_fair_window(bids1, limit1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2 from the prompt.
    bids2: List[int] = [5, 5, 5, 2, 3, 5, 2, 2, 2]
    limit2: int = 0
    result2: int = solution.longest_fair_window(bids2, limit2)
    print("Example 2 Result:", result2)  # Expected: 6

    # A few additional sanity checks.
    bids3: List[int] = [1]
    limit3: int = 0
    result3: int = solution.longest_fair_window(bids3, limit3)
    print("Single element Result:", result3)  # Expected: 0 or 1? Check fairness:
    # For [1], maxFreq = 1, L = 1, condition is 1 <= 0 + 0 -> false, so expected 0.
    # However, the problem constraints and examples imply we seek the longest fair window,
    # and empty window is not considered as a subarray answer in most formulations.
    # Since the algorithm only considers non-empty windows, this case depends on whether
    # a fair window must be non-empty. The original examples avoid this ambiguity.
    # We print the algorithm's result for visibility.

    bids4: List[int] = [1, 2, 3, 4]
    limit4: int = 0
    result4: int = solution.longest_fair_window(bids4, limit4)
    print("All distinct Result:", result4)  # Expected: 4