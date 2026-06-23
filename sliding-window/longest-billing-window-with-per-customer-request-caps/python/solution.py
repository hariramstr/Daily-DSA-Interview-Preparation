"""
Title: Longest Billing Window With Per-Customer Request Caps

Problem Description:
A cloud platform records API requests in chronological order. Each request is labeled
with the customer ID that generated it. For billing analysis, you are given an array
requests where requests[i] is the customer ID of the i-th request, and an integer
limit. A contiguous time window is considered billable if no customer appears more
than limit times inside that window.

Your task is to return the length of the longest billable contiguous window.

This is harder than a standard frequency-limited window because the input size is
large, customer IDs may be large integers, and the optimal solution is expected to
run in linear time using a sliding window with dynamic frequency tracking. Any
solution that repeatedly recomputes frequencies for candidate windows will be too slow.

Formally, find the maximum value of (right - left + 1) over all pairs
0 <= left <= right < n such that for every customer ID x, the number of indices i
in [left, right] with requests[i] = x is at most limit.

Constraints:
- 1 <= requests.length <= 200000
- 1 <= requests[i] <= 1000000000
- 1 <= limit <= requests.length
- requests is already ordered by time

Example 1:
Input: requests = [4, 7, 4, 2, 7, 4, 7], limit = 2
Output: 5
Explanation: The longest valid window is [4, 2, 7, 4, 7], where customer 4 appears
2 times and customer 7 appears 2 times.

Example 2:
Input: requests = [9, 9, 9, 3, 3, 8, 8, 8, 8], limit = 2
Output: 4
Explanation: One optimal window is [9, 3, 3, 8]. Any longer window would cause
either customer 9 or customer 8 to appear more than 2 times.

Return only the maximum valid window length.
"""

from typing import Dict, List


class Solution:
    def longest_billable_window(self, requests: List[int], limit: int) -> int:
        """
        Find the length of the longest contiguous window where no customer ID
        appears more than `limit` times.

        Args:
            requests: Chronological list of customer IDs for each request.
            limit: Maximum allowed frequency of any single customer inside a valid window.

        Returns:
            The maximum length of a valid contiguous window.

        Time Complexity:
            O(n), where n is the length of requests.
            Each element is added to the window once and removed from the window once.

        Space Complexity:
            O(k), where k is the number of distinct customer IDs currently tracked
            in the frequency dictionary. In the worst case, this can be O(n).
        """
        # This dictionary stores how many times each customer ID appears
        # in the current sliding window [left, right].
        #
        # Why a dictionary?
        # - Customer IDs can be very large (up to 1,000,000,000), so using a list
        #   indexed by customer ID would waste huge amounts of memory.
        # - A dictionary only stores IDs that actually appear.
        freq: Dict[int, int] = {}

        # `left` marks the start of the current window.
        # We will expand the window by moving `right` forward one step at a time.
        left: int = 0

        # This will store the best (maximum) valid window length found so far.
        best: int = 0

        # We iterate `right` from 0 to the end of the array.
        # At each step, we include requests[right] into the current window.
        for right, customer_id in enumerate(requests):
            # Add the current customer to the frequency map because the window
            # is being expanded to include index `right`.
            freq[customer_id] = freq.get(customer_id, 0) + 1

            # After adding this customer, the window may become invalid.
            # The only frequency that could have just exceeded the limit is
            # the frequency of `customer_id`, because that is the only count
            # we changed in this iteration.
            #
            # So while this specific customer appears too many times, we shrink
            # the window from the left until the constraint is restored.
            while freq[customer_id] > limit:
                # Identify which customer is leaving the window.
                left_customer: int = requests[left]

                # Decrease its count because index `left` will no longer be
                # part of the current window after we move `left` forward.
                freq[left_customer] -= 1

                # Move the left boundary rightward to shrink the window.
                left += 1

            # At this point, the window [left, right] is guaranteed valid:
            # no customer appears more than `limit` times.
            #
            # Compute the current window length.
            current_length: int = right - left + 1

            # Update the best answer if this valid window is larger than any
            # valid window seen before.
            if current_length > best:
                best = current_length

        # After processing all positions, `best` is the maximum valid window length.
        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    requests_1: List[int] = [4, 7, 4, 2, 7, 4, 7]
    limit_1: int = 2
    result_1: int = solution.longest_billable_window(requests_1, limit_1)
    print(result_1)  # Expected: 5

    # Example 2
    requests_2: List[int] = [9, 9, 9, 3, 3, 8, 8, 8, 8]
    limit_2: int = 2
    result_2: int = solution.longest_billable_window(requests_2, limit_2)
    print(result_2)  # Expected: 4

    # Additional quick sanity checks
    requests_3: List[int] = [1, 2, 3, 4]
    limit_3: int = 1
    result_3: int = solution.longest_billable_window(requests_3, limit_3)
    print(result_3)  # Expected: 4

    requests_4: List[int] = [5, 5, 5, 5]
    limit_4: int = 2
    result_4: int = solution.longest_billable_window(requests_4, limit_4)
    print(result_4)  # Expected: 2