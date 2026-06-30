"""
Title: Count Consistent Alias Pairs

Problem Description:
A messaging platform stores user aliases as lowercase strings. Two aliases are considered
consistent if they use exactly the same set of distinct characters, regardless of order
or how many times each character appears.

For example:
- "abbca" and "cba" are consistent because both contain the character set {a, b, c}
- "aabc" and "abdd" are not consistent

Given an array aliases, return the number of index pairs (i, j) such that:
0 <= i < j < aliases.length
and aliases[i] is consistent with aliases[j].

The key idea is to convert each alias into a canonical hashed representation of its
distinct characters, then count how many previous aliases share the same representation.

Constraints:
- 1 <= aliases.length <= 100000
- 1 <= aliases[i].length <= 100
- aliases[i] consists only of lowercase English letters
"""

from typing import Dict, List


class Solution:
    def _alias_mask(self, alias: str) -> int:
        """
        Convert an alias into a bitmask representing its distinct characters.

        Each lowercase English letter maps to one bit position:
        - 'a' -> bit 0
        - 'b' -> bit 1
        - ...
        - 'z' -> bit 25

        If a character appears multiple times, setting the same bit again does not
        change the mask, which is exactly what we want because only distinct characters
        matter.

        Args:
            alias: A lowercase string alias.

        Returns:
            An integer bitmask encoding the set of distinct characters in the alias.

        Time complexity:
            O(len(alias))

        Space complexity:
            O(1)
        """
        # Start with an empty set of characters.
        # In bitmask form, this means all 26 bits are initially 0.
        mask: int = 0

        # Process every character in the alias.
        for ch in alias:
            # Convert the character into a zero-based index:
            # 'a' -> 0, 'b' -> 1, ..., 'z' -> 25
            bit_index: int = ord(ch) - ord("a")

            # Turn on the corresponding bit using bitwise OR.
            # Example:
            # if ch == 'c', then bit_index == 2, and (1 << 2) == 4 (binary 100)
            # OR-ing this into mask records that 'c' is present.
            mask |= 1 << bit_index

        # The final mask uniquely identifies the set of distinct letters.
        return mask

    def countConsistentAliasPairs(self, aliases: List[str]) -> int:
        """
        Count how many index pairs of aliases have exactly the same set of distinct characters.

        The algorithm scans aliases from left to right. For each alias:
        1. Convert it into a canonical representation (a 26-bit integer mask).
        2. Look up how many previous aliases already had the same mask.
        3. Add that count to the answer, because each previous matching alias forms
           one valid pair with the current alias.
        4. Record the current alias mask in a frequency dictionary.

        This avoids comparing every pair of strings directly, which would be too slow
        for large inputs.

        Args:
            aliases: A list of lowercase alias strings.

        Returns:
            The total number of consistent index pairs.

        Time complexity:
            O(n * m), where:
            - n is the number of aliases
            - m is the average alias length
            Since each alias is processed once and each character is visited once.

        Space complexity:
            O(k), where k is the number of distinct masks encountered.
            In the worst case, this is O(n).
        """
        # This dictionary stores how many times each character-set mask has appeared so far.
        #
        # Key:
        #   mask (int) -> canonical representation of a set of distinct characters
        #
        # Value:
        #   count (int) -> number of previous aliases with that exact mask
        #
        # Example:
        #   if frequency[7] == 3, that means we have already seen 3 aliases whose
        #   distinct character set corresponds to mask 7.
        frequency: Dict[int, int] = {}

        # This will accumulate the total number of valid pairs.
        total_pairs: int = 0

        # Iterate through aliases in order.
        # This ordering matters because for each current alias, we only want to count
        # pairs with earlier aliases, ensuring i < j automatically.
        for alias in aliases:
            # Step 1: Convert the current alias into its canonical bitmask.
            #
            # Why bitmask?
            # - It is compact.
            # - It is fast to build.
            # - Two aliases are consistent if and only if their masks are equal.
            mask: int = self._alias_mask(alias)

            # Step 2: Count how many previous aliases had the same mask.
            #
            # Every previous alias with this same mask forms one valid pair with
            # the current alias.
            #
            # Example:
            #   Suppose current alias mask is for {a,b,c}, and frequency says we have
            #   already seen 2 aliases with {a,b,c}. Then the current alias forms
            #   exactly 2 new pairs.
            previous_count: int = frequency.get(mask, 0)
            total_pairs += previous_count

            # Step 3: Record that we have now seen one more alias with this mask.
            frequency[mask] = previous_count + 1

        # After processing all aliases, total_pairs contains the answer.
        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    aliases1: List[str] = ["abbca", "cba", "aaaa", "a", "bac", "xy", "yx"]
    result1: int = solution.countConsistentAliasPairs(aliases1)
    print("Example 1 Result:", result1)  # Expected: 5

    # Example 2 from the problem statement
    aliases2: List[str] = ["abc", "de", "eed", "fff", "fed", "cab", "xyz"]
    result2: int = solution.countConsistentAliasPairs(aliases2)
    print("Example 2 Result:", result2)  # Expected: 2

    # Additional quick sanity checks
    aliases3: List[str] = ["a", "a", "a"]
    result3: int = solution.countConsistentAliasPairs(aliases3)
    print("Additional Test 1 Result:", result3)  # Expected: 3

    aliases4: List[str] = ["ab", "ba", "abc", "cba", "bca", "z"]
    result4: int = solution.countConsistentAliasPairs(aliases4)
    print("Additional Test 2 Result:", result4)  # Expected: 4