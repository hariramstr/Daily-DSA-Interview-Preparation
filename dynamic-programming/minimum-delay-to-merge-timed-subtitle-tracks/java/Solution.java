import java.util.*;

/*
 * Title: Minimum Delay to Merge Timed Subtitle Tracks
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A streaming platform receives a spoken sentence split across two subtitle tracks, A and B.
 * Each track is a string of lowercase letters, where each character represents one subtitle token in order.
 * You must build a final merged subtitle by repeatedly taking the next unused character from either A or B
 * until both tracks are exhausted. The relative order of characters taken from the same track must remain unchanged.
 *
 * However, switching between tracks causes a synchronization delay. If two consecutive chosen characters come
 * from different tracks, you pay a delay cost of 1. In addition, the platform dislikes repeated adjacent
 * characters in the final merged subtitle because they look visually noisy. For every pair of adjacent equal
 * characters in the merged string, you also pay a noise cost of 1.
 *
 * Your task is to compute the minimum total cost (switch delays + repeated-adjacent noise penalties)
 * over all valid merges of A and B.
 *
 * Formally, if the merged string is formed by interleaving A and B, then for each adjacent pair in the merge:
 * - add 1 if the source track changes,
 * - add 1 if the two adjacent characters are equal.
 *
 * Return the minimum possible total cost.
 *
 * Constraints:
 * - 1 <= |A|, |B| <= 2000
 * - A and B consist only of lowercase English letters
 * - An O(|A| * |B|) dynamic programming solution is expected
 *
 * Example 1:
 * Input: A = "ab", B = "ac"
 * Output: 2
 *
 * Example 2:
 * Input: A = "aaa", B = "aa"
 * Output: 4
 */

public class Solution {

    /**
     * A large value used as "infinity" for minimization DP.
     */
    private static final int INF = 1_000_000_000;

