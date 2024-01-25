
/**
 * @author Khang Tran
 * Program: Creating and Exponentially-Searching a Binary File
 * Part B of Program 1. The program reads binary file and print to the screen the content of the binary file.
 * The program prints the first three records of data, the middle three records(or four if the quantity of records 
 * is even), and the last three records of data. Then the program display the total number of records in the 
 * binary file. Next, it prints the top ten items contain the most apparent depths. Also, display all of the items
 * with apparent depths that tie the depth of the tenth–deepest. When the binary file does not contain at least 
 * ten records, print as many as exist for each of the four groups of records. Lastly, allow the user to provide
 * name of items,  for each name locate within the binary file using exponential binary search,
 * , and display to the screen the same four field values (name, diameter, apparent depth, and age). 
 * Programming language: JavaSE-16
 * 
 */
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Prog1B {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RandomAccessFile binFile = null;// RAF specializes in binary file I/O

		// Determine how many records are in the binary file,
		// also retrieve information stored on top of the file.
		long numberOfRecords = 0; // Quantity of records in the binary file
		long recordLength = 0; // length in bytes of each record
		int craterNameMaxLength = 0; // max length of field name
		int ageMaxLength = 0;// max length of field age

		// information of long(8 bytes) recordLength,
		// and int(4 + 4) craterName and age max length.
		long header = 8 + 4 + 4;
		long pointer = header;

		// Check if a file path is provided as a command line argument
		if (args.length != 1) {
			System.out.println("Please provide the file pathname as a command line argument.");
			return;
		}

		// Retrieve the file path from the command line argument
		// and use the fileName for reading the file
		String filePath = args[0];
		File file = new File(filePath);
		String fileName = file.getName();

		// Open the binary file of data for reading.
		try {
			binFile = new RandomAccessFile(fileName, "r");
		} catch (IOException e) {
			System.out.println("I/O ERROR: Something went wrong with the " + "opening of the RandomAccessFile object.");
			System.exit(-1);
		}

		// seek to the beginning of the binary file
		try {
			binFile.seek(0);
		} catch (IOException e) {
			System.out.println("I/O ERROR: Seems we can't reset the file " + "pointer to the start of the file.");
			System.exit(-1);
		}

		// Read and store information on top of the binary file
		try {
			recordLength = binFile.readLong();
			craterNameMaxLength = binFile.readInt();
			ageMaxLength = binFile.readInt();
		} catch (IOException e) {
			System.out.println("I/O ERROR: Failed to read data from the binary file.");
			System.exit(-1);
		}

		// Find the total number of records
		try {
			numberOfRecords = (binFile.length() - header) / recordLength;// minus header for the information at the top
																			// of the
																			// file
		} catch (IOException e) {
			System.out.println("I/O ERROR: Couldn't get the file's length.");
			System.exit(-1);
		}

		// Print the content of the Crater name, Diameter, Apparent depth, and
		// age fields of the first three records of data, the middle three or four if
		// records are even and the last three to screen

		// If the binary file does not contain at least ten records, print as many as
		// exist for each of the four groups of records.
		if (numberOfRecords < 10) {
			int recordToPrint = (int) numberOfRecords;
			try {
				for (int i = 0; i < 3; i++) {
					printRecords(binFile, pointer, recordToPrint, craterNameMaxLength, ageMaxLength);
				}
			} catch (IOException e) {
				System.out.println("I/O ERROR: Couldn't get the binary file.");
				System.exit(-1);
			}
		}

		// If the binary file contains more than ten records then print as usual
		else {
			try {
				// Print the first three
				int recordToPrint = 3;
				printRecords(binFile, pointer, recordToPrint, craterNameMaxLength, ageMaxLength);
				// Print the middle three or four
				long middleRecord = numberOfRecords / 2;

				// Print the middle four records if the quantity of records is even
				if (numberOfRecords % 2 == 0) {
					recordToPrint = 4;
					pointer = header + (middleRecord * recordLength) - (recordLength * 2);
					printRecords(binFile, pointer, recordToPrint, craterNameMaxLength, ageMaxLength);
				}
				// Print three if not even.
				else {
					recordToPrint = 3;
					pointer = header + (middleRecord * recordLength) - (recordLength);
					printRecords(binFile, pointer, recordToPrint, craterNameMaxLength, ageMaxLength);
				}

				// Print the last 3 records.
				recordToPrint = 3;
				pointer = binFile.length() - (recordLength * 3);
				printRecords(binFile, pointer, recordToPrint, craterNameMaxLength, ageMaxLength);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("I/O ERROR: Couldn't get the binary file.");
				System.exit(-1);
			}
		}

		// Display the total number of records in the binary file
		System.out.println("Total number of records: " + numberOfRecords);

		// Conclude the output with a list of ten craters having the largest apparent
		// depths. Also, display craters with depths that tie the depth of the
		// tenth-deepest crater
		if (numberOfRecords >= 10) {
			try {
				List<Crater> craters = findTopTen(binFile, header, craterNameMaxLength, ageMaxLength, recordLength);
				System.out.println("Top ten craters having the largest apparent depths: ");
				for (Crater crater : craters) {
					System.out.println(crater.getName() + ": " + crater.getDepth());
				}
			} catch (IOException e) {
				System.out.println("I/O ERROR: Couldn't read binary file to get the top ten deepest depth");
				System.exit(-1);
			}
		}
		// If number of records less than ten than print as many as possible in the
		// correct order
		else {
			try {
				List<Crater> craters = listOfTopTen(binFile, header, craterNameMaxLength, ageMaxLength,
						numberOfRecords);
				System.out.println("Top craters having the largest apparent depths: ");
				for (Crater crater : craters) {
					System.out.println(crater.getName() + ": " + crater.getDepth());
				}
			} catch (IOException e) {
				System.out.println("I/O ERROR: Couldn't read binary file to get the top ten deepest depth");
				System.exit(-1);
			}
		}

		// Allow the user to provide crater names. Then use exponential binary search to
		// search for the name and print to the screen the values of that name. Print
		// 'not found' if name doesn't exist in binary file.
		try {
			Scanner scanner = new Scanner(System.in);
			while (true) {
				System.out.println("Enter a crater name or 'e' or 'E' to exit:");
				String name = scanner.nextLine();
				if (name.equalsIgnoreCase("e")) // exit if user enter 'e' or 'E'
					break;
				int index = exponentialBinarySearch(binFile, name, recordLength, header, craterNameMaxLength,
						ageMaxLength, numberOfRecords); // get index to for pointer to seek for in printRecords method
				if (index == -1) { // if index is -1 then the name is not found or user enter it wrong
					System.out.println("Crater not found.");
				}
				// print to screen if found
				else {
					printRecords(binFile, index, 1, craterNameMaxLength, ageMaxLength);
				}
			}
			scanner.close();
		} catch (IOException e) {
			System.out.println("I/O ERROR: Failed to read data from the binary file.");
			System.exit(-1);
		}

		// close the binary file
		try {
			binFile.close();
		} catch (IOException e) {
			System.out.println("I/O ERROR: Couldn't close binary file");
			System.exit(-1);
		}

	}

	/**
	 * Method for printing the first, middle, last three(four) records. Seek to the
	 * referent pointer, then use read() function from RandomAccessFile to help
	 * printing out the information to screen.
	 * 
	 * @param binFile
	 * @param start
	 * @param recordToPrint
	 * @param craterNameMaxLength
	 * @param ageMaxLength
	 * @throws IOException
	 */
	public static void printRecords(RandomAccessFile binFile, long start, int recordToPrint, int craterNameMaxLength,
			int ageMaxLength) throws IOException {
		binFile.seek(start);
		for (int i = 0; i < recordToPrint; i++) {
			byte[] craterNameLength = new byte[craterNameMaxLength];
			binFile.readFully(craterNameLength);
			String craterName = new String(craterNameLength).trim();
			System.out.print("[" + craterName + "]");

			// print diameter
			System.out.print("[" + binFile.readDouble() + "]");

			// skip latitude, longitude, apparent diameter,
			// and floor diameter to print apparent depth
			binFile.skipBytes(8 * 4);
			System.out.print("[" + binFile.readDouble() + "]");

			// skip interior volume and height of central peak
			// to print age field
			binFile.skipBytes(8 * 2);
			byte[] ageLength = new byte[ageMaxLength];
			binFile.readFully(ageLength);
			String age = new String(ageLength).trim();
			System.out.println("[" + age + "]");
		}
	}

	/**
	 * Method to find the top ten craters having the largest apparent depths. Make
	 * use of the listOfTopTen method to first create a list of the first ten
	 * records. After creating a list, seek to the eleventh record and start looking
	 * for the top ten of all.
	 * 
	 * @param binFile
	 * @param header
	 * @param craterNameMaxLength
	 * @param AgeMaxLength
	 * @param recordLength
	 * @return
	 * @throws IOException
	 */
	public static List<Crater> findTopTen(RandomAccessFile binFile, long header, int craterNameMaxLength,
			int AgeMaxLength, long recordLength) throws IOException {
		// Get an initial list for the first ten records
		List<Crater> craters = listOfTopTen(binFile, header, craterNameMaxLength, AgeMaxLength, 10);
		long currentLength = recordLength * 10 + header;// for keeping track in binary file
		binFile.seek(currentLength); // seek to the eleventh record after getting a list of initial top ten

		// keep finding the top ten craters until out of binary file length
		while (currentLength < binFile.length()) {
			currentLength += recordLength; // increment current each time a record is read
			// store crater's name and depth to put to the list if condition is met
			byte[] craterNameLength = new byte[craterNameMaxLength];
			binFile.readFully(craterNameLength);
			String craterName = new String(craterNameLength).trim();
			binFile.skipBytes(8 * 5);// skip name and 5 other fields to get depth
			double depth = binFile.readDouble();
			binFile.skipBytes(AgeMaxLength + (8 * 2));// skip age and two other double fields to get to new record

			// if depth equals to the top ten depth then add for ties
			if (depth == craters.get(craters.size() - 1).getDepth()) {
				craters.add(new Crater(craterName, depth));
			}
			// if depth is greater than the top ten depth then remove the
			// top ten and its tie until no ties is found or the list
			// is less than ten elements
			else if (depth > craters.get(craters.size() - 1).getDepth()) {
				int lastIndex = craters.size() - 1;

				// check if the last element in the list is equal to the previous one.
				if (craters.get(lastIndex - 1).getDepth() == craters.get(lastIndex).getDepth()) {

					// If it is equal, then remove the ties until the list has less than
					// 10 craters.
					while (craters.get(lastIndex - 1).getDepth() == craters.get(lastIndex).getDepth()
							&& craters.size() > 9) {
						craters.remove(lastIndex);
						lastIndex--;
					} // end while
				}

				// if the least deepest doesn't have ties, then remove it
				else {
					craters.remove(lastIndex);
				}
				// Add the new deeper after removing the last one
				craters.add(new Crater(craterName, depth));
				Collections.sort(craters, Comparator.comparing(Crater::getDepth).reversed());
			}

		}
		return craters;
	}

	/**
	 * Method for creating an initial top list of the first ten records. Also used
	 * for when number of records are less than 10.
	 * 
	 * @param binFile
	 * @param header
	 * @param craterNameMaxLength
	 * @param AgeMaxLength
	 * @param numberOfRecords
	 * @return
	 * @throws IOException
	 */
	public static List<Crater> listOfTopTen(RandomAccessFile binFile, long header, int craterNameMaxLength,
			int AgeMaxLength, long numberOfRecords) throws IOException {
		binFile.seek(header); // seek to pointer
		List<Crater> craters = new ArrayList<>();// create a list of craters

		// Get the initial records into the list, store the crater's name and depth by a
		// crater object
		for (int i = 0; i < numberOfRecords; i++) {
			byte[] craterNameLength = new byte[craterNameMaxLength];
			binFile.readFully(craterNameLength);
			String craterName = new String(craterNameLength).trim();

			binFile.skipBytes(8 * 5);// skip 5 fields to get to depth
			double depth = binFile.readDouble();
			craters.add(new Crater(craterName, depth));// add crater object to lsit

			binFile.skipBytes(AgeMaxLength + (8 * 2)); // Skip age and two other double fields.
		}
		// Sort the list
		Collections.sort(craters, Comparator.comparing(Crater::getDepth).reversed());
		return craters;

	}

	/**
	 * Extension of normal binary search. Used for a faster search. Double the
	 * search every time until the index is invalid or an element is greater than or
	 * equal to the desired target.
	 * 
	 * @param craters
	 * @param target
	 * @return
	 * @throws IOException
	 */
	public static int exponentialBinarySearch(RandomAccessFile binFile, String target, long recordLength, long header,
			int craterNameMaxLength, int ageMaxLength, long numberOfRecords) throws IOException {
		int i = 0;
		int lineInBin = 0;
		int checkBound = 0;
		int currentLength = (int) header;

		while (checkBound < binFile.length()) {
			lineInBin = 2 * ((int) (Math.pow(2, i)) - 1);
			currentLength = (int) ((lineInBin) * recordLength + header);
			binFile.seek(currentLength);
			byte[] craterNameLength = new byte[craterNameMaxLength];
			binFile.readFully(craterNameLength);
			String craterName = new String(craterNameLength).trim();

			// return current byte to know where the target is in the binary file
			if (craterName.equals(target))
				return currentLength;
			else if (craterName.compareTo(target) > 0) {
				break;
			} else
				i++;
			// Check for out of bound after incrementing i
			checkBound = (int) (recordLength * (2 * ((int) (Math.pow(2, i)) - 1)));
		}

		int left = 2 * (((int) Math.pow(2, i - 1)) - 1) + 1;
		int right = (int) Math.min(2 * (((int) Math.pow(2, i)) - 1) - 1, numberOfRecords);
		return binarySearch(binFile, left, right, target, recordLength, header, craterNameMaxLength, ageMaxLength,
				numberOfRecords);
	}

	/**
	 * Normal Binary search
	 * 
	 * @param craters
	 * @param left
	 * @param right
	 * @param target
	 * @return
	 * @throws IOException
	 */
	public static int binarySearch(RandomAccessFile binFile, int left, int right, String target, long recordLength,
			long header, int craterNameMaxLength, int ageMaxLength, long numberOfRecords) throws IOException {

		byte[] craterNameLength = new byte[craterNameMaxLength];
		String craterName;

		while (left <= right) {
			long mid = left + ((right - left) / 2);
			int currentLength = (int) (mid * recordLength + header);
			binFile.seek(currentLength);
			binFile.readFully(craterNameLength);
			craterName = new String(craterNameLength).trim();
			if (craterName.equals(target))
				return currentLength;
			if (craterName.compareTo(target) < 0)
				left = (int) (mid + 1);
			else
				right = (int) (mid - 1);
		}

		return -1;
	}

	/**
	 * Class Crater
	 * 
	 * @author Khang Tran
	 * 
	 *         Purpose: An object of this class holds the field values of record of
	 *         data.
	 */

	public static class Crater {

		// Class constants
		private String name;
		private double depth;

		// Constructor
		public Crater(String name, double depth) {
			this.name = name;
			this.depth = depth;
			;
		}

		// 'Getters' for the data field values
		public String getName() {
			return name;
		}

		public double getDepth() {
			return depth;
		}

	}
}
