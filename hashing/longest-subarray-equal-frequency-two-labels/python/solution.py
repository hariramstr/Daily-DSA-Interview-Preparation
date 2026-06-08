```python
"""
Title: Find Longest Subarray with Equal Frequency of Two Labels
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given an array of strings `labels` where each element is either 'A' or 'B'.
Your task is to find the length of the longest contiguous subarray in which the number
of 'A's equals the number of 'B's.

This is a classic balance-tracking problem: assign +1 for each 'A' and -1 for each 'B',
then find the longest subarray with a prefix sum of 0. Use a hash map to record the
first occurrence of each prefix sum value.

Constraints:
- 1 <= labels.length <= 10^5
- labels[i] is either 'A' or 'B'

Example 1:
Input: labels = ["A", "B", "B", "A", "A", "B", "A"]
Output: 6
Explanation: labels[0..5] = ["A", "B", "B", "A", "A", "B"] has 3 A's and 3 B's.

Example 2:
Input: labels = ["A", "A", "A", "B"]
Output: 2
Explanation: labels[2..3] = ["A", "B"] has 1 A and 1 B.
"""

from typing import List, Dict


class Solution:
    def findLongestSubarray(self, labels: List[str]) -> int:
        """
        Find the length of the longest contiguous subarray with equal 'A's and 'B's.

        Core Idea:
        - Treat 'A' as +1 and 'B' as -1.
        - Compute a running prefix sum as we scan left to right.
        - If prefix_sum[i] == prefix_sum[j] for i < j, then the subarray
          labels[i+1 .. j] has equal counts of 'A' and 'B' (the +1s and -1s cancel).
        - We want to maximize (j - i), so we store the FIRST time we see each
          prefix sum value in a hash map.

        Args:
            labels: A list of strings, each either 'A' or 'B'.

        Returns:
            The length of the longest subarray with equal frequency of 'A' and 'B'.

        Time Complexity:  O(n) — single pass through the array.
        Space Complexity: O(n) — hash map stores at most n+1 distinct prefix sums.
        """

        # -----------------------------------------------------------------------
        # Step 1: Initialize the prefix-sum hash map.
        #
        # We map each prefix_sum value to the EARLIEST index at which it occurred.
        # We seed the map with {0: -1} to handle the edge case where the entire
        # prefix from index 0 to j is balanced (prefix_sum[j] == 0).
        # In that case, the subarray length is j - (-1) = j + 1, which is correct.
        # -----------------------------------------------------------------------
        first_occurrence: Dict[int, int] = {0: -1}

        # -----------------------------------------------------------------------
        # Step 2: Initialize tracking variables.
        #
        # - prefix_sum: the running balance (+1 for 'A', -1 for 'B').
        # - max_length: the best (longest) balanced subarray length found so far.
        # -----------------------------------------------------------------------
        prefix_sum: int = 0
        max_length: int = 0

        # -----------------------------------------------------------------------
        # Step 3: Iterate through each label with its index.
        #
        # For every position i, we:
        #   a) Update the prefix sum based on the current label.
        #   b) Check if this prefix sum was seen before.
        #      - If YES: the subarray between the first occurrence and now is balanced.
        #        Compute its length and update max_length if it's larger.
        #      - If NO: record this prefix sum with the current index as its first
        #        occurrence (we only store the FIRST occurrence to maximize length).
        # -----------------------------------------------------------------------
        for i, label in enumerate(labels):

            # --- 3a: Update the running balance ---
            # 'A' contributes +1 (more A's push balance up)
            # 'B' contributes -1 (more B's push balance down)
            if label == 'A':
                prefix_sum += 1
            else:  # label == 'B'
                prefix_sum -= 1

            # --- 3b: Check if this prefix sum has been seen before ---
            if prefix_sum in first_occurrence:
                # The subarray from (first_occurrence[prefix_sum] + 1) to i
                # has a net balance of 0, meaning equal A's and B's.
                #
                # Length = i - first_occurrence[prefix_sum]
                # Example: if prefix_sum was first seen at index -1 (our sentinel)
                # and we're now at index 5, length = 5 - (-1) = 6. ✓
                current_length = i - first_occurrence[prefix_sum]
                max_length = max(max_length, current_length)
                # IMPORTANT: We do NOT update first_occurrence[prefix_sum] here.
                # Keeping the earliest index maximizes the potential subarray length.
            else:
                # First time seeing this prefix sum — record it.
                first_occurrence[prefix_sum] = i

        # -----------------------------------------------------------------------
        # Step 4: Return the maximum balanced subarray length found.
        # -----------------------------------------------------------------------
        return max_length

    def findLongestSubarrayKLabels(self, labels: List[str]) -> int:
        """
        Follow-up: Generalize to k distinct labels, finding the longest subarray
        where ALL distinct labels appear with equal frequency.

        Approach:
        - Count frequencies of each label as we scan.
        - Normalize the frequency vector by subtracting the count of the first
          (or any reference) label from all others, creating a "balance tuple".
        - If the same balance tuple appears at two indices, the subarray between
          them has equal frequencies for all labels.
        - Store the first occurrence of each balance tuple in a hash map.

        Args:
            labels: A list of strings with k distinct label types.

        Returns:
            The length of the longest subarray where all labels appear equally often.

        Time Complexity:  O(n * k) — building the balance tuple at each step costs O(k).
        Space Complexity: O(n * k) — storing tuples of size k in the hash map.
        """

        # -----------------------------------------------------------------------
        # Step 1: Discover all distinct labels and assign them a consistent order.
        # -----------------------------------------------------------------------
        distinct_labels: List[str] = sorted(set(labels))
        k = len(distinct_labels)

        # If there's only one distinct label, no balanced subarray exists
        # (we need at least two labels to have "equal frequency of two labels").
        if k == 1:
            return 0

        # Map each label to an index for fast lookup.
        label_index: Dict[str, int] = {lbl: idx for idx, lbl in enumerate(distinct_labels)}

        # -----------------------------------------------------------------------
        # Step 2: Initialize frequency counts and the hash map.
        #
        # The "balance tuple" is computed relative to the first label's count.
        # When all labels have equal frequency, all differences are 0.
        # We seed the map with the zero-tuple at virtual index -1.
        # -----------------------------------------------------------------------
        freq: List[int] = [0] * k
        zero_tuple: tuple = tuple([0] * (k - 1))
        first_occurrence: Dict[tuple, int] = {zero_tuple: -1}

        max_length: int = 0

        # -----------------------------------------------------------------------
        # Step 3: Scan through labels, updating frequencies and checking balance.
        # -----------------------------------------------------------------------
        for i, label in enumerate(labels):
            # Update the frequency of the current label.
            freq[label_index[label]] += 1

            # Build the balance tuple: differences of each label's count
            # relative to the first label's count.
            # When all are equal, all differences are 0.
            ref = freq[0]
            balance: tuple = tuple(freq[j] - ref for j in range(1, k))

            if balance in first_occurrence:
                current_length = i - first_occurrence[balance]
                max_length = max(max_length, current_length)
            else:
                first_occurrence[balance] = i

        return max_length


# -------------------------------------------------------------------------------
# Trace-through verification before running:
#
# Example 1: labels = ["A", "B", "B", "A", "A", "B", "A"]
# first_occurrence = {0: -1}, prefix_sum = 0, max_length = 0
#
# i=0, label='A': prefix_sum = 1. Not in map → first_occurrence[1] = 0
# i=1, label='B': prefix_sum = 0. In map (0: -1) → length = 1-(-1) = 2. max=2
# i=2, label='B': prefix_sum = -1. Not in map → first_occurrence[-1] = 2
# i=3, label='A': prefix_sum = 0. In map (0: -1) → length = 3-(-1) = 4. max=4
# i=4, label='A': prefix_sum = 1. In map (1: 0) → length = 4-0 = 4. max=4
# i=5, label='B': prefix_sum = 0. In map (0: -1) → length = 5-(-1) = 6. max=6 ✓
# i=6, label='A': prefix_sum = 1. In map (1: 0) → length = 6-0 = 6. max=6
# Result: 6 ✓
#
# Example 2: labels = ["A", "A", "A", "B"]
# first_occurrence = {0: -1}, prefix_sum = 0, max_length = 0
#
# i=0, label='A': prefix_sum = 1. Not in map → first_occurrence[1] = 0
# i=1, label='A': prefix_sum = 2. Not in map → first_occurrence[2] = 1
# i=2, label='A': prefix_sum = 3. Not in map → first_occurrence[3] = 2
# i=3, label='B': prefix_sum = 2. In map (2: 1) → length = 3-1 = 2. max=2 ✓
# Result: 2 ✓
# -------------------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Test Case 1 (from problem description)
    # Expected Output: 6
    # ------------------------------------------------------------------
    labels1 = ["A", "B", "B", "A", "A", "B", "A"]
    result1 = solution.findLongestSubarray(labels1)
    print(f"Test 1 - Input:  {labels1}")
    print(f"Test 1 - Output: {result1}  (Expected: 6)")
    print()

    # ------------------------------------------------------------------
    # Test Case 2 (from problem description)
    # Expected Output: 2
    # ------------------------------------------------------------------
    labels2 = ["A", "A", "A", "B"]
    result2 = solution.findLongestSubarray(labels2)
    print(f"Test 2 - Input:  {labels2}")
    print(f"Test 2 - Output: {result2}  (Expected: 2)")
    print()

    # ------------------------------------------------------------------
    # Test Case 3: All A's — no balanced subarray possible
    # Expected Output: 0
    # ------------------------------------------------------------------
    labels3 = ["A", "A", "A", "A"]
    result3 = solution.findLongestSubarray(labels3)
    print(f"Test 3 - Input:  {labels3}")
    print(f"Test 3 - Output: {result3}  (Expected: 0)")
    print()

    # ------------------------------------------------------------------
    # Test Case 4: Alternating A and B — entire array is balanced
    # Expected Output: 6
    # ------------------------------------------------------------------
    labels4 = ["A", "B", "A", "B", "A", "B"]
    result4 = solution.findLongestSubarray(labels4)
    print(f"Test 4 - Input:  {labels4}")
    print(f"Test 4 - Output: {result4}  (Expected: 6)")
    print()

    # ------------------------------------------------------------------
    # Test Case 5: Single element — no balanced subarray
    # Expected Output: 0
    # ------------------------------------------------------------------
    labels5 = ["A"]
    result5 = solution.findLongestSubarray(labels5)
    print(f"Test 5 - Input:  {labels5}")
    print(f"Test 5 - Output: {result5}  (Expected: 0)")
    print()

    # ------------------------------------------------------------------
    # Follow-up: k-label generalization
    # labels = ["A", "B", "C", "A", "B", "C"]
    # The entire array has 2 A's, 2 B's, 2 C's → length 6
    # Expected Output: 6
    # ------------------------------------------------------------------
    print("--- Follow-up: k-label generalization ---")
    labels6 = ["A", "B", "C", "A", "B", "C"]
    result6 = solution.findLongestSubarrayKLabels(labels6)
    print(f"Follow-up Test 1 - Input:  {labels6}")
    print(f"Follow-up Test 1 - Output: {result6}  (Expected: 6)")
    print()

    # labels = ["A", "A", "B", "C"]
    # Subarray ["A", "B", "C"] (indices 1-3) has 1 each → length 3
    # Expected Output: 3
    labels7 = ["A", "A", "B", "C"]
    result7 = solution.findLongestSubarrayKLabels(labels7)
    print(f"Follow-up Test 2 - Input:  {labels7}")
    print(f"Follow-up Test 2 - Output: {result7}  (Expected: 3)")
```