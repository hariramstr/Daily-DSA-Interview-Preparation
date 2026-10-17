import java.util.*;

/*
 * Title: Minimum Cost to Stitch a Banner from Overlapping Slogans
 * Difficulty: Hard
 * Topic: Dynamic Programming
 *
 * Problem Description:
 * You are given a target banner string target that a marketing system must assemble exactly
 * from left to right. You also receive n slogan fragments. The i-th fragment is a string
 * fragments[i] with an associated usage cost cost[i]. You may use any fragment any number of times.
 *
 * When placing a fragment, it must match a contiguous substring of target starting at some
 * position p. However, unlike simple concatenation, the new fragment is allowed to overlap
 * the already-built banner by up to k characters. More formally, if you have already built
 * the prefix target[0...p-1], then you may place a fragment of length L so that its first t
 * characters overlap the suffix target[p-t...p-1] for some 0 <= t <= min(k, L-1, p), and its
 * full content still matches target[p-t...p-t+L-1]. After placing it, the built prefix extends
 * to max(p, p-t+L). Each placement adds cost[i].
 *
 * Return the minimum total cost to build the entire target, or -1 if it is impossible.
 *
 * This is a weighted string-construction problem where the best solution may require intentionally
 * using overlaps to reduce total cost. Greedy choices are not sufficient because a cheaper fragment
 * now may block a more efficient overlap later.
 *
 * Constraints:
 * - 1 <= target.length <= 5000
 * - 1 <= n <= 2000
 * - 1 <= fragments[i].length <= 50
 * - 0 <= k <= 50
 * - 1 <= cost[i] <= 10^9
 * - target and all fragments[i] consist of lowercase English letters.
 *
 * Example 1:
 * Input:
 *  target = "ababa"
 *  fragments = ["aba", "baba", "ab"]
 *  cost = [4, 5, 2]
 *  k = 2
 * Output:
 *  6
 *
 * Example 2:
 * Input:
 *  target = "aaaaab"
 *  fragments = ["aaa", "aaab", "b"]
 *  cost = [3, 4, 10]
 *  k = 2
 * Output:
 *  7
 */

public class Solution {

    /**
     * Internal representation of a fragment after preprocessing.
     * We keep:
     * - the fragment text
     * - its cost
     * - its length
     */
    private static class Fragment {
        String text;
        long cost;
        int len;

        Fragment(String text, long cost) {
            this.text = text;
            this.cost = cost;
            this.len = text.length();
        }
    }

    /**
     * Computes the minimum total cost to build the target exactly.
     *
     * Core idea:
     * We use dynamic programming on prefix length.
     *
     * Let dp[p] = minimum cost to have already built exactly target[0..p-1].
     * So:
     * - dp[0] = 0, because building an empty prefix costs nothing.
     * - We want dp[m], where m = target.length().
     *
     * Transition:
     * From a built prefix of length p, we may place any fragment with any allowed overlap t:
     *   0 <= t <= min(k, fragmentLength - 1, p)
     *
     * If we overlap by t, then the fragment starts at index s = p - t in the target.
     * The fragment is valid only if:
     *   target[s .. s + L - 1] == fragment
     * and s + L <= m.
     *
     * After placing it, the built prefix extends to:
     *   newP = max(p, s + L)
     * But because s = p - t and t <= L - 1, we always add at least one new character,
     * so newP = s + L.
     *
     * Therefore:
     *   dp[newP] = min(dp[newP], dp[p] + fragmentCost)
     *
     * To make matching efficient, for every possible start position s in the target we precompute
     * which fragments match starting at s.
     *
     * Then for each dp state p, instead of trying all fragments and then checking whether they match,
     * we enumerate all possible overlap values t, derive s = p - t, and use the precomputed list of
     * fragments that match at s.
     *
     * Important subtlety:
     * A fragment matching at s is usable from state p only if:
     *   p is inside the fragment but not beyond its end
     * which is equivalent to:
     *   s <= p <= s + L - 1
     * Since s = p - t and t <= L - 1, this is already enforced by the overlap bound.
     *
     * @param target the banner string that must be built exactly
     * @param fragments available slogan fragments, reusable unlimited times
     * @param cost usage cost for each fragment
     * @param k maximum allowed overlap length
     * @return the minimum total cost, or -1 if impossible
     * Time complexity: O(n * m * avgLen + m * k * avgMatchesPerStart) in practice,
     *                  more concretely O(n * m * 50 + m * k * A), where A is the number
     *                  of matching fragments per relevant start position. With given limits,
     *                  this is efficient enough.
     * Space complexity: O(m + totalMatches + n)
     */
    public long minimumCost(String target, String[] fragments, int[] cost, int k) {
        int m = target.length();
        char[] targetChars = target.toCharArray();

        // Step 1:
        // Convert raw input arrays into Fragment objects.
        List<Fragment> allFragments = new ArrayList<>(fragments.length);
        for (int i = 0; i < fragments.length; i++) {
            allFragments.add(new Fragment(fragments[i], cost[i]));
        }

        // Step 2:
        // For every start position s in the target, store all fragments that match exactly at s.
        //
        // matchesAt[s] contains indices into the "usableFragments" list.
        // We use ArrayList<Integer>[] for simplicity and beginner-friendliness.
        @SuppressWarnings("unchecked")
        List<Integer>[] matchesAt = new ArrayList[m];
        for (int i = 0; i < m; i++) {
            matchesAt[i] = new ArrayList<>();
        }

        // We keep fragments in a separate list so we can reference them by index.
        List<Fragment> usableFragments = new ArrayList<>(allFragments);

        // Precompute exact matches of each fragment at each target position.
        //
        // Because fragment length <= 50, direct character comparison is fast enough.
        for (int idx = 0; idx < usableFragments.size(); idx++) {
            Fragment f = usableFragments.get(idx);
            int len = f.len;

            // A fragment can only start where it fully fits inside the target.
            for (int start = 0; start + len <= m; start++) {
                if (matches(targetChars, start, f.text)) {
                    matchesAt[start].add(idx);
                }
            }
        }

        // Step 3:
        // Standard DP over built prefix length.
        long INF = Long.MAX_VALUE / 4;
        long[] dp = new long[m + 1];
        Arrays.fill(dp, INF);
        dp[0] = 0L;

        // For each reachable prefix length p...
        for (int p = 0; p <= m; p++) {
            if (dp[p] == INF) {
                continue; // unreachable state
            }

            // Try every allowed overlap length t.
            //
            // If we have already built prefix length p, and overlap by t,
            // then the fragment starts at s = p - t.
            //
            // Constraints on t:
            // - cannot exceed k
            // - cannot exceed p (cannot overlap more characters than already built)
            int maxOverlap = Math.min(k, p);

            for (int t = 0; t <= maxOverlap; t++) {
                int s = p - t;

                // If s == m, no fragment can start there because target is already ended.
                if (s >= m) {
                    continue;
                }

                // Every fragment that matches at start s is a candidate.
                for (int fragIndex : matchesAt[s]) {
                    Fragment f = usableFragments.get(fragIndex);

                    // Overlap t must also satisfy t <= L - 1.
                    // Why L - 1?
                    // Because the placement must extend the built prefix by at least one new character.
                    if (t > f.len - 1) {
                        continue;
                    }

                    int newP = s + f.len;

                    // Since s = p - t and t <= L - 1, newP > p always holds.
                    // Still, we keep the logic explicit and safe.
                    if (newP <= p) {
                        continue;
                    }

                    long candidate = dp[p] + f.cost;
                    if (candidate < dp[newP]) {
                        dp[newP] = candidate;
                    }
                }
            }
        }

        return dp[m] == INF ? -1L : dp[m];
    }

