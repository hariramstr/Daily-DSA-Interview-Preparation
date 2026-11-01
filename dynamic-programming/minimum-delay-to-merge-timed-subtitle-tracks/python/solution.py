"""
Title: Minimum Delay to Merge Timed Subtitle Tracks

Problem Description:
A streaming platform receives a spoken sentence split across two subtitle tracks, A and B.
Each track is a string of lowercase letters, where each character represents one subtitle
token in order. You must build a final merged subtitle by repeatedly taking the next unused
character from either A or B until both tracks are exhausted. The relative order of characters
taken from the same track must remain unchanged.

However, switching between tracks causes a synchronization delay. If two consecutive chosen
characters come from different tracks, you pay a delay cost of 1. In addition, the platform
dislikes repeated adjacent characters in the final merged subtitle because they look visually
noisy. For every pair of adjacent equal characters in the merged string, you also pay a noise
cost of 1.

Your task is to compute the minimum total cost (switch delays + repeated-adjacent noise
penalties) over all valid merges of A and B.

Formally, if the merged string is formed by interleaving A and B, then for each adjacent pair
in the merge:
- add 1 if the source track changes,
- add 1 if the two adjacent characters are equal.

Return the minimum possible total cost.

Constraints:
- 1 <= |A|, |B| <= 2000
- A and B consist only of lowercase English letters
- An O(|A| * |B|) dynamic programming solution is expected
"""

from typing import List


