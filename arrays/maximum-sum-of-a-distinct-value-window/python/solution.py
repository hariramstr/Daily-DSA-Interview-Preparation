"""
Title: Maximum Sum of a Distinct-Value Window

Problem Description:
You are given an integer array nums and an integer k. A window is any contiguous
subarray of length exactly k. A window is called valid if all k elements inside it
are pairwise distinct, meaning no value appears more than once in that window.

Your task is to return the maximum possible sum among all valid windows of length k.
If there is no valid window of length k, return 0.

This problem models a situation where you want to choose exactly k consecutive
records, but duplicate values in the chosen range are not allowed because they
would represent repeated IDs, repeated product codes, or duplicate events. The
challenge is to evaluate every length-k range efficiently without recomputing its
sum and uniqueness from scratch.

A correct solution should work efficiently for large inputs. In particular,
iterating over every window and checking duplicates naively may be too slow.
Think about how to maintain both the current window sum and the frequency of values
as the window slides by one position.

Constraints:
- 1 <= nums.length <= 200000
- 1 <= nums[i] <= 1000000000
- 1 <= k <= nums.length

Example 1:
Input: nums = [5,2,3,5,4,6], k = 3
Output: 15

Example 2:
Input: nums = [4,4,2,1,2], k = 3
Output: 7
"""

from typing import Dict, List


class Solution:
    def maximumSubarraySum(self, nums: List[int], k: int) -> int:
        """
        Return the maximum sum among all contiguous subarrays of length k
        whose elements are all distinct.

        Args:
            nums: List of positive integers.
            k: Required fixed window length.

        Returns:
            The maximum sum of any valid window of length k. Returns 0 if no
            such window exists.

        Time complexity:
            O(n), where n is len(nums), because each element is added to and
            removed from the sliding window at most once.

        Space complexity:
            O(k), because the frequency map stores counts for elements currently
            inside the window, and the window size never exceeds k.
        """
        # This dictionary will store how many times each value appears
        # in the current sliding window.
        #
        # Example:
        # If the current window is [2, 3, 2], then:
        # freq = {2: 2, 3: 1}
        #
        # We use a dictionary because:
        # - values can be as large as 1,000,000,000
        # - we only need counts for values currently in the window
        # - dictionary operations are average O(1)
        freq: Dict[int, int] = {}

        # current_sum keeps the sum of the current window.
        # Instead of recomputing the sum from scratch for every window,
        # we update it incrementally:
        # - add the new incoming value
        # - subtract the outgoing value
        #
        # This is the key optimization that makes the solution efficient.
        current_sum: int = 0

        # best_sum stores the maximum sum found among all valid windows.
        # If no valid window exists, it should remain 0.
        best_sum: int = 0

        # We will expand the window one element at a time using the right pointer.
        for right, value in enumerate(nums):
            # ---------------------------------------------------------------
            # STEP 1: Add the new element at index "right" into the window.
            # ---------------------------------------------------------------

            # Add its value to the running sum.
            current_sum += value

            # Increase its frequency count in the current window.
            freq[value] = freq.get(value, 0) + 1

            # ---------------------------------------------------------------
            # STEP 2: If the window became larger than k, shrink it from left.
            # ---------------------------------------------------------------
            #
            # The current window represented by indices is:
            # [right - current_window_size + 1, ..., right]
            #
            # Once right >= k, the window size is k + 1, so we must remove
            # the element at index right - k to bring the size back to exactly k.
            #
            # Why right >= k?
            # - When right == k - 1, window size is exactly k -> valid size
            # - When right == k, window size becomes k + 1 -> too large
            if right >= k:
                left_value: int = nums[right - k]

                # Remove the outgoing element from the running sum.
                current_sum -= left_value

                # Decrease its frequency because it is no longer in the window.
                freq[left_value] -= 1

                # If its count becomes zero, remove it from the dictionary.
                # This keeps the dictionary small and, importantly, allows
                # len(freq) to represent the number of distinct values
                # currently inside the window.
                if freq[left_value] == 0:
                    del freq[left_value]

            # ---------------------------------------------------------------
            # STEP 3: Once the window size is exactly k, check validity.
            # ---------------------------------------------------------------
            #
            # A window of size k is valid if all k elements are distinct.
            #
            # Since freq stores counts of values in the current window:
            # - len(freq) == number of distinct values in the window
            # - if len(freq) == k, then all k elements are distinct
            #
            # This works because the window size is exactly k here.
            if right >= k - 1:
                if len(freq) == k:
                    # The current window is valid, so update the answer.
                    best_sum = max(best_sum, current_sum)

        # After checking all windows, return the best valid sum found.
        # If none were valid, best_sum is still 0.
        return best_sum

    def trace_examples(self) -> None:
        """
        Run the examples from the problem statement and print their outputs.

        Args:
            None

        Returns:
            None

        Time complexity:
            O(n) per example due to the main algorithm.

        Space complexity:
            O(k) per example due to the frequency map.
        """
        # Example 1 from the prompt:
        # nums = [5,2,3,5,4,6], k = 3
        # Windows:
        # [5,2,3] -> distinct, sum = 10
        # [2,3,5] -> distinct, sum = 10
        # [3,5,4] -> distinct, sum = 12
        # [5,4,6] -> distinct, sum = 15
        # Expected answer = 15
        nums1: List[int] = [5, 2, 3, 5, 4, 6]
        k1: int = 3
        result1: int = self.maximumSubarraySum(nums1, k1)
        print("Example 1:")
        print(f"nums = {nums1}, k = {k1}")
        print(f"Output = {result1}")
        print("Expected = 15")
        print()

        # Example 2 from the prompt:
        # nums = [4,4,2,1,2], k = 3
        # Windows:
        # [4,4,2] -> invalid (duplicate 4)
        # [4,2,1] -> valid, sum = 7
        # [2,1,2] -> invalid (duplicate 2)
        # Expected answer = 7
        nums2: List[int] = [4, 4, 2, 1, 2]
        k2: int = 3
        result2: int = self.maximumSubarraySum(nums2, k2)
        print("Example 2:")
        print(f"nums = {nums2}, k = {k2}")
        print(f"Output = {result2}")
        print("Expected = 7")
        print()


if __name__ == "__main__":
    solution = Solution()

    # Run the exact examples from the problem statement to verify correctness.
    solution.trace_examples()

    # Additional sample tests for beginner-friendly demonstration.

    # Case where no valid window exists:
    # Every length-2 window contains duplicates.
    nums3: List[int] = [1, 1, 1, 1]
    k3: int = 2
    print("Additional Test 1:")
    print(f"nums = {nums3}, k = {k3}")
    print(f"Output = {solution.maximumSubarraySum(nums3, k3)}")
    print("Expected = 0")
    print()

    # Case where k = 1:
    # Every single-element window is automatically distinct.
    # Maximum sum is simply the maximum element.
    nums4: List[int] = [8, 3, 10, 2]
    k4: int = 1
    print("Additional Test 2:")
    print(f"nums = {nums4}, k = {k4}")
    print(f"Output = {solution.maximumSubarraySum(nums4, k4)}")
    print("Expected = 10")
    print()