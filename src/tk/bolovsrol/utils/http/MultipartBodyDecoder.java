package tk.bolovsrol.utils.http;

import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.caseinsensitive.CaseInsensitiveLinkedHashMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Вытаскивает данные из, например, POST-запроса.
 * <p/>
 * Сделано для парсинга multipart/form-data, остальные тельца возвращает как есть, а этот разбивает на несколько.
 * Парсит довольно грязненько, старается распарсить как можно больше, ошибки обходит как может.
 */
public final class MultipartBodyDecoder {

    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    private MultipartBodyDecoder() {
    }

    public static Map<String, HttpBodyPart> parse(HttpEntity entity) {
        String contentType = entity.getContentType();
        if (contentType == null) {
            return Collections.singletonMap(null, new HttpBodyPart(null, null, null, entity.getBody()));
        } else {
            String[] typeDetails = StringUtils.parseDelimited(contentType, ';');
            String typeItself = typeDetails[0];
            if (MULTIPART_FORM_DATA.equals(typeItself)) {
                return decodeMultipart(entity, typeDetails);
            } else {
                // всё остальное, что мы не понимаем, мы считаем единым тельцем
                return Collections.singletonMap(null, new HttpBodyPart(null, null, entity.headers().dump(), entity.getBody()));
            }
        }
    }

    private static Map<String, HttpBodyPart> decodeMultipart(HttpEntity entity, String[] typeDetails) {
        byte[] body = entity.getBody();
        byte[] boundary = null;
        for (int i = 1; i < typeDetails.length; i++) {
            String typeDetail = typeDetails[i];
            if (typeDetail.startsWith("boundary=")) {
                boundary = typeDetail.substring("boundary=".length()).getBytes(StandardCharsets.ISO_8859_1);
                break;
            }
        }
        if (boundary == null) {
            // в заголовках не оказалось. Ну да не беда, возмём начало тела до первого перевода строки. Возможно, это некорректное поведеннее!
            for (int i = 0; i < body.length; i++) {
                if (body[i] == 0x0d) {
                    boundary = Arrays.copyOf(body, i);
                    break;
                }
            }
            if (boundary == null) {
                // в теле нет переводов строк. Ну и значит это на самом деле одно тельце
                return Collections.singletonMap(null, new HttpBodyPart(null, null, entity.headers().dump(), body));
            }
        }

        // найдём все точки, в которых заканчивается boundary
        ArrayList<Integer> contentStartPositions = new ArrayList<>();
        {
            int pos = boundary.length;
            int limit = body.length - 1;
            while (pos < limit) {
                if (body[pos] == 0x0d && body[pos + 1] == 0x0a && isBoundary(boundary, body, pos)) {
                    // если в предыдущем байте заканчивается делимитер, то это то что надо
                    contentStartPositions.add(pos + 2);
                    pos += boundary.length;
                }
                pos++;
            }
            if (isBoundary(boundary, body, body.length - 4)) {
                contentStartPositions.add(body.length - 2);
            }
        }
        // а теперь для каждой точки выкусим заголовки и тельце.
        Map<String, HttpBodyPart> result = new CaseInsensitiveLinkedHashMap<>();
        {
            int limit = contentStartPositions.size() - 1;
            for (int i = 0; i < limit; i++) {
                int start = contentStartPositions.get(i);
                int end = contentStartPositions.get(i + 1) - boundary.length - 6;
                int pos = start;
                while (pos < end) {
                    if (body[pos] == 0x0d && body[pos + 1] == 0x0a && body[pos + 2] == 0x0d && body[pos + 3] == 0x0a) {
                        String[] kludges = StringUtils.parseDelimited(new String(body, start, pos - start), "\r\n");
                        byte[] subBody = Arrays.copyOfRange(body, pos + 4, end);
                        HttpBodyPart cc = HttpBodyPart.parse(kludges, subBody);
                        result.put(cc.getName(), cc); // возможно, тут образуется дублирование, и какая-то информация пропадёт, но нам, в общем, пофиг
                        break;
                    }
                    pos++;
                }
            }
        }

        return result;
    }

    /**
     * сравнивает boundary с body, начиная с последнего байта boundary и байта lastpos body.
     *
     * @param boundary
     * @param body
     * @param lastpos
     * @return
     */

    private static boolean isBoundary(byte[] boundary, byte[] body, int lastpos) {
        int i = lastpos - 1, u = boundary.length - 1;
        while (u >= 0) {
            if (boundary[u] != body[i]) {
                return false;
            }
            u--;
            i--;
        }
        return true;
    }


//    public static void main(String[] args) throws UnexpectedBehaviourException {
//        byte[] body = StringUtils.getBytesForHexDump("2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 32 31 34.34 32 38 31.38 32 37 35.37 39 38 36.31 39 31 38.39 36 34 33.38 33 35 39.31 0d 0a 43.6f 6e 74 65.6e 74
// 2d 44.69 73 70 6f.73 69 74 69.6f 6e 3a 20.66 6f 72 6d.2d 64 61 74.61 3b 20 6e.61 6d 65 3d.22 74 65 78.74 6c 69 6e.65 22 0d 0a.0d 0a 62 75.64 64 61 64.75 64 64 61.0d 0a 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d
// 2d 2d 32.31 34 34 32.38 31 38 32.37 35 37 39.38 36 31 39.31 38 39 36.34 33 38 33.35 39 31 0d.0a 43 6f 6e.74 65 6e 74.2d 44 69 73.70 6f 73 69.74 69 6f 6e.3a 20 66 6f.72 6d 2d 64.61 74 61 3b.20 6e 61 6d.65 3d 22 64.61 74 61 66.69 6c 65 22.3b 20 66 69
// .6c 65 6e 61.6d 65 3d 22.62 69 67 64.61 74 61 32.30 22 0d 0a.43 6f 6e 74.65 6e 74 2d.54 79 70 65.3a 20 61 70.70 6c 69 63.61 74 69 6f.6e 2f 6f 63.74 65 74 2d.73 74 72 65.61 6d 0d 0a.0d 0a d0 98.20 d0 bc d0.bd d0 be d0.b3 d0 be 2d.d0 bc d0 bd.d0 be d0
// b3.d0 be 20 d1.80 d0 b0 d0.b4 d0 be d1.81 d1 82 d0.b8 20 d0 b4.d0 b5 d1 82.d0 b8 d1 88.d0 ba d0 b0.d0 bc 20 d0.bf d1 80 d0.b8 d0 bd d0.b5 d1 81 d0.bb d0 b0 0a.0d 0a 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d 2d 2d.2d 2d
// 2d 32.31 34 34 32.38 31 38 32.37 35 37 39.38 36 31 39.31 38 39 36.34 33 38 33.35 39 31 2d.2d 0d 0a");
//        System.out.println("body: " + Spell.get(body));
//        HttpRequest hreq = new HttpRequest(Protocol.HTTP_1_0);
////        hreq.getKludges().set("Content-Type", "multipart/form-data; boundary=---------------------------2144281827579861918964383591");
////        hreq.getKludges().set("Content-Length", 431);
//        hreq.setBody(body, "multipart/form-data; boundary=---------------------------2144281827579861918964383591");
//        for (Map.Entry<String, ContentContainer> e : MultipartFormDataDecoder.parse(hreq).entrySet()) {
//            System.out.println(Spell.get(e.getKey()) + '→');
//            System.out.println(Spell.get(e.getValue()));
//        }
//    }
}
