# USB Port Power Negotiation Bits

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Bitwise XOR, Bit Counting

---

## 🗂 Problem Overview

Given a 32-bit integer `status` encoding the active/idle state of USB ports, apply a toggle mask via XOR, then count active bits within the lowest `k` positions of the result. The output is `[newStatus, activeCount]`. The non-trivial constraint is that the popcount is scoped to a bit-window `[0, k-1]`, not the full integer — requiring deliberate masking before counting rather than a naive full-word popcount.

---

## 🌍 Engineering Impact

This pattern is pervasive in systems programming. Linux's `cpumask_t` uses exactly this model — XOR to toggle CPU affinity bits, then popcount within a range to count eligible cores. Network ASICs track port link-state in status registers and apply change masks on interrupt. Embedded power managers toggle peripheral enable bits and count active consumers to enforce current budgets. Without O(1) bitwise operations here, polling-based firmware loops become the bottleneck, and at scale — hundreds of network ports or thousands of CPU cores — the difference between a register operation and a loop is architectural.

---

## 🔍 Problem Statement

**Inputs:**
- `status` (`0 ≤ status ≤ 2³¹ − 1`): current port state bitmap
- `mask` (`0 ≤ mask ≤ 2³¹ − 1`): ports to toggle
- `k` (`1 ≤ k ≤ 32`): bit-window width for counting

**Output:** `[newStatus, activeCount]`
- `newStatus = status XOR mask`
- `activeCount` = number of set bits in `newStatus` restricted to bit positions `0` through `k-1`

**Examples:**

| status | mask | k | newStatus | activeCount |
|--------|------|---|-----------|-------------|
| 13 (`01101`) | 10 (`01010`) | 4 | 7 (`0111`) | 3 |
| 255 (`11111111`) | 170 (`10101010`) | 8 | 85 (`01010101`) | 4 |

**Key constraint driving the algorithm:** `k` can equal 32, which means a naïve `(1 << k)` window mask overflows a 32-bit signed integer — requiring careful handling with unsigned types or a conditional.

---

## 🪜 How to Solve This

1. **Toggle via XOR** → XOR is the canonical bit-flip: `x ^ 1 = ~x` per bit, `x ^ 0 = x`. Applying `mask` flips exactly the requested ports and leaves all others unchanged. No branching needed.

2. **Scope the count to `[0, k-1]`** → You can't popcount the full integer; you need only the lower `k` bits. The natural tool is an isolation mask: a value with exactly `k` low bits set. AND-ing `newStatus` with this mask zeroes out everything above position `k-1`.

3. **Construct the window mask carefully** → `(1 << k) - 1` works for `k < 32`, but `1 << 32` is undefined behavior in C/C++ and overflows in Java/Python's 32-bit context. The safe pattern: if `k == 32`, the window mask is `0xFFFFFFFF` (all bits); otherwise `(1 << k) - 1`.

4. **Count set bits** → Use the language's built-in popcount (`Integer.bitCount`, `bin().count('1')`, `__builtin_popcount`) — these compile to a single hardware instruction on modern CPUs. A manual loop works but signals unfamiliarity with the platform.

---

## 🧩 Algorithm Walkthrough

**Pattern: Bitmasking + Windowed Popcount**

This is a two-phase bitmasking problem. Phase one is a pure XOR toggle; phase two is a scoped popcount using an isolation mask.

**Step 1 — XOR Toggle:**
Compute `newStatus = status ^ mask`. XOR's self-inverse property guarantees idempotency: applying the same mask twice returns to the original state. This invariant is critical in toggle-based state machines (think: keyboard modifier keys, feature flags).

**Step 2 — Build the isolation mask:**
Construct `windowMask` to isolate bits `[0, k-1]`:
- If `k == 32`: `windowMask = 0xFFFFFFFF` (or `-1` in signed 32-bit, all bits set)
- Otherwise: `windowMask = (1 << k) - 1`

