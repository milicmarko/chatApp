package main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

public class Server extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8676733392220278537L;
	private JTextArea textArea;
	private JButton btnPokreni;
	private JButton btnZaustavi;

	private int port = 1433;
	private volatile boolean zaustavi = false;
	private ServerSocket ss;
	private Socket s;
	private Task task;
	private volatile boolean nema = false;

	private List<String> poruke;
	private HashSet<String> korisnici;
	private Map<String, PrintWriter> poslati;

	class Task extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			try {
				korisnici = new HashSet<String>();
				poslati = new HashMap<String, PrintWriter>();
				poruke = new ArrayList<String>();


				ss = new ServerSocket(port);
				textArea.append("Server pokrenut...\n");

				korisnici.add("pero"); // dodat korisnik
				poruke.add("Marko: Kako je?");
				poruke.add("Bogdan: Nije lose. Sta ima?");

				while (true) {
					s = ss.accept();
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
								PrintWriter out = new PrintWriter(s.getOutputStream(), true);


								String username = in.readLine().trim();


								if (!korisnici.contains(username)) {

									out.println("uspesno");

									korisnici.add(username);
									System.out.println(username);
									poslati.put(username, out);
									for (String string : korisnici) {
										System.out.println(string);
									}
									textArea.append("Korisnik " + username + " konektovan...\n");

									System.out.println(poruke.size());
									//slanje istorije poruka
									if (poruke.size() > 0) {
										for (String poruka : poruke) {
											out.println(poruka);
											System.out.println(poruka);
										}
										textArea.append("Istorija poruka poslata klijentu: " + username + "\n");
									}



									while (true) {
										String pristigla = in.readLine();
										poruke.add(pristigla);

										if (!korisnici.contains(username)) {
											obavesti(username, pristigla);
										}
										if (s.isClosed()) {
											poslati.remove(out);
										}
									}
								} else {
									out.println("neuspesno");
									//zaustavi = true;
								}
							} catch (IOException e) {
								textArea.setText("Greska pri pokretanju servera...\n");
								e.printStackTrace();
							}
						}

					}).start();
				}
			} catch (IOException e) {
				textArea.setText("Greska pri pokretanju servera...\n");
				e.printStackTrace();
			} finally {
				ss.close();
			}
			return null;
		}
	}

	public void obavesti(String korisnik, String poruka) {
		PrintWriter pw = poslati.get(korisnik);
		pw.println(poruka);
	}

	public Server() {
		setSize(new Dimension(450, 300));
		setTitle("ChatServer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new MigLayout("", "[280.00][51.00][]", "[grow][]"));

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, "cell 0 0 2 1,grow");

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		btnPokreni = new JButton("Pokreni");
		getContentPane().add(btnPokreni, "cell 0 1");
		btnPokreni.addActionListener(this);

		btnZaustavi = new JButton("Zaustavi");
		btnZaustavi.setEnabled(false);
		getContentPane().add(btnZaustavi, "cell 1 1");
		btnZaustavi.addActionListener(this);
	}

	public static void main(String[] args) {
		Server frame = new Server();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnPokreni) {
			zaustavi = false;
			btnPokreni.setEnabled(false);
			btnZaustavi.setEnabled(true);
			task = new Task();
			task.execute();
		}
		if (e.getSource() == btnZaustavi) {
			btnPokreni.setEnabled(true);
			task.cancel(true);
			textArea.append("Server zaustavljen...\n");
			try {
				ss.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//zaustavi = true;
			btnZaustavi.setEnabled(false);
		}
	}

}
