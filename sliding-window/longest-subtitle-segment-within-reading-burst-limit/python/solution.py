"""
Title: Longest Subtitle Segment Within Reading Burst Limit

Problem Description:
You are given an array `words`, where `words[i]` is the number of words shown in the
`i`-th subtitle frame of a video. A viewer can comfortably follow a contiguous segment
only if:
1. The total number of words in that segment does not exceed `maxWords`
2. The segment contains at most `k` dense frames

A frame is considered dense if `words[i] >= denseThreshold`.

Return the length of the longest contiguous segment of subtitle frames that satisfies
both conditions.

This models a real video playback system where subtitle readability depends on both
the overall reading load and the number of especially text-heavy frames. The answer
should be the maximum number of consecutive frames the viewer can watch without
exceeding the reading burst limit.

Constraints:
- 1 <= words.length <= 200000
- 0 <= words[i] <= 100000
- 0 <= maxWords <= 10^14
- 0 <= k <= words.length
- 1 <= denseThreshold <= 100000
"""

from typing import List


class Solution:
    def longest_subtitle_segment(
        self,
        words: List[int],
        maxWords: int,
        k: int,
        denseThreshold: int,
    ) -> int:
        """
        Find the maximum length of a contiguous subtitle segment such that:
        - the sum of words in the segment is at most maxWords
        - the number of dense frames in the segment is at most k

        Args:
            words: List of word counts for each subtitle frame.
            maxWords: Maximum allowed total words in a valid segment.
            k: Maximum allowed number of dense frames in a valid segment.
            denseThreshold: A frame is dense if its word count is at least this value.

        Returns:
            The length of the longest valid contiguous segment.

        Time complexity:
            O(n), where n is the length of words.
            Each element is added to the window once and removed at most once.

        Space complexity:
            O(1), excluding the input array.
        """
        # We use the classic sliding window / two-pointer technique because:
        # - We need a contiguous segment.
        # - All values in `words` are non-negative.
        # - When the current window becomes invalid, moving the left pointer to the right
        #   can only reduce the total sum and possibly reduce the dense-frame count.
        #
        # This makes it possible to maintain a valid window in linear time.

        # `left` marks the start index of the current window.
        left: int = 0

        # `current_sum` stores the total number of words inside the current window.
        current_sum: int = 0

        # `dense_count` stores how many frames in the current window are dense.
        dense_count: int = 0

        # `best` stores the maximum valid window length found so far.
        best: int = 0

        # Expand the window one frame at a time using `right`.
        for right, value in enumerate(words):
            # Step 1: Include the new frame at index `right` into the window.
            current_sum += value

            # If this frame is dense, increase the dense-frame counter.
            if value >= denseThreshold:
                dense_count += 1

            # Step 2: If the window violates either rule, shrink it from the left
            # until it becomes valid again.
            #
            # The window is invalid if:
            # - total words exceed maxWords, OR
            # - dense frame count exceeds k
            #
            # We use a while-loop because removing just one element may not be enough.
            while current_sum > maxWords or dense_count > k:
                # Before moving `left`, remove the contribution of words[left]
                # from our tracked window state.
                left_value: int = words[left]
                current_sum -= left_value

                # If the frame leaving the window was dense, update dense_count.
                if left_value >= denseThreshold:
                    dense_count -= 1

                # Actually shrink the window by moving the left boundary rightward.
                left += 1

            # Step 3: At this point, the window [left, right] is guaranteed valid.
            # Compute its length and update the best answer if this window is longer.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        # After scanning all possible right endpoints, `best` is the answer.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    words1: List[int] = [4, 9, 3, 8, 2, 7]
    max_words1: int = 20
    k1: int = 2
    dense_threshold1: int = 7
    result1: int = solution.longest_subtitle_segment(
        words1, max_words1, k1, dense_threshold1
    )
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    words2: List[int] = [6, 1, 7, 2, 9, 1, 1]
    max_words2: int = 10
    k2: int = 1
    dense_threshold2: int = 6
    result2: int = solution.longest_subtitle_segment(
        words2, max_words2, k2, dense_threshold2
    )
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick checks
    words3: List[int] = [0, 0, 0]
    max_words3: int = 0
    k3: int = 0
    dense_threshold3: int = 1
    result3: int = solution.longest_subtitle_segment(
        words3, max_words3, k3, dense_threshold3
    )
    print("Additional Check 1 Result:", result3)  # Expected: 3

    words4: List[int] = [10, 10, 10]
    max_words4: int = 5
    k4: int = 3
    dense_threshold4: int = 10
    result4: int = solution.longest_subtitle_segment(
        words4, max_words4, k4, dense_threshold4
    )
    print("Additional Check 2 Result:", result4)  # Expected: 0