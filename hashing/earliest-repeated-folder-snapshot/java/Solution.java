import java.util.*;

/*
Problem Title: Earliest Repeated Folder Snapshot

Problem Description:
A cloud storage system records the state of a folder once per minute. Each snapshot is represented by an array of file IDs currently present in the folder. The order of file IDs inside a snapshot is not meaningful, but duplicate IDs will never appear within the same snapshot.

Two snapshots are considered identical if they contain exactly the same set of file IDs, regardless of order. Given a list of snapshots in chronological order, return the earliest pair of indices [i, j] such that i < j and snapshots[i] and snapshots[j] are identical. If multiple repeated states exist, choose the pair with the smallest j. If there is still a tie, choose the one with the smallest i. If no folder state repeats, return [-1, -1].

Your task is to design an efficient solution using hashing so that each snapshot can be normalized and compared quickly.

Constraints:
- 1 <= snapshots.length <= 100000
- 0 <= snapshots[i].length <= 1000
- 0 <= fileID <= 1000000000
- The sum of all snapshots[i].length over the entire input does not exceed 200000
- Each individual snapshot contains distinct file IDs

Example 1:
Input: snapshots = [[5,1,9],[3,4],[9,5,1],[4,3],[7]]
Output: [0,2]
Explanation: Snapshot 0 and snapshot 2 contain the same file IDs {1,5,9}. Although snapshot 1 and snapshot 3 also match, index 2 is the earliest repeated occurrence.

Example 2:
Input: snapshots = [[],[8],[2,6],[6,2],[]]
Output: [1,3]
Explanation: Snapshot 0 and snapshot 4 are identical, but [1,3] is chosen because j = 3 is smaller than j = 4. Snapshot 1 is not repeated. Snapshot 2 and snapshot 3 both represent the set {2,6}.

Return the answer as a length-2 array. A typical approach is to convert each snapshot into a canonical representation, such as a sorted tuple or delimiter-safe string, then use a hash map to store the first index where each normalized state appeared.
*/

public class Solution {

    /**
     * Finds the earliest pair of indices [i, j] such that snapshots[i] and snapshots[j]
     * contain exactly the same file IDs, ignoring order.
     *
     * The key idea is:
     * 1. Convert each snapshot into a canonical form so that equal sets produce the same key.
     * 2. Use a hash map to remember the first index where each canonical form appeared.
     * 3. Scan from left to right. The first time we encounter a repeated canonical form,
     *    that current index is the smallest possible j, because we are processing snapshots
     *    in chronological order.
     * 4. For that repeated canonical form, the stored index is the earliest i for that state,
     *    because we only store the first occurrence.
     *
     * Canonical form used here:
     * - Copy the snapshot
     * - Sort the copy
     * - Build a delimiter-safe string such as "1#5#9#"
     *
     * This works because:
     * - Order inside a snapshot does not matter
     * - Sorting removes order differences
     * - The delimiter prevents ambiguity between values like [1, 23] and [12, 3]
     *
     * @param snapshots a 2D array where snapshots[k] contains the file IDs present in the folder at minute k
     * @return a length-2 array [i, j] representing the earliest repeated folder state; returns [-1, -1] if no state repeats
     * Time complexity: O(totalElements * log(maxSnapshotLength)) in the worst case due to sorting each snapshot
     * Space complexity: O(totalElements) for canonical keys stored in the hash map, plus O(maxSnapshotLength) temporary space per snapshot copy
     */
    public int[] earliestRepeatedSnapshot(int[][] snapshots) {
        // This map stores:
        // key   -> canonical representation of a snapshot
        // value -> earliest index where that exact folder state first appeared
        Map<String, Integer> firstSeenIndex = new HashMap<>();

        // We process snapshots from left to right.
        // This is extremely important for correctness:
        // - The first repeated state we detect will automatically have the smallest possible j.
        // - Because we store only the first occurrence of each state, the associated i is also minimal.
        for (int j = 0; j < snapshots.length; j++) {
            // Convert the current snapshot into a normalized representation.
            // If two snapshots contain the same set of file IDs, regardless of order,
            // they will produce the same canonical key.
            String key = canonicalKey(snapshots[j]);

            // If we have seen this key before, then we found a repeated folder state.
            if (firstSeenIndex.containsKey(key)) {
                // The earliest index where this exact state appeared.
                int i = firstSeenIndex.get(key);

                // Because we scan j from left to right, this is the smallest possible j.
                // Because we stored only the first occurrence, this is the smallest possible i
                // for this particular j and state.
                return new int[] { i, j };
            }

            // Otherwise, record the first time we see this state.
            // We do NOT overwrite existing values, because the problem wants the smallest i.
            firstSeenIndex.put(key, j);
        }

        // If we finish the scan without finding any repeated state,
        // then no snapshot appears more than once.
        return new int[] { -1, -1 };
    }

