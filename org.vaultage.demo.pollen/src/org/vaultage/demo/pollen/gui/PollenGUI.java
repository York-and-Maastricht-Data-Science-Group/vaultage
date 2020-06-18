package org.vaultage.demo.pollen.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.vaultage.core.VaultageMessage;
import org.vaultage.core.VaultageServer;
import org.vaultage.demo.pollen.NumberPoll;
import org.vaultage.demo.pollen.SendNumberPollRequestBaseHandler;
import org.vaultage.demo.pollen.SendNumberPollRequestHandler;
import org.vaultage.demo.pollen.SendNumberPollResponseHandler;
import org.vaultage.demo.pollen.User;
import org.vaultage.demo.pollen.util.PollenUtil;

public class PollenGUI extends JFrame {

	private JPanel contentPane;

	public static User user;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
//					System.out.println("Working Directory = " + System.getProperty("user.dir"));
					
					String name = args[0];

					PollenGUI frame = new PollenGUI();
					frame.setVisible(true);

					frame.setTitle(frame.getTitle() + " - " + name);
					frame.setLocationRelativeTo(null);

					initialise();
					
					user.setId(name);
					user.setName(name);
					PollenUtil.savePublicKey(user);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void initialise() throws Exception {
////		String address = "vm://localhost";
//		String address = "tcp://localhost:61616";
//
//		final VaultageServer pollenBroker = new VaultageServer(address);
//
//		user = new User();
//		
//
//		user.setSendNumberPollRequestBaseHandler(new GUISendNumberPollRequestHandler() {
//			@Override
//			public double run(VaultageMessage senderMessage, NumberPoll poll) throws Exception {
//				User localVault = (User) this.vault;
//				double total = localVault.sendNumberPoll(senderMessage.getFrom(), poll);
//				double mySalary = 0;
//				localVault.addPollRealValue(poll.getId(), mySalary);
//				result = total + mySalary;
//				return (double) result;
//			}
//		});
//		user.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());
//
//		System.out.println("IsConnected = " + user.register(pollenBroker));
	}

	/**
	 * Create the frame.
	 * @throws Exception 
	 */
	public PollenGUI() throws Exception {
		setResizable(false);
		setTitle("Pollen");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 564, 359);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblMyPolls = new JLabel("My Polls");
		lblMyPolls.setBounds(10, 11, 83, 14);
		contentPane.add(lblMyPolls);

		JLabel lblOtherPolls = new JLabel("Other Polls");
		lblOtherPolls.setBounds(10, 137, 83, 14);
		contentPane.add(lblOtherPolls);

		JList listOtherPolls = new JList();
		listOtherPolls.setModel(new DefaultListModel<>());
		listOtherPolls.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOtherPolls.setSelectedIndices(new int[] { 0 });
		listOtherPolls.setBorder(new LineBorder(new Color(0, 0, 0)));
		listOtherPolls.setBounds(10, 162, 179, 143);
		contentPane.add(listOtherPolls);

		JList listMyPolls = new JList();
		listMyPolls.setModel(new DefaultListModel<>());
		listMyPolls.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listMyPolls.setSelectedIndices(new int[] { 0 });
		listMyPolls.setBorder(new LineBorder(new Color(0, 0, 0)));
		listMyPolls.setBounds(10, 36, 179, 79);
		contentPane.add(listMyPolls);

		JLabel lblQuestion = new JLabel("Question:");
		lblQuestion.setBounds(224, 11, 254, 14);
		contentPane.add(lblQuestion);

		JTextPane textPaneQuestion = new JTextPane();
		textPaneQuestion.setText("How much is your salary?");
		textPaneQuestion.setBackground(Color.WHITE);
		textPaneQuestion.setBounds(224, 36, 314, 79);
		contentPane.add(textPaneQuestion);

		JLabel lblAnswer = new JLabel("Answer:");
		lblAnswer.setBounds(223, 126, 151, 14);
		contentPane.add(lblAnswer);

		JSpinner spinnerAnswer = new JSpinner();
		spinnerAnswer.setModel(new SpinnerNumberModel(new Double(100), new Double(0), null, new Double(1)));
		spinnerAnswer.setBounds(224, 151, 150, 20);
		contentPane.add(spinnerAnswer);

		JButton btnAnswer = new JButton("Answer");
		btnAnswer.setBounds(224, 194, 89, 23);
		contentPane.add(btnAnswer);

		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					NumberPoll poll = new NumberPoll();
					poll.setQuestion(textPaneQuestion.getText());
					poll.setOriginator(user.getPublicKey());
					List<String> participants = PollenUtil.getParticipants(user.getName());
					poll.setParticipants(participants);
					
					
					DefaultListModel<String> model = (DefaultListModel<String>) listMyPolls.getModel();
					model.addElement(poll.getId());
					
					user.getRemoteRequester().requestSendNumberPoll(participants.get(0), poll);
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		btnCreate.setBounds(449, 194, 89, 23);
		contentPane.add(btnCreate);

		JSpinner spinnerResult = new JSpinner();
		spinnerResult.setModel(new SpinnerNumberModel(new Double(100), null, null, new Double(1)));
		spinnerResult.setBounds(384, 151, 154, 20);
		contentPane.add(spinnerResult);

		JLabel lblResult = new JLabel("Result:");
		lblResult.setBounds(384, 126, 153, 14);
		contentPane.add(lblResult);
		
		// POLLEN
		String address = "tcp://localhost:61616";

		final VaultageServer pollenBroker = new VaultageServer(address);

		user = new User();

		user.setSendNumberPollRequestBaseHandler(new SendNumberPollRequestHandler(null) {
			@Override
			public double run(VaultageMessage senderMessage, NumberPoll poll) throws Exception {
				
				DefaultListModel<String> model = (DefaultListModel<String>) listMyPolls.getModel();
				model.addElement(poll.getId());
				
//				User localVault = (User) this.vault;
//				double total = localVault.sendNumberPoll(senderMessage.getFrom(), poll);
//				double mySalary = 0;
//				localVault.addPollRealValue(poll.getId(), mySalary);
//				result = total + mySalary;
//				return (double) result;
				return 0;
			}
		});
		user.getSendNumberPollRequestBaseHandler().isImmediatelyResponded(false);
		user.setSendNumberPollResponseBaseHandler(new SendNumberPollResponseHandler());

		System.out.println("IsConnected = " + user.register(pollenBroker));
	}
}
