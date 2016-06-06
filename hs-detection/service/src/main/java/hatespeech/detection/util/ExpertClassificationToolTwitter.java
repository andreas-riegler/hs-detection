package hatespeech.detection.util;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCTwitterDAO;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.Tweet;
import hatespeech.detection.model.TweetImage;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit.CutAction;

public class ExpertClassificationToolTwitter extends JFrame{

	
	private static final long serialVersionUID = 1L;
	private JLabel messageLabel;
	private JPanel controlPanel,imagePanel;
	private List<Tweet> tweetList;
	private int currentPostId;
	private JDBCTwitterDAO jdbcTw;

	public ExpertClassificationToolTwitter(int anz)
	{
		initGUI();
		jdbcTw=new JDBCTwitterDAO();
		tweetList=jdbcTw.getUnclassifiedTweetsRange(anz);
		currentPostId=0;
	}
	
	private void initGUI() {
		setTitle("Expertenklassifizierer");
        setSize(600, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));
        
        messageLabel = new JLabel("", JLabel.CENTER);
        
        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        imagePanel = new JPanel();
        imagePanel.setLayout(new FlowLayout());

        this.add(messageLabel);
        this.add(imagePanel);
        this.add(controlPanel);
        this.setVisible(true);  
		
	}
	private void initializeClassification() {
		
		messageLabel.setText("<html><center>"+getLabelText(currentPostId)+"</center></html>"); 
		
		int i=0;
		for(TweetImage twImages: tweetList.get(currentPostId).getTwImages())
		{
			JButton imgButton=new JButton("IMG"+i);
			
			imgButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					try {
						BufferedImage image = ImageIO.read(new File(
								twImages.getUrl()));

						JLabel lbl = new JLabel(new ImageIcon(image));
						JOptionPane.showMessageDialog(null, lbl, "ImageDialog",
								JOptionPane.PLAIN_MESSAGE, null);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
	         imagePanel.add(imgButton);
	         i++;
		}
		JButton yesButton = new JButton("JA");
		yesButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {     
	        	 jdbcTw.updateResult(tweetList.get(currentPostId).getTweetid(), 1);
	        	 actualizeText();
	         }
	      });
		JButton noButton = new JButton("NEIN");
		noButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {     
	        	 jdbcTw.updateResult(tweetList.get(currentPostId).getTweetid(), 0);
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
		if(currentPostId<tweetList.size())
			return tweetList.get(currentPostId).getContent();
		else
		{
			this.remove(controlPanel);
			this.revalidate();
			this.repaint();
			return "Fertig! Sehr Brav!";
		}
			
	}

	public static void main(String[] args) {
		if(args.length==1)
		{
			ExpertClassificationToolTwitter exptClass=new ExpertClassificationToolTwitter(Integer.parseInt(args[0]));
			exptClass.initializeClassification();
		}
		else
		{
			System.err.println("RowId-Range not set!");
		}

	}
	

}
