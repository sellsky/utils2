package tk.bolovsrol.utils.store;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.conf.AutoConfiguration;
import tk.bolovsrol.utils.conf.Param;
import tk.bolovsrol.utils.io.LazyFileOutputStream;
import tk.bolovsrol.utils.io.LineInputStream;
import tk.bolovsrol.utils.io.LineOutputStream;
import tk.bolovsrol.utils.log.LogDome;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Делает ЭТО в файлы в указанной директории.
 * Переданные идентификаторы при необходимости инвалидируются и используются как имена файлов.
 * <p>
 * Прочитанный файл переименовывается в *.bak.
 */
public class DirectoryStoreStreamer implements StoreStreamer<DirectoryStoreStreamer.Conf> {
    private File path;

    public DirectoryStoreStreamer() {
    }

    public static class Conf extends AutoConfiguration {
        @Param(desc = "путь к директории, в которой следует хранить информацию", mandatory = true) public String path;
    }

    @Override public void init(LogDome log, Conf conf) throws UnexpectedBehaviourException {
        if (path != null) {
            throw new IllegalStateException("Already initialized");
        }
        File path = new File(revealEnvironmentMacros(conf.path));
        log.trace("Store directory: " + Spell.get(path));
        validatePath(path);
        this.path = path;
    }

    private static String revealEnvironmentMacros(String s) {
        if (s.indexOf('%') < 0) {
            return s;
        }
		StringBuilder sb = new StringBuilder(s.length() * 2);
		StringTokenizer st = new StringTokenizer(s, "%");
		boolean macro = s.startsWith("%");
        while (st.hasMoreTokens()) {
            String subs = st.nextToken();
            if (macro) {
                sb.append(System.getenv(subs));
            } else {
                sb.append(subs);
            }
            macro = !macro;
        }
        return sb.toString();
    }

    private static void validatePath(File path) throws UnexpectedBehaviourException {
        if (!path.exists() && !path.mkdirs()) {
            throw new UnexpectedBehaviourException("Cannot create directory " + Spell.get(path));
        }
        if (!path.isDirectory()) {
            throw new UnexpectedBehaviourException("Specified path " + Spell.get(path) + " is not a directory");
        }
        if (!path.canRead() && !path.setReadable(true)) {
            throw new UnexpectedBehaviourException("Cannot gain read access to directory " + Spell.get(path));
        }
        if (!path.canWrite() && !path.setWritable(true)) {
            throw new UnexpectedBehaviourException("Cannot gain write access to directory " + Spell.get(path));
        }
//        if (!directory.canExecute() && !directory.setExecutable(true)) {
//            throw new UnexpectedBehaviourException("Cannot gain execute access to directory " + Spell.get(directory));
//        }
    }

    @Override public LineOutputStream newStoreOutputStream(String id) throws IOException {
        File target = new File(path, id);
        deleteViaBak(target);
        return new LineOutputStream(new LazyFileOutputStream(target));
    }

    @Override public LineInputStream newStoreInputStream(String id) throws IOException {
        File file = new File(path, id);
        if (file.exists()) {
            return new LineInputStream(new FileInputStream(file));
        } else {
            return null;
        }
    }

    private static void deleteViaBak(File file) throws IOException {
        if (file.exists()) {
            File bak = new File(file.getAbsolutePath() + ".bak");
			if (bak.exists() && !bak.delete()) {
				throw new IOException("Error deleting " + Spell.get(bak));
            }
            if (!file.renameTo(bak)) {
                throw new IOException("Error renaming " + Spell.get(file) + " to " + Spell.get(bak));
            }
        }
    }

}