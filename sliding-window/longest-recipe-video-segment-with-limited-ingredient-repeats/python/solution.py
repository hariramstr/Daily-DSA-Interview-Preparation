"""
Title: Longest Recipe Video Segment With Limited Ingredient Repeats

Problem Description:
You are given an array ingredients where ingredients[i] is the ingredient name
mentioned at second i of a cooking video. The editor wants to extract one
contiguous segment of the video such that no ingredient is mentioned more than
k times inside that segment. Your task is to return the length of the longest
valid segment.

A segment is valid if, for every distinct ingredient appearing in it, its
frequency within the segment is at most k. The segment must be contiguous, so
you may only choose a continuous block of timestamps.

This problem models a common interview pattern: maintaining counts inside a
moving window while expanding and shrinking the boundaries efficiently.

Return an integer representing the maximum number of timestamps in any valid
segment.

Constraints:
- 1 <= ingredients.length <= 200000
- 1 <= ingredients[i].length <= 20
- ingredients[i] consists of lowercase English letters
- 1 <= k <= ingredients.length
"""

from collections import defaultdict
from typing import DefaultDict, List


class Solution:
    def longest_valid_segment(self, ingredients: List[str], k: int) -> int:
        """
        Find the length of the longest contiguous segment where no ingredient
        appears more than k times.

        Args:
            ingredients: A list of ingredient names mentioned at each second.
            k: The maximum allowed frequency for any ingredient inside the window.

        Returns:
            The maximum length of a valid contiguous segment.

        Time Complexity:
            O(n), where n is the length of ingredients.
            Each element is added to the sliding window once and removed once.

        Space Complexity:
            O(m), where m is the number of distinct ingredients currently tracked
            in the frequency map. In the worst case, this can be O(n).
        """
        # This dictionary stores how many times each ingredient appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is ["salt", "pepper", "salt"],
        # then counts will be:
        # {
        #     "salt": 2,
        #     "pepper": 1
        # }
        counts: DefaultDict[str, int] = defaultdict(int)

        # left is the starting index of the current window.
        # We will expand the window by moving right forward,
        # and shrink the window by moving left forward when needed.
        left: int = 0

        # best stores the maximum valid window length found so far.
        best: int = 0

        # We iterate with right as the ending index of the current window.
        # At each step, we include ingredients[right] into the window.
        for right, ingredient in enumerate(ingredients):
            # Add the new ingredient at the right boundary into our frequency map.
            counts[ingredient] += 1

            # After adding this ingredient, the window may become invalid.
            # Importantly, only the count of the newly added ingredient could
            # have crossed the limit k, because all other counts were already valid
            # before this step.
            #
            # So while this specific ingredient appears too many times,
            # we move the left boundary to the right to remove elements
            # until the window becomes valid again.
            while counts[ingredient] > k:
                # Identify which ingredient is leaving the window.
                left_ingredient: str = ingredients[left]

                # Decrease its count because it is no longer inside the window.
                counts[left_ingredient] -= 1

                # Move the left boundary rightward by one position.
                left += 1

            # At this point, the current window [left, right] is guaranteed valid:
            # every ingredient appears at most k times.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger.
            if current_length > best:
                best = current_length

        # After checking all possible right boundaries, best contains
        # the length of the longest valid segment.
        return best


def run_example(ingredients: List[str], k: int) -> None:
    """
    Run one example test case and print the result.

    Args:
        ingredients: The list of ingredient names.
        k: Maximum allowed frequency for any ingredient.

    Returns:
        None

    Time Complexity:
        O(n), delegated to the solution method.

    Space Complexity:
        O(m), delegated to the solution method.
    """
    solution = Solution()
    result = solution.longest_valid_segment(ingredients, k)
    print(f"ingredients = {ingredients}")
    print(f"k = {k}")
    print(f"Longest valid segment length = {result}")
    print("-" * 60)


if __name__ == "__main__":
    # Example 1 from the problem statement.
    # ["salt","pepper","salt","oil","salt","pepper"], k = 2
    # The correct answer is 4.
    example_1_ingredients: List[str] = [
        "salt",
        "pepper",
        "salt",
        "oil",
        "salt",
        "pepper",
    ]
    example_1_k: int = 2

    # Example 2 from the problem statement.
    # ["egg","egg","milk","egg","milk","milk","flour"], k = 2
    # The correct answer is 5.
    example_2_ingredients: List[str] = [
        "egg",
        "egg",
        "milk",
        "egg",
        "milk",
        "milk",
        "flour",
    ]
    example_2_k: int = 2

    run_example(example_1_ingredients, example_1_k)
    run_example(example_2_ingredients, example_2_k)