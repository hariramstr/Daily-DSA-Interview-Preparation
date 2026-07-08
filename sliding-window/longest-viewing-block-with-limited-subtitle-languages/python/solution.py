"""
Title: Longest Viewing Block With Limited Subtitle Languages

Problem Description:
A streaming platform stores the subtitle language used for each minute of a live broadcast
in an array `languages`, where `languages[i]` is a string such as "en", "es", or "fr".

A user wants to watch one continuous block of the broadcast, but they are only comfortable
switching between at most `k` distinct subtitle languages during that block.

Your task is to return the length of the longest contiguous segment of `languages` that
contains at most `k` distinct language codes.

This models a realistic product analytics problem: find the longest uninterrupted viewing
interval that stays within a user's subtitle tolerance. The segment must be contiguous,
and repeated occurrences of the same language do not increase the distinct count.

Constraints:
- 1 <= languages.length <= 200000
- 1 <= languages[i].length <= 10
- languages[i] consists of lowercase English letters
- 1 <= k <= languages.length

Example 1:
Input: languages = ["en","en","es","es","fr","es","es"], k = 2
Output: 4

Example 2:
Input: languages = ["jp","kr","jp","cn","cn","jp","jp"], k = 1
Output: 2
"""

from typing import Dict, List


class Solution:
    def longest_viewing_block(self, languages: List[str], k: int) -> int:
        """
        Compute the length of the longest contiguous segment containing at most
        k distinct subtitle languages.

        Args:
            languages: List of subtitle language codes for each minute.
            k: Maximum number of distinct languages allowed in the chosen block.

        Returns:
            The maximum length of a contiguous valid segment.

        Time Complexity:
            O(n), where n is the length of languages.
            Each element is added to the window once and removed at most once.

        Space Complexity:
            O(k) in the typical sliding-window sense for active distinct items,
            and O(min(n, number of unique languages)) overall for the frequency map.
        """
        # This dictionary stores how many times each language appears
        # inside the CURRENT sliding window.
        #
        # Example:
        # If the current window is ["en", "en", "es"],
        # then counts will be:
        # {
        #     "en": 2,
        #     "es": 1
        # }
        #
        # We use a dictionary because:
        # 1. Language codes are strings, not small integer indexes.
        # 2. We need fast increment/decrement operations.
        # 3. We need to know when a language count drops to zero so that
        #    it is no longer considered part of the current window.
        counts: Dict[str, int] = {}

        # `left` is the left boundary of our sliding window.
        # The right boundary will be controlled by a loop variable `right`.
        #
        # At any moment, the current window is:
        # languages[left:right+1]
        left: int = 0

        # This will store the best (maximum) valid window length found so far.
        max_length: int = 0

        # We expand the window one step at a time by moving `right`
        # from the beginning of the array to the end.
        for right, language in enumerate(languages):
            # STEP 1: Include the new language at position `right`
            # into the current window.
            #
            # If the language is already present in the window,
            # increase its frequency.
            # Otherwise, start its frequency at 1.
            counts[language] = counts.get(language, 0) + 1

            # STEP 2: If the window now contains too many distinct languages,
            # we must shrink it from the left until it becomes valid again.
            #
            # Why `while` and not `if`?
            # Because removing just one element from the left may still leave
            # more than k distinct languages in the window.
            #
            # `len(counts)` tells us how many DISTINCT languages are currently
            # inside the window, because we remove keys whose count becomes zero.
            while len(counts) > k:
                # Identify the language that is currently at the left edge
                # of the window. This is the language we are about to remove
                # one occurrence of as we shrink the window.
                left_language: str = languages[left]

                # Decrease its count because that element is leaving the window.
                counts[left_language] -= 1

                # If its count becomes zero, that means this language no longer
                # exists anywhere in the current window.
                #
                # It is very important to delete it from the dictionary.
                # Otherwise, `len(counts)` would incorrectly count it as still
                # being a distinct language in the window.
                if counts[left_language] == 0:
                    del counts[left_language]

                # Move the left boundary one step to the right,
                # effectively shrinking the window.
                left += 1

            # STEP 3: At this point, the window is guaranteed to be valid:
            # it contains at most k distinct languages.
            #
            # So we can safely compute its length and compare it against
            # the best answer seen so far.
            current_length: int = right - left + 1
            if current_length > max_length:
                max_length = current_length

        # After processing all positions, `max_length` holds the answer.
        return max_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # languages = ["en","en","es","es","fr","es","es"], k = 2
    # Valid longest length should be 4.
    languages1: List[str] = ["en", "en", "es", "es", "fr", "es", "es"]
    k1: int = 2
    result1: int = solution.longest_viewing_block(languages1, k1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2:
    # languages = ["jp","kr","jp","cn","cn","jp","jp"], k = 1
    # Valid longest length should be 2.
    languages2: List[str] = ["jp", "kr", "jp", "cn", "cn", "jp", "jp"]
    k2: int = 1
    result2: int = solution.longest_viewing_block(languages2, k2)
    print("Example 2 Result:", result2)  # Expected: 2

    # Additional quick sanity checks
    languages3: List[str] = ["en"]
    k3: int = 1
    result3: int = solution.longest_viewing_block(languages3, k3)
    print("Additional Test 1 Result:", result3)  # Expected: 1

    languages4: List[str] = ["en", "es", "fr", "de"]
    k4: int = 4
    result4: int = solution.longest_viewing_block(languages4, k4)
    print("Additional Test 2 Result:", result4)  # Expected: 4