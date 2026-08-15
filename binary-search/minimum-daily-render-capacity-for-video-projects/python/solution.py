"""
Title: Minimum Daily Render Capacity for Video Projects

Problem Description:
A media studio needs to render a sequence of video projects on a shared render farm.
You are given an array `frames`, where `frames[i]` is the number of frame-units
required by the `i`th project. The projects must be rendered in the given order,
and a single day can process only a contiguous group of projects.

If the render farm has daily capacity `C`, then the total frame-units assigned to
any one day cannot exceed `C`.

Given `frames` and an integer `d`, return the minimum daily render capacity needed
to finish all projects in at most `d` days.

Rules:
- You cannot split a single project across multiple days.
- You cannot reorder projects.
- Each day processes a contiguous block of projects.

This is a classic "minimum feasible capacity" problem:
- We can test whether a chosen capacity works by simulating the rendering process.
- Then we can use binary search to find the smallest capacity that is feasible.

Constraints:
- 1 <= frames.length <= 100000
- 1 <= frames[i] <= 1000000000
- 1 <= d <= frames.length
- The answer fits in a 64-bit signed integer.
"""

from typing import List


class Solution:
    def _can_finish_with_capacity(self, frames: List[int], d: int, capacity: int) -> bool:
        """
        Check whether all projects can be rendered in at most `d` days
        if the daily render capacity is `capacity`.

        Args:
            frames: List of frame-units required for each project, in fixed order.
            d: Maximum number of days allowed.
            capacity: Candidate daily render capacity to test.

        Returns:
            True if all projects can be completed in at most `d` days, otherwise False.

        Time complexity:
            O(n), where n is the number of projects.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We start with day 1 because we always need at least one day
        # when there is at least one project.
        days_used: int = 1

        # This variable stores the total frame-units already assigned
        # to the current day.
        current_day_load: int = 0

        # We process projects in the given order because reordering is forbidden.
        for project_frames in frames:
            # If adding the current project would exceed today's capacity,
            # we must start a new day.
            #
            # Why?
            # - Projects cannot be split.
            # - Each day must contain a contiguous sequence of projects.
            # So once a project does not fit in the current day, the only legal
            # action is to move it to the next day.
            if current_day_load + project_frames > capacity:
                days_used += 1
                current_day_load = project_frames

                # Early exit optimization:
                # If we already need more than d days, then this capacity is not feasible.
                if days_used > d:
                    return False
            else:
                # Otherwise, the project fits in the current day,
                # so we simply add it to today's load.
                current_day_load += project_frames

        # If we finish processing all projects without exceeding d days,
        # then this capacity works.
        return True

    def min_render_capacity(self, frames: List[int], d: int) -> int:
        """
        Find the minimum daily render capacity needed to finish all projects
        in at most `d` days.

        This uses binary search on the answer:
        - The minimum possible capacity is the largest single project,
          because every project must fit in one day by itself if needed.
        - The maximum possible capacity is the sum of all projects,
          which means everything can be rendered in one day.

        Args:
            frames: List of frame-units required for each project, in fixed order.
            d: Maximum number of days allowed.

        Returns:
            The smallest feasible daily render capacity.

        Time complexity:
            O(n * log(sum(frames))), where n is the number of projects.

        Space complexity:
            O(1), excluding input storage.
        """
        # Lower bound:
        # The capacity cannot be smaller than the largest single project,
        # because splitting a project is not allowed.
        left: int = max(frames)

        # Upper bound:
        # The capacity can always be the total sum, which means all projects
        # are rendered in one day.
        right: int = sum(frames)

        # Binary search invariant:
        # - Every feasible answer is in the range [left, right].
        # - We want the smallest feasible value.
        while left < right:
            # Middle capacity to test.
            # Using integer division keeps everything as integers.
            mid: int = (left + right) // 2

            # If `mid` is enough to finish within `d` days,
            # then the true answer is <= mid.
            # So we keep the left half, including mid.
            if self._can_finish_with_capacity(frames, d, mid):
                right = mid
            else:
                # Otherwise, `mid` is too small, so the answer must be > mid.
                left = mid + 1

        # When left == right, binary search has found the smallest feasible capacity.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # Correct trace:
    # frames = [30, 10, 20, 40, 25], d = 3
    #
    # Capacity 50:
    # Day 1: 30 + 10 = 40, next 20 would make 60 -> stop
    # Day 2: 20, next 40 would make 60 -> stop
    # Day 3: 40, next 25 would make 65 -> stop
    # Day 4: 25
    # Needs 4 days, so 50 is NOT enough.
    #
    # Capacity 65:
    # Day 1: 30 + 10 + 20 = 60, next 40 would make 100 -> stop
    # Day 2: 40 + 25 = 65
    # Needs 2 days, so 65 works.
    #
    # We can also verify smaller values:
    # Capacity 60:
    # Day 1: 30 + 10 + 20 = 60
    # Day 2: 40
    # Day 3: 25
    # Needs 3 days, so 60 works.
    #
    # Capacity 59:
    # Day 1: 30 + 10 = 40, next 20 would make 60 -> stop
    # Day 2: 20, next 40 would make 60 -> stop
    # Day 3: 40, next 25 would make 65 -> stop
    # Day 4: 25
    # Needs 4 days, so 59 does not work.
    #
    # Therefore the true minimum is 60.
    frames1: List[int] = [30, 10, 20, 40, 25]
    d1: int = 3
    result1: int = solution.min_render_capacity(frames1, d1)
    print(f"Example 1 result: {result1}")  # Expected correct answer: 60

    # Example 2
    # frames = [8, 15, 7, 12, 10], d = 2
    #
    # Capacity 29:
    # Day 1: 8 + 15 = 23, next 7 would make 30 -> stop
    # Day 2: 7 + 12 = 19, next 10 would make 29 -> actually fits
    # So capacity 29 works in 2 days:
    # [8, 15] and [7, 12, 10]
    #
    # Capacity 28:
    # Day 1: 8 + 15 = 23, next 7 would make 30 -> stop
    # Day 2: 7 + 12 = 19, next 10 would make 29 -> stop
    # Day 3: 10
    # Needs 3 days, so 28 does not work.
    #
    # Therefore the true minimum is 29.
    frames2: List[int] = [8, 15, 7, 12, 10]
    d2: int = 2
    result2: int = solution.min_render_capacity(frames2, d2)
    print(f"Example 2 result: {result2}")  # Expected correct answer: 29

    # Additional quick sanity checks
    frames3: List[int] = [5]
    d3: int = 1
    result3: int = solution.min_render_capacity(frames3, d3)
    print(f"Single project result: {result3}")  # Expected: 5

    frames4: List[int] = [1, 2, 3, 4, 5]
    d4: int = 5
    result4: int = solution.min_render_capacity(frames4, d4)
    print(f"One project per day result: {result4}")  # Expected: 5

    frames5: List[int] = [1, 2, 3, 4, 5]
    d5: int = 1
    result5: int = solution.min_render_capacity(frames5, d5)
    print(f"All in one day result: {result5}")  # Expected: 15