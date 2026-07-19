"""
Title: Count Products With a Unique Reviewer Set

Problem Description:
An e-commerce platform stores product reviews as pairs of integers [productId, userId].
A product may be reviewed multiple times by the same user due to edits or updated ratings,
but for this problem only the set of distinct users who reviewed each product matters.

Two products are considered equivalent if they were reviewed by exactly the same set of users,
regardless of review order or duplicate entries.

Task:
Return the number of products whose reviewer set is unique among all products that appear
in the input.

In other words:
1. Build the distinct reviewer set for every product.
2. Group products by these reviewer sets.
3. Count how many products belong to a group of size 1.

Important notes:
- A product is considered only if it appears in at least one review record.
- Duplicate review records for the same (productId, userId) pair do not change the reviewer set.

Constraints:
- 1 <= reviews.length <= 2 * 10^5
- 1 <= productId, userId <= 10^9
- Each review is a pair [productId, userId]
- The answer should be computed in near-linear time relative to the number of review records
"""

from collections import defaultdict
from typing import DefaultDict, Dict, FrozenSet, List, Set, Tuple


class Solution:
    def count_unique_reviewer_set_products(self, reviews: List[List[int]]) -> int:
        """
        Count how many products have a reviewer set that no other product shares.

        The method first builds the set of distinct reviewers for each product.
        Then it groups products by their reviewer sets and counts how many products
        belong to reviewer-set groups of size exactly 1.

        Args:
            reviews: A list of [productId, userId] pairs.

        Returns:
            The number of products whose reviewer set is unique among all products.

        Time Complexity:
            O(n + total_unique_reviewers_across_products)
            In practice this is near-linear in the number of review records.

        Space Complexity:
            O(total_unique_reviewers_across_products + number_of_products)
        """
        # This dictionary maps:
        #   productId -> set of distinct userIds who reviewed that product
        #
        # Why use a set?
        # Because the problem explicitly says duplicate reviews by the same user
        # for the same product should count only once.
        #
        # Example:
        #   reviews contains [103, 3] twice
        # Then product 103 should still have reviewer set {3}, not {3, 3}.
        product_to_reviewers: DefaultDict[int, Set[int]] = defaultdict(set)

        # Step 1:
        # Build the distinct reviewer set for each product.
        #
        # We iterate through every review record exactly once.
        # For each [productId, userId]:
        #   - find the set for that product
        #   - add the userId into the set
        #
        # Because sets automatically ignore duplicates, repeated identical pairs
        # do not affect correctness.
        for product_id, user_id in reviews:
            product_to_reviewers[product_id].add(user_id)

        # This dictionary maps:
        #   frozen reviewer set -> number of products having exactly that reviewer set
        #
        # Why use frozenset instead of set?
        # Normal set is mutable and therefore cannot be used as a dictionary key.
        # frozenset is immutable and hashable, so it can be used safely as a key.
        reviewer_set_frequency: Dict[FrozenSet[int], int] = defaultdict(int)

        # Step 2:
        # Convert each product's reviewer set into a frozenset and count how many
        # products share that exact reviewer set.
        #
        # Example:
        #   product 101 -> {1, 2}
        #   product 102 -> {1, 2}
        # Both become frozenset({1, 2}), so the frequency for that key becomes 2.
        for reviewers in product_to_reviewers.values():
            reviewer_signature: FrozenSet[int] = frozenset(reviewers)
            reviewer_set_frequency[reviewer_signature] += 1

        # Step 3:
        # Count products whose reviewer-set group size is exactly 1.
        #
        # We again inspect each product's reviewer set.
        # If the frequency of that set is 1, then this product is the only product
        # with that reviewer set, so it should be counted.
        unique_products_count = 0

        for reviewers in product_to_reviewers.values():
            reviewer_signature = frozenset(reviewers)
            if reviewer_set_frequency[reviewer_signature] == 1:
                unique_products_count += 1

        return unique_products_count

    def solve(self, reviews: List[List[int]]) -> int:
        """
        Wrapper method for the main algorithm.

        Args:
            reviews: A list of [productId, userId] pairs.

        Returns:
            The number of products with a unique reviewer set.

        Time Complexity:
            O(n + total_unique_reviewers_across_products)

        Space Complexity:
            O(total_unique_reviewers_across_products + number_of_products)
        """
        return self.count_unique_reviewer_set_products(reviews)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt
    # Product 101 -> {1, 2}
    # Product 102 -> {1, 2}
    # Product 103 -> {3}
    # Product 104 -> {4}
    #
    # Unique reviewer sets belong to:
    #   103 -> {3}
    #   104 -> {4}
    # So the answer is 2.
    reviews1: List[List[int]] = [
        [101, 1],
        [101, 2],
        [102, 2],
        [102, 1],
        [103, 3],
        [103, 3],
        [104, 4],
    ]
    result1 = solution.solve(reviews1)
    print(result1)  # Expected: 2

    # Example 2 from the prompt
    # Product 10 -> {7, 8}
    # Product 11 -> {7}
    # Product 12 -> {8}
    # Product 13 -> {9, 10}
    # Product 14 -> {9, 10}
    #
    # Unique reviewer sets belong to:
    #   10 -> {7, 8}
    #   11 -> {7}
    #   12 -> {8}
    # Products 13 and 14 share the same set, so they are not unique.
    # Therefore the correct answer is 3.
    #
    # Note:
    # The prompt's "Output: 2" conflicts with its own explanation and data.
    # The explanation clearly supports 3, and this algorithm correctly returns 3.
    reviews2: List[List[int]] = [
        [10, 7],
        [10, 8],
        [11, 7],
        [12, 8],
        [12, 8],
        [13, 9],
        [13, 10],
        [14, 9],
        [14, 10],
    ]
    result2 = solution.solve(reviews2)
    print(result2)  # Expected: 3

    # Additional quick sanity check
    # All products share the same reviewer set {1}
    # So none are unique.
    reviews3: List[List[int]] = [
        [1, 1],
        [2, 1],
        [3, 1],
    ]
    result3 = solution.solve(reviews3)
    print(result3)  # Expected: 0