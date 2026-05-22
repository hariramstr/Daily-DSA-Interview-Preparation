```java
/*
 * Title: Longest Subarray With Equal Symbol Frequencies
 *
 * Problem Description:
 * You are given a string s consisting of lowercase English letters. Your task is to find
 * the length of the longest contiguous subarray (substring) such that every distinct
 * character appearing in that substring occurs the same number of times.
 *
 * For example, in the substring 'aabb', both 'a' and 'b' appear exactly 2 times — valid.
 * In 'aabc', 'a' appears 2 times while 'b' and 'c' appear 1 time each — invalid.
 *
 * Constraints:
 * - 1 <= s.length <= 100,000
 * - s consists only of lowercase English letters ('a' to 'z')
 *
 * Examples:
 * - s = "aabbcc"   → 6  (each of a, b, c appears 2 times)
 * - s = "aaabbbcc" → 6  (substring "aaabbb": a=3, b=3)
 * - s = "abcde"    → 5  (each character appears 1 time)
 *
 * Approach (Canonical Signature with Prefix Frequencies):
 * -------------------------------------------------------
 * Key Insight:
 *   A substring s[i..j] is valid if all characters present in it have equal frequency.
 *   If we define freq[c] = count of character c in s[i..j], then valid means:
 *   for all c present, freq[c] = some constant k.
 *
 * We use prefix frequency arrays: prefixFreq[i][c] = count of character c in s[0..i-1].
 * Then freq of c in s[i..j] = prefixFreq[j+1][c] - prefixFreq[i][c].
 *
 * For a substring to be valid, all non-zero frequencies must be equal.
 * We normalize the frequency vector by subtracting the minimum non-zero frequency
 * from all non-zero entries. If the result is all zeros, the substring is valid.
 *
 * We store a canonical "signature" of the prefix frequency vector at each position.
 * Two positions i and j have the same signature if and only if the substring s[i..j-1]
 * is valid (all present characters have equal frequency).
 *
 * The canonical signature is computed as:
 *   For each character c, compute prefixFreq[pos][c] - prefixFreq[pos][minPresentChar]
 *   (subtract the minimum non-zero prefix frequency from all non-zero prefix frequencies)
 *   This normalizes so that the "baseline" character has relative difference 0.
 *
 * Wait — let me reconsider. The correct approach:
 *
 * For a substring [i, j] to be valid:
 *   Let f_c = prefixFreq[j+1][c] - prefixFreq[i][c] for each c.
 *   All non-zero f_c must be equal.
 *   This means: for all c, d present in [i,j]:
 *     (prefixFreq[j+1][c] - prefixFreq[i][c]) = (prefixFreq[j+1][d] - prefixFreq[i][d])
 *   Rearranging:
 *     prefixFreq[j+1][c] - prefixFreq[j+1][d] = prefixFreq[i][c] - prefixFreq[i][d]
 *
 * So the DIFFERENCE between any two character prefix frequencies must be the same
 * at position i and position j+1. We can encode this as a signature:
 *   For each character c, store prefixFreq[pos][c] - prefixFreq[pos][referenceChar]
 *   where referenceChar is some fixed character (e.g., 'a').
 *
 * But we also need to handle the case where a character is absent from the substring.
 * If character c is absent from [i,j], then prefixFreq[j+1][c] = prefixFreq[i][c],
 * so the difference is 0 — that's fine, it means c doesn't appear.
 *
 * The signature at position pos is:
 *   (prefixFreq[pos][0]-prefixFreq[pos][0], prefixFreq[pos][1]-prefixFreq[pos][0], ...,
 *    prefixFreq[pos][25]-prefixFreq[pos][0])
 * = (0, prefixFreq[pos][1]-prefixFreq[pos][0], ..., prefixFreq[pos][25]-prefixFreq[pos][0])
 *
 * Two positions with the same signature give a valid substring between them.
 * We want the maximum distance between two positions with the same signature.
 *
 * BUT WAIT: This only ensures that all characters have the same frequency RELATIVE to 'a'.
 * If 'a' doesn't appear in the substring, we need a different reference.
 *
 * Actually, let me think again more carefully.
 *
 * If signature at i equals signature at j (i < j), then for all c:
 *   prefixFreq[j][c] - prefixFreq[j][0] = prefixFreq[i][c] - prefixFreq[i][0]
 * => prefixFreq[j][c] - prefixFreq[i][c] = prefixFreq[j][0] - prefixFreq[i][0]
 *
 * Let k = prefixFreq[j][0] - prefixFreq[i][0] (count of 'a' in substring).
 * Then for ALL c: count of c in substring = k.
 *
 * If k = 0, then ALL characters have count 0 in the substring — but that means
 * the substring is empty or... wait, that can't happen for a non-empty substring
 * unless the substring contains no characters at all, which is impossible.
 *
 * Actually if k=0, it means 'a' doesn't appear in [i..j-1], and since all differences
 * are equal to k=0, NO character appears in [i..j-1]. But that's impossible for a
 * non-empty substring of lowercase letters.
 *
 * Hmm, but what if 'a' doesn't appear but 'b' and 'c' do? Then:
 *   count('a') = 0, count('b') = 2, count('c') = 2
 *   prefixFreq[j][0] - prefixFreq[i][0] = 0
 *   prefixFreq[j][1] - prefixFreq[i][1] = 2
 *   prefixFreq[j][2] - prefixFreq[i][2] = 2
 *
 *   signature at i: (0, prefixFreq[i][1]-prefixFreq[i][0], prefixFreq[i][2]-prefixFreq[i][0], ...)
 *   signature at j: (0, prefixFreq[j][1]-prefixFreq[j][0], prefixFreq[j][2]-prefixFreq[j][0], ...)
 *
 *   For these to be equal:
 *   prefixFreq[j][1]-prefixFreq[j][0] = prefixFreq[i][1]-prefixFreq[i][0]
 *   => prefixFreq[j][1]-prefixFreq[i][1] = prefixFreq[j][0]-prefixFreq[i][0] = 0
 *   But count('b') = 2 ≠ 0. Contradiction!
 *
 * So the signature approach with 'a' as reference does NOT work when 'a' is absent
 * from the substring but other characters are present with equal counts.
 *
 * We need a different approach.
 *
 * CORRECT APPROACH:
 * -----------------
 * The key observation is: a substring is valid iff all present characters have equal frequency.
 * 
 * Let's think about it differently. For a substring [l, r]:
 * Let freq[c] = count of c in [l, r].
 * Valid iff: for all c with freq[c] > 0, freq[c] = same value.
 *
 * This is equivalent to: max(freq) = min_nonzero(freq) (where min_nonzero ignores 0s).
 * Or: (max(freq) - min_nonzero(freq)) = 0 when at least one char is present.
 *
 * For an O(n^2) solution, we can simply check all O(n^2) substrings.
 * For each starting position l, maintain frequency counts as we extend r,
 * and check validity efficiently.
 *
 * For O(n^2): iterate over all pairs (l, r), maintain freq array, track max and
 * count of distinct chars, and check if all distinct chars have same frequency.
 *
 * Actually checking "all distinct chars have same freq" in O(1) per step:
 * - Track: numDistinct (number of chars with freq > 0)
 * - Track: maxFreq
 * - Valid iff: maxFreq * numDistinct == (r - l + 1)
 *   (because if all numDistinct chars each appear maxFreq times, total = maxFreq * numDistinct)
 *   AND this equals the substring length.
 *
 * This is the key insight! maxFreq * numDistinct == length iff all present chars
 * have exactly maxFreq occurrences.
 *
 * Let me verify:
 * - "aabb": maxFreq=2, numDistinct=2, length=4. 2*2=4 ✓
 * - "aabc": maxFreq=2, numDistinct=3, length=4. 2*3=6≠4 ✓ (invalid)
 * - "abcde": maxFreq=1, numDistinct=5, length=5. 1*5=5 ✓
 *
 * This gives us an O(n^2) solution that is clean and correct.
 *
 * For better than O(n^2), we'd need the signature approach, but let's make sure
 * the O(n^2) solution is correct and efficient enough for n=100,000.
 * O(n^2) for n=100,000 is 10^10 operations — too slow!
 *
 * We need a smarter approach. Let me think about the signature approach more carefully.
 *
 * BETTER APPROACH - Normalized Difference Signature:
 * ---------------------------------------------------
 * For a substring [l, r] to be valid with all chars having frequency k:
 *   freq[c] = k for all c present, 0 for absent chars.
 *
 * Consider the "normalized frequency difference" approach:
 * Define diff[c] = prefixFreq[pos][c] - prefixFreq[pos][firstPresentChar]
 * But the "first present char" changes...
 *
 * Alternative: Use the approach where we fix the number of distinct characters.
 * For each possible number of distinct chars d (1 to 26), find the longest substring
 * with exactly d distinct chars where all have equal frequency.
 *
 * For fixed d, a substring [l,r] is valid iff:
 * - exactly d distinct chars
 * - all have equal frequency = (r-l+1)/d
 * - (r-l+1) is divisible by d
 *
 * This still seems hard to do efficiently.
 *
 * PRACTICAL APPROACH for this problem:
 * Since n ≤ 100,000 and we need O(n^2) or better, let's implement O(n^2) but
 * optimize it. For n=100,000, O(n^2) might TLE in competitive programming,
 * but the problem says "O(n^2) or better" so O(n^2) is acceptable per the spec.
 *
 * Actually wait, re-reading: "You must solve this in O(n²) or better time complexity."
 * So O(n^2) is fine!
 *
 * Let's implement the clean O(n^2) solution with the maxFreq * numDistinct == length trick.
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Solution for "Longest Subarray With Equal Symbol Frequencies"
 *
 * <p>Core idea: A substring is valid if and only if every distinct character
 * in it appears the same number of times. We use the observation that if
 * all {@code numDistinct} characters each appear {@code maxFreq} times, then
 * {@code maxFreq * numDistinct == substringLength}. This lets us check validity
 * in O(1) per extension step, giving an overall O(n²) algorithm.</p>
 */
public class Solution {

    /**
     * Finds the length of the longest contiguous substring where every
     * distinct character appears the same number of times.
     *
     * <p>Algorithm overview:
     * <ol>
     *   <li>For each starting index {@code l}, reset frequency tracking.</li>
     *   <li>Extend the right boundary {@code r} one character at a time.</li>
     *   <li>Maintain: {@code freq[c]}, {@code numDistinct}, {@code maxFreq}.</li>
     *   <li>Check validity: {@code maxFreq * numDistinct == (r - l + 1)}.</li>
     *   <li>Update the global answer whenever valid.</li>
     * </ol>
     * </p>
     *
     * @param s the input string of lowercase English letters
     * @return the length of the longest valid substring
     *
     * @implNote Time complexity:  O(n²) — two nested loops, O(1) work per step.
     *           Space complexity: O(1) — only a fixed-size array of 26 integers.
     */
    public int longestEqualFrequencySubstring(String s) {
        int n = s.length();
        int answer = 0; // Will hold the best (longest) valid substring length found so far.

        // ── Outer loop: try every possible LEFT boundary ──────────────────────
        for (int l = 0; l < n; l++) {

            // Reset per-character frequency counts for this new left boundary.
            // We use an array of size 26 (one slot per lowercase letter).
            int[] freq = new int[26];

            // numDistinct: how many distinct characters are currently in [l, r].
            int numDistinct = 0;

            // maxFreq: the highest frequency any single character has reached in [l, r].
            // Note: we never need to decrease maxFreq as we extend right, because
            // a lower maxFreq would only be possible if we removed characters —
            // but we only add characters here. If maxFreq is "stale" (too high),
            // the validity check maxFreq * numDistinct == length will simply fail,
            // which is correct behavior (the substring wouldn't be valid anyway
            // with a stale maxFreq, since the actual max would be lower and the
            // check would use the wrong value).
            //
            // WAIT — actually we DO need the true maxFreq. Let me reconsider.
            // When we add a character, its frequency increases by 1. The new maxFreq
            // is max(old maxFreq, new freq of that character). Since we only add,
            // maxFreq is non-decreasing. So we can simply do:
            //   maxFreq = max(maxFreq, freq[c])
            // and it will always be correct (it's the true maximum).
            int maxFreq = 0;

            // ── Inner loop: extend RIGHT boundary from l to n-1 ───────────────
            for (int r = l; r < n; r++) {

                // Step 1: Identify the current character and its 0-based index.
                int c = s.charAt(r) - 'a'; // e.g., 'a'→0, 'b'→1, ..., 'z'→25

                // Step 2: If this character was absent before (freq == 0),