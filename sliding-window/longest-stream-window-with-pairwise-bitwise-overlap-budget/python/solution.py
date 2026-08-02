"""
Title: Longest Stream Window With Pairwise Bitwise Overlap Budget

Problem Description:
You are given an array nums of length n, where each nums[i] is a non-negative integer
representing the feature mask of the i-th event in a real-time stream. Two events are
considered conflicting if their bitwise AND is non-zero, meaning they share at least one
enabled feature bit.

For any contiguous window nums[l..r], define its overlap cost as the total number of
conflicting pairs inside that window. In other words, for all pairs (i, j) such that
l <= i < j <= r, count 1 if (nums[i] & nums[j]) != 0, and 0 otherwise. The overlap cost
of the window is the sum of those counts.

Your task is to return the length of the longest contiguous window whose overlap cost is
at most k.

This problem is harder than a standard sliding window because adding one value may create
conflicts with many earlier values, and the number of conflicts depends on shared bits
across the whole window. A correct solution must efficiently maintain the number of
conflicting pairs while expanding and shrinking the window.

Constraints:
- 1 <= n <= 2 * 10^5
- 0 <= nums[i] < 2^20
- 0 <= k <= n * (n - 1) / 2
- nums may contain duplicates

Example 1:
Input: nums = [1, 2, 3, 8, 10], k = 2
Output: 4

Example 2:
Input: nums = [5, 1, 4, 2, 8, 3], k = 1
Output: 3
"""

from typing import Dict, List, Tuple


