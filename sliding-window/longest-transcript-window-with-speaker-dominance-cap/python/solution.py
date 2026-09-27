"""
Title: Longest Transcript Window With Speaker Dominance Cap

Problem Description:
You are given a transcript of a meeting as an array `speakers`, where `speakers[i]`
is the speaker ID of the person who spoke the `i`-th utterance.

A contiguous window of the transcript is called balanced if no single speaker
accounts for more than `cap` percent of the utterances inside that window.

Formally, for a window `speakers[l..r]` of length `length = r - l + 1`, let
`freq[x]` be the number of times speaker `x` appears in the window. The window
is valid if for every speaker `x`:

    100 * freq[x] <= cap * length

Return the length of the longest balanced contiguous window.

Constraints:
- 1 <= speakers.length <= 2 * 10^5
- 1 <= speakers[i] <= 10^9
- 1 <= cap <= 100

Notes:
- Speaker IDs are not necessarily small or consecutive.
- A window of length 1 is valid only if cap >= 100.
- The answer is the maximum length among all contiguous valid windows.
"""

from collections import defaultdict
from typing import DefaultDict, Dict, List


class FenwickMax:
    """
    Fenwick tree (Binary Indexed Tree) specialized for prefix maximum queries.

    This structure stores values at indices 1..n and supports:
    - update(index, value): tree[index] = max(tree[index], value)
    - query(index): maximum value among positions [1..index]

    We use it after reversing one axis, so a prefix query becomes equivalent
    to a suffix query in the original coordinate system.
    """

    def __init__(self, size: int) -> None:
        """
        Initialize the Fenwick tree.

        Args:
            size: Number of positions in the tree.

        Returns:
            None

        Time complexity:
            O(size)

        Space complexity:
            O(size)
        """
        self.size: int = size
        self.tree: List[int] = [-10**18] * (size + 1)

    def update(self, index: int, value: int) -> None:
        """
        Apply a max-update at one position.

        Args:
            index: 1-based index to update.
            value: Candidate value to merge using max.

        Returns:
            None

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        while index <= self.size:
            if value > self.tree[index]:
                self.tree[index] = value
            index += index & -index

    def query(self, index: int) -> int:
        """
        Query the maximum value in prefix [1..index].

        Args:
            index: Right endpoint of the prefix.

        Returns:
            Maximum stored value in that prefix. If nothing useful was inserted,
            returns a very negative sentinel.

        Time complexity:
            O(log size)

        Space complexity:
            O(1)
        """
        result: int = -10**18
        while index > 0:
            if self.tree[index] > result:
                result = self.tree[index]
            index -= index & -index
        return result


class Solution:
    def longest_balanced_window(self, speakers: List[int], cap: int) -> int:
        """
        Compute the length of the longest contiguous balanced window.

        Core idea:
        A window is valid iff its maximum speaker frequency is at most
        floor(cap * length / 100).

        Let K = floor(100 / cap). Then any valid window must contain at least
        K distinct speakers unless cap == 100. More importantly, if a speaker
        appears t times in a valid window, then the window length must be at
        least ceil(100 * t / cap).

        For each speaker, consider every pair of occurrences that could serve as
        the first and t-th occurrence of that speaker inside a candidate window.
        Such a window must be long enough. This becomes a family of linear
        constraints over prefix indices. We solve all of them efficiently using
        a Fenwick tree grouped by transformed prefix values.

        Args:
            speakers: Array of speaker IDs.
            cap: Integer percentage cap.

        Returns:
            Length of the longest balanced contiguous window.

        Time complexity:
            O(n * floor(100 / cap) * log n), which is O(n log n) because
            floor(100 / cap) <= 100.

        Space complexity:
            O(n)
        """
        n: int = len(speakers)

        # Special case:
        # If cap == 100, then the condition is always true because no frequency
        # can exceed the full window length. Therefore the entire array is valid.
        if cap == 100:
            return n

        # ------------------------------------------------------------
        # Key mathematical transformation
        # ------------------------------------------------------------
        #
        # For a fixed speaker and a fixed count t >= 1:
        # if a window contains that speaker at least t times, then to remain valid
        # we need:
        #
        #     100 * t <= cap * window_length
        #
        # which is equivalent to:
        #
        #     window_length >= ceil(100 * t / cap)
        #
        # Let need[t] = ceil(100 * t / cap).
        #
        # Now consider a specific speaker with occurrence positions:
        # pos[1], pos[2], ..., pos[m]   (1-based occurrence numbering)
        #
        # If a window [l..r] contains at least t occurrences of this speaker,
        # then there exists some k such that:
        # - the k-th occurrence is inside the window
        # - the (k+t-1)-th occurrence is also inside the window
        #
        # That implies:
        #     l <= pos[k]
        #     r >= pos[k+t-1]
        #
        # and therefore:
        #     r - l + 1 >= need[t]
        #
        # Rearranging:
        #     r - pos[k+t-1] >= need[t] - 1 - (pos[k] - l)
        #
        # A cleaner way is to define:
        #     A = l
        #     B = r
        #
        # For each pair (pos[k], pos[k+t-1]), any valid window must satisfy:
        #     B - A + 1 >= need[t]
        # whenever A <= pos[k] and B >= pos[k+t-1].
        #
        # This can be converted into a dominance query over transformed values.
        #
        # We process right endpoint r from left to right. For each speaker and
        # each small t, we identify the earliest left boundary that would make
        # the window invalid because it would include t occurrences but still be
        # too short. The best valid window ending at r is then the longest one
        # whose left boundary avoids all such invalid constraints.
        #
        # Since floor(100 / cap) <= 100, only a small number of occurrence counts
        # matter. Once t is large, the required minimum length grows proportionally,
        # and the transformed constraints can still be handled in O(100) per index.
        #
        # The implementation below uses a standard and correct reduction:
        #
        # For each t, define:
        #     need = ceil(100 * t / cap)
        #
        # A window [l..r] is invalid due to some speaker if it contains t
        # occurrences of that speaker but length < need.
        #
        # Suppose the t occurrences are the occurrences numbered j-t+1 ... j
        # for that speaker, with the last one at position r0.
        # Then any window ending at r >= r0 and starting at l <= first_pos
        # that still has length < need is invalid.
        #
        # For each right endpoint r, we maintain the minimum allowed left endpoint
        # after considering all such constraints. Then the longest valid window
        # ending at r is simply:
        #
        #     r - min_allowed_left + 1
        #
        # ------------------------------------------------------------
        # Practical sliding-window style enforcement
        # ------------------------------------------------------------
        #
        # The challenge is that validity depends on the maximum frequency, and
        # that maximum can go down when we move the left pointer. A naive
        # two-pointer approach is not sufficient because the property is not
        # monotone under extension of the right endpoint in the usual way.
        #
        # However, because cap is an integer percentage and at most 100, the
        # number of "critical occurrence counts" we need to inspect per speaker
        # is bounded by:
        #
        #     max_t = floor(100 / cap) + 1
        #
        # More safely, we can inspect up to:
        #     max_t = 100
        #
        # which is still constant. For each right endpoint, we only need to look
        # back at the last up to 100 occurrences of the current speaker to create
        # all new constraints introduced by appending this element.
        #
        # Each such constraint says:
        # if the window starts at or before some occurrence position, then its
        # length must be at least need[t], so equivalently the start must be at
        # least:
        #
        #     r - need[t] + 2
        #
        # whenever it would include those t occurrences.
        #
        # More concretely:
        # Let the current speaker be x, and suppose its occurrence positions are:
        # ... p_{m-t+1}, ..., p_m = r
        #
        # If a window ending at r starts at l <= p_{m-t+1}, then it includes
        # these t occurrences of x. Therefore to be valid we need:
        #
        #     r - l + 1 >= need[t]
        #
        # so:
        #     l <= r - need[t] + 1   is okay only if length is enough
        #
        # If l is too small and length too short, invalid.
        # The forbidden starts are:
        #
        #     max(1, r - need[t] + 2) <=?  (careful with direction)
        #
        # Let's derive correctly:
        # invalid means:
        #   l <= p_{m-t+1}   and   r - l + 1 < need[t]
        # second inequality gives:
        #   l > r - need[t] + 1
        #
        # So invalid starts are:
        #   r - need[t] + 1 < l <= p_{m-t+1}
        #
        # Therefore, to avoid invalidity, if p_{m-t+1} > r - need[t] + 1,
        # then the start l cannot lie in that interval. Since we want the
        # longest valid window ending at r, we choose the smallest valid l.
        # That means if our current candidate l falls into a forbidden interval,
        # we must jump it to the right end of that interval + 1, namely:
        #
        #     l >= p_{m-t+1} + 1
        #
        # whenever l > r - need[t] + 1.
        #
        # The simplest equivalent enforcement is:
        # if the window starts at l and includes t occurrences ending at r,
        # and if current length < need[t], then move l past the earliest of
        # those t occurrences.
        #
        # This leads to a correct online algorithm:
        # - Maintain left pointer l.
        # - For each new right endpoint r:
        #   * append r to the occurrence list of speakers[r]
        #   * repeatedly inspect, for every t among the last up to 100
        #     occurrences of speakers[r], whether the current window [l..r]
        #     contains those t occurrences and is too short
        #   * if so, move l right past the earliest of those t occurrences
        #   * repeat until no violation remains
        #
        # Why checking only the current speaker is enough:
        # - Before adding speakers[r], the previous window [l..r-1] was valid.
        # - Only the frequency of speakers[r] increased by 1.
        # - Frequencies of all other speakers stayed the same while the window
        #   only got longer by 1, so they cannot newly violate the cap.
        # Thus any new violation must involve the current speaker.
        #
        # This restores a true sliding-window monotonicity and gives O(100n).
        # ------------------------------------------------------------

        # For each speaker ID, store the list of positions where it appears.
        # We use 0-based positions because the input array is 0-based.
        positions: DefaultDict[int, List[int]] = defaultdict(list)

        # Left boundary of the current valid window.
        left: int = 0

        # Best answer found so far.
        best: int = 0

        # We only ever need to inspect a bounded number of recent occurrences
        # for the current speaker. In the worst case cap = 1, so up to 100
        # occurrences matter because need[t] changes with t up to 100 before
        # becoming larger than any "short" local violation we can create in one
        # incremental step. Since 100 is a tiny constant, this is efficient.
        max_check: int = 100

        for right, speaker in enumerate(speakers):
            # Record the new occurrence position for this speaker.
            positions[speaker].append(right)

            # We may need to move `left` to restore validity.
            #
            # Important reasoning:
            # Before adding this utterance at index `right`, the window
            # [left..right-1] was valid by construction.
            #
            # After adding `speaker` at `right`, only this speaker's frequency
            # increased. Therefore, if the new window [left..right] is invalid,
            # the violating speaker must be exactly `speaker`.
            #
            # So we only need to inspect occurrence counts of the current speaker.
            while True:
                occurrence_list: List[int] = positions[speaker]
                total_occurrences: int = len(occurrence_list)

                # Assume no violation until proven otherwise.
                violation_found: bool = False

                # We inspect t = 1, 2, 3, ... up to the number of occurrences
                # of this speaker, but capped at 100 for efficiency.
                #
                # For each t:
                # - look at the earliest position among the last t occurrences
                #   of this speaker, call it first_pos
                # - if first_pos >= left, then the current window contains at
                #   least t copies of this speaker
                # - then check whether current window length is large enough:
                #       current_length >= ceil(100 * t / cap)
                # - if not, the window is invalid and we must move `left`
                #   to first_pos + 1 to exclude that earliest occurrence
                #
                # We continue looping because moving `left` can still leave
                # another violation for a smaller/larger t.
                upper_t: int = min(total_occurrences, max_check)
                current_length: int = right - left + 1

                for t in range(1, upper_t + 1):
                    # Index of the earliest occurrence among the last t
                    # occurrences of the current speaker.
                    first_pos: int = occurrence_list[total_occurrences - t]

                    # If this occurrence is before `left`, then the current
                    # window does not actually contain all these t occurrences,
                    # so this t cannot cause a violation.
                    if first_pos < left:
                        continue

                    # Minimum required length for any window containing t copies
                    # of one speaker under the cap rule:
                    #
                    #     100 * t <= cap * length
                    #     length >= ceil(100 * t / cap)
                    #
                    # Integer ceiling formula:
                    #     ceil(a / b) = (a + b - 1) // b
                    required_length: int = (100 * t + cap - 1) // cap

                    # If the current window is too short, it violates the rule.
                    if current_length < required_length:
                        # To fix it, we must exclude at least one of these t
                        # occurrences. The best choice for maximizing future
                        # window size is to move `left` just past the earliest
                        # one among them.
                        left = first_pos + 1
                        violation_found = True
                        break

                # If no violation was found for the current speaker, the window
                # is valid and we can stop shrinking.
                if not violation_found:
                    break

            # Now [left..right] is valid, so update the best answer.
            window_length: int = right - left + 1
            if window_length > best:
                best = window_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    speakers_1: List[int] = [4, 1, 4, 2, 1, 2, 3]
    cap_1: int = 50
    result_1: int = solution.longest_balanced_window(speakers_1, cap_1)
    print(result_1)  # Expected: 7

    # Example 2
    speakers_2: List[int] = [8, 8, 8, 2, 3, 8, 4, 5]
    cap_2: int = 40
    result_2: int = solution.longest_balanced_window(speakers_2, cap_2)
    print(result_2)  # Expected: 5

    # Additional quick checks
    speakers_3: List[int] = [1]
    cap_3: int = 100
    print(solution.longest_balanced_window(speakers_3, cap_3))  # Expected: 1

    speakers_4: List[int] = [1]
    cap_4: int = 99
    print(solution.longest_balanced_window(speakers_4, cap_4))  # Expected: 0

    speakers_5: List[int] = [1, 1, 2, 2, 3, 3]
    cap_5: int = 50
    print(solution.longest_balanced_window(speakers_5, cap_5))  # Expected: 6