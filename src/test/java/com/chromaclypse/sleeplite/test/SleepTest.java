package com.chromaclypse.sleeplite.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.chromaclypse.sleeplite.Control;
import com.chromaclypse.sleeplite.SleepConfig.SleepWorld;

@RunWith(Parameterized.class)
public class SleepTest {
	private int sleeping;
	private int total;
	private int minimum;
	private int maximum;
	private double ratio;
	private int expected;
	
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{0, 1, 1, 100, 0.5, 1},
			{2, 5, 1, 100, 0.5, 1},
			{14, 200, 100, 100, 0.5, 86},
			{14, 15, 100, 100, 0.5, 1},
			{10, 100, 1, 10, 0.5, 0},
			{1, 1, 2, 20, 0.5, 0},
			{1, 2, 2, 20, 0.5, 1},
			{3, 4, 4, 20, 0.5, 1},
		});
	}
	
	public SleepTest(int sleeping, int total, int minimum, int maximum, double ratio, int expected) {
		this.sleeping = sleeping;
		this.total = total;
		this.minimum = minimum;
		this.maximum = maximum;
		this.ratio = ratio;
		this.expected = expected;
	}
	
	@Test
	public void testControl() {
		SleepWorld world = new SleepWorld();
		Control control = new Control(null);
		
		world.minimum = minimum;
		world.maximum = maximum;
		world.ratio = ratio;

		assertEquals(expected, control.getNeeded(sleeping, total, world));
	}
}
