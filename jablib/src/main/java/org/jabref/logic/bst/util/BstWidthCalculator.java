package org.jabref.logic.bst.util;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

///
/// The |built_in| function {\.{purify\$}} pops the top (string) literal, removes
/// nonalphanumeric characters except for |white_space| and |sep_char| characters
/// (these get converted to a |space|) and removes certain alphabetic characters
/// contained in the control sequences associated with a special character, and
/// pushes the resulting string. If the literal isn't a string, it complains and
/// pushes the null string.
@NullMarked
public class BstWidthCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BstWidthCalculator.class);

    private static final int MAX_ASCII_CHAR = 128;

    // Named constants for special character combination widths
    private static final int WIDTH_OE_LOWER = 778;
    private static final int WIDTH_OE_UPPER = 1014;
    private static final int WIDTH_AE_LOWER = 722;
    private static final int WIDTH_AE_UPPER = 903;
    private static final int WIDTH_SS_LOWER = 500;

    /*
     * Quoted from Bibtex:
     *
     * Now we initialize the system-dependent |char_width| array, for which
     * |space| is the only |white_space| character given a nonzero printing
     * width. The widths here are taken from Stanford's June~'87 $cmr10$~font
     * and represent hundredths of a point (rounded), but since they're used
     * only for relative comparisons, the units have no meaning.
     */
    private static final int[] CHAR_WIDTHS = {
            // 0-31: Control characters
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            // 32-47: space ! " # $ % & ' ( ) * + , - . /
            278, 278, 500, 833, 500, 833, 778, 278, 389, 389, 500, 778, 278, 333, 278, 500,
            // 48-63: 0 1 2 3 4 5 6 7 8 9 : ; < = > ?
            500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 278, 278, 278, 778, 472, 472,
            // 64-79: @ A B C D E F G H I J K L M N O
            778, 750, 708, 722, 764, 681, 653, 785, 750, 361, 514, 778, 625, 917, 750, 778,
            // 80-95: P Q R S T U V W X Y Z [ \ ] ^ _
            681, 778, 736, 556, 722, 750, 750, 1028, 750, 750, 611, 278, 500, 278, 500, 278,
            // 96-111: ` a b c d e f g h i j k l m n o
            278, 500, 556, 444, 556, 444, 306, 500, 556, 278, 306, 528, 278, 833, 556, 500,
            // 112-127: p q r s t u v w x y z { | } ~ DEL
            556, 528, 392, 394, 389, 556, 528, 722, 528, 528, 444, 500, 1000, 500, 500, 0
    };

    private BstWidthCalculator() {
    }

    private static int getSpecialCharWidth(char[] c, int pos) {
        if ((pos + 1) < c.length) {
            if ((c[pos] == 'o') && (c[pos + 1] == 'e')) {
                return WIDTH_OE_LOWER;
            }
            if ((c[pos] == 'O') && (c[pos + 1] == 'E')) {
                return WIDTH_OE_UPPER;
            }
            if ((c[pos] == 'a') && (c[pos + 1] == 'e')) {
                return WIDTH_AE_LOWER;
            }
            if ((c[pos] == 'A') && (c[pos + 1] == 'E')) {
                return WIDTH_AE_UPPER;
            }
            if ((c[pos] == 's') && (c[pos + 1] == 's')) {
                return WIDTH_SS_LOWER;
            }
        }
        return getCharWidth(c[pos]);
    }

    public static int getCharWidth(char c) {
        if ((c >= 0) && (c < MAX_ASCII_CHAR)) {
            return CHAR_WIDTHS[c];
        } else {
            return 0;
        }
    }

    public static int width(String toMeasure) {
        /*
         * From Bibtex: We use the natural width for all but special characters,
         * and we complain if the string isn't brace-balanced.
         */

        int i = 0;
        int n = toMeasure.length();
        int braceLevel = 0;
        char[] c = toMeasure.toCharArray();
        int result = 0;

        /*
         * From Bibtex:
         *
         * We use the natural widths of all characters except that some
         * characters have no width: braces, control sequences (except for the
         * usual 13 accented and foreign characters, whose widths are given in
         * the next module), and |white_space| following control sequences (even
         * a null control sequence).
         *
         */
        while (i < n) {
            if (c[i] == '{') {
                braceLevel++;
                if ((braceLevel == 1) && ((i + 1) < n) && (c[i + 1] == '\\')) {
                    i++; // skip brace
                    while ((i < n) && (braceLevel > 0)) {
                        i++; // skip backslash

                        int afterBackslash = i;
                        while ((i < n) && Character.isLetter(c[i])) {
                            i++;
                        }
                        if ((i < n) && (i == afterBackslash)) {
                            i++; // Skip non-alpha control seq
                        } else {
                            if (BstCaseChanger.findSpecialChar(c, afterBackslash).isPresent()) {
                                result += getSpecialCharWidth(c, afterBackslash);
                            }
                        }
                        while ((i < n) && Character.isWhitespace(c[i])) {
                            i++;
                        }
                        while ((i < n) && (braceLevel > 0) && (c[i] != '\\')) {
                            if (c[i] == '}') {
                                braceLevel--;
                            } else if (c[i] == '{') {
                                braceLevel++;
                            } else {
                                result += getCharWidth(c[i]);
                            }
                            i++;
                        }
                    }
                    continue;
                }
            } else if (c[i] == '}') {
                if (braceLevel > 0) {
                    braceLevel--;
                } else {
                    LOGGER.warn("Too many closing braces in string: {}", toMeasure);
                }
            }
            result += getCharWidth(c[i]);
            i++;
        }
        if (braceLevel > 0) {
            LOGGER.warn("No enough closing braces in string: {}", toMeasure);
        }
        return result;
    }
}
