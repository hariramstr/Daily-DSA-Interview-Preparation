```python
"""
Title: Employee Shift Overlap Detector
Difficulty: Easy
Topic: Hashing

Problem Description:
A company tracks employee check-in and check-out times throughout the day. Each employee
is identified by a unique ID, and they may check in and out multiple times. You are given
a list of log entries where each entry is a string in the format "employeeId action timestamp",
where action is either "in" or "out" and timestamp is an integer representing minutes since midnight.

Two employees are considered to have an overlapping shift if there exists any minute during
which both employees are simultaneously checked in. Given the list of log entries, return a
list of all unique pairs [id1, id2] (where id1 < id2 lexicographically) of employees whose
shifts overlap at any point. Return the pairs in any order.

You may assume each employee's check-in is always followed by a check-out before their next
check-in, and all timestamps are valid.

Constraints:
- 1 <= logs.length <= 1000
- Each employeeId is a non-empty string of lowercase letters with length at most 10
- 0 <= timestamp <= 1440
- No two log entries share the same timestamp for the same employee

Example 1:
Input: logs = ["alice in 100", "bob in 120", "alice out 200", "bob out 250"]
Output: [["alice", "bob"]]
Explanation: Alice is checked in from 100–200 and Bob from 120–250. They overlap from 120 to 200.

Example 2:
Input: logs = ["alice in 50", "alice out 90", "bob in 100", "bob out 150"]
Output: []
Explanation: Alice leaves at 90 and Bob arrives at 100, so there is no overlap.
"""

from typing import List, Dict, Tuple, Set


class Solution:
    def findOverlappingShifts(self, logs: List[str]) -> List[List[str]]:
        """
        Find all pairs of employees whose shifts overlap at any point in time.

        The approach:
        1. Parse all log entries to build a list of (start, end) intervals per employee.
        2. For each pair of employees, check if any of their intervals overlap.
        3. Two intervals [s1, e1] and [s2, e2] overlap if s1 < e2 AND s2 < e1.
           (strict inequality because touching at a single point, e.g., one ends at 90
            and another starts at 100, is NOT considered overlapping per the problem.)

        Args:
            logs: A list of strings in the format "employeeId action timestamp"

        Returns:
            A list of unique pairs [id1, id2] (id1 < id2 lexicographically) of employees
            whose shifts overlap at any point.

        Time Complexity: O(N^2 * M) where N is the number of unique employees and M is
                         the maximum number of shifts per employee. Parsing is O(L) where
                         L is the number of log entries.
        Space Complexity: O(E * S) where E is the number of employees and S is the average
                          number of shifts per employee (to store intervals).
        """

        # -----------------------------------------------------------------------
        # STEP 1: Parse log entries and build a dictionary mapping each employee
        #         to their list of (check_in_time, check_out_time) intervals.
        #
        # We use a dictionary:
        #   - Key: employee ID (string)
        #   - Value: list of (start, end) tuples representing each shift interval
        #
        # We also use a temporary dictionary to track the current check-in time
        # for each employee (since check-in always precedes check-out).
        # -----------------------------------------------------------------------

        # Dictionary to store completed intervals for each employee
        # Format: { "alice": [(100, 200), (300, 400)], "bob": [(120, 250)], ... }
        employee_intervals: Dict[str, List[Tuple[int, int]]] = {}

        # Temporary dictionary to track the most recent check-in time for each employee
        # Format: { "alice": 100, "bob": 120 }
        current_checkin: Dict[str, int] = {}

        # -----------------------------------------------------------------------
        # STEP 2: Iterate through each log entry and process it.
        #
        # Each log entry has the format: "employeeId action timestamp"
        # We split by space to extract the three components.
        # -----------------------------------------------------------------------

        for log in logs:
            # Split the log entry into its three parts
            parts = log.split()
            employee_id = parts[0]   # e.g., "alice"
            action = parts[1]        # e.g., "in" or "out"
            timestamp = int(parts[2])  # e.g., 100

            # Initialize the employee's interval list if not already present
            if employee_id not in employee_intervals:
                employee_intervals[employee_id] = []

            if action == "in":
                # Record the check-in time for this employee
                # We'll pair it with the corresponding check-out later
                current_checkin[employee_id] = timestamp

            elif action == "out":
                # We found the matching check-out for this employee's last check-in
                # Retrieve the stored check-in time
                checkin_time = current_checkin[employee_id]
                checkout_time = timestamp

                # Add the completed interval (start, end) to this employee's list
                employee_intervals[employee_id].append((checkin_time, checkout_time))

                # Remove from current_checkin since this shift is now complete
                del current_checkin[employee_id]

        # -----------------------------------------------------------------------
        # STEP 3: Get the sorted list of all unique employee IDs.
        #
        # Sorting ensures we can easily create lexicographically ordered pairs
        # and avoid duplicate pairs (we only consider i < j in the nested loop).
        # -----------------------------------------------------------------------

        employee_ids = sorted(employee_intervals.keys())

        # -----------------------------------------------------------------------
        # STEP 4: Check every pair of employees for overlapping shifts.
        #
        # We use a set to store overlapping pairs to avoid duplicates.
        # For each pair (emp_a, emp_b), we check all combinations of their
        # respective intervals to see if any two intervals overlap.
        #
        # Two intervals [s1, e1] and [s2, e2] overlap if and only if:
        #   s1 < e2  AND  s2 < e1
        #
        # Why strict inequality?
        # - If alice checks out at 90 and bob checks in at 90, they are NOT
        #   simultaneously checked in at any minute (the problem's Example 2
        #   shows alice out at 90, bob in at 100 => no overlap, but even if
        #   bob were at 90, the boundary is exclusive for the "out" action).
        # - Actually, looking at Example 2: alice out=90, bob in=100 => no overlap.
        #   The problem says "any minute during which both are simultaneously checked in."
        #   If we treat intervals as [in, out), then touching at a point means no overlap.
        #   Using strict inequality (s1 < e2 AND s2 < e1) correctly handles this.
        # -----------------------------------------------------------------------

        # Use a set of tuples to store unique overlapping pairs
        # We'll convert to a list of lists at the end
        overlapping_pairs: Set[Tuple[str, str]] = set()

        # Iterate over all unique pairs of employees
        # Using indices i < j ensures we don't check the same pair twice
        for i in range(len(employee_ids)):
            for j in range(i + 1, len(employee_ids)):
                emp_a = employee_ids[i]
                emp_b = employee_ids[j]

                # Get all intervals for both employees
                intervals_a = employee_intervals[emp_a]
                intervals_b = employee_intervals[emp_b]

                # Check all combinations of intervals between emp_a and emp_b
                overlap_found = False
                for (s1, e1) in intervals_a:
                    for (s2, e2) in intervals_b:
                        # Two intervals [s1, e1) and [s2, e2) overlap if:
                        # s1 < e2 AND s2 < e1
                        # This means one interval starts before the other ends,
                        # and vice versa — guaranteeing actual overlap.
                        if s1 < e2 and s2 < e1:
                            overlap_found = True
                            break  # No need to check more intervals for this pair
                    if overlap_found:
                        break  # No need to check more intervals for this pair

                if overlap_found:
                    # Since employee_ids is sorted and i < j, emp_a < emp_b lexicographically
                    overlapping_pairs.add((emp_a, emp_b))

        # -----------------------------------------------------------------------
        # STEP 5: Convert the set of tuples to a list of lists for the final result.
        #
        # Each tuple (id1, id2) becomes a list [id1, id2].
        # id1 < id2 lexicographically is already guaranteed by our sorted iteration.
        # -----------------------------------------------------------------------

        result = [[pair[0], pair[1]] for pair in overlapping_pairs]

        return result


# -------------------------------------------------------------------------------
# Main block: Test the solution with the provided examples and additional cases.
# -------------------------------------------------------------------------------

if __name__ == "__main__":
    solution = Solution()

    # -------------------------------------------------------------------
    # Example 1:
    # Alice is checked in from 100–200 and Bob from 120–250.
    # They overlap from 120 to 200.
    # Expected Output: [["alice", "bob"]]
    # -------------------------------------------------------------------
    logs1 = [
        "alice in 100",
        "bob in 120",
        "alice out 200",
        "bob out 250"
    ]
    result1 = solution.findOverlappingShifts(logs1)
    print("Example 1:")
    print(f"  Input:    {logs1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: [['alice', 'bob']]")
    print()

    # -------------------------------------------------------------------
    # Example 2:
    # Alice leaves at 90 and Bob arrives at 100, so there is no overlap.
    # Expected Output: []
    # -------------------------------------------------------------------
    logs2 = [
        "alice in 50",
        "alice out 90",
        "bob in 100",
        "bob out 150"
    ]
    result2 = solution.findOverlappingShifts(logs2)
    print("Example 2:")
    print(f"  Input:    {logs2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: []")
    print()

    # -------------------------------------------------------------------
    # Additional Test Case 1: Three employees, two pairs overlap
    # alice: 100-200, bob: 150-300, charlie: 50-110
    # alice & bob overlap (150-200), alice & charlie overlap (100-110)
    # bob & charlie: bob starts at 150, charlie ends at 110 => no overlap
    # Expected Output: [["alice", "bob"], ["alice", "charlie"]] (any order)
    # -------------------------------------------------------------------
    logs3 = [
        "alice in 100",
        "charlie in 50",
        "bob in 150",
        "charlie out 110",
        "alice out 200",
        "bob out 300"
    ]
    result3 = solution.findOverlappingShifts(logs3)
    print("Additional Test Case 1 (Three employees):")
    print(f"  Input:    {logs3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: [['alice', 'bob'], ['alice', 'charlie']] (any order)")
    print()

    # -------------------------------------------------------------------
    # Additional Test Case 2: Employees with multiple shifts
    # alice: 50-100, 200-300
    # bob: 90-210
    # alice shift1 (50-100) and bob (90-210): overlap at 90-100 => YES
    # alice shift2 (200-300) and bob (90-210): overlap at 200-210 => YES
    # Expected Output: [["alice", "bob"]]
    # -------------------------------------------------------------------
    logs4 = [
        "alice in 50",
        "alice out 100",
        "bob in 90",
        "alice in 200",
        "bob out 210",
        "alice out 300"
    ]
    result4 = solution.findOverlappingShifts(logs4)
    print("Additional Test Case 2 (Multiple shifts per employee):")
    print(f"  Input:    {logs4}")
    print(f"  Output:   {result4}")
    print(f"  Expected: [['alice', 'bob']]")
    print()

    # -------------------------------------------------------------------
    # Additional Test Case 3: Boundary touching — should NOT overlap
    # alice: 100-200, bob: 200-300
    # They touch at exactly 200 but do not overlap (strict inequality)
    # Expected Output: []
    # -------------------------------------------------------------------
    logs5 = [
        "alice in 100",
        "alice out 200",
        "bob in 200",
        "bob out 300"
    ]
    result5 = solution.findOverlappingShifts(logs5)
    print("Additional Test Case 3 (Boundary touching):")
    print(f"  Input:    {logs5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: []")
    print()

    # -------------------------------------------------------------------
    # Additional Test Case 4: Single employee — no pairs possible
    # Expected Output: []
    # -------------------------------------------------------------------
    logs6 = [
        "alice in 100",
        "alice out 200"
    ]
    result6 = solution.findOverlappingShifts(logs6)
    print("Additional Test Case 4 (Single employee):")
    print(f"  Input:    {logs6}")
    print(f"  Output:   {result6}")
    print(f"  Expected: []")
```