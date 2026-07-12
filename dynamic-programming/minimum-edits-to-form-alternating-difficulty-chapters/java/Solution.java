import java.util.*;

/*
Problem Title: Minimum Edits to Form Alternating Difficulty Chapters

Problem Description:
You are given a string chapters of length n representing the draft order of book chapters.
Each character is either 'E' (easy) or 'H' (hard).

The publisher wants the final book to be split into exactly k non-empty contiguous chapter groups.
After grouping, each group must be made uniform, meaning every chapter in that group must have
the same difficulty label. In addition, the labels of adjacent groups must alternate:
if one group is all 'E', the next must be all 'H', then 'E' again, and so on.

You may edit any chapter by changing 'E' to 'H' or 'H' to 'E', and each such change costs 1.
Return the minimum total number of edits needed to transform the draft into exactly k alternating
uniform groups. If it is impossible, return -1.

Example:
- chapters = "EEHHE", k = 3
  Split as EE | HH | E, which already alternates and requires 0 edits.

- chapters = "EEE", k = 2
  We need two non-empty alternating groups, so at least one chapter must be changed.

Constraints:
- 1 <= n <= 2000
- 1 <= k <= n
- chapters[i] is either 'E' or 'H'
- Groups must be contiguous and non-empty
*/

public class Solution {

    /**
     * Computes the minimum number of edits needed to split the given chapter string
     * into exactly k non-empty contiguous groups such that:
     * 1) every group is uniform (all 'E' or all 'H')
     * 2) adjacent groups alternate in label
     *
     * Core idea:
     * We use dynamic programming over prefixes of the string.
     *
     * Let:
     * dpE[g][i] = minimum edits needed to partition the first i characters
     *             into exactly g alternating uniform groups,
     *             where the g-th (last) group is all 'E'
     *
     * dpH[g][i] = same idea, but the g-th (last) group is all 'H'
     *
     * Transition:
     * Suppose the last group starts at position j and ends at i (1-based prefix length),
     * so the last segment is chapters[j..i].
     *
     * If the last group is all 'E', then the previous group (if any) must be all 'H'.
     * So:
     * dpE[g][i] = min over j from g..i of dpH[g-1][j-1] + costToMakeE(j..i)
     *
     * Similarly:
     * dpH[g][i] = min over j from g..i of dpE[g-1][j-1] + costToMakeH(j..i)
     *
     * We optimize the transition from O(k * n^2) to O(k * n) by maintaining running minima:
     *
     * dpE[g][i] = prefixH[i] + min over t in [g-1..i-1] (dpH[g-1][t] - prefixH[t])
     * dpH[g][i] = prefixE[i] + min over t in [g-1..i-1] (dpE[g-1][t] - prefixE[t])
     *
     * where:
     * prefixE[i] = number of 'E' in first i characters
     * prefixH[i] = number of 'H' in first i characters
     *
     * Cost to make substring (t+1..i) all 'E' is number of 'H' in that substring:
     * prefixH[i] - prefixH[t]
     *
     * Cost to make substring (t+1..i) all 'H' is number of 'E' in that substring:
     * prefixE[i] - prefixE[t]
     *
     * @param chapters the original chapter difficulty string consisting only of 'E' and 'H'
     * @param k the exact number of contiguous non-empty alternating uniform groups required
     * @return the minimum number of edits needed, or -1 if impossible
     * Time complexity: O(n * k)
     * Space complexity: O(n)
     */
    public int minEdits(String chapters, int k) {
        int n = chapters.length();

        // If we need more groups than characters, it is impossible because
        // every group must be non-empty.
        if (k > n) {
            return -1;
        }

        // Prefix counts:
        // prefixE[i] = number of 'E' characters in chapters[0..i-1]
        // prefixH[i] = number of 'H' characters in chapters[0..i-1]
        //
        // These arrays let us compute the cost of converting any substring
        // into all 'E' or all 'H' in O(1) time.
        int[] prefixE = new int[n + 1];
        int[] prefixH = new int[n + 1];

        for (int i = 1; i <= n; i++) {
            char c = chapters.charAt(i - 1);
            prefixE[i] = prefixE[i - 1] + (c == 'E' ? 1 : 0);
            prefixH[i] = prefixH[i - 1] + (c == 'H' ? 1 : 0);
        }

        // We use a large number as "infinity" for impossible states.
        final int INF = 1_000_000_000;

        // Previous layer of DP for g-1 groups.
        int[] prevE = new int[n + 1];
        int[] prevH = new int[n + 1];
        Arrays.fill(prevE, INF);
        Arrays.fill(prevH, INF);

        // Base case: 0 groups can form only an empty prefix with cost 0.
        // There is no meaningful "last group label" when group count is 0,
        // but for transition convenience we allow both arrays at index 0 to be 0.
        prevE[0] = 0;
        prevH[0] = 0;

        // Build DP group by group.
        for (int g = 1; g <= k; g++) {
            int[] currE = new int[n + 1];
            int[] currH = new int[n + 1];
            Arrays.fill(currE, INF);
            Arrays.fill(currH, INF);

            // We maintain:
            // bestForE = min(prevH[t] - prefixH[t]) for valid t
            // bestForH = min(prevE[t] - prefixE[t]) for valid t
            //
            // Here t is the length of the prefix covered by the first g-1 groups.
            // Then the last group is substring (t+1 .. i).
            int bestForE = INF;
            int bestForH = INF;

            // To form exactly g non-empty groups using first i characters,
            // we must have i >= g.
            //
            // We iterate i from g to n.
            // Before computing dp for this i, we add t = i-1 as a possible split point.
            // Over the whole loop, this means all t in [g-1 .. i-1] are considered.
            for (int i = g; i <= n; i++) {
                int t = i - 1;

                // Update running minima with split point t.
                // This means:
                // - first g-1 groups cover first t characters
                // - last group starts at t+1
                if (prevH[t] < INF) {
                    bestForE = Math.min(bestForE, prevH[t] - prefixH[t]);
                }
                if (prevE[t] < INF) {
                    bestForH = Math.min(bestForH, prevE[t] - prefixE[t]);
                }

                // Compute current states:
                //
                // If last group is all 'E':
                // cost of making substring (t+1..i) all 'E' is number of 'H' there
                // = prefixH[i] - prefixH[t]
                //
                // After minimizing over t:
                // dpE[g][i] = prefixH[i] + min(prevH[t] - prefixH[t])
                if (bestForE < INF) {
                    currE[i] = prefixH[i] + bestForE;
                }

                // If last group is all 'H':
                // cost of making substring (t+1..i) all 'H' is number of 'E' there
                // = prefixE[i] - prefixE[t]
                //
                // After minimizing over t:
                // dpH[g][i] = prefixE[i] + min(prevE[t] - prefixE[t])
                if (bestForH < INF) {
                    currH[i] = prefixE[i] + bestForH;
                }
            }

            // Move current layer to previous for next iteration.
            prevE = currE;
            prevH = currH;
        }

        int answer = Math.min(prevE[n], prevH[n]);
        return answer >= INF ? -1 : answer;
    }

