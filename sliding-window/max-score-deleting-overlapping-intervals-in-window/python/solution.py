```python
"""
Title: Maximum Score from Deleting Overlapping Intervals in a Window
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a list of n sensor readings, where each reading is represented as an integer
array readings[i] = [start, end, value]. The start and end values represent a time range
(inclusive) during which the sensor is active, and value represents the score contributed
by that sensor.

You are also given an integer W, representing the size of a sliding time window. For each
window of size W (i.e., every contiguous time range [t, t+W-1]), you must select a
non-overlapping subset of sensors that are fully contained within the window
(i.e., start >= t and end <= t+W-1). The goal is to maximize the total score from the
selected non-overlapping sensors.

Return an array result where result[i] is the maximum score achievable in the window
starting at time i.

Constraints:
- 1 <= n <= 1000
- 0 <= start <= end <= 10^4
- 1 <= value <= 10^4
- 1 <= W <= 10^4
- Time ranges start from 0 and the last window starts at max(end) - W + 1
"""

from typing import List


class Solution:
    def max_score_weighted_interval_scheduling(
        self, intervals: List[List[int]], window_start: int, window_end: int
    ) -> int:
        """
        Compute the maximum score from non-overlapping intervals fully within [window_start, window_end].

        This uses the classic Weighted Interval Scheduling DP approach:
        - Sort intervals by end time
        - For each interval, either skip it or take it (and skip all overlapping ones before it)

        Args:
            intervals: List of [start, end, value] sensor readings
            window_start: The left boundary of the current window (inclusive)
            window_end: The right boundary of the current window (inclusive)

        Returns:
            Maximum total score from a non-overlapping subset of valid intervals

        Time Complexity: O(k^2) where k is the number of intervals fully inside the window
        Space Complexity: O(k) for the DP array
        """
        # Step 1: Filter intervals that are fully contained within [window_start, window_end]
        # An interval [s, e, v] is valid only if s >= window_start AND e <= window_end
        valid = [
            (s, e, v)
            for (s, e, v) in intervals
            if s >= window_start and e <= window_end
        ]

        # If no valid intervals exist in this window, the score is 0
        if not valid:
            return 0

        # Step 2: Sort valid intervals by their END time (ascending)
        # This is the standard approach for weighted interval scheduling DP:
        # sorting by end time allows us to efficiently find the latest non-overlapping
        # interval that ends before the current one starts.
        valid.sort(key=lambda x: x[1])

        k = len(valid)

        # Step 3: Build the DP array
        # dp[i] = maximum score considering only the first (i+1) intervals (0-indexed)
        # dp[i] represents the best score we can achieve using intervals from valid[0..i]
        dp = [0] * k

        # Base case: the first interval's score is just its own value
        dp[0] = valid[0][2]

        for i in range(1, k):
            s_i, e_i, v_i = valid[i]

            # Option A: Skip interval i — the best score is whatever we had up to i-1
            skip_score = dp[i - 1]

            # Option B: Take interval i
            # We need to find the latest interval j < i such that valid[j].end < s_i
            # (i.e., interval j ends strictly before interval i starts — no overlap)
            # Two intervals [s1,e1] and [s2,e2] overlap if s2 <= e1 (when s2 >= s1)
            # They do NOT overlap if e1 < s2, i.e., e1 <= s2 - 1
            take_score = v_i  # Start with just interval i's value

            # Binary search (or linear scan) for the rightmost j where valid[j].end < s_i
            # We use a simple linear scan here since k <= 1000 (O(k^2) total is acceptable)
            best_prev = -1  # Index of the best compatible previous interval
            for j in range(i - 1, -1, -1):
                # valid[j].end < s_i means interval j ends before interval i starts
                if valid[j][1] < s_i:
                    best_prev = j
                    break  # Since sorted by end, the first j we find going right-to-left is the best

            if best_prev >= 0:
                # Add the best score achievable using intervals up to best_prev
                take_score += dp[best_prev]

            # Take the maximum of skipping or taking interval i
            dp[i] = max(skip_score, take_score)

        # The answer for this window is the last element of dp
        return dp[k - 1]

    def maximum_score_sliding_window(
        self, readings: List[List[int]], W: int
    ) -> List[int]:
        """
        For each sliding window of size W, compute the maximum score from non-overlapping
        sensor intervals fully contained within that window.

        Args:
            readings: List of [start, end, value] sensor readings
            W: Size of the sliding time window

        Returns:
            List of maximum scores for each window starting at time t

        Time Complexity: O(T * k^2) where T is the number of windows and k <= n
        Space Complexity: O(n) for storing valid intervals per window
        """
        # Step 1: Determine the range of window start times
        # The problem states: time ranges start from 0 and the last window starts at max(end) - W + 1
        if not readings:
            return []

        # Find the maximum end time across all readings
        max_end = max(r[1] for r in readings)

        # The first window starts at t=0
        # The last window starts at max_end - W + 1
        # (so that the window [t, t+W-1] still covers up to max_end)
        first_window_start = 0
        last_window_start = max_end - W + 1

        # Edge case: if W > max_end + 1, there's only one window starting at 0
        # But last_window_start could be negative; clamp to 0
        if last_window_start < 0:
            last_window_start = 0

        # Step 2: Iterate over each window start time t
        result = []

        for t in range(first_window_start, last_window_start + 1):
            # The current window covers [t, t + W - 1]
            window_end = t + W - 1

            # Step 3: For this window, compute the maximum non-overlapping score
            # using the weighted interval scheduling DP
            score = self.max_score_weighted_interval_scheduling(readings, t, window_end)
            result.append(score)

        return result


# ─────────────────────────────────────────────────────────────────────────────
# Verification / Trace-through
# ─────────────────────────────────────────────────────────────────────────────
#
# Example 1:
#   readings = [[0,2,10],[1,3,5],[2,4,8],[3,5,6]], W = 4
#   max_end = 5, last_window_start = 5 - 4 + 1 = 2
#   Windows: t=0 → [0,3], t=1 → [1,4], t=2 → [2,5]
#
#   Window [0,3]:
#     Valid intervals (start>=0, end<=3): [0,2,10], [1,3,5]
#     Sorted by end: [0,2,10], [1,3,5]
#     dp[0] = 10
#     dp[1]: skip=10, take=[1,3,5]: s_i=1, look for j where end<1 → none → take=5
#             dp[1] = max(10, 5) = 10
#     Score = 10
#
#   Hmm, but expected output says [18, 14, 14]. Let me re-read the problem...
#
#   Wait — the example explanation says "select [0,2,10] + [3,5,6]" but then says
#   [3,5,6] ends at 5 which is outside [0,3]. The explanation itself is contradictory.
#   Let me re-read the expected output carefully.
#
#   The problem says Output: [18, 14, 14] but the explanation is confusing/contradictory.
#   Let me check window [2,5]: valid = [2,4,8], [3,5,6]
#     These don't overlap (4 < 3 is false... wait: [2,4] and [3,5]: do they overlap?
#     [2,4] ends at 4, [3,5] starts at 3. Since 3 <= 4, they overlap!
#     So we can only pick one: max(8, 6) = 8. But expected says 14 = 8+6.
#
#   Hmm, that means the overlap definition might be STRICT: two intervals overlap only
#   if they share more than a single point, i.e., they overlap if s2 < e1 (not s2 <= e1).
#   Or perhaps "non-overlapping" means end < start (strictly), i.e., touching at a point
#   is allowed.
#
#   Let's try: intervals don't overlap if e1 < s2 OR e2 < s1 (strictly less than).
#   [2,4] and [3,5]: e1=4, s2=3 → 4 < 3 is false; s1=2, e2=5 → 5 < 2 is false → they overlap.
#   Still overlapping.
#
#   Let's try: intervals don't overlap if e1 <= s2 (end of first <= start of second).
#   [2,4] and [3,5]: 4 <= 3? No → still overlap.
#
#   Hmm. Let me try another interpretation: maybe "non-overlapping" means e1 < s2
#   but the check is e1 <= s2 (i.e., touching at a point is OK):
#   [2,4] and [3,5]: 4 <= 3? No.
#
#   Wait, maybe the problem uses a different overlap definition where intervals
#   [s1,e1] and [s2,e2] (s1<=s2) overlap only if s2 < e1 (strictly), meaning
#   sharing just the endpoint is NOT considered overlapping.
#   [2,4] and [3,5]: s2=3 < e1=4 → they overlap. Still overlapping.
#
#   Let me try yet another: maybe the problem considers intervals non-overlapping
#   if they don't share any interior point, i.e., e1 <= s2.
#   [2,4] and [3,5]: 4 <= 3? No.
#
#   I'm confused. Let me try to work backwards from the expected output.
#
#   Window [2,5], expected score = 14 = 8 + 6.
#   The only way to get 14 is to select both [2,4,8] and [3,5,6].
#   These intervals share times 3 and 4. So the problem must consider them non-overlapping?
#
#   OR maybe the problem's definition of "non-overlapping" is that the intervals
#   don't share any time point, but the check is done differently...
#
#   Actually wait — re-reading: maybe the problem means intervals are non-overlapping
#   if one ends before the other starts, i.e., e1 < s2. Let me check:
#   [2,4] and [3,5]: e1=4, s2=3 → 4 < 3? No.
#
#   Hmm. Let me look at Example 2:
#   Window [0,3], expected = 13 = 7 + 6 from [0,1,7] and [2,3,6].
#   [0,1] and [2,3]: e1=1, s2=2 → 1 < 2 → non-overlapping. OK.
#
#   Window [1,4], expected = 15. Valid intervals (start>=1, end<=4):
#   [2,3,6] (start=2>=1, end=3<=4) ✓
#   What else? [0,1,7]: start=0 < 1 → invalid. [0,3,10]: start=0 < 1 → invalid.
#   [4,5,9]: end=5 > 4 → invalid.
#   So only [2,3,6] is valid → score = 6. But expected = 15!
#
#   That can't be right. Let me re-read the problem constraints.
#   "Time ranges start from 0 and the last window starts at max(end) - W + 1"
#
#   For Example 2: readings = [[0,1,7],[0,3,10],[2,3,6],[4,5,9]], W=4
#   max_end = 5, last_window_start = 5 - 4 + 1 = 2
#   Windows: t=0→[0,3], t=1→[1,4], t=2→[2,5]
#
#   Window [1,4]: valid = intervals with start>=1 AND end<=4:
#   [0,1,7]: start=0 < 1 → NO
#   [0,3,10]: start=0 < 1 → NO
#   [2,3,6]: start=2>=1, end=3<=4 → YES
#   [4,5,9]: end=5 > 4 → NO
#   Only [2,3,6] → score=6. But expected=15.
#
#   Something is very wrong with my understanding. Let me re-read the problem.
#
#   Oh wait! Maybe the window condition is different. Maybe "fully contained" means
#   the interval overlaps with the window, not that it's fully inside?
#   Or maybe the condition is start >= t and end <= t+W-1 but I'm computing wrong.
#
#   Window [1,4]: t=1, W=4, so window_end = 1+4-1 = 4. That's [1,4]. My calculation is correct.
#
#   Hmm. Let me try: maybe the window is [t, t+W] (size W+1)?
#   Window [1,5]: valid = [2,3,6] (yes), [4,5,9] (start=4>=1, end=5<=5 yes)
#   Non-overlapping: [2,3,6] and [4,5,9] → e1=3 < s2=4 → non-overlapping → 6+9=15! ✓
#
#   So the window is [t, t+W], not [t, t+W-1]! The window has W+1 time points, or
#   equivalently the window size is W but it's [t, t+W].
#
#   Let me verify with Example 2:
#   max_end=5, last_window_start = 5 - W = 5 - 4 = 1? Or max_end - W + 1 = 2?
#
#   If window is [t, t+W]:
#   t=0: [0,4], t=1: [1,5], t=2: [2,6]
#   But max_end=5, so last window where t+W <= max_end: t <= 5-4=1.
#   Windows: t=0,1 → only 2 windows, but expected output has 3 elements.
#
#   Hmm. Let me try yet another interpretation.
#
#