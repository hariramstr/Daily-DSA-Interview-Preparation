# Minimum Unique Colors in Every Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Array

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have a long row of colored paint buckets and a fixed-size frame you slide along the row, one bucket at a time. At each position, you count how many different colors are visible through the frame. This problem asks: what is the fewest number of different colors you ever see in that frame, and where does that happen first?

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This technique mirrors real-world problems in data analysis and operations. Retailers use it to find the time window with the least product variety on shelves — a signal to restock. Cybersecurity teams scan network traffic in fixed time windows to find the quietest period of activity, which can indicate a lull before an attack. Streaming platforms like Spotify analyze listening windows to detect moments of lowest genre diversity, helping personalize recommendations. Getting this right efficiently — without slowing down as data grows — directly translates to faster insights, lower computing costs, and better customer experiences.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a museum guard holding a viewing card with three cut-out slots, sliding it across a row of colored paintings. At each position, the guard counts how many different colors are visible. Your job is to find the position where the guard sees the fewest distinct colors — and if there's a tie, report the earliest such position. Think of it as finding the most "repetitive" stretch in a sequence.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an integer array `colors` of length `n` (where `colors[i]` is a positive integer representing a color) and an integer `k`, compute the number of **distinct values** in every contiguous subarray (window) of size `k`. Return a pair `[minDistinct, startIndex]` where:

- `minDistinct` is the minimum distinct-color count across all windows.
- `startIndex` is the smallest starting index of a window achieving that minimum.

**Constraints:**
- `1 <= k <= n <= 100,000`
- `1 <= colors[i] <= 10^6`
- Solution must run in **O(n)** time.

**Examples:**

| Input | k | Output |
|---|---|---|
| `[1, 2, 1, 3, 2, 1, 1]` | 3 | `[2, 0]` |
| `[4, 4, 4, 1, 2, 3]` | 2 | `[1, 0]` |

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use a **sliding window** paired with a **hash map** to maintain the frequency of each color currently inside the window. This avoids reprocessing the entire window at every step.

1. **Initialize the first window:** Iterate over the first `k` elements, adding each color to a frequency map (`colorCount`). Count the number of distinct colors (keys with count > 0). This is our baseline.

2. **Record the initial best:** Set `minDistinct = distinct colors in first window` and `bestStart = 0`.

3. **Slide the window forward (index `i` from `k` to `n-1`):**
   - **Add the incoming element** (`colors[i]`): Increment its count in the map. If its count just became 1, it's a new distinct color — increment `distinctCount`.
   - **Remove the outgoing element** (`colors[i - k]`): Decrement its count. If its count drops to 0, it's no longer in the window — decrement `distinctCount`.

4. **Update the minimum:** After each slide, if `distinctCount < minDistinct`, update `minDistinct` and record the new `bestStart = i - k + 1`. We only update on strict improvement to preserve the smallest starting index automatically.

5. **Return** `[minDistinct, bestStart]`.

Each element enters and exits the window exactly once, keeping the algorithm linear.

---

## 📊 Worked Example *(For Developers)*

**Input:** `colors = [1, 2, 1, 3, 2, 1, 1]`, `k = 3`

| Step | Action | Window | colorCount | distinctCount | minDistinct | bestStart |
|---|---|---|---|---|---|---|
| Init | Build first window | `[1,2,1]` | `{1:2, 2:1}` | 2 | 2 | 0 |
| i=3 | Add 3, remove 1 | `[2,1,3]` | `{1:1, 2:1, 3:1}` | 3 | 2 | 0 |
| i=4 | Add 2, remove 2 | `[1,3,2]` | `{1:1, 3:1, 2:1}` | 3 | 2 | 0 |
| i=5 | Add 1, remove 1 | `[3,2,1]` | `{3:1, 2:1, 1:1}` | 3 | 2 | 0 |
| i=6 | Add 1, remove 3 | `[2,1,1]` | `{3:0, 2:1, 1:2}` | 2 | 2 | 0 |

No window beats 2 distinct colors, and the first occurrence is at index 0.
**Output:** `[2, 0]`

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n)** — Each of the `n` elements is added to the window exactly once and removed exactly once. Hash map insertions and lookups are O(1) on average. This means the algorithm scales linearly: doubling the input size doubles the runtime, not quadruples it — critical for large datasets.

### Space Complexity

**O(k)** — The hash map holds at most `k` entries at any time (one per element in the current window). In the worst case (all colors unique), this equals the window size. For most real inputs, it will be considerably smaller.

---

## 💡 Key Takeaways *(For Everyone)*

- **Business insight:** Efficiently finding the "least diverse" segment in a data stream is a pattern that appears in inventory management, fraud detection, and content recommendation — all problems where speed at scale has direct cost implications.
- **Business insight:** An O(n) solution means this analysis can run in real time on millions of records without expensive infrastructure — a meaningful competitive advantage.
- **Technical insight:** The sliding window pattern avoids redundant recomputation by incrementally updating state — only the entering and exiting elements are processed at each step.
- **Technical insight:** A frequency map (hash map) is the right tool when you need to track membership and count simultaneously; decrementing to zero is the clean way to signal "no longer present."
- **Technical insight:** To preserve the smallest starting index on ties, only update `bestStart` on **strict** improvement (`<`), never on equality (`<=`).

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Maximum diversity:** Instead of the minimum, find the window of size `k` with the **most** distinct colors and its starting index. How does the update condition change?
- **Variation 2 — Variable window size:** Given a target number of distinct colors `t`, find the **shortest** subarray that contains exactly `t` distinct colors. This transforms the fixed-window problem into a dynamic one requiring two pointers.
- **Variation 3 — All qualifying windows:** Return the starting indices of **all** windows that achieve the minimum distinct count, not just the first. Consider how output size affects your complexity analysis.

---