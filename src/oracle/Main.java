package oracle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import similarity.ModifiedAnalyzer;

public class Main {

	public static void main(String[] args) throws IOException {
		
		if (args.length>2 || args.length==0) {
			System.out.println("Two arguments max:");
			System.out.println("    1 - Target directory (use \\ in your file path)");
			System.out.println("    2 - (Optional) Similarity threshold value (ex: 500)");
			return;
		}
		
		String path = Paths.get(args[0]).toString();

		ModifiedAnalyzer ax = new ModifiedAnalyzer();
		double threshold = args.length==2 ? Double.parseDouble(args[1]) : 400;
		
		String[] filenames = new File(path).list(new FilenameFilter() {
			public boolean accept(File dir, String name)
			  { return !name.equals("Thumbs.db"); }
		});
		Image[] images = new Image[filenames.length];
		
		System.out.println(filenames.length+" pictures - starting now!");
		
		//build image signatures
		//everything will be downscaled (or upscaled) to 300x300
		ax.scaledSize = 300;
		
		for (int i=0;i<filenames.length;i++) {
			BufferedImage base = ImageIO.read(new File(path+"\\"+filenames[i]));
			if (base!=null) {
				BufferedImage rescaled = ax.rescale(base, 300, 300, true);
				images[i] = new Image(filenames[i], ax.calcSignature(rescaled));
			}
			
			if (i%50==0 || i+1==filenames.length) progress(1, i+1, filenames.length);
		}
		
		ArrayList<String> matches = new ArrayList<>();
		int comparisons = 0;
		double maxcomparisons = 1f*images.length*images.length/2f - 0.5*images.length;
		
		//compare signatures
		for (int x=0;x<images.length-1;x++) {
			for (int y=x+1;y<images.length;y++) {
				if (images[x]!=null && images[y]!=null) {
					double dist = ax.quickDistance(images[x].getSignature(), images[y].getSignature());
					if (dist <= threshold) {
						matches.add(String.format("%s <-> %s (value: %.0f)", images[x].getName(), images[y].getName(), dist));
					}
				}
				
				comparisons++;
				if (comparisons%500==0 || comparisons>=maxcomparisons) progress(2, comparisons, maxcomparisons);
			}		
			
		}
		
		System.out.println("\n");
		for (String s: matches)
			System.out.println(s);
		System.out.println(String.format("\nPerformed %d %s with a threshold of %.0f.", 
				comparisons, comparisons==1 ? "comparison" : "comparisons", threshold));
		
	}
	
	private static void progress(int step, double current, double max) {
		
		int perc = 0;
		System.out.print("\r[");
		
		if (step==1) { //0-80 make sigs
			perc = new Double(100f*current/max*0.8).intValue();			
		} else { //80-100 calc dists
			perc = new Double(100f*current/max*0.2+80f).intValue();
		}
		
		for (int x=0;x<10;x++)
			if (x<perc/10) System.out.print("■");
			else System.out.print(" ");
		System.out.print("] "+perc+"%");
	}

}
