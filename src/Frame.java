import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.zip.CRC32;

public class Frame {
    byte Flag, Control;
    int[] Address;
    String Data;
    int FCS;
    static int nextFrame = 0;

    public Frame(int[] address, byte control, String data) {
        Flag = 0b01111110;
        Address = address;
        Control = control;
        Data = data;
    }

    public static String[] getData(String filePath) {
        FileReader in;
        BufferedReader br;
        String s;

        ArrayList<String> strings = new ArrayList<String>();
        try {
            in = new FileReader(filePath);

            br = new BufferedReader(in);
            while ((s = br.readLine()) != null) {
                strings.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        strings.add("0");
        return strings.toArray(new String[0]);
    }

    public String toString() {
        StringBuilder temp = new StringBuilder("01111110"), temp2;
        int i, len = Data.length(), j;
        for (i = 0; i < 4; i++) {
            temp2 = new StringBuilder();
            temp2.append(Integer.toBinaryString(Address[i]));

            for (j = temp2.length(); j < 8; j++)
                temp2.insert(0, "0");
            temp.append(temp2);
        }
        temp2 = new StringBuilder();
        temp2.append(Integer.toBinaryString(Control));

        for (j = temp2.length(); j < 8; j++)
            temp2.insert(0, "0");
        temp.append(temp2);

        for (i = 0; i < len; i++) {
            temp2 = new StringBuilder();
            temp2.append(Integer.toBinaryString(Data.charAt(i)));
            for (j = temp2.length(); j < 8; j++)
                temp2.insert(0, "0");
            temp.append(temp2);
        }
        StringBuilder stringBuilder = new StringBuilder(temp.substring(48));
        for (i = 0; i < 32; i++)
            stringBuilder.append("0");
        temp2 = new StringBuilder();
        temp2.append(MakeCRC32(stringBuilder));

        for (j = temp2.length(); j < 32; j++)
            temp2.insert(0, "0");
        temp.append(temp2);
        len = temp.length();
        int is1 = 0;
        for (i = 8; i < len; i++) {
            if (is1 == 5) {
                temp.insert(i, '0');
                len++;
                is1 = 0;
            } else if (temp.charAt(i) == '0') {
                is1 = 0;
            } else {
                is1++;
            }
        }

        temp.append("01111110");
        return temp.toString();
    }

    public static Frame[] makeFrame(int[] address, String[] datas) {
        byte control, i;
        Frame[] frames = new Frame[datas.length];

        for (i = 0; i < datas.length; i++) {
            control = (byte) ((i % 8) * 16 + ((i + 1) % 8));
            frames[i] = new Frame(address, control, datas[i]);
        }
        return frames;
    }

    public static String Decode(String data) {
        int i, j, len = data.length() - 8, is1 = 0;
        StringBuilder temp = new StringBuilder(data.substring(0, len));
        for (i = 8; i < len; i++) {
            if (is1 == 5) {
                temp.deleteCharAt(i);
                is1 = 0;
                i--;
                len--;
            } else if (temp.charAt(i) == '0') {
                is1 = 0;
            } else
                is1++;
        }
        String str = temp.substring(48, temp.length() - 31);
        StringBuilder temp2 = new StringBuilder(temp.substring(48));


        if (!testCRC32(temp2)) {
            return null;
        }
        StringBuilder tmp = new StringBuilder();
        int count = str.length() / 8, num;
        char[] str2 = new char[count];
        for (i = 0; i < count; i++) {
            num = 0;
            for (j = i * 8; j < (i + 1) * 8; j++) {

                num = num * 2 + str.charAt(j) - '0';
            }
            str2[i] = (char) num;
        }
        tmp.append(str2);
        return tmp.toString();
    }

    static StringBuilder MakeCRC32(StringBuilder data) {
        StringBuilder temp = data, CRC32_div = new StringBuilder("100000100110000010001110110110111");
        int i, j, len1 = data.length() - CRC32_div.length(), len2 = CRC32_div.length();
        for (i = 0; i <= len1; i++) {
            if (temp.charAt(i) == '1') {
                for (j = 0; j < len2; j++) {
                    if (temp.charAt(i + j) == CRC32_div.charAt(j))
                        temp.deleteCharAt(i + j).insert(i + j, '0');
                    else
                        temp.deleteCharAt(i + j).insert(i + j, '1');
                }
            }
        }
        while (temp.charAt(0) == '0')
            temp.deleteCharAt(0);
        return temp;
    }

    static boolean testCRC32(StringBuilder data) {
        StringBuilder temp = data, CRC32_div = new StringBuilder("100000100110000010001110110110111");
        int i, j, len1 = data.length() - CRC32_div.length(), len2 = CRC32_div.length();
        for (i = 0; i <= len1; i++) {
            if (temp.charAt(i) == '1') {
                for (j = 0; j < len2; j++) {
                    if (temp.charAt(i + j) == CRC32_div.charAt(j))
                        temp.deleteCharAt(i + j).insert(i + j, '0');
                    else
                        temp.deleteCharAt(i + j).insert(i + j, '1');
                }
            }
        }
        len1 = data.length();
        for (i = 0; i < len1; i++) {
            if (data.charAt(i) == '1')
                return false;
        }
        return true;
    }

    static boolean isNextFrame(String data) {

        String temp = data.substring(42);
        int i, num = 0;
        for (i = 0; i < 3; i++)
            num = num * 2 + temp.charAt(i) - '0';
        if (nextFrame != num) {
            return false;
        }
        num = 0;
        for (i++; i < 7; i++)
            num = num * 2 + temp.charAt(i) - '0';

        nextFrame = num;
        return true;
    }

    static String getDecoded(String data) {
        int i, len = data.length() - 8, is1 = 0;
        StringBuilder temp = new StringBuilder(data.substring(0, len));
        for (i = 8; i < len; i++) {
            if (is1 == 5) {
                temp.deleteCharAt(i);
                is1 = 0;
                i--;
                len--;
            } else if (temp.charAt(i) == '0') {
                is1 = 0;
            } else
                is1++;
        }
        return temp.toString();
    }
}

