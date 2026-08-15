"""
Title: Count Mirror-Shifted Coupon Codes

Problem Description:
An e-commerce platform stores promotional coupon codes as strings of lowercase English letters.
Two coupon codes are considered mirror-shifted if one can be transformed into the other by
applying the same cyclic alphabet shift to every character.

For example:
- Shifting each character in "abc" by 2 produces "cde"
- Shifting each character in "xyz" by 3 produces "abc" because the alphabet wraps around

Therefore, "abc", "bcd", and "xyz" all belong to the same mirror-shifted group.
However, codes of different lengths can never belong to the same group.

You are given an array codes where each element is a non-empty string.
Count how many unordered pairs of indices (i, j) with i < j belong to the same
mirror-shifted group.

A common way to solve this is to normalize each string into a canonical signature
based on the differences between consecutive characters modulo 26, then use a hash map
to count how many times each signature appears.

Return the total number of valid pairs.

Constraints:
- 1 <= codes.length <= 100000
- 1 <= codes[i].length <= 100000
- codes[i] contains only lowercase English letters
- The sum of all string lengths does not exceed 200000

Example 1:
Input: codes = ["abc", "bcd", "ace", "xyz", "az", "ba", "a", "z"]
Output: 5

Explanation:
The groups are:
- ["abc", "bcd", "xyz"] -> 3 pairs
- ["az", "ba"] -> 1 pair
- ["a", "z"] -> 1 pair
Total = 5.

Example 2:
Input: codes = ["aa", "bb", "ab", "za", "yx"]
Output: 4

Explanation:
- ["aa", "bb"] share the same signature, contributing 1 pair.
- ["ab", "za", "yx"] share the same signature, contributing 3 pairs.
Total = 4.
"""

from typing import Dict, List, Tuple


class Solution:
    def _signature(self, code: str) -> Tuple[int, ...]:
        """
        Build a canonical signature for a code using consecutive character differences modulo 26.

        The key idea:
        If two strings can be transformed into each other by shifting every character by the same
        amount, then the differences between neighboring characters stay exactly the same.

        Examples:
        - "abc" -> differences: (1, 1)
        - "bcd" -> differences: (1, 1)
        - "xyz" -> differences: (1, 1) because:
            y - x = 1
            z - y = 1

        Single-character strings all become the same empty signature, which is correct because any
        one-letter string can be shifted into any other one-letter string.

        Args:
            code: A lowercase English string.

        Returns:
            A tuple of integers representing modulo-26 differences between consecutive characters.

        Time complexity:
            O(len(code))

        Space complexity:
            O(len(code))
        """
        # We store the pattern as a tuple because:
        # 1. Tuples are immutable, so they are safe dictionary keys.
        # 2. The tuple fully captures the "shape" of the string under cyclic shifts.
        # 3. Strings with the same tuple belong to the same mirror-shifted group.
        diffs: List[int] = []

        # We compare each character with the one immediately before it.
        # The modulo 26 is essential because the alphabet wraps around:
        # for example, from 'z' to 'a' the difference should be 1, not negative 25.
        for i in range(1, len(code)):
            prev_val = ord(code[i - 1]) - ord("a")
            curr_val = ord(code[i]) - ord("a")
            diff = (curr_val - prev_val) % 26
            diffs.append(diff)

        return tuple(diffs)

    def count_mirror_shifted_pairs(self, codes: List[str]) -> int:
        """
        Count unordered pairs of indices whose strings belong to the same mirror-shifted group.

        Strategy:
        1. Convert every string into a canonical signature.
        2. Count how many times each signature appears using a hash map.
        3. For each signature with frequency f, add f * (f - 1) // 2 pairs.

        Why this works:
        - Strings in the same shift-group have identical consecutive-difference signatures.
        - Strings in different groups will have different signatures.
        - Once grouped by signature, counting valid unordered pairs is a standard combination count.

        Args:
            codes: List of non-empty lowercase strings.

        Returns:
            The total number of unordered valid pairs.

        Time complexity:
            O(total length of all strings)

        Space complexity:
            O(total number of distinct signatures + total signature storage)
        """
        # This dictionary maps:
        # signature -> how many codes have this signature
        #
        # We use a dictionary (hash map) because it gives average O(1) insertion and lookup,
        # which is exactly what we want for near-linear overall performance.
        signature_count: Dict[Tuple[int, ...], int] = {}

        # Process each code exactly once.
        for code in codes:
            # Convert the current code into its canonical form.
            sig = self._signature(code)

            # Increase the frequency of this signature.
            # If the signature has not been seen before, start its count at 0.
            signature_count[sig] = signature_count.get(sig, 0) + 1

        # Now compute the number of unordered pairs inside each group.
        #
        # If a group has frequency f, then the number of ways to choose 2 indices is:
        #   C(f, 2) = f * (f - 1) // 2
        #
        # We sum this over all groups.
        total_pairs = 0
        for freq in signature_count.values():
            total_pairs += freq * (freq - 1) // 2

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement.
    codes1 = ["abc", "bcd", "ace", "xyz", "az", "ba", "a", "z"]
    result1 = solution.count_mirror_shifted_pairs(codes1)
    print("Example 1 result:", result1)  # Expected: 5

    # Example 2 from the problem statement.
    codes2 = ["aa", "bb", "ab", "za", "yx"]
    result2 = solution.count_mirror_shifted_pairs(codes2)
    print("Example 2 result:", result2)  # Expected: 4

    # Additional small sanity checks for beginners:
    # All single-character strings belong to the same group.
    codes3 = ["a", "b", "z"]
    result3 = solution.count_mirror_shifted_pairs(codes3)
    print("Single-character test result:", result3)  # Expected: 3

    # No matching groups.
    codes4 = ["ab", "ac", "ad"]
    result4 = solution.count_mirror_shifted_pairs(codes4)
    print("No-match test result:", result4)  # Expected: 0