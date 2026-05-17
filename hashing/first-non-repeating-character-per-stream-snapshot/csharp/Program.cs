/*
 * Title: First Non-Repeating Character Per Stream Snapshot
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a string `stream` representing a sequence of characters arriving one at a time.
 * After processing each character, you must record the first non-repeating character seen so far.
 * If no such character exists at that point, record '#' instead.
 *
 * Return a string `result` of the same length as `stream`, where `result[i]` is the first
 * non-repeating character after processing the first i+1 characters of `stream`.
 *
 * A character is considered non-repeating if it has appeared exactly once so far.
 *
 * Example 1:
 *   Input:  stream = "aabccb"
 *   Output: "a#bbb#"
 *
 * Example 2:
 *   Input:  stream = "abcd"
 *   Output: "aaaa"
 *
 * Constraints:
 *   1 <= stream.length <= 10^5
 *   stream consists of only lowercase English letters.
 */

using System;
using System.Collections.Generic;
using System.Text;

// ─────────────────────────────────────────────────────────────────────────────
// Solution class containing the algorithm
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// For every prefix of the stream, finds the first non-repeating character.
    ///
    /// Time Complexity:  O(N * 26) = O(N)
    ///   - We iterate over the stream once (N steps).
    ///   - At each step we scan the insertion-order queue to find the first
    ///     character whose count is exactly 1.  The queue holds at most 26
    ///     distinct lowercase letters, so each scan is O(26) = O(1).
    ///   - Overall: O(N).
    ///
    /// Space Complexity: O(1)  (ignoring the output string)
    ///   - The frequency dictionary holds at most 26 entries.
    ///   - The LinkedList (used as an ordered queue) holds at most 26 nodes.
    ///   - The output StringBuilder is O(N) but is considered part of the output.
    /// </summary>
    public string FirstNonRepeatingPerSnapshot(string stream)
    {
        // ── Step 1: Frequency table ───────────────────────────────────────────
        // We need to know how many times each character has appeared so far.
        // A Dictionary<char, int> gives O(1) look-up and update.
        // Key   = the character
        // Value = how many times it has been seen up to the current position
        var frequency = new Dictionary<char, int>();

        // ── Step 2: Ordered structure to track insertion order ────────────────
        // We need to quickly find the FIRST character that still has count == 1.
        // "First" means the one that appeared earliest in the stream.
        //
        // A LinkedList<char> preserves insertion order and lets us:
        //   • AddLast  in O(1) when a new character arrives for the first time.
        //   • RemoveFirst / traverse from the front to find the first unique char.
        //
        // Why not a plain Queue?  A Queue only lets us remove from the front,
        // but we might need to skip over characters that became repeating later.
        // With a LinkedList we can peek at the front and remove it if it is now
        // repeating, continuing until we find a unique one or exhaust the list.
        var orderQueue = new LinkedList<char>();

        // ── Step 3: Output builder ────────────────────────────────────────────
        // StringBuilder is efficient for building strings character by character
        // (avoids creating many intermediate string objects).
        var result = new StringBuilder(stream.Length);

        // ── Step 4: Process each character in the stream one at a time ────────
        foreach (char ch in stream)
        {
            // ── 4a: Update the frequency count for this character ─────────────
            // If the character is new, initialise its count to 0 first, then
            // increment.  If it already exists, just increment.
            if (!frequency.ContainsKey(ch))
            {
                // First time we see this character:
                //   • Set its count to 1.
                //   • Add it to the BACK of the ordered queue so we remember
                //     the order in which characters first appeared.
                frequency[ch] = 1;
                orderQueue.AddLast(ch);
            }
            else
            {
                // We have seen this character before — it is now repeating.
                // Increment its count (it will be > 1, so it is no longer unique).
                frequency[ch]++;
                // Note: we do NOT add it to the queue again.  It is already
                // present (from its first occurrence) and will be skipped when
                // we scan from the front.
            }

            // ── 4b: Clean the front of the queue ─────────────────────────────
            // Characters at the front of the queue that now have frequency > 1
            // can never become the "first non-repeating" character again.
            // Remove them eagerly so the front of the queue is always a
            // candidate (count == 1) or the queue becomes empty.
            //
            // Why remove eagerly?  It keeps subsequent look-ups fast and
            // ensures the front of the queue is always meaningful.
            while (orderQueue.Count > 0 && frequency[orderQueue.First!.Value] > 1)
            {
                // The character at the front is now repeating — discard it.
                orderQueue.RemoveFirst();
            }

            // ── 4c: Record the snapshot answer ───────────────────────────────
            // After cleaning, the front of the queue (if any) is the first
            // character that has appeared exactly once so far.
            if (orderQueue.Count > 0)
            {
                // The front node holds the first non-repeating character.
                result.Append(orderQueue.First!.Value);
            }
            else
            {
                // No unique character exists at this point in the stream.
                result.Append('#');
            }
        }

        // ── Step 5: Return the completed result string ────────────────────────
        return result.ToString();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / driver code (top-level statements)
// ─────────────────────────────────────────────────────────────────────────────

var solver = new Solution();

Console.WriteLine("=== First Non-Repeating Character Per Stream Snapshot ===");
Console.WriteLine();

// ── Example 1 ────────────────────────────────────────────────────────────────
// Trace:
//   'a' → freq{a:1}         queue[a]       → front='a'  → result: "a"
//   'a' → freq{a:2}         queue[]        → empty       → result: "a#"
//   'b' → freq{a:2,b:1}     queue[b]       → front='b'  → result: "a#b"
//   'c' → freq{a:2,b:1,c:1} queue[b,c]     → front='b'  → result: "a#bb"
//   'c' → freq{a:2,b:1,c:2} queue[b]       → front='b'  → result: "a#bbb"
//   'b' → freq{a:2,b:2,c:2} queue[]        → empty       → result: "a#bbb#"
// Expected output: "a#bbb#"
string stream1 = "aabccb";
string output1 = solver.FirstNonRepeatingPerSnapshot(stream1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  Input:    \"{stream1}\"");
Console.WriteLine($"  Output:   \"{output1}\"");
Console.WriteLine($"  Expected: \"a#bbb#\"");
Console.WriteLine($"  Correct:  {output1 == "a#bbb#"}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
// Trace:
//   'a' → freq{a:1}            queue[a]       → front='a' → result: "a"
//   'b' → freq{a:1,b:1}        queue[a,b]     → front='a' → result: "aa"
//   'c' → freq{a:1,b:1,c:1}    queue[a,b,c]   → front='a' → result: "aaa"
//   'd' → freq{a:1,b:1,c:1,d:1}queue[a,b,c,d] → front='a' → result: "aaaa"
// Expected output: "aaaa"
string stream2 = "abcd";
string output2 = solver.FirstNonRepeatingPerSnapshot(stream2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  Input:    \"{stream2}\"");
Console.WriteLine($"  Output:   \"{output2}\"");
Console.WriteLine($"  Expected: \"aaaa\"");
Console.WriteLine($"  Correct:  {output2 == "aaaa"}");
Console.WriteLine();

// ── Additional edge-case: all same characters ─────────────────────────────────
// Every character repeats immediately after the first one.
// Expected: "a######..."  → "a" then all '#'
string stream3 = "aaaaaaa";
string output3 = solver.FirstNonRepeatingPerSnapshot(stream3);
Console.WriteLine($"Edge Case (all same):");
Console.WriteLine($"  Input:    \"{stream3}\"");
Console.WriteLine($"  Output:   \"{output3}\"");
Console.WriteLine($"  Expected: \"a######\"");
Console.WriteLine($"  Correct:  {output3 == "a######"}");
Console.WriteLine();

// ── Additional edge-case: single character ────────────────────────────────────
string stream4 = "z";
string output4 = solver.FirstNonRepeatingPerSnapshot(stream4);
Console.WriteLine($"Edge Case (single char):");
Console.WriteLine($"  Input:    \"{stream4}\"");
Console.WriteLine($"  Output:   \"{output4}\"");
Console.WriteLine($"  Expected: \"z\"");
Console.WriteLine($"  Correct:  {output4 == "z"}");
Console.WriteLine();

// ── Additional edge-case: interleaved repeats ─────────────────────────────────
// stream = "abacbc"
// 'a' → {a:1}           queue[a]     → 'a'
// 'b' → {a:1,b:1}       queue[a,b]   → 'a'
// 'a' → {a:2,b:1}       queue[b]     → 'b'
// 'c' → {a:2,b:1,c:1}   queue[b,c]   → 'b'
// 'b' → {a:2,b:2,c:1}   queue[c]     → 'c'
// 'c' → {a:2,b:2,c:2}   queue[]      → '#'
// Expected: "aabbb#"
string stream5 = "abacbc";
string output5 = solver.FirstNonRepeatingPerSnapshot(stream5);
Console.WriteLine($"Edge Case (interleaved):");
Console.WriteLine($"  Input:    \"{stream5}\"");
Console.WriteLine($"  Output:   \"{output5}\"");
Console.WriteLine($"  Expected: \"aabbb#\"");
Console.WriteLine($"  Correct:  {output5 == "aabbb#"}");