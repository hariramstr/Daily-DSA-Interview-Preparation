"""
Title: Count Balanced Shift Intervals

Problem Description:
A company records employee shift activity for a single day as an array `hours`,
where `hours[i]` is the number of hours worked during the `i`-th time block.
Management defines a time block as `heavy` if `hours[i] >= threshold`,
otherwise it is `light`.

An interval is called balanced if it contains the same number of heavy blocks
and light blocks. Your task is to return the total number of balanced intervals
in the array.

Formally, count the number of pairs `(l, r)` such that `0 <= l <= r < n` and in
the subarray `hours[l...r]`, the number of indices with `hours[i] >= threshold`
is equal to the number of indices with `hours[i] < threshold`.

This problem is intended to be solved efficiently for large inputs. A brute-force
solution that checks every subarray will be too slow. Think about converting each
element into a contribution and using prefix sums to count how many earlier
prefixes could form a balanced interval with the current position.

Constraints:
- 1 <= n == hours.length <= 2 * 10^5
- 0 <= hours[i] <= 10^9
- 0 <= threshold <= 10^9
"""

from typing import Dict, List


class Solution:
    def count_balanced_intervals(self, hours: List[int], threshold: int) -> int:
        """
        Count how many subarrays contain the same number of heavy and light blocks.

        The key idea is to convert each element into:
        - +1 if it is heavy   (hours[i] >= threshold)
        - -1 if it is light   (hours[i] < threshold)

        Then a subarray is balanced exactly when its transformed sum is 0.
        Using prefix sums, a subarray sum from l to r is 0 when:
            prefix[r + 1] == prefix[l]

        So for each current prefix sum, we count how many times the same prefix
        sum has appeared before.

        Args:
            hours: List of worked hours for each time block.
            threshold: Value that separates heavy from light blocks.

        Returns:
            The total number of balanced intervals.

        Time complexity:
            O(n), where n is the length of hours.

        Space complexity:
            O(n) in the worst case for the prefix frequency map.
        """
        # This dictionary stores how many times each prefix sum has appeared so far.
        #
        # Why do we need this?
        # If the current prefix sum is S, then every earlier position that also had
        # prefix sum S forms a balanced subarray ending at the current index.
        #
        # Example:
        # Suppose prefix sum S appeared 3 times before.
        # Then there are 3 different starting points that create a zero-sum subarray
        # ending here, so we add 3 to the answer.
        prefix_count: Dict[int, int] = {0: 1}

        # Running prefix sum of the transformed array:
        # +1 for heavy, -1 for light.
        prefix_sum: int = 0

        # Final answer: total number of balanced intervals.
        balanced_intervals: int = 0

        # Process each time block from left to right.
        for value in hours:
            # Convert the current block into +1 or -1.
            #
            # Why this transformation works:
            # - A heavy block contributes +1
            # - A light block contributes -1
            #
            # If a subarray has equal numbers of heavy and light blocks, then:
            #   (#heavy) * (+1) + (#light) * (-1) = 0
            # because #heavy == #light
            #
            # So balanced intervals become exactly the zero-sum subarrays.
            if value >= threshold:
                prefix_sum += 1
            else:
                prefix_sum -= 1

            # If this prefix sum has been seen before, then each previous occurrence
            # corresponds to one balanced subarray ending at the current position.
            #
            # Why?
            # Let current transformed prefix sum be P at position i + 1.
            # If an earlier prefix sum at position j is also P, then:
            #   transformed_sum(j ... i) = prefix[i + 1] - prefix[j] = P - P = 0
            #
            # Therefore, the subarray from j to i is balanced.
            balanced_intervals += prefix_count.get(prefix_sum, 0)

            # Record that we have now seen this prefix sum one more time.
            prefix_count[prefix_sum] = prefix_count.get(prefix_sum, 0) + 1

        return balanced_intervals


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # hours = [6, 3, 8, 2, 7], threshold = 5
    # transformed = [+1, -1, +1, -1, +1]
    # Balanced intervals:
    # [0, 1], [1, 2], [2, 3], [0, 3]
    # Expected output: 4
    hours1: List[int] = [6, 3, 8, 2, 7]
    threshold1: int = 5
    result1: int = solution.count_balanced_intervals(hours1, threshold1)
    print(result1)

    # Example 2
    # The problem statement's explanation appears inconsistent.
    # For hours = [4, 4, 9, 1], threshold = 4
    # transformed = [+1, +1, +1, -1]
    #
    # Subarrays:
    # [0,0] -> +1
    # [0,1] -> +2
    # [0,2] -> +3
    # [0,3] -> +2
    # [1,1] -> +1
    # [1,2] -> +2
    # [1,3] -> +1
    # [2,2] -> +1
    # [2,3] ->  0  <-- balanced
    # [3,3] -> -1
    #
    # So the correct answer is 1, not 2.
    hours2: List[int] = [4, 4, 9, 1]
    threshold2: int = 4
    result2: int = solution.count_balanced_intervals(hours2, threshold2)
    print(result2)