    /**
     * Checks whether the fragment string matches the target character array starting at a given index.
     *
     * @param targetChars target as a character array
     * @param start start index in the target
     * @param fragment fragment text to compare
     * @return true if fragment equals target substring starting at start, otherwise false
     * Time complexity: O(fragment.length)
     * Space complexity: O(1)
     */
    public boolean matches(char[] targetChars, int start, String fragment) {
        int len = fragment.length();
        if (start < 0 || start + len > targetChars.length) {
            return false;
        }

        for (int i = 0; i < len; i++) {
            if (targetChars[start + i] != fragment.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convenience wrapper using int return type when the caller expects the result to fit in int.
     * If the true answer exceeds Integer.MAX_VALUE, this method still returns the mathematically
     * correct value cast to int only if it fits; otherwise it throws an exception.
     *
     * @param target the banner string that must be built exactly
     * @param fragments available slogan fragments
     * @param cost usage cost for each fragment
     * @param k maximum allowed overlap length
     * @return the minimum total cost as an int, or -1 if impossible
     * Time complexity: same as minimumCost(...)
     * Space complexity: same as minimumCost(...)
     */
    public int minimumCostInt(String target, String[] fragments, int[] cost, int k) {
        long ans = minimumCost(target, fragments, cost, k);
        if (ans == -1L) {
            return -1;
        }
        if (ans > Integer.MAX_VALUE) {
            throw new ArithmeticException("Answer exceeds int range: " + ans);
        }
        return (int) ans;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) outside the invoked sample runs
     * Space complexity: O(1) outside the invoked sample runs
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1
        String target1 = "ababa";
        String[] fragments1 = {"aba", "baba", "ab"};
        int[] cost1 = {4, 5, 2};
        int k1 = 2;
        long result1 = solution.minimumCost(target1, fragments1, cost1, k1);
        System.out.println(result1); // Expected: 6

        // Sample 2
        String target2 = "aaaaab";
        String[] fragments2 = {"aaa", "aaab", "b"};
        int[] cost2 = {3, 4, 10};
        int k2 = 2;
        long result2 = solution.minimumCost(target2, fragments2, cost2, k2);
        System.out.println(result2); // Expected: 7

        // Additional quick sanity checks

        // Impossible case
        String target3 = "abc";
        String[] fragments3 = {"ab", "bc"};
        int[] cost3 = {2, 2};
        int k3 = 0;
        long result3 = solution.minimumCost(target3, fragments3, cost3, k3);
        System.out.println(result3); // Expected: -1

        // Simple exact cover without overlap
        String target4 = "hello";
        String[] fragments4 = {"he", "llo", "hello"};
        int[] cost4 = {2, 3, 10};
        int k4 = 0;
        long result4 = solution.minimumCost(target4, fragments4, cost4, k4);
        System.out.println(result4); // Expected: 5
    }
}