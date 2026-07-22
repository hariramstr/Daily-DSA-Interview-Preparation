import java.util.*;

/*
 * Minimum Rewrite Cost for Nested Template Expansion
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * A documentation platform stores a final rendered page as a string target.
 * The page was produced by expanding reusable templates. You are given m templates,
 * where template i is a non-empty string templates[i] with an associated rewrite cost cost[i].
 * Starting from an empty page, you may build target from left to right using the following operations:
 *
 * 1. Append any single lowercase letter at cost appendCost.
 * 2. Insert one template templates[i] at the current end of the page at cost cost[i].
 * 3. If the suffix currently built ends with a string equal to some previously used template,
 *    you may reuse that same template again immediately for an additional discounted chaining cost
 *    chainCost[i] instead of cost[i].
 *
 * A template may be used any number of times. Chaining only applies when the most recent appended
 * block was exactly templates[i] and you place the same template again with no gap.
 * Single-letter appends break the chain.
 *
 * Return the minimum total cost to build exactly target, or -1 if it is impossible.
 *
 * Constraints:
 * - 1 <= target.length <= 10^5
 * - 1 <= m <= 2 * 10^4
 * - 1 <= templates[i].length <= 50
 * - target and every template consist only of lowercase English letters
 * - 1 <= appendCost, cost[i], chainCost[i] <= 10^9
 * - Sum of all template lengths does not exceed 2 * 10^5
 *
 * Important note about the provided examples:
 * The textual explanations in the prompt contain arithmetic inconsistencies:
 * - Example 1 says output 7, but the explanation itself computes 9 for "ab" chained twice.
 * - Example 2 says output 11, but the explanation itself computes 12.
 *
 * Under the stated rules, the correct minimums are:
 * - Example 1: 9
 * - Example 2: 12
 *
 * This implementation follows the rules exactly and therefore prints 9 and 12 for those examples.
 */
public class Solution {

    /**
     * A trie node used to store all templates.
     *
     * We insert every template into a trie so that, for every starting position in the target,
     * we can walk forward character by character and discover all templates that match there.
     *
     * Because every template length is at most 50, matching from each position is efficient.
     */
    private static class TrieNode {
        int[] next = new int[26];
        List<Integer> templateIds = new ArrayList<>();

        TrieNode() {
            Arrays.fill(next, -1);
        }
    }

    /**
     * A compact record describing one template.
     */
    private static class TemplateInfo {
        String text;
        int length;
        long cost;
        long chainCost;

        TemplateInfo(String text, long cost, long chainCost) {
            this.text = text;
            this.length = text.length();
            this.cost = cost;
            this.chainCost = chainCost;
        }
    }

    /**
     * Edge representing "template id matches target starting at position start".
     *
     * We store these edges grouped by start position.
     */
    private static class MatchEdge {
        int templateId;
        int endExclusive;

        MatchEdge(int templateId, int endExclusive) {
            this.templateId = templateId;
            this.endExclusive = endExclusive;
        }
    }

