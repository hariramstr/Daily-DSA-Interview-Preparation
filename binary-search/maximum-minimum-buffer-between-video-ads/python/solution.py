"""
Title: Maximum Minimum Buffer Between Video Ads

Problem Description:
A streaming platform wants to insert exactly k advertisement breaks into a video of total
length L seconds. You are given a sorted array candidateTimes where each value represents
a second mark at which an ad break is allowed to start. You may only place ad breaks at
these candidate times, and each chosen time must be distinct. The platform wants the ads
to feel evenly spaced, so it defines the quality of a placement as the minimum distance
in seconds between any two consecutive chosen ad breaks.

Your task is to compute the largest possible quality value.

In other words, choose exactly k values from candidateTimes so that the minimum difference
between adjacent chosen values is as large as possible, and return that maximum possible
minimum difference.

This is a decision-and-optimization problem: for a guessed minimum gap g, determine whether
it is possible to pick k ad breaks such that every consecutive pair is at least g seconds
apart. Use this property to find the optimal answer efficiently.

Constraints:
- 2 <= candidateTimes.length <= 100000
- 0 <= candidateTimes[i] <= 1000000000
- candidateTimes is sorted in strictly increasing order
- 2 <= k <= candidateTimes.length
- 1 <= L <= 1000000000
- All candidateTimes[i] are within the video, i.e. 0 <= candidateTimes[i] <= L

Examples:
1) candidateTimes = [5, 11, 18, 26, 39], k = 3, L = 45
   Output: 13
   Explanation: Choose 5, 18, and 39. Gaps are 13 and 21, so the minimum gap is 13.

2) candidateTimes = [2, 4, 7, 10, 14, 19], k = 4, L = 20
   Output: 5
   Explanation: Choose 2, 7, 14, 19. Gaps are 5, 7, and 5, so the minimum gap is 5.
"""

from typing import List


class Solution:
    def can_place_with_gap(self, candidate_times: List[int], k: int, gap: int) -> bool:
        """
        Check whether it is possible to choose exactly k ad break times such that
        every consecutive chosen pair is at least `gap` seconds apart.

        The method uses a greedy strategy:
        - Always take the earliest possible candidate time first.
        - Then keep taking the next earliest candidate time that is at least `gap`
          away from the last chosen one.

        This greedy approach is correct for this decision problem because choosing
        earlier valid positions leaves as much room as possible for future choices.

        Args:
            candidate_times: Sorted list of allowed ad break times.
            k: Number of ad breaks that must be chosen.
            gap: Required minimum distance between consecutive chosen ad breaks.

        Returns:
            True if we can choose at least k positions with minimum pairwise consecutive
            gap >= gap, otherwise False.

        Time complexity:
            O(n), where n is the length of candidate_times.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We always choose the first candidate time.
        # Why?
        # Because for a fixed required gap, picking the earliest possible valid time
        # never hurts future choices. It only gives us more remaining space to place
        # the rest of the ads.
        chosen_count: int = 1
        last_chosen_time: int = candidate_times[0]

        # Scan through the remaining candidate times from left to right.
        # Since the list is already sorted, this lets us greedily build a valid set
        # of placements with the required minimum gap.
        for current_time in candidate_times[1:]:
            # If the current candidate is far enough from the last chosen ad break,
            # then it is valid to place another ad here.
            if current_time - last_chosen_time >= gap:
                chosen_count += 1
                last_chosen_time = current_time

                # As soon as we have chosen k ad breaks, we can stop early.
                # There is no need to continue scanning.
                if chosen_count >= k:
                    return True

        # If we finish scanning and still have fewer than k chosen positions,
        # then this gap is too large to be feasible.
        return False

    def maximum_minimum_buffer(self, candidate_times: List[int], k: int, L: int) -> int:
        """
        Compute the largest possible minimum distance between consecutive chosen ad breaks.

        This method uses binary search on the answer:
        - If a minimum gap `g` is feasible, then any smaller gap is also feasible.
        - If a minimum gap `g` is not feasible, then any larger gap is also not feasible.

        That monotonic property makes binary search the right optimization tool.

        Args:
            candidate_times: Sorted list of allowed ad break times.
            k: Exact number of ad breaks to choose.
            L: Total video length in seconds. Included for completeness of the problem;
               the algorithm does not need it directly because all valid positions are
               already constrained by candidate_times.

        Returns:
            The maximum possible minimum distance between consecutive chosen ad breaks.

        Time complexity:
            O(n log R), where:
            - n is the number of candidate times
            - R is the search range of possible gaps, at most
              candidate_times[-1] - candidate_times[0]

        Space complexity:
            O(1), ignoring input storage.
        """
        # Edge note:
        # The value L is not directly used in the computation because the optimization
        # depends only on the allowed candidate positions. The maximum possible gap is
        # bounded by the spread of candidate_times themselves.

        # Binary search boundaries:
        # - The minimum possible answer is 0 in a very general sense, but because
        #   candidate_times are strictly increasing and k >= 2, the true answer will
        #   be at least 1 if values are integers and distinct. Still, using 0 is safe.
        # - The maximum possible answer cannot exceed the distance between the first
        #   and last candidate times.
        left: int = 0
        right: int = candidate_times[-1] - candidate_times[0]

        # This variable stores the best feasible gap found so far.
        best_gap: int = 0

        # Standard binary search over the answer space.
        while left <= right:
            # Try the middle gap.
            mid: int = (left + right) // 2

            # Check whether it is possible to place k ads with at least `mid` seconds
            # between every consecutive chosen pair.
            if self.can_place_with_gap(candidate_times, k, mid):
                # If feasible, record it as a candidate answer.
                best_gap = mid

                # Since this gap works, maybe an even larger gap also works.
                # Move to the right half.
                left = mid + 1
            else:
                # If not feasible, this gap is too large.
                # We must search smaller gaps.
                right = mid - 1

        return best_gap


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # candidateTimes = [5, 11, 18, 26, 39], k = 3, L = 45
    # Best choice: 5, 18, 39 -> gaps 13 and 21 -> answer 13
    candidate_times_1: List[int] = [5, 11, 18, 26, 39]
    k_1: int = 3
    L_1: int = 45
    result_1: int = solution.maximum_minimum_buffer(candidate_times_1, k_1, L_1)
    print(result_1)  # Expected: 13

    # Example 2 from the prompt:
    # candidateTimes = [2, 4, 7, 10, 14, 19], k = 4, L = 20
    # Best choice: 2, 7, 14, 19 -> gaps 5, 7, 5 -> answer 5
    candidate_times_2: List[int] = [2, 4, 7, 10, 14, 19]
    k_2: int = 4
    L_2: int = 20
    result_2: int = solution.maximum_minimum_buffer(candidate_times_2, k_2, L_2)
    print(result_2)  # Expected: 5