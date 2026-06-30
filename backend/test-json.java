public class TestJson {
    public static void main(String[] args) {
        String s = "```json\n{\"test\":\"value\"}\n```";
        System.out.println(s.replaceAll("(?s)^```json\\s*|\\s*```$", "").trim());
    }
}
