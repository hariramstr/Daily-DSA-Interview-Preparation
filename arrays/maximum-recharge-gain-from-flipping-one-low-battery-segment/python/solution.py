"""
Title: Maximum Recharge Gain from Flipping One Low-Battery Segment

Problem Description:
A mobile device records its battery trend over a day as a binary array `status`,
where `status[i] = 1` means the battery level was healthy during minute `i`,
and `status[i] = 0` means the device was in a low-battery state during that minute.

You are allowed to apply exactly one recovery patch to a single contiguous segment
of the array. The patch flips every value in that chosen segment:
- each `0` becomes `1`
- each `1` becomes `0`

Your task is to return the maximum possible number of healthy minutes after applying
the patch exactly once.

Because the patch must be used exactly once, you must choose a non-empty subarray
to flip, even if doing so decreases the final number of healthy minutes.

A useful way to think about the problem is to measure the gain of flipping each
position:
- flipping a `0` adds one healthy minute, so its gain is +1
- flipping a `1` removes one healthy minute, so its gain is -1

Then the problem becomes:
1. Count how many healthy minutes already exist
2. Find the contiguous segment with the maximum total gain
3. Add that best gain to the original count of healthy minutes

This can be solved in linear time using a variant of Kadane's algorithm.
"""

from typing import List


class Solution:
    def max_healthy_minutes_after_one_flip(self, status: List[int]) -> int:
        """
        Return the maximum number of healthy minutes after flipping exactly one
        non-empty contiguous segment.

        Args:
            status: A binary list where 1 means healthy and 0 means low-battery.

        Returns:
            The maximum possible number of healthy minutes after exactly one flip.

        Time complexity:
            O(n), where n is the length of status, because we scan the array once.

        Space complexity:
            O(1), because we use only a constant amount of extra space.
        """
        # Step 1:
        # Count how many minutes are already healthy before any flip.
        #
        # Why do we need this?
        # The final answer can be viewed as:
        #     original_healthy_count + best_gain_from_one_flipped_segment
        #
        # So we first compute the baseline number of 1s.
        original_healthy_count: int = 0
        for value in status:
            if value == 1:
                original_healthy_count += 1

        # Step 2:
        # We now transform the problem into a "maximum subarray sum" problem.
        #
        # For each position:
        # - If status[i] == 0, flipping it changes 0 -> 1, which is a gain of +1
        # - If status[i] == 1, flipping it changes 1 -> 0, which is a loss of -1
        #
        # So each element contributes either +1 or -1 to the total gain of a chosen segment.
        #
        # We want the contiguous segment with the largest total gain.
        #
        # Important detail:
        # The flip must be used exactly once, and the segment must be non-empty.
        # Therefore, unlike some Kadane implementations, we must not allow the
        # "empty subarray" with sum 0 as the answer.
        #
        # To enforce that, we initialize both current_gain and best_gain using the
        # first transformed value rather than 0.
        first_gain: int = 1 if status[0] == 0 else -1
        current_gain: int = first_gain
        best_gain: int = first_gain

        # Step 3:
        # Run Kadane's algorithm over the transformed gains.
        #
        # current_gain = best gain of a subarray that MUST end at the current index
        # best_gain    = best gain seen anywhere so far
        #
        # At each new position, we decide:
        # - Start a new segment here
        # - Or extend the previous segment
        #
        # This is the classic Kadane recurrence:
        # current_gain = max(gain_here, current_gain + gain_here)
        #
        # Why does this work?
        # If the previous running sum is hurting us, it is better to discard it and
        # start fresh at the current position.
        for i in range(1, len(status)):
            gain_here: int = 1 if status[i] == 0 else -1

            # Decide whether extending the previous segment is better than
            # starting a brand-new segment at index i.
            current_gain = max(gain_here, current_gain + gain_here)

            # Update the global best gain if the current segment is the best so far.
            best_gain = max(best_gain, current_gain)

        # Step 4:
        # The final answer is:
        # original number of healthy minutes + best possible gain from one flip
        #
        # This automatically handles all cases:
        # - Mixed 0s and 1s
        # - All 1s: best_gain will be -1, meaning we lose one healthy minute,
        #   which is correct because we must flip something
        # - All 0s: best_gain will be n, meaning we flip the whole array
        return original_healthy_count + best_gain

    def solve(self, status: List[int]) -> int:
        """
        Convenience wrapper that calls the main algorithm.

        Args:
            status: A binary list where 1 means healthy and 0 means low-battery.

        Returns:
            The maximum possible number of healthy minutes after exactly one flip.

        Time complexity:
            O(n), because it delegates to the linear-time main method.

        Space complexity:
            O(1), because it uses constant extra space.
        """
        return self.max_healthy_minutes_after_one_flip(status)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement:
    # status = [1,0,0,1,0,1]
    #
    # Original healthy count = 3
    # Gain array = [-1, +1, +1, -1, +1, -1]
    # Best subarray gain = 2
    # Final answer = 3 + 2 = 5
    sample_1: List[int] = [1, 0, 0, 1, 0, 1]
    result_1: int = solution.solve(sample_1)
    print(f"Input: {sample_1}")
    print(f"Output: {result_1}")
    print("Expected: 5")
    print()

    # Example 2 from the problem statement:
    # status = [1,1,1,1]
    #
    # Original healthy count = 4
    # Gain array = [-1, -1, -1, -1]
    # Best non-empty subarray gain = -1
    # Final answer = 4 + (-1) = 3
    sample_2: List[int] = [1, 1, 1, 1]
    result_2: int = solution.solve(sample_2)
    print(f"Input: {sample_2}")
    print(f"Output: {result_2}")
    print("Expected: 3")
    print()

    # Additional beginner-friendly checks:
    additional_samples: List[List[int]] = [
        [0],
        [1],
        [0, 0, 0],
        [1, 0, 1, 0, 1],
        [0, 1, 1, 0, 0, 1, 0],
    ]

    for sample in additional_samples:
        result: int = solution.solve(sample)
        print(f"Input: {sample} -> Maximum healthy minutes after one flip: {result}")