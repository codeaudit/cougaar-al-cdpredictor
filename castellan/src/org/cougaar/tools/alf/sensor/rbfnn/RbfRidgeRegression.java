/***
	programmed by Yunho Hong
	This is based on the Mark Orr matlab code
	May 12, 2002
***/

package org.cougaar.tools.alf.sensor.rbfnn;

import java.lang.*;
import java.io.*;
import java.util.*;
import com.axiom.lib.util.* ;
import com.axiom.lib.mat.* ;

public class RbfRidgeRegression implements java.io.Serializable {

	double [][] X;
	double [][] H;
	double [][] m; // Center
	double [][] A;
	double [][] T;

	double [] r; // Radius
	double [] w; // Weight
	double [] y;

	double increment;
	int num_training;
	int num_testing;
	int dimension;
	int num_hidden;
	double lamda;
	int count_limit;

	String Data, Model, Result;
	
	public RbfRidgeRegression()  {

		num_training = 250; // default
		dimension = 1;		// default
		count_limit = 10;
	}
	
	public void setParameters(int num_hidden1, double lamda1, double increment1, int count_limit1, int dimension1) {

		num_hidden = num_hidden1;
		lamda = lamda1;
		increment = increment1;
		count_limit = count_limit1;
		dimension = dimension1;

	}

	public void setData(String data) { Data = data;	} // Regardless of training or testing data

	public void readData() {

		try
		{
			java.io.BufferedReader DataReader = new java.io.BufferedReader ( new java.io.FileReader(Data));
			String s = DataReader.readLine(); // number of DataReader data, number of attributes per each data
			int is = 0;
			int ix = s.indexOf(",");
			num_training = (Integer.valueOf(s.substring(is,ix))).intValue();
			dimension =	(Integer.valueOf(s.substring(ix+1, s.length()))).intValue();

			X = new double[num_training][dimension];
			y = new double[num_training];

			for (int i=0;i<num_training;i++)
			{
				s = DataReader.readLine(); 
				is = 0;
				for (int j=0;j<dimension;j++)
				{
					ix = s.indexOf(",",is);	
					X[i][j] = (Double.valueOf(s.substring(is,ix))).doubleValue();
					is = ix+1;
				}
				y[i] = (Double.valueOf(s.substring(ix+1, s.length()))).doubleValue();
			}			
			DataReader.close();
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read or write file, io error" );
			System.err.println ("RbfRidgeRegression constructor");
	    }
		
	}

	public void readParam(File paramFile) {

		String s;

		try
		{
			java.io.BufferedReader paramReader = new java.io.BufferedReader ( new java.io.FileReader(paramFile));

			if((s=paramReader.readLine())!=null)
			{
				StringTokenizer st = new StringTokenizer(s,",",false);
				num_hidden = Integer.valueOf(st.nextToken().trim()).intValue();
				lamda = Double.valueOf(st.nextToken().trim()).doubleValue();
				increment = Double.valueOf(st.nextToken().trim()).doubleValue();
				count_limit = Integer.valueOf(st.nextToken().trim()).intValue();
				dimension = Integer.valueOf(st.nextToken().trim()).intValue();
			}
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read or write file, io error" );
			System.err.println ("RbfRidgeRegression constructor");
	    }
	}

	public void readModel(File inputFile) {

		int is, ix;
		String s;

		try
		{
			java.io.BufferedReader ModelReader = new java.io.BufferedReader ( new java.io.FileReader(inputFile));

			m = new double[num_hidden][dimension]; // Center
			w = new double[num_hidden];
			r = new double[num_hidden];

			// Read center of hidden nodes
			for (int i=0;i<num_hidden;i++)
			{
				s = ModelReader.readLine(); 
				is = 0;
				for (int j=0;j<dimension;j++)
				{
					ix = s.indexOf(",",is);	
					m[i][j] = (Double.valueOf(s.substring(is,ix))).doubleValue();
					is = ix+1;
				}
			}			

			// Read width of hidden nodes
			s = ModelReader.readLine(); 
			is = 0;
			for (int j=0;j<num_hidden;j++)
			{
					ix = s.indexOf(",",is);	
					r[j] = (Double.valueOf(s.substring(is,ix))).doubleValue();
					is = ix+1;
			}

			// Read weight of hidden nodes
			s = ModelReader.readLine(); 
			is = 0;
			for (int j=0;j<num_hidden;j++)
			{
					ix = s.indexOf(",",is);	
					w[j] = (Double.valueOf(s.substring(is,ix))).doubleValue();
					is = ix+1;
			}
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read or write file, io error" );
			System.err.println ("RbfRidgeRegression constructor");
	    }
	}

