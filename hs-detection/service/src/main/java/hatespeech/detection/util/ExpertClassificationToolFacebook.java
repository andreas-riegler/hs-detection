package hatespeech.detection.util;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.model.FBComment;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit.CutAction;

public class ExpertClassificationToolFacebook extends JFrame{

	
	private static final long serialVersionUID = 1L;
	private JLabel messageLabel;
	private JPanel controlPanel;
	private List<FBComment> commentList;
	private int currentPostId;
	private JDBCFBCommentDAO jdbcComm;

	public ExpertClassificationToolFacebook(int min,int max)
	{
		initGUI();
		jdbcComm=new JDBCFBCommentDAO();
		commentList=jdbcComm.getUnclassifiedFBCommentsRange(min, max);
		currentPostId=0;
	}
	
	private void initGUI() {
		setTitle("Expertenklassifizierer");
        setSize(600, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(2, 1));
        
        messageLabel = new JLabel("", JLabel.CENTER);
        
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        this.add(messageLabel);
        this.add(controlPanel);
        this.setVisible(true);  
		
	}
	private void initializeClassification() {
		
		messageLabel.setText("<html><center>"+getLabelText(currentPostId)+"</center></html>"); 

		JButton yesButton = new JButton("JA");
		yesButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {     
	        	 jdbcComm.updateResult(commentList.get(currentPostId).getId(), 1);
	        	 actualizeText();
	         }
	      });
		JButton noButton = new JButton("NEIN");
		noButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {     
	        	 jdbcComm.updateResult(commentList.get(currentPostId).getId(), 0);
	        	 actualizeText();
	         }
	      }); 
		JButton nextButton = new JButton("Weiter");
		nextButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {     
	        	 actualizeText();
	         }
	      });

	      controlPanel.add(yesButton);
	      controlPanel.add(noButton);
	      controlPanel.add(nextButton);       

	      this.setVisible(true); 
		
	}
	
	private void actualizeText() {
		System.out.println(getLabelText(currentPostId));
		currentPostId++;
		
		messageLabel.setText("<html><center>"+getLabelText(currentPostId)+"</center></html>");
		
	}

	private String getLabelText(int currentPostId) {
		if(currentPostId<commentList.size())
			return commentList.get(currentPostId).getMessage();
		else
		{
			this.remove(controlPanel);
			this.revalidate();
			this.repaint();
			return "Fertig! Sehr Brav!";
		}
			
	}

	public static void main(String[] args) {
		if(args.length==2)
		{
			ExpertClassificationToolFacebook exptClass=new ExpertClassificationToolFacebook(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
			exptClass.initializeClassification();
		}
		else
		{
			System.err.println("RowId-Range not set!");
		}

	}
	

}
