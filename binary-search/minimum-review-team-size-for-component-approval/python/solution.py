"""
Title: Minimum Review Team Size for Component Approval

Problem Description:
You are planning code reviews for a large release made up of n software components.
Component i requires reviews[i] independent review comments before it can be approved.

You have a pool of engineers, and every engineer can review a contiguous block of
components during the release window. Because of domain knowledge limits, a single
engineer can cover at most span consecutive components, but while assigned to that
block, the engineer contributes exactly 1 review to every component in the block.

You may choose any number of engineers and assign each engineer to any contiguous
block of length at most span. Multiple engineers may review overlapping blocks.
A component is approved if the total number of engineers whose assigned blocks include
that component is at least reviews[i].

Return the minimum number of engineers needed so that every component is approved.

Key idea:
- The answer is monotonic:
  if x engineers are enough, then any number > x is also enough.
- That allows binary search on the minimum feasible number of engineers.
- To test whether a fixed number of engineers is enough, we use a greedy left-to-right
  construction with a difference array to efficiently track how many active engineers
  currently cover each component.
"""

from typing import List


class Solution:
    def minimum_review_team_size(self, reviews: List[int], span: int) -> int:
        """
        Compute the minimum number of engineers needed to satisfy all review requirements.

        The method uses binary search on the answer. For a candidate team size k,
        it checks feasibility using a greedy sweep:
        - Move from left to right.
        - At each component, determine how many reviews are already provided by
          previously started engineers.
        - If the current component still needs more reviews, start exactly that many
          new engineers here, extending them as far right as possible (up to span).
          This is optimal because longer coverage helps future components the most.

        Args:
            reviews: List where reviews[i] is the minimum required review count
                     for component i.
            span: Maximum number of consecutive components one engineer can cover.

        Returns:
            The minimum number of engineers required.

        Time complexity:
            O(n log S), where n is len(reviews) and S is the answer range
            (bounded by sum(reviews)).

        Space complexity:
            O(n) for the difference array used in feasibility checking.
        """
        n: int = len(reviews)

        # Lower bound:
        # At least 0 engineers are needed.
        left: int = 0

        # Upper bound:
        # A simple always-valid construction is to satisfy each component independently
        # using blocks of length 1. That uses exactly sum(reviews) engineers.
        # Since each engineer may cover at most span components, length 1 is allowed.
        right: int = sum(reviews)

        # Standard binary search for the smallest feasible value.
        while left < right:
            mid: int = (left + right) // 2

            # If mid engineers are enough, try to find an even smaller answer.
            if self._can_approve_with_k_engineers(reviews, span, mid):
                right = mid
            else:
                # Otherwise, we need more engineers.
                left = mid + 1

        return left

    def _can_approve_with_k_engineers(self, reviews: List[int], span: int, k: int) -> bool:
        """
        Check whether all components can be approved using at most k engineers.

        Greedy strategy:
        - Process components from left to right.
        - Maintain the number of currently active engineers covering the current index.
        - If current coverage is below reviews[i], we must add more engineers now.
          Waiting is impossible because future-starting engineers cannot cover the
          current component.
        - When adding engineers at position i, assign them to the longest possible
          block [i, min(n - 1, i + span - 1)] so they help as many future components
          as possible.

        We implement active coverage efficiently with a difference array:
        - When x engineers start covering at i, active coverage increases by x at i.
        - Their effect ends after their block, so we subtract x at end + 1.

        Args:
            reviews: Required review counts per component.
            span: Maximum block length one engineer can cover.
            k: Candidate number of engineers available.

        Returns:
            True if all requirements can be met with at most k engineers, else False.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(reviews)

        # diff[pos] stores how much the active coverage changes when we arrive at pos.
        # We use size n + 1 so that an "end + 1" update is always safe.
        diff: List[int] = [0] * (n + 1)

        # active_coverage = number of engineers whose assigned blocks currently include i.
        active_coverage: int = 0

        # used_engineers = total number of engineers started so far.
        used_engineers: int = 0

        # Sweep from left to right.
        for i in range(n):
            # Apply all coverage changes that begin/end at this position.
            # This updates the number of engineers currently covering component i.
            active_coverage += diff[i]

            # If current coverage is already enough, no action is needed.
            if active_coverage >= reviews[i]:
                continue

            # Otherwise, we are short by exactly this many reviews at component i.
            # Because no future-starting engineer can cover i, these extra engineers
            # MUST be started now if a solution exists.
            needed: int = reviews[i] - active_coverage

            # If using these engineers would exceed the budget k, then k is not feasible.
            used_engineers += needed
            if used_engineers > k:
                return False

            # Start 'needed' engineers at i.
            # Greedy choice: make each one cover the longest possible block.
            # Why this is optimal:
            # - Every new engineer must cover i.
            # - Extending farther right can only help future components, never hurt.
            active_coverage += needed

            # Their coverage ends after index end.
            end: int = min(n - 1, i + span - 1)

            # At position end + 1, these engineers stop contributing.
            if end + 1 < len(diff):
                diff[end + 1] -= needed

        # If we finish the sweep without exceeding k, then k engineers are enough.
        return True


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    reviews1: List[int] = [1, 2, 2, 1]
    span1: int = 2
    result1: int = solution.minimum_review_team_size(reviews1, span1)
    print("Example 1:")
    print(f"reviews = {reviews1}, span = {span1}")
    print(f"Minimum engineers needed = {result1}")
    print("Expected = 3")
    print()

    # Example 2
    reviews2: List[int] = [3, 0, 1, 4, 2]
    span2: int = 3
    result2: int = solution.minimum_review_team_size(reviews2, span2)
    print("Example 2:")
    print(f"reviews = {reviews2}, span = {span2}")
    print(f"Minimum engineers needed = {result2}")
    print("Expected = 6")
    print()

    # Additional small sanity checks
    reviews3: List[int] = [0, 0, 0]
    span3: int = 2
    result3: int = solution.minimum_review_team_size(reviews3, span3)
    print("Sanity Check 1:")
    print(f"reviews = {reviews3}, span = {span3}")
    print(f"Minimum engineers needed = {result3}")
    print("Expected = 0")
    print()

    reviews4: List[int] = [5]
    span4: int = 1
    result4: int = solution.minimum_review_team_size(reviews4, span4)
    print("Sanity Check 2:")
    print(f"reviews = {reviews4}, span = {span4}")
    print(f"Minimum engineers needed = {result4}")
    print("Expected = 5")