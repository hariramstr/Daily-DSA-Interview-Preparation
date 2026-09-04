"""
Title: Longest Transcript Window With Bounded Filler Ratio

Problem Description:
You are given a transcript of a meeting as an array `words`, where each element is a
lowercase word spoken at a particular time step. Some words are considered filler words
(such as "um", "uh", or "like"). You are also given an array `isFiller` of the same
length, where `isFiller[i] = 1` if `words[i]` is a filler word and `0` otherwise.

A contiguous window of the transcript is called usable if it satisfies both of the
following conditions:
1. The ratio of filler words in the window is at most `p / q`, where `p` and `q` are
   positive integers and `0 <= p <= q`.
2. The window contains at least `k` distinct non-filler words.

Return the length of the longest usable contiguous window.

Notes:
- Only non-filler words count toward the distinct-word requirement.
- Filler words still count toward the total window length and toward the filler ratio.
- The filler ratio of a window with `f` filler words and total length `len` is `f / len`.
  To avoid precision issues, compare using integer arithmetic.
- If no window satisfies the conditions, return `0`.

Constraints:
- `1 <= words.length == isFiller.length <= 2 * 10^5`
- `1 <= words[i].length <= 20`
- `words[i]` consists of lowercase English letters
- `isFiller[i]` is either `0` or `1`
- `0 <= p <= q <= 10^6`
- `1 <= k <= words.length`
"""

from collections import defaultdict
from typing import DefaultDict, Dict, List


class Solution:
    def _window_satisfies_ratio(self, filler_count: int, window_length: int, p: int, q: int) -> bool:
        """
        Check whether the current window satisfies the filler ratio constraint.

        We must verify:
            filler_count / window_length <= p / q

        To avoid floating-point precision issues, we cross-multiply:
            filler_count * q <= p * window_length

        Args:
            filler_count: Number of filler words in the current window.
            window_length: Total number of words in the current window.
            p: Numerator of the allowed filler ratio.
            q: Denominator of the allowed filler ratio.

        Returns:
            True if the ratio constraint is satisfied, otherwise False.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        return filler_count * q <= p * window_length

    def longest_usable_window(self, words: List[str], isFiller: List[int], p: int, q: int, k: int) -> int:
        """
        Return the length of the longest contiguous window that:
        1) Has filler ratio at most p / q
        2) Contains at least k distinct non-filler words

        Core idea:
        - We use a sliding window [left, right].
        - The filler-ratio condition is "hard": if it is violated, we must move `left`
          forward until it becomes valid again.
        - While maintaining a valid ratio window, we track frequencies of non-filler words
          and the number of distinct non-filler words currently inside the window.
        - Whenever the ratio is valid and distinct_non_filler >= k, the current window is
          usable, so we update the answer.

        Important correctness note:
        - The ratio condition is monotonic with respect to shrinking from the left:
          if a window violates the ratio, removing elements from the left can eventually
          restore validity, and we can maintain the smallest left that keeps the ratio valid.
        - Because we examine every right endpoint exactly once and move left only forward,
          the total work is linear.

        Args:
            words: Transcript words.
            isFiller: Parallel array where 1 means filler and 0 means non-filler.
            p: Numerator of allowed filler ratio.
            q: Denominator of allowed filler ratio.
            k: Minimum number of distinct non-filler words required.

        Returns:
            Length of the longest usable contiguous window. Returns 0 if none exists.

        Time complexity:
            O(n), where n is len(words), because each index enters and leaves the window
            at most once.

        Space complexity:
            O(m), where m is the number of distinct non-filler words currently tracked.
            In the worst case this is O(n).
        """
        n: int = len(words)

        # Frequency map for non-filler words currently inside the sliding window.
        # We do NOT track filler words here because they do not contribute to the
        # distinct-word requirement. We only need a simple count of fillers.
        non_filler_freq: DefaultDict[str, int] = defaultdict(int)

        # `distinct_non_filler` stores how many different non-filler words are present
        # in the current window with positive frequency.
        distinct_non_filler: int = 0

        # `filler_count` stores how many filler words are in the current window.
        filler_count: int = 0

        # Standard sliding-window left boundary.
        left: int = 0

        # Best answer found so far.
        best: int = 0

        # Expand the window by moving `right` from left to right across the array.
        for right in range(n):
            # ---------------------------------------------------------------
            # STEP 1: Add the new element at index `right` into the window.
            # ---------------------------------------------------------------
            if isFiller[right] == 1:
                # This word is filler, so it increases the filler count.
                filler_count += 1
            else:
                # This word is non-filler.
                # We update its frequency in the hash map.
                word: str = words[right]
                if non_filler_freq[word] == 0:
                    # Frequency goes from 0 -> 1, so this is a newly distinct
                    # non-filler word inside the window.
                    distinct_non_filler += 1
                non_filler_freq[word] += 1

            # ---------------------------------------------------------------
            # STEP 2: Enforce the hard ratio constraint.
            #
            # If the current window violates:
            #     filler_count / window_length <= p / q
            # we must shrink from the left until it becomes valid again.
            #
            # This is the key "hard sliding window" part:
            # - We never allow the maintained window to remain ratio-invalid.
            # - `left` only moves forward, so total complexity stays linear.
            # ---------------------------------------------------------------
            while left <= right and not self._window_satisfies_ratio(
                filler_count=filler_count,
                window_length=right - left + 1,
                p=p,
                q=q,
            ):
                # Remove the element at index `left` from the window.
                if isFiller[left] == 1:
                    # Removing a filler decreases the filler count.
                    filler_count -= 1
                else:
                    # Removing a non-filler means updating its frequency.
                    left_word: str = words[left]
                    non_filler_freq[left_word] -= 1
                    if non_filler_freq[left_word] == 0:
                        # Frequency dropped to zero, so this distinct non-filler
                        # word is no longer present in the window.
                        distinct_non_filler -= 1
                        del non_filler_freq[left_word]

                # Move the left boundary rightward.
                left += 1

            # ---------------------------------------------------------------
            # STEP 3: At this point, the current window [left, right] satisfies
            # the ratio constraint.
            #
            # Now we only need to check the second condition:
            #     at least k distinct non-filler words
            #
            # If that is also true, this window is usable.
            # ---------------------------------------------------------------
            if distinct_non_filler >= k:
                current_length: int = right - left + 1
                if current_length > best:
                    best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    words1: List[str] = ["we", "should", "um", "ship", "this", "uh", "week"]
    is_filler1: List[int] = [0, 0, 1, 0, 0, 1, 0]
    p1: int = 1
    q1: int = 3
    k1: int = 4
    result1: int = solution.longest_usable_window(words1, is_filler1, p1, q1, k1)
    print(result1)  # Expected: 6

    # Example 2
    words2: List[str] = ["uh", "plan", "plan", "um", "launch", "now", "like", "launch", "ready"]
    is_filler2: List[int] = [1, 0, 0, 1, 0, 0, 1, 0, 0]
    p2: int = 1
    q2: int = 4
    k2: int = 3
    result2: int = solution.longest_usable_window(words2, is_filler2, p2, q2, k2)
    print(result2)  # Expected: 5