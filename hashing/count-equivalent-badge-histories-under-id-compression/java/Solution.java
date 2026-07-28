import java.util.*;

/*
Title: Count Equivalent Badge Histories Under ID Compression
Difficulty: Hard
Topic: Hashing

Problem Description:
A company stores each employee's badge scan history as an integer array. Two histories are considered equivalent if they have the same repetition structure after compressing badge IDs by first appearance order. For example, the histories [42, 99, 42, 17] and [7, 3, 7, 8] are equivalent because both compress to [0, 1, 0, 2]. Likewise, [5, 5, 8] compresses to [0, 0, 1], which is different from [5, 8, 5] compressing to [0, 1, 0].

You are given n badge histories, where the i-th history is an array of integers and histories may have different lengths. Return the number of unordered pairs of histories that are equivalent under this compression rule.

Because badge IDs may be very large or negative, solutions that rely on value ranges are not acceptable. An efficient solution should build a canonical representation or hash for each history and count how many times the same pattern appears.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= total number of scanned IDs across all histories <= 2 * 10^5
- -10^9 <= badgeID <= 10^9
- 1 <= length of each history
- Return the answer as a 64-bit integer

Example 1:
Input: histories = [[42,99,42,17],[7,3,7,8],[5,5,8],[8,8,1],[10,11,10,12]]
Output: 4
Explanation: The first, second, and fifth histories all compress to [0,1,0,2], contributing 3 pairs. The third and fourth histories both compress to [0,0,1], contributing 1 pair. Total = 4.

Example 2:
Input: histories = [[1,2,1,2],[4,4,5,5],[9],[3,1,3],[8,6,8,7]]
The narrative in the prompt contains a correction note. For this exact list:
- [1,2,1,2] compresses to [0,1,0,1]
- [4,4,5,5] compresses to [0,0,1,1]
- [9] compresses to [0]
- [3,1,3] compresses to [0,1,0]
- [8,6,8,7] compresses to [0,1,0,2]
So there are no equivalent pairs and the correct output is 0.

Your task is to implement a function that computes this count in near-linear time with respect to the total input size.
*/

/**
 * A complete runnable solution for counting unordered pairs of badge histories
 * that become identical after compression by first appearance order.
 *
 * <p>The key idea is:
 * <ol>
 *     <li>Convert each history into its canonical compressed form.</li>
 *     <li>Use that compressed form as a key in a hash map.</li>
 *     <li>If the same compressed form appears k times, it contributes k * (k - 1) / 2 pairs.</li>
 * </ol>
 *
 * <p>This implementation is designed to be beginner-friendly and uses only standard Java libraries.
 */
public class Solution {

    /**
     * Counts the number of unordered pairs of histories that are equivalent under
     * first-appearance compression.
     *
     * <p>Example:
     * <pre>
     * [42, 99, 42, 17] -> [0, 1, 0, 2]
     * [7,  3,  7,  8 ] -> [0, 1, 0, 2]
     * </pre>
     * These two histories are equivalent.
     *
     * <p>How the algorithm works step by step:
     * <ol>
     *     <li>For each history, build its compressed pattern:
     *         <ul>
     *             <li>The first distinct value gets compressed ID 0.</li>
     *             <li>The next new distinct value gets compressed ID 1.</li>
     *             <li>And so on.</li>
     *         </ul>
     *     </li>
     *     <li>Store the compressed pattern in a hash map and count how many times it appears.</li>
     *     <li>Every time we see a pattern that has already appeared c times, it forms c new pairs
     *         with the previous histories having the same pattern.</li>
     * </ol>
     *
     * @param histories a 2D array where each inner array is one badge history
     * @return the number of unordered equivalent pairs as a 64-bit integer
     * Time complexity: O(T), where T is the total number of badge scans across all histories
     * Space complexity: O(T), in the worst case for storing canonical patterns and temporary maps
     */
    public long countEquivalentHistories(int[][] histories) {
        // This map stores:
        // key   -> canonical compressed representation of a history
        // value -> how many times we have seen that exact pattern so far
        Map<PatternKey, Integer> frequency = new HashMap<>();

        // We use long because the number of pairs can be large:
        // up to n * (n - 1) / 2, which does not fit safely in int for large n.
        long answer = 0L;

        // Process each history independently.
        for (int[] history : histories) {
            // Convert the raw integer history into its canonical compressed pattern.
            PatternKey key = buildPatternKey(history);

            // If this pattern has already appeared "seen" times,
            // then the current history forms exactly "seen" new unordered pairs
            // with those previous histories.
            int seen = frequency.getOrDefault(key, 0);
            answer += seen;

            // Record that we have now seen this pattern one more time.
            frequency.put(key, seen + 1);
        }

        return answer;
    }

