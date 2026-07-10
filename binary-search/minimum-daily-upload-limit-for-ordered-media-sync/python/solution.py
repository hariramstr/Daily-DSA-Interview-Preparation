"""
Title: Minimum Daily Upload Limit for Ordered Media Sync

Problem Description:
A media company needs to synchronize a sequence of video segments to a remote archive.
The segments must be uploaded in the given order, and each segment is indivisible:
it must be uploaded entirely on a single day. The company has exactly d days to finish
the synchronization.

For each segment i, uploading it consumes uploadSizes[i] units of bandwidth on the day
it is assigned. If the daily upload limit is L, then the total size of all segments
assigned to any single day cannot exceed L. Because the upload order cannot change,
each day receives a contiguous block of segments.

Your task is to find the minimum possible daily upload limit L that allows all segments
to be uploaded within at most d days.

Constraints:
- 1 <= uploadSizes.length <= 200000
- 1 <= uploadSizes[i] <= 1000000000
- 1 <= d <= uploadSizes.length
- The answer fits in a 64-bit signed integer.

Examples:
1) uploadSizes = [7,2,5,10,8], d = 2
   Output: 18

2) uploadSizes = [4,4,4,4,4,4,4], d = 3
   Output: 12
"""

from typing import List


class Solution:
    def _can_finish_with_limit(self, upload_sizes: List[int], d: int, limit: int) -> bool:
        """
        Check whether all segments can be uploaded within at most d days
        if the daily upload limit is fixed to `limit`.

        Args:
            upload_sizes: List of segment sizes that must stay in original order.
            d: Maximum number of days allowed.
            limit: Candidate daily upload limit to test.

        Returns:
            True if it is possible to upload all segments in at most d days,
            otherwise False.

        Time complexity:
            O(n), where n is the number of segments.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We start by assuming we are using the first day.
        days_used: int = 1

        # This variable tracks how much total upload size has already been assigned
        # to the current day.
        current_day_total: int = 0

        # We process segments strictly from left to right because the problem states
        # that the order cannot be changed.
        for size in upload_sizes:
            # If a single segment is larger than the candidate limit, then this limit
            # is immediately impossible. Even if we gave the whole day to this one
            # segment, it still would not fit.
            if size > limit:
                return False

            # If adding this segment to the current day stays within the limit,
            # we keep it on the same day.
            if current_day_total + size <= limit:
                current_day_total += size
            else:
                # Otherwise, we must start a new day for this segment.
                days_used += 1
                current_day_total = size

                # If we already need more than d days, then this limit is not feasible.
                if days_used > d:
                    return False

        # If we finished processing all segments without exceeding d days,
        # then this limit works.
        return True

    def minimum_daily_upload_limit(self, upload_sizes: List[int], d: int) -> int:
        """
        Find the minimum daily upload limit needed to upload all segments
        within at most d days while preserving order.

        This uses binary search on the answer:
        - If a limit works, then any larger limit also works.
        - If a limit fails, then any smaller limit also fails.
        That monotonic behavior makes binary search the correct tool.

        Args:
            upload_sizes: List of segment sizes in required upload order.
            d: Maximum number of days allowed.

        Returns:
            The minimum feasible daily upload limit.

        Time complexity:
            O(n log S), where:
            - n is the number of segments
            - S is the search range between max(upload_sizes) and sum(upload_sizes)

        Space complexity:
            O(1), excluding input storage.
        """
        # The smallest possible answer cannot be less than the largest single segment,
        # because every segment must fit into some day by itself if necessary.
        left: int = max(upload_sizes)

        # The largest possible answer is the sum of all segments, which corresponds
        # to uploading everything in one day.
        right: int = sum(upload_sizes)

        # We will binary search for the smallest feasible limit.
        #
        # Invariant:
        # - The true answer is always somewhere in the range [left, right].
        #
        # Why binary search works:
        # - If a candidate limit is feasible, then all larger limits are also feasible.
        # - If a candidate limit is not feasible, then all smaller limits are also not feasible.
        while left < right:
            # Midpoint of current search range.
            # Using this form avoids overflow in languages with fixed integer sizes.
            # Python integers are unbounded, but this is still the standard safe pattern.
            mid: int = left + (right - left) // 2

            # Test whether this candidate limit is enough.
            if self._can_finish_with_limit(upload_sizes, d, mid):
                # If mid works, it might be the answer, but maybe there is a smaller
                # feasible limit. So we keep searching the left half, including mid.
                right = mid
            else:
                # If mid does not work, then the answer must be larger than mid.
                left = mid + 1

        # At loop end, left == right, and that value is the minimum feasible limit.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    upload_sizes_1: List[int] = [7, 2, 5, 10, 8]
    d_1: int = 2
    result_1: int = solution.minimum_daily_upload_limit(upload_sizes_1, d_1)
    print("Example 1 Result:", result_1)  # Expected: 18

    # Example 2
    upload_sizes_2: List[int] = [4, 4, 4, 4, 4, 4, 4]
    d_2: int = 3
    result_2: int = solution.minimum_daily_upload_limit(upload_sizes_2, d_2)
    print("Example 2 Result:", result_2)  # Expected: 12

    # Additional quick sanity checks for beginners:
    # If d equals the number of segments, each day can take exactly one segment,
    # so the answer should be the maximum segment size.
    upload_sizes_3: List[int] = [3, 1, 9, 2]
    d_3: int = 4
    result_3: int = solution.minimum_daily_upload_limit(upload_sizes_3, d_3)
    print("Sanity Check 1 Result:", result_3)  # Expected: 9

    # If d is 1, everything must be uploaded in one day,
    # so the answer should be the total sum.
    upload_sizes_4: List[int] = [3, 1, 9, 2]
    d_4: int = 1
    result_4: int = solution.minimum_daily_upload_limit(upload_sizes_4, d_4)
    print("Sanity Check 2 Result:", result_4)  # Expected: 15