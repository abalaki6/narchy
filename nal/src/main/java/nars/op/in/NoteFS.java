package nars.op.in;

import com.google.common.io.Files;
import jcog.io.FSWatch;
import nars.NAR;
import nars.NARS;
import nars.Narsese;
import nars.Task;
import nars.control.NARService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NoteFS extends NARService {

    static final Logger logger = LoggerFactory.getLogger(NoteFS.class);
    final FSWatch fs;
    final Map<Path,List<Task>> loaded = new ConcurrentHashMap<>();
    private final NAR nar;

    public NoteFS(String path, NAR n) throws IOException {
        super(n);
        this.nar = n;
        fs = new FSWatch(path, n.exe, this::reload);
    }

    private void reload(Path path) {

        path = path.toAbsolutePath();

        if (!loadable(path))
            return;

        loaded.compute(path, (p, exists) -> {
            synchronized(fs) {
                if (exists != null)
                    unload(p, exists);

                try {
                    List<Task> t = Narsese.tasks(Files.asCharSource(p.toFile(), Charset.defaultCharset()).read(), nar);
                    logger.info("{} loaded {} tasks", p, t.size());
                    return load(p, t);
                } catch (FileNotFoundException e) {
                    if (exists!=null) {
                        //deleted, or otherwise not found
                        logger.warn("{} {}", p, e.getMessage());
                    } else {
                        logger.error("{} {}", p, e);
                    }
                } catch (IOException e) {
                    logger.error("{} {}", p, e);
                } catch (Narsese.NarseseException e) {
                    logger.error("{} {}", p, e);
                }
            }

            return null; //Collections.emptyList();
        });
    }

    private boolean loadable(Path path) {
        return path.getFileName().toString().endsWith(".nal");
    }

    private List<Task> load(Path path, List<Task> tasks) {
//        tasks.forEach(x -> {
//            logger.info("+ {}", x.toString(nar));
//        });
        nar.input(tasks);
        return tasks;
    }

    private void unload(Path p, List<Task> tasks) {
        logger.info("{} unload {} tasks", p, tasks.size());
        tasks.forEach(t -> {
            if (t.isEternal() && t.isInput())
                nar.retract(t.stamp()[0]);
        });
    }

    public static void main(String[] args) throws IOException {
        NAR n = NARS.realtime().get();
        n.log();
        new NoteFS("/tmp/nal", n);
    }
}