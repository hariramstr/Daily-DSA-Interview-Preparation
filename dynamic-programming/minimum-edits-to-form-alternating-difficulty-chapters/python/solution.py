"""
Title: Minimum Edits to Form Alternating Difficulty Chapters

Problem Description:
You are given a string `chapters` of length `n` representing the draft order of book chapters.
Each character is either 'E' (easy) or 'H' (hard).

The publisher wants the final book to be split into exactly `k` non-empty contiguous chapter groups.
After grouping, each group must be made uniform, meaning every chapter in that group must have the
same difficulty label.

In addition, the labels of adjacent groups must alternate:
if one group is all 'E', the next must be all 'H', then 'E' again, and so on.

You may edit any chapter by changing 'E' to 'H' or 'H' to 'E', and each such change costs 1.

Return the minimum total number of edits needed to transform the draft into exactly `k`
alternating uniform groups. If it is impossible, return -1.

Constraints:
- 1 <= n <= 2000
- 1 <= k <= n
- chapters[i] is either 'E' or 'H'
- Groups must be contiguous and non-empty
"""

from typing import List


class Solution:
    def _build_prefix_counts(self, chapters: str) -> List[int]:
        """
        Build prefix counts of how many 'E' characters appear up to each position.

        This allows us to quickly count how many 'E' characters exist in any substring,
        which then lets us compute the cost to convert that substring into all 'E' or all 'H'.

        Args:
            chapters: The original chapter difficulty string.

        Returns:
            A prefix array where prefix[i] is the number of 'E' characters in chapters[:i].

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(chapters)

        # prefix[i] = number of 'E' characters in chapters[0:i]
        # We use length n + 1 so that substring counts can be computed cleanly:
        # count_E_in(l..r) = prefix[r + 1] - prefix[l]
        prefix: List[int] = [0] * (n + 1)

        for i, ch in enumerate(chapters):
            prefix[i + 1] = prefix[i] + (1 if ch == "E" else 0)

        return prefix

    def _segment_cost(self, prefix_e: List[int], left: int, right: int, target: str) -> int:
        """
        Compute the number of edits needed to convert chapters[left:right+1]
        into a uniform segment of the given target character.

        Args:
            prefix_e: Prefix counts of 'E'.
            left: Left index of the segment, inclusive.
            right: Right index of the segment, inclusive.
            target: Either 'E' or 'H'.

        Returns:
            Minimum edits needed to make the whole segment equal to target.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        length: int = right - left + 1
        count_e: int = prefix_e[right + 1] - prefix_e[left]
        count_h: int = length - count_e

        if target == "E":
            # To make everything 'E', every existing 'H' must be changed.
            return count_h

        # To make everything 'H', every existing 'E' must be changed.
        return count_e

    def min_edits(self, chapters: str, k: int) -> int:
        """
        Compute the minimum number of edits required to split the string into exactly k
        non-empty contiguous groups such that:
        1) each group is uniform, and
        2) adjacent groups alternate between 'E' and 'H'.

        We use dynamic programming:
        - dp_e[i]: minimum cost to split the first i characters into the current number of groups,
          where the last group ends at position i - 1 and is labeled 'E'.
        - dp_h[i]: same idea, but the last group is labeled 'H'.

        Transition idea:
        Suppose we are building group number g and it ends at position i - 1.
        Let the last group start at position t, where g - 1 <= t < i.
        Then:
        - previous first t characters must already form g - 1 alternating groups
        - if current last group is 'E', previous last group must be 'H'
        - if current last group is 'H', previous last group must be 'E'

        So:
        dp_e_new[i] = min over t of dp_h_old[t] + cost(t, i - 1, 'E')
        dp_h_new[i] = min over t of dp_e_old[t] + cost(t, i - 1, 'H')

        A direct implementation would be O(k * n^2), which is too slow for n = 2000.
        We optimize using prefix minima.

        Observe:
        cost(t, i - 1, 'E') = number of 'H' in chapters[t:i]
                            = (i - t) - (#E in chapters[t:i])
                            = i - t - (prefix_e[i] - prefix_e[t])
                            = (i - prefix_e[i]) + (prefix_e[t] - t)

        Therefore:
        dp_e_new[i] = (i - prefix_e[i]) + min over t of (dp_h_old[t] + prefix_e[t] - t)

        Similarly:
        cost(t, i - 1, 'H') = #E in chapters[t:i]
                            = prefix_e[i] - prefix_e[t]
        So:
        dp_h_new[i] = prefix_e[i] + min over t of (dp_e_old[t] - prefix_e[t])

        This lets us process each group count in O(n), giving total O(k * n).

        Args:
            chapters: String of 'E' and 'H'.
            k: Exact number of required contiguous non-empty groups.

        Returns:
            The minimum edit cost, or -1 if impossible.

        Time complexity:
            O(k * n)

        Space complexity:
            O(n)
        """
        n: int = len(chapters)

        # If we need more groups than characters, it is impossible because every group
        # must be non-empty.
        if k > n or k <= 0:
            return -1

        # Prefix counts of 'E' let us compute segment conversion costs in O(1).
        prefix_e: List[int] = self._build_prefix_counts(chapters)

        # We use a large number to represent an unreachable state.
        inf: int = 10**15

        # Base case: exactly 1 group.
        # For the first i characters:
        # - if the single group is all 'E', cost is number of 'H' in chapters[0:i]
        # - if the single group is all 'H', cost is number of 'E' in chapters[0:i]
        #
        # dp_e[i] and dp_h[i] are only meaningful for i >= 1 here,
        # because 1 non-empty group cannot cover 0 characters.
        dp_e: List[int] = [inf] * (n + 1)
        dp_h: List[int] = [inf] * (n + 1)

        for i in range(1, n + 1):
            # Cost to convert chapters[0:i-1] into all 'E':
            # total length i minus number of 'E' gives number of 'H'
            dp_e[i] = i - prefix_e[i]

            # Cost to convert chapters[0:i-1] into all 'H':
            # number of 'E' characters must be changed
            dp_h[i] = prefix_e[i]

        # Build solutions for 2 groups, 3 groups, ..., k groups.
        for groups in range(2, k + 1):
            new_dp_e: List[int] = [inf] * (n + 1)
            new_dp_h: List[int] = [inf] * (n + 1)

            # We will sweep i from left to right.
            #
            # For a fixed number of groups, the last group starts at some t and ends at i - 1.
            # Since all groups are non-empty:
            # - first groups - 1 groups must occupy at least groups - 1 characters
            # - therefore t must be at least groups - 1
            # - and i must be at least groups
            #
            # We maintain:
            # best_for_e = min(dp_h[t] + prefix_e[t] - t) over valid t seen so far
            # best_for_h = min(dp_e[t] - prefix_e[t]) over valid t seen so far
            #
            # Then:
            # new_dp_e[i] = (i - prefix_e[i]) + best_for_e
            # new_dp_h[i] = prefix_e[i] + best_for_h
            best_for_e: int = inf
            best_for_h: int = inf

            # Before computing state for i, we add t = i - 1 as a possible start
            # for the last group. This ensures the last group is non-empty.
            for i in range(groups, n + 1):
                t: int = i - 1

                # Update the running minima with this newly available split point t.
                # If the new last group is 'E', then previous last group must be 'H'.
                if dp_h[t] < inf:
                    candidate_e: int = dp_h[t] + prefix_e[t] - t
                    if candidate_e < best_for_e:
                        best_for_e = candidate_e

                # If the new last group is 'H', then previous last group must be 'E'.
                if dp_e[t] < inf:
                    candidate_h: int = dp_e[t] - prefix_e[t]
                    if candidate_h < best_for_h:
                        best_for_h = candidate_h

                # Now compute the best cost for first i characters with exactly "groups" groups.
                #
                # Make the last group all 'E':
                # cost contribution from the last segment is encoded by (i - prefix_e[i]),
                # while the best previous split contribution is stored in best_for_e.
                if best_for_e < inf:
                    new_dp_e[i] = (i - prefix_e[i]) + best_for_e

                # Make the last group all 'H':
                # cost contribution from the last segment is encoded by prefix_e[i],
                # while the best previous split contribution is stored in best_for_h.
                if best_for_h < inf:
                    new_dp_h[i] = prefix_e[i] + best_for_h

            # Move to the next layer of DP.
            dp_e = new_dp_e
            dp_h = new_dp_h

        # Final answer: exactly k groups covering all n characters,
        # ending with either 'E' or 'H'.
        answer: int = min(dp_e[n], dp_h[n])

        return -1 if answer >= inf else answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt:
    # "EEHHE" with k = 3 can be split as "EE | HH | E"
    # This already alternates and each group is uniform, so answer should be 0.
    chapters_1: str = "EEHHE"
    k_1: int = 3
    result_1: int = solution.min_edits(chapters_1, k_1)
    print(f"chapters = {chapters_1}, k = {k_1} -> {result_1}")

    # About Example 2:
    # The prompt's written explanation appears inconsistent, because
    # "E | H | EEE | H" is already 4 alternating uniform groups with cost 0.
    # Therefore the mathematically correct minimum for this input is 0.
    chapters_2: str = "EHEEEH"
    k_2: int = 4
    result_2: int = solution.min_edits(chapters_2, k_2)
    print(f"chapters = {chapters_2}, k = {k_2} -> {result_2}")

    # Additional sample:
    # "EEE" with k = 2:
    # We need two non-empty alternating groups, so one possible target is "E | H"
    # or "H | E". Minimum edits is 1.
    chapters_3: str = "EEE"
    k_3: int = 2
    result_3: int = solution.min_edits(chapters_3, k_3)
    print(f"chapters = {chapters_3}, k = {k_3} -> {result_3}")