package shadowlord.announce;

public class Util {

    public static String join(String[] array, int startInclusive, int endExclusive) {
        if (array == null || array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = startInclusive; i < endExclusive && i < array.length; i++) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(array[i]);
        }
        return sb.toString();
    }
}
