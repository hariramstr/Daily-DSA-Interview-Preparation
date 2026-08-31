"""
Title: Longest Ad Rotation With Brand Separation

Problem Description:
You are given an array brands where brands[i] is the brand ID of the i-th advertisement
shown in a video stream, in chronological order.

A stream segment is considered valid if, for every brand that appears in that segment,
any two consecutive ads from the same brand inside the segment are at least gap + 1
positions apart.

Equivalently:
- If the same brand appears at positions i and j inside the chosen contiguous segment,
  and these are consecutive occurrences of that brand within the segment,
  then j - i - 1 >= gap
- This is the same as saying j - i > gap

Your task is to return the length of the longest valid contiguous segment.

A segment of length 1 is always valid. If gap = 0, then every segment is valid because
repeated brands may be adjacent.

Constraints:
- 1 <= brands.length <= 200000
- 1 <= brands[i] <= 1000000000
- 0 <= gap <= brands.length

Examples:
1) brands = [4, 1, 2, 4, 3, 1, 5], gap = 2
   Output: 7

2) brands = [7, 2, 7, 3, 4, 7], gap = 2
   Output: 4
"""

from typing import Dict, List


class Solution:
    def longest_valid_segment(self, brands: List[int], gap: int) -> int:
        """
        Return the length of the longest contiguous segment where repeated brands
        are separated by at least `gap` other ads.

        The method uses a sliding window:
        - Expand the right end one ad at a time.
        - Track the most recent index of each brand.
        - If the current brand was seen too recently inside the current window,
          move the left boundary just past that previous occurrence.

        Args:
            brands: List of brand IDs in chronological order.
            gap: Minimum number of ads that must exist between consecutive
                 occurrences of the same brand inside a valid segment.

        Returns:
            The maximum length of any valid contiguous segment.

        Time complexity:
            O(n), where n is the length of brands.
            Each index is processed once, and window left only moves forward.

        Space complexity:
            O(k), where k is the number of distinct brands stored in the hash map.
            In the worst case, O(n).
        """
        # This dictionary stores the most recent position where each brand appeared.
        #
        # Why do we need it?
        # When we are currently examining brands[right], we need to know whether
        # this same brand appeared earlier in the current window, and if so,
        # whether that earlier appearance is too close.
        #
        # Example:
        # brands = [7, 2, 7], gap = 2
        # At right = 2, the previous 7 was at index 0.
        # Distance between positions is 2, so there is only 1 ad between them.
        # Since we need at least 2 ads between them, this is invalid.
        last_seen: Dict[int, int] = {}

        # `left` is the start index of the current sliding window.
        #
        # The window [left, right] will always be maintained as valid after
        # we process each position `right`.
        left: int = 0

        # `best` stores the maximum valid window length found so far.
        best: int = 0

        # Iterate through the array, treating each position as the right end
        # of the current window.
        for right, brand in enumerate(brands):
            # Check whether this brand has appeared before.
            if brand in last_seen:
                previous_index: int = last_seen[brand]

                # We only care if the previous occurrence is inside the current window.
                #
                # If previous_index < left, then that earlier occurrence is already
                # outside the current segment, so it cannot violate the rule.
                if previous_index >= left:
                    # The rule says there must be at least `gap` ads between
                    # consecutive occurrences of the same brand.
                    #
                    # If the previous occurrence is at `previous_index` and the
                    # current one is at `right`, then the number of ads between them is:
                    #     right - previous_index - 1
                    #
                    # This must be >= gap.
                    #
                    # Equivalent condition for violation:
                    #     right - previous_index - 1 < gap
                    # which is the same as:
                    #     right - previous_index <= gap
                    #
                    # If violated, we must move `left` so that the previous occurrence
                    # is excluded from the window. The smallest valid new left is:
                    #     previous_index + 1
                    #
                    # Why is that enough?
                    # Because the only new conflict introduced by adding brands[right]
                    # is with the previous occurrence of the same brand. Once that
                    # earlier occurrence is removed, the window becomes valid again.
                    if right - previous_index <= gap:
                        left = previous_index + 1

            # Update the most recent position of this brand to the current index.
            #
            # This must happen after conflict handling logic, because we need the
            # old previous index first to decide whether the current window is valid.
            last_seen[brand] = right

            # Compute the current valid window length.
            current_length: int = right - left + 1

            # Update the best answer if this window is the largest valid one so far.
            if current_length > best:
                best = current_length

        return best

    def longestAdRotation(self, brands: List[int], gap: int) -> int:
        """
        Convenience wrapper matching an alternative method name.

        Args:
            brands: List of brand IDs in chronological order.
            gap: Minimum number of ads required between repeated brands.

        Returns:
            The length of the longest valid contiguous segment.

        Time complexity:
            O(n)

        Space complexity:
            O(n) in the worst case
        """
        return self.longest_valid_segment(brands, gap)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    brands1: List[int] = [4, 1, 2, 4, 3, 1, 5]
    gap1: int = 2
    result1: int = solution.longest_valid_segment(brands1, gap1)
    print("Example 1:")
    print("brands =", brands1)
    print("gap =", gap1)
    print("Longest valid segment length =", result1)
    print()

    # Example 2
    brands2: List[int] = [7, 2, 7, 3, 4, 7]
    gap2: int = 2
    result2: int = solution.longest_valid_segment(brands2, gap2)
    print("Example 2:")
    print("brands =", brands2)
    print("gap =", gap2)
    print("Longest valid segment length =", result2)
    print()

    # Additional quick checks
    brands3: List[int] = [1]
    gap3: int = 5
    result3: int = solution.longest_valid_segment(brands3, gap3)
    print("Additional Check 1:")
    print("brands =", brands3)
    print("gap =", gap3)
    print("Longest valid segment length =", result3)
    print()

    brands4: List[int] = [1, 1, 1, 1]
    gap4: int = 0
    result4: int = solution.longest_valid_segment(brands4, gap4)
    print("Additional Check 2:")
    print("brands =", brands4)
    print("gap =", gap4)
    print("Longest valid segment length =", result4)
    print()

    brands5: List[int] = [1, 2, 1, 2, 1]
    gap5: int = 1
    result5: int = solution.longest_valid_segment(brands5, gap5)
    print("Additional Check 3:")
    print("brands =", brands5)
    print("gap =", gap5)
    print("Longest valid segment length =", result5)