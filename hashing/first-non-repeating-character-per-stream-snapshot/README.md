# First Non-Repeating Character Per Stream Snapshot

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, Queue, String

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine characters arriving one by one, like letters dropping into a mailbox. After each new arrival, you need to instantly identify the first letter that has appeared only once so far. If every letter has shown up more than once, you report "none." The goal is to produce a running snapshot of that answer after every single arrival.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This pattern powers real-time data monitoring systems used across many industries. Live customer support dashboards use it to surface the first unresolved ticket type. Financial platforms apply it to flag the earliest unique transaction anomaly in a fraud-detection stream. Search engines like Google use similar streaming logic to track trending-but-not-yet-repeated queries. Getting this right means faster responses, fewer missed signals, and better user experiences — all translating directly into cost savings and customer retention.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a busy café where customers call out their orders one at a time. After each order, the barista needs to immediately recall the first drink that only one person has ordered so far. If every drink has been ordered by multiple people, the barista says "nothing unique yet." This problem asks you to record that answer after every single order is placed.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a string `stream` of lowercase English letters arriving sequentially, produce a result string of equal length where `result[i]` holds the first non-repeating character after processing characters `stream[0..i]`. A character is non-repeating if its frequency in the processed prefix equals exactly one. If no such character exists, `result[i]` is `'#'`.

**Constraints:**
- `1 <= stream.length <= 10^5`
- `stream` consists of only lowercase English letters (`a–z`)

**Examples:**

| Input | Output |
|---|---|
| `"aabccb"` | `"a#bbb#"` |
| `"abcd"` | `"aaaa"` |

For `"aabccb"`: after processing each character the first non-repeating character evolves as `a → # → b → b → b → #`.

---

## 🧩 Approach: How We Solve It *(For Developers)*

We combine a **Hash Map** (to count frequencies) with a **Queue** (to track insertion order) for an efficient single-pass solution.

1. **Initialize a frequency map and a queue.** The map tracks how many times each character has appeared. The queue records characters in the order they first arrived, preserving arrival sequence.

2. **Process each character in the stream.** For every incoming character, increment its count in the frequency map. This keeps our occurrence data current in O(1) time.

3. **Add the character to the queue.** Even if it is a duplicate, we enqueue it. The queue represents candidates for "first non-repeating," ordered by arrival time.

4. **Clean the front of the queue.** Peek at the front element. If its frequency in the map is greater than one, it has repeated — dequeue and discard it. Repeat until the front has a frequency of exactly one, or the queue is empty. This lazy-removal strategy avoids rescanning the entire queue each time.

5. **Record the result.** If the queue is non-empty, its front is our answer for this snapshot. Otherwise, record `'#'`.

6. **Repeat for every character** until the stream is fully processed.

---

## 📊 Worked Example *(For Developers)*

Tracing `stream = "aabccb"` step by step:

| Step | Char | Frequency Map | Queue (front→back) | Front Valid? | Result |
|------|------|---------------|--------------------|--------------|--------|
| 1 | `a` | `{a:1}` | `[a]` | a=1 ✅ | `a` |
| 2 | `a` | `{a:2}` | `[a]` → pop `a` → `[]` | empty ❌ | `#` |
| 3 | `b` | `{a:2, b:1}` | `[b]` | b=1 ✅ | `b` |
| 4 | `c` | `{a:2, b:1, c:1}` | `[b, c]` | b=1 ✅ | `b` |
| 5 | `c` | `{a:2, b:1, c:2}` | `[b, c]` | b=1 ✅ | `b` |
| 6 | `b` | `{a:2, b:2, c:2}` | `[b, c]` → pop `b`, pop `c` → `[]` | empty ❌ | `#` |

**Final output:** `"a#bbb#"` ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n)** — We process each character exactly once when enqueuing, and each character is dequeued at most once. Even with the queue-cleaning loop, every element enters and exits the queue only one time total, keeping the overall work linear regardless of stream length.

### Space Complexity

**O(1)** — Although we use a map and a queue, both are bounded by the alphabet size (26 lowercase letters). No matter how long the stream grows, our extra memory stays constant and never scales with input size.

---

## 💡 Key Takeaways *(For Everyone)*

- **Real-time uniqueness detection is everywhere** — from fraud alerts to live dashboards, knowing the "first unique signal" in a stream is a high-value business capability.
- **Streaming problems demand instant answers** — users and systems cannot wait for the full dataset; this approach delivers results after every single data point arrives.
- **A Queue preserves order cheaply** — pairing it with a Hash Map lets us track both frequency and arrival sequence without expensive re-sorting.
- **Lazy deletion is a powerful pattern** — rather than removing stale entries immediately, we clean them only when we need the answer, avoiding unnecessary work.
- **Bounded alphabets unlock O(1) space** — when the input domain is fixed and small (like 26 letters), space complexity becomes constant regardless of data volume.

---

## 🚀 Try It Yourself *(For Developers)*

- **Extend to Unicode or larger alphabets:** What changes when characters are not limited to 26 lowercase letters — does the O(1) space claim still hold, and how would you adapt the map?
- **Sliding window variant:** Instead of the entire prefix, find the first non-repeating character within only the last `k` characters of the stream at each step.
- **First non-repeating word in a sentence stream:** Generalise the approach from single characters to whole words arriving token by token, and consider how tokenisation affects your data structures.

---