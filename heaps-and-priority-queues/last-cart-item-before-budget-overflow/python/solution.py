"""
Title: Last Cart Item Before Budget Overflow

Problem Description:
You are building a shopping app that processes item prices one by one in the order
they were scanned. A customer has a fixed budget, but the app is allowed to remove
previously scanned expensive items if that helps keep the current cart affordable.

After each new item is scanned, the cart should contain as many of the scanned items
as possible while keeping the total cost less than or equal to the budget. If removing
items is necessary, always remove the most expensive item currently in the cart first.

Your task is to return the number of items left in the cart after all prices have been
processed.

This problem is naturally solved with a heap or priority queue: keep track of the items
currently in the cart, and when the total cost exceeds the budget, repeatedly remove the
highest-priced item until the cart is valid again.

Constraints:
- 1 <= prices.length <= 100000
- 1 <= prices[i] <= 1000000000
- 1 <= budget <= 1000000000
- The answer fits in a 32-bit integer.

Example 1:
Input: prices = [4, 2, 7, 1, 3], budget = 10
Output: 3

Explanation:
Scan 4, 2, 7, 1, 3 in order.
After scanning 7, total becomes 13, so remove the most expensive item 7.
The cart becomes [4, 2].
Then add 1 -> total 7, add 3 -> total 10.
Final cart size is 3.

Example 2:
Input: prices = [8, 5, 2, 6], budget = 9
Output: 2

Explanation:
Add 8 -> total 8.
Add 5 -> total 13, remove 8, cart becomes [5].
Add 2 -> total 7.
Add 6 -> total 13, remove 6, cart remains [5, 2].
Final cart size is 2.
"""

from typing import List
import heapq


class Solution:
    def last_cart_item_before_budget_overflow(self, prices: List[int], budget: int) -> int:
        """
        Return the number of items remaining in the cart after processing all prices.

        The method scans prices from left to right. Every scanned item is first added
        to the cart. If the total cost becomes larger than the budget, the method
        repeatedly removes the most expensive item currently in the cart until the
        total cost is valid again.

        A max-heap behavior is needed to quickly remove the largest price. Since
        Python's heapq implements a min-heap, negative values are stored so that the
        smallest negative value corresponds to the largest original price.

        Args:
            prices: A list of scanned item prices in the order they are processed.
            budget: The maximum allowed total cost of the cart.

        Returns:
            The number of items left in the cart after all prices are processed.

        Time complexity:
            O(n log n), where n is the number of prices. Each item is pushed once,
            and each removed item is popped once from the heap.

        Space complexity:
            O(n) in the worst case for the heap storing cart items.
        """
        # This heap will represent the items currently kept in the cart.
        #
        # Important detail:
        # Python's built-in heapq is a min-heap, meaning it gives us the smallest
        # value quickly. However, this problem requires removing the MOST expensive
        # item whenever the budget is exceeded.
        #
        # To simulate a max-heap using heapq, we store negative prices:
        # - price 7 is stored as -7
        # - price 3 is stored as -3
        #
        # Then the "smallest" negative number is the most negative one, which
        # corresponds to the largest original price.
        max_heap: List[int] = []

        # This variable keeps track of the total cost of all items currently
        # in the cart. Maintaining this running sum lets us check the budget
        # in O(1) time after each insertion or removal.
        current_total: int = 0

        # Process each scanned price in the exact order given.
        # The order matters because the app sees items one by one as they are scanned.
        for price in prices:
            # Step 1: Add the newly scanned item into the cart.
            #
            # We always try to include the current item first, because the problem
            # says that after each scan, the cart should contain as many scanned
            # items as possible while staying within budget.
            #
            # By adding first and fixing later, we naturally model the process:
            # "include the item, then if needed remove the most expensive items."
            current_total += price
            heapq.heappush(max_heap, -price)

            # Step 2: If the cart is now too expensive, remove items until it fits.
            #
            # Why a while-loop instead of an if-statement?
            # Because in general, one removal may not be enough if the budget is
            # exceeded by a large amount. We must keep removing the most expensive
            # item until current_total <= budget.
            while current_total > budget:
                # Pop the most expensive item from the cart.
                #
                # Since values are stored as negatives:
                # popped value might be -7, meaning the real price is 7.
                most_expensive = -heapq.heappop(max_heap)

                # Subtract that removed item's price from the running total.
                current_total -= most_expensive

                # After this removal, the loop checks again whether the cart
                # is still over budget. If yes, we remove the next most expensive item.

        # At the end, the heap contains exactly the items still in the cart.
        # Therefore, the number of remaining items is simply the heap size.
        return len(max_heap)

    def solve(self, prices: List[int], budget: int) -> int:
        """
        Convenience wrapper that calls the main algorithm.

        Args:
            prices: A list of scanned item prices.
            budget: The maximum allowed total cart cost.

        Returns:
            The number of items left in the cart.

        Time complexity:
            O(n log n), where n is the number of prices.

        Space complexity:
            O(n) in the worst case.
        """
        return self.last_cart_item_before_budget_overflow(prices, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # prices = [4, 2, 7, 1, 3], budget = 10
    #
    # Manual trace:
    # Add 4  -> total = 4, cart = [4]
    # Add 2  -> total = 6, cart = [4, 2]
    # Add 7  -> total = 13, over budget
    # Remove most expensive 7 -> total = 6, cart = [4, 2]
    # Add 1  -> total = 7, cart = [4, 2, 1]
    # Add 3  -> total = 10, cart = [4, 2, 1, 3]
    #
    # Wait carefully:
    # The final cart here contains 4 items with total 10, so the correct count
    # under the stated algorithm is 4.
    #
    # The provided example text says 3, but tracing the described process gives 4.
    # We must follow the problem's algorithmic rule correctly.
    prices1 = [4, 2, 7, 1, 3]
    budget1 = 10
    result1 = solution.solve(prices1, budget1)
    print("Example 1 result:", result1)

    # Example 2:
    # prices = [8, 5, 2, 6], budget = 9
    #
    # Manual trace:
    # Add 8 -> total = 8, cart = [8]
    # Add 5 -> total = 13, remove 8 -> total = 5, cart = [5]
    # Add 2 -> total = 7, cart = [5, 2]
    # Add 6 -> total = 13, remove 6 -> total = 7, cart = [5, 2]
    # Final count = 2
    prices2 = [8, 5, 2, 6]
    budget2 = 9
    result2 = solution.solve(prices2, budget2)
    print("Example 2 result:", result2)

    # Additional simple test
    prices3 = [10]
    budget3 = 5
    result3 = solution.solve(prices3, budget3)
    print("Additional test result:", result3)