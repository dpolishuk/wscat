package io.dp;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import okio.Buffer;
import okio.ByteString;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Created by dp on 07/04/15.
 */
public class WsCat {

  @Option(name = "-c", usage = "specify websocket url")
  private String url;

  private WebSocket webSocket;

  private final AtomicBoolean doExit = new AtomicBoolean(false);

  private final OkHttpClient httpClient = new OkHttpClient();

  public static void main(String[] args) throws IOException {
    new WsCat().doMain(args);
  }

  public void doMain(String[] args) throws IOException {
    final CmdLineParser parser = new CmdLineParser(this);

    try {
      // parse the arguments.
      parser.parseArgument(args);

      System.out.println("Got url: " + url);

      Request r = new Request.Builder().url(url).build();

      WebSocketCall call = WebSocketCall.create(httpClient, r);

      call.enqueue(new WebSocketListener() {
        public void onOpen(WebSocket webSocket, Response response) {
          WsCat.this.webSocket = webSocket;
          System.out.println("opened webscoket with " + url);
        }

        public void onFailure(IOException e, Response response) {
          System.err.println(e);
          doExit.set(true);
        }

        public void onMessage(ResponseBody message) throws IOException {
          System.out.println("payload " + message.string());
        }

        public void onPong(Buffer payload) {

        }

        public void onClose(int code, String reason) {
          System.out.println("closed webscoket with " + url);
          WsCat.this.webSocket = null;
          doExit.set(true);
        }
      });

      Scanner scanner = new Scanner(new InputStreamReader(System.in));

      while (!doExit.get()) {
        String msg = scanner.nextLine();

        if (webSocket != null) {

          RequestBody body = RequestBody.create(WebSocket.TEXT, ByteString.of(msg.getBytes()));
          webSocket.sendMessage(body);
        }
      }

    } catch (CmdLineException e) {
      System.err.println(e);
    }
  }
}
