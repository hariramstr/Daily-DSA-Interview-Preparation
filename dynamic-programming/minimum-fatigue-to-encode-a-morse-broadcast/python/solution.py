"""
Title: Minimum Fatigue to Encode a Morse Broadcast

Problem Description:
A rescue team needs to send a long emergency message using a custom telegraph key.
The message is represented as a string `s` consisting only of lowercase English letters.
Each letter must be encoded using standard Morse code.

Pressing the key has a fatigue cost:
- If two consecutive Morse symbols in the final transmitted stream are the same,
  the second symbol costs `sameCost`.
- Otherwise, the second symbol costs `switchCost`.
- The very first symbol of the entire transmission always costs `startCost`.

Before transmitting, you may partition the original string into any number of non-empty
contiguous groups. For each group, you are allowed to reverse the order of letters inside
that group exactly once or leave it unchanged. After choosing orientations for all groups,
concatenate the groups in their original group order and transmit the resulting Morse stream.

Task:
Compute the minimum possible total fatigue.

Constraints:
- 1 <= s.length <= 300
- 1 <= startCost, sameCost, switchCost <= 10^6
- s contains only lowercase English letters
- Standard Morse code mapping for the 26 lowercase English letters must be used
"""

from typing import List, Tuple


class Solution:
    MORSE: List[str] = [
        ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---",
        "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-",
        "..-", "...-", ".--", "-..-", "-.--", "--.."
    ]

    def _char_index(self, ch: str) -> int:
        """
        Convert a lowercase letter into its 0-based alphabet index.

        Args:
            ch: A lowercase English letter.

        Returns:
            Integer index in range [0, 25].

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        return ord(ch) - ord("a")

    def _transition_cost(self, prev_symbol: int, next_symbol: int, same_cost: int, switch_cost: int) -> int:
        """
        Compute the fatigue cost of appending one Morse symbol after another.

        Symbols are encoded as:
        - 0 for '.'
        - 1 for '-'

        Args:
            prev_symbol: Previous Morse symbol.
            next_symbol: Next Morse symbol.
            same_cost: Cost when symbols are equal.
            switch_cost: Cost when symbols differ.

        Returns:
            Fatigue cost for the transition.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        return same_cost if prev_symbol == next_symbol else switch_cost

    def minimum_fatigue(self, s: str, start_cost: int, same_cost: int, switch_cost: int) -> int:
        """
        Compute the minimum total fatigue after partitioning the string into contiguous groups
        and optionally reversing each group.

        Core idea:
        1. Precompute Morse information for every single letter:
           - first Morse symbol
           - last Morse symbol
           - internal Morse cost of that letter alone, excluding the very first symbol cost
        2. Build substring information for every s[i:j+1]:
           - forward orientation: letters i..j
           - reversed orientation: letters j..i
           For each orientation we store:
           - first Morse symbol of the whole substring
           - last Morse symbol of the whole substring
           - internal cost of the whole Morse stream excluding the first symbol
        3. Dynamic programming over prefixes:
           dp_end[k][last_symbol] = minimum cost to encode some valid transformed version
           of s[0:k], where the final Morse symbol is last_symbol.
        4. Transition by choosing the last group [i..j], either forward or reversed.

        Important observation:
        The total cost of a Morse stream can be decomposed into:
        - cost of the first symbol of the entire stream = start_cost
        - plus one transition cost for every later symbol
        Therefore, for any segment/orientation, if we know:
        - its first symbol
        - its last symbol
        - its internal transition cost excluding the first symbol
        then we can join it to a previous segment using exactly one boundary transition.

        Args:
            s: Original lowercase string.
            start_cost: Cost of the first Morse symbol of the entire transmission.
            same_cost: Cost when consecutive Morse symbols are the same.
            switch_cost: Cost when consecutive Morse symbols differ.

        Returns:
            Minimum possible total fatigue.

        Time complexity:
            O(n^2), where n = len(s)

        Space complexity:
            O(n^2)
        """
        n: int = len(s)

        # ------------------------------------------------------------
        # Step 1: Precompute Morse information for each individual letter.
        #
        # For each letter we store:
        # - first_sym[c]: first Morse symbol of the letter (0='.', 1='-')
        # - last_sym[c]:  last Morse symbol of the letter
        # - inner_cost[c]: cost to transmit the entire letter EXCEPT the first symbol
        #
        # Example:
        #   letter 'b' = "-..."
        #   first symbol is '-'
        #   remaining transitions are:
        #       '-' -> '.'
        #       '.' -> '.'
        #       '.' -> '.'
        #   So inner_cost['b'] = switch/same/same accordingly
        #
        # This is useful because any substring is just a sequence of letters, and we can
        # combine letter blocks by adding one boundary transition between them.
        # ------------------------------------------------------------
        first_sym: List[int] = [0] * 26
        last_sym: List[int] = [0] * 26
        inner_cost: List[int] = [0] * 26

        for idx, code in enumerate(self.MORSE):
            first_sym[idx] = 0 if code[0] == "." else 1
            last_sym[idx] = 0 if code[-1] == "." else 1

            cost: int = 0
            for k in range(1, len(code)):
                prev_symbol: int = 0 if code[k - 1] == "." else 1
                curr_symbol: int = 0 if code[k] == "." else 1
                cost += self._transition_cost(prev_symbol, curr_symbol, same_cost, switch_cost)
            inner_cost[idx] = cost

        # ------------------------------------------------------------
        # Step 2: Precompute substring data for all intervals [i][j].
        #
        # We need two versions for every substring:
        #   A) forward  -> s[i], s[i+1], ..., s[j]
        #   B) reversed -> s[j], s[j-1], ..., s[i]
        #
        # For each version we store:
        # - first symbol of the whole Morse stream
        # - last symbol of the whole Morse stream
        # - internal cost excluding the first symbol
        #
        # Why excluding the first symbol?
        # Because the first symbol of the entire transmission is special: it costs start_cost.
        # Every later symbol is charged via transitions from the previous symbol.
        # This representation makes segment concatenation very clean.
        #
        # Forward recurrence:
        #   info(i, j) can be built from info(i, j-1) + letter s[j]
        #
        # Reversed recurrence:
        #   reversed(i, j) corresponds to sequence s[j], s[j-1], ..., s[i]
        #   It can be built from reversed(i+1, j) with letter s[i] appended at the end.
        # ------------------------------------------------------------
        f_first: List[List[int]] = [[0] * n for _ in range(n)]
        f_last: List[List[int]] = [[0] * n for _ in range(n)]
        f_cost: List[List[int]] = [[0] * n for _ in range(n)]

        r_first: List[List[int]] = [[0] * n for _ in range(n)]
        r_last: List[List[int]] = [[0] * n for _ in range(n)]
        r_cost: List[List[int]] = [[0] * n for _ in range(n)]

        # Base case: single-letter substrings.
        for i in range(n):
            c_idx: int = self._char_index(s[i])

            f_first[i][i] = first_sym[c_idx]
            f_last[i][i] = last_sym[c_idx]
            f_cost[i][i] = inner_cost[c_idx]

            r_first[i][i] = first_sym[c_idx]
            r_last[i][i] = last_sym[c_idx]
            r_cost[i][i] = inner_cost[c_idx]

        # Build longer substrings.
        for length in range(2, n + 1):
            for i in range(0, n - length + 1):
                j: int = i + length - 1

                # -------------------------
                # Forward substring [i..j]
                # -------------------------
                # Start from forward [i..j-1], then append letter s[j].
                left_first: int = f_first[i][j - 1]
                left_last: int = f_last[i][j - 1]
                left_cost: int = f_cost[i][j - 1]

                c_idx = self._char_index(s[j])

                f_first[i][j] = left_first
                f_last[i][j] = last_sym[c_idx]
                f_cost[i][j] = (
                    left_cost
                    + self._transition_cost(left_last, first_sym[c_idx], same_cost, switch_cost)
                    + inner_cost[c_idx]
                )

                # --------------------------
                # Reversed substring [i..j]
                # --------------------------
                # Reversed order is s[j], s[j-1], ..., s[i].
                # Start from reversed [i+1..j], which is s[j], ..., s[i+1],
                # then append letter s[i] at the end.
                right_first: int = r_first[i + 1][j]
                right_last: int = r_last[i + 1][j]
                right_cost: int = r_cost[i + 1][j]

                c_idx = self._char_index(s[i])

                r_first[i][j] = right_first
                r_last[i][j] = last_sym[c_idx]
                r_cost[i][j] = (
                    right_cost
                    + self._transition_cost(right_last, first_sym[c_idx], same_cost, switch_cost)
                    + inner_cost[c_idx]
                )

        # ------------------------------------------------------------
        # Step 3: Dynamic programming over prefixes.
        #
        # dp[pos][sym] means:
        #   minimum cost to encode some valid transformed version of s[0:pos]
        #   such that the final Morse symbol of the encoded stream is `sym`
        #   (0='.', 1='-')
        #
        # We process prefixes by choosing the last group [i..j], where j = pos-1.
        # That group may be:
        #   - forward orientation
        #   - reversed orientation
        #
        # If i == 0:
        #   This is the first group of the entire transmission.
        #   Total cost = start_cost + segment_internal_cost
        #
        # If i > 0:
        #   We already encoded prefix s[0:i].
        #   To append the chosen segment, we add:
        #     previous_cost
        #     + boundary transition from previous last symbol to segment first symbol
        #     + segment internal cost
        # ------------------------------------------------------------
        INF: int = 10**30
        dp: List[List[int]] = [[INF, INF] for _ in range(n + 1)]

        for j in range(n):
            pos: int = j + 1

            for i in range(0, j + 1):
                # Try forward orientation for segment [i..j].
                seg_first: int = f_first[i][j]
                seg_last: int = f_last[i][j]
                seg_cost: int = f_cost[i][j]

                if i == 0:
                    # This segment starts the entire transmission.
                    total: int = start_cost + seg_cost
                    if total < dp[pos][seg_last]:
                        dp[pos][seg_last] = total
                else:
                    # Append after an already encoded prefix.
                    for prev_last in range(2):
                        if dp[i][prev_last] == INF:
                            continue
                        total = (
                            dp[i][prev_last]
                            + self._transition_cost(prev_last, seg_first, same_cost, switch_cost)
                            + seg_cost
                        )
                        if total < dp[pos][seg_last]:
                            dp[pos][seg_last] = total

                # Try reversed orientation for segment [i..j].
                seg_first = r_first[i][j]
                seg_last = r_last[i][j]
                seg_cost = r_cost[i][j]

                if i == 0:
                    total = start_cost + seg_cost
                    if total < dp[pos][seg_last]:
                        dp[pos][seg_last] = total
                else:
                    for prev_last in range(2):
                        if dp[i][prev_last] == INF:
                            continue
                        total = (
                            dp[i][prev_last]
                            + self._transition_cost(prev_last, seg_first, same_cost, switch_cost)
                            + seg_cost
                        )
                        if total < dp[pos][seg_last]:
                            dp[pos][seg_last] = total

        return min(dp[n][0], dp[n][1])

    def minFatigue(self, s: str, startCost: int, sameCost: int, switchCost: int) -> int:
        """
        Public wrapper matching a common interview / platform naming style.

        Args:
            s: Original lowercase string.
            startCost: Cost of the first Morse symbol.
            sameCost: Cost when consecutive Morse symbols are equal.
            switchCost: Cost when consecutive Morse symbols differ.

        Returns:
            Minimum possible fatigue.

        Time complexity:
            O(n^2)

        Space complexity:
            O(n^2)
        """
        return self.minimum_fatigue(s, startCost, sameCost, switchCost)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    s1 = "cab"
    start_cost_1 = 3
    same_cost_1 = 1
    switch_cost_1 = 4
    result_1 = solution.minFatigue(s1, start_cost_1, same_cost_1, switch_cost_1)
    print(result_1)  # Expected: 16

    # Example 2
    s2 = "azaz"
    start_cost_2 = 2
    same_cost_2 = 5
    switch_cost_2 = 1
    result_2 = solution.minFatigue(s2, start_cost_2, same_cost_2, switch_cost_2)
    print(result_2)  # Expected: 14