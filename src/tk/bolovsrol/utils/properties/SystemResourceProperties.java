package tk.bolovsrol.utils.properties;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.sources.EmptyReadOnlySource;
import tk.bolovsrol.utils.properties.sources.MapReadOnlySource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Хелпер для чтения системных ресурсов в проперти.
 * <p/>
 * Использовать предполагается как-то так:
 * <pre>
 *   public static final String RESOURCE_NAME = "ru/plasticmedia/foo.properties";
 *
 *   public static ReadOnlyProperties get() {
 *       try {
 *           return SystemResourceProperties.get(RESOURCE_NAME);
 *       } catch (Exception e) {
 *           throw new RuntimeException("Error reading internal Foo Properties", e);
 *       }
 *   }
 * </pre>
 */
public final class SystemResourceProperties {
	private static final boolean LOG = Cfg.getBoolean("log.resourceProperties", System.getProperty("log.resourceProperties", "false").equals("true"));

	private SystemResourceProperties() {
	}

    private static final ConcurrentMap<String, ReadOnlyProperties> PROPERTIES = new ConcurrentSkipListMap<>();

    /**
     * Возвращает найденные ресурсы с указанным именем.
     * <p/>
     * Если ни одного ресурса не найдено, возвращает пустые проперти {@link EmptyReadOnlySource#EMPTY_PROPERTIES}.
     * <p/>
     * Если найдено два и более ресурса, читает их последовательно в естественном порядке их появления,
     * и возвращает проперти с получившейся картой с, возможно, перезаписанными ключами.
     *
     * @param propertyResource файл ресурса
     * @return проперти указанного файла
     * @throws SystemResourcePropertiesLoadingException
     */
    public static ReadOnlyProperties get(String propertyResource) throws SystemResourcePropertiesLoadingException {
        ReadOnlyProperties result = PROPERTIES.get(propertyResource);
        if (result == null) {
            try {
                result = readResources(propertyResource);
            } catch (IOException e) {
                throw SystemResourcePropertiesLoadingException.getForResourceName(propertyResource, e);
            }
            PROPERTIES.putIfAbsent(propertyResource, result);
        }
        return result;
    }

    private static ReadOnlyProperties readResources(String propertyResource) throws IOException {
        if (LOG) {
            Log.trace("Looking for system resources by name " + Spell.get(propertyResource) + "...");
        }
        Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(propertyResource);
        if (!urlEnumeration.hasMoreElements()) {
            if (LOG) {
                Log.trace("No properties found");
            }
            return EmptyReadOnlySource.EMPTY_PROPERTIES;
        } else {
            Properties p = new Properties();
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                if (LOG) {
                    Log.trace("Adding properties from resource " + Spell.get(url) + "...");
                }
                InputStream is = url.openStream();
                try {
                    p.load(is);
                } finally {
                    is.close();
                }
            }
            Map<String, String> lhm = new TreeMap<>();
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                // можно (нужно) просто кастить, так как наши ресурсы должны содержать только строки.
                lhm.put((String) entry.getKey(), (String) entry.getValue());
            }
            return new ReadOnlyProperties(new MapReadOnlySource(lhm));
        }
    }

}
