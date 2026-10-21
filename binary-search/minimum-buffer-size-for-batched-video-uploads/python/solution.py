"""
Title: Minimum Buffer Size for Batched Video Uploads

Problem Description:
A media platform needs to upload a sequence of video clips in the given order.
The size of the i-th clip is clips[i] megabytes. The platform has a temporary
upload buffer with capacity B megabytes. Clips must be grouped into consecutive
batches, and the total size of each batch cannot exceed B. Once a batch is
formed, it is uploaded together.

You are also given an integer maxBatches, the maximum number of batches the
platform is allowed to use.

Your task is to find the minimum buffer size B such that all clips can be
uploaded in order using at most maxBatches batches.

This is not a reordering problem: clips must stay in their original order, and
every clip must belong to exactly one batch. A buffer size is considered
feasible if it is possible to partition the array into at most maxBatches
contiguous groups where each group sum is at most B.

Return the smallest feasible buffer size.

Typical approach:
- Check whether a candidate buffer size is feasible in linear time.
- Binary search the answer between max(clips) and sum(clips).
"""

from typing import List


class Solution:
    def can_upload_with_buffer(self, clips: List[int], max_batches: int, buffer_size: int) -> bool:
        """
        Check whether all clips can be uploaded using at most max_batches batches
        when each batch sum must not exceed buffer_size.

        Args:
            clips: List of clip sizes in original order.
            max_batches: Maximum number of allowed contiguous batches.
            buffer_size: Candidate buffer capacity to test.

        Returns:
            True if the clips can be partitioned into at most max_batches
            contiguous groups with each group sum <= buffer_size, otherwise False.

        Time complexity:
            O(n), where n is the number of clips.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We build batches greedily from left to right.
        #
        # Why greedy works here:
        # - For a fixed buffer size, the best way to minimize the number of batches
        #   is to pack as many consecutive clips as possible into the current batch.
        # - If adding the next clip would exceed the buffer, we must start a new batch.
        # - This strategy produces the minimum number of batches needed for this
        #   buffer size.
        #
        # If even this minimum number of batches is greater than max_batches,
        # then the buffer size is not feasible.

        batches_used: int = 1
        current_batch_sum: int = 0

        for clip_size in clips:
            # Safety check:
            # If a single clip is larger than the candidate buffer size,
            # then this buffer can never work because every clip must fit
            # into some batch by itself if necessary.
            if clip_size > buffer_size:
                return False

            # If the current clip fits into the current batch, add it there.
            if current_batch_sum + clip_size <= buffer_size:
                current_batch_sum += clip_size
            else:
                # Otherwise, we must start a new batch beginning with this clip.
                batches_used += 1
                current_batch_sum = clip_size

                # Early exit optimization:
                # As soon as we exceed the allowed number of batches,
                # we already know this buffer size is not feasible.
                if batches_used > max_batches:
                    return False

        # If we finished processing all clips without exceeding max_batches,
        # then this buffer size is feasible.
        return True

    def minimumBufferSize(self, clips: List[int], maxBatches: int) -> int:
        """
        Find the minimum feasible buffer size needed to upload all clips in order
        using at most maxBatches contiguous batches.

        Args:
            clips: List of clip sizes in original order.
            maxBatches: Maximum number of allowed contiguous batches.

        Returns:
            The smallest buffer size such that the clips can be partitioned into
            at most maxBatches contiguous groups and each group sum is at most
            that buffer size.

        Time complexity:
            O(n * log(S)), where:
            - n is the number of clips
            - S is the search range size, approximately sum(clips) - max(clips)

        Space complexity:
            O(1), excluding input storage.
        """
        # Binary search on the answer:
        #
        # We are looking for the smallest feasible buffer size.
        #
        # Important observation:
        # - If a buffer size B works, then any larger buffer size also works.
        # - This "feasible / not feasible" pattern is monotonic.
        # - Monotonicity is exactly what makes binary search applicable.
        #
        # Search boundaries:
        # - Lower bound = max(clips)
        #   Because the buffer must be large enough to hold the largest single clip.
        #
        # - Upper bound = sum(clips)
        #   Because one batch containing all clips is always possible if the buffer
        #   equals the total sum. Since maxBatches >= 1, this is always a valid
        #   upper bound.

        left: int = max(clips)
        right: int = sum(clips)

        # We maintain the invariant that the answer lies somewhere in [left, right].
        #
        # At each step:
        # - Compute mid
        # - If mid is feasible, try smaller values by moving right to mid
        # - If mid is not feasible, we need a larger buffer, so move left to mid + 1
        #
        # We use the "left < right" binary search pattern to find the first feasible value.
        while left < right:
            mid: int = left + (right - left) // 2

            # Test whether this candidate buffer size is enough.
            if self.can_upload_with_buffer(clips, maxBatches, mid):
                # mid works, so the answer could be mid or something smaller.
                right = mid
            else:
                # mid does not work, so the answer must be larger than mid.
                left = mid + 1

        # When left == right, we have found the smallest feasible buffer size.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    clips1: List[int] = [8, 3, 5, 7, 2]
    max_batches1: int = 3
    result1: int = solution.minimumBufferSize(clips1, max_batches1)
    print("Example 1:")
    print(f"clips = {clips1}, maxBatches = {max_batches1}")
    print(f"Minimum buffer size = {result1}")
    print("Expected = 10")
    print()

    # Example 2
    clips2: List[int] = [4, 4, 4, 4]
    max_batches2: int = 2
    result2: int = solution.minimumBufferSize(clips2, max_batches2)
    print("Example 2:")
    print(f"clips = {clips2}, maxBatches = {max_batches2}")
    print(f"Minimum buffer size = {result2}")
    print("Expected = 8")
    print()

    # Additional quick checks for beginners to see behavior on edge-style inputs.

    # If only one batch is allowed, the buffer must hold everything.
    clips3: List[int] = [1, 2, 3, 4]
    max_batches3: int = 1
    result3: int = solution.minimumBufferSize(clips3, max_batches3)
    print("Additional Test 1:")
    print(f"clips = {clips3}, maxBatches = {max_batches3}")
    print(f"Minimum buffer size = {result3}")
    print("Expected = 10")
    print()

    # If as many batches as clips are allowed, the answer is the largest single clip.
    clips4: List[int] = [9, 1, 7, 3]
    max_batches4: int = 4
    result4: int = solution.minimumBufferSize(clips4, max_batches4)
    print("Additional Test 2:")
    print(f"clips = {clips4}, maxBatches = {max_batches4}")
    print(f"Minimum buffer size = {result4}")
    print("Expected = 9")