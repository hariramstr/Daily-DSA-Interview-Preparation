"""
Title: Minimum Server Version to Pass All Client Requirements

Problem Description:
A company is rolling out a backend API in numbered versions from 1 to m.
There are n client applications, and each client i can successfully connect
only if the server version is at least requirements[i].

You are also given an integer k, the minimum number of clients that must be
supported immediately after launch.

Your task is to find the smallest server version v such that at least k clients
can connect when version v is deployed.

Formally, return the minimum integer v where the count of values in requirements
that are less than or equal to v is at least k.

If it is impossible because k > n, return -1.

Notes:
- The input array is not guaranteed to be sorted.
- If the minimum required version exceeds m, then no valid deployment inside
  the allowed range exists, and you should return -1.

Examples:
1)
requirements = [5, 2, 8, 4, 4], m = 10, k = 3
Output: 4

2)
requirements = [7, 9, 12], m = 10, k = 2
Output: 9
"""

from bisect import bisect_right
from typing import List


class Solution:
    def min_server_version(self, requirements: List[int], m: int, k: int) -> int:
        """
        Find the minimum server version that supports at least k clients.

        The idea is:
        1. If k is larger than the number of clients, it is impossible.
        2. Sort the requirements so we can efficiently count how many clients
           are supported by any chosen version.
        3. Binary search on the server version range [1, m].
        4. For each candidate version mid, count how many requirements are
           <= mid using binary search (bisect_right).
        5. If at least k clients are supported, try a smaller version.
           Otherwise, try a larger version.

        Args:
            requirements: A list where requirements[i] is the minimum server
                version needed by client i.
            m: The maximum allowed server version.
            k: The minimum number of clients that must be supported.

        Returns:
            The smallest valid server version in [1, m] that supports at least
            k clients, or -1 if no such version exists.

        Time complexity:
            O(n log n + log m * log n)
            - Sorting costs O(n log n)
            - Each binary search step on version uses bisect_right in O(log n)
            - There are O(log m) version-search steps

        Space complexity:
            O(1) extra space beyond the sort behavior of Python's sorting
            implementation, not counting the input list itself.
        """
        # If we need to support more clients than actually exist,
        # the task is impossible by definition.
        if k > len(requirements):
            return -1

        # Sorting is the key preparation step.
        #
        # Why sort?
        # After sorting, all clients with requirement <= v will appear in a
        # contiguous prefix of the array. That means we can quickly count how
        # many clients are supported by version v using binary search.
        #
        # Example:
        # requirements = [5, 2, 8, 4, 4]
        # sorted       = [2, 4, 4, 5, 8]
        #
        # For version v = 4, the supported clients are exactly the first 3
        # values: [2, 4, 4].
        requirements.sort()

        # Before doing a full binary search on [1, m], we can quickly detect
        # one impossible case:
        #
        # The minimum version needed to support at least k clients is exactly
        # the k-th smallest requirement (1-indexed), which is located at
        # index k - 1 after sorting.
        #
        # If that value is greater than m, then even the maximum allowed server
        # version cannot support k clients, so the answer must be -1.
        kth_smallest_requirement = requirements[k - 1]
        if kth_smallest_requirement > m:
            return -1

        # We now binary search for the smallest version in the allowed range
        # [1, m] that supports at least k clients.
        #
        # Invariant:
        # - Any version that supports at least k clients is a "valid" candidate.
        # - We want the smallest such valid candidate.
        left = 1
        right = m
        answer = -1

        while left <= right:
            # Standard midpoint calculation.
            mid = left + (right - left) // 2

            # Count how many clients can connect if we deploy version = mid.
            #
            # bisect_right(sorted_list, mid) returns the insertion position to
            # place mid on the right side of existing equal values.
            #
            # That insertion index is exactly the number of elements <= mid.
            #
            # Example:
            # sorted requirements = [2, 4, 4, 5, 8]
            # mid = 4
            # bisect_right(...) = 3
            # meaning 3 clients have requirement <= 4
            supported_clients = bisect_right(requirements, mid)

            # If this version supports enough clients, it is a valid answer.
            # But we still want the minimum valid version, so we continue
            # searching on the left half.
            if supported_clients >= k:
                answer = mid
                right = mid - 1
            else:
                # Otherwise, this version is too small and does not support
                # enough clients, so we must search larger versions.
                left = mid + 1

        return answer

    def min_server_version_direct(self, requirements: List[int], m: int, k: int) -> int:
        """
        Find the minimum server version using the direct observation that the
        answer is the k-th smallest requirement, if it is within [1, m].

        This method is simpler than binary searching on the answer:
        after sorting, the smallest version that supports at least k clients
        is exactly the k-th smallest requirement.

        Args:
            requirements: A list where requirements[i] is the minimum server
                version needed by client i.
            m: The maximum allowed server version.
            k: The minimum number of clients that must be supported.

        Returns:
            The smallest valid server version, or -1 if impossible.

        Time complexity:
            O(n log n)

        Space complexity:
            O(1) extra space beyond the sort behavior of Python's sorting
            implementation, not counting the input list itself.
        """
        # Again, if we need more clients than exist, impossible.
        if k > len(requirements):
            return -1

        # Sort so the k-th smallest requirement is easy to access.
        requirements.sort()

        # The smallest version that supports at least k clients is the k-th
        # smallest requirement.
        answer = requirements[k - 1]

        # But the deployed version must be within the allowed range [1, m].
        if answer > m:
            return -1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    requirements_1 = [5, 2, 8, 4, 4]
    m_1 = 10
    k_1 = 3
    result_1 = solution.min_server_version(requirements_1[:], m_1, k_1)
    print("Example 1 Result:", result_1)  # Expected: 4

    # Example 2
    requirements_2 = [7, 9, 12]
    m_2 = 10
    k_2 = 2
    result_2 = solution.min_server_version(requirements_2[:], m_2, k_2)
    print("Example 2 Result:", result_2)  # Expected: 9

    # Additional checks
    requirements_3 = [11, 12, 13]
    m_3 = 10
    k_3 = 1
    result_3 = solution.min_server_version(requirements_3[:], m_3, k_3)
    print("Additional Check 1:", result_3)  # Expected: -1

    requirements_4 = [3, 1, 6, 2]
    m_4 = 6
    k_4 = 4
    result_4 = solution.min_server_version(requirements_4[:], m_4, k_4)
    print("Additional Check 2:", result_4)  # Expected: 6

    requirements_5 = [3, 1, 6, 2]
    m_5 = 5
    k_5 = 5
    result_5 = solution.min_server_version(requirements_5[:], m_5, k_5)
    print("Additional Check 3:", result_5)  # Expected: -1