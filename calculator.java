import java.util.Scanner;

public class EnhancedCalculator {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n=========== HERE YOU GO A  CALCULATOR FOR ALL OPERATIONS . ENJOY , LET THE NUMBERS DO THE MAGIC!===========");
            System.out.println("1. Basic Arithmetic (Multiple Inputs)");
            System.out.println("2. Scientific Calculations");
            System.out.println("3. Quadratic Equation Solver");
            System.out.println("4. Permutation & Combination");
            System.out.println("5. Matrix Calculations");
            System.out.println("6. Binary ↔ Decimal Conversion");
            System.out.println("7. Unit Conversions");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1 :BasicArithmeticOperations(); break;
                case 2 :ScientificCalculations();break;
                case 3 : QuadraticEquation(); break;
                case 4 :PermutationCombination(); break;
                case 5 : MatrixCalculations();break;
                case 6 : BinaryToDecimalConversion();break;
                case 7 : UnitConversions();break;
                case 8 : {
                    System.out.println("Thank you for using the calculator!");
                    return;
                }
                default :System.out.println("Invalid choice!");
            }
        }
    }

    
    static void BasicArithmeticOperations() {
        System.out.print("How many numbers? ");
        int n = sc.nextInt();

        double[] arr = new double[n];
        for (int i = 0; i < n; i++) {
            System.out.print("Enter number " + (i + 1) + ": ");
            arr[i] = sc.nextDouble();
        }

        System.out.println("1.Add  2.Subtract  3.Multiply  4.Divide");
        int op = sc.nextInt();

        double result = arr[0];

        switch (op) {
            case 1:
                for (int i = 1; i < n; i++) result += arr[i];
                System.out.println("Addition Result: " + result);
                break;

            case 2:
                for (int i = 1; i < n; i++) result -= arr[i];
                System.out.println("Subtraction Result: " + result);
                break;

            case 3:
                for (int i = 1; i < n; i++) result *= arr[i];
                System.out.println("Multiplication Result: " + result);
                break;

            case 4:
                for (int i = 1; i < n; i++) {
                    if (arr[i] == 0) {
                        System.out.println("Division by zero error!");
                        return;
                    }
                    result /= arr[i];
                }
                System.out.println("Division Result: " + result);
                break;

            default:
                System.out.println("Invalid operation!");
        }
    }


    static void ScientificCalculations() {
        System.out.println("""
                1. Square Root
                2. Power
                3. Factorial
                4. Log10
                5. Natural Log
                6. Trigonometry
                """);

        int ch = sc.nextInt();

        switch (ch) {
            case 1:
                System.out.print("Enter number: ");
                System.out.println(Math.sqrt(sc.nextDouble()));
                break;

            case 2:
                System.out.print("Base: ");
                double b = sc.nextDouble();
                System.out.print("Exponent: ");
                double e = sc.nextDouble();
                System.out.println(Math.pow(b, e));
                break;

            case 3:
                System.out.print("Enter number: ");
                int n = sc.nextInt();
                long fact = 1;
                for (int i = 1; i <= n; i++) fact *= i;
                System.out.println("Factorial: " + fact);
                break;

            case 4:
                System.out.print("Enter number: ");
                System.out.println(Math.log10(sc.nextDouble()));
                break;

            case 5:
                System.out.print("Enter number: ");
                System.out.println(Math.log(sc.nextDouble()));
                break;

            case 6:
                System.out.print("Angle (degrees): ");
                double angle = Math.toRadians(sc.nextDouble());
                System.out.println("sin: " + Math.sin(angle));
                System.out.println("cos: " + Math.cos(angle));
                System.out.println("tan: " + Math.tan(angle));
                break;

            default:
                System.out.println("Invalid choice!");
        }
    }

    
    static void QuadraticEquation() {
        System.out.print("Enter a, b, c: ");
        double a = sc.nextDouble();
        double b = sc.nextDouble();
        double c = sc.nextDouble();

        double d = b * b - 4 * a * c;

        if (d > 0) {
            System.out.println("Roots: " +
                    ((-b + Math.sqrt(d)) / (2 * a)) + ", " +
                    ((-b - Math.sqrt(d)) / (2 * a)));
        } else if (d == 0) {
            System.out.println("Root: " + (-b / (2 * a)));
        } else {
            System.out.println("Complex roots.");
        }
    }

    
    static void PermutationCombination() {
        System.out.print("Enter n: ");
        int n = sc.nextInt();
        System.out.print("Enter r: ");
        int r = sc.nextInt();

        long nf = Factorial(n);
        long rf = Factorial(r);
        long nrf = Factorial(n - r);

        System.out.println("nPr: " + (nf / nrf));
        System.out.println("nCr: " + (nf / (rf * nrf)));
    }

    static long Factorial(int n) {
        long f = 1;
        for (int i = 1; i <= n; i++) f *= i;
        return f;
    }

    
    static void MatrixCalculations() {
        System.out.print("Enter rows and columns: ");
        int r = sc.nextInt();
        int c = sc.nextInt();

        int[][] A = new int[r][c];
        int[][] B = new int[r][c];

        System.out.println("Enter Matrix A:");
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                A[i][j] = sc.nextInt();

        System.out.println("Enter Matrix B:");
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                B[i][j] = sc.nextInt();

        System.out.println("Matrix Addition:");
        printMatrix(add(A, B));

        System.out.println("Matrix Subtraction:");
        printMatrix(sub(A, B));

        System.out.println("Matrix Multiplication:");
        printMatrix(mul(A, B));
    }

    static int[][] add(int[][] A, int[][] B) {
        int r = A.length, c = A[0].length;
        int[][] R = new int[r][c];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                R[i][j] = A[i][j] + B[i][j];
        return R;
    }

    static int[][] sub(int[][] A, int[][] B) {
        int r = A.length, c = A[0].length;
        int[][] R = new int[r][c];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                R[i][j] = A[i][j] - B[i][j];
        return R;
    }

    static int[][] mul(int[][] A, int[][] B) {
        int r = A.length, c = B[0].length;
        int[][] R = new int[r][c];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                for (int k = 0; k < B.length; k++)
                    R[i][j] += A[i][k] * B[k][j];
        return R;
    }

    static void printMatrix(int[][] M) {
        for (int[] row : M) {
            for (int v : row) System.out.print(v + " ");
            System.out.println();
        }
    }

    
    static void BinaryToDecimalConversion() {
        System.out.println("1. Binary to Decimal\n2. Decimal to Binary");
        int ch = sc.nextInt();

        if (ch == 1) {
            System.out.print("Binary: ");
            System.out.println(Integer.parseInt(sc.next(), 2));
        } else {
            System.out.print("Decimal: ");
            System.out.println(Integer.toBinaryString(sc.nextInt()));
        }
    }

    
    static void UnitConversions() {
        System.out.println("1. Celsius to Fahrenheit\n2. Fahrenheit to Celsius");
        int ch = sc.nextInt();

        if (ch == 1)
            System.out.println((sc.nextDouble() * 9 / 5) + 32);
        else
            System.out.println((sc.nextDouble() - 32) * 5 / 9);
    }
}
