package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

public class SpellChecker {

	/** Number of words in dictionary */
	private static int dictionaryCount = 0;

	/**
	 * The Size of the hash table will be calculated based on the performance::
	 * λ (load factor) = size_of_dictionary / size_of_table
	 * 
	 * U(λ) = (1+ 1 /(1-λ)^2) /2
	 * 
	 * With a goal U(λ) of 3, the result for a dictionary of the given size
	 * (25144) is 45485. Choosing the next prime, 45491 as the table size.
	 */
	private static int tableSize = 45491;
	/** String array that will serve as our hash table */
	private static String[] hashTable;
	/** number of words to be spell-checked */
	private static int n = 0;
	/** number of misspelled or questionable words */
	private static int qWords = 0;
	/** total number of probes made during the linear probe search */
	private static int probeCount = 0;
	/** average number of probes per word */
	private static double avgProbe = 0;
	/** total number of lookups made */
	private static int lookUpCount = 0;
	/** average number of probes per lookup */
	private static double avgLookUp = 0;
	/** output printstream */
	private static PrintStream out;

	/**
	 * The user is prompted for a dictionary file, an input file, and an output
	 * file. The provided file is files/dict.txt, and the hash function and hash
	 * table are based on a dictionary with approximately 25,000 words.
	 * 
	 * The Program will then read in the words from the dictionary, run a spell
	 * check on the input, and then print the results to the output file
	 * 
	 * @param args
	 *            command line arguments
	 * @throws FileNotFoundException
	 *             if the input and/or dictionary files are not found
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// ---- get data from user ----
		Scanner in = new Scanner(System.in);
		System.out.print("Dictionary list? ");
		File dict = new File(in.next());
		System.out.print("Input file? ");
		File input = new File(in.next());
		System.out.print("Output file? ");
		File output = new File(in.next());

		out = new PrintStream(output);

		// Build a new hash table and fill it with the words from the
		// dictionary
		buildHashTable(dict);

		// ---- Output the Results ----
		out.println("=============================================");
		out.println("===== Misspelled/ Questionable Words: =======");
		out.println("=============================================");

		processWords(input);

		avgProbe = probeCount / (double) n;
		avgLookUp = probeCount / (double) lookUpCount;

		out.println("=============================================");
		out.printf("Number of words in dictionary: %d \n", dictionaryCount);
		out.printf("Number of words to be examined: %d \n", n);
		out.printf("Number of misspelled/ questionable words: %d \n", qWords);
		out.println("=============================================");
		out.printf("Total number of probes: %d \n", probeCount);
		out.printf("Average number of probes per word: %.2f \n", avgProbe);
		out.printf("Average number of probes per lookup: %.2f \n", avgLookUp);

		out.println("=============================================");
		in.close();
	}

	/**
	 * Build the hash table using the words in the dictionary file The hash
	 * table size is based on the results of the performance calculation based
	 * on a dictionary of approximately 25000 words and a goal of 3 probes per
	 * word
	 * 
	 * @param dict
	 *            dictionary of words to add to the hash table
	 * @throws FileNotFoundException
	 *             if dictionary cannot be read
	 */
	private static void buildHashTable(File dict) throws FileNotFoundException {

		// Set the hash table to be a new table of this size
		hashTable = new String[tableSize + 1];

		Scanner readDictionary = new Scanner(dict);
		// read in the words from the dictionary, perform the hash function, and
		// then add them to the hash table//
		while (readDictionary.hasNext()) {
			String word = readDictionary.next();
			int hf = hashFunction(word);
			while (hashTable[hf] != null) {
				hf++;
			}
			hashTable[hf] = word;
			dictionaryCount++;
		}
		readDictionary.close();

	}

	/**
	 * Reads the input file word by word, ensuring that the words are in the
	 * proper format before checking them against the dictionary
	 * 
	 * @param input
	 * @throws FileNotFoundException
	 */
	private static void processWords(File input) throws FileNotFoundException {
		Scanner in = new Scanner(input);
		boolean found = false;
		while (in.hasNext()) {
			String s = format(in.next());

			n++;
			found = spellCheck(s);
			if (!found) {
				qWords++;
				out.println(s);
			}
		}

		in.close();

	}

