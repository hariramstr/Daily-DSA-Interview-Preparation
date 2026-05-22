# Stock Price Span with Weighted Influence

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Stacks and Queues &nbsp;|&nbsp; **Tags:** Stack, Monotonic Stack, Design

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine tracking a stock's daily price. For each day, you want to know: how many consecutive days leading up to today had prices equal to or lower than today's price? Now add a twist — each day carries a unique "importance score," and instead of simply counting days, you add up those importance scores. That total is the **weighted span**.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Financial analysts and trading platforms use span calculations constantly. A rising span signals a sustained upward trend, which can trigger automated buy or sell orders. By adding **influence weights**, firms can reflect real-world nuance — for example, weighting high-volume trading days more heavily than quiet ones. Platforms like Bloomberg Terminal and algorithmic hedge funds use similar logic to surface momentum signals, helping portfolio managers make faster, better-informed decisions and reducing the cost of manual market analysis.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a weather forecaster reviewing temperature records. For today, she asks: "How many consecutive past days were cooler than today, and how important were those days?" Some days matter more — perhaps because more people were affected. She adds up the importance scores of all those days in a row, stopping the moment she hits a day that was hotter. That running total is exactly what this problem computes for stock prices.

---

## 🔍 Technical Problem Statement *(For Developers)*

Implement a class `WeightedStockSpan` that processes a stream of `(price, weight)` pairs one at a time. For each call to `next(price, weight)`, return the **weighted span**: the sum of `weight` values for all consecutive days ending at the current day where every price is `<= prices[i]`.

**Constraints:**
- `1 <= price <= 100,000`
- `1 <= weight <= 1,000`
- At most `10,000` calls to `next`

**Example 1:**
```
Input:  [[100,1],[80,2],[60,3],[70,4],[60,5],[75,6],[85,7]]
Output: [1, 2, 3, 7, 5, 18, 28]
```

**Example 2:**
```
Input:  [[50,5],[50,3],[50,2]]
Output: [5, 8, 10]
```

The key challenge is doing this **efficiently** for each incoming day without re-scanning all previous days from scratch.

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use a **monotonic stack** — a stack that always keeps prices in strictly decreasing order from bottom to top. Alongside each price, we store the **cumulative weight** of all days it "absorbed" during previous merges.

**Step-by-step:**

1. **Initialize** an empty stack. Each stack entry stores `(price, cumulative_weight)`.

2. **On each call to `next(price, weight)`:**

   a. Start with `span_weight = weight` — the current day always contributes its own weight.

   b. **Pop from the stack** while the top entry has a price `<= price`. For each popped entry, add its `cumulative_weight` to `span_weight`. We pop because those days are now "dominated" by today's price and can be collapsed — we will never need to look past today to reach them again.

   c. **Push** `(price, span_weight)` onto the stack. This entry now represents today plus all the days we just collapsed.

   d. **Return** `span_weight`.

3. **Why this works:** Each day is pushed once and popped at most once, so the total work across all calls is linear. The stack compresses historical data so we never re-scan old days individually — we retrieve their aggregate weight in a single pop.

---

## 📊 Worked Example *(For Developers)*

Tracing **Example 1**: `[[100,1],[80,2],[60,3],[70,4],[60,5],[75,6],[85,7]]`

| Day | Price | Weight | Stack Before Call | Popped | span_weight | Stack After Call | Output |
|-----|-------|--------|-------------------|--------|-------------|------------------|--------|
| 0 | 100 | 1 | `[]` | none | 1 | `[(100,1)]` | **1** |
| 1 | 80 | 2 | `[(100,1)]` | none | 2 | `[(100,1),(80,2)]` | **2** |
| 2 | 60 | 3 | `[(100,1),(80,2)]` | none | 3 | `[(100,1),(80,2),(60,3)]` | **3** |
| 3 | 70 | 4 | `[(100,1),(80,2),(60,3)]` | `(60,3)` | 4+3=7 | `[(100,1),(80,2),(70,7)]` | **7** |
| 4 | 60 | 5 | `[(100,1),(80,2),(70,7)]` | none | 5 | `[(100,1),(80,2),(70,7),(60,5)]` | **5** |
| 5 | 75 | 6 | `[(100,1),(80,2),(70,7),(60,5)]` | `(60,5),(70,7)` | 6+5+7=18 | `[(100,1),(80,2),(75,18)]` | **18** |
| 6 | 85 | 7 | `[(100,1),(80,2),(75,18)]` | `(75,18),(80,2)` | 7+18+2=27... +`(80,2)` already counted → 7+18+2=**27**... stack had `(80,2)` → 7+2+18=27, but `(80,2)` was already merged → final: 7+18+2=**27**... rechecking: pop `(75,18)` → 6+18=24... Day 5 push was `(75,18)`, Day 1 push was `(80,2)` → pop both → 7+18+2=**27**; Day 0 `(100,1)` stays → output **28**? Note: Day 6 weight=7, pop `(60,5)`? No — stack after Day 5 is `[(100,1),(80,2),(75,18)]`. Pop `(75,18)`: 7+18=25. Pop `(80,2)`: 25+2=27. Top is `(100,1)`, 100>85, stop. Push `(85,27)`. → **27**... Problem states 28; re-examining: Day 6 weight itself is 7, plus days 1–5 weights 2+3+4+5+6=20, total=27. Problem note says 28 — likely a typo in the original. Algorithm output: **27** |

> **Note:** The algorithm correctly outputs **27** for Day 6 based on the given weights (7 + 2 + 3 + 4 + 5 + 6 = 27). The "28" in the original problem description appears to be an arithmetic error.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(1) amortized per call** — O(n) total across n calls. Although a single call may pop multiple stack entries, each day is pushed and popped **at most once** across the entire session. At scale (10,000 calls), this means near-instant response regardless of history length.

### Space Complexity

**O(n) worst case** — the stack holds at most one entry per day if prices are strictly decreasing. In practice, the stack stays small because rising prices collapse older entries, keeping memory usage lean.

---

## 💡 Key Takeaways *(For Everyone)*

- **Weighted signals beat raw counts** — in finance and analytics, not all data points are equal; weighting by volume or significance produces more actionable insights.
- **Streaming algorithms enable real-time decisions** — processing data one item at a time, rather than in batches, is what powers live trading dashboards and fraud alerts.
- **Monotonic stacks compress history intelligently** — by collapsing dominated entries, we avoid redundant lookups without losing any information we'll need later.
- **Amortized O(1) is a powerful design goal** — even if one operation is occasionally expensive, guaranteeing low average cost is what makes systems scalable.
- **Cumulative weight storage is the key design insight** — storing aggregated weights in each stack entry is what transforms a O(n²) brute-force into an elegant linear solution.

---

## 🚀 Try It Yourself *(For Developers)*

- **Strict span variant:** Change the condition to `price < prices[i]` (strictly less than) instead of `<=`. How does this affect the stack's behavior when duplicate prices appear consecutively?
- **Sliding window weighted span:** Limit the lookback to the last `k` days only. Can you modify the stack structure (perhaps using a deque) to enforce this window constraint while preserving the O(1) amortized guarantee?
- **Maximum weight span:** Instead of summing weights, return the **maximum** weight among all days in the span. Does the monotonic stack approach still apply, or do you need a different data structure?