    /**
     * Builds a canonical, delimiter-safe string key for one snapshot.
     *
     * Step-by-step:
     * 1. Make a copy of the input array so we do not modify the original snapshot.
     * 2. Sort the copy so that any two snapshots with the same file IDs end up in the same order.
     * 3. Append each number followed by a delimiter to a StringBuilder.
     *
     * Example:
     * - [5, 1, 9] -> copy -> [5, 1, 9] -> sort -> [1, 5, 9] -> "1#5#9#"
     * - [9, 5, 1] -> copy -> [9, 5, 1] -> sort -> [1, 5, 9] -> "1#5#9#"
     *
     * Therefore both snapshots map to the same key.
     *
     * Empty snapshot:
     * - [] -> ""
     * This is fine because all empty snapshots should be considered identical.
     *
     * @param snapshot one folder snapshot containing distinct file IDs
     * @return a canonical string representation that is identical for snapshots with the same set of file IDs
     * Time complexity: O(m log m), where m is snapshot.length, due to sorting
     * Space complexity: O(m) for the copied array and the generated key
     */
    public String canonicalKey(int[] snapshot) {
        // Create a copy so the original input remains unchanged.
        int[] copy = Arrays.copyOf(snapshot, snapshot.length);

        // Sort the copied snapshot.
        // After sorting, snapshots with the same elements will have the same order.
        Arrays.sort(copy);

        // Build a delimiter-safe string representation.
        // We use '#' after every number to avoid accidental collisions.
        StringBuilder sb = new StringBuilder();

        // Append each sorted file ID followed by a delimiter.
        for (int fileId : copy) {
            sb.append(fileId).append('#');
        }

        return sb.toString();
    }

    /**
     * Utility method to convert an int array into a readable string for printing.
     *
     * @param arr the array to print
     * @return a human-readable string such as "[0, 2]"
     * Time complexity: O(n)
     * Space complexity: O(n) for the resulting string
     */
    public String arrayToString(int[] arr) {
        return Arrays.toString(arr);
    }

    /**
     * Utility method to convert a 2D int array into a readable string for printing.
     *
     * @param matrix the 2D array to print
     * @return a human-readable string such as "[[5, 1, 9], [3, 4], [9, 5, 1]]"
     * Time complexity: O(total number of elements)
     * Space complexity: O(total number of characters in the output string)
     */
    public String matrixToString(int[][] matrix) {
        return Arrays.deepToString(Arrays.stream(matrix).boxed().toArray());
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size across the demo cases)
     * Space complexity: O(total input size across the demo cases)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // snapshots[0] = [5,1,9] -> canonical key "1#5#9#"
        // snapshots[1] = [3,4]   -> canonical key "3#4#"
        // snapshots[2] = [9,5,1] -> canonical key "1#5#9#" -> repeats snapshot 0
        // Therefore the earliest repeated pair is [0,2].
        int[][] snapshots1 = {
            {5, 1, 9},
            {3, 4},
            {9, 5, 1},
            {4, 3},
            {7}
        };
        int[] result1 = solution.earliestRepeatedSnapshot(snapshots1);
        System.out.println("Example 1 Input: " + Arrays.deepToString(snapshots1));
        System.out.println("Example 1 Output: " + solution.arrayToString(result1));
        System.out.println("Expected: [0, 2]");
        System.out.println();

        // Example 2:
        // snapshots[0] = []      -> key ""
        // snapshots[1] = [8]     -> key "8#"
        // snapshots[2] = [2,6]   -> key "2#6#"
        // snapshots[3] = [6,2]   -> key "2#6#" -> repeats snapshot 2
        // snapshots[4] = []      -> key ""     -> repeats snapshot 0, but j=4 is later than j=3
        // Therefore the correct answer is [2,3].
        //
        // Note:
        // The problem statement's Example 2 says output [1,3], but that is inconsistent because
        // snapshot 1 is [8] and snapshot 3 is [6,2], which are not identical.
        // Based on the stated rules, the correct output is [2,3].
        int[][] snapshots2 = {
            {},
            {8},
            {2, 6},
            {6, 2},
            {}
        };
        int[] result2 = solution.earliestRepeatedSnapshot(snapshots2);
        System.out.println("Example 2 Input: " + Arrays.deepToString(snapshots2));
        System.out.println("Example 2 Output: " + solution.arrayToString(result2));
        System.out.println("Correct by problem rules: [2, 3]");
        System.out.println();

        // Additional demo: no repeated snapshot
        int[][] snapshots3 = {
            {1},
            {2},
            {3, 4},
            {},
            {5, 6, 7}
        };
        int[] result3 = solution.earliestRepeatedSnapshot(snapshots3);
        System.out.println("Additional Input: " + Arrays.deepToString(snapshots3));
        System.out.println("Additional Output: " + solution.arrayToString(result3));
        System.out.println("Expected: [-1, -1]");
    }
}