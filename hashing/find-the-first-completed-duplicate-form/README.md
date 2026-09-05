# Find the First Completed Duplicate Form

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, Bitmasking, Set

---

## 🗂 Problem Overview
You receive an ordered stream of `[formId, fieldName]` submissions and a fixed list of required fields. A form becomes complete once it has seen every required field at least once; repeated submissions for the same field do not matter. Return the first `formId` that becomes complete when another form has already completed with the exact same unique required-field set. The challenge is online processing at scale: up to `200000` events, arbitrary ordering, duplicates, and irrelevant fields.

## 🌍 Engineering Impact
This pattern shows up in streaming pipelines, fraud detection, event-sourced workflows, and form or profile completion systems where state is assembled incrementally from unordered events. The core requirement is online equivalence detection under deduplication: recognize when an entity crosses a threshold and matches a previously observed canonical state. Without hashing and compact state encoding, teams fall into repeated scans, per-entity set comparisons, or batch recomputation, which collapse under high-cardinality streams. The right approach enables single-pass processing, bounded per-event work, and deterministic behavior suitable for real-time alerting, workflow orchestration, and low-latency product logic.

## 🔍 Problem Statement
Given `requiredFields`, a distinct list of up to `20` field names, and an ordered list of `submissions`, where each submission is `[formId, fieldName]`, process the stream in order and return the first `formId` that becomes a completed duplicate.

A form is complete once it has received all required fields at least once. Duplicate submissions for the same field on the same form count only once. Submissions for non-required fields must be ignored. A form is a completed duplicate only at the instant it becomes complete and only if some other form has already completed with the same required-field signature.

Because every completed form must contain all required fields, every completed form has the same final required signature; the real task is therefore to detect the second form to reach completeness, respecting stream order.

Example 1:  
`requiredFields = ["name","email","phone"]`  
`submissions = [[101,"name"],[102,"email"],[101,"email"],[102,"name"],[101,"phone"],[102,"phone"]]`  
Output: `102`

Example 2:  
`requiredFields = ["id","photo"]`  
`submissions = [[7,"id"],[8,"id"],[7,"photo"],[7,"photo"],[9,"photo"],[8,"photo"]]`  
Output: `8`

The key constraint is `200000` ordered submissions, which rules out rescanning per form or comparing sets repeatedly.

## 🪜 How to Solve This
1. Read the problem carefully → the stream order matters, so this is not a sort-then-group problem. We need an online algorithm.

2. Notice what determines progress for a form → only required fields matter, and each required field should count once. That suggests maintaining per-form deduplicated state.

3. Required fields are capped at `20` → that is a strong signal to encode field presence as a bitmask instead of a dynamic set. Each field gets a bit position.

4. Completion becomes easy → a form is complete when its mask equals the `fullMask` with all required bits set.

5. Now ask what “completed duplicate” really means → when a form first reaches `fullMask`, has any other form already reached `fullMask` earlier?

6. That means we only need:
   - a map from `fieldName -> bit`
   - a map from `formId -> currentMask`
   - a count of how many forms have already completed

7. Process each submission once:
   - ignore non-required fields
   - OR the corresponding bit into the form’s mask
   - if the mask changed and just became `fullMask`, check whether `completedCount > 0`
   - if yes, return this `formId`; otherwise increment `completedCount`

This is the classic combination of hashing for lookup and bitmasking for compact incremental state.

## 🧩 Algorithm Walkthrough
1. **Precompute field encoding using a Hash Map + Bitmasking pattern.**  
   Assign each required field an index `0..k-1`, where `k = len(requiredFields)`. Store `fieldToBit[field] = 1 << index`. Build `fullMask = (1 << k) - 1`.  
   **Why correct:** every required field maps to one unique bit, so a form’s mask exactly represents the set of required fields seen so far.  
   **Invariant:** `fullMask` is the canonical signature of a complete form.

2. **Track per-form state in a hash map.**  
   Maintain `formMask[formId]`, defaulting to `0`.  
   **Why correct:** submissions for the same form can arrive in any order, so state must be addressable by `formId` in O(1) expected time.  
   **Invariant:** `formMask[id]` contains exactly the required fields observed for that form.

