"""
Title: Longest Packet Window With Exact Priority Balance

Problem Description:
A network monitor records a stream of packet priorities in an integer array priorities,
where each value is in the range [1, m]. You are also given an integer array target of
length m, where target[i] represents the exact number of packets with priority i + 1
that must appear inside a valid window. A contiguous window is called balanced if, for
every priority level p from 1 to m, the count of p inside the window is exactly
target[p - 1].

However, the monitor is noisy: packets with priority values greater than m are considered
corrupted and may appear in the stream. A balanced window cannot contain any corrupted
packet. Your task is to return the length of the longest contiguous balanced window.
If no such window exists, return 0.

Because the target counts are exact, a valid answer is not just any subarray with bounded
frequencies. The window must match the full frequency profile exactly and contain no extra
valid-priority packets beyond the required counts. This makes the problem subtle when many
repeated values and corrupted packets split the stream into candidate regions.

Constraints:
- 1 <= priorities.length <= 200000
- 1 <= m <= 100000
- 0 <= target[i] <= priorities.length
- 1 <= priorities[i] <= 1000000000
- The sum of all target values may be 0
"""

from typing import List


class Solution:
    def longest_balanced_window(self, priorities: List[int], target: List[int]) -> int:
        """
        Return the length of the longest contiguous window whose frequency profile matches
        the target array exactly and contains no corrupted values.

        Args:
            priorities: Stream of packet priorities.
            target: Exact required counts for priorities 1..m, where m == len(target).

        Returns:
            The length of the longest valid balanced window. Returns 0 if none exists.

        Time complexity:
            O(n), where n is len(priorities). Each element is processed a constant number
            of times by the sliding window.

        Space complexity:
            O(m), for the frequency array used to track counts inside the current window.
        """
        # The number of valid priority values is exactly the length of target.
        # Valid priorities are 1, 2, ..., m.
        m: int = len(target)

        # The total number of packets required in any valid window.
        # This is extremely important:
        # if a window is valid, its length is forced to be exactly this sum.
        # There is no flexibility because counts must match target exactly.
        required_total: int = sum(target)

        # Special case:
        # If the target asks for zero packets of every priority, then the only window
        # that matches exactly is the empty window, whose length is 0.
        # Since the problem asks for the longest contiguous balanced window length,
        # the answer is 0 in this case.
        if required_total == 0:
            return 0

        n: int = len(priorities)

        # current_count[value] will store how many times priority "value" appears
        # in the current sliding window.
        #
        # We allocate size m + 1 so that we can use direct indexing by priority value:
        # current_count[1] corresponds to priority 1, ..., current_count[m] to priority m.
        # Index 0 is unused.
        current_count: List[int] = [0] * (m + 1)

        # Sliding window left boundary.
        left: int = 0

        # This variable counts how many priority types currently match their target exactly.
        #
        # Example:
        # if target = [2, 2, 1], then there are 3 priority types total.
        # If current window has counts:
        #   priority 1 -> 2  (matches)
        #   priority 2 -> 1  (does not match)
        #   priority 3 -> 1  (matches)
        # then exact_match_types = 2.
        #
        # A window is valid exactly when:
        #   1) its length is required_total
        #   2) exact_match_types == m
        exact_match_types: int = 0

        # Before processing any element, every current count is 0.
        # For any priority whose target is also 0, that priority already matches.
        # We count those initially.
        for priority in range(1, m + 1):
            if target[priority - 1] == 0:
                exact_match_types += 1

        # Best answer found so far.
        best: int = 0

        # Helper logic is written inline for clarity and performance.
        # We expand the window by moving "right" from left to right.
        for right in range(n):
            value: int = priorities[right]

            # Corrupted packet handling:
            # Any value outside the valid range [1, m] cannot appear in a valid window.
            # Therefore, such a value splits the array into independent regions.
            #
            # When we hit a corrupted value, every window that includes it is invalid.
            # So we must completely reset the sliding window and start fresh after it.
            if value < 1 or value > m:
                # Remove all elements currently in the window one by one so that
                # current_count and exact_match_types return to the empty-window state.
                while left < right:
                    remove_value: int = priorities[left]

                    # Before changing the count, if this priority currently matches target,
                    # it will stop matching after the decrement, so we subtract first.
                    if current_count[remove_value] == target[remove_value - 1]:
                        exact_match_types -= 1

                    current_count[remove_value] -= 1

                    # After decrementing, it may become equal to target again.
                    # If so, we restore one exact match.
                    if current_count[remove_value] == target[remove_value - 1]:
                        exact_match_types += 1

                    left += 1

                # Skip the corrupted element itself by moving left to right + 1.
                left = right + 1
                continue

            # Add the new valid value at the right end of the window.
            #
            # We carefully maintain exact_match_types:
            # - If the count was exactly equal to target before incrementing,
            #   then incrementing breaks the match, so subtract 1 first.
            if current_count[value] == target[value - 1]:
                exact_match_types -= 1

            current_count[value] += 1

            # - If the count becomes exactly equal to target after incrementing,
            #   then we gained a match, so add 1.
            if current_count[value] == target[value - 1]:
                exact_match_types += 1

            # Since any valid window must have length exactly required_total,
            # we shrink from the left whenever the window becomes too large.
            while right - left + 1 > required_total:
                remove_value = priorities[left]

                # Same careful update logic while removing from the left.
                if current_count[remove_value] == target[remove_value - 1]:
                    exact_match_types -= 1

                current_count[remove_value] -= 1

                if current_count[remove_value] == target[remove_value - 1]:
                    exact_match_types += 1

                left += 1

            # Now the window length is at most required_total.
            # It can only be valid if its length is exactly required_total
            # and every priority type matches target exactly.
            if right - left + 1 == required_total and exact_match_types == m:
                best = required_total
                # Because every valid window must have this exact fixed length,
                # once we find one, this is already the maximum possible answer.
                # We can return immediately for efficiency.
                return best

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    priorities1: List[int] = [1, 2, 1, 3, 2, 1, 2]
    target1: List[int] = [2, 2, 1]
    result1: int = solution.longest_balanced_window(priorities1, target1)
    print(result1)  # Expected: 5

    # Example 2
    priorities2: List[int] = [4, 1, 2, 1, 3, 2, 5, 1, 2, 3]
    target2: List[int] = [1, 1, 1]
    result2: int = solution.longest_balanced_window(priorities2, target2)
    print(result2)  # Expected: 3

    # Additional quick checks
    priorities3: List[int] = [1, 1, 2, 2, 3]
    target3: List[int] = [2, 2, 1]
    result3: int = solution.longest_balanced_window(priorities3, target3)
    print(result3)  # Expected: 5

    priorities4: List[int] = [10, 10, 10]
    target4: List[int] = [1]
    result4: int = solution.longest_balanced_window(priorities4, target4)
    print(result4)  # Expected: 0

    priorities5: List[int] = [1, 2, 3]
    target5: List[int] = [0, 0, 0]
    result5: int = solution.longest_balanced_window(priorities5, target5)
    print(result5)  # Expected: 0