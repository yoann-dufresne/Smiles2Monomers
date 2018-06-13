import model.Family;

public class Test {

	public static void main(String[] args) {
		
		Family f1 = new Family();
		System.out.println(f1);
		
		
		
		Family f2;
		try {
			f2 = f1.getClass().newInstance();
			System.out.println(f2);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Family f3 = new Family();
		System.out.println(f3);
		

		System.out.println(exec(20));
				
			
		
	}
	
	static int exec(int v) {
		v--;
		if (v!=0)
			return exec(v);
		else 
			return v;
		
		
		
	}

}
