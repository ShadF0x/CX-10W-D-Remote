package org.cyanotic.cx10.common;

import com.sun.net.httpserver.HttpServer;
import org.cyanotic.cx10.io.controls.Controller;
import org.cyanotic.cx10.io.controls.IController;
import org.cyanotic.cx10.io.video.FFMpegProcessVideoEncoder;
import org.cyanotic.cx10.io.video.FFPlayProcessVideoPlayer;
import org.cyanotic.cx10.io.video.IVideoEncoder;
import org.cyanotic.cx10.io.video.IVideoPlayer;
import org.cyanotic.cx10.net.CX10NalDecoder;
import org.cyanotic.cx10.net.CommandConnection;
import org.cyanotic.cx10.net.Heartbeat;
import org.cyanotic.cx10.net.TransportConnection;
import org.cyanotic.cx10.utils.ByteUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cyanotic on 28/11/2016.
 */
public class CX10 {
    public static final String HOST = "172.16.10.1";

    private TransportConnection transportConnection;
    private Controller controller;
    private IVideoPlayer previewPlayer;
    private IVideoEncoder recorder;
    private Heartbeat heartbeat;
    private OutputStream ffplayOutput;
    private OutputStream fileOutput;
    private OutputStream ffmpegOutput;
    private Socket ffplaySocket;
    private Socket ffmpegSocket;
    private CX10NalDecoder decoder;
    private File tempFile;

    public void connect() throws IOException {
        if (transportConnection != null) {
            transportConnection.disconnect();
        }
        transportConnection = new TransportConnection(HOST, 8888);
        transportConnection.connect();
        transportConnection.setName("Transport Connection");
        transportConnection.sendMessage(ByteUtils.loadMessageFromFile("message1.bin"), 106);
        transportConnection.sendMessage(ByteUtils.loadMessageFromFile("message2.bin"), 106);
        transportConnection.sendMessage(ByteUtils.loadMessageFromFile("message3.bin"), 170);
        transportConnection.sendMessage(ByteUtils.loadMessageFromFile("message4.bin"), 106);
        transportConnection.sendMessage(ByteUtils.loadMessageFromFile("message5.bin"), 106);
        heartbeat = new Heartbeat(HOST, 8888);
        heartbeat.start();
    }

    public void disconnect() {
        if (heartbeat != null) {
            heartbeat.kill();
            try {
                heartbeat.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        stopControls();
        stopVideoStream();
        stopVideoRecorder();

        if (transportConnection != null) {
            try {
                transportConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startControls(IController inputDevice) throws IOException {
        stopControls();
        controller = new Controller(inputDevice, new CommandConnection(HOST, 8895));
        controller.start();
    }

    public void stopControls() {
        if (controller != null) {
            controller.kill();
            try {
                controller.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller = null;
        }
    }

    public void startVideoStream(String SO) throws IOException {
        stopVideoStream();
        previewPlayer = new FFPlayProcessVideoPlayer(SO);
        previewPlayer.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final InetAddress ffplay = InetAddress.getByName("localhost");
        ffplaySocket = new Socket(ffplay, 8889);
        ffplayOutput = new BufferedOutputStream(ffplaySocket.getOutputStream());
        startVideo();
    }

    public void stopVideoStream() {
        if (previewPlayer != null) {
            previewPlayer.stop();
            previewPlayer = null;
        }

        if (ffplayOutput != null && ffplaySocket != null) {
            try {
                ffplayOutput.close();
                ffplaySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ffplayOutput = null;
            ffplaySocket = null;
        }
    }

    public void startVideoRecorder() throws IOException {
        stopVideoRecorder();
        recorder = new FFMpegProcessVideoEncoder();
        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        recorder.setFileName("output-" + timestamp + ".mp4");
        recorder.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final InetAddress ffmpeg = InetAddress.getByName("localhost");
        ffmpegSocket = new Socket(ffmpeg, 8890);
        ffmpegOutput = new BufferedOutputStream(ffmpegSocket.getOutputStream());
        startVideo();

    }

    public void stopVideoRecorder() {
        if (recorder != null) {
            recorder.stop();
            recorder = null;
        }
        if (ffmpegOutput != null && ffmpegSocket != null) {
            try {
                ffmpegOutput.close();
                ffmpegSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ffmpegOutput = null;
            ffmpegSocket = null;
        }
    }

    private void startVideo() throws IOException {
        if (decoder != null) {
            return;
        }

        decoder = new CX10NalDecoder(HOST, 8888);
        decoder.connect();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/page", httpExchange -> {
            byte[] response = ByteUtils.loadMessageFromFile("videopage.html");
            System.out.println("Page Requested");
            httpExchange.sendResponseHeaders(200, response.length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(response);
            os.close();
        });
        server.createContext("/video.mp4", httpExchange -> {
            httpExchange.sendResponseHeaders(200, -1);
            System.out.println("Video requested");
            while (true) {
                System.out.print(".");
                byte[] buffer = new byte[4096];
                decoder.read(buffer);
                httpExchange.getResponseBody().write(buffer);
            }
        });
        server.setExecutor(null); // creates a default executor
        server.start();
        try {
            java.awt.Desktop.getDesktop().browse(new URI("http://localhost:8000/page"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
