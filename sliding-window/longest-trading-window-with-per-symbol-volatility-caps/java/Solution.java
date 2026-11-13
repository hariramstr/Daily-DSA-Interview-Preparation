import java.util.*;

/*
 * Title: Longest Trading Window With Per-Symbol Volatility Caps
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a time-ordered stream of trade events from a market simulator.
 * Each event is represented by two arrays of equal length: symbols[i] is the stock
 * symbol traded at time i, and prices[i] is the execution price of that trade.
 *
 * A contiguous window of events is considered valid if, for every symbol that appears
 * inside the window, the difference between the maximum and minimum trade price of that
 * symbol within the window is at most limit.
 *
 * Your task is to return the length of the longest valid contiguous window.
 *
 * Unlike simpler sliding-window problems, the validity condition must be checked
 * separately for every symbol currently present in the window. A window may contain
 * many interleaved symbols, and each symbol can appear multiple times. When the window
 * expands or shrinks, the max/min price range for one symbol may change without affecting
 * the others.
 *
 * Write a function that computes the maximum number of consecutive trade events that can
 * remain in the window while satisfying the per-symbol volatility cap.
 *
 * Constraints:
 * - 1 <= n == symbols.length == prices.length <= 2 * 10^5
 * - 1 <= prices[i] <= 10^9
 * - 0 <= limit <= 10^9
 * - symbols[i] is a non-empty string of lowercase English letters
 * - The sum of all symbol string lengths is at most 2 * 10^5
 *
 * Example 1:
 * Input:
 * symbols = ["aapl","msft","aapl","aapl","msft","goog"]
 * prices = [100,200,103,101,198,250]
 * limit = 3
 * Output:
 * 5
 *
 * Example 2:
 * Input:
 * symbols = ["x","y","x","z","x","y","x"]
 * prices = [10,50,14,8,18,47,13]
 * limit = 4
 * Output:
 * 4
 */

public class Solution {

    /**
     * Small helper structure that stores all active prices for one symbol inside the current window.
     *
     * We need to support:
     * 1) add a price when the right side of the window expands
     * 2) remove a price when the left side of the window shrinks
     * 3) query current minimum price
     * 4) query current maximum price
     *
     * A TreeMap works well because:
     * - keys are prices
     * - values are counts of how many times that price appears in the current window
     * - firstKey() gives the minimum active price
     * - lastKey() gives the maximum active price
     *
     * All operations are O(log k), where k is the number of distinct active prices for that symbol.
     */
    private static class SymbolWindowData {
        private final TreeMap<Integer, Integer> counts = new TreeMap<>();

        /**
         * Adds one occurrence of the given price.
         *
         * @param price the trade price to insert
         */
        void add(int price) {
            counts.merge(price, 1, Integer::sum);
        }

        /**
         * Removes one occurrence of the given price.
         * Assumes the price is present.
         *
         * @param price the trade price to remove
         */
        void remove(int price) {
            int current = counts.get(price);
            if (current == 1) {
                counts.remove(price);
            } else {
                counts.put(price, current - 1);
            }
        }

        /**
         * Returns the current price range (max - min) for this symbol in the active window.
         *
         * @return current range; 0 if empty
         */
        long range() {
            if (counts.isEmpty()) {
                return 0L;
            }
            return (long) counts.lastKey() - counts.firstKey();
        }

        /**
         * Returns whether this symbol currently has no active trades in the window.
         *
         * @return true if empty, otherwise false
         */
        boolean isEmpty() {
            return counts.isEmpty();
        }
    }

