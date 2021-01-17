package de.undercouch.citeproc.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberHelper {
    public static class NumberToken {
        private final String token;
        private final NumberTokenType type;

        public NumberToken(String token, NumberTokenType type) {
            this.token = token;
            this.type = type;
        }

        public String getToken() {
            return token;
        }

        public NumberTokenType getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NumberToken that = (NumberToken)o;
            return token.equals(that.token) && type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(token, type);
        }

        @Override
        public String toString() {
            return token + "[" + type + "]";
        }
    }

    public enum NumberTokenType {
        NUMBER,
        SEPARATOR
    }

    private static final String NUMBER = "([a-zA-Z]*[0-9]+[a-zA-Z]*)";
    private static final String SEPARATOR = "([,\\-&])";
    private static final String NUMERIC_REGEX = "^\\s*" + NUMBER + "(\\s*" + SEPARATOR + "\\s*" + NUMBER + ")*\\s*$";
    private static final String EXTRACT_REGEX = NUMBER + "|" + SEPARATOR;
    private static final Pattern numericRegex = Pattern.compile(NUMERIC_REGEX);
    private static final Pattern extractRegex = Pattern.compile(EXTRACT_REGEX);

    public static boolean isNumeric(String str) {
        return numericRegex.matcher(str).matches();
    }

    public static List<NumberToken> tokenize(String str) {
        List<NumberToken> result = new ArrayList<>();
        Matcher matcher = extractRegex.matcher(str);
        while (matcher.find()) {
            String g1 = matcher.group(1);
            String g2 = matcher.group(2);
            if (g1 != null) {
                result.add(new NumberToken(g1, NumberTokenType.NUMBER));
            } else if (g2 != null) {
                result.add(new NumberToken(g2, NumberTokenType.SEPARATOR));
            }
        }
        return result;
    }
}
