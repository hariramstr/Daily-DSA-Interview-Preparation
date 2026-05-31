/*
 * Title: Find All Symmetric Pairs in a Contact List
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of n contact pairs where each pair [caller, receiver]
 * represents a phone call from caller to receiver. A symmetric pair exists when
 * both [A, B] and [B, A] appear in the list. Your task is to return all unique
 * symmetric pairs found in the contact list.
 *
 * A pair [A, B] and its symmetric counterpart [B, A] should only be reported
 * once in the output (report the version where the smaller value comes first).
 * If the same pair appears multiple times in the input, it should still only
 * generate one symmetric result.
 *
 * Constraints:
 * - 1 <= n <= 10^5
 * - 1 <= caller, receiver <= 10^6
 * - caller != receiver
 * - The input list may contain duplicate pairs.
 *
 * Example 1:
 * Input: contacts = [[1, 2], [3, 4], [2, 1], [5, 6], [4, 3], [7, 8]]
 * Output: [[1, 2], [3, 4]]
 *
 * Example 2:
 * Input: contacts = [[10, 20], [20, 10], [10, 20], [30, 40]]
 * Output: [[10, 20]]
 */

import java.util.*;

/**
 * Solution class for finding all symmetric pairs in a contact list.
 *
 * <p>A symmetric pair [A, B] exists when both [A, B] and [B, A] are present
 * in the contact list. Each symmetric pair is reported exactly once, with
 * the smaller value first.</p>
 */
public class Solution {

    /**
     * Finds all unique symmetric pairs in the given contact list.
     *
     * <p>Algorithm Overview:
     * We use a HashSet to track which pairs we have already seen (as normalized
     * strings), and a HashMap to store each unique pair we encounter. When we
     * process a pair [A, B], we check if its reverse [B, A] has already been
     * seen. If yes, we record the symmetric pair (with smaller value first).
     * We also use a result set to avoid duplicate output entries.</p>
     *
     * @param contacts a 2D array where each row is [caller, receiver]
     * @return a list of unique symmetric pairs, each with the smaller value first
     *
     * Time Complexity: O(n) — we iterate through all n pairs once; HashMap/HashSet
     *                  operations are O(1) on average.
     * Space Complexity: O(n) — we store up to n pairs in the HashMap and HashSet.
     */
    public List<int[]> findSymmetricPairs(int[][] contacts) {

        // -----------------------------------------------------------------------
        // Step 1: Create a HashSet to track all unique pairs we have seen so far.
        //         We encode each pair [A, B] as the string "A,B" for easy lookup.
        //         Using a set ensures we handle duplicate input pairs gracefully.
        // -----------------------------------------------------------------------
        Set<String> seenPairs = new HashSet<>();

        // -----------------------------------------------------------------------
        // Step 2: Create a HashSet to track symmetric pairs we have already added
        //         to the result. We encode each result pair as "min,max" to avoid
        //         adding the same symmetric pair twice (e.g., if both [1,2]→[2,1]
        //         and [2,1]→[1,2] would otherwise both trigger an addition).
        // -----------------------------------------------------------------------
        Set<String> addedSymmetric = new HashSet<>();

        // -----------------------------------------------------------------------
        // Step 3: Prepare the result list that will hold our symmetric pairs.
        // -----------------------------------------------------------------------
        List<int[]> result = new ArrayList<>();

        // -----------------------------------------------------------------------
        // Step 4: Iterate over every contact pair in the input list.
        // -----------------------------------------------------------------------
        for (int[] pair : contacts) {
            int caller   = pair[0];
            int receiver = pair[1];

            // ------------------------------------------------------------------
            // Step 4a: Encode the current pair as "caller,receiver".
            //          This is the key we use to record that we've seen this pair.
            // ------------------------------------------------------------------
            String currentKey = caller + "," + receiver;

            // ------------------------------------------------------------------
            // Step 4b: Encode the reverse pair as "receiver,caller".
            //          We will check whether this reverse pair has been seen before.
            // ------------------------------------------------------------------
            String reverseKey = receiver + "," + caller;

            // ------------------------------------------------------------------
            // Step 4c: Check if the reverse pair has already been seen.
            //          If seenPairs contains reverseKey, then we have a symmetric
            //          pair: [caller, receiver] and [receiver, caller] both exist.
            // ------------------------------------------------------------------
            if (seenPairs.contains(reverseKey)) {

                // --------------------------------------------------------------
                // Step 4d: Determine the canonical form of this symmetric pair:
                //          always put the smaller number first so that [1,2] and
                //          [2,1] both produce the same canonical key "1,2".
                // --------------------------------------------------------------
                int smaller = Math.min(caller, receiver);
                int larger  = Math.max(caller, receiver);
                String canonicalKey = smaller + "," + larger;

                // --------------------------------------------------------------
                // Step 4e: Only add this symmetric pair to the result if we have
                //          not already added it. This prevents duplicates in the
                //          output when the same symmetric relationship appears
                //          multiple times in the input (e.g., [10,20] twice with
                //          [20,10] once).
                // --------------------------------------------------------------
                if (!addedSymmetric.contains(canonicalKey)) {
                    addedSymmetric.add(canonicalKey);
                    result.add(new int[]{smaller, larger});
                }
            }

            // ------------------------------------------------------------------
            // Step 4f: Mark the current pair as seen (only if not already there).
            //          We add it to seenPairs so future pairs can detect it as
            //          a reverse match. Using a set automatically handles
            //          duplicates — adding an already-present key is a no-op.
            // ------------------------------------------------------------------
            seenPairs.add(currentKey);
        }

        // -----------------------------------------------------------------------
        // Step 5: Return the collected list of unique symmetric pairs.
        // -----------------------------------------------------------------------
        return result;
    }

