```python
"""
Maximum Distinct Flavors in a Circular Tasting Menu
====================================================

A restaurant offers a circular tasting menu with `n` dishes arranged in a circle,
where each dish has a flavor profile represented by an integer in `flavors[]`.
A group of `k` guests will each receive a contiguous segment of `k` dishes from
this circular arrangement (wrapping around if necessary). However, due to dietary
restrictions, the segment must contain at most `m` repeated flavor values
(i.e., the count of any single flavor within the chosen segment must not exceed `m`).

Your task is to find the maximum number of distinct flavor values achievable in any
valid contiguous circular segment of exactly `k` dishes, subject to the constraint
that no single flavor appears more than `m` times within the segment.
If no valid segment exists, return -1.

Constraints:
- 1 <= n <= 100000
- 1 <= k <= n
- 1 <= m <= k
- 1 <= flavors[i] <= 100000
"""

from typing import List, Dict


class Solution:
    def maxDistinctFlavors(self, flavors: List[int], k: int, m: int) -> int:
        """
        Find the maximum number of distinct flavors in any valid circular window of size k,
        where no single flavor appears more than m times.

        Args:
            flavors: List of integers representing flavor profiles of dishes arranged in a circle.
            k: The exact size of the contiguous segment (window) to consider.
            m: The maximum allowed count of any single flavor within the segment.

        Returns:
            The maximum number of distinct flavors in any valid window of size k,
            or -1 if no valid window exists.

        Time Complexity: O(n) — We iterate through the doubled array once using a
                         fixed-size sliding window of length k.
        Space Complexity: O(n) — We store the doubled array and a frequency dictionary
                          that holds at most k entries.
        """

        # -----------------------------------------------------------------------
        # Step 1: Handle the circular nature by "doubling" the array.
        #
        # Since the dishes are arranged in a circle, a window of size k can wrap
        # around the end back to the beginning. A classic trick to handle circular
        # arrays is to concatenate the array with itself. This way, every possible
        # circular window of size k appears as a contiguous subarray in the doubled
        # array — but only within the first 2n - k + 1 starting positions (to avoid
        # counting the same window twice when n > k).
        #
        # Example: flavors = [1, 2, 3], k = 2
        # Doubled: [1, 2, 3, 1, 2, 3]
        # Windows of size 2: [1,2], [2,3], [3,1], [1,2], [2,3]
        # We only need the first n = 3 starting positions: [1,2], [2,3], [3,1]
        # -----------------------------------------------------------------------
        n = len(flavors)

        # If k equals n, there's only one possible window (the entire circle),
        # so we don't need to double — but doubling still works correctly.
        doubled = flavors + flavors  # Length 2n

        # -----------------------------------------------------------------------
        # Step 2: Initialize the sliding window data structures.
        #
        # We use a dictionary `freq` to track the count of each flavor in the
        # current window. This allows O(1) updates as we slide the window.
        #
        # `distinct_count` tracks how many flavors currently have count >= 1
        # in the window.
        #
        # `invalid_count` tracks how many flavors currently violate the constraint
        # (i.e., have count > m). A window is valid only when invalid_count == 0.
        # -----------------------------------------------------------------------
        freq: Dict[int, int] = {}  # flavor -> count in current window
        distinct_count = 0         # number of distinct flavors in current window
        invalid_count = 0          # number of flavors with count > m (violations)
        max_distinct = -1          # best answer found so far

        # -----------------------------------------------------------------------
        # Step 3: Build the initial window of size k (indices 0 to k-1).
        #
        # We "add" the first k elements to our frequency map and update
        # distinct_count and invalid_count accordingly.
        # -----------------------------------------------------------------------
        for i in range(k):
            flavor = doubled[i]

            # If this flavor is new to the window, increment distinct count
            if flavor not in freq or freq[flavor] == 0:
                distinct_count += 1

            # Update frequency
            freq[flavor] = freq.get(flavor, 0) + 1

            # Check if this flavor just crossed the threshold m
            # (i.e., its count went from m to m+1, becoming a violation)
            if freq[flavor] == m + 1:
                invalid_count += 1

        # -----------------------------------------------------------------------
        # Step 4: Check if the initial window is valid and update max_distinct.
        #
        # A window is valid if no flavor appears more than m times,
        # i.e., invalid_count == 0.
        # -----------------------------------------------------------------------
        if invalid_count == 0:
            max_distinct = distinct_count

        # -----------------------------------------------------------------------
        # Step 5: Slide the window across the doubled array.
        #
        # We consider all starting positions from 1 to n-1 (inclusive).
        # Why n-1? Because:
        #   - Starting position 0 corresponds to the original array start.
        #   - Starting position n-1 corresponds to the last element of the
        #     original array as the start (window wraps around).
        #   - Starting position n would be the same as starting position 0
        #     (full wrap), so we stop before that.
        #
        # For each new starting position `start`, we:
        #   a) Remove the element that's leaving the window (index start - 1)
        #   b) Add the element that's entering the window (index start + k - 1)
        #   c) Check validity and update max_distinct
        # -----------------------------------------------------------------------
        for start in range(1, n):
            # -------------------------------------------------------------------
            # Step 5a: Remove the outgoing element (left side of window).
            #
            # The element leaving is at index `start - 1` in the doubled array.
            # -------------------------------------------------------------------
            outgoing = doubled[start - 1]

            # Before decrementing, check if this removal fixes a violation
            # (i.e., count was m+1, now becomes m — no longer a violation)
            if freq[outgoing] == m + 1:
                invalid_count -= 1

            # Decrement the frequency
            freq[outgoing] -= 1

            # If count drops to 0, this flavor is no longer in the window
            if freq[outgoing] == 0:
                distinct_count -= 1
                # Optionally clean up the dict to save memory
                del freq[outgoing]

            # -------------------------------------------------------------------
            # Step 5b: Add the incoming element (right side of window).
            #
            # The new element entering is at index `start + k - 1`.
            # -------------------------------------------------------------------
            incoming = doubled[start + k - 1]

            # If this flavor is new to the window, increment distinct count
            if incoming not in freq or freq[incoming] == 0:
                distinct_count += 1

            # Update frequency
            freq[incoming] = freq.get(incoming, 0) + 1

            # Check if this addition creates a new violation
            # (count just went from m to m+1)
            if freq[incoming] == m + 1:
                invalid_count += 1

            # -------------------------------------------------------------------
            # Step 5c: Update the answer if the current window is valid.
            # -------------------------------------------------------------------
            if invalid_count == 0:
                if distinct_count > max_distinct:
                    max_distinct = distinct_count

        # -----------------------------------------------------------------------
        # Step 6: Return the result.
        #
        # If max_distinct is still -1, no valid window was found.
        # Otherwise, return the maximum distinct flavor count found.
        # -----------------------------------------------------------------------
        return max_distinct


# -------------------------------------------------------------------------------
# Main block: Test the solution with the provided examples and additional cases.
# -------------------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # ---------------------------------------------------------------------------
    # Example 1 (from problem description):
    # flavors = [1, 2, 1, 3, 2, 4, 1], k = 4, m = 1
    #
    # Let's trace through the windows of size 4:
    # Start 0: [1, 2, 1, 3] -> flavor 1 appears 2 times > m=1, INVALID
    # Start 1: [2, 1, 3, 2] -> flavor 2 appears 2 times > m=1, INVALID
    # Start 2: [1, 3, 2, 4] -> all appear once, VALID, distinct=4
    # Start 3: [3, 2, 4, 1] -> all appear once, VALID, distinct=4
    # Start 4: [2, 4, 1, 1] -> flavor 1 appears 2 times > m=1, INVALID
    # Start 5: [4, 1, 1, 2] -> flavor 1 appears 2 times > m=1, INVALID
    # Start 6: [1, 1, 2, 1] -> flavor 1 appears 3 times > m=1, INVALID
    # (We only check starts 0 through n-1 = 6)
    #
    # Maximum distinct = 4
    # Expected output: 4
    # ---------------------------------------------------------------------------
    flavors1 = [1, 2, 1, 3, 2, 4, 1]
    k1, m1 = 4, 1
    result1 = solution.maxDistinctFlavors(flavors1, k1, m1)
    print(f"Example 1: flavors={flavors1}, k={k1}, m={m1}")
    print(f"  Result: {result1}  (Expected: 4)")
    print()

    # ---------------------------------------------------------------------------
    # Example 2 (from problem description):
    # flavors = [5, 5, 5, 5], k = 3, m = 1
    #
    # Every window of size 3 from [5,5,5,5,5,5,5,5] (doubled):
    # Start 0: [5, 5, 5] -> flavor 5 appears 3 times > m=1, INVALID
    # Start 1: [5, 5, 5] -> same, INVALID
    # Start 2: [5, 5, 5] -> same, INVALID
    # Start 3: [5, 5, 5] -> same, INVALID
    # (We only check starts 0 through n-1 = 3)
    #
    # No valid window found, return -1
    # Expected output: -1
    # ---------------------------------------------------------------------------
    flavors2 = [5, 5, 5, 5]
    k2, m2 = 3, 1
    result2 = solution.maxDistinctFlavors(flavors2, k2, m2)
    print(f"Example 2: flavors={flavors2}, k={k2}, m={m2}")
    print(f"  Result: {result2}  (Expected: -1)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 1: Circular wrap is needed for best answer
    # flavors = [3, 1, 2, 3], k = 3, m = 1
    #
    # Windows of size 3:
    # Start 0: [3, 1, 2] -> all once, VALID, distinct=3
    # Start 1: [1, 2, 3] -> all once, VALID, distinct=3
    # Start 2: [2, 3, 3] -> flavor 3 appears 2 times > m=1, INVALID
    # Start 3: [3, 3, 1] -> flavor 3 appears 2 times > m=1, INVALID
    #
    # Maximum distinct = 3
    # ---------------------------------------------------------------------------
    flavors3 = [3, 1, 2, 3]
    k3, m3 = 3, 1
    result3 = solution.maxDistinctFlavors(flavors3, k3, m3)
    print(f"Additional Test 1: flavors={flavors3}, k={k3}, m={m3}")
    print(f"  Result: {result3}  (Expected: 3)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 2: m = 2 allows more flexibility
    # flavors = [1, 2, 1, 2, 3], k = 4, m = 2
    #
    # Windows of size 4:
    # Start 0: [1, 2, 1, 2] -> 1 appears 2, 2 appears 2, both <= m=2, VALID, distinct=2
    # Start 1: [2, 1, 2, 3] -> 2 appears 2, 1 appears 1, 3 appears 1, VALID, distinct=3
    # Start 2: [1, 2, 3, 1] -> 1 appears 2, 2 appears 1, 3 appears 1, VALID, distinct=3
    # Start 3: [2, 3, 1, 2] -> 2 appears 2, 3 appears 1, 1 appears 1, VALID, distinct=3
    # Start 4: [3, 1, 2, 1] -> 1 appears 2, 3 appears 1, 2 appears 1, VALID, distinct=3
    #
    # Maximum distinct = 3
    # ---------------------------------------------------------------------------
    flavors4 = [1, 2, 1, 2, 3]
    k4, m4 = 4, 2
    result4 = solution.maxDistinctFlavors(flavors4, k4, m4)
    print(f"Additional Test 2: flavors={flavors4}, k={k4}, m={m4}")
    print(f"  Result: {result4}  (Expected: 3)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 3: Single element array
    # flavors = [7], k = 1, m = 1
    #
    # Only one window: [7], distinct=1, VALID
    # Expected: 1
    # ---------------------------------------------------------------------------
    flavors5 = [7]
    k5, m5 = 1, 1
    result5 = solution.maxDistinctFlavors(flavors5, k5, m5)
    print(f"Additional Test 3: flavors={flavors5}, k={k5}, m={m5}")
    print(f"  Result: {result5}  (Expected: 1)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 4: All distinct flavors
    # flavors = [1, 2, 3, 4, 5], k = 3, m = 1
    #
    # All windows of size 3 have all distinct elements (each appears once),
    # so all are valid. Maximum distinct = 3.
    # ---------------------------------------------------------------------------
    flavors6 = [1, 2, 3, 4, 5]
    k6, m6 = 3, 1
    result6 = solution.maxDistinctFlavors(flavors6, k6, m6)
    print(f"Additional Test 4: flavors={flavors6}, k={k6}, m={m6}")
    print(f"  Result: {result6}  (Expected: 3)")
    print()

    # ---------------------------------------------------------------------------
    # Additional Test 5: Circular wrap gives best answer
    # flavors = [4, 1, 2, 3], k = 3, m = 1
    #
    # Windows of size 3 (n=4, check starts 0..3):
    # Start 0: [4, 1, 2] -> all once, VALID, distinct=3
    # Start 1: [1, 2, 3] -> all once, VALID, distinct=3
    # Start 2: [2, 