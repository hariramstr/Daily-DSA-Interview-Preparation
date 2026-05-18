/*
 * Employee Shift Overlap Detector
 * ================================
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * A company tracks employee check-in and check-out times throughout the day.
 * Each employee is identified by a unique ID, and they may check in and out
 * multiple times. You are given a list of log entries where each entry is a
 * string in the format "employeeId action timestamp", where action is either
 * "in" or "out" and timestamp is an integer representing minutes since midnight.
 *
 * Two employees are considered to have an overlapping shift if there exists any
 * minute during which both employees are simultaneously checked in. Given the
 * list of log entries, return a list of all unique pairs [id1, id2]
 * (where id1 < id2 lexicographically) of employees whose shifts overlap at any
 * point. Return the pairs in any order.
 *
 * You may assume each employee's check-in is always followed by a check-out
 * before their next check-in, and all timestamps are valid.
 *
 * Constraints:
 * - 1 <= logs.length <= 1000
 * - Each employeeId is a non-empty string of lowercase letters with length at most 10
 * - 0 <= timestamp <= 1440
 * - No two log entries share the same timestamp for the same employee
 *
 * Example 1:
 * Input: logs = ["alice in 100", "bob in 120", "alice out 200", "bob out 250"]
 * Output: [["alice", "bob"]]
 * Explanation: Alice is checked in from 100–200 and Bob from 120–250.
 *              They overlap from 120 to 200.
 *
 * Example 2:
 * Input: logs = ["alice in 50", "alice out 90", "bob in 100", "bob out 150"]
 * Output: []
 * Explanation: Alice leaves at 90 and Bob arrives at 100, so there is no overlap.
 */

import java.util.*;

/**
 * Solution class for the Employee Shift Overlap Detector problem.
 * This solution uses hashing to store each employee's shift intervals,
 * then checks all pairs of employees for any overlapping intervals.
 */
public class Solution {

