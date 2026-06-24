"""
Title: Minimum Swaps to Cluster Priority Orders

Problem Description:
A warehouse tracks outgoing orders in an array where each value is either 0 or 1.
A value of 1 represents a priority order, and 0 represents a regular order.
To speed up loading, the warehouse wants all priority orders to appear together
in one contiguous block somewhere in the array. The block does not need to be at
the beginning or the end; it can be placed anywhere as long as all 1s are grouped
together.

In one operation, you may swap the values at any two different indices in the array.
Return the minimum number of swaps required to make all priority orders contiguous.

You are not asked to return the final arrangement, only the minimum number of swaps.

This problem is about choosing the best window of length equal to the total number
of priority orders. Inside that window, every 0 represents a regular order that
must be swapped out with a priority order from outside the window.

Constraints:
- 1 <= orders.length <= 100000
- orders[i] is either 0 or 1
- The answer fits in a 32-bit integer

Example 1:
Input: orders = [1,0,1,0,1]
Output: 1

Example 2:
Input: orders = [0,0,1,0,1,1,0]
Output: 1

If the array contains 0 or 1 priority order, the answer is 0 because all priority
orders are already contiguous.
"""

from typing import List


class Solution:
    def minSwaps(self, orders: List[int]) -> int:
        """
        Compute the minimum number of swaps needed to group all 1s into one contiguous block.

        The key idea is:
        - Let total_ones be the total number of 1s in the array.
        - If we want all 1s to become contiguous, then in the final arrangement they must occupy
          some window of length total_ones.
        - For any such window, every 0 inside that window must be swapped out with a 1 from outside.
        - Therefore, the number of swaps needed for a window is exactly the number of 0s inside it.
        - So we want the window of length total_ones that contains the fewest 0s.

        Args:
            orders: A list of integers containing only 0 and 1.

        Returns:
            The minimum number of swaps required to make all 1s contiguous.

        Time complexity:
            O(n), where n is the length of orders, because we scan the array a constant number of times.

        Space complexity:
            O(1), because we use only a few extra variables.
        """
        # Step 1: Count how many priority orders (1s) exist in the entire array.
        # This count determines the exact size of the window we need to examine.
        #
        # Why?
        # If there are, for example, 3 ones total, then in the final grouped arrangement
        # those 3 ones must occupy 3 consecutive positions. So every candidate block
        # must have length exactly 3.
        total_ones: int = sum(orders)

        # Step 2: Handle easy edge cases immediately.
        #
        # If there are 0 ones:
        # - There are no priority orders to group.
        # - They are trivially "contiguous", so answer is 0.
        #
        # If there is 1 one:
        # - A single priority order is already contiguous by itself.
        # - No swap is needed.
        if total_ones <= 1:
            return 0

        # Step 3: Build the first sliding window of length total_ones.
        #
        # We count how many zeros are inside this first window.
        # That count equals how many swaps would be needed if we choose this window
        # as the final location for all grouped 1s.
        #
        # Example 1:
        # orders = [1,0,1,0,1], total_ones = 3
        # first window = [1,0,1]
        # zeros_in_window = 1
        zeros_in_window: int = 0
        for i in range(total_ones):
            if orders[i] == 0:
                zeros_in_window += 1

        # This is our best answer seen so far.
        min_swaps: int = zeros_in_window

        # Step 4: Slide the window across the array one position at a time.
        #
        # We maintain the number of zeros in the current window efficiently:
        # - One element leaves from the left.
        # - One element enters from the right.
        #
        # Instead of recounting the whole window each time (which would be too slow),
        # we update the zero count in O(1) time per move.
        #
        # Window boundaries:
        # - Current window always has size total_ones.
        # - right is the index of the new entering element.
        # - left is the index of the old leaving element.
        for right in range(total_ones, len(orders)):
            left: int = right - total_ones

            # If the element leaving the window is 0, then the current window
            # loses one zero.
            if orders[left] == 0:
                zeros_in_window -= 1

            # If the new element entering the window is 0, then the current window
            # gains one zero.
            if orders[right] == 0:
                zeros_in_window += 1

            # After updating the window, compare against the best answer found so far.
            #
            # The minimum number of zeros in any valid window is exactly the minimum
            # number of swaps required.
            if zeros_in_window < min_swaps:
                min_swaps = zeros_in_window

        # Step 5: Return the best result.
        #
        # Why is this correct?
        # - Every final grouped arrangement of all 1s corresponds to some window
        #   of length total_ones.
        # - In that window, each 0 must be replaced by a 1 from outside via a swap.
        # - So swaps needed = number of 0s in the chosen window.
        # - Therefore, minimizing swaps is the same as minimizing zeros in such a window.
        return min_swaps


def run_example(orders: List[int]) -> None:
    """
    Run the solution on one example input and print the result.

    Args:
        orders: A list of integers containing only 0 and 1.

    Returns:
        None. Prints the input and computed answer.

    Time complexity:
        O(n), where n is the length of orders.

    Space complexity:
        O(1), excluding the input list itself.
    """
    solution = Solution()
    result: int = solution.minSwaps(orders)
    print(f"orders = {orders}")
    print(f"minimum swaps = {result}")
    print("-" * 40)


if __name__ == "__main__":
    # Example 1 from the problem statement:
    #
    # orders = [1,0,1,0,1]
    # total_ones = 3
    #
    # Windows of length 3:
    # 1) [1,0,1] -> one zero -> 1 swap
    # 2) [0,1,0] -> two zeros -> 2 swaps
    # 3) [1,0,1] -> one zero -> 1 swap
    #
    # Best answer = 1
    example_1: List[int] = [1, 0, 1, 0, 1]

    # Example 2 from the problem statement:
    #
    # orders = [0,0,1,0,1,1,0]
    # total_ones = 3
    #
    # Windows of length 3:
    # 1) [0,0,1] -> two zeros -> 2 swaps
    # 2) [0,1,0] -> two zeros -> 2 swaps
    # 3) [1,0,1] -> one zero  -> 1 swap
    # 4) [0,1,1] -> one zero  -> 1 swap
    # 5) [1,1,0] -> one zero  -> 1 swap
    #
    # Best answer = 1
    example_2: List[int] = [0, 0, 1, 0, 1, 1, 0]

    # Additional beginner-friendly edge cases:
    #
    # No priority orders.
    example_3: List[int] = [0, 0, 0, 0]

    # Only one priority order.
    example_4: List[int] = [0, 1, 0, 0]

    # Already contiguous.
    example_5: List[int] = [0, 1, 1, 1, 0]

    # Run all examples.
    run_example(example_1)
    run_example(example_2)
    run_example(example_3)
    run_example(example_4)
    run_example(example_5)