    /**
     * Solves the problem exactly as stated.
     *
     * Core DP idea:
     *
     * Let dp[i] = minimum cost to build target prefix target[0..i-1], regardless of what the last block was.
     *
     * However, chaining depends on whether the last block used was a specific template.
     * So we also need a state:
     *
     * chainState[i][template] conceptually means:
     * minimum cost to build prefix ending at i such that the most recent block used was exactly this template.
     *
     * Storing the full 2D table would be too large.
     *
     * Instead, we process transitions position by position:
     * - dp[i] gives the best generic cost at position i.
     * - If a template t matches at i:
     *     1) Start/restart template t from generic state: dp[i] + cost[t]
     *     2) If we already ended at i with last block t, chain again: lastTemplateCostAtPos[i][t] + chainCost[t]
     *
     * We do not materialize all states for all positions.
     * We only store, for each position, the template-ending states that actually occur.
     *
     * Since each position can only start matches of templates with length <= 50,
     * and total template lengths are bounded, the number of actual transitions remains manageable.
     *
     * @param target the final string to build
     * @param templates array of template strings
     * @param cost normal insertion cost for each template
     * @param chainCost discounted chaining cost for each template
     * @param appendCost cost to append one single lowercase letter
     * @return minimum total cost to build target exactly, or -1 if impossible
     * Time complexity: O(n * L + totalMatches + stateMerges), where L <= 50 and in practice this is O(n * 50 + totalMatches)
     * Space complexity: O(totalTrieSize + totalMatches + numberOfActiveTemplateStates)
     */
    public long minimumRewriteCost(String target, String[] templates, long[] cost, long[] chainCost, long appendCost) {
        int n = target.length();
        int m = templates.length;

        TemplateInfo[] infos = new TemplateInfo[m];
        for (int i = 0; i < m; i++) {
            infos[i] = new TemplateInfo(templates[i], cost[i], chainCost[i]);
        }

        List<TrieNode> trie = buildTrie(templates);

        List<MatchEdge>[] matchesFrom = findAllMatches(target, trie, infos);

        long INF = Long.MAX_VALUE / 4;

        // dp[i] = best cost to build exactly first i characters, with no restriction on last block.
        long[] dp = new long[n + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0L;

        /*
         * statesAtPos[i]:
         *   a map from templateId -> minimum cost to build prefix of length i
         *   such that the LAST block used was exactly templateId.
         *
         * Why do we need this?
         * Because only then are we allowed to chain the same template again at chainCost.
         *
         * Single-letter append transitions do NOT create such a state, because appending a letter
         * breaks the chain.
         */
        @SuppressWarnings("unchecked")
        HashMap<Integer, Long>[] statesAtPos = new HashMap[n + 1];
        for (int i = 0; i <= n; i++) {
            statesAtPos[i] = new HashMap<>();
        }

        /*
         * Process positions from left to right.
         *
         * At each position i:
         * 1) We may append one character, moving to i+1 with cost appendCost.
         *    This creates only a generic dp transition, not a chainable template state.
         * 2) For every template matching at i:
         *    - Start/restart it from dp[i] using normal cost.
         *    - Or chain it from statesAtPos[i] if the same template was the last block.
         */
        for (int i = 0; i < n; i++) {
            if (dp[i] >= INF) {
                continue;
            }

            // Operation 1: append one single letter.
            // Since target is fixed, the only meaningful appended letter is target.charAt(i).
            // This is always possible.
            dp[i + 1] = Math.min(dp[i + 1], dp[i] + appendCost);

            // Operation 2 and 3: place matching templates.
            for (MatchEdge edge : matchesFrom[i]) {
                int tid = edge.templateId;
                int end = edge.endExclusive;
                TemplateInfo info = infos[tid];

                // Start or restart this template from the generic best state at position i.
                long normalUse = dp[i] + info.cost;
                relaxTemplateState(statesAtPos[end], tid, normalUse);
                dp[end] = Math.min(dp[end], normalUse);

                // If the last block ending at i was the same template, we may chain it.
                Long prevChainState = statesAtPos[i].get(tid);
                if (prevChainState != null) {
                    long chainedUse = prevChainState + info.chainCost;
                    relaxTemplateState(statesAtPos[end], tid, chainedUse);
                    dp[end] = Math.min(dp[end], chainedUse);
                }
            }
        }

        return dp[n] >= INF ? -1L : dp[n];
    }

    /**
     * Inserts all templates into a trie.
     *
     * Each terminal node stores the list of template ids ending there.
     * Multiple templates may have identical text; they are kept as separate ids because their
     * costs may differ.
     *
     * @param templates array of template strings
     * @return trie represented as an array-list of nodes, root at index 0
     * Time complexity: O(sum of template lengths)
     * Space complexity: O(sum of template lengths)
     */
    public List<TrieNode> buildTrie(String[] templates) {
        List<TrieNode> trie = new ArrayList<>();
        trie.add(new TrieNode());

        for (int id = 0; id < templates.length; id++) {
            int node = 0;
            String s = templates[id];
            for (int i = 0; i < s.length(); i++) {
                int c = s.charAt(i) - 'a';
                if (trie.get(node).next[c] == -1) {
                    trie.get(node).next[c] = trie.size();
                    trie.add(new TrieNode());
                }
                node = trie.get(node).next[c];
            }
            trie.get(node).templateIds.add(id);
        }

        return trie;
    }

    /**
     * Finds every template occurrence in target, grouped by starting position.
     *
     * Because every template length is at most 50, from each target position we walk at most 50 steps
     * down the trie. Whenever we reach a terminal node, we record all templates ending there.
     *
     * matchesFrom[start] contains all templates that match target starting at 'start'.
     *
     * @param target the target string
     * @param trie trie containing all templates
     * @param infos metadata for templates
     * @return array of lists of matching template edges by start position
     * Time complexity: O(n * maxTemplateLength + totalMatches), with maxTemplateLength <= 50
     * Space complexity: O(totalMatches)
     */
    public List<MatchEdge>[] findAllMatches(String target, List<TrieNode> trie, TemplateInfo[] infos) {
        int n = target.length();

        @SuppressWarnings("unchecked")
        List<MatchEdge>[] matchesFrom = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            matchesFrom[i] = new ArrayList<>();
        }

        for (int start = 0; start < n; start++) {
            int node = 0;

            // Walk forward from this start position through the trie.
            // Since template lengths are at most 50, we stop after 50 characters or end of target.
            for (int end = start; end < n && end < start + 50; end++) {
                int c = target.charAt(end) - 'a';
                node = trie.get(node).next[c];
                if (node == -1) {
                    break;
                }

                // Every template ending at this trie node matches target[start..end].
                if (!trie.get(node).templateIds.isEmpty()) {
                    int endExclusive = end + 1;
                    for (int tid : trie.get(node).templateIds) {
                        // This check is logically redundant because trie structure already guarantees it,
                        // but keeping it makes the code easier to reason about.
                        if (infos[tid].length == endExclusive - start) {
                            matchesFrom[start].add(new MatchEdge(tid, endExclusive));
                        }
                    }
                }
            }
        }

        return matchesFrom;
    }