    /**
     * Helper method that computes the number of edits needed to convert a substring
     * into all 'E', using prefix counts.
     *
     * The substring is inclusive and uses 0-based indices.
     *
     * @param prefixH prefix count array for 'H'
     * @param left left index of substring, inclusive
     * @param right right index of substring, inclusive
     * @return number of edits needed to make chapters[left..right] all 'E'
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int costToMakeAllE(int[] prefixH, int left, int right) {
        return prefixH[right + 1] - prefixH[left];
    }

    /**
     * Helper method that computes the number of edits needed to convert a substring
     * into all 'H', using prefix counts.
     *
     * The substring is inclusive and uses 0-based indices.
     *
     * @param prefixE prefix count array for 'E'
     * @param left left index of substring, inclusive
     * @param right right index of substring, inclusive
     * @return number of edits needed to make chapters[left..right] all 'H'
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int costToMakeAllH(int[] prefixE, int left, int right) {
        return prefixE[right + 1] - prefixE[left];
    }

    /**
     * Demonstrates the solution on sample inputs and a few additional checks.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding calls to minEdits
     * Space complexity: O(1), excluding internal method usage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String chapters1 = "EEHHE";
        int k1 = 3;
        int result1 = solution.minEdits(chapters1, k1);
        System.out.println("chapters = " + chapters1 + ", k = " + k1 + " -> " + result1);
        // Expected: 0

        String chapters2 = "EHEEEH";
        int k2 = 4;
        int result2 = solution.minEdits(chapters2, k2);
        System.out.println("chapters = " + chapters2 + ", k = " + k2 + " -> " + result2);

        String chapters3 = "EEE";
        int k3 = 2;
        int result3 = solution.minEdits(chapters3, k3);
        System.out.println("chapters = " + chapters3 + ", k = " + k3 + " -> " + result3);
        // Expected: 1

        String chapters4 = "E";
        int k4 = 1;
        int result4 = solution.minEdits(chapters4, k4);
        System.out.println("chapters = " + chapters4 + ", k = " + k4 + " -> " + result4);
        // Expected: 0

        String chapters5 = "HHHH";
        int k5 = 4;
        int result5 = solution.minEdits(chapters5, k5);
        System.out.println("chapters = " + chapters5 + ", k = " + k5 + " -> " + result5);
        // Each single-character group must alternate, so two positions need edits. Expected: 2
    }
}