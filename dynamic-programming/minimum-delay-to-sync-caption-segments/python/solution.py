"""
Title: Minimum Delay to Sync Caption Segments

Problem Description:
A video platform stores an automatically generated caption track as a string `s`
of lowercase English letters, where each character represents the dominant word
category spoken during one second of video. To improve readability, the platform
wants to split the caption track into contiguous segments. Each segment must have
length between `minLen` and `maxLen` inclusive.

For any chosen segment, its delay cost is defined as the number of character
changes needed to make the entire segment consist of only one repeated character.
For example, the segment `abaca` has delay cost `2`, because changing the two
non-`a` characters makes all characters equal. The total synchronization delay is
the sum of delay costs over all segments.

Your task is to return the minimum possible total synchronization delay needed to
partition the entire string into valid segments. If it is impossible to partition
the full string using only segment lengths in the allowed range, return `-1`.

Constraints:
- 1 <= s.length <= 5000
- s contains only lowercase English letters
- 1 <= minLen <= maxLen <= 100
"""

from typing import List


class Solution:
    def _build_prefix_counts(self, s: str) -> List[List[int]]:
        """
        Build prefix frequency counts for all 26 lowercase letters.

        The returned table lets us quickly count how many times each character
        appears in any substring s[l:r] (inclusive) in O(26) time.

        Args:
            s: Input lowercase string.

        Returns:
            A 2D list prefix where prefix[i + 1][c] stores how many times
            character c appears in s[0:i + 1].

        Time complexity:
            O(26 * n)

        Space complexity:
            O(26 * n)
        """
        n: int = len(s)

        # prefix[i] will describe the first i characters of the string.
        # That means:
        # - prefix[0] corresponds to the empty prefix
        # - prefix[1] corresponds to s[0]
        # - prefix[n] corresponds to the full string
        prefix: List[List[int]] = [[0] * 26 for _ in range(n + 1)]

        for i, ch in enumerate(s):
            # Copy all previous counts first.
            # This means prefix[i + 1] starts as an exact copy of prefix[i].
            for c in range(26):
                prefix[i + 1][c] = prefix[i][c]

            # Then add the current character.
            prefix[i + 1][ord(ch) - ord("a")] += 1

        return prefix

    def _segment_cost(self, prefix: List[List[int]], left: int, right: int) -> int:
        """
        Compute the minimum number of changes needed to make s[left:right + 1]
        consist of one repeated character.

        The best target character is simply the most frequent character already
        present in the segment. If the segment length is L and the maximum
        frequency inside it is M, then the minimum changes needed is L - M.

        Args:
            prefix: Prefix frequency table for the string.
            left: Left index of the segment, inclusive.
            right: Right index of the segment, inclusive.

        Returns:
            Minimum normalization cost for this segment.

        Time complexity:
            O(26)

        Space complexity:
            O(1) auxiliary
        """
        length: int = right - left + 1
        max_frequency: int = 0

        # For each lowercase letter, compute how many times it appears in the
        # segment using prefix sums:
        # count in [left, right] = prefix[right + 1] - prefix[left]
        for c in range(26):
            count: int = prefix[right + 1][c] - prefix[left][c]
            if count > max_frequency:
                max_frequency = count

        return length - max_frequency

    def minimum_delay(self, s: str, minLen: int, maxLen: int) -> int:
        """
        Return the minimum total synchronization delay to partition the string
        into contiguous segments whose lengths are all between minLen and maxLen.

        Dynamic Programming idea:
        - Let dp[i] be the minimum cost to partition the prefix s[0:i].
        - For each ending position i, try every valid segment length L.
        - If the previous prefix s[0:i-L] is partitionable, then:
              dp[i] = min(dp[i], dp[i-L] + cost of segment s[i-L:i-1])

        Because maxLen <= 100 and there are only 26 letters, this approach is
        efficient enough for n <= 5000.

        Args:
            s: Input lowercase string.
            minLen: Minimum allowed segment length.
            maxLen: Maximum allowed segment length.

        Returns:
            The minimum total delay, or -1 if no valid partition exists.

        Time complexity:
            O(n * (maxLen - minLen + 1) * 26)

        Space complexity:
            O(26 * n + n)
        """
        n: int = len(s)

        # Quick impossibility check:
        # If the string is shorter than the minimum allowed segment length,
        # there is no way to create even one valid segment.
        if n < minLen:
            return -1

        # Build prefix counts once so every segment cost can be computed quickly.
        prefix: List[List[int]] = self._build_prefix_counts(s)

        # We use a large number to represent "currently impossible".
        inf: int = 10**15

        # dp[i] = minimum cost to partition the first i characters, i.e. s[0:i]
        # Example:
        # - dp[0] = 0 because the empty prefix needs no cost
        # - dp[n] will be our final answer for the whole string
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # We process prefixes from length 1 to n.
        for i in range(1, n + 1):
            # Try every valid segment length that could end at position i - 1.
            # The segment would then start at i - length.
            for length in range(minLen, maxLen + 1):
                start: int = i - length

                # If start < 0, this segment would extend before the string starts,
                # so it is invalid.
                if start < 0:
                    continue

                # If dp[start] is still infinite, then the prefix before this segment
                # cannot be partitioned validly, so we skip this option.
                if dp[start] == inf:
                    continue

                # Compute the cost of normalizing the current segment
                # s[start:i], which corresponds to indices [start, i - 1].
                current_segment_cost: int = self._segment_cost(prefix, start, i - 1)

                # Candidate total cost = best cost for previous prefix
                #                      + cost of current segment
                candidate: int = dp[start] + current_segment_cost

                # Keep the minimum among all possible previous cuts.
                if candidate < dp[i]:
                    dp[i] = candidate

        # If dp[n] was never updated, the full string cannot be partitioned.
        return -1 if dp[n] == inf else dp[n]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    s1: str = "abacbc"
    min_len_1: int = 2
    max_len_1: int = 3
    result1: int = solution.minimum_delay(s1, min_len_1, max_len_1)
    print(f"Input: s={s1!r}, minLen={min_len_1}, maxLen={max_len_1}")
    print(f"Output: {result1}")
    print("Expected: 2")
    print()

    # Example 2
    # The string length is 8 and every segment must have length exactly 3.
    # Since 8 cannot be written as a sum of 3s, partitioning is impossible.
    s2: str = "aaabbbcc"
    min_len_2: int = 3
    max_len_2: int = 3
    result2: int = solution.minimum_delay(s2, min_len_2, max_len_2)
    print(f"Input: s={s2!r}, minLen={min_len_2}, maxLen={max_len_2}")
    print(f"Output: {result2}")
    print("Expected: -1")
    print()

    # Additional small sanity checks
    s3: str = "aaaa"
    min_len_3: int = 2
    max_len_3: int = 2
    result3: int = solution.minimum_delay(s3, min_len_3, max_len_3)
    print(f"Input: s={s3!r}, minLen={min_len_3}, maxLen={max_len_3}")
    print(f"Output: {result3}")
    print("Expected: 0")
    print()

    s4: str = "abcd"
    min_len_4: int = 1
    max_len_4: int = 2
    result4: int = solution.minimum_delay(s4, min_len_4, max_len_4)
    print(f"Input: s={s4!r}, minLen={min_len_4}, maxLen={max_len_4}")
    print(f"Output: {result4}")