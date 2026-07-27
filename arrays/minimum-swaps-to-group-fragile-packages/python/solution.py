"""
Title: Minimum Swaps to Group Fragile Packages

Problem Description:
A warehouse stores packages in a single row represented by an integer array `packages`,
where `packages[i] = 1` means the package at position `i` is fragile and
`packages[i] = 0` means it is not fragile.

For safety inspection, all fragile packages should be placed next to each other in one
contiguous block. In one operation, you may swap the contents of any two positions in
the array.

Return the minimum number of swaps needed to group all fragile packages together.

You are not required to preserve the relative order of packages. If there are zero or
one fragile packages, the answer is `0` because they are already trivially grouped.

Key observation:
If there are `k` fragile packages in total, then in the final arrangement all fragile
packages must occupy some contiguous window of length `k`.

For any chosen window of length `k`:
- Every fragile package already inside that window is already in a good position.
- Every non-fragile package (`0`) inside that window must be swapped out.
- Each such `0` can be swapped with a fragile package (`1`) outside the window.

So, for a given window, the number of swaps needed is:
    window_size - number_of_fragile_packages_in_window
which is:
    k - number_of_ones_in_window

Therefore, to minimize swaps, we should find the window of length `k` that contains
the maximum number of fragile packages (`1`s).

Constraints:
- 1 <= packages.length <= 100000
- packages[i] is either 0 or 1

Example 1:
Input: packages = [1,0,1,0,1]
Output: 1

Example 2:
Input: packages = [0,0,1,0,1,1,0]
Output: 1
"""

from typing import List


class Solution:
    def minSwaps(self, packages: List[int]) -> int:
        """
        Compute the minimum number of swaps needed to group all fragile packages
        (represented by 1s) into one contiguous block.

        The method uses a sliding window:
        1. Count how many fragile packages exist in total. Let that count be k.
        2. Any valid final grouped arrangement must occupy a window of length k.
        3. For each window of length k, count how many 1s are already inside it.
        4. The best window is the one with the maximum number of 1s.
        5. The minimum swaps needed is the number of 0s in that best window:
           k - max_ones_in_any_window

        Args:
            packages: A list of integers where 1 means fragile and 0 means not fragile.

        Returns:
            The minimum number of swaps needed to group all fragile packages together.

        Time complexity:
            O(n), where n is the length of packages.
            We count total 1s once, build the first window once, and slide across the array once.

        Space complexity:
            O(1), ignoring input storage, because we use only a few variables.
        """
        # Step 1: Count the total number of fragile packages.
        #
        # Why?
        # If there are k fragile packages total, then after grouping them together,
        # they must occupy exactly k consecutive positions.
        #
        # Example:
        # packages = [1, 0, 1, 0, 1]
        # total_fragile = 3
        # So the final grouped block must be some window of length 3.
        total_fragile: int = sum(packages)

        # Step 2: Handle easy edge cases.
        #
        # If there are 0 fragile packages:
        # - There is nothing to group.
        #
        # If there is 1 fragile package:
        # - A single package is already trivially contiguous by itself.
        #
        # In both cases, no swaps are needed.
        if total_fragile <= 1:
            return 0

        # Step 3: Build the first sliding window of length total_fragile.
        #
        # We want to know how many fragile packages are already inside this window.
        # The more 1s already inside, the fewer swaps we need.
        #
        # For the first window, we simply sum the first `total_fragile` elements.
        current_ones_in_window: int = sum(packages[:total_fragile])

        # This variable tracks the best window seen so far:
        # specifically, the maximum number of 1s found in any window of size total_fragile.
        max_ones_in_window: int = current_ones_in_window

        # Step 4: Slide the window across the array one position at a time.
        #
        # Sliding window idea:
        # Instead of recounting every window from scratch, we update the count efficiently:
        #
        # New window = old window
        #              - element leaving from the left
        #              + element entering from the right
        #
        # This keeps the algorithm O(n) instead of O(n * k).
        #
        # The right end of the window starts at index total_fragile and moves to the end.
        for right in range(total_fragile, len(packages)):
            # The leftmost element of the previous window is the one that leaves.
            left: int = right - total_fragile

            # Remove the contribution of the outgoing element.
            current_ones_in_window -= packages[left]

            # Add the contribution of the incoming element.
            current_ones_in_window += packages[right]

            # Update the best window if this one contains more fragile packages.
            if current_ones_in_window > max_ones_in_window:
                max_ones_in_window = current_ones_in_window

        # Step 5: Compute the answer.
        #
        # In the best window of length total_fragile:
        # - max_ones_in_window positions already contain fragile packages
        # - the remaining positions are non-fragile packages that must be swapped out
        #
        # Therefore:
        # minimum swaps = total_fragile - max_ones_in_window
        #
        # Why is this correct?
        # Every 0 inside the chosen window corresponds to a 1 outside the window
        # that needs to be brought in. Since we can swap any two positions,
        # each such mismatch can be fixed with exactly one swap.
        return total_fragile - max_ones_in_window


def run_demo(packages: List[int]) -> None:
    """
    Run the solution on one sample input and print the result.

    Args:
        packages: The input array representing fragile and non-fragile packages.

    Returns:
        None. This function prints the input and computed answer.

    Time complexity:
        O(n), because it calls the main algorithm once.

    Space complexity:
        O(1), excluding the input list.
    """
    solution = Solution()
    result = solution.minSwaps(packages)
    print(f"packages = {packages}")
    print(f"minimum swaps = {result}")
    print("-" * 50)


if __name__ == "__main__":
    # Example 1 from the problem statement:
    #
    # packages = [1, 0, 1, 0, 1]
    # total fragile = 3
    #
    # Windows of length 3:
    # [1, 0, 1] -> 2 ones -> 1 swap needed
    # [0, 1, 0] -> 1 one  -> 2 swaps needed
    # [1, 0, 1] -> 2 ones -> 1 swap needed
    #
    # Best window has 2 ones, so answer = 3 - 2 = 1
    sample_1: List[int] = [1, 0, 1, 0, 1]

    # Example 2 from the problem statement:
    #
    # packages = [0, 0, 1, 0, 1, 1, 0]
    # total fragile = 3
    #
    # Windows of length 3:
    # [0, 0, 1] -> 1 one  -> 2 swaps needed
    # [0, 1, 0] -> 1 one  -> 2 swaps needed
    # [1, 0, 1] -> 2 ones -> 1 swap needed
    # [0, 1, 1] -> 2 ones -> 1 swap needed
    # [1, 1, 0] -> 2 ones -> 1 swap needed
    #
    # Best window has 2 ones, so answer = 3 - 2 = 1
    sample_2: List[int] = [0, 0, 1, 0, 1, 1, 0]

    # Additional beginner-friendly checks:
    sample_3: List[int] = [0, 0, 0, 0]      # No fragile packages -> 0
    sample_4: List[int] = [1, 0, 0, 0]      # One fragile package -> 0
    sample_5: List[int] = [1, 1, 1, 1]      # Already grouped -> 0
    sample_6: List[int] = [1, 0, 1, 1, 0]   # total fragile = 3, best window has 2 ones -> 1

    run_demo(sample_1)
    run_demo(sample_2)
    run_demo(sample_3)
    run_demo(sample_4)
    run_demo(sample_5)
    run_demo(sample_6)