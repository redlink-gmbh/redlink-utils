/*
 * Copyright 2017 redlink GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.redlink.utils.lang.de;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Gender-related helper methods (German)
 */
public class GenderUtils {

    private static final Pattern TOKENIZER = Pattern.compile("[,;]?\\s+");

    /**
     * Extract all explicit gender forms from the label.
     * @param label the label to de-generize
     */
    public static String[] degender(String label) {
        if (label.endsWith("(m./w.)")) {
            return new String[] { label.substring(0,label.length()-7).trim() };
        }

        // Remove parenthesis
        if (label.indexOf('(') < label.indexOf(')') && label.indexOf(')') == label.length()-1) {
            int p = label.indexOf('(');
            String parenth = label.substring(p+1, label.length() -1);
            label = label.substring(0, p);
            String resultMale = String.format("%s (%s)", getMaleLabel(label), getMaleLabel(parenth));
            String resultFemale = String.format("%s (%s)", getFemaleLabel(label), getFemaleLabel(parenth));
            if(!resultFemale.equals(resultMale)) {
                return new String[] {resultMale, resultFemale };
            } else {
                return new String[] { resultMale };
            }
        } else {
            // split the different terms and de-gender them individually
            String resultMale = getMaleLabel(label);
            String resultFemale = getFemaleLabel(label);
            if(!resultFemale.equals(resultMale)) {
                return new String[] {resultMale, resultFemale };
            } else {
                return new String[] { label };
            }
        }
    }

    private static String getMaleLabel(String label) {
        if (label.indexOf(',') > 0 && label.indexOf(',') == label.lastIndexOf(',')) {
            String[] pair = label.split(",");
            int dist = StringUtils.getLevenshteinDistance(pair[0].trim(), pair[1].trim());
            int wc = count(' ', pair[0])+1;
            if (dist <= 4*wc) {
                return getMaleLabel(pair[0]);
            }
        }

        StringBuilder resultMale = new StringBuilder();
        Matcher m = TOKENIZER.matcher(label);
        int tStart = 0;
        while (m.find()) {
            int tEnd = m.start();
            if (tStart < tEnd) {
                resultMale.append(getMaleWord(label.substring(tStart,tEnd)));
            }
            resultMale.append(m.group());
            tStart = m.end();
        }
        if (tStart < label.length()) {
            resultMale.append(getMaleWord(label.substring(tStart)));
        }
        return resultMale.toString().trim();
    }

    private static int count(char c, String string) {
        int num = 0;
        for(int i = 0; i < string.length(); i++)
            if (string.charAt(i)==c) num++;
        return num;
    }

    private static String getFemaleLabel(String label) {
        if (label.indexOf(',') > 0 && label.indexOf(',') == label.lastIndexOf(',')) {
            String[] pair = label.split(",");
            int dist = StringUtils.getLevenshteinDistance(pair[0].trim(), pair[1].trim());
            int wc = count(' ', pair[1])+1;
            if (dist <= 4*wc) {
                return getFemaleLabel(pair[1]);
            }
        }

        StringBuilder resultFemale = new StringBuilder();
        Matcher m = TOKENIZER.matcher(label);
        int tStart = 0;
        while (m.find()) {
            int tEnd = m.start();
            if (tStart < tEnd) {
                resultFemale.append(getFemaleWord(label.substring(tStart,tEnd)));
            }
            resultFemale.append(m.group());
            tStart = m.end();
        }
        if (tStart < label.length()) {
            resultFemale.append(getFemaleWord(label.substring(tStart)));
        }

        return resultFemale.toString().trim();
    }

