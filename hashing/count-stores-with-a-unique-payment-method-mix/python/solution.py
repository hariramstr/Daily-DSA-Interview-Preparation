"""
Title: Count Stores With a Unique Payment Method Mix

Problem Description:
A retail analytics system records, for each store, the payment methods used during a day.
Each store is represented by a list of method names such as "cash", "card", "wallet",
or "gift". The same method may appear multiple times for a store because many customers
can use it, but for this task only the set of distinct methods matters.

Two stores are considered to have the same payment method mix if the set of distinct
method names used at both stores is identical, regardless of order or repetition.
For example, ["cash", "card", "cash"] and ["card", "cash"] represent the same mix.

Given a 2D array methodsUsed where methodsUsed[i] is the list of payment methods
recorded for store i, return the number of stores whose payment method mix is unique
across all stores. In other words, count how many stores belong to a distinct-method
set that appears exactly once.

You should design an efficient solution using hashing. A common approach is to normalize
each store's method list into a canonical representation of its distinct methods, then
count how many times each normalized signature appears.

Constraints:
- 1 <= methodsUsed.length <= 100000
- 0 <= methodsUsed[i].length <= 100
- 1 <= total number of method entries across all stores <= 200000
- Each method name consists of lowercase English letters and has length from 1 to 20
"""

from typing import Dict, List, Tuple


class Solution:
    def _build_signature(self, methods: List[str]) -> Tuple[str, ...]:
        """
        Build a canonical signature for one store's payment methods.

        The signature must ignore:
        1. Repetition of the same method within the store
        2. Original order of methods

        To achieve that, we:
        - Convert the list to a set to keep only distinct method names
        - Sort the distinct names so the representation is always consistent
        - Convert the sorted result to a tuple so it can be used as a dictionary key

        Args:
            methods: List of payment method names recorded for one store.

        Returns:
            A tuple containing the sorted distinct payment methods for that store.

        Time complexity:
            O(k log k), where k is the number of distinct methods in this store.

        Space complexity:
            O(k), for the set and tuple of distinct methods.
        """
        # Step 1: Remove duplicates by converting the list into a set.
        # Example:
        # ["cash", "card", "cash"] -> {"cash", "card"}
        distinct_methods = set(methods)

        # Step 2: Sort the distinct methods so that stores with the same set
        # always get the exact same ordering in their signature.
        # Example:
        # {"cash", "card"} -> ["card", "cash"]
        #
        # This is important because sets are unordered, and dictionary keys
        # must be consistent. Without sorting, two equal sets could produce
        # different representations.
        sorted_methods = sorted(distinct_methods)

        # Step 3: Convert the sorted list into a tuple.
        # Tuples are immutable and hashable, so they can safely be used
        # as keys in a dictionary.
        # Example:
        # ["card", "cash"] -> ("card", "cash")
        return tuple(sorted_methods)

    def count_unique_payment_method_mixes(self, methods_used: List[List[str]]) -> int:
        """
        Count how many stores have a payment method mix that appears exactly once.

        A store's payment method mix is defined by the set of distinct payment methods
        used at that store. Two stores are considered the same if those distinct sets
        are identical.

        The algorithm uses hashing:
        1. Normalize each store's list into a canonical signature
        2. Count how many times each signature appears
        3. Count how many stores belong to signatures with frequency 1

        Args:
            methods_used: A 2D list where methods_used[i] contains the payment methods
                recorded for store i.

        Returns:
            The number of stores whose distinct payment method set appears exactly once.

        Time complexity:
            Let N be the number of stores, and let T be the total number of method entries
            across all stores. Since each store has at most 100 entries, normalization is
            efficient. Overall complexity is O(T + sum(k_i log k_i)), where k_i is the
            number of distinct methods for store i.

        Space complexity:
            O(N * m) in the worst case for storing signatures in the frequency map,
            where m is the average number of distinct methods per store.
        """
        # This dictionary will map:
        #   signature -> number of stores that have this exact distinct-method mix
        #
        # Example:
        # ("card", "cash") -> 2
        # ("wallet",) -> 1
        # () -> 2
        signature_count: Dict[Tuple[str, ...], int] = {}

        # First pass:
        # For every store, compute its normalized signature and count how often
        # that signature appears.
        for methods in methods_used:
            # Build a canonical representation for the current store.
            signature = self._build_signature(methods)

            # Increase the count for this signature in the hash map.
            # dict.get(key, 0) returns 0 if the key does not exist yet.
            signature_count[signature] = signature_count.get(signature, 0) + 1

        # Second pass:
        # We now know how many times each distinct-method mix appears.
        # Count stores whose signature appears exactly once.
        unique_store_count = 0

        for methods in methods_used:
            # Rebuild the same signature for the current store.
            signature = self._build_signature(methods)

            # If this signature occurs exactly once in the entire input,
            # then this store's payment method mix is unique.
            if signature_count[signature] == 1:
                unique_store_count += 1

        return unique_store_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    methods_used_1: List[List[str]] = [
        ["cash", "card", "cash"],
        ["wallet"],
        ["card", "cash"],
        ["gift", "wallet"],
        ["wallet", "gift"],
        ["bank"],
    ]
    result_1 = solution.count_unique_payment_method_mixes(methods_used_1)
    print("Example 1 Output:", result_1)  # Expected: 2

    # Example 2
    methods_used_2: List[List[str]] = [
        ["cash"],
        [],
        ["card", "wallet"],
        ["wallet", "card", "wallet"],
        [],
    ]
    result_2 = solution.count_unique_payment_method_mixes(methods_used_2)
    print("Example 2 Output:", result_2)  # Expected: 1

    # Additional quick sanity check
    methods_used_3: List[List[str]] = [
        ["cash", "cash"],
        ["cash"],
        ["card"],
        ["wallet"],
        ["wallet", "wallet"],
    ]
    result_3 = solution.count_unique_payment_method_mixes(methods_used_3)
    print("Additional Example Output:", result_3)  # Expected: 1