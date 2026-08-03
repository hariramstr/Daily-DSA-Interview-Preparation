import java.util.*;

/*
 * Title: Minimum Repaints to Form Three Color Districts
 * Difficulty: Hard
 * Topic: Arrays
 *
 * Problem Description:
 * A city boulevard is decorated with a row of buildings, represented by a string colors of length n.
 * Each character is one of 'R', 'G', or 'B', indicating the current paint color of a building.
 * The mayor wants the boulevard to be divided into exactly three contiguous non-empty districts
 * from left to right:
 *   1) the first district must be entirely red ('R')
 *   2) the second district must be entirely green ('G')
 *   3) the third district must be entirely blue ('B')
 *
 * In one operation, you may repaint any single building to any of the three colors.
 * Return the minimum number of repaint operations required to transform the boulevard into a valid
 * arrangement of the form:
 *
 *   R...R G...G B...B
 *
 * where all three districts are contiguous and each district contains at least one building.
 *
 * Constraints:
 * - 3 <= n <= 200000
 * - colors.length == n
 * - colors[i] is one of 'R', 'G', or 'B'
 *
 * Example 1:
 * Input: colors = "RGRBB"
 * Output: 1
 * Explanation:
 * One optimal split is:
 *   "R" | "G" | "RBB"
 * Repaint the first building of the third district from 'R' to 'B', producing "RGBBB".
 *
 * Example 2:
 * Input: colors = "BBRGRG"
 * Output: 3
 *
 * Notes:
 * - The split points are not given and must be chosen optimally.
 * - Every building must belong to exactly one of the three districts.
 * - A brute-force check over all pairs of split points is too slow for the largest constraints,
 *   so an O(n) or O(n log n) solution is expected.
 */

public class Solution {

    /**
     * Computes the minimum number of repaint operations needed to transform the given boulevard
     * into exactly three contiguous non-empty districts in this order:
     * all 'R', then all 'G', then all 'B'.
     *
     * Core idea:
     * We choose two split points:
     * - first district:  [0 .. i]
     * - second district: [i+1 .. j]
     * - third district:  [j+1 .. n-1]
     *
     * with 0 <= i < j < n-1 so that all three districts are non-empty.
     *
     * For any fixed split, the repaint cost is:
     * - number of non-'R' characters in the first district
     * - plus number of non-'G' characters in the second district
     * - plus number of non-'B' characters in the third district
     *
     * To compute these costs efficiently for all possible splits, we build prefix counts for
     * each color. Then each segment cost can be answered in O(1), and we scan all valid middle
     * boundaries in O(n) total using a running minimum.
     *
     * @param colors the input string of building colors, containing only 'R', 'G', and 'B'
     * @return the minimum repaint operations required
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public int minimumRepaints(String colors) {
        int n = colors.length();

        // Prefix count arrays:
        // prefixR[i] = number of 'R' characters in colors[0 .. i-1]
        // prefixG[i] = number of 'G' characters in colors[0 .. i-1]
        // prefixB[i] = number of 'B' characters in colors[0 .. i-1]
        //
        // We use length n + 1 so that:
        // count of color X in [l .. r] = prefixX[r + 1] - prefixX[l]
        int[] prefixR = new int[n + 1];
        int[] prefixG = new int[n + 1];
        int[] prefixB = new int[n + 1];

        // Build prefix counts one character at a time.
        for (int i = 0; i < n; i++) {
            prefixR[i + 1] = prefixR[i];
            prefixG[i + 1] = prefixG[i];
            prefixB[i + 1] = prefixB[i];

            char c = colors.charAt(i);
            if (c == 'R') {
                prefixR[i + 1]++;
            } else if (c == 'G') {
                prefixG[i + 1]++;
            } else {
                prefixB[i + 1]++;
            }
        }

        // We will scan possible positions for the end of the second district.
        //
        // Let j be the end index of the second district.
        // Then:
        // - first district is [0 .. i]
        // - second district is [i+1 .. j]
        // - third district is [j+1 .. n-1]
        //
        // For each j, we need the best possible i where 0 <= i < j.
        //
        // Rearranging the total cost:
        //
        // cost(i, j) =
        //   cost to make [0..i] all R
        // + cost to make [i+1..j] all G
        // + cost to make [j+1..n-1] all B
        //
        // The third part depends only on j.
        // The first two parts can be rewritten so we can maintain a running minimum.
        //
        // Specifically:
        // costRPrefix(i) = (length of [0..i]) - countR(0, i)
        //                = (i + 1) - countR(0, i)
        //
        // costGSegment(i+1, j) = (length of [i+1..j]) - countG(i+1, j)
        //                      = (j - i) - (countG(0, j) - countG(0, i))
        //
        // Summing and simplifying:
        // costRPrefix(i) + costGSegment(i+1, j)
        // = [(i+1) - countR(0,i)] + [(j-i) - countG(0,j) + countG(0,i)]
        // = (j+1) - countG(0,j) + [countG(0,i) - countR(0,i)]
        //
        // So for each j, we need:
        // min over i in [0 .. j-1] of [countG(0,i) - countR(0,i)]
        //
        // We can maintain that minimum while scanning j from left to right.
        int answer = Integer.MAX_VALUE;

        // This variable stores:
        // minValue = minimum of (prefixG[i+1] - prefixR[i+1]) over valid i seen so far
        //
        // Why prefix index i+1?
        // Because prefix arrays are 1-based with respect to string positions:
        // count in [0..i] is prefixX[i+1].
        //
        // Initially, before processing j = 1, the only valid i is 0.
        int minValue = prefixG[1] - prefixR[1];

        // j is the end index of the second district.
        // Since all three districts must be non-empty:
        // - second district must have at least one element, so j >= 1
        // - third district must have at least one element, so j <= n - 2
        for (int j = 1; j <= n - 2; j++) {

            // Before evaluating this j, ensure minValue contains all valid i in [0 .. j-1].
            // For j = 1, minValue already corresponds to i = 0.
            // For larger j, we add candidate i = j-1 at the end of the previous iteration,
            // so minValue is always ready here.

            // Cost to make suffix [j+1 .. n-1] all B:
            // suffix length = n - (j+1)
            // repaint count = suffix length - number of B in suffix
            int suffixLength = n - (j + 1);
            int countBInSuffix = prefixB[n] - prefixB[j + 1];
            int costSuffixBlue = suffixLength - countBInSuffix;

            // Cost for first two districts using the best split i:
            // (j + 1) - countG(0, j) + minValue
            int countGInPrefixToJ = prefixG[j + 1];
            int costFirstTwo = (j + 1) - countGInPrefixToJ + minValue;

            int totalCost = costFirstTwo + costSuffixBlue;
            answer = Math.min(answer, totalCost);

            // Prepare minValue for the next j.
            // When moving from current j to next j+1, the new valid i range becomes [0 .. j].
            // So we must include i = j as a candidate.
            int candidate = prefixG[j + 1] - prefixR[j + 1];
            minValue = Math.min(minValue, candidate);
        }

        return answer;
    }

    /**
     * Helper method that computes the minimum repaint operations using a straightforward
     * prefix-sum formula over all split pairs. This method is less optimized than the main
     * method but still useful for understanding and validation.
     *
     * It checks every valid pair of split points:
     * - first district:  [0 .. i]
     * - second district: [i+1 .. j]
     * - third district:  [j+1 .. n-1]
     *
     * This is O(n^2), so it is only suitable for small inputs and demonstration/testing.
     *
     * @param colors the input string of building colors
     * @return the minimum repaint operations required
     * Time complexity: O(n^2)
     * Space complexity: O(n)
     */
    public int minimumRepaintsBruteForceForValidation(String colors) {
        int n = colors.length();

        int[] prefixR = new int[n + 1];
        int[] prefixG = new int[n + 1];
        int[] prefixB = new int[n + 1];

        for (int i = 0; i < n; i++) {
            prefixR[i + 1] = prefixR[i];
            prefixG[i + 1] = prefixG[i];
            prefixB[i + 1] = prefixB[i];

            char c = colors.charAt(i);
            if (c == 'R') {
                prefixR[i + 1]++;
            } else if (c == 'G') {
                prefixG[i + 1]++;
            } else {
                prefixB[i + 1]++;
            }
        }

        int answer = Integer.MAX_VALUE;

        for (int i = 0; i <= n - 3; i++) {
            for (int j = i + 1; j <= n - 2; j++) {
                int len1 = i + 1;
                int countR = prefixR[i + 1];
                int cost1 = len1 - countR;

                int len2 = j - i;
                int countG = prefixG[j + 1] - prefixG[i + 1];
                int cost2 = len2 - countG;

                int len3 = n - (j + 1);
                int countB = prefixB[n] - prefixB[j + 1];
                int cost3 = len3 - countB;

                answer = Math.min(answer, cost1 + cost2 + cost3);
            }
        }

        return answer;
    }

