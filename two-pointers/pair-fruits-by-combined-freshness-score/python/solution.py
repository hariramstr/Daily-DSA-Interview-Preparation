"""
Problem Title: Pair Fruits by Combined Freshness Score
Difficulty: Easy
Topic: Two Pointers

Problem Description:
A grocery store receives a shipment of fruits, each assigned a freshness score between 1 and 100.
The store wants to pair up fruits such that each pair has a combined freshness score equal to
exactly a given target value k. Each fruit can be used in at most one pair.

Given a sorted array freshness of integers representing the freshness scores of the fruits,
and an integer k, return the maximum number of non-overlapping pairs you can form where each
pair's scores sum to exactly k.

Constraints:
- 2 <= freshness.length <= 10^5
- 1 <= freshness[i] <= 100
- 1 <= k <= 200
- The array freshness is sorted in non-decreasing order.

Example 1:
Input: freshness = [1, 2, 3, 4, 5, 6, 7], k = 8
Output: 3
Explanation: The pairs are (1,7), (2,6), (3,5). The value 4 cannot be paired.

Example 2:
Input: freshness = [1, 1, 2, 3, 4, 4], k = 5
Output: 2
Explanation: The pairs are (1,4) and (1,4). The values 2 and 3 cannot be paired.
"""

from typing import List


class Solution:
    def maxPairs(self, freshness: List[int], k: int) -> int:
        """
        Find the maximum number of non-overlapping pairs that sum to exactly k.

        Uses a two-pointer technique on the sorted array:
        - Left pointer starts at the beginning (smallest values)
        - Right pointer starts at the end (largest values)
        - If the sum equals k, we found a valid pair; move both pointers inward
        - If the sum is less than k, we need a larger value; move left pointer right
        - If the sum is greater than k, we need a smaller value; move right pointer left

        Args:
            freshness (List[int]): A sorted (non-decreasing) list of freshness scores.
            k (int): The target combined freshness score for each pair.

        Returns:
            int: The maximum number of non-overlapping pairs whose scores sum to k.

        Time Complexity: O(n) — We traverse the array at most once with two pointers.
        Space Complexity: O(1) — Only a constant number of extra variables are used.
        """

        # -----------------------------------------------------------------------
        # Step 1: Initialize two pointers
        # -----------------------------------------------------------------------
        # 'left' starts at the beginning of the array (index 0).
        # 'right' starts at the end of the array (last valid index).
        # These two pointers will "squeeze" toward each other.
        left: int = 0
        right: int = len(freshness) - 1

        # -----------------------------------------------------------------------
        # Step 2: Initialize the pair counter
        # -----------------------------------------------------------------------
        # 'pairs' keeps track of how many valid pairs we have found so far.
        pairs: int = 0

        # -----------------------------------------------------------------------
        # Step 3: Loop until the two pointers meet or cross
        # -----------------------------------------------------------------------
        # As long as left < right, there are at least two distinct elements
        # to consider for pairing. When left == right, we only have one element
        # left, which cannot form a pair by itself.
        while left < right:

            # -------------------------------------------------------------------
            # Step 3a: Compute the current sum of the two pointed elements
            # -------------------------------------------------------------------
            # freshness[left] is the smaller (or equal) value,
            # freshness[right] is the larger (or equal) value.
            current_sum: int = freshness[left] + freshness[right]

            # -------------------------------------------------------------------
            # Step 3b: Check if we found a valid pair
            # -------------------------------------------------------------------
            if current_sum == k:
                # We found a pair! Increment the pair count.
                pairs += 1

                # Move BOTH pointers inward because both elements are now "used".
                # - left moves right to consider the next smallest unused element.
                # - right moves left to consider the next largest unused element.
                left += 1
                right -= 1

            # -------------------------------------------------------------------
            # Step 3c: Sum is too small — need a larger right element or
            #          a larger left element
            # -------------------------------------------------------------------
            elif current_sum < k:
                # The current sum is less than k.
                # Since the array is sorted, increasing the left pointer will
                # give us a larger value at freshness[left], potentially
                # bringing the sum closer to k.
                # We do NOT move the right pointer because we still want to
                # try pairing the current right element with a larger left value.
                left += 1

            # -------------------------------------------------------------------
            # Step 3d: Sum is too large — need a smaller right element
            # -------------------------------------------------------------------
            else:
                # current_sum > k
                # The current sum exceeds k.
                # Since the array is sorted, decreasing the right pointer will
                # give us a smaller value at freshness[right], potentially
                # bringing the sum closer to k.
                # We do NOT move the left pointer because we still want to
                # try pairing the current left element with a smaller right value.
                right -= 1

        # -----------------------------------------------------------------------
        # Step 4: Return the total number of valid pairs found
        # -----------------------------------------------------------------------
        return pairs


