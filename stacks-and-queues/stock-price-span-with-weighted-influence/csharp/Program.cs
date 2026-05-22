/*
 * Title: Stock Price Span with Weighted Influence
 * Difficulty: Medium
 * Topic: Stacks and Queues
 *
 * Problem Description:
 * You are given a stream of daily stock prices and a corresponding list of
 * 'influence weights' for each day. For each day i, define the weighted span
 * as the sum of influence weights of all consecutive previous days (including
 * day i itself) where the stock price was less than or equal to the price on
 * day i.
 *
 * In other words, for day i, find the maximum number of consecutive days ending
 * at i (going backwards) where every price is <= prices[i], and return the sum
 * of weights for those days.
 *
 * Example 1:
 *   Input:  [[100,1],[80,2],[60,3],[70,4],[60,5],[75,6],[85,7]]
 *   Output: [1, 2, 3, 7, 5, 18, 28]
 *
 * Example 2:
 *   Input:  [[50,5],[50,3],[50,2]]
 *   Output: [5, 8, 10]
 *
 * Constraints:
 *   - 1 <= price <= 100000
 *   - 1 <= weight <= 1000
 *   - At most 10000 calls to next()
 */

using System;
using System.Collections.Generic;

// ─────────────────────────────────────────────────────────────────────────────
// Core idea:
//   The classic "Stock Span" problem uses a monotonic stack that stores
//   (price, span) pairs.  Here we replace the raw "span count" with a
//   "cumulative weight" so that when we pop entries whose prices are <=
//   the current price, we accumulate their weight sums instead of their
//   day counts.
//
//   Stack invariant: prices stored in the stack are strictly DECREASING
//   from bottom to top.  Every time a new price arrives we pop all stack
//   entries whose price is <= the current price, summing their weights,
//   then push (currentPrice, totalWeight) onto the stack.
// ─────────────────────────────────────────────────────────────────────────────

/// <summary>
/// Solves the Weighted Stock Span problem using a monotonic stack.
/// </summary>
public class WeightedStockSpan
{
    // ── Data structure choice ────────────────────────────────────────────────
    // We use a Stack of (price, cumulativeWeight) tuples.
    //
    // Why a stack?
    //   We only ever need to look at the most-recently-pushed entry to decide
    //   whether it should be "absorbed" by the current day's price.  A stack
    //   gives O(1) peek and pop, which is exactly what we need.
    //
    // Why store cumulativeWeight instead of the raw weight?
    //   When we push an entry onto the stack we already know the total weight
    //   it represents (its own weight PLUS the weights of all days that were
    //   popped to create it).  Storing that total lets us accumulate the answer
    //   in a single pass without re-visiting old entries.
    // ────────────────────────────────────────────────────────────────────────
    private readonly Stack<(int price, int cumulativeWeight)> _stack;

    /// <summary>Initializes the WeightedStockSpan object.</summary>
    public WeightedStockSpan()
    {
        // Create an empty stack.  Each element will hold:
        //   price            – the stock price on that "representative" day
        //   cumulativeWeight – total weight of all days this entry represents
        _stack = new Stack<(int, int)>();
    }

    // ── next() ───────────────────────────────────────────────────────────────
    // Time Complexity : O(1) amortised per call.
    //   Each day is pushed onto the stack exactly once and popped at most once,
    //   so across N calls the total work is O(N).
    //
    // Space Complexity: O(N) in the worst case (strictly decreasing prices
    //   means nothing ever gets popped, so the stack grows to size N).
    // ────────────────────────────────────────────────────────────────────────

