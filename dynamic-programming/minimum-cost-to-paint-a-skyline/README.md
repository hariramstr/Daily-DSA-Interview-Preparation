# Minimum Cost to Paint a Skyline

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Matrix DP, Graph Transitions

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you need to paint a row of buildings, each one a different color from its neighbor. But switching colors between buildings isn't free — there's an extra fee every time you change. This problem asks: given the cost to paint each building any color, and the fees for switching colors, what is the cheapest way to paint every building while following the rules?

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This type of problem appears everywhere in business operations. Airlines use similar logic to schedule crews across flights, minimizing the cost of repositioning staff between cities. Manufacturers use it to sequence production runs on assembly lines, where switching between product types incurs a costly retooling fee. Telecommunications companies apply it when routing signals across networks, balancing transmission costs with switching overhead. Solving this efficiently translates directly into measurable cost savings — sometimes millions of dollars annually — by finding the globally optimal sequence rather than making locally cheap decisions that prove expensive later.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a painter hired to decorate a street of houses. Each house must be a different color from the one next to it, and painting each house a particular color has its own price. On top of that, every time the painter switches to a new color, there's a setup fee that depends on which two colors are being swapped. The goal is to find the painting plan that keeps the total bill — paint plus setup fees — as low as possible.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given `n` buildings and `k` colors, you receive:
- A 2D matrix `cost[i][j]` — the cost to paint building `i` with color `j`.
- A 2D matrix `transition[a][b]` — the fee paid when painting a building color `b` immediately after painting the previous building color `a`. `transition[a][a] = 0` and no fee applies to the first building.

**Constraint:** No two adjacent buildings may share the same color.

**Goal:** Return the minimum total cost (painting + transition fees) to paint all buildings.

**Constraints:** `1 ≤ n ≤ 1000`, `1 ≤ k ≤ 20`, `1 ≤ cost[i][j] ≤ 10⁴`, `0 ≤ transition[a][b] ≤ 10⁴`.

**Example 1:**
- Input: `cost = [[1,3,2],[4,1,5],[3,2,1]]`, `transition = [[0,2,3],[2,0,1],[3,1,0]]`
- Output: `5`

**Example 2:**
- Input: `cost = [[5,8],[4,3]]`, `transition = [[0,1],[1,0]]`
- Output: `7`

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use **bottom-up Dynamic Programming**, building a table of minimum costs as we process each building left to right.

1. **Define the state.** Let `dp[i][j]` = the minimum total cost to paint buildings `0` through `i`, where building `i` is painted color `j`. This captures everything we need to make future decisions.

2. **Initialize the base case.** For the first building (`i = 0`), there is no previous building and no transition fee. So `dp[0][j] = cost[0][j]` for every color `j`.

3. **Fill the table iteratively.** For each subsequent building `i` (from `1` to `n-1`) and each candidate color `j`, we consider every possible previous color `p` (where `p ≠ j`). The cost of this choice is:
   ```
   dp[i][j] = cost[i][j] + min over all p≠j of (dp[i-1][p] + transition[p][j])
   ```
   We try all `k` previous colors and take the cheapest valid option. This ensures the no-adjacent-same-color rule is enforced.

4. **Why DP and not greedy?** A greedy approach (always pick the cheapest color for the current building) can lead to expensive transitions later. DP evaluates all paths simultaneously, guaranteeing the global optimum.

5. **Extract the answer.** After filling the table, the answer is `min(dp[n-1][j])` over all colors `j` — the cheapest way to finish the last building in any color.

---

## 📊 Worked Example *(For Developers)*

Using **Example 1**: `cost = [[1,3,2],[4,1,5],[3,2,1]]`, `transition = [[0,2,3],[2,0,1],[3,1,0]]`

**Step 1 — Initialize (Building 0):**

| Color | dp[0][j] |
|-------|----------|
| 0     | 1        |
| 1     | 3        |
| 2     | 2        |

**Step 2 — Building 1** (`cost[1] = [4,1,5]`):

| New Color j | Best previous p (p≠j) | Calculation                        | dp[1][j] |
|-------------|----------------------|------------------------------------|----------|
| 0           | p=2: 2+3=5 → +4=9   | min(1+2, 3+2, 2+3)+4 = 5+4        | 9        |
| 1           | p=0: 1+2=3 → +1=4   | min(1+2, 2+1)+1 = 3+1             | 4        |
| 2           | p=1: 3+1=4 → +5=9   | min(1+3, 3+1)+5 = 4+5             | 9        |

**Step 3 — Building 2** (`cost[2] = [3,2,1]`):

| New Color j | Best previous p | Calculation          | dp[2][j] |
|-------------|----------------|----------------------|----------|
| 0           | p=1: 4+2=6→+3  | 6+3                  | 9        |
| 1           | p=2: 9+1=10→+2 | min(9+2,9+1)+2=10+2  | 12       |
| 2           | p=1: 4+1=5→+1  | 5+1                  | **5**    |

**Answer:** `min(9, 12, 5) = 5` ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n × k²)** — For each of the `n` buildings, we evaluate `k` color choices, and for each choice we scan `k` previous colors. With `n = 1000` and `k = 20`, this is just 400,000 operations — fast enough to run in milliseconds even on modest hardware.

### Space Complexity

**O(n × k)** for the full DP table, reducible to **O(k)** by keeping only the previous row. In practice, storing only two rows of size `k` means the memory footprint is negligible regardless of how many buildings there are.

---

## 💡 Key Takeaways *(For Everyone)*

- **Switching costs are everywhere in business** — from airline crew scheduling to manufacturing changeovers; this algorithm finds the globally cheapest sequence, not just locally cheap steps.
- **Greedy thinking fails here** — choosing the cheapest option at each step can trap you into expensive transitions later; only a full look-ahead approach guarantees the best result.
- **Dynamic Programming works by reusing subproblem results** — instead of re-evaluating every possible painting sequence (which would be astronomically large), we build the answer incrementally one building at a time.
- **State design is the critical skill** — defining `dp[i][j]` as "minimum cost to reach building `i` painted color `j`" is what makes the recurrence clean and correct.
- **Space optimization is a practical DP technique** — since each building only depends on the previous one, you can discard older rows and keep memory usage constant, which matters at production scale.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Classic Paint House (LeetCode #256 & #265):** Solve the same problem without transition fees between colors. This is a simpler warm-up that isolates the core DP pattern before adding the transition matrix complexity.
- **Variation 2 — Circular Skyline:** Modify the problem so that the last building is also adjacent to the first, forming a circle. How does this change the initialization and final answer extraction?
- **Variation 3 — Maximum Color Runs:** Instead of minimizing cost, maximize the number of buildings painted with the same color as their neighbor (relaxing the adjacency constraint to a soft penalty). How does the recurrence change when the constraint becomes a cost rather than a hard rule?

---