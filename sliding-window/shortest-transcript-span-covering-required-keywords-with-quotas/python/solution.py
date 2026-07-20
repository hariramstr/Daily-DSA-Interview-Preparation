"""
Title: Shortest Transcript Span Covering Required Keywords with Quotas

Problem Description:
You are given a transcript represented by an array of lowercase words `words`, where
`words[i]` is the `i`-th token spoken in order. You are also given a list of required
keywords with minimum occurrence quotas, represented by two arrays: `required` and `need`,
where `required[j]` must appear at least `need[j]` times inside the chosen contiguous span.

Your task is to return the length of the shortest contiguous subarray of `words` that
satisfies all keyword quotas. If no such span exists, return `-1`.

Unlike the classic minimum-cover problems where each target appears once, the same keyword
may need to appear multiple times, and the transcript can be very large. An efficient
sliding window solution is expected. The challenge is to maintain counts correctly while
shrinking the window as aggressively as possible without violating any quota.

Formally, find the minimum value of `r - l + 1` such that for every index `j`, the subarray
`words[l...r]` contains at least `need[j]` occurrences of `required[j]`.

Constraints:
- 1 <= words.length <= 200000
- 1 <= required.length == need.length <= 100000
- 1 <= sum(need) <= 200000
- 1 <= words[i].length, required[j].length <= 20
- All strings contain only lowercase English letters.
- All values in `required` are distinct.

Example 1:
Input:
words = ["api","error","db","api","timeout","error","api"]
required = ["api","error"]
need = [2,1]
Output: 4

Explanation:
The shortest valid span is ["api","error","db","api"] with length 4.
It contains "api" twice and "error" once.

Example 2:
Input:
words = ["login","cache","login","queue","cache","queue"]
required = ["login","queue","cache"]
need = [2,1,2]
Output: 5

Explanation:
The span ["login","cache","login","queue","cache"] has two "login", one "queue",
and two "cache". No shorter valid span exists.

Return only the minimum length, not the span itself.
"""

from typing import Dict, List


class Solution:
    def min_span_with_quotas(
        self,
        words: List[str],
        required: List[str],
        need: List[int],
    ) -> int:
        """
        Find the length of the shortest contiguous subarray that satisfies all keyword quotas.

        The method uses a classic sliding window:
        - Expand the right boundary until the window becomes valid.
        - Then shrink the left boundary as much as possible while keeping the window valid.
        - Track the minimum valid window length seen.

        Args:
            words: The full transcript as a list of lowercase words.
            required: Distinct required keywords.
            need: Minimum required counts for each corresponding keyword.

        Returns:
            The minimum length of a contiguous subarray that contains every required keyword
            at least the needed number of times. Returns -1 if no such subarray exists.

        Time Complexity:
            O(n + m), where:
            - n = len(words)
            - m = len(required)
            Each word is processed at most twice (once by the right pointer and once by the left pointer).

        Space Complexity:
            O(m), for the hash maps storing required counts and current window counts.
        """
        # Build a dictionary that tells us the quota for each required keyword.
        # Example:
        # required = ["api", "error"], need = [2, 1]
        # target = {"api": 2, "error": 1}
        #
        # Why use a dictionary?
        # Because we need O(1) average-time lookup to quickly answer:
        # "Is this word relevant?" and "How many of this word do we need?"
        target: Dict[str, int] = {}
        for word, quota in zip(required, need):
            target[word] = quota

        # This dictionary stores how many times each required keyword currently appears
        # inside the active sliding window [left, right].
        #
        # We only store counts for words that are actually required.
        # Non-required words do not help satisfy quotas, so we do not need to track them.
        window_count: Dict[str, int] = {}

        # `formed` counts how many distinct required keywords currently meet their quota.
        #
        # Example:
        # target = {"api": 2, "error": 1}
        # If window_count = {"api": 2, "error": 0}, then formed = 1
        # because only "api" has reached its required amount.
        #
        # The window is valid exactly when:
        # formed == total_required_types
        formed: int = 0
        total_required_types: int = len(required)

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far.
        # Start with infinity so any valid window will be smaller.
        best_length: int = float("inf")

        # Move the right boundary from left to right across the transcript.
        for right, word in enumerate(words):
            # Step 1: Expand the window by including words[right].
            #
            # If this word is not required, it does not affect quota satisfaction.
            # We still allow it inside the window because valid spans can contain extra words.
            if word in target:
                # Increase the count of this required word in the current window.
                window_count[word] = window_count.get(word, 0) + 1

                # If this increment makes the count exactly equal to the needed quota,
                # then one more required keyword type is now satisfied.
                #
                # Important:
                # We only increase `formed` when the count becomes exactly equal to target[word].
                # If it becomes larger than the target, we do NOT increase `formed` again.
                if window_count[word] == target[word]:
                    formed += 1

            # Step 2: If the current window is valid, try to shrink it from the left.
            #
            # This is the key optimization:
            # Once a window is valid, any larger version of it is not better than a smaller
            # valid version. So we aggressively move `left` rightward while preserving validity.
            while formed == total_required_types and left <= right:
                # The current window [left, right] satisfies all quotas.
                current_length: int = right - left + 1
                if current_length < best_length:
                    best_length = current_length

                # We are about to remove words[left] from the window.
                left_word: str = words[left]

                # Only required words matter for quota tracking.
                if left_word in target:
                    # Decrease its count because it will no longer be inside the window.
                    window_count[left_word] -= 1

                    # If the count drops below the required quota, the window becomes invalid.
                    #
                    # Example:
                    # target["api"] = 2
                    # window_count["api"] was 2, then after decrement it becomes 1
                    # => no longer enough "api" words
                    # => formed must decrease
                    if window_count[left_word] < target[left_word]:
                        formed -= 1

                # Actually move the left boundary forward.
                left += 1

        # If best_length was never updated, then no valid window exists.
        if best_length == float("inf"):
            return -1

        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    words1 = ["api", "error", "db", "api", "timeout", "error", "api"]
    required1 = ["api", "error"]
    need1 = [2, 1]
    result1 = solution.min_span_with_quotas(words1, required1, need1)
    print(result1)  # Expected: 4

    # Example 2
    words2 = ["login", "cache", "login", "queue", "cache", "queue"]
    required2 = ["login", "queue", "cache"]
    need2 = [2, 1, 2]
    result2 = solution.min_span_with_quotas(words2, required2, need2)
    print(result2)  # Expected: 5

    # Additional quick checks
    words3 = ["a", "b", "c"]
    required3 = ["a", "c"]
    need3 = [1, 1]
    result3 = solution.min_span_with_quotas(words3, required3, need3)
    print(result3)  # Expected: 3

    words4 = ["a", "b", "a"]
    required4 = ["a", "b", "c"]
    need4 = [1, 1, 1]
    result4 = solution.min_span_with_quotas(words4, required4, need4)
    print(result4)  # Expected: -1