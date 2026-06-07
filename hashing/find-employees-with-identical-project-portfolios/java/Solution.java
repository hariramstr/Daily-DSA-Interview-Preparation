/*
 * Title: Find Employees with Identical Project Portfolios
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
 * - Each [employeeId, projectId] pair is unique (an employee is not assigned to the same project twice).
 * - An employee appears in at least one assignment.
 *
 * Example 1:
 * Input: assignments = [[1,3],[1,5],[2,3],[2,5],[3,1],[3,3],[4,1],[4,3]]
 * Output: [[1,2],[3,4]]
 * Explanation: Employee 1 and Employee 2 both work on projects {3,5}.
 *              Employee 3 and Employee 4 both work on projects {1,3}.
 *
 * Example 2:
 * Input: assignments = [[1,2],[1,4],[2,3],[3,2],[3,4]]
 * Output: [[1,3]]
 * Explanation: Employees 1 and 3 both work on projects {2,4}.
 *              Employee 2 only works on project {3} and has no match.
 */

import java.util.*;

/**
 * Solution class for finding employees with identical project portfolios.
 * Uses a hashing approach to group employees by their sorted project sets.
 */
public class Solution {

    /**
     * Finds all groups of employees who share identical project portfolios.
     *
     * <p>Algorithm Overview:
     * 1. Build a map from each employee to their set of projects.
     * 2. Create a canonical "signature" for each employee's project set (sorted, joined string).
     * 3. Group employees by their signature.
     * 4. Return only groups with 2 or more employees.
     *
     * @param assignments A 2D array where assignments[i] = [employeeId, projectId].
     * @return A list of groups (each group is a list of employee IDs) sharing identical portfolios.
     *         Only groups with at least 2 employees are included.
     *
     * Time Complexity:  O(N * K * log K) where N = number of assignments,
     *                   K = max number of projects per employee (for sorting the project list).
     * Space Complexity: O(N) for storing the maps and result groups.
     */
    public List<List<Integer>> findIdenticalPortfolios(int[][] assignments) {

        // -----------------------------------------------------------------------
        // STEP 1: Build a map from employeeId -> set of projectIds
        // We use a TreeSet so that projects are automatically kept in sorted order.
        // This will make generating the canonical signature straightforward.
        // -----------------------------------------------------------------------
        // Key: employeeId, Value: sorted set of projectIds assigned to that employee
        Map<Integer, TreeSet<Integer>> employeeProjects = new HashMap<>();

        for (int[] assignment : assignments) {
            int employeeId = assignment[0];
            int projectId  = assignment[1];

            // If this employee hasn't been seen yet, create a new TreeSet for them
            // computeIfAbsent returns the existing set or creates a new one
            employeeProjects.computeIfAbsent(employeeId, k -> new TreeSet<>()).add(projectId);
        }

        // -----------------------------------------------------------------------
        // STEP 2: Create a canonical signature for each employee's project portfolio.
        // Since we used a TreeSet, the projects are already in ascending sorted order.
        // We join them into a single string like "3,5" or "1,3" to use as a map key.
        //
        // Why a string key? Two employees have identical portfolios if and only if
        // their sorted project lists are equal — the string captures this exactly.
        // -----------------------------------------------------------------------
        // Key: canonical signature string, Value: list of employeeIds with that signature
        Map<String, List<Integer>> signatureToEmployees = new HashMap<>();

        for (Map.Entry<Integer, TreeSet<Integer>> entry : employeeProjects.entrySet()) {
            int employeeId          = entry.getKey();
            TreeSet<Integer> projects = entry.getValue();

            // Build the signature by joining sorted project IDs with a delimiter
            // e.g., projects {3, 5} -> "3,5"
            // Using StringJoiner for clean, efficient string construction
            StringJoiner joiner = new StringJoiner(",");
            for (int projectId : projects) {
                joiner.add(String.valueOf(projectId));
            }
            String signature = joiner.toString();

            // Group this employee under their portfolio signature
            signatureToEmployees.computeIfAbsent(signature, k -> new ArrayList<>()).add(employeeId);
        }

        // -----------------------------------------------------------------------
        // STEP 3: Collect groups that have at least 2 employees.
        // Single-employee groups are not considered "identical portfolio" groups.
        // -----------------------------------------------------------------------
        List<List<Integer>> result = new ArrayList<>();

        for (List<Integer> group : signatureToEmployees.values()) {
            // Only include groups with 2 or more employees
            if (group.size() >= 2) {
                // Sort the group for consistent, readable output (optional but nice)
                Collections.sort(group);
                result.add(group);
            }
        }

        // The problem states groups can be in any order, so we return as-is
        return result;
    }

