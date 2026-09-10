"""
Title: Find the Shortest Unique Username Abbreviation

Problem Description:
You are given an array of distinct lowercase usernames and an integer index `p`
pointing to one target username.

A valid abbreviation of a username is formed by:
- keeping a non-empty prefix of the username, and
- replacing the remaining suffix with `*`.

Examples for "marina":
- "m*"
- "ma*"
- "mar*"
- "mari*"
- "marin*"

The full username without `*` is also allowed and is considered an abbreviation
of length equal to the whole word.

Your task is to return the shortest valid abbreviation of `usernames[p]` that
does not match the abbreviation of any other username in the array.

Two abbreviations match if their resulting strings are exactly equal.

If multiple shortest answers exist, return the lexicographically smallest one,
although under this abbreviation rule the answer is typically unique.

Constraints:
- 1 <= usernames.length <= 2 * 10^5
- 1 <= usernames[i].length <= 10^5
- Sum of all username lengths does not exceed 2 * 10^5
- All usernames contain only lowercase English letters
- All usernames are distinct
- 0 <= p < usernames.length
"""

from typing import Dict, List


class Solution:
    def shortest_unique_abbreviation(self, usernames: List[str], p: int) -> str:
        """
        Return the shortest abbreviation of usernames[p] that is unique among all usernames.

        The key observation is:
        - An abbreviation formed from a prefix of length k, written as prefix + "*",
          will match another username if and only if that other username also starts
          with the same prefix of length k and has length at least k + 1.
        - If another username has exactly length k and equals that prefix, then it does
          NOT produce prefix + "*", because its full-word abbreviation is just the word
          itself, without the star.

        Therefore, for each possible prefix length k of the target word (from 1 to len(word)-1),
        we only need to know how many usernames have:
        - the same prefix of length k, and
        - length at least k + 1.

        If that count is exactly 1, then only the target username can produce that
        abbreviation, so it is unique.

        If no starred abbreviation is unique, then the full username itself is always
        unique because all usernames are distinct.

        Args:
            usernames: List of distinct lowercase usernames.
            p: Index of the target username.

        Returns:
            The shortest unique valid abbreviation for usernames[p].

        Time complexity:
            O(total_length), where total_length is the sum of lengths of all usernames.

        Space complexity:
            O(total_length), for storing prefix frequency counts.
        """
        target: str = usernames[p]

        # This dictionary counts how many usernames can produce each starred abbreviation.
        #
        # Example:
        # If a username is "marina", then it contributes to:
        # "m*", "ma*", "mar*", "mari*", "marin*"
        #
        # It does NOT contribute the full word "marina" here, because full words are
        # handled separately and are automatically unique due to all usernames being distinct.
        #
        # Why this works:
        # We want to know whether a target abbreviation like "mar*" is shared by any
        # other username. If we count how many usernames generate "mar*", then:
        # - count == 1 means only the target generates it, so it is unique
        # - count > 1 means at least one other username also generates it
        star_count: Dict[str, int] = {}

        # Build frequency counts for every possible starred abbreviation from every username.
        #
        # Because the total sum of all username lengths is at most 2 * 10^5,
        # generating all prefixes across all words is efficient enough.
        for name in usernames:
            # A starred abbreviation must keep a non-empty prefix and replace a non-empty
            # remaining suffix with '*'. Therefore, the prefix length can be:
            # 1, 2, ..., len(name) - 1
            #
            # If len(name) == 1, this loop does not run, which is correct:
            # a one-letter word has no starred abbreviation, only the full word itself.
            for prefix_len in range(1, len(name)):
                abbr: str = name[:prefix_len] + "*"
                star_count[abbr] = star_count.get(abbr, 0) + 1

        # Now test the target's possible abbreviations from shortest to longest.
        #
        # This guarantees that the first unique one we find is the shortest.
        # Under this abbreviation format, there is at most one abbreviation for each
        # prefix length, so lexicographic tie-breaking is naturally satisfied.
        for prefix_len in range(1, len(target)):
            abbr = target[:prefix_len] + "*"

            # If exactly one username generates this abbreviation, it must be the target
            # itself, because we are iterating over abbreviations that the target can generate.
            if star_count.get(abbr, 0) == 1:
                return abbr

        # If no starred abbreviation is unique, the full username is always unique because
        # all usernames in the input are distinct.
        return target


if __name__ == "__main__":
    solution = Solution()

    usernames1 = ["marina", "mark", "mason", "mila"]
    p1 = 0
    result1 = solution.shortest_unique_abbreviation(usernames1, p1)
    print(result1)  # Expected: "mari*"

    usernames2 = ["zoe", "zora", "zack", "amy"]
    p2 = 3
    result2 = solution.shortest_unique_abbreviation(usernames2, p2)
    print(result2)  # Expected: "a*"

    # Additional quick checks
    usernames3 = ["a"]
    p3 = 0
    result3 = solution.shortest_unique_abbreviation(usernames3, p3)
    print(result3)  # Expected: "a"

    usernames4 = ["ab", "ac", "ad"]
    p4 = 1
    result4 = solution.shortest_unique_abbreviation(usernames4, p4)
    print(result4)  # Expected: "ac" because "a*" is shared by all three

    usernames5 = ["mar", "marina"]
    p5 = 0
    result5 = solution.shortest_unique_abbreviation(usernames5, p5)
    print(result5)  # Expected: "mar" since "m*" and "ma*" are shared

    usernames6 = ["mar", "marina"]
    p6 = 1
    result6 = solution.shortest_unique_abbreviation(usernames6, p6)
    print(result6)  # Expected: "mar*" because "mar" does not generate "mar*" but "marina" does