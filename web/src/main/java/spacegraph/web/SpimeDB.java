package spacegraph.web;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.extensions.PerMessageDeflateHandshake;
import jcog.Texts;
import jcog.Util;
import jcog.data.MutableInteger;
import nars.Wiki;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.nar.Default;
import nars.term.Term;
import nars.term.obj.IntTerm;
import nars.test.DeductiveMeshTest;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static io.undertow.Handlers.*;
import static io.undertow.UndertowOptions.ENABLE_HTTP2;
import static io.undertow.UndertowOptions.ENABLE_SPDY;
import static java.util.zip.Deflater.BEST_COMPRESSION;
import static nars.$.*;
import static nars.net.IRCAgent.hear;
import static nars.net.IRCAgent.newRealtimeNAR;


public class SpimeDB /*extends PathHandler*/ {


    public final Undertow server;
    private final PathHandler path;


    final static Logger logger = LoggerFactory.getLogger(SpimeDB.class);


    public static HttpHandler socket(WebSocketConnectionCallback w) {
        return websocket(w).addExtension(new PerMessageDeflateHandshake(false, BEST_COMPRESSION));
    }


    @SuppressWarnings("HardcodedFileSeparator")
    public SpimeDB(int httpPort) {


        this.path = path()
                .addPrefixPath("/", resource(

                        new FileResourceManager(getResourcePath().toFile(), 0))

//                        new CachingResourceManager(
//                                16384,
//                                16*1024*1024,
//                                new DirectBufferCache(100, 10, 1000),
//                                new PathResourceManager(getResourcePath(), 0, true, true),
//                                0 //7 * 24 * 60 * 60 * 1000
//                        ))
                                .setCachable((x) -> false)
                                .setDirectoryListingEnabled(true)
                                .addWelcomeFiles("spime.html")
                );

        //https://github.com/undertow-io/undertow/blob/master/examples/src/main/java/io/undertow/examples/sessionhandling/SessionServer.java

        server = Undertow.builder()
                .addHttpListener(httpPort, "0.0.0.0")
                .setServerOption(ENABLE_HTTP2, true)
                .setServerOption(ENABLE_SPDY, true)
                .setIoThreads(4)
                .setHandler(path)
                .build();


//        path
//                .addPrefixPath("/{chan}/feed", socket(new WebsocketRouter()));


        logger.info("http start: port={}", httpPort);
        synchronized (server) {
            server.start();
        }


    }

    private Path getResourcePath() {
        //TODO use ClassPathHandler and store the resources in the .jar

        File c = new File("./web/src/main/resources/");
        //File c = new File(WebServer.class.getResource("/").getPath());
        String cp = c.getAbsolutePath().replace("./", "");

        if (cp.contains("web/web")) //happens if run from web/ directory
            cp = cp.replace("web/web", "web");

        return Paths.get(
                //System.getProperty("user.home")
                cp
        );
    }


    public void stop() {
        synchronized (server) {
            server.stop();
            logger.info("stop");
        }
    }

    public static void main(String[] args) throws Exception {


        int httpPort = args.length < 1 ? 8080 : Integer.parseInt(args[0]);

        SpimeDB w = new SpimeDB(httpPort);

        //new IRCServer("localhost", 6667);

        @NotNull Default nar = newRealtimeNAR(512, 3, 2);
        nar.input("a:b. b:c. c:d. d:e.");
        //Default nar = new Default();

//        new IRCAgent(nar,
//                "experiment1", "irc.freenode.net",
//                //"#123xyz"
//                "#netention"
//                //"#nars"
//        ).start();


        {
            //access to the NAR

            final Set<Class> classWhitelist = newHashSet(4);
            classWhitelist.add(org.apache.commons.lang3.mutable.MutableFloat.class);
            classWhitelist.add(MutableInteger.class);
            classWhitelist.add(AtomicBoolean.class);
            classWhitelist.add(AtomicLong.class);

            Field[] ff = nar.getClass().getFields();

        }


        nar.on("grid", (terms) -> {
            IntTerm x = (IntTerm) terms[0];
            IntTerm y = (IntTerm) terms[1];
            new DeductiveMeshTest(nar, new int[] { x.val(), y.val() });
            return null;
        });

        nar.on("read", (terms) -> {

            String protocol = terms[0].toString();
            String lookup = terms[1].toString();
            switch (protocol) {
                case "wiki": //wikipedia
                    String base = "simple.wikipedia.org";
                    //"en.wikipedia.org";
                    Wiki enWiki = new Wiki(base);

                    try {
                        //remove quotes
                        String page = enWiki.normalize(lookup.replace("\"", ""));
                        //System.out.println(page);

                        enWiki.setMaxLag(-1);

                        String html = enWiki.getRenderedText(page);
                        html = StringEscapeUtils.unescapeHtml4(html);
                        String strippedText = html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ").toLowerCase();

                        //System.out.println(strippedText);

                        hear(nar, strippedText, page, 200 /*ms per word */);

                        return the("Reading " + base + ":" + page + ": " + strippedText.length() + " characters...");

                    } catch (IOException e) {
                        return the(e.toString());
                    }

                case "url":
                    //TODO
                    break;

                case "json":
                    //TODO
                    break;

                //...

            }

            return the("Unknown protocol");

        });
        nar.on("clear", (terms) -> {
            long dt = Util.time(() -> {
                ((Default) nar).core.active.clear();
            });
            return the("Ready (" + dt + " ms)");
        });
        nar.on("memstat", (terms) ->
            quote(nar.concepts.summary())
        );
        nar.on("top", (terms) -> {

                    int length = 16;
                    List<Term> b = newArrayList(length);
                    @NotNull Bag<Concept> cbag = ((Default) nar).core.active;

                    String query;
//            if (arguments.size() > 0 && arguments.term(0) instanceof Atom) {
//                query = arguments.term(0).toString().toLowerCase();
//            } else {
                    query = null;
//            }

                    cbag.forEachWhile(c -> {
                        String bs = c.get().toString();
                        if (query == null || bs.toLowerCase().contains(query)) {
                            b.add(p(c.get().term(),
                                    quote(
                                            //TODO better summary, or use a table representation
                                            Texts.n2(c.pri())
                                    )));
                        }
                        return b.size() <= length;
                    });

                    return p(b);
                }

        );

        new nars.web.NARServices(nar, w.path);

        //new IRCAgent(nar, "localhost", "NARchy", "#x");





    }


    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     * @throws InterruptedException When socket related I/O errors occur.
     */
    /*public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }*/

}
