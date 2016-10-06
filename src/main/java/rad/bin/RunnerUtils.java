package rad.bin;

public class RunnerUtils {

	public static void lineInterp (int NDATA, Double[] XDATA, Double FDATA[], int N, Double[] XVEC, Double[] VALUE, int IER) {
		
	/*'»нтерпол€ци€ параметры слева на право:
	*'NDATA количество точек на старой сетке,
	*'XDATA стара€ сетка,
	*'FDATA стара€ функци€,
	*'N количество новых точек,
	*'XVEC нова€ сетка,
	*'Value нова€ функци€
	**********************************************************/
		//subroutine LineInterp (NDATA, XDATA, FDATA, N, XVEC, VALUE, IER)
		
		//!DEC$ ATTRIBUTES DLLEXPORT::LineInterp
		//implicit none
		
		int I; 
		//REAL XDATA(NDATA), FDATA(NDATA), XVEC(N), VALUE(N), T
		//real LYNTERP
		 for(I = 0; I<=XVEC.length-1; I++) { //do I= 1,N
		 //		T = 	LYNTERP(NDATA, XDATA, FDATA, XVEC(I), IER)
			VALUE[I] = RunnerUtils.LYNTERP(NDATA, XDATA, FDATA, XVEC[I], IER);
	//		VALUE(i) = t
		//end do
		 }
	
	
	//!	contains
	/*	Function LYNTERP(ByVal N As Integer, Z As Variant, W As Variant, ByVal Z1 As Double, _
	*Optional IER As Variant) As Double
	*N количество узлов старой сетки
	*Z стара€ сетка
	*W старые значени€
	*Z1 значение сетки на которое надо интерполировать
	*IER код возвращаемой ошибки
	
		end subroutine*/ 
		
		}

	public static double  LYNTERP(int N, Double[] Z, Double[] W, Double Z1, int ier){
	//real function LYNTERP( N, Z, W, Z1, IER)
	//!DEC$ ATTRIBUTES DLLEXPORT::LYNTERP
	//implicit none
	int ub, lb, K, I=-1;
	//real Z(N), W(N), Z1
	ub = Z.length-1;
	lb = 0;
	if ((Z1>Z[ub])&(Z1>Z[lb])) {
		ier = 1;
		//LYNTERP = W(ub)
		if (Z[ub]>Z[lb]){
			return W[ub];
		} else {
			return W[lb];
		}
		
	};
	if ((Z1<Z[ub])&(Z1<Z[lb])) {
		ier = 2;
		//LYNTERP = W(lb)
		//return W[lb];
		if (Z[ub]<Z[lb]){
			return W[ub];
		} else {
			return W[lb];
		}
	};
	K = 0;
	try{
		if	(Z[lb]<Z[ub]) {
			for(I = lb; I <= ub; I++) { // do I = lb, ub
				if (Z1.doubleValue() < Z[I].doubleValue()) {
				    K = I;
				    I=ub+1; }
				    else if (Z1.doubleValue()==Z[I].doubleValue()) {
				        //LYNTERP = W(I)
				        return W[I];
				}
				//end do
				}
			K = K - 1;
		} else {
			for(I = ub; I >= lb; I--) { // do I = lb, ub
				if (Z1.doubleValue() < Z[I].doubleValue()) {
				    K = I;
				    I=lb-1; 
				    
				    }
				    else if (Z1.doubleValue()==Z[I].doubleValue()) {
				        //LYNTERP = W(I)
				        return W[I];
				}
				//end do
				}
			//K = K + 1;
		}
	
	
	/*LYNTERP = W(K) + ((Z1 - Z(K)) / (Z(K + 1) - Z(K))) * (W(K + 1) - W
	(K))*/
	  return W[K] + ((Z1 - Z[K]) / (Z[K + 1] - Z[K])) * (W[K + 1] - W[K]);
	} catch (ArrayIndexOutOfBoundsException IOBE){
		System.out.println("I:= "+I);
		System.out.println("Z1:= "+Z1);
		System.out.println("Z[ub]:= "+Z[ub]);
		System.out.println("Z[lb]:= "+Z[lb]);
		System.out.println("Z.length:= "+Z.length);
		System.out.println("Z[I]:= "+Z[I]);
		System.out.println("W.length:= "+W.length);
		System.out.println("W[I]:= "+W[I]);
		System.out.println("K:= "+K);
		return W[K] + ((Z1 - Z[K]) / (Z[K + 1] - Z[K])) * (W[K + 1] - W[K]);
	}
	//end function LYNTERP
	
	}

	

}
