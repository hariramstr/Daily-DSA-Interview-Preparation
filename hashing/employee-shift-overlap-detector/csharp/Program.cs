/*
 * ============================================================
 * Title: Employee Shift Overlap Detector
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
 * Input:  logs = ["alice in 100", "bob in 120", "alice out 200", "bob out 250"]
 * Output: [["alice", "bob"]]
 * Explanation: Alice is checked in from 100–200 and Bob from 120–250.
 *              They overlap from 120 to 200.
 *
 * Example 2:
 * Input:  logs = ["alice in 50", "alice out 90", "bob in 100", "bob out 150"]
 * Output: []
 * Explanation: Alice leaves at 90 and Bob arrives at 100, so there is no overlap.
 * ============================================================
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the core algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Finds all pairs of employees whose shifts overlap at any point in time.
    ///
    /// Time Complexity:  O(N * S^2) where N is the number of log entries and
    ///                   S is the number of distinct employees.
    ///                   Parsing is O(N). Building intervals is O(N).
    ///                   Comparing every pair of employees is O(S^2), and for
    ///                   each pair we compare every interval combination.
    ///
    /// Space Complexity: O(N) to store all parsed intervals for every employee.
    /// </summary>
    public List<List<string>> FindOverlappingPairs(string[] logs)
    {
        // ── Step 1: Parse every log entry and group intervals by employee ──────
        //
        // We use a Dictionary where:
        //   Key   = employeeId (string)
        //   Value = list of (checkIn, checkOut) tuples representing each shift
        //
        // Why a Dictionary?  It gives O(1) average-case lookup by employee ID,
        // so we can quickly find the right employee's list when we see a log entry.
        //
        // Why store tuples of (in, out)?  An overlap check between two employees
        // reduces to checking whether any interval from employee A overlaps with
        // any interval from employee B.  Storing complete intervals makes that
        // comparison straightforward.
        var intervals = new Dictionary<string, List<(int checkIn, int checkOut)>>();

        // We also need a temporary place to remember the most recent check-in
        // time for each employee while we haven't yet seen their check-out.
        // Key = employeeId, Value = the timestamp of the pending check-in.
        var pendingCheckIn = new Dictionary<string, int>();

        // ── Step 2: Iterate through every log entry in the order given ─────────
        //
        // The problem guarantees that a check-in is always followed by a
        // check-out before the next check-in, so we can safely pair them up
        // as we scan linearly.
        foreach (string log in logs)
        {
            // Split the log entry on spaces.
            // Expected format: "employeeId action timestamp"
            // parts[0] = employeeId, parts[1] = "in" or "out", parts[2] = timestamp
            string[] parts = log.Split(' ');
            string employeeId = parts[0];
            string action     = parts[1];
            int    timestamp  = int.Parse(parts[2]);

            if (action == "in")
            {
                // ── Step 2a: Record a pending check-in ────────────────────────
                // We don't have a complete interval yet (we're missing the
                // check-out), so just remember when this employee clocked in.
                pendingCheckIn[employeeId] = timestamp;
            }
            else // action == "out"
            {
                // ── Step 2b: Complete the interval and store it ────────────────
                // Now we have both endpoints.  Retrieve the saved check-in time,
                // form the interval, and add it to this employee's list.

                int checkInTime = pendingCheckIn[employeeId];

                // Make sure the employee already has a list in the dictionary;
                // if not, create one.
                if (!intervals.ContainsKey(employeeId))
                    intervals[employeeId] = new List<(int, int)>();

                intervals[employeeId].Add((checkInTime, timestamp));

                // Remove the pending entry — this employee is now clocked out
                // and ready for a potential future check-in.
                pendingCheckIn.Remove(employeeId);
            }
        }

        // ── Step 3: Collect all employee IDs into a sorted list ───────────────
        //
        // We sort them so that when we form pairs (id1, id2) we can guarantee
        // id1 < id2 lexicographically simply by always taking the earlier element
        // in the sorted order as id1.
        var employeeIds = new List<string>(intervals.Keys);
        employeeIds.Sort(StringComparer.Ordinal); // lexicographic sort

        // ── Step 4: Check every unique pair of employees for overlap ──────────
        //
        // We use a nested loop: the outer loop picks the first employee (i),
        // the inner loop picks the second employee (j > i).
        // Because j always starts above i, we never check the same pair twice
        // and we never compare an employee with themselves.
        var result = new List<List<string>>();

        for (int i = 0; i < employeeIds.Count; i++)
        {
            for (int j = i + 1; j < employeeIds.Count; j++)
            {
                string empA = employeeIds[i]; // lexicographically smaller (sorted)
                string empB = employeeIds[j]; // lexicographically larger

                List<(int checkIn, int checkOut)> intervalsA = intervals[empA];
                List<(int checkIn, int checkOut)> intervalsB = intervals[empB];

                // ── Step 4a: Check all interval pairs between empA and empB ───
                //
                // Two intervals [a_in, a_out] and [b_in, b_out] overlap if and
                // only if:
                //
                //     a_in  < b_out   AND   b_in  < a_out
                //
                // Why this formula?
                //   Intervals do NOT overlap when one ends before the other starts:
                //     a_out <= b_in   (A finishes before B starts)
                //     b_out <= a_in   (B finishes before A starts)
                //   Negating both non-overlap conditions gives the overlap condition.
                //
                // Note: we use strict inequality (<) because the problem says
                // "simultaneously checked in".  If Alice checks out at minute 90
                // and Bob checks in at minute 90, they are NOT simultaneously in
                // (Alice is already out), so that is NOT an overlap — matching
                // Example 2 exactly.
                bool foundOverlap = false;

                foreach (var (aIn, aOut) in intervalsA)
                {
                    foreach (var (bIn, bOut) in intervalsB)
                    {
                        // Overlap condition: the intervals share at least one
                        // interior minute (strict inequalities exclude touching
                        // endpoints, which are not "simultaneously checked in").
                        if (aIn < bOut && bIn < aOut)
                        {
                            foundOverlap = true;
                            break; // no need to check more intervals for this pair
                        }
                    }

                    if (foundOverlap) break; // exit outer interval loop too
                }

                // ── Step 4b: If any interval pair overlapped, record the pair ──
                if (foundOverlap)
                {
                    // empA is already lexicographically ≤ empB because we sorted
                    // employeeIds before the loop.
                    result.Add(new List<string> { empA, empB });
                }
            }
        }

        // ── Step 5: Return the collected overlapping pairs ────────────────────
        return result;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// ── Helper: pretty-print the result ──────────────────────────────────────────
static void PrintResult(List<List<string>> pairs)
{
    if (pairs.Count == 0)
    {
        Console.WriteLine("[]");
        return;
    }

    Console.Write("[");
    for (int k = 0; k < pairs.Count; k++)
    {
        Console.Write($"[\"{pairs[k][0]}\", \"{pairs[k][1]}\"]");
        if (k < pairs.Count - 1) Console.Write(", ");
    }
    Console.WriteLine("]");
}

// ── Example 1 ─────────────────────────────────────────────────────────────────
// Alice: 100–200, Bob: 120–250  →  overlap 120–200  →  expected [["alice","bob"]]
Console.WriteLine("=== Example 1 ===");
string[] logs1 = { "alice in 100", "bob in 120", "alice out 200", "bob out 250" };
var result1 = solution.FindOverlappingPairs(logs1);
Console.Write("Output: ");
PrintResult(result1);
// Trace:
//   intervals["alice"] = [(100, 200)]
//   intervals["bob"]   = [(120, 250)]
//   Pair (alice, bob): aIn=100 < bOut=250 ✓  AND  bIn=120 < aOut=200 ✓  → overlap!
//   Result: [["alice","bob"]]  ✓

Console.WriteLine();

// ── Example 2 ─────────────────────────────────────────────────────────────────
// Alice: 50–90, Bob: 100–150  →  no overlap  →  expected []
Console.WriteLine("=== Example 2 ===");
string[] logs2 = { "alice in 50", "alice out 90", "bob in 100", "bob out 150" };
var result2 = solution.FindOverlappingPairs(logs2);
Console.Write("Output: ");
PrintResult(result2);
// Trace:
//   intervals["alice"] = [(50, 90)]
//   intervals["bob"]   = [(100, 150)]
//   Pair (alice, bob): aIn=50 < bOut=150 ✓  BUT  bIn=100 < aOut=90? NO (100 >= 90)
//   → no overlap
//   Result: []  ✓

Console.WriteLine();

// ── Example 3: Multiple shifts, partial overlap ───────────────────────────────
// carol: 0–60 and 200–300
// dave:  50–150 and 250–350
// Expected overlapping pairs: [["carol","dave"]]
//   carol[0–60] vs dave[50–150]: 0 < 150 ✓ AND 50 < 60 ✓ → overlap
Console.WriteLine("=== Example 3 (multiple shifts) ===");
string[] logs3 =
{
    "carol in 0",   "dave in 50",
    "carol out 60", "dave out 150",
    "carol in 200", "dave in 250",
    "carol out 300","dave out 350"
};
var result3 = solution.FindOverlappingPairs(logs3);
Console.Write("Output: ");
PrintResult(result3);

Console.WriteLine();

// ── Example 4: Three employees, only two pairs overlap ────────────────────────
// eve:   10–50
// frank: 40–80   → overlaps eve (40 < 50)
// grace: 60–100  → overlaps frank (60 < 80), does NOT overlap eve (60 >= 50)
// Expected: [["eve","frank"], ["frank","grace"]]
Console.WriteLine("=== Example 4 (three employees) ===");
string[] logs4 =
{
    "eve in 10",   "frank in 40",  "grace in 60",
    "eve out 50",  "frank out 80", "grace out 100"
};
var result4 = solution.FindOverlappingPairs(logs4);
Console.Write("Output: ");
PrintResult(result4);