This mask has exactly `k` trailing 1-bits. The off-by-one trap: `k=4` should yield `0b1111` (15), not `0b10000` (16).

**Step 3 — Isolate the window:**
`windowed = newStatus & windowMask`. This zeroes all bits at position `k` and above, leaving only the ports within scope.

**Step 4 — Popcount:**
`activeCount = popcount(windowed)`. On x86, this resolves to the `POPCNT` instruction — O(1) in hardware, not O(k).

**Step 5 — Return** `[newStatus, activeCount]`. Note: `newStatus` is the full toggled value, not the windowed one.

---

## 📊 Worked Example

**Input:** `status = 13`, `mask = 10`, `k = 4`

| Step | Operation | Binary | Decimal |
|------|-----------|--------|---------|
| Initial status | — | `...0000 1101` | 13 |
| mask | — | `...0000 1010` | 10 |
| XOR toggle | `13 ^ 10` | `...0000 0111` | 7 |
| windowMask (`k=4`) | `(1<<4)-1` | `...0000 1111` | 15 |
| Windowed AND | `7 & 15` | `...0000 0111` | 7 |
| Popcount | `popcount(7)` | bits set: 0,1,2 | **3** |

**Output:** `[7, 3]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(1).** Every operation — XOR, AND, popcount — executes in constant time regardless of input magnitude. At 10⁶ or 10⁹ invocations, this is purely throughput-bound by memory bandwidth and call overhead, not by the computation itself. Hardware `POPCNT` makes the bit-counting step a single clock cycle.

### Space Complexity

**O(1).** No auxiliary data structures are allocated. All intermediate values (`newStatus`, `windowMask`, `windowed`) are scalar registers. There is no space/time trade-off to consider here — this is already optimal on both axes.

---

## 💡 Key Takeaways

- **Pattern signal — interview:** When a problem says "flip specific elements" and elements map to boolean states, XOR is the canonical tool. The word "toggle" in any problem statement is a direct pointer to XOR.
- **Pattern signal — code review:** If you see a loop iterating over bits to conditionally flip them, it's a refactor target — a single XOR replaces the entire loop with no loss of clarity.
- **Gotcha — overflow on `k=32`:** `1 << 32` is undefined behavior in C/C++ and silently wrong in Java (shift amount is masked to 5 bits, so `1 << 32 == 1`). Always special-case `k == 32` or use `>>> k` with a 64-bit intermediate.
- **Gotcha — return the full `newStatus`, not the windowed value:** The AND with `windowMask` is only for counting. Returning the masked value as `newStatus` silently corrupts the upper port states — a subtle bug that only surfaces when `k < 32`.
- **Architectural insight:** Encoding state as a bitmap and operating with XOR/AND/OR gives you atomic, branch-free state transitions that compose cleanly. This is why hardware registers, Linux kernel bitmasks, and network protocol headers all converge on this representation — it's not just an optimization, it's the right abstraction for dense boolean state at any scale.

---

## 🚀 Variations & Further Practice

- **Sliding window popcount:** Instead of a fixed `[0, k-1]` window, count active bits in an arbitrary range `[i, j]`. The twist: you need two isolation masks and must handle the case where `i > 0`, which requires shifting or combining two masked popcounts — prefix-sum thinking applied to bit positions.
- **Multi-word bitmaps (k > 32):** Extend the problem to a `uint64_t` or an array of 32-bit words representing 128 or 1024 ports. The toggle and count logic is the same per word, but you must handle word-boundary alignment for the `k`-bit window — this is exactly how `cpumask_t` and Java's `BitSet` are implemented internally.
- **Toggle with priority constraints:** Given a toggle mask and a priority mask, only toggle ports that are in the mask AND not in a protected set, then return the minimum number of additional toggles needed to reach a target state. This introduces constraint intersection (AND) before XOR and connects to Hamming distance — the number of differing bits between two states.