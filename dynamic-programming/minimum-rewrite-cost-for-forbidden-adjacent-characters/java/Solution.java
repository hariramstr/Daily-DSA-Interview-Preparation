import java.util.*;

/*
Problem Title: Minimum Rewrite Cost for Forbidden Adjacent Characters

Problem Description:
You are given a string s of length n consisting of lowercase English letters. Some pairs of letters
are incompatible and are not allowed to appear next to each other in the final string. You are also
given a non-negative cost matrix changeCost of size 26 x 26, where changeCost[a][b] is the cost to
rewrite character a into character b. Rewriting a character does not affect any other position, and
every position must end as exactly one lowercase letter.

Your task is to compute the minimum total rewrite cost needed to transform s into a new string t such
that for every adjacent pair t[i - 1], t[i], that ordered pair is allowed. If it is impossible to
produce any valid final string, return -1.

The incompatibility rules are provided as a list of forbidden ordered pairs. If (x, y) is forbidden,
then x cannot be immediately followed by y. Note that (x, y) and (y, x) are different constraints.

This is a global optimization problem: choosing the cheapest letter for one position may force
expensive choices later, so greedy methods do not work in general.

Constraints:
- 1 <= n <= 100000
- s consists only of lowercase English letters
- 0 <= changeCost[i][j] <= 10^9
- 0 <= number of forbidden pairs <= 26 * 26
- The answer may exceed 32-bit integer range

Examples:
1)
s = "abca"
forbiddenPairs = [["a","b"],["b","c"]]
changeCost = identity cost 0, and additionally:
a->c = 2, b->a = 3, b->d = 1, c->a = 4, all other non-diagonal changes = 5
Output: 3

2)
s = "aaa"
forbiddenPairs = every ordered pair over all 26 letters forbidden
changeCost = any matrix
Output: -1
*/

public class Solution {

    /**
     * Computes the minimum total rewrite cost to transform the input string into a valid final string
     * where no forbidden ordered adjacent pair appears.
     *
     * Dynamic programming idea:
     * Let dpPrev[last] be the minimum cost to rewrite the processed prefix so that the last chosen
     * character is 'last'.
     *
     * Transition:
     * For the next position and target character 'cur',
     * dpNext[cur] = rewriteCost(currentOriginalChar -> cur) + min(dpPrev[prev]) over all prev such that (prev, cur) is allowed.
     *
     * Because the alphabet size is only 26, checking all 26 previous letters for each of 26 current
     * letters is cheap:
     * 26 * 26 per position, which is effectively constant work per character.
     *
     * @param s the original lowercase string
     * @param forbiddenPairs list of forbidden ordered pairs; each element should contain exactly two
     *                       one-character strings, e.g. {"a", "b"} meaning 'a' cannot be followed by 'b'
     * @param changeCost 26 x 26 matrix where changeCost[from][to] is the cost to rewrite one letter into another
     * @return the minimum total rewrite cost, or -1 if no valid final string exists
     * Time complexity: O(n * 26 * 26), which is O(n) because 26 is constant
     * Space complexity: O(26 * 26 + 26), which is O(1) auxiliary space excluding input
     */
    public long minimumRewriteCost(String s, List<String[]> forbiddenPairs, long[][] changeCost) {
        int n = s.length();

        // allowed[a][b] tells us whether letter a may be immediately followed by letter b.
        // We begin by assuming every ordered pair is allowed, then mark forbidden ones as false.
        boolean[][] allowed = buildAllowedMatrix(forbiddenPairs);

        // A very large value used to represent "unreachable".
        // We keep it safely below Long.MAX_VALUE to avoid overflow when adding costs.
        final long INF = Long.MAX_VALUE / 4;

        // dpPrev[x] = minimum cost for processed prefix ending with letter x.
        long[] dpPrev = new long[26];
        Arrays.fill(dpPrev, INF);

        // Base case for position 0:
        // There is no adjacency constraint before the first character,
        // so we may rewrite s[0] into any target letter independently.
        int firstOriginal = s.charAt(0) - 'a';
        for (int target = 0; target < 26; target++) {
            dpPrev[target] = changeCost[firstOriginal][target];
        }

        // Process positions 1..n-1.
        for (int i = 1; i < n; i++) {
            int original = s.charAt(i) - 'a';

            // dpNext[cur] = best cost for prefix ending at current position with letter cur.
            long[] dpNext = new long[26];
            Arrays.fill(dpNext, INF);

            // For every possible current final letter...
            for (int cur = 0; cur < 26; cur++) {
                long rewrite = changeCost[original][cur];

                // We must choose a previous ending letter 'prev' such that (prev, cur) is allowed.
                long bestPrevious = INF;

                // Since there are only 26 letters, we simply scan all possibilities.
                for (int prev = 0; prev < 26; prev++) {
                    if (allowed[prev][cur] && dpPrev[prev] < bestPrevious) {
                        bestPrevious = dpPrev[prev];
                    }
                }

                // If no valid previous letter exists, this state remains unreachable.
                if (bestPrevious < INF) {
                    dpNext[cur] = bestPrevious + rewrite;
                }
            }

            // Move to the next position.
            dpPrev = dpNext;
        }

        // The answer is the minimum cost among all possible ending letters.
        long answer = INF;
        for (int last = 0; last < 26; last++) {
            answer = Math.min(answer, dpPrev[last]);
        }

        return answer >= INF ? -1L : answer;
    }

