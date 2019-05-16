package tk.bolovsrol.utils;

import java.io.ByteArrayOutputStream;

public final class QuotedPrintable {

    private QuotedPrintable() {
    }

    public static byte[] decode(String quotedPrintable) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(quotedPrintable.length());
        char[] chars = quotedPrintable.toCharArray();
        int i = 0;
        while (i < chars.length) {
            if (chars[i] != '=') {
                baos.write(chars[i]);
            } else {
                if (i < chars.length - 2) {
                    char high = chars[++i];
                    if (high != '\n') {
                        char low = chars[++i];
                        if (high != '\r') {
                            baos.write(Integer.parseInt(String.valueOf(high) + low, 16));
                        }
                    }
                }
            }
            i++;
        }
        return baos.toByteArray();
    }

    public static String encode(byte[] data) {
        StringBuilder result = new StringBuilder(data.length * 3);
        StringBuilder line = new StringBuilder(80);
        for (byte aData : data) {
            int b = (int) aData & 0xff;
            if (b == (int) '\n') {
                result.append(line);
                result.append('\n');
                line.setLength(0);
            } else if (b == (int) '\r' || b >= 32 && b <= 60 || b >= 62 && b <= 126) {
                if (line.length() >= 75) {
                    result.append(line);
                    result.append("=\r\n");
                    line.setLength(0);
                }
                line.append((char) b);
            } else {
                if (line.length() >= 73) {
                    result.append(line);
                    result.append("=\r\n");
                    line.setLength(0);
                }
                line.append('=');
                if (b < 16) {
                    line.append('0');
                }
                line.append(Integer.toHexString(b).toUpperCase());
            }
        }
        result.append(line);

        int po = result.length() - 1;
        while (true) {
            char ch = result.charAt(po);
            if (ch == ' ') {
                result.replace(po, po + 1, "=20");
                po--;
            } else {
                break;
            }
        }

        return result.toString();
    }

//    public static void main(String[] args) throws Exception {
//        String quotedPrintable = "=ED=C5=CE=D1 =DA=CF=D7=D5=D4 =F7=C1=CE=D1 =D1 =C9=DA =FE=C5=D2=CE=CF=D7=C3=\n=CF=D7 =CD=CE=C5 23 =C7=CF=C4=C1 =D6=C5=CE=C1=D4=D9=CD =CE=C5=C2=D9=CC.=20=";
//        System.out.println(Spell.get(new String(decode(quotedPrintable), "koi8-r")));
//    }

//    public static void main(String[] args) throws Exception {
//        String src = "This line has to be quoted." +
//                "\r\n А потом надо декодировать. ";
//        String quoted = encode(src.getBytes("UTF-8"));
//        System.out.println("Quoted: " + Spell.get(quoted));
//        String dequoted = new String(decode(quoted), "UTF-8");
//        System.out.println("Dequoted: " + Spell.get(dequoted));
//    }
}