    /**
     * Finds all pairs of employees whose shifts overlap at any point in time.
     *
     * <p>Algorithm Overview:
     * 1. Parse all log entries to build a map of employee -> list of [checkIn, checkOut] intervals.
     * 2. For every unique pair of employees, check if any of their intervals overlap.
     * 3. Two intervals [a, b] and [c, d] overlap if a < d AND c < b (strict overlap means
     *    at least one shared minute; touching endpoints do NOT count as overlap per Example 2).
     * 4. Collect overlapping pairs in lexicographic order and return them.
     *
     * @param logs A list of log entry strings in the format "employeeId action timestamp"
     * @return A list of unique pairs [id1, id2] (id1 < id2 lexicographically) of employees
     *         whose shifts overlap at any point in time.
     *
     * Time Complexity:  O(N^2 * M) where N = number of employees and M = max intervals per employee.
     *                   Parsing is O(L) where L = number of log entries.
     * Space Complexity: O(L) to store all intervals for all employees.
     */
    public List<List<String>> findOverlappingPairs(List<String> logs) {

        // -----------------------------------------------------------------------
        // STEP 1: Parse log entries and build a map from employeeId -> list of intervals.
        //         Each interval is stored as an int[] of length 2: [checkInTime, checkOutTime].
        //         We use a temporary map to hold the "pending" check-in time for each employee.
        // -----------------------------------------------------------------------

        // Maps each employee ID to their list of completed [checkIn, checkOut] intervals
        Map<String, List<int[]>> employeeIntervals = new HashMap<>();

        // Temporarily stores the check-in time for an employee who has checked in
        // but not yet checked out (i.e., their interval is not yet complete)
        Map<String, Integer> pendingCheckIn = new HashMap<>();

        // Iterate over every log entry to parse it
        for (String log : logs) {
            // Split the log entry by spaces into exactly 3 parts:
            // parts[0] = employeeId, parts[1] = action ("in" or "out"), parts[2] = timestamp
            String[] parts = log.split(" ");
            String employeeId = parts[0];
            String action     = parts[1];
            int    timestamp  = Integer.parseInt(parts[2]);

            if (action.equals("in")) {
                // Employee is checking IN: record the pending check-in time
                // We'll complete the interval when we see their "out" entry
                pendingCheckIn.put(employeeId, timestamp);

            } else {
                // action.equals("out"): Employee is checking OUT
                // Retrieve the matching check-in time from pendingCheckIn
                int checkInTime = pendingCheckIn.get(employeeId);

                // Create the completed interval [checkInTime, timestamp]
                int[] interval = new int[]{checkInTime, timestamp};

                // Add this interval to the employee's list of intervals
                // computeIfAbsent creates a new ArrayList if the key doesn't exist yet
                employeeIntervals.computeIfAbsent(employeeId, k -> new ArrayList<>()).add(interval);

                // Remove the pending check-in since the interval is now complete
                pendingCheckIn.remove(employeeId);
            }
        }

        // -----------------------------------------------------------------------
        // STEP 2: Collect all unique employee IDs into a sorted list so we can
        //         generate pairs in lexicographic order (id1 < id2).
        // -----------------------------------------------------------------------
        List<String> employees = new ArrayList<>(employeeIntervals.keySet());
        Collections.sort(employees); // Sort lexicographically

        // This will hold the result: all overlapping pairs
        List<List<String>> result = new ArrayList<>();

        // -----------------------------------------------------------------------
        // STEP 3: Check every unique pair of employees (i, j) where i < j.
        //         Because employees is sorted, employees.get(i) < employees.get(j)
        //         lexicographically, satisfying the id1 < id2 requirement.
        // -----------------------------------------------------------------------
        for (int i = 0; i < employees.size(); i++) {
            for (int j = i + 1; j < employees.size(); j++) {

                String emp1 = employees.get(i); // id1 (lexicographically smaller)
                String emp2 = employees.get(j); // id2 (lexicographically larger)

                // Get all intervals for each employee
                List<int[]> intervals1 = employeeIntervals.get(emp1);
                List<int[]> intervals2 = employeeIntervals.get(emp2);

                // ---------------------------------------------------------------
                // STEP 4: Check if ANY interval of emp1 overlaps with ANY interval
                //         of emp2.
                //
                //         Two intervals [a, b] and [c, d] overlap (share at least
                //         one common minute) if and only if:
                //             a < d  AND  c < b
                //
                //         Why strict inequalities?
                //         - If a == d, emp1 checks out exactly when emp2 checks in
                //           → they do NOT share a common minute (see Example 2).
                //         - Similarly if c == b.
                //
                //         Example 2 trace:
                //           alice: [50, 90], bob: [100, 150]
                //           Check: 50 < 150 → true, but 100 < 90 → FALSE → no overlap ✓
                //
                //         Example 1 trace:
                //           alice: [100, 200], bob: [120, 250]
                //           Check: 100 < 250 → true, AND 120 < 200 → true → OVERLAP ✓
                // ---------------------------------------------------------------
                boolean overlaps = false; // Assume no overlap until proven otherwise

                outerLoop:
                for (int[] interval1 : intervals1) {
                    for (int[] interval2 : intervals2) {
                        int a = interval1[0]; // emp1 check-in
                        int b = interval1[1]; // emp1 check-out
                        int c = interval2[0]; // emp2 check-in
                        int d = interval2[1]; // emp2 check-out

                        // Overlap condition: intervals share at least one common minute
                        if (a < d && c < b) {
                            overlaps = true;
                            break outerLoop; // No need to check further intervals
                        }
                    }
                }

                // If an overlap was found, add this pair to the result
                if (overlaps) {
                    result.add(Arrays.asList(emp1, emp2));
                }
            }
        }

        // -----------------------------------------------------------------------
        // STEP 5: Return the list of all overlapping pairs.
        // -----------------------------------------------------------------------
        return result;
    }

