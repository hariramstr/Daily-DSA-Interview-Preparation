import java.util.*;

/*
Problem Title: Minimum Cost to Encode a Chord Progression with Reusable Motifs

Problem Description:
A music production tool stores a long chord progression as a string progression of uppercase letters,
where each character represents one chord symbol after normalization. To reduce storage, the tool may
encode the progression using a library of reusable motifs. Each motif motifs[i] is a non-empty string
and has an associated non-negative cost cost[i]. You may use any motif any number of times. When a motif
is used, it must match the next characters of the progression exactly and advances the encoding position
by its length. You may also encode a single chord directly without using a motif, paying a fixed cost
singleCost for that one character.

Your task is to compute the minimum total cost required to encode the entire progression from left to right.
If multiple motifs overlap in possible usage, you may choose whichever sequence yields the smallest total cost.
Motifs may have repeated strings with different costs; you should treat them as separate options, though an
optimal solution will naturally prefer cheaper uses. Return the minimum possible cost.

This is not a compression ratio problem: every character of progression must be covered exactly once,
either by a matching motif or by paying singleCost for that character.

Constraints:
- 1 <= progression.length <= 10^5
- 1 <= motifs.length <= 10^4
- 1 <= sum(motifs[i].length) <= 2 * 10^5
- 1 <= motifs[i].length <= 10^4
- 0 <= cost[i], singleCost <= 10^9
- progression and all motifs[i] consist only of uppercase English letters.

Example 1:
Input: progression = "ABABAC", motifs = ["ABA", "BA", "AC"], cost = [4, 2, 3], singleCost = 2
Output: 7

Example 2:
Input: progression = "AAAAAA", motifs = ["A", "AA", "AAA"], cost = [5, 3, 4], singleCost = 2
Output: 8
*/

public class Solution {

    /**
     * Trie node used by the Aho-Corasick automaton.
     *
     * Each node represents a prefix of one or more motifs.
     * next[c] stores the transition for uppercase letter c.
     * fail is the failure link used when the current path no longer matches.
     * outputHead points to a linked list of motif endings that terminate at this node.
     */
    private static class Node {
        int[] next = new int[26];
        int fail;
        int outputHead = -1;

        Node() {
            Arrays.fill(next, -1);
            fail = 0;
        }
    }

    /**
     * Linked-list style storage for motif endings.
     *
     * When a motif ends at some trie node, we store:
     * - len: motif length
     * - cost: motif cost
     * - next: next output entry for the same node
     *
     * This avoids creating many small Java objects and is memory-friendly.
     */
    private static class OutputEntry {
        int len;
        long cost;
        int next;

        OutputEntry(int len, long cost, int next) {
            this.len = len;
            this.cost = cost;
            this.next = next;
        }
    }

    private final List<Node> trie = new ArrayList<>();
    private final List<OutputEntry> outputs = new ArrayList<>();

