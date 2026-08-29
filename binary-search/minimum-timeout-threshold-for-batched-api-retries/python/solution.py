"""
Title: Minimum Timeout Threshold for Batched API Retries

Difficulty: Medium
Topic: Binary Search

Problem Description:
A backend service needs to process a sequence of API calls in the given order.
The i-th call takes requestTimes[i] milliseconds if executed normally.
To avoid excessive retry overhead, the service groups consecutive calls into batches.

For any batch, the total time of all calls inside that batch must not exceed a
chosen timeout threshold T. If adding the next call would make the batch total
exceed T, the current batch is closed and a new batch starts with that call.

You are given an array requestTimes and an integer maxBatches. Find the minimum
integer timeout threshold T such that all API calls can be processed using at
most maxBatches batches.

You must preserve the original order of calls, and every call must belong to
exactly one batch. The threshold T must be at least as large as the longest
single request time, otherwise that call could never fit into any batch.

Return the smallest possible T.

Constraints:
- 1 <= requestTimes.length <= 100000
- 1 <= requestTimes[i] <= 1000000000
- 1 <= maxBatches <= requestTimes.length
- The answer fits in a 64-bit signed integer.

Example 1:
Input: requestTimes = [7, 2, 5, 10, 8], maxBatches = 2
Output: 18

Explanation:
With T = 18, the calls can be grouped as [7, 2, 5] and [10, 8].
Using T = 17 would require 3 batches: [7, 2, 5], [10], [8].
So 18 is the minimum feasible threshold.

Example 2:
Input: requestTimes = [4, 4, 4, 4], maxBatches = 3
Output: 8

Explanation:
With T = 8, one valid grouping is [4, 4], [4], [4], which uses 3 batches.
With T = 7, each batch can contain only one call, so 4 batches are required.
Therefore the minimum threshold is 8.
"""

from typing import List


class Solution:
    def _can_process_with_threshold(self, request_times: List[int], max_batches: int, threshold: int) -> bool:
        """
        Check whether all requests can be processed using at most max_batches
        when each batch sum must be <= threshold.

        Args:
            request_times: List of API request durations in original order.
            max_batches: Maximum number of allowed batches.
            threshold: Candidate timeout threshold to test.

        Returns:
            True if the requests can be split into at most max_batches batches,
            otherwise False.

        Time complexity:
            O(n), where n is the number of requests.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We start with one batch because if there is at least one request,
        # we need at least one batch to hold it.
        batches_used: int = 1

        # This variable stores the running total of the current batch.
        current_batch_sum: int = 0

        # We scan requests from left to right because the original order
        # must be preserved. We are not allowed to reorder requests.
        for time in request_times:
            # If a single request is larger than the threshold, then this
            # threshold is impossible immediately, because that request
            # cannot fit into any batch at all.
            if time > threshold:
                return False

            # If adding this request to the current batch stays within the
            # threshold, we greedily keep it in the current batch.
            #
            # Why greedy works:
            # - We want to minimize the number of batches used for a given threshold.
            # - Packing as many consecutive requests as possible into the current
            #   batch is always optimal for that goal.
            # - Starting a new batch earlier than necessary could only increase
            #   the number of batches, never decrease it.
            if current_batch_sum + time <= threshold:
                current_batch_sum += time
            else:
                # Otherwise, adding this request would exceed the threshold.
                # So we must close the current batch and start a new one.
                batches_used += 1
                current_batch_sum = time

                # Small optimization:
                # If we already used more than max_batches, there is no need
                # to continue scanning. This threshold is not feasible.
                if batches_used > max_batches:
                    return False

        # If we finish processing all requests without exceeding max_batches,
        # then this threshold works.
        return True

    def minimum_timeout_threshold(self, requestTimes: List[int], maxBatches: int) -> int:
        """
        Find the minimum integer timeout threshold that allows processing all
        requests in order using at most maxBatches batches.

        Args:
            requestTimes: List of API request durations.
            maxBatches: Maximum number of allowed batches.

        Returns:
            The smallest feasible timeout threshold.

        Time complexity:
            O(n * log(S)), where n is the number of requests and
            S is the search range between max(requestTimes) and sum(requestTimes).

        Space complexity:
            O(1), excluding input storage.
        """
        # The smallest possible threshold must be at least the largest single
        # request time. Otherwise, that request could never fit into any batch.
        left: int = max(requestTimes)

        # The largest possible threshold is the sum of all request times.
        # With that threshold, all requests can fit into one single batch.
        right: int = sum(requestTimes)

        # We will binary search for the smallest feasible threshold.
        #
        # Why binary search is valid:
        # - If a threshold T works, then any threshold larger than T also works.
        # - If a threshold T does not work, then any threshold smaller than T
        #   also does not work.
        # This monotonic behavior is exactly what binary search needs.
        while left < right:
            # Middle candidate threshold.
            mid: int = left + (right - left) // 2

            # Check whether this candidate threshold is sufficient.
            if self._can_process_with_threshold(requestTimes, maxBatches, mid):
                # If mid works, it could be the answer, but maybe there is a
                # smaller feasible threshold. So we keep searching on the left side,
                # including mid itself.
                right = mid
            else:
                # If mid does not work, then every threshold <= mid also does not work.
                # So we must search strictly to the right of mid.
                left = mid + 1

        # At the end of binary search, left == right and points to the smallest
        # feasible threshold.
        return left


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # requestTimes = [7, 2, 5, 10, 8], maxBatches = 2
    # Expected output: 18
    #
    # Quick trace:
    # - Threshold 18:
    #   Batch 1: 7 + 2 + 5 = 14
    #   Next 10 would make 24 > 18, so start Batch 2
    #   Batch 2: 10 + 8 = 18
    #   Total batches = 2 -> feasible
    #
    # - Threshold 17:
    #   Batch 1: 7 + 2 + 5 = 14
    #   Next 10 would exceed -> Batch 2: 10
    #   Next 8 would exceed 10 + 8 = 18 > 17 -> Batch 3: 8
    #   Total batches = 3 -> not feasible
    #
    # Therefore answer is 18.
    request_times_1: List[int] = [7, 2, 5, 10, 8]
    max_batches_1: int = 2
    result_1: int = solution.minimum_timeout_threshold(request_times_1, max_batches_1)
    print("Example 1 Result:", result_1)

    # Example 2:
    # requestTimes = [4, 4, 4, 4], maxBatches = 3
    # Expected output: 8
    #
    # Quick trace:
    # - Threshold 8:
    #   Batch 1: 4 + 4 = 8
    #   Batch 2: 4
    #   Batch 3: 4
    #   Total batches = 3 -> feasible
    #
    # - Threshold 7:
    #   Each batch can only hold one 4, because 4 + 4 = 8 > 7
    #   So we need 4 batches -> not feasible
    #
    # Therefore answer is 8.
    request_times_2: List[int] = [4, 4, 4, 4]
    max_batches_2: int = 3
    result_2: int = solution.minimum_timeout_threshold(request_times_2, max_batches_2)
    print("Example 2 Result:", result_2)