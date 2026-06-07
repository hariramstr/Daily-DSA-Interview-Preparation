"""
Problem Title: Partition Array into Balanced Halves by Digit Sum
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array `nums` of even length `n`. Your task is to partition
the array into two groups of exactly `n / 2` elements each, such that the absolute
difference between the total digit sum of the first group and the total digit sum of
the second group is minimized.

The digit sum of a number is the sum of all its individual digits. For example,
the digit sum of 123 is 1 + 2 + 3 = 6.

Return the minimum possible absolute difference between the total digit sums of the two groups.

Constraints:
- 2 <= nums.length <= 40 and nums.length is even
- 1 <= nums[i] <= 10^5
"""

from typing import List
from itertools import combinations


class Solution:
    def digit_sum(self, n: int) -> int:
        """
        Compute the digit sum of a non-negative integer.

        Args:
            n: A positive integer.

        Returns:
            The sum of all digits of n.

        Time complexity: O(d) where d is the number of digits in n.
        Space complexity: O(1)
        """
        # We repeatedly extract the last digit using modulo 10,
        # add it to our running total, then remove that digit by integer division.
        total = 0
        while n > 0:
            total += n % 10  # Extract the last digit
            n //= 10         # Remove the last digit
        return total

    def minimumDifference(self, nums: List[int]) -> int:
        """
        Partition nums into two equal-sized halves minimizing the absolute
        difference of their total digit sums.

        Approach:
            1. Convert every number to its digit sum.
            2. We need to choose exactly n//2 elements for Group 1; the rest go to Group 2.
            3. We enumerate all C(n, n//2) subsets of size n//2 using itertools.combinations.
            4. For each subset (Group 1), compute its digit-sum total.
               Group 2's total = overall_total - Group1_total.
            5. Track the minimum |Group1_total - Group2_total|.

        Why combinations?
            n <= 40, so n//2 <= 20. C(40, 20) ≈ 137 billion — too large for n=40.
            However, we can use a meet-in-the-middle approach for large n,
            but since the problem states n <= 40 and typical test cases are small,
            we first check if a direct combination approach is feasible.

            Actually C(40,20) ~ 1.4e11 which is too large. We use meet-in-the-middle:
            - Split digit_sums into two halves A and B of size n//2 each.
            - Enumerate all subsets of A of every size k (0..n//2), recording their sums.
            - Enumerate all subsets of B of every size k (0..n//2), recording their sums.
            - For Group 1 we need exactly n//2 elements total: k from A and (n//2 - k) from B.
            - For each k, combine sums from A (size k) with sums from B (size n//2 - k)
              and find the pair minimizing |combined - half_total| where half_total = total/2.

        Args:
            nums: List of positive integers with even length.

        Returns:
            Minimum absolute difference between the two groups' digit sum totals.

        Time complexity: O(2^(n/2) * n/2) for meet-in-the-middle enumeration.
        Space complexity: O(2^(n/2)) to store subset sums.
        """

        # ----------------------------------------------------------------
        # Step 1: Convert each number to its digit sum.
        # We only care about digit sums from here on.
        # ----------------------------------------------------------------
        digit_sums = [self.digit_sum(x) for x in nums]

        n = len(digit_sums)
        half = n // 2  # Each group must have exactly this many elements

        # ----------------------------------------------------------------
        # Step 2: Compute the overall total digit sum.
        # If Group 1 has sum S1 and Group 2 has sum S2, then S1 + S2 = total.
        # We want to minimize |S1 - S2| = |2*S1 - total|.
        # So we want S1 as close to total/2 as possible.
        # ----------------------------------------------------------------
        total = sum(digit_sums)

        # ----------------------------------------------------------------
        # Step 3: Split digit_sums into two halves for meet-in-the-middle.
        # A = first half of the array, B = second half.
        # ----------------------------------------------------------------
        A = digit_sums[:half]   # First half of elements
        B = digit_sums[half:]   # Second half of elements

        # ----------------------------------------------------------------
        # Step 4: Enumerate all subsets of A grouped by subset size.
        # subset_sums_A[k] = list of all possible sums when choosing k elements from A.
        # We use itertools.combinations to generate subsets of each size.
        # ----------------------------------------------------------------
        # Size of A is `half`, so k ranges from 0 to half.
        subset_sums_A: List[List[int]] = [[] for _ in range(half + 1)]
        for k in range(half + 1):
            for combo in combinations(A, k):
                subset_sums_A[k].append(sum(combo))

        # ----------------------------------------------------------------
        # Step 5: Enumerate all subsets of B grouped by subset size.
        # subset_sums_B[k] = list of all possible sums when choosing k elements from B.
        # ----------------------------------------------------------------
        subset_sums_B: List[List[int]] = [[] for _ in range(half + 1)]
        for k in range(half + 1):
            for combo in combinations(B, k):
                subset_sums_B[k].append(sum(combo))

        # ----------------------------------------------------------------
        # Step 6: Sort the B-lists so we can binary-search for the best complement.
        # For a fixed k elements from A (sum = sA), we take (half - k) elements from B.
        # We want sA + sB as close to total/2 as possible.
        # Equivalently, sB as close to (total/2 - sA) as possible.
        # Binary search on sorted B-sums gives us the nearest value quickly.
        # ----------------------------------------------------------------
        import bisect
        for k in range(half + 1):
            subset_sums_B[k].sort()

        # ----------------------------------------------------------------
        # Step 7: Iterate over all combinations of (k from A, half-k from B).
        # For each sum sA from A's k-subsets, binary-search B's (half-k)-subsets
        # for the value closest to (total - 2*sA) / 2, i.e., target_sB = total/2 - sA.
        # We use integer arithmetic: minimize |2*(sA+sB) - total|.
        # ----------------------------------------------------------------
        min_diff = float('inf')

        for k in range(half + 1):
            # Number of elements we must pick from B to complete Group 1
            j = half - k

            # Get the sorted list of B-sums for choosing j elements
            b_sums = subset_sums_B[j]
            if not b_sums:
                continue  # No valid subsets of this size

            for sA in subset_sums_A[k]:
                # We want sA + sB ≈ total / 2
                # So target for sB is (total - 2*sA) / 2, but we work with integers.
                # Minimize |2*(sA + sB) - total| = |2*sA + 2*sB - total|
                # Let target_sB = (total - 2*sA) / 2 (may be fractional)
                # Binary search for the closest integer sB in b_sums.

                # The ideal sB (as a real number)
                ideal_sB = (total - 2 * sA) / 2.0

                # Binary search: find insertion point for ideal_sB in sorted b_sums
                pos = bisect.bisect_left(b_sums, ideal_sB)

                # Check the candidate at pos and pos-1 (the two nearest neighbors)
                for idx in [pos - 1, pos]:
                    if 0 <= idx < len(b_sums):
                        sB = b_sums[idx]
                        diff = abs(2 * (sA + sB) - total)
                        if diff < min_diff:
                            min_diff = diff

        return min_diff


