"""
Title: Find the First Neighbor Swap That Sorts a Line

Problem Description:
You are given an integer array nums representing the priority values of items standing in a line.
You may perform at most one operation: choose an index i and swap nums[i] with nums[i + 1],
meaning only neighboring items can be swapped.

Your task is to find the smallest index i such that performing this single adjacent swap makes
the entire array sorted in non-decreasing order.

If the array is already sorted, return -1.
If no single adjacent swap can sort the array, also return -1.

Return the index of the left element in the swap.

An array is considered sorted in non-decreasing order if nums[j] <= nums[j + 1] for every valid j.

This problem is meant to test careful array scanning and boundary checking.
A correct solution should avoid trying every possible swap when unnecessary and should correctly
handle duplicates.

Constraints:
- 1 <= nums.length <= 100000
- -1000000000 <= nums[i] <= 1000000000

Example 1:
Input: nums = [1, 3, 2, 4]
Output: 1
Explanation: Swapping nums[1] and nums[2] gives [1, 2, 3, 4], which is sorted.

Example 2:
Input: nums = [1, 5, 3, 4, 2]
Output: -1
Explanation: No single swap of neighboring elements can make the full array sorted.

Notes:
- If nums = [1, 2, 2, 3], the answer is -1 because the array is already sorted.
- If multiple adjacent swaps could sort the array, return the smallest valid index.
"""

from typing import List


