"""
Title: Count Container Pairs Within Fill Difference

Problem Description:
A shipping company records the current fill level of each storage container as an
integer percentage from 0 to 100. For a balancing operation, two containers are
considered compatible if the absolute difference between their fill levels is at
most k.

You are given an integer array fills, where fills[i] is the fill level of the
i-th container, and an integer k. Return the number of index pairs (i, j) such
that i < j and |fills[i] - fills[j]| <= k.

Because the number of valid pairs can be large, your algorithm should be efficient
enough for large inputs. A brute-force O(n^2) solution will not pass the largest
test cases.

Your task is to count all compatible pairs, not to list them.

Constraints:
- 1 <= fills.length <= 2 * 10^5
- 0 <= fills[i] <= 10^9
- 0 <= k <= 10^9
- The answer may exceed 32-bit integer range, so use a 64-bit integer type where needed.

Example 1:
Input: fills = [10, 13, 15, 19], k = 3
Output: 2

Explanation:
After checking all pairs:
- |10 - 13| = 3 -> valid
- |10 - 15| = 5 -> invalid
- |10 - 19| = 9 -> invalid
- |13 - 15| = 2 -> valid
- |13 - 19| = 6 -> invalid
- |15 - 19| = 4 -> invalid

So the answer is 2.

Example 2:
Input: fills = [7, 7, 8, 12, 13], k = 1
Output: 4

Explanation:
Valid pairs are:
- (7 at index 0, 7 at index 1)
- (7 at index 0, 8)
- (7 at index 1, 8)
- (12, 13)

So the answer is 4.
"""

from typing import List


class Solution:
    def count_compatible_pairs(self, fills: List[int], k: int) -> int:
        """
        Count the number of index pairs (i, j) such that i < j and
        |fills[i] - fills[j]| <= k.

        The method sorts the array and then uses a two-pointer sliding window.
        For each right pointer position, it finds the smallest left pointer
        position such that the difference between fills[right] and fills[left]
        is at most k. All elements between left and right - 1 then form valid
        pairs with right.

        Args:
            fills: List of container fill levels.
            k: Maximum allowed absolute difference.

        Returns:
            The total number of valid pairs.

        Time complexity:
            O(n log n), due to sorting. The two-pointer scan itself is O(n).

        Space complexity:
            O(n) in Python because sorted() creates a new list.
        """
        # Step 1:
        # Sort the fill levels.
        #
        # Why sorting helps:
        # The condition involves absolute difference:
        #     |fills[i] - fills[j]| <= k
        #
        # Once the array is sorted, for any pair where left < right, we know:
        #     sorted_fills[right] >= sorted_fills[left]
        #
        # So the absolute value is no longer needed:
        #     |sorted_fills[right] - sorted_fills[left]|
        # becomes
        #     sorted_fills[right] - sorted_fills[left]
        #
        # This makes the condition much easier to manage with two pointers.
        sorted_fills: List[int] = sorted(fills)

        # This variable will store the final answer.
        #
        # In Python, int automatically grows as needed, so it safely handles
        # values larger than 32-bit range.
        total_pairs: int = 0

        # The left pointer marks the beginning of the current valid window.
        #
        # For each position "right", we want the smallest "left" such that:
        #     sorted_fills[right] - sorted_fills[left] <= k
        #
        # Then every index from left to right - 1 forms a valid pair with right.
        left: int = 0

        # Step 2:
        # Move the right pointer from left to right across the sorted array.
        #
        # At each step, we expand the window by including sorted_fills[right].
        for right in range(len(sorted_fills)):
            # Step 3:
            # If the current window is invalid, move the left pointer forward
            # until the window becomes valid again.
            #
            # The window [left, right] is valid if:
            #     sorted_fills[right] - sorted_fills[left] <= k
            #
            # If it is larger than k, then the leftmost value is too far away
            # from the current right value, so it cannot form a valid pair with
            # this right. Because the array is sorted, no earlier left would help,
            # so we must move left forward.
            while sorted_fills[right] - sorted_fills[left] > k:
                left += 1

            # Step 4:
            # Count how many valid pairs end at index "right".
            #
            # After the while-loop finishes, the window [left, right] satisfies:
            #     sorted_fills[right] - sorted_fills[left] <= k
            #
            # Since the array is sorted, every element between left and right
            # is at least as large as sorted_fills[left], so each one also
            # differs from sorted_fills[right] by at most k.
            #
            # Therefore, the valid pairs ending at "right" are:
            #     (left, right), (left + 1, right), ..., (right - 1, right)
            #
            # The number of such pairs is:
            #     right - left
            #
            # Example:
            # If left = 2 and right = 5, then valid pair starts are 2, 3, 4.
            # That is 3 pairs, which equals 5 - 2.
            total_pairs += right - left

        # Step 5:
        # Return the total number of valid pairs found.
        return total_pairs

    def countPairs(self, fills: List[int], k: int) -> int:
        """
        Wrapper method matching a common interview/platform naming style.

        Args:
            fills: List of container fill levels.
            k: Maximum allowed absolute difference.

        Returns:
            The total number of valid pairs.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        return self.count_compatible_pairs(fills, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    fills1: List[int] = [10, 13, 15, 19]
    k1: int = 3
    result1: int = solution.countPairs(fills1, k1)
    print("Example 1:")
    print("fills =", fills1)
    print("k =", k1)
    print("Output =", result1)
    print("Expected = 2")
    print()

    # Example 2 from the problem statement
    fills2: List[int] = [7, 7, 8, 12, 13]
    k2: int = 1
    result2: int = solution.countPairs(fills2, k2)
    print("Example 2:")
    print("fills =", fills2)
    print("k =", k2)
    print("Output =", result2)
    print("Expected = 4")
    print()

    # Additional small sanity checks for beginners
    fills3: List[int] = [5]
    k3: int = 10
    result3: int = solution.countPairs(fills3, k3)
    print("Additional Test 1:")
    print("fills =", fills3)
    print("k =", k3)
    print("Output =", result3)
    print("Expected = 0")
    print()

    fills4: List[int] = [1, 1, 1, 1]
    k4: int = 0
    result4: int = solution.countPairs(fills4, k4)
    print("Additional Test 2:")
    print("fills =", fills4)
    print("k =", k4)
    print("Output =", result4)
    print("Expected = 6")
    print()

    fills5: List[int] = [1, 10, 20, 30]
    k5: int = 5
    result5: int = solution.countPairs(fills5, k5)
    print("Additional Test 3:")
    print("fills =", fills5)
    print("k =", k5)
    print("Output =", result5)
    print("Expected = 0")