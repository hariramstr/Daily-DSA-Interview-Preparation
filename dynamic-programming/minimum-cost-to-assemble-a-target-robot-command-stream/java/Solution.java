import java.util.*;

/*
Problem Title: Minimum Cost to Assemble a Target Robot Command Stream

Problem Description:
A robotics team stores reusable command macros in a library. Each macro is a non-empty string made of lowercase English letters, and executing that macro contributes a fixed cost. You are given a target command stream target, along with arrays macros and cost, where macros[i] can be used any number of times and appending macros[i] adds cost[i] to the total cost.

Your task is to build target exactly by concatenating chosen macros in order. You may reuse the same macro many times, but every chosen macro must match the next characters of target exactly at the position where it is placed. Return the minimum total cost needed to assemble the entire target string, or -1 if it is impossible.

This is not a shortest-length problem: a more expensive long macro may be worse than several cheaper short macros, and duplicate macro strings may appear with different costs. The answer should use the cheapest possible combination.

Constraints:
- 1 <= target.length <= 10^5
- 1 <= macros.length <= 10^5
- 1 <= macros[i].length <= 10^5
- Sum of all macros[i].length <= 2 * 10^5
- 1 <= cost[i] <= 10^9
- target and every macros[i] consist only of lowercase English letters

Example 1:
Input: target = "ababa", macros = ["ab", "aba", "ba", "a"], cost = [4, 5, 2, 10]
Output: 7
Explanation: One optimal construction is "aba" + "ba" with total cost 5 + 2 = 7. Using "ab" + "a" + "ba" would cost 16, which is worse.

Example 2:
Input: target = "robot", macros = ["ro", "bot", "obo", "t"], cost = [3, 4, 10, 1]
Output: -1
Explanation: You can start with "ro", leaving "bot", but no sequence of allowed concatenations matches the remaining characters exactly from every position. Therefore the target cannot be assembled.
*/

public class Solution {

    private static final long INF = Long.MAX_VALUE / 4;

    /**
     * Trie node used by the Aho-Corasick automaton.
     *
     * Each node represents a prefix of one or more macros.
     * We store:
     * - next transitions for 26 lowercase letters
     * - failure link
     * - output link to the next terminal node on the failure chain
     * - bestCost: minimum cost among all macros ending exactly at this node
     * - depth: length of the string represented by this node
     */
    private static class Node {
        int[] next = new int[26];
        int fail;
        int output;
        long bestCost = INF;
        int depth;

        Node() {
            Arrays.fill(next, -1);
        }
    }

    private final List<Node> trie = new ArrayList<>();

    /**
     * Constructor initializes the trie with the root node.
     */
    public Solution() {
        trie.add(new Node());
    }

