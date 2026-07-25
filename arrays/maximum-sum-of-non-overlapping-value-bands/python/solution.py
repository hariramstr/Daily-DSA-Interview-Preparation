"""
Title: Maximum Sum of Non-Overlapping Value Bands

Problem Description:
You are given an integer array nums and an integer k. A value band is a contiguous
subarray nums[l..r] such that the difference between the maximum and minimum value
inside that subarray is at most k. You may choose any number of value bands, but no
two chosen bands may overlap. The score of a chosen band is the sum of its elements.
Your task is to return the maximum total score obtainable by selecting a set of
non-overlapping value bands.

A band of length 1 is always valid. You are allowed to skip elements entirely if doing
so increases the total score. Note that even if a subarray satisfies the value-band
condition, it may be better not to take it if its sum is negative or if taking smaller
bands leads to a larger total.

Design an algorithm that works efficiently for large inputs.

Constraints:
- 1 <= nums.length <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- 0 <= k <= 10^9
- The answer fits in a signed 64-bit integer.
"""

from collections import deque
from typing import List


class SegmentTreeMax:
    """Segment tree supporting point updates with max and range maximum queries."""

    def __init__(self, size: int) -> None:
        """
        Initialize a max segment tree.

        Args:
            size: Number of positions to store.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.n: int = 1
        while self.n < size:
            self.n <<= 1
        self.tree: List[int] = [-(10**30)] * (2 * self.n)

    def update(self, index: int, value: int) -> None:
        """
        Set tree[index] = max(tree[index], value).

        Args:
            index: Position to update.
            value: Candidate value.

        Returns:
            None

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        i: int = index + self.n
        if value <= self.tree[i]:
            return
        self.tree[i] = value
        i //= 2
        while i >= 1:
            left: int = 2 * i
            right: int = left + 1
            new_value: int = self.tree[left] if self.tree[left] >= self.tree[right] else self.tree[right]
            if new_value == self.tree[i]:
                break
            self.tree[i] = new_value
            i //= 2

    def query(self, left: int, right: int) -> int:
        """
        Query maximum value in inclusive range [left, right].

        Args:
            left: Left boundary.
            right: Right boundary.

        Returns:
            Maximum value in the range. If left > right, returns a very small number.

        Time complexity:
            O(log n)

        Space complexity:
            O(1) extra
        """
        if left > right:
            return -(10**30)

        left += self.n
        right += self.n
        result: int = -(10**30)

        while left <= right:
            if left % 2 == 1:
                if self.tree[left] > result:
                    result = self.tree[left]
                left += 1
            if right % 2 == 0:
                if self.tree[right] > result:
                    result = self.tree[right]
                right -= 1
            left //= 2
            right //= 2

        return result


