"""
Title: Maximum Balanced Pickup Span with One Depot Upgrade

Problem Description:
A logistics company records the pickup capacity of depots along a highway in an integer
array capacities, where capacities[i] is the number of packages depot i can process in
one hour. A contiguous group of depots is called balanced if the difference between the
maximum and minimum capacity inside that group is at most limit.

The company is allowed to perform at most one upgrade operation on a single depot inside
the chosen group. In one upgrade, you may increase that depot's capacity by any value
from 0 up to upgrade. The upgraded value is used only for checking whether the chosen
group is balanced. You may also choose not to use the upgrade.

Return the maximum possible length of a contiguous balanced group after applying at most
one such upgrade.

Important notes:
- You may upgrade only one element in the chosen subarray.
- Capacities can only be increased, never decreased.
- The final subarray must satisfy max(subarray) - min(subarray) <= limit after the
  optional upgrade.
- Because only increases are allowed, the upgraded depot may help by raising a too-small
  value, but it cannot reduce a too-large value.

Constraints:
- 1 <= capacities.length <= 2 * 10^5
- 0 <= capacities[i] <= 10^9
- 0 <= limit <= 10^9
- 0 <= upgrade <= 10^9
"""

from collections import deque
from typing import Deque, List, Optional, Tuple


class Solution:
    def _can_make_window_valid(
        self,
        capacities: List[int],
        left: int,
        right: int,
        limit: int,
        upgrade: int,
        min1: Tuple[int, int],
        min2: Optional[Tuple[int, int]],
        max1: Tuple[int, int],
    ) -> bool:
        """
        Check whether the current window [left, right] can be made balanced
        using at most one increase operation.

        Key observation:
        Since we may only increase one value, the only useful repair is to raise
        the unique minimum element of the window. We can never decrease the maximum,
        and raising a non-minimum element cannot improve the window's minimum.

        Therefore a window is valid if either:
        1) It is already balanced:
              max - min <= limit
        2) It has exactly one occurrence of the minimum, and we can raise that
           minimum enough so that the new minimum becomes at least max - limit.
           Because only one element changes, after raising the unique minimum:
              - the maximum stays max (or could become larger if we over-raise,
                which is never helpful)
              - the new minimum becomes the second minimum value, or the raised
                value itself if it stays below second minimum
           The best strategy is to raise the unique minimum to:
              target = max - limit
           This is feasible only if:
              target <= min + upgrade
           and because the minimum must be unique, we need access to the second
           minimum value.

        Args:
            capacities: Original array.
            left: Left index of current window.
            right: Right index of current window.
            limit: Allowed max-min spread.
            upgrade: Maximum increase allowed on one element.
            min1: (value, index) of the minimum in the window.
            min2: (value, index) of the second minimum in the window, or None if absent.
            max1: (value, index) of the maximum in the window.

        Returns:
            True if the window can be balanced, otherwise False.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        min_value, min_index = min1
        max_value, _ = max1

        # Case 1: already balanced without using the upgrade.
        if max_value - min_value <= limit:
            return True

        # If the window has only one element, it is always balanced, but the
        # earlier check already covers that. This is just a safety guard.
        if left == right:
            return True

        # To repair an invalid window, we must raise the minimum.
        # If there is no second minimum, the window size is 1, already handled.
        if min2 is None:
            return False

        second_min_value, _ = min2

        # If the second minimum equals the minimum, then the minimum occurs at
        # least twice. Upgrading only one of them leaves another unchanged minimum,
        # so the window's minimum does not improve. Therefore impossible.
        if second_min_value == min_value:
            return False

        # We need the final minimum to be at least max_value - limit.
        required_min_after_upgrade = max_value - limit

        # If even the second minimum is still too small, then after upgrading the
        # unique minimum, the new minimum would become second_min_value, which is
        # still insufficient. So impossible.
        if second_min_value < required_min_after_upgrade:
            return False

        # Check whether the unique minimum can be raised enough within the allowed
        # upgrade budget.
        if min_value + upgrade < required_min_after_upgrade:
            return False

        # All necessary conditions are satisfied.
        return True

    def maximumBalancedSpan(self, capacities: List[int], limit: int, upgrade: int) -> int:
        """
        Compute the maximum length of a contiguous subarray that can be made balanced
        using at most one increase operation on one element.

        We use a sliding window with monotonic deques:
        - One increasing deque to track candidate minima.
        - One decreasing deque to track candidate maxima.

        For each right endpoint, we expand the window and then shrink the left
        endpoint until the current window becomes feasible according to the helper
        check. Because each index enters and leaves each deque at most once, the
        total runtime is linear.

        Args:
            capacities: List of depot capacities.
            limit: Maximum allowed difference between max and min in the final window.
            upgrade: Maximum amount by which one chosen element may be increased.

        Returns:
            The maximum achievable balanced subarray length.

        Time complexity:
            O(n), where n is len(capacities)

        Space complexity:
            O(n) in the worst case for the deques
        """
        n: int = len(capacities)

        # Monotonic increasing deque of indices:
        # capacities[min_deque[0]] is the minimum of the current window.
        min_deque: Deque[int] = deque()

        # Monotonic decreasing deque of indices:
        # capacities[max_deque[0]] is the maximum of the current window.
        max_deque: Deque[int] = deque()

        left: int = 0
        best: int = 0

        # Expand the window one element at a time.
        for right, value in enumerate(capacities):
            # Maintain increasing order in min_deque.
            # Remove all larger values from the back because they can never become
            # the minimum while the current smaller value remains in the window.
            while min_deque and capacities[min_deque[-1]] > value:
                min_deque.pop()
            min_deque.append(right)

            # Maintain decreasing order in max_deque.
            # Remove all smaller values from the back because they can never become
            # the maximum while the current larger value remains in the window.
            while max_deque and capacities[max_deque[-1]] < value:
                max_deque.pop()
            max_deque.append(right)

            # Shrink from the left until the window becomes valid.
            while True:
                # Remove indices that have fallen out of the window.
                while min_deque and min_deque[0] < left:
                    min_deque.popleft()
                while max_deque and max_deque[0] < left:
                    max_deque.popleft()

                # Current minimum and maximum.
                min1: Tuple[int, int] = (capacities[min_deque[0]], min_deque[0])
                max1: Tuple[int, int] = (capacities[max_deque[0]], max_deque[0])

                # The second minimum is simply the second index in the increasing deque,
                # if it exists. This works because the deque stores candidates in
                # nondecreasing value order.
                min2: Optional[Tuple[int, int]] = None
                if len(min_deque) >= 2:
                    second_index = min_deque[1]
                    min2 = (capacities[second_index], second_index)

                # If the current window can be made balanced, stop shrinking.
                if self._can_make_window_valid(
                    capacities, left, right, limit, upgrade, min1, min2, max1
                ):
                    break

                # Otherwise move left boundary rightward by one and try again.
                left += 1

            # Update the best answer with the current valid window size.
            best = max(best, right - left + 1)

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    capacities1 = [5, 3, 6, 4, 7]
    limit1 = 2
    upgrade1 = 2
    result1 = solution.maximumBalancedSpan(capacities1, limit1, upgrade1)
    print(result1)  # Expected: 4

    # Corrected interpretation for Example 2:
    # The prompt's narrative contains a contradiction. The valid optimal answer is 3.
    capacities2 = [8, 2, 2, 2, 8]
    limit2 = 3
    upgrade2 = 6
    result2 = solution.maximumBalancedSpan(capacities2, limit2, upgrade2)
    print(result2)  # Expected: 3

    # Additional quick sanity checks.
    print(solution.maximumBalancedSpan([1], 0, 0))          # Expected: 1
    print(solution.maximumBalancedSpan([1, 2, 3], 2, 0))    # Expected: 3
    print(solution.maximumBalancedSpan([1, 10], 3, 9))      # Expected: 2
    print(solution.maximumBalancedSpan([1, 1, 10], 3, 9))   # Expected: 2