    /**
     * Computes the minimum total cost to encode the entire progression.
     *
     * Core idea:
     * 1. Build an Aho-Corasick automaton from all motifs.
     * 2. Scan the progression once from left to right.
     * 3. Let dp[i] be the minimum cost to encode the first i characters.
     * 4. At each position:
     *    - We can always encode the current character directly:
     *      dp[i + 1] = min(dp[i + 1], dp[i] + singleCost)
     *    - We also discover every motif that ends at this position using the automaton.
     *      If a motif of length L and cost C ends at position i, then:
     *      dp[i + 1] = min(dp[i + 1], dp[i + 1 - L] + C)
     *
     * This guarantees exact left-to-right coverage with no overlap mistakes,
     * because every transition in dp represents covering a contiguous suffix.
     *
     * @param progression the full chord progression to encode
     * @param motifs array of reusable motif strings
     * @param cost array where cost[i] is the cost of motifs[i]
     * @param singleCost cost to encode one character directly
     * @return the minimum possible total encoding cost
     * Time complexity: O(n + totalMotifLength + totalMatches), where n is progression length
     * Space complexity: O(totalMotifLength + n)
     */
    public long minimumEncodingCost(String progression, String[] motifs, int[] cost, int singleCost) {
        buildAutomaton(motifs, cost);

        int n = progression.length();
        long[] dp = new long[n + 1];
        long inf = Long.MAX_VALUE / 4;
        Arrays.fill(dp, inf);
        dp[0] = 0L;

        int state = 0;

        for (int i = 0; i < n; i++) {
            char ch = progression.charAt(i);
            int c = ch - 'A';

            /*
             * Step 1: Advance the automaton with the current character.
             *
             * The automaton tells us all motifs that end exactly at position i.
             * Because failure links are already baked into transitions during build,
             * this move is O(1).
             */
            state = trie.get(state).next[c];

            /*
             * Step 2: Directly encode this single character.
             *
             * If we already know the best way to encode the first i characters,
             * then paying singleCost covers progression[i] and reaches dp[i + 1].
             */
            if (dp[i] + (long) singleCost < dp[i + 1]) {
                dp[i + 1] = dp[i] + (long) singleCost;
            }

            /*
             * Step 3: Consider every motif that ends at this position.
             *
             * Suppose a motif has length L and ends at index i.
             * Then it covers progression[i - L + 1 .. i].
             * So the previous encoded prefix length must be (i + 1 - L).
             *
             * Transition:
             * dp[i + 1] = min(dp[i + 1], dp[i + 1 - L] + motifCost)
             */
            int out = trie.get(state).outputHead;
            while (out != -1) {
                OutputEntry entry = outputs.get(out);
                int startPrefixLength = (i + 1) - entry.len;
                long candidate = dp[startPrefixLength] + entry.cost;
                if (candidate < dp[i + 1]) {
                    dp[i + 1] = candidate;
                }
                out = entry.next;
            }
        }

        return dp[n];
    }

    /**
     * Builds the Aho-Corasick automaton from the given motifs and costs.
     *
     * Detailed process:
     * 1. Start with an empty root node.
     * 2. Insert every motif into the trie.
     * 3. At the terminal node of each motif, attach an output entry containing:
     *    - motif length
     *    - motif cost
     * 4. Run BFS to compute failure links.
     * 5. While building failure links, also merge output lists from failure states,
     *    so every node directly knows all motifs ending there.
     * 6. Fill missing transitions so every next[state][char] is valid.
     *
     * This makes the later scan of progression linear.
     *
     * @param motifs array of motif strings
     * @param cost array of motif costs
     * @return nothing
     * Time complexity: O(totalMotifLength * alphabetSize) in practice with alphabet size 26
     * Space complexity: O(totalMotifLength)
     */
    public void buildAutomaton(String[] motifs, int[] cost) {
        trie.clear();
        outputs.clear();
        trie.add(new Node()); // root

        /*
         * Insert every motif into the trie.
         *
         * Repeated motif strings are allowed.
         * We keep them as separate output entries exactly as the problem states.
         * The DP will naturally choose the cheaper one when beneficial.
         */
        for (int i = 0; i < motifs.length; i++) {
            insertMotif(motifs[i], cost[i]);
        }

        /*
         * Build failure links using BFS.
         *
         * For the root:
         * - Missing transitions point back to root itself.
         * - Existing children have failure link 0.
         */
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        Node root = trie.get(0);

        for (int c = 0; c < 26; c++) {
            if (root.next[c] == -1) {
                root.next[c] = 0;
            } else {
                int child = root.next[c];
                trie.get(child).fail = 0;
                queue.offer(child);
            }
        }

        /*
         * Standard Aho-Corasick BFS.
         *
         * For each node and each character:
         * - If there is a trie edge, compute its failure link.
         * - If there is no trie edge, inherit the transition from the failure state.
         *
         * We also merge outputs from the failure node into the child node.
         * That way, when we land on a node during scanning, we immediately know
         * every motif that ends there, including motifs that are suffixes of others.
         */
        while (!queue.isEmpty()) {
            int current = queue.poll();
            Node currentNode = trie.get(current);

            for (int c = 0; c < 26; c++) {
                int nextState = currentNode.next[c];
                if (nextState != -1) {
                    int failState = trie.get(currentNode.fail).next[c];
                    trie.get(nextState).fail = failState;

                    /*
                     * Merge output list from failure state into this child.
                     *
                     * If the failure state already corresponds to one or more motif endings,
                     * then those motifs are also suffix matches at this child.
                     */
                    int failOutputHead = trie.get(failState).outputHead;
                    if (failOutputHead != -1) {
                        trie.get(nextState).outputHead = cloneOutputChainAndPrepend(
                                failOutputHead,
                                trie.get(nextState).outputHead
                        );
                    }

                    queue.offer(nextState);
                } else {
                    currentNode.next[c] = trie.get(currentNode.fail).next[c];
                }
            }
        }
    }

