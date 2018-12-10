import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class GUI extends JFrame {
    private final JTextArea outputTextArea;
    private JComboBox<String> targetComboBox;
    private final JLabel questionLabel = new JLabel("Запрос");
    private final DefaultComboBoxModel answerModel = new DefaultComboBoxModel();
    private final JComboBox answerComboBox = new JComboBox(answerModel);
    private final JButton start = new JButton("Начать");
    private final JButton answerButton = new JButton("Ответить");

    private ArrayList<Rule> rules = new ArrayList<>();
    private String finalTarget;
    private List<Pair<String, String>> knownPairs = new ArrayList<>();
    private Vector<String> targets = new Vector<>();

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            findTarget(finalTarget);
        }
    };

    public GUI() {
        super("База знаний");
        init();
        setLayout(new GridLayout(1, 0, 5, 5));

        outputTextArea = new JTextArea();
        outputTextArea.setLineWrap(true);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        add(scrollPane);

        JLabel targetLabel = new JLabel("Выберите критерий поиска: ");
        JPanel panel = new JPanel();
        GridLayout panelLayout = new GridLayout(0, 1);
        panelLayout.setHgap(10);
        panelLayout.setVgap(50);
        panel.setLayout(panelLayout);
        panel.add(targetLabel);
        panel.add(targetComboBox);
        panel.add(questionLabel);
        panel.add(answerComboBox);

        answerButton.setEnabled(false);
        answerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (runnable) {
                    runnable.notify();
                }
            }
        });
        panel.add(answerButton);

        add(panel);

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                answerButton.setEnabled(true);
                start.setEnabled(false);
                Thread otherThread = new Thread(runnable);
                otherThread.start();
            }
        });
        panel.add(start);
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void init() {
        List<String> ruleStrings = new ArrayList<>();
        try {
            ruleStrings = Files.readAllLines(Paths.get("db.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String ruleString : ruleStrings) {
            rules.add(RuleParser.parseRule(ruleString));
        }

        for (Rule rule : rules) {
            if (!targets.contains(rule.getTarget())) {
                targets.add(rule.getTarget());
            }
        }
        targetComboBox = new JComboBox<>(targets);
        setTargetComboBoxActionListener();
    }

    private void setTargetComboBoxActionListener() {
        targetComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (this) {
                    finalTarget = targetComboBox.getSelectedItem().toString();
                    System.out.println(finalTarget);
                }
            }
        });
    }

    private String isRulesValueRight(Rule rule) {
        String[] keys = new String[rule.getAttributes().size()];
        rule.getAttributes().keySet().toArray(keys);

        ArrayList<String> keysOfPairs = new ArrayList<>();
        for (Pair<String, String> pair : knownPairs) {
            keysOfPairs.add(pair.getKey());
        }

        for (String key : keys) {
            if (keysOfPairs.contains(key)) {
                if (!knownPairs.contains(new Pair<>(key, rule.getAttributes().get(key)))) {
                    return "wrong";
                }
            } else {
                return "unknown";
            }
        }

        return "right";
    }

    private Rule findNextRule(String curTarget, int previousRuleIndex) {
        for (int i = 0; i < rules.size(); i++) {
            String target = rules.get(i).getTarget();
            if (i > previousRuleIndex && target.equals(curTarget)) {
                return rules.get(i);
            }
        }
        return null;
    }


    private String findFirstUnknownAttribute(Rule rule) {
        String[] keys = new String[rule.getAttributes().size()];
        rule.getAttributes().keySet().toArray(keys);

        ArrayList<String> keysOfPairs = new ArrayList<>();
        for (Pair<String, String> pair : knownPairs) {
            keysOfPairs.add(pair.getKey());
        }

        for (String key : keys) {
            if (!keysOfPairs.contains(key)) {
                return key;
            }
        }

        return null;
    }

    private String contextStackFindKey(LinkedList<Pair<String, String>> contextStack, String key) {
        for (Pair<String, String> pair : contextStack) {
            if (pair.getKey().equals(key)) {
                return pair.getValue();
            }
        }
        return null;
    }

    private Vector<String> findAllValues(String target) {
        Vector<String> result = new Vector<>();
        LinkedHashMap<String, String> attrs;
        for (Rule rule : rules) {
            attrs = rule.getAttributes();
            if (attrs.get(target) != null && !result.contains(attrs.get(target))) {
                result.add(attrs.get(target));
            }
        }
        return result;
    }

    private void findTarget(String target) {
        LinkedList<String> targetsStack = new LinkedList<>();
        LinkedList<Integer> targetsStackForRulesIndexes = new LinkedList<>();
        //key - признак, value - значение

        if (target == null){
            target = targetComboBox.getSelectedItem().toString();
        }

        LinkedList<Pair<String, String>> contextStack = new LinkedList<>();
        Rule curRule = new Rule();
        String curTarget = target;
        boolean flag = false;
        ArrayList<Integer> acceptedRules = new ArrayList<>();
        ArrayList<Integer> discardedRules = new ArrayList<>();
        int previousRuleIndex = -1;
        boolean jumpToCase = false;

        targetsStack.addLast(target);
        while (!flag) {
            if (!jumpToCase) {
                curRule = findNextRule(curTarget, previousRuleIndex);
            }
            if (jumpToCase || curRule != null) {
                jumpToCase = false;
                switch (isRulesValueRight(curRule)) {
                    case "right":
                        contextStack.addLast(new Pair<>(curRule.getTarget(), curRule.getTargetValue()));
                        knownPairs.add(new Pair<>(curRule.getTarget(), curRule.getTargetValue()));
                        acceptedRules.add(rules.indexOf(curRule));
                        outputTextArea.append(curRule.toString());
                        targetsStack.pollLast();
                        if (targetsStack.isEmpty()) {
                            flag = true;
                        } else {
                            curTarget = targetsStack.pollLast();//not remove?
                            previousRuleIndex = -1;
                        }
                        break;
                    case "wrong":
                        discardedRules.add(rules.indexOf(curRule));
                        rules.remove(curRule);
                        //
                        curTarget = curRule.getTarget();
                        break;
                    case "unknown":
                        curTarget = findFirstUnknownAttribute(curRule);
                        targetsStack.addLast(curTarget);
                        targetsStackForRulesIndexes.addLast(rules.indexOf(curRule));//duplicate rules!
                        break;
                    default:
                }
            } else {
                if (!targets.contains(curTarget)) {
                    questionLabel.setText("Выберите " + curTarget);
                    answerModel.removeAllElements();
                    answerModel.addElement("-");
                    Vector<String> vector = findAllValues(curTarget);
                    for (String value : vector) {
                        answerModel.addElement(value);
                    }
                    answerModel.setSelectedItem("-");
                    synchronized (runnable) {
                        while (answerComboBox.getSelectedItem().toString().equals("-")) {
                            try {
                                runnable.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    targetsStack.pollLast();
                    outputTextArea.append("\n" + curTarget + " - " + answerComboBox.getSelectedItem().toString() + "\n");
                    contextStack.addLast(new Pair<>(curTarget, answerComboBox.getSelectedItem().toString()));
                    knownPairs.add(new Pair<>(curTarget, answerComboBox.getSelectedItem().toString()));
                    if (!targetsStackForRulesIndexes.isEmpty()) {
                        jumpToCase = true;
                        curRule = rules.get(targetsStackForRulesIndexes.pollLast());
                    }
                } else {
                    flag = true;
                }
            }
        }
        String answer = contextStackFindKey(contextStack, target);
        if (answer != null) {
            outputTextArea.append("\nОтвет: " + target + " - " + answer + ".\n");
        } else {
            outputTextArea.append("\nОтсутствует в базе знаний.\n");
        }
    }
}
