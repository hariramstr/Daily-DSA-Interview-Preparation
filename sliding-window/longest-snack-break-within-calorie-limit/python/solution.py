"""
Longest Snack Break Within Calorie Limit
=========================================

Problem Description:
You are tracking the snacks eaten by an employee during a workday. Each snack is
represented by its calorie count in an integer array `calories`, where `calories[i]`
is the calorie count of the i-th snack consumed in order.

An employee is allowed a continuous snack break, which is defined as a contiguous
subarray of snacks. The break is considered 'within budget' if the total calories
of all snacks in that break does not exceed a given limit `maxCalories`.

Return the maximum number of snacks the employee can eat in a single contiguous
snack break without exceeding the calorie limit.

If no single snack fits within the limit, return 0.

Constraints:
- 1 <= calories.length <= 10^5
- 1 <= calories[i] <= 1000
- 1 <= maxCalories <= 10^7
"""

from typing import List


class Solution:
    def longestSnackBreak(self, calories: List[int], maxCalories: int) -> int:
        """
        Find the maximum number of snacks in a contiguous subarray whose
        total calories do not exceed maxCalories.

        This uses the classic Sliding Window technique:
        - Maintain a window [left, right] that represents the current subarray.
        - Expand the window by moving 'right' forward.
        - Shrink the window from the left when the sum exceeds maxCalories.
        - Track the maximum window size seen while the sum is within budget.

        Args:
            calories (List[int]): List of calorie counts for each snack.
            maxCalories (int): The maximum total calories allowed in a break.

        Returns:
            int: The maximum number of snacks in a valid contiguous break.
                 Returns 0 if no single snack fits within the limit.

        Time Complexity:  O(n) — each element is added and removed from the
                          window at most once, so we do at most 2n operations.
        Space Complexity: O(1) — only a handful of integer variables are used;
                          no extra data structures proportional to input size.
        """

        # ------------------------------------------------------------------ #
        # STEP 1 — Initialise sliding-window bookkeeping variables
        # ------------------------------------------------------------------ #
        # 'left'        : the index of the leftmost snack in the current window.
        # 'current_sum' : the running total of calories inside the window.
        # 'max_snacks'  : the best (largest) valid window size found so far.
        left: int = 0
        current_sum: int = 0
        max_snacks: int = 0

        # ------------------------------------------------------------------ #
        # STEP 2 — Expand the window one snack at a time using 'right'
        # ------------------------------------------------------------------ #
        # We iterate over every index 'right' from 0 to len(calories)-1.
        # Think of 'right' as the index of the newest snack we are considering
        # adding to the current break.
        for right in range(len(calories)):

            # ---------------------------------------------------------------- #
            # STEP 2a — Include the snack at 'right' in the current window
            # ---------------------------------------------------------------- #
            # Add its calorie count to the running window sum.
            current_sum += calories[right]

            # ---------------------------------------------------------------- #
            # STEP 2b — Shrink the window from the left if the budget is blown
            # ---------------------------------------------------------------- #
            # While the total calories in the window exceed maxCalories, we
            # must remove snacks from the LEFT side of the window.
            #
            # Why a while loop (not just an if)?
            # In theory, a single removal is enough here because each calorie
            # value is >= 1, so removing one element always reduces the sum.
            # However, using 'while' makes the logic robust and mirrors the
            # general sliding-window pattern correctly.
            while current_sum > maxCalories:
                # Remove the leftmost snack's calories from the running sum.
                current_sum -= calories[left]
                # Slide the left boundary one position to the right.
                left += 1

            # ---------------------------------------------------------------- #
            # STEP 2c — The window [left..right] is now valid (sum <= maxCalories)
            # ---------------------------------------------------------------- #
            # Calculate the number of snacks in the current valid window.
            # Window size = right - left + 1  (both endpoints are inclusive).
            window_size: int = right - left + 1

            # Update the best answer if this window is larger than any seen so far.
            if window_size > max_snacks:
                max_snacks = window_size

        # ------------------------------------------------------------------ #
        # STEP 3 — Return the best answer
        # ------------------------------------------------------------------ #
        # If every individual snack exceeded maxCalories, the while-loop would
        # have shrunk the window to size 0 each time (left would equal right+1),
        # so max_snacks would remain 0 — exactly the required return value.
        return max_snacks


# --------------------------------------------------------------------------- #
# Verification trace for the provided examples
# --------------------------------------------------------------------------- #
# Example 1: calories = [100, 200, 150, 50, 300, 80], maxCalories = 400
#
#   right=0: sum=100, window=[100]          size=1, max=1
#   right=1: sum=300, window=[100,200]      size=2, max=2
#   right=2: sum=450 > 400 → remove 100    sum=350, window=[200,150]
#            size=2, max=2
#   right=3: sum=400, window=[200,150,50]   size=3, max=3  ✓
#   right=4: sum=700 > 400 → remove 200    sum=500
#            still > 400  → remove 150     sum=350
#            window=[50,300]               size=2, max=3
#   right=5: sum=430 > 400 → remove 50     sum=380
#            window=[300,80]               size=2, max=3
#   Result: 3  ✓
#
# Example 2: calories = [500, 600, 700], maxCalories = 400
#
#   right=0: sum=500 > 400 → remove 500    sum=0, left=1, size=0, max=0
#   right=1: sum=600 > 400 → remove 600    sum=0, left=2, size=0, max=0
#   right=2: sum=700 > 400 → remove 700    sum=0, left=3, size=0, max=0
#   Result: 0  ✓
# --------------------------------------------------------------------------- #


if __name__ == "__main__":
    solver = Solution()

    # ---------------------------------------------------------------------- #
    # Example 1 — Expected output: 3
    # ---------------------------------------------------------------------- #
    calories_1 = [100, 200, 150, 50, 300, 80]
    max_calories_1 = 400
    result_1 = solver.longestSnackBreak(calories_1, max_calories_1)
    print("Example 1")
    print(f"  Input  : calories={calories_1}, maxCalories={max_calories_1}")
    print(f"  Output : {result_1}")
    print(f"  Expected: 3")
    print(f"  Pass   : {result_1 == 3}")
    print()

    # ---------------------------------------------------------------------- #
    # Example 2 — Expected output: 0
    # ---------------------------------------------------------------------- #
    calories_2 = [500, 600, 700]
    max_calories_2 = 400
    result_2 = solver.longestSnackBreak(calories_2, max_calories_2)
    print("Example 2")
    print(f"  Input  : calories={calories_2}, maxCalories={max_calories_2}")
    print(f"  Output : {result_2}")
    print(f"  Expected: 0")
    print(f"  Pass   : {result_2 == 0}")
    print()

    # ---------------------------------------------------------------------- #
    # Additional edge-case tests
    # ---------------------------------------------------------------------- #

    # Single element within limit
    result_3 = solver.longestSnackBreak([300], 300)
    print(f"Single snack within limit  → {result_3}  (expected 1, pass={result_3 == 1})")

    # Single element exceeds limit
    result_4 = solver.longestSnackBreak([500], 300)
    print(f"Single snack exceeds limit → {result_4}  (expected 0, pass={result_4 == 0})")

    # All snacks fit in one window
    result_5 = solver.longestSnackBreak([10, 20, 30, 40], 200)
    print(f"All snacks fit             → {result_5}  (expected 4, pass={result_5 == 4})")

    # Large uniform array
    large_calories = [100] * 100_000
    result_6 = solver.longestSnackBreak(large_calories, 10_000_000)
    print(f"Large uniform array        → {result_6}  (expected 100000, pass={result_6 == 100_000})")