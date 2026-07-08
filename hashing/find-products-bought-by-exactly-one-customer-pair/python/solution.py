"""
Title: Find Products Bought by Exactly One Customer Pair

Problem Description:
You are given purchase records from an online store. Each record is a pair
[customerId, productId], meaning that the customer bought that product at least once.
The same customer may appear multiple times with the same product in the input due to
duplicate logs, but for this problem, repeated purchases of the same product by the
same customer should count only once.

A product is called pair-exclusive if it was purchased by exactly two distinct customers,
and no other customer purchased it. Your task is to return all pair-exclusive products
grouped by the customer pair that bought them.

More formally, for every product, consider the set of distinct customers who purchased it.
If the size of that set is exactly 2, let those customers be a and b. Then that product
belongs to the pair (min(a, b), max(a, b)). Build a mapping from each such customer pair
to the list of productIds that are pair-exclusive for that pair.

Return the result as a list of entries in the form [customerA, customerB, sortedProductIds],
sorted first by customerA, then by customerB. Each sortedProductIds list must be in
increasing order.

Constraints:
- 1 <= records.length <= 200000
- 1 <= customerId, productId <= 10^9
- Duplicate records may exist
- The total number of distinct (customerId, productId) pairs is at most 200000
"""

from collections import defaultdict
from typing import DefaultDict, Dict, List, Set, Tuple


class Solution:
    def find_pair_exclusive_products(self, records: List[List[int]]) -> List[List[object]]:
        """
        Find all products purchased by exactly two distinct customers and group them by customer pair.

        Args:
            records: A list of [customerId, productId] purchase records. Duplicate records may exist.

        Returns:
            A list of [customerA, customerB, sortedProductIds], sorted by customerA then customerB.

        Time complexity:
            O(n + d + p log p + k log k), where:
            - n is the number of input records
            - d is the number of distinct (customer, product) pairs
            - p is the total number of pair-exclusive products across all groups
            - k is the number of customer pairs in the result
            In practice, this is efficient for the given constraints.

        Space complexity:
            O(d), where d is the number of distinct (customer, product) pairs / product-to-customer relations stored.
        """
        # Step 1:
        # Build a mapping from each product to the set of DISTINCT customers who bought it.
        #
        # Why a set?
        # - The problem explicitly says duplicate logs can exist.
        # - If the same customer appears multiple times for the same product, it should count only once.
        # - A set automatically removes duplicates for us.
        #
        # Example:
        # records = [[2,104],[1,104],[2,104]]
        # product_to_customers[104] should become {1, 2}, not [2, 1, 2].
        product_to_customers: DefaultDict[int, Set[int]] = defaultdict(set)

        # We read every record once and insert the customer into the set for that product.
        for customer_id, product_id in records:
            product_to_customers[product_id].add(customer_id)

        # Step 2:
        # Now inspect each product's customer set.
        # We only care about products bought by EXACTLY TWO distinct customers.
        #
        # For each such product:
        # - Extract the two customers
        # - Sort them into canonical order (smaller first)
        #   so that pair (1,2) and pair (2,1) are treated as the same pair
        # - Append the product to that pair's list
        pair_to_products: DefaultDict[Tuple[int, int], List[int]] = defaultdict(list)

        for product_id, customers in product_to_customers.items():
            # If a product was bought by exactly two distinct customers,
            # then it is pair-exclusive and should be grouped.
            if len(customers) == 2:
                customer_a, customer_b = sorted(customers)
                pair_to_products[(customer_a, customer_b)].append(product_id)

        # Step 3:
        # Build the final answer in the required format:
        # [customerA, customerB, sortedProductIds]
        #
        # Requirements:
        # - The outer list must be sorted by customerA, then customerB
        # - Each product list must be sorted increasingly
        result: List[List[object]] = []

        # Sorting the dictionary keys gives us pairs in ascending order:
        # first by the first customer id, then by the second customer id.
        for (customer_a, customer_b) in sorted(pair_to_products.keys()):
            # Sort the product ids for this pair as required by the problem.
            sorted_product_ids: List[int] = sorted(pair_to_products[(customer_a, customer_b)])
            result.append([customer_a, customer_b, sorted_product_ids])

        return result


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    records1: List[List[int]] = [
        [1, 101],
        [2, 101],
        [1, 102],
        [3, 102],
        [2, 103],
        [3, 103],
        [2, 104],
        [1, 104],
        [2, 104],
    ]
    result1 = solution.find_pair_exclusive_products(records1)
    print("Example 1 Output:")
    print(result1)
    # Expected:
    # [[1, 2, [101, 104]], [1, 3, [102]], [2, 3, [103]]]

    # Example 2
    records2: List[List[int]] = [
        [5, 200],
        [7, 200],
        [8, 200],
        [5, 201],
        [7, 201],
        [5, 202],
        [5, 202],
        [9, 203],
        [10, 203],
    ]
    result2 = solution.find_pair_exclusive_products(records2)
    print("Example 2 Output:")
    print(result2)
    # Expected:
    # [[5, 7, [201]], [9, 10, [203]]]