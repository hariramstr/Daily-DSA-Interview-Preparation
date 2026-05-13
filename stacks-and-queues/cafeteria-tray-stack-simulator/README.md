# Cafeteria Tray Stack Simulator

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Stacks and Queues &nbsp;|&nbsp; **Tags:** Stack, Simulation, Array

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine a spring-loaded tray dispenser at a cafeteria — the kind where you push trays down onto a spring and the top tray pops up when you grab it. Trays arrive in a fixed order and get stacked one by one. The question is simple: given a specific order in which customers want their trays, can the dispenser actually serve them in that exact sequence?

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This type of problem mirrors how computers manage tasks, memory, and workflows every single day. Operating systems use stacks to track which program is running and in what order. Call centres use similar logic to route customer requests through queues. Warehouse management systems rely on stack-like storage to determine whether goods can be retrieved in a specific order without reorganising the entire shelf. Getting this logic right means fewer bottlenecks, lower operational costs, and faster service — whether you are shipping packages or processing thousands of web requests per second.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a stack of cafeteria trays numbered 1 to 5, loaded from the bottom up. You can only ever grab the tray sitting on top. A line of customers each want a specific numbered tray in a specific order. Your job is to figure out whether you can load and unload trays in just the right sequence to satisfy every customer — without ever skipping ahead or rearranging the pile.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an integer `n` and an array `requests` (a permutation of integers `1` to `n`), simulate a stack where trays numbered `1` through `n` are pushed in ascending order. At each step you may either push the next tray onto the stack or pop the top tray to fulfil the next customer request. Determine whether the entire `requests` sequence can be satisfied using only these two operations.

**Constraints:**
- `1 <= n <= 1000`
- `requests` is a permutation of integers `1` to `n`
- Every tray must be used exactly once

**Examples:**

| Input | Output | Reason |
|---|---|---|
| `n=3, requests=[2,1,3]` | `true` | Push 1→Push 2→Pop 2→Pop 1→Push 3→Pop 3 |
| `n=3, requests=[3,1,2]` | `false` | After popping 3, tray 2 is on top but customer needs tray 1 |

---

## 🧩 Approach: How We Solve It *(For Developers)*

The algorithm simulates the push/pop process directly, using a pointer to track which tray arrives next and another pointer to track which customer request we are currently trying to fulfil.

1. **Initialise variables.** Create an empty stack, set `next_tray = 1` (the next tray to push), and set `req_index = 0` (the current customer request we need to serve).

2. **Iterate through each customer request.** For each value in `requests`, we need to get that specific tray to the top of the stack.

3. **Push trays until the needed tray is on top.** While the stack is empty or the top of the stack does not match the current request, push the next arriving tray. This is valid because trays must arrive in order — we cannot skip one.

4. **Check if pushing is still possible.** If `next_tray` exceeds `n` and the top still does not match the request, the sequence is impossible — return `false`.

5. **Pop the matching tray.** Once the top of the stack matches the current request, pop it to serve the customer and advance to the next request.

6. **Repeat until all requests are fulfilled.** If every customer is served successfully, return `true`.

This greedy simulation works because there is only one valid decision at each moment: keep pushing until you can serve the next customer, or declare it impossible.

---

## 📊 Worked Example *(For Developers)*

**Input:** `n = 3, requests = [2, 1, 3]`

| Step | Action | Stack State | next_tray | req_index | Notes |
|------|--------|-------------|-----------|-----------|-------|
| 1 | Push 1 | [1] | 2 | 0 | Top (1) ≠ request (2), keep pushing |
| 2 | Push 2 | [1, 2] | 3 | 0 | Top (2) = request (2) ✓ |
| 3 | Pop 2 | [1] | 3 | 1 | Served customer 1; next request is 1 |
| 4 | Pop 1 | [] | 3 | 2 | Top (1) = request (1) ✓; served customer 2 |
| 5 | Push 3 | [3] | 4 | 2 | Stack empty; push next tray |
| 6 | Pop 3 | [] | 4 | 3 | Top (3) = request (3) ✓; served customer 3 |

**Result:** All requests fulfilled → `true`

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n)** — Each tray is pushed onto the stack exactly once and popped exactly once, giving at most `2n` operations total. Even as `n` grows to thousands or millions, the algorithm scales linearly, meaning doubling the input only doubles the time — highly efficient.

### Space Complexity

**O(n)** — In the worst case (e.g., requests in reverse order), all `n` trays are pushed onto the stack before any are popped. This means extra memory usage grows proportionally with the number of trays, which is acceptable for practical input sizes.

---

## 💡 Key Takeaways *(For Everyone)*

- **Order constraints are everywhere in business.** Whether it is warehouse retrieval, task scheduling, or customer service queues, knowing whether a specific sequence is achievable saves time and prevents costly mistakes.
- **Not every order is possible with a stack.** Understanding this limitation helps system designers choose the right data structure — sometimes a queue or priority queue is needed instead.
- **A greedy simulation is the right tool here.** When there is only one logical action at each step, simulate it directly rather than exploring all possibilities.
- **The "impossible" check is critical.** Always verify that you have not run out of items to push before concluding a sequence is achievable — missing this causes incorrect results.
- **Push and pop counts are symmetric.** Every item pushed must be popped exactly once; tracking this invariant is a useful debugging and verification technique for any stack-based problem.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Two Stacks:** Can you serve the same `requests` sequence if you have two stacks available instead of one? How does this change which sequences become possible?
- **Variation 2 — Count Operations:** Instead of returning `true`/`false`, return the minimum total number of push and pop operations required to serve a valid sequence, or `-1` if impossible.
- **Variation 3 — Streaming Requests:** Adapt the solution to handle requests arriving as a real-time stream (one at a time) rather than as a pre-known array, and decide on each arrival whether the sequence is still achievable.

---