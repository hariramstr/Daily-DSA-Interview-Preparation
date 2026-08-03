"""
Title: Minimum Repaints to Form Three Color Districts

Problem Description:
A city boulevard is decorated with a row of buildings, represented by a string colors
of length n. Each character is one of 'R', 'G', or 'B', indicating the current paint
color of a building. The mayor wants the boulevard to be divided into exactly three
contiguous non-empty districts from left to right:
- the first district must be entirely red ('R')
- the second district entirely green ('G')
- the third district entirely blue ('B')

In one operation, you may repaint any single building to any of the three colors.
Return the minimum number of repaint operations required to transform the boulevard
into a valid arrangement of the form:

    R...R G...G B...B

where all three districts are contiguous and each district contains at least one building.

Constraints:
- 3 <= n <= 200000
- colors.length == n
- colors[i] is one of 'R', 'G', or 'B'

Examples:
1) colors = "RGRBB"
   Output: 1

2) colors = "BBRGRG"
   Output: 3

Goal:
Compute the minimum repaint cost efficiently for large inputs.
"""

from typing import List


class Solution:
    def minimum_repaints(self, colors: str) -> int:
        """
        Compute the minimum number of repaint operations needed to transform the
        string into three contiguous non-empty districts in the exact order:
        all 'R', then all 'G', then all 'B'.

        We use prefix counts so that we can quickly ask:
        "How many characters in a substring are already a desired color?"
        Then the repaint cost of a substring to one target color is simply:
            substring_length - count_of_target_color_in_that_substring

        We try every valid pair of split points implicitly in O(n) total time by
        fixing the end of the first district and the end of the second district.

        Args:
            colors: A string consisting only of 'R', 'G', and 'B'.

        Returns:
            The minimum repaint count required.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(colors)

        # Prefix count arrays:
        # prefix_r[i] = number of 'R' characters in colors[0:i]
        # prefix_g[i] = number of 'G' characters in colors[0:i]
        # prefix_b[i] = number of 'B' characters in colors[0:i]
        #
        # Important indexing detail:
        # - These arrays have length n + 1
        # - prefix_x[0] = 0 means "empty prefix"
        # - prefix_x[i] describes the first i characters, i.e. colors[0] through colors[i-1]
        #
        # This makes substring counting very clean:
        # count of color X in colors[left:right] = prefix_x[right] - prefix_x[left]
        prefix_r: List[int] = [0] * (n + 1)
        prefix_g: List[int] = [0] * (n + 1)
        prefix_b: List[int] = [0] * (n + 1)

        # Build prefix counts in one left-to-right pass.
        for i, ch in enumerate(colors, start=1):
            # First copy previous totals.
            prefix_r[i] = prefix_r[i - 1]
            prefix_g[i] = prefix_g[i - 1]
            prefix_b[i] = prefix_b[i - 1]

            # Then add the current character to the matching color count.
            if ch == 'R':
                prefix_r[i] += 1
            elif ch == 'G':
                prefix_g[i] += 1
            else:  # ch == 'B'
                prefix_b[i] += 1

        # Helper logic explanation:
        #
        # Suppose we split the string into:
        #   colors[0:i]     -> must all become 'R'
        #   colors[i:j]     -> must all become 'G'
        #   colors[j:n]     -> must all become 'B'
        #
        # Validity requires:
        #   1 <= i < j <= n - 1
        # because each district must be non-empty.
        #
        # Cost to repaint colors[0:i] into all 'R':
        #   length - number already 'R'
        #   = i - (prefix_r[i] - prefix_r[0])
        #   = i - prefix_r[i]
        #
        # Cost to repaint colors[i:j] into all 'G':
        #   (j - i) - (prefix_g[j] - prefix_g[i])
        #
        # Cost to repaint colors[j:n] into all 'B':
        #   (n - j) - (prefix_b[n] - prefix_b[j])
        #
        # Total:
        #   cost(i, j) = [i - prefix_r[i]]
        #              + [(j - i) - (prefix_g[j] - prefix_g[i])]
        #              + [(n - j) - (prefix_b[n] - prefix_b[j])]
        #
        # We can evaluate all valid pairs (i, j), but that would be O(n^2).
        # Instead, we rearrange:
        #
        #   cost(i, j)
        #   = (i - prefix_r[i]) + ((j - i) - (prefix_g[j] - prefix_g[i])) + blue_suffix_cost(j)
        #   = (i - prefix_r[i]) + (j - i - prefix_g[j] + prefix_g[i]) + blue_suffix_cost(j)
        #   = (-prefix_r[i] + prefix_g[i]) + (j - prefix_g[j]) + blue_suffix_cost(j)
        #
        # More directly and safely in code, we keep:
        #   red_cost(i) = cost to make [0:i] all R
        #   green_cost(i, j) = cost to make [i:j] all G
        #   blue_cost(j) = cost to make [j:n] all B
        #
        # Then for each j, we want the best i < j:
        #   min(red_cost(i) + green_cost(i, j)) + blue_cost(j)
        #
        # Expand the first part:
        #   red_cost(i) + green_cost(i, j)
        #   = [i - prefix_r[i]] + [(j - i) - (prefix_g[j] - prefix_g[i])]
        #   = [prefix_g[i] - prefix_r[i]] + [j - prefix_g[j]]
        #
        # For fixed j, the term [j - prefix_g[j]] is constant,
        # so we only need the minimum value of:
        #   prefix_g[i] - prefix_r[i]
        # over all valid i < j, with i >= 1.
        #
        # We scan j from left to right and maintain the best such value seen so far.

        # This will store the answer.
        min_operations: int = float('inf')

        # We need to maintain:
        #   best_left_value = min(prefix_g[i] - prefix_r[i]) for valid i values seen so far
        #
        # When processing a particular j:
        # - i must satisfy 1 <= i < j
        # - j itself must satisfy 2 <= j <= n - 1
        #
        # Therefore:
        # - before evaluating a given j, we should already have incorporated i = j - 1
        #   only if that still respects i < j. A simpler pattern is:
        #   initialize with i = 1 before loop starts,
        #   then for each j from 2 to n - 1:
        #       use current best over i in [1, j-1]
        #       then after that, next iteration can include i = j
        #
        # So we start with i = 1 available for j = 2.
        best_left_value: int = prefix_g[1] - prefix_r[1]

        # Iterate over all valid starts of the blue district.
        # j is the first index of the blue district, so j cannot be 0, 1, or n.
        # Specifically:
        # - first district [0:i] non-empty => i >= 1
        # - second district [i:j] non-empty => j >= i + 1 >= 2
        # - third district [j:n] non-empty => j <= n - 1
        for j in range(2, n):
            # Cost to repaint suffix colors[j:n] into all 'B'.
            #
            # Count of 'B' in suffix = prefix_b[n] - prefix_b[j]
            # Suffix length = n - j
            # Repaints needed = length - already_blue
            blue_cost: int = (n - j) - (prefix_b[n] - prefix_b[j])

            # Cost contribution for the middle district for fixed j,
            # after algebraic simplification:
            #   j - prefix_g[j]
            middle_constant: int = j - prefix_g[j]

            # Total best cost for this j:
            #   best over i<j of (prefix_g[i] - prefix_r[i]) + (j - prefix_g[j]) + blue_cost
            current_total: int = best_left_value + middle_constant + blue_cost

            # Update global minimum answer.
            if current_total < min_operations:
                min_operations = current_total

            # Prepare for the next j by allowing i = j as a future split point.
            #
            # For next iteration, valid i values become [1, j].
            # So we update best_left_value with the candidate from current j.
            candidate_left_value: int = prefix_g[j] - prefix_r[j]
            if candidate_left_value < best_left_value:
                best_left_value = candidate_left_value

        return min_operations

    def minimumRepaints(self, colors: str) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            colors: A string consisting only of 'R', 'G', and 'B'.

        Returns:
            The minimum repaint count required.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return self.minimum_repaints(colors)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[str] = [
        "RGRBB",
        "BBRGRG",
        "RGB",
        "RRRGGGBBB",
        "BBBBBGRRR",
    ]

    for s in sample_inputs:
        result = solution.minimum_repaints(s)
        print(f"colors = {s}, minimum repaints = {result}")