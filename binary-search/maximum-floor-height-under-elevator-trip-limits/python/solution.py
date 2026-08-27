"""
Title: Maximum Floor Height Under Elevator Trip Limits

Problem Description:
A logistics company is configuring a freight elevator in a warehouse tower. There are n
delivery batches, and batch i contains boxes[i] identical boxes. The elevator can carry
at most cap boxes per trip, where cap is a positive integer chosen once for all batches.

A single batch may be split across multiple trips, but boxes from different batches
cannot be mixed in the same trip because each batch must remain sealed and tracked
separately. Therefore, batch i requires ceil(boxes[i] / cap) trips.

The elevator is only allowed to make at most maxTrips total trips during the shift.
Your task is to compute the largest integer capacity cap such that all batches can still
be transported within maxTrips trips.

If it is impossible even when cap is arbitrarily large, return -1.

Formally, find the maximum integer cap >= 1 satisfying:
ceil(boxes[0] / cap) + ceil(boxes[1] / cap) + ... + ceil(boxes[n-1] / cap) <= maxTrips.

Constraints:
- 1 <= n <= 200000
- 1 <= boxes[i] <= 10^12
- 1 <= maxTrips <= 10^18
- The answer must fit in a signed 64-bit integer

Important note about the search range:
Although any cap larger than max(boxes) behaves the same as max(boxes) for every batch,
the problem explicitly asks us to search cap only in the meaningful bounded range
[1, max(boxes)]. Therefore, we return the largest feasible cap in that range.

Examples:
1) boxes = [8, 5, 13], maxTrips = 8
   cap = 4 gives trips = 2 + 2 + 4 = 8, so it is feasible.
   Larger capacities are also feasible, and within [1, 13] the largest feasible cap is 13.
   At cap = 13, trips = 1 + 1 + 1 = 3 <= 8, so the correct answer is 13.

2) boxes = [4, 4, 4], maxTrips = 2
   Even at cap = 4, each batch needs 1 trip, so total trips = 3.
   Since 3 > 2, it is impossible, so the answer is -1.
"""

from typing import List


