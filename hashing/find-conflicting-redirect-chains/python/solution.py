"""
Title: Find Conflicting Redirect Chains

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
"""

from typing import Dict, List


class Solution:
    def count_conflicting_redirect_chains(self, redirects: List[List[str]]) -> int:
        """
        Count unordered pairs of starting URLs whose redirect chains resolve to the same terminal result.

        A terminal result is either:
        - a URL with no outgoing redirect, or
        - the lexicographically smallest URL inside a detected cycle.

        Args:
            redirects: List of [fromUrl, toUrl] redirect pairs.

        Returns:
            The number of unordered conflicting pairs among all valid starting URLs.

        Time complexity:
            O(n + total_string_comparison_cost), where n is the number of redirects.
            Each starting URL is resolved once due to memoization, and each edge is processed
            a constant number of times overall.

        Space complexity:
            O(n) for the redirect map, memoization map, and temporary path-tracking structures.
        """
        # Build a direct lookup table:
        # redirect_map[u] = v means URL u redirects to URL v.
        #
        # Why a dictionary?
        # - We need O(1) average-time access to "what is the next URL from here?"
        # - The problem guarantees each fromUrl is unique, so a dictionary is a perfect fit.
        redirect_map: Dict[str, str] = {}
        for from_url, to_url in redirects:
            redirect_map[from_url] = to_url

        # memo[url] will store the already-computed terminal result for this starting URL.
        #
        # This is the key optimization:
        # once we know where a URL eventually ends, any future chain that reaches this URL
        # can immediately reuse the answer instead of recomputing the rest of the chain.
        memo: Dict[str, str] = {}

        # Resolve the terminal result for every valid starting URL.
        #
        # Important:
        # Only URLs that appear as a fromUrl are considered starting URLs.
        # So we iterate over redirect_map keys, not over every URL seen anywhere.
        for start_url in redirect_map:
            if start_url not in memo:
                self._resolve_terminal(start_url, redirect_map, memo)

        # Count how many starting URLs map to each terminal result.
        #
        # Example:
        # if terminal "/final" is reached by 5 starting URLs, then that group contributes
        # 5 choose 2 = 10 conflicting unordered pairs.
        group_count: Dict[str, int] = {}
        for start_url in redirect_map:
            terminal = memo[start_url]
            group_count[terminal] = group_count.get(terminal, 0) + 1

        # Sum combinations size * (size - 1) // 2 over all groups.
        total_pairs = 0
        for size in group_count.values():
            total_pairs += size * (size - 1) // 2

        return total_pairs

    def _resolve_terminal(
        self,
        start_url: str,
        redirect_map: Dict[str, str],
        memo: Dict[str, str],
    ) -> str:
        """
        Resolve the terminal result for one starting URL and memoize all URLs encountered on the path.

        This method follows redirects until it reaches:
        - a URL with no outgoing redirect, or
        - a URL whose terminal result is already memoized, or
        - a cycle inside the current traversal.

        For a cycle, the terminal result is the lexicographically smallest URL in that cycle.

        Args:
            start_url: The starting URL to resolve.
            redirect_map: Mapping from fromUrl to toUrl.
            memo: Cache of already-resolved terminal results.

        Returns:
            The terminal result for start_url.

        Time complexity:
            Amortized O(length of newly explored chain). Across all calls, each URL is processed
            only a constant number of times before being memoized.

        Space complexity:
            O(length of current traversal path) for path storage and index tracking.
        """
        # If already solved, return immediately.
        if start_url in memo:
            return memo[start_url]

        # path stores the exact sequence of URLs visited in this traversal.
        #
        # We need the ordered path for two reasons:
        # 1. If we hit a known terminal or a dead end, we can assign that same terminal
        #    to every URL in the path.
        # 2. If we detect a cycle, we need to know exactly which suffix of the path forms
        #    the cycle so we can compute its canonical representative.
        path: List[str] = []

        # index_in_path[url] = position of url inside path.
        #
        # This lets us detect a cycle in O(1) average time:
        # if we revisit a URL already in the current path, then the segment from its first
        # occurrence to the end of the path is the cycle.
        index_in_path: Dict[str, int] = {}

        current = start_url

        while True:
            # Case 1:
            # We reached a URL whose terminal result is already known.
            #
            # Then every URL in the current path must resolve to that same terminal result.
            if current in memo:
                terminal = memo[current]
                for url in path:
                    memo[url] = terminal
                return terminal

            # Case 2:
            # current has no outgoing redirect.
            #
            # That means current itself is the terminal URL.
            # Even if current is not a valid starting URL, it is still a valid terminal result.
            if current not in redirect_map:
                terminal = current
                for url in path:
                    memo[url] = terminal
                return terminal

            # Case 3:
            # current is already in the current traversal path.
            #
            # This means we found a cycle.
            # Example path: [a, b, c, d], and current == b
            # Then cycle is [b, c, d].
            if current in index_in_path:
                cycle_start_index = index_in_path[current]
                cycle_nodes = path[cycle_start_index:]

                # The problem asks us to represent a cycle by the lexicographically smallest
                # URL inside that cycle.
                cycle_canonical = min(cycle_nodes)

                # Every URL in the cycle resolves to this canonical cycle representation.
                for url in cycle_nodes:
                    memo[url] = cycle_canonical

                # Every URL before the cycle also eventually enters the same cycle,
                # so they resolve to the same canonical representation too.
                for i in range(cycle_start_index):
                    memo[path[i]] = cycle_canonical

                return cycle_canonical

            # Otherwise, continue walking forward.
            #
            # We record current in the path and remember its index before moving to the next URL.
            index_in_path[current] = len(path)
            path.append(current)
            current = redirect_map[current]


if __name__ == "__main__":
    solution = Solution()

    redirects1 = [
        ["/a", "/b"],
        ["/b", "/final"],
        ["/c", "/final"],
        ["/d", "/e"],
        ["/e", "/final"],
    ]
    result1 = solution.count_conflicting_redirect_chains(redirects1)
    print("Example 1 result:", result1)  # Expected: 10

    redirects2 = [
        ["/p", "/q"],
        ["/q", "/p"],
        ["/x", "/q"],
        ["/m", "/n"],
        ["/n", "/end"],
        ["/z", "/end"],
    ]
    result2 = solution.count_conflicting_redirect_chains(redirects2)
    print("Example 2 result:", result2)  # Expected: 6

    redirects3 = [
        ["/a", "/b"],
        ["/b", "/c"],
        ["/c", "/a"],
        ["/x", "/y"],
        ["/y", "/z"],
    ]
    result3 = solution.count_conflicting_redirect_chains(redirects3)
    print("Example 3 result:", result3)  # Cycle group size 3 -> 3 pairs, chain to /z group size 2 -> 1 pair, total 4