    /**
     * Computes the minimum total cost needed to assemble the target exactly.
     *
     * Core idea:
     * 1. Build an Aho-Corasick automaton from all macros.
     * 2. Scan the target once. At each target position, the automaton tells us all macros
     *    that end at that position.
     * 3. Dynamic programming:
     *      dp[i] = minimum cost to build target prefix of length i
     *    If a macro of length L and cost C ends at position i - 1, then:
     *      dp[i] = min(dp[i], dp[i - L] + C)
     *
     * Why Aho-Corasick?
     * - It finds all dictionary matches in a text in near-linear time.
     * - That avoids checking every macro at every target position.
     *
     * Important detail:
     * - Duplicate macro strings may appear with different costs.
     * - We only keep the minimum cost for the same exact string, because using a more
     *   expensive duplicate is never beneficial.
     *
     * @param target the target command stream to assemble
     * @param macros the reusable command macros
     * @param cost the execution cost for each macro
     * @return the minimum total cost, or -1 if exact assembly is impossible
     * Time complexity: O(totalMacroLength + targetLength + numberOfMatches)
     * Space complexity: O(totalMacroLength + targetLength)
     */
    public long minimumCost(String target, String[] macros, int[] cost) {
        buildAutomaton(macros, cost);

        int n = target.length();
        long[] dp = new long[n + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0L;

        /*
         * We now scan the target from left to right using the automaton.
         *
         * state = current automaton node after reading target[0..i]
         *
         * For each position i:
         *   - We move through the automaton using target.charAt(i)
         *   - Every terminal pattern ending here gives a valid transition in DP
         *
         * If a macro of length len ends at position i, then it covers:
         *   target[i - len + 1 .. i]
         *
         * So if we already know the best cost to build the prefix before that macro,
         * namely dp[i - len + 1], then:
         *   dp[i + 1] = min(dp[i + 1], dp[i - len + 1] + macroCost)
         *
         * We use i + 1 because dp is prefix-based:
         *   dp[k] = best cost to build target[0 .. k-1]
         */
        int state = 0;

        for (int i = 0; i < n; i++) {
            int ch = target.charAt(i) - 'a';
            state = trie.get(state).next[ch];

            /*
             * We must consider:
             * 1. A macro ending exactly at this state
             * 2. Any additional macros reachable through output links
             *
             * output links jump between terminal nodes on the failure chain,
             * so we can enumerate all matched macro endings at this position.
             */
            int current = state;
            while (current != 0) {
                Node node = trie.get(current);

                if (node.bestCost != INF) {
                    int len = node.depth;
                    int startPrefixLength = i + 1 - len;

                    /*
                     * If dp[startPrefixLength] is reachable, then we can append
                     * this matched macro and update dp[i + 1].
                     */
                    if (dp[startPrefixLength] != INF) {
                        dp[i + 1] = Math.min(dp[i + 1], dp[startPrefixLength] + node.bestCost);
                    }
                }

                current = node.output;
            }
        }

        return dp[n] == INF ? -1 : dp[n];
    }

    /**
     * Builds the Aho-Corasick automaton from the given macros and costs.
     *
     * Step-by-step:
     * 1. Insert every macro into the trie.
     * 2. At the terminal node of each macro, store the minimum cost for that exact string.
     * 3. Run BFS to compute failure links.
     * 4. Build output links so we can quickly enumerate all matched terminal nodes.
     *
     * @param macros the macro strings
     * @param cost the corresponding costs
     * @return nothing
     * Time complexity: O(sum of macro lengths * alphabetSize) in this implementation,
     *                  which is effectively O(sum of macro lengths) because alphabet size is 26
     * Space complexity: O(sum of macro lengths)
     */
    public void buildAutomaton(String[] macros, int[] cost) {
        trie.clear();
        trie.add(new Node());

        for (int i = 0; i < macros.length; i++) {
            insert(macros[i], cost[i]);
        }

        Queue<Integer> queue = new ArrayDeque<>();

        /*
         * Initialize root transitions.
         *
         * For Aho-Corasick, missing transitions from the root point back to root.
         * Existing children of root have failure link = root.
         */
        for (int c = 0; c < 26; c++) {
            int nextState = trie.get(0).next[c];
            if (nextState != -1) {
                trie.get(nextState).fail = 0;
                trie.get(nextState).output = trie.get(nextState).bestCost != INF ? nextState : 0;
                queue.offer(nextState);
            } else {
                trie.get(0).next[c] = 0;
            }
        }

        /*
         * BFS over trie nodes to compute:
         * - failure links
         * - completed automaton transitions
         * - output links
         *
         * For each node and character:
         *   if child exists:
         *      fail(child) = next(fail(node), character)
         *   else:
         *      next(node, character) = next(fail(node), character)
         *
         * This makes automaton transitions O(1) during target scanning.
         */
        while (!queue.isEmpty()) {
            int v = queue.poll();
            Node node = trie.get(v);

            for (int c = 0; c < 26; c++) {
                int u = node.next[c];
                if (u != -1) {
                    int failure = trie.get(v).fail;
                    trie.get(u).fail = trie.get(failure).next[c];

                    /*
                     * output link should point to the nearest terminal node on the failure chain.
                     * If fail(u) itself is terminal, output(u) = fail(u)
                     * Otherwise, inherit output(fail(u))
                     */
                    int failNode = trie.get(u).fail;
                    if (trie.get(failNode).bestCost != INF) {
                        trie.get(u).output = failNode;
                    } else {
                        trie.get(u).output = trie.get(failNode).output;
                    }

                    queue.offer(u);
                } else {
                    node.next[c] = trie.get(node.fail).next[c];
                }
            }
        }

        /*
         * Small correction for root children:
         * if a root child is terminal, its output should not point to itself,
         * because during traversal we already start from the current state and then
         * follow output links. Self-loop would cause infinite iteration.
         *
         * So for every node, output should point only to ANOTHER terminal node on the
         * failure chain, not itself.
         */
        for (int i = 1; i < trie.size(); i++) {
            int failNode = trie.get(i).fail;
            if (trie.get(failNode).bestCost != INF) {
                trie.get(i).output = failNode;
            } else {
                trie.get(i).output = trie.get(failNode).output;
            }
        }
    }

    /**
     * Inserts one macro into the trie.
     *
     * If the same macro string appears multiple times with different costs,
     * we keep only the minimum cost at its terminal node.
     *
     * @param word the macro string to insert
     * @param wordCost the cost of this macro
     * @return nothing
     * Time complexity: O(word.length())
     * Space complexity: O(word.length()) in the worst case if all nodes are new
     */
    public void insert(String word, int wordCost) {
        int nodeIndex = 0;

        for (int i = 0; i < word.length(); i++) {
            int c = word.charAt(i) - 'a';
            int nextNode = trie.get(nodeIndex).next[c];

            if (nextNode == -1) {
                Node newNode = new Node();
                newNode.depth = trie.get(nodeIndex).depth + 1;
                trie.add(newNode);
                nextNode = trie.size() - 1;
                trie.get(nodeIndex).next[c] = nextNode;
            }

            nodeIndex = nextNode;
        }

        trie.get(nodeIndex).bestCost = Math.min(trie.get(nodeIndex).bestCost, wordCost);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size for the demo)
     * Space complexity: O(total input size for the demo)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String target1 = "ababa";
        String[] macros1 = {"ab", "aba", "ba", "a"};
        int[] cost1 = {4, 5, 2, 10};
        System.out.println(solution.minimumCost(target1, macros1, cost1)); // Expected: 7

        String target2 = "robot";
        String[] macros2 = {"ro", "bot", "obo", "t"};
        int[] cost2 = {3, 4, 10, 1};
        System.out.println(solution.minimumCost(target2, macros2, cost2)); // Expected: 7

        /*
         * Note:
         * The provided problem statement says Example 2 output is -1, but tracing the example:
         *   "robot" = "ro" + "bot"
         * with cost 3 + 4 = 7
         * which is a valid exact assembly.
         *
         * Therefore the correct answer for that exact input is 7.
         * The algorithm returns the mathematically correct result.
         */
    }
}