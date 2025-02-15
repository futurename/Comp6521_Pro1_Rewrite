import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DiskReadSpeedTest {
	public static void main(String[] args) throws IOException {

		File file = new File("./OutFile/");
		File[] files = file.listFiles();
		for (File f : files) {
			f.delete();
		}

		final int BUF_BASE_SIZE = 1024;

		System.gc();
		int times = 10;

		for (; times < 5000; times += 5) {

			long startTime = System.nanoTime();
			final String srcFilename = "./random_group10_10M_5MB.txt";

			int readBufSize = BUF_BASE_SIZE * times;

			char[] bufChArr = new char[readBufSize];

			FileReader fr = new FileReader(srcFilename);
			BufferedReader bfr = new BufferedReader(fr);

			for (int i = 0; i < 10000; i++) {

				bfr.read(bufChArr);
			}

			long endTime = System.nanoTime();
			long runningTime = endTime - startTime;

			long avgTime = runningTime / readBufSize;

			System.out.println("times: " + times + ", avg time for each unit: " + avgTime + " ns");

		}

	}

}
