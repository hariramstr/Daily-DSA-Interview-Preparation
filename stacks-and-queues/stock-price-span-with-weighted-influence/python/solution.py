```python
"""
Title: Stock Price Span with Weighted Influence
Difficulty: Medium
Topic: Stacks and Queues

Problem Description:
You are given a stream of daily stock prices and a corresponding list of 'influence weights'
for each day. For each day i, define the weighted span as the sum of influence weights of all
consecutive previous days (including day i itself) where the stock price was less than or equal
to the price on day i.

In other words, for day i, find the maximum number of consecutive days ending at i (going
backwards) where every price is <= prices[i], and return the sum of weights for those days.

Implement a class WeightedStockSpan with the following methods:
- WeightedStockSpan() — Initializes the object.
- int next(int price, int weight) — Adds the next price and weight, and returns the weighted
  span for the current day.

Constraints:
- 1 <= price <= 100000
- 1 <= weight <= 1000
- At most 10000 calls will be made to next.
"""

from typing import List, Tuple


class WeightedStockSpan:
    """
    A class to compute the weighted stock span for a stream of daily prices and weights.

    The key insight is to use a monotonic stack that stores (price, cumulative_weight) pairs.
    Instead of storing individual weights, we store the cumulative weight of all days that
    have been "absorbed" into each stack entry. This allows O(1) amortized time per call.

    Why a monotonic stack?
    - We want to efficiently find all consecutive previous days with price <= current price.
    - A monotonic decreasing stack (by price) lets us pop all entries with price <= current,
      accumulating their weights, and then push the current entry with the total accumulated weight.
    - This way, each entry on the stack represents a "barrier" — a day whose price is strictly
      greater than everything that came after it (up to the next barrier).
    """

    def __init__(self) -> None:
        """
        Initializes the WeightedStockSpan object.

        The stack stores tuples of (price, cumulative_weight) where:
        - price: the stock price on that day
        - cumulative_weight: the sum of weights for this day AND all consecutive days
          after it (going forward) that had prices <= this day's price at the time
          they were popped.

        Wait — let me re-think. The stack stores (price, accumulated_weight) where
        accumulated_weight is the sum of weights of all days that this entry "represents"
        (i.e., itself plus all days that were popped when this entry was pushed).

        Time Complexity: O(1)
        Space Complexity: O(1)
        """
        # Stack of tuples: (price, accumulated_weight)
        # accumulated_weight = weight of this day + weights of all days popped when this was pushed
        # The stack is maintained in strictly decreasing order of price (monotonic decreasing stack)
        self.stack: List[Tuple[int, int]] = []

    def next(self, price: int, weight: int) -> int:
        """
        Processes the next day's stock price and weight, returning the weighted span.

        The weighted span for day i is the sum of weights of all consecutive days
        ending at day i (going backwards) where every price <= prices[i].

        Args:
            price (int): The stock price on the current day. (1 <= price <= 100000)
            weight (int): The influence weight for the current day. (1 <= weight <= 1000)

        Returns:
            int: The weighted span — sum of weights of all consecutive days (including
                 the current day) where price <= current day's price.

        Time Complexity: O(1) amortized — each element is pushed and popped at most once
                         across all calls to next().
        Space Complexity: O(n) in the worst case where n is the number of calls, if prices
                          are strictly decreasing (nothing ever gets popped).

        Algorithm (Monotonic Stack):
        1. Start with accumulated_weight = weight (the current day's own weight).
        2. While the stack is not empty AND the top of the stack has price <= current price:
           - Pop the top entry (it means those days are within our span).
           - Add the popped entry's accumulated_weight to our accumulated_weight.
        3. Push (current_price, accumulated_weight) onto the stack.
        4. Return accumulated_weight as the weighted span.

        Why this works:
        - The stack maintains a monotonically decreasing sequence of prices.
        - Each stack entry's accumulated_weight already encodes the total weight of all
          consecutive days it "absorbed" when it was pushed.
        - When we pop entries with price <= current price, we're collecting all the weights
          of consecutive days that are within the current span.
        - The remaining stack entries (not popped) have prices > current price, meaning
          they form a barrier — the span cannot extend past them.
        """

        # -----------------------------------------------------------------------
        # Step 1: Initialize accumulated weight with the current day's own weight.
        # This is the minimum possible weighted span (just the current day itself).
        # -----------------------------------------------------------------------
        accumulated_weight: int = weight

        # -----------------------------------------------------------------------
        # Step 2: Pop all stack entries with price <= current price.
        #
        # Why? Because those days are part of our consecutive span going backwards.
        # Each popped entry's accumulated_weight already represents the total weight
        # of a group of consecutive days that were previously merged together.
        #
        # Example: If the stack has [(100, 1), (80, 9)] and current price is 85:
        # - Top is (80, 9): 80 <= 85, so pop it and add 9 to accumulated_weight.
        # - Top is (100, 1): 100 > 85, so stop.
        # - accumulated_weight = weight + 9
        # This correctly captures all days with price <= 85 going backwards.
        # -----------------------------------------------------------------------
        while self.stack and self.stack[-1][0] <= price:
            # Pop the top entry from the stack
            popped_price, popped_weight = self.stack.pop()

            # Add the popped entry's accumulated weight to our running total.
            # This popped entry represents one or more consecutive days that all
            # had prices <= current price (they were merged when they were pushed).
            accumulated_weight += popped_weight

        # -----------------------------------------------------------------------
        # Step 3: Push the current day onto the stack.
        #
        # We push (price, accumulated_weight) where accumulated_weight is the total
        # weight of the current day PLUS all days that were popped (absorbed).
        #
        # Why store accumulated_weight instead of just weight?
        # Because future days with higher prices will pop this entry and need to
        # know the total weight of all days this entry represents, not just one day.
        #
        # This is the key optimization: by storing the merged weight, we avoid
        # re-traversing already-processed days.
        # -----------------------------------------------------------------------
        self.stack.append((price, accumulated_weight))

        # -----------------------------------------------------------------------
        # Step 4: Return the accumulated weight as the weighted span for today.
        #
        # accumulated_weight = current day's weight + weights of all consecutive
        # previous days with price <= current price.
        # -----------------------------------------------------------------------
        return accumulated_weight


class Solution:
    """
    Solution class that demonstrates the WeightedStockSpan algorithm.
    """

    def compute_weighted_spans(
        self, prices: List[int], weights: List[int]
    ) -> List[int]:
        """
        Computes the weighted span for each day given a list of prices and weights.

        Args:
            prices (List[int]): List of daily stock prices.
            weights (List[int]): List of daily influence weights.

        Returns:
            List[int]: List of weighted spans for each day.

        Time Complexity: O(n) amortized, where n = len(prices).
                         Each element is pushed and popped from the stack at most once.
        Space Complexity: O(n) for the stack in the worst case.
        """
        # Validate that prices and weights have the same length
        if len(prices) != len(weights):
            raise ValueError("prices and weights must have the same length")

        # Create a fresh WeightedStockSpan object for this computation
        wss = WeightedStockSpan()

        # Process each (price, weight) pair and collect results
        results: List[int] = []
        for price, weight in zip(prices, weights):
            # Call next() for each day and store the weighted span
            span = wss.next(price, weight)
            results.append(span)

        return results


# -------------------------------------------------------------------------------
# Manual Trace / Verification
# -------------------------------------------------------------------------------
# Let's trace through Example 1 to verify correctness:
# calls = [[100,1],[80,2],[60,3],[70,4],[60,5],[75,6],[85,7]]
# Expected: [1, 2, 3, 7, 5, 18, 28]
#
# Day 0: price=100, weight=1
#   accumulated_weight = 1
#   Stack is empty, no pops.
#   Push (100, 1). Stack: [(100, 1)]
#   Return 1 ✓
#
# Day 1: price=80, weight=2
#   accumulated_weight = 2
#   Top is (100, 1): 100 > 80, no pop.
#   Push (80, 2). Stack: [(100, 1), (80, 2)]
#   Return 2 ✓
#
# Day 2: price=60, weight=3
#   accumulated_weight = 3
#   Top is (80, 2): 80 > 60, no pop.
#   Push (60, 3). Stack: [(100, 1), (80, 2), (60, 3)]
#   Return 3 ✓
#
# Day 3: price=70, weight=4
#   accumulated_weight = 4
#   Top is (60, 3): 60 <= 70, pop! accumulated_weight = 4 + 3 = 7
#   Top is (80, 2): 80 > 70, stop.
#   Push (70, 7). Stack: [(100, 1), (80, 2), (70, 7)]
#   Return 7 ✓
#
# Day 4: price=60, weight=5
#   accumulated_weight = 5
#   Top is (70, 7): 70 > 60, no pop.
#   Push (60, 5). Stack: [(100, 1), (80, 2), (70, 7), (60, 5)]
#   Return 5 ✓
#
# Day 5: price=75, weight=6
#   accumulated_weight = 6
#   Top is (60, 5): 60 <= 75, pop! accumulated_weight = 6 + 5 = 11
#   Top is (70, 7): 70 <= 75, pop! accumulated_weight = 11 + 7 = 18
#   Top is (80, 2): 80 > 75, stop.
#   Push (75, 18). Stack: [(100, 1), (80, 2), (75, 18)]
#   Return 18 ✓
#
# Day 6: price=85, weight=7
#   accumulated_weight = 7
#   Top is (75, 18): 75 <= 85, pop! accumulated_weight = 7 + 18 = 25
#   Top is (80, 2): 80 <= 85, pop! accumulated_weight = 25 + 2 = 27... hmm
#   Wait, expected is 28. Let me recheck.
#   Top is (100, 1): 100 > 85, stop.
#   Push (85, 27). Stack: [(100, 1), (85, 27)]
#   Return 27... but expected is 28?
#
# Let me re-read the problem explanation:
# "Day 6: price=85, weight=7 → days 1..6 → weighted span = 2+3+4+5+6+7=27"
# The problem says "27... wait days 1–6: 2+3+4+5+6+7=27, plus day 6 itself already counted → 28"
# Hmm, that's confusing. Let me recount:
# Days 1-6 weights: day1=2, day2=3, day3=4, day4=5, day5=6, day6=7
# Sum = 2+3+4+5+6+7 = 27
# But the expected output says 28. Let me check if day 0 (price=100) is included.
# Day 0: price=100 > 85, so NOT included.
# So the span should be days 1-6: 2+3+4+5+6+7 = 27.
#
# But the expected output is 28. Let me re-examine...
# Actually wait, let me recount: 2+3+4+5+6+7 = 27, not 28.
# The problem statement itself seems confused ("27... wait... 28").
# Let me verify with Example 2 which is cleaner:
# [[50,5],[50,3],[50,2]] → [5, 8, 10]
# Day 0: 5 ✓
# Day 1: 5+3=8 ✓
# Day 2: 5+3+2=10 ✓
#
# For Example 1, let me recount day 6 manually:
# Prices: [100, 80, 60, 70, 60, 75, 85]
# Weights:[  1,  2,  3,  4,  5,  6,  7]
# Day 6 price=85. Going backwards:
# Day 6: 85 <= 85 ✓ weight=7
# Day 5: 75 <= 85 ✓ weight=6
# Day 4: 60 <= 85 ✓ weight=5
# Day 3: 70 <= 85 ✓ weight=4
# Day 2: 60 <= 85 ✓ weight=3
# Day 1: 80 <= 85 ✓ weight=2
# Day 0: 100 > 85 ✗ STOP
# Total = 7+6+5+4+3+2 = 27
#
# So the correct answer for day 6 is 27, not 28.
# The problem statement has a typo/error. Our algorithm produces 27 which is correct.
# We'll use 27 in our test and note the discrepancy.
# -------------------------------------------------------------------------------


if __name__ == "__main__":
    # Create the Solution object
    solution = Solution()

    print("=" * 60)
    print("WeightedStockSpan - Stock Price Span with Weighted Influence")
    print("=" * 60)

    # -----------------------------------------------------------------------
    # Example 1 from the problem
    # -----------------------------------------------------------------------
    print("\n--- Example 1 ---")
    prices1 = [100, 80, 60, 70, 60, 75, 85]
    weights1 = [1, 2, 3, 4, 5, 6, 7]

    # Note: The problem states expected output as [1, 2, 3, 7, 5, 18, 28]
    # but based on manual calculation, day 6 should be 27 (not 28).
    # Days 1-6 weights: 2+3+4+5+6+7 = 27 (day 0 has price 100 > 85, excluded)
    # Our algorithm correctly computes 27.
    expected1 = [1, 2, 3, 7, 5, 18, 27]  # Corrected from problem's 28 to 27

    result1 = solution.compute_weighted_spans(prices1, weights1)

    print(f"Prices:   {prices1}")
    print(f"Weights:  {weights1}")
    print(f"Result:   {result1}")
    print(f"Expected: {expected1}")
    print(f"Match:    {result1 == expected1}")

    # Show step-by-step breakdown
    print("\nStep-by-step breakdown:")
    wss1