    // ===========================================================================
    // Helper method to print a list of int[] pairs in a readable format.
    // ===========================================================================

    /**
     * Converts a list of int[] pairs into a human-readable string like [[1, 2], [3, 4]].
     *
     * @param pairs the list of integer pairs to format
     * @return a formatted string representation of the pairs
     *
     * Time Complexity: O(k) where k is the number of pairs in the list.
     * Space Complexity: O(k) for the string builder output.
     */
    public static String formatPairs(List<int[]> pairs) {
        // Use a StringBuilder for efficient string concatenation
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < pairs.size(); i++) {
            int[] p = pairs.get(i);
            sb.append("[").append(p[0]).append(", ").append(p[1]).append("]");
            // Add a comma separator between pairs, but not after the last one
            if (i < pairs.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    // ===========================================================================
    // Main method: demonstrates the solution with the provided examples.
    // ===========================================================================

    /**
     * Entry point for demonstrating the symmetric pair finder.
     *
     * <p>Traces through both examples from the problem description and prints
     * the results to standard output.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        // Create an instance of Solution to call the instance method
        Solution solution = new Solution();

        // =======================================================================
        // Example 1:
        // Input:  [[1, 2], [3, 4], [2, 1], [5, 6], [4, 3], [7, 8]]
        // Expected Output: [[1, 2], [3, 4]]
        //
        // Trace:
        //   Process [1,2]: seenPairs={}, reverseKey="2,1" not in seen → add "1,2" to seen
        //   Process [3,4]: reverseKey="4,3" not in seen → add "3,4" to seen
        //   Process [2,1]: reverseKey="1,2" IS in seen → symmetric! canonical="1,2" → add [1,2]
        //                  add "2,1" to seen
        //   Process [5,6]: reverseKey="6,5" not in seen → add "5,6" to seen
        //   Process [4,3]: reverseKey="3,4" IS in seen → symmetric! canonical="3,4" → add [3,4]
        //                  add "4,3" to seen
        //   Process [7,8]: reverseKey="8,7" not in seen → add "7,8" to seen
        //   Result: [[1,2],[3,4]] ✓
        // =======================================================================
        System.out.println("=== Example 1 ===");
        int[][] contacts1 = {{1, 2}, {3, 4}, {2, 1}, {5, 6}, {4, 3}, {7, 8}};
        List<int[]> result1 = solution.findSymmetricPairs(contacts1);
        System.out.println("Input:    [[1, 2], [3, 4], [2, 1], [5, 6], [4, 3], [7, 8]]");
        System.out.println("Output:   " + formatPairs(result1));
        System.out.println("Expected: [[1, 2], [3, 4]]");
        System.out.println();

        // =======================================================================
        // Example 2:
        // Input:  [[10, 20], [20, 10], [10, 20], [30, 40]]
        // Expected Output: [[10, 20]]
        //
        // Trace:
        //   Process [10,20]: reverseKey="20,10" not in seen → add "10,20" to seen
        //   Process [20,10]: reverseKey="10,20" IS in seen → symmetric! canonical="10,20" → add [10,20]
        //                    add "20,10" to seen
        //   Process [10,20]: reverseKey="20,10" IS in seen → symmetric! canonical="10,20"
        //                    BUT "10,20" already in addedSymmetric → skip (no duplicate)
        //                    "10,20" already in seenPairs → no change
        //   Process [30,40]: reverseKey="40,30" not in seen → add "30,40" to seen
        //   Result: [[10,20]] ✓
        // =======================================================================
        System.out.println("=== Example 2 ===");
        int[][] contacts2 = {{10, 20}, {20, 10}, {10, 20}, {30, 40}};
        List<int[]> result2 = solution.findSymmetricPairs(contacts2);
        System.out.println("Input:    [[10, 20], [20, 10], [10, 20], [30, 40]]");
        System.out.println("Output:   " + formatPairs(result2));
        System.out.println("Expected: [[10, 20]]");
        System.out.println();

        // =======================================================================
        // Additional Example 3: No symmetric pairs
        // Input:  [[1, 2], [3, 4], [5, 6]]
        // Expected Output: []
        // =======================================================================
        System.out.println("=== Example 3 (No symmetric pairs) ===");
        int[][] contacts3 = {{1, 2}, {3, 4}, {5, 6}};
        List<int[]> result3 = solution.findSymmetricPairs(contacts3);
        System.out.println("Input:    [[1, 2], [3, 4], [5, 6]]");
        System.out.println("Output:   " + formatPairs(result3));
        System.out.println("Expected: []");
        System.out.println();

        // =======================================================================
        // Additional Example 4: Multiple symmetric pairs including duplicates
        // Input:  [[5, 10], [10, 5], [5, 10], [10, 5], [100, 200], [200, 100]]
        // Expected Output: [[5, 10], [100, 200]]
        // =======================================================================
        System.out.println("=== Example 4 (Multiple duplicates) ===");
        int[][] contacts4 = {{5, 10}, {10, 5}, {5, 10}, {10, 5}, {100, 200}, {200, 100}};
        List<int[]> result4 = solution.findSymmetricPairs(contacts4);
        System.out.println("Input:    [[5, 10], [10, 5], [5, 10], [10, 5], [100, 200], [200, 100]]");
        System.out.println("Output:   " + formatPairs(result4));
        System.out.println("Expected: [[5, 10], [100, 200]]");
    }
}