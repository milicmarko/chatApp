package main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.swing.MigLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Client extends JFrame implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 621762080549421636L;
	private JTextField txtText;
	private JTextField txtUnesiteUsername;
	private JTextArea textArea;
	private JButton btnPosalji;
	private JButton btnLogin;

	private static int port = 1433;

	private static Socket sock;
	private static BufferedReader in;
	private static PrintWriter out;

	class Task extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {

			posalji();

			return null;
		}

		@Override
		protected void done() {
			super.done();
		}
	}
	/*Salje poruku serveru*/
	public void posalji() {
		out.println(txtUnesiteUsername.getText() + ": " + txtText.getText());
	}

	public static void connect() {
		try {
			sock = new Socket("localhost", port);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Client() {
		setSize(new Dimension(375, 300));
		setTitle("ChatApp");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new MigLayout("", "[grow][]", "[34.00][grow][]"));

		txtUnesiteUsername = new JTextField();
		txtUnesiteUsername.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				txtUnesiteUsername.setText("");
			}
		});
		txtUnesiteUsername.setText("Unesite username");
		getContentPane().add(txtUnesiteUsername, "cell 0 0,growx");
		txtUnesiteUsername.setColumns(10);
		txtUnesiteUsername.addActionListener(this);

		btnLogin = new JButton("Login");
		getContentPane().add(btnLogin, "cell 1 0");
		btnLogin.addActionListener(this);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, "cell 0 1,grow");

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		txtText = new JTextField();
		txtText.setEnabled(false);
		getContentPane().add(txtText, "cell 0 2,grow");
		txtText.setColumns(10);

		btnPosalji = new JButton("Posalji");
		btnPosalji.setEnabled(false);
		getContentPane().add(btnPosalji, "cell 1 2");
		btnPosalji.addActionListener(this);
	}

	public static void createGUI() {
		Client frame = new Client();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		createGUI();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnLogin) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						connect();
						out.println(txtUnesiteUsername.getText());
						String username = in.readLine();

						username.trim();
						if (!username.equals("uspesno")) {
							
							JOptionPane.showMessageDialog(Client.this, "Korisnicko ime je zauzeto, pokusajte ponovo.");
							
						} else {
							JOptionPane.showMessageDialog(Client.this, "Uspesno konektovan.");
							btnPosalji.setEnabled(true);
							txtText.setEnabled(true);
							txtUnesiteUsername.setEnabled(false);
							btnLogin.setEnabled(false);
							// String du = in.readLine().trim();
							// int duzina = du.charAt(0);

							// System.out.println(duzina);
							String por;
							while ((por = in.readLine()) != null) {
								textArea.append(por);
								textArea.append("\n");
								textArea.setCaretPosition(textArea.getText().length());
							}
							textArea.append("Istorija chat je prazna...");
						}

					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		if (e.getSource() == txtUnesiteUsername) {
			txtUnesiteUsername.setText("");

		}

		if (e.getSource() == btnPosalji) { 
			textArea.append("ja: " + txtText.getText());
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					posalji();					
				}
			}).start();
		}
	}

}
