/***
	programmed by Yunho Hong
	This is based on the Mark Orr matlab code
	May 12, 2002
***/

package org.cougaar.tools.alf.sensor.rbfnn;

import java.util.*;
import java.io.*;
import java.lang.*;
// import ultrarbf.*;

public class Kmeans {

	double [][] X;
	int [] label;

	double [][] m; // Center
	double [] w; // Width

	int num_training;
	int dimension;
	int num_hidden;

	BufferedReader br;

	public Kmeans(double [][] x, int num_hidden1) {
		
		X = x;
		num_hidden = num_hidden1;
		num_training = X.length;
		dimension = X[0].length;

		//  open up standard input 
//	    br = new BufferedReader(new java.io.InputStreamReader(System.in)); 

	}

	public double [] getWidth() {

		double [] w = new double[num_hidden];

		for (int i=0;i < num_hidden;i++)
		{
			double nearestDistance = Double.MAX_VALUE;
			
			for (int j=0;j < num_hidden;j++)
			{
				if (i!=j)
				{
					double d = distanceBetween(m[i],m[j]);
					if (d < nearestDistance)
					{
						nearestDistance = d;
					}
				}
			}
			
			w[i] = nearestDistance;
		}

		return w;
	}

	public double[][] clustering() {

		// initialize center
		m = new double[num_hidden][dimension];
		double [][] oldm = new double[num_hidden][dimension];		
//		double [][] cov = new double[dimension][dimension];				
		double [] stds = new double[dimension];		
		double [] means = new double[dimension];		
		label = new int[num_training];

		int i,j,l,a; 

		Random rn = new Random(System.currentTimeMillis());
		
		// finding mean
		for (j=0;j<dimension;j++) {		means[j] = 0;	}
		
		for (j=0;j<dimension;j++)
		{
			for (i=0;i<num_training;i++)
			{
				means[j] = means[j] + X[i][j];
			}
		}

		for (j=0;j<dimension;j++) {	means[j] = means[j]/num_training;	}

/*****/
//		Matrix.showVector("means", means);
/*****/

		for (l=0;l<dimension;l++)
		{
			double VAR = 0;


			for (a=0;a<num_training;a++)
			{
					VAR = VAR + (X[a][l]-means[l])*(X[a][l]-means[l]);
			}
				
			stds[l] = Math.sqrt(VAR/(num_training-1));
		}
		

/*
		// Covariance
		for (k=0;k<dimension;k++)
		{
			for (l=0;l<dimension;l++)
			{
				double COV = 0;

				// covariance
				for (a=0;a<num_training;a++)
				{
					COV = COV + (X[a][k]-means[k])*(X[a][l]-means[l]);

				}
				
				cov[k][l] = COV/(num_training-1);
			}
		}		

		for (i=0;i<num_hidden;i++)
		{
			for (j=0;j<dimension;j++)
			{
				double t =0, sqrtCOV = 0, COV = 0;
				
				for (a=0;a<dimension;a++)
				{
					if (cov[j][a] < 0)
					{
						COV = cov[j][a]*-1;
						sqrtCOV = -1 * java.lang.Math.sqrt(COV);
					} else {
						sqrtCOV = java.lang.Math.sqrt(cov[j][a]);
					}
					t = t + sqrtCOV*rn.nextGaussian();
				}

				m[i][j] = t + means[j];
			}
		}
*/
			

		// calc range
		double [] range = new double[dimension];
		
		for (i=0;i<dimension;i++)
		{
			double min=Double.MAX_VALUE, max = 0;
			for (j=0;j<num_training;j++ )
			{
				if (min > X[j][i])
				{
					min = X[j][i];
				}

				if (max < X[j][i])
				{
					max = X[j][i];
				}
			}
			range[i] = max - min;
		}
		
		
		for (i=0;i<num_hidden;i++)
		{
//			int q = rn.nextInt(num_training);
			for (j=0;j<dimension;j++)
			{
//				m[i][j] = means[j] + stds[j]*rn.nextGaussian();
				m[i][j] = range[j]*rn.nextDouble();
//				m[i][j] = X[q][j];
			}
		}
		
/****/
//			showMatrix("X", X,num_training,dimension);
/****/

/****/
//			Matrix.showVector("stds", stds);
/****/


/****/
//			showMatrix("m", m,num_hidden, dimension);
/****/

/****/
//			showMatrix("oldm", oldm,num_hidden, dimension);
/****/

		while (isDifferent(m,oldm))
		{
/****/
//			showMatrix("m", m,num_hidden, dimension);
/****/
			// find the near center
			for (i=0;i<num_training;i++)
			{
				label[i] = nearestCenter(X[i]);
			}

//			Matrix.showVector("label",label);

			// calculate new center
			copyMtoOldM(m,oldm);
/****/
//			showMatrix("oldm", oldm,num_hidden, dimension);
/****/

			calculateNewCenter();

		}

		return m;
	}

