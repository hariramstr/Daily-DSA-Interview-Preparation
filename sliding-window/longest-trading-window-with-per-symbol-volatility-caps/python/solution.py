"""
Title: Longest Trading Window With Per-Symbol Volatility Caps

Problem Description:
You are given a time-ordered stream of trade events from a market simulator.
Each event is represented by two arrays of equal length: symbols[i] is the stock
symbol traded at time i, and prices[i] is the execution price of that trade.

A contiguous window of events is considered valid if, for every symbol that
appears inside the window, the difference between the maximum and minimum trade
price of that symbol within the window is at most limit.

Your task is to return the length of the longest valid contiguous window.

Unlike simpler sliding-window problems, the validity condition must be checked
separately for every symbol currently present in the window. A window may contain
many interleaved symbols, and each symbol can appear multiple times. When the
window expands or shrinks, the max/min price range for one symbol may change
without affecting the others.

Constraints:
- 1 <= n == symbols.length == prices.length <= 2 * 10^5
- 1 <= prices[i] <= 10^9
- 0 <= limit <= 10^9
- symbols[i] is a non-empty string of lowercase English letters
- The sum of all symbol string lengths is at most 2 * 10^5
"""

from collections import defaultdict, deque
from typing import Deque, DefaultDict, Dict, List, Tuple


