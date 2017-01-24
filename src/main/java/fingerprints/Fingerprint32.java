package fingerprints;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import simhash_impl.BinaryWordSeg;
import simhash_impl.Simhash;

public class Fingerprint32 {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String distHammingThreshold = args[2];
		int k = Integer.parseInt(distHammingThreshold);		
		if (args.length != 3) {
			System.err.println("Usage:");
			System.err.println("1st arg: input filepath");
			System.err.println("2nd arg: output filepath");
			System.err.println("3rd arg: Distance Hamming Threshold(integer)");
			return;
		}
		//Time-counter start
		long start = System.nanoTime();
		// Creates SimHash object.
		Simhash simHash = new Simhash(new BinaryWordSeg());
		// DocHashes is a list that will contain all of the calculated hashes.
		ArrayList<Long> docHashes = Lists.newArrayList();
		// 1. Read the input file.
		// 2. Identify its format and assume its structure
		// 3. Retrieve the documents based on its assumed structure
		// 4. 1 document => 1 string, Printed in output file.
		List<String> docs = readDocs(args);
		System.out.println("");
		System.out.println("Creating output file...");
		File output = new File(args[1]);
		for (int i = 0; i < docs.size(); i++) {
			Files.append("DOC " + i + ": " + docs.get(i) + "\n", output, Charsets.UTF_8);
		}
		System.out.println("Output file is ready!");
		System.out.println("");
		System.out.println("");
		System.out.println("Start to build index...");
		for (String doc : docs) {
			// Calculate the document hash.
			long docHash = simHash.simhash32(doc);
			// Store the document hash in a list.
			docHashes.add(docHash);
		}
		System.out.println("Index is built!");
		System.out.println("");
		System.out.println("");
		System.out.println("You set Hamming Distance Threshold <= " + k);
		// Compare each fingerprint with
		ArrayList<Integer> similars = new ArrayList<Integer>();
		Set<Integer> redundants = new TreeSet<Integer>();
		ArrayList<Integer> uniques = new ArrayList<Integer>();
		for (int i = 0; i < docHashes.size(); i++) {
			for (int j = 0; j < docHashes.size(); j++) {
				if (i != j) {
					int dist = simHash.hammingDistance(docHashes.get(i), docHashes.get(j));
					if (dist <= k) {
						similars.add(j);
						//System.out.println(Long.toBinaryString(docHashes.get(i)));
						//System.out.println(Long.toBinaryString(docHashes.get(j)));
						}
					}
				}
			if (!similars.isEmpty()){
				uniques.add(i);
				System.out.println("DOC: " + i + " considered duplicate with docs: " + similars);
				for (int l = 0; l < similars.size(); l++) {
					if (!uniques.contains(similars.get(l))) {
						redundants.add(similars.get(l));
					}
				}
				similars.clear();
			}
		}
		System.out.println("");
		System.out.println("");
		System.out.println("Number of docs: " + docs.size());
		System.out.println("");
		System.out.println("Remove the following " + redundants.size() + " docs: ");
		System.out.println(redundants);
		System.out.println("");
		System.out.println("...and you'll only have unique docs!");
		System.out.println("");
		System.out.println("Elapsed time: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));

	}
	
	private static boolean isPathValid(String path) {

		try {

			Paths.get(path);
			File inputFile = new File(path);

			if (!inputFile.exists())
				return false;

		} catch (InvalidPathException | NullPointerException ex) {

			return false;
		}

		return true;
	}

	private static List<String> readDocs(String[] args) throws IOException {
		if (isPathValid(args[0])) {
			System.out.println("Path:" + args[0] + " is valid");
			File inputFile = new File(args[0]);
			String fileName = inputFile.getName();	
			String fileType = Files.getFileExtension(fileName);
			if (fileType.equals("csv"))
				return Files.readLines(inputFile, Charsets.UTF_8);
			return readDocsFromTxt(inputFile, Charsets.UTF_8);

		} else
			return null;
	}

	private static List<String> readDocsFromTxt(File inputFile, Charset charset) throws IOException {

		List<String> docs = new ArrayList<>();

		LineIterator lineIterator = FileUtils.lineIterator(inputFile);

		boolean docStart = false;

		String doc = "";
		while (lineIterator.hasNext()) {
			String line = lineIterator.next().trim();

			if (docStart)
				doc = doc + line;

			if (docStart && line.equals("") && !doc.equals("")) {
				docStart = false;
				docs.add(doc);
				doc = "";
			}
			if (line.contains("Content-Length:"))
				docStart = true;

		}
		System.out.println("Number of docs found:" + docs.size());

		return docs;
	}

}
