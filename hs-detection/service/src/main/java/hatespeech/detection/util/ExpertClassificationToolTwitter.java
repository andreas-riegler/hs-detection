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

	public ExpertClassificationToolTwitter(String min,String max)
	{
		initGUI();
		jdbcTw=new JDBCTwitterDAO();
		tweetList=jdbcTw.getUnclassifiedTweetsRange("751748094570526208", "751748294570526208");
		//tweetList=jdbcTw.getRandomUnclassifiedTweetsContainingWordByCount(400, "vieh");
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
		actualizeImages();
		
		JButton hateButton = new JButton("Hassrede");
		hateButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {     
	        	 jdbcTw.updateResult(tweetList.get(currentPostId).getTweetid(), 1);
	        	 actualizeText();
	         }
	      });
		JButton insultButton = new JButton("Beleidigung");
		insultButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {     
	        	 jdbcTw.updateResult(tweetList.get(currentPostId).getTweetid(), 2);
	        	 actualizeText();
	         }
	      });
		JButton otherButton = new JButton("Sonstiges");
		otherButton.addActionListener(new ActionListener() {
	         public void actionPerformed(ActionEvent e) {     
	        	 jdbcTw.updateResult(tweetList.get(currentPostId).getTweetid(), 3);
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

		  controlPanel.add(hateButton);
	      controlPanel.add(insultButton);
	      controlPanel.add(otherButton);
	      controlPanel.add(noButton);
	      controlPanel.add(nextButton);       

	      this.setVisible(true); 
		
	}
	
	private void actualizeText() {
		
		imagePanel.removeAll();
		this.revalidate();
		this.repaint();
		
		currentPostId++;
		System.out.println(currentPostId+": "+getLabelText(currentPostId));
		
		messageLabel.setText("<html><center>"+getLabelText(currentPostId)+"</center></html>");
		actualizeImages();
	}
	private void actualizeImages()
	{
		int i=0;
		if(currentPostId<tweetList.size())
		{
			for (TweetImage twImages : tweetList.get(currentPostId)
					.getTwImages()) {
				JButton imgButton = new JButton("IMG" + i);

				imgButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

						try {
							BufferedImage image = ImageIO.read(new File(
									twImages.getUrl()));
							System.out.println(twImages.getUrl());
							JLabel lbl = new JLabel(new ImageIcon(image));
							JOptionPane.showMessageDialog(null, lbl,
									"ImageDialog", JOptionPane.PLAIN_MESSAGE,
									null);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				});
				imagePanel.add(imgButton);
				i++;
			}
		}
	}
	private String getLabelText(int currentPostId) {
		if(currentPostId<tweetList.size())
			return tweetList.get(currentPostId).getMessage();
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
			ExpertClassificationToolTwitter exptClass=new ExpertClassificationToolTwitter(args[0],args[1]);
			exptClass.initializeClassification();
		}
		else
		{
			System.err.println("RowId-Range not set!");
		}

	}
	

}