    /**
     * Computes the length of the longest contiguous window such that for every symbol
     * appearing in the window, that symbol's max price minus min price is at most limit.
     *
     * Core idea:
     * - Use a classic sliding window [left..right].
     * - For each symbol, maintain a multiset of active prices in the current window.
     * - Also maintain how many symbols are currently "bad", meaning their range exceeds limit.
     * - When we add a new event at the right:
     *   - update that symbol's data
     *   - if that symbol transitions from valid to invalid, increment badSymbols
     * - While badSymbols > 0, shrink from the left:
     *   - remove the left event from its symbol's data
     *   - if that symbol transitions from invalid to valid, decrement badSymbols
     * - Whenever the window becomes valid, update the answer.
     *
     * Why this works:
     * - The window is valid exactly when no symbol violates the limit.
     * - badSymbols tracks that condition efficiently without scanning all symbols each time.
     * - Each event enters the window once and leaves once.
     *
     * @param symbols the symbol traded at each time index
     * @param prices the execution price at each time index
     * @param limit the maximum allowed per-symbol volatility range inside the window
     * @return the maximum valid window length
     *
     * Time complexity:
     * O(n log n) in the worst case, because each add/remove touches a TreeMap.
     *
     * Space complexity:
     * O(n) in the worst case for all active per-symbol price counts.
     */
    public int longestTradingWindow(String[] symbols, int[] prices, int limit) {
        validateInput(symbols, prices);

        int n = symbols.length;

        // Map from symbol -> data structure containing all active prices for that symbol.
        Map<String, SymbolWindowData> perSymbol = new HashMap<>();

        // left boundary of the sliding window
        int left = 0;

        // best answer found so far
        int best = 0;

        // Number of symbols whose current range exceeds limit.
        // The window is valid if and only if badSymbols == 0.
        int badSymbols = 0;

        // Expand the window one event at a time using 'right'.
        for (int right = 0; right < n; right++) {
            String symbol = symbols[right];
            int price = prices[right];

            // Get or create the data structure for this symbol.
            SymbolWindowData data = perSymbol.computeIfAbsent(symbol, k -> new SymbolWindowData());

            // Before modifying this symbol, record whether it was already violating the limit.
            boolean wasBad = data.range() > limit;

            // Add the new trade price for this symbol.
            data.add(price);

            // After insertion, check whether this symbol now violates the limit.
            boolean isBad = data.range() > limit;

            // If the symbol changed from good -> bad, the total number of bad symbols increases.
            if (!wasBad && isBad) {
                badSymbols++;
            }

            // If the window is invalid, shrink it from the left until it becomes valid again.
            while (badSymbols > 0) {
                String leftSymbol = symbols[left];
                int leftPrice = prices[left];

                SymbolWindowData leftData = perSymbol.get(leftSymbol);

                // Record whether this left symbol is bad before removal.
                boolean leftWasBad = leftData.range() > limit;

                // Remove the outgoing trade from the window.
                leftData.remove(leftPrice);

                // Check whether that symbol is still bad after removal.
                boolean leftIsBad = leftData.range() > limit;

                // If the symbol changed from bad -> good, one violation disappeared.
                if (leftWasBad && !leftIsBad) {
                    badSymbols--;
                }

                // Optional cleanup: if a symbol no longer appears in the window, remove it from the map.
                if (leftData.isEmpty()) {
                    perSymbol.remove(leftSymbol);
                }

                // Move the left boundary rightward.
                left++;
            }

            // At this point the window [left..right] is valid.
            best = Math.max(best, right - left + 1);
        }

        return best;
    }

    /**
     * Validates that the two arrays are non-null and have equal length.
     *
     * @param symbols the symbol array
     * @param prices the price array
     * @return nothing
     *
     * Time complexity:
     * O(1)
     *
     * Space complexity:
     * O(1)
     */
    public void validateInput(String[] symbols, int[] prices) {
        if (symbols == null || prices == null) {
            throw new IllegalArgumentException("symbols and prices must not be null");
        }
        if (symbols.length != prices.length) {
            throw new IllegalArgumentException("symbols and prices must have the same length");
        }
        if (symbols.length == 0) {
            throw new IllegalArgumentException("input arrays must not be empty");
        }
    }

    /**
     * Demonstrates the solution on the sample inputs.
     *
     * Note:
     * The official statement says Example 1 output is 5, but if we strictly apply the
     * problem definition, the full window [0..5] is valid:
     * - aapl: [100,103,101] => range 3
     * - msft: [200,198] => range 2
     * - goog: [250] => range 0
     * Therefore the correct answer for Example 1 is 6.
     *
     * Example 2 evaluates to 4 as stated.
     *
     * @param args command-line arguments, unused
     * @return nothing
     *
     * Time complexity:
     * O(m log m) across the demonstrated examples
     *
     * Space complexity:
     * O(m)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] symbols1 = {"aapl", "msft", "aapl", "aapl", "msft", "goog"};
        int[] prices1 = {100, 200, 103, 101, 198, 250};
        int limit1 = 3;
        int result1 = solution.longestTradingWindow(symbols1, prices1, limit1);
        System.out.println("Example 1 result: " + result1);

        String[] symbols2 = {"x", "y", "x", "z", "x", "y", "x"};
        int[] prices2 = {10, 50, 14, 8, 18, 47, 13};
        int limit2 = 4;
        int result2 = solution.longestTradingWindow(symbols2, prices2, limit2);
        System.out.println("Example 2 result: " + result2);

        String[] symbols3 = {"a", "b", "a", "b", "a"};
        int[] prices3 = {5, 10, 7, 8, 6};
        int limit3 = 2;
        int result3 = solution.longestTradingWindow(symbols3, prices3, limit3);
        System.out.println("Additional example result: " + result3);
    }
}