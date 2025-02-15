import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.function.LongToDoubleFunction;

public class GetNumCount {
	public static void main(String[] args) throws IOException {
		final String srcFilename = "./Random_50M.txt";
		long start = System.currentTimeMillis()
		/*
		RandomAccessFile rFile = new RandomAccessFile(srcFilename, "r");
		long size = rFile.length() / 4;
		
		
		
		System.out.println("size: " + size + ", length: " + rFile.length());*/
		
		BufferedReader bfr = new BufferedReader(new FileReader(srcFilename));
		
		bfr.readLine();
		bfr.readLine();
		
		String[] arrays = bfr.readLine().split(" ");
		
		System.out.println("total numbers: " + arrays.length);
		
		long end = System.currentTimeMillis();
		long run = end - start;
		
		System.out.println("run time: " + run + " ms");
		
	}
}