# -----------------------------------------------------------------------
# Main block: demonstrate the solution with the provided examples.
# -----------------------------------------------------------------------
if __name__ == "__main__":
    sol = Solution()

    # ------------------------------------------------------------------
    # Example 1
    # nums = [14, 21, 35, 9]
    # Digit sums: 14->5, 21->3, 35->8, 9->9  => [5, 3, 8, 9]
    # Total digit sum = 25
    # We want each group's sum as close to 12.5 as possible.
    # Best split: Group1=[14,35] sums=5+8=13, Group2=[21,9] sums=3+9=12
    # |13 - 12| = 1  ✓
    # ------------------------------------------------------------------
    nums1 = [14, 21, 35, 9]
    result1 = sol.minimumDifference(nums1)
    print(f"Example 1: nums = {nums1}")
    print(f"  Digit sums: {[sol.digit_sum(x) for x in nums1]}")
    print(f"  Result: {result1}  (Expected: 1)")
    print()

    # ------------------------------------------------------------------
    # Example 2
    # nums = [10, 22, 33, 45, 50, 67]
    # Digit sums: 10->1, 22->4, 33->6, 45->9, 50->5, 67->13 => [1,4,6,9,5,13]
    # Total = 38, half_total = 19
    # We want each group's sum = 19.
    # Group1=[10,45,67] sums=1+9+13=23, Group2=[22,33,50] sums=4+6+5=15 => diff=8
    # Let's try Group1=[22,45,50] sums=4+9+5=18, Group2=[10,33,67] sums=1+6+13=20 => diff=2
    # Group1=[10,33,67] sums=1+6+13=20, Group2=[22,45,50] sums=4+9+5=18 => diff=2
    # Group1=[22,33,67] sums=4+6+13=23, Group2=[10,45,50] sums=1+9+5=15 => diff=8
    # Group1=[10,45,50] sums=1+9+5=15, Group2=[22,33,67] sums=4+6+13=23 => diff=8
    # Group1=[22,45,67] sums=4+9+13=26, Group2=[10,33,50] sums=1+6+5=12 => diff=14
    # Group1=[10,22,67] sums=1+4+13=18, Group2=[33,45,50] sums=6+9+5=20 => diff=2
    # Group1=[33,45,50] sums=6+9+5=20, Group2=[10,22,67] sums=1+4+13=18 => diff=2
    # Hmm, the problem says output is 0. Let me re-check digit sums.
    # 67: 6+7=13. Total=1+4+6+9+5+13=38. Half=19.
    # Need a subset of size 3 summing to 19.
    # Possible: 1+5+13=19 => [10,50,67] ✓  Group2=[22,33,45] sums=4+6+9=19 ✓ diff=0
    # ------------------------------------------------------------------
    nums2 = [10, 22, 33, 45, 50, 67]
    result2 = sol.minimumDifference(nums2)
    print(f"Example 2: nums = {nums2}")
    print(f"  Digit sums: {[sol.digit_sum(x) for x in nums2]}")
    print(f"  Result: {result2}  (Expected: 0)")
    print()

    # ------------------------------------------------------------------
    # Additional test: all same digit sums
    # nums = [11, 20, 101, 200]  digit sums = [2, 2, 2, 2]
    # Any split gives equal sums => diff = 0
    # ------------------------------------------------------------------
    nums3 = [11, 20, 101, 200]
    result3 = sol.minimumDifference(nums3)
    print(f"Example 3: nums = {nums3}")
    print(f"  Digit sums: {[sol.digit_sum(x) for x in nums3]}")
    print(f"  Result: {result3}  (Expected: 0)")
    print()

    # ------------------------------------------------------------------
    # Additional test: simple two-element array
    # nums = [1, 100]  digit sums = [1, 1]  diff = 0
    # ------------------------------------------------------------------
    nums4 = [1, 100]
    result4 = sol.minimumDifference(nums4)
    print(f"Example 4: nums = {nums4}")
    print(f"  Digit sums: {[sol.digit_sum(x) for x in nums4]}")
    print(f"  Result: {result4}  (Expected: 0)")