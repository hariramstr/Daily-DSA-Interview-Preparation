"""
Title: Minimum XOR Merges to Isolate a Signature
Difficulty: Hard
Topic: Bit Manipulation

Problem Description:
You are given an array nums of n non-negative integers representing packet signatures.
In one operation, you may choose any adjacent pair nums[i] and nums[i+1], remove both
values, and replace them with a single value equal to their bitwise XOR. This reduces
the array length by 1. You may repeat this operation any number of times until only one
value remains, or stop earlier.

Your task is to find the minimum number of merge operations required so that the value x
appears somewhere in the array at least once after performing the operations. If it is
impossible, return -1.

A merge can only be performed on adjacent elements, and each merge changes the array
structure, so the order of remaining segments must always be consistent with the original
order. Equivalently, after several merges, every remaining element corresponds to the XOR
of some contiguous subarray of the original array.

Return the minimum number of merges needed to make at least one remaining segment have XOR
exactly x.

Constraints:
- 1 <= n <= 200000
- 0 <= nums[i] < 2^30
- 0 <= x < 2^30
- The solution is expected to be better than O(n^2).

Key Observation:
If a contiguous subarray has XOR equal to x and its length is L, then we can merge that
subarray into one segment in exactly L - 1 operations. Therefore, the problem becomes:
find the shortest contiguous subarray whose XOR is x. The answer is then:
    shortest_length - 1
If no such subarray exists, return -1.
"""

from typing import Dict, List


class Solution:
    def min_xor_merges(self, nums: List[int], x: int) -> int:
        """
        Compute the minimum number of adjacent XOR merge operations needed so that
        some remaining segment has value exactly x.

        The problem reduces to finding the shortest contiguous subarray whose XOR is x.
        If such a subarray has length L, then merging it into one segment takes exactly
        L - 1 operations.

        Args:
            nums: List of non-negative integers.
            x: Target XOR value.

        Returns:
            The minimum number of merge operations required, or -1 if impossible.

        Time complexity:
            O(n), where n is the length of nums.

        Space complexity:
            O(n) in the worst case for the prefix XOR map.
        """
        # We use the standard prefix XOR technique.
        #
        # Let prefix[i] represent XOR of nums[0] through nums[i - 1].
        # Then XOR of subarray nums[l..r] is:
        #     prefix[r + 1] ^ prefix[l]
        #
        # We want this subarray XOR to equal x:
        #     prefix[r + 1] ^ prefix[l] = x
        #
        # Rearranging:
        #     prefix[l] = prefix[r + 1] ^ x
        #
        # So while scanning from left to right, for each current prefix value,
        # we want to know whether we have seen the needed earlier prefix value.
        #
        # Since we want the SHORTEST subarray, for a fixed right endpoint we should
        # pair it with the LATEST possible matching left endpoint. That means:
        # for each prefix XOR value, we store its most recent index.
        #
        # Why "most recent" and not "first"?
        # - If we stored the first occurrence, we would get the longest subarray
        #   ending here among matches.
        # - But we need the shortest subarray, so we want the largest l.
        #
        # Prefix indexing convention:
        # - Before reading any element, prefix XOR is 0 at index 0.
        # - After processing nums[i], we are at prefix index i + 1.
        #
        # If a subarray runs from l to r inclusive, then its length is:
        #     (r - l + 1) = (r + 1) - l = current_prefix_index - previous_prefix_index

        n: int = len(nums)

        # latest_index maps a prefix XOR value to the latest prefix index where it appeared.
        # Example:
        # latest_index[0] = 0 means before processing any elements, prefix XOR 0 occurs at index 0.
        latest_index: Dict[int, int] = {0: 0}

        # Running prefix XOR as we scan the array.
        prefix_xor: int = 0

        # Track the shortest valid subarray length found so far.
        # Start with a value larger than any possible subarray length.
        shortest_length: int = n + 1

        # Iterate through the array.
        for i, value in enumerate(nums):
            # Update the running prefix XOR to include the current element.
            prefix_xor ^= value

            # Current prefix index is i + 1 because prefix[0] is the empty prefix.
            current_prefix_index: int = i + 1

            # We need an earlier prefix value such that:
            # earlier_prefix ^ current_prefix = x
            # so earlier_prefix = current_prefix ^ x
            needed_prefix: int = prefix_xor ^ x

            # If that needed prefix has been seen before, then we found a subarray
            # ending at index i whose XOR is exactly x.
            if needed_prefix in latest_index:
                previous_prefix_index: int = latest_index[needed_prefix]

                # Length of the subarray is the difference between prefix indices.
                current_length: int = current_prefix_index - previous_prefix_index

                # Keep the shortest one found.
                if current_length < shortest_length:
                    shortest_length = current_length

            # Update the latest occurrence of the current prefix XOR.
            # We do this AFTER checking, because the earlier prefix must come before
            # the current position. Storing now ensures future positions can use it.
            latest_index[prefix_xor] = current_prefix_index

        # If we never found any subarray with XOR x, answer is impossible.
        if shortest_length == n + 1:
            return -1

        # A subarray of length L can be merged into one segment in exactly L - 1 merges.
        return shortest_length - 1


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # nums = [5, 1, 4, 1], x = 4
    # Shortest subarray with XOR 4 is [5, 1], length 2 -> 1 merge
    nums1: List[int] = [5, 1, 4, 1]
    x1: int = 4
    result1: int = solution.min_xor_merges(nums1, x1)
    print(f"nums = {nums1}, x = {x1} -> {result1}")  # Expected: 1

    # Example 2:
    # nums = [2, 7, 2, 7], x = 0
    # XOR of entire array is 0, length 4 -> 3 merges
    # No shorter subarray has XOR 0
    nums2: List[int] = [2, 7, 2, 7]
    x2: int = 0
    result2: int = solution.min_xor_merges(nums2, x2)
    print(f"nums = {nums2}, x = {x2} -> {result2}")  # Expected: 3

    # Additional sanity checks:

    # Already contains x, so answer should be 0.
    nums3: List[int] = [8, 3, 6]
    x3: int = 3
    result3: int = solution.min_xor_merges(nums3, x3)
    print(f"nums = {nums3}, x = {x3} -> {result3}")  # Expected: 0

    # Impossible case.
    nums4: List[int] = [1, 2, 4]
    x4: int = 7
    result4: int = solution.min_xor_merges(nums4, x4)
    print(f"nums = {nums4}, x = {x4} -> {result4}")  # Expected: -1

    # Entire array needed.
    nums5: List[int] = [1, 2, 3]
    x5: int = 0
    result5: int = solution.min_xor_merges(nums5, x5)
    print(f"nums = {nums5}, x = {x5} -> {result5}")  # Expected: 2