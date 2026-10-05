import java.util.*;

/*
 * Title: Minimum Cost to Rebuild a Damaged DNA Template
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A genetics lab stores a reference DNA template as a string target consisting only of the
 * characters 'A', 'C', 'G', and 'T'. After a storage failure, the lab can no longer access
 * the original template directly, but it still has a collection of DNA fragments.
 *
 * Each fragment fragments[i] can be used any number of times, and using it once adds a fixed
 * assembly cost cost[i]. A fragment may be placed only if it exactly matches the corresponding
 * substring of target at the chosen position. The final reconstructed sequence must equal
 * target exactly, with no extra characters and no mismatches.
 *
 * Your task is to compute the minimum total cost required to reconstruct the entire target.
 * If it is impossible, return -1.
 *
 * Fragments may overlap in the input set, and different fragments may have the same string
 * but different costs. Because fragments can be reused, choosing a locally cheapest fragment
 * does not always lead to a globally optimal answer. An efficient dynamic programming solution
 * is required.
 *
 * Constraints:
 * - 1 <= target.length <= 10^5
 * - 1 <= fragments.length <= 10^4
 * - 1 <= fragments[i].length <= 200
 * - sum(fragments[i].length) <= 2 * 10^5
 * - 1 <= cost[i] <= 10^9
 * - target and every fragment contain only 'A', 'C', 'G', and 'T'
 *
 * Key observation:
 * This is a minimum-cost exact segmentation problem.
 * If dp[i] is the minimum cost to build the prefix target[0..i-1], then:
 *
 *   dp[i + len(fragment)] = min(dp[i + len(fragment)], dp[i] + cost(fragment))
 *
 * whenever the fragment matches target starting at position i.
 *
 * To perform matching efficiently for many fragments and many positions, we build a Trie of
 * all unique fragment strings, keeping only the minimum cost for duplicate strings.
 * Then from each reachable position i, we walk forward in the Trie while scanning target,
 * and every time we reach a terminal Trie node, we update the DP state.
 */

public class Solution {

    /**
     * Trie node used to store DNA fragments.
     *
     * Each edge corresponds to one of the four DNA characters:
     * A, C, G, T.
     *
     * If a node represents the end of a fragment, endCost stores the minimum cost among all
     * identical fragment strings from the input.
     */
    private static class TrieNode {
        TrieNode[] next = new TrieNode[4];
        long endCost = Long.MAX_VALUE;
    }

