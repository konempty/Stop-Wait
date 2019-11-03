import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Sender extends JFrame {
    String ServerIP;
    int[] Address;
    JTextArea textArea = new JTextArea(20, 20);
    JScrollPane scrollPane = new JScrollPane(textArea);

    Sender() {
        super("Sender");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = getContentPane();
        content.setLayout(new FlowLayout());
        content.add(new JLabel("IP : "));
        JFormattedTextField ftf1 = new JFormattedTextField(new IPAddressFormatter());
        ftf1.setColumns(20);
        content.add(ftf1);
        JButton button = new JButton("시작");
        button.addActionListener(e -> {
            Address = (int[]) ftf1.getValue();
            if (Address != null) {
                ServerIP = String.format("%d.%d.%d.%d", Address[0], Address[1], Address[2], Address[3]);
                Thread thread = new Thread(new MyRunnableS());
                thread.start();
            }
        });
        content.add(button);
        textArea.setEditable(false);
        content.add(scrollPane);

        setSize(300, 440);
        setVisible(true);
    }

    public static void main(String a[]) {
        new Sender();
    }


    class MyRunnableS implements Runnable {
        int ServerPort = 1216;

        @Override
        public void run() {
            try {
                System.out.println("S: Connecting...");
                Socket client = new Socket(ServerIP, ServerPort);
                PrintWriter out = new PrintWriter(client.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String[] datas = Frame.getData("Data.txt");
                Frame[] frames = Frame.makeFrame(Address, datas);
                Random random = new Random();
                System.out.println("S: Receiving...");
                int i = 0;
                textArea.append("////start////\n");
                while (true) {
                    try {
                        textArea.append(String.format("%02d : ", i + 1) + datas[i] + "\n");
                        textArea.append(frames[i].toString() + "\n");
                        if (random.nextInt(10) > 0) {
                            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                            Thread.sleep(random.nextInt(1000) + 500);
                            String send_data = frames[i].toString();
                            if (random.nextInt(9) > 0) {
                                out.println(send_data);
                                out.flush();
                            } else {
                                textArea.append("\n!!!!!!!DATA ERROR!!!!!!!\n\n");
                                StringBuilder stringBuilder = new StringBuilder(send_data);
                                stringBuilder.insert(random.nextInt(send_data.length() - 88) + 48, "1");
                                out.println(stringBuilder.toString());
                                out.flush();
                            }
                            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                        } else {
                            textArea.append("\n!!!!!!!DATA LOST!!!!!!!\n\n");
                            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                            Thread.sleep(random.nextInt(1000) + 500);
                        }
                        try {
                            client.setSoTimeout(1600);
                            String ack = in.readLine();
                            String data = Frame.Decode(ack);
                            if (data==null) {
                                textArea.append("\nERROR Detected\n\n");
                                continue;
                            }
                            if (data.contains("Received Error")) {
                                textArea.append("\n!!!!!!!ERROR!!!!!!!\n\n");
                                i--;
                            }
                        } catch (Exception e) {
                            textArea.append("\nTime Out!!! Retransmission.\n\n");
                            i--;
                        }
                        if (++i >= frames.length)
                            break;
                    } catch (Exception e) {
                        System.out.println("S: Error");
                        e.printStackTrace();
                    }
                }
                int len1 = textArea.getText().length(), len2;
                textArea.append("////Finish////");
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                len2 = textArea.getText().length();
                System.out.println("S: Done.");
                in.close();
                out.close();
                client.close();
            } catch (Exception e) {
                System.out.println("S: Error2");
                e.printStackTrace();
            }

        }


    }

}
