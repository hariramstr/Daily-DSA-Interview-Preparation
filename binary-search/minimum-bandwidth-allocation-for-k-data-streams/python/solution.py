```python
"""
Title: Minimum Bandwidth Allocation for K Data Streams
Difficulty: Hard
Topic: Binary Search

Problem Description:
You are managing a network server that must handle `n` data streams, where each stream
has a bandwidth requirement given in the array `streams` (streams[i] is the bandwidth
needed by stream i in Mbps). You have exactly `k` network channels available, and you
must assign every stream to exactly one channel. Each channel's total load is the sum
of bandwidths of all streams assigned to it.

To ensure fair distribution and avoid bottlenecks, you want to minimize the maximum
total load across all channels. However, there is a constraint: streams must be assigned
to channels in contiguous groups (i.e., streams[0..j1] go to channel 1,
streams[j1+1..j2] go to channel 2, etc.).

Return the minimum possible value of the maximum channel load.

Constraints:
- 1 <= k <= n <= 10^5
- 1 <= streams[i] <= 10^9
- It is guaranteed that k <= n

Example 1:
- Input: streams = [10, 20, 30, 40, 50], k = 3
- Output: 60
- Explanation: Split as [10, 20, 30], [40], [50]. Loads are 60, 40, 50. Max = 60.

Example 2:
- Input: streams = [7, 2, 5, 10, 8], k = 2
- Output: 18
- Explanation: Split as [7, 2, 5] and [10, 8]. Loads are 14 and 18. Max = 18.
"""

from typing import List


class Solution:
    def minimumBandwidthAllocation(self, streams: List[int], k: int) -> int:
        """
        Find the minimum possible maximum channel load when splitting streams
        into exactly k contiguous groups.

        This is a classic "minimize the maximum" problem solved with binary search
        on the answer combined with a greedy feasibility check.

        Core Idea:
        - Binary search on the answer (the maximum allowed load per channel).
        - For each candidate maximum load `mid`, check if it's feasible to split
          the streams into AT MOST k contiguous groups where each group's sum <= mid.
        - If feasible with <= k groups, then `mid` might be our answer (try smaller).
        - If not feasible, we need a larger maximum load (try bigger).

        Args:
            streams: List of bandwidth requirements for each stream (in Mbps).
            k: Number of available network channels.

        Returns:
            The minimum possible value of the maximum channel load.

        Time Complexity: O(n * log(sum(streams)))
            - Binary search runs O(log(sum)) times
            - Each feasibility check is O(n)
            - sum(streams) can be up to 10^5 * 10^9 = 10^14

        Space Complexity: O(1)
            - Only a constant amount of extra space is used
        """

        # -----------------------------------------------------------------------
        # STEP 1: Define the search space for binary search
        # -----------------------------------------------------------------------
        # The answer (minimum possible maximum load) must lie in the range:
        #   - Lower bound (lo): max(streams)
        #     Because every stream must be assigned to some channel, the maximum
        #     load can never be less than the largest single stream value.
        #     (A channel containing just the largest stream would have that load.)
        #   - Upper bound (hi): sum(streams)
        #     In the worst case, all streams go to one channel, giving total sum.
        #
        # We binary search within [lo, hi] to find the smallest feasible value.

        lo = max(streams)   # Minimum possible answer: must fit the largest stream
        hi = sum(streams)   # Maximum possible answer: all streams on one channel

        # -----------------------------------------------------------------------
        # STEP 2: Binary search loop
        # -----------------------------------------------------------------------
        # We use the standard "find the leftmost valid value" binary search pattern.
        # We maintain the invariant:
        #   - Everything < lo is infeasible (too small a max load)
        #   - Everything >= hi is feasible (large enough max load)
        # We shrink the range until lo == hi, which is our answer.

        while lo < hi:
            # Pick the midpoint to test as a candidate maximum load
            # Using (lo + hi) // 2 avoids integer overflow (Python handles big ints,
            # but this is good practice)
            mid = (lo + hi) // 2

            # -----------------------------------------------------------------------
            # STEP 3: Feasibility check — can we split into <= k groups with max <= mid?
            # -----------------------------------------------------------------------
            # Call our helper function to check if `mid` is a feasible maximum load.
            if self._is_feasible(streams, k, mid):
                # If feasible, `mid` could be our answer, but maybe we can do better.
                # Move the upper bound down to search for a smaller feasible value.
                hi = mid
            else:
                # If not feasible, `mid` is too small. We need a larger maximum load.
                # Move the lower bound up past `mid`.
                lo = mid + 1

        # -----------------------------------------------------------------------
        # STEP 4: Return the result
        # -----------------------------------------------------------------------
        # When lo == hi, we've found the smallest feasible maximum load.
        return lo

    def _is_feasible(self, streams: List[int], k: int, max_load: int) -> bool:
        """
        Check if it's possible to split streams into at most k contiguous groups
        such that each group's sum does not exceed max_load.

        This uses a greedy approach: greedily pack as many streams as possible
        into the current channel without exceeding max_load. When adding the next
        stream would exceed max_load, start a new channel.

        If the total number of channels needed is <= k, then max_load is feasible.

        Args:
            streams: List of bandwidth requirements.
            k: Maximum number of channels allowed.
            max_load: The candidate maximum load per channel to test.

        Returns:
            True if we can split streams into at most k groups with each group
            sum <= max_load, False otherwise.

        Time Complexity: O(n) — single pass through the streams array
        Space Complexity: O(1) — only counters used
        """

        # -----------------------------------------------------------------------
        # Greedy feasibility check
        # -----------------------------------------------------------------------
        # We simulate the assignment process:
        # - Start with 1 channel (we always need at least one)
        # - Keep a running sum of the current channel's load
        # - For each stream, try to add it to the current channel
        #   - If it fits (current_sum + stream <= max_load), add it
        #   - If it doesn't fit, open a new channel and start fresh with this stream

        channels_used = 1    # We always need at least 1 channel
        current_sum = 0      # Running sum of the current channel's load

        for bandwidth in streams:
            # Check if adding this stream to the current channel would exceed max_load
            if current_sum + bandwidth <= max_load:
                # It fits! Add this stream to the current channel.
                current_sum += bandwidth
            else:
                # It doesn't fit. We must start a new channel for this stream.
                # (Since lo = max(streams), every individual stream fits in max_load
                #  when max_load >= max(streams), so we never need to split a single
                #  stream across channels — which is not allowed anyway.)
                channels_used += 1
                current_sum = bandwidth  # New channel starts with just this stream

                # Early termination: if we've already exceeded k channels,
                # this max_load is not feasible — no need to continue.
                if channels_used > k:
                    return False

        # If we finished assigning all streams and used <= k channels, it's feasible.
        return True


# -------------------------------------------------------------------------------
# VERIFICATION / TRACING THROUGH EXAMPLES
# -------------------------------------------------------------------------------
# Example 1: streams = [10, 20, 30, 40, 50], k = 3
#   lo = max([10,20,30,40,50]) = 50
#   hi = sum([10,20,30,40,50]) = 150
#
#   Iteration 1: mid = (50+150)//2 = 100
#     _is_feasible([10,20,30,40,50], 3, 100):
#       bandwidth=10: 0+10=10 <= 100, current_sum=10
#       bandwidth=20: 10+20=30 <= 100, current_sum=30
#       bandwidth=30: 30+30=60 <= 100, current_sum=60
#       bandwidth=40: 60+40=100 <= 100, current_sum=100
#       bandwidth=50: 100+50=150 > 100, new channel, channels_used=2, current_sum=50
#       Result: channels_used=2 <= 3 → True
#     hi = 100
#
#   Iteration 2: mid = (50+100)//2 = 75
#     _is_feasible([10,20,30,40,50], 3, 75):
#       bandwidth=10: current_sum=10
#       bandwidth=20: current_sum=30
#       bandwidth=30: current_sum=60
#       bandwidth=40: 60+40=100 > 75, new channel, channels_used=2, current_sum=40
#       bandwidth=50: 40+50=90 > 75, new channel, channels_used=3, current_sum=50
#       Result: channels_used=3 <= 3 → True
#     hi = 75
#
#   Iteration 3: mid = (50+75)//2 = 62
#     _is_feasible([10,20,30,40,50], 3, 62):
#       bandwidth=10: current_sum=10
#       bandwidth=20: current_sum=30
#       bandwidth=30: current_sum=60
#       bandwidth=40: 60+40=100 > 62, new channel, channels_used=2, current_sum=40
#       bandwidth=50: 40+50=90 > 62, new channel, channels_used=3, current_sum=50
#       Result: channels_used=3 <= 3 → True
#     hi = 62
#
#   Iteration 4: mid = (50+62)//2 = 56
#     _is_feasible([10,20,30,40,50], 3, 56):
#       bandwidth=10: current_sum=10
#       bandwidth=20: current_sum=30
#       bandwidth=30: current_sum=60 > 56? No: 30+30=60 > 56, new channel, channels_used=2, current_sum=30
#       bandwidth=40: 30+40=70 > 56, new channel, channels_used=3, current_sum=40
#       bandwidth=50: 40+50=90 > 56, new channel, channels_used=4 > 3 → False
#     lo = 57
#
#   Iteration 5: mid = (57+62)//2 = 59
#     _is_feasible([10,20,30,40,50], 3, 59):
#       bandwidth=10: current_sum=10
#       bandwidth=20: current_sum=30
#       bandwidth=30: 30+30=60 > 59, new channel, channels_used=2, current_sum=30
#       bandwidth=40: 30+40=70 > 59, new channel, channels_used=3, current_sum=40
#       bandwidth=50: 40+50=90 > 59, new channel, channels_used=4 > 3 → False
#     lo = 60
#
#   Iteration 6: mid = (60+62)//2 = 61
#     _is_feasible([10,20,30,40,50], 3, 61):
#       bandwidth=10: current_sum=10
#       bandwidth=20: current_sum=30
#       bandwidth=30: 30+30=60 <= 61, current_sum=60
#       bandwidth=40: 60+40=100 > 61, new channel, channels_used=2, current_sum=40
#       bandwidth=50: 40+50=90 > 61, new channel, channels_used=3, current_sum=50
#       Result: channels_used=3 <= 3 → True
#     hi = 61
#
#   Iteration 7: mid = (60+61)//2 = 60
#     _is_feasible([10,20,30,40,50], 3, 60):
#       bandwidth=10: current_sum=10
#       bandwidth=20: current_sum=30
#       bandwidth=30: 30+30=60 <= 60, current_sum=60
#       bandwidth=40: 60+40=100 > 60, new channel, channels_used=2, current_sum=40
#       bandwidth=50: 40+50=90 > 60, new channel, channels_used=3, current_sum=50
#       Result: channels_used=3 <= 3 → True
#     hi = 60
#
#   lo == hi == 60 → Answer: 60 ✓
#
# Example 2: streams = [7, 2, 5, 10, 8], k = 2
#   lo = max([7,2,5,10,8]) = 10
#   hi = sum([7,2,5,10,8]) = 32
#
#   Binary search will converge to 18.
#   Verification: _is_feasible([7,2,5,10,8], 2, 18):
#     bandwidth=7: current_sum=7
#     bandwidth=2: current_sum=9
#     bandwidth=5: current_sum=14
#     bandwidth=10: 14+10=24 > 18, new channel, channels_used=2, current_sum=10
#     bandwidth=8: 10+8=18 <= 18, current_sum=18
#     Result: channels_used=2 <= 2 → True ✓
#
#   _is_feasible([7,2,5,10,8], 2, 17):
#     bandwidth=7: current_sum=7
#     bandwidth=2: current_sum=9
#     bandwidth=5: current_sum=14
#     bandwidth=10: 14+10=24 > 17, new channel, channels_used=2, current_sum=10
#     bandwidth=8: 10+8=18 > 17, new channel, channels_used=3 > 2 → False ✓
#   So answer is 18 ✓
# -------------------------------------------------------------------------------


if __name__ == "__main__":
    # Create a Solution instance
    solution = Solution()

    # -----------------------------------------------------------------------
    # Test Case 1 (from problem description)
    # -----------------------------------------------------------------------
    streams1 = [10, 20, 30, 40, 50]
    k1 = 3
    result1 = solution.minimumBandwidthAllocation(streams1, k1)
    print(f"Example 1:")
    print(f"  Input:    streams = {streams1}, k = {k1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: 60")
    print(f"  Correct:  {result1 == 60}")
    print()

    # -----------------------------------------------------------------------
    # Test Case 2 (from problem description)
    # -----------------------------------------------------------------------
    streams2 = [7, 2, 5, 10, 8]
    k2 = 2
    result2 = solution.minimumBandwidthAllocation(streams2, k2)
    print(f"Example 2:")
    print(f"  Input:    streams = {streams2}, k = {k2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: 18")
    print(f"  Correct:  {result2 == 18}")
    print()

    # -----------------------------------------------------------------------
    # Additional Test Cases
    # -----------------------------------------------------------------------

    # Edge case: k == n (each stream gets its own channel)
    # Maximum load = max single stream
    streams3 = [5, 3, 8, 