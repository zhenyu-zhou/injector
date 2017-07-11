package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util
{
	/**
	 * Get a random object from the given distribution
	 * Note that the probability should sum up to 1 with the error 0.000001
	 * 
	 * @param dist
	 * 			The distribution given by all objects and the corresponding possibility
	 * @see #randomGet(List, double)
	 * @author zzy
	 * @return
	 */
	public static <T> T randomGet(List<Pair<T, Double>> dist)
	{
		return randomGet(dist, 0.000001);
	}

	/**
	 * Get a random object from the given distribution
	 * 
	 * @param dist
	 * 			The distribution given by all objects and the corresponding possibility
	 * @param epsilon
	 * 			The error could be tolerated if the possibilities do not sum up to 1
	 * 			Must be \in [0, 0.1]
	 * @throws RuntimeException
	 * 			When epsilon is not valid or the probability sum error cannot be tolerated
	 * @see #randomGet(List)
	 * @author zzy
	 * @return
	 * 			A random object from the distribution
	 */
	public static <T> T randomGet(List<Pair<T, Double>> dist, double epsilon)
	{
		if (epsilon < 0 || epsilon > 0.1)
		{
			throw new RuntimeException("Invalid epsilon");
		}

		List<Double> cdf = new ArrayList<Double>();
		double p = 0;
		int i = 0;
		int size = dist.size();

		for (Pair<T, Double> d : dist)
		{
			p += d.getSecond();
			cdf.add(p);
		}
		if (Math.abs(1 - p) > epsilon)
		{
			throw new RuntimeException(
					"Probability should sum up to 1, but in fact " + p);
		}

		Random r = new Random();
		p = r.nextDouble();
		while (i < size && p > cdf.get(i))
		{
			i++;
		}

		// zzy: Remove the condition `epsilon > 0`
		// Avoid the ArrayOutOfBoundException when p \in (1-epsilon, 1)
		if (i == size)
			i--;

		return dist.get(i).getFirst();
	}
}
