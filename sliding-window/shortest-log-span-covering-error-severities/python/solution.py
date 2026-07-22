"""
Title: Shortest Log Span Covering Error Severities

Problem Description:
A monitoring system records a stream of application logs as an array `logs`, where each
element is an integer severity level. You are also given another array `required`, where
each integer represents a severity level that must appear in a chosen contiguous log span.
Duplicates in `required` matter: if `required = [2, 2, 5]`, then the chosen span must
contain severity `2` at least twice and severity `5` at least once.

Your task is to return the length of the shortest contiguous subarray of `logs` that
satisfies all required severity counts. If no such subarray exists, return `-1`.

This problem models alert triage, where investigators need the smallest time window
containing all critical error patterns, including repeated occurrences of the same
severity. The arrays may be large, so solutions that check every possible subarray
will be too slow.

Constraints:
- 1 <= logs.length <= 2 * 10^5
- 1 <= required.length <= 2 * 10^5
- 1 <= logs[i], required[i] <= 10^9
- The answer should be computed in better than O(n^2) time.

Examples:
1)
Input: logs = [4, 2, 7, 2, 5, 1, 2, 5], required = [2, 5, 2]
Output: 4
Explanation: The shortest valid span is [2, 7, 2, 5], which contains severity 2 twice
and severity 5 once.

2)
Input: logs = [3, 1, 4, 1, 5, 9], required = [1, 1, 2]
Output: -1
Explanation: No contiguous span can satisfy the requirement because severity 2 never
appears in logs.
"""

from typing import Dict, List


class Solution:
    def _build_frequency(self, values: List[int]) -> Dict[int, int]:
        """
        Build a frequency map for the given list of integers.

        Args:
            values: List of integers whose counts should be recorded.

        Returns:
            A dictionary mapping each integer to its number of occurrences.

        Time complexity:
            O(m), where m is len(values)

        Space complexity:
            O(k), where k is the number of distinct integers in values
        """
        frequency: Dict[int, int] = {}
        for value in values:
            frequency[value] = frequency.get(value, 0) + 1
        return frequency

    def shortest_log_span(self, logs: List[int], required: List[int]) -> int:
        """
        Find the length of the shortest contiguous subarray of logs that contains
        all required severity levels with the correct multiplicities.

        This uses the classic sliding window technique:
        - Expand the right pointer to include more logs until the window becomes valid.
        - Then shrink the left pointer as much as possible while keeping the window valid.
        - Track the smallest valid window length seen.

        Args:
            logs: The full log stream as a list of severity integers.
            required: The required severity levels, where duplicates matter.

        Returns:
            The length of the shortest valid contiguous subarray, or -1 if impossible.

        Time complexity:
            O(n + m), where n is len(logs) and m is len(required)

        Space complexity:
            O(k), where k is the number of distinct severity values in required
            plus any tracked values inside the current window
        """
        # If required is somehow empty, the smallest span would conceptually be 0.
        # The problem constraints guarantee required has at least one element,
        # but handling this case makes the method more robust and complete.
        if not required:
            return 0

        # Build the target frequency map.
        # Example:
        # required = [2, 5, 2]
        # need = {2: 2, 5: 1}
        #
        # This tells us exactly how many times each severity must appear.
        need: Dict[int, int] = self._build_frequency(required)

        # This dictionary stores counts of required severities currently inside
        # the sliding window [left, right].
        #
        # We only care about severities that appear in `need`, because all other
        # values are irrelevant to satisfying the requirement.
        window_count: Dict[int, int] = {}

        # `required_unique` is the number of distinct severity values we must satisfy.
        # For required = [2, 5, 2], this is 2 because the distinct values are {2, 5}.
        required_unique: int = len(need)

        # `formed` counts how many distinct severity values are currently satisfied
        # exactly up to the needed threshold.
        #
        # A severity is considered "formed" when:
        # window_count[value] == need[value]
        #
        # Example:
        # need = {2: 2, 5: 1}
        # if window_count becomes {2: 2, 5: 1}, then formed = 2, meaning the
        # current window is valid.
        formed: int = 0

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far.
        # Start with infinity so any valid window will be smaller.
        best_length: int = float("inf")

        # Move the right boundary one step at a time across the logs.
        for right, severity in enumerate(logs):
            # Only update counts for severities that matter.
            # If a severity is not in `need`, it cannot help satisfy the requirement,
            # so we do not need to store it in the window_count dictionary.
            if severity in need:
                window_count[severity] = window_count.get(severity, 0) + 1

                # If after adding this severity, its count exactly matches the required
                # count, then one more distinct requirement has been satisfied.
                #
                # We use equality here, not >=, because we only want to increment
                # `formed` once when the threshold is first reached.
                if window_count[severity] == need[severity]:
                    formed += 1

            # If all distinct required severities are satisfied, the current window
            # [left, right] is valid. Now we try to shrink it from the left to make
            # it as short as possible while still remaining valid.
            while formed == required_unique and left <= right:
                # Compute current window length.
                current_length: int = right - left + 1

                # Update the best answer if this valid window is smaller.
                if current_length < best_length:
                    best_length = current_length

                # We are about to move `left` forward, so remove logs[left]
                # from the window.
                left_severity: int = logs[left]

                # Again, only required severities affect validity.
                if left_severity in need:
                    # If the count is currently exactly at the required threshold,
                    # then removing this element will make the window invalid for
                    # that severity. Therefore, `formed` must decrease.
                    if window_count[left_severity] == need[left_severity]:
                        formed -= 1

                    # Actually remove the leftmost required severity from the window.
                    window_count[left_severity] -= 1

                # Shrink the window by advancing the left boundary.
                left += 1

        # If best_length was never updated, no valid window exists.
        if best_length == float("inf"):
            return -1

        return best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    logs1: List[int] = [4, 2, 7, 2, 5, 1, 2, 5]
    required1: List[int] = [2, 5, 2]
    result1: int = solution.shortest_log_span(logs1, required1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    logs2: List[int] = [3, 1, 4, 1, 5, 9]
    required2: List[int] = [1, 1, 2]
    result2: int = solution.shortest_log_span(logs2, required2)
    print("Example 2 Result:", result2)  # Expected: -1

    # Additional quick checks
    logs3: List[int] = [2, 2, 5]
    required3: List[int] = [2, 2, 5]
    result3: int = solution.shortest_log_span(logs3, required3)
    print("Example 3 Result:", result3)  # Expected: 3

    logs4: List[int] = [1, 2, 3, 4, 5]
    required4: List[int] = [3]
    result4: int = solution.shortest_log_span(logs4, required4)
    print("Example 4 Result:", result4)  # Expected: 1