"""
Title: Minimum Swaps to Group Delayed Flights

Problem Description:
An airport operations dashboard stores the status of flights in a binary array
`flights`, where `flights[i] = 1` means the `i`-th flight is delayed and
`flights[i] = 0` means it is on time. For reporting purposes, the airport wants
all delayed flights to appear together in one contiguous block in the array.
You may swap the values at any two different indices, and each swap counts as
one operation.

Return the minimum number of swaps needed to group all delayed flights together.

If there are no delayed flights, or there is only one delayed flight, the answer
is `0` because they are already trivially grouped.

A useful way to think about the problem is that if there are `k` delayed flights
in total, then the final grouped block must have length `k`. For any candidate
block of length `k`, every on-time flight inside that block would need to be
swapped with a delayed flight outside the block. Your task is to find the best
such block.

Constraints:
- 1 <= flights.length <= 100000
- flights[i] is either 0 or 1

Example 1:
Input: flights = [1,0,1,0,1]
Output: 1

Example 2:
Input: flights = [0,0,1,0,1,1,0]
Output: 1
"""

from typing import List


class Solution:
    def min_swaps(self, flights: List[int]) -> int:
        """
        Compute the minimum number of swaps needed to group all delayed flights
        (represented by 1s) into one contiguous block.

        The key idea:
        - Let k be the total number of delayed flights (total number of 1s).
        - In the final arrangement, all delayed flights must occupy some
          contiguous window of length k.
        - For any such window, every 0 inside that window must be swapped with
          a 1 outside the window.
        - Therefore, the number of swaps needed for a window is exactly the
          number of 0s inside it.
        - So we want the window of length k with the fewest 0s.

        Args:
            flights: A binary list where 1 means delayed and 0 means on time.

        Returns:
            The minimum number of swaps required.

        Time complexity:
            O(n), where n is the length of flights.

        Space complexity:
            O(1), ignoring input storage.
        """
        # Step 1: Count how many delayed flights exist in total.
        #
        # Why this matters:
        # If there are k delayed flights, then after grouping them together,
        # they must occupy exactly k consecutive positions.
        #
        # Example:
        # flights = [1, 0, 1, 0, 1]
        # total_delayed = 3
        # So we only need to inspect windows of length 3.
        total_delayed: int = sum(flights)

        # Step 2: Handle trivial cases early.
        #
        # If there are 0 delayed flights:
        # - There is nothing to group.
        #
        # If there is 1 delayed flight:
        # - A single 1 is already "grouped" by itself.
        #
        # In both cases, no swaps are needed.
        if total_delayed <= 1:
            return 0

        # Step 3: Build the first sliding window of size total_delayed.
        #
        # We want to know how many on-time flights (0s) are inside this window,
        # because each such 0 would need to be swapped out with a 1 from outside.
        #
        # Instead of counting zeros directly, we count how many 1s are inside
        # the window. Then:
        # zeros_in_window = window_size - ones_in_window
        #
        # This is efficient and easy to update while sliding.
        window_ones: int = sum(flights[:total_delayed])

        # The number of swaps needed for the first window is the number of 0s
        # inside it.
        min_swaps_needed: int = total_delayed - window_ones

        # Step 4: Slide the window across the array one position at a time.
        #
        # Sliding window technique:
        # - Remove the element that leaves the window.
        # - Add the element that enters the window.
        #
        # This lets us update the count in O(1) time per move, instead of
        # recomputing the whole window every time.
        #
        # Window boundaries:
        # - The first window is flights[0 : total_delayed]
        # - The next window is flights[1 : total_delayed + 1]
        # - Continue until the last valid window.
        for right in range(total_delayed, len(flights)):
            # The leftmost index of the previous window that is now leaving.
            left: int = right - total_delayed

            # Remove the contribution of the element leaving the window.
            window_ones -= flights[left]

            # Add the contribution of the new element entering the window.
            window_ones += flights[right]

            # Compute how many 0s are in the current window.
            #
            # Why this equals swaps:
            # Every 0 inside the target block must be replaced by a 1 from
            # outside the block. Since swaps can happen between any two indices,
            # each such mismatch can be fixed with one swap.
            current_swaps_needed: int = total_delayed - window_ones

            # Keep the best (smallest) answer seen so far.
            if current_swaps_needed < min_swaps_needed:
                min_swaps_needed = current_swaps_needed

        # Step 5: Return the minimum number of swaps among all candidate windows.
        return min_swaps_needed


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    flights_1: List[int] = [1, 0, 1, 0, 1]
    result_1: int = solution.min_swaps(flights_1)
    print(f"Input: {flights_1}")
    print(f"Minimum swaps needed: {result_1}")
    print()

    # Example 2
    flights_2: List[int] = [0, 0, 1, 0, 1, 1, 0]
    result_2: int = solution.min_swaps(flights_2)
    print(f"Input: {flights_2}")
    print(f"Minimum swaps needed: {result_2}")
    print()

    # Additional beginner-friendly test cases

    # No delayed flights
    flights_3: List[int] = [0, 0, 0, 0]
    result_3: int = solution.min_swaps(flights_3)
    print(f"Input: {flights_3}")
    print(f"Minimum swaps needed: {result_3}")
    print()

    # Only one delayed flight
    flights_4: List[int] = [0, 1, 0, 0]
    result_4: int = solution.min_swaps(flights_4)
    print(f"Input: {flights_4}")
    print(f"Minimum swaps needed: {result_4}")
    print()

    # Already grouped
    flights_5: List[int] = [0, 1, 1, 1, 0]
    result_5: int = solution.min_swaps(flights_5)
    print(f"Input: {flights_5}")
    print(f"Minimum swaps needed: {result_5}")
    print()

    # Mixed case
    flights_6: List[int] = [1, 0, 0, 1, 0, 1, 1]
    result_6: int = solution.min_swaps(flights_6)
    print(f"Input: {flights_6}")
    print(f"Minimum swaps needed: {result_6}")