    /**
     * Inserts one motif into the trie and records its terminal output.
     *
     * @param motif the motif string to insert
     * @param motifCost the cost of using this motif
     * @return nothing
     * Time complexity: O(motif.length())
     * Space complexity: O(motif.length()) in the worst case if all nodes are new
     */
    public void insertMotif(String motif, int motifCost) {
        int nodeIndex = 0;

        for (int i = 0; i < motif.length(); i++) {
            int c = motif.charAt(i) - 'A';
            Node node = trie.get(nodeIndex);

            if (node.next[c] == -1) {
                node.next[c] = trie.size();
                trie.add(new Node());
            }

            nodeIndex = node.next[c];
        }

        /*
         * Attach this motif as an output at the terminal node.
         *
         * We store all motifs, even duplicates, because the problem allows them.
         */
        Node terminal = trie.get(nodeIndex);
        int newHead = outputs.size();
        outputs.add(new OutputEntry(motif.length(), motifCost, terminal.outputHead));
        terminal.outputHead = newHead;
    }

    /**
     * Clones an output linked list and prepends it before an existing output list.
     *
     * Why clone?
     * Because multiple trie nodes may need to inherit the same failure outputs.
     * If we reused the same linked list nodes directly and modified pointers,
     * we could accidentally corrupt another node's output chain.
     *
     * Since the total motif length is bounded and alphabet is small, this remains practical.
     *
     * @param sourceHead head of the source output chain to copy
     * @param existingHead head of the destination chain that should come after the copied nodes
     * @return head index of the newly combined chain
     * Time complexity: O(k), where k is the number of output entries in the source chain
     * Space complexity: O(k) for the cloned entries
     */
    public int cloneOutputChainAndPrepend(int sourceHead, int existingHead) {
        /*
         * First collect source entries so we can rebuild them in the same order.
         */
        List<OutputEntry> temp = new ArrayList<>();
        int cur = sourceHead;
        while (cur != -1) {
            OutputEntry e = outputs.get(cur);
            temp.add(e);
            cur = e.next;
        }

        int head = existingHead;

        /*
         * Rebuild in reverse so the final order matches the original source order,
         * followed by the existing chain.
         */
        for (int i = temp.size() - 1; i >= 0; i--) {
            OutputEntry e = temp.get(i);
            int idx = outputs.size();
            outputs.add(new OutputEntry(e.len, e.cost, head));
            head = idx;
        }

        return head;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(total input size of the demonstrations)
     * Space complexity: O(total input size of the demonstrations)
     */
    public static void main(String[] args) {
        Solution solution1 = new Solution();
        String progression1 = "ABABAC";
        String[] motifs1 = {"ABA", "BA", "AC"};
        int[] cost1 = {4, 2, 3};
        int singleCost1 = 2;
        long result1 = solution1.minimumEncodingCost(progression1, motifs1, cost1, singleCost1);
        System.out.println(result1); // Expected: 7

        Solution solution2 = new Solution();
        String progression2 = "AAAAAA";
        String[] motifs2 = {"A", "AA", "AAA"};
        int[] cost2 = {5, 3, 4};
        int singleCost2 = 2;
        long result2 = solution2.minimumEncodingCost(progression2, motifs2, cost2, singleCost2);
        System.out.println(result2); // Expected: 8
    }
}