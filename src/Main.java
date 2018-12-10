import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        //File input = new File("rules.txt");
        /*List<String> ruleStrings = new ArrayList<>();
        ArrayList<Rule> rules = new ArrayList<>();
        try {
            ruleStrings = Files.readAllLines(Paths.get("rules.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String ruleString : ruleStrings) {
            rules.add(RuleParser.parseRule(ruleString));
        }
        System.out.println(rules);*/

        GUI frame = new GUI();
        /*Executor e = new Executor();
        new Thread(e).start();*/
    }
}
