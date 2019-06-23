package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import similarity.Analyzer;

public class Tests {
	
	private Analyzer ax;
	private File car;
	private File car_bw;
	private File tree_png;
	private File tree_png_mini;
	private File tree_jpg;
	private File tree_2;
	private File tiny;
	private double delta = 0.00001;

	@Before
	public void setUpClass() {
		ax = new Analyzer();
		car = new File("src/tests/car.png");
		car_bw = new File("src/tests/car_black_and_white.png");
		tree_png = new File("src/tests/png_tree.png");
		tree_png_mini = new File("src/tests/png_tree_mini.png");
		tree_jpg = new File("src/tests/jpg_tree.jpg");
		tree_2 = new File("src/tests/different_tree.png");
		tiny = new File("src/tests/tiny.png");
	}
	
	@Test
	public void equal() throws IOException {
		assertEquals(ax.compare(car, car),0,delta);
		assertEquals(ax.compare(car_bw, car_bw),0,delta);
		assertEquals(ax.compare(tree_png, tree_png),0,delta);
		assertEquals(ax.compare(tree_png_mini, tree_png_mini),0,delta);
		assertEquals(ax.compare(tree_jpg, tree_jpg),0,delta);
		assertEquals(ax.compare(tree_2, tree_2),0,delta);
		assertEquals(ax.compare(tiny, tiny),0,delta);
	}
	
	@Test
	public void similar() throws IOException {
		assertTrue(ax.compareThreshold(tree_png, tree_jpg, 10));
		assertTrue(ax.compareThreshold(tree_png, tree_png_mini, 20));
		assertTrue(ax.compareThreshold(tree_jpg, tree_png, 50));
		assertTrue(ax.compareThreshold(car, car_bw, 500));
	}
	
	@Test
	public void distant() throws IOException {		
		assertFalse(ax.compareThreshold(tree_2, tree_png, 500));
		assertFalse(ax.compareThreshold(tiny, tree_png, 1000));
		assertFalse(ax.compareThreshold(car, tree_2, 2000));
		assertFalse(ax.compareThreshold(car_bw, tree_2, 2500));
	}
	
}
