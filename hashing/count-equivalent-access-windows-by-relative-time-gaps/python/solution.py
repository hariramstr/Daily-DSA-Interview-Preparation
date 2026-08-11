"""
Title: Count Equivalent Access Windows by Relative Time Gaps
Difficulty: Hard
Topic: Hashing

Problem Description:
You are given an integer array timestamps representing event times in nondecreasing order,
and an integer k. Consider every contiguous window of exactly k timestamps. Two windows are
considered equivalent if their internal pattern of time gaps is identical. In other words,
for a window [t[i], t[i+1], ..., t[i+k-1]], define its gap signature as
[t[i+1]-t[i], t[i+2]-t[i+1], ..., t[i+k-1]-t[i+k-2]]. Two length-k windows are equivalent
if these gap signatures are exactly the same element by element, even if the absolute
starting times differ.

Return the number of unordered pairs of distinct windows that are equivalent.

Because timestamps can be very large and the number of windows can be large, a solution that
compares every pair directly will time out. You are expected to design an efficient
hashing-based approach that groups windows by signature. Be careful with repeated timestamps,
large values, and the special case k = 1. When k = 1, every window has an empty gap
signature, so all single-element windows are equivalent.

Constraints:
- 1 <= timestamps.length <= 200000
- 1 <= k <= timestamps.length
- 0 <= timestamps[i] <= 10^18
- timestamps is sorted in nondecreasing order

Example 1:
Input: timestamps = [2, 5, 9, 12, 15, 19], k = 3
Output: 1

Explanation:
Length-3 windows are:
[2,5,9] -> gaps [3,4]
[5,9,12] -> gaps [4,3]
[9,12,15] -> gaps [3,3]
[12,15,19] -> gaps [3,4]
Only the 1st and 4th windows share the same gap signature, so the answer is 1 pair.

Example 2:
Input: timestamps = [7, 7, 10, 13, 13, 16], k = 2
Output: 4

Explanation:
Length-2 windows are:
[7,7] -> gaps [0]
[7,10] -> gaps [3]
[10,13] -> gaps [3]
[13,13] -> gaps [0]
[13,16] -> gaps [3]
There are two windows with signature [0], contributing 1 pair, and three windows with
signature [3], contributing 3 pairs. Total = 4.
"""

from typing import Dict, List, Tuple


