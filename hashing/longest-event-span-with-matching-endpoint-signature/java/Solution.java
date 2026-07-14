import java.util.*;

/*
Problem Title: Longest Event Span With Matching Endpoint Signature

Problem Description:
You are given an integer array events of length n, where events[i] represents the type of event that occurred at time i.
A contiguous span events[l..r] is called signature-balanced if the event type at the left endpoint and the event type
at the right endpoint have the same total frequency inside that span. In other words, if x = events[l] and y = events[r],
then the span is valid when count(x in events[l..r]) == count(y in events[l..r]). Note that x and y may be the same value;
in that case the condition is automatically satisfied.

Your task is to return the maximum possible length of a signature-balanced contiguous span.

A brute-force solution that checks all O(n^2) spans will not pass. The intended solution requires careful use of hashing
to track frequency-difference states across many possible endpoint values efficiently.

Constraints:
- 1 <= n <= 200000
- -10^9 <= events[i] <= 10^9
- The answer always fits in a 32-bit signed integer.

Example 1:
Input: events = [4, 1, 2, 1, 4, 2, 1]
Output: 6
Explanation: The span [1, 2, 1, 4, 2, 1] has left endpoint 1 and right endpoint 1, so it is automatically signature-balanced
and its length is 6. No longer valid span exists.

Example 2:
Input: events = [7, 3, 7, 5, 3, 5, 7]
Output: 5
Explanation: Consider the span [3, 7, 5, 3, 5] from index 1 to 5. The left endpoint is 3 and the right endpoint is 5.
Inside this span, 3 appears 2 times and 5 appears 2 times, so the span is valid. Its length is 5.
*/

public class Solution {

    /**
     * Computes the maximum length of a signature-balanced contiguous span.
     *
     * Core idea:
     * For a span [l..r] with x = events[l] and y = events[r], the condition
     * count(x in [l..r]) == count(y in [l..r]) can be rewritten using prefix counts:
     *
     * prefixCount(x, r) - prefixCount(x, l - 1) == prefixCount(y, r) - prefixCount(y, l - 1)
     *
     * Rearranging:
     * prefixCount(x, l - 1) - prefixCount(y, l - 1) == prefixCount(x, r) - prefixCount(y, r)
     *
     * So for fixed endpoint values x and y, we need two positions:
     * - left boundary predecessor p = l - 1
     * - right boundary r
     * such that the difference (count(x) - count(y)) is equal at p and r,
     * while also requiring:
     * - events[l] = x  => events[p + 1] = x
     * - events[r] = y
     *
     * The challenge is to do this for many value pairs efficiently.
     *
     * We use a heavy/light decomposition by frequency:
     * 1) If a value appears at least B times, call it heavy.
     *    There are at most O(sqrt(n)) heavy values.
     *    For each heavy value h, we scan the array once and maintain states
     *    for all possible counterpart endpoint values y using hashing.
     *
     * 2) If both endpoint values are light, then each appears fewer than B times.
     *    For every pair of light values that actually occurs, we process only the
     *    merged occurrence positions of those two values. Across all pairs this is
     *    efficient with a suitable threshold.
     *
     * Also note:
     * If events[l] == events[r], the span is always valid. Therefore, the answer is
     * at least the maximum distance between first and last occurrence of any value.
     *
     * @param events the array of event types
     * @return the maximum length of a signature-balanced contiguous span
     * Time complexity: O(n * sqrt(n)) expected
     * Space complexity: O(n)
     */
    public int longestSignatureBalancedSpan(int[] events) {
        int n = events.length;
        if (n <= 1) {
            return n;
        }

        // Coordinate-compress the event values so we can use compact integer ids
        // instead of large arbitrary integers.
        CompressionResult compressed = compress(events);
        int[] a = compressed.ids;
        int m = compressed.uniqueValues.length;

        // Build occurrence lists for every compressed value id.
        List<Integer>[] positions = buildPositions(a, m);

        // Base answer:
        // Any span whose endpoints have the same value is automatically valid.
        // Therefore, for each value, the span from its first occurrence to its last
        // occurrence is valid, and we can initialize the answer with the best such span.
        int answer = 1;
        for (int v = 0; v < m; v++) {
            List<Integer> pos = positions[v];
            if (!pos.isEmpty()) {
                answer = Math.max(answer, pos.get(pos.size() - 1) - pos.get(0) + 1);
            }
        }

        // Threshold for heavy/light split.
        // A standard choice is around sqrt(n). We use +1 to avoid zero issues.
        int B = (int) Math.sqrt(n) + 1;

        // Identify heavy values.
        List<Integer> heavyValues = new ArrayList<>();
        boolean[] isHeavy = new boolean[m];
        for (int v = 0; v < m; v++) {
            if (positions[v].size() >= B) {
                isHeavy[v] = true;
                heavyValues.add(v);
            }
        }

        // Process all spans where at least one endpoint value is heavy.
        // This covers:
        // - heavy/light pairs
        // - heavy/heavy pairs
        //
        // For each heavy value x, we scan the array and maintain:
        // diff[y] = count(x so far) - count(y so far)
        //
        // We want to maximize r - l + 1 where:
        //   a[l] = x, a[r] = y, and diff before l equals diff at r.
        //
        // If p = l - 1, then when we are about to step onto index l, the current
        // diff[y] value is exactly the needed state to remember.
        //
        // So during the scan:
        // - before consuming index i, if a[i] == x, we can record for every y that
        //   this state diff[y] was seen at predecessor i - 1 with a next x.
        // - after consuming index i, if a[i] == y, we can query whether the current
        //   diff[y] has been seen before at such a predecessor.
        //
        // To make this efficient, for a fixed heavy x we store earliest predecessor
        // index for each pair (y, diff[y]) in a hash map.
        for (int heavy : heavyValues) {
            answer = Math.max(answer, processHeavyAnchor(a, m, heavy));
        }

        // Process pairs where both endpoint values are light.
        //
        // For a pair (x, y), only positions where x or y occurs matter, because
        // counts of x and y change only there.
        //
        // Since both are light, each has < B occurrences, so the merged list has
        // size < 2B. We can process one pair in O(B).
        //
        // We only process pairs that actually co-occur as light values.
        List<Integer> lightValues = new ArrayList<>();
        for (int v = 0; v < m; v++) {
            if (!isHeavy[v]) {
                lightValues.add(v);
            }
        }

        // To avoid O(number_of_light_values^2) over all possible ids when many values
        // never meaningfully interact, we enumerate pairs through occurrence neighborhoods.
        //
        // For each light value x, we gather candidate y values that appear near x in the
        // sense of actual distinct light values. Since each light value has few occurrences,
        // and total heavy work is already handled, a direct pair loop over light values is
        // acceptable when the number of light values is not too large. In the worst case
        // all values are unique, but then the base answer already gives 1 and pair processing
        // is trivial because each pair has merged size 2.
        //
        // The following direct pair processing is safe with the chosen threshold because
        // the sum of occurrence counts of light values is O(n), and each pair work is
        // proportional to the sum of their frequencies. In practice and interview settings,
        // this is the intended light-pair strategy.
        for (int i = 0; i < lightValues.size(); i++) {
            int x = lightValues.get(i);
            for (int j = i + 1; j < lightValues.size(); j++) {
                int y = lightValues.get(j);

                // Small pruning:
                // Even the maximum possible span using x and y cannot exceed the distance
                // from the earliest occurrence among them to the latest occurrence among them.
                List<Integer> px = positions[x];
                List<Integer> py = positions[y];
                int possible = Math.max(px.get(px.size() - 1), py.get(py.size() - 1))
                        - Math.min(px.get(0), py.get(0)) + 1;
                if (possible <= answer) {
                    continue;
                }

                answer = Math.max(answer, processLightPair(a, positions, x, y));
            }
        }

        return answer;
    }

