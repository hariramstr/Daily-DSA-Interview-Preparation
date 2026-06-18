"""
Title: Longest Upgrade Window With One Rollback

Problem Description:
A deployment team records the result of each software upgrade attempt in chronological
order using an integer array `status`, where `1` means the upgrade at that minute
succeeded and `0` means it failed. The team is allowed to perform at most one rollback
operation on a single failed attempt, turning one `0` into a `1`. Your task is to find
the length of the longest contiguous time window that can consist entirely of successful
upgrades after applying at most one rollback.

Return the maximum possible length of such a contiguous window.

This is an arrays problem focused on finding an optimal continuous segment efficiently.
A brute-force check of every subarray will be too slow for large inputs, so you should
design an algorithm that runs in linear time.

Constraints:
- 1 <= status.length <= 100000
- status[i] is either 0 or 1

Example 1:
Input: status = [1,1,0,1,1,1,0,1]
Output: 6
Explanation: Roll back the failure at index 2. The subarray [1,1,0,1,1,1] becomes six
consecutive successes.

Example 2:
Input: status = [0,1,1,0,1,0,1,1]
Output: 4
Explanation: Roll back the failure at index 3. Then the window [1,1,0,1] becomes four
consecutive successes. No longer contiguous window can be made all 1s using only one
rollback.

Notes:
- You may choose not to use the rollback if the array already contains all successful
  upgrades.
- The rollback can be applied to any single failed attempt, but only once.
- The answer must be based on a contiguous subarray, not scattered positions.
"""

from typing import List


class Solution:
    def longest_upgrade_window(self, status: List[int]) -> int:
        """
        Find the maximum length of a contiguous subarray that can become all 1s
        after changing at most one 0 into 1.

        Args:
            status: A list of integers where each value is either 0 or 1.

        Returns:
            The maximum possible length of a contiguous window consisting entirely
            of successful upgrades after at most one rollback.

        Time complexity:
            O(n), where n is the length of the input array. Each element is visited
            at most a constant number of times by the sliding window pointers.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # We use the classic "sliding window" technique.
        #
        # Core idea:
        # We want the longest contiguous window that contains at most one 0.
        # Why "at most one 0"?
        # Because we are allowed to roll back (flip) at most one failed attempt.
        # So any valid window can contain:
        #   - zero failures already, or
        #   - exactly one failure that we will convert to success.
        #
        # If a window ever contains two or more zeros, then even after one rollback,
        # at least one zero would remain, so that window cannot become all 1s.
        #
        # Therefore:
        #   Maintain a window [left, right]
        #   Expand right one step at a time
        #   Count how many zeros are inside the window
        #   If zeros > 1, shrink from the left until zeros <= 1 again
        #   Track the maximum valid window length seen

        left: int = 0
        zero_count: int = 0
        best_length: int = 0

        # Move the right pointer across the array one element at a time.
        for right in range(len(status)):
            # If the new element entering the window is a failure (0),
            # increase the zero counter because our current window now
            # contains one more failed attempt.
            if status[right] == 0:
                zero_count += 1

            # If the window contains more than one zero, it is no longer valid.
            # We must move the left pointer rightward to remove elements from
            # the window until the number of zeros is back to at most one.
            while zero_count > 1:
                # If the element leaving the window is a zero, then removing it
                # reduces the number of failures inside the current window.
                if status[left] == 0:
                    zero_count -= 1

                # Shrink the window from the left.
                left += 1

            # At this point, the window [left, right] is guaranteed to contain
            # at most one zero, so it is a valid candidate.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any
            # valid window we have seen before.
            if current_length > best_length:
                best_length = current_length

        return best_length

    def longestUpgradeWindow(self, status: List[int]) -> int:
        """
        Compatibility wrapper using camelCase naming.

        Args:
            status: A list of integers where each value is either 0 or 1.

        Returns:
            The maximum possible length of a contiguous window consisting entirely
            of successful upgrades after at most one rollback.

        Time complexity:
            O(n), where n is the length of the input array.

        Space complexity:
            O(1).
        """
        return self.longest_upgrade_window(status)


if __name__ == "__main__":
    solution = Solution()

    sample_inputs: List[List[int]] = [
        [1, 1, 0, 1, 1, 1, 0, 1],
        [0, 1, 1, 0, 1, 0, 1, 1],
        [1, 1, 1, 1],
        [0],
        [1],
        [0, 0, 0, 0],
        [1, 0, 1, 1, 0, 1, 1, 1],
    ]

    for status in sample_inputs:
        result: int = solution.longest_upgrade_window(status)
        print(f"status = {status}")
        print(f"Longest upgrade window with at most one rollback = {result}")
        print("-" * 60)