"""
Title: Shortest Segment With Target XOR

Problem Description:
You are given an array `nums` of non-negative integers and an integer `target`.
A contiguous segment of the array is called valid if the bitwise XOR of all values
in that segment is exactly equal to `target`.

Return the length of the shortest valid segment. If no such segment exists, return `-1`.

A segment must contain at least one element. The XOR of a segment `nums[l..r]` is defined as:
nums[l] ^ nums[l+1] ^ ... ^ nums[r]

This problem is intended to test your ability to combine prefix ideas with bit
manipulation. A brute-force solution that checks every subarray will be too slow
for the largest inputs.

Constraints:
- 1 <= nums.length <= 2 * 10^5
- 0 <= nums[i] <= 10^9
- 0 <= target <= 10^9
"""

from typing import Dict, List


class Solution:
    def shortest_segment_with_target_xor(self, nums: List[int], target: int) -> int:
        """
        Find the length of the shortest contiguous segment whose XOR equals target.

        The key idea is to use prefix XOR:
        - Let prefix_xor[i] represent XOR of nums[0..i-1]
        - Then XOR of subarray nums[l..r] is:
              prefix_xor[r + 1] ^ prefix_xor[l]
        - We want:
              prefix_xor[r + 1] ^ prefix_xor[l] == target
          which rearranges to:
              prefix_xor[l] == prefix_xor[r + 1] ^ target

        So while scanning from left to right, for each current prefix XOR value,
        we look for a previous prefix XOR that would make the subarray XOR equal
        to target.

        Since we want the shortest segment, for each prefix XOR value we should
        remember the most recent index where it appeared. That gives the smallest
        distance to the current position.

        Args:
            nums: List of non-negative integers.
            target: Desired XOR value for a contiguous segment.

        Returns:
            The length of the shortest valid segment, or -1 if none exists.

        Time complexity:
            O(n), where n is the length of nums.

        Space complexity:
            O(n) in the worst case for the prefix XOR map.
        """
        # This dictionary maps:
        #   prefix XOR value -> latest index where this prefix XOR was seen
        #
        # Important indexing detail:
        # We treat prefix XOR positions as "between elements".
        #
        # prefix position 0 means:
        #   XOR of zero elements before the array starts = 0
        #
        # prefix position i means:
        #   XOR of nums[0..i-1]
        #
        # If a subarray is nums[l..r], then its XOR is:
        #   prefix[r + 1] ^ prefix[l]
        #
        # We store the LATEST occurrence because for a fixed current position,
        # using the largest possible previous index gives the SHORTEST subarray.
        latest_index_of_prefix: Dict[int, int] = {0: 0}

        # Running prefix XOR as we scan the array.
        prefix_xor: int = 0

        # Start with "infinity" so any real answer will be smaller.
        best_length: int = float("inf")

        # We iterate through the array using 1-based prefix positions.
        #
        # After processing nums[i - 1], we are at prefix position i.
        # That means:
        #   prefix_xor = XOR of nums[0..i-1]
        for i, value in enumerate(nums, start=1):
            # Update the running prefix XOR to include the current element.
            prefix_xor ^= value

            # We need an earlier prefix value such that:
            #   earlier_prefix ^ current_prefix = target
            #
            # Rearranging:
            #   earlier_prefix = current_prefix ^ target
            needed_prefix: int = prefix_xor ^ target

            # If we have seen that needed prefix before, then the subarray
            # from that earlier prefix position up to current position - 1
            # has XOR exactly equal to target.
            if needed_prefix in latest_index_of_prefix:
                previous_index: int = latest_index_of_prefix[needed_prefix]

                # Length of subarray:
                # current prefix position i minus earlier prefix position previous_index
                current_length: int = i - previous_index

                # Update the best answer if this segment is shorter.
                if current_length < best_length:
                    best_length = current_length

            # Store/update the latest position for this prefix XOR.
            #
            # Why latest, not earliest?
            # Because if the same prefix XOR appears multiple times, then for future
            # positions we want the closest previous occurrence to minimize segment length.
            latest_index_of_prefix[prefix_xor] = i

        # If best_length was never updated, then no valid segment exists.
        return -1 if best_length == float("inf") else best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    nums1 = [5, 1, 2, 1, 5]
    target1 = 3
    result1 = solution.shortest_segment_with_target_xor(nums1, target1)
    print(f"nums = {nums1}, target = {target1} -> shortest length = {result1}")
    # Expected: 2
    # Valid shortest segment: [1, 2], because 1 ^ 2 = 3

    # Example 2
    nums2 = [4, 7, 4, 7]
    target2 = 0
    result2 = solution.shortest_segment_with_target_xor(nums2, target2)
    print(f"nums = {nums2}, target = {target2} -> shortest length = {result2}")
    # Expected: 4
    # Entire array XOR is 0, and no shorter contiguous segment has XOR 0

    # Additional quick checks
    nums3 = [3]
    target3 = 3
    result3 = solution.shortest_segment_with_target_xor(nums3, target3)
    print(f"nums = {nums3}, target = {target3} -> shortest length = {result3}")
    # Expected: 1

    nums4 = [1, 2, 4]
    target4 = 7
    result4 = solution.shortest_segment_with_target_xor(nums4, target4)
    print(f"nums = {nums4}, target = {target4} -> shortest length = {result4}")
    # Expected: 3

    nums5 = [1, 2, 4]
    target5 = 6
    result5 = solution.shortest_segment_with_target_xor(nums5, target5)
    print(f"nums = {nums5}, target = {target5} -> shortest length = {result5}")
    # Expected: 2, because 2 ^ 4 = 6