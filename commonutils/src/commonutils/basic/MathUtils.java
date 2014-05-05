package commonutils.basic;

public class MathUtils {

	public static double round(double d, int numDecimals) {
		
		double roundedVal = d;
		int multiplier = 1;
		
		for ( int i = 0; i < numDecimals; i++ ) {
			roundedVal *= 10;
			multiplier *= 10;
		}
		
		roundedVal = (int)Math.floor(roundedVal + 0.5);
		
//		for ( int i = 0; i < numDecimals; i++ ) {
//			roundedVal /= 10;
//		}
		roundedVal /= multiplier;
		
		return roundedVal;
		
	}
	
	public static double roundToFourDecPlaces( double d ) {
		return round( d, 4 );
	}
	
	public static void main(String[] args) {
		
		double d;
		int numDecimals;
		double roundedD;
		
		numDecimals = 0;

		d = 0.5;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.1;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.9;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.99;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.05;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.09;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.01;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );
		
		numDecimals = 1;

		d = 0.5;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.1;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.9;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.99;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.05;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.09;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.01;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );
		
		numDecimals = 2;

		d = 0.5;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.1;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.9;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.99;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.05;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.09;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );

		d = 0.01;
		roundedD = round(d, numDecimals);
		System.out.println( d + " (" + numDecimals + ") -> " + roundedD );
	}
	
}
