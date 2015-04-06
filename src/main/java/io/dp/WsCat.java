package io.dp;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ws.WebSocket;
import com.squareup.okhttp.ws.WebSocketCall;
import com.squareup.okhttp.ws.WebSocketListener;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;

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
        @Override
        public void onOpen(WebSocket webSocket, Request request, Response response)
            throws IOException {
          WsCat.this.webSocket = webSocket;
          System.out.println("opened webscoket with " + url);
        }

        @Override
        public void onMessage(BufferedSource payload, WebSocket.PayloadType type)
            throws IOException {
          System.out.println("payload " + payload.readUtf8());
        }

        @Override
        public void onPong(Buffer payload) {

        }

        @Override
        public void onClose(int code, String reason) {
          System.out.println("closed webscoket with " + url);
          WsCat.this.webSocket = null;
          doExit.set(true);
        }

        @Override
        public void onFailure(IOException e) {
          System.err.println(e);
          doExit.set(true);
        }
      });

      Scanner scanner = new Scanner(new InputStreamReader(System.in));

      while (!doExit.get()) {
        String msg = scanner.nextLine();

        if (webSocket != null) {
          InputStream is = new ByteArrayInputStream(msg.getBytes());
          webSocket.sendMessage(WebSocket.PayloadType.TEXT, Okio.buffer(Okio.source(is)).buffer());
        }
      }

    } catch (CmdLineException e) {
      System.err.println(e);
    }
  }
}