	/**
	 * Repeatedly checks a word using different spelling variations until it is
	 * determined that the word cannot be found in the dictionary.
	 * 
	 * If the word ends in 's, then the possessive is removed and the word is
	 * checked again. If the word is plural, the s / es ending is removed and
	 * the word is checked again. If the word is a past-tense verb, the -d/ -ed
	 * is removed and the word is checked again. If the word ends in -ing, the
	 * suffix is removed and the word is checked again. If the word ends in -er,
	 * the suffix is removed and the word is checked again
	 * 
	 * This is not a comprehensive spellchecker; some misspelled words will make
	 * it through the system, while some correctly spelled words will be marked
	 * as incorrect, depending on how thorough the dictionary file is to begin
	 * with.
	 * 
	 * IE: In the sample dictionary files/dict.txt, "family" is correct, but
	 * "families" would be marked incorrect.
	 * 
	 * @param s
	 * @return
	 */
	private static boolean spellCheck(String s) {
		lookUpCount++;
		int index = hashFunction(s);
		while (hashTable[index] != null) {
			if (s.equals(hashTable[index])) {
				return true;
			} else {
				probeCount++;
				index++;
			}
		}

		// ---- set up some of the sneak characters/ character strings that
		// might make a correctly spelled word seem misspelled.

		// Because all single letters are in the dictionary, any word that
		// reaches this point is guaranteed to have at least 2 characters, so we
		// do not need to test for length before assigning the end string
		if (Character.isUpperCase(s.charAt(0))) {
			s = s.toLowerCase();
			return spellCheck(s);
		}
		String end = "";
		if (s.length() >= 2) {
			end = s.substring(s.length() - 2);
		}
		char fin = s.charAt(s.length() - 1);
		// However, we do need to check that a string has three characters
		// before assigning the 3-char end string to check for 'ing'
		String ing = "";
		if (s.length() > 3) {
			ing = s.substring(s.length() - 3);
		}
		if (ing.equals("ing")) {
			s = s.substring(0, s.length() - 3);
			return spellCheck(s);
		} else if (end.equals("ly") || end.equals("'s")) {
			s = s.substring(0, s.length() - 2);
			return spellCheck(s);
		} else if (fin == 's') {
			s = s.substring(0, s.length() - 1);
			Boolean finFound = spellCheck(s);
			if (!finFound && s.charAt(s.length() - 1) == 'e') {
				s = s.substring(0, s.length() - 1);
				finFound = spellCheck(s);
			}
			return finFound;
		} else if (fin == 'd') {
			s = s.substring(0, s.length() - 1);
			Boolean finFound = spellCheck(s);
			if (!finFound && s.charAt(s.length() - 1) == 'e') {
				s = s.substring(0, s.length() - 1);
				finFound = spellCheck(s);
			}
			return finFound;
		} else if (fin == 'r') {
			s = s.substring(0, s.length() - 1);
			Boolean finFound = spellCheck(s);
			if (!finFound && s.charAt(s.length() - 1) == 'e') {
				s = s.substring(0, s.length() - 1);
				finFound = spellCheck(s);
			}
			return finFound;
		}

		return false;

	}

	/**
	 * Before spell-checking the word, it should be formatted to ensure that no
	 * leading/ trailing punctuation mark will cause it to be erroneously
	 * flagged as misspelled
	 * 
	 * @param s
	 * @return
	 */
	private static String format(String s) {
		if (!Character.isLetterOrDigit(s.charAt(0))) {
			s = s.substring(1);
		}
		while (!Character.isLetter(s.charAt(s.length() - 1)) && s.length() > 1) {
			s = s.substring(0, s.length() - 1);
		}

		return s;

	}

	/**
	 * The hash function assigns an index value to each word into the hash
	 * table. The goal of the hash function is to reduce collisions within the
	 * table so that search functions can be performed efficiently
	 * 
	 * @param s
	 *            the word to hash
	 * @return the hash-code index into the table
	 */
	private static int hashFunction(String s) {
		int hashKey = 0;

		for (int x = 0; x < s.length(); x++) {
			int askey = Math.abs(s.charAt(x) - 47);
			hashKey = (int) ((hashKey * 47 + askey) % tableSize);
		}
		return hashKey;

	}

}