	public void readModel() {

		int is, ix;
		String s;

		try
		{
			java.io.BufferedReader ModelReader = new java.io.BufferedReader ( new java.io.FileReader(Model));

			m = new double[num_hidden][dimension]; // Center
			w = new double[num_hidden];
			r = new double[num_hidden];

			// Read center of hidden nodes
			for (int i=0;i<num_hidden;i++)
			{
				s = ModelReader.readLine(); 
				is = 0;
				for (int j=0;j<dimension;j++)
				{
					ix = s.indexOf(",",is);	
					m[i][j] = (Double.valueOf(s.substring(is,ix))).doubleValue();
					is = ix+1;
				}
			}			

			// Read width of hidden nodes
			s = ModelReader.readLine(); 
			is = 0;
			for (int j=0;j<num_hidden;j++)
			{
					ix = s.indexOf(",",is);	
					r[j] = (Double.valueOf(s.substring(is,ix))).doubleValue();
					is = ix+1;
			}

			// Read weight of hidden nodes
			s = ModelReader.readLine(); 
			is = 0;
			for (int j=0;j<num_hidden;j++)
			{
					ix = s.indexOf(",",is);	
					w[j] = (Double.valueOf(s.substring(is,ix))).doubleValue();
					is = ix+1;
			}
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read or write file, io error" );
			System.err.println ("RbfRidgeRegression constructor");
	    }
	}

	// save trained model 
	public void saveModel() {

		int is, ix;

		try
		{
			java.io.BufferedWriter ModelWriter = new java.io.BufferedWriter ( new java.io.FileWriter(Model, true ));			

			System.out.println("save Model");
			// Write center of hidden nodes
			for (int i=0;i<num_hidden;i++)
			{
				for (int j=0;j<dimension;j++)
				{
					ModelWriter.write(m[i][j]+",");
					System.out.print(m[i][j]+",");
				}
				ModelWriter.write("\n");
				System.out.println("");
			}			

			// Write width of hidden nodes
			for (int j=0;j<num_hidden;j++)
			{
					ModelWriter.write(r[j]+",");
			}
			ModelWriter.write("\n");

			// Write weight of hidden nodes
			for (int j=0;j<num_hidden;j++)
			{
					ModelWriter.write(w[j]+",");
			}
			ModelWriter.write("\n");
			ModelWriter.close();
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read or write file, io error" );
			System.err.println ("RbfRidgeRegression constructor");
	    }
	}