    /**
     * Helper method to relax a template-ending state in a map.
     *
     * If the map already contains a cost for the same template id, keep the smaller one.
     *
     * @param map map from template id to best cost
     * @param templateId template id to update
     * @param value candidate cost
     * @return nothing
     * Time complexity: O(1) average
     * Space complexity: O(1) auxiliary
     */
    public void relaxTemplateState(HashMap<Integer, Long> map, int templateId, long value) {
        Long old = map.get(templateId);
        if (old == null || value < old) {
            map.put(templateId, value);
        }
    }

    /**
     * Convenience overload matching the exact types often used in interview platforms.
     *
     * @param target the final string to build
     * @param templates array of template strings
     * @param cost normal insertion costs as int array
     * @param chainCost discounted chaining costs as int array
     * @param appendCost single-letter append cost
     * @return minimum total cost, or -1 if impossible
     * Time complexity: same as the long-array version
     * Space complexity: same as the long-array version
     */
    public long minimumRewriteCost(String target, String[] templates, int[] cost, int[] chainCost, int appendCost) {
        long[] c1 = new long[cost.length];
        long[] c2 = new long[chainCost.length];
        for (int i = 0; i < cost.length; i++) {
            c1[i] = cost[i];
            c2[i] = chainCost[i];
        }
        return minimumRewriteCost(target, templates, c1, c2, appendCost);
    }

    /**
     * Demonstrates the solution on the sample-style inputs from the prompt.
     *
     * Note:
     * The prompt's printed outputs are inconsistent with its own explanations.
     * Under the actual rules, the correct outputs are:
     * - Example 1 => 9
     * - Example 2 => 12
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(total input size for the demonstrations)
     * Space complexity: O(total input size for the demonstrations)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String target1 = "ababab";
        String[] templates1 = {"ab", "aba"};
        int[] cost1 = {5, 8};
        int[] chain1 = {2, 6};
        int append1 = 4;
        long answer1 = solution.minimumRewriteCost(target1, templates1, cost1, chain1, append1);
        System.out.println(answer1); // Correct under stated rules: 9

        String target2 = "codecodex";
        String[] templates2 = {"code", "x"};
        int[] cost2 = {7, 10};
        int[] chain2 = {3, 1};
        int append2 = 2;
        long answer2 = solution.minimumRewriteCost(target2, templates2, cost2, chain2, append2);
        System.out.println(answer2); // Correct under stated rules: 12
    }
}