
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

public class TPMMS {
	static long totalWriteTime = 0;
	static long totalReadTime = 0;
	static int unFinishNumInBuf = -1;
	static final int BUF_BASE_SIZE = 51200;
	static int fileCounter = 0;

	public static void main(String[] args) throws IOException {
		final String srcFilename = "./random_group10_10M_5MB.txt";
		final String outFileBase = "./TempOutFile/!!!Output_";
		
		System.out.println("\nfile to be sorted: " + srcFilename.substring(2) + "\n\n");
		
		String exsitFilename = "./".concat(outFileBase.substring(14)).concat("Sorted_Result.txt");		
				
		File existFile = new File(exsitFilename);
		if(existFile.exists()) {
			existFile.delete();
		}

		File outDir = new File("./TempOutFile/");
		outDir.mkdir();

		System.gc();

		Long startTime = System.currentTimeMillis();

		int times = 10;

		

		passOne(srcFilename, outFileBase, times);

		passTwo(outFileBase);

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;

		System.out.println("\ntotal time: " + runTime + " ms");	
		
		System.out.println("\n*** Manually delete all temporary and sorted files/folder before next running! ***");
		
	}

	private static void passTwo(String srcFileBase) throws IOException {
		long startTime = System.currentTimeMillis();

		long freeMem = Runtime.getRuntime().freeMemory();

		int bufSize = (int) (freeMem / 4 * 0.6);

		MappedByteBuffer[] mapBufArray = new MappedByteBuffer[fileCounter];

		for (int i = 0; i < fileCounter; i++) {
			String curFilename = srcFileBase.concat(String.valueOf(i)).concat(".txt");

			RandomAccessFile curCAF = new RandomAccessFile(curFilename, "rw");

			mapBufArray[i] = curCAF.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, curCAF.length());
		}

		int[] curReadBufArray = new int[fileCounter];

		for (int i = 0; i < fileCounter; i++) {
			curReadBufArray[i] = mapBufArray[i].getInt();
		}

		String outputFilename = srcFileBase.substring(14).concat("Sorted_Result.txt");
		boolean isFinished = false;
		int curMin = -1, curMinRow;
		int[] outputBuf = new int[bufSize];

		final int BIG_NUMBER = 10000000;

		while (!isFinished) {
			for (int i = 0; i < bufSize; i++) {
				curMinRow = IntStream.range(0, fileCounter)
						.reduce((m, n) -> curReadBufArray[m] > curReadBufArray[n] ? n : m).getAsInt();
				curMin = curReadBufArray[curMinRow];

				if (curMin != BIG_NUMBER) {
					outputBuf[i] = curMin;
					if (mapBufArray[curMinRow].hasRemaining()) {
						curReadBufArray[curMinRow] = mapBufArray[curMinRow].getInt();
					} else {
						curReadBufArray[curMinRow] = BIG_NUMBER;
					}
				} else {
					int[] lastOutputBuf = new int[i];
					System.arraycopy(outputBuf, 0, lastOutputBuf, 0, i - 1);
					passtwoWriteToFile(outputFilename, lastOutputBuf);
					isFinished = true;
					break;
				}
			}
			if (!isFinished) {
				passtwoWriteToFile(outputFilename, outputBuf);
			}
		}

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;

		System.out.println("pass two time: " + runTime + " ms");
	}

	private static void passtwoWriteToFile(String outputfilename, int[] outputBuf) throws IOException {

		int size = outputBuf.length;

		BufferedWriter bfw = new BufferedWriter(new FileWriter(outputfilename, true));

		for (int i = 0; i < size; i++) {
			bfw.write(String.valueOf(outputBuf[i]));
			bfw.write('\t');
		}
		bfw.close();
	}

	private static void passOne(String srcFilename, String outFileBase, int times) throws IOException {
		long startTime = System.currentTimeMillis();

		int readBufSize = BUF_BASE_SIZE * times;

		RandomAccessFile rFile = new RandomAccessFile(srcFilename, "r");
		FileChannel fChannel = rFile.getChannel();

		int readPos = 0;

		ArrayList<Integer> bufList = new ArrayList<>(readBufSize);

		MappedByteBuffer mappedByteBuffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, rFile.length());
		int enterCh;
		boolean isFirstBuf = true;

		while (mappedByteBuffer.hasRemaining()) {
			if (isFirstBuf) {
				isFirstBuf = false;
				enterCh = mappedByteBuffer.get();

				while (enterCh != 13) {
					enterCh = mappedByteBuffer.get();
				}

				readPos = mappedByteBuffer.position();

				readAndSaveNumbers(bufList, mappedByteBuffer, readPos, readBufSize, outFileBase);
			} else {
				int mapPos = mappedByteBuffer.position();
				int unreadbytes = mappedByteBuffer.capacity() - mapPos;
				int readLimit = unreadbytes > readBufSize ? readBufSize : unreadbytes;

				readAndSaveNumbers(bufList, mappedByteBuffer, 0, readLimit, outFileBase);
			}
		}

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		System.out.println("pass one time: " + runTime + " ms");

		rFile.close();
		fChannel.close();

	}

	private static void readAndSaveNumbers(ArrayList<Integer> bufList, MappedByteBuffer mappedByteBuffer, int readPos,
			int readBufSize, String outFileBase) throws IOException {

		long startTime = System.currentTimeMillis();

		int partOne, partTwo, tempNum = -1, curDigit;

		for (int i = readPos + 1; i < readBufSize; i++) {
			int curAsc = mappedByteBuffer.get();

			curDigit = curAsc - 48;

			if (curDigit >= 0 && curDigit <= 9) {
				if (unFinishNumInBuf != -1) {
					tempNum = unFinishNumInBuf;
					unFinishNumInBuf = -1;

					partOne = tempNum << 3;
					partTwo = tempNum << 1;
					tempNum = partOne + partTwo + curDigit;
					continue;
				}
				if (tempNum == -1) {
					tempNum = curDigit;
				} else {
					partOne = tempNum << 3;
					partTwo = tempNum << 1;
					tempNum = partOne + partTwo + curDigit;
				}
			} else {
				if (unFinishNumInBuf != -1) {
					bufList.add(unFinishNumInBuf);
					unFinishNumInBuf = -1;
				} else if (tempNum != -1) {
					bufList.add(tempNum);
					tempNum = -1;
				}
			}
		}

		curDigit = mappedByteBuffer.get() - 48;
		if (curDigit >= 0 && curDigit <= 9) {
			if (tempNum == -1) {
				unFinishNumInBuf = curDigit;
			} else {
				partOne = tempNum << 3;
				partTwo = tempNum << 1;
				tempNum = partOne + partTwo + curDigit;

				unFinishNumInBuf = tempNum;
			}
		} else {

			bufList.add(tempNum);
			tempNum = -1;
		}

		Collections.sort(bufList);

		String outFilename = outFileBase.concat(String.valueOf(fileCounter)).concat(".txt");

		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;

		totalReadTime += runTime;

		writeToFile(bufList, outFilename);
		fileCounter++;

		bufList.clear();

	}

	private static void writeToFile(ArrayList<Integer> bufList, String outFilename) throws IOException {
		RandomAccessFile aFile = new RandomAccessFile(outFilename, "rw");

		FileChannel fc = aFile.getChannel();

		MappedByteBuffer out = fc.map(FileChannel.MapMode.READ_WRITE, aFile.length(), bufList.size() * Integer.BYTES);

		for (Integer num : bufList) {
			out.putInt(num);
		}
	}

}