class Solution:
    def max_non_overlapping_value_bands(self, nums: List[int], k: int) -> int:
        """
        Compute the maximum total sum of non-overlapping valid value bands.

        A valid band is a contiguous subarray whose max value minus min value is at most k.
        We may choose any number of non-overlapping valid bands, and skipping elements is allowed.

        Args:
            nums: Input integer array.
            k: Maximum allowed difference between max and min inside a chosen band.

        Returns:
            Maximum obtainable total score.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        n: int = len(nums)

        # Prefix sums let us compute any subarray sum quickly:
        # sum(nums[l..r]) = prefix[r + 1] - prefix[l]
        prefix: List[int] = [0] * (n + 1)
        for i in range(n):
            prefix[i + 1] = prefix[i] + nums[i]

        # dp[i] will mean:
        # maximum score obtainable using only the first i elements,
        # i.e. considering nums[0..i-1].
        #
        # Therefore:
        # - dp[0] = 0 (no elements, no score)
        # - answer = dp[n]
        dp: List[int] = [0] * (n + 1)

        # We maintain a sliding window [left..right] that is always valid:
        # max(nums[left..right]) - min(nums[left..right]) <= k
        #
        # To support this efficiently, we use:
        # - max_deque: decreasing deque of indices, front is current maximum
        # - min_deque: increasing deque of indices, front is current minimum
        max_deque: deque[int] = deque()
        min_deque: deque[int] = deque()
        left: int = 0

        # Key DP transformation:
        #
        # If we choose a band [j..r], then total score is:
        #   dp[j] + sum(j..r)
        # = dp[j] + prefix[r+1] - prefix[j]
        # = prefix[r+1] + (dp[j] - prefix[j])
        #
        # So for a fixed right endpoint r, among all valid starts j in the current
        # valid range [left..r], we want the maximum of:
        #   dp[j] - prefix[j]
        #
        # We need to query that maximum over a changing interval [left..r].
        # A segment tree over indices j stores values (dp[j] - prefix[j]).
        seg: SegmentTreeMax = SegmentTreeMax(n + 1)

        # Initially, j = 0 is available before processing any element.
        seg.update(0, dp[0] - prefix[0])

        for right in range(n):
            current_value: int = nums[right]

            # ------------------------------------------------------------
            # Step 1: Insert nums[right] into the monotonic deques.
            #
            # max_deque keeps values in decreasing order.
            # Any smaller value at the back can never become the maximum
            # while current_value remains in the window, so we remove it.
            # ------------------------------------------------------------
            while max_deque and nums[max_deque[-1]] <= current_value:
                max_deque.pop()
            max_deque.append(right)

            # ------------------------------------------------------------
            # min_deque keeps values in increasing order.
            # Any larger value at the back can never become the minimum
            # while current_value remains in the window, so we remove it.
            # ------------------------------------------------------------
            while min_deque and nums[min_deque[-1]] >= current_value:
                min_deque.pop()
            min_deque.append(right)

            # ------------------------------------------------------------
            # Step 2: Shrink the left boundary until the window becomes valid.
            #
            # The window is invalid if current max - current min > k.
            # When moving left forward, we also remove outdated indices
            # from the fronts of the deques.
            # ------------------------------------------------------------
            while nums[max_deque[0]] - nums[min_deque[0]] > k:
                if max_deque[0] == left:
                    max_deque.popleft()
                if min_deque[0] == left:
                    min_deque.popleft()
                left += 1

            # ------------------------------------------------------------
            # At this point, every subarray [j..right] with j >= left is valid,
            # and every subarray [j..right] with j < left is invalid.
            #
            # So the best band ending at 'right' is:
            #   prefix[right+1] + max(dp[j] - prefix[j]) for j in [left..right]
            #
            # Note:
            # j can be right as well, corresponding to a length-1 band.
            # We do NOT use j = right+1 because that would be an empty band.
            # ------------------------------------------------------------
            best_start_value: int = seg.query(left, right)
            best_band_ending_here: int = prefix[right + 1] + best_start_value

            # ------------------------------------------------------------
            # Step 3: Standard interval DP transition.
            #
            # Option A: skip nums[right]
            #   dp[right + 1] = dp[right]
            #
            # Option B: take some valid band ending at right
            #   dp[right + 1] = best_band_ending_here
            #
            # We choose the better one.
            # ------------------------------------------------------------
            dp[right + 1] = dp[right]
            if best_band_ending_here > dp[right + 1]:
                dp[right + 1] = best_band_ending_here

            # ------------------------------------------------------------
            # Step 4: Make index (right + 1) available as a future band start.
            #
            # For future endpoints, if a band starts at j = right + 1,
            # the needed stored value is:
            #   dp[j] - prefix[j]
            # = dp[right + 1] - prefix[right + 1]
            # ------------------------------------------------------------
            seg.update(right + 1, dp[right + 1] - prefix[right + 1])

        return dp[n]


if __name__ == "__main__":
    solution = Solution()

    nums1 = [4, 2, 3, 7, 6, 5]
    k1 = 2
    result1 = solution.max_non_overlapping_value_bands(nums1, k1)
    print(result1)  # Expected: 27

    nums2 = [5, -4, 6, 6, -2, 7]
    k2 = 1
    result2 = solution.max_non_overlapping_value_bands(nums2, k2)
    print(result2)  # Expected: 24