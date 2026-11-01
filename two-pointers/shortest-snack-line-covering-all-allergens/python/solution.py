"""
Title: Shortest Snack Line Covering All Allergens

Problem Description:
A food safety team is reviewing a cafeteria snack line. The line is represented by an
array `snacks`, where `snacks[i]` is the allergen label of the snack at position `i`.
Labels are strings such as "nuts", "dairy", or "soy".

You are also given an array `required`, containing the set of allergen labels that must
all appear at least once in a contiguous section of the line for inspection.

Your task is to return the length of the shortest contiguous subarray of `snacks` that
contains every allergen in `required` at least once. If no such subarray exists, return -1.

Important details:
- Duplicate labels may appear many times in `snacks`.
- `required` should be treated as a set of distinct labels, even if duplicates are present.
- The solution should be efficient for large inputs, which suggests a sliding window /
  two-pointers approach.

Examples:
1)
Input:
    snacks = ["dairy", "soy", "nuts", "gluten", "soy", "dairy"]
    required = ["soy", "nuts", "dairy"]
Output:
    3
Explanation:
    The subarray ["dairy", "soy", "nuts"] contains all required allergens.

2)
Input:
    snacks = ["fruit", "seed", "dairy", "fruit", "seed"]
    required = ["dairy", "soy"]
Output:
    -1
Explanation:
    No contiguous section contains both "dairy" and "soy".
"""

from typing import Dict, List, Set


class Solution:
    def shortest_snack_line(self, snacks: List[str], required: List[str]) -> int:
        """
        Return the length of the shortest contiguous subarray of `snacks`
        that contains every distinct allergen from `required` at least once.

        Args:
            snacks: Ordered list of allergen labels along the snack line.
            required: List of allergen labels that must all appear in the window.
                      Duplicates are ignored because the requirement is set-based.

        Returns:
            The minimum window length containing all required allergens, or -1
            if no such window exists.

        Time Complexity:
            O(n + m), where:
            - n = len(snacks)
            - m = len(required)
            Each pointer moves at most n times, and building the required set
            takes O(m).

        Space Complexity:
            O(k), where k is the number of distinct required allergens.
            We store:
            - the required set
            - a frequency map for required allergens currently inside the window
        """
        # Convert `required` into a set because the problem explicitly says
        # it should be treated as a set of distinct labels.
        #
        # Example:
        # required = ["soy", "soy", "nuts"] should behave exactly like
        # required = ["soy", "nuts"].
        required_set: Set[str] = set(required)

        # If for some reason the required set is empty, then technically the
        # shortest window covering "nothing" would be length 0.
        #
        # The given constraints imply required.length >= 1, but handling this
        # case makes the method more robust and complete.
        if not required_set:
            return 0

        # This dictionary tracks how many times each required allergen appears
        # inside the current sliding window [left, right].
        #
        # We only store counts for allergens that are actually in required_set.
        # Non-required snack labels are irrelevant for coverage, so we do not
        # waste memory tracking them.
        window_count: Dict[str, int] = {}

        # `formed` tells us how many distinct required allergens are currently
        # satisfied in the window.
        #
        # A required allergen is considered "formed" when its count in the
        # current window becomes at least 1.
        formed: int = 0

        # Total number of distinct required allergens we need to satisfy.
        need: int = len(required_set)

        # Standard sliding window left pointer.
        left: int = 0

        # Track the best (smallest) window length found so far.
        # Start with infinity so any valid window will be smaller.
        best_length: int = float("inf")

        # Expand the window by moving `right` from left to right across the array.
        for right, label in enumerate(snacks):
            # Only required allergens matter for satisfying the condition.
            # If the current snack label is not required, it can still be inside
            # the window, but it does not help us cover missing allergens.
            if label in required_set:
                # Increase the count of this required allergen in the window.
                window_count[label] = window_count.get(label, 0) + 1

                # If this is the first occurrence of this allergen in the window,
                # then we have newly satisfied one more required allergen.
                if window_count[label] == 1:
                    formed += 1

            # Once `formed == need`, the current window contains every required
            # allergen at least once.
            #
            # At this point, we try to shrink the window from the left to remove
            # unnecessary elements and make it as short as possible while still valid.
            while formed == need:
                # Current window is [left, right], inclusive.
                current_length: int = right - left + 1

                # Update the best answer if this valid window is smaller.
                if current_length < best_length:
                    best_length = current_length

                # We are about to move `left` forward, so inspect the element
                # that is leaving the window.
                left_label: str = snacks[left]

                # If the outgoing label is required, its count in the window
                # must be decreased.
                if left_label in required_set:
                    window_count[left_label] -= 1

                    # If its count becomes 0, then after removing it, the window
                    # no longer contains that required allergen.
                    #
                    # This means the window stops being valid, so `formed`
                    # decreases and the while-loop will end after we move left.
                    if window_count[left_label] == 0:
                        formed -= 1

                # Move the left boundary rightward to continue shrinking.
                left += 1

        # If best_length was never updated, then no valid window exists.
        if best_length == float("inf"):
            return -1

        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    snacks1: List[str] = ["dairy", "soy", "nuts", "gluten", "soy", "dairy"]
    required1: List[str] = ["soy", "nuts", "dairy"]
    result1: int = solution.shortest_snack_line(snacks1, required1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2
    snacks2: List[str] = ["fruit", "seed", "dairy", "fruit", "seed"]
    required2: List[str] = ["dairy", "soy"]
    result2: int = solution.shortest_snack_line(snacks2, required2)
    print("Example 2 Result:", result2)  # Expected: -1

    # Additional quick check: duplicates in required should be ignored
    snacks3: List[str] = ["soy", "dairy", "soy", "nuts"]
    required3: List[str] = ["soy", "soy", "nuts"]
    result3: int = solution.shortest_snack_line(snacks3, required3)
    print("Additional Example Result:", result3)  # Expected: 2 ("soy", "nuts")