"""
Title: Count Customers with a Unique Favorite Product
Difficulty: Easy
Topic: Hashing

Problem Description:
You are given a list of customer purchase preferences. Each element in the list
represents the favorite product chosen by one customer, identified by a string
product code. A product is considered unique if exactly one customer selected it
as their favorite.

Your task is to return the number of customers whose favorite product is unique
across the entire list.

In other words, count how many entries in the array belong to product codes that
appear exactly once. This is a common analytics task when identifying niche
product preferences that are not shared by other customers.

Implement a function that takes an array of strings `favorites` and returns an
integer.

Constraints:
- 1 <= favorites.length <= 100000
- 1 <= favorites[i].length <= 50
- favorites[i] consists of lowercase English letters, digits, and underscores
- The answer should be computed in O(n) time on average using hashing

Example 1:
Input: favorites = ["phone_case", "charger", "phone_case", "notebook", "pen"]
Output: 3
Explanation: "charger", "notebook", and "pen" each appear exactly once, so there
are 3 customers whose favorite product is unique.

Example 2:
Input: favorites = ["mouse", "mouse", "keyboard", "keyboard", "monitor"]
Output: 1
Explanation: Only "monitor" appears once. All other product codes appear more
than once.

A straightforward solution is to use a hash map to count the frequency of each
product code, then scan the array or the frequency map to count how many product
codes have frequency 1. Be careful not to count distinct unique products if the
problem asks for customers; in this case, those values are the same because each
unique product contributes exactly one customer.
"""

from typing import Dict, List


class Solution:
    def count_unique_favorite_customers(self, favorites: List[str]) -> int:
        """
        Count how many customers chose a favorite product code that appears exactly once.

        Args:
            favorites: A list of product codes, where each element represents one
                customer's favorite product.

        Returns:
            The number of customers whose favorite product code is unique in the list.

        Time Complexity:
            O(n) on average, where n is the number of customers, because we perform
            linear passes over the input and use hash map operations that are O(1)
            on average.

        Space Complexity:
            O(k), where k is the number of distinct product codes stored in the
            frequency hash map.
        """
        # This dictionary will store how many times each product code appears.
        # Key   -> product code (string)
        # Value -> frequency count (integer)
        #
        # We use a dictionary (hash map) because:
        # 1. It lets us update counts efficiently.
        # 2. Average-case insertion and lookup are O(1).
        # 3. This matches the problem requirement to solve the task in O(n) time
        #    on average using hashing.
        frequency: Dict[str, int] = {}

        # First pass:
        # Count how many times each favorite product appears in the input list.
        #
        # Example for:
        # ["phone_case", "charger", "phone_case", "notebook", "pen"]
        #
        # After this loop, frequency becomes:
        # {
        #     "phone_case": 2,
        #     "charger": 1,
        #     "notebook": 1,
        #     "pen": 1
        # }
        for product_code in favorites:
            # If the product code has not been seen before, start its count at 0,
            # then add 1.
            #
            # dict.get(product_code, 0) means:
            # - return the current count if product_code exists
            # - otherwise return 0
            frequency[product_code] = frequency.get(product_code, 0) + 1

        # This variable will store the final answer:
        # the number of customers whose favorite product is unique.
        unique_customer_count: int = 0

        # Second pass:
        # Go through the original favorites list again.
        #
        # Why scan the original list instead of only the dictionary?
        # - The problem asks for the number of customers whose favorite is unique.
        # - Each unique product contributes exactly one customer.
        # - Counting entries in the original list whose frequency is 1 is direct
        #   and easy to understand for beginners.
        #
        # For each customer's favorite product:
        # - If its total frequency is exactly 1, then that customer should be counted.
        for product_code in favorites:
            if frequency[product_code] == 1:
                unique_customer_count += 1

        # Return the total number of customers with unique favorite products.
        return unique_customer_count


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # "phone_case" appears 2 times -> not unique
    # "charger" appears 1 time     -> unique
    # "notebook" appears 1 time    -> unique
    # "pen" appears 1 time         -> unique
    # Expected answer: 3
    favorites1: List[str] = ["phone_case", "charger", "phone_case", "notebook", "pen"]
    result1: int = solution.count_unique_favorite_customers(favorites1)
    print("Example 1 Output:", result1)

    # Example 2:
    # "mouse" appears 2 times      -> not unique
    # "keyboard" appears 2 times   -> not unique
    # "monitor" appears 1 time     -> unique
    # Expected answer: 1
    favorites2: List[str] = ["mouse", "mouse", "keyboard", "keyboard", "monitor"]
    result2: int = solution.count_unique_favorite_customers(favorites2)
    print("Example 2 Output:", result2)

    # Additional simple check:
    # Every product appears once, so every customer is counted.
    # Expected answer: 4
    favorites3: List[str] = ["a", "b", "c", "d"]
    result3: int = solution.count_unique_favorite_customers(favorites3)
    print("Additional Example Output:", result3)