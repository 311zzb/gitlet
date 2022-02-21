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
    public static void main(String[] args) {
        assertNotArgsNum("Gitlet", args, 0);
        String command = args[0];
        String[] operands = getOperands(args);

        switch (command) {
            case "init" -> {
                assertArgsNum(command, operands, 0);
                Repository.init();
            }
            case "add" -> {
                assertArgsNum(command, operands, 1);
                Repository.add(operands[0]);
            }
            case "commit" -> {
                assertArgsNum(command, operands, 1);
                Repository.commit(operands[0]);
            }
            case "rm" -> {
                assertArgsNum(command, operands, 1);
                Repository.rm(operands[0]);
            }
            case "log" -> {
                assertArgsNum(command, operands, 0);
                Repository.log();
            }
            case "global-log" -> {
                assertArgsNum(command, operands, 0);
                Repository.globalLog();
            }
            case "checkout" -> {
                switch (operands.length) {
                    case 1 -> {
                        Repository.checkout3(operands[0]);
                    }
                    case 2 -> {
                        assertString("--", operands[0]);
                        Repository.checkout1(operands[1]);
                    }
                    case 3 -> {
                        assertString("--", operands[1]);
                        Repository.checkout2(operands[0], operands[2]);
                    }
                    default -> throw new GitletException("Invalid number of arguments for: checkout.");
                }
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

    /**
     * Assert two String are equal.
     * @param expected the expected String
     * @param actual the actual String
     */
    private static void assertString(String expected, String actual) {
       if (expected == null) {
           if (actual == null) {
               return;
           } else {
               throw new GitletException("Make sure you use " + expected + ".");
           }
       }
       if (!expected.equals(actual)) {
           throw new GitletException("Make sure you use " + expected + ".");
       }
    }
}
