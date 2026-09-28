"""
Title: Shortest Playlist Segment With Mood Coverage and Replay Caps
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A music streaming team is analyzing a generated playlist. Each song belongs to exactly one mood category,
represented by an integer in the array `moods`, where `moods[i]` is the mood of the `i`-th song.
You are also given a dictionary `required` where `required[x]` is the minimum number of songs of mood `x`
that must appear in a segment, and an integer `cap`.

Find the length of the shortest contiguous segment of the playlist such that:
1. For every mood `x` in `required`, the segment contains at least `required[x]` songs of mood `x`.
2. No mood appears more than `cap` times inside the segment, including moods that are not listed in `required`.

Return the minimum possible length of such a segment, or `-1` if no valid segment exists.

This is a hard sliding window problem because the window must simultaneously satisfy lower bounds for some
categories and a global upper bound for all categories. A window may fail because it is missing required moods,
or because some mood appears too often and forces the left boundary to move.

Constraints:
- `1 <= moods.length <= 2 * 10^5`
- `1 <= moods[i] <= 10^9`
- `1 <= required.size <= 2 * 10^5`
- `1 <= required[x] <= cap <= moods.length`
- The sum of distinct moods in `moods` and `required` can be large, so solutions depending on small value ranges
  are not allowed.

Example 1:
Input: moods = [4,1,2,1,3,2,1,4], required = {1:2, 2:1, 3:1}, cap = 3
Output: 5
Explanation: The segment [1,2,1,3,2] has counts {1:2, 2:2, 3:1}. It satisfies all required minimums,
and no mood appears more than 3 times. No valid segment of length 4 exists.

Example 2:
Input: moods = [5,5,1,2,5,3,1,2], required = {1:1, 2:1, 3:1}, cap = 2
Output: 4
Explanation: The segment [2,5,3,1] is valid with counts {2:1, 5:1, 3:1, 1:1}. The segment [5,3,1,2]
is also valid. Any shorter segment misses at least one required mood.
"""

from typing import Dict, List


