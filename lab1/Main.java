import java.util.NoSuchElementException;

public class Main {
    public static void main(String[] args) {
        Queue<Integer> q = new Queue<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                switch (args[i].charAt(1)) {
                case 'S':
                    q = new Queue<>(Integer.parseInt(args[++i]));
                    System.out.print("S " + args[i] + " ");
                    break;
                case 'I':
                    try {
                        System.out.print("I ");
                        while (i + 1 < args.length && args[i + 1].charAt(0) != '-')
                            q.add(Integer.parseInt(args[++i]));
                        System.out.print(q.toString().substring(1).replaceAll(",|]", " "));
                    } catch (IllegalStateException e) {
                        System.out.println("E ");
                        e.printStackTrace();
                        return;
                    }
                    break;
                case 'O':
                    try {
                        System.out.print("O ");
                        int n = Integer.parseInt(args[++i]);
                        while (n-- > 0)
                            q.remove();
                        System.out.print(q.toString().substring(1).replaceAll(",|]", " "));
                    } catch (NoSuchElementException e) {
                        System.out.println("E ");
                        e.printStackTrace();
                        return;
                    }
                    break;
                default:
                    break;
                }
            }
        }
        System.out.println();
    }
}
