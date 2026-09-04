import java.util.*;

/*
Title: Longest Transcript Window With Bounded Filler Ratio
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a transcript of a meeting as an array words, where each element is a lowercase word spoken at a particular time step.
Some words are considered filler words (such as "um", "uh", or "like"). You are also given an array isFiller of the same length,
where isFiller[i] = 1 if words[i] is a filler word and 0 otherwise.

A contiguous window of the transcript is called usable if it satisfies both of the following conditions:
1. The ratio of filler words in the window is at most p / q, where p and q are positive integers and 0 <= p <= q.
2. The window contains at least k distinct non-filler words.

Return the length of the longest usable contiguous window.

Notes:
- Only non-filler words count toward the distinct-word requirement.
- Filler words still count toward the total window length and toward the filler ratio.
- The filler ratio of a window with f filler words and total length len is f / len.
  To avoid precision issues, compare using integer arithmetic.
- If no window satisfies the conditions, return 0.

Constraints:
- 1 <= words.length == isFiller.length <= 2 * 10^5
- 1 <= words[i].length <= 20
- words[i] consists of lowercase English letters
- isFiller[i] is either 0 or 1
- 0 <= p <= q <= 10^6
- 1 <= k <= words.length

Key idea of the solution:
- The ratio condition for a window [l..r] is:
      fillerCount / windowLength <= p / q
  which is equivalent to:
      fillerCount * q <= windowLength * p
- Rearranging:
      (q - p) * fillerCount <= p * nonFillerCount
  because windowLength = fillerCount + nonFillerCount.
- Define a transformed weight per position:
      weight[i] = p                  if isFiller[i] == 0
                  -(q - p)          if isFiller[i] == 1
  Then a window satisfies the ratio condition exactly when the sum of weights in that window is >= 0.
- We also need at least k distinct non-filler words.

To solve efficiently:
1. Compress all non-filler words to integer ids.
2. Use a classic two-pointer / sliding-window pass to compute, for every right endpoint r,
   the largest left boundary Lk[r] such that every window [l..r] with l >= Lk[r] has at least k distinct non-filler words.
   In other words, [Lk[r]..r] is the shortest suffix ending at r that still has at least k distinct non-filler words.
3. Let prefix sums of the transformed weights be pref[0..n], where sum of window [l..r] is pref[r+1] - pref[l].
   We need:
      pref[l] <= pref[r+1]
   and also
      l <= Lk[r].
   So for each r, we need the smallest index l among all prefix indices 0..Lk[r] whose prefix value is <= pref[r+1].
   That gives the longest valid window ending at r.
4. This becomes an offline prefix-query problem:
   - Add prefix indices in increasing order as they become allowed.
   - Query the minimum index among all added prefixes with value <= current prefix value.
   - Coordinate-compress prefix sums and use a segment tree for prefix minimum queries.

This yields an O(n log n) solution, which is suitable for n up to 2 * 10^5.
*/

public class Solution {