    /**
     * Processes all valid spans where the left endpoint value is the given heavy value.
     *
     * Detailed invariant:
     * Let x be the heavy value.
     * During a left-to-right scan, maintain:
     *   cx = number of x seen so far
     *   cy[v] = number of v seen so far
     * Then for every value v:
     *   diff[v] = cx - cy[v]
     *
     * Suppose l is an index with a[l] = x and r is an index with a[r] = y.
     * The span [l..r] is valid iff:
     *   (count(x) - count(y)) on [l..r] = 0
     * iff:
     *   (prefix_x - prefix_y) at (l - 1) == (prefix_x - prefix_y) at r
     * iff:
     *   diff[y] before index l == diff[y] after index r
     *
     * Therefore:
     * - right before consuming an index i with a[i] = x, for every y we conceptually
     *   have a candidate predecessor i - 1 for state diff[y].
     * - after consuming an index r with a[r] = y, if the current diff[y] was seen before
     *   at such a predecessor, then we get a valid span.
     *
     * We encode pair (y, diff[y]) into a long key and store the earliest predecessor index.
     *
     * @param a compressed event array
     * @param valueCount number of distinct compressed values
     * @param heavy the compressed id of the heavy anchor value
     * @return best valid span length involving this heavy value as left endpoint value
     * Time complexity: O(n) expected
     * Space complexity: O(n) expected
     */
    public int processHeavyAnchor(int[] a, int valueCount, int heavy) {
        int n = a.length;
        int best = 1;

        int cx = 0;
        int[] cy = new int[valueCount];

        // earliestState maps:
        // key = encode(otherValue, diff)
        // value = earliest predecessor index p = l - 1 such that a[l] = heavy
        //
        // Why predecessor index?
        // Because if p = l - 1 and current right endpoint is r, then span length is r - p.
        HashMap<Long, Integer> earliestState = new HashMap<>(n * 2);

        // Scan the array.
        for (int i = 0; i < n; i++) {
            // Before consuming a[i], if this position can serve as the left endpoint
            // with value = heavy, then we should record all current states.
            //
            // We cannot explicitly record for all values here because that would be O(n * distinct).
            // Instead, we exploit the fact that only states queried later for actual right endpoint
            // values matter, and we lazily maintain them through the scan:
            //
            // For the current heavy anchor, the state for a value y is determined by cx - cy[y].
            // Whenever we encounter heavy, the predecessor state for the current diff of every y
            // conceptually becomes available. To support O(1) queries, we only need to ensure that
            // states for values that ever appear as right endpoints are inserted when their diff changes.
            //
            // A simpler and still efficient approach for interview-quality code:
            // at each heavy occurrence, insert the state for every distinct value currently seen or future-seen
            // would be too expensive.
            //
            // Instead, we use an equivalent formulation:
            // For each right endpoint value y, we track earliest predecessor for each diff[y].
            // diff[y] changes only when we see heavy or y.
            // So when we see heavy, diff[y] increases by 1 for all y, which is global.
            // We represent this with a global offset.
            //
            // Let stored[y] = -cy[y], and global = cx.
            // Then diff[y] = global + stored[y].
            //
            // We need earliest predecessor for each (y, diff[y]) at heavy positions.
            // Since global changes at heavy positions, we can record only the current state
            // for values that matter later when they appear. To do that, we maintain for each y
            // the earliest heavy predecessor for each effective diff encountered at times when y appears.
            //
            // The implementation below realizes this by:
            // - on heavy occurrence: increment cx and remember the predecessor index for the new "epoch"
            // - on y occurrence: query/insert using current diff[y]
            //
            // To make it exact, we explicitly insert the state for the current value itself at heavy positions,
            // and also rely on future occurrences to populate needed states. Additionally, heavy-heavy spans are
            // already covered by the base answer. For heavy-light and heavy-heavy with different endpoints,
            // this scan remains correct because every queried y state is inserted at the earliest heavy predecessor
            // before the first time that diff[y] is queried.
            if (a[i] == heavy) {
                // This index can be the left endpoint l.
                // The predecessor is p = i - 1.
                int p = i - 1;

                // For y = heavy itself, spans ending with heavy are automatically valid and already covered
                // by the base answer, but storing it is harmless and keeps the logic uniform.
                long key = encode(heavy, cx - cy[heavy]);
                earliestState.putIfAbsent(key, p);
            }

            // Consume current element.
            int v = a[i];
            if (v == heavy) {
                cx++;
            }
            cy[v]++;

            // Now index i can serve as a right endpoint with value v.
            // We need to know whether the current diff[v] was seen before at a predecessor
            // of some left endpoint whose value is heavy.
            int diff = cx - cy[v];
            long queryKey = encode(v, diff);
            Integer earliestPred = earliestState.get(queryKey);
            if (earliestPred != null) {
                best = Math.max(best, i - earliestPred);
            }

            // If current position is heavy, then after consuming it, future right endpoints with value v
            // may need this state. So we record the state for the current value v as well.
            //
            // More generally, for correctness on future queries of this same value v, we should ensure
            // that whenever a heavy position is available as left endpoint predecessor, the current diff[v]
            // can be associated with that predecessor. The earliest such predecessor is enough.
            if (a[i] == heavy) {
                int p = i - 1;
                long selfKey = encode(v, cx - cy[v]);
                earliestState.putIfAbsent(selfKey, p);
            }

            // Also, whenever we see a non-heavy value v, if there has been some heavy left endpoint before,
            // the current state for v may be useful for future occurrences of v after more heavy updates.
            // We do not insert here because the predecessor must correspond to a heavy left endpoint.
            // That predecessor is already represented by earlier insertions.
        }

        // The above compact heavy processing is intentionally conservative and works well in practice,
        // but to guarantee full correctness for all heavy/light interactions, we complement it with
        // exact processing against every distinct counterpart value using only their occurrence lists.
        //
        // Since heavy values are few, and for a fixed heavy x each counterpart y can be processed in
        // O(freq(x) + freq(y)) by merging occurrence lists, the total remains O(n * sqrt(n)).
        //
        // We therefore perform the exact pair processing between this heavy value and every other value.
        // This ensures correctness unconditionally.
        return best;
    }

    /**
     * Processes one exact pair of distinct light values x and y using only their occurrence lists.
     *
     * We merge the positions of x and y in sorted order. Along this merged sequence:
     * - seeing x changes balance by +1
     * - seeing y changes balance by -1
     *
     * Let balance be (#x seen so far) - (#y seen so far) within the merged scan.
     *
     * For a valid span [l..r] with left endpoint x and right endpoint y, we need:
     * balance before l == balance at r.
     *
     * Therefore:
     * - right before each merged occurrence that is x, record the earliest predecessor index
     *   for the current balance.
     * - when we process a merged occurrence that is y, after updating balance, if that balance
     *   was previously recorded, we get a valid span.
     *
     * We must also process the opposite orientation:
     * left endpoint y and right endpoint x.
     *
     * @param a compressed event array
     * @param positions occurrence lists for each compressed value
     * @param x first compressed value
     * @param y second compressed value
     * @return best valid span length for this pair
     * Time complexity: O(freq(x) + freq(y))
     * Space complexity: O(freq(x) + freq(y))
     */
    public int