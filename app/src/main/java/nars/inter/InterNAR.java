package nars.inter;


import com.google.common.collect.Lists;
import nars.NAR;
import nars.concept.Concept;
import nars.inter.gnutella.*;
import nars.nal.Tense;
import nars.nar.Default;
import nars.task.Task;
import nars.util.IO;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

import static nars.$.$;

/**
 * Peer interface for an InterNARS mesh
 * https://github.com/addthis/meshy/blob/master/src/test/java/com/addthis/
 */
public class InterNAR extends Peer implements ClientModel {

    final Logger logger;
    final NAR nar;

    public InterNAR(NAR n) throws IOException {
        super();

        logger = LoggerFactory.getLogger(n.self + "," + getClass().getSimpleName());

        this.nar = n;
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        NAR a = new Default().setSelf("a");
        InterNAR ai = new InterNAR(a);

        NAR b = new Default().setSelf("b");
        InterNAR bi = new InterNAR(b);

        bi.connect(ai);

        b.believe("(X --> y)");

        String question = "(?x --> y)?";
        ai.query(question);
        //TODO a.ask(question);

        Thread.sleep(2000);

        a.onTask(tt -> {
           System.out.println(b + ": " + tt);
        });


        a.run(16);
        b.run(16);

    }


    public void send(Task t) {

        DataOutput d = new DataOutputStream(null);
        try {
            IO.writeTask(d, t);
            //o.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onQueryHit(Client client, QueryHitMessage q) {
        logger.info("{} told \"{}\" by {}", nar.self, Arrays.toString(q.getFileName()), q.responder);

    }

    @Override
    public void data(Client client, String file, ByteBuffer b, int rangeByte) {

    }

    @Override
    public void search(Client client, QueryMessage message, Consumer<QueryHitMessage> eachResult) {
        String queryString = message.query;
        logger.info("{} asked \"{}\" by {}", nar.self, queryString, message.recipient);

        final int[] count = { 3 };
        nar.ask($(queryString), Tense.ETERNAL, a -> {
            eachResult.accept(client.createQueryHit(message.idBytes(), 1, Lists.newArrayList(
                    Triple.of(a.toString(), 0, 0)
            )) );
            return (count[0]--) > 0;
        });
    }

    @Override
    public byte[] data(Client client, String file, int rangePosition) {
        return null;
    }

}
