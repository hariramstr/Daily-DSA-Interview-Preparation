"""
Find Employees with Identical Project Portfolios
================================================

Problem Description:
A company stores its employee-project assignments as a list of pairs [employeeId, projectId].
Two employees are considered to have identical project portfolios if they are assigned to
exactly the same set of projects (regardless of order). Your task is to find all groups of
employees who share identical project portfolios.

Given a 2D integer array assignments where assignments[i] = [employeeId, projectId], return
a list of groups where each group contains employee IDs that share the same project portfolio.
Only include groups with at least 2 employees. The groups and the employee IDs within each
group can be returned in any order.

Constraints:
- 1 <= assignments.length <= 10^5
- 1 <= employeeId, projectId <= 10^4
- Each [employeeId, projectId] pair is unique (an employee is not assigned to the same project twice).
- An employee appears in at least one assignment.
"""

from collections import defaultdict
from typing import List, Dict, Tuple


class Solution:
    def findEmployeesWithIdenticalPortfolios(self, assignments: List[List[int]]) -> List[List[int]]:
        """
        Find all groups of employees who share identical project portfolios.

        This method uses hashing to group employees by their sorted set of projects.
        A frozenset (or sorted tuple) of project IDs serves as a canonical key that
        is order-independent, allowing us to detect identical portfolios efficiently.

        Args:
            assignments (List[List[int]]): A 2D list where each element is
                                           [employeeId, projectId].

        Returns:
            List[List[int]]: A list of groups, where each group is a list of
                             employee IDs sharing the same project portfolio.
                             Only groups with at least 2 employees are included.

        Time Complexity:  O(N * log N) where N is the number of assignments.
                          - Building the employee->projects map: O(N)
                          - For each employee, sorting their projects: O(P * log P)
                            where P is the number of projects per employee.
                          - Overall dominated by sorting across all employees: O(N log N)

        Space Complexity: O(N) for storing the employee-to-projects mapping and
                          the portfolio-to-employees grouping dictionary.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build a mapping from each employee to their set of projects.
        #
        # We use a defaultdict(list) so that we can append project IDs without
        # needing to check if the key already exists.
        #
        # After this step, employee_projects will look like:
        #   {1: [3, 5], 2: [3, 5], 3: [1, 3], 4: [1, 3]}  (for Example 1)
        # -----------------------------------------------------------------------
        employee_projects: Dict[int, List[int]] = defaultdict(list)

        for employee_id, project_id in assignments:
            # Append this project to the employee's list of projects.
            # Using a list here because we'll sort it later to create a canonical key.
            employee_projects[employee_id].append(project_id)

        # -----------------------------------------------------------------------
        # STEP 2: Build a mapping from a "portfolio signature" to a list of
        #         employees who have that exact portfolio.
        #
        # The portfolio signature is a TUPLE of SORTED project IDs.
        # Why a tuple? Because:
        #   - Tuples are hashable (can be used as dictionary keys), lists are not.
        #   - Sorting ensures order-independence: {3,5} and {5,3} both become (3,5).
        #
        # After this step, portfolio_groups will look like:
        #   {(3,5): [1, 2], (1,3): [3, 4]}  (for Example 1)
        # -----------------------------------------------------------------------
        portfolio_groups: Dict[Tuple[int, ...], List[int]] = defaultdict(list)

        for employee_id, projects in employee_projects.items():
            # Sort the project list to create a canonical, order-independent key.
            # Example: projects [5, 3] becomes the tuple (3, 5).
            portfolio_key: Tuple[int, ...] = tuple(sorted(projects))

            # Group this employee under their portfolio signature.
            portfolio_groups[portfolio_key].append(employee_id)

        # -----------------------------------------------------------------------
        # STEP 3: Collect only the groups that have at least 2 employees.
        #
        # A group with only 1 employee has no "match", so we skip it.
        # We convert each group list to a sorted list for consistent output,
        # though the problem says any order is acceptable.
        # -----------------------------------------------------------------------
        result: List[List[int]] = []

        for portfolio_key, employee_group in portfolio_groups.items():
            # Only include groups where 2 or more employees share the same portfolio.
            if len(employee_group) >= 2:
                # Sort the employee IDs within the group for readability.
                result.append(sorted(employee_group))

        # -----------------------------------------------------------------------
        # STEP 4: Return the final result.
        #
        # The result is a list of groups. Each group is a list of employee IDs
        # who all share the exact same set of projects.
        # -----------------------------------------------------------------------
        return result


# =============================================================================
# TRACE-THROUGH VERIFICATION
# =============================================================================
#
# Example 1:
#   Input: [[1,3],[1,5],[2,3],[2,5],[3,1],[3,3],[4,1],[4,3]]
#
#   Step 1 - employee_projects:
#     {1: [3,5], 2: [3,5], 3: [1,3], 4: [1,3]}
#
#   Step 2 - portfolio_groups:
#     (3,5) -> [1, 2]
#     (1,3) -> [3, 4]
#
#   Step 3 - result (both groups have >= 2 employees):
#     [[1,2], [3,4]]
#
#   Expected: [[1,2],[3,4]]  ✓
#
# Example 2:
#   Input: [[1,2],[1,4],[2,3],[3,2],[3,4]]
#
#   Step 1 - employee_projects:
#     {1: [2,4], 2: [3], 3: [2,4]}
#
#   Step 2 - portfolio_groups:
#     (2,4) -> [1, 3]
#     (3,)  -> [2]
#
#   Step 3 - result (only (2,4) group has >= 2 employees):
#     [[1,3]]
#
#   Expected: [[1,3]]  ✓
#
# =============================================================================


if __name__ == "__main__":
    # Create a Solution instance to call our method.
    solution = Solution()

    # ------------------------------------------------------------------
    # Test Case 1 (from the problem description)
    # ------------------------------------------------------------------
    assignments1 = [[1, 3], [1, 5], [2, 3], [2, 5], [3, 1], [3, 3], [4, 1], [4, 3]]
    result1 = solution.findEmployeesWithIdenticalPortfolios(assignments1)
    print("Test Case 1:")
    print(f"  Input:    {assignments1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: [[1, 2], [3, 4]]")
    print()

    # ------------------------------------------------------------------
    # Test Case 2 (from the problem description)
    # ------------------------------------------------------------------
    assignments2 = [[1, 2], [1, 4], [2, 3], [3, 2], [3, 4]]
    result2 = solution.findEmployeesWithIdenticalPortfolios(assignments2)
    print("Test Case 2:")
    print(f"  Input:    {assignments2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: [[1, 3]]")
    print()

    # ------------------------------------------------------------------
    # Test Case 3: No matching portfolios (all employees unique)
    # ------------------------------------------------------------------
    assignments3 = [[1, 1], [2, 2], [3, 3]]
    result3 = solution.findEmployeesWithIdenticalPortfolios(assignments3)
    print("Test Case 3 (No matches):")
    print(f"  Input:    {assignments3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: []")
    print()

    # ------------------------------------------------------------------
    # Test Case 4: All employees share the same portfolio
    # ------------------------------------------------------------------
    assignments4 = [[1, 10], [2, 10], [3, 10], [1, 20], [2, 20], [3, 20]]
    result4 = solution.findEmployeesWithIdenticalPortfolios(assignments4)
    print("Test Case 4 (All share same portfolio):")
    print(f"  Input:    {assignments4}")
    print(f"  Output:   {result4}")
    print(f"  Expected: [[1, 2, 3]]")
    print()

    # ------------------------------------------------------------------
    # Test Case 5: Single assignment (only one employee, one project)
    # ------------------------------------------------------------------
    assignments5 = [[1, 5]]
    result5 = solution.findEmployeesWithIdenticalPortfolios(assignments5)
    print("Test Case 5 (Single assignment):")
    print(f"  Input:    {assignments5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: []")
    print()

    # ------------------------------------------------------------------
    # Test Case 6: Multiple groups of matching employees
    # ------------------------------------------------------------------
    assignments6 = [
        [1, 1], [1, 2],
        [2, 1], [2, 2],
        [3, 3], [3, 4],
        [4, 3], [4, 4],
        [5, 5],
    ]
    result6 = solution.findEmployeesWithIdenticalPortfolios(assignments6)
    print("Test Case 6 (Multiple groups):")
    print(f"  Input:    {assignments6}")
    print(f"  Output:   {result6}")
    print(f"  Expected: [[1, 2], [3, 4]]  (Employee 5 has no match)")