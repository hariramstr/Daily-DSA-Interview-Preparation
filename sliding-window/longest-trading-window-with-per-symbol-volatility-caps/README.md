# Longest Trading Window With Per-Symbol Volatility Caps

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Monotonic Queue

---

## 🗂 Problem Overview
Given two aligned arrays, `symbols` and `prices`, find the maximum length of a contiguous subarray such that, for every symbol appearing in that window, that symbol’s local price range stays within `limit`. The challenge is that validity is not global across all prices; it must be enforced independently per symbol while symbols are interleaved arbitrarily. The output is a single integer: the longest valid window length.

## 🌍 Engineering Impact
This pattern shows up in streaming market surveillance, per-tenant SLO enforcement, keyed anomaly detection, and event pipelines where constraints apply independently to each entity inside one shared time window. Examples include fraud systems tracking per-card spend spread, observability backends enforcing per-service latency bands, and ad systems validating per-campaign bid drift. Without a keyed sliding-window design, implementations degrade into repeated rescans or per-window recomputation, which collapses under high-cardinality streams. The right approach enables online processing, bounded memory growth per active key, and predictable latency under sustained throughput.

## 🔍 Problem Statement
You are given `n` time-ordered trade events, where `symbols[i]` is the traded stock symbol and `prices[i]` is the execution price at time `i`. Return the length of the longest contiguous window such that, for every symbol present in that window, `maxPrice(symbol) - minPrice(symbol) <= limit`.

Constraints:

- `1 <= n == symbols.length == prices.length <= 2 * 10^5`
- `1 <= prices[i] <= 10^9`
- `0 <= limit <= 10^9`
- `symbols[i]` is a non-empty lowercase string
- Total symbol characters across the input are at most `2 * 10^5`

Example 1:

```text
symbols = ["aapl","msft","aapl","aapl","msft","goog"]
prices  = [100,200,103,101,198,250]
limit = 3
Output: 5
```

Example 2:

```text
symbols = ["x","y","x","z","x","y","x"]
prices  = [10,50,14,8,18,47,13]
limit = 4
Output: 4
```

The key constraint is `n = 2e5`: any solution that rescans a window or recomputes per-symbol min/max repeatedly will time out.

## 🪜 How to Solve This
1. Read the validity rule carefully → it is not one global max/min over the whole window. Each symbol has its own constraint.
2. That immediately suggests grouping state by symbol → use a `HashMap<symbol, state>`.
3. We need the **longest contiguous** valid region → classic sliding window / two pointers.
4. When the right pointer expands the window, only one symbol changes: `symbols[right]`. So only that symbol’s min/max can newly violate the rule.
5. We therefore need per-symbol structures that support:
   - append a new trade,
   - remove expired trades from the left,
   - query current min and max quickly.
6. A monotonic min deque and monotonic max deque per symbol give exactly that in amortized O(1).
7. If adding the new event makes that symbol invalid, shrink from the left until it becomes valid again. Other symbols remain valid because they were valid before, and shrinking cannot increase a symbol’s range.
8. Track the best window length after each expansion.

That chain leads naturally to: **sliding window + hash map of per-symbol monotonic queues**.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers pattern.**  
   Maintain a window `[left, right]` over the event stream. Expand `right` one event at a time. This is the correct abstraction because we need the longest contiguous segment under a monotone validity condition: shrinking the window never makes any symbol’s range worse.

2. **Store per-symbol window state.**  
   For each symbol, maintain two deques of `(index, price)`:
   - a min deque with increasing prices,
   - a max deque with decreasing prices.  
   These let us query that symbol’s current minimum and maximum in O(1).

3. **Insert the new event at `right`.**  
   Let `s = symbols[right]`, `p = prices[right]`. Push `(right, p)` into `s`’s min/max deques, popping dominated tail elements first. This preserves the monotonic invariant.

4. **Check only the updated symbol for violation.**  
   If `maxDeque[s].front.price - minDeque[s].front.price > limit`, the window is invalid. No other symbol can have become invalid, because only `s` changed.

5. **Shrink from the left until valid again.**  
   Repeatedly advance `left`. For the symbol leaving the window, remove front entries from its deques if their stored index equals the old `left`. Shrinking may reduce ranges; it cannot increase them. Stop once symbol `s` is valid again.

6. **Update the answer.**  
   After restoration, `[left, right]` is valid for every symbol. Record `max(answer, right - left + 1)`.

The core invariant is: after each iteration, every symbol’s deques contain exactly the active window entries for that symbol, and each symbol’s front elements are its current min and max.

## 📊 Worked Example
Example: `symbols = ["x","y","x","z","x","y","x"]`, `prices = [10,50,14,8,18,47,13]`, `limit = 4`

| right | event   | left after fix | active window | x range | y range | z range | best |
|------:|---------|----------------|---------------|---------|---------|---------|------|
| 0 | (x,10) | 0 | [0..0] | 0 | - | - | 1 |
| 1 | (y,50) | 0 | [0..1] | 0 | 0 | - | 2 |
| 2 | (x,14) | 0 | [0..2] | 4 | 0 | - | 3 |
| 3 | (z,8)  | 0 | [0..3] | 4 | 0 | 0 | 4 |
| 4 | (x,18) | 3 | [3..4] | 0 | - | 0 | 4 |
| 5 | (y,47) | 3 | [3..5] | 0 | 0 | 0 | 4 |
| 6 | (x,13) | 3 | [3..6] | 5 | 0 | 0 | 4 |

At `right = 4`, symbol `x` has prices `[10,14,18]`, range `8`, so we shrink left past indices `0`, `1`, and `2`. After index `2` leaves, only `x=18` remains, restoring validity. The maximum valid length seen is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` amortized. Each event is inserted once into its symbol’s min/max deques and removed at most once from each. The dominant work is deque push/pop plus hash map lookup, all amortized constant time. This remains practical at `10^6` events; anything with per-window rescans becomes non-viable long before `10^9`.

### Space Complexity
`O(n)` in the worst case. The space is owned by the per-symbol deques and hash map entries for events still represented in the active window. You can’t reduce this asymptotically without giving up O(1) min/max queries, which would force slower re-evaluation during shrink.

## 💡 Key Takeaways
• If the problem asks for the longest contiguous region under a constraint that only gets easier when you remove elements, start with sliding window / two pointers.  
• If validity is tracked independently per key inside one shared window, think `HashMap<key, state>` rather than one global structure.  
• Store indices in the monotonic deques, not just prices; expiry is by window position, not by value.  
• When the window becomes invalid after adding `right`, only the just-updated symbol needs to be checked in the shrink loop.  
• In production streams, keyed monotonic state is the difference between online enforcement with bounded latency and repeated recomputation that fails under high-cardinality traffic.

## 🚀 Variations & Further Practice
- Require the window to satisfy both per-symbol volatility caps and a global cross-symbol cap; now you need keyed monotonic queues plus a global min/max structure.
- Replace `max - min <= limit` with median- or percentile-based constraints per symbol; monotonic queues no longer suffice, pushing you toward indexed multisets or order-statistics trees.
- Make the stream append-only and ask for answers after each event in real time with symbol eviction policies; this adds lifecycle management and memory-pressure trade-offs on top of the same core pattern.