    /**
     * Computes the minimum total cost to merge two strings while preserving the internal order of each string.
     *
     * Core idea:
     * We build the merged string one character at a time.
     * At any point, suppose we have already used:
     * - i characters from A
     * - j characters from B
     *
     * Then the next state depends on which source produced the last chosen character:
     * - dpA[i][j]: minimum cost after using i chars from A and j chars from B, where the LAST chosen char came from A
     * - dpB[i][j]: minimum cost after using i chars from A and j chars from B, where the LAST chosen char came from B
     *
     * Why do we need to know the last source?
     * Because when we append the next character, we must know:
     * 1) whether the source changed (switch cost),
     * 2) what the previous character was, to check whether adjacent characters are equal (noise cost).
     *
     * Fortunately, if the last source is known:
     * - for dpA[i][j], the previous character is A[i - 1]
     * - for dpB[i][j], the previous character is B[j - 1]
     *
     * Transition examples:
     * From dpA[i][j], we can append:
     * - A[i] (stay on A): add equal-char penalty if A[i - 1] == A[i]
     * - B[j] (switch to B): add 1 for switching, plus equal-char penalty if A[i - 1] == B[j]
     *
     * Similarly from dpB[i][j].
     *
     * Initialization:
     * The very first chosen character has no previous neighbor, so it contributes 0 cost.
     * Therefore:
     * - if A is non-empty, starting with A[0] gives dpA[1][0] = 0
     * - if B is non-empty, starting with B[0] gives dpB[0][1] = 0
     *
     * Final answer:
     * After all characters are used, the result is:
     * min(dpA[n][m], dpB[n][m])
     *
     * To keep memory O(|B|), we process row by row over i and store only:
     * - previous row for states ending in A and B
     * - current row for states ending in A and B
     *
     * @param a the first subtitle track
     * @param b the second subtitle track
     * @return the minimum possible total cost over all valid merges
     * Time complexity: O(|A| * |B|)
     * Space complexity: O(|B|)
     */
    public int minimumMergeCost(String a, String b) {
        int n = a.length();
        int m = b.length();

        char[] A = a.toCharArray();
        char[] B = b.toCharArray();

        /*
         * prevA[j] = dpA[i-1][j] for the previous row
         * prevB[j] = dpB[i-1][j] for the previous row
         *
         * curA[j]  = dpA[i][j]   for the current row
         * curB[j]  = dpB[i][j]   for the current row
         */
        int[] prevA = new int[m + 1];
        int[] prevB = new int[m + 1];
        int[] curA = new int[m + 1];
        int[] curB = new int[m + 1];

        Arrays.fill(prevA, INF);
        Arrays.fill(prevB, INF);

        /*
         * Base initialization for i = 0 row:
         * We are using only characters from B.
         *
         * Starting with B[0]:
         * dpB[0][1] = 0
         *
         * Then if we continue taking from B only:
         * each adjacent pair contributes +1 if the characters are equal, and +0 switch cost
         * because source does not change.
         */
        if (m >= 1) {
            prevB[1] = 0;
            for (int j = 2; j <= m; j++) {
                prevB[j] = prevB[j - 1] + equalCost(B[j - 2], B[j - 1]);
            }
        }

        /*
         * Now iterate over i = 1..n.
         * For each row i, we compute states using exactly i chars from A and j chars from B.
         */
        for (int i = 1; i <= n; i++) {
            Arrays.fill(curA, INF);
            Arrays.fill(curB, INF);

            /*
             * First handle j = 0 separately:
             * We are using only characters from A.
             *
             * If i == 1:
             *   starting with A[0] gives cost 0
             * Else:
             *   extend dpA[i-1][0] by taking A[i-1]
             *   add equal-char penalty if A[i-2] == A[i-1]
             */
            if (i == 1) {
                curA[0] = 0;
            } else {
                curA[0] = prevA[0] + equalCost(A[i - 2], A[i - 1]);
            }

            /*
             * For j >= 1, compute both:
             * - curA[j] = dpA[i][j]
             * - curB[j] = dpB[i][j]
             */
            for (int j = 1; j <= m; j++) {
                /*
                 * Compute curA[j]:
                 * The last chosen character is A[i-1].
                 *
                 * There are two ways to arrive here:
                 *
                 * 1) Previous state also ended in A:
                 *    dpA[i-1][j] -> dpA[i][j]
                 *    No switch cost.
                 *    Add equal-char penalty if previous char A[i-2] equals current char A[i-1].
                 *
                 * 2) Previous state ended in B:
                 *    dpB[i-1][j] -> dpA[i][j]
                 *    Add switch cost 1.
                 *    Add equal-char penalty if previous char B[j-1] equals current char A[i-1].
                 */
                if (i == 1 && j == 0) {
                    // Not used here because j starts from 1.
                } else {
                    if (i > 1 && prevA[j] < INF) {
                        curA[j] = Math.min(curA[j], prevA[j] + equalCost(A[i - 2], A[i - 1]));
                    }
                    if (prevB[j] < INF) {
                        curA[j] = Math.min(curA[j], prevB[j] + 1 + equalCost(B[j - 1], A[i - 1]));
                    }
                }

                /*
                 * Compute curB[j]:
                 * The last chosen character is B[j-1].
                 *
                 * There are two ways to arrive here:
                 *
                 * 1) Previous state also ended in B:
                 *    dpB[i][j-1] -> dpB[i][j]
                 *    No switch cost.
                 *    Add equal-char penalty if B[j-2] == B[j-1].
                 *
                 * 2) Previous state ended in A:
                 *    dpA[i][j-1] -> dpB[i][j]
                 *    Add switch cost 1.
                 *    Add equal-char penalty if A[i-1] == B[j-1].
                 */
                if (j > 1 && curB[j - 1] < INF) {
                    curB[j] = Math.min(curB[j], curB[j - 1] + equalCost(B[j - 2], B[j - 1]));
                }
                if (curA[j - 1] < INF) {
                    curB[j] = Math.min(curB[j], curA[j - 1] + 1 + equalCost(A[i - 1], B[j - 1]));
                }
            }

            /*
             * Move current row into previous row for the next iteration.
             */
            int[] tempA = prevA;
            prevA = curA;
            curA = tempA;

            int[] tempB = prevB;
            prevB = curB;
            curB = tempB;
        }

        return Math.min(prevA[m], prevB[m]);
    }

    /**
     * Returns the adjacency noise cost between two consecutive characters.
     * The cost is 1 if the characters are equal, otherwise 0.
     *
     * @param x the previous character
     * @param y the current character
     * @return 1 if x == y, otherwise 0
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int equalCost(char x, char y) {
        return x == y ? 1 : 0;
    }

    /**
     * Demonstrates the solution on the sample inputs and a few extra checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input sizes used in the demo)
     * Space complexity: O(max length of B among demo cases)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String a1 = "ab";
        String b1 = "ac";
        int result1 = solution.minimumMergeCost(a1, b1);
        System.out.println("A = \"" + a1 + "\", B = \"" + b1 + "\" -> " + result1);

        String a2 = "aaa";
        String b2 = "aa";
        int result2 = solution.minimumMergeCost(a2, b2);
        System.out.println("A = \"" + a2 + "\", B = \"" + b2 + "\" -> " + result2);

        String a3 = "a";
        String b3 = "b";
        int result3 = solution.minimumMergeCost(a3, b3);
        System.out.println("A = \"" + a3 + "\", B = \"" + b3 + "\" -> " + result3);

        String a4 = "abc";
        String b4 = "def";
        int result4 = solution.minimumMergeCost(a4, b4);
        System.out.println("A = \"" + a4 + "\", B = \"" + b4 + "\" -> " + result4);
    }
}