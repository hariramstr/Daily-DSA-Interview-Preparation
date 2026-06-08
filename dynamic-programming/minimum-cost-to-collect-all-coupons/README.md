# Minimum Cost to Collect All Coupons

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Bitmask DP, Graph, Greedy

---

## 🗂 Problem Overview

Given `n` coupon types and a set of conditional discount bundles, find the minimum cost to acquire all coupons. A bundle `[a, b, discount]` lets you buy coupon `b` at reduced cost only if you already own `a`. The non-trivial constraint: the order of acquisition matters, and owning different subsets unlocks different discounts — making this an ordering-dependent subset optimization problem, not a simple greedy.

---

## 🌍 Engineering Impact

This pattern surfaces directly in dependency-aware cost optimization: cloud resource provisioning (reserved instance chaining), compiler pass ordering where earlier transformations unlock cheaper later ones, and feature-flag rollout sequencing where enabling feature A reduces the migration cost of feature B. In package managers, install-order-dependent pricing mirrors this exactly. Without bitmask DP, naive enumeration of acquisition orderings explodes combinatorially — this approach keeps state tractable up to ~20 nodes, covering most real dependency graphs in build systems and workflow engines.

---

## 🔍 Problem Statement

**Input:**
- `cost`: integer array of length `n`, where `cost[i]` is the base price of coupon `i`
- `bundles`: list of triples `[a, b, discount]` — owning `a` lets you buy `b` for `discount` instead of `cost[b]`

**Output:** Minimum total cost to own all `n` coupons.

**Constraints:** `1 ≤ n ≤ 20`, `1 ≤ cost[i] ≤ 1000`, no duplicate `(a, b)` pairs, `1 ≤ discount ≤ cost[b]`.

**Examples:**

| Input | Output |
|---|---|
| `cost=[5,3,4]`, `bundles=[[0,1,1],[0,2,2]]` | `8` |
| `cost=[10,6,7,4]`, `bundles=[[0,1,2],[1,2,3],[2,3,1]]` | `16` |

**Key driver:** `n ≤ 20` is the explicit signal — the search space is over subsets of owned coupons, and `2^20 ≈ 10^6` is the tractable ceiling that makes bitmask DP the intended approach.

---

## 🪜 How to Solve This

1. **Notice the dependency structure** → buying coupon `b` cheaply requires owning `a` first. This is an ordering constraint, not just a selection constraint. Greedy fails because buying a cheap coupon early might unlock a larger chain of discounts later.

2. **Ordering constraints + subset tracking → think bitmask DP.** The state we care about is: *which coupons do I currently own?* That's a subset of `{0..n-1}`, representable as an integer bitmask.

3. **Define `dp[mask]` = minimum cost to own exactly the coupons in `mask`.** Transition: to add coupon `i` to `mask`, look at all coupons already in `mask` — if any has a bundle to `i`, use the cheapest discount; otherwise pay base cost.

4. **Build up from smaller masks to larger ones.** For each mask, try adding each coupon not yet in it. The best price for the new coupon is determined by what's already owned — i.e., what bits are set in `mask`.

5. **Answer is `dp[(1<<n) - 1]`.** The reasoning is clean: every valid acquisition sequence corresponds to a path through this DP, and we're taking the minimum over all paths.

---

## 🧩 Algorithm Walkthrough

**Pattern: Bitmask DP over subset states**

This is the right abstraction because the "what discounts am I eligible for" question is entirely determined by the current owned set — a classic optimal substructure over subsets.

**Steps:**

1. **Preprocessing — build discount lookup:** For each coupon `b`, build a map `best_discount[a][b] = discount`. This lets you query in O(1): "if I own `a`, what's the cheapest way to get `b`?"

2. **Initialize DP array:** `dp[0] = 0` (own nothing, zero cost). All other states = `∞`.

3. **Iterate over all masks `0` to `2^n - 1`:** For each mask, if `dp[mask] == ∞`, skip. Otherwise, try adding each coupon `i` not in `mask`.

4. **Compute cost to add coupon `i` given `mask`:** Start with `cost[i]` (base price). For every coupon `j` already in `mask` (i.e., bit `j` is set), check if bundle `[j, i, discount]` exists. Track the minimum available price across all eligible bundles.

5. **Transition:** `dp[mask | (1 << i)] = min(dp[mask | (1 << i)], dp[mask] + best_price_for_i)`.

6. **Invariant maintained:** `dp[mask]` always holds the globally optimal cost to reach exactly that owned set, regardless of acquisition order.

7. **Return `dp[(1 << n) - 1]`.**

---

## 📊 Worked Example

**Input:** `cost = [10, 6, 7, 4]`, `bundles = [[0,1,2],[1,2,3],[2,3,1]]`

| Step | Current Mask (owned) | Adding Coupon | Price Paid | New Mask | dp[new mask] |
|---|---|---|---|---|---|
| 1 | `0000` (∅) | 0 | 10 (base) | `0001` | 10 |
| 2 | `0001` ({0}) | 1 | 2 (bundle 0→1) | `0011` | 12 |
| 3 | `0011` ({0,1}) | 2 | 3 (bundle 1→2) | `0111` | 15 |
| 4 | `0111` ({0,1,2}) | 3 | 1 (bundle 2→3) | `1111` | 16 |

**Result:** `dp[1111] = 16`. The chain of bundles produces a strictly better outcome than any partial use of discounts.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(2^n · n)** — we iterate over all `2^n` masks and for each, try adding up to `n` coupons. For each addition, scanning owned bits is O(n). At `n = 20` this is ~20 million operations — fast. At `n = 30` it becomes ~30 billion and is no longer viable without pruning.

### Space Complexity

**O(2^n)** for the DP table, plus O(n²) for the discount lookup map. At `n = 20`, the DP array holds ~1M integers (~4MB). Reducible only by meet-in-the-middle at significant implementation cost.

---

## 💡 Key Takeaways

- **Pattern signal #1:** Constraint `n ≤ 20` with subset-dependent state is the canonical bitmask DP fingerprint — see it, think `dp[mask]`.
- **Pattern signal #2:** When the *order* of acquiring items changes their cost and greedy fails to find a global optimum, the state must encode *what you own*, not just *what you've spent*.
- **Gotcha #1:** When computing the best price for coupon `i` given `mask`, you must scan all set bits in `mask` for eligible bundles — missing even one can produce a suboptimal transition that silently propagates through the DP.
- **Gotcha #2:** Skipping masks where `dp[mask] == ∞` is not just an optimization — propagating infinity produces incorrect results if your min-comparison isn't guarded.
- **Architectural insight:** Bitmask DP is the right tool whenever your system's "eligibility state" is a small set of boolean flags and transitions are set-monotone (you only add, never remove). This maps directly to feature-gating, permission accumulation, and staged rollout systems.

---

## 🚀 Variations & Further Practice

- **Multiple bundles per pair with stacking discounts:** If `(a, b)` can appear multiple times with different prerequisites (e.g., own both `a` and `c` for a deeper discount), the transition must evaluate power-set conditions per coupon — pushing complexity toward O(3^n) via subset-sum-over-subsets enumeration.
- **Coupon expiry / ordering windows:** Add a time dimension where coupons expire after `k` steps. Now state becomes `(mask, step)`, blowing the table to `O(2^n · k)` and requiring careful handling of unreachable states.
- **Related — Shortest Path in DAG with set-state (TSP variant):** The Travelling Salesman bitmask DP (`dp[mask][i]` = min cost to visit exactly `mask` ending at `i`) is the direct structural cousin — practice it to internalize the two-dimensional bitmask DP extension.