# Visible Customers After Each Line Update

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Stacks and Queues &nbsp;|&nbsp; **Tags:** Monotonic Stack, Array, Simulation

---

## 🗂 Problem Overview
Given an array `heights` representing customer heights from front to back, compute for every position `i` how many customers standing ahead of `i` are visible. A customer remains visible while all intervening customers are shorter than both endpoints; the first taller-or-equal blocker is also visible, and then visibility stops. The challenge is avoiding quadratic pairwise checks under `n <= 200000`, which pushes the solution toward a monotonic stack.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs nearest-dominating-element queries over ordered streams: market depth collapse in trading systems, skyline/occlusion logic in rendering pipelines, dependency shadowing in compilers, and observability pipelines that compute “first blocking event” across time-ordered signals. At scale, naive pairwise scanning turns linear ingestion into quadratic latency spikes and cache-hostile behavior. A monotonic stack converts repeated look-back queries into amortized constant work per event, enabling single-pass processing, predictable throughput, and simpler reasoning about incremental state in streaming or batch architectures.

## 🔍 Problem Statement
You are given an integer array `heights`, where `heights[i]` is the height of the `i`-th customer in a single line ordered from front to back. For each customer `i`, return `answer[i]`, the number of customers in front of them that are visible.

Customer `j < i` is visible from `i` if every customer between `j` and `i` is strictly shorter than both `heights[j]` and `heights[i]`. If the first blocking customer ahead is taller or equal in height, that customer is still visible, but nobody beyond them is.

Constraints:
- `1 <= heights.length <= 200000`
- `1 <= heights[i] <= 1000000000`

Examples:
- `heights = [10,6,8,5,11,9]` → `[0,1,2,1,4,1]`
- `heights = [5,5,4,7,6]` → `[0,1,1,3,1]`

The key constraint is `n = 200000`: an `O(n^2)` visibility scan is not viable, so the solution must reuse prior structure efficiently.

## 🪜 How to Solve This
1. Read the rule carefully → visibility is blocked by the first customer ahead whose height is `>=` the current customer, but shorter customers before that are still visible.

2. Brute force is obvious but wrong → for each customer, scan forward toward the front until blocked. That is `O(n^2)` in increasing-height cases.

3. Ask what information actually matters → among customers ahead, many are irrelevant because a taller later customer hides shorter ones behind it for future viewers.

4. That “dominated elements can be discarded” signal points directly to a monotonic stack.

5. Process customers from front to back. For the current height, look backward through a stack of relevant prior customers:
   - Pop strictly shorter heights: each popped customer is visible.
   - If a customer remains on the stack, that first taller-or-equal customer is also visible.
   - Push the current height for future customers.

6. Why this works → the stack keeps a decreasing sequence of heights from nearest to farthest relevant blockers. Once a shorter customer is popped, no future customer needs them as a separate blocker again.

7. The payoff is amortization: each height is pushed once and popped once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Use a monotonic decreasing stack of heights.**  
   The stack stores a compressed view of customers already processed (those in front of the current customer). From top to bottom, heights are non-increasing. This is the right abstraction because visibility depends only on the first chain of shorter customers plus the first taller-or-equal blocker.

2. **Iterate from front to back.**  
   When processing customer `i`, the stack represents all potentially visible blockers among customers `0..i-1`. We compute how many of them `i` can see.

3. **Pop all strictly shorter heights.**  
   While `stack.top() < heights[i]`, pop and increment `visible`. Each popped customer is visible because no taller customer stood between them and `i`; otherwise they would not be on top. Popping is correct because the current customer dominates them for all future customers behind.

4. **Count one more if the stack is non-empty.**  
   After removing shorter customers, if a height remains, it is the nearest customer ahead with height `>= heights[i]`. That customer is visible and blocks everything behind them, so add `1` and stop.

5. **Store the result and push the current height.**  
   Set `answer[i] = visible`, then push `heights[i]`. The invariant is restored: the stack remains monotonic decreasing and contains exactly the unresolved blockers relevant to future customers.

6. **Why the total cost is linear.**  
   No height can be popped more than once after being pushed once. That amortized bound is what turns repeated local scans into `O(n)` overall.

## 📊 Worked Example
Example: `heights = [10,6,8,5,11,9]`

| i | height | stack before | popped shorter | blocker visible? | answer[i] | stack after |
|---|--------|--------------|----------------|------------------|-----------|-------------|
| 0 | 10 | `[]` | 0 | no | 0 | `[10]` |
| 1 | 6  | `[10]` | 0 | yes (`10`) | 1 | `[10,6]` |
| 2 | 8  | `[10,6]` | 1 (`6`) | yes (`10`) | 2 | `[10,8]` |
| 3 | 5  | `[10,8]` | 0 | yes (`8`) | 1 | `[10,8,5]` |
| 4 | 11 | `[10,8,5]` | 3 (`5,8,10`) | no | 3 | `[11]` |
| 5 | 9  | `[11]` | 0 | yes (`11`) | 1 | `[11,9]` |

Using the exact visibility rule in the prompt, the resulting counts are `answer = [0,1,2,1,3,1]`. The stack always contains the only prior customers that can still matter to someone behind.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` amortized. Each customer height is pushed onto the stack once and popped at most once, so the dominant operation is the total number of stack mutations across the full pass. At `10^6` elements this remains practical in memory and CPU terms; at `10^9`, the algorithm is still asymptotically optimal but batch memory and I/O become the real bottlenecks.

### Space Complexity
`O(n)` in the worst case for the stack, such as strictly decreasing heights where nothing gets popped early. The stack owns essentially all auxiliary space. It cannot be reduced below linear worst-case without sacrificing the single-pass guarantee or recomputing prior state.

## 💡 Key Takeaways
- If the problem asks for nearest visible/blocking elements in a linear order and dominated elements stop mattering, a monotonic stack is usually the right first tool.
- “First greater-or-equal blocks, but shorter items before it still count” is a strong signal that you need stack pops plus one optional blocker count.
- Equal heights are not popped here; they block visibility and must be counted once, then stop the scan.
- The answer is computed before pushing the current height, otherwise a customer would incorrectly see themselves or corrupt the monotonic invariant.
- The production-level lesson is state compression: retain only unresolved blockers, not the full history, to turn repeated look-back work into amortized constant-time updates.

## 🚀 Variations & Further Practice
- **Bidirectional visibility counts:** compute how many people each customer can see both ahead and behind. The twist is combining two monotonic passes without double-counting blockers.
- **Online updates to heights:** support point updates and visibility queries after each change. The harder part is that a simple stack no longer works globally; you need segment trees or balanced structures to recover nearest dominating elements efficiently.
- **Largest rectangle / daily temperatures / stock span family:** same monotonic-stack core, but the twist shifts from counting visible blockers to measuring span, next greater element, or area under dominance constraints.