	public void saveResult() {

		System.out.println("Design Matrix H");	
		// Find H function

		H = calcDesignMatrix(X,m,r);

		System.out.println("weight");	
		// Calculate Weight Matrix
		w = calcWeight(H,lamda,y);

		try
		{
			java.io.BufferedWriter ResultWriter = new java.io.BufferedWriter ( new java.io.FileWriter(Result, true ));			

			// Show the result in a Matlab
//			MatEng eng = MatEng.getInstance();

			// Create a matrix with 16 rows and 4 columns.
//			FloatMatrix matmatrix = new FloatMatrix(num_training,  2 ) ;
			double mse = 0, me = 0, mae = 0;

			for (int i=0;i<num_training;i++)
			{
//				for (int j=0;j<dimension;j++)
//				{
//					ResultWriter.write(X[i][j]+" ");
//				}

				double yy = 0;
				for (int j=0;j<num_hidden ;j++ )
				{
					yy = yy + H[i][j]*w[j];
				}
//				Result.write(X[i][0]+","+yy+"\n");
//				matmatrix.set(i,0,(float) X[i][0]);
//				matmatrix.set(i,0,(float) y[i]);
//				matmatrix.set(i,2,(float) X[i][0]);
//				matmatrix.set(i,1,(float) yy);

//				Model.write(X[i][0]+" "+y[i]+" "+yy+"\n");
//				Model.write(X[i][0]+" "+y[i]+"\n");
//			}
//		}
//		catch (java.io.IOException ioexc)
//	    {
//		    System.err.println ("can't read or write file, io error" );
//			System.err.println ("RbfRidgeRegression constructor");
//	    }

				ResultWriter.write(y[i]+ " "+ yy + "  " + (y[i] - yy) + "\n");
				me = me + y[i] - yy;
				mae = mae + Math.abs(y[i] - yy);
				mse = mse + Math.pow(y[i] - yy,2);
			}			

			ResultWriter.write("error = " + calcError() + "\n");
			ResultWriter.write("MSE = " + (mse/num_training) + "\n");
			ResultWriter.write("ME = " + (me/num_training) + "\n");
			ResultWriter.write("MAE = " + (mae/num_training) + "\n");
//			eng.putArray( "myMatrix", matmatrix ) ;
//			eng.evalString( "figure" ) ;
//			eng.evalString( "plot( myMatrix)" ) ;

			ResultWriter.close();
		}
		catch (java.io.IOException ioexc)
	    {
		    System.err.println ("can't read or write file, io error" );
			System.err.println ("RbfRidgeRegression constructor");
	    }
	}

	public void setModel(String ModelFile) { Model = ModelFile; }
		
	public void setResult(String ResultFile) { Result = ResultFile; }
		
	public void trainRbf() { 

		readData();

		// Assume X, y are array;
		Kmeans kmeans = new Kmeans(X, num_hidden);

		System.out.println("Kmeans clustering");	

		m = kmeans.clustering();
		r = kmeans.getWidth();

//		Matrix.showMatrix("m",m);

		int count = 0; 
		boolean improved = true;
		while (count < count_limit && improved == true)
		{
			System.out.println("Design Matrix H");	
			// Find H function
			H = calcDesignMatrix(X,m,r);

			System.out.println("weight");	
			// Calculate Weight Matrix
			w = calcWeight(H,lamda,y);

			// Calculate new lamda
//	lamda = calcNewlamda();
			System.out.println("change center and width");	

			improved = changemandw();

			System.out.println("error = " + calcError());	
			count++;
		}

		saveModel();

	} 


/*
	public double calcNewlamda() {

		double [][] P = calcProject();
		double n = multiply(trace(minus(inverse(A),multiply(lamda,inverse(inverse(A))))),multiply(transpose(y),multiply(multiply(P,P),y));
		double d = multiply(trace(P),multiply(transpose(w),multiply(inverse(A),w)));

		lamda = n/d;

	}

	public double [][] calcProject() {

		double [][] P = multiply(H,multiply(inverse(A),T));

		for (int i=0;i < num_training ; i++)
		{
			for (int j=0;j < num_training ; j++)
			{
				if (i==j)
				{
					P[i][j] = 1 -1 * P[i][j];
				} else {
					P[i][j] = -1 * P[i][j];
				}
			}
		}

		return P;
	}
*/


	private double trace(double [][] B) {

		int l = B.length;
		double t = 0;

		for (int i=0;i<l ;i++ )
		{
			t = t + B[i][i];
		}

		return t;
	}
	
	public double [][] minus(double [][] B, double [][] C) {

			int l = B.length;
			int m = B[0].length;

			double [][] D = new double[l][m];

			for (int i=0;i<l;i++)
			{
				for (int j=0;j<m;j++ )
				{
					D[i][j] = B[i][j] - C[i][j];
				}
			}

		  return D;
	}

