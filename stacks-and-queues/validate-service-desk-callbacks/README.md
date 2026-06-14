# Validate Service Desk Callbacks

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Stacks and Queues &nbsp;|&nbsp; **Tags:** queue, simulation, array

---

## 🗂 Problem Overview
Given two arrays, `arrivals` and `handled`, determine whether `handled` could be the actual processing log of a FIFO support queue. Calls arrive in the exact order listed by `arrivals`, and the desk can only handle the current front of the queue. Some calls may remain unhandled, so `handled` does not need to include every arrival. The non-trivial constraint is validating order without explicitly simulating expensive queue removals across up to `100000` elements.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must validate claimed processing order against an append-only intake stream: ticketing systems, message brokers, job schedulers, event ingestion pipelines, and audit logs for customer support or fulfillment workflows. At scale, you often need to prove that downstream consumers respected ordering guarantees without replaying the full system. Without a linear validation approach, audit checks become too expensive or too stateful. With it, you can cheaply enforce FIFO contracts, detect corrupted logs, and build confidence in operational telemetry, reconciliation jobs, and compliance reporting.

## 🔍 Problem Statement
You are given two arrays of unique integers:

- `arrivals`: call IDs in the exact order they entered the waiting queue
- `handled`: call IDs in the exact order the service desk claims to have processed them

Return `true` if `handled` is a valid callback log, otherwise return `false`.

A valid log must satisfy all of the following:

- every handled call must appear in `arrivals`
- calls are processed strictly in FIFO order
- `handled` may be shorter than `arrivals`, since some calls can remain waiting
- no call appears more than once in either array

Constraints:

- `1 <= arrivals.length <= 100000`
- `0 <= handled.length <= arrivals.length`
- values are distinct in both arrays

Examples:

- `arrivals = [10, 20, 30, 40]`, `handled = [10, 20]` → `true`
- `arrivals = [10, 20, 30, 40]`, `handled = [10, 30]` → `false`

The key constraint is size: `100000` elements rules out repeated searching or front-removal simulation with poor asymptotics.

## 🪜 How to Solve This
1. Read the FIFO rule carefully → this is not a general subsequence problem. A handled call is valid only if it is exactly the next unprocessed arrival.

2. That immediately suggests a queue view of `arrivals`, but you do not need a real queue. Since arrivals are fixed, the “front” is just the next unread index.

3. So track one pointer into `arrivals` and scan `handled` from left to right.

4. For each handled call, ask one question: does it equal the current front of the queue?  
   - If yes, advance the arrival pointer.  
   - If no, the log is invalid immediately.

5. Why is this enough? Because calls are unique and the desk cannot skip over earlier arrivals. If `handled[i]` differs from `arrivals[p]`, then some older call is still waiting, which violates FIFO.

6. This turns the problem into a simple linear simulation with no extra data structure required. Once you see “validate claimed order against strict queue semantics,” the one-pointer scan becomes the obvious fit.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Queue simulation with a single pointer.**  
   The system is FIFO, and `handled` is claimed to be a processed prefix of that queue. Because `arrivals` is already in queue order, the next valid handled call must always be `arrivals[front]`.

2. **Initialize `front = 0`.**  
   This pointer represents the current front of the waiting queue. Invariant: all calls in `arrivals[0..front-1]` have been validly handled.

3. **Iterate through each call ID in `handled`.**  
   For each `call`, compare it with `arrivals[front]`.

4. **If `front` is out of bounds or `call != arrivals[front]`, return `false`.**  
   This is correct because FIFO forbids skipping. If the claimed handled call is not the oldest waiting call, the log cannot be valid.

5. **Otherwise, advance `front++`.**  
   This simulates removing the front element from the queue. Invariant remains preserved: the processed prefix still matches exactly.

6. **After all handled calls are checked, return `true`.**  
   If every handled call matched the current queue front in order, then `handled` is a valid processed prefix. Remaining arrivals, if any, are simply still waiting.

Why this abstraction fits: this is not a needlessly materialized queue, nor a hash-based membership problem. It is a deterministic prefix-validation problem over a FIFO stream, so pointer-based simulation is both minimal and optimal.

## 📊 Worked Example
Example: `arrivals = [10, 20, 30, 40]`, `handled = [10, 20]`

| Step | `front` before | Expected front | Current handled | Match? | `front` after |
|------|----------------|----------------|-----------------|--------|---------------|
| 1    | 0              | 10             | 10              | Yes    | 1             |
| 2    | 1              | 20             | 20              | Yes    | 2             |

Trace:

1. Start with `front = 0`, so the queue front is `arrivals[0] = 10`.
2. First handled call is `10`, which matches. Advance `front` to `1`.
3. Now the queue front is `arrivals[1] = 20`.
4. Second handled call is `20`, which also matches. Advance `front` to `2`.
5. End of `handled`. Validation succeeds.

Calls `30` and `40` remain in the queue, which is allowed because `handled` only needs to be a valid prefix of the full processing order.

## ⏱ Complexity Analysis
### Time Complexity
`O(h)`, where `h = handled.length`; equivalently `O(n)` in the worst case when all arrivals are handled. The dominant operation is one equality check per handled call. At `10^6` elements this is trivial in memory-local scans; at `10^9`, runtime becomes bandwidth-bound, but the algorithm remains asymptotically optimal.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only an index into `arrivals` and iterates over `handled`. Space cannot be meaningfully reduced below constant; adding an explicit queue would only increase memory with no correctness or performance benefit.

## 💡 Key Takeaways
- If a problem says “processed in arrival order” and asks whether another sequence is valid, first test whether it is simply a prefix-validation problem over a queue.
- When the source order is already fixed and immutable, a pointer often replaces a full queue data structure.
- Do not accidentally solve the weaker “is `handled` a subsequence of `arrivals`?” problem; FIFO requires exact front matching, not arbitrary skipping.
- Guard the empty-`handled` case correctly: it is always valid, even when `arrivals` is non-empty.
- In production systems, many ordering audits reduce to validating invariants over append-only logs rather than replaying full operational state.

## 🚀 Variations & Further Practice
- Allow interleaved arrivals and handled events in a single event stream; the harder twist is maintaining queue state online rather than validating against a static array.
- Validate logs when duplicate call IDs are allowed; the harder twist is that equality alone no longer identifies a unique queue position, so counts or indexed identities are required.
- Extend to multiple service desks with different scheduling policies; the harder twist is validating against FIFO per partition, priority queues, or work-stealing semantics instead of one global queue.