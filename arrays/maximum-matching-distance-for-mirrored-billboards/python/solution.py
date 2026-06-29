"""
Title: Maximum Matching Distance for Mirrored Billboards

Problem Description:
A city installs two parallel rows of digital billboards along the same highway.
The first row is represented by array top and the second row by array bottom,
where top[i] and bottom[i] are the ad category IDs shown at position i in each row.

Define the matching distance of a category x as the largest absolute difference
|i - j| such that top[i] = x and bottom[j] = x. In other words, you may pair one
occurrence of x from the top row with one occurrence of x from the bottom row,
and the score for that category is how far apart those positions are.

If a category appears in only one row, it contributes nothing.

Return the maximum matching distance over all categories.

Constraints:
- 1 <= top.length, bottom.length <= 200000
- top.length == bottom.length
- 1 <= top[i], bottom[i] <= 1000000000
- The answer fits in a 32-bit signed integer
"""

from typing import Dict, List


class Solution:
    def max_matching_distance(self, top: List[int], bottom: List[int]) -> int:
        """
        Compute the maximum matching distance between equal category IDs
        appearing in the top and bottom arrays.

        For each value x, we want:
            max(|i - j|) where top[i] == x and bottom[j] == x

        A key observation:
        For a fixed value x, the largest distance must be achieved by pairing
        one extreme index from top with one extreme index from bottom.
        Therefore, it is enough to know:
        - the smallest and largest index of x in top
        - the smallest and largest index of x in bottom

        Then the best distance for x is:
            max(
                abs(min_top[x] - max_bottom[x]),
                abs(max_top[x] - min_bottom[x])
            )

        Args:
            top: List of category IDs in the top row.
            bottom: List of category IDs in the bottom row.

        Returns:
            The maximum matching distance over all category IDs that appear
            in both arrays. Returns 0 if no category appears in both rows.

        Time complexity:
            O(n), where n is the length of the arrays.

        Space complexity:
            O(k), where k is the number of distinct category IDs.
        """
        # These dictionaries store the first and last positions where each value
        # appears in the top array.
        #
        # Why store both first and last?
        # Because the maximum absolute difference for a value must involve an
        # extreme position from one side and an extreme position from the other.
        top_first: Dict[int, int] = {}
        top_last: Dict[int, int] = {}

        # These dictionaries do the same for the bottom array.
        bottom_first: Dict[int, int] = {}
        bottom_last: Dict[int, int] = {}

        # Scan the top array once.
        #
        # For each value:
        # - If this is the first time we see it, record its first index.
        # - Always update its last index to the current position.
        #
        # This gives us the full leftmost/rightmost range of every value in top.
        for index, value in enumerate(top):
            if value not in top_first:
                top_first[value] = index
            top_last[value] = index

        # Scan the bottom array once with the same logic.
        #
        # After this loop:
        # - bottom_first[x] is the earliest index of x in bottom
        # - bottom_last[x] is the latest index of x in bottom
        for index, value in enumerate(bottom):
            if value not in bottom_first:
                bottom_first[value] = index
            bottom_last[value] = index

        # This will store the best answer found so far.
        answer: int = 0

        # We only need to consider values that appear in top.
        # For each such value, we check whether it also appears in bottom.
        #
        # If it does not appear in bottom, it contributes nothing and we skip it.
        for value in top_first:
            if value not in bottom_first:
                continue

            # For a fixed value, the maximum distance between any top occurrence
            # and any bottom occurrence is achieved by comparing opposite extremes:
            #
            # 1) earliest top with latest bottom
            # 2) latest top with earliest bottom
            #
            # We do not need to compare all pairs, which would be too slow.
            distance_using_top_left_and_bottom_right = abs(
                top_first[value] - bottom_last[value]
            )
            distance_using_top_right_and_bottom_left = abs(
                top_last[value] - bottom_first[value]
            )

            # The best distance for this value is the larger of the two.
            best_for_value = max(
                distance_using_top_left_and_bottom_right,
                distance_using_top_right_and_bottom_left,
            )

            # Update the global answer if this value gives a better result.
            if best_for_value > answer:
                answer = best_for_value

        # If no value appeared in both arrays, answer remains 0, which is correct.
        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    # top = [4, 7, 2, 7, 9]
    # bottom = [8, 7, 4, 2, 7]
    #
    # Correct analysis:
    # - Value 4: top index 0, bottom index 2 => distance 2
    # - Value 2: top index 2, bottom index 3 => distance 1
    # - Value 7: top indices 1, 3 and bottom indices 1, 4
    #            best is max(|1-4|, |3-1|) = max(3, 2) = 3
    # - Value 9 appears only in top
    # Therefore the correct answer is 3.
    top1 = [4, 7, 2, 7, 9]
    bottom1 = [8, 7, 4, 2, 7]
    result1 = solution.max_matching_distance(top1, bottom1)
    print("Example 1 result:", result1)  # Expected: 3

    # Example 2
    # top = [5, 1, 5, 3, 1, 6]
    # bottom = [1, 5, 2, 5, 7, 1]
    #
    # Correct analysis:
    # - Value 1: top indices 1, 4 and bottom indices 0, 5
    #            best is max(|1-5|, |4-0|) = max(4, 4) = 4
    # - Value 5: top indices 0, 2 and bottom indices 1, 3
    #            best is max(|0-3|, |2-1|) = max(3, 1) = 3
    # No other shared values improve the answer.
    # Therefore the correct answer is 4.
    top2 = [5, 1, 5, 3, 1, 6]
    bottom2 = [1, 5, 2, 5, 7, 1]
    result2 = solution.max_matching_distance(top2, bottom2)
    print("Example 2 result:", result2)  # Expected: 4

    # Additional quick check: no shared values
    top3 = [10, 20, 30]
    bottom3 = [40, 50, 60]
    result3 = solution.max_matching_distance(top3, bottom3)
    print("No shared values result:", result3)  # Expected: 0