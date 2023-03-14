import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import static java.lang.Character.isDigit;
import static java.lang.Character.isUpperCase;

public class Main {
    static HashMap<String, Boolean> values=new HashMap<>();
    static int trueCount = 0;
    static int falseCount = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LinkedList<Lexeme> lexemes = lexemeReader(scanner.nextLine());
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
        Node tree = expression(lexemes);
        compute(tree);
    }


    static void compute(Node tree) {
        String[] keysArray = new String[values.size()];
        values.keySet().toArray(keysArray);
        for (int i = 0; i < Math.pow(2, values.size()); i++) {
            StringBuilder converted = new StringBuilder(Integer.toBinaryString(i));
            int diff = values.size() - converted.length();
            for (int j = 0; j < diff; j++) {
                converted.insert(0, "0");
            }
            for (int j = 0; j < values.size(); j++) {
                if (converted.charAt(j) == '0') {
                    values.put(keysArray[j], false);
                } else {
                    values.put(keysArray[j], true);
                }
            }
            if (execute(tree)) {
                trueCount++;
            }
            else {
                falseCount++;
            }
        }
        if (falseCount==0){
            System.out.println("Valid");
        }
        else if(trueCount==0){
            System.out.println("Unsatisfiable");
        }else {
            System.out.printf("Satisfiable and invalid, %d true and %d false cases", trueCount, falseCount);
        }
    }

    static boolean execute(Node tree){
        switch (tree.type) {
            case IMPLICATION:
                return Operations._implication(execute(tree.left), execute(tree.right));
            case CONJUNCTION:
                return Operations._conjunction(execute(tree.left), execute(tree.right));
            case DISJUNCTION:
                return Operations._disjunction(execute(tree.left), execute(tree.right));
            case DENIAL:
                return Operations._denial(execute(tree.left));
            case VARIABLE:
                return values.get(tree.str);
        }
        System.exit(3);
        return false;
    }


    static Node denial(LinkedList<Lexeme> lexemes) {
        Lexeme lexeme = lexemes.peek();
        lexemes.poll();
        if (lexeme.type == Type.DENIAL) {
            Node new_node = new Node(Type.DENIAL, "!");
            new_node.left = denial(lexemes);
            return new_node;
        }
        if (lexeme.type == Type.VARIABLE) {
            return new Node(Type.VARIABLE, lexeme.str);
        }
        if (lexeme.type == Type.LEFT_PARENTHESIS) {
            Node exp = expression(lexemes);
            lexemes.poll();
            return exp;
        }
        return null;
    }

    static Node conjunction(LinkedList<Lexeme> lexemes) {
        Node left = denial(lexemes);
        while (!lexemes.isEmpty()) {
            if (!lexemes.peek().str.equals("&")) {
                return left;
            }
            lexemes.poll();
            Node new_node = new Node(Type.CONJUNCTION, "&");
            new_node.left = left;
            new_node.right = denial(lexemes);
            left = new_node;
        }
        return left;

    }

    static Node disjunction(LinkedList<Lexeme> lexemes) {
        Node left = conjunction(lexemes);
        while (!lexemes.isEmpty()) {
            if (!lexemes.peek().str.equals("|")) {
                return left;
            }
            lexemes.poll();
            Node new_node = new Node(Type.DISJUNCTION, "|");
            new_node.left = left;
            new_node.right = conjunction(lexemes);
            left = new_node;
        }
        return left;

    }

    static Node expression(LinkedList<Lexeme> lexemes) {
        Node left = disjunction(lexemes);
        if (lexemes.isEmpty()) {
            return left;
        }
        if (!lexemes.peek().str.equals("->")) {
            return left;
        }
        lexemes.poll();
        Node new_node = new Node(Type.IMPLICATION, "->");
        new_node.left = left;
        new_node.right = expression(lexemes);
        return new_node;

    }


    static LinkedList<Lexeme> lexemeReader(String str) {
        str = str.replaceAll("\\s", "");
        LinkedList<Lexeme> lexemes = new LinkedList<>();
        int i = 0;
        while (i<str.length()) {
            String buffer = "";
            if (isUpperCase(str.charAt(i))) {
                for (int j = i; j < str.length(); j++) {
                    if (isUpperCase(str.charAt(j)) || isDigit(str.charAt(j)) || str.charAt(j) == '\'') {
                        if (str.charAt(j) == '’') {
                            buffer += (char) 39;
                        } else {
                            buffer += str.charAt(j);
                        }
                    } else {
                        i = j - 1;
                        break;
                    }
                    if (j==str.length()-1) {
                        i = j;
                    }
                }
                values.put(buffer, false);
                lexemes.add(new Lexeme(Type.VARIABLE, buffer));
                i++;
                continue;
            }
            if (str.charAt(i) == '-') {
                lexemes.add(new Lexeme(Type.IMPLICATION, "->"));
            }
            if (str.charAt(i) == '|') {
                lexemes.add(new Lexeme(Type.DISJUNCTION, "|"));
            }
            if (str.charAt(i) == '&') {
                lexemes.add(new Lexeme(Type.CONJUNCTION, "&"));
            }
            if (str.charAt(i) == '!') {
                lexemes.add(new Lexeme(Type.DENIAL, "!"));
            }
            if (str.charAt(i) == '(') {
                lexemes.add(new Lexeme(Type.LEFT_PARENTHESIS, "("));
            }
            if (str.charAt(i) == ')') {
                lexemes.add(new Lexeme(Type.RIGHT_PARENTHESIS, ")"));
            }
            i++;
        }
        return lexemes;
    }
}

class Lexeme {
    Type type;
    String str;

    Lexeme(Type type, String str) {
        this.type = type;
        this.str = str;
    }
}

class Node {
    Type type;
    String str;
    Node left;
    Node right;

    Node(Type type, String str) {
        this.type = type;
        this.str = str;
    }

    @Override
    public String toString() {
        if (type == Type.DENIAL) {
            return "(!" + left.toString() + ")";
        }
        if (type == Type.VARIABLE) {
            return str;
        }
        return "(" + str + "," + left.toString() + "," + right.toString() + ")";
    }
}

enum Type {
    VARIABLE,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS,
    IMPLICATION,
    CONJUNCTION,
    DISJUNCTION,
    DENIAL
}