    /**
     * Get the male form of a genderised word (e.g. AntragstellerIn -> Antragsteller)
     * @param word the word to degenerize
     */
    private static String getMaleWord(String word) {
        if(word.indexOf(' ') >= 0) {
            throw new IllegalArgumentException("can only convert a single word");
        }

        // special cases
        if(word.endsWith("koch/-köchin")) {
            return word.substring(0,word.length()-8);
        }
        if(word.endsWith("pfleger/-schwester")) {
            return word.substring(0,word.length()-11);
        }
        if(word.endsWith("gehilfe/-gehilfin")) {
            return word.substring(0,word.length()-10);
        }
        if(word.equals("Gehilfe/Gehilfin")) {
            return "Gehilfe";
        }
        if(word.endsWith("mann/-frau")) {
            return word.substring(0,word.length()-6);
        }
        if(word.endsWith("mann/-dame")) {
            return word.substring(0,word.length()-6);
        }
        if(word.endsWith("bursch/-mädchen")) {
            return word.substring(0,word.length()-9);
        }
        if(word.endsWith("arzt/-ärztin")) {
            return word.substring(0,word.length()-8);
        }

        // words ends with (e)in -> replace the (e)in by in and return the word
        if(word.length() > 3 && word.endsWith("(e)in")) {
            return word.substring(0,word.length()-5) + "e";
        }
        // words ends with (er)e -> replace the (er)e by er and return the word
        if(word.length() > 5 && word.endsWith("(er)e")) {
            return word.substring(0,word.length()-5) + "er";
        }
        // words ends with (er)in -> replace the (er)in by er and return the word
        if(word.length() > 6 && word.endsWith("(er)in")) {
            return word.substring(0,word.length()-6) + "er";
        }
        // words ends with (e)innen -> replace the (e)innen by en and return the word
        if(word.length() > 8 && word.endsWith("(e)innen")) {
            return word.substring(0,word.length()-8) + "e";
        }
        // words ends with (en)innen -> replace the (en)innen by en and return the word
        if(word.length() > 9 && word.endsWith("(en)innen")) {
            return word.substring(0,word.length()-9) + "en";
        }

        // words ends with /in -> remove the /in and return the word
        if(word.length() > 3 && word.endsWith("/in")) {
            return word.substring(0,word.length()-3);
        }

        // words ends with /innen -> remove the /innen and return the word
        if(word.length() > 6 && word.endsWith("/innen")) {
            return word.substring(0,word.length()-6);
        }

        // word ends with In -> remove the In and return the word
        if(word.length() > 2 && word.endsWith("In")) {
            return word.substring(0,word.length()-2);
        }

        // word ends with Innen -> remove the Innen and return the word
        if(word.length() > 5 && word.endsWith("Innen")) {
            return word.substring(0,word.length()-5);
        }

        // word ends with R -> replace the R by a small r and return the word
        if(word.length() > 1 && word.endsWith("R")) {
            return word.substring(0,word.length()-1) + "r";
        }
        // word ends with E -> remove the E and return the word (fr)
        if(word.length() > 1 && word.endsWith("E")) {
            return word.substring(0,word.length()-1);
        }



        return word;
    }

    /**
     * Get the male form of a genderised word (e.g. AntragstellerIn -> Antragsteller)
     * @param word the word to degenerize
     */
    private static String getFemaleWord(String word) {
        if(word.indexOf(' ') >= 0) {
            throw new IllegalArgumentException("can only convert a single word");
        }

        // special cases
        if(word.endsWith("koch/-köchin")) {
            return word.substring(0,word.length()-12)+ "köchin";
        }
        if(word.endsWith("pfleger/-schwester")) {
            return word.substring(0,word.length()-18)+"schwester";
        }
        if(word.endsWith("gehilfe/-gehilfin")) {
            return word.substring(0,word.length()-17)+"gehilfin";
        }
        if(word.equals("Gehilfe/Gehilfin")) {
            return "Gehilfin";
        }
        if(word.endsWith("mann/-frau")) {
            return word.substring(0,word.length()-10)+"frau";
        }
        if(word.endsWith("mann/-dame")) {
            return word.substring(0,word.length()-10)+"dame";
        }
        if(word.endsWith("bursch/-mädchen")) {
            return word.substring(0,word.length()-15)+"mädchen";
        }
        if(word.endsWith("arzt/-ärztin")) {
            return word.substring(0,word.length()-12)+"ärztin";
        }

        // words ends with (e)in -> replace the (e)in by in and return the word
        if(word.length() > 3 && word.endsWith("(e)in")) {
            return word.substring(0,word.length()-5) + "in";
        }
        // words ends with (er)e -> replace the (er)e by er and return the word
        if(word.length() > 5 && word.endsWith("(er)e")) {
            return word.substring(0,word.length()-5) + "e";
        }
        // words ends with (er)in -> replace the (er)in by er and return the word
        if(word.length() > 6 && word.endsWith("(er)in")) {
            return word.substring(0,word.length()-6) + "in";
        }
        // words ends with (e)innen -> replace the (e)innen by en and return the word
        if(word.length() > 8 && word.endsWith("(e)innen")) {
            return word.substring(0,word.length()-8) + "innen";
        }
        // words ends with (en)innen -> replace the (en)innen by innen and return the word
        if(word.length() > 9 && word.endsWith("(en)innen")) {
            return word.substring(0,word.length()-9) + "innen";
        }


        // words ends with /in -> remove the /in and return the word
        if(word.length() > 3 && word.endsWith("/in")) {
            return word.substring(0,word.length()-3) + "in";
        }

        // words ends with /innen -> remove the / and return the word
        if(word.length() > 6 && word.endsWith("/innen")) {
            return word.substring(0,word.length()-6) + "innen";
        }


        // word ends with In -> remove the In and return the word
        if(word.length() > 2 && word.endsWith("In")) {
            return word.substring(0,word.length()-2) + "in";
        }

        // word ends with Innen -> remove the Innen and return the word
        if(word.length() > 5 && word.endsWith("Innen")) {
            return word.substring(0,word.length()-5) + "innen";
        }


        // word ends with R -> remove the R and return the word
        if(word.length() > 1 && word.endsWith("R")) {
            return word.substring(0,word.length()-1);
        }
        // word ends with E -> replace the E by a small e and return the word (fr)
        if(word.length() > 1 && word.endsWith("E")) {
            return word.substring(0,word.length()-1) + "e";
        }


        return word;
    }

}
