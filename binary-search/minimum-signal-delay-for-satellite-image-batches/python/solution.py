"""
Title: Minimum Signal Delay for Satellite Image Batches

Problem Description:
A ground station receives satellite image batches in a fixed order. The i-th batch has
size batches[i] megabytes. You must transmit all batches within h minutes, and the
transmitter can be configured to send data at a constant integer speed of s megabytes
per minute.

During each minute, the transmitter works on only one batch. If the current batch has
fewer than s megabytes remaining, the unused portion of that minute is wasted and you
may continue with the next batch only in the following minute. In other words, sending
a batch of size x at speed s takes ceil(x / s) whole minutes.

Your task is to return the minimum integer transmission speed s such that all batches
can be sent within h minutes.

If it is impossible even when the speed is very large, return -1. This happens when h
is smaller than the number of batches, because every non-empty batch needs at least one
full minute.

Constraints:
- 1 <= batches.length <= 100000
- 1 <= batches[i] <= 1000000000
- 1 <= h <= 1000000000
- The answer must fit in a 32-bit signed integer.
"""

from typing import List


class Solution:
    def minTransmissionSpeed(self, batches: List[int], h: int) -> int:
        """
        Find the minimum integer transmission speed needed to send all batches
        within h minutes.

        The key observation is monotonicity:
        - If a speed s is fast enough, then any speed larger than s is also fast enough.
        - That allows binary search over the answer.

        Args:
            batches: A list where batches[i] is the size of the i-th image batch.
            h: The maximum number of minutes allowed.

        Returns:
            The minimum integer speed that finishes all batches within h minutes,
            or -1 if it is impossible.

        Time Complexity:
            O(n log M), where:
            - n is the number of batches
            - M is the maximum batch size

        Space Complexity:
            O(1), ignoring input storage
        """
        # Every non-empty batch needs at least 1 full minute, no matter how large
        # the speed is, because the transmitter can work on only one batch per minute.
        # Therefore, if we have more batches than available minutes, the task is impossible.
        if h < len(batches):
            return -1

        # The slowest meaningful speed is 1 MB/minute.
        left: int = 1

        # The fastest speed we ever need to test is the size of the largest batch.
        # Why is this enough?
        # - At speed = max(batches), every batch finishes in exactly 1 minute.
        # - Since we already checked h >= number of batches, this speed is guaranteed
        #   to be sufficient.
        # So the answer must lie in [1, max(batches)].
        right: int = max(batches)

        # Standard binary search for the smallest feasible speed.
        while left < right:
            # Midpoint speed candidate.
            mid: int = (left + right) // 2

            # Check whether this candidate speed can finish all batches within h minutes.
            if self._can_finish(batches, h, mid):
                # If mid works, we try to find an even smaller working speed.
                right = mid
            else:
                # If mid does not work, we must increase the speed.
                left = mid + 1

        # When the loop ends, left == right and points to the minimum feasible speed.
        return left

    def _can_finish(self, batches: List[int], h: int, speed: int) -> bool:
        """
        Check whether all batches can be transmitted within h minutes at a given speed.

        Args:
            batches: List of batch sizes.
            h: Maximum allowed minutes.
            speed: Candidate transmission speed in MB/minute.

        Returns:
            True if all batches can be sent within h minutes, otherwise False.

        Time Complexity:
            O(n), where n is the number of batches

        Space Complexity:
            O(1)
        """
        # This variable accumulates the total number of minutes needed
        # to send every batch at the given speed.
        total_minutes: int = 0

        # Process each batch independently.
        for batch_size in batches:
            # We need ceil(batch_size / speed) minutes for this batch.
            #
            # Instead of importing math.ceil and using floating-point division,
            # we use the integer formula:
            #   ceil(a / b) = (a + b - 1) // b
            #
            # This is:
            # - faster
            # - exact
            # - avoids floating-point issues
            total_minutes += (batch_size + speed - 1) // speed

            # Important optimization:
            # As soon as total time exceeds h, we already know this speed fails.
            # No need to continue scanning the remaining batches.
            if total_minutes > h:
                return False

        # If we finished the loop and total_minutes never exceeded h,
        # then this speed is sufficient.
        return True


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    batches_1: List[int] = [30, 11, 23, 4, 20]
    h_1: int = 6
    result_1: int = solution.minTransmissionSpeed(batches_1, h_1)
    print("Example 1:")
    print(f"batches = {batches_1}, h = {h_1}")
    print(f"Minimum transmission speed = {result_1}")
    print("Expected = 23")
    print()

    # Example 2
    batches_2: List[int] = [8, 5, 8]
    h_2: int = 2
    result_2: int = solution.minTransmissionSpeed(batches_2, h_2)
    print("Example 2:")
    print(f"batches = {batches_2}, h = {h_2}")
    print(f"Minimum transmission speed = {result_2}")
    print("Expected = -1")
    print()

    # Additional quick sanity check
    batches_3: List[int] = [3, 6, 7, 11]
    h_3: int = 8
    result_3: int = solution.minTransmissionSpeed(batches_3, h_3)
    print("Additional Test:")
    print(f"batches = {batches_3}, h = {h_3}")
    print(f"Minimum transmission speed = {result_3}")
    print("Expected = 4")