    /**
     * Convenience overload that accepts forbidden pairs as a 2D string array.
     *
     * @param s the original lowercase string
     * @param forbiddenPairs forbidden ordered pairs, each row like {"a", "b"}
     * @param changeCost 26 x 26 matrix of rewrite costs
     * @return the minimum total rewrite cost, or -1 if impossible
     * Time complexity: O(n * 26 * 26)
     * Space complexity: O(1) auxiliary space excluding input
     */
    public long minimumRewriteCost(String s, String[][] forbiddenPairs, long[][] changeCost) {
        List<String[]> list = new ArrayList<>();
        for (String[] pair : forbiddenPairs) {
            list.add(pair);
        }
        return minimumRewriteCost(s, list, changeCost);
    }

    /**
     * Builds the allowed adjacency matrix from the forbidden pair list.
     *
     * allowed[a][b] is true if letter a may be followed by letter b.
     *
     * @param forbiddenPairs list of forbidden ordered pairs
     * @return a 26 x 26 boolean matrix of allowed transitions
     * Time complexity: O(26 * 26 + f), where f is the number of forbidden pairs
     * Space complexity: O(26 * 26)
     */
    public boolean[][] buildAllowedMatrix(List<String[]> forbiddenPairs) {
        boolean[][] allowed = new boolean[26][26];

        // Initially every ordered pair is allowed.
        for (int i = 0; i < 26; i++) {
            Arrays.fill(allowed[i], true);
        }

        // Mark each forbidden ordered pair as not allowed.
        for (String[] pair : forbiddenPairs) {
            if (pair == null || pair.length != 2 || pair[0] == null || pair[1] == null
                    || pair[0].length() != 1 || pair[1].length() != 1) {
                throw new IllegalArgumentException("Each forbidden pair must be exactly two one-character strings.");
            }

            int from = pair[0].charAt(0) - 'a';
            int to = pair[1].charAt(0) - 'a';

            if (from < 0 || from >= 26 || to < 0 || to >= 26) {
                throw new IllegalArgumentException("Forbidden pairs must contain lowercase English letters only.");
            }

            allowed[from][to] = false;
        }

        return allowed;
    }

    /**
     * Creates a 26 x 26 cost matrix where diagonal entries are 0 and all non-diagonal entries are
     * initialized to the provided default cost.
     *
     * @param defaultNonDiagonalCost cost assigned to every change from one letter to a different letter
     * @return initialized 26 x 26 cost matrix
     * Time complexity: O(26 * 26)
     * Space complexity: O(26 * 26)
     */
    public static long[][] createBaseCostMatrix(long defaultNonDiagonalCost) {
        long[][] cost = new long[26][26];
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 26; j++) {
                cost[i][j] = (i == j) ? 0L : defaultNonDiagonalCost;
            }
        }
        return cost;
    }

    /**
     * Sets one directed rewrite cost in the matrix.
     *
     * @param cost the 26 x 26 cost matrix
     * @param from source lowercase letter
     * @param to target lowercase letter
     * @param value rewrite cost
     * @return the same matrix reference for convenient chaining
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public static long[][] setCost(long[][] cost, char from, char to, long value) {
        cost[from - 'a'][to - 'a'] = value;
        return cost;
    }

    /**
     * Demonstrates the solution on sample-style inputs from the problem statement.
     *
     * @param args command-line arguments, not used
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, aside from the called algorithm
     * Space complexity: O(1) auxiliary space for the demonstration itself
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------
        // Example 1
        // -----------------------------
        // s = "abca"
        // forbiddenPairs = [["a","b"],["b","c"]]
        // changeCost:
        //   identity cost 0
        //   a->c = 2
        //   b->a = 3
        //   b->d = 1
        //   c->a = 4
        //   all other non-diagonal changes = 5
        //
        // One valid optimal result is "adca":
        //   a -> a : 0
        //   b -> d : 1
        //   c -> c : 0
        //   a -> a : 0
        // Total = 1
        //
        // Under the exact matrix described above, the true minimum is therefore 1.
        // This is what the algorithm correctly computes.
        String s1 = "abca";
        String[][] forbidden1 = {
                {"a", "b"},
                {"b", "c"}
        };
        long[][] cost1 = createBaseCostMatrix(5);
        setCost(cost1, 'a', 'c', 2);
        setCost(cost1, 'b', 'a', 3);
        setCost(cost1, 'b', 'd', 1);
        setCost(cost1, 'c', 'a', 4);

        long result1 = solution.minimumRewriteCost(s1, forbidden1, cost1);
        System.out.println(result1);

        // -----------------------------
        // Example 2
        // -----------------------------
        // Every ordered pair is forbidden.
        // For any string of length > 1, no valid adjacent pair can exist, so answer is -1.
        String s2 = "aaa";
        List<String[]> forbidden2 = new ArrayList<>();
        for (char x = 'a'; x <= 'z'; x++) {
            for (char y = 'a'; y <= 'z'; y++) {
                forbidden2.add(new String[]{String.valueOf(x), String.valueOf(y)});
            }
        }
        long[][] cost2 = createBaseCostMatrix(7);

        long result2 = solution.minimumRewriteCost(s2, forbidden2, cost2);
        System.out.println(result2);
    }
}