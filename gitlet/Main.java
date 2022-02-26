package gitlet;


import java.util.Objects;

import static gitlet.Repository.printAndExit;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 * Account for validating the number of arguments and invoking package-private methods according to received commands.
 * The cache write back method `Cache.writeBack()` which enabling the persistence of Gitlet is also invoked in this class.
 *
 * @author XIE Changyuan
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        assertNotArgsNum(args, 0);
        String command = args[0];
        String[] operands = getOperands(args);

        switch (command) {
            case "init" -> {
                assertArgsNum(operands, 0);
                Repository.init();
            }
            case "add" -> {
                assertArgsNum(operands, 1);
                Repository.add(operands[0]);
            }
            case "commit" -> {
                assertArgsNum(operands, 1);
                Repository.commit(operands[0]);
            }
            case "rm" -> {
                assertArgsNum(operands, 1);
                Repository.rm(operands[0]);
            }
            case "log" -> {
                assertArgsNum(operands, 0);
                Repository.log();
            }
            case "global-log" -> {
                assertArgsNum(operands, 0);
                Repository.globalLog();
            }
            case "find" -> {
                assertArgsNum(operands, 1);
                Repository.find(operands[0]);
            }
            case "status" -> {
                assertArgsNum(operands, 0);
                Repository.status();
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
                    default -> printAndExit("Invalid number of arguments for: checkout.");
                }
            }
            case "branch" -> {
                assertArgsNum(operands, 1);
                Repository.branch(operands[0]);
            }
            case "rm-branch" -> {
                assertArgsNum(operands, 1);
                Repository.rmBranch(operands[0]);
            }
            case "reset" -> {
                assertArgsNum(operands, 1);
                Repository.reset(operands[0]);
            }
            case "merge" -> {
                assertArgsNum(operands, 1);
                Repository.merge(operands[0]);
            }

            default -> printAndExit("No command with that name exists.");
        }
        Cache.writeBack();
    }

    /* HELPER METHODS ------------------------------------------------------------------------------------------------*/

    /**
     * Throw a GitletException if args don't have exactly n elements.
     * @param args the input arguments
     * @param n the necessary number of arguments
     */
    private static void assertArgsNum(String[] args, int n) {
        if (args.length != n) {
            printAndExit("Incorrect operands.");
        }
    }

    /**
     * Throw a GitletException if args have exactly n elements.
     * @param args the input arguments
     * @param n the tattoo number of arguments
     */
    private static void assertNotArgsNum(String[] args, int n) {
        if (args.length == n) {
            printAndExit("Please enter a command.");
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
        if (!Objects.equals(expected, actual)) {
            printAndExit("Incorrect operands.");
        }
    }
}
