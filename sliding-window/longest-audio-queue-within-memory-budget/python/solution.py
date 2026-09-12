"""
Title: Longest Audio Queue Within Memory Budget

Problem Description:
A media player buffers a sequence of audio clips before playback. The i-th clip
requires memory[i] megabytes to keep in RAM, and the player must preserve the
original order of clips. Given an array memory and an integer budget, return the
length of the longest contiguous block of clips that can be buffered at the same
time without exceeding the total memory budget.

You may choose any contiguous subarray of memory, but the sum of its values must
be less than or equal to budget. Your task is to compute the maximum possible
number of clips in such a block.

This problem is intended to be solved efficiently for large inputs. A brute-force
solution that checks every possible subarray will be too slow. Think about how to
maintain a valid range while expanding and shrinking a window.

Constraints:
- 1 <= memory.length <= 200000
- 1 <= memory[i] <= 1000000000
- 1 <= budget <= 100000000000000
- The answer always fits in a 32-bit signed integer.

Example 1:
Input: memory = [4, 2, 1, 7, 3, 2], budget = 8
Output: 3
Explanation: The longest valid block is [4, 2, 1] with total memory 7. Other
length-3 blocks like [2, 1, 7] or [7, 3, 2] exceed the budget.

Example 2:
Input: memory = [5, 1, 1, 1, 5], budget = 7
Output: 3
Explanation: One optimal block is [1, 1, 5] with total memory 7. The full array
uses 13, and no valid block of length 4 exists.
"""

from typing import List


class Solution:
    def longest_audio_queue(self, memory: List[int], budget: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is <= budget.

        This uses the sliding window / two-pointer technique. Because every
        memory value is positive, once the current window sum becomes too large,
        moving the left pointer to the right is the correct way to reduce the sum
        and restore validity.

        Args:
            memory: A list where memory[i] is the RAM needed for the i-th clip.
            budget: The maximum total RAM allowed for a buffered contiguous block.

        Returns:
            The length of the longest contiguous block with total sum <= budget.

        Time complexity:
            O(n), where n is the length of memory. Each element is added to the
            window once and removed from the window at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The left boundary of our current sliding window.
        # The window will always represent memory[left:right+1].
        left: int = 0

        # This stores the sum of all values currently inside the window.
        # We update it incrementally instead of recomputing sums repeatedly,
        # which is what makes the solution efficient.
        current_sum: int = 0

        # This will store the best (maximum) valid window length found so far.
        max_length: int = 0

        # We expand the window by moving the right boundary one step at a time.
        # For each new right position, we include memory[right] in the window.
        for right in range(len(memory)):
            current_sum += memory[right]

            # At this point, the window may have become invalid
            # (meaning its sum is now greater than the allowed budget).
            #
            # Because all memory values are positive:
            # - Expanding the window can only increase the sum.
            # - If the sum is too large, the only way to fix it is to shrink
            #   the window from the left.
            #
            # We keep removing elements from the left until the window becomes valid.
            while current_sum > budget:
                current_sum -= memory[left]
                left += 1

            # Now the window memory[left:right+1] is guaranteed to be valid:
            # its sum is <= budget.
            #
            # We compute its length and compare it with the best answer seen so far.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # After processing all possible right boundaries, max_length contains
        # the length of the longest valid contiguous block.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # memory = [4, 2, 1, 7, 3, 2], budget = 8
    # Valid longest block length should be 3, for example [4, 2, 1].
    memory1: List[int] = [4, 2, 1, 7, 3, 2]
    budget1: int = 8
    result1: int = solution.longest_audio_queue(memory1, budget1)
    print(f"Example 1 Result: {result1}")  # Expected: 3

    # Example 2:
    # memory = [5, 1, 1, 1, 5], budget = 7
    # Valid longest block length should be 3, for example [1, 1, 5].
    memory2: List[int] = [5, 1, 1, 1, 5]
    budget2: int = 7
    result2: int = solution.longest_audio_queue(memory2, budget2)
    print(f"Example 2 Result: {result2}")  # Expected: 3

    # Additional small sanity checks for beginner-friendly demonstration.

    # Single element that fits.
    memory3: List[int] = [6]
    budget3: int = 10
    result3: int = solution.longest_audio_queue(memory3, budget3)
    print(f"Additional Test 1 Result: {result3}")  # Expected: 1

    # Single element equal to budget.
    memory4: List[int] = [7]
    budget4: int = 7
    result4: int = solution.longest_audio_queue(memory4, budget4)
    print(f"Additional Test 2 Result: {result4}")  # Expected: 1

    # Entire array fits.
    memory5: List[int] = [1, 2, 3, 1]
    budget5: int = 7
    result5: int = solution.longest_audio_queue(memory5, budget5)
    print(f"Additional Test 3 Result: {result5}")  # Expected: 4