class Solution:
    def _build_gaps(self, timestamps: List[int]) -> List[int]:
        """
        Build the array of consecutive differences between adjacent timestamps.

        Args:
            timestamps: Sorted list of event times.

        Returns:
            A list where gaps[i] = timestamps[i + 1] - timestamps[i].

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        return [timestamps[i + 1] - timestamps[i] for i in range(len(timestamps) - 1)]

    def count_equivalent_windows(self, timestamps: List[int], k: int) -> int:
        """
        Count unordered pairs of length-k contiguous windows that have identical gap signatures.

        The key observation is:
        - A length-k window is fully described by its k-1 consecutive gaps.
        - Therefore, instead of comparing timestamp windows directly, we compare subarrays
          of the gaps array of length k-1.
        - We use a rolling hash so each signature can be processed in O(1) amortized time
          after preprocessing.

        To make collisions negligibly unlikely, we use double hashing:
        - Two different moduli
        - One shared base
        - The pair of hash values is used as the dictionary key

        Args:
            timestamps: Sorted list of event times in nondecreasing order.
            k: Exact window size.

        Returns:
            The number of unordered pairs of distinct equivalent windows.

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        n: int = len(timestamps)

        # Special case: if k == 1, every single timestamp forms a window with an empty
        # gap signature. Since all empty signatures are identical, every pair of windows
        # matches. If there are n windows, the number of unordered pairs is n choose 2.
        if k == 1:
            return n * (n - 1) // 2

        # If k > 1, each window of size k corresponds to a subarray of length k - 1
        # inside the gaps array.
        gaps: List[int] = self._build_gaps(timestamps)

        # Number of windows of size k in the original timestamps array.
        window_count: int = n - k + 1

        # Length of each signature inside the gaps array.
        signature_length: int = k - 1

        # We use polynomial rolling hash over the gaps array.
        #
        # Why hashing?
        # Directly storing each signature as a tuple of length k-1 would work logically,
        # but in the worst case it would cost too much memory and time because each
        # window could require copying a large tuple.
        #
        # Rolling hash lets us:
        # 1. Precompute prefix hashes
        # 2. Extract each signature hash in O(1)
        # 3. Group equal signatures efficiently in a dictionary
        #
        # Since gap values can be as large as 10^18, we reduce them modulo each modulus
        # before inserting into the polynomial hash.
        base: int = 911382323
        mod1: int = 1_000_000_007
        mod2: int = 1_000_000_009

        m: int = len(gaps)

        # Precompute powers of the base:
        # power1[i] = base^i mod mod1
        # power2[i] = base^i mod mod2
        #
        # These are needed to extract subarray hashes from prefix hashes.
        power1: List[int] = [1] * (m + 1)
        power2: List[int] = [1] * (m + 1)
        for i in range(1, m + 1):
            power1[i] = (power1[i - 1] * base) % mod1
            power2[i] = (power2[i - 1] * base) % mod2

        # Prefix hashes:
        # prefix1[i] stores the hash of gaps[0:i]
        # prefix2[i] stores the hash of gaps[0:i]
        #
        # Standard polynomial construction:
        # prefix_next = prefix_current * base + value
        prefix1: List[int] = [0] * (m + 1)
        prefix2: List[int] = [0] * (m + 1)

        for i, gap in enumerate(gaps):
            value1: int = gap % mod1
            value2: int = gap % mod2
            prefix1[i + 1] = (prefix1[i] * base + value1) % mod1
            prefix2[i + 1] = (prefix2[i] * base + value2) % mod2

        # Dictionary that counts how many times each signature hash has appeared.
        #
        # Key:
        #   (hash under mod1, hash under mod2)
        # Value:
        #   frequency of that signature among processed windows
        freq: Dict[Tuple[int, int], int] = {}

        # This will accumulate the number of unordered matching pairs.
        #
        # Important counting trick:
        # When we see a signature for the x-th time, it forms exactly x-1 new pairs
        # with all previous identical signatures.
        #
        # Example:
        # frequencies encountered: 1st, 2nd, 3rd occurrence
        # new pairs added:         0,   1,   2
        # total = 3 = C(3, 2)
        answer: int = 0

        # Iterate over every possible window in the original timestamps array.
        #
        # A timestamp window starting at index s corresponds to the gaps subarray:
        # gaps[s : s + signature_length]
        #
        # There are window_count such windows.
        for start in range(window_count):
            end: int = start + signature_length

            # Extract hash of gaps[start:end] in O(1) using prefix hashes.
            #
            # For polynomial rolling hash:
            # hash(l, r) = prefix[r] - prefix[l] * base^(r-l)
            # Then normalize modulo mod.
            hash1: int = (prefix1[end] - prefix1[start] * power1[end - start]) % mod1
            hash2: int = (prefix2[end] - prefix2[start] * power2[end - start]) % mod2
            key: Tuple[int, int] = (hash1, hash2)

            # If this signature has already appeared c times, then the current window
            # forms c new unordered pairs with those previous windows.
            previous_count: int = freq.get(key, 0)
            answer += previous_count

            # Record that we have now seen this signature one more time.
            freq[key] = previous_count + 1

        return answer


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the statement:
    # Windows of size 3:
    # [2,5,9]   -> gaps [3,4]
    # [5,9,12]  -> gaps [4,3]
    # [9,12,15] -> gaps [3,3]
    # [12,15,19]-> gaps [3,4]
    # Matching signatures: [3,4] appears twice -> 1 pair
    timestamps1: List[int] = [2, 5, 9, 12, 15, 19]
    k1: int = 3
    result1: int = solution.count_equivalent_windows(timestamps1, k1)
    print("Example 1 Result:", result1)  # Expected: 1

    # Example 2 from the statement:
    # Windows of size 2:
    # [7,7]   -> [0]
    # [7,10]  -> [3]
    # [10,13] -> [3]
    # [13,13] -> [0]
    # [13,16] -> [3]
    # [0] appears twice -> 1 pair
    # [3] appears three times -> 3 pairs
    # Total = 4
    timestamps2: List[int] = [7, 7, 10, 13, 13, 16]
    k2: int = 2
    result2: int = solution.count_equivalent_windows(timestamps2, k2)
    print("Example 2 Result:", result2)  # Expected: 4

    # Additional check for k = 1:
    # Every single-element window has empty signature, so all windows are equivalent.
    # For 4 timestamps, answer = C(4,2) = 6
    timestamps3: List[int] = [1, 10, 10, 100]
    k3: int = 1
    result3: int = solution.count_equivalent_windows(timestamps3, k3)
    print("k = 1 Result:", result3)  # Expected: 6