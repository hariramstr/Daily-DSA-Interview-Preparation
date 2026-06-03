# Simulate a Cafeteria Tray Stack

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Stacks and Queues &nbsp;|&nbsp; **Tags:** Stack, Simulation, Array

---

## 🗂 Problem Overview

Given a sequence of string operations, simulate a LIFO stack and return the results of all `pop` and `peek` operations in order. The input/output contract is straightforward: parse each operation, mutate or query the stack, and collect outputs only for observable operations. The non-trivial constraint is graceful handling of underflow — `pop` and `peek` on an empty stack must emit `-1` rather than raising an error, which forces explicit guard logic at every read path.

---

## 🌍 Engineering Impact

Stack-based simulation is the backbone of several production-critical patterns. Undo/redo systems in editors (VS Code, Figma) maintain operation stacks where underflow handling determines whether a no-op or a crash occurs. Compiler and interpreter symbol tables use nested scopes as stacks — popping a scope that doesn't exist is a fatal parse error. Browser history, call-stack unwinding in crash reporters, and backpressure buffers in streaming pipelines (Kafka consumer offset tracking) all rely on the same LIFO contract. Getting underflow semantics wrong at scale means silent data loss or cascading failures that are notoriously hard to reproduce.

---

## 🔍 Problem Statement

Simulate a spring-loaded tray dispenser (a stack) given a list of 1–1000 string operations:

- `"push X"` — push integer `X` onto the top; `-10^4 <= X <= 10^4`
- `"pop"` — remove and record the top element; record `-1` if empty
- `"peek"` — record the top element without removing it; record `-1` if empty

Return a list of integers containing results from `pop` and `peek` operations only, in the order encountered. `push` operations produce no output.

**Example 1:**
```
Input:  ["push 5", "push 3", "peek", "pop", "pop", "pop"]
Output: [3, 3, 5, -1]
```

**Example 2:**
```
Input:  ["pop", "push 10", "push 20", "peek", "pop"]
Output: [-1, 20, 20]
```

The constraint driving the algorithmic choice is the strict LIFO access order — no random access is ever needed, making a stack the only correct abstraction.

---

## 🪜 How to Solve This

1. **Read the problem** → every operation either writes to the stack or reads from the top. No scanning, no sorting, no cross-element comparison. This is pure state mutation with sequential queries.

2. **Access pattern is LIFO** → the only element ever touched is the top. That immediately signals a stack, not a queue, deque, or array with indexing.

3. **Output is selective** → only `pop` and `peek` contribute to the result list. `push` is a side-effect-only operation. Separating "mutate" from "observe" keeps the loop body clean.

4. **Underflow must be explicit** → rather than relying on language exceptions, check `len(stack) == 0` before every read. This makes the guard visible and testable, and mirrors how production systems handle resource exhaustion without crashing.

5. **Parsing is mechanical** → `split()` on each operation string gives the command and optional argument in one step. No regex needed.

The solution falls out naturally: one loop, one stack, one results list, two guard checks.

---

## 🧩 Algorithm Walkthrough

**Pattern: Stack Simulation**

A stack is the right abstraction because the problem's access semantics are purely LIFO — there is no scenario where an element other than the top is relevant. Using a plain dynamic array (Python list, Java ArrayList) gives O(1) amortized push/pop from the tail, which maps directly to the top of the logical stack.

**Steps:**

1. **Initialize** an empty list `stack` and an empty list `results`. These are the only two data structures needed for the entire simulation.

2. **Iterate** over each operation string in order. Order matters — operations are stateful and sequential.

3. **Parse** the operation by splitting on whitespace. The first token is the command; the second token (if present) is the integer argument for `push`.

4. **Branch on command:**
   - `push`: convert the argument to `int`, append to `stack`. No output.
   - `pop`: if `stack` is non-empty, pop and append the value to `results`; otherwise append `-1`. The invariant maintained: `stack` always reflects the true state after every operation.
   - `peek`: if `stack` is non-empty, append `stack[-1]` to `results` without modifying `stack`; otherwise append `-1`.

5. **Return** `results` after all operations are processed.

The key invariant: after each operation, `stack` exactly represents the ordered set of trays currently in the dispenser, with index `-1` always being the top.

---

## 📊 Worked Example

Using Example 1: `["push 5", "push 3", "peek", "pop", "pop", "pop"]`

| Step | Operation | Stack (bottom→top) | Output Appended | `results` so far |
|------|-----------|--------------------|-----------------|------------------|
| 1    | push 5    | [5]                | —               | []               |
| 2    | push 3    | [5, 3]             | —               | []               |
| 3    | peek      | [5, 3]             | 3               | [3]              |
| 4    | pop       | [5]                | 3               | [3, 3]           |
| 5    | pop       | []                 | 5               | [3, 3, 5]        |
| 6    | pop       | []                 | -1 (underflow)  | [3, 3, 5, -1]    |

`peek` at step 3 reads `stack[-1] = 3` without mutation. Step 6 hits the empty-stack guard, emitting `-1`. Final output: `[3, 3, 5, -1]`.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** where `n` is the number of operations. Each operation executes in O(1) — array append and pop from the tail are O(1) amortized; string split on a bounded-length string is effectively constant. At 10^6 operations this is sub-millisecond; at 10^9 it remains linear and cache-friendly with no hidden log factors.

### Space Complexity

**O(n)** in the worst case — the `stack` structure owns this space when all operations are `push` with no intervening `pop`. The `results` list adds at most O(n) additional space. Neither can be eliminated without changing the output contract; no meaningful trade-off exists here.

---

## 💡 Key Takeaways

- **Pattern signal — exclusive top access:** When a problem only ever reads or writes the most recently added element, a stack is the correct abstraction. If you find yourself indexing into the middle of the structure, reconsider.
- **Pattern signal — operation dispatch loop:** A string-encoded command sequence with branching behavior maps directly to a simulation loop. Recognize this shape in system design questions (e.g., "process a stream of events") as well as algorithm problems.
- **Implementation gotcha — underflow on `peek`:** `peek` is read-only but still requires the empty-stack guard. It's easy to add the guard for `pop` and forget it for `peek` since `peek` "doesn't change anything" — both paths must be protected.
- **Implementation gotcha — `pop` vs. `peek` mutation:** `pop` must remove the element; `peek` must not. Using `stack.pop()` for both is a silent correctness bug that passes many test cases until a sequence like `["peek", "pop"]` exposes it.
- **Architectural insight — explicit underflow over exceptions:** Returning a sentinel value (`-1`) instead of raising on empty is the production-correct pattern for resource-bounded systems. Exception-based underflow handling breaks fast-path performance and complicates caller contracts in high-throughput pipelines.

---

## 🚀 Variations & Further Practice

- **Min Stack (LeetCode #155):** Extend this stack to support an O(1) `getMin()` operation. The twist is maintaining minimum state across pushes and pops without scanning — requires a secondary stack tracking minimums, which introduces a non-obvious invariant about when to push/pop the auxiliary structure.
- **Evaluate Reverse Polish Notation (LeetCode #150):** Operands push onto a stack; operators pop two operands, compute, and push the result. The conceptual twist is that the stack now holds intermediate computed values, not raw inputs — the simulation loop must handle heterogeneous token types and operator precedence implicitly through evaluation order.
- **Implement a Queue Using Two Stacks (LeetCode #232):** Simulate FIFO semantics using only LIFO primitives. The twist is amortized O(1) dequeue via lazy transfer between stacks, which tests whether you understand the difference between worst-case and amortized complexity in data structure design.