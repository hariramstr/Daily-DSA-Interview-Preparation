import java.util.*;

/*
Problem Title: Minimum Rewrite Cost for Chunked DNA Assembly

Problem Description:
A bioinformatics pipeline is assembling a target DNA string from a catalog of reusable fragments.
You are given a target string target of length n and a list of fragments parts, where each fragment
parts[i] has an associated non-negative rewrite cost cost[i].

You may build the target from left to right by repeatedly choosing any fragment and placing it over
the next uncovered portion of the target. A fragment can only be placed if its characters exactly
match the corresponding substring of target. Fragments may be reused any number of times.

However, each time you use a fragment whose length is different from the length of the fragment used
immediately before it, the assembly machine must be recalibrated. This adds an extra penalty
switchCost to that placement, except for the very first fragment, which never pays a switch penalty.

Return the minimum total cost needed to assemble the entire target exactly. If it is impossible,
return -1.

Formally, if you place fragments with indices f1, f2, ..., fk, such that their concatenation equals
target, then the total cost is:
cost[f1] + cost[f2] + ... + cost[fk] + switchCost * (# of i from 2..k where len(parts[fi]) != len(parts[f(i-1)]))

Constraints:
- 1 <= n <= 10^4
- 1 <= parts.length <= 2 * 10^4
- 1 <= parts[i].length <= 50
- sum(parts[i].length) <= 2 * 10^5
- 0 <= cost[i] <= 10^9
- 0 <= switchCost <= 10^9
- target and all parts[i] consist only of uppercase letters A, C, G, T
*/

public class Solution {

    /**
     * A very large value used as "infinity" for DP.
     * We keep it safely below Long.MAX_VALUE to avoid overflow when adding costs.
     */
    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Stores the cheapest available fragment cost for each exact fragment string.
     * If the same fragment text appears multiple times with different costs, only the minimum matters.
     */
    private static class FragmentDictionary {
        Map<String, Long> minCostByString = new HashMap<>();
    }

    /**
     * Solves the DNA assembly problem.
     *
     * Core idea:
     * 1) Deduplicate fragments by exact string, keeping only the minimum cost for each string.
     * 2) For every target position i and every possible fragment length len (1..50),
     *    determine whether target.substring(i, i+len) exists as a usable fragment and, if so,
     *    what its minimum cost is.
     * 3) Use dynamic programming over:
     *      - current covered prefix length i
     *      - previous fragment length prevLen
     *    where prevLen = 0 means "no previous fragment yet".
     *
     * DP state:
     *   dp[i][prevLen] = minimum cost to build target[0..i-1], where the last used fragment length is prevLen.
     *
     * Transition:
     *   From state (i, prevLen), try every fragment length nextLen that matches target at position i.
     *   Let fragmentCost be the minimum cost of that exact matching fragment.
     *   Additional cost:
     *      fragmentCost + (prevLen != 0 && prevLen != nextLen ? switchCost : 0)
     *   Then update:
     *      dp[i + nextLen][nextLen]
     *
     * Because fragment lengths are at most 50, the DP is efficient.
     *
     * @param target the DNA string that must be assembled exactly
     * @param parts available reusable fragments
     * @param cost rewrite cost for each fragment in parts
     * @param switchCost extra penalty paid whenever consecutive fragment lengths differ
     * @return the minimum total assembly cost, or -1 if assembly is impossible
     * Time complexity: O(m * L + n * L * L), where m = number of parts and L = 50.
     * Since L is bounded by 50, this is effectively O(totalPartsLength + n).
     * Space complexity: O(n * L + u), where u is the number of unique fragment strings.
     */
    public long minimumRewriteCost(String target, String[] parts, int[] cost, long switchCost) {
        int n = target.length();
        int maxLen = 50;

        // Step 1:
        // Build a dictionary that stores, for each exact fragment string,
        // the minimum cost among all occurrences of that string.
        FragmentDictionary dictionary = buildDictionary(parts, cost);

        // Step 2:
        // For each position i in the target and each possible length len,
        // store the minimum fragment cost if target substring [i, i+len) is available as a fragment.
        //
        // matchCost[i][len] = minimum cost of a fragment equal to target.substring(i, i+len),
        // or INF if no such fragment exists.
        long[][] matchCost = precomputeMatches(target, dictionary, maxLen);

        // Step 3:
        // Dynamic programming table.
        //
        // dp[i][prevLen]:
        //   minimum cost to assemble the first i characters of target,
        //   where the last fragment used has length prevLen.
        //
        // prevLen ranges from 0..50
        //   0 means "we have not used any fragment yet" (only valid at i = 0).
        long[][] dp = new long[n + 1][maxLen + 1];
        for (int i = 0; i <= n; i++) {
            Arrays.fill(dp[i], INF);
        }
        dp[0][0] = 0L;

        // Step 4:
        // Process positions from left to right.
        for (int i = 0; i < n; i++) {
            for (int prevLen = 0; prevLen <= maxLen; prevLen++) {
                long currentCost = dp[i][prevLen];
                if (currentCost == INF) {
                    continue;
                }

                // Try placing every possible fragment length nextLen at position i.
                for (int nextLen = 1; nextLen <= maxLen && i + nextLen <= n; nextLen++) {
                    long fragmentCost = matchCost[i][nextLen];
                    if (fragmentCost == INF) {
                        continue;
                    }

                    long extra = fragmentCost;

                    // Switch penalty applies only if this is NOT the first fragment
                    // and the fragment length changes.
                    if (prevLen != 0 && prevLen != nextLen) {
                        extra += switchCost;
                    }

                    long candidate = currentCost + extra;
                    if (candidate < dp[i + nextLen][nextLen]) {
                        dp[i + nextLen][nextLen] = candidate;
                    }
                }
            }
        }

        // Step 5:
        // The answer is the minimum over all possible last fragment lengths at position n.
        long answer = INF;
        for (int lastLen = 1; lastLen <= maxLen; lastLen++) {
            answer = Math.min(answer, dp[n][lastLen]);
        }

        return answer == INF ? -1L : answer;
    }

