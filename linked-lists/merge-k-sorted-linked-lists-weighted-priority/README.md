# Merge K Sorted Linked Lists with Weighted Priority

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked Lists, Priority Queue, Merge Sort

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have several sorted to-do lists and you want to combine them into one master list — in order — but with a rule: if two items are identical, the one from the more important list goes first. After combining, you also want to remove any item that shows up too many times. This problem asks you to do exactly that, efficiently.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This algorithm mirrors real systems used every day. Search engines like Google merge ranked results from multiple data centres, prioritising higher-quality sources when scores tie. Financial platforms merge transaction streams from multiple banks, giving priority feeds precedence during conflicts. News aggregators combine articles from many outlets, surfacing premium sources first. The "threshold" filtering step mirrors spam detection — automatically suppressing any signal that appears suspiciously often. Getting this right means faster results, fairer rankings, and a better user experience at scale.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture three librarians, each handing you a stack of books sorted by title. You must interleave all three stacks into one perfectly sorted pile. When two librarians offer the same book simultaneously, you take it first from the more senior librarian. Once your pile is complete, you remove any book title that appears more than a set number of times — because that many copies is simply too many.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given `k` sorted singly linked lists and a `weights` array of length `k`, merge all lists into one sorted linked list. Tie-breaking rules: equal-valued nodes from different lists are ordered by **descending weight**; if weights also tie, the node from the **smaller list index** comes first.

After merging, remove every node whose value appears **more than `threshold` times** in the merged result. Return the head of the final list.

**Constraints:** `1 ≤ k ≤ 10⁴`, `0 ≤ weights[i] ≤ 10⁹`, each list has at most `500` nodes, node values ∈ `[-10⁵, 10⁵]`, `1 ≤ threshold ≤ 10⁴`.

**Example 1:**
- Input: `lists = [[1,4,7],[2,4,6],[1,3,5]]`, `weights = [3,1,2]`, `threshold = 2`
- Output: `[1,1,2,3,4,4,5,6,7]`

**Example 2:**
- Input: `lists = [[1,1,3],[1,2,3],[3,4,5]]`, `weights = [2,2,1]`, `threshold = 2`
- Output: `[2,4,5]`

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Initialise a min-heap (priority queue).** Push the head node of every non-empty list onto the heap. Each heap entry stores `(value, -weight, list_index, node)`. Negating weight converts our "higher weight wins" rule into a standard min-heap comparison — the heap naturally surfaces the correct node first.

2. **Define a custom comparison key.** The tuple `(value, -weight, list_index)` encodes all three tie-breaking rules in one structure. Python's tuple comparison handles them left-to-right automatically, requiring no extra comparator logic.

3. **Extract nodes greedily.** Repeatedly pop the smallest tuple from the heap. Append that node to the result list. If that node has a `next` pointer, push the next node (from the same list, preserving its weight and index) onto the heap. This is the classic k-way merge pattern.

4. **Count value frequencies during merge.** Maintain a hash map `freq[value]` incremented each time a node is appended. This is O(1) per node and avoids a second pass for counting.

5. **Filter by threshold.** Walk the merged list with a dummy head. Skip any node whose `freq[value] > threshold`. Re-link the remaining nodes to form the final list.

6. **Return the result.** Return `dummy.next` as the head of the cleaned, sorted, weighted-merged list.

---

## 📊 Worked Example *(For Developers)*

Using **Example 2:** `lists = [[1,1,3],[1,2,3],[3,4,5]]`, `weights = [2,2,1]`, `threshold = 2`

| Step | Heap (value, −weight, idx) | Node Popped | Merged So Far | freq |
|------|---------------------------|-------------|---------------|------|
| Init | `(1,−2,0)` `(1,−2,1)` `(3,−1,2)` | — | `[]` | `{}` |
| 1 | `(1,−2,1)` `(3,−1,2)` `(1,−2,0→next)` | `1` (idx 0) | `[1]` | `{1:1}` |
| 2 | `(1,−2,0)` `(3,−1,2)` `(2,−2,1)` | `1` (idx 1) | `[1,1]` | `{1:2}` |
| 3 | `(1,−2,0→next)` `(2,−2,1)` `(3,−1,2)` | `1` (idx 0, 2nd) | `[1,1,1]` | `{1:3}` |
| 4 | `(2,−2,1)` `(3,−1,2)` `(3,−2,0)` | `2` (idx 1) | `[1,1,1,2]` | `{1:3,2:1}` |
| 5–9 | *(continue similarly)* | `3,3,3,4,5` | `[1,1,1,2,3,3,3,4,5]` | `{1:3,2:1,3:3,4:1,5:1}` |
| Filter | Remove values where freq > 2: remove all `1`s and `3`s | | **`[2,4,5]`** | ✓ |

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(N log k)** — where `N` is the total number of nodes across all lists and `k` is the number of lists. Each of the `N` nodes is pushed and popped from the heap exactly once; each heap operation costs `O(log k)`. At 10⁴ lists × 500 nodes, that is ~5 million nodes processed with only ~14 comparisons each — highly practical.

### Space Complexity

**O(k + N)** — the heap holds at most `k` entries at any time (`O(k)`), and the frequency map and output list together consume `O(N)`. No full copy of the input is required beyond the output structure itself.

---

## 💡 Key Takeaways *(For Everyone)*

- **Priority-aware merging is everywhere in business** — search ranking, financial feed aggregation, and content curation all rely on this pattern to surface the "most important" item when sources conflict.
- **Threshold filtering is a powerful quality gate** — automatically discarding over-represented values mirrors how spam filters, anomaly detectors, and deduplication pipelines clean noisy data before it reaches users.
- **A min-heap is the right tool for k-way merges** — it keeps only `k` candidates in memory at once, making it far more scalable than sorting all nodes upfront.
- **Encoding multi-rule tie-breaking into a tuple is elegant and efficient** — a single composite key `(value, -weight, index)` replaces a complex comparator with zero extra overhead.
- **Counting during traversal, not after, saves a full O(N) pass** — incrementing a frequency map as you build the merged list is a general pattern worth remembering for streaming and pipeline problems.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Dynamic weights:** Modify the problem so that a list's weight decreases by 1 each time one of its nodes is selected. How does this change the heap entry structure and the overall complexity?
- **Variation 2 — Threshold as a percentage:** Instead of an absolute count, remove any value that appears in more than 50% of the input lists. How would you adapt the frequency-counting step?
- **Variation 3 — Streaming input:** Suppose the `k` lists arrive as live data streams with unknown lengths. Design a solution that outputs nodes in real time without waiting for all input, and analyse what guarantees you can still provide about ordering.

---