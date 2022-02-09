package gitlet;

import java.io.IOException;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 * Account for validating the number of arguments and invoking package-private methods according to received commands.
 * The cache write back method `Cache.writeBack()` which enabling the persistence of Gitlet is also invoked in this class.
 *
 * @author XIE Changyuan
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        assertNotArgsNum("Gitlet", args, 0);
        String command = args[0];
        String[] operands = getOperands(args);

        switch (command) {
            case "init" -> {
                assertArgsNum("init", operands, 0);
                Repository.init();
            }
            case "add" -> {
                assertArgsNum("add", operands, 1);
                Repository.add(operands[0]);
            }
            case "commit" -> {
                assertArgsNum("commit", operands, 1);
                Repository.commit(operands[0]);
            }
            case "log" -> {
                assertArgsNum("log", operands, 0);
                Repository.log();
            }
            default -> throw new GitletException("Unexpected command: " + command);
        }
        Cache.writeBack();
    }

    /**
     * Throw a GitletException if args don't have exactly n elements.
     * @param cmd the current command
     * @param args the input arguments
     * @param n the necessary number of arguments
     */
    private static void assertArgsNum(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new GitletException(
                    String.format("Invalid number of arguments for: %s.", cmd)
            );
        }
    }

    /**
     * Throw a GitletException if args have exactly n elements.
     * @param cmd the current command
     * @param args the input arguments
     * @param n the tattoo number of arguments
     */
    private static void assertNotArgsNum(String cmd, String[] args, int n) {
        if (args.length == n) {
            throw new GitletException(
                    String.format("Invalid number of arguments for: %s.", cmd)
            );
        }
    }

    /**
     * Strip the first element of the input array and return the rest.
     * @param args the args
     * @return the operands as a String array
     */
    private static String[] getOperands(String[] args) {
        String[] operands = new String[args.length - 1];
        System.arraycopy(args, 1, operands, 0, args.length - 1);
        return operands;
    }
}
