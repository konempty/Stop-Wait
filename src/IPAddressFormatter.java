import javax.swing.text.DefaultFormatter;
import java.text.ParseException;
import java.util.StringTokenizer;

class IPAddressFormatter extends DefaultFormatter {

    public String valueToString(Object value) throws ParseException {
        if (!(value instanceof byte[])) throw new ParseException("Not a byte[]", 0);
        byte[] a = (byte[]) value;
        if (a.length != 4) throw new ParseException("Length != 4", 0);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int b = a[i];
            if (b < 0) b += 256;
            builder.append(String.valueOf(b));
            if (i < 3) builder.append('.');
        }
        return builder.toString();
    }

    public int[] stringToValue(String text) throws ParseException {
        StringTokenizer tokenizer = new StringTokenizer(text, ".");
        int[] a = new int[4];
        for (int i = 0; i < 4; i++) {
            int b ;
            if (!tokenizer.hasMoreTokens()) throw new ParseException("Too few bytes", 0);
            try {
                b = Integer.parseInt(tokenizer.nextToken());
            } catch (NumberFormatException e) {
                throw new ParseException("Not an integer", 0);
            }
            if (b < 0 || b >= 256) throw new ParseException("Byte out of range", 0);
            a[i] = b;
        }
        if (tokenizer.hasMoreTokens()) throw new ParseException("Too many bytes", 0);
        return a;
    }
}