"""
Title: Shortest Maintenance Span Covering All Tool Classes

Problem Description:
A factory records the sequence of tools used during a long maintenance session.
Each tool use is represented by an integer tool class ID in the array tools,
where tools[i] is the class of the i-th tool used.

You are also given an integer array required, where required[j] is a tool class
that must appear at least once inside a valid contiguous span. The required array
may contain duplicates, meaning the span must include that many occurrences of
the corresponding class.

For example:
required = [2, 2, 5]
means a valid span must contain at least two class-2 tools and at least one
class-5 tool.

Return the length of the shortest contiguous subarray of tools that satisfies
all requirements. If no such span exists, return -1.

This problem is designed for large inputs, so solutions that check every
subarray will time out. A correct solution should efficiently maintain counts
while expanding and shrinking a window.
"""

from collections import Counter, defaultdict
from typing import DefaultDict, Dict, List


class Solution:
    def shortest_maintenance_span(self, tools: List[int], required: List[int]) -> int:
        """
        Find the length of the shortest contiguous subarray of tools that satisfies
        all required tool-class counts.

        Args:
            tools: List of tool class IDs representing the maintenance session.
            required: List of required tool class IDs. Duplicates mean multiple
                occurrences are required inside the window.

        Returns:
            The length of the shortest valid contiguous subarray, or -1 if no such
            subarray exists.

        Time Complexity:
            O(n + m), where n = len(tools) and m = len(required).
            Each tool is processed at most twice: once when the right pointer expands
            and once when the left pointer shrinks.

        Space Complexity:
            O(k), where k is the number of distinct tool classes appearing in
            required (and possibly tracked in the current window).

        """
        # If there are no requirements, then the shortest span is conceptually 0.
        # However, based on the problem constraints required.length >= 1, so this
        # branch is mostly defensive programming.
        if not required:
            return 0

        # Build the frequency map of what we need.
        #
        # Example:
        # required = [2, 5, 2]
        # need = {2: 2, 5: 1}
        #
        # Why Counter?
        # - It is the most direct and readable way to count duplicates.
        # - We need exact required frequencies for each tool class.
        need: Dict[int, int] = Counter(required)

        # This dictionary stores the counts of required tool classes currently
        # inside the sliding window.
        #
        # Important detail:
        # We only care about counts for tool classes that appear in "need".
        # Any other tool class can exist in the window, but it does not help
        # satisfy the requirement.
        window_count: DefaultDict[int, int] = defaultdict(int)

        # "formed" counts how many distinct required tool classes currently meet
        # their needed frequency inside the window.
        #
        # Example:
        # need = {2: 2, 5: 1}
        # If window_count = {2: 2, 5: 0}, then formed = 1
        # because tool class 2 is satisfied, but 5 is not.
        formed: int = 0

        # "required_types" is the number of distinct tool classes that must be
        # satisfied.
        #
        # Example:
        # need = {2: 2, 5: 1} => required_types = 2
        required_types: int = len(need)

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far.
        # Start with infinity so any valid window will be smaller.
        best_length: int = float("inf")

        # Expand the window by moving "right" from left to right across the array.
        for right, tool in enumerate(tools):
            # If the current tool is one we care about, include it in the window count.
            if tool in need:
                window_count[tool] += 1

                # If after adding this tool, its count exactly matches what is needed,
                # then one more required tool class has become satisfied.
                #
                # We use "==" here, not ">=":
                # - formed should increase only once when the requirement becomes met.
                # - If count goes above the requirement, it should not increase again.
                if window_count[tool] == need[tool]:
                    formed += 1

            # Once all distinct required tool classes are satisfied, try to shrink
            # the window from the left to make it as short as possible while still valid.
            while formed == required_types and left <= right:
                # Current window is [left, right], inclusive.
                current_length: int = right - left + 1

                # Update the best answer if this valid window is smaller.
                if current_length < best_length:
                    best_length = current_length

                # We will now try to remove tools[left] from the window and move left
                # forward by one step.
                left_tool: int = tools[left]

                # Only required tool classes affect validity.
                if left_tool in need:
                    # If the count is currently exactly equal to the needed amount,
                    # then removing this tool will make that requirement unsatisfied.
                    #
                    # Example:
                    # need[2] = 2, window_count[2] = 2
                    # Removing one 2 would reduce it to 1, so the window would no
                    # longer satisfy tool class 2.
                    if window_count[left_tool] == need[left_tool]:
                        formed -= 1

                    # Actually remove the tool from the window count.
                    window_count[left_tool] -= 1

                # Move the left boundary rightward to continue shrinking.
                left += 1

        # If best_length was never updated, then no valid window exists.
        return -1 if best_length == float("inf") else best_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    tools_1: List[int] = [7, 2, 3, 2, 5, 2, 1, 5]
    required_1: List[int] = [2, 5, 2]
    result_1: int = solution.shortest_maintenance_span(tools_1, required_1)
    print("Example 1 Result:", result_1)  # Expected: 3

    # Example 2
    tools_2: List[int] = [4, 1, 4, 3, 6, 1, 3]
    required_2: List[int] = [1, 3, 3]
    result_2: int = solution.shortest_maintenance_span(tools_2, required_2)
    print("Example 2 Result:", result_2)  # Expected: -1

    # Additional quick sanity checks
    tools_3: List[int] = [2, 5, 2]
    required_3: List[int] = [2, 2, 5]
    result_3: int = solution.shortest_maintenance_span(tools_3, required_3)
    print("Additional Test 1 Result:", result_3)  # Expected: 3

    tools_4: List[int] = [1, 2, 3, 4]
    required_4: List[int] = [4]
    result_4: int = solution.shortest_maintenance_span(tools_4, required_4)
    print("Additional Test 2 Result:", result_4)  # Expected: 1