    /**
     * Computes the length of the longest usable contiguous window.
     *
     * A window is usable if:
     * 1) fillerCount / length <= p / q
     * 2) it contains at least k distinct non-filler words
     *
     * This implementation runs in O(n log n) time by combining:
     * - a sliding window to enforce the "at least k distinct non-filler words" condition
     * - prefix sums with transformed weights to encode the filler ratio condition
     * - coordinate compression + segment tree to answer prefix minimum index queries
     *
     * @param words the transcript words
     * @param isFiller array where 1 means the corresponding word is filler, 0 otherwise
     * @param p numerator of the allowed filler ratio
     * @param q denominator of the allowed filler ratio
     * @param k minimum number of distinct non-filler words required in a usable window
     * @return the maximum length of a usable contiguous window, or 0 if none exists
     *
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public int longestUsableWindow(String[] words, int[] isFiller, int p, int q, int k) {
        int n = words.length;
        if (n == 0 || k > n) {
            return 0;
        }

        // Step 1:
        // Compress only non-filler words to integer ids.
        // This makes frequency tracking much faster and more memory-efficient than
        // using strings directly inside the core sliding-window logic.
        int[] nonFillerId = new int[n];
        Arrays.fill(nonFillerId, -1);

        Map<String, Integer> idMap = new HashMap<>();
        int nextId = 0;
        for (int i = 0; i < n; i++) {
            if (isFiller[i] == 0) {
                Integer existing = idMap.get(words[i]);
                if (existing == null) {
                    existing = nextId++;
                    idMap.put(words[i], existing);
                }
                nonFillerId[i] = existing;
            }
        }

        // If the total number of distinct non-filler words in the entire array is less than k,
        // then no window can ever satisfy the distinct-word requirement.
        if (nextId < k) {
            return 0;
        }

        // Step 2:
        // Compute Lk[r]:
        // For each right endpoint r, Lk[r] is the maximum left index such that
        // window [Lk[r]..r] still has at least k distinct non-filler words.
        //
        // Important interpretation:
        // If a window ending at r has at least k distinct non-filler words,
        // then its left boundary l must satisfy l <= Lk[r].
        //
        // Why?
        // - Moving l to the right can only remove words, never add new distinct words.
        // - [Lk[r]..r] is the shortest suffix ending at r that still keeps at least k distinct.
        // - Therefore any earlier left boundary also works, but any later one does not.
        int[] lk = computeMaxLeftForAtLeastKDistinct(nonFillerId, isFiller, nextId, k);

        // Step 3:
        // Build transformed prefix sums for the ratio condition.
        //
        // Let each non-filler contribute +p.
        // Let each filler contribute -(q - p).
        //
        // Then a window sum >= 0 is exactly equivalent to:
        // fillerCount / length <= p / q
        //
        // We use long to be completely safe with arithmetic.
        long[] pref = new long[n + 1];
        long fillerPenalty = (long) q - p;
        for (int i = 0; i < n; i++) {
            long delta = (isFiller[i] == 1) ? -fillerPenalty : (long) p;
            pref[i + 1] = pref[i] + delta;
        }

        // Step 4:
        // Coordinate-compress all prefix sums so we can use them as indices in a segment tree.
        long[] sorted = pref.clone();
        Arrays.sort(sorted);
        int m = uniqueInPlace(sorted);

        // Step 5:
        // Segment tree stores the minimum prefix index seen so far for each compressed prefix value.
        // Query over all compressed values <= current prefix value gives the earliest l
        // such that pref[l] <= pref[r+1], which ensures the ratio condition.
        SegmentTreeMin segTree = new SegmentTreeMin(m);

        int answer = 0;

        // "addedUpTo" tells us which prefix indices have already been inserted into the segment tree.
        // For a given r, allowed left boundaries satisfy l <= lk[r].
        // Since prefix index corresponding to left boundary l is exactly l,
        // we need to add prefix indices 0..lk[r] before querying for this r.
        int addedUpTo = -1;

        for (int r = 0; r < n; r++) {
            // If lk[r] == -1, then no window ending at r has at least k distinct non-filler words.
            // So we skip this r entirely.
            if (lk[r] == -1) {
                continue;
            }

            // Add all newly allowed prefix indices.
            while (addedUpTo < lk[r]) {
                addedUpTo++;
                int compressed = lowerBound(sorted, m, pref[addedUpTo]);
                segTree.update(compressed, addedUpTo);
            }

            // We need the earliest l among allowed prefixes with pref[l] <= pref[r+1].
            int currentCompressed = upperBound(sorted, m, pref[r + 1]) - 1;

            // Since pref[r+1] itself is in the compressed list, currentCompressed is always >= 0.
            int earliestLeft = segTree.query(0, currentCompressed);

            if (earliestLeft != SegmentTreeMin.INF) {
                answer = Math.max(answer, r - earliestLeft + 1);
            }
        }

        return answer;
    }

    /**
     * Computes, for every right endpoint r, the largest left index L such that
     * the window [L..r] still contains at least k distinct non-filler words.
     *
     * If no window ending at r satisfies the distinct-word condition, the result at r is -1.
     *
     * This is done with a standard sliding window:
     * - Expand the right end one step at a time.
     * - Track frequencies of non-filler word ids inside the current window.
     * - While the window has at least k distinct non-filler words, try to shrink from the left.
     * - The last valid left before breaking is exactly the maximum left boundary that still works.
     *
     * @param nonFillerId compressed ids for non-filler words, or -1 for filler positions
     * @param isFiller array where 1 means filler, 0 means non-filler
     * @param distinctUniverse total number of distinct non-filler word ids
     * @param k required number of distinct non-filler words
     * @return array lk where lk[r] is the maximum valid left boundary for windows ending at r, or -1
     *
     * Time complexity: O(n)
     * Space complexity: O(u), where u is the number of distinct non-filler words
     */
    public int[] computeMaxLeftForAtLeastKDistinct(int[] nonFillerId, int[] isFiller, int distinctUniverse, int k) {
        int n = nonFillerId.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);

        int[] freq = new int[distinctUniverse];
        int distinct = 0;
        int left = 0;

