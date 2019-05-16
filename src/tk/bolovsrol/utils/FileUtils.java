package tk.bolovsrol.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Возвращает список всех файлов в указанной поддиректории и её поддиректориях.
     * <p>
     * Сама поддиректория ожидается в той директории, в которой находится переданный класс.
     * <p>
     * Работает и с классами, и с джарами.
     * <p>
     * Содержимое полученных файлов можно получить методом {@link Class#getResourceAsStream(String)}. Например:
     * <pre>
     * try (BufferedReader br = new BufferedReader(new InputStreamReader(rootClass.getResourceAsStream(subdirName + fileName)))) {
     *   ...
     * }
     * </pre>
     *
     * @param rootClass класс для определения базовой директории
     * @param subdirName название поддиректории
     * @return список файлов
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Collection<String> listFiles(Class<?> rootClass, String subdirName) throws IOException, URISyntaxException {
        String basepath = rootClass.getName().replace('.', '/');
        basepath = basepath.substring(0, basepath.lastIndexOf('/') + 1);
        if (!subdirName.endsWith("/")) {
            subdirName += "/";
        }
        String fullpath = basepath + subdirName;

        URL dirURL = rootClass.getClassLoader().getResource(fullpath);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            Path start = Paths.get(dirURL.toURI());
            int startPathLen = start.toString().length() + 1;
            return Files.find(start, Integer.MAX_VALUE, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile())
                .map(Path::toString)
                .map(s -> s.substring(startPathLen))
                .sorted()
                .collect(Collectors.toList());
        }

        if (dirURL == null) { // не знаю, зачем это нужно, у нас урлы сразу хорошие, но пусть будет.
        /*
         * In case of a jar file, we can't actually find a directory.
         * Have to assume the same jar as clazz.
         */
            String me = rootClass.getName().replace('.', '/') + ".class";
            dirURL = rootClass.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
        /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            int fullpathLength = fullpath.length();

            ArrayList<String> result = new ArrayList<>();
            while (jar.entries().hasMoreElements()) {
                String name = jar.entries().nextElement().getName();
                if (!name.startsWith(fullpath) || name.endsWith("/")) {
                    continue;
                }
                name = name.substring(fullpathLength);
                if(name.isEmpty()) {
                    continue;
                }
                result.add(name);
            }
            result.sort(null);
            return result;
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL + ", protocol " + dirURL.getProtocol());
    }
}
