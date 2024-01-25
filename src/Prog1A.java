
/**
 * @author Khang Tran
 * Program: Creating and Exponentially-Searching a Binary File
 * Part A of Program #1. The program creates a binary file version of the provided text file's content
 * whose records are stored in the same order as provided in the data file. The program accept the
 * complete data file pathname as a command line argument. For String field type, avoid wasting storage
 * by choosing an excessive maximum length, and the maximum length are stored at the top of the binary file.
 * Programming language: JavaSE-16
 * 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Prog1A {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Check if a file path is provided as a command line argument
		if (args.length != 1) {
			System.out.println("Please provide the file pathname as a command line argument.");
			return;
		}

		// Retrieve the file path from the command line argument
		// and create a fileName with a bin extension to create
		// the binary file
		String filePath = args[0];
		File file = new File(filePath);
		String fileName = file.getName().replace(".csv", ".bin");

		// Read the whole provided file to find the max length of string fields.
		// Also find the length of a record by adding the bytes of the two string fields
		// and 8 double fields.
		long recordLength = 0;
		int craterNameMaxLength = 0;
		int ageMaxLength = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line = br.readLine(); // skip the field names found in the first line of the file

			// Handle the situation where a file has no records
			if (line == null) {
				System.out.println("The provided file is empty");
				System.exit(-1);
			}
			// If the file has records then proceed to find max length of strings and record
			else {
				while ((line = br.readLine()) != null) {
					String[] fields = line.split(",");
					int craterNameLength = fields[0].length();
					// the last column usually has missing values so the
					// program checks for the ones that have values to retrieve
					// the maximum length of the last column
					if (fields.length > 9) {
						int ageLength = fields[9].length();
						if (ageLength > ageMaxLength) {
							ageMaxLength = ageLength;
						}
					}
					if (craterNameLength > craterNameMaxLength) {
						craterNameMaxLength = craterNameLength;
					}
				}
				recordLength = craterNameMaxLength + (8 * 8) + ageMaxLength;
			}

		} catch (IOException e) {
			System.out.println("I/O ERROR: Couldn't open, or couldn't read " + "from, the CSV file. "
					+ "Maybe the file doesn't exist?");
			System.exit(-1);
		}

		// Create the binary file using RAF
		File fileRef = null; // provides exists(), delete()
		RandomAccessFile binFile = null;

		// If an old version of this binary file exists, delete and start fresh.
		try {
			fileRef = new File(fileName);
			if (fileRef.exists()) {
				fileRef.delete();
			}
		} catch (Exception e) {
			System.out.println("I/O ERROR: Something went wrong with the " + "deletion of the previous binary file.");
			System.exit(-1);
		}

		// Create the binary file
		try {
			binFile = new RandomAccessFile(fileRef, "rw");
		} catch (IOException e) {
			System.out
					.println("I/O ERROR: Something went wrong with the " + "creation of the RandomAccessFile object.");
			System.exit(-1);
		}

		// Place the maximum length of string fields and record
		// at the top of the binary file
		try {
			binFile.writeLong(recordLength);
			binFile.writeInt(craterNameMaxLength);
			binFile.writeInt(ageMaxLength);
		} catch (IOException e) {
			System.out.println("I/O ERROR: Couldn't write maximum length to the top " + "the binary file.");
			System.exit(-1);
		}

		// After retrieving the maximum length, proceed to
		// read the provided file and convert to binary file.
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] fields = line.split(",");
				for (int i = 0; i < fields.length; i++) {
					// handle first string field. pad strings on the right with spaces to reach the
					// needed length
					if (i == 0) {
						if (fields[i].length() < craterNameMaxLength) {
							StringBuffer nameBuffer = new StringBuffer(fields[i]);
							addLength(nameBuffer, craterNameMaxLength);
							binFile.writeBytes(nameBuffer.toString());
						} else {
							StringBuffer nameBuffer = new StringBuffer(fields[i]);
							binFile.writeBytes(nameBuffer.toString());
						}
					}
					// handle the last string field if exists
					else if (i == 9) {
						if (fields[i].length() < ageMaxLength) {
							StringBuffer ageBuffer = new StringBuffer(fields[i]);
							addLength(ageBuffer, ageMaxLength);
							binFile.writeBytes(ageBuffer.toString());
						} else {
							StringBuffer ageBuffer = new StringBuffer(fields[i]);
							binFile.writeBytes(ageBuffer.toString());
						}
					} else {
						binFile.writeDouble(Double.parseDouble(fields[i]));
					}

				}
				// if the last string field doesn't exist, assign null to the field
				if (fields.length < 10) {
					String temp = "null";
					StringBuffer ageBuffer = new StringBuffer(temp);
					addLength(ageBuffer, ageMaxLength);
					binFile.writeBytes(ageBuffer.toString());
				}
			}
		} catch (IOException e) {
			System.out.println("I/O ERROR: Couldn't open, or couldn't read " + "from, the CSV file.");
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
	 * Method to pad strings on the right with spaces to reach the needed length
	 * 
	 * @param obj
	 * @param maxLength
	 */
	private static void addLength(StringBuffer obj, int maxLength) {
		int lengthToAdd = maxLength - obj.length();
		int count = 0;
		while (count < lengthToAdd) {
			obj.append(' ');
			count++;
		}
	}

}
