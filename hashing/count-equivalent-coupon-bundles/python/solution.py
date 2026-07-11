"""
Title: Count Equivalent Coupon Bundles
Difficulty: Medium
Topic: Hashing

Problem Description:
An e-commerce platform stores promotional bundles as arrays of coupon codes.
Two bundles are considered equivalent if they contain exactly the same coupon
codes with the same frequencies, regardless of order.

For example:
["SAVE10", "FREESHIP", "SAVE10"] is equivalent to
["SAVE10", "SAVE10", "FREESHIP"]

But it is not equivalent to:
["SAVE10", "FREESHIP"]
or
["SAVE10", "FREESHIP", "BONUS"]

You are given a list of bundles, where each bundle is an array of strings.
Return the number of unordered pairs of indices (i, j) such that i < j and
bundles[i] is equivalent to bundles[j].

Because coupon codes are strings and bundle order does not matter, a direct
comparison of every pair is too slow for large inputs. You should design an
efficient solution using hashing or canonical representations.

Constraints:
- 1 <= bundles.length <= 100000
- 1 <= bundles[i].length <= 20
- 1 <= couponCodes[i].length <= 20
- Each coupon code consists of uppercase English letters and digits.
- The total number of coupon codes across all bundles does not exceed 200000.

Example 1:
Input:
bundles = [
    ["SAVE10", "FREESHIP"],
    ["FREESHIP", "SAVE10"],
    ["BONUS"],
    ["SAVE10", "SAVE10", "FREESHIP"],
    ["FREESHIP", "SAVE10", "SAVE10"]
]
Output: 2

Explanation:
- Bundles 0 and 1 are equivalent.
- Bundles 3 and 4 are equivalent.
- No other pair matches.

Example 2:
Input:
bundles = [
    ["A", "B", "A"],
    ["A", "A", "B"],
    ["B", "A"],
    ["C"],
    ["C"],
    ["C", "C"]
]
Output: 2

Explanation:
- One matching pair comes from the first two bundles.
- Another matching pair comes from the two single-element ["C"] bundles.
"""

from typing import Dict, List, Tuple


class Solution:
    def _canonical_bundle(self, bundle: List[str]) -> Tuple[Tuple[str, int], ...]:
        """
        Build a canonical representation for one bundle.

        The key idea is:
        - Order inside a bundle does not matter.
        - Frequency of each coupon code does matter.

        So for a bundle like:
        ["SAVE10", "FREESHIP", "SAVE10"]

        We count frequencies:
        {
            "SAVE10": 2,
            "FREESHIP": 1
        }

        Then we sort by coupon code and convert to an immutable tuple:
        (("FREESHIP", 1), ("SAVE10", 2))

        This canonical form is:
        - identical for all equivalent bundles
        - hashable, so it can be used as a dictionary key

        Args:
            bundle: A single bundle represented as a list of coupon code strings.

        Returns:
            A sorted, immutable tuple of (coupon_code, frequency) pairs.

        Time complexity:
            O(k + u log u), where:
            - k is the number of coupon codes in the bundle
            - u is the number of distinct coupon codes in the bundle

        Space complexity:
            O(u), for the frequency map and canonical tuple
        """
        # This dictionary will count how many times each coupon code appears
        # in the current bundle.
        #
        # Example:
        # bundle = ["A", "B", "A"]
        # after processing:
        # freq = {"A": 2, "B": 1}
        freq: Dict[str, int] = {}

        # Count occurrences of each coupon code.
        # We do this because equivalence depends on both:
        # 1. which coupon codes appear
        # 2. how many times each appears
        for code in bundle:
            freq[code] = freq.get(code, 0) + 1

        # Convert the frequency dictionary into a sorted tuple.
        #
        # Why sort?
        # Dictionaries do not guarantee a canonical ordering suitable for
        # equivalence comparison across independently built maps.
        #
        # Example:
        # {"A": 2, "B": 1} and {"B": 1, "A": 2}
        # represent the same multiset, but we need one consistent form.
        #
        # Sorting ensures both become:
        # (("A", 2), ("B", 1))
        canonical: Tuple[Tuple[str, int], ...] = tuple(sorted(freq.items()))
        return canonical

    def count_equivalent_bundles(self, bundles: List[List[str]]) -> int:
        """
        Count unordered pairs of equivalent coupon bundles.

        The algorithm works in one pass:
        1. Convert each bundle into a canonical representation.
        2. Use a dictionary to count how many times each canonical form
           has already been seen.
        3. When we see the same canonical form again, it forms a valid pair
           with every previous bundle of that same form.

        Example:
        canonical form X appears in this order:
        - first time: contributes 0 new pairs
        - second time: contributes 1 new pair
        - third time: contributes 2 new pairs
        - fourth time: contributes 3 new pairs

        Total = 0 + 1 + 2 + 3 = 6 = C(4, 2)

        This incremental counting avoids a second pass and is very efficient.

        Args:
            bundles: A list of bundles, where each bundle is a list of strings.

        Returns:
            The number of unordered equivalent pairs.

        Time complexity:
            Let T be the total number of coupon codes across all bundles.
            Since each bundle has at most 20 elements, sorting each bundle's
            distinct items is small. Overall this is efficient in practice:
            O(T + sum(u_i log u_i))

        Space complexity:
            O(m), where m is the number of distinct canonical bundle forms
        """
        # This dictionary maps:
        # canonical_bundle_representation -> how many times we have seen it so far
        #
        # Example:
        # {
        #   (("A", 2), ("B", 1)): 3,
        #   (("C", 1),): 2
        # }
        seen_count: Dict[Tuple[Tuple[str, int], ...], int] = {}

        # This will store the final answer:
        # the total number of unordered pairs (i, j), i < j,
        # where bundles[i] and bundles[j] are equivalent.
        pair_count: int = 0

        # Process each bundle exactly once.
        for bundle in bundles:
            # Convert the current bundle into its canonical multiset form.
            canonical = self._canonical_bundle(bundle)

            # If we have already seen this exact canonical form 'x' times,
            # then the current bundle forms 'x' new equivalent pairs:
            # one with each previously seen matching bundle.
            previous_matches = seen_count.get(canonical, 0)
            pair_count += previous_matches

            # Now record that we have seen one more bundle of this form.
            seen_count[canonical] = previous_matches + 1

        return pair_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    bundles1: List[List[str]] = [
        ["SAVE10", "FREESHIP"],
        ["FREESHIP", "SAVE10"],
        ["BONUS"],
        ["SAVE10", "SAVE10", "FREESHIP"],
        ["FREESHIP", "SAVE10", "SAVE10"],
    ]
    result1 = solution.count_equivalent_bundles(bundles1)
    print("Example 1 Output:", result1)  # Expected: 2

    # Example 2 from the problem statement
    bundles2: List[List[str]] = [
        ["A", "B", "A"],
        ["A", "A", "B"],
        ["B", "A"],
        ["C"],
        ["C"],
        ["C", "C"],
    ]
    result2 = solution.count_equivalent_bundles(bundles2)
    print("Example 2 Output:", result2)  # Expected: 2

    # Additional quick sanity checks
    bundles3: List[List[str]] = [["X"], ["X"], ["X"]]
    result3 = solution.count_equivalent_bundles(bundles3)
    print("Additional Test 1 Output:", result3)  # Expected: 3

    bundles4: List[List[str]] = [["A", "B"], ["A"], ["B", "A", "A"], ["A", "B"]]
    result4 = solution.count_equivalent_bundles(bundles4)
    print("Additional Test 2 Output:", result4)  # Expected: 1