3. **Process submissions in stream order.**  
   For each `[formId, fieldName]`, ignore it if `fieldName` is not required. Otherwise compute `newMask = oldMask | fieldBit`.  
   **Why correct:** OR-ing a bit deduplicates repeated field submissions automatically.  
   **Invariant:** masks are monotonic; bits only turn on, never off.

4. **Detect first-time completion only.**  
   If `oldMask != fullMask` and `newMask == fullMask`, the form has just become complete.  
   **Why correct:** this excludes repeated submissions after completion and ensures the completion event is counted once.  
   **Invariant:** each form contributes at most one completion event.

5. **Check for duplicate completion.**  
   Maintain `completedCount`, the number of forms that have already completed earlier in the stream. If a form just completed and `completedCount > 0`, return its `formId`; otherwise increment `completedCount`.  
   **Why correct:** since all complete forms share the same required-field signature, any prior completed form makes the current one a completed duplicate.  
   **Invariant:** before processing each event, `completedCount` equals the number of distinct forms that completed strictly earlier.

6. **Finish the scan.**  
   If no completion event occurs with `completedCount > 0`, return `-1`.  
   This is the right abstraction because the problem is fundamentally **streaming state aggregation with hash-indexed entity state and bitmask-based set representation**.

## 📊 Worked Example
Use Example 1.

`requiredFields = ["name","email","phone"]`  
Bit mapping: `name=001`, `email=010`, `phone=100`, so `fullMask=111`.

| Step | Submission      | Form 101 | Form 102 | completedCount | Action |
|------|-----------------|----------|----------|----------------|--------|
| 1    | `[101,"name"]`  | 001      | 000      | 0              | partial |
| 2    | `[102,"email"]` | 001      | 010      | 0              | partial |
| 3    | `[101,"email"]` | 011      | 010      | 0              | partial |
| 4    | `[102,"name"]`  | 011      | 011      | 0              | partial |
| 5    | `[101,"phone"]` | 111      | 011      | 1              | 101 completes first |
| 6    | `[102,"phone"]` | 111      | 111      | 1              | 102 just completed and a prior completed form exists → return `102` |

The important detail is step 6: we only declare a duplicate when the form *transitions* into completeness, not when we later observe more submissions for an already complete form.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`, where `n` is the number of submissions. Each event performs constant expected-time hash lookups and one bitwise OR. There is no nested scan over forms or fields during stream processing. At `10^6` events this remains practical; at `10^9`, the algorithm is still asymptotically right but system design shifts to partitioning, persistence, and memory locality.

### Space Complexity
`O(f + k)`, where `f` is the number of distinct `formId`s seen and `k` is the number of required fields. The dominant structure is the `formId -> mask` map. Space can only be reduced by evicting inactive forms or externalizing state, trading memory for complexity and latency.

## 💡 Key Takeaways
- If the input is an ordered event stream and each entity accumulates a deduplicated set of attributes, think hash-indexed per-entity state rather than sorting or rescanning.
- If the universe of tracked attributes is small and fixed, a bitmask is usually the right canonical representation for set membership and completion checks.
- Only trigger completion logic on the transition `oldMask != fullMask && newMask == fullMask`; otherwise repeated submissions will produce false positives.
- Ignore non-required fields before touching per-form state, or you risk unnecessary map growth and polluted logic paths.
- In production systems, compact canonical state plus single-pass hashing is what turns high-cardinality event correlation from a batch problem into a real-time one.

## 🚀 Variations & Further Practice
- Return the earliest submission index at which any equivalence class reaches its second completed form, where forms may complete with different allowed target field sets; the twist is grouping by arbitrary final signatures, not one fixed `fullMask`.
- Support dynamic required-field definitions per tenant or per form type; the harder part is maintaining separate field encodings and canonical signatures without cross-schema collisions.
- Extend to distributed stream processing where submissions for the same `formId` may arrive on different partitions; the conceptual twist is state sharding, exactly-once completion detection, and duplicate suppression across workers.