/*
 * Title: Decode XOR Encrypted Segments
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an integer array `encoded` of length `n - 1` and an integer `first`,
 * where `encoded[i] = arr[i] XOR arr[i + 1]` represents a pairwise XOR encoding of
 * an original array `arr` of length `n`. However, there is a twist: the array `arr`
 * is divided into segments of length `k`. Within each segment, consecutive elements
 * are XOR-encoded as usual, but between segments, a secret key `secret` is XOR-applied
 * once to the transition element before encoding.
 *
 * Specifically:
 *   encoded[k-1]   = arr[k-1]   XOR secret XOR arr[k]
 *   encoded[2k-1]  = arr[2k-1]  XOR secret XOR arr[2k]
 *   ... and so on for every segment boundary.
 *
 * Given `encoded`, `first` (the value of arr[0]), `k` (segment length), and `secret`,
 * reconstruct and return the original array `arr`.
 *
 * Constraints:
 *   - 2 <= n <= 10^5
 *   - n is divisible by k
 *   - 1 <= k <= n
 *   - 0 <= encoded[i] <= 10^5
 *   - 0 <= first <= 10^5
 *   - 0 <= secret <= 10^5
 *
 * Example 1:
 *   Input:  encoded = [1, 2, 7, 3, 4], first = 4, k = 3, secret = 5
 *   Output: [4, 5, 7, 1, 2]
 *   Trace:
 *     arr[0] = 4 (given as first)
 *     arr[1] = arr[0] XOR encoded[0] = 4 XOR 1 = 5
 *     arr[2] = arr[1] XOR encoded[1] = 5 XOR 2 = 7
 *     -- Boundary at index 2 (k-1 = 2), so encoded[2] includes secret --
 *     arr[3] = arr[2] XOR secret XOR encoded[2] = 7 XOR 5 XOR 7 = 1
 *     arr[4] = arr[3] XOR encoded[3] = 1 XOR 3 = 2
 *     Result: [4, 5, 7, 1, 2] ✓
 *
 * Example 2:
 *   Input:  encoded = [3, 1, 0, 2], first = 2, k = 2, secret = 3
 *   Output: [2, 1, 3, 3]
 *   Trace:
 *     arr[0] = 2 (given as first)
 *     arr[1] = arr[0] XOR encoded[0] = 2 XOR 3 = 1
 *     -- Boundary at index 1 (k-1 = 1), so encoded[1] includes secret --
 *     arr[2] = arr[1] XOR secret XOR encoded[1] = 1 XOR 3 XOR 1 = 3
 *     arr[3] = arr[2] XOR encoded[2] = 3 XOR 0 = 3
 *     -- Boundary at index 3 (2k-1 = 3), but no arr[4] to decode --
 *     Result: [2, 1, 3, 3] ✓
 */

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────