        for (int right = 0; right < n; right++) {
            // Include the new rightmost element into the window.
            if (isFiller[right] == 0) {
                int id = nonFillerId[right];
                if (freq[id] == 0) {
                    distinct++;
                }
                freq[id]++;
            }

            // If we do not yet have k distinct non-filler words, then no window ending at "right"
            // can satisfy the distinct-word requirement.
            if (distinct < k) {
                continue;
            }

            // We currently have at least k distinct non-filler words.
            // Now shrink from the left as much as possible while preserving that property.
            //
            // After this loop:
            // - "left" will be the first position that would make the window invalid if removed further.
            // - Therefore [left..right] is the shortest valid suffix ending at right.
            while (left <= right) {
                if (isFiller[left] == 1) {
                    // Removing a filler does not affect the distinct non-filler count,
                    // so we can always discard it while still having at least k distinct.
                    left++;
                } else {
                    int id = nonFillerId[left];
                    if (freq[id] >= 2) {
                        // This non-filler word still appears elsewhere in the window,
                        // so removing this occurrence does not reduce the distinct count.
                        freq[id]--;
                        left++;
                    } else {
                        // freq[id] == 1
                        // Removing this would reduce the distinct count below what we currently have.
                        // Since we are exactly trying to keep at least k distinct,
                        // we must stop here.
                        break;
                    }
                }
            }

            // "left" is now the maximum left boundary that still keeps at least k distinct.
            result[right] = left;
        }

        return result;
    }

    /**
     * Returns the number of unique values in the sorted array after compacting them in-place
     * into the prefix of the same array.
     *
     * Example:
     * sorted = [1,1,2,2,5] becomes [1,2,5,...] and returns 3.
     *
     * @param sorted a sorted array
     * @return number of unique values after in-place compaction
     *
     * Time complexity: O(n)
     * Space complexity: O(1) extra
     */
    public int uniqueInPlace(long[] sorted) {
        if (sorted.length == 0) {
            return 0;
        }
        int write = 1;
        for (int read = 1; read < sorted.length; read++) {
            if (sorted[read] != sorted[write - 1]) {
                sorted[write++] = sorted[read];
            }
        }
        return write;
    }

    /**
     * Finds the first index in the first "len" elements of the sorted array
     * whose value is >= target.
     *
     * @param arr sorted array
     * @param len number of valid elements to consider from the front
     * @param target target value
     * @return first index with arr[index] >= target, or len if none exists
     *
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(long[] arr, int len, long target) {
        int lo = 0;
        int hi = len;
        while (lo < hi) {
            int mid = lo + ((hi - lo) >>> 1);
            if (arr[mid] >= target) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    /**
     * Finds the first index in the first "len" elements of the sorted array
     * whose value is > target.
     *
     * @param arr sorted array
     * @param len number of valid elements to consider from the front
     * @param target target value
     * @return first index with arr[index] > target, or len if none exists
     *
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int upperBound(long[] arr, int len, long target) {
        int lo = 0;
        int hi = len;
        while (lo < hi) {
            int mid = lo + ((hi - lo) >>> 1);
            if (arr[mid] > target) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(n log n) per demonstration call
     * Space complexity: O(n) per demonstration call
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        String[] words1 = {"we", "should", "um", "ship", "this", "uh", "week"};
        int[] isFiller1 = {0, 0, 1, 0, 0, 1, 0};
        int p1 = 1, q1 = 3, k1 = 4;
        int ans1 = sol.longestUsableWindow(words1, isFiller1, p1, q1, k1);
        System.out.println(ans1); // Expected: 6

        String[] words2 = {"uh", "plan", "plan", "um", "launch", "now", "like", "launch", "ready"};
        int[] isFiller2 = {1, 0, 0, 1, 0, 0, 1, 0, 0};
        int p2 = 1, q2 = 4, k2 = 3;
        int ans2 = sol.longestUsableWindow(words2, isFiller2, p2, q2, k2);
        System.out.println(ans2); // Expected: 5
    }

    /**
     * Segment tree for range minimum query with point updates.
     *
     * We store, for each compressed prefix-sum value, the smallest prefix index that has been added.
     * Querying a prefix range [0..x] returns the earliest allowed left boundary whose prefix sum
     * is <= the current prefix sum.
     */
    static class SegmentTreeMin {
        static final int INF = Integer.MAX_VALUE / 4;

        private final int size;
        private final int[] tree;

        /**
         * Creates a segment tree over "n" positions, initialized to INF.
         *
         * @param n number of positions
         */
        SegmentTreeMin(int n) {
            int s = 1;
            while (s < n) {
                s <<= 1;
            }
            this.size = s;
            this.tree = new int[size << 1];
            Arrays.fill(tree, INF);
        }

        /**
         * Applies a point update:
         * tree[index] = min(tree[index], value)
         *
         * @param index compressed coordinate to update
         * @param value prefix index to minimize with
         */
        void update(int index, int value) {
            int pos = index + size;
            tree[pos] = Math.min(tree[pos], value);
            pos >>= 1;
            while (pos > 0) {
                tree[pos] = Math.min(tree[pos << 1], tree[(pos << 1) | 1]);
                pos >>=