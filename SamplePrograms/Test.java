class Test{
    public static void main(String[] a){
    	System.out.println( new TestClass().Foo(1,2,3,4,5) );
    }
}

class TestBase {
	
	int v;
	int[] z;

    public int Bar(int a, int b, int c, int d, int e) {
    	int f;
    	int i;
    	z = new int[10 * 2 - 5 ];
    	f = 0;
    	i = 0;
    	while ( i < z.length - 1 ) {
    		f = f + 8;
    		if ( f < 30 ) {
    			v = v +  f;
    		}
    		else v = f;
    		z[i] = v;
    		System.out.println( i );
    		System.out.println( z[i] );
    		i = i + 1;    		
    	}
    	/*
    	while ( i < z.length ) {
    		f = (a * b * c * d) - e + v + f;
    		z[i] = f;
    		i = i + 1;
    	}
    	*/
    	return f;
    }
}


class TestClass extends TestBase {
	
    public int Foo(int a, int b, int c, int d, int e) {
    	int g;
    	int val;
    	v = (a * b - c * d) * e;
    	val = this.Bar(a, b, c, d, e);
    	g = val + v;
    	return g;
    }
    
}