class Solution:
    def shortest_playlist_segment(self, moods: List[int], required: Dict[int, int], cap: int) -> int:
        """
        Find the minimum length of a contiguous segment that satisfies:
        1. Every required mood appears at least the requested number of times.
        2. No mood appears more than `cap` times.

        This uses a sliding window with frequency counting.

        Args:
            moods: List of mood IDs for the playlist.
            required: Mapping from mood ID to minimum required count in the segment.
            cap: Maximum allowed count for any mood inside the segment.

        Returns:
            The length of the shortest valid segment, or -1 if no such segment exists.

        Time complexity:
            O(n), where n is the length of `moods`.
            Each element is added to the window once and removed from the window once.

        Space complexity:
            O(k), where k is the number of distinct moods currently tracked in the window
            plus the number of required moods.
        """
        # ------------------------------------------------------------
        # Early feasibility check:
        # If the entire playlist does not even contain enough copies of a required mood,
        # then no subarray can possibly satisfy the lower-bound requirement.
        #
        # This is not strictly necessary for correctness, but it is a helpful and
        # beginner-friendly optimization because it lets us reject impossible cases early.
        # ------------------------------------------------------------
        total_counts: Dict[int, int] = {}
        for mood in moods:
            total_counts[mood] = total_counts.get(mood, 0) + 1

        for mood, need in required.items():
            if total_counts.get(mood, 0) < need:
                return -1

        # ------------------------------------------------------------
        # Sliding window setup:
        #
        # left  -> left boundary of the current window
        # right -> right boundary as we iterate through the array
        #
        # window_counts[m] stores how many times mood m appears in the current window.
        #
        # We also maintain:
        # - satisfied_required:
        #   number of required moods whose current count has reached the needed minimum.
        # - total_required_kinds:
        #   total number of distinct moods that must be satisfied.
        #
        # A window is valid if:
        #   satisfied_required == total_required_kinds
        # and
        #   no mood count exceeds cap
        #
        # For the second condition, instead of scanning all moods every time,
        # we maintain:
        # - over_cap_kinds:
        #   number of moods whose count is currently > cap
        #
        # Then the cap condition is simply:
        #   over_cap_kinds == 0
        # ------------------------------------------------------------
        window_counts: Dict[int, int] = {}
        left: int = 0
        satisfied_required: int = 0
        total_required_kinds: int = len(required)
        over_cap_kinds: int = 0

        # Use a large sentinel value for the best answer found so far.
        best: int = len(moods) + 1

        # ------------------------------------------------------------
        # Expand the window by moving `right` from left to right.
        # For each new mood added:
        # 1. Update its frequency in the window.
        # 2. If it just crossed from cap to cap+1, increase over_cap_kinds.
        # 3. If it is a required mood and just reached its required minimum,
        #    increase satisfied_required.
        # ------------------------------------------------------------
        for right, mood in enumerate(moods):
            old_count: int = window_counts.get(mood, 0)
            new_count: int = old_count + 1
            window_counts[mood] = new_count

            # If this mood was exactly at cap before adding,
            # then after adding it becomes cap+1 and violates the cap rule.
            if old_count == cap:
                over_cap_kinds += 1

            # If this mood is required and we just reached the needed count,
            # then one more required category is now satisfied.
            if mood in required and new_count == required[mood]:
                satisfied_required += 1

            # --------------------------------------------------------
            # First, enforce the hard upper-bound rule:
            # while any mood appears more than cap times, the window is invalid
            # regardless of required coverage, so we must shrink from the left.
            #
            # This loop guarantees that after it finishes:
            #   over_cap_kinds == 0
            # meaning every mood count is <= cap.
            # --------------------------------------------------------
            while over_cap_kinds > 0:
                left_mood: int = moods[left]
                left_old_count: int = window_counts[left_mood]

                # If left_mood is required and removing it would drop the count
                # from exactly the required threshold to below it, then we lose
                # satisfaction for that required mood.
                if left_mood in required and left_old_count == required[left_mood]:
                    satisfied_required -= 1

                # If left_mood currently exceeds cap by being exactly cap+1,
                # then removing one occurrence will bring it back down to cap,
                # so one cap violation disappears.
                if left_old_count == cap + 1:
                    over_cap_kinds -= 1

                # Actually remove the leftmost element from the window.
                new_left_count: int = left_old_count - 1
                if new_left_count == 0:
                    del window_counts[left_mood]
                else:
                    window_counts[left_mood] = new_left_count

                left += 1

            # --------------------------------------------------------
            # At this point, the window satisfies the cap rule.
            #
            # If it also satisfies all required lower bounds, then it is valid.
            # We now try to shrink it from the left as much as possible while
            # keeping it valid, because for a fixed `right`, the shortest valid
            # window ending at `right` is the one we want to record.
            #
            # Important idea:
            # We can safely remove the leftmost mood if doing so does NOT break:
            # - a required minimum
            #
            # We do not need to worry about the cap rule while shrinking here,
            # because removing elements can never create a new "count > cap" problem.
            # It only makes counts smaller.
            # --------------------------------------------------------
            while satisfied_required == total_required_kinds:
                current_length: int = right - left + 1
                if current_length < best:
                    best = current_length

                left_mood = moods[left]
                left_count = window_counts[left_mood]

                # Decide whether removing the leftmost mood would break validity.
                #
                # If left_mood is required and its count is exactly the required minimum,
                # then removing it would make the window invalid, so we must stop.
                if left_mood in required and left_count == required[left_mood]:
                    break

                # Otherwise, it is safe to remove:
                # - either left_mood is not required
                # - or it is required but currently appears more than needed
                new_left_count = left_count - 1
                if new_left_count == 0:
                    del window_counts[left_mood]
                else:
                    window_counts[left_mood] = new_left_count

                left += 1

        return best if best <= len(moods) else -1

    def minSegmentLength(self, moods: List[int], required: Dict[int, int], cap: int) -> int:
        """
        Wrapper method matching a shorter interview-style naming convention.

        Args:
            moods: List of mood IDs for the playlist.
            required: Mapping from mood ID to minimum required count in the segment.
            cap: Maximum allowed count for any mood inside the segment.

        Returns:
            The length of the shortest valid segment, or -1 if none exists.

        Time complexity:
            O(n), where n is the length of `moods`.

        Space complexity:
            O(k), where k is the number of tracked distinct moods.
        """
        return self.shortest_playlist_segment(moods, required, cap)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    moods1: List[int] = [4, 1, 2, 1, 3, 2, 1, 4]
    required1: Dict[int, int] = {1: 2, 2: 1, 3: 1}
    cap1: int = 3
    result1: int = solution.shortest_playlist_segment(moods1, required1, cap1)
    print(result1)  # Expected: 5

    # Example 2
    moods2: List[int] = [5, 5, 1, 2, 5, 3, 1, 2]
    required2: Dict[int, int] = {1: 1, 2: 1, 3: 1}
    cap2: int = 2
    result2: int = solution.shortest_playlist_segment(moods2, required2, cap2)
    print(result2)  # Expected: 4

    # Additional quick sanity checks
    moods3: List[int] = [1, 1, 1]
    required3: Dict[int, int] = {1: 2}
    cap3: int = 2
    result3: int = solution.shortest_playlist_segment(moods3, required3, cap3)
    print(result3)  # Expected: 2

    moods4: List[int] = [1, 2, 3]
    required4: Dict[int, int] = {1: 1, 4: 1}
    cap4: int = 2
    result4: int = solution.shortest_playlist_segment(moods4, required4, cap4)
    print(result4)  # Expected: -1