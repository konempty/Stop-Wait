import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Receiver extends JFrame {
    static JTextArea textArea = new JTextArea(20, 20);
    static JScrollPane scrollPane = new JScrollPane(textArea);
    static int before_len = 0;

    Receiver() {
        super("Receiver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = getContentPane();
        content.setLayout(new FlowLayout());

        textArea.setEditable(false);
        content.add(scrollPane);

        setSize(300, 380);
        setVisible(true);
    }

    public static void main(String a[]) {
        new Receiver();
        Thread thread = new Thread(new MyRunnableR());
        thread.run();


        ////////////////

    }

    static class MyRunnableR implements Runnable {
        int ServerPort = 1216;

        @Override
        public void run() {

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(ServerPort);
                InetAddress ip = InetAddress.getLocalHost();
                textArea.append(ip.getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    System.out.println("S: Connecting...");
                    Socket client = serverSocket.accept();
                    String address = client.getInetAddress().toString();
                    textArea.setText("");
                    textArea.append("Receive from " + address + "\n");
                    int i, j;
                    i = 0;
                    while (address.charAt(i) < '0' || address.charAt(i) > '9')
                        i++;
                    int[] addresses = {0, 0, 0, 0};
                    j = 0;
                    for (i = 0; i < address.length(); i++) {
                        if (address.charAt(i) == '.') {
                            i++;
                            j++;
                        }
                        addresses[j] =  addresses[j] * 10 + address.charAt(i) - '0';
                    }
                    String[] strings = {"Received Error", "Received OK"};

                    Frame[] frames = Frame.makeFrame(addresses, strings);

                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    Random random = new Random();
                    i = 0;
                    while (true) {
                        try {
                            String str = in.readLine(), data;
                            data = Frame.Decode(str);
                            if (data == null) {
                                textArea.append("\nError Detected\n\n");
                                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                                Thread.sleep(random.nextInt(1000) + 500);
                                out.println(frames[0].toString());
                                out.flush();
                                before_len = textArea.getText().length();
                                continue;
                            }

                            System.out.println("S: Received: '" + data + "'");
                            if (!Frame.isNextFrame(Frame.getDecoded(str))) {
                                textArea.replaceRange("//" + data + "\n", before_len, textArea.getText().length());
                                i--;
                            }
                            textArea.append(String.format("%02d : ", i + 1) + data + "\n");
                            before_len = textArea.getText().length();
                            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                            String send_data = frames[1].toString();
                            if (random.nextInt(10) > 1) {
                                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                                Thread.sleep(random.nextInt(1000) + 500);
                                out.println(send_data);
                                out.flush();
                            } else if (random.nextBoolean()) {
                                textArea.append("\n!!!!!!!ACK ERROR!!!!!!!\n\n");
                                StringBuilder stringBuilder = new StringBuilder(send_data);
                                stringBuilder.insert(random.nextInt(send_data.length() - 88) + 48, "1");
                                out.println(stringBuilder.toString());
                                out.flush();
                                before_len = textArea.getText().length();
                                if (i == 26)
                                    data = "1";
                            } else {
                                textArea.append("\n!!!!!!!ACK LOST!!!!!!!\n\n");
                                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                                Thread.sleep(random.nextInt(1000) + 500);
                                before_len = textArea.getText().length();
                                if (i == 26)
                                    data = "1";
                            }
                            if (data.equals("0"))
                                break;
                        } catch (Exception e) {
                            System.out.println("S: Error");
                            e.printStackTrace();
                        }
                        i++;
                    }
                    textArea.append("////Finish////\n");
                    scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                    System.out.println("S: Done.");
                    in.close();
                    out.close();
                    client.close();
                } catch (Exception e) {
                    System.out.println("S: Error");
                    e.printStackTrace();
                }

            }
        }
    }
}
