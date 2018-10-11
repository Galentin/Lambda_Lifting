import java.io.File;

public class Main {
    public static void main(String[] args) {
        Gameboard gameboard = new Gameboard();
        Solver solver = new Solver(gameboard);
        if(args.length != 1) System.err.println("Missing input arguments");
        else{
            File file = new File(args[0]);
            if(file.exists()){
                gameboard.init(args[0]);
                solver.makeMove();
            } else System.err.println("Input file is missing");
        }
    }
}
