import java.util.*;

/*
Problem Title: Longest Compliance Window with Forbidden Pair Threshold

Problem Description:
A security team monitors a sequence of access events represented by an integer array events, where each value denotes the policy group of the event. Some pairs of policy groups are considered incompatible when they appear together too often in a short time span. You are also given a list forbiddenPairs, where each element [a, b] means groups a and b form a forbidden pair, and an integer limit.

For any contiguous window of events, define its conflict count as the total number of index pairs (i, j) inside the window such that i < j, events[i] = a, events[j] = b, and {a, b} is a forbidden pair. If a forbidden pair is [x, x], then every pair of equal x values inside the window contributes to the conflict count. The order of values in forbiddenPairs does not matter: [2, 5] and [5, 2] describe the same rule.

Return the length of the longest contiguous subarray whose conflict count is at most limit.

A valid solution must be efficient enough for large inputs. The challenge is that when the window expands or shrinks, the number of newly created or removed conflicting pairs may be much larger than 1, so recomputing the entire window each time is too slow.

Constraints:
- 1 <= events.length <= 200000
- 1 <= events[i] <= 200000
- 1 <= forbiddenPairs.length <= 200000
- 1 <= a, b <= 200000
- 0 <= limit <= 10^15
- forbiddenPairs may contain duplicates; duplicates should be treated as a single forbidden rule.

Example 1:
Input: events = [1,2,1,3,2,1], forbiddenPairs = [[1,2],[2,3]], limit = 2
Output: 4
Explanation: The window [1,3,2,1] has conflict count 2: pair (2,3) appears once and pair (1,2) appears once. No longer valid window has conflict count at most 2.

Example 2:
Input: events = [4,4,4,2,4], forbiddenPairs = [[4,4],[2,4]], limit = 3
Output: 3
Explanation: In window [4,4,2], the equal-group pair contribution from [4,4] is 1, and the cross-group contribution from [2,4] is 2, for a total of 3. Any length-4 window exceeds the limit.
*/

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray whose conflict count
     * is at most the given limit.
     *
     * Core idea:
     * We use a sliding window [left, right]. The difficult part is updating the
     * conflict count efficiently when we add or remove one event.
     *
     * Let the new value be x.
     * - When x is added to the right side of the window:
     *   every existing value y already in the window forms exactly count[y]
     *   new conflicting pairs with x if {x, y} is forbidden.
     *   So the increase is:
     *       sum(count[y]) over all forbidden neighbors y of x
     *   with special handling that [x, x] contributes count[x].
     *
     * - When the leftmost value x is removed from the window:
     *   it currently forms conflicting pairs with the remaining values in the window.
     *   After decrementing count[x], the number of removed conflicting pairs is:
     *       sum(count[y]) over all forbidden neighbors y of x in the remaining window
     *   and [x, x] contributes the new count[x] after decrement.
     *
     * To make these updates fast, we split values into:
     * - heavy values: values with many forbidden neighbors
     * - light values: values with few forbidden neighbors
     *
     * For heavy values h, we maintain:
     *   heavyNeighborCount[h] = sum(count[y]) for all forbidden neighbors y of h
     * This lets us query the add/remove contribution for h in O(1).
     *
     * When any count[v] changes by +1 or -1, we update heavyNeighborCount[h]
     * for every heavy value h that is forbidden with v.
     *
     * This gives an efficient near O((n + m) * sqrt(m)) style solution suitable
     * for the given constraints.
     *
     * @param events the array of event group IDs
     * @param forbiddenPairs the list of forbidden unordered pairs
     * @param limit the maximum allowed conflict count inside a window
     * @return the maximum valid window length
     * Time complexity: O((n + U + m) * sqrt(m)) in the standard heavy-light bound,
     *                  where n = events.length, m = number of distinct forbidden rules,
     *                  U = maximum value appearing in inputs (bounded here by 200000)
     * Space complexity: O(U + m)
     */
    public int longestComplianceWindow(int[] events, int[][] forbiddenPairs, long limit) {
        int maxValue = findMaxValue(events, forbiddenPairs);

        List<Integer>[] graph = buildDistinctForbiddenGraph(maxValue, forbiddenPairs);

        int threshold = Math.max(1, (int) Math.sqrt(Math.max(1, countDistinctEdges(graph, maxValue))));
        int[] heavyIdOfValue = new int[maxValue + 1];
        Arrays.fill(heavyIdOfValue, -1);

        List<Integer> heavyValuesList = new ArrayList<>();
        for (int value = 1; value <= maxValue; value++) {
            if (graph[value] != null && graph[value].size() >= threshold) {
                heavyIdOfValue[value] = heavyValuesList.size();
                heavyValuesList.add(value);
            }
        }

        int heavyCount = heavyValuesList.size();

        List<Integer>[] heavyLinkedToValue = buildHeavyReverseLinks(maxValue, graph, heavyIdOfValue);
        long[] heavyNeighborCount = new long[heavyCount];
        long[] count = new long[maxValue + 1];

        long conflicts = 0L;
        int left = 0;
        int best = 0;

        for (int right = 0; right < events.length; right++) {
            int x = events[right];

            /*
             * STEP 1: Add events[right] = x into the current window.
             *
             * We must count how many NEW conflicting pairs are created by this new x.
             * Those are exactly the existing window elements y such that {x, y} is forbidden.
             *
             * If x is heavy, we already maintain the total number of forbidden neighbors
             * currently present in the window in heavyNeighborCount[heavyIdOfValue[x]].
             *
             * If x is light, we simply iterate through all its forbidden neighbors and sum counts.
             */
            conflicts += contributionIfAdd(x, graph, count, heavyIdOfValue, heavyNeighborCount);

            /*
             * STEP 2: Actually increase the frequency of x in the window.
             */
            addValueToWindow(x, count, heavyLinkedToValue, heavyNeighborCount);

            /*
             * STEP 3: If the window became invalid, move left forward until valid again.
             */
            while (conflicts > limit) {
                int remove = events[left++];

                /*
                 * To remove the leftmost value "remove", we first decrease its count.
                 * After that, the number of conflicting pairs that disappear is exactly
                 * the number of remaining forbidden neighbors still in the window.
                 */
                removeValueFromWindow(remove, count, heavyLinkedToValue, heavyNeighborCount);
                conflicts -= contributionIfRemove(remove, graph, count, heavyIdOfValue, heavyNeighborCount);
            }

            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Finds the maximum value appearing either in events or in forbiddenPairs.
     * This lets us allocate compact arrays up to the required range.
     *
     * @param events the event values
     * @param forbiddenPairs the forbidden pair list
     * @return the maximum value encountered
     * Time complexity: O(n + p)
     * Space complexity: O(1)
     */
    public int findMaxValue(int[] events, int[][] forbiddenPairs) {
        int max = 0;
        for (int value : events) {
            max = Math.max(max, value);
        }
        for (int[] pair : forbiddenPairs) {
            max = Math.max(max, Math.max(pair[0], pair[1]));
        }
        return max;
    }

    /**
     * Builds an undirected graph of distinct forbidden rules.
     *
     * Important details:
     * - Duplicate forbidden pairs must be ignored.
     * - Since the pair is unordered, [a, b] and [b, a] are the same rule.
     * - A self-pair [x, x] is stored once in x's adjacency list.
     *
     * @param maxValue maximum value used for array sizing
     * @param forbiddenPairs input forbidden rules
     * @return adjacency list graph where graph[v] contains all forbidden neighbors of v
     * Time complexity: O(p)
     * Space complexity: O(m + maxValue), where m is the number of distinct rules
     */
    @SuppressWarnings("unchecked")
    public List<Integer>[] buildDistinctForbiddenGraph(int maxValue, int[][] forbiddenPairs) {
        List<Integer>[] graph = new ArrayList[maxValue + 1];
        HashSet<Long> seen = new HashSet<>(forbiddenPairs.length * 2);

        for (int[] pair : forbiddenPairs) {
            int a = pair[0];
            int b = pair[1];
            int x = Math.min(a, b);
            int y = Math.max(a, b);
            long key = encodePair(x, y);

            if (!seen.add(key)) {
                continue;
            }

            if (graph[x] == null) {
                graph[x] = new ArrayList<>();
            }
            graph[x].add(y);

            if (x != y) {
                if (graph[y] == null) {
                    graph[y] = new ArrayList<>();
                }
                graph[y].add(x);
            }
        }

        return graph;
    }

    /**
     * Counts the number of distinct forbidden rules represented by the graph.
     *
     * Since the graph is undirected:
     * - each non-self edge appears twice
     * - each self edge appears once
     *
     * @param graph adjacency list graph
     * @param maxValue maximum value index
     * @return number of distinct forbidden rules
     * Time complexity: O(maxValue + total adjacency size)
     * Space complexity: O(1)
     */
    public int countDistinctEdges(List<Integer>[] graph, int maxValue) {
        long total = 0;
        long self = 0;

        for (int v = 1; v <= maxValue; v++) {
            if (graph[v] == null) {
                continue;
            }
            for (int to : graph[v]) {
                total++;
                if (to == v) {
                    self++;
                }
            }
        }

        return (int) ((total - self) / 2 + self);
    }

    /**
     * Builds reverse links from a value v to all heavy nodes h such that {v, h} is forbidden.
     *
     * Why this is useful:
     * whenever count[v] changes by +1 or -1, every heavy node h that is connected to v
     * must update heavyNeighborCount[h] by the same delta.
     *
     * @param maxValue maximum value index
     * @param graph adjacency list graph
     * @param heavyIdOfValue maps a value to its heavy ID, or -1 if not heavy
     * @return reverse adjacency from value -> list of heavy IDs connected to that value
     * Time complexity: O(total adjacency size)
     * Space complexity: O(total heavy-related adjacency size + maxValue)
     */
    @SuppressWarnings("unchecked")
    public List<Integer>[] buildHeavyReverseLinks(int maxValue, List<Integer>[] graph, int[] heavyIdOfValue) {
        List<Integer>[] heavyLinkedToValue = new ArrayList[maxValue + 1];

        for (int heavyValue = 1; heavyValue <= maxValue; heavyValue++) {
            int heavyId = heavyIdOfValue[heavyValue];
            if (heavyId == -1 || graph[heavyValue] == null) {
                continue;
            }

            for (int neighbor : graph[heavyValue]) {
                if (heavyLinkedToValue[neighbor] == null) {
                    heavyLinkedToValue[neighbor] = new ArrayList<>();
                }
                heavyLinkedToValue[neighbor].add(heavyId);
            }
        }

        return heavyLinkedToValue;
    }

    /**
     * Computes how many new conflicting pairs are created if value x is added to the window.
     *
     * If x is heavy:
     * - heavyNeighborCount[heavyId] already equals the sum of counts of all forbidden neighbors.
     *
     * If x is light:
     * - we iterate through x's forbidden neighbors and sum their counts.
     *
     * Self-pair [x, x]:
     * - contributes count[x], which is naturally included because x appears in its own adjacency list.
     *
     * @param x the value being added
     * @param graph adjacency list of forbidden relations
     * @param count current frequencies inside the window before adding x
     * @param heavyIdOfValue heavy ID mapping
     * @param heavyNeighborCount maintained sums for heavy values
     * @return number of newly created conflicting pairs
     * Time complexity: O(1) for heavy x, O(deg(x)) for light x
     * Space complexity: O(1)
     */
    public long contributionIfAdd(int x, List<Integer>[] graph, long[] count, int[] heavyIdOfValue, long[] heavyNeighborCount) {
        int heavyId = heavyIdOfValue[x];
        if (heavyId != -1) {
            return heavyNeighborCount[heavyId];
        }

        long add = 0L;
        if (graph[x] != null) {
            for (int neighbor : graph[x]) {
                add += count[neighbor];
            }
        }
        return add;
    }

    /**
     * Adds one occurrence of value x into the current window and updates all helper structures.
     *
     * Detailed effect:
     * - count[x] increases by 1
     * - for every heavy node h forbidden with x, heavyNeighborCount[h] also increases by 1
     *   because the total number of h's forbidden neighbors currently in the window increased.
     *
     * @param x the value to add
     * @param count current frequencies in the window
     * @param heavyLinkedToValue reverse links from value to heavy IDs
     * @param heavyNeighborCount maintained sums for heavy values
     * Time complexity: O(number of heavy nodes connected to x)
     * Space complexity: O(1)
     */
    public void addValueToWindow(int x, long[] count, List<Integer>[] heavyLinkedToValue, long[] heavyNeighborCount) {
        count[x]++;

        if (heavyLinkedToValue[x] != null) {
            for (int heavyId : heavyLinkedToValue[x]) {
                heavyNeighborCount[heavyId]++;
            }
        }
    }

    /**
     * Removes one occurrence of value x from the current window and updates all helper structures.
     *
     * Important order:
     * - We first decrement count[x].
     * - Then we update heavyNeighborCount for heavy nodes connected to x.
     *
     * This order is correct because after removal, helper structures should represent
     * the remaining window.
     *
     * @param x the value to remove
     * @param count current frequencies in the window
     * @param heavyLinkedToValue reverse links from value to heavy IDs
     * @param heavyNeighborCount maintained sums for heavy values
     * Time complexity: O(number of heavy nodes connected to x)
     * Space complexity: O(1)
     */
    public void removeValueFromWindow(int x, long[] count, List<Integer>[] heavyLinkedToValue, long[] heavyNeighborCount) {
        count[x]--;

        if (heavyLinkedToValue[x] != null) {
            for (int heavyId : heavyLinkedToValue[x]) {
                heavyNeighborCount[heavyId]--;
            }
        }
    }

    /**
     * Computes how many conflicting pairs disappear after one occurrence of x has already
     * been removed from the window.
     *
     * After decrementing count[x], the removed element used to conflict with every remaining
     * forbidden neighbor y currently in the window.
     *
     * Therefore the number of deleted pairs is the same style of sum as "add", but evaluated
     * on the remaining window after x has been decremented.
     *
     * @param x the value that was removed
     * @param graph adjacency list of forbidden relations
     * @param count current frequencies inside the window after removing x
     * @param heavyIdOfValue heavy ID mapping
     * @param heavyNeighborCount maintained sums for heavy values
     * @return number of conflicting pairs removed
     * Time complexity: O(1) for heavy x, O(deg(x)) for light x
     * Space complexity: O(1)
     */
    public long contributionIfRemove(int x, List<Integer>[] graph, long[] count, int[] heavyIdOfValue, long[] heavyNeighborCount) {
        int heavyId = heavyIdOfValue[x];
        if (heavyId != -1) {
            return heavyNeighborCount[heavyId];
        }

        long removed = 0L;
        if (graph[x] != null) {
            for (int neighbor : graph[x]) {
                removed += count[neighbor];
            }
        }
        return removed;
    }

    /**
     * Encodes an unordered pair (a, b) with a <= b into one long key.
     *
     * @param a smaller endpoint
     * @param b larger endpoint
     * @return unique long key
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long encodePair(int a, int b) {
        return (((long) a) << 32) | (b & 0xffffffffL);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 4
     * Example 2 -> 3
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding called algorithm work
     * Space complexity: O(1) excluding called algorithm work
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] events1 = {1, 2, 1, 3, 2, 1};
        int[][] forbiddenPairs1 = {
            {1, 2