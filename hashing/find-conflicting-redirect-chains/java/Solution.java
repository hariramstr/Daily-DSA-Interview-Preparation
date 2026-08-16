/*
Problem Title: Find Conflicting Redirect Chains

Problem Description:
A web platform stores URL redirects as pairs [fromUrl, toUrl]. Each fromUrl appears at most once,
meaning a page can redirect to only one next page. However, many different pages may redirect to
the same destination.

A redirect chain for a starting URL is formed by repeatedly following redirects until one of the
following happens:
1. you reach a URL with no outgoing redirect,
2. you revisit a URL already seen in the current chain, creating a cycle.

Two starting URLs are considered conflicting if their redirect chains eventually end at the same
terminal result. A terminal result is defined as either:
- the final URL with no outgoing redirect, or
- the canonical representation of the cycle they enter.

For a cycle, use the lexicographically smallest URL inside that cycle as its canonical representation.

Your task is to return the number of unordered pairs of distinct starting URLs whose redirect chains
conflict.

Only URLs that appear as a fromUrl in the input are considered valid starting URLs.

Constraints:
- 1 <= redirects.length <= 2 * 10^5
- redirects[i].length == 2
- 1 <= fromUrl.length, toUrl.length <= 30
- URLs consist of lowercase English letters, digits, '/', '-', and '_'
- Each fromUrl is unique
- The total length of all URL strings is at most 4 * 10^5

Important note:
The textual examples in the prompt contain inconsistent numeric outputs in a few places, but the
detailed explanations clearly show the intended correct answers:
- Example 1 resolves all 5 starting URLs to the same terminal result, so the answer is 10.
- Example 2 forms two groups of size 3, so the answer is 6.

This implementation follows the problem definition and those correct explanations.
*/

import java.util.*;

public class Solution {

    /**
     * Counts the number of unordered pairs of distinct starting URLs whose redirect chains
     * resolve to the same terminal result.
     *
     * A terminal result is:
     * - a URL with no outgoing redirect, or
     * - the lexicographically smallest URL in the cycle reached by the chain.
     *
     * Core idea:
     * 1. Build a directed graph where each starting URL has exactly one outgoing edge.
     * 2. Resolve every starting URL to its terminal result using DFS-style path walking with memoization.
     * 3. Group starting URLs by terminal result.
     * 4. For each group of size k, add k * (k - 1) / 2 to the answer.
     *
     * @param redirects the redirect pairs, where redirects[i][0] redirects to redirects[i][1]
     * @return the number of conflicting unordered pairs of starting URLs
     * Time complexity: O(n + totalStringWork), effectively O(n) graph work plus string comparisons
     *                  needed while identifying cycle minima, where n = redirects.length
     * Space complexity: O(n)
     */
    public long countConflictingRedirectChains(String[][] redirects) {
        Map<String, String> next = new HashMap<>(redirects.length * 2);
        for (String[] edge : redirects) {
            next.put(edge[0], edge[1]);
        }

        // resolved[url] = terminal result for this starting URL or intermediate URL that appears as a fromUrl.
        // Once computed, we never recompute it.
        Map<String, String> resolved = new HashMap<>(redirects.length * 2);

        // Resolve every valid starting URL (every key in the map).
        for (String start : next.keySet()) {
            if (!resolved.containsKey(start)) {
                resolveTerminal(start, next, resolved);
            }
        }

        // Count how many starting URLs map to each terminal result.
        Map<String, Integer> groupSize = new HashMap<>();
        for (String start : next.keySet()) {
            String terminal = resolved.get(start);
            groupSize.put(terminal, groupSize.getOrDefault(terminal, 0) + 1);
        }

        // Sum combinations C(k, 2) for each group.
        long answer = 0L;
        for (int size : groupSize.values()) {
            answer += (long) size * (size - 1) / 2;
        }

        return answer;
    }