	public double [] calcWeight(double [][] H, double lamda, double []y) {

		int nt = H.length;

		double [] w = new double[num_hidden];

		T = transpose(H);

//		Matrix.showMatrix("H",H);
//		Matrix.showMatrix("T",T);

		A = multiply(T,H);

//		Matrix.showMatrix("G",G);

		for (int i = 0; i < num_hidden ; i++)
		{
			A[i][i] = A[i][i] + 2*lamda/nt;
		}
		
//		Matrix.showMatrix("G",G);

		double [][] IG = inverse(A);

//		Matrix.showMatrix("IG",IG);

		double [][] K = multiply(IG,T);

//		Matrix.showMatrix("K",K);
		
		w = multiply(K,y);
		
//		Matrix.showVector("w",w);

		return w;
	}

	public double [][] inverse(double [][] G) {


		double alpha;
		double beta;
		int i;
		int j;
		int k;
		int error;

		int n = G.length;

		double [][] D = new double[n][2*n];
		double [][] E = new double[n][n];

		error = 0;
		int n2 = 2*n;
		
		/* copy to D */

		for (i=0;i<n;i++)
		{
			for (j=0;j<n ;j++ )
			{
				D[i][j] = G[i][j];
			}
		}

		/* init the reduction matrix  */
		for( i = 0; i < n; i++ )
		{
			for( j = 0; j < n; j++ )
			{
				D[i][j+n] = 0.;
			}
			D[i][i+n] = 1.0;
		}

		/* perform the reductions  */
		for( i = 0; i < n; i++ )
		{
			alpha = D[i][i];
			if( alpha == 0.0 ) /* error - singular matrix */
			{
				error = 1;
				break;
			}
			else
			{
				for( j = 0; j < n2; j++ )
				{
					D[i][j] = D[i][j]/alpha;
				}
				for( k = 0; k < n; k++ )
				{
					if( (k-i) != 0 )
					{
						beta = D[k][i];
						for( j = 0; j < n2; j++ )
						{
							D[k][j] = D[k][j] - beta*D[i][j];
						}
					}
				}
			}
		}

		for (i=0;i<n;i++)
		{
			for (j=0;j<n ;j++ )
			{
				E[i][j] = D[i][j+n];
			}
		}

		return E;
	}

	private double [][] multiply(double [][] T,double [][] H) {

		int r = T.length;
		int p = T[0].length;
		int q = H.length;
		int c = H[0].length;

		double [][] G = new double[r][c];

		if (p != q)
		{
			System.out.println("We cannot multiply in [][] * [][] !!!");
			return G;
		}

		for (int j = 0; j <r;j++ )
		{
			for (int i=0;i <c;i++)
			{
				G[j][i] = 0;
				for (int k=0;k<p;k++)
				{
					G[j][i] = G[j][i] + T[j][k]*H[k][i];
				}
			}
		}

		return G;
	}

	private double [] multiply(double [][] T,double [] H) {

		int r = T.length;
		int p = T[0].length;
		int q = H.length;


		double [] G = new double[r];

		if (p != q)
		{
			System.out.println("We cannot multiply in [][] * [] !!!");
			return G;
		}

		for (int j = 0; j <r;j++ )
		{
			G[j] = 0;
			for (int i=0;i <q;i++)
			{
				G[j]= G[j] + T[j][i]*H[i];
			}
		}

		return G;
	}

	private double [][] transpose(double [][] H) {

		int r = H.length;
		int c = H[0].length;

		double [][] T = new double[c][r];

		for (int i=0;i <r;i++)
		{
			for (int j=0;j<c;j++ )
			{
				T[j][i] = H[i][j];
			}
		}

		return T;
	}
/*
	private double [] transpose(double [] z) {

		int r = H.length;
		int c = H[0].length;

		double [][] T = new double[c][r];

		for (int i=0;i <r;i++)
		{
			for (int j=0;j<c;j++ )
			{
				T[j][i] = H[i][j];
			}
		}

		return T;
	}
*/
	private double [][] calcDesignMatrix(double [][] X, double [][]m, double []d){

		int r = X.length;
		int c = m.length;

//		Matrix.showMatrix("X",X);

		double [][] H = new double[r][c];

		for (int i=0;i<r;i++)
		{
			for (int j=0;j<c;j++ )
			{
//				H[i][j] = Math.exp(-1*calcL2norm(X[i],m[j])/Math.pow(d[j],2));
				H[i][j] = h(j,X[i]);
			}
		}
		return H;
	}

