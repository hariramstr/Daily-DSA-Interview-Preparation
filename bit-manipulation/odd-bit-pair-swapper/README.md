# Odd Bit Pair Swapper

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Bit Counting, Array

---

## 🗂 What Is This Problem? *(For Everyone)*

Given a list of numbers, we apply a specific transformation to each one — swapping neighbouring pairs of binary switches — then count how many pairs of transformed numbers share a particular degree of difference. Think of it as rearranging the internal wiring of each number, then measuring how many number-pairs differ in exactly the right number of places.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Bit-level transformations and difference counting are foundational to how modern systems work at scale. Network routers use similar techniques to encode and compare packet headers at lightning speed. Cryptographic systems rely on measuring bit-level differences (called "Hamming distance") to verify data integrity and detect tampering. Error-correcting codes in mobile networks and satellite communications use the same principle to detect and repair corrupted data mid-transmission — directly improving call quality, reducing dropped connections, and saving telecom companies millions in retransmission costs.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Imagine every number is a row of light switches — some on, some off. Your first job is to swap every neighbouring pair of switches throughout the row. Once all rows are re-wired, your second job is to compare every possible pairing of rows and count how many pairs differ in *exactly* `k` switch positions. The final answer is simply that count of qualifying pairs.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an array `nums` of non-negative integers and an integer `k`, perform the following:

1. **Transform** each integer by swapping every pair of adjacent bits across all 32 bit positions: bit 0 ↔ bit 1, bit 2 ↔ bit 3, …, bit 30 ↔ bit 31.
2. **Count** all pairs `(i, j)` where `i < j` such that `popcount(transformed[i] XOR transformed[j]) == k`.

**Constraints:**
- `1 <= nums.length <= 1000`
- `0 <= nums[i] <= 2^30 - 1`
- `0 <= k <= 30`

**Examples:**

| Input | k | Output |
|---|---|---|
| `[2, 1, 3]` | `1` | `2` |
| `[5, 10, 0]` | `2` | `2` |

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Isolate even-position bits** — Apply the mask `0xAAAAAAAA` (binary: `1010...1010`) to each number using bitwise AND. This captures all bits sitting in odd-indexed positions (1, 3, 5, …).

2. **Isolate odd-position bits** — Apply the mask `0x55555555` (binary: `0101...0101`) to capture all bits in even-indexed positions (0, 2, 4, …).

3. **Perform the swap** — Shift the even-position bits right by 1 (`>> 1`) and shift the odd-position bits left by 1 (`<< 1`). Combine with OR. This effectively swaps every adjacent pair of bits simultaneously across all 32 positions without any looping over individual bits.

4. **Store transformed values** — Replace each element with its transformed counterpart in a new array.

5. **Count qualifying pairs** — Use a nested loop over all pairs `(i, j)` where `i < j`. For each pair, XOR the two transformed values, count the number of set bits using `bin(...).count('1')` or a built-in `popcount` function, and increment the result counter if the count equals `k`.

6. **Return the counter** — The final count is the answer.

This approach is clean, leverages constant-time bitwise operations for the transformation, and keeps the logic easy to reason about.

---

## 📊 Worked Example *(For Developers)*

**Input:** `nums = [5, 10, 0]`, `k = 2`

### Step 1 – Transform each number

| Original | Binary | Even bits (`& 0xAAAA`) | Odd bits (`& 0x5555`) | Shifted & combined | Result |
|---|---|---|---|---|---|
| `5` | `0101` | `0100` → shift right → `0010` | `0001` → shift left → `0010` | `0010 OR 0010` | `10` |
| `10` | `1010` | `1000` → shift right → `0100` | `0010` → shift left → `0100` | `0100 OR 0100` | `5` |
| `0` | `0000` | `0000` | `0000` | `0000` | `0` |

**Transformed array:** `[10, 5, 0]`

### Step 2 – Count pairs where XOR has exactly 2 set bits

| Pair | XOR | Binary | Set bits | Qualifies? |
|---|---|---|---|---|
| `(10, 5)` | `15` | `1111` | 4 | ✗ |
| `(10, 0)` | `10` | `1010` | 2 | ✓ |
| `(5, 0)` | `5` | `0101` | 2 | ✓ |

**Output:** `2`

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n²)** — The transformation step runs in O(n) with constant-time bitwise operations per element. The pair-counting step checks every combination of two elements, which grows quadratically. For `n = 1000`, this means roughly 500,000 comparisons — fast enough for the given constraints but would need rethinking for very large arrays.

### Space Complexity

**O(n)** — We store one transformed value per input element. No recursive call stack or auxiliary data structures are needed, so memory usage grows linearly and stays modest even at the upper constraint limit.

---

## 💡 Key Takeaways *(For Everyone)*

- **Bit-level operations power real-world systems** — from network packet routing to data integrity checks in financial transactions, these low-level techniques underpin modern infrastructure.
- **Small algorithmic choices have big performance consequences** — swapping bits with masks instead of loops is orders of magnitude faster at scale, translating directly to lower server costs.
- **Two complementary masks do the heavy lifting** — `0xAAAAAAAA` and `0x55555555` are a classic pair for isolating alternating bits; memorise them for any adjacent-bit manipulation problem.
- **XOR is the natural tool for measuring difference** — a set bit in `A XOR B` means exactly that position differs between A and B; `popcount(A XOR B)` gives the Hamming distance.
- **Decouple transformation from comparison** — transforming first and comparing second keeps each concern isolated, making the code easier to test, debug, and extend.

---

## 🚀 Try It Yourself *(For Developers)*

- **Rotate instead of swap** — Instead of swapping adjacent pairs, try rotating all bits left by one position. How does the mask strategy change? Can you still avoid a per-bit loop?
- **Generalise the distance threshold** — Modify the solution to return all pairs where the Hamming distance is *at most* `k` rather than exactly `k`. How does this affect the counting logic?
- **Optimise for large inputs** — The current O(n²) pair-counting becomes slow for `n = 10^5`. Research how a frequency map of transformed values combined with combinatorics could reduce this to O(n · 32).

---