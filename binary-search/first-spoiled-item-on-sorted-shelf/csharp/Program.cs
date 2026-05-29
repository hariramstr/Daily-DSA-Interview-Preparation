/*
 * Title: First Spoiled Item on Sorted Shelf
 * Difficulty: Easy
 * Topic: Binary Search
 *
 * Problem Description:
 * A grocery store arranges items on a shelf sorted by their expiration dates in ascending order
 * (oldest expiration date first). Each item has an integer expiration date represented as the
 * number of days from today. An item is considered 'spoiled' if its expiration date is less than
 * or equal to 0 (meaning it has already expired or expires today).
 *
 * Given a sorted array `expirations` of integers representing the expiration dates of items on
 * the shelf, return the index of the FIRST NON-SPOILED item (i.e., the first item with an
 * expiration date strictly greater than 0). If all items are spoiled, return -1.
 * If no items are spoiled, return 0.
 *
 * You must solve this in O(log n) time complexity.
 *
 * Constraints:
 *   - 1 <= expirations.length <= 10^5
 *   - -10^4 <= expirations[i] <= 10^4
 *   - The array is sorted in non-decreasing order.
 *
 * Examples:
 *   Input: [-5, -3, -1, 0, 2, 4, 7]  => Output: 4
 *   Input: [1, 3, 5, 8]              => Output: 0
 *   Input: [-4, -2, 0]               => Output: -1
 */

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Finds the index of the first non-spoiled item using Binary Search.
    ///
    /// Time Complexity:  O(log n) — each iteration halves the search space.
    /// Space Complexity: O(1)    — only a handful of integer variables are used;
    ///                             no extra data structures are allocated.
    /// </summary>
    /// <param name="expirations">
    ///   A sorted (non-decreasing) array of integers representing expiration dates.
    ///   Negative values and zero mean the item is spoiled; positive means fresh.
    /// </param>
    /// <returns>
    ///   The index of the first element that is strictly greater than 0,
    ///   or -1 if every element is <= 0.
    /// </returns>
    public int FirstNonSpoiled(int[] expirations)
    {
        // ── STEP 1: Handle the trivial edge cases first ──────────────────────
        // Before running binary search, we can answer two questions in O(1):
        //
        //   a) Is the very FIRST item already fresh (> 0)?
        //      Because the array is sorted ascending, if expirations[0] > 0
        //      then ALL items are fresh, so the answer is index 0.
        //
        //   b) Is the very LAST item still spoiled (<= 0)?
        //      If even the largest value in the array is <= 0, every item is
        //      spoiled and we must return -1.
        //
        // These checks also protect us from accessing out-of-bounds indices later.

        int n = expirations.Length; // total number of items on the shelf

        // Edge case (a): no items are spoiled at all
        if (expirations[0] > 0)
        {
            // The smallest expiration date is already positive, so the first
            // non-spoiled item is right at the beginning of the shelf.
            return 0;
        }

        // Edge case (b): every item is spoiled
        if (expirations[n - 1] <= 0)
        {
            // Even the freshest item (last in sorted order) is expired or
            // expires today, so there is no non-spoiled item at all.
            return -1;
        }

        // ── STEP 2: Set up the binary search boundaries ──────────────────────
        // We now KNOW that:
        //   • expirations[0]     <= 0  (at least the first item is spoiled)
        //   • expirations[n - 1] >  0  (at least the last item is fresh)
        //
        // This guarantees the answer exists somewhere in (0, n-1].
        // We want to find the LEFTMOST index where the value first becomes > 0.
        //
        // Classic "find first true" binary search pattern:
        //   - Maintain a window [left, right].
        //   - Shrink it until left == right, which is our answer.

        int left  = 0;     // left boundary  — always points to a spoiled item
        int right = n - 1; // right boundary — always points to a fresh item

        // ── STEP 3: Binary search loop ───────────────────────────────────────
        // We continue as long as there is more than one candidate index.
        // Invariant maintained throughout:
        //   expirations[left]  <= 0  (spoiled)
        //   expirations[right] >  0  (fresh)
        //
        // When left + 1 == right, 'right' is the first fresh index.

        while (left + 1 < right)
        {
            // Calculate the midpoint without integer overflow.
            // Using left + (right - left) / 2 is safer than (left + right) / 2
            // when indices could be very large (not an issue here, but good habit).
            int mid = left + (right - left) / 2;

            // ── STEP 3a: Inspect the middle element ──────────────────────────
            // We check whether the item at 'mid' is spoiled or fresh.

            if (expirations[mid] > 0)
            {
                // The item at 'mid' is FRESH (expiration > 0).
                // The first fresh item could be at 'mid' OR somewhere to its LEFT.
                // So we move the RIGHT boundary to 'mid', keeping 'mid' in range.
                //
                // Why not mid - 1?  Because 'mid' itself might BE the answer;
                // we must not exclude it.
                right = mid;
            }
            else
            {
                // The item at 'mid' is SPOILED (expiration <= 0).
                // The first fresh item must be somewhere to the RIGHT of 'mid'.
                // So we move the LEFT boundary to 'mid'.
                //
                // Why not mid + 1?  Because we want to preserve the invariant
                // that left always points to a spoiled item.  mid is spoiled,
                // so setting left = mid is safe.
                left = mid;
            }

            // After each iteration the window [left, right] shrinks by at least
            // half, guaranteeing O(log n) total iterations.
        }

        // ── STEP 4: Return the result ─────────────────────────────────────────
        // The loop exits when left + 1 == right.
        // At that point:
        //   • expirations[left]  <= 0  → still spoiled
        //   • expirations[right] >  0  → first fresh item  ← our answer
        //
        // 'right' is the smallest index with a positive expiration date.
        return right;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code  (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solver = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Shelf: [-5, -3, -1, 0, 2, 4, 7]
// Indices 0-3 are spoiled (values <= 0). Index 4 has value 2 (fresh).
// Expected output: 4
int[] shelf1  = { -5, -3, -1, 0, 2, 4, 7 };
int   result1 = solver.FirstNonSpoiled(shelf1);
Console.WriteLine($"Example 1 — Input: [{string.Join(", ", shelf1)}]");
Console.WriteLine($"           Output: {result1}   (Expected: 4)");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Shelf: [1, 3, 5, 8]
// All items are fresh. The first non-spoiled item is at index 0.
// Expected output: 0
int[] shelf2  = { 1, 3, 5, 8 };
int   result2 = solver.FirstNonSpoiled(shelf2);
Console.WriteLine($"Example 2 — Input: [{string.Join(", ", shelf2)}]");
Console.WriteLine($"           Output: {result2}   (Expected: 0)");
Console.WriteLine();

// ── Example 3 ────────────────────────────────────────────────────────────────
// Shelf: [-4, -2, 0]
// All items are spoiled (largest value is 0, which is <= 0).
// Expected output: -1
int[] shelf3  = { -4, -2, 0 };
int   result3 = solver.FirstNonSpoiled(shelf3);
Console.WriteLine($"Example 3 — Input: [{string.Join(", ", shelf3)}]");
Console.WriteLine($"           Output: {result3}  (Expected: -1)");
Console.WriteLine();

// ── Additional edge cases ─────────────────────────────────────────────────────

// Single element — fresh
int[] shelf4  = { 5 };
int   result4 = solver.FirstNonSpoiled(shelf4);
Console.WriteLine($"Edge case 4 — Single fresh item [{string.Join(", ", shelf4)}]");
Console.WriteLine($"             Output: {result4}   (Expected: 0)");
Console.WriteLine();

// Single element — spoiled
int[] shelf5  = { -1 };
int   result5 = solver.FirstNonSpoiled(shelf5);
Console.WriteLine($"Edge case 5 — Single spoiled item [{string.Join(", ", shelf5)}]");
Console.WriteLine($"             Output: {result5}  (Expected: -1)");
Console.WriteLine();

// Boundary: transition right at the last index
int[] shelf6  = { -3, -2, -1, 0, 1 };
int   result6 = solver.FirstNonSpoiled(shelf6);
Console.WriteLine($"Edge case 6 — Transition at last index [{string.Join(", ", shelf6)}]");
Console.WriteLine($"             Output: {result6}   (Expected: 4)");
Console.WriteLine();

// Boundary: transition right at index 1
int[] shelf7  = { 0, 1, 2, 3 };
int   result7 = solver.FirstNonSpoiled(shelf7);
Console.WriteLine($"Edge case 7 — Transition at index 1 [{string.Join(", ", shelf7)}]");
Console.WriteLine($"             Output: {result7}   (Expected: 1)");