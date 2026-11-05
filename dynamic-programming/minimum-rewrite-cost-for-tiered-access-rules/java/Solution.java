import java.util.*;

/*
Problem Title: Minimum Rewrite Cost for Tiered Access Rules

Problem Description:
A security team stores an access policy as a string s of length n, where each character is one of A, B, or C.
The company wants to rewrite the policy so that all rules appear in nondecreasing tier order:
first zero or more A rules, then zero or more B rules, then zero or more C rules.
Any of the three groups may be empty, but after rewriting the final string must have the form A...AB...BC...C.

For each position i, changing s[i] to A, B, or C has different costs given by arrays costA, costB, and costC,
where costX[i] is the cost to rewrite character s[i] into tier X.
Keeping the original character is allowed and may still have cost 0 or a positive value depending on the input.

Task:
Compute the minimum total rewrite cost needed to make the entire policy valid.

Constraints:
- 1 <= n <= 200000
- s.length == n
- s[i] is one of A, B, C
- 0 <= costA[i], costB[i], costC[i] <= 10^9
- Return the minimum possible total cost as a 64-bit integer

Examples:
1)
s = "CBAC"
costA = [3,0,2,4]
costB = [1,5,0,2]
costC = [0,3,4,0]
Output: 2

2)
s = "ACBAA"
costA = [0,4,3,0,0]
costB = [2,1,1,2,2]
costC = [5,0,0,3,4]
Output: 3
*/

public class Solution {

    /**
     * Computes the minimum total rewrite cost so that the final string is in the form:
     * zero or more 'A', followed by zero or more 'B', followed by zero or more 'C'.
     *
     * Core idea:
     * We process the string from left to right and maintain dynamic programming states
     * representing which ordered phase we are currently in:
     *
     * dpA = minimum cost to make the processed prefix consist only of A's
     * dpB = minimum cost to make the processed prefix consist of A's then B's
     * dpC = minimum cost to make the processed prefix consist of A's then B's then C's
     *
     * At each position, we decide what final character that position becomes:
     * - If it becomes 'A', we must still be in phase A.
     * - If it becomes 'B', we may come from phase A or stay in phase B.
     * - If it becomes 'C', we may come from phase A, phase B, or stay in phase C.
     *
     * Because empty groups are allowed, the final answer is min(dpA, dpB, dpC)
     * after processing all positions.
     *
     * @param s the original policy string; each character is 'A', 'B', or 'C'
     * @param costA costA[i] is the cost to rewrite position i into 'A'
     * @param costB costB[i] is the cost to rewrite position i into 'B'
     * @param costC costC[i] is the cost to rewrite position i into 'C'
     * @return the minimum possible total rewrite cost as a long
     * Time complexity: O(n), where n is the length of the string
     * Space complexity: O(1), excluding input storage
     */
    public long minimumRewriteCost(String s, int[] costA, int[] costB, int[] costC) {
        validateInput(s, costA, costB, costC);

        final int n = s.length();

        // dpA:
        // Minimum cost for the prefix processed so far if the final rewritten prefix
        // contains only 'A' characters.
        long dpA = 0L;

        // dpB:
        // Minimum cost for the prefix processed so far if the final rewritten prefix
        // is of the form A...AB...B.
        long dpB = 0L;

        // dpC:
        // Minimum cost for the prefix processed so far if the final rewritten prefix
        // is of the form A...AB...BC...C.
        long dpC = 0L;

        // We now scan every position from left to right.
        for (int i = 0; i < n; i++) {
            long toA = costA[i];
            long toB = costB[i];
            long toC = costC[i];

            // Save previous states before updating them.
            long prevA = dpA;
            long prevB = dpB;
            long prevC = dpC;

            // If current position becomes 'A':
            // We must remain in the all-A phase.
            // So the only valid previous state is prevA.
            long nextA = prevA + toA;

            // If current position becomes 'B':
            // We can either:
            // 1) start the B block now after an A-only prefix  -> prevA + toB
            // 2) continue an existing B block                  -> prevB + toB
            long nextB = Math.min(prevA, prevB) + toB;

            // If current position becomes 'C':
            // We can either:
            // 1) start the C block directly after A-only prefix -> prevA + toC
            // 2) start/continue C after A...B prefix            -> prevB + toC
            // 3) continue existing C block                      -> prevC + toC
            long nextC = Math.min(prevA, Math.min(prevB, prevC)) + toC;

            // Commit updates for the next iteration.
            dpA = nextA;
            dpB = nextB;
            dpC = nextC;
        }

        // Since any of the three groups may be empty, the final valid string may end in:
        // - phase A only
        // - phase B
        // - phase C
        return Math.min(dpA, Math.min(dpB, dpC));
    }

    /**
     * Validates the input according to the problem constraints.
     *
     * This method checks:
     * - non-null references
     * - equal lengths
     * - valid characters in the string
     *
     * @param s the original string
     * @param costA cost array for rewriting to 'A'
     * @param costB cost array for rewriting to 'B'
     * @param costC cost array for rewriting to 'C'
     * @return nothing; throws IllegalArgumentException if input is invalid
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public void validateInput(String s, int[] costA, int[] costB, int[] costC) {
        if (s == null || costA == null || costB == null || costC == null) {
            throw new IllegalArgumentException("Input string and cost arrays must not be null.");
        }

        int n = s.length();
        if (costA.length != n || costB.length != n || costC.length != n) {
            throw new IllegalArgumentException("All cost arrays must have the same length as the string.");
        }

        for (int i = 0; i < n; i++) {
            char ch = s.charAt(i);
            if (ch != 'A' && ch != 'B' && ch != 'C') {
                throw new IllegalArgumentException("String contains invalid character at index " + i + ": " + ch);
            }
        }
    }

    /**
     * Demonstrates the solution on the sample-style inputs and prints the results.
     *
     * Note:
     * The example explanations in the prompt are inconsistent with the provided arrays,
     * but this method runs the mathematically correct dynamic programming algorithm
     * on the exact given inputs.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(n) per demonstration case
     * Space complexity: O(1), excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String s1 = "CBAC";
        int[] costA1 = {3, 0, 2, 4};
        int[] costB1 = {1, 5, 0, 2};
        int[] costC1 = {0, 3, 4, 0};
        long result1 = solution.minimumRewriteCost(s1, costA1, costB1, costC1);
        System.out.println("Example 1 result: " + result1);

        String s2 = "ACBAA";
        int[] costA2 = {0, 4, 3, 0, 0};
        int[] costB2 = {2, 1, 1, 2, 2};
        int[] costC2 = {5, 0, 0, 3, 4};
        long result2 = solution.minimumRewriteCost(s2, costA2, costB2, costC2);
        System.out.println("Example 2 result: " + result2);

        // Additional small sanity checks for beginners:
        String s3 = "ABC";
        int[] costA3 = {0, 10, 10};
        int[] costB3 = {10, 0, 10};
        int[] costC3 = {10, 10, 0};
        long result3 = solution.minimumRewriteCost(s3, costA3, costB3, costC3);
        System.out.println("Sanity check 1 result: " + result3);

        String s4 = "CCC";
        int[] costA4 = {1, 1, 1};
        int[] costB4 = {2, 2, 2};
        int[] costC4 = {0, 0, 0};
        long result4 = solution.minimumRewriteCost(s4, costA4, costB4, costC4);
        System.out.println("Sanity check 2 result: " + result4);
    }
}