class Solution:
    def longest_trading_window(self, symbols: List[str], prices: List[int], limit: int) -> int:
        """
        Compute the maximum length of a contiguous valid trading window.

        A window is valid when, for every symbol currently present in the window,
        that symbol's maximum price minus minimum price inside the window is at most limit.

        Args:
            symbols: List of traded stock symbols in time order.
            prices: List of trade prices aligned with symbols.
            limit: Maximum allowed per-symbol price range inside the window.

        Returns:
            The length of the longest valid contiguous window.

        Time Complexity:
            O(n), where n is the number of trade events.
            Each event is inserted into and removed from its symbol's deques at most once.

        Space Complexity:
            O(n) in the worst case for the per-symbol deques across the active window.
        """
        n: int = len(symbols)

        # For each symbol, we maintain two monotonic deques:
        #
        # 1) min_deques[symbol]
        #    - Stores pairs (index, price)
        #    - Prices are kept in increasing order
        #    - The front always contains the minimum price for that symbol
        #      among events currently inside the sliding window
        #
        # 2) max_deques[symbol]
        #    - Stores pairs (index, price)
        #    - Prices are kept in decreasing order
        #    - The front always contains the maximum price for that symbol
        #      among events currently inside the sliding window
        #
        # Why deques?
        # We need fast:
        # - insertion of a new event on the right side of the window
        # - deletion of expired events from the left side of the window
        # - query of current min and max for each symbol
        #
        # Monotonic deques give all of these in amortized O(1) time.
        min_deques: DefaultDict[str, Deque[Tuple[int, int]]] = defaultdict(deque)
        max_deques: DefaultDict[str, Deque[Tuple[int, int]]] = defaultdict(deque)

        # This dictionary tracks whether a symbol is currently violating the rule:
        # current max - current min > limit
        #
        # We do not need to scan all symbols every time the window changes.
        # Instead, we only update the status of the symbol that changed.
        violating: Dict[str, bool] = {}

        # Number of symbols currently violating the limit.
        # The whole window is valid exactly when this count is zero.
        bad_count: int = 0

        # Standard sliding window left pointer.
        left: int = 0

        # Best answer found so far.
        best: int = 0

        def update_symbol_status(symbol: str) -> None:
            """
            Recompute whether one symbol currently violates the limit and update bad_count.

            Args:
                symbol: The symbol whose min/max deques may have changed.

            Returns:
                None

            Time Complexity:
                O(1)

            Space Complexity:
                O(1)
            """
            nonlocal bad_count

            # If a symbol has no active events in the window, it cannot violate anything.
            if not min_deques[symbol]:
                was_bad: bool = violating.get(symbol, False)
                if was_bad:
                    bad_count -= 1
                violating[symbol] = False
                return

            current_min: int = min_deques[symbol][0][1]
            current_max: int = max_deques[symbol][0][1]
            is_bad: bool = (current_max - current_min) > limit
            was_bad = violating.get(symbol, False)

            # Adjust the global count only if the status changed.
            if was_bad != is_bad:
                if is_bad:
                    bad_count += 1
                else:
                    bad_count -= 1

            violating[symbol] = is_bad

        # Expand the window one event at a time using the right pointer.
        for right in range(n):
            symbol: str = symbols[right]
            price: int = prices[right]

            # -----------------------------
            # Step 1: Add the new event into the symbol's min deque.
            # -----------------------------
            #
            # We want increasing prices in min_deques[symbol].
            # So while the last price is >= the new price, it can never become the minimum
            # for any future window that also contains the new event.
            while min_deques[symbol] and min_deques[symbol][-1][1] >= price:
                min_deques[symbol].pop()
            min_deques[symbol].append((right, price))

            # -----------------------------
            # Step 2: Add the new event into the symbol's max deque.
            # -----------------------------
            #
            # We want decreasing prices in max_deques[symbol].
            # So while the last price is <= the new price, it can never become the maximum
            # for any future window that also contains the new event.
            while max_deques[symbol] and max_deques[symbol][-1][1] <= price:
                max_deques[symbol].pop()
            max_deques[symbol].append((right, price))

            # -----------------------------
            # Step 3: Recompute only this symbol's violation status.
            # -----------------------------
            #
            # Only this symbol changed due to the insertion at index right.
            update_symbol_status(symbol)

            # -----------------------------
            # Step 4: Shrink from the left until the entire window becomes valid.
            # -----------------------------
            #
            # The window is invalid if at least one symbol violates the limit.
            # bad_count > 0 means "some symbol currently has max - min > limit".
            while bad_count > 0:
                left_symbol: str = symbols[left]
                left_price: int = prices[left]

                # Remove the leftmost event from that symbol's min deque if it is sitting at the front.
                #
                # Why only the front?
                # Because deques store candidates in window order. If the expired event is not at the front,
                # it was already dominated by a better candidate and is irrelevant.
                if min_deques[left_symbol] and min_deques[left_symbol][0][0] == left:
                    min_deques[left_symbol].popleft()

                # Remove the leftmost event from that symbol's max deque if it is sitting at the front.
                if max_deques[left_symbol] and max_deques[left_symbol][0][0] == left:
                    max_deques[left_symbol].popleft()

                # After removing index left, only left_symbol may have changed its min/max range.
                update_symbol_status(left_symbol)

                # Move the left boundary rightward by one.
                left += 1

            # -----------------------------
            # Step 5: The current window [left..right] is valid.
            # Update the best answer.
            # -----------------------------
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    #
    # Important note:
    # By the stated rule, the full window [0..5] is actually valid:
    # - aapl: [100, 103, 101] -> range 3
    # - msft: [200, 198] -> range 2
    # - goog: [250] -> range 0
    #
    # Therefore the correct answer under the written problem statement is 6, not 5.
    symbols1: List[str] = ["aapl", "msft", "aapl", "aapl", "msft", "goog"]
    prices1: List[int] = [100, 200, 103, 101, 198, 250]
    limit1: int = 3
    result1: int = solution.longest_trading_window(symbols1, prices1, limit1)
    print("Example 1 result:", result1)

    # Example 2 from the prompt.
    # One valid longest window is [1..4] with length 4.
    symbols2: List[str] = ["x", "y", "x", "z", "x", "y", "x"]
    prices2: List[int] = [10, 50, 14, 8, 18, 47, 13]
    limit2: int = 4
    result2: int = solution.longest_trading_window(symbols2, prices2, limit2)
    print("Example 2 result:", result2)

    # Additional quick sanity checks.
    symbols3: List[str] = ["a", "a", "a"]
    prices3: List[int] = [5, 5, 5]
    limit3: int = 0
    result3: int = solution.longest_trading_window(symbols3, prices3, limit3)
    print("All same prices result:", result3)

    symbols4: List[str] = ["a", "b", "a", "b"]
    prices4: List[int] = [1, 100, 10, 90]
    limit4: int = 5
    result4: int = solution.longest_trading_window(symbols4, prices4, limit4)
    print("Mixed symbols result:", result4)