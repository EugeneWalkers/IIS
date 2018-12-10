import java.util.LinkedHashMap;

public class Rule {
    /*private String attribute;
    private String value;*/

    private LinkedHashMap<String, String> attributes;
    private String target;
    private String targetValue;
    private int nextAttribute;

    public Rule() {
        attributes = new LinkedHashMap<>();
        nextAttribute = 0;
    }

    public LinkedHashMap<String, String> getAttributes() {
        return attributes;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public void addAttribute(String attribute, String value) {
        attributes.put(attribute, value);
    }

    @Override
    public String toString() {
        StringBuilder ruleString = new StringBuilder("Если: ");
        attributes.forEach((attribute, value) -> ruleString.append(attribute).append(" - ").append(value).append(", "));
        ruleString.append("то: ");
        ruleString.append(target).append(" - ").append(targetValue).append(".\n");
        return ruleString.toString();
    }
}