	private void calculateNewCenter() {

		double [] d = new double[dimension];

		int i, j, k, l,  c=0;

		for (i=0;i<num_hidden;i++ )
		{
//			System.out.println("Z");
			// initialize
			c = 0;
			for (j=0;j<dimension;j++)
			{
				d[j] = 0; 
			}

//			System.out.println("A");

			for (k=0;k<num_training;k++ )
			{
				if (label[k] == i)
				{
					c++;
					for (l=0;l<dimension;l++)
					{
						d[l] = d[l] + X[k][l];
					}
				}
			}			
//			showVector("d",d,dimension);
//			System.out.println("B" + c);

			for (j=0;j<dimension;j++)
			{	if (c > 0) {
					m[i][j] = d[j]/c;
				} else {
//					m[i][j] = d[j];
					m[i][j] = m[i][j] ;
				}
			}

			/****/
//			System.out.println("ith = " +i);
//			showMatrix("m", m, num_hidden, dimension);
			/****/
		}
	}


	private void copyMtoOldM(double [][] m, double [][] oldm) {

		for (int i=0;i<num_hidden;i++ )
		{
			for (int j=0;j<dimension;j++)
			{
				oldm[i][j] = m[i][j];
			}
		}
	}

	// 
	private int nearestCenter(double [] x) {

		int nearestcenter = -1;
		double nearestDistance = Double.MAX_VALUE;

		for (int i=0;i<num_hidden;i++)
		{
			double d = distanceBetween(x,m[i]);

			if (d < nearestDistance)
			{
				nearestDistance = d;
				nearestcenter = i;
			}
		}

		return nearestcenter;
	}

	private double distanceBetween(double []x, double []m) {

		double dist = 0;
		for (int i=0;i<dimension;i++)
		{
			dist = dist + java.lang.Math.pow((x[i]-m[i]),2);
		}
		return java.lang.Math.sqrt(dist);
	}

	// Compare two matrices 
	private boolean isDifferent(double [][] m, double [][] oldm) {

		boolean answer = false;

		for (int i=0;i<num_hidden;i++)
		{
			for (int j=0;j<dimension;j++)
			{
				if (m[i][j] != oldm[i][j])
				{
					return (answer = true);
				}
			}
		}

		return answer;
	}
/*
	private void showMatrix(String title, double [][]X,int r, int c) {

//			int i, j;

			System.out.println(title);
			for (int i=0;i<r;i++ )
			{
				for (int j=0;j<c;j++ )
				{
					System.out.print(X[i][j] + " ");
				}
				System.out.println(" ");
			}

//			try { 
//		         br.readLine(); 
//		    } catch (java.io.IOException ioe) { 
//				 System.out.println("IO error trying to read your name!"); 
//		         System.exit(1); 
//		    } 

	}

	private void showVector(String title, double []X,int r) {

//			int i;

			System.out.println(title);
			for (int i=0;i<r;i++ )
			{
				System.out.println(X[i]);
			}

//			try { 
//		         br.readLine(); 
//		    } catch (java.io.IOException ioe) { 
//				 System.out.println("IO error trying to read your name!"); 
//		         System.exit(1); 
//		    } 

	}

	private void showVector(String title, int []X,int r) {

//			int i, j;

			System.out.println(title);
			for (int i=0;i<r;i++ )
			{
				System.out.println(X[i]);
			}

//			try { 
//		         br.readLine(); 
//		    } catch (java.io.IOException ioe) { 
//				 System.out.println("IO error trying to read your name!"); 
//		         System.exit(1); 
//		    } 

	}
*/
}