# Zigzag Array Reconstruction

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, In-place

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine arranging a row of numbers so they alternate between "valleys" and "peaks" — low, high, low, high. This problem asks us to rearrange a list of numbers into exactly that wave-like pattern, where every other number dips down and every number in between rises up. There are many valid arrangements, and we just need to find one that works.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Zigzag-style ordering appears in more places than you might expect. Music streaming platforms like Spotify use alternating ranking strategies to balance popular and discovery tracks in a playlist, keeping listeners engaged without overwhelming them with either hits or unknowns. Retail merchandising uses similar logic to alternate high-margin and high-volume products on shelves, maximising both revenue and foot traffic. In financial dashboards, alternating high and low data points are deliberately arranged to make trend anomalies visually obvious, helping analysts spot problems faster and reducing the cost of missed signals.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a row of stepping stones across a stream. The rule is simple: every other stone must be a "low" stone (shorter than its neighbours), and the stones in between must be "high" stones (taller than their neighbours). You're handed a pile of stones in random order and must place them — without sorting the pile first — so that the low-high-low-high stepping pattern holds all the way across.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an integer array `nums`, rearrange its elements **in-place** so the result satisfies the zigzag condition:

```
nums[0] <= nums[1] >= nums[2] <= nums[3] >= nums[4] ...
```

Elements at **even indices** must be less than or equal to their neighbours; elements at **odd indices** must be greater than or equal to their neighbours. You **may not sort** the array first. The rearrangement must be done in a **single pass** using adjacent swaps only. Return any valid arrangement.

**Constraints:**
- `1 <= nums.length <= 10^4`
- `0 <= nums[i] <= 10^5`
- A valid zigzag arrangement is always guaranteed to exist.

**Examples:**

| Input | Output | Verification |
|---|---|---|
| `[4, 3, 7, 8, 6, 2, 1]` | `[3, 7, 4, 8, 2, 6, 1]` | `3 <= 7 >= 4 <= 8 >= 2 <= 6 >= 1` ✅ |
| `[1, 2, 3]` | `[1, 3, 2]` | `1 <= 3 >= 2` ✅ |

---

## 🧩 Approach: How We Solve It *(For Developers)*

The key insight is that we don't need a globally sorted view — we only need each adjacent pair to satisfy a **local** condition. This makes a single greedy pass sufficient.

1. **Iterate through every index** from `0` to `n - 2`. We check each adjacent pair `(i, i+1)` exactly once, moving left to right.

2. **Determine the expected relationship** for the current index `i`:
   - If `i` is **even**, we need `nums[i] <= nums[i+1]` (a valley at `i`).
   - If `i` is **odd**, we need `nums[i] >= nums[i+1]` (a peak at `i`).

3. **Check whether the condition is already satisfied.** If it is, do nothing and move on — no unnecessary work.

4. **If the condition is violated, swap `nums[i]` and `nums[i+1]`.** This is the greedy step. Swapping adjacent elements to fix a local violation is always safe because:
   - The swap cannot break the condition we fixed at the previous step (the previous pair's relationship is unaffected by the right element changing).
   - The problem guarantees a valid arrangement always exists, so local fixes accumulate into a globally valid result.

5. **Continue until all pairs are processed.** After one full pass, every adjacent pair satisfies the zigzag rule, and the array is correctly rearranged.

No sorting, no extra data structures — just one pass and a conditional swap.

---

## 📊 Worked Example *(For Developers)*

**Input:** `nums = [4, 3, 7, 8, 6, 2, 1]`

| Step | Index `i` | Parity | Condition Needed | Current Pair | Swap? | Array After Step |
|------|-----------|--------|-----------------|--------------|-------|-----------------|
| 1 | 0 | Even | `[0] <= [1]` | `4, 3` → 4 ≤ 3? ❌ | Yes | `[3, 4, 7, 8, 6, 2, 1]` |
| 2 | 1 | Odd | `[1] >= [2]` | `4, 7` → 4 ≥ 7? ❌ | Yes | `[3, 7, 4, 8, 6, 2, 1]` |
| 3 | 2 | Even | `[2] <= [3]` | `4, 8` → 4 ≤ 8? ✅ | No | `[3, 7, 4, 8, 6, 2, 1]` |
| 4 | 3 | Odd | `[3] >= [4]` | `8, 6` → 8 ≥ 6? ✅ | No | `[3, 7, 4, 8, 6, 2, 1]` |
| 5 | 4 | Even | `[4] <= [5]` | `6, 2` → 6 ≤ 2? ❌ | Yes | `[3, 7, 4, 8, 2, 6, 1]` |
| 6 | 5 | Odd | `[5] >= [6]` | `6, 1` → 6 ≥ 1? ✅ | No | `[3, 7, 4, 8, 2, 6, 1]` |

**Final Output:** `[3, 7, 4, 8, 2, 6, 1]` → `3 <= 7 >= 4 <= 8 >= 2 <= 6 >= 1` ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n)** — We make exactly one pass through the array, visiting each adjacent pair once. Doubling the size of the input doubles the work linearly. For an array of 10,000 elements, this completes in microseconds on modern hardware, making it suitable for real-time applications.

### Space Complexity

**O(1)** — The algorithm operates entirely in-place. Swaps use only a temporary variable regardless of input size. No additional arrays, buffers, or recursive call stacks are needed, making memory usage constant and predictable.

---

## 💡 Key Takeaways *(For Everyone)*

- **Local rules can produce global order** — enforcing a simple neighbour-by-neighbour rule in one pass is enough to satisfy a pattern across the entire dataset, no big-picture sorting required.
- **In-place algorithms save money at scale** — when processing millions of records, eliminating extra memory allocation reduces infrastructure costs and improves throughput significantly.
- **Greedy algorithms work when local optimality guarantees global correctness** — the swap-if-violated strategy succeeds here because fixing each pair never undoes a previously fixed pair.
- **Even/odd index parity is a powerful control mechanism** — toggling behaviour based on index parity is a reusable pattern applicable to scheduling, interleaving, and load-balancing problems.
- **Always verify the "no-sort" constraint matters** — sorting would also solve this in O(n log n), but the single-pass O(n) greedy approach is meaningfully faster and is the intended insight to internalise.

---

## 🚀 Try It Yourself *(For Developers)*

- **Strict zigzag:** Modify the solution to enforce strict inequalities (`<` and `>` instead of `<=` and `>=`) and determine what input constraints would guarantee a valid arrangement still exists.
- **Peak-first variant:** Adapt the algorithm so the pattern starts with a peak instead of a valley (`nums[0] >= nums[1] <= nums[2] ...`) and verify it still works in a single pass.
- **Zigzag with duplicates stress test:** Generate large arrays (up to `10^4` elements) filled with repeated values and confirm your solution handles ties correctly without infinite loops or incorrect outputs.

---