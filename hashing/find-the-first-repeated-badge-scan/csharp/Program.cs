/*
Title: Find the First Repeated Badge Scan

Problem Description:
You are given a list of employee badge scan IDs in the order they were recorded at a building entrance.
Each scan ID is a string consisting of letters and digits. Some employees may scan multiple times
because they forgot an item, re-entered the building, or accidentally scanned twice.

Your task is to return the first scan ID that appears more than once when reading the list from left to right.
In other words, find the earliest duplicate event in the stream. If no scan ID is repeated, return an empty string.

This problem is about efficiently detecting duplicates while preserving the original arrival order.
A simple nested-loop solution works for small inputs, but interviewers expect a faster approach using hashing
to track which scan IDs have already been seen.

Return the repeated scan ID itself, not its index.

Constraints:
- 1 <= scans.length <= 100000
- 1 <= scans[i].length <= 30
- scans[i] contains only English letters and digits
- Comparison is case-sensitive, so "A12" and "a12" are different IDs

Example 1:
Input: scans = ["E45", "B12", "C77", "B12", "E45"]
Output: "B12"
Explanation: "B12" is the first scan ID whose second appearance is encountered while scanning from left to right.

Example 2:
Input: scans = ["AA1", "BB2", "CC3", "DD4"]
Output: ""
Explanation: No scan ID appears more than once, so return an empty string.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the input list exactly once.
    - Each HashSet operation (Contains / Add) is O(1) on average.

    Space Complexity: O(n)
    - In the worst case, no scan ID repeats, so we store all scan IDs in the HashSet.
    */
    public string FirstRepeatedBadgeScan(string[] scans)
    {
        // We use a HashSet<string> because it is designed for fast membership checks.
        // This means:
        // - We can quickly ask: "Have we seen this scan ID before?"
        // - We can quickly add a new scan ID after we process it.
        //
        // Why HashSet is a good choice:
        // - A list would require searching through previous items one by one, which is slower.
        // - A HashSet gives average O(1) lookup and insertion, making the whole solution efficient.
        var seen = new HashSet<string>();

        // We now walk through the scan IDs from left to right.
        // This order is extremely important because the problem asks for the first scan ID
        // whose repeated occurrence is encountered earliest in the stream.
        //
        // Example:
        // scans = ["E45", "B12", "C77", "B12", "E45"]
        // We must return "B12" because its second appearance happens before the second appearance of "E45".
        foreach (string scanId in scans)
        {
            // Step 1: Check whether the current scan ID has already been seen before.
            //
            // Why this step is necessary:
            // - If it HAS been seen, then the current scan is the second (or later) occurrence.
            // - Since we are scanning from left to right, this is the earliest duplicate event encountered so far.
            //
            // If true, we can immediately return this scan ID.
            if (seen.Contains(scanId))
            {
                return scanId;
            }

            // Step 2: If the scan ID was not seen before, record it in the HashSet.
            //
            // Why this step is necessary:
            // - Future occurrences of this same scan ID should be recognized as duplicates.
            // - By adding it now, we remember that this ID has already appeared once.
            seen.Add(scanId);
        }

        // If we finish the entire loop without returning, that means no scan ID ever appeared twice.
        // According to the problem statement, in that case we must return an empty string.
        return "";
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// Input: ["E45", "B12", "C77", "B12", "E45"]
// Walkthrough:
// - "E45" -> not seen, add it
// - "B12" -> not seen, add it
// - "C77" -> not seen, add it
// - "B12" -> already seen, so this is the first repeated badge scan
// Expected output: "B12"
string[] scans1 = { "E45", "B12", "C77", "B12", "E45" };
string result1 = solution.FirstRepeatedBadgeScan(scans1);
Console.WriteLine(result1);

// Example 2:
// Input: ["AA1", "BB2", "CC3", "DD4"]
// Walkthrough:
// - Every scan ID is unique
// - No duplicate is found
// Expected output: ""
string[] scans2 = { "AA1", "BB2", "CC3", "DD4" };
string result2 = solution.FirstRepeatedBadgeScan(scans2);
Console.WriteLine(result2);

// Additional demo:
// Input: ["A12", "a12", "A12"]
// Because comparison is case-sensitive:
// - "A12" and "a12" are different
// - The third item repeats the first item
// Expected output: "A12"
string[] scans3 = { "A12", "a12", "A12" };
string result3 = solution.FirstRepeatedBadgeScan(scans3);
Console.WriteLine(result3);