	private double h(int j, double [] x) {
		
		return Math.exp(-1*calcL2norm(x,m[j])/Math.pow(r[j],2));

	}

	private double calcL2norm(double []x, double []m) {

		double L2norm = 0;
		
		int c = x.length;

		for (int i=0;i<c;i++)
		{
			L2norm = L2norm + Math.pow((x[i]-m[i]),2);
		}
		return L2norm;
	}

	public void testRbf() { 
	
			readData();
			readModel();
			saveResult();
	}

	private void pause() {

		BufferedReader br = new BufferedReader(new java.io.InputStreamReader(System.in)); 
		
		try { 
		         br.readLine(); 
				 br.close();
		    } catch (java.io.IOException ioe) { 
				 System.out.println("IO error trying to read your name!"); 
		         System.exit(1); 
		    } 
	}

	private void hermiteFunction() {
		
		X = new double[num_training][1];
		y = new double[num_training];

		X[0][0] = -4;
		y[0] = hermite(X[0][0]);

		for (int i=1;i<num_training;i++)
		{
			X[i][0] = X[i-1][0] + 0.032;
			y[i] = hermite(X[i][0]);
		}
	}

	private void logisticMap() {
		
		dimension = 4;
		X = new double[num_training][dimension];
		y = new double[num_training];

		y[0] = 0.5;

		for (int i=1;i<num_training;i++)
		{
			// L = 4
			if (i >= 4)
			{
				for (int j=0;j<4;j++)
				{
					X[i][j] = y[i-j-1];
				}
			}

			y[i] = 3.8*y[i-1]*(1-y[i-1]);
		}
	}


	private double hermite(double x) {
		return 1 + (1-x+2*Math.pow(x,2))*Math.exp(-1*Math.pow(x,2));
	}


	private boolean changemandw() {

		double [][] Gradient1 = new double[num_hidden][dimension]; // for mu
		double [] Gradient2 = new double[num_hidden]; // for sigma

		double difference = 0;
//		boolean improved = false;

		for (int i=0;i<num_training;i++)
		{
			double yy = 0;
			for (int j=0;j<num_hidden ;j++ )
			{
				yy = yy + H[i][j]*w[j];
			}
			difference = difference + (yy - y[i]);
		}

		// center
		for (int i=0;i<num_hidden;i++)
		{
			for (int k=0;k<dimension;k++)
			{
				Gradient1[i][k] = 0;
				for (int j=0 ;j<num_training;j++)
				{
					Gradient1[i][k] = Gradient1[i][k] + w[i]*((X[j][k]-m[i][k])/Math.pow(r[i],2))*h(i,X[j]);
				}
				Gradient1[i][k] = Gradient1[i][k]*difference;
//				System.out.println("current error = " + currentErr);
			}
		}

		// width
		for (int i=0;i<num_hidden;i++)
		{
			Gradient2[i] = 0;
			for (int j=0 ;j<num_training;j++)
			{
				Gradient2[i] = Gradient2[i] + w[i]*(calcL2norm(X[j],m[i])/Math.pow(r[i],3))*h(i,X[j]);
			}
			Gradient2[i] = Gradient2[i]*difference;
		}

		// find delta value. during finding delta value center and width are already changed.
		return findbestdelta(Gradient1, Gradient2);
	}

