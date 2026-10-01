"""
Title: Count Apartment Pairs Within Noise Difference

Problem Description:
You are given an array `noise` where `noise[i]` represents the measured nighttime
noise level of the `i`th apartment in a building. You are also given an integer
`limit`. Two apartments form a compatible pair if the absolute difference between
their noise levels is less than or equal to `limit`.

Your task is to return the total number of distinct compatible pairs `(i, j)` such
that `0 <= i < j < n` and `|noise[i] - noise[j]| <= limit`.

Because the input size can be large, an efficient solution is required. A brute-force
approach that checks every pair will be too slow. Think about how sorting and a
two-pointer technique can help count many valid pairs at once.

Constraints:
- 1 <= noise.length <= 200000
- 0 <= noise[i] <= 1000000000
- 0 <= limit <= 1000000000
- The answer may be large, so return it as a 64-bit integer.
"""

from typing import List


class Solution:
    def count_compatible_pairs(self, noise: List[int], limit: int) -> int:
        """
        Count the number of distinct apartment pairs whose noise difference
        is less than or equal to the given limit.

        Args:
            noise: List of apartment noise levels.
            limit: Maximum allowed absolute difference for a compatible pair.

        Returns:
            Total number of valid pairs as an integer.

        Time Complexity:
            O(n log n), due to sorting the array once. The two-pointer scan is O(n).

        Space Complexity:
            O(n) in Python because sorted() creates a new list. The pointer scan
            itself uses O(1) extra space.
        """
        # Step 1: Sort the noise levels.
        #
        # Why sort?
        # ----------
        # The condition involves an absolute difference:
        #     |noise[i] - noise[j]| <= limit
        #
        # After sorting, for any pair with left index < right index, we know:
        #     sorted_noise[right] >= sorted_noise[left]
        #
        # So the absolute difference becomes:
        #     sorted_noise[right] - sorted_noise[left]
        #
        # This removes the need to consider both directions and makes the condition
        # monotonic, which is exactly what allows a two-pointer solution.
        sorted_noise: List[int] = sorted(noise)

        # This variable will store the total number of valid pairs.
        total_pairs: int = 0

        # Step 2: Use a sliding window / two-pointer approach.
        #
        # We maintain a window [left, right] such that every value inside the window
        # differs from sorted_noise[right] by at most `limit`.
        #
        # For each `right`, once we find the smallest valid `left`, then every index
        # from `left` to `right - 1` forms a valid pair with `right`.
        #
        # Number of such pairs is:
        #     right - left
        #
        # This is the key optimization:
        # instead of checking each pair individually, we count many at once.
        left: int = 0

        # Move `right` from left to right across the sorted array.
        for right in range(len(sorted_noise)):
            # Step 3: Shrink the window from the left while it is invalid.
            #
            # If the difference between the current largest value in the window
            # (sorted_noise[right]) and the smallest value in the window
            # (sorted_noise[left]) is too large, then `left` cannot stay in the
            # valid window, so we move it forward.
            #
            # Because the array is sorted, if sorted_noise[right] - sorted_noise[left]
            # is too large, then any even smaller index would also be invalid.
            while sorted_noise[right] - sorted_noise[left] > limit:
                left += 1

            # Step 4: Count how many valid pairs end at index `right`.
            #
            # At this point, the window [left, right] is valid, meaning:
            #     sorted_noise[right] - sorted_noise[k] <= limit
            # for every k in [left, right].
            #
            # We do not pair `right` with itself, so valid partner indices are:
            #     left, left + 1, ..., right - 1
            #
            # Count of these indices:
            #     right - left
            #
            # Add that count to the answer.
            total_pairs += right - left

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    #
    # Original input:
    # noise = [12, 7, 10, 15], limit = 3
    #
    # All index pairs:
    # (0,1): |12-7|  = 5  -> invalid
    # (0,2): |12-10| = 2  -> valid
    # (0,3): |12-15| = 3  -> valid
    # (1,2): |7-10|  = 3  -> valid
    # (1,3): |7-15|  = 8  -> invalid
    # (2,3): |10-15| = 5  -> invalid
    #
    # Total valid pairs = 3
    #
    # Note:
    # The problem statement's listed output/explanation for Example 1 is inconsistent.
    # The mathematically correct answer for the given input is 3.
    noise1: List[int] = [12, 7, 10, 15]
    limit1: int = 3
    result1: int = solution.count_compatible_pairs(noise1, limit1)
    print("Example 1 Result:", result1)  # Correct result: 3

    # Example 2
    #
    # noise = [4, 4, 4, 9], limit = 0
    #
    # Valid pairs among the three 4's:
    # (0,1), (0,2), (1,2) => 3 pairs
    noise2: List[int] = [4, 4, 4, 9]
    limit2: int = 0
    result2: int = solution.count_compatible_pairs(noise2, limit2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick sanity checks
    noise3: List[int] = [1]
    limit3: int = 5
    result3: int = solution.count_compatible_pairs(noise3, limit3)
    print("Single Apartment Result:", result3)  # Expected: 0

    noise4: List[int] = [1, 2, 3, 4]
    limit4: int = 10
    result4: int = solution.count_compatible_pairs(noise4, limit4)
    print("All Pairs Valid Result:", result4)  # Expected: 6