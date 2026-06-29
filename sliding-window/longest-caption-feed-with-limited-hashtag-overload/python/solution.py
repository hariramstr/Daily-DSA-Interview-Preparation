"""
Title: Longest Caption Feed With Limited Hashtag Overload

Problem Description:
A social media analytics team is reviewing a chronological feed of post captions.
Each caption is represented by an integer in the array `hashtags`, where the value
is the hashtag ID used in that post.

A feed segment is considered valid if no hashtag appears more than `limit` times
inside that contiguous segment.

Given an array `hashtags` and an integer `limit`, return the length of the longest
valid contiguous segment.

In other words, find the maximum size of a subarray such that for every distinct
hashtag ID inside the subarray, its frequency is at most `limit`.

Constraints:
- 1 <= hashtags.length <= 200000
- 1 <= hashtags[i] <= 1000000000
- 1 <= limit <= hashtags.length

Examples:
1)
Input: hashtags = [4, 1, 4, 2, 2, 4, 3], limit = 2
Output: 5
Explanation:
One longest valid segment is [1, 4, 2, 2, 4].
In this segment:
- hashtag 4 appears 2 times
- hashtag 2 appears 2 times
Both are within the allowed limit.

2)
Input: hashtags = [7, 7, 7, 8, 8, 9], limit = 1
Output: 3
Explanation:
When limit = 1, every value may appear at most once, so we are looking for the
longest subarray with all distinct values. One valid answer is [7, 8, 9].
"""

from typing import Dict, List


class Solution:
    def max_valid_segment_length(self, hashtags: List[int], limit: int) -> int:
        """
        Return the length of the longest contiguous subarray where every distinct
        value appears at most `limit` times.

        Args:
            hashtags: List of hashtag IDs in chronological order.
            limit: Maximum allowed frequency for any hashtag inside a valid segment.

        Returns:
            The maximum length of a valid contiguous segment.

        Time Complexity:
            O(n), where n is the length of `hashtags`.
            Each element is added to the sliding window once and removed at most once.

        Space Complexity:
            O(k), where k is the number of distinct values currently tracked in the
            frequency dictionary. In the worst case, this can be O(n).
        """
        # This dictionary stores how many times each hashtag appears
        # inside the current sliding window.
        #
        # Example:
        # If the current window is [1, 4, 2, 2, 4], then:
        # counts = {1: 1, 4: 2, 2: 2}
        counts: Dict[int, int] = {}

        # `left` is the starting index of the current window.
        # `right` will expand from left to right as we scan the array.
        left: int = 0

        # This will store the best (maximum) valid window length found so far.
        best_length: int = 0

        # We expand the window one element at a time using `right`.
        for right, hashtag in enumerate(hashtags):
            # Step 1: Include the new element at index `right` into the window.
            #
            # We increase its frequency in the dictionary.
            counts[hashtag] = counts.get(hashtag, 0) + 1

            # Step 2: If adding this hashtag made its frequency exceed `limit`,
            # the current window is no longer valid.
            #
            # Important observation:
            # Before adding `hashtags[right]`, the window was valid.
            # Therefore, the only possible rule violation after adding one element
            # is that this newly added hashtag now appears too many times.
            #
            # So we shrink the window from the left until this hashtag's count
            # becomes valid again.
            while counts[hashtag] > limit:
                # The element leaving the window is at index `left`.
                left_hashtag: int = hashtags[left]

                # Decrease its count because it is no longer inside the window.
                counts[left_hashtag] -= 1

                # Move the left boundary to the right, effectively shrinking the window.
                left += 1

            # Step 3: At this point, the window [left, right] is valid.
            # Every hashtag inside it appears at most `limit` times.
            current_length: int = right - left + 1

            # Step 4: Update the best answer if this valid window is larger.
            if current_length > best_length:
                best_length = current_length

        # After scanning the entire array, `best_length` is the answer.
        return best_length

    def maxSubarrayLength(self, hashtags: List[int], limit: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            hashtags: List of hashtag IDs in chronological order.
            limit: Maximum allowed frequency for any hashtag inside a valid segment.

        Returns:
            The maximum length of a valid contiguous segment.

        Time Complexity:
            O(n), where n is the length of `hashtags`.

        Space Complexity:
            O(n) in the worst case for the frequency dictionary.
        """
        return self.max_valid_segment_length(hashtags, limit)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    hashtags_1: List[int] = [4, 1, 4, 2, 2, 4, 3]
    limit_1: int = 2
    result_1: int = solution.max_valid_segment_length(hashtags_1, limit_1)
    print("Example 1 Result:", result_1)  # Expected: 5

    # Example 2
    hashtags_2: List[int] = [7, 7, 7, 8, 8, 9]
    limit_2: int = 1
    result_2: int = solution.max_valid_segment_length(hashtags_2, limit_2)
    print("Example 2 Result:", result_2)  # Expected: 3

    # Additional quick checks
    hashtags_3: List[int] = [1, 2, 3, 4]
    limit_3: int = 1
    result_3: int = solution.max_valid_segment_length(hashtags_3, limit_3)
    print("Additional Check 1:", result_3)  # Expected: 4

    hashtags_4: List[int] = [5, 5, 5, 5]
    limit_4: int = 2
    result_4: int = solution.max_valid_segment_length(hashtags_4, limit_4)
    print("Additional Check 2:", result_4)  # Expected: 2