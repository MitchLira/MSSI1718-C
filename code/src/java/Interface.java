import java.awt.EventQueue;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;


import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.SpinnerNumberModel;

public class Interface {

	private JFrame frame;
	private int numberOfGroups = 1;
	private JSpinner numberOfSimulations;
	private JRadioButton braess;
	private JRadioButton nobraess;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Interface window = new Interface();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Interface() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		ButtonGroup group = new ButtonGroup();
		
		JLabel lblNumberOfSimulations = new JLabel("Number of simulations:");
		lblNumberOfSimulations.setBounds(16, 21, 144, 14);
		frame.getContentPane().add(lblNumberOfSimulations);
		
		numberOfSimulations = new JSpinner();
		numberOfSimulations.setModel(new SpinnerNumberModel(new Integer(1), null, null, new Integer(1)));
		numberOfSimulations.setBounds(170, 18, 40, 20);
		frame.getContentPane().add(numberOfSimulations);
		
		JPanel panel = new JPanel();
		panel.setBounds(26, 46, 331, 158);
		frame.getContentPane().add(panel);
		
		JButton btnAddMore = new JButton("+ Add more");
		btnAddMore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				JLabel lblNumberOfAgents = new JLabel("Number of agents:");
				lblNumberOfAgents.setBounds(10, 39, 115, 14);
				JSpinner spinnerN1 = new JSpinner();
				spinnerN1.setModel(new SpinnerNumberModel(new Integer(50), null, null, new Integer(1)));

				spinnerN1.setBounds(332, 35, 46, 23);
				spinnerN1.setName("numberOfAgents" + numberOfGroups);
				JLabel lblTimeGainPercentage = new JLabel("Time gain percentage:");
				lblTimeGainPercentage.setBounds(203, 39, 128, 14);
				JSpinner spinnerN2 = new JSpinner();
				spinnerN2.setBounds(135, 35, 46, 23);
				spinnerN2.setModel(new SpinnerNumberModel(new Double(96), null, null, new Double(1)));
				spinnerN2.setName("percentage" + numberOfGroups);
				panel.add(lblNumberOfAgents);
				panel.add(spinnerN1);
				panel.add(lblTimeGainPercentage);
				panel.add(spinnerN2);				
				frame.revalidate();
                frame.repaint();
                numberOfGroups++;
                if(numberOfGroups == 2)
                	btnAddMore.setEnabled(false);
			}
		});
		btnAddMore.setBounds(102, 215, 89, 23);
		frame.getContentPane().add(btnAddMore);
		
		
		
		braess = new JRadioButton("Braess Network");
		braess.setSelected(true);
		group.add(braess);
		
		JLabel label = new JLabel("");
		
		JLabel label_1 = new JLabel("");
		
		JLabel label_2 = new JLabel("");
		
		JLabel label_3 = new JLabel("");
		
		JLabel label_4 = new JLabel("");
		
		JSpinner spinner_2 = new JSpinner();
		spinner_2.setModel(new SpinnerNumberModel(new Double(96), null, null, new Double(1)));
		spinner_2.setBounds(332, 35, 46, 23);

		spinner_2.setName("percentage0");
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel.add(braess);
		panel.add(label);
		panel.add(label_1);
		panel.add(label_2);
		panel.add(label_3);
		
		nobraess = new JRadioButton("No Braess Network");
		group.add(nobraess);
		panel.add(nobraess);
		panel.add(label_4);
		
		JLabel lblNumberOfAgents = new JLabel("Number of agents:");
		panel.add(lblNumberOfAgents);
		
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Integer(50), null, null, new Integer(1)));
		spinner.setBounds(332, 35, 46, 23);
		spinner.setName("numberOfAgents0");
		panel.add(spinner);
		
		JLabel lblTimeGainPercentage = new JLabel("Time gain percentage:");
		panel.add(lblTimeGainPercentage);
		panel.add(spinner_2);
		
		JButton btnNewButton = new JButton("Run");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String agent = "";
				for(int i = 0; i < panel.getComponents().length; i++) {
					if(panel.getComponents()[i] instanceof JSpinner) {
						if (panel.getComponents()[i].getName().contains("numberOfAgents") ) {
							agent += ((JSpinner)panel.getComponents()[i]).getValue() + "-";
						}
						else {
							agent += ((double)((JSpinner)panel.getComponents()[i]).getValue())/100 + ",";
						}
					}
				}
				agent = agent.substring(0, agent.length()-1);
				App.agent = agent;
				if(braess.isSelected())
					App.mode = "--braess";
				else
					App.mode = "--no-braess";
				App.numberOfSimulations = (int) numberOfSimulations.getValue();
				String[] args = new String[1];
				args[0] = "bdi.mas2j";
				Runnable r = new Runnable() {
				    @Override
				    public void run() {
				    	try {
							App.main(args);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }
				};

				Thread thread = new Thread(r);
				thread.start();
				frame.dispose();

			
				
			}
		});
		btnNewButton.setBounds(231, 215, 89, 23);
		frame.getContentPane().add(btnNewButton);
	}
}