class Solution:
    def _enumerate_submasks(self, mask: int) -> List[int]:
        """
        Generate all non-zero submasks of a bitmask.

        This helper is used for inclusion-exclusion over the set bits of a number.
        Because nums[i] < 2^20, each number has at most 20 bits, and in many practical
        cases far fewer set bits. Enumerating submasks of the current value lets us count
        how many existing numbers in the window share at least one bit with it.

        Args:
            mask: The bitmask whose non-zero submasks should be generated.

        Returns:
            A list of all non-zero submasks of mask.

        Time complexity:
            O(2^b), where b is the number of set bits in mask.

        Space complexity:
            O(2^b) for the returned list.
        """
        submasks: List[int] = []
        sub: int = mask
        while sub:
            submasks.append(sub)
            sub = (sub - 1) & mask
        return submasks

    def _count_conflicts_with_window(
        self,
        value: int,
        subset_count: Dict[int, int],
    ) -> int:
        """
        Count how many numbers currently in the window conflict with `value`.

        A conflict means bitwise AND is non-zero. We compute this using inclusion-exclusion:
        for every non-zero submask `s` of `value`, if the number of bits in `s` is odd we add
        the count of window numbers containing all bits of `s`; if even we subtract it.

        Why this works:
        - Let A_b be the set of window numbers that contain bit b.
        - We want the size of the union of A_b over all bits b present in `value`.
        - Inclusion-exclusion gives exactly that union size.

        The dictionary `subset_count[s]` stores how many current window numbers are supersets
        of submask `s`, meaning those numbers contain every bit in `s`.

        Args:
            value: The new or outgoing number whose conflicts with the current window we want.
            subset_count: Maps a non-zero bitmask s to the number of window elements that
                contain all bits of s.

        Returns:
            The number of current window elements that conflict with `value`.

        Time complexity:
            O(2^b), where b is the number of set bits in value.

        Space complexity:
            O(1) auxiliary, excluding the dictionary passed in.
        """
        conflicts: int = 0

        # Enumerate every non-zero subset of bits from `value`.
        # For each subset, inclusion-exclusion tells us whether to add or subtract.
        sub: int = value
        while sub:
            # bit_count() is available in modern Python 3 and efficiently counts set bits.
            if sub.bit_count() % 2 == 1:
                conflicts += subset_count.get(sub, 0)
            else:
                conflicts -= subset_count.get(sub, 0)
            sub = (sub - 1) & value

        return conflicts

    def _add_value(self, value: int, subset_count: Dict[int, int]) -> None:
        """
        Add one value into the window bookkeeping structure.

        For every non-zero submask of `value`, increment how many window numbers contain
        that submask.

        Args:
            value: The number being inserted into the current window.
            subset_count: Dictionary storing counts for all non-zero submasks.

        Returns:
            None.

        Time complexity:
            O(2^b), where b is the number of set bits in value.

        Space complexity:
            O(1) auxiliary, excluding dictionary growth.
        """
        sub: int = value
        while sub:
            subset_count[sub] = subset_count.get(sub, 0) + 1
            sub = (sub - 1) & value

    def _remove_value(self, value: int, subset_count: Dict[int, int]) -> None:
        """
        Remove one value from the window bookkeeping structure.

        For every non-zero submask of `value`, decrement how many window numbers contain
        that submask.

        Args:
            value: The number being removed from the current window.
            subset_count: Dictionary storing counts for all non-zero submasks.

        Returns:
            None.

        Time complexity:
            O(2^b), where b is the number of set bits in value.

        Space complexity:
            O(1) auxiliary.
        """
        sub: int = value
        while sub:
            new_count: int = subset_count[sub] - 1
            if new_count == 0:
                del subset_count[sub]
            else:
                subset_count[sub] = new_count
            sub = (sub - 1) & value

    def longest_window_with_overlap_budget(self, nums: List[int], k: int) -> int:
        """
        Return the maximum length of a contiguous subarray whose overlap cost is at most k.

        Core idea:
        We use a sliding window with two pointers:
        - Expand the right pointer one step at a time.
        - When adding nums[right], compute how many existing window elements conflict with it.
          That number is exactly how much the total pairwise overlap cost increases.
        - If the cost becomes too large, move the left pointer rightward, removing elements
          and subtracting the number of conflicts each removed element had with the rest of
          the current window.

        The difficult part is efficiently counting "how many current window numbers share at
        least one bit with x". We solve that with inclusion-exclusion over submasks:
        - Maintain counts for every non-zero submask s:
              subset_count[s] = number of window values that contain all bits of s
        - Then the number of window values that share at least one bit with x is:
              sum over non-zero submasks s of x:
                  (+ subset_count[s]) if popcount(s) is odd
                  (- subset_count[s]) if popcount(s) is even

        This is exact and avoids double-counting numbers that share multiple bits.

        Args:
            nums: List of non-negative integers representing feature masks.
            k: Maximum allowed number of conflicting pairs inside the window.

        Returns:
            The maximum valid window length.

        Time complexity:
            O(sum(2^popcount(nums[i]))) over all insertions and removals.
            Since nums[i] < 2^20, each value has at most 20 set bits.
            In the worst case this is O(n * 2^20), but with the given bit limit and typical
            sparse masks this approach is practical and exact.

        Space complexity:
            O(M), where M is the number of distinct non-zero submasks currently stored.
        """
        # This dictionary is the heart of the solution.
        #
        # Meaning:
        # subset_count[s] = how many numbers currently inside the sliding window contain
        # every bit that appears in submask `s`.
        #
        # Example:
        # If the window contains [3, 7] = [0b011, 0b111], then:
        # - subset_count[1] counts numbers containing bit 0
        # - subset_count[2] counts numbers containing bit 1
        # - subset_count[3] counts numbers containing both bits 0 and 1
        #
        # With these counts, inclusion-exclusion can tell us exactly how many window numbers
        # overlap with a new value in at least one bit.
        subset_count: Dict[int, int] = {}

        # Standard sliding window left boundary.
        left: int = 0

        # Current overlap cost of the window nums[left:right+1].
        # This is the total number of conflicting pairs currently inside the window.
        current_cost: int = 0

        # Best answer found so far.
        best: int = 0

        # Expand the window one element at a time.
        for right, value in enumerate(nums):
            # Step 1: determine how many existing window elements conflict with `value`.
            #
            # Every such conflict creates exactly one new pair involving the new rightmost
            # element, so the overlap cost increases by exactly this amount.
            added_conflicts: int = self._count_conflicts_with_window(value, subset_count)
            current_cost += added_conflicts

            # Step 2: now that we have accounted for the new pairs, actually insert `value`
            # into the bookkeeping structure so future elements can see it.
            self._add_value(value, subset_count)

            # Step 3: if the window became invalid (cost > k), shrink from the left until
            # it becomes valid again.
            #
            # Important subtlety when removing nums[left]:
            # - First remove it from subset_count, so the structure represents "the rest of
            #   the window" after removal.
            # - Then count how many conflicts the removed value had with that remaining window.
            #   Those are exactly the pairs that disappear from current_cost.
            while current_cost > k:
                outgoing: int = nums[left]

                # Remove outgoing from the data structure first.
                self._remove_value(outgoing, subset_count)

                # Count how many remaining window elements conflict with outgoing.
                # Since outgoing has already been removed from subset_count, this count refers
                # only to the other elements still in the window, which is exactly what we need
                # to subtract from the pair count.
                removed_conflicts: int = self._count_conflicts_with_window(
                    outgoing,
                    subset_count,
                )
                current_cost -= removed_conflicts

                # Move left boundary forward.
                left += 1

            # Step 4: the current window is valid, so update the best length.
            window_length: int = right - left + 1
            if window_length > best:
                best = window_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    nums1: List[int] = [1, 2, 3, 8, 10]
    k1: int = 2
    result1: int = solution.longest_window_with_overlap_budget(nums1, k1)
    print(result1)  # Expected: 4

    # Example 2
    nums2: List[int] = [5, 1, 4, 2, 8, 3]
    k2: int = 1
    result2: int = solution.longest_window_with_overlap_budget(nums2, k2)
    print(result2)  # Expected: 3

    # Additional quick sanity checks
    nums3: List[int] = [0, 0, 0]
    k3: int = 0
    result3: int = solution.longest_window_with_overlap_budget(nums3, k3)
    print(result3)  # Expected: 3

    nums4: List[int] = [1, 1, 1]
    k4: int = 1
    result4: int = solution.longest_window_with_overlap_budget(nums4, k4)
    print(result4)  # Expected: 2