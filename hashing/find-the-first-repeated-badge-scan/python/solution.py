"""
Title: Find the First Repeated Badge Scan

Problem Description:
You are given a list of employee badge scan IDs in the order they were recorded at a
building entrance. Each scan ID is a string consisting of letters and digits. Some
employees may scan multiple times because they forgot an item, re-entered the building,
or accidentally scanned twice.

Your task is to return the first scan ID that appears more than once when reading the
list from left to right. In other words, find the earliest duplicate event in the stream.
If no scan ID is repeated, return an empty string.

This problem is about efficiently detecting duplicates while preserving the original
arrival order. A simple nested-loop solution works for small inputs, but interviewers
expect a faster approach using hashing to track which scan IDs have already been seen.

Return the repeated scan ID itself, not its index.

Constraints:
- 1 <= scans.length <= 100000
- 1 <= scans[i].length <= 30
- scans[i] contains only English letters and digits
- Comparison is case-sensitive, so "A12" and "a12" are different IDs

Example 1:
Input: scans = ["E45", "B12", "C77", "B12", "E45"]
Output: "B12"

Explanation:
Read from left to right:
- "E45" -> first time seen
- "B12" -> first time seen
- "C77" -> first time seen
- "B12" -> already seen before, so this is the first repeated scan encountered
Therefore, the answer is "B12".

Example 2:
Input: scans = ["AA1", "BB2", "CC3", "DD4"]
Output: ""

Explanation:
Every scan ID appears only once, so there is no repeated scan ID.
"""

from typing import List, Set


class Solution:
    def first_repeated_badge_scan(self, scans: List[str]) -> str:
        """
        Find the first scan ID whose second appearance is encountered while
        traversing the list from left to right.

        Args:
            scans: A list of badge scan ID strings in recorded order.

        Returns:
            The first repeated scan ID encountered during the left-to-right scan.
            Returns an empty string if no scan ID repeats.

        Time Complexity:
            O(n), where n is the number of scan IDs, because each scan ID is
            processed once and set membership checks are average O(1).

        Space Complexity:
            O(n) in the worst case, if all scan IDs are unique and must be stored
            in the set of seen IDs.
        """
        # We use a set because it gives very fast average-time lookup.
        # Specifically, checking:
        #   "Have we seen this scan ID before?"
        # is typically O(1) with a hash set.
        #
        # This is much better than comparing the current ID against all previous IDs,
        # which would lead to O(n^2) time in the worst case.
        seen: Set[str] = set()

        # We process the scan IDs in the exact order they were recorded.
        # This is important because the problem asks for the first repeated event
        # encountered from left to right.
        for scan_id in scans:
            # Step 1: Check whether the current scan ID has already been seen.
            #
            # If it is already in the set, that means this is at least the second
            # time we are seeing it. Since we are scanning from left to right,
            # this is the earliest duplicate event encountered so far.
            if scan_id in seen:
                return scan_id

            # Step 2: If the current scan ID has not been seen before,
            # add it to the set so future occurrences can be detected.
            seen.add(scan_id)

        # If we finish the loop without finding any duplicate, then every scan ID
        # appeared only once. The problem requires returning an empty string.
        return ""


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem description
    scans1 = ["E45", "B12", "C77", "B12", "E45"]
    result1 = solution.first_repeated_badge_scan(scans1)
    print(result1)  # Expected: "B12"

    # Example 2 from the problem description
    scans2 = ["AA1", "BB2", "CC3", "DD4"]
    result2 = solution.first_repeated_badge_scan(scans2)
    print(result2)  # Expected: ""

    # Additional beginner-friendly test cases
    scans3 = ["X1", "Y2", "X1"]
    result3 = solution.first_repeated_badge_scan(scans3)
    print(result3)  # Expected: "X1"

    scans4 = ["A12", "a12", "A12"]
    result4 = solution.first_repeated_badge_scan(scans4)
    print(result4)  # Expected: "A12" because comparison is case-sensitive

    scans5 = ["ONE"]
    result5 = solution.first_repeated_badge_scan(scans5)
    print(result5)  # Expected: ""