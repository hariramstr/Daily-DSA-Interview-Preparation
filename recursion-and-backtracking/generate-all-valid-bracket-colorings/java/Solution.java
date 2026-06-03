```java
/*
 * Title: Generate All Valid Bracket Colorings
 * Difficulty: Easy
 * Topic: Recursion and Backtracking
 *
 * Problem Description:
 * You are given a positive integer `n` representing the number of pairs of brackets.
 * Your task is to generate all valid combinations of `n` pairs of brackets, where each
 * bracket (both opening and closing) is also assigned one of two colors: Red or Blue.
 *
 * A bracket sequence is considered valid if:
 * 1. Every opening bracket has a corresponding closing bracket.
 * 2. The brackets are properly nested.
 *
 * For each valid bracket sequence, every individual bracket character (both `(` and `)`)
 * must be assigned a color. Two colorings of the same bracket sequence are considered
 * distinct if any bracket differs in color.
 *
 * Return a list of all distinct results as strings. Each bracket should be represented as
 * R(, B(, R), or B) for red-open, blue-open, red-close, and blue-close respectively.
 * The output can be in any order.
 *
 * Constraints:
 * - 1 <= n <= 4
 *
 * Example 1:
 * - Input: n = 1
 * - Output: ["R(R)", "R(B)", "B(R)", "B(B)"]
 *
 * Example 2:
 * - Input: n = 2
 * - Output: 32 strings total (2 valid sequences × 2^4 colorings each)
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Solution class for generating all valid bracket colorings.
 *
 * <p>The approach is to use recursive backtracking to:
 * 1. Generate all valid bracket sequences (like the classic "Generate Parentheses" problem).
 * 2. For each bracket placed, try both colors (Red and Blue).
 *
 * <p>This means at each recursive step, when we decide to place an opening or closing bracket,
 * we branch into two sub-problems: one where the bracket is Red, and one where it is Blue.
 */
public class Solution {

    /**
     * Generates all valid bracket colorings for n pairs of brackets.
     *
     * <p>This is the main entry point. It initializes the recursion with an empty
     * current string, 0 open brackets used, 0 close brackets used, and an empty result list.
     *
     * @param n the number of bracket pairs (1 <= n <= 4)
     * @return a list of all distinct valid colored bracket strings
     *
     * Time Complexity: O(C(n) * 2^(2n)) where C(n) is the nth Catalan number
     *                  (number of valid bracket sequences) and 2^(2n) is the number
     *                  of colorings per sequence. For n=4, this is manageable.
     * Space Complexity: O(n) for the recursion stack depth (2n levels deep),
     *                   plus O(C(n) * 2^(2n) * 2n) for storing all results.
     */
    public List<String> generateColoredBrackets(int n) {
        // This list will accumulate all valid colored bracket strings
        List<String> results = new ArrayList<>();

        // Start the backtracking recursion:
        // - current: the bracket string built so far (starts empty)
        // - open: how many opening brackets have been placed so far (starts at 0)
        // - close: how many closing brackets have been placed so far (starts at 0)
        // - n: total pairs needed
        // - results: the list to add complete valid strings to
        backtrack("", 0, 0, n, results);

        return results;
    }

    /**
     * Recursive backtracking method that builds valid colored bracket strings.
     *
     * <p>At each step, we decide whether to place an opening bracket or a closing bracket
     * (following the rules for valid bracket sequences). For whichever bracket type we place,
     * we try BOTH colors (Red and Blue), effectively doubling the branches at each step.
     *
     * <p>Rules for valid bracket sequences:
     * - We can place an opening bracket as long as we haven't used all n opening brackets.
     * - We can place a closing bracket as long as there are more open brackets than close brackets
     *   (i.e., there is an unmatched opening bracket to close).
     *
     * @param current the bracket string built so far in this recursive path
     * @param open    the number of opening brackets placed so far
     * @param close   the number of closing brackets placed so far
     * @param n       the total number of bracket pairs required
     * @param results the list to add complete valid colored strings to
     *
     * Time Complexity: O(C(n) * 2^(2n)) — each valid sequence has 2^(2n) colorings
     * Space Complexity: O(n) recursion depth (the string grows to length 2n)
     */
    private void backtrack(String current, int open, int close, int n, List<String> results) {

        // -----------------------------------------------------------------------
        // BASE CASE: We have placed exactly n opening and n closing brackets.
        // This means we have a complete, valid bracket sequence of length 2n.
        // Add it to the results list.
        // -----------------------------------------------------------------------
        if (open == n && close == n) {
            // The current string is a fully formed valid colored bracket sequence
            results.add(current);
            return; // Backtrack to explore other possibilities
        }

        // -----------------------------------------------------------------------
        // RECURSIVE CASE 1: Place an opening bracket (if we still can)
        //
        // We can place an opening bracket as long as we haven't used all n of them.
        // When we place an opening bracket, we try BOTH colors:
        //   - Red opening bracket: "R("
        //   - Blue opening bracket: "B("
        // -----------------------------------------------------------------------
        if (open < n) {
            // Branch 1a: Place a RED opening bracket
            // Append "R(" to the current string, increment open count
            backtrack(current + "R(", open + 1, close, n, results);

            // Branch 1b: Place a BLUE opening bracket
            // Append "B(" to the current string, increment open count
            backtrack(current + "B(", open + 1, close, n, results);
        }

        // -----------------------------------------------------------------------
        // RECURSIVE CASE 2: Place a closing bracket (if we still can)
        //
        // We can place a closing bracket only if there are more opening brackets
        // than closing brackets placed so far (open > close). This ensures we
        // never have an unmatched closing bracket, keeping the sequence valid.
        //
        // When we place a closing bracket, we try BOTH colors:
        //   - Red closing bracket: "R)"
        //   - Blue closing bracket: "B)"
        // -----------------------------------------------------------------------
        if (close < open) {
            // Branch 2a: Place a RED closing bracket
            // Append "R)" to the current string, increment close count
            backtrack(current + "R)", open, close + 1, n, results);

            // Branch 2b: Place a BLUE closing bracket
            // Append "B)" to the current string, increment close count
            backtrack(current + "B)", open, close + 1, n, results);
        }

        // -----------------------------------------------------------------------
        // After all recursive calls return, we implicitly "undo" the last choice
        // because we're using string concatenation (immutable strings in Java),
        // so `current` is not modified — each recursive call gets its own copy.
        // This is the "backtracking" part: we naturally return to the previous state.
        // -----------------------------------------------------------------------
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * <p>Traces through the examples from the problem description and prints results.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1: n = 1
        // Expected: ["R(R)", "R(B)", "B(R)", "B(B)"] (4 results)
        // Only one valid bracket sequence: ()
        // Each of the 2 brackets can be R or B → 2^2 = 4 colorings
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1: n = 1 ===");
        List<String> result1 = solution.generateColoredBrackets(1);
        System.out.println("Number of results: " + result1.size() + " (expected: 4)");
        System.out.println("Results:");
        for (String s : result1) {
            System.out.println("  " + s);
        }
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: n = 2
        // Expected: 32 results
        // Two valid bracket sequences: (()) and ()()
        // Each has 4 brackets → 2^4 = 16 colorings per sequence
        // Total: 2 × 16 = 32
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2: n = 2 ===");
        List<String> result2 = solution.generateColoredBrackets(2);
        System.out.println("Number of results: " + result2.size() + " (expected: 32)");
        System.out.println("Results:");
        for (String s : result2) {
            System.out.println("  " + s);
        }
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: n = 3
        // There are 5 valid bracket sequences for n=3 (Catalan number C(3) = 5)
        // Each has 6 brackets → 2^6 = 64 colorings per sequence
        // Total: 5 × 64 = 320
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3: n = 3 ===");
        List<String> result3 = solution.generateColoredBrackets(3);
        System.out.println("Number of results: " + result3.size() + " (expected: 320)");
        System.out.println("First 10 results:");
        for (int i = 0; i < Math.min(10, result3.size()); i++) {
            System.out.println("  " + result3.get(i));
        }
        System.out.println("  ... and " + (result3.size() - 10) + " more");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 4: n = 4
        // There are 14 valid bracket sequences for n=4 (Catalan number C(4) = 14)
        // Each has 8 brackets → 2^8 = 256 colorings per sequence
        // Total: 14 × 256 = 3584
        // -----------------------------------------------------------------------
        System.out.println("=== Example 4: n = 4 ===");
        List<String> result4 = solution.generateColoredBrackets(4);
        System.out.println("Number of results: " + result4.size() + " (expected: 3584)");
        System.out.println("First 5 results:");
        for (int i = 0; i < Math.min(5, result4.size()); i++) {
            System.out.println("  " + result4.get(i));
        }
        System.out.println("  ... and " + (result4.size() - 5) + " more");
        System.out.println();

        // -----------------------------------------------------------------------
        // Verification: Check specific expected values from Example 1
        // -----------------------------------------------------------------------
        System.out.println("=== Verification for n=1 ===");
        System.out.println("Contains 'R(R)': " + result1.contains("R(R)") + " (expected: true)");
        System.out.println("Contains 'R(B)': " + result1.contains("R(B)") + " (expected: true)");
        System.out.println("Contains 'B(R)': " + result1.contains("B(R)") + " (expected: true)");
        System.out.println("Contains 'B(B)': " + result1.contains("B(B)") + " (expected: true)");
        System.out.println();

        // -----------------------------------------------------------------------
        // Verification: Check specific expected values from Example 2
        // -----------------------------------------------------------------------
        System.out.println("=== Verification for n=2 ===");
        System.out.println("Contains 'R(R(R)R)': " + result2.contains("R(R(R)R)") + " (expected: true)");
        System.out.println("Contains 'R(R(R)B)': " + result2.contains("R(R(R)B)") + " (expected: true)");
        System.out.println("Contains 'R(R)R(R)': " + result2.contains("R(R)R(R)") + " (expected: true)");
    }
}
```