    /**
     * Builds the canonical compressed representation of one history.
     *
     * <p>Example:
     * <pre>
     * history = [42, 99, 42, 17]
     *
     * first time seeing 42 -> assign 0
     * first time seeing 99 -> assign 1
     * 42 already seen      -> use 0
     * first time seeing 17 -> assign 2
     *
     * result = [0, 1, 0, 2]
     * </pre>
     *
     * <p>This compressed sequence is the canonical signature of the repetition structure.
     * Two histories are equivalent if and only if these compressed sequences are identical.
     *
     * @param history one badge history
     * @return a PatternKey wrapping the compressed sequence
     * Time complexity: O(m), where m is the length of the history
     * Space complexity: O(m), for the map of first appearances and the compressed array
     */
    public PatternKey buildPatternKey(int[] history) {
        // Map original badge ID -> compressed ID assigned by first appearance order.
        //
        // We use HashMap<Integer, Integer> because:
        // - badge IDs may be negative
        // - badge IDs may be very large
        // - we cannot rely on array indexing by value
        Map<Integer, Integer> idMap = new HashMap<>();

        // This array will hold the compressed pattern.
        int[] compressed = new int[history.length];

        // nextId is the next compressed ID to assign to a new badge value.
        int nextId = 0;

        // Walk through the history from left to right.
        for (int i = 0; i < history.length; i++) {
            int badgeId = history[i];

            // Check whether this badge ID has already appeared earlier in this history.
            Integer mapped = idMap.get(badgeId);

            if (mapped == null) {
                // This is the first time we see this badge ID in this history.
                // Assign the next available compressed ID.
                mapped = nextId;
                idMap.put(badgeId, nextId);
                nextId++;
            }

            // Store the compressed ID in the output pattern.
            compressed[i] = mapped;
        }

        return new PatternKey(compressed);
    }

    /**
     * Demonstrates the solution on sample inputs from the prompt and an additional corrected example.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(T) over the demonstrated inputs
     * Space complexity: O(T) over the demonstrated inputs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the prompt.
        int[][] histories1 = {
            {42, 99, 42, 17},
            {7, 3, 7, 8},
            {5, 5, 8},
            {8, 8, 1},
            {10, 11, 10, 12}
        };

        // Expected:
        // [42,99,42,17] -> [0,1,0,2]
        // [7,3,7,8]     -> [0,1,0,2]
        // [5,5,8]       -> [0,0,1]
        // [8,8,1]       -> [0,0,1]
        // [10,11,10,12] -> [0,1,0,2]
        //
        // Pattern [0,1,0,2] appears 3 times -> 3 choose 2 = 3 pairs
        // Pattern [0,0,1] appears 2 times   -> 2 choose 2 = 1 pair
        // Total = 4
        System.out.println(solution.countEquivalentHistories(histories1)); // 4

        // Example 2 as literally listed in the prompt.
        // The prompt text includes a correction note; for this exact input the correct answer is 0.
        int[][] histories2 = {
            {1, 2, 1, 2},
            {4, 4, 5, 5},
            {9},
            {3, 1, 3},
            {8, 6, 8, 7}
        };

        // Compressions:
        // [1,2,1,2] -> [0,1,0,1]
        // [4,4,5,5] -> [0,0,1,1]
        // [9]       -> [0]
        // [3,1,3]   -> [0,1,0]
        // [8,6,8,7] -> [0,1,0,2]
        //
        // No two are equal, so answer = 0.
        System.out.println(solution.countEquivalentHistories(histories2)); // 0

        // Additional corrected example that truly has one pair.
        int[][] histories3 = {
            {1, 2, 1, 2},
            {6, 7, 6, 7},
            {9},
            {3, 1, 3},
            {8, 6, 8, 7}
        };

        // Here:
        // [1,2,1,2] -> [0,1,0,1]
        // [6,7,6,7] -> [0,1,0,1]
        // So exactly one equivalent pair exists.
        System.out.println(solution.countEquivalentHistories(histories3)); // 1
    }

    /**
     * Immutable wrapper around an int[] compressed pattern so it can be used safely
     * as a key in a hash map.
     *
     * <p>Why this class is needed:
     * <ul>
     *     <li>In Java, raw arrays do not compare by contents in hash-based collections.</li>
     *     <li>Two int[] arrays with the same numbers are not equal unless they are the same object.</li>
     *     <li>So we wrap the array and define equals/hashCode using Arrays.equals / Arrays.hashCode.</li>
     * </ul>
     */
    public static final class PatternKey {
        private final int[] pattern;
        private final int hash;

        /**
         * Creates a key from a compressed pattern.
         *
         * @param pattern the compressed sequence
         * @return not applicable
         * Time complexity: O(m), where m is the pattern length, due to hash computation
         * Space complexity: O(1) auxiliary, not counting the referenced array
         */
        public PatternKey(int[] pattern) {
            this.pattern = pattern;
            this.hash = Arrays.hashCode(pattern);
        }

        /**
         * Compares this key with another object by compressed pattern contents.
         *
         * @param obj the other object
         * @return true if the other object is a PatternKey with the same pattern contents; false otherwise
         * Time complexity: O(m) in the worst case
         * Space complexity: O(1)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PatternKey other)) {
                return false;
            }
            return Arrays.equals(this.pattern, other.pattern);
        }

        /**
         * Returns the cached hash code of the compressed pattern.
         *
         * @param none no parameters
         * @return hash code based on pattern contents
         * Time complexity: O(1)
         * Space complexity: O(1)
         */
        @Override
        public int hashCode() {
            return hash;
        }
    }
}