    /**
     * Runs a few demonstration test cases and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total length of demonstrated inputs)
     * Space complexity: O(max input length among demonstrated inputs)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String colors1 = "RGRBB";
        int result1 = solution.minimumRepaints(colors1);
        System.out.println("Input: " + colors1);
        System.out.println("Minimum repaints: " + result1);
        System.out.println("Expected: 1");
        System.out.println();

        String colors2 = "BBRGRG";
        int result2 = solution.minimumRepaints(colors2);
        System.out.println("Input: " + colors2);
        System.out.println("Minimum repaints: " + result2);
        System.out.println("Expected: 3");
        System.out.println();

        String colors3 = "RGB";
        int result3 = solution.minimumRepaints(colors3);
        System.out.println("Input: " + colors3);
        System.out.println("Minimum repaints: " + result3);
        System.out.println("Expected: 0");
        System.out.println();

        String colors4 = "RRRGGGBBB";
        int result4 = solution.minimumRepaints(colors4);
        System.out.println("Input: " + colors4);
        System.out.println("Minimum repaints: " + result4);
        System.out.println("Expected: 0");
        System.out.println();

        String colors5 = "BBBBB";
        int result5 = solution.minimumRepaints(colors5);
        System.out.println("Input: " + colors5);
        System.out.println("Minimum repaints: " + result5);
        System.out.println();

        // Optional small validation: compare optimized and brute-force answers
        // on a few short strings to build confidence in correctness.
        String[] validationCases = {
            "RGRBB",
            "BBRGRG",
            "RGB",
            "RBB",
            "GRB",
            "BRG",
            "RRGGBB",
            "GBBRRG"
        };

        for (String s : validationCases) {
            int fast = solution.minimumRepaints(s);
            int slow = solution.minimumRepaintsBruteForceForValidation(s);
            System.out.println("Validate: " + s + " -> fast=" + fast + ", brute=" + slow);
        }
    }
}