    /**
     * Prints a list of groups in a readable format.
     *
     * @param groups The list of employee groups to print.
     *
     * Time Complexity:  O(G * E) where G = number of groups, E = employees per group.
     * Space Complexity: O(1) extra space.
     */
    public static void printGroups(List<List<Integer>> groups) {
        System.out.print("[");
        for (int i = 0; i < groups.size(); i++) {
            System.out.print(groups.get(i));
            if (i < groups.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    /**
     * Main method demonstrating the solution with the provided examples.
     * Traces through each example and prints results.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // assignments = [[1,3],[1,5],[2,3],[2,5],[3,1],[3,3],[4,1],[4,3]]
        //
        // Step-by-step trace:
        //   Employee 1 -> projects {3, 5}  -> signature "3,5"
        //   Employee 2 -> projects {3, 5}  -> signature "3,5"
        //   Employee 3 -> projects {1, 3}  -> signature "1,3"
        //   Employee 4 -> projects {1, 3}  -> signature "1,3"
        //
        //   signatureToEmployees:
        //     "3,5" -> [1, 2]
        //     "1,3" -> [3, 4]
        //
        //   Both groups have >= 2 employees, so result = [[1,2],[3,4]]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int[][] assignments1 = {
            {1, 3}, {1, 5},
            {2, 3}, {2, 5},
            {3, 1}, {3, 3},
            {4, 1}, {4, 3}
        };
        List<List<Integer>> result1 = solution.findIdenticalPortfolios(assignments1);
        System.out.print("Output: ");
        printGroups(result1);
        System.out.println("Expected: [[1, 2], [3, 4]]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // assignments = [[1,2],[1,4],[2,3],[3,2],[3,4]]
        //
        // Step-by-step trace:
        //   Employee 1 -> projects {2, 4}  -> signature "2,4"
        //   Employee 2 -> projects {3}     -> signature "3"
        //   Employee 3 -> projects {2, 4}  -> signature "2,4"
        //
        //   signatureToEmployees:
        //     "2,4" -> [1, 3]
        //     "3"   -> [2]
        //
        //   Only "2,4" has >= 2 employees, so result = [[1,3]]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int[][] assignments2 = {
            {1, 2}, {1, 4},
            {2, 3},
            {3, 2}, {3, 4}
        };
        List<List<Integer>> result2 = solution.findIdenticalPortfolios(assignments2);
        System.out.print("Output: ");
        printGroups(result2);
        System.out.println("Expected: [[1, 3]]");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 3: No matching portfolios
        // assignments = [[1,1],[2,2],[3,3]]
        //
        // Step-by-step trace:
        //   Employee 1 -> projects {1} -> signature "1"
        //   Employee 2 -> projects {2} -> signature "2"
        //   Employee 3 -> projects {3} -> signature "3"
        //
        //   No group has >= 2 employees, so result = []
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3 (No matches) ===");
        int[][] assignments3 = {
            {1, 1},
            {2, 2},
            {3, 3}
        };
        List<List<Integer>> result3 = solution.findIdenticalPortfolios(assignments3);
        System.out.print("Output: ");
        printGroups(result3);
        System.out.println("Expected: []");
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Example 4: Three employees with the same portfolio
        // assignments = [[1,5],[2,5],[3,5]]
        //
        // Step-by-step trace:
        //   Employee 1 -> projects {5} -> signature "5"
        //   Employee 2 -> projects {5} -> signature "5"
        //   Employee 3 -> projects {5} -> signature "5"
        //
        //   signatureToEmployees:
        //     "5" -> [1, 2, 3]
        //
        //   Group has 3 employees (>= 2), so result = [[1,2,3]]
        // -----------------------------------------------------------------------
        System.out.println("=== Example 4 (Three employees, same portfolio) ===");
        int[][] assignments4 = {
            {1, 5},
            {2, 5},
            {3, 5}
        };
        List<List<Integer>> result4 = solution.findIdenticalPortfolios(assignments4);
        System.out.print("Output: ");
        printGroups(result4);
        System.out.println("Expected: [[1, 2, 3]]");
    }
}