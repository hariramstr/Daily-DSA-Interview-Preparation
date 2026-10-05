"""
Title: Longest Lecture Segment With Limited Slide Repeats

Problem Description:
You are given an array slides where slides[i] is the ID of the slide shown at minute i
during a recorded lecture. Because instructors sometimes revisit the same slide multiple
times, the video platform wants to extract the longest contiguous segment that is still
easy for students to review. A segment is considered review-friendly if no slide ID
appears more than k times inside that segment.

Return the length of the longest contiguous subarray of slides that is review-friendly.

In other words, find the maximum window [l, r] such that for every distinct slide ID in
slides[l...r], its frequency within that window is at most k.

This is a realistic stream-processing problem: the answer must be based on a contiguous
time interval, not on reordering or deleting arbitrary elements outside the chosen interval.

Constraints:
- 1 <= slides.length <= 200000
- 1 <= slides[i] <= 1000000000
- 1 <= k <= slides.length
- The solution should run efficiently for large inputs.

Example 1:
Input: slides = [4, 2, 4, 3, 2, 4, 2, 5], k = 2
Output: 5
Explanation: One optimal segment is [4, 3, 2, 4, 2], which has slide 4 appearing 2 times
and slide 2 appearing 2 times. No slide appears more than 2 times, so the segment length
is 5.

Example 2:
Input: slides = [7, 7, 7, 1, 2, 1, 2, 3], k = 1
Output: 4
Explanation: With k = 1, all slide IDs in the chosen segment must be unique, so the
maximum valid length is 4.
"""

from typing import Dict, List


class Solution:
    def max_review_friendly_segment(self, slides: List[int], k: int) -> int:
        """
        Find the length of the longest contiguous subarray where no value appears more than k times.

        Args:
            slides: List of slide IDs shown over time.
            k: Maximum allowed frequency of any slide ID inside the chosen window.

        Returns:
            The maximum length of a valid contiguous segment.

        Time complexity:
            O(n), where n is the length of slides, because each element is added to and
            removed from the sliding window at most once.

        Space complexity:
            O(m), where m is the number of distinct slide IDs currently tracked in the
            frequency dictionary. In the worst case, O(n).
        """
        # This dictionary stores how many times each slide ID appears
        # inside the current sliding window [left, right].
        #
        # Example:
        # If the current window is [4, 2, 4, 3], then:
        # counts = {4: 2, 2: 1, 3: 1}
        counts: Dict[int, int] = {}

        # 'left' is the start index of our current window.
        # We will expand the window by moving 'right' forward,
        # and shrink it by moving 'left' forward whenever the window becomes invalid.
        left: int = 0

        # This will store the best (maximum) valid window length found so far.
        best: int = 0

        # We iterate 'right' from 0 to len(slides) - 1.
        # At each step, we include slides[right] into the window.
        for right, slide_id in enumerate(slides):
            # Add the new slide to the frequency map.
            # If it was not present before, start from 0.
            counts[slide_id] = counts.get(slide_id, 0) + 1

            # After adding slides[right], the only possible reason the window becomes invalid
            # is that this specific slide_id now appears too many times.
            #
            # Why only this slide?
            # Because before adding it, the window was valid.
            # Adding one element can only increase the count of that one element.
            #
            # So while this slide exceeds the allowed frequency k,
            # we must move 'left' forward to remove elements until the window is valid again.
            while counts[slide_id] > k:
                # The element leaving the window is slides[left].
                left_slide_id: int = slides[left]

                # Decrease its count because it is no longer inside the window.
                counts[left_slide_id] -= 1

                # Move the left boundary rightward by one position.
                left += 1

            # At this point, the window [left, right] is guaranteed to be valid:
            # every slide ID appears at most k times.
            #
            # So we compute its length.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is longer than any previous one.
            if current_length > best:
                best = current_length

        # After processing all positions, 'best' is the maximum valid window length.
        return best

    def maxSubarrayLength(self, slides: List[int], k: int) -> int:
        """
        Compatibility wrapper using a common interview-style method name.

        Args:
            slides: List of slide IDs shown over time.
            k: Maximum allowed frequency of any slide ID inside the chosen window.

        Returns:
            The maximum length of a valid contiguous segment.

        Time complexity:
            O(n), where n is the length of slides.

        Space complexity:
            O(n) in the worst case for the frequency dictionary.
        """
        return self.max_review_friendly_segment(slides, k)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    slides1: List[int] = [4, 2, 4, 3, 2, 4, 2, 5]
    k1: int = 2
    result1: int = solution.max_review_friendly_segment(slides1, k1)
    print(result1)  # Expected: 5

    # Example 2
    slides2: List[int] = [7, 7, 7, 1, 2, 1, 2, 3]
    k2: int = 1
    result2: int = solution.max_review_friendly_segment(slides2, k2)
    print(result2)  # Expected: 4

    # Additional quick checks
    slides3: List[int] = [1, 1, 1, 1]
    k3: int = 2
    result3: int = solution.max_review_friendly_segment(slides3, k3)
    print(result3)  # Expected: 2

    slides4: List[int] = [1, 2, 3, 4, 5]
    k4: int = 1
    result4: int = solution.max_review_friendly_segment(slides4, k4)
    print(result4)  # Expected: 5