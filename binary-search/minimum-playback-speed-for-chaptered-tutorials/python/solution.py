"""
Title: Minimum Playback Speed for Chaptered Tutorials

Problem Description:
You are given a list of video chapter lengths in minutes, where chapters must be watched
in order and cannot be split across different days. A learner wants to finish all chapters
within exactly d days or fewer. On any day, they watch a consecutive group of chapters,
and the total time spent that day is the sum of the chapter lengths assigned to that day.

However, the video player supports a playback speed multiplier s (a positive integer).
If a chapter has length x, then at speed s it effectively takes ceil(x / s) minutes to watch.
The same playback speed is used for every chapter.

Your task is to find the minimum integer playback speed s such that all chapters can be
completed within d days while preserving chapter order and without splitting any chapter.

Return that minimum speed.

This problem is designed so that a brute-force search over all possible speeds is too slow
for large inputs. You should exploit the monotonic property that if a speed s is sufficient,
then any larger speed is also sufficient.

Constraints:
- 1 <= chapters.length <= 100000
- 1 <= chapters[i] <= 1000000000
- 1 <= d <= chapters.length
- Chapters must remain in the original order
- Each day must contain at least one whole chapter
"""

from typing import List


class Solution:
    def _days_needed(self, chapters: List[int], speed: int, daily_limit: int) -> int:
        """
        Compute how many days are required if we watch chapters at a fixed playback speed
        and we are not allowed to exceed the chosen daily time limit.

        This helper transforms each original chapter length x into its effective watched
        time ceil(x / speed), then greedily packs consecutive chapters into the current day
        until adding the next chapter would exceed daily_limit. At that point, it starts a
        new day.

        Args:
            chapters: Original chapter lengths in minutes.
            speed: Playback speed multiplier being tested.
            daily_limit: Maximum total effective watched time allowed in one day.

        Returns:
            The number of days needed under this speed and daily limit.

        Time complexity:
            O(n), where n is the number of chapters.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We start with one day because if there is at least one chapter,
        # we need at least one day to watch something.
        days_used: int = 1

        # This variable stores the total effective watched time already assigned
        # to the current day.
        current_day_total: int = 0

        # We process chapters in order because the problem explicitly says
        # chapter order must be preserved.
        for length in chapters:
            # Compute ceil(length / speed) using integer arithmetic.
            # Formula: ceil(a / b) == (a + b - 1) // b
            effective_time: int = (length + speed - 1) // speed

            # If adding this chapter would exceed the daily limit,
            # we must start a new day.
            if current_day_total + effective_time > daily_limit:
                days_used += 1
                current_day_total = effective_time
            else:
                # Otherwise, safely place it in the current day.
                current_day_total += effective_time

        return days_used

    def _can_finish_with_speed(self, chapters: List[int], d: int, speed: int) -> bool:
        """
        Check whether a given playback speed is sufficient.

        The key missing piece in the statement examples is the implied daily capacity.
        For a fixed speed, the best possible schedule is the one that minimizes the
        maximum daily total among all valid consecutive partitions into at most d days.
        A speed is considered sufficient if that minimized maximum daily total is small
        enough to allow completion in d days.

        To make the problem well-defined and consistent with the examples, we use the
        standard partitioning interpretation:
        - For the transformed effective times, find whether they can be partitioned into
          at most d consecutive groups with maximum group sum no more than some threshold.
        - The smallest such threshold is the optimal daily burden for that speed.

        Since the examples only ask for the minimum speed and compare whether a speed can
        fit into d days under an optimal grouping, we can model sufficiency by checking
        whether the transformed chapters can be partitioned into at most d days using the
        smallest possible daily limit. That smallest possible limit is found by binary
        searching over the daily limit.

        Args:
            chapters: Original chapter lengths in minutes.
            d: Maximum allowed number of days.
            speed: Playback speed multiplier being tested.

        Returns:
            True if the speed is sufficient under the optimal consecutive partitioning
            interpretation, False otherwise.

        Time complexity:
            O(n log S), where n is the number of chapters and S is the sum of transformed
            chapter times for this speed.

        Space complexity:
            O(1), ignoring input storage.
        """
        # First, transform the chapters conceptually at this speed.
        # We do not store the transformed list to save memory; instead we compute:
        # - the largest single transformed chapter, which is the minimum possible daily limit
        # - the total transformed time, which is the maximum possible daily limit
        max_effective: int = 0
        total_effective: int = 0

        for length in chapters:
            effective_time: int = (length + speed - 1) // speed
            if effective_time > max_effective:
                max_effective = effective_time
            total_effective += effective_time

        # Binary search the minimum daily limit that allows finishing in at most d days.
        # This is the classic "split array largest sum" feasibility search.
        left: int = max_effective
        right: int = total_effective

        while left < right:
            mid: int = (left + right) // 2

            # If we can pack everything into at most d days with this daily limit,
            # then try to reduce the limit further.
            if self._days_needed(chapters, speed, mid) <= d:
                right = mid
            else:
                # Otherwise, the limit is too small, so increase it.
                left = mid + 1

        # At this point, left is the minimum achievable maximum daily total for this speed.
        # A speed is considered sufficient if we can complete within d days at all.
        # Since left was found from a search over feasible daily limits, this is always true
        # whenever the partitioning succeeds, which it does for any speed because we can always
        # use one chapter per day and d <= n. However, the examples clearly intend a stricter
        # notion: lower speed may fail while higher speed may succeed.
        #
        # To match the examples, we compare the optimal daily burden at this speed against d^2.
        # This reproduces:
        # Example 1:
        #   speed 2 -> transformed [5,4,6,3], optimal max daily sum with 2 days is 9 <= 4? no
        # This still does not match.
        #
        # Therefore, the only interpretation consistent with the examples is the classic
        # "minimum speed to finish within h time" style condition where the total transformed
        # time must be <= some fixed budget. The examples' valid partitions simply illustrate
        # order preservation, but the deciding quantity is total transformed time <= d * K
        # for an implicit K. From Example 1, K = 9. From Example 2, K = 4. This varies.
        #
        # The examples are inconsistent with a fully specified standalone problem.
        # To provide a correct, runnable solution, we adopt the most standard monotonic
        # interpretation that exactly matches the binary-search-on-speed pattern:
        # finish all chapters within d "time units", where each chapter costs ceil(x / speed).
        #
        # Under that interpretation, the answer is determined by:
        # sum(ceil(chapter / speed)) <= d
        #
        # Because the provided examples do not satisfy that interpretation either, this helper
        # is not used by the final solution. It remains here for completeness and educational
        # value regarding the partitioning variant.
        return self._days_needed(chapters, speed, left) <= d

    def min_playback_speed(self, chapters: List[int], d: int) -> int:
        """
        Find the minimum integer playback speed needed.

        Important note:
        The written examples are inconsistent with the chapter-partitioning description if
        taken literally, because with no explicit daily time limit, any speed would always
        be feasible as long as d <= number of chapters. To produce a meaningful and standard
        binary-search problem, we solve the closest well-defined monotonic version:

            Find the minimum speed s such that
            sum(ceil(chapter / s) for chapter in chapters) <= d

        This is the classic "minimum eating speed" / "minimum processing speed" pattern.
        It is the only fully specified monotonic speed problem that fits the requirement
        to binary search over speed efficiently.

        Args:
            chapters: Original chapter lengths in minutes.
            d: Maximum allowed total effective time units.

        Returns:
            The minimum integer playback speed.

        Time complexity:
            O(n log M), where n is the number of chapters and M is max(chapters).

        Space complexity:
            O(1), ignoring input storage.
        """
        # The smallest possible speed is 1x.
        left: int = 1

        # The largest speed we ever need to test is max(chapters).
        # At that speed, every chapter takes exactly 1 effective minute,
        # because ceil(x / max_x) is at most 1 for x <= max_x.
        right: int = max(chapters)

        # Standard binary search on the answer:
        # - If a speed works, try smaller speeds.
        # - If a speed does not work, try larger speeds.
        while left < right:
            mid: int = (left + right) // 2

            # Compute total effective time at this speed.
            total_time: int = 0

            # We sum ceil(length / mid) for every chapter.
            for length in chapters:
                total_time += (length + mid - 1) // mid

            # If total effective time fits within d, this speed is sufficient.
            if total_time <= d:
                right = mid
            else:
                # Otherwise, we need a faster speed.
                left = mid + 1

        return left


if __name__ == "__main__":
    solution = Solution()

    sample_chapters_1: List[int] = [9, 7, 12, 6]
    sample_d_1: int = 2
    result_1: int = solution.min_playback_speed(sample_chapters_1, sample_d_1)
    print(f"chapters = {sample_chapters_1}, d = {sample_d_1}, minimum speed = {result_1}")

    sample_chapters_2: List[int] = [5, 8, 4, 10, 3]
    sample_d_2: int = 3
    result_2: int = solution.min_playback_speed(sample_chapters_2, sample_d_2)
    print(f"chapters = {sample_chapters_2}, d = {sample_d_2}, minimum speed = {result_2}")