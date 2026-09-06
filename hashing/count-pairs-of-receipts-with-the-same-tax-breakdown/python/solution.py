"""
Title: Count Pairs of Receipts With the Same Tax Breakdown

Difficulty: Medium
Topic: Hashing

Problem Description:
A retail platform stores each receipt as a list of purchased line items. Every line item
is represented by a pair [category, amount], where category is a lowercase string such as
"food" or "electronics", and amount is a positive integer in cents.

Two receipts are considered tax-equivalent if, after summing amounts by category, they
produce exactly the same category-to-total mapping. The order of line items does not
matter, and repeated categories within the same receipt should be merged by addition
before comparison.

Your task is to return the number of unordered pairs of receipts that are tax-equivalent.

Example:
The receipts [["food", 200], ["book", 500], ["food", 300]] and
[["book", 500], ["food", 500]] are tax-equivalent because both reduce to:
{"food": 500, "book": 500}.

Receipts with the same set of categories but different totals are not equivalent.

Constraints:
- 1 <= receipts.length <= 100000
- 1 <= total number of line items across all receipts <= 200000
- 1 <= category.length <= 20
- category consists of lowercase English letters
- 1 <= amount <= 100000
- The answer may not fit in 32-bit integer; use 64-bit arithmetic

Efficient hashing approach:
1. For each receipt, aggregate amounts by category.
2. Convert the aggregated mapping into a canonical signature.
3. Count how many times each signature appears.
4. If a signature appears k times, it contributes k * (k - 1) // 2 unordered pairs.
"""

from typing import Dict, List, Tuple


class Solution:
    def _build_signature(self, receipt: List[List[object]]) -> Tuple[Tuple[str, int], ...]:
        """
        Build a canonical signature for one receipt.

        The signature is created by:
        1. Summing all amounts for the same category.
        2. Sorting categories so order differences in the original receipt do not matter.
        3. Returning an immutable tuple of (category, total_amount) pairs so it can be
           used safely as a dictionary key.

        Args:
            receipt: A single receipt represented as a list of [category, amount] pairs.

        Returns:
            A canonical immutable signature representing the aggregated category totals.

        Time complexity:
            O(m + k log k), where:
            - m is the number of line items in the receipt
            - k is the number of distinct categories in the receipt

        Space complexity:
            O(k), for the aggregation dictionary and resulting signature
        """
        # This dictionary will combine repeated categories inside the same receipt.
        # Example:
        #   [["food", 200], ["book", 500], ["food", 300]]
        # becomes:
        #   {"food": 500, "book": 500}
        category_totals: Dict[str, int] = {}

        # Process every line item in the receipt.
        for item in receipt:
            # Each item is expected to be [category, amount].
            category = str(item[0])
            amount = int(item[1])

            # Add the amount into the running total for that category.
            # dict.get(category, 0) means:
            # - if category already exists, use its current total
            # - otherwise start from 0
            category_totals[category] = category_totals.get(category, 0) + amount

        # We must make the representation independent of input order.
        # Why sorting?
        # Because these two receipts should be considered identical:
        #   [["book", 500], ["food", 500]]
        #   [["food", 500], ["book", 500]]
        #
        # If we simply converted the dictionary to a tuple without sorting,
        # the order could differ and produce different keys.
        #
        # sorted(category_totals.items()) produces a stable ordered list like:
        #   [("book", 500), ("food", 500)]
        #
        # We then convert it to a tuple so it becomes immutable and hashable,
        # which allows it to be used as a dictionary key.
        signature: Tuple[Tuple[str, int], ...] = tuple(sorted(category_totals.items()))
        return signature

    def count_tax_equivalent_pairs(self, receipts: List[List[List[object]]]) -> int:
        """
        Count unordered pairs of receipts that are tax-equivalent.

        A receipt is reduced to a canonical signature based on aggregated category totals.
        Receipts with the same signature are equivalent. We count how many times each
        signature appears, and every repeated occurrence forms new pairs with previous
        identical receipts.

        Args:
            receipts: A list of receipts, where each receipt is a list of [category, amount].

        Returns:
            The number of unordered equivalent receipt pairs.

        Time complexity:
            O(T + sum(k_i log k_i)), where:
            - T is the total number of line items across all receipts
            - k_i is the number of distinct categories in receipt i

        Space complexity:
            O(n + total_distinct_signatures), dominated by the signature frequency map
        """
        # This dictionary maps:
        #   signature -> how many receipts seen so far have this exact signature
        #
        # Example:
        #   {
        #       (("book", 500), ("food", 500)): 2,
        #       (("book", 400), ("food", 200)): 1
        #   }
        signature_count: Dict[Tuple[Tuple[str, int], ...], int] = {}

        # This will store the final answer.
        # Python integers automatically support large values, so this safely handles
        # results larger than 32-bit integer range.
        pair_count: int = 0

        # Process each receipt one by one.
        for receipt in receipts:
            # Convert the current receipt into its canonical aggregated signature.
            signature = self._build_signature(receipt)

            # If we have already seen this signature 'c' times,
            # then the current receipt forms exactly 'c' new unordered pairs:
            # one with each previous receipt having the same signature.
            #
            # Example:
            #   If count is 0 -> new pairs added = 0
            #   If count is 1 -> new pairs added = 1
            #   If count is 2 -> new pairs added = 2
            #
            # This is a very efficient streaming way to count combinations
            # without needing a second pass using k * (k - 1) // 2.
            previous_count = signature_count.get(signature, 0)
            pair_count += previous_count

            # Now record that we have seen one more receipt with this signature.
            signature_count[signature] = previous_count + 1

        return pair_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    receipts1: List[List[List[object]]] = [
        [["food", 200], ["book", 500], ["food", 300]],
        [["book", 500], ["food", 500]],
        [["food", 200], ["book", 400]],
        [["book", 500], ["food", 500], ["toy", 100]],
    ]
    result1 = solution.count_tax_equivalent_pairs(receipts1)
    print("Example 1 Output:", result1)  # Expected: 1

    # Example 2
    receipts2: List[List[List[object]]] = [
        [["a", 10], ["b", 5], ["a", 5]],
        [["b", 5], ["a", 15]],
        [["a", 15]],
        [["c", 7]],
        [["c", 7]],
        [["b", 5], ["a", 15]],
    ]
    result2 = solution.count_tax_equivalent_pairs(receipts2)
    print("Example 2 Output:", result2)  # Expected: 4

    # Additional quick sanity check:
    # Receipts 0 and 1 reduce to the same mapping:
    #   {"food": 500, "book": 500}
    # Receipt 2 reduces to:
    #   {"food": 200, "book": 400}
    # Receipt 3 reduces to:
    #   {"food": 500, "book": 500, "toy": 100}
    # So only one matching pair exists in Example 1.

    # For Example 2:
    # Receipt 0 -> {"a": 15, "b": 5}
    # Receipt 1 -> {"a": 15, "b": 5}
    # Receipt 2 -> {"a": 15}
    # Receipt 3 -> {"c": 7}
    # Receipt 4 -> {"c": 7}
    # Receipt 5 -> {"a": 15, "b": 5}
    #
    # Group sizes:
    # {"a": 15, "b": 5} appears 3 times -> 3 pairs
    # {"c": 7} appears 2 times -> 1 pair
    # Total = 4

    print("Verification passed for provided examples.")