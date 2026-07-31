"""
Title: Longest Reading List Within Page Limit

Problem Description:
You are given an array `pages` where `pages[i]` is the number of pages in the
i-th article of an online reading list. A user wants to read a consecutive group
of articles in one session, but they can read at most `maxPages` pages total.

Your task is to return the maximum number of consecutive articles the user can
read without the sum of pages in that group exceeding `maxPages`.

In other words, find the length of the longest contiguous subarray whose sum is
less than or equal to `maxPages`.

This problem is a classic sliding window problem because:
- All values are non-negative
- When the window sum becomes too large, moving the left pointer forward can only
  decrease or keep the same total sum
- This allows us to process the array in linear time

Constraints:
- 1 <= pages.length <= 100000
- 0 <= pages[i] <= 10000
- 0 <= maxPages <= 1000000000
- All page counts are non-negative integers

Example 1:
Input: pages = [4, 2, 1, 7, 3, 2], maxPages = 8
Output: 3
Explanation:
- [4, 2, 1] has sum 7, so length 3 is valid
- Any window of length 4 exceeds 8
- Therefore, the answer is 3

Example 2:
Input: pages = [1, 1, 1, 1, 1], maxPages = 3
Output: 3
Explanation:
- Any 3 consecutive articles sum to 3, which is valid
- Any 4 consecutive articles sum to 4, which exceeds the limit
- Therefore, the answer is 3
"""

from typing import List


class Solution:
    def longest_reading_list(self, pages: List[int], maxPages: int) -> int:
        """
        Find the maximum number of consecutive articles whose total pages
        do not exceed maxPages.

        Args:
            pages: A list of non-negative integers where each value represents
                the number of pages in an article.
            maxPages: The maximum total number of pages the user can read in
                one session.

        Returns:
            The length of the longest contiguous subarray with sum less than
            or equal to maxPages.

        Time Complexity:
            O(n), where n is the length of pages, because each element is added
            to the window once and removed from the window at most once.

        Space Complexity:
            O(1), because we use only a constant amount of extra space.
        """
        # The left pointer marks the beginning of the current sliding window.
        # We will expand the window by moving the right pointer one step at a time.
        left: int = 0

        # This variable stores the total number of pages currently inside the window.
        # At any moment, the current window is pages[left:right+1].
        current_sum: int = 0

        # This keeps track of the best (largest) valid window length found so far.
        max_length: int = 0

        # We iterate with the right pointer through every article.
        # Each step means: "try including this article in the current reading session."
        for right in range(len(pages)):
            # Add the new article's page count to the current window sum because
            # the window is expanding to include pages[right].
            current_sum += pages[right]

            # If the total pages now exceed the allowed limit, the window is invalid.
            # Because all values are non-negative, the only way to make the window
            # valid again is to move the left pointer to the right and remove articles
            # from the beginning of the window.
            #
            # We use a while loop instead of an if statement because removing just one
            # article may still leave the sum too large. We keep shrinking until the
            # window becomes valid again.
            while current_sum > maxPages and left <= right:
                # Remove the article at the left edge from the current sum because
                # it is no longer part of the window after we move left forward.
                current_sum -= pages[left]

                # Move the left boundary one step to the right.
                left += 1

            # At this point, the window pages[left:right+1] is guaranteed to be valid:
            # its sum is <= maxPages.
            #
            # The current window length is:
            #   right - left + 1
            #
            # We compare it with the best answer seen so far and keep the larger one.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # After scanning the entire array, max_length holds the length of the
        # longest valid consecutive group of articles.
        return max_length

    def maxConsecutiveArticles(self, pages: List[int], maxPages: int) -> int:
        """
        Wrapper method matching a common interview-style naming convention.

        Args:
            pages: A list of non-negative integers representing article page counts.
            maxPages: The maximum allowed total pages.

        Returns:
            The maximum number of consecutive articles that can be read.

        Time Complexity:
            O(n), where n is the length of pages.

        Space Complexity:
            O(1).
        """
        return self.longest_reading_list(pages, maxPages)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    pages1: List[int] = [4, 2, 1, 7, 3, 2]
    max_pages1: int = 8
    result1: int = solution.longest_reading_list(pages1, max_pages1)
    print("Example 1:")
    print("pages =", pages1)
    print("maxPages =", max_pages1)
    print("Output =", result1)
    print("Expected = 3")
    print()

    # Example 2 from the problem statement
    pages2: List[int] = [1, 1, 1, 1, 1]
    max_pages2: int = 3
    result2: int = solution.longest_reading_list(pages2, max_pages2)
    print("Example 2:")
    print("pages =", pages2)
    print("maxPages =", max_pages2)
    print("Output =", result2)
    print("Expected = 3")
    print()

    # Additional beginner-friendly test cases
    pages3: List[int] = [0, 0, 0, 0]
    max_pages3: int = 0
    result3: int = solution.longest_reading_list(pages3, max_pages3)
    print("Additional Test 1:")
    print("pages =", pages3)
    print("maxPages =", max_pages3)
    print("Output =", result3)
    print("Expected = 4")
    print()

    pages4: List[int] = [10, 20, 30]
    max_pages4: int = 15
    result4: int = solution.longest_reading_list(pages4, max_pages4)
    print("Additional Test 2:")
    print("pages =", pages4)
    print("maxPages =", max_pages4)
    print("Output =", result4)
    print("Expected = 1")
    print()

    pages5: List[int] = [5, 1, 2, 1, 1]
    max_pages5: int = 5
    result5: int = solution.longest_reading_list(pages5, max_pages5)
    print("Additional Test 3:")
    print("pages =", pages5)
    print("maxPages =", max_pages5)
    print("Output =", result5)
    print("Expected = 4")