# =============================================================================
# Main block: Demonstrate the solution with sample inputs from the problem
# =============================================================================
if __name__ == "__main__":
    # Create an instance of the Solution class
    solution = Solution()

    # -------------------------------------------------------------------------
    # Example 1
    # freshness = [1, 2, 3, 4, 5, 6, 7], k = 8
    # Expected Output: 3
    # Trace:
    #   left=0(1), right=6(7): sum=8 == k → pair! pairs=1, left=1, right=5
    #   left=1(2), right=5(6): sum=8 == k → pair! pairs=2, left=2, right=4
    #   left=2(3), right=4(5): sum=8 == k → pair! pairs=3, left=3, right=3
    #   left=3, right=3: left is NOT < right → loop ends
    # Result: 3 ✓
    # -------------------------------------------------------------------------
    freshness1: List[int] = [1, 2, 3, 4, 5, 6, 7]
    k1: int = 8
    result1: int = solution.maxPairs(freshness1, k1)
    print(f"Example 1:")
    print(f"  Input:    freshness = {freshness1}, k = {k1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: 3")
    print(f"  Correct:  {result1 == 3}")
    print()

    # -------------------------------------------------------------------------
    # Example 2
    # freshness = [1, 1, 2, 3, 4, 4], k = 5
    # Expected Output: 2
    # Trace:
    #   left=0(1), right=5(4): sum=5 == k → pair! pairs=1, left=1, right=4
    #   left=1(1), right=4(4): sum=5 == k → pair! pairs=2, left=2, right=3
    #   left=2(2), right=3(3): sum=5 == k → pair! pairs=3, left=3, right=2
    #   Wait — that gives 3, but expected is 2. Let me re-check...
    #
    # Re-trace carefully:
    #   freshness = [1, 1, 2, 3, 4, 4], indices 0..5
    #   left=0(1), right=5(4): 1+4=5 == k → pair! pairs=1, left=1, right=4
    #   left=1(1), right=4(4): 1+4=5 == k → pair! pairs=2, left=2, right=3
    #   left=2(2), right=3(3): 2+3=5 == k → pair! pairs=3, left=3, right=2
    #   left(3) >= right(2) → loop ends
    # Result: 3
    #
    # But the expected answer is 2. Let me re-read the problem...
    # "The pairs are (1,4) and (1,4). The values 2 and 3 cannot be paired
    #  to form 5 without reusing already paired elements."
    #
    # Hmm, but 2+3=5 and neither 2 nor 3 was used in the first two pairs.
    # The problem explanation seems incorrect, or the expected output is wrong.
    # Mathematically, (1,4), (1,4), (2,3) are three valid non-overlapping pairs.
    # Our algorithm correctly returns 3 for this input.
    #
    # We'll trust the algorithm (which is mathematically correct) and note the
    # discrepancy with the problem's stated expected output.
    # -------------------------------------------------------------------------
    freshness2: List[int] = [1, 1, 2, 3, 4, 4]
    k2: int = 5
    result2: int = solution.maxPairs(freshness2, k2)
    print(f"Example 2:")
    print(f"  Input:    freshness = {freshness2}, k = {k2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: 2 (per problem statement) or 3 (mathematically correct)")
    print(f"  Note: Our algorithm returns {result2}.")
    print(f"        Pairs found: (1,4), (1,4), (2,3) — all valid and non-overlapping.")
    print(f"        The problem's explanation appears to contain an error.")
    print()

    # -------------------------------------------------------------------------
    # Additional test cases
    # -------------------------------------------------------------------------

    # Test: No valid pairs possible
    freshness3: List[int] = [1, 2, 3]
    k3: int = 10
    result3: int = solution.maxPairs(freshness3, k3)
    print(f"Additional Test 1 (no valid pairs):")
    print(f"  Input:    freshness = {freshness3}, k = {k3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: 0")
    print(f"  Correct:  {result3 == 0}")
    print()

    # Test: All elements can be paired
    freshness4: List[int] = [1, 2, 3, 4]
    k4: int = 5
    result4: int = solution.maxPairs(freshness4, k4)
    print(f"Additional Test 2 (all paired):")
    print(f"  Input:    freshness = {freshness4}, k = {k4}")
    print(f"  Output:   {result4}")
    print(f"  Expected: 2  (pairs: (1,4) and (2,3))")
    print(f"  Correct:  {result4 == 2}")
    print()

    # Test: Duplicate elements
    freshness5: List[int] = [2, 2, 2, 2]
    k5: int = 4
    result5: int = solution.maxPairs(freshness5, k5)
    print(f"Additional Test 3 (all duplicates):")
    print(f"  Input:    freshness = {freshness5}, k = {k5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: 2  (pairs: (2,2) and (2,2))")
    print(f"  Correct:  {result5 == 2}")
    print()

    # Test: Single possible pair
    freshness6: List[int] = [3, 5, 7]
    k6: int = 8
    result6: int = solution.maxPairs(freshness6, k6)
    print(f"Additional Test 4 (one valid pair):")
    print(f"  Input:    freshness = {freshness6}, k = {k6}")
    print(f"  Output:   {result6}")
    print(f"  Expected: 1  (pair: (3,5); 7 is left unpaired)")
    print(f"  Correct:  {result6 == 1}")