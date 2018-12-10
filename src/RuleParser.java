import java.util.StringTokenizer;

class RuleParser {
    static Rule parseRule(String ruleString) {
        if (ruleString == null || ruleString.equals("")) {
            return null;
        }
        Rule rule = new Rule();
        int conclusionSeparatorIndex = ruleString.indexOf('=');
        String premisses = ruleString.substring(0, conclusionSeparatorIndex);
        String conclusion = ruleString.substring(conclusionSeparatorIndex + 1);
        StringTokenizer tokenizer = new StringTokenizer(premisses, ":;");
        while (tokenizer.hasMoreTokens()) {
            rule.addAttribute(correctWord(tokenizer.nextToken()),correctWord(tokenizer.nextToken()));
        }
        tokenizer = new StringTokenizer(conclusion, ":");
        rule.setTarget(tokenizer.nextToken());
        rule.setTargetValue(tokenizer.nextToken());
        return rule;
    }

    private static String correctWord(String word){
        StringBuffer correctKey = new StringBuffer();
        for (int i = 0; i < word.length(); i++) {
            if (Character.isAlphabetic(word.charAt(i))) {
                correctKey.append(word.charAt(i));
            }
        }
        return correctKey.toString();
    }
}























