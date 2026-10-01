"""
Title: Longest DNA Read Window With Bounded GC Imbalance

Problem Description:
You are analyzing a long DNA read represented by a string `s` consisting only of the
characters 'A', 'C', 'G', and 'T'. In genome quality control, a contiguous segment is
considered stable if both of the following conditions hold:

1. The absolute difference between the number of 'G' bases and the number of 'C' bases
   in the segment is at most `k`.
2. The total number of 'A' and 'T' bases in the segment is at most `m`.

Return the length of the longest stable contiguous segment.

A segment may be empty, but the answer should be the maximum length among all contiguous
non-empty segments unless no valid non-empty segment exists, in which case return `0`.

Constraints:
- 1 <= s.length <= 2 * 10^5
- 0 <= k <= s.length
- 0 <= m <= s.length
- s[i] is one of 'A', 'C', 'G', 'T'

Examples:
1) s = "GCGATCGG", k = 1, m = 2
   Output: 6
   Explanation: "CGATCG" is valid:
   - G = 2, C = 2, so |G - C| = 0 <= 1
   - A + T = 2 <= 2
   Length = 6

2) s = "ATATGGCCG", k = 0, m = 1
   Output: 5
   Explanation: "TGGCC" is valid:
   - G = 2, C = 2, so |G - C| = 0 <= 0
   - A + T = 1 <= 1
   Length = 5
"""

from typing import Dict


class Solution:
    def longest_stable_segment(self, s: str, k: int, m: int) -> int:
        """
        Find the length of the longest contiguous DNA segment that satisfies:
        1) |count('G') - count('C')| <= k
        2) count('A') + count('T') <= m

        This uses a sliding window with two pointers. The key idea is:
        - Expand the right side of the window one character at a time.
        - If the window becomes invalid, move the left side forward until it becomes valid again.
        - Track the maximum valid window length seen.

        Args:
            s: DNA string containing only 'A', 'C', 'G', 'T'.
            k: Maximum allowed absolute difference between counts of 'G' and 'C'.
            m: Maximum allowed total count of 'A' and 'T'.

        Returns:
            The maximum length of a valid non-empty contiguous segment.
            Returns 0 if no valid non-empty segment exists.

        Time complexity:
            O(n), where n is the length of s.
            Each character is added to the window once and removed at most once.

        Space complexity:
            O(1), because we store only a fixed number of counters.
        """
        # We maintain a classic sliding window [left, right].
        # The window always represents the current substring we are considering.
        #
        # We need to know, for the current window:
        # - how many 'G' characters it contains
        # - how many 'C' characters it contains
        # - how many characters belong to {'A', 'T'}
        #
        # Since the alphabet is tiny and fixed, simple integer counters are the most
        # efficient and beginner-friendly choice. This gives O(1) updates per step.
        left: int = 0
        max_length: int = 0

        g_count: int = 0
        c_count: int = 0
        at_count: int = 0

        # We scan the string from left to right using `right`.
        # At each step, we include s[right] into the current window.
        for right, ch in enumerate(s):
            # Add the new character into the window counts.
            #
            # Why this works:
            # The window grows by exactly one character on the right,
            # so we only need to update the count affected by that character.
            if ch == "G":
                g_count += 1
            elif ch == "C":
                c_count += 1
            else:
                # The only remaining valid DNA characters are 'A' and 'T'.
                # Both contribute to the second constraint: total A+T <= m.
                at_count += 1

            # After adding the new character, the window may become invalid.
            #
            # A window is valid if BOTH conditions hold:
            # 1) abs(g_count - c_count) <= k
            # 2) at_count <= m
            #
            # If either condition fails, we must shrink the window from the left
            # until the window becomes valid again.
            #
            # Why shrinking from the left is correct:
            # In a sliding window approach, once a window is invalid, the only way
            # to potentially restore validity while keeping `right` fixed is to
            # remove characters from the left side.
            while abs(g_count - c_count) > k or at_count > m:
                left_ch: str = s[left]

                # Remove s[left] from the window counts because we are moving
                # the left boundary one step to the right.
                if left_ch == "G":
                    g_count -= 1
                elif left_ch == "C":
                    c_count -= 1
                else:
                    at_count -= 1

                left += 1

            # At this point, the current window [left, right] is guaranteed valid.
            # So we can safely use its length to update the answer.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # If no valid non-empty substring exists, max_length will remain 0.
        # Otherwise, it contains the best answer found.
        return max_length


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement.
    s1: str = "GCGATCGG"
    k1: int = 1
    m1: int = 2
    result1: int = solution.longest_stable_segment(s1, k1, m1)
    print(f"Input: s = {s1!r}, k = {k1}, m = {m1}")
    print(f"Output: {result1}")
    print("Expected: 6")
    print()

    # Example 2 from the problem statement.
    s2: str = "ATATGGCCG"
    k2: int = 0
    m2: int = 1
    result2: int = solution.longest_stable_segment(s2, k2, m2)
    print(f"Input: s = {s2!r}, k = {k2}, m = {m2}")
    print(f"Output: {result2}")
    print("Expected: 5")
    print()

    # Additional small sanity checks for beginner-friendly demonstration.

    # Entire string valid.
    s3: str = "GGCC"
    k3: int = 0
    m3: int = 0
    result3: int = solution.longest_stable_segment(s3, k3, m3)
    print(f"Input: s = {s3!r}, k = {k3}, m = {m3}")
    print(f"Output: {result3}")
    print("Expected: 4")
    print()

    # No non-empty valid substring because any single character 'A' or 'T'
    # would violate m = 0, and there are no G/C characters.
    s4: str = "ATAT"
    k4: int = 10
    m4: int = 0
    result4: int = solution.longest_stable_segment(s4, k4, m4)
    print(f"Input: s = {s4!r}, k = {k4}, m = {m4}")
    print(f"Output: {result4}")
    print("Expected: 0")