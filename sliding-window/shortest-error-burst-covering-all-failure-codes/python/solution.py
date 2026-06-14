"""
Title: Shortest Error Burst Covering All Failure Codes

Problem Description:
You are given a chronological stream of application error reports represented by an
integer array reports, where reports[i] is the failure code produced at time i.
You are also given an integer array required containing the set of distinct failure
codes that an incident investigation must observe at least once. In addition, some
failure codes may appear many times in the stream, and the same required code may
be scattered far apart.

Your task is to find the length of the shortest contiguous subarray of reports that
contains every code in required at least once and also contains at least k total
occurrences of critical codes, where critical is another integer array of distinct
codes. A report whose code belongs to critical contributes 1 toward this count even
if that code is not in required. If no such subarray exists, return -1.

Formally, find the minimum length of a window reports[l..r] such that:
1. Every value in required appears at least once in reports[l..r].
2. The number of indices j in [l, r] with reports[j] in critical is at least k.

The arrays required and critical may overlap partially, completely, or not at all.
All codes are positive integers.
"""

from typing import Dict, List, Set


class Solution:
    def shortest_error_burst(
        self,
        reports: List[int],
        required: List[int],
        critical: List[int],
        k: int,
    ) -> int:
        """
        Find the minimum length contiguous subarray that contains all required codes
        at least once and contains at least k total critical-code occurrences.

        Args:
            reports: Chronological stream of failure codes.
            required: Distinct codes that must each appear at least once in the window.
            critical: Distinct codes whose occurrences count toward the critical total.
            k: Minimum number of critical occurrences required in the window.

        Returns:
            The length of the shortest valid subarray, or -1 if no valid subarray exists.

        Time complexity:
            O(n), where n is len(reports), because each pointer moves at most n times.

        Space complexity:
            O(r + c), where r is len(required) and c is len(critical), due to sets/maps.
        """
        # Convert the required and critical arrays into sets for O(1) average-time membership checks.
        # This is important because we will inspect each report value many times while sliding the window.
        required_set: Set[int] = set(required)
        critical_set: Set[int] = set(critical)

        # If there are no required codes, then the "contains every required code" condition
        # is automatically satisfied by any window. The problem constraints do not explicitly
        # say required can be empty, but handling it makes the solution more robust.
        #
        # In that case, we only need the shortest window with at least k critical occurrences.
        # The same sliding-window logic below already handles that correctly because:
        # - len(required_set) would be 0
        # - matched_required would start at 0
        # - so the required condition is immediately satisfied
        #
        # Therefore, no special return is needed here.

        # This dictionary stores how many times each required code appears inside the current window.
        # We only track counts for required values because non-required values do not affect the
        # "all required present" condition.
        required_counts: Dict[int, int] = {}

        # Number of distinct required codes currently present in the window with count >= 1.
        # When this equals len(required_set), the window satisfies condition (1).
        matched_required: int = 0

        # Number of report positions in the current window whose code belongs to critical_set.
        # This counts occurrences, not distinct critical values.
        critical_occurrences: int = 0

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far. Start with infinity so any valid window is smaller.
        best_length: int = float("inf")

        # Expand the window by moving the right boundary one step at a time.
        for right, value in enumerate(reports):
            # Step 1: Include reports[right] into the current window.

            # If this value is one of the required codes, update its count.
            if value in required_set:
                previous_count: int = required_counts.get(value, 0)
                required_counts[value] = previous_count + 1

                # If the count changed from 0 to 1, this required code is now newly satisfied.
                if previous_count == 0:
                    matched_required += 1

            # If this value is critical, it contributes one occurrence toward the k requirement.
            if value in critical_set:
                critical_occurrences += 1

            # Step 2: While the current window is valid, try to shrink it from the left.
            #
            # This is the heart of the sliding-window technique:
            # - We expand right until the window becomes valid.
            # - Then we shrink left as much as possible while preserving validity.
            # - Every time the window is valid, we record its length.
            #
            # This guarantees we find the shortest valid window ending at each right index,
            # and overall the shortest valid window in the entire array.
            while matched_required == len(required_set) and critical_occurrences >= k:
                # The current window reports[left..right] satisfies both conditions.
                current_length: int = right - left + 1
                if current_length < best_length:
                    best_length = current_length

                # Prepare to remove reports[left] from the window and move left forward.
                left_value: int = reports[left]

                # If the outgoing value is required, decrease its tracked count.
                if left_value in required_set:
                    required_counts[left_value] -= 1

                    # If its count becomes 0, the window will no longer contain that required code,
                    # so the required condition will become unsatisfied after this removal.
                    if required_counts[left_value] == 0:
                        matched_required -= 1

                # If the outgoing value is critical, removing it decreases the critical occurrence total.
                if left_value in critical_set:
                    critical_occurrences -= 1

                # Actually shrink the window from the left.
                left += 1

        # If best_length was never updated, no valid window exists.
        return -1 if best_length == float("inf") else best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    reports_1 = [7, 4, 9, 4, 2, 8, 9, 2, 5]
    required_1 = [4, 2, 5]
    critical_1 = [9, 8, 5]
    k_1 = 2
    result_1 = solution.shortest_error_burst(reports_1, required_1, critical_1, k_1)
    print(result_1)  # Expected: 6

    # Example 2
    reports_2 = [1, 3, 1, 6, 7, 3, 6]
    required_2 = [1, 6]
    critical_2 = [3]
    k_2 = 2
    result_2 = solution.shortest_error_burst(reports_2, required_2, critical_2, k_2)
    print(result_2)  # Expected: 5

    # Additional quick checks
    reports_3 = [5, 5, 5]
    required_3 = [5]
    critical_3 = [5]
    k_3 = 2
    result_3 = solution.shortest_error_burst(reports_3, required_3, critical_3, k_3)
    print(result_3)  # Expected: 2

    reports_4 = [1, 2, 3]
    required_4 = [4]
    critical_4 = [1, 2]
    k_4 = 1
    result_4 = solution.shortest_error_burst(reports_4, required_4, critical_4, k_4)
    print(result_4)  # Expected: -1