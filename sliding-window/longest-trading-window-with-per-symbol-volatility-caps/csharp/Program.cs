/*
Title: Longest Trading Window With Per-Symbol Volatility Caps

Problem Description:
You are given a time-ordered stream of trade events from a market simulator. Each event is represented by two arrays of equal length: symbols[i] is the stock symbol traded at time i, and prices[i] is the execution price of that trade.

A contiguous window of events is considered valid if, for every symbol that appears inside the window, the difference between the maximum and minimum trade price of that symbol within the window is at most limit.

Your task is to return the length of the longest valid contiguous window.

Unlike simpler sliding-window problems, the validity condition must be checked separately for every symbol currently present in the window. A window may contain many interleaved symbols, and each symbol can appear multiple times. When the window expands or shrinks, the max/min price range for one symbol may change without affecting the others.

Constraints:
- 1 <= n == symbols.length == prices.length <= 2 * 10^5
- 1 <= prices[i] <= 10^9
- 0 <= limit <= 10^9
- symbols[i] is a non-empty string of lowercase English letters
- The sum of all symbol string lengths is at most 2 * 10^5

Examples:
1)
symbols = ["aapl","msft","aapl","aapl","msft","goog"]
prices  = [100,200,103,101,198,250]
limit = 3

A correct per-symbol check shows:
- Window [0..4] is valid:
  aapl -> [100,103,101], range = 3
  msft -> [200,198], range = 2
- Window [0..5] is also valid because goog appears once, so its range is 0.

Therefore, the true longest valid window for this exact input is 6.

2)
symbols = ["x","y","x","z","x","y","x"]
prices  = [10,50,14,8,18,47,13]
limit = 4

One longest valid window is [1..4]:
- y -> [50], range = 0
- x -> [14,18], range = 4
- z -> [8], range = 0

Length = 4, which is correct.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n log n) in the worst case.

    Why:
    - Each trade event is added to the sliding window exactly once.
    - Each trade event is removed from the sliding window exactly once.
    - For each symbol, we maintain:
        1) a min deque
        2) a max deque
      Each index enters and leaves each deque at most one time.
    - We also maintain a global set of "currently violating symbols" using SortedSet<string>,
      where add/remove/check operations are O(log k), with k = number of active violating symbols.

    Space Complexity:
    O(n)

    Why:
    - In the worst case, the active window can contain all events.
    - The per-symbol deques together store each index only a constant number of times.
    - Dictionaries and the violation set also scale with the number of distinct symbols / events.
    */
    public int LongestTradingWindow(string[] symbols, int[] prices, int limit)
    {
        int n = symbols.Length;

        // For each symbol currently seen in the sliding window, we need to know:
        // - the minimum price inside the current window
        // - the maximum price inside the current window
        //
        // A classic way to support "current min/max in a sliding window" is to use monotonic deques.
        //
        // But here the window is not over one single array value stream. Instead, each symbol has its own
        // subsequence of prices interleaved across the global event stream.
        //
        // So we keep one pair of deques PER SYMBOL:
        // - MinDeque: increasing by price, so the front is the current minimum price for that symbol
        // - MaxDeque: decreasing by price, so the front is the current maximum price for that symbol
        //
        // Each deque stores event indices, not just prices.
        // Storing indices is important because when the left side of the global window moves forward,
        // we can detect whether the front element of a symbol's deque has fallen out of the window.
        var states = new Dictionary<string, SymbolState>(StringComparer.Ordinal);

        // We need to know whether the current window is valid.
        // The window is valid if NO symbol has (max - min) > limit.
        //
        // Instead of scanning all symbols every time the window changes (which would be too slow),
        // we maintain a set of only the symbols that are currently violating the rule.
        //
        // If this set is empty, the window is valid.
        // If this set is non-empty, the window is invalid and we must shrink from the left.
        var violatingSymbols = new SortedSet<string>(StringComparer.Ordinal);

        int left = 0;
        int best = 0;

        // Expand the right side of the window one event at a time.
        for (int right = 0; right < n; right++)
        {
            string symbol = symbols[right];
            int price = prices[right];

            // Get or create the per-symbol state object.
            if (!states.TryGetValue(symbol, out var state))
            {
                state = new SymbolState();
                states[symbol] = state;
            }

            // STEP 1: Add the new event at index "right" into this symbol's min deque.
            //
            // The min deque must remain increasing by price.
            // So while the last element has a price >= current price, it can never become the minimum
            // for any future window that also contains the current event, because the current event is:
            // - newer (so it stays in the window at least as long)
            // - smaller or equal in price
            //
            // Therefore, those larger/equal tail elements are useless and can be removed.
            while (state.MinDeque.Count > 0 && prices[state.MinDeque.Last!.Value] >= price)
            {
                state.MinDeque.RemoveLast();
            }
            state.MinDeque.AddLast(right);

            // STEP 2: Add the new event into this symbol's max deque.
            //
            // The max deque must remain decreasing by price.
            // So while the last element has a price <= current price, it can never become the maximum
            // for any future window that also contains the current event, because the current event is:
            // - newer
            // - larger or equal in price
            while (state.MaxDeque.Count > 0 && prices[state.MaxDeque.Last!.Value] <= price)
            {
                state.MaxDeque.RemoveLast();
            }
            state.MaxDeque.AddLast(right);

            // STEP 3: After inserting the new event, recompute whether THIS symbol now violates the limit.
            //
            // Only this symbol's range could have changed due to the insertion.
            UpdateViolationStatus(symbol, state, prices, limit, violatingSymbols);

            // STEP 4: If any symbol violates the limit, the whole window is invalid.
            // We must move "left" forward until all violations disappear.
            while (violatingSymbols.Count > 0)
            {
                string leftSymbol = symbols[left];
                int leftPrice = prices[left];

                // We are removing the event at index "left" from the global window.
                // This only affects the deques of symbols[left].
                var leftState = states[leftSymbol];

                // If the outgoing index is currently at the front of the min deque,
                // it means that event was the minimum candidate and is now leaving the window.
                // So we remove it from the front.
                if (leftState.MinDeque.Count > 0 && leftState.MinDeque.First!.Value == left)
                {
                    leftState.MinDeque.RemoveFirst();
                }

                // Similarly for the max deque.
                if (leftState.MaxDeque.Count > 0 && leftState.MaxDeque.First!.Value == left)
                {
                    leftState.MaxDeque.RemoveFirst();
                }

                // After removing the leftmost event, only leftSymbol's range may have changed.
                // So we update just that symbol's violation status.
                UpdateViolationStatus(leftSymbol, leftState, prices, limit, violatingSymbols);

                // Finally, shrink the window.
                left++;
            }

            // At this point, the window [left..right] is guaranteed valid.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }

    private static void UpdateViolationStatus(
        string symbol,
        SymbolState state,
        int[] prices,
        int limit,
        SortedSet<string> violatingSymbols)
    {
        // If both deques are empty, this symbol no longer appears in the current window.
        // Therefore it cannot violate the rule.
        if (state.MinDeque.Count == 0 || state.MaxDeque.Count == 0)
        {
            violatingSymbols.Remove(symbol);
            return;
        }

        // The front of the min deque is the smallest price currently in the window for this symbol.
        int currentMin = prices[state.MinDeque.First!.Value];

        // The front of the max deque is the largest price currently in the window for this symbol.
        int currentMax = prices[state.MaxDeque.First!.Value];

        // Check the per-symbol volatility cap.
        if ((long)currentMax - currentMin > limit)
        {
            violatingSymbols.Add(symbol);
        }
        else
        {
            violatingSymbols.Remove(symbol);
        }
    }

    private sealed class SymbolState
    {
        public LinkedList<int> MinDeque { get; } = new();
        public LinkedList<int> MaxDeque { get; } = new();
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// Important note:
// The written explanation in the prompt says output 5, but by the stated rule the full window [0..5] is valid,
// because goog appears only once and does not break anything.
// So the correct answer for this exact input is 6.
string[] symbols1 = ["aapl", "msft", "aapl", "aapl", "msft", "goog"];
int[] prices1 = [100, 200, 103, 101, 198, 250];
int limit1 = 3;
int result1 = solution.LongestTradingWindow(symbols1, prices1, limit1);
Console.WriteLine(result1);

// Example 2 from the prompt.
string[] symbols2 = ["x", "y", "x", "z", "x", "y", "x"];
int[] prices2 = [10, 50, 14, 8, 18, 47, 13];
int limit2 = 4;
int result2 = solution.LongestTradingWindow(symbols2, prices2, limit2);
Console.WriteLine(result2);

// Additional small sanity check.
string[] symbols3 = ["a", "a", "a", "b", "b"];
int[] prices3 = [5, 7, 6, 10, 12];
int limit3 = 2;
// Entire array is valid:
// a -> [5,7,6], range 2
// b -> [10,12], range 2
int result3 = solution.LongestTradingWindow(symbols3, prices3, limit3);
Console.WriteLine(result3);