class Solution:
    def minimum_merge_cost(self, a: str, b: str) -> int:
        """
        Compute the minimum total cost over all valid interleavings of strings a and b.

        We use dynamic programming where the state remembers:
        - how many characters have already been used from a
        - how many characters have already been used from b
        - which track supplied the most recently appended character

        This is enough because the cost of adding the next character depends only on:
        - whether we switch tracks
        - whether the new character equals the previously appended character

        Args:
            a: First subtitle track.
            b: Second subtitle track.

        Returns:
            The minimum possible total cost.

        Time complexity:
            O(len(a) * len(b))

        Space complexity:
            O(len(b))
        """
        n: int = len(a)
        m: int = len(b)

        # We use a large number to represent "currently unreachable / not yet computed".
        inf: int = 10**18

        # dp_a[j]:
        #   After consuming some fixed number i of characters from a (handled by the outer loop)
        #   and exactly j characters from b, this stores the minimum cost among all valid merges
        #   whose LAST appended character came from a.
        #
        # dp_b[j]:
        #   Same idea, but the LAST appended character came from b.
        #
        # Important interpretation:
        #   During the outer loop iteration for a particular i, dp_a and dp_b represent states
        #   for exactly that i.
        #
        # Why only O(m) memory?
        #   Because transitions only need:
        #   - the previous row (i - 1), and
        #   - the current row while scanning j from left to right.
        dp_a: List[int] = [inf] * (m + 1)
        dp_b: List[int] = [inf] * (m + 1)

        # Base case:
        # If we take the very first character from a, there is no previous character,
        # so no adjacent-pair cost is created yet.
        #
        # State:
        #   consumed 1 char from a, 0 chars from b, last source = a
        if n >= 1:
            dp_a[0] = 0

        # Also initialize the states where we take only from b at the beginning.
        #
        # For j >= 1, if we build a prefix using only b, then every new character is adjacent
        # to the previous one and:
        #   - there is NO source switch because we stay on track b
        #   - there IS a +1 noise penalty whenever b[j-1] == b[j-2]
        #
        # So we can fill dp_b for the row i = 0.
        if m >= 1:
            dp_b[1] = 0
            for j in range(2, m + 1):
                dp_b[j] = dp_b[j - 1] + (1 if b[j - 1] == b[j - 2] else 0)

        # Special handling for i = 0 row is already done above.
        # Now we iterate i from 1 to n.
        #
        # For each i, we compute the row representing states after consuming exactly i chars from a.
        # We need:
        #   prev_a / prev_b = row for i - 1
        #   cur_a / cur_b   = row for i
        #
        # To make the code uniform, we start from the already prepared row for i = 1 if possible.
        prev_a: List[int] = dp_a[:]
        prev_b: List[int] = dp_b[:]

        # If n == 1, prev_* already corresponds to i = 1.
        # If n > 1, we will build rows i = 2..n.
        #
        # But before that, we must also correctly fill the rest of row i = 1 for j >= 1.
        # The initial prev_a[0] = 0 is known, and prev_b[*] for i = 0 is known.
        #
        # We now compute row i = 1 from left to right if n >= 1.
        if n >= 1:
            cur_a: List[int] = [inf] * (m + 1)
            cur_b: List[int] = [inf] * (m + 1)

            # State (1, 0, last=a) is the first character taken from a.
            cur_a[0] = 0

            # There is no valid state (1, 0, last=b), because if we consumed one char from a
            # and zero from b, the last character cannot come from b.
            cur_b[0] = inf

            # Fill j from 1 to m.
            for j in range(1, m + 1):
                # ------------------------------------------------------------
                # Compute cur_a[j]:
                # We want the minimum cost after consuming:
                #   - i = 1 chars from a
                #   - j chars from b
                # and the LAST appended character is a[0].
                #
                # Since the last character is from a, the immediately previous state must have
                # consumed one fewer character from a:
                #   state (0, j, last=a) -> impossible
                #   state (0, j, last=b) -> possible if j >= 1
                #
                # So only transition from prev_b[j] matters here because prev_* still represents
                # row i = 0 before we overwrite it.
                #
                # Added cost:
                #   +1 for switching source (b -> a)
                #   +1 if previous character equals current character
                #       previous char is b[j-1], current char is a[0]
                # ------------------------------------------------------------
                if prev_b[j] < inf:
                    cur_a[j] = min(
                        cur_a[j],
                        prev_b[j] + 1 + (1 if b[j - 1] == a[0] else 0),
                    )

                # ------------------------------------------------------------
                # Compute cur_b[j]:
                # We want the minimum cost after consuming:
                #   - 1 char from a
                #   - j chars from b
                # and the LAST appended character is b[j-1].
                #
                # The previous state must be at (1, j-1).
                #
                # Two possibilities:
                # 1) previous last was also from b:
                #       no switch cost
                #       add noise cost if b[j-2] == b[j-1]
                # 2) previous last was from a:
                #       switch cost +1
                #       add noise cost if a[0] == b[j-1]
                # ------------------------------------------------------------
                # From cur_b[j - 1] -> stay on b
                if j >= 2 and cur_b[j - 1] < inf:
                    cur_b[j] = min(
                        cur_b[j],
                        cur_b[j - 1] + (1 if b[j - 2] == b[j - 1] else 0),
                    )

                # From cur_a[j - 1] -> switch a -> b
                if cur_a[j - 1] < inf:
                    cur_b[j] = min(
                        cur_b[j],
                        cur_a[j - 1] + 1 + (1 if a[0] == b[j - 1] else 0),
                    )

            prev_a, prev_b = cur_a, cur_b

        # Now build rows i = 2..n.
        for i in range(2, n + 1):
            # New row arrays for exactly i consumed chars from a.
            cur_a = [inf] * (m + 1)
            cur_b = [inf] * (m + 1)

            # ------------------------------------------------------------
            # First fill column j = 0.
            # This means we consumed i chars from a and 0 chars from b.
            #
            # The only possible merged string is a[:i], so the last character must be from a.
            #
            # Transition from state (i-1, 0, last=a):
            #   no source switch
            #   add noise cost if a[i-2] == a[i-1]
            # ------------------------------------------------------------
            if prev_a[0] < inf:
                cur_a[0] = prev_a[0] + (1 if a[i - 2] == a[i - 1] else 0)

            # Impossible for last source to be b when j = 0.
            cur_b[0] = inf

            # ------------------------------------------------------------
            # Fill the rest of the row from left to right.
            # This left-to-right order is important because cur_b[j] depends on cur_a[j-1]
            # and cur_b[j-1], both of which are already known when processing j.
            # ------------------------------------------------------------
            for j in range(1, m + 1):
                # ============================================================
                # Compute cur_a[j]
                # ============================================================
                # We append a[i-1] as the last character.
                #
                # Previous state must be at (i-1, j):
                #   1) last was from a  -> no switch
                #   2) last was from b  -> switch cost +1
                #
                # In both cases, we may also add +1 if the previous character equals a[i-1].
                #
                # If previous last was from a, previous character is a[i-2].
                # If previous last was from b, previous character is b[j-1].
                # ============================================================
                if prev_a[j] < inf:
                    cur_a[j] = min(
                        cur_a[j],
                        prev_a[j] + (1 if a[i - 2] == a[i - 1] else 0),
                    )

                if prev_b[j] < inf:
                    cur_a[j] = min(
                        cur_a[j],
                        prev_b[j] + 1 + (1 if b[j - 1] == a[i - 1] else 0),
                    )

                # ============================================================
                # Compute cur_b[j]
                # ============================================================
                # We append b[j-1] as the last character.
                #
                # Previous state must be at (i, j-1):
                #   1) last was from b  -> no switch
                #   2) last was from a  -> switch cost +1
                #
                # If previous last was from b, previous character is b[j-2].
                # If previous last was from a, previous character is a[i-1].
                # ============================================================
                if j >= 2 and cur_b[j - 1] < inf:
                    cur_b[j] = min(
                        cur_b[j],
                        cur_b[j - 1] + (1 if b[j - 2] == b[j - 1] else 0),
                    )

                if cur_a[j - 1] < inf:
                    cur_b[j] = min(
                        cur_b[j],
                        cur_a[j - 1] + 1 + (1 if a[i - 1] == b[j - 1] else 0),
                    )

            # Move current row into previous row variables for the next iteration.
            prev_a, prev_b = cur_a, cur_b

        # Handle edge cases where one string length is 1 and the loops above may not have
        # produced the final row in a uniform way.
        #
        # At this point:
        # - if n >= 1, prev_a / prev_b represent row i = n
        # - if n == 0 this problem does not allow it, so no need to handle
        #
        # The final answer can end with either source track.
        return min(prev_a[m], prev_b[m])


if __name__ == "__main__":
    solution = Solution()

    # Sample 1
    a1: str = "ab"
    b1: str = "ac"
    result1: int = solution.minimum_merge_cost(a1, b1)
    print(f'A = "{a1}", B = "{b1}" -> {result1}')

    # Sample 2
    a2: str = "aaa"
    b2: str = "aa"
    result2: int = solution.minimum_merge_cost(a2, b2)
    print(f'A = "{a2}", B = "{b2}" -> {result2}')