    /**
     * Demonstrates the solution with the provided sample inputs and prints results.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -------------------------------------------------------------------
        // Example 1:
        // Alice is checked in from 100 to 200.
        // Bob is checked in from 120 to 250.
        // They overlap from minute 120 to minute 200.
        // Expected Output: [["alice", "bob"]]
        // -------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        List<String> logs1 = Arrays.asList(
            "alice in 100",
            "bob in 120",
            "alice out 200",
            "bob out 250"
        );
        List<List<String>> result1 = solution.findOverlappingPairs(logs1);
        System.out.println("Input:    " + logs1);
        System.out.println("Output:   " + result1);
        System.out.println("Expected: [[alice, bob]]");
        System.out.println();

        // -------------------------------------------------------------------
        // Example 2:
        // Alice is checked in from 50 to 90.
        // Bob is checked in from 100 to 150.
        // Alice leaves at 90, Bob arrives at 100 — no shared minute.
        // Expected Output: []
        // -------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        List<String> logs2 = Arrays.asList(
            "alice in 50",
            "alice out 90",
            "bob in 100",
            "bob out 150"
        );
        List<List<String>> result2 = solution.findOverlappingPairs(logs2);
        System.out.println("Input:    " + logs2);
        System.out.println("Output:   " + result2);
        System.out.println("Expected: []");
        System.out.println();

        // -------------------------------------------------------------------
        // Example 3 (Extra): Multiple employees, multiple shifts.
        // alice:   [10, 50], [200, 300]
        // bob:     [40, 100]
        // charlie: [60, 150]
        //
        // alice vs bob:     [10,50] vs [40,100] → 10<100 AND 40<50 → OVERLAP
        // alice vs charlie: [10,50] vs [60,150] → 10<150 AND 60<50 → NO
        //                   [200,300] vs [60,150] → 200<150 → NO
        // bob vs charlie:   [40,100] vs [60,150] → 40<150 AND 60<100 → OVERLAP
        //
        // Expected Output: [[alice, bob], [bob, charlie]]
        // -------------------------------------------------------------------
        System.out.println("=== Example 3 (Extra) ===");
        List<String> logs3 = Arrays.asList(
            "alice in 10",
            "alice out 50",
            "bob in 40",
            "charlie in 60",
            "bob out 100",
            "charlie out 150",
            "alice in 200",
            "alice out 300"
        );
        List<List<String>> result3 = solution.findOverlappingPairs(logs3);
        System.out.println("Input:    " + logs3);
        System.out.println("Output:   " + result3);
        System.out.println("Expected: [[alice, bob], [bob, charlie]]");
        System.out.println();

        // -------------------------------------------------------------------
        // Example 4 (Extra): Single employee — no pairs possible.
        // Expected Output: []
        // -------------------------------------------------------------------
        System.out.println("=== Example 4 (Extra - Single Employee) ===");
        List<String> logs4 = Arrays.asList(
            "alice in 0",
            "alice out 1440"
        );
        List<List<String>> result4 = solution.findOverlappingPairs(logs4);
        System.out.println("Input:    " + logs4);
        System.out.println("Output:   " + result4);
        System.out.println("Expected: []");
        System.out.println();

        // -------------------------------------------------------------------
        // Example 5 (Extra): Touching endpoints — should NOT overlap.
        // alice: [0, 100], bob: [100, 200]
        // alice checks out at 100, bob checks in at 100 → no shared minute.
        // Expected Output: []
        // -------------------------------------------------------------------
        System.out.println("=== Example 5 (Extra - Touching Endpoints) ===");
        List<String> logs5 = Arrays.asList(
            "alice in 0",
            "alice out 100",
            "bob in 100",
            "bob out 200"
        );
        List<List<String>> result5 = solution.findOverlappingPairs(logs5);
        System.out.println("Input:    " + logs5);
        System.out.println("Output:   " + result5);
        System.out.println("Expected: []");
    }
}