	private boolean findbestdelta(double [][] Gradient1, double [] Gradient2) {

		double currentErr = calcError();
		double newErr = 0;
		boolean improved = false;
		double delta = increment;
		double tnewErr;
		double lb, ub;
		int count = 0;
		
		int direction = 1;

		double [][] m1 = new double[num_hidden][dimension];
		double [] r1 = new double[num_hidden];

////////// Backup begins
			// Center
			for (int i=0;i<num_hidden;i++)	{
				for (int j=0;j<dimension;j++)	{	m1[i][j] = m[i][j];		}
			}

			// width
			for (int i=0;i<num_hidden;i++)	{	r1[i] = r[i];	}
////////// Backup ends

		lb = 0;
		ub = increment;

		tnewErr = currentErr;

		System.out.println("cur error = " + currentErr);	
		while (count < 20 && (ub-lb) > 0.000000000000001)
		{
			// Change the center and width
			changem(Gradient1,delta);
			changer(Gradient2,delta);

			// Find H function
			H = calcDesignMatrix(X,m,r);

			// Calculate Weight Matrix
			w = calcWeight(H,lamda,y);

			newErr = calcError();

			System.out.println("new error = " + newErr);	
			
			if (newErr > currentErr)
			{
				if (direction == 1)
				{
					ub = delta;
					direction = -1;	
				} else {
//					direction = 1;
					ub = delta;
				}
				delta = lb+(ub-lb)*0.2;				
////////// Restore begins
				// Center
				for (int i=0;i<num_hidden;i++)	{
					for (int j=0;j<dimension;j++)	{	m[i][j] = m1[i][j];	}
				}

				// width
				for (int i=0;i<num_hidden;i++)	{	r[i] = r1[i];	}
////////// Restore ends

			} else { 
				count = 0;
				if (direction == 1)
				{
					lb = delta;
					if (lb >= ub)
					{
						ub = lb + increment;
					}
					delta = lb + increment;
				} else {
					ub = delta;
					if (lb < delta)
					{
						delta = lb + (lb+ub)*0.2;				
					} else {
						delta = lb;
						count = 20;
					}
				}
			}

			tnewErr = newErr;

			if (newErr < currentErr)
			{
				improved = true;
				currentErr = newErr;
				System.out.println("cur error = " + currentErr);	
////////// Backup begins
				// Center
				for (int i=0;i<num_hidden;i++)	{
					for (int j=0;j<dimension;j++)	{	m1[i][j] = m[i][j];		}
				}

				// width
				for (int i=0;i<num_hidden;i++)	{	r1[i] = r[i];	}
////////// Backup ends
			}
			count++;
		}

////////// Restore begins
		// Center
		for (int i=0;i<num_hidden;i++)	{
				for (int j=0;j<dimension;j++)	{	m[i][j] = m1[i][j];	}
		}

		// width
		for (int i=0;i<num_hidden;i++)	{	r[i] = r1[i];	}

			// Find H function
			H = calcDesignMatrix(X,m,r);

			// Calculate Weight Matrix
			w = calcWeight(H,lamda,y);

////////// Restore ends

		return improved;
	}

	private void changem(double [][]Gradient1, double delta) {
	
		// change m and w values 
		for (int i=0;i<num_hidden;i++)
		{
			for (int j=0;j<dimension;j++)
			{
				m[i][j] = m[i][j] + delta*Gradient1[i][j];
			}
		}
	}

	private void changer(double [] Gradient2, double delta) {
	
		// change m and w values 
		for (int i=0;i<num_hidden;i++)
		{
			r[i] = r[i] + delta*Gradient2[i];
		}
	}


	private double calcError() {

		// Gunner Ratsch's paper
		double err1 = 0;

		for (int i=0;i<num_training;i++)
		{
			err1 = err1 + Math.pow(f(X[i]) - y[i],2);
		}
		err1 = err1/2;

		// bias term
		double err2 = 0;

		for (int j=0;j<num_hidden;j++)
		{
			err2 = err2 + Math.pow(w[j],2);
		}

		err2 = err2*lamda/(2*num_training);

		return err1+err2;		
	}

	public double f(double []x) {

		double fv = 0;

		for (int i=0;i<num_hidden;i++ )
		{
			fv = fv + w[i]*h(i,x);
		}

		return fv;
	}

}