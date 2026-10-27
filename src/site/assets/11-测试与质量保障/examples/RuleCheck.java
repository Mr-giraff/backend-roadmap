public class RuleCheck {
    public static void main(String[] args) {
        if (!"ODS".equals(NameRules.normalize(" ODS "))) {
            throw new AssertionError("trim failed");
        }
        try {
            NameRules.normalize(" ");
            throw new AssertionError("blank accepted");
        } catch (IllegalArgumentException expected) {
            if (!"名称不能为空".equals(expected.getMessage())) {
                throw new AssertionError("wrong blank error", expected);
            }
        }
        NameChecker checker = new NameChecker(name -> "ODS".equals(name));
        try {
            checker.requireAvailable(" ODS ");
            throw new AssertionError("duplicate accepted");
        } catch (IllegalStateException expected) {
            if (!"名称已存在".equals(expected.getMessage())) {
                throw new AssertionError("wrong duplicate error", expected);
            }
        }
        if (!"ADS".equals(checker.requireAvailable(" ADS "))) {
            throw new AssertionError("available name failed");
        }
        System.out.println("RuleCheck: PASS (trim, blank, duplicate, available)");
    }
}
