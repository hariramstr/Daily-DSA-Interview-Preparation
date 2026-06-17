"""
Title: Longest Chat Streak With At Most One Silent Minute

Problem Description:
You are given a binary array `messages` representing a minute-by-minute chat activity
log for a support agent. Each element is either `1` or `0`, where `1` means the agent
sent at least one message during that minute, and `0` means the minute was silent.

A chat streak is defined as a contiguous block of minutes that is considered active
if you are allowed to ignore at most one silent minute inside the block.

Your task is to return the length of the longest possible active chat streak.

In other words, find the maximum length of a contiguous subarray containing at most one `0`.

This models a real analytics scenario where a single short pause should not necessarily
break an otherwise continuous support conversation. The solution should be efficient
enough for large logs, so a sliding window approach is expected.

Constraints:
- 1 <= messages.length <= 100000
- messages[i] is either 0 or 1

Example 1:
Input: messages = [1,1,0,1,1,1,0,1]
Output: 6

Example 2:
Input: messages = [0,1,1,1,0,1,1]
Output: 5
"""

from typing import List


class Solution:
    def longest_chat_streak(self, messages: List[int]) -> int:
        """
        Find the length of the longest contiguous subarray containing at most one 0.

        This uses the sliding window technique:
        - Expand the right side of the window one step at a time.
        - Count how many zeros are currently inside the window.
        - If the window becomes invalid (more than one zero), move the left side
          forward until the window is valid again.
        - Track the maximum valid window length seen so far.

        Args:
            messages: A list of integers where each value is either 0 or 1.

        Returns:
            The maximum length of a contiguous subarray that contains at most one 0.

        Time complexity:
            O(n), where n is the length of messages, because each element is processed
            at most twice: once by the right pointer and once by the left pointer.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # `left` marks the beginning of our current sliding window.
        # The window will always represent a contiguous section of the array.
        left: int = 0

        # `zero_count` stores how many silent minutes (0 values) are currently
        # inside the window from `left` to `right`.
        #
        # We need this because the problem allows at most one 0 in any valid streak.
        zero_count: int = 0

        # `best_length` stores the maximum valid window size we have found so far.
        best_length: int = 0

        # Move `right` from left to right across the array.
        # At each step, we include messages[right] into the current window.
        for right in range(len(messages)):
            # If the new element is a silent minute, increase the zero counter.
            # This helps us quickly know whether the current window is still valid.
            if messages[right] == 0:
                zero_count += 1

            # If the window contains more than one 0, it is invalid.
            # We must shrink it from the left until it becomes valid again.
            #
            # Why a while loop instead of an if?
            # Because moving left by one position may still leave more than one 0
            # inside the window, so we keep shrinking until zero_count <= 1.
            while zero_count > 1:
                # If the element leaving the window is a 0, we must decrease
                # zero_count because that silent minute is no longer inside the window.
                if messages[left] == 0:
                    zero_count -= 1

                # Move the left boundary rightward to shrink the window.
                left += 1

            # At this point, the window [left, right] is guaranteed to be valid:
            # it contains at most one 0.
            #
            # Compute its length. Since both ends are inclusive, length is:
            # right - left + 1
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any
            # previously seen valid window.
            if current_length > best_length:
                best_length = current_length

        # After scanning the entire array, best_length holds the answer.
        return best_length

    def longestSubarray(self, messages: List[int]) -> int:
        """
        Compatibility wrapper that calls the main solution method.

        Args:
            messages: A list of integers where each value is either 0 or 1.

        Returns:
            The maximum length of a contiguous subarray that contains at most one 0.

        Time complexity:
            O(n), where n is the length of messages.

        Space complexity:
            O(1).
        """
        return self.longest_chat_streak(messages)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement.
    messages1: List[int] = [1, 1, 0, 1, 1, 1, 0, 1]
    result1: int = solution.longest_chat_streak(messages1)
    print(f"Input: {messages1}")
    print(f"Longest chat streak: {result1}")
    print("Expected: 6")
    print()

    # Example 2 from the problem statement.
    messages2: List[int] = [0, 1, 1, 1, 0, 1, 1]
    result2: int = solution.longest_chat_streak(messages2)
    print(f"Input: {messages2}")
    print(f"Longest chat streak: {result2}")
    print("Expected: 5")
    print()

    # Additional beginner-friendly test cases.
    messages3: List[int] = [1]
    result3: int = solution.longest_chat_streak(messages3)
    print(f"Input: {messages3}")
    print(f"Longest chat streak: {result3}")
    print("Expected: 1")
    print()

    messages4: List[int] = [0]
    result4: int = solution.longest_chat_streak(messages4)
    print(f"Input: {messages4}")
    print(f"Longest chat streak: {result4}")
    print("Expected: 1")
    print()

    messages5: List[int] = [1, 1, 1, 1]
    result5: int = solution.longest_chat_streak(messages5)
    print(f"Input: {messages5}")
    print(f"Longest chat streak: {result5}")
    print("Expected: 4")
    print()

    messages6: List[int] = [0, 0, 0, 0]
    result6: int = solution.longest_chat_streak(messages6)
    print(f"Input: {messages6}")
    print(f"Longest chat streak: {result6}")
    print("Expected: 1")
    print()

    messages7: List[int] = [1, 0, 1, 0, 1, 1]
    result7: int = solution.longest_chat_streak(messages7)
    print(f"Input: {messages7}")
    print(f"Longest chat streak: {result7}")
    print("Expected: 4")