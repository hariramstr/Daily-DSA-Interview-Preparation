"""
Title: Find the Earliest Duplicate Custom Alias

Problem Description:
A messaging platform lets users define custom aliases for channels. Two aliases are
considered equivalent if, after normalizing them, they become identical.

Normalization rules:
1. Convert all uppercase letters to lowercase
2. Remove every hyphen '-' and underscore '_'
3. Keep all other characters unchanged

Given a list of aliases in the order they were created, return the index of the first
alias that is equivalent to any earlier alias after normalization. If no such alias
exists, return -1.

We must detect the earliest duplicate by creation time, meaning we scan from left to
right and return the first position i such that the normalized form of aliases[i] has
already appeared among aliases[0...i-1].

Constraints:
- 1 <= aliases.length <= 200000
- 1 <= aliases[i].length <= 100
- aliases[i] consists of English letters, digits, hyphens '-', underscores '_',
  and periods '.'
- The answer should be computed in O(total input size) expected time using hashing

Example 1:
Input: aliases = ["Team-Chat", "alerts", "team_chat", "team.chat"]
Output: 2

Explanation:
- "Team-Chat" -> "teamchat"
- "alerts" -> "alerts"
- "team_chat" -> "teamchat"
Since "teamchat" was already seen at index 0, index 2 is the first duplicate.

Example 2:
Input: aliases = ["build.v1", "build_v1", "BUILD-V2", "buildv2"]
Output: 3

Explanation:
- "build.v1" -> "build.v1"
- "build_v1" -> "buildv1"
- "BUILD-V2" -> "buildv2"
- "buildv2" -> "buildv2"
The first repeated normalized alias appears at index 3.
"""

from typing import List, Set


class Solution:
    def normalize_alias(self, alias: str) -> str:
        """
        Normalize a single alias according to the problem rules.

        The normalization process:
        - converts uppercase letters to lowercase
        - removes '-' and '_'
        - keeps all other characters unchanged

        Args:
            alias: The original alias string.

        Returns:
            The normalized alias string.

        Time complexity:
            O(len(alias))

        Space complexity:
            O(len(alias))
        """
        # We build the normalized string character by character.
        #
        # Why use a list of characters?
        # - In Python, repeatedly concatenating strings can be inefficient because
        #   strings are immutable.
        # - Appending characters to a list and joining once at the end is the
        #   standard efficient approach.
        normalized_chars: List[str] = []

        # Process each character exactly once.
        for ch in alias:
            # If the character is a hyphen or underscore, we skip it entirely
            # because the problem says these characters must be removed.
            if ch == "-" or ch == "_":
                continue

            # For every other character, we keep it, but letters must be treated
            # case-insensitively. Calling lower() handles uppercase-to-lowercase
            # conversion while leaving digits and periods unchanged.
            normalized_chars.append(ch.lower())

        # Join all kept characters into the final normalized string.
        return "".join(normalized_chars)

    def earliest_duplicate_alias(self, aliases: List[str]) -> int:
        """
        Return the index of the first alias whose normalized form has already appeared.

        The algorithm scans the list from left to right:
        - normalize the current alias
        - check whether that normalized form has been seen before
        - if yes, return the current index immediately
        - otherwise, record it and continue

        This guarantees we return the earliest duplicate by creation time.

        Args:
            aliases: List of aliases in creation order.

        Returns:
            The index of the first duplicate alias after normalization, or -1 if none exists.

        Time complexity:
            O(total input size) expected time, because each character is processed once
            during normalization and each hash set lookup/insert is O(1) expected.

        Space complexity:
            O(number of distinct normalized aliases * average normalized length)
        """
        # This set stores every normalized alias we have already encountered.
        #
        # Why a set?
        # - We only need to know whether a normalized alias has appeared before.
        # - A hash set gives expected O(1) membership checks and insertions.
        # - This is exactly what the problem asks for: an efficient hashing-based solution.
        seen: Set[str] = set()

        # We scan from left to right because the problem wants the earliest duplicate
        # by creation time. The first time we find a repeated normalized alias,
        # that index is the correct answer.
        for index, alias in enumerate(aliases):
            # Normalize the current alias using the required rules.
            normalized = self.normalize_alias(alias)

            # If this normalized form is already in the set, then some earlier alias
            # produced the same normalized result. That means the current alias is
            # the first duplicate encountered so far in left-to-right order.
            if normalized in seen:
                return index

            # Otherwise, this is the first time we have seen this normalized alias,
            # so we record it for future comparisons.
            seen.add(normalized)

        # If we finish the loop without finding any repeated normalized alias,
        # then no duplicate exists.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    aliases1 = ["Team-Chat", "alerts", "team_chat", "team.chat"]
    result1 = solution.earliest_duplicate_alias(aliases1)
    print(result1)  # Expected: 2

    # Example 2 from the problem statement
    aliases2 = ["build.v1", "build_v1", "BUILD-V2", "buildv2"]
    result2 = solution.earliest_duplicate_alias(aliases2)
    print(result2)  # Expected: 3

    # Additional sample with no duplicates
    aliases3 = ["alpha", "beta", "gamma.delta", "delta_gamma"]
    result3 = solution.earliest_duplicate_alias(aliases3)
    print(result3)  # Expected: -1