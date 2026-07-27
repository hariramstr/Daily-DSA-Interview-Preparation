"""
Title: Minimum Reservation Window for Conference Rooms

Problem Description:
A company wants to reserve identical conference rooms for a large training event.
There are n available time blocks, and the i-th block can host at most rooms[i]
rooms if the reservation window is W minutes long. However, room providers impose
a setup overhead: each provider can only contribute floor(blockLength[i] / W)
rooms to the event, where blockLength[i] is the total number of minutes that
provider can offer and W must be the same for every provider.

You are given an array blockLength where each value represents the total
reservable minutes from one provider, and an integer k representing the number
of rooms that must be created.

Return the minimum positive integer reservation window W such that it is possible
to create at least k rooms in total, where each room must receive exactly W
minutes from a single provider. If it is impossible to create k rooms even with
W = 1, return -1.

More formally, find the smallest integer W >= 1 such that:
    sum(floor(blockLength[i] / W)) >= k

Important note about the examples:
The mathematical statement above asks for the minimum positive integer W.
Because W = 1 is the smallest allowed positive integer, whenever creating at
least k rooms is possible at all, W = 1 will always satisfy the condition.
Therefore, under the problem statement exactly as written, the answer is:
- 1 if sum(blockLength) >= k
- -1 otherwise

This implementation follows the problem statement exactly and also includes a
binary-search-based helper to demonstrate the monotonic feasibility idea
mentioned in the prompt. However, the main returned answer must match the formal
definition, so it returns 1 when feasible and -1 when impossible.
"""

from typing import List


class Solution:
    def can_make_at_least_k_rooms(
        self,
        block_length: List[int],
        k: int,
        window: int,
    ) -> bool:
        """
        Check whether a given reservation window size can produce at least k rooms.

        Args:
            block_length: List of total reservable minutes from each provider.
            k: Required number of rooms.
            window: Candidate reservation window size W.

        Returns:
            True if sum(floor(block_length[i] / window)) >= k, otherwise False.

        Time complexity:
            O(n), where n is the number of providers.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We accumulate how many rooms can be formed using the given window size.
        # Each provider contributes floor(total_minutes / window) rooms.
        total_rooms: int = 0

        for minutes in block_length:
            total_rooms += minutes // window

            # Early stopping optimization:
            # As soon as we already have at least k rooms, we can return True.
            # This avoids unnecessary work on very large inputs.
            if total_rooms >= k:
                return True

        return False

    def binary_search_boundary_window(self, block_length: List[int], k: int) -> int:
        """
        Demonstrate the monotonic binary search boundary described in the prompt.

        This method finds the largest window W such that:
            sum(floor(block_length[i] / W)) >= k

        Why largest and not smallest?
        Because the feasibility condition is:
            feasible(W) = sum(floor(block_length[i] / W)) >= k
        This condition is monotonic in the following direction:
            - If a larger W is feasible, then every smaller W is also feasible.
            - Therefore, the feasible region is a prefix [1 ... answer].
        In such a situation, binary search naturally finds the maximum feasible W,
        which is the interesting boundary value.

        Note:
        This helper is included because the prompt discusses monotonic feasibility
        and binary search. However, the formal problem statement asks for the
        minimum W, which is always 1 whenever any feasible solution exists.

        Args:
            block_length: List of total reservable minutes from each provider.
            k: Required number of rooms.

        Returns:
            The largest feasible window size, or -1 if impossible even for W = 1.

        Time complexity:
            O(n log M), where M is max(block_length).

        Space complexity:
            O(1), ignoring input storage.
        """
        # First, check whether the task is impossible even with the smallest
        # possible window size W = 1.
        if not self.can_make_at_least_k_rooms(block_length, k, 1):
            return -1

        # The search space for W is from 1 to max(block_length).
        # Any window larger than the maximum block length would contribute
        # zero rooms from every provider, so it cannot be useful.
        left: int = 1
        right: int = max(block_length)
        best: int = 1

        # Standard binary search on the answer space.
        while left <= right:
            mid: int = left + (right - left) // 2

            # If mid is feasible, we try to move right to find a larger feasible
            # window, because we are searching for the boundary.
            if self.can_make_at_least_k_rooms(block_length, k, mid):
                best = mid
                left = mid + 1
            else:
                # If mid is not feasible, all larger values are also not feasible,
                # so we move left.
                right = mid - 1

        return best

    def minimum_reservation_window(self, block_length: List[int], k: int) -> int:
        """
        Return the minimum positive integer reservation window W such that
        sum(floor(block_length[i] / W)) >= k.

        Important correctness observation:
        Since W must be a positive integer and the minimum possible positive
        integer is 1, the answer is:
            - 1 if W = 1 is feasible
            - -1 otherwise

        This follows directly from the formal statement of the problem.

        Args:
            block_length: List of total reservable minutes from each provider.
            k: Required number of rooms.

        Returns:
            1 if it is possible to create at least k rooms, otherwise -1.

        Time complexity:
            O(n)

        Space complexity:
            O(1), ignoring input storage.
        """
        # Step 1:
        # We test the smallest allowed window size, W = 1.
        #
        # Why?
        # Because the problem asks for the minimum positive integer W.
        # There is no positive integer smaller than 1.
        #
        # Therefore:
        # - If W = 1 works, then it is automatically the minimum answer.
        # - If W = 1 does not work, then no answer exists, because making W larger
        #   can only decrease or keep the same the number of rooms.
        #
        # This is the key mathematical simplification that makes the exact
        # statement of the problem trivial once interpreted carefully.
        total_rooms_at_one: int = 0

        # Step 2:
        # Sum all contributions at W = 1.
        # Since floor(x / 1) = x for integers, each provider contributes exactly
        # its block length in number of 1-minute rooms.
        for minutes in block_length:
            total_rooms_at_one += minutes

            # Early exit:
            # If we already reached k, we know W = 1 is feasible and therefore
            # is the minimum possible answer.
            if total_rooms_at_one >= k:
                return 1

        # Step 3:
        # If we finish the loop and still have fewer than k rooms, then even the
        # smallest possible window size is not enough. Larger windows will only
        # reduce the room count, so the task is impossible.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # blockLength = [8, 5, 12], k = 7
    #
    # According to the exact formal statement:
    # - At W = 1, total rooms = 8 + 5 + 12 = 25 >= 7
    # - Since 1 is the smallest positive integer, the minimum W is 1
    example_1_block_length: List[int] = [8, 5, 12]
    example_1_k: int = 7
    result_1: int = solution.minimum_reservation_window(example_1_block_length, example_1_k)
    print(result_1)  # Expected: 1

    # Example 2 from the prompt:
    # blockLength = [2, 1], k = 10
    #
    # At W = 1, total rooms = 2 + 1 = 3 < 10
    # Therefore it is impossible.
    example_2_block_length: List[int] = [2, 1]
    example_2_k: int = 10
    result_2: int = solution.minimum_reservation_window(example_2_block_length, example_2_k)
    print(result_2)  # Expected: -1

    # Optional demonstration of the binary-search boundary helper:
    # This is NOT the formal answer to the stated problem, but it shows the
    # monotonic boundary discussed in the prompt.
    boundary_result_1: int = solution.binary_search_boundary_window(example_1_block_length, example_1_k)
    print(boundary_result_1)  # For [8, 5, 12], k = 7, the largest feasible W is 3