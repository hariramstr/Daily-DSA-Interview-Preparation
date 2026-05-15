```python
"""
Title: Pair Sum Closest to Target After Removing One Element
Difficulty: Medium
Topic: Two Pointers

Problem Description:
You are given a sorted integer array `nums` and an integer `target`. Your task is to find
the pair of elements (from different indices) whose sum is closest to `target`, but with
one twist: you must remove exactly one element from the array before choosing your pair.
The removed element can be any element in the array.

Return the minimum absolute difference between any valid pair sum and `target` after
optimally removing one element. A valid pair consists of two elements at different indices
from the remaining array after removal.

Note: The array after removal must still contain at least 2 elements to form a valid pair.

Constraints:
- 3 <= nums.length <= 10^5
- -10^5 <= nums[i] <= 10^5
- -2 * 10^5 <= target <= 2 * 10^5
- nums is sorted in non-decreasing order
"""

from typing import List


class Solution:
    def min_abs_diff_after_removal(self, nums: List[int], target: int) -> int:
        """
        Find the minimum absolute difference between any valid pair sum and target,
        after optimally removing exactly one element from the sorted array.

        Key Insight:
        ------------
        Instead of literally removing each element and running two-pointer on the
        remaining array (which would be O(n^2)), we observe:

        When we remove element at index `k`, the remaining array is:
            nums[0..k-1] + nums[k+1..n-1]

        We need the best pair from this remaining sorted array.

        A smarter approach: for each possible "removed index" k, we run two pointers
        on the array excluding index k. However, this is still O(n^2) naively.

        Better approach: We iterate over all possible removed indices k (0 to n-1).
        For each k, we use two pointers on the remaining array. But we can be smart:
        we only need to try removing each element once, and for each removal, the
        two-pointer search on the remaining sorted array is O(n). Total: O(n^2).

        Given n <= 10^5, O(n^2) might be tight, but let's think about whether we
        can do better.

        Actually, for this problem with n up to 10^5, O(n^2) would be 10^10 operations
        which is too slow. We need a smarter approach.

        Smarter O(n log n) or O(n) approach:
        --------------------------------------
        Key observation: The optimal answer comes from one of these scenarios:
        1. We remove an element that is NOT part of the optimal pair.
        2. We remove an element that IS part of the optimal pair (forcing us to pick
           a different pair).

        But actually, let's think differently:
        - For any pair (i, j) with i < j, we can remove ANY element k != i and k != j.
          This is always possible as long as n >= 3 (which is guaranteed).
        - So effectively, we just need the best pair from the FULL array, because we
          can always remove some other element!

        Wait, that's the key insight! Since n >= 3, for any pair (i, j), there exists
        at least one other index k (k != i, k != j) that we can remove. So the answer
        is simply the minimum |pair_sum - target| over all pairs in the original array!

        Let me verify with examples:
        Example 1: nums = [1, 3, 5, 8, 12], target = 10
          - n = 5, so we can always remove a third element.
          - Best pair: (2+8)=10? No, 2 is not in array. Let's find best pair:
            Two pointers: left=0(1), right=4(12), sum=13, diff=3
            Move left: left=1(3), right=4(12), sum=15, diff=5
            Move right: left=0(1), right=3(8), sum=9, diff=1
            Move left: left=1(3), right=3(8), sum=11, diff=1
            Move right: left=1(3), right=2(5), sum=8, diff=2
            Hmm, best diff so far = 1. But expected output is 0.

          Wait, let me re-read the problem. The explanation says remove 12, remaining
          [1,3,5,8], pair (2+8) — but 2 is not in the array. The explanation seems
          confusing. Let me re-read...

          Actually the explanation says "remove 5: [1,3,8,12]. Pair (1+... no. Remove 12:
          [1,3,5,8]. Pair (3+7)? No. Pair (2+8)=10, diff=0." — but 2 is not in [1,3,5,8].

          I think the problem explanation has errors. Let me just check: in [1,3,5,8,12],
          what pair sums to 10? 2+8 (2 not present), 3+7 (7 not present), 5+5 (same index).
          No pair sums to exactly 10. Closest: 3+8=11 (diff=1) or 1+8=9 (diff=1) or
          3+5=8 (diff=2) or 5+8=13 (diff=3).

          Hmm, but the output is 0. Let me reconsider... Maybe after removing one element,
          we can get a pair sum of exactly 10?

          Remove 1: [3,5,8,12] → best pairs: 3+5=8(2), 3+8=11(1), 3+12=15(5), 5+8=13(3),
                                              5+12=17(7), 8+12=20(10) → min diff = 1
          Remove 3: [1,5,8,12] → 1+5=6(4), 1+8=9(1), 1+12=13(3), 5+8=13(3), 5+12=17(7),
                                  8+12=20(10) → min diff = 1
          Remove 5: [1,3,8,12] → 1+3=4(6), 1+8=9(1), 1+12=13(3), 3+8=11(1), 3+12=15(5),
                                  8+12=20(10) → min diff = 1
          Remove 8: [1,3,5,12] → 1+3=4(6), 1+5=6(4), 1+12=13(3), 3+5=8(2), 3+12=15(5),
                                  5+12=17(7) → min diff = 2
          Remove 12: [1,3,5,8] → 1+3=4(6), 1+5=6(4), 1+8=9(1), 3+5=8(2), 3+8=11(1),
                                  5+8=13(3) → min diff = 1

          So the minimum across all removals is 1, not 0! The expected output of 0 seems
          wrong for Example 1, OR I'm misunderstanding the problem.

          Let me re-read the problem statement more carefully...

          Oh wait, maybe the problem allows removing any element and the explanation is
          just poorly written. Let me trust the output: 0.

          Hmm, but I've exhaustively checked all removals for Example 1 and the minimum
          diff is 1, not 0. Unless I'm making an arithmetic error...

          Actually wait - let me recheck remove 5: [1,3,8,12], target=10
          1+3=4, |4-10|=6
          1+8=9, |9-10|=1
          1+12=13, |13-10|=3
          3+8=11, |11-10|=1
          3+12=15, |15-10|=5
          8+12=20, |20-10|=10
          Min diff = 1. Not 0.

          I believe the expected output for Example 1 should be 1, not 0. The problem
          statement's explanation is incorrect/confusing.

          Let me verify Example 2: nums = [1, 2, 4, 7, 9], target = 6
          Remove 9: [1,2,4,7] → 1+2=3(3), 1+4=5(1), 1+7=8(2), 2+4=6(0)! → diff=0 ✓

          Example 3: nums = [1, 1, 2, 3], target = 100
          Remove any element, best pair sum = 2+3=5, diff=95 ✓

          So Example 1's output of 0 seems wrong based on my analysis. I'll implement
          the correct algorithm and trust Examples 2 and 3.

          Actually, I wonder if Example 1's output is indeed 1 and there's a typo.
          I'll implement the correct solution.

        Algorithm:
        ----------
        Since n >= 3, for any pair (i, j) with i < j, we can always remove some
        element k where k != i and k != j (there's at least one such k since n >= 3).

        Therefore, the answer is simply: find the pair in the original array with
        sum closest to target, using the standard two-pointer technique on the
        sorted array.

        This is O(n) time after the array is already sorted.

        Args:
            nums: Sorted integer array (non-decreasing order)
            target: The target sum to get closest to

        Returns:
            Minimum absolute difference between any valid pair sum and target,
            after optimally removing one element.

        Time Complexity: O(n) - single two-pointer pass through the sorted array
        Space Complexity: O(1) - only a constant number of variables used
        """

        # -----------------------------------------------------------------------
        # Step 1: Initialize two pointers and result tracker
        # -----------------------------------------------------------------------
        # left pointer starts at the beginning (smallest element)
        # right pointer starts at the end (largest element)
        # This is the classic two-pointer approach for sorted arrays
        left: int = 0
        right: int = len(nums) - 1

        # Track the minimum absolute difference found so far
        # Initialize to infinity so any real difference will be smaller
        min_diff: int = float('inf')

        # -----------------------------------------------------------------------
        # Step 2: Two-pointer traversal
        # -----------------------------------------------------------------------
        # Key insight: Since n >= 3, for ANY pair (left, right) we choose,
        # there exists at least one other index we can "remove" (since n >= 3
        # guarantees at least 3 elements, so at least one element is not in our pair).
        # Therefore, we just need to find the best pair in the entire array.
        #
        # The two-pointer technique works because the array is sorted:
        # - If current sum < target: move left pointer right to increase sum
        # - If current sum > target: move right pointer left to decrease sum
        # - If current sum == target: we found diff=0, can't do better, return 0
        while left < right:
            # Calculate the current pair sum
            current_sum: int = nums[left] + nums[right]

            # Calculate absolute difference from target
            current_diff: int = abs(current_sum - target)

            # Update minimum difference if current is better
            min_diff = min(min_diff, current_diff)

            # If we found an exact match, no need to continue
            if current_diff == 0:
                return 0

            # Move pointers based on whether sum is too small or too large
            if current_sum < target:
                # Sum is too small, move left pointer right to try larger values
                left += 1
            else:
                # Sum is too large, move right pointer left to try smaller values
                right -= 1

        # -----------------------------------------------------------------------
        # Step 3: Return the minimum difference found
        # -----------------------------------------------------------------------
        return min_diff

    def min_abs_diff_brute_force(self, nums: List[int], target: int) -> int:
        """
        Brute force solution: try removing each element, then find best pair
        in the remaining array using two pointers.

        This is O(n^2) and serves as a verification tool for the optimized solution.

        Args:
            nums: Sorted integer array (non-decreasing order)
            target: The target sum to get closest to

        Returns:
            Minimum absolute difference between any valid pair sum and target,
            after optimally removing one element.

        Time Complexity: O(n^2) - for each of n removals, O(n) two-pointer search
        Space Complexity: O(1) - no extra space needed
        """
        n: int = len(nums)
        overall_min_diff: int = float('inf')

        # Try removing each element at index k
        for k in range(n):
            # -----------------------------------------------------------------------
            # For removal of index k, use two pointers on the remaining array
            # The remaining array is nums[0..k-1] + nums[k+1..n-1]
            # -----------------------------------------------------------------------
            left: int = 0
            right: int = n - 1

            # Skip the removed index k
            # We need to find a valid starting left (not k)
            if left == k:
                left = 1

            # We need to find a valid starting right (not k)
            if right == k:
                right = n - 2

            # Two-pointer search on array excluding index k
            while left < right:
                # Skip the removed index
                if left == k:
                    left += 1
                    continue
                if right == k:
                    right -= 1
                    continue

                # Ensure left < right after skipping
                if left >= right:
                    break

                current_sum: int = nums[left] + nums[right]
                current_diff: int = abs(current_sum - target)
                overall_min_diff = min(overall_min_diff, current_diff)

                if current_diff == 0:
                    return 0

                if current_sum < target:
                    left += 1
                else:
                    right -= 1

        return overall_min_diff


if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1: nums = [1, 3, 5, 8, 12], target = 10
    # Expected output: 0 (per problem statement, though analysis suggests 1)
    # Let's run both methods and see
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("Example 1:")
    nums1 = [1, 3, 5, 8, 12]
    target1 = 10
    result1_optimized = solution.min_abs_diff_after_removal(nums1, target1)
    result1_brute = solution.min_abs_diff_brute_force(nums1, target1)
    print(f"  nums = {nums1}, target = {target1}")
    print(f"  Optimized result: {result1_optimized}")
    print(f"  Brute force result: {result1_brute}")
    print(f"  Problem says expected: 0")
    print(f"  Note: Both methods agree. If brute force gives 1, problem example may have a typo.")

    # -----------------------------------------------------------------------
    # Example 2: nums = [1, 2, 4, 7, 9], target = 6
    # Expected output: 0
    # Remove 9 → [1,2,4,7], pair (2,4) sums to 6, diff = 0
    # -----------------------------------------------------------------------
    print("\nExample 2:")
    nums2 = [1, 2, 4, 7, 9]
    target2 = 6
    result2_optimized = solution.min_abs_diff_after_removal(nums2, target2)
    result2_brute = solution.min_abs_diff_brute_force(nums2, target2)
    print(f"  nums = {nums2}, target = {target2}")
    print(f"  Optimized result: {result2_optimized}")
    print(f"  Brute force result: {result2_brute}")
    print(f"  Expected: