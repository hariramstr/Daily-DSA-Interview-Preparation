import java.util.*;

/*
Problem Title: Find the First Repeated Badge Scan

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

public class Solution {

    /**
     * Finds the first badge scan ID that repeats while traversing the array from left to right.
     *
     * The idea is simple:
     * 1. Keep a hash set of scan IDs we have already seen.
     * 2. Read each scan ID in order.
     * 3. If the current scan ID is already in the set, then this is the first duplicate event
     *    encountered in the stream, so return it immediately.
     * 4. Otherwise, add it to the set and continue.
     * 5. If we finish the loop without finding any duplicate, return an empty string.
     *
     * This works because we process the scans in the exact order they were recorded.
     * The first time we encounter a scan ID that has already been seen, that is exactly
     * the earliest repeated scan event.
     *
     * @param scans the array of badge scan IDs recorded in order
     * @return the first repeated scan ID encountered from left to right, or an empty string if none repeats
     * Time complexity: O(n), where n is the number of scans, because each lookup/add in the hash set is O(1) on average
     * Space complexity: O(n), in the worst case when all scan IDs are unique and must be stored in the set
     */
    public String findFirstRepeatedBadgeScan(String[] scans) {
        // Create a HashSet to store every scan ID we have already encountered.
        // Why a HashSet?
        // Because it gives us very fast average-time operations:
        // - contains(...) to check whether we have seen an ID before
        // - add(...) to record a new ID
        Set<String> seen = new HashSet<>();

        // Process the scan IDs one by one in the original left-to-right order.
        // This order is extremely important because the problem asks for the
        // first repeated scan event as it appears in the stream.
        for (String scan : scans) {
            // Step 1: Check whether this scan ID has already been seen before.
            // If yes, then this is the first duplicate event encountered during our traversal.
            if (seen.contains(scan)) {
                // We immediately return the scan ID itself, not its index.
                return scan;
            }

            // Step 2: If it has not been seen before, record it in the set
            // so future occurrences can be detected as duplicates.
            seen.add(scan);
        }

        // If we finish scanning the entire array and never find a repeated ID,
        // then there is no duplicate event in the input.
        return "";
    }

    /**
     * A small helper method to print an input array in a readable format.
     *
     * @param scans the array of badge scan IDs to display
     * @return a string representation of the array
     * Time complexity: O(n), where n is the number of scan IDs
     * Space complexity: O(n), due to building the output string
     */
    public String formatScans(String[] scans) {
        return Arrays.toString(scans);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional examples.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demonstration size, or O(total input size) for all tested arrays combined
     * Space complexity: O(total unique IDs per test case) due to the hash set used by the algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1:
        // scans = ["E45", "B12", "C77", "B12", "E45"]
        // Walkthrough:
        // - See "E45" first time -> store it
        // - See "B12" first time -> store it
        // - See "C77" first time -> store it
        // - See "B12" again -> duplicate found immediately
        // So the correct output is "B12"
        String[] scans1 = {"E45", "B12", "C77", "B12", "E45"};
        System.out.println("Input:  " + solution.formatScans(scans1));
        System.out.println("Output: " + solution.findFirstRepeatedBadgeScan(scans1));
        System.out.println("Expected: B12");
        System.out.println();

        // Sample Input 2:
        // scans = ["AA1", "BB2", "CC3", "DD4"]
        // Walkthrough:
        // - Every ID appears only once
        // - No duplicate is ever found
        // So the correct output is an empty string
        String[] scans2 = {"AA1", "BB2", "CC3", "DD4"};
        System.out.println("Input:  " + solution.formatScans(scans2));
        System.out.println("Output: " + solution.findFirstRepeatedBadgeScan(scans2));
        System.out.println("Expected: ");
        System.out.println();

        // Additional example:
        // The first repeated event is "X1" because its second appearance is encountered first.
        String[] scans3 = {"X1", "Y2", "Z3", "X1", "Y2"};
        System.out.println("Input:  " + solution.formatScans(scans3));
        System.out.println("Output: " + solution.findFirstRepeatedBadgeScan(scans3));
        System.out.println("Expected: X1");
        System.out.println();

        // Additional example showing case sensitivity:
        // "A12" and "a12" are different IDs, so the duplicate is "A12" only when it appears again exactly.
        String[] scans4 = {"A12", "a12", "B34", "A12"};
        System.out.println("Input:  " + solution.formatScans(scans4));
        System.out.println("Output: " + solution.findFirstRepeatedBadgeScan(scans4));
        System.out.println("Expected: A12");
    }
}