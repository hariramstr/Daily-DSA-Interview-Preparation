"""
Title: Minimum Beacon Radius for Mountain Rescue Posts

Problem Description:
A rescue agency maintains emergency beacon posts along a long mountain trail.
The trail is modeled as a one-dimensional line.

You are given two integer arrays:
- posts, where posts[i] is the position of the i-th rescue post
- shelters, where shelters[j] is the position of the j-th shelter that must be
  covered by at least one post

A rescue post with signal radius R covers every shelter whose distance from that
post is at most R.

Your task is to compute the minimum integer radius R such that every shelter is
covered by at least one rescue post.

The arrays are not guaranteed to be sorted, and positions may be negative.
Multiple posts or shelters may share the same coordinate.

A straightforward check of every shelter against every post is too slow.
A typical efficient solution sorts the coordinates and uses binary search on the
answer, together with a linear or logarithmic feasibility check.

Return the smallest integer R for which all shelters are covered.

Constraints:
- 1 <= posts.length, shelters.length <= 2 * 10^5
- -10^9 <= posts[i], shelters[j] <= 10^9
- Coordinates may repeat
- Answer fits in a 32-bit signed integer
"""

from typing import List


class Solution:
    def _can_cover_all(self, posts: List[int], shelters: List[int], radius: int) -> bool:
        """
        Check whether a given radius is sufficient to cover every shelter.

        This method assumes both input arrays are already sorted in non-decreasing order.
        It uses a two-pointer sweep:
        - One pointer walks through shelters from left to right.
        - One pointer walks through posts from left to right.
        For each post, we compute the interval [post - radius, post + radius].
        Every shelter inside that interval is covered, so we advance the shelter pointer.

        Args:
            posts: Sorted list of rescue post positions.
            shelters: Sorted list of shelter positions.
            radius: Candidate signal radius to test.

        Returns:
            True if every shelter is covered by at least one post, otherwise False.

        Time complexity:
            O(len(posts) + len(shelters))

        Space complexity:
            O(1)
        """
        # This pointer tells us the index of the first shelter that has not yet
        # been confirmed as covered.
        shelter_index: int = 0
        total_shelters: int = len(shelters)

        # We process posts from left to right.
        # Because both arrays are sorted, once a shelter is covered, we never need
        # to revisit it again.
        for post_position in posts:
            # Compute the leftmost and rightmost shelter positions this post can cover.
            left_reach: int = post_position - radius
            right_reach: int = post_position + radius

            # If the current uncovered shelter lies to the left of left_reach,
            # that shelter cannot be covered by this post.
            # However, it might have been coverable by an earlier post.
            # Since we always move left to right and only keep the first uncovered
            # shelter, if shelters[shelter_index] < left_reach here, then that
            # shelter was not covered by any earlier post either.
            # Therefore coverage is impossible for this radius.
            if shelter_index < total_shelters and shelters[shelter_index] < left_reach:
                return False

            # Advance the shelter pointer while the current post covers shelters.
            # Every shelter with position <= right_reach is covered by this post.
            while shelter_index < total_shelters and shelters[shelter_index] <= right_reach:
                shelter_index += 1

            # Small optimization:
            # If all shelters are covered, we can stop early.
            if shelter_index == total_shelters:
                return True

        # After using all posts, coverage is successful only if we consumed all shelters.
        return shelter_index == total_shelters

    def find_min_radius(self, posts: List[int], shelters: List[int]) -> int:
        """
        Compute the minimum integer radius needed so every shelter is covered.

        The algorithm works in two main phases:
        1. Sort both arrays so we can reason about positions from left to right.
        2. Binary search the answer radius R.
           - If a radius works, try smaller.
           - If a radius does not work, try larger.

        Why binary search works:
        - Feasibility is monotonic.
        - If radius R can cover all shelters, then any radius > R can also cover all shelters.
        - If radius R cannot cover all shelters, then any radius < R also cannot.

        Args:
            posts: Positions of rescue posts.
            shelters: Positions of shelters.

        Returns:
            The smallest integer radius that covers all shelters.

        Time complexity:
            O((len(posts) + len(shelters)) * log M + len(posts) log len(posts) + len(shelters) log len(shelters))
            where M is the search range of the answer.

        Space complexity:
            O(1) extra space beyond the space used by sorting, if sorting in place is allowed.
        """
        # Sort both arrays in place.
        # Sorting is essential because:
        # - It allows a linear feasibility check with two pointers.
        # - It makes the "covered interval" idea easy to apply.
        posts.sort()
        shelters.sort()

        # Establish binary search bounds.
        #
        # Lower bound:
        # The minimum possible radius is 0.
        #
        # Upper bound:
        # A safe upper bound is the maximum distance between any shelter and any post
        # at the extremes. Using the farthest possible difference guarantees that the
        # answer is inside [0, high].
        #
        # Since arrays are sorted:
        # - leftmost post is posts[0]
        # - rightmost post is posts[-1]
        # - leftmost shelter is shelters[0]
        # - rightmost shelter is shelters[-1]
        #
        # The maximum needed radius cannot exceed the worst-case distance from a shelter
        # to one of the extreme posts.
        high: int = max(
            abs(shelters[0] - posts[0]),
            abs(shelters[0] - posts[-1]),
            abs(shelters[-1] - posts[0]),
            abs(shelters[-1] - posts[-1]),
        )
        low: int = 0

        # Standard binary search for the first feasible radius.
        #
        # Invariant:
        # - The true answer is always within [low, high].
        while low < high:
            mid: int = (low + high) // 2

            # Test whether this candidate radius is enough.
            if self._can_cover_all(posts, shelters, mid):
                # mid works, so the answer is <= mid.
                # Keep searching the left half to find the minimum feasible radius.
                high = mid
            else:
                # mid does not work, so the answer must be > mid.
                low = mid + 1

        # At loop end, low == high and points to the smallest feasible radius.
        return low


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    posts_1: List[int] = [1, 8, 15]
    shelters_1: List[int] = [2, 6, 10, 14]
    result_1: int = solution.find_min_radius(posts_1, shelters_1)
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2
    posts_2: List[int] = [-5, 0, 9]
    shelters_2: List[int] = [-8, -4, 3, 11]
    result_2: int = solution.find_min_radius(posts_2, shelters_2)
    print("Example 2 Result:", result_2)  # Expected: 4

    # Additional quick checks
    posts_3: List[int] = [0]
    shelters_3: List[int] = [0]
    result_3: int = solution.find_min_radius(posts_3, shelters_3)
    print("Additional Check 1:", result_3)  # Expected: 0

    posts_4: List[int] = [5, 5, 5]
    shelters_4: List[int] = [1, 5, 9]
    result_4: int = solution.find_min_radius(posts_4, shelters_4)
    print("Additional Check 2:", result_4)  # Expected: 4