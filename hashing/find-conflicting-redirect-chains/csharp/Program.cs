/*
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

Your task is to return the number of unordered pairs of distinct starting URLs whose redirect chains conflict.

Only URLs that appear as a fromUrl in the input are considered valid starting URLs.

Constraints:
- 1 <= redirects.length <= 2 * 10^5
- redirects[i].length == 2
- 1 <= fromUrl.length, toUrl.length <= 30
- URLs consist of lowercase English letters, digits, '/', '-', and '_'
- Each fromUrl is unique
- The total length of all URL strings is at most 4 * 10^5
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n + total_cycle_sizes + total_string_work), which is effectively O(n) dictionary/graph work
    plus string hashing/comparison costs. Each starting URL is processed once, and each edge is
    traversed a constant number of times overall.

    Space Complexity:
    O(n), where n is the number of redirects / starting URLs, for:
    - the redirect map
    - the resolved terminal result map
    - temporary path/index tracking while exploring chains
    - grouping counts
    */
    public long CountConflictingRedirectPairs(IList<IList<string>> redirects)
    {
        // Step 1:
        // Build a direct lookup table: fromUrl -> toUrl.
        //
        // Why this is necessary:
        // We need to follow redirect chains quickly. A dictionary gives us average O(1)
        // lookup time for "what is the next URL from here?".
        //
        // Important observation:
        // Only URLs that appear as a fromUrl are valid starting URLs.
        // So the keys of this dictionary are exactly the starting URLs we care about.
        var next = new Dictionary<string, string>(redirects.Count);

        foreach (var pair in redirects)
        {
            next[pair[0]] = pair[1];
        }

        // Step 2:
        // resolved[url] = terminal result for this starting URL.
        //
        // The terminal result is:
        // - a sink URL (a URL with no outgoing redirect), OR
        // - the canonical representative of a cycle (lexicographically smallest URL in that cycle).
        //
        // Why memoization is necessary:
        // Many chains may merge. If we already know the terminal result for one URL,
        // every earlier URL that reaches it has the same result.
        // This avoids recomputing the same suffix many times.
        var resolved = new Dictionary<string, string>(redirects.Count);

        // Step 3:
        // Process every valid starting URL exactly once.
        //
        // If a URL is already resolved, we skip it.
        // Otherwise, we walk its chain until we hit:
        // - a sink,
        // - a previously resolved URL,
        // - or a cycle discovered in the current traversal.
        foreach (var start in next.Keys)
        {
            if (resolved.ContainsKey(start))
            {
                continue;
            }

            // path stores the exact order of URLs visited in THIS traversal.
            //
            // Why we need it:
            // Once we discover the terminal result, we must assign that same result
            // to every URL we visited on the way.
            var path = new List<string>();

            // indexInPath tells us whether a URL has already appeared in the current path,
            // and if so, at which index.
            //
            // Why this is necessary:
            // If we revisit a URL already in the current path, we found a cycle.
            // The cycle is path[cycleStartIndex .. end].
            var indexInPath = new Dictionary<string, int>();

            string current = start;

            while (true)
            {
                // Case A:
                // current has already been resolved in an earlier traversal.
                //
                // Then this entire current path must end at the same terminal result.
                if (resolved.TryGetValue(current, out var knownTerminal))
                {
                    foreach (var url in path)
                    {
                        resolved[url] = knownTerminal;
                    }
                    break;
                }

                // Case B:
                // current has no outgoing redirect.
                //
                // That means current itself is the terminal sink URL.
                // Every URL in the current path resolves to this sink.
                if (!next.TryGetValue(current, out var nextUrl))
                {
                    foreach (var url in path)
                    {
                        resolved[url] = current;
                    }
                    break;
                }

                // Case C:
                // current is already in the current path.
                //
                // This means we found a cycle.
                // We must compute the canonical representation of that cycle:
                // the lexicographically smallest URL inside the cycle.
                if (indexInPath.TryGetValue(current, out var cycleStartIndex))
                {
                    // Find the lexicographically smallest URL among the cycle nodes.
                    string canonicalCycleRepresentative = path[cycleStartIndex];

                    for (int i = cycleStartIndex + 1; i < path.Count; i++)
                    {
                        if (string.CompareOrdinal(path[i], canonicalCycleRepresentative) < 0)
                        {
                            canonicalCycleRepresentative = path[i];
                        }
                    }

                    // Every URL in the cycle resolves to the canonical cycle representative.
                    for (int i = cycleStartIndex; i < path.Count; i++)
                    {
                        resolved[path[i]] = canonicalCycleRepresentative;
                    }

                    // Every URL before the cycle also resolves to that same cycle representative,
                    // because following redirects from them eventually enters this cycle.
                    for (int i = 0; i < cycleStartIndex; i++)
                    {
                        resolved[path[i]] = canonicalCycleRepresentative;
                    }

                    break;
                }

                // Case D:
                // We are visiting current for the first time in this traversal.
                //
                // Record it in the path and continue following the redirect.
                indexInPath[current] = path.Count;
                path.Add(current);
                current = nextUrl;
            }
        }

        // Step 4:
        // Group starting URLs by their terminal result.
        //
        // Why this works:
        // Two starting URLs conflict if and only if they resolve to the same terminal result.
        // So if a terminal result is shared by k starting URLs, it contributes:
        // k choose 2 = k * (k - 1) / 2
        // conflicting unordered pairs.
        var groupCounts = new Dictionary<string, long>();

        foreach (var start in next.Keys)
        {
            string terminal = resolved[start];

            if (!groupCounts.ContainsKey(terminal))
            {
                groupCounts[terminal] = 0;
            }

            groupCounts[terminal]++;
        }

        // Step 5:
        // Sum combinations over all groups.
        long answer = 0;

        foreach (var count in groupCounts.Values)
        {
            answer += count * (count - 1) / 2;
        }

        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1
// Redirects:
// /a -> /b -> /final
// /c -> /final
// /d -> /e -> /final
//
// Starting URLs are: /a, /b, /c, /d, /e
// All of them resolve to /final
// So answer = C(5, 2) = 10
var redirects1 = new List<IList<string>>
{
    new List<string> { "/a", "/b" },
    new List<string> { "/b", "/final" },
    new List<string> { "/c", "/final" },
    new List<string> { "/d", "/e" },
    new List<string> { "/e", "/final" }
};

Console.WriteLine(solution.CountConflictingRedirectPairs(redirects1)); // Expected: 10

// Example 2
// Cycle: /p <-> /q, canonical representative is /p
// /x -> /q -> cycle => /p
// /m -> /n -> /end
// /z -> /end
//
// Starting URLs: /p, /q, /x, /m, /n, /z
// Group 1: /p, /q, /x => size 3 => 3 pairs
// Group 2: /m, /n, /z => size 3 => 3 pairs
// Total = 6
var redirects2 = new List<IList<string>>
{
    new List<string> { "/p", "/q" },
    new List<string> { "/q", "/p" },
    new List<string> { "/x", "/q" },
    new List<string> { "/m", "/n" },
    new List<string> { "/n", "/end" },
    new List<string> { "/z", "/end" }
};

Console.WriteLine(solution.CountConflictingRedirectPairs(redirects2)); // Expected: 6

// Additional demo with multiple separate outcomes
var redirects3 = new List<IList<string>>
{
    new List<string> { "/u1", "/u2" },
    new List<string> { "/u2", "/sink1" },
    new List<string> { "/v1", "/v2" },
    new List<string> { "/v2", "/v1" },
    new List<string> { "/w1", "/sink1" },
    new List<string> { "/x1", "/x2" },
    new List<string> { "/x2", "/sink2" }
};

// Terminal results:
// /u1 -> /sink1
// /u2 -> /sink1
// /w1 -> /sink1   => group size 3 => 3 pairs
// /v1 -> canonical cycle rep /v1
// /v2 -> canonical cycle rep /v1 => group size 2 => 1 pair
// /x1 -> /sink2
// /x2 -> /sink2   => group size 2 => 1 pair
// Total = 5
Console.WriteLine(solution.CountConflictingRedirectPairs(redirects3)); // Expected: 5