    /**
     * Resolves one URL to its terminal result and memoizes results for every URL encountered
     * on the current walk that appears as a fromUrl.
     *
     * This method carefully handles three situations:
     * 1. We reach a URL with no outgoing redirect:
     *    that URL itself is the terminal result.
     * 2. We reach a URL whose terminal result was already computed:
     *    reuse that memoized result.
     * 3. We revisit a URL already seen in the current walk:
     *    we found a cycle. The terminal result for all nodes in that cycle and all nodes leading
     *    into it is the lexicographically smallest URL in the cycle.
     *
     * The graph is functional (out-degree at most 1 for starting URLs), which allows this linear
     * path-walking approach.
     *
     * @param start the URL to resolve
     * @param next mapping from fromUrl to toUrl
     * @param resolved memoization map storing already computed terminal results
     * @return the terminal result for start
     * Time complexity: Amortized O(length of newly explored path)
     * Space complexity: O(length of current path)
     */
    public String resolveTerminal(String start, Map<String, String> next, Map<String, String> resolved) {
        // If already known, return immediately.
        if (resolved.containsKey(start)) {
            return resolved.get(start);
        }

        // path stores the exact traversal order of URLs encountered in this walk.
        List<String> path = new ArrayList<>();

        // indexInPath lets us detect a cycle inside the current traversal.
        // If we see the same URL again before finishing, we know exactly where the cycle starts.
        Map<String, Integer> indexInPath = new HashMap<>();

        String current = start;

        while (true) {
            // Case 1: current is a URL whose terminal result is already known.
            // Then every URL on the current path resolves to the same terminal result.
            if (resolved.containsKey(current)) {
                String terminal = resolved.get(current);

                // Propagate this terminal result backward to every URL on the path.
                for (String url : path) {
                    resolved.put(url, terminal);
                }
                return terminal;
            }

            // Case 2: current has no outgoing redirect.
            // This means current itself is the terminal result.
            if (!next.containsKey(current)) {
                String terminal = current;

                // Every URL on the path resolves to this final URL.
                for (String url : path) {
                    resolved.put(url, terminal);
                }
                return terminal;
            }

            // Case 3: current was already seen in this same traversal.
            // That means we found a cycle.
            Integer cycleStartIndex = indexInPath.get(current);
            if (cycleStartIndex != null) {
                // The cycle consists of path[cycleStartIndex ... path.size()-1].
                // We must compute the canonical representation:
                // the lexicographically smallest URL in the cycle.
                String canonicalCycleRep = path.get(cycleStartIndex);
                for (int i = cycleStartIndex + 1; i < path.size(); i++) {
                    String candidate = path.get(i);
                    if (candidate.compareTo(canonicalCycleRep) < 0) {
                        canonicalCycleRep = candidate;
                    }
                }

                // All nodes in the cycle resolve to the canonical cycle representative.
                for (int i = cycleStartIndex; i < path.size(); i++) {
                    resolved.put(path.get(i), canonicalCycleRep);
                }

                // All nodes before the cycle also resolve to that same cycle representative.
                for (int i = 0; i < cycleStartIndex; i++) {
                    resolved.put(path.get(i), canonicalCycleRep);
                }

                return canonicalCycleRep;
            }

            // Otherwise, continue walking forward.
            indexInPath.put(current, path.size());
            path.add(current);
            current = next.get(current);
        }
    }

    /**
     * Builds and returns the terminal result for every valid starting URL.
     * This is useful for demonstration, debugging, and understanding how the algorithm works.
     *
     * @param redirects the redirect pairs
     * @return a map from each starting URL (each fromUrl) to its resolved terminal result
     * Time complexity: O(n + totalStringWork)
     * Space complexity: O(n)
     */
    public Map<String, String> resolveAllStartingUrls(String[][] redirects) {
        Map<String, String> next = new HashMap<>(redirects.length * 2);
        for (String[] edge : redirects) {
            next.put(edge[0], edge[1]);
        }

        Map<String, String> resolved = new HashMap<>(redirects.length * 2);
        for (String start : next.keySet()) {
            if (!resolved.containsKey(start)) {
                resolveTerminal(start, next, resolved);
            }
        }

        Map<String, String> result = new TreeMap<>();
        for (String start : next.keySet()) {
            result.put(start, resolved.get(start));
        }
        return result;
    }

    /**
     * Demonstrates the solution on the sample inputs and prints the results.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n) for each demonstration input
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[][] redirects1 = {
                {"/a", "/b"},
                {"/b", "/final"},
                {"/c", "/final"},
                {"/d", "/e"},
                {"/e", "/final"}
        };

        String[][] redirects2 = {
                {"/p", "/q"},
                {"/q", "/p"},
                {"/x", "/q"},
                {"/m", "/n"},
                {"/n", "/end"},
                {"/z", "/end"}
        };

        long answer1 = solution.countConflictingRedirectChains(redirects1);
        long answer2 = solution.countConflictingRedirectChains(redirects2);

        System.out.println("Example 1 terminal results:");
        for (Map.Entry<String, String> entry : solution.resolveAllStartingUrls(redirects1).entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println("Example 1 conflicting pairs = " + answer1);
        System.out.println("Expected = 10");
        System.out.println();

        System.out.println("Example 2 terminal results:");
        for (Map.Entry<String, String> entry : solution.resolveAllStartingUrls(redirects2).entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println("Example 2 conflicting pairs = " + answer2);
        System.out.println("Expected = 6");
    }
}