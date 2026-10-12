"""
Title: Longest Caption Segment Within Reading Speed

Problem Description:
A video platform stores the number of words shown in each consecutive subtitle frame
in an integer array `words`, where `words[i]` is the word count displayed during the
`i`-th frame interval. A viewer has a reading capacity limit `maxWords`, meaning they
can comfortably follow any contiguous segment of subtitle frames only if the total
number of words shown in that segment does not exceed `maxWords`.

Your task is to return the length of the longest contiguous segment of frames the
viewer can watch comfortably. In other words, find the maximum size of a subarray
whose sum is less than or equal to `maxWords`.

This problem should be solved efficiently for large inputs. A brute-force solution
that checks every possible segment will be too slow. Since all values in `words`
are non-negative, a sliding window approach is expected.

Constraints:
- 1 <= words.length <= 100000
- 0 <= words[i] <= 10000
- 0 <= maxWords <= 10^9

Example 1:
Input: words = [3, 1, 2, 1, 1, 4], maxWords = 5
Output: 4
Explanation: The longest valid segment is [1, 2, 1, 1], whose total is 5, so the answer is 4.

Example 2:
Input: words = [5, 2, 2, 6, 1], maxWords = 7
Output: 2
Explanation: Valid longest segments include [5, 2] and [6, 1], each with total 7.
No contiguous segment of length 3 or more stays within the reading limit.

Goal:
Return only the maximum number of consecutive frame intervals that can be included
in such a segment.
"""

from typing import List


class Solution:
    def longest_caption_segment(self, words: List[int], maxWords: int) -> int:
        """
        Find the maximum length of a contiguous subarray whose sum is less than
        or equal to maxWords using the sliding window technique.

        Args:
            words: A list of non-negative integers where each value represents
                the number of words shown in one subtitle frame interval.
            maxWords: The maximum total number of words the viewer can comfortably
                read in any contiguous segment.

        Returns:
            The length of the longest contiguous segment whose sum is <= maxWords.

        Time complexity:
            O(n), where n is the length of words, because each element is added
            to the window once and removed from the window at most once.

        Space complexity:
            O(1), because only a few variables are used regardless of input size.
        """
        # The left pointer marks the beginning of our current window.
        # The right pointer will move through the array one step at a time.
        #
        # The "window" is the current contiguous segment words[left:right+1].
        left: int = 0

        # This variable stores the sum of all values currently inside the window.
        # We update it incrementally instead of recomputing sums repeatedly,
        # which is what makes the solution efficient.
        current_sum: int = 0

        # This will store the best (largest) valid window length found so far.
        max_length: int = 0

        # We expand the window by moving the right pointer from left to right
        # across the entire array.
        for right in range(len(words)):
            # Step 1: Include the new element at index "right" into the window.
            # Since the window grows by one element on the right side,
            # we add that element's value to the running sum.
            current_sum += words[right]

            # Step 2: If the window sum is too large, it is no longer valid.
            # Because all numbers are non-negative, the only way to reduce the sum
            # is to move the left pointer to the right, removing elements from
            # the start of the window.
            #
            # We keep shrinking until the window becomes valid again
            # (that is, until current_sum <= maxWords).
            while current_sum > maxWords and left <= right:
                # Remove the element currently at the left edge of the window
                # from the running sum because it is leaving the window.
                current_sum -= words[left]

                # Move the left boundary one step to the right.
                left += 1

            # Step 3: At this point, the window is guaranteed to be valid
            # because either:
            # - current_sum <= maxWords, or
            # - the window has been shrunk as much as necessary.
            #
            # So we can safely compute its length and compare it with the best
            # answer seen so far.
            window_length: int = right - left + 1

            # Update the maximum length if the current valid window is larger.
            if window_length > max_length:
                max_length = window_length

        # After processing every possible right boundary, max_length contains
        # the length of the longest valid contiguous segment.
        return max_length

    def maxSubarrayLength(self, words: List[int], maxWords: int) -> int:
        """
        Wrapper method that matches a common interview-style naming convention.

        Args:
            words: A list of non-negative integers representing word counts.
            maxWords: Maximum allowed sum for a valid contiguous segment.

        Returns:
            The maximum valid contiguous segment length.

        Time complexity:
            O(n), where n is the length of words.

        Space complexity:
            O(1).
        """
        return self.longest_caption_segment(words, maxWords)


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement.
    words1: List[int] = [3, 1, 2, 1, 1, 4]
    max_words1: int = 5
    result1: int = solution.longest_caption_segment(words1, max_words1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2 from the problem statement.
    words2: List[int] = [5, 2, 2, 6, 1]
    max_words2: int = 7
    result2: int = solution.longest_caption_segment(words2, max_words2)
    print("Example 2 Result:", result2)  # Expected: 2

    # Additional beginner-friendly checks.
    words3: List[int] = [0, 0, 0, 0]
    max_words3: int = 0
    result3: int = solution.longest_caption_segment(words3, max_words3)
    print("All zeros Result:", result3)  # Expected: 4

    words4: List[int] = [10, 20, 30]
    max_words4: int = 5
    result4: int = solution.longest_caption_segment(words4, max_words4)
    print("No valid positive segment Result:", result4)  # Expected: 0

    words5: List[int] = [1, 2, 3, 4]
    max_words5: int = 6
    result5: int = solution.longest_caption_segment(words5, max_words5)
    print("Mixed values Result:", result5)  # Expected: 3