    /**
     * Computes the minimum total cost to reconstruct the target exactly using the given fragments.
     *
     * Approach:
     * 1. Deduplicate identical fragment strings by keeping only the minimum cost for each string.
     * 2. Insert all unique fragments into a Trie.
     * 3. Use dynamic programming:
     *    - dp[i] = minimum cost to build target prefix of length i
     *    - Start with dp[0] = 0
     *    - For each reachable position i, walk through the Trie while scanning target from i onward
     *    - Whenever a Trie terminal node is reached, update dp[endPosition]
     *
     * Why this works:
     * - Every valid reconstruction is a sequence of fragments that partitions the target exactly.
     * - The DP considers every reachable prefix and every fragment that can start there.
     * - Since each transition adds the fragment cost, the minimum over all such transitions gives
     *   the optimal answer.
     *
     * @param target the DNA template that must be reconstructed exactly
     * @param fragments the available DNA fragments, each reusable any number of times
     * @param cost the assembly cost for each corresponding fragment
     * @return the minimum total cost to form target exactly, or -1 if impossible
     * @implNote Time complexity: O(S + n * L), where S is the total fragment length and
     *           L is the maximum fragment length (<= 200). More precisely, each reachable
     *           position scans at most 200 characters through the Trie.
     * @implNote Space complexity: O(S + n), for the Trie and DP array.
     */
    public long minimumCost(String target, String[] fragments, int[] cost) {
        int n = target.length();

        // Step 1:
        // Deduplicate identical fragment strings.
        //
        // If the same fragment string appears multiple times with different costs,
        // only the cheapest one matters because fragments can be reused unlimited times.
        //
        // Example:
        // fragments = ["AC", "AC"], cost = [4, 2]
        // We only need "AC" with cost 2.
        Map<String, Integer> minCostByFragment = new HashMap<>();
        for (int i = 0; i < fragments.length; i++) {
            String fragment = fragments[i];
            int c = cost[i];
            minCostByFragment.merge(fragment, c, Math::min);
        }

        // Step 2:
        // Build a Trie from all unique fragments.
        //
        // This allows us to efficiently test all fragments that match target starting
        // at a given position, by walking character-by-character through target.
        TrieNode root = new TrieNode();
        int maxLen = 0;
        for (Map.Entry<String, Integer> entry : minCostByFragment.entrySet()) {
            insert(root, entry.getKey(), entry.getValue());
            maxLen = Math.max(maxLen, entry.getKey().length());
        }

        // Step 3:
        // Dynamic programming array.
        //
        // dp[i] = minimum cost to reconstruct target[0..i-1]
        //
        // We use a large INF value to represent "unreachable".
        long INF = Long.MAX_VALUE / 4;
        long[] dp = new long[n + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0;

        // Step 4:
        // Process each prefix position.
        //
        // If dp[i] is unreachable, we cannot start a fragment there as part of a valid
        // full reconstruction, so we skip it.
        //
        // Otherwise, we walk forward in the Trie while reading target characters from i.
        // Every time we hit a terminal Trie node, that means a fragment matches exactly
        // starting at i and ending at the current position.
        for (int i = 0; i < n; i++) {
            if (dp[i] == INF) {
                continue;
            }

            TrieNode current = root;

            // We never need to scan beyond the maximum fragment length.
            // This keeps the per-position work bounded by at most 200.
            int limit = Math.min(n, i + maxLen);

            for (int j = i; j < limit; j++) {
                int idx = charToIndex(target.charAt(j));

                // If there is no Trie edge for this character, then no longer fragment
                // can match starting at position i, so we stop immediately.
                current = current.next[idx];
                if (current == null) {
                    break;
                }

                // If current node marks the end of a fragment, then target[i..j]
                // is a valid fragment placement.
                //
                // Transition:
                // dp[j + 1] = min(dp[j + 1], dp[i] + fragmentCost)
                if (current.endCost != Long.MAX_VALUE) {
                    long candidate = dp[i] + current.endCost;
                    if (candidate < dp[j + 1]) {
                        dp[j + 1] = candidate;
                    }
                }
            }
        }

        return dp[n] == INF ? -1 : dp[n];
    }

    /**
     * Inserts a fragment into the Trie, storing the minimum cost at the terminal node.
     *
     * @param root the root of the Trie
     * @param word the fragment string to insert
     * @param cost the minimum cost associated with this fragment string
     * @return nothing
     * @implNote Time complexity: O(word.length())
     * @implNote Space complexity: O(word.length()) in the worst case for newly created nodes
     */
    public void insert(TrieNode root, String word, int cost) {
        TrieNode current = root;
        for (int i = 0; i < word.length(); i++) {
            int idx = charToIndex(word.charAt(i));
            if (current.next[idx] == null) {
                current.next[idx] = new TrieNode();
            }
            current = current.next[idx];
        }
        current.endCost = Math.min(current.endCost, cost);
    }

    /**
     * Converts a DNA character into a compact Trie index.
     *
     * Mapping:
     * A -> 0
     * C -> 1
     * G -> 2
     * T -> 3
     *
     * @param ch the DNA character
     * @return the corresponding index in [0, 3]
     * @implNote Time complexity: O(1)
     * @implNote Space complexity: O(1)
     */
    public int charToIndex(char ch) {
        return switch (ch) {
            case 'A' -> 0;
            case 'C' -> 1;
            case 'G' -> 2;
            case 'T' -> 3;
            default -> throw new IllegalArgumentException("Invalid DNA character: " + ch);
        };
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * @implNote Time complexity: O(total work of the demonstrated test cases)
     * @implNote Space complexity: O(size of each test case)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String target1 = "ACGTAC";
        String[] fragments1 = {"AC", "CGT", "GT", "AC"};
        int[] cost1 = {4, 5, 3, 2};
        long result1 = solution.minimumCost(target1, fragments1, cost1);
        System.out.println("Example 1 Output: " + result1); // Expected: 7

        String target2 = "AAGT";
        String[] fragments2 = {"AA", "AG", "GT"};
        int[] cost2 = {3, 4, 2};
        long result2 = solution.minimumCost(target2, fragments2, cost2);
        System.out.println("Example 2 Output: " + result2); // Expected: 5

        // Additional quick sanity checks.
        String target3 = "ACG";
        String[] fragments3 = {"A", "CG", "AC"};
        int[] cost3 = {2, 3, 10};
        long result3 = solution.minimumCost(target3, fragments3, cost3);
        System.out.println("Additional Test 1 Output: " + result3); // Expected: 5

        String target4 = "ACGT";
        String[] fragments4 = {"AC", "G"};
        int[] cost4 = {1, 1};
        long result4 = solution.minimumCost(target4, fragments4, cost4);
        System.out.println("Additional Test 2 Output: " + result4); // Expected: -1
    }
}