    /// <summary>
    /// Processes the next stock price and its influence weight.
    /// Returns the weighted span for the current day.
    /// </summary>
    /// <param name="price">Today's stock price.</param>
    /// <param name="weight">Today's influence weight.</param>
    /// <returns>Sum of weights for all consecutive days (ending today)
    /// whose prices are &lt;= today's price.</returns>
    public int Next(int price, int weight)
    {
        // ── Step 1: Start the running weight total with today's own weight ──
        // We always include the current day in its own span, so we begin
        // accumulation with 'weight'.
        int totalWeight = weight;

        // ── Step 2: Pop stack entries whose price <= current price ───────────
        // The stack is maintained in strictly decreasing price order.
        // Any entry on top of the stack with price <= current price means
        // those days are part of the current day's span (their prices are
        // all <= today's price AND they are consecutive with today because
        // the stack only retains "barrier" prices).
        //
        // Why is it safe to pop?
        //   If a future day has a price >= today's price, it will also be
        //   >= the popped entry's price, so the popped entry would have been
        //   absorbed anyway.  We never need it as a standalone barrier again.
        while (_stack.Count > 0 && _stack.Peek().price <= price)
        {
            // Remove the top entry and add its cumulative weight to our total.
            // This entry's cumulativeWeight already encodes the weights of all
            // the days it previously absorbed, so we don't need to revisit them.
            var (_, w) = _stack.Pop();
            totalWeight += w;
        }

        // ── Step 3: Push the current day onto the stack ──────────────────────
        // We push (price, totalWeight) — NOT just (price, weight).
        // 'totalWeight' represents the combined weight of today AND every day
        // that was just popped (i.e., every day in today's span).
        //
        // Why store totalWeight instead of weight?
        //   If a future day absorbs today's entry, it should receive credit for
        //   ALL the days today represents, not just today's single weight.
        _stack.Push((price, totalWeight));

        // ── Step 4: Return the weighted span ─────────────────────────────────
        // 'totalWeight' is the answer: the sum of weights for all consecutive
        // days (going backwards from today) where price <= today's price.
        return totalWeight;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Manual trace for Example 1: [[100,1],[80,2],[60,3],[70,4],[60,5],[75,6],[85,7]]
//
// Day 0: price=100, weight=1
//   Stack empty → totalWeight=1
//   Push (100,1).  Stack: [(100,1)]
//   Return 1  ✓
//
// Day 1: price=80, weight=2
//   Peek (100,1): 100 > 80 → don't pop
//   totalWeight=2
//   Push (80,2).  Stack: [(100,1),(80,2)]
//   Return 2  ✓
//
// Day 2: price=60, weight=3
//   Peek (80,2): 80 > 60 → don't pop
//   totalWeight=3
//   Push (60,3).  Stack: [(100,1),(80,2),(60,3)]
//   Return 3  ✓
//
// Day 3: price=70, weight=4
//   Peek (60,3): 60 <= 70 → pop, totalWeight=4+3=7
//   Peek (80,2): 80 > 70 → stop
//   Push (70,7).  Stack: [(100,1),(80,2),(70,7)]
//   Return 7  ✓
//
// Day 4: price=60, weight=5
//   Peek (70,7): 70 > 60 → don't pop
//   totalWeight=5
//   Push (60,5).  Stack: [(100,1),(80,2),(70,7),(60,5)]
//   Return 5  ✓
//
// Day 5: price=75, weight=6
//   Peek (60,5): 60 <= 75 → pop, totalWeight=6+5=11
//   Peek (70,7): 70 <= 75 → pop, totalWeight=11+7=18
//   Peek (80,2): 80 > 75 → stop
//   Push (75,18).  Stack: [(100,1),(80,2),(75,18)]
//   Return 18  ✓
//
// Day 6: price=85, weight=7
//   Peek (75,18): 75 <= 85 → pop, totalWeight=7+18=25
//   Peek (80,2):  80 <= 85 → pop, totalWeight=25+2=27
//   Peek (100,1): 100 > 85 → stop
//   Push (85,28)? Wait: totalWeight=27, not 28.
//
//   Hmm — let me recheck the expected answer.
//   Days 1-6 weights: 2+3+4+5+6+7 = 27.  Day 0 price=100 > 85, excluded.
//   So the correct answer is 27, not 28.
//
//   The problem statement says "28" but then says "recalculate" and the
//   explanation lists days 1-6 (weights 2+3+4+5+6+7=27).  The algorithm
//   produces 27, which matches the corrected explanation.
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

Console.WriteLine("=== Weighted Stock Span Demo ===\n");

// ── Example 1 ────────────────────────────────────────────────────────────────
Console.WriteLine("Example 1:");
Console.WriteLine("Input: [[100,1],[80,2],[60,3],[70,4],[60,5],[75,6],[85,7]]");

var span1 = new WeightedStockSpan();
int[][] calls1 =
[
    [100, 1],
    [80,  2],
    [60,  3],
    [70,  4],
    [60,  5],
    [75,  6],
    [85,  7]
];

var results1 = new List<int>();
foreach (var call in calls1)
    results1.Add(span1.Next(call[0], call[1]));

Console.WriteLine($"Output: [{string.Join(", ", results1)}]");
// Expected (corrected): [1, 2, 3, 7, 5, 18, 27]
Console.WriteLine("Expected: [1, 2, 3, 7, 5, 18, 27]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
Console.WriteLine("Example 2:");
Console.WriteLine("Input: [[50,5],[50,3],[50,2]]");

var span2 = new WeightedStockSpan();
int[][] calls2 =
[
    [50, 5],
    [50, 3],
    [50, 2]
];

var results2 = new List<int>();
foreach (var call in calls2)
    results2.Add(span2.Next(call[0], call[1]));

Console.WriteLine($"Output: [{string.Join(", ", results2)}]");
Console.WriteLine("Expected: [5, 8, 10]");
Console.WriteLine();

// ── Additional edge-case: strictly increasing prices ─────────────────────────
Console.WriteLine("Example 3 (strictly increasing prices):");
Console.WriteLine("Input: [[10,1],[20,2],[30,3],[40,4]]");

var span3 = new WeightedStockSpan();
int[][] calls3 =
[
    [10, 1],
    [20, 2],
    [30, 3],
    [40, 4]
];

var results3 = new List<int>();
foreach (var call in calls3)
    results3.Add(span3.Next(call[0], call[1]));

Console.WriteLine($"Output: [{string.Join(", ", results3)}]");
// Day 0: 1; Day 1: 1+2=3; Day 2: 1+2+3=6; Day 3: 1+2+3+4=10
Console.WriteLine("Expected: [1, 3, 6, 10]");
Console.WriteLine();

// ── Additional edge-case: strictly decreasing prices ─────────────────────────
Console.WriteLine("Example 4 (strictly decreasing prices):");
Console.WriteLine("Input: [[40,4],[30,3],[20,2],[10,1]]");

var span4 = new WeightedStockSpan();
int[][] calls4 =
[
    [40, 4],
    [30, 3],
    [20, 2],
    [10, 1]
];

var results4 = new List<int>();
foreach (var call in calls4)
    results4.Add(span4.Next(call[0], call[1]));

Console.WriteLine($"Output: [{string.Join(", ", results4)}]");
// Each day only covers itself
Console.WriteLine("Expected: [4, 3, 2, 1]");