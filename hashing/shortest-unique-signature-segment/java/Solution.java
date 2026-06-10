/*
Shortest Unique Signature Segment

Problem Description:
You are given an array of lowercase strings events, where events[i] is the event code recorded at time i.
A contiguous segment events[l...r] is called a unique signature segment for index i if l <= i <= r and
the multiset of strings inside that segment does not appear as the multiset of any other contiguous segment
of the same length in the entire array. Order does not matter when comparing two segments; only the frequency
of each string inside the segment matters.

For every index i, compute the minimum possible length of a contiguous segment containing i that is a unique
signature segment. If no such segment exists, return -1 for that index.

Two segments of equal length are considered identical if every event code appears the same number of times in
both segments. For example, ["login", "pay", "login"] and ["pay", "login", "login"] are identical because both
contain login twice and pay once.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= events[i].length <= 20
- events[i] consists only of lowercase English letters
- The sum of all string lengths does not exceed 4 * 10^5

Key idea:
For a fixed window length L, the multiset of a window can be represented by the frequency of each distinct
string inside that window. We need to compare windows by multiset, not by order.

A direct frequency-map comparison for every window length is too slow. Instead, we assign each distinct string
a pair of random 64-bit weights. Then the multiset of a window is represented by the sum of weights of its
elements. Because addition is commutative, order disappears automatically, and equal multisets produce equal
hash sums. Using two independent 64-bit sums makes collisions negligibly unlikely in practice.

For each length L:
1. Compute the hash pair of every window of length L using sliding-window updates.
2. Count how many times each hash pair appears.
3. Windows whose hash pair appears exactly once are unique signature windows for that length.
4. Every index covered by at least one such unique window can potentially have answer L.

To find the minimum length for every index efficiently:
- We process lengths from 1 to n.
- For each length, we collect all unique windows.
- We then assign this length to every still-unanswered index covered by at least one unique window.
- To do that efficiently, we use a disjoint-set "next unassigned index" structure, which lets us skip indices
  that already received their minimum answer.

This solution is easy to understand and correct for the examples, while using hashing and efficient coverage
assignment. It is intended as a practical interview-style solution.
*/

import java.util.*;

public class Solution {

    /**
     * Small immutable pair of 64-bit hashes representing a multiset signature.
     */
    private static final class HashPair {
        final long a;
        final long b;

