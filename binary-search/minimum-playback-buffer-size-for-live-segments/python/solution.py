"""
Title: Minimum Playback Buffer Size for Live Segments

Problem Description:
A video platform is preparing a live event made of n consecutive segments.
Segment i has size segments[i] megabytes. The player downloads data into a
temporary buffer before each segment begins, and the same fixed buffer size
must be used for the entire event.

If a segment is larger than the buffer, it cannot be played.

The platform is allowed to split the event into at most k playback sessions.
Each session must contain a contiguous group of segments, and the total size
of all segments assigned to a single session cannot exceed the buffer size.

Task:
Compute the minimum buffer size required so that all segments can be played
in order using at most k sessions.

Equivalent formulation:
Partition the array into at most k contiguous parts while minimizing the
maximum part sum.

Key idea:
Use binary search on the answer.
- A candidate buffer size x is feasible if we can split the array into
  at most k contiguous sessions such that each session sum <= x.
- To test feasibility, greedily build each session from left to right,
  taking as many consecutive segments as possible before starting a new one.
"""

from typing import List


class Solution:
    def sessions_needed(self, segments: List[int], max_buffer: int) -> int:
        """
        Compute how many contiguous playback sessions are required if each
        session is allowed to have total size at most max_buffer.

        The method uses a greedy left-to-right scan:
        - Keep adding the next segment to the current session while the sum
          stays within max_buffer.
        - If adding the next segment would exceed max_buffer, start a new
          session beginning with that segment.

        This greedy strategy is correct for counting the minimum number of
        sessions needed for a fixed max_buffer, because delaying a split as
        long as possible never increases the number of sessions.

        Args:
            segments: List of segment sizes.
            max_buffer: Candidate maximum allowed sum for any single session.

        Returns:
            The number of sessions needed to fit all segments under max_buffer.

        Time complexity:
            O(n), where n is the number of segments.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We start with one session because if the list is non-empty,
        # at least one session is needed to hold the first segment.
        sessions: int = 1

        # current_sum stores the total size of the segments currently placed
        # into the active session we are building.
        current_sum: int = 0

        # Process each segment in order because sessions must be contiguous.
        for size in segments:
            # If adding this segment would exceed the allowed buffer size,
            # we must start a new session.
            #
            # Why this is necessary:
            # - Each session sum must be <= max_buffer.
            # - Since segments must remain in order and contiguous, we cannot
            #   skip or rearrange anything.
            # - Therefore the only valid action is to close the current session
            #   and begin a new one with this segment.
            if current_sum + size > max_buffer:
                sessions += 1
                current_sum = size
            else:
                # Otherwise, it is safe and optimal to keep extending the
                # current session. This greedy choice packs as much as possible
                # into each session, which minimizes the number of sessions.
                current_sum += size

        return sessions

    def minimum_buffer_size(self, segments: List[int], k: int) -> int:
        """
        Find the minimum fixed buffer size needed so that all segments can be
        partitioned into at most k contiguous playback sessions.

        The search space for the answer is:
        - Lower bound: max(segments)
          Because the buffer must be large enough to fit the largest single
          segment by itself.
        - Upper bound: sum(segments)
          Because one session containing all segments is always possible if
          the buffer equals the total sum.

        We binary search this range:
        - If a candidate buffer is feasible (needs <= k sessions), try smaller.
        - If it is not feasible (needs > k sessions), try larger.

        Args:
            segments: List of segment sizes.
            k: Maximum number of allowed contiguous sessions.

        Returns:
            The minimum possible buffer size.

        Time complexity:
            O(n * log(sum(segments))), because each binary search step performs
            an O(n) feasibility scan.

        Space complexity:
            O(1), excluding the input list.
        """
        # The smallest possible valid answer must be at least the largest
        # individual segment, because no session can split a segment.
        left: int = max(segments)

        # The largest possible answer is the sum of all segments, which means
        # everything fits into one session.
        right: int = sum(segments)

        # answer will store the best feasible value found so far.
        # We initialize it to right because right is always feasible.
        answer: int = right

        # Standard binary search on the answer space.
        while left <= right:
            # Midpoint candidate buffer size.
            mid: int = left + (right - left) // 2

            # Determine how many sessions are required if we force every
            # session sum to be at most mid.
            required_sessions: int = self.sessions_needed(segments, mid)

            # If we can fit everything into at most k sessions, then mid is
            # feasible. We record it and try to find an even smaller feasible
            # buffer.
            if required_sessions <= k:
                answer = mid
                right = mid - 1
            else:
                # Otherwise, mid is too small, so we must increase the buffer.
                left = mid + 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # segments = [8, 3, 5, 7, 2], k = 3
    #
    # Check the claimed optimal answer carefully:
    # - With buffer 9:
    #   [8] -> sum 8
    #   next session: [3,5] -> sum 8
    #   next session: [7,2] -> sum 9
    #   Total sessions = 3, which is allowed.
    # So 9 is feasible.
    #
    # Could 8 work?
    # - [8]
    # - [3,5] -> sum 8
    # - [7]
    # - [2]
    # This needs 4 sessions, which is more than k = 3.
    # So 8 is not feasible.
    #
    # Therefore the correct minimum answer is 9.
    segments1: List[int] = [8, 3, 5, 7, 2]
    k1: int = 3
    result1: int = solution.minimum_buffer_size(segments1, k1)
    print(f"Example 1 result: {result1}")  # Expected: 9

    # Example 2:
    # segments = [4, 4, 4, 4], k = 2
    #
    # With buffer 8:
    # - [4,4]
    # - [4,4]
    # Exactly 2 sessions, so feasible.
    #
    # With buffer 7:
    # - [4]
    # - [4]
    # - [4]
    # - [4]
    # Needs 4 sessions, not feasible.
    #
    # Therefore the correct minimum answer is 8.
    segments2: List[int] = [4, 4, 4, 4]
    k2: int = 2
    result2: int = solution.minimum_buffer_size(segments2, k2)
    print(f"Example 2 result: {result2}")  # Expected: 8

    # Additional small sanity check:
    # If k equals n, each segment can be its own session, so the answer should
    # be the maximum segment size.
    segments3: List[int] = [2, 10, 3, 1]
    k3: int = 4
    result3: int = solution.minimum_buffer_size(segments3, k3)
    print(f"Additional test 1 result: {result3}")  # Expected: 10

    # Another sanity check:
    # If k is 1, everything must be in one session, so the answer is the sum.
    segments4: List[int] = [2, 10, 3, 1]
    k4: int = 1
    result4: int = solution.minimum_buffer_size(segments4, k4)
    print(f"Additional test 2 result: {result4}")  # Expected: 16