public class Solution
{
    /// <summary>
    /// Decodes the XOR-encrypted segmented array.
    ///
    /// Time Complexity:  O(n) — We iterate through the encoded array exactly once,
    ///                          performing O(1) work per element.
    /// Space Complexity: O(n) — We allocate the output array `arr` of length n.
    ///                          No additional auxiliary data structures are needed.
    /// </summary>
    /// <param name="encoded">The encoded array of length n-1.</param>
    /// <param name="first">The first element of the original array arr[0].</param>
    /// <param name="k">The segment length.</param>
    /// <param name="secret">The secret key XOR-applied at each segment boundary.</param>
    /// <returns>The reconstructed original array arr of length n.</returns>
    public int[] Decode(int[] encoded, int first, int k, int secret)
    {
        // ── Step 1: Determine the size of the original array ──────────────────
        // The encoded array has length n-1, so the original array has length n.
        // We add 1 to get back the original length.
        int n = encoded.Length + 1;

        // ── Step 2: Allocate the result array ─────────────────────────────────
        // We create an integer array of size n to hold the reconstructed values.
        // Using a simple array is ideal here because we access elements by index
        // and the size is known upfront.
        int[] arr = new int[n];

        // ── Step 3: Seed the first element ────────────────────────────────────
        // We are given arr[0] = first directly. This is our starting point.
        // Without this anchor value, we could not decode any subsequent element
        // because XOR decoding requires knowing one side of the equation.
        arr[0] = first;

        // ── Step 4: Iterate through each encoded value to reconstruct arr ─────
        // For each index i in the encoded array (0 to n-2), we compute arr[i+1].
        //
        // Key insight about XOR:
        //   If  encoded[i] = A XOR B,  then  B = encoded[i] XOR A
        //   (XOR-ing both sides by A cancels A on the right, leaving B)
        //
        // Normal positions (within a segment):
        //   encoded[i] = arr[i] XOR arr[i+1]
        //   => arr[i+1] = arr[i] XOR encoded[i]
        //
        // Boundary positions (at the end of each segment, i.e., i = k-1, 2k-1, ...):
        //   encoded[i] = arr[i] XOR secret XOR arr[i+1]
        //   => arr[i+1] = arr[i] XOR secret XOR encoded[i]
        //
        // We detect a boundary by checking if (i+1) is a multiple of k,
        // because encoded[i] connects arr[i] and arr[i+1], and the boundary
        // occurs when arr[i] is the last element of a segment (index i = mk-1
        // for some integer m), which means (i+1) % k == 0.

        for (int i = 0; i < encoded.Length; i++)
        {
            // ── Step 4a: Check if this encoded position is a segment boundary ──
            // The encoded value at index i connects arr[i] to arr[i+1].
            // A boundary occurs when arr[i] is the last element of a segment.
            // arr[i] is the last element of a segment when (i+1) is a multiple of k.
            // Example: k=3, boundaries at i=2 (3rd element), i=5 (6th element), etc.
            bool isBoundary = (i + 1) % k == 0;

            if (isBoundary)
            {
                // ── Step 4b: Decode a boundary-encoded value ───────────────────
                // At a boundary, the encoding includes the secret:
                //   encoded[i] = arr[i] XOR secret XOR arr[i+1]
                // Solving for arr[i+1]:
                //   arr[i+1] = arr[i] XOR secret XOR encoded[i]
                //
                // Why XOR with secret here? Because the problem states that at
                // each segment boundary, the secret is mixed in before encoding.
                // To reverse this, we must XOR the secret back out.
                arr[i + 1] = arr[i] ^ secret ^ encoded[i];
            }
            else
            {
                // ── Step 4c: Decode a normal (non-boundary) encoded value ──────
                // Within a segment, the encoding is standard:
                //   encoded[i] = arr[i] XOR arr[i+1]
                // Solving for arr[i+1]:
                //   arr[i+1] = arr[i] XOR encoded[i]
                //
                // No secret is involved here — this is pure XOR decoding.
                arr[i + 1] = arr[i] ^ encoded[i];
            }
        }

        // ── Step 5: Return the fully reconstructed array ──────────────────────
        // At this point, every element of arr has been computed. We return it.
        return arr;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Test Code (Top-Level Statements)
// ─────────────────────────────────────────────────────────────────────────────

var solution = new Solution();

// Helper method to print an array nicely
static void PrintArray(string label, int[] array)
{
    Console.Write($"{label}: [");
    Console.Write(string.Join(", ", array));
    Console.WriteLine("]");
}

Console.WriteLine("=== Decode XOR Encrypted Segments ===");
Console.WriteLine();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Input:  encoded = [1, 2, 7, 3, 4], first = 4, k = 3, secret = 5
// Expected Output: [4, 5, 7, 1, 2]
//
// Manual trace:
//   arr[0] = 4
//   i=0: (0+1)%3=1 ≠ 0 → normal  → arr[1] = 4 XOR 1 = 5
//   i=1: (1+1)%3=2 ≠ 0 → normal  → arr[2] = 5 XOR 2 = 7
//   i=2: (2+1)%3=0     → boundary → arr[3] = 7 XOR 5 XOR 7 = 1
//   i=3: (3+1)%3=1 ≠ 0 → normal  → arr[4] = 1 XOR 3 = 2
//   Result: [4, 5, 7, 1, 2] ✓

Console.WriteLine("--- Example 1 ---");
int[] encoded1 = { 1, 2, 7, 3, 4 };
int first1 = 4, k1 = 3, secret1 = 5;
int[] result1 = solution.Decode(encoded1, first1, k1, secret1);
PrintArray("Input encoded", encoded1);
Console.WriteLine($"first = {first1}, k = {k1}, secret = {secret1}");
PrintArray("Output arr   ", result1);
Console.WriteLine("Expected:    [4, 5, 7, 1, 2]");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Input:  encoded = [3, 1, 0, 2], first = 2, k = 2, secret = 3
// Expected Output: [2, 1, 3, 3]
//
// Manual trace:
//   arr[0] = 2
//   i=0: (0+1)%2=1 ≠ 0 → normal   → arr[1] = 2 XOR 3 = 1
//   i=1: (1+1)%2=0     → boundary  → arr[2] = 1 XOR 3 XOR 1 = 3
//   i=2: (2+1)%2=1 ≠ 0 → normal   → arr[3] = 3 XOR 0 = 3
//   Result: [2, 1, 3, 3] ✓

Console.WriteLine("--- Example 2 ---");
int[] encoded2 = { 3, 1, 0, 2 };
int first2 = 2, k2 = 2, secret2 = 3;
int[] result2 = solution.Decode(encoded2, first2, k2, secret2);
PrintArray("Input encoded", encoded2);
Console.WriteLine($"first = {first2}, k = {k2}, secret = {secret2}");
PrintArray("Output arr   ", result2);
Console.WriteLine("Expected:    [2, 1, 3, 3]");
Console.WriteLine();

// ── Example 3: Edge case — k equals n (single segment, no boundaries) ────────
// Input:  encoded = [1, 3, 2], first = 5, k = 4, secret = 99
// Since k = n = 4, there are no segment boundaries in the encoded array
// (boundaries would be at i = k-1 = 3, but encoded only goes to index 2).
// So all decoding is normal XOR.
//
// Manual trace:
//   arr[0] = 5
//   i=0: (0+1)%4=1 ≠ 0 → normal → arr[1] = 5 XOR 1 = 4
//   i=1: (1+1)%4=2 ≠ 0 → normal → arr[2] = 4 XOR 3 = 7
//   i=2: (2+1)%4=3 ≠ 0 → normal → arr[3] = 7 XOR 2 = 5
//   Result: [5, 4, 7, 5]

Console.WriteLine("--- Example 3 (k = n, no boundaries) ---");
int[] encoded3 = { 1, 3, 2 };
int first3 = 5, k3 = 4, secret3 = 99;
int[] result3 = solution.Decode(encoded3, first3, k3, secret3);
PrintArray("Input encoded", encoded3);
Console.WriteLine($"first = {first3}, k = {k3}, secret = {secret3}");
PrintArray("Output arr   ", result3);
Console.WriteLine("Expected:    [5, 4, 7, 5]");
Console.WriteLine();

// ── Example 4: k = 1 (every transition is a boundary) ───────────────────────
// Input:  encoded = [2, 6, 4], first = 3, k = 1, secret = 1
// With k=1, every encoded[i] is a boundary (i+1 is always a multiple of 1).
//
// Manual trace:
//   arr[0] = 3
//   i=0: (0+1)%1=0 → boundary → arr[1] = 3 XOR 1 XOR 2 = 0
//   i=1: (1+1)%1=0 → boundary → arr[2] = 0 XOR 1 XOR 6 = 7
//   i=2: (2+1)%1=0 → boundary → arr[3] = 7 XOR 1 XOR 4 = 2
//   Result: [3, 0, 7, 2]

Console.WriteLine("--- Example 4 (k = 1, all boundaries) ---");
int[] encoded4 = { 2, 6, 4 };
int first4 = 3, k4 = 1, secret4 = 1;
int[] result4 = solution.Decode(encoded4, first4, k4