class Solution:
    def _is_sorted_after_swap(self, nums: List[int], i: int, bad_positions: List[int]) -> bool:
        """
        Check whether swapping nums[i] and nums[i + 1] would make the full array sorted.

        Instead of rebuilding the whole array or checking every pair again, we use an important
        observation: swapping neighboring elements only changes comparisons near that swap.
        Therefore, only a very small set of adjacent positions can change their sorted/not-sorted
        status.

        Args:
            nums: Original array of integers.
            i: Index of the left element in the adjacent swap.
            bad_positions: List of indices j where nums[j] > nums[j + 1] in the original array.

        Returns:
            True if swapping nums[i] and nums[i + 1] makes the array sorted, otherwise False.

        Time complexity:
            O(k), where k is the number of originally bad positions plus a constant amount of
            local checking. In practice this is O(1) after the early pruning used by the main
            method, because only a few positions matter.

        Space complexity:
            O(1) extra space, ignoring the input list.
        """
        n: int = len(nums)

        # The only adjacent comparisons that can possibly change after swapping positions i and i+1
        # are:
        #   - pair ending at i-1: (i-1, i)
        #   - pair at i:          (i, i+1)
        #   - pair starting at i+1: (i+1, i+2)
        #
        # Every other pair uses exactly the same values in exactly the same order as before.
        affected = {i - 1, i, i + 1}

        # If there exists any originally bad position outside the affected area, then that bad pair
        # will remain bad after the swap, because the swap does not touch it. In that case sorting
        # the whole array is impossible with this swap.
        for pos in bad_positions:
            if pos not in affected:
                return False

        # To test the affected comparisons, we need a way to read what value would appear at each
        # index after the swap, without actually modifying the array.
        def value_after_swap(index: int) -> int:
            """
            Return the value that would be at 'index' after swapping nums[i] and nums[i + 1].
            """
            if index == i:
                return nums[i + 1]
            if index == i + 1:
                return nums[i]
            return nums[index]

        # Now verify that every affected adjacent pair is sorted after the swap.
        # We only need to check valid pair starts in the range [0, n - 2].
        for pos in (i - 1, i, i + 1):
            if 0 <= pos < n - 1:
                if value_after_swap(pos) > value_after_swap(pos + 1):
                    return False

        # If all unaffected bad positions were absent and all affected pairs are now sorted,
        # then the entire array is sorted after this swap.
        return True

    def first_neighbor_swap_to_sort(self, nums: List[int]) -> int:
        """
        Find the smallest index i such that swapping nums[i] and nums[i + 1] sorts the array.

        The algorithm first scans the array to find all positions where the non-decreasing order
        is violated, meaning nums[j] > nums[j + 1]. These are the only places that are currently
        "wrong".

        Key insight:
        A single adjacent swap only changes local comparisons near the swap position. Therefore,
        if the array can be fixed by one adjacent swap, the swap must happen near the first bad
        position. In fact, only two candidate swap indices need to be checked:
            - the first bad position itself
            - the position immediately before it

        Why?
        Because to remove the first inversion nums[p] > nums[p + 1], the swap must involve one of
        those two elements. The only adjacent swaps that touch them are:
            - swap at p     -> swaps nums[p] and nums[p + 1]
            - swap at p - 1 -> swaps nums[p - 1] and nums[p], which may move nums[p] left

        This gives an O(n) solution instead of trying every possible adjacent swap.

        Args:
            nums: List of integers.

        Returns:
            The smallest valid swap index, or -1 if the array is already sorted or cannot be
            sorted by one adjacent swap.

        Time complexity:
            O(n), because we scan once to collect inversions and then perform only constant-number
            candidate checks.

        Space complexity:
            O(n) in the worst case for storing bad positions, though only O(number of inversions).
        """
        n: int = len(nums)

        # Arrays of length 0 or 1 are already sorted by definition.
        # The constraints start at length 1, but this check keeps the method robust and clear.
        if n <= 1:
            return -1

        # Step 1: Find every position j where the sorted order is broken:
        # nums[j] > nums[j + 1]
        #
        # We store these indices in bad_positions.
        # Example:
        #   nums = [1, 3, 2, 4]
        #   bad_positions = [1] because 3 > 2
        #
        # This list tells us exactly where the array is currently not sorted.
        bad_positions: List[int] = []
        for j in range(n - 1):
            if nums[j] > nums[j + 1]:
                bad_positions.append(j)

        # If there are no bad positions, the array is already sorted.
        # The problem explicitly says to return -1 in that case.
        if not bad_positions:
            return -1

        # Step 2: Determine the only meaningful candidate swap indices.
        #
        # Let p be the first bad position.
        # To fix the earliest place where sorting fails, the swap must affect index p or p+1.
        # The only adjacent swaps that do that are:
        #   - swap at p-1
        #   - swap at p
        #
        # We must return the smallest valid index, so we test p-1 first (if valid), then p.
        p: int = bad_positions[0]
        candidates: List[int] = []

        if p - 1 >= 0:
            candidates.append(p - 1)
        candidates.append(p)

        # Step 3: Test candidates in increasing order.
        #
        # The helper method performs a local verification:
        #   - Any bad position outside the swap's neighborhood means failure immediately.
        #   - Then it checks the few adjacent pairs that could change.
        #
        # The first candidate that works is the answer, because we test in increasing order.
        for i in candidates:
            if self._is_sorted_after_swap(nums, i, bad_positions):
                return i

        # If neither candidate works, then no single adjacent swap can sort the array.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Sample inputs from the problem statement and a few extra checks.
    test_cases: List[List[int]] = [
        [1, 3, 2, 4],      # Expected: 1
        [1, 5, 3, 4, 2],   # Expected: -1
        [1, 2, 2, 3],      # Expected: -1 (already sorted)
        [2, 1],            # Expected: 0
        [1, 2, 4, 3, 5],   # Expected: 2
        [3, 1, 2],         # Expected: 0 -> swap 3 and 1 => [1,3,2] not sorted, so actually -1
        [2, 3, 1],         # Expected: -1
        [1],               # Expected: -1
        [1, 1, 0, 1],      # Expected: 1 -> swap 1 and 0 => [1,0,1,1] not sorted, so -1
        [1, 0, 1, 1],      # Expected: 0
    ]

    for nums in test_cases:
        result = solution.first_neighbor_swap_to_sort(nums)
        print(f"nums = {nums} -> {result}")