import java.util.Scanner;

public class App {

	static class Functions {
		
		/*PARÁMETROS GLOBALES*/
		static boolean DEG = true; //trigonometría en grados
		
		/*FUNCIONES DEL INTÉRPRETE*/
		public final int abs = 1;
		public double abs(double[] x) {
			return Math.abs(x[0]);
		}
		public final int sqrt = 1;
		public double sqrt(double[] x) {
			return Math.sqrt(x[0]);
		}
		public final int pow = 2;
		public double pow(double[] x) {
			return Math.pow(x[0], x[1]);
		}
		public final int floor = 1;
		public double floor(double[] x) {
			return Math.floor(x[0]);
		}
		public final int round = 1;
		public double round(double[] x) {
			return Math.round(x[0]);
		}
		public final int PI = 0;
		public double PI(double[] x) {
			return Math.PI;
		}
		public final int degrees = 0;
		public double degrees(double[] x) {
			Functions.DEG ^= true;
			return 0;
		}
		public final int sin = 1;
		public double sin(double[] x) {
			if(DEG) {
				return Math.sin(Math.PI*x[0]/180);
			}
			else return Math.sin(x[0]);
		}
		public final int cos = 1;
		public double cos(double[] x) {
			if(DEG) {
				return Math.cos(Math.PI*x[0]/180);
			}
			else return Math.cos(x[0]);
		}

		public final int E = 0;
		public double E(double[] x) {
			return Math.E;
		}
	}

	public static void main(String[] args) throws Exception {
		MathParser parser = new MathParser(new Functions());
		Scanner read = new Scanner(System.in);

        while(true) {
            System.out.print("calcular: ");
            parser.parse(read.nextLine());
            System.out.println("resultado: " + parser.getResult() + "\n");      
		}
	}
}