class Solution:
    def _can_finish(self, boxes: List[int], max_trips: int, cap: int) -> bool:
        """
        Check whether a given elevator capacity is feasible.

        For each batch, the number of required trips is ceil(boxes[i] / cap).
        We sum these values and verify whether the total is at most max_trips.

        Args:
            boxes: List of batch sizes.
            max_trips: Maximum allowed total trips.
            cap: Candidate elevator capacity.

        Returns:
            True if all batches can be transported within max_trips using this cap,
            otherwise False.

        Time complexity:
            O(n), where n is the number of batches.

        Space complexity:
            O(1), ignoring input storage.
        """
        total_trips: int = 0

        # We process each batch independently because batches cannot be mixed.
        # For a batch of size x and capacity cap, the number of trips needed is:
        #   ceil(x / cap)
        #
        # In integer arithmetic, a standard way to compute ceil(x / cap) is:
        #   (x + cap - 1) // cap
        #
        # This avoids floating-point operations and is exact.
        for batch_size in boxes:
            total_trips += (batch_size + cap - 1) // cap

            # Early stopping optimization:
            # If we already exceeded max_trips, there is no need to continue.
            # This keeps the check efficient in many practical cases.
            if total_trips > max_trips:
                return False

        return total_trips <= max_trips

    def maximum_capacity(self, boxes: List[int], maxTrips: int) -> int:
        """
        Find the largest feasible elevator capacity in the range [1, max(boxes)].

        The key observation is monotonicity:
        - If a capacity cap is feasible, then any larger capacity is also feasible,
          because increasing capacity can only keep the same number of trips or reduce it.
        - Therefore, the feasibility condition forms a monotonic boolean pattern,
          which makes binary search the correct and efficient approach.

        We first check whether the task is impossible even with the largest meaningful
        capacity max(boxes). If so, return -1.

        Otherwise, we binary search for the maximum feasible capacity.

        Args:
            boxes: List of batch sizes.
            maxTrips: Maximum allowed total trips.

        Returns:
            The largest integer capacity in [1, max(boxes)] such that the total number
            of required trips is at most maxTrips, or -1 if impossible.

        Time complexity:
            O(n log M), where:
            - n is the number of batches
            - M is max(boxes)

        Space complexity:
            O(1), ignoring input storage.
        """
        # Defensive handling is not strictly necessary under the given constraints,
        # but this keeps the method robust and beginner-friendly.
        if not boxes:
            return -1

        # The largest meaningful capacity is the largest batch size.
        # Any capacity above this value would still make every batch require exactly 1 trip,
        # so searching beyond this point is unnecessary and explicitly excluded by the prompt.
        max_box: int = max(boxes)

        # Before binary search, we must determine whether a solution exists at all.
        #
        # The fewest possible trips within the allowed search range [1, max_box]
        # happen at cap = max_box, because that is the largest capacity we consider.
        #
        # If even this best-case capacity is not feasible, then no smaller capacity
        # can possibly work, because smaller capacities require the same or more trips.
        if not self._can_finish(boxes, maxTrips, max_box):
            return -1

        # Standard binary search for the "largest feasible value".
        #
        # Search space:
        #   left  = smallest possible capacity
        #   right = largest meaningful capacity
        #
        # Invariant idea:
        # - We keep searching while left <= right.
        # - If mid is feasible, it is a valid answer candidate, and we try to go larger.
        # - If mid is not feasible, we must go smaller.
        left: int = 1
        right: int = max_box
        answer: int = -1

        while left <= right:
            # Midpoint of the current search interval.
            # Using this formula avoids overflow in languages with fixed-width integers.
            # In Python overflow is not an issue, but this is still best practice.
            mid: int = left + (right - left) // 2

            # Check whether this candidate capacity works.
            if self._can_finish(boxes, maxTrips, mid):
                # mid is feasible, so it is a valid candidate answer.
                answer = mid

                # Because we want the LARGEST feasible capacity,
                # we continue searching to the right half.
                left = mid + 1
            else:
                # mid is not feasible, so any smaller-or-equal monotonic reasoning tells us:
                # actually, since feasibility increases with capacity, if mid is not feasible,
                # then all smaller capacities are also not feasible.
                # Therefore, we must search the right side? Let's reason carefully:
                #
                # trips decrease as cap increases
                # so feasibility is:
                #   small cap -> maybe false
                #   large cap -> true
                #
                # That means if mid is NOT feasible, we need a LARGER capacity.
                left = mid + 1

                # However, the above would be correct only if we were searching for the
                # smallest feasible capacity. Here we want the largest feasible capacity
                # in a monotonic false->true setting over [1, max_box].
                #
                # Since the problem's bounded range guarantees max_box is feasible whenever
                # a solution exists, the largest feasible capacity is simply max_box.
                #
                # But to preserve a proper binary-search structure and correctness under the
                # stated monotonicity, we should search for the first feasible value if needed.
                #
                # The prompt explicitly asks for the largest feasible value in [1, max(boxes)],
                # and because feasibility is monotonic increasing with cap, once a capacity is
                # feasible, every larger one is also feasible. Therefore the largest feasible
                # value is always max_box whenever any feasible value exists.
                #
                # To keep the implementation both correct and aligned with the prompt examples,
                # we can return max_box after the initial feasibility check. This branch is thus
                # unreachable in a meaningful final algorithm.
                #
                # We break here to avoid misleading behavior.
                break

        # Due to the monotonic direction of this problem and the bounded search range,
        # if max_box is feasible, then every larger meaningful candidate up to max_box
        # is trivially included, so the largest feasible capacity is max_box.
        #
        # This matches Example 1:
        # boxes = [8, 5, 13], maxTrips = 8
        # cap = 13 => trips = 1 + 1 + 1 = 3 <= 8, so answer = 13
        #
        # And Example 2:
        # boxes = [4, 4, 4], maxTrips = 2
        # cap = 4 => trips = 1 + 1 + 1 = 3 > 2, so answer = -1
        return max_box

    def solve(self, boxes: List[int], maxTrips: int) -> int:
        """
        Public wrapper method for the problem.

        Args:
            boxes: List of batch sizes.
            maxTrips: Maximum allowed total trips.

        Returns:
            Largest feasible capacity in [1, max(boxes)], or -1 if impossible.

        Time complexity:
            O(n), because after the feasibility insight the answer is determined by
            checking max(boxes).

        Space complexity:
            O(1), ignoring input storage.
        """
        return self.maximum_capacity(boxes, maxTrips)


if __name__ == "__main__":
    solution = Solution()

    # Sample input 1 from the prompt.
    boxes1: List[int] = [8, 5, 13]
    max_trips1: int = 8
    result1: int = solution.solve(boxes1, max_trips1)
    print(result1)  # Expected: 13

    # Sample input 2 from the prompt.
    boxes2: List[int] = [4, 4, 4]
    max_trips2: int = 2
    result2: int = solution.solve(boxes2, max_trips2)
    print(result2)  # Expected: -1

    # Additional quick sanity checks.
    boxes3: List[int] = [10]
    max_trips3: int = 1
    result3: int = solution.solve(boxes3, max_trips3)
    print(result3)  # Expected: 10

    boxes4: List[int] = [10, 10, 10]
    max_trips4: int = 3
    result4: int = solution.solve(boxes4, max_trips4)
    print(result4)  # Expected: 10