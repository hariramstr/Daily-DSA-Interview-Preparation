/*
 * Find Employees with Identical Project Portfolios
 * ================================================
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * A company stores its employee-project assignments as a list of pairs [employeeId, projectId].
 * Two employees are considered to have identical project portfolios if they are assigned to
 * exactly the same set of projects (regardless of order). Your task is to find all groups of
 * employees who share identical project portfolios.
 *
 * Given a 2D integer array assignments where assignments[i] = [employeeId, projectId],
 * return a list of groups where each group contains employee IDs that share the same project
 * portfolio. Only include groups with at least 2 employees. The groups and the employee IDs
 * within each group can be returned in any order.
 *
 * Constraints:
 * - 1 <= assignments.length <= 10^5
 * - 1 <= employeeId, projectId <= 10^4
 * - Each [employeeId, projectId] pair is unique
 * - An employee appears in at least one assignment
 *
 * Example 1:
 * Input:  assignments = [[1,3],[1,5],[2,3],[2,5],[3,1],[3,3],[4,1],[4,3]]
 * Output: [[1,2],[3,4]]
 *
 * Example 2:
 * Input:  assignments = [[1,2],[1,4],[2,3],[3,2],[3,4]]
 * Output: [[1,3]]
 */

using System;
using System.Collections.Generic;
using System.Linq;

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Finds all groups of employees that share identical project portfolios.
    ///
    /// Time Complexity:  O(N * K * log K)
    ///   where N = number of assignments, K = average projects per employee.
    ///   - Building the employee→projects map: O(N)
    ///   - Sorting each employee's project list: O(E * K * log K) where E = number of employees
    ///   - Hashing / grouping: O(E * K)
    ///   Overall dominated by the sorting step.
    ///
    /// Space Complexity: O(N)
    ///   - We store every (employeeId, projectId) pair at most once across all data structures.
    /// </summary>
    public IList<IList<int>> FindIdenticalPortfolios(int[][] assignments)
    {
        // ── STEP 1: Build a map from each employee to their list of projects ──────
        //
        // Why? We need to know which projects each employee is assigned to before
        // we can compare portfolios. A Dictionary<int, List<int>> is perfect here:
        //   Key   = employeeId
        //   Value = list of projectIds assigned to that employee
        //
        // We iterate over every assignment pair and append the projectId to the
        // correct employee's list.

        var employeeProjects = new Dictionary<int, List<int>>();

        foreach (var assignment in assignments)
        {
            int employeeId = assignment[0];
            int projectId  = assignment[1];

            // If this employee hasn't been seen yet, create a new list for them.
            if (!employeeProjects.ContainsKey(employeeId))
            {
                employeeProjects[employeeId] = new List<int>();
            }

            // Add the project to this employee's portfolio.
            employeeProjects[employeeId].Add(projectId);
        }

        // ── STEP 2: Create a canonical (sorted) string key for each employee ──────
        //
        // Why sort? Two employees have the SAME portfolio if they work on the SAME
        // SET of projects, regardless of the order in which the assignments appear
        // in the input. By sorting the project IDs and joining them into a string,
        // we get a deterministic "fingerprint" that is identical for any two employees
        // with the same set of projects.
        //
        // Example: projects {3,5} → sorted → [3,5] → key "3,5"
        //          projects {5,3} → sorted → [3,5] → key "3,5"  ← same key ✓
        //
        // We then group employees by this key using another dictionary:
        //   Key   = canonical project-set string
        //   Value = list of employeeIds that share this portfolio

        var portfolioGroups = new Dictionary<string, List<int>>();

        foreach (var kvp in employeeProjects)
        {
            int employeeId      = kvp.Key;
            List<int> projects  = kvp.Value;

            // Sort the project list so the key is order-independent.
            projects.Sort();

            // Build the canonical key by joining sorted project IDs with a separator.
            // Using "," as separator avoids collisions like [1,23] vs [12,3].
            string portfolioKey = string.Join(",", projects);

            // If no group exists for this portfolio yet, create one.
            if (!portfolioGroups.ContainsKey(portfolioKey))
            {
                portfolioGroups[portfolioKey] = new List<int>();
            }

            // Add this employee to the group that shares their portfolio.
            portfolioGroups[portfolioKey].Add(employeeId);
        }

        // ── STEP 3: Collect groups that have at least 2 employees ────────────────
        //
        // Why filter? The problem asks only for groups where more than one employee
        // shares the same portfolio. A group of size 1 means that employee has a
        // unique portfolio and should not appear in the output.

        var result = new List<IList<int>>();

        foreach (var group in portfolioGroups.Values)
        {
            // Only include groups with 2 or more employees.
            if (group.Count >= 2)
            {
                // Optionally sort the employee IDs within the group for readability.
                // The problem says any order is acceptable, but sorted output is cleaner.
                group.Sort();
                result.Add(group);
            }
        }

        // ── STEP 4: Return the final result ──────────────────────────────────────
        //
        // Each element of `result` is a list of employee IDs that all share the
        // exact same set of projects. The outer list can be in any order.

        return result;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Driver Code  (top-level statements — no Main method needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

// Helper: pretty-print a list of groups
static void PrintGroups(IList<IList<int>> groups, string label)
{
    Console.WriteLine(label);

    if (groups.Count == 0)
    {
        Console.WriteLine("  (no groups with 2+ employees)");
    }
    else
    {
        foreach (var group in groups)
        {
            Console.WriteLine("  [" + string.Join(", ", group) + "]");
        }
    }

    Console.WriteLine();
}

var solver = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// assignments = [[1,3],[1,5],[2,3],[2,5],[3,1],[3,3],[4,1],[4,3]]
// Expected output: [[1,2],[3,4]]
//
// Trace:
//   Employee 1 → projects {3,5}  → key "3,5"
//   Employee 2 → projects {3,5}  → key "3,5"   ← same as Employee 1
//   Employee 3 → projects {1,3}  → key "1,3"
//   Employee 4 → projects {1,3}  → key "1,3"   ← same as Employee 3
//
//   Groups: "3,5" → [1,2],  "1,3" → [3,4]
//   Both groups have size ≥ 2, so both are included. ✓

int[][] example1 = new int[][]
{
    new int[] {1, 3},
    new int[] {1, 5},
    new int[] {2, 3},
    new int[] {2, 5},
    new int[] {3, 1},
    new int[] {3, 3},
    new int[] {4, 1},
    new int[] {4, 3}
};

var result1 = solver.FindIdenticalPortfolios(example1);
PrintGroups(result1, "Example 1 — Expected: [[1,2],[3,4]]");

// ── Example 2 ────────────────────────────────────────────────────────────────
// assignments = [[1,2],[1,4],[2,3],[3,2],[3,4]]
// Expected output: [[1,3]]
//
// Trace:
//   Employee 1 → projects {2,4}  → key "2,4"
//   Employee 2 → projects {3}    → key "3"
//   Employee 3 → projects {2,4}  → key "2,4"   ← same as Employee 1
//
//   Groups: "2,4" → [1,3],  "3" → [2]
//   Only "2,4" has size ≥ 2. ✓

int[][] example2 = new int[][]
{
    new int[] {1, 2},
    new int[] {1, 4},
    new int[] {2, 3},
    new int[] {3, 2},
    new int[] {3, 4}
};

var result2 = solver.FindIdenticalPortfolios(example2);
PrintGroups(result2, "Example 2 — Expected: [[1,3]]");

// ── Extra edge-case: no matching portfolios ───────────────────────────────────
// Each employee has a unique portfolio → output should be empty.

int[][] example3 = new int[][]
{
    new int[] {1, 1},
    new int[] {2, 2},
    new int[] {3, 3}
};

var result3 = solver.FindIdenticalPortfolios(example3);
PrintGroups(result3, "Example 3 (edge case) — Expected: (no groups)");

// ── Extra edge-case: three employees share the same portfolio ─────────────────

int[][] example4 = new int[][]
{
    new int[] {1, 10},
    new int[] {1, 20},
    new int[] {2, 20},
    new int[] {2, 10},
    new int[] {3, 10},
    new int[] {3, 20},
    new int[] {4, 99}
};

var result4 = solver.FindIdenticalPortfolios(example4);
PrintGroups(result4, "Example 4 (three-way match) — Expected: [[1,2,3]]");