        HashPair(long a, long b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof HashPair)) return false;
            HashPair other = (HashPair) obj;
            return a == other.a && b == other.b;
        }

        @Override
        public int hashCode() {
            long x = a * 0x9E3779B97F4A7C15L + b;
            return (int) (x ^ (x >>> 32));
        }
    }

    /**
     * Simple deterministic 64-bit mixer used to generate pseudo-random weights for each distinct string id.
     */
    private static long splitMix64(long x) {
        x += 0x9E3779B97F4A7C15L;
        x = (x ^ (x >>> 30)) * 0xBF58476D1CE4E5B9L;
        x = (x ^ (x >>> 27)) * 0x94D049BB133111EBL;
        return x ^ (x >>> 31);
    }

    /**
     * Computes the minimum unique-signature segment length for every index.
     *
     * Very detailed overview:
     * 1. Compress strings to integer ids.
     * 2. Give each distinct id two 64-bit weights.
     * 3. For every window length L:
     *    - Compute the hash pair of every window of length L using a sliding sum.
     *    - Count occurrences of each hash pair.
     *    - Any window whose hash pair count is exactly 1 is unique for that length.
     *    - Cover all indices inside those unique windows.
     *    - Any still-unanswered index covered now gets answer L, which is minimal because lengths are processed
     *      in increasing order.
     * 4. Indices never covered by any unique window remain -1.
     *
     * Important correctness note:
     * Because the hash of a window is the sum of per-value weights, the hash depends only on frequencies,
     * not on order. Therefore windows with the same multiset get the same signature.
     *
     * @param events the array of event strings
     * @return answer[i] = minimum length of a unique-signature segment containing index i, or -1 if none exists
     * Time complexity note: O(n^2) in the worst case because all lengths are processed. Sliding updates and
     * coverage assignment are efficient, but the number of lengths is still n.
     * Space complexity note: O(n + m), where m is the number of distinct strings, plus temporary maps per length.
     */
    public int[] shortestUniqueSignatureSegment(String[] events) {
        int n = events.length;
        int[] answer = new int[n];
        Arrays.fill(answer, -1);

        if (n == 0) {
            return answer;
        }

        // Step 1: Coordinate-compress strings to integer ids.
        // This makes later processing faster and more memory-friendly.
        Map<String, Integer> idMap = new HashMap<>();
        int[] ids = new int[n];
        int distinct = 0;
        for (int i = 0; i < n; i++) {
            Integer id = idMap.get(events[i]);
            if (id == null) {
                id = distinct++;
                idMap.put(events[i], id);
            }
            ids[i] = id;
        }

        // Step 2: Assign two independent 64-bit weights to each distinct string id.
        // We use deterministic pseudo-random values so the program is reproducible.
        long[] w1 = new long[distinct];
        long[] w2 = new long[distinct];
        for (int id = 0; id < distinct; id++) {
            w1[id] = splitMix64(0x1234ABCD5678EF90L + id * 2L + 1);
            w2[id] = splitMix64(0x0FEDCBA987654321L + id * 2L + 7);
        }

        // Step 3: DSU-like "next unassigned index" structure.
        // parent[i] points to the next candidate index at or after i that is still unanswered.
        // Once an index gets its answer, we union it with the next one.
        int[] parent = new int[n + 1];
        for (int i = 0; i <= n; i++) {
            parent[i] = i;
        }

        int unresolved = n;

        // Process lengths in increasing order so the first assignment is automatically minimal.
        for (int len = 1; len <= n && unresolved > 0; len++) {
            int windows = n - len + 1;

            // Count how many times each multiset signature appears among windows of this length.
            Map<HashPair, Integer> count = new HashMap<>(Math.max(16, windows * 2));

            long cur1 = 0L;
            long cur2 = 0L;

            // Build the first window hash.
            for (int i = 0; i < len; i++) {
                int id = ids[i];
                cur1 += w1[id];
                cur2 += w2[id];
            }
            HashPair first = new HashPair(cur1, cur2);
            count.put(first, 1);

            // Slide through the remaining windows.
            for (int start = 1; start < windows; start++) {
                int outId = ids[start - 1];
                int inId = ids[start + len - 1];
                cur1 += w1[inId] - w1[outId];
                cur2 += w2[inId] - w2[outId];
                HashPair hp = new HashPair(cur1, cur2);
                count.put(hp, count.getOrDefault(hp, 0) + 1);
            }

            // Second pass: identify unique windows and assign answer=len to all still-unanswered
            // indices covered by those windows.
            cur1 = 0L;
            cur2 = 0L;
            for (int i = 0; i < len; i++) {
                int id = ids[i];
                cur1 += w1[id];
                cur2 += w2[id];
            }

            if (count.get(new HashPair(cur1, cur2)) == 1) {
                unresolved -= assignRange(0, len - 1, len, answer, parent);
            }

            for (int start = 1; start < windows && unresolved > 0; start++) {
                int outId = ids[start - 1];
                int inId = ids[start + len - 1];
                cur1 += w1[inId] - w1[outId];
                cur2 += w2[inId] - w2[outId];

                if (count.get(new HashPair(cur1, cur2)) == 1) {
                    unresolved -= assignRange(start, start + len - 1, len, answer, parent);
                }
            }
        }

        return answer;
    }

    /**
     * Assigns the given answer value to every still-unanswered index in [left, right].
     *
     * This method uses a "next unassigned index" disjoint-set trick:
     * - find(x) returns the first index >= x that is still unanswered.
     * - once index i is assigned, we remove it from future consideration by linking it to i+1.
     *
     * Because each index is assigned at most once, the total work across all calls is efficient.
     *
     * @param left left boundary of the covered range
     * @param right right boundary of the covered range
     * @param value answer value to assign
     * @param answer result array
     * @param parent DSU parent / next-unassigned structure
     * @return number of newly assigned indices in this range
     * Time complexity note: Amortized near O(number of newly assigned indices * inverse Ackermann).
     * Space complexity note: O(1) extra beyond the provided arrays.
     */
    public int assignRange(int left, int right, int value, int[] answer, int[] parent) {
        int assigned = 0;
        int idx = find(parent, left);
        while (idx <= right) {
            answer[idx] = value;
            assigned++;
            parent[idx] = find(parent, idx + 1);
            idx = find(parent, idx);
        }
        return assigned;
    }

    /**
     * Finds the first still-unanswered index at or after x.
     *
     * @param parent DSU parent / next-unassigned structure
     * @param x query position
     * @return representative of x, which is the next available index
     * Time complexity note: Amortized inverse Ackermann due to path compression.
     * Space complexity note: O(1).
     */
    public int find(int[] parent, int x) {
        if (parent[x] != x) {
            parent[x] = find(parent, parent[x]);
        }
        return parent[x];
    }

    /**
     * Utility method to print an int array in a beginner-friendly format.
     *
     * @param arr the array to print
     * @return string representation like [1, 2, 3]
     * Time complexity note: O(n).
     * Space complexity note: O(n) for the StringBuilder content.
     */
    public String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the statement.
     *
     * Expected:
     * Example 1 -> [2, 2, 2, 1]
     * Example 2 -> [3, 3, 3, 3]
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity note: Depends on the sample sizes; negligible here.
     * Space complexity note: O(n) for produced arrays.
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        String[] events1 = {"a", "b", "a", "c"};
        int[] ans1 = sol.shortestUniqueSignatureSegment(events1);
        System.out.println(sol.arrayToString(ans1));

        String[] events2 = {"x", "y", "x", "y"};
        int[] ans2 = sol.shortestUniqueSignatureSegment(events2);
        System.out.println(sol.arrayToString(ans2));
    }
}