    /**
     * Builds a dictionary of unique fragment strings with their minimum cost.
     *
     * If the same fragment text appears multiple times, only the cheapest one matters,
     * because using a more expensive identical fragment is never beneficial.
     *
     * @param parts array of fragment strings
     * @param cost cost associated with each fragment
     * @return a dictionary mapping each unique fragment string to its minimum cost
     * Time complexity: O(sum of fragment lengths)
     * Space complexity: O(number of unique fragments)
     */
    public FragmentDictionary buildDictionary(String[] parts, int[] cost) {
        FragmentDictionary dictionary = new FragmentDictionary();

        for (int i = 0; i < parts.length; i++) {
            String fragment = parts[i];
            long c = cost[i];

            Long existing = dictionary.minCostByString.get(fragment);
            if (existing == null || c < existing) {
                dictionary.minCostByString.put(fragment, c);
            }
        }

        return dictionary;
    }

    /**
     * Precomputes which target substrings can be matched by available fragments.
     *
     * For each target position i and each length len from 1 to maxLen:
     *   - if target.substring(i, i+len) exists in the fragment dictionary,
     *     store its minimum cost
     *   - otherwise store INF
     *
     * This allows the DP to check valid placements in O(1) time per (position, length).
     *
     * @param target the target DNA string
     * @param dictionary dictionary of unique fragment strings with minimum costs
     * @param maxLen maximum possible fragment length
     * @return a 2D array matchCost where matchCost[i][len] is the minimum cost of the matching fragment, or INF
     * Time complexity: O(n * maxLen * averageSubstringCost), practically O(n * maxLen^2) due to substring creation,
     * but with maxLen <= 50 this is efficient enough.
     * Space complexity: O(n * maxLen)
     */
    public long[][] precomputeMatches(String target, FragmentDictionary dictionary, int maxLen) {
        int n = target.length();
        long[][] matchCost = new long[n][maxLen + 1];

        for (int i = 0; i < n; i++) {
            Arrays.fill(matchCost[i], INF);

            // Build substrings of increasing length starting at i.
            // Since maxLen is only 50, this is small and manageable.
            StringBuilder builder = new StringBuilder();
            for (int len = 1; len <= maxLen && i + len <= n; len++) {
                builder.append(target.charAt(i + len - 1));
                String sub = builder.toString();

                Long c = dictionary.minCostByString.get(sub);
                if (c != null) {
                    matchCost[i][len] = c;
                }
            }
        }

        return matchCost;
    }

    /**
     * Convenience wrapper matching a common interview-style signature using int switchCost.
     *
     * @param target the DNA string that must be assembled exactly
     * @param parts available reusable fragments
     * @param cost rewrite cost for each fragment
     * @param switchCost extra penalty for changing fragment length between consecutive placements
     * @return the minimum total assembly cost, or -1 if impossible
     * Time complexity: same as minimumRewriteCost(String, String[], int[], long)
     * Space complexity: same as minimumRewriteCost(String, String[], int[], long)
     */
    public long minimumRewriteCost(String target, String[] parts, int[] cost, int switchCost) {
        return minimumRewriteCost(target, parts, cost, (long) switchCost);
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The first example's corrected optimal answer is 8:
     *   "AC" + "GT" + "AC" => 3 + 2 + 3 = 8
     * with no switch penalties because all lengths are 2.
     *
     * The second example is impossible because no fragment matches the final "T".
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) outside the invoked solver calls
     * Space complexity: O(1) outside the invoked solver calls
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String target1 = "ACGTAC";
        String[] parts1 = {"AC", "GT", "ACG", "TAC"};
        int[] cost1 = {3, 2, 5, 4};
        int switchCost1 = 6;
        System.out.println(solution.minimumRewriteCost(target1, parts1, cost1, switchCost1)); // Expected: 8

        String target2 = "AACGT";
        String[] parts2 = {"AA", "A", "CG", "GT"};
        int[] cost2 = {4, 2, 3, 3};
        int switchCost2 = 5;
        System.out.println(solution.minimumRewriteCost(target2, parts2, cost2, switchCost2)); // Expected: -1

        String target3 = "AAAA";
        String[] parts3 = {"A", "AA", "AAA"};
        int[] cost3 = {5, 3, 10};
        int switchCost3 = 4;
        System.out.println(solution.minimumRewriteCost(target3, parts3, cost3, switchCost3)); // Expected: 6 ("AA" + "AA")

        String target4 = "ACGT";
        String[] parts4 = {"A", "C", "G", "T", "AC", "GT"};
        int[] cost4 = {2, 2, 2, 2, 3, 3};
        int switchCost4 = 5;
        System.out.println(solution.minimumRewriteCost(target4, parts4, cost4, switchCost4)); // Expected: 6 ("AC" + "GT")
    }
}