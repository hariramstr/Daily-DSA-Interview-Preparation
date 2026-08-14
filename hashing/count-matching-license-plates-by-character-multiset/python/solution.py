"""
Title: Count Matching License Plates by Character Multiset

Problem Description:
A parking analytics system stores vehicle license plates as uppercase alphanumeric strings.
Two plates are considered matching if they contain exactly the same characters with the same
frequencies, regardless of order.

Examples:
- "A1B1" and "1AB1" match
- "AB12" and "B2A1" match
- "AAB1" and "AB11" do not match

Given an array plates, return the number of unordered pairs of indices (i, j) such that
i < j and plates[i] matches plates[j] by character multiset.

Constraints:
- 1 <= plates.length <= 100000
- 1 <= plates[i].length <= 20
- plates[i] consists only of characters 'A' to 'Z' and digits '0' to '9'
- The answer may be large, so use a 64-bit integer type where needed
"""

from typing import Dict, List, Tuple


class Solution:
    def _char_to_index(self, ch: str) -> int:
        """
        Convert a valid plate character into a fixed index in the frequency array.

        We map:
        - '0' to '9'  -> indices 0 to 9
        - 'A' to 'Z'  -> indices 10 to 35

        Args:
            ch: A single character that is guaranteed to be a digit or uppercase letter.

        Returns:
            The integer index representing that character in a 36-slot frequency array.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        if '0' <= ch <= '9':
            return ord(ch) - ord('0')
        return 10 + ord(ch) - ord('A')

    def _build_signature(self, plate: str) -> Tuple[int, ...]:
        """
        Build a canonical frequency-based signature for one license plate.

        The key idea is:
        if two plates contain exactly the same characters with the same counts,
        then their frequency arrays will be identical, even if the characters
        appear in different orders.

        For example:
        - "A1B1" -> same signature as "1AB1"
        - "XYZ"  -> same signature as "ZYX"

        We use a tuple because:
        - tuples are immutable
        - immutable objects can be used as dictionary keys
        - this lets us hash each plate by its character multiset

        Args:
            plate: The license plate string.

        Returns:
            A tuple of length 36 containing the frequency of each possible character.

        Time complexity:
            O(L), where L is the length of the plate

        Space complexity:
            O(1), because the frequency array always has fixed size 36
        """
        # Create a fixed-size frequency array:
        # 10 slots for digits + 26 slots for uppercase letters = 36 total.
        counts: List[int] = [0] * 36

        # Count each character in the current plate.
        # This ignores order completely and only records how many times each
        # character appears, which is exactly what we need for multiset matching.
        for ch in plate:
            index: int = self._char_to_index(ch)
            counts[index] += 1

        # Convert the list to a tuple so it can be used as a dictionary key.
        return tuple(counts)

    def count_matching_pairs(self, plates: List[str]) -> int:
        """
        Count how many unordered pairs of plates match by character multiset.

        Strategy:
        1. Convert each plate into a canonical signature based on character counts.
        2. Use a hash map to track how many times each signature has appeared before.
        3. When we see a signature again, every previous occurrence of that same
           signature forms a new valid pair with the current plate.

        Example:
        If a signature has already appeared 3 times, and we see it again now,
        then the current plate forms 3 new pairs.

        This is more efficient than comparing every pair of plates directly.

        Args:
            plates: A list of uppercase alphanumeric license plate strings.

        Returns:
            The total number of matching unordered pairs.

        Time complexity:
            O(N * L), where:
            - N is the number of plates
            - L is the maximum plate length
            Since each plate is processed once and each signature is built in O(L).

        Space complexity:
            O(N) in the worst case for the hash map of unique signatures.
        """
        # This dictionary maps:
        # signature -> how many times we have seen this exact signature so far
        #
        # Why a dictionary?
        # - We need fast lookup by signature
        # - Python dictionaries provide average O(1) insert and lookup
        seen_count: Dict[Tuple[int, ...], int] = {}

        # This will store the final answer.
        # Python integers automatically support large values, so it safely handles
        # results that would require 64-bit integers in other languages.
        total_pairs: int = 0

        # Process each plate one by one.
        for plate in plates:
            # Step 1: Convert the current plate into its canonical frequency signature.
            signature: Tuple[int, ...] = self._build_signature(plate)

            # Step 2: Check how many identical signatures we have already seen.
            #
            # If we have seen this signature k times before, then the current plate
            # forms exactly k new matching pairs:
            # - with the 1st previous identical plate
            # - with the 2nd previous identical plate
            # - ...
            # - with the kth previous identical plate
            previous_matches: int = seen_count.get(signature, 0)

            # Add those newly formed pairs to the running total.
            total_pairs += previous_matches

            # Step 3: Record that we have now seen one more plate with this signature.
            seen_count[signature] = previous_matches + 1

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # "A1B1" and "1AB1" match
    # "AB12" and "B2A1" match
    # "XYZ" and "ZYX" match
    # Total = 3
    plates1: List[str] = ["A1B1", "1AB1", "AB12", "B2A1", "XYZ", "ZYX"]
    result1: int = solution.count_matching_pairs(plates1)
    print("Example 1 result:", result1)  # Expected: 3

    # Example 2 from the prompt:
    # First group: ["AA11", "1A1A", "A11A"] -> 3 choose 2 = 3
    # Second group: ["BB2", "2BB", "B2B"]   -> 3 choose 2 = 3
    # "C3" matches none
    # Total = 6
    plates2: List[str] = ["AA11", "1A1A", "A11A", "BB2", "2BB", "B2B", "C3"]
    result2: int = solution.count_matching_pairs(plates2)
    print("Example 2 result:", result2)  # Expected: 6

    # Additional small sanity check:
    # All three are identical by multiset -> 3 pairs
    plates3: List[str] = ["AB12", "21BA", "B1A2"]
    result3: int = solution.count_matching_